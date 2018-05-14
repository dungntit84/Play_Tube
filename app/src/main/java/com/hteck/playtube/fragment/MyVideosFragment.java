package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.SwipeRefreshViewBinding;
import com.hteck.playtube.service.AccountContext;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeAccountService;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public class MyVideosFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<YoutubeInfo> _songList = new ArrayList<>();
    YoutubeByPageAdapter _adapterSong;
    private LoadingView _busyView;
    private boolean _isLoading = false;
    private int _accountViewType;
    String _playlistId;
    private View _viewReload;
    private boolean _isLoadMore;
    private SwipeRefreshViewBinding _binding;

    public static MyVideosFragment newInstance(int accountViewType) {
        MyVideosFragment v = new MyVideosFragment();

        v._accountViewType = accountViewType;

        return v;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        createMainView(container);

        loadData();

        return _binding.getRoot();
    }

    private void loadData() {
        if (_accountViewType == Constants.AccountViewType.WhatToWatch) {
            loadData(false);
        } else {
            loadChannelInfo();
        }
    }

    private void createMainView(ViewGroup container) {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.swipe_refresh_view, container, false);

        _adapterSong = new YoutubeByPageAdapter(_songList);
        _binding.listView.setAdapter(_adapterSong);
        _binding.listView.setOnItemClickListener(this);
        _binding.listView.setOnScrollListener(this);

        _binding.swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadData();
                _binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void setDataSource(ArrayList<YoutubeInfo> songList) {

        _binding.listView.setEmptyView(_binding.textViewMsg);
        _binding.textViewMsg.setVisibility(songList.size() == 0 ? View.VISIBLE : View.GONE);
        _songList = songList;
        _adapterSong.setDataSource(_songList);
        _adapterSong.notifyDataSetChanged();
    }

    private void resetDataSource() {
        _binding.listView.setSelectionFromTop(0, 0);
        _binding.textViewMsg.setVisibility(View.GONE);
        _songList = new ArrayList<YoutubeInfo>();
        _adapterSong.setDataSource(_songList);
        _adapterSong.notifyDataSetChanged();
        _nextPageToken = "";
    }

    private void loadChannelInfo() {
        if (_isLoading) {
            return;
        }

        if (AccountContext.getInstance().getAccountInfo() != null) {
            _playlistId = getPlaylistId(AccountContext.getInstance().getAccountInfo());
            if (!Utils.stringIsNullOrEmpty(_playlistId)) {
                loadData(false);
                return;
            }
        }
    }

    private String getPlaylistId(ChannelInfo channelInfo) {
        if (_accountViewType == Constants.AccountViewType.Uploads) {
            return channelInfo.uploadPlaylistId;
        } else if (_accountViewType == Constants.AccountViewType.Favourites) {
            return channelInfo.favouritePlaylistId;
        } else {
            return channelInfo.likePlaylistId;
        }
    }

    private void loadData(boolean isLoadMore) {
        if (_isLoading) {
            return;
        }

        if (isLoadMore && Utils.stringIsNullOrEmpty(_nextPageToken)) {
            return;
        }
        _isLoading = true;
        if (!isLoadMore) {
            _nextPageToken = "";
            showBusyAnimation();
        }
        _isLoadMore = isLoadMore;
        YoutubeAccountService youtubeService = new YoutubeAccountService(
                new YoutubeAccountService.IYoutubeAccountService() {

                    @Override
                    public void onServiceSuccess(Object userToken, final Object data) {
                        MainActivity.getInstance().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            processVideoData(data
                                                    .toString());
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onServiceFailed(Object userToken, final String error) {
                        _isLoading = false;
                        MainActivity.getInstance().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        hideBusyAnimation();
                                        Utils.showMessage(error);
                                        if (_songList.size() == 0) {
                                            initReloadEvent();
                                        } else {
                                            _adapterSong.setIsNetworkError(true);
                                            _adapterSong.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                });
        if (_accountViewType == Constants.AccountViewType.WhatToWatch) {
            youtubeService.loadWhatToWatch(_nextPageToken);
        } else {
            youtubeService
                    .loadMyPlaylistVideos(_playlistId, _nextPageToken);
        }

    }

    private void processVideoData(String s) {
        try {
            AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult;
            if (_accountViewType == Constants.AccountViewType.WhatToWatch) {
                searchResult = YoutubeHelper.getVideosInAccount(s);
            } else {
                searchResult = YoutubeHelper.getVideosInPlaylist(s, 0);
            }

            ArrayList<YoutubeInfo> songList = searchResult.getValue();
            _nextPageToken = searchResult.getKey();

            if (songList.size() == 0) {
                if (_songList.size() > 0
                        && _songList.get(_songList.size() - 1) == null) {
                    _songList.remove(_songList.size() - 1);
                }

                setDataSource(_songList);
                hideBusyAnimation();
                _isLoading = false;
            } else {
                loadVideosInfo(songList);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            hideBusyAnimation();
            _isLoading = false;
        }
    }

    private void loadVideosInfo(final ArrayList<YoutubeInfo> youtubeList) {
        String videoIds = Utils.getYoutubeIds(youtubeList, 0);
        String url = String
                .format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                        videoIds);
        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Utils.showMessage(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_songList.size() == 0) {
                            initReloadEvent();
                        } else {
                            _adapterSong.setIsNetworkError(true);
                            _adapterSong.notifyDataSetChanged();
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
                            YoutubeHelper.populateYoutubeListInfo(youtubeList, s);

                            if (_songList.size() > 0
                                    && _songList.get(_songList.size() - 1) == null) {
                                _songList.remove(_songList.size() - 1);
                            }
                            if (!_isLoadMore) {
                                _songList = new ArrayList<>();
                            }
                            _songList.addAll(YoutubeHelper
                                    .getAvailableVideos(youtubeList));
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _songList.add(null);
                            }
                            setDataSource(_songList);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideBusyAnimation();
                        _isLoading = false;
                    }
                });
            }
        });
        httpOk.start();
    }

    private void hideBusyAnimation() {
        Utils.hideProgressBar(_binding.layoutMain, _busyView);
    }

    private void showBusyAnimation() {
        Utils.showProgressBar(_binding.layoutMain, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        int index = (int) arg3;
        if (index == _songList.size() - 1 && _songList.get(_songList.size() - 1) == null) {
            if (_adapterSong.getIsNetworkError()) {
                _adapterSong.setIsNetworkError(false);
                _adapterSong.notifyDataSetChanged();
                loadData(true);
            }
        } else {
            ArrayList<YoutubeInfo> playingSongList = new ArrayList<>();
            playingSongList.addAll(_songList);
            if (playingSongList.size() > 0
                    && playingSongList.get(playingSongList.size() - 1) == null) {
                playingSongList.remove(playingSongList.size() - 1);
            }

            MainActivity.getInstance().playYoutube(_songList.get(index),
                    playingSongList, true);
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
            if (_binding.listView.getLastVisiblePosition() == _binding.listView.getAdapter()
                    .getCount() - 1) {
                if (_songList.size() > 0
                        && _songList.get(_songList.size() - 1) == null) {
                    if (!_adapterSong.getIsNetworkError()) {
                        loadData(true);
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
                loadData();
                return false;
            }
        });
    }

    @Override
    public String getTitle() {
        String title = "";
        if (_accountViewType == Constants.AccountViewType.WhatToWatch) {
            title = Utils.getString(R.string.what_to_watch);
        } else if (_accountViewType == Constants.AccountViewType.Uploads) {
            title = Utils.getString(R.string.uploads);
        } else if (_accountViewType == Constants.AccountViewType.Favourites) {
            title = Utils.getString(R.string.favourites);
        } else if (_accountViewType == Constants.AccountViewType.Likes) {
            title = Utils.getString(R.string.likes);
        }
        return title;
    }
}
