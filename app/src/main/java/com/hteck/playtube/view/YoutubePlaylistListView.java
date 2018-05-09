package com.hteck.playtube.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubePlaylistByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.fragment.YoutubePlaylistVideosFragment;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;


public class YoutubePlaylistListView extends FrameLayout implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<YoutubePlaylistInfo> _playlists = new ArrayList<>();
    private YoutubePlaylistByPageAdapter _adapterPlaylist;
    private String _query;
    private LoadingView _busyView;
    public boolean mIsSearched;
    private boolean _isLoading = false;
    private String _channelId;
    private View _viewReload;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;

    public YoutubePlaylistListView(Context context) {
        super(context);

        addView(createView());
    }

    public YoutubePlaylistListView(Context context, String channelId) {
        super(context);
        _channelId = channelId;

        addView(createView());
    }

    public View createView() {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, null, false);

        _adapterPlaylist = new YoutubePlaylistByPageAdapter(getContext(), _playlists);
        _binding.listView.setAdapter(_adapterPlaylist);
        _binding.listView.setOnItemClickListener(this);
        _binding.listView.setOnScrollListener(this);

        return _binding.getRoot();
    }

    public void setDataSource(ArrayList<YoutubePlaylistInfo> playlists,
                              boolean isInit) {
        _binding.listView.setEmptyView(_binding.textViewMsg);
        if (isInit || playlists.size() > 0) {
            _binding.textViewMsg.setVisibility(View.GONE);
        } else {
            _binding.textViewMsg.setText(Utils.getString(
                    R.string.no_playlist_found));
            _binding.textViewMsg.setVisibility(View.VISIBLE);
        }
        _playlists = playlists;
        _adapterPlaylist.setDataSource(_playlists);
        _adapterPlaylist.notifyDataSetChanged();
    }

    public void search(String query) {
        if (Utils.stringIsNullOrEmpty(query)) {
            return;
        }
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        _playlists = new ArrayList<>();
        setDataSource(_playlists, true);
        _nextPageToken = "";
        _query = query;
        cancelAllRequests();

        search();
        showProgressBar();
    }

    private void cancelAllRequests() {
        if (_httpOk != null) {
            _httpOk.cancel();
        }
    }

    CustomCallback eventDownloadCompleted = new CustomCallback() {
        @Override
        public void onFailure(Request request, IOException e) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showMessage(Utils
                            .getString(R.string.network_error));
                    hideProgressBar();
                    _isLoading = false;
                    if (_playlists.size() == 0) {
                        initReloadEvent();
                    } else {
                        _adapterPlaylist.setIsNetworkError(true);
                        _adapterPlaylist.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onResponse(final Response response) throws IOException {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean isChannelPlaylists = !Utils
                                .stringIsNullOrEmpty(_channelId);
                        String s = response.body().string();
                        AbstractMap.SimpleEntry<String, ArrayList<YoutubePlaylistInfo>> searchResult = YoutubeHelper
                                .getPlaylists(s, isChannelPlaylists, false);
                        _nextPageToken = searchResult.getKey();
                        ArrayList<YoutubePlaylistInfo> playlists = searchResult.getValue();

                        if (playlists.size() == 0
                                || isChannelPlaylists) {
                            if (_playlists.size() > 0
                                    && _playlists.get(_playlists
                                    .size() - 1) == null) {
                                _playlists.remove(_playlists.size() - 1);
                            }
                            if (isChannelPlaylists) {
                                _playlists.addAll(playlists);
                                if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                    _playlists.add(null);
                                }
                            }
                            setDataSource(_playlists, false);
                            hideProgressBar();
                            _isLoading = false;
                        } else {
                            loadPlaylistsInfo(playlists);
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                        hideProgressBar();
                        _isLoading = false;
                    }

                }
            });
        }
    };

    private void search() {
        String url = String
                .format(PlayTubeController.getConfigInfo().searchPlaylistUrl, _nextPageToken,
                        Utils.urlEncode(_query));
        _httpOk = new CustomHttpOk(url, eventDownloadCompleted);
        _httpOk.start();
    }

    public void searchMore() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        search();
    }

    public void loadData() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;

        String url = String
                .format(PlayTubeController.getConfigInfo().loadPlaylistsInChannelUrl, _nextPageToken,
                        _channelId, Constants.PAGE_SIZE);
        _httpOk = new CustomHttpOk(url, eventDownloadCompleted);
        _httpOk.start();
        if (_playlists.size() == 0) {
            showProgressBar();
        }
    }

    private void loadMorePlaylistsInfo() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        ArrayList<YoutubePlaylistInfo> playlists = new ArrayList<>();
        int count = 0;
        for (YoutubePlaylistInfo p : _playlists) {
            if (Utils.stringIsNullOrEmpty(p.title)) {
                count++;
                playlists.add(p);
                if (count == Constants.PAGE_SIZE) {
                    break;
                }
            }
        }
        loadPlaylistsInfo(playlists);
    }

    private void loadPlaylistsInfo(ArrayList<YoutubePlaylistInfo> playlists) {
        String ids = "";
        for (YoutubePlaylistInfo playlistInfo : playlists) {
            if (ids == "") {
                ids = playlistInfo.id;
            } else {
                ids = ids + "," + playlistInfo.id;
            }
        }
        String url = String
                .format(PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl,
                        ids);
        _httpOk = new CustomHttpOk(url, new CustomCallback(playlists) {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            hideProgressBar();
                            _isLoading = false;
                            if (_playlists.size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapterPlaylist.setIsNetworkError(true);
                                _adapterPlaylist.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final ArrayList<YoutubePlaylistInfo> originPlaylists = (ArrayList<YoutubePlaylistInfo>) this.getDataContext();
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            String s = response.body().string();
                            if (Utils.isLoadMorePlaylists(_playlists)) {
                                YoutubeHelper.fillDataToPlaylists(s, _playlists);
                            } else {
                                ArrayList<YoutubePlaylistInfo> playlists = YoutubeHelper.getPlaylists(s,
                                        originPlaylists);

                                if (_playlists.size() > 0
                                        && _playlists.get(_playlists
                                        .size() - 1) == null) {
                                    _playlists.remove(_playlists.size() - 1);
                                }
                                _playlists.addAll(playlists);
                                if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                    _playlists.add(null);
                                }
                            }
                            setDataSource(_playlists, false);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideProgressBar();
                        _isLoading = false;

                    }
                });
            }
        });
        _httpOk.start();
    }

    private void hideProgressBar() {
        Utils.hideProgressBar(_binding.layoutMain, _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(_binding.layoutMain, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        int index = (int) arg3;
        if (index == _playlists.size() - 1 && _playlists.get(_playlists.size() - 1) == null) {
            if (_adapterPlaylist.getIsNetworkError()) {
                _adapterPlaylist.setIsNetworkError(false);
                _adapterPlaylist.notifyDataSetChanged();
                if (Utils.stringIsNullOrEmpty(_channelId)) {
                    searchMore();
                } else {
                    loadData();
                }
            }
        } else {
            YoutubePlaylistInfo playlistInfo = _playlists.get(index);
            if (playlistInfo == null) {
                return;
            }
            YoutubePlaylistVideosFragment playlistVideosFragment = YoutubePlaylistVideosFragment.newInstance(playlistInfo);
            MainActivity.getInstance().addFragment(playlistVideosFragment);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {


    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (_binding.listView.getLastVisiblePosition() == _binding.listView.getAdapter()
                    .getCount() - 1) {
                if (Utils.isLoadMorePlaylists(_playlists)
                        || (_playlists.size() > 0 && _playlists.get(_playlists.size() - 1) == null)) {
                    if (!_adapterPlaylist.getIsNetworkError()) {
                        if (Utils.isLoadMorePlaylists(_playlists)) {
                            loadMorePlaylistsInfo();
                        } else {
                            if (Utils.stringIsNullOrEmpty(_channelId)) {
                                searchMore();
                            } else {

                                loadData();
                            }
                        }
                    }
                }

            }
        }
    }

    private void initReloadEvent() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _viewReload = inflater.inflate(R.layout.retry_view, null);
        _binding.layoutMain.addView(_viewReload);
        _viewReload.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_viewReload != null) {
                    _binding.layoutMain.removeView(_viewReload);
                    _viewReload = null;
                }
                if (Utils.stringIsNullOrEmpty(_channelId)) {
                    search();
                } else {
                    loadData();
                }
                return false;
            }
        });
    }
}
