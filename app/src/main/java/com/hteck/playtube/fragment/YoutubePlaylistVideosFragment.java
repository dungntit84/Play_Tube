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
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
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

import static com.hteck.playtube.common.Constants.PAGE_SIZE;


public class YoutubePlaylistVideosFragment extends BaseFragment implements OnScrollListener {

    private YoutubeByPageAdapter _adapter;
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>(),
            _videoListLoading;
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private LoadingView _busyView;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;
    private YoutubePlaylistInfo _playlistInfo;

    public static YoutubePlaylistVideosFragment newInstance(YoutubePlaylistInfo _playlistInfo) {
        YoutubePlaylistVideosFragment channelVideosFragment = new YoutubePlaylistVideosFragment();
        channelVideosFragment._playlistInfo = _playlistInfo;
        return channelVideosFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        createView(container);

        loadData();
        return _binding.getRoot();
    }

    private View createView(ViewGroup container) {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, container, false);
        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));

        _adapter = new YoutubeByPageAdapter(_youtubeList);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setOnScrollListener(this);
        _binding.listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {

                if (index == _youtubeList.size() - 1
                        && _youtubeList.get(index) == null) {
                    if (_adapter.getIsNetworkError()) {
                        _adapter.setIsNetworkError(false);
                        _adapter.notifyDataSetChanged();
                        loadMore();
                    }
                } else {
                    YoutubeInfo youtubeInfo = _youtubeList.get(index);
                    ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
                    for (YoutubeInfo y : _youtubeList) {
                        if (y != null) {
                            youtubeList.add(y);
                        }
                    }
                    MainActivity.getInstance().playYoutube(youtubeInfo, youtubeList, true);
                }
            }
        });

        return _binding.getRoot();
    }

    private void setDataSource(ArrayList<YoutubeInfo> videoList) {
        _binding.listView.setEmptyView(_binding.textViewMsg);
        if (videoList.size() > 0) {
            _binding.textViewMsg.setVisibility(View.GONE);
        } else {
            _binding.textViewMsg.setVisibility(View.VISIBLE);
        }

        _youtubeList = videoList;
        _adapter.setDataSource(_youtubeList);
    }

    private void resetDataSource() {
        _binding.listView.setSelectionFromTop(0, 0);

        _youtubeList = new ArrayList<>();
        _adapter.setDataSource(_youtubeList);
        _binding.textViewMsg.setVisibility(View.GONE);
        _nextPageToken = "";
    }

    public void loadData() {
        if (_isLoading) {
            return;
        }

        resetDataSource();

        _isLoading = true;
        loadData(_playlistInfo);
    }

    public void loadMore() {
        if (_isLoading || Utils.stringIsNullOrEmpty(_nextPageToken)) {
            return;
        }

        _isLoading = true;
        loadData(_playlistInfo);
    }

    private void loadData(YoutubePlaylistInfo playlistInfo) {
        String url = String
                .format(PlayTubeController.getConfigInfo().loadVideosInPlaylistUrl, _nextPageToken,
                        playlistInfo.id, PAGE_SIZE);
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
                        if (_youtubeList.size() != 0) {
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

                            AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult = YoutubeHelper
                                    .getVideosInPlaylist(s, 0);
                            _videoListLoading = searchResult.getValue();
                            _nextPageToken = searchResult.getKey();

                            if (_videoListLoading.size() == 0) {
                                if (_youtubeList.size() > 0
                                        && _youtubeList.get(_youtubeList
                                        .size() - 1) == null) {
                                    _youtubeList.remove(_youtubeList.size() - 1);
                                }

                                setDataSource(_youtubeList);
                                hideProgressBar();
                                _isLoading = false;
                            } else {
                                loadVideosInfo();
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

        if (_youtubeList.size() == 0) {
            showProgressBar();
        }
    }

    private void loadVideosInfo() {
        String videoIds = "";
        for (YoutubeInfo y : _videoListLoading) {
            if (Objects.equals(videoIds, "")) {
                videoIds = y.id;
            } else {
                videoIds = videoIds + "," + y.id;
            }
        }
        String url = String
                .format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                        videoIds);
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils.getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_youtubeList.size() != 0) {
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

                            YoutubeHelper.populateYoutubeListInfo(_videoListLoading,
                                    s);

                            if (_youtubeList.size() > 0
                                    && _youtubeList.get(_youtubeList.size() - 1) == null) {
                                _youtubeList.remove(_youtubeList.size() - 1);
                            }

                            _youtubeList.addAll(YoutubeHelper
                                    .getAvailableVideos(_videoListLoading));
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _youtubeList.add(null);
                            }
                            setDataSource(_youtubeList);

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
                if (_youtubeList.size() > 0
                        && _youtubeList.get(_youtubeList.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadMore();
                    }
                }
            }
        }
    }

    @Override
    public String getTitle() {
        return _playlistInfo.title;
    }
}
