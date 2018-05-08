package com.hteck.playtube.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubePlaylistByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ListViewBinding;
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
    private ArrayList<YoutubePlaylistInfo> _playlistsSearching = new ArrayList<>();
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
        showBusyAnimation();
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
                    hideBusyAnimation();
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
                        _playlistsSearching = searchResult.getValue();

                        if (_playlistsSearching.size() == 0
                                || isChannelPlaylists) {
                            if (_playlists.size() > 0
                                    && _playlists.get(_playlists
                                    .size() - 1) == null) {
                                _playlists.remove(_playlists.size() - 1);
                            }
                            if (isChannelPlaylists) {
                                _playlists.addAll(_playlistsSearching);
                                if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                    _playlists.add(null);
                                }
                            }
                            setDataSource(_playlists, false);
                            hideBusyAnimation();
                            _isLoading = false;
                        } else {
                            loadPlaylistsInfo();
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                        hideBusyAnimation();
                        _isLoading = false;
                    }

                }
            });
        }
    };
//	IEventHandler() {
//
//		@Override
//		public void returnResult(Object sender, final ResultType resultType,
//				final byte[] data) {
//
//		}
//	};

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
                .format(PlayTubeController.getConfigInfo().loadPlaylistsInChannelUrl,_nextPageToken,
                        _channelId,  Constants.PAGE_SIZE);
        _httpOk = new CustomHttpOk(url, eventDownloadCompleted);
        _httpOk.start();
        if (_playlists.size() == 0) {
            showBusyAnimation();
        }
    }

    private void loadMorePlaylistsInfo() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        _playlistsSearching = new ArrayList<>();
        int count = 0;
        for (YoutubePlaylistInfo p : _playlists) {
            if (Utils.stringIsNullOrEmpty(p.title)) {
                count++;
                _playlistsSearching.add(p);
                if (count == Constants.PAGE_SIZE) {
                    break;
                }
            }
        }
        loadPlaylistsInfo();
    }

    private void loadPlaylistsInfo() {
        String ids = "";
        for (YoutubePlaylistInfo playlistInfo : _playlistsSearching) {
            if (ids == "") {
                ids = playlistInfo.id;
            } else {
                ids = ids + "," + playlistInfo.id;
            }
        }
        String url = String
                .format(PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl,
                        ids);
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Success) {
                            try {

                                String s = new String(data);
                                if (Utils.isLoadMorePlaylists(_playlists)) {
                                    YoutubeHelper.populatePlaylistsInfo(_playlists,
                                            s);
                                } else {
                                    YoutubeHelper.populatePlaylistsInfo(
                                            _playlistsSearching, s);

                                    if (_playlists.size() > 0
                                            && _playlists.get(_playlists
                                            .size() - 1) == null) {
                                        _playlists.remove(_playlists.size() - 1);
                                    }
                                    _playlists.addAll(_playlistsSearching);
                                    if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                        _playlists.add(null);
                                    }
                                }
                                setDataSource(_playlists, false);

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            hideBusyAnimation();
                            _isLoading = false;
                        }

                        if (resultType == ResultType.NetworkError) {
                            Utils.showMessageToast(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_playlists.size() == 0) {
                                initReloadEvent();
                            } else {
                                _isNetworkError = true;
                                _adapterPlaylist.mIsNetworkError = true;
                                _adapterPlaylist.notifyDataSetChanged();
                            }
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

                                String s = response.body().string();
                                if (Utils.isLoadMorePlaylists(_playlists)) {
                                    YoutubeHelper.getPlaylists().populatePlaylistsInfo(_playlists,
                                            s);
                                } else {
                                    YoutubeHelper.populatePlaylistsInfo(
                                            _playlistsSearching, s);

                                    if (_playlists.size() > 0
                                            && _playlists.get(_playlists
                                            .size() - 1) == null) {
                                        _playlists.remove(_playlists.size() - 1);
                                    }
                                    _playlists.addAll(_playlistsSearching);
                                    if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                        _playlists.add(null);
                                    }
                                }
                                setDataSource(_playlists, false);

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            hideBusyAnimation();
                            _isLoading = false;

                    }
                });
            }
        });
    }

    private void hideBusyAnimation() {
        Utils.hideBusyAnimation(_contentView, _busyView);
    }

    private void showBusyAnimation() {
        _busyView = Utils.showBusyAnimation(_contentView, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        int index = (int) arg3;
        if (index == _playlists.size() - 1 && _playlists.lastElement() == null) {
            if (_isNetworkError) {
                _isNetworkError = false;
                _adapterPlaylist.mIsNetworkError = false;
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
            YoutubePlaylistVideosView youtubePlaylistDetails = YoutubePlaylistVideosView
                    .newInstance(playlistInfo, VideoListType.Normal);
            MainActivity.getInstance().launchFragment(youtubePlaylistDetails);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (_listView.getLastVisiblePosition() == _listView.getAdapter()
                    .getCount() - 1) {
                if (Utils.isLoadMorePlaylists(_playlists)
                        || (_playlists.size() > 0 && _playlists.lastElement() == null)) {
                    if (!_isNetworkError) {
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
        _viewReload = (ViewGroup) inflater.inflate(R.layout.reload_view, null);
        _contentView.addView(_viewReload);
        _viewReload.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_viewReload != null) {
                    _contentView.removeView(_viewReload);
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
