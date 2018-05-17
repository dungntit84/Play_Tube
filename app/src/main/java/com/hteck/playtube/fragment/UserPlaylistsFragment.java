package com.hteck.playtube.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.adapter.YoutubePlaylistByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.ChannelSectionInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

import static com.hteck.playtube.common.Constants.PAGE_SIZE;


public class UserPlaylistsFragment extends BaseFragment implements OnScrollListener {

    private YoutubePlaylistByPageAdapter _adapter;
    private ArrayList<YoutubePlaylistInfo> _playlists = new ArrayList<>();
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private LoadingView _busyView;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;
    private ChannelInfo _channelInfo;
    private ChannelSectionInfo _activityInfo;

    public static UserPlaylistsFragment newInstance(ChannelInfo channelInfo, ChannelSectionInfo activityInfo) {
        UserPlaylistsFragment userVideosFragment = new UserPlaylistsFragment();
        userVideosFragment._channelInfo = channelInfo;
        userVideosFragment._activityInfo = activityInfo;
        return userVideosFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        createView(container);

        if (_activityInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
            setDataSource((ArrayList<YoutubePlaylistInfo>) _activityInfo.dataInfo);
        } else {
            loadData();
        }
        return _binding.getRoot();
    }

    private View createView(ViewGroup container) {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, container, false);
        _binding.textViewMsg.setText(Utils.getString(R.string.no_playlist_found));

        _adapter = new YoutubePlaylistByPageAdapter(getContext(), _playlists);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setOnScrollListener(this);
        _binding.listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {

                if (index == _playlists.size() - 1
                        && _playlists.get(index) == null) {
                    if (_adapter.getIsNetworkError()) {
                        _adapter.setIsNetworkError(false);
                        _adapter.notifyDataSetChanged();
                        loadMore();
                    }
                } else {
                    YoutubePlaylistInfo playlistInfo = _playlists.get(index);
                    ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();

                    // TODO:
                }
            }
        });

        return _binding.getRoot();
    }

    private void setDataSource(ArrayList<YoutubePlaylistInfo> videoList) {
        _binding.listView.setEmptyView(_binding.textViewMsg);
        if (videoList.size() > 0) {
            _binding.textViewMsg.setVisibility(View.GONE);
        } else {
            _binding.textViewMsg.setVisibility(View.VISIBLE);
        }

        _playlists = videoList;
        _adapter.setDataSource(_playlists);
    }

    private void resetDataSource() {
        _binding.listView.setSelectionFromTop(0, 0);

        _playlists = new ArrayList<>();
        _adapter.setDataSource(_playlists);
        _binding.textViewMsg.setVisibility(View.GONE);
        _nextPageToken = "";
    }

    public void loadData() {
        if (_isLoading) {
            return;
        }

        resetDataSource();

        _isLoading = true;
        loadData(_channelInfo);
    }

    public void loadMore() {
        if (_isLoading || Utils.stringIsNullOrEmpty(_nextPageToken)) {
            return;
        }

        _isLoading = true;
        loadData(_channelInfo);
    }

    private void loadData(ChannelInfo channelInfo) {
        String url = String.format(
                PlayTubeController.getConfigInfo().loadPlaylistsInChannelUrl, _nextPageToken,
                channelInfo.id, PAGE_SIZE);
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils
                                .getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_playlists.size() != 0) {
                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();

                            AbstractMap.SimpleEntry<String, ArrayList<YoutubePlaylistInfo>> searchResult = YoutubeHelper
                                    .getPlaylists(s, true, false);
                            ArrayList<YoutubePlaylistInfo> playlists = searchResult.getValue();
                            _nextPageToken = searchResult.getKey();

                            if (playlists.size() == 0) {
                                if (_playlists.size() > 0
                                        && _playlists.get(_playlists
                                        .size() - 1) == null) {
                                    _playlists.remove(_playlists.size() - 1);
                                }

                                setDataSource(_playlists);
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
        });
        _httpOk.start();

        if (_playlists.size() == 0) {
            showProgressBar();
        }
    }

    private void loadPlaylistsInfo(final ArrayList<YoutubePlaylistInfo> playlists) {
        String playlistIds = Utils.getPlaylistIds(playlists, 0);
        String url = String
                .format(PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl,
                        playlistIds);
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils.getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_playlists.size() != 0) {
                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();

                            YoutubeHelper.fillDataToPlaylists(s, playlists);

                            if (_playlists.size() > 0
                                    && _playlists.get(_playlists.size() - 1) == null) {
                                _playlists.remove(_playlists.size() - 1);
                            }

                            _playlists.addAll(playlists);
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _playlists.add(null);
                            }
                            setDataSource(_playlists);

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
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (_binding.listView.getLastVisiblePosition() == _binding.listView.getAdapter()
                    .getCount() - 1) {
                if (_playlists.size() > 0
                        && _playlists.get(_playlists.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadMore();
                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        if (_activityInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
            return Utils.getString(R.string.created_playlists);
        }
        return _channelInfo.title;
    }
}
