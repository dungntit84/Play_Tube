package com.hteck.playtube.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.YoutubeHelper;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Objects;


public class YoutubeListView extends FrameLayout implements OnScrollListener {

    private YoutubeByPageAdapter _adapter;
    private ArrayList<YoutubeInfo> _videoList = new ArrayList<>(),
            _videoListLoading;
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private LoadingView _busyView;
    private String _query;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;

    public YoutubeListView(Context context) {
        super(context);
        View v = createView();

        addView(v);
    }

    private View createView() {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, null, false);
        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));

        _adapter = new YoutubeByPageAdapter(_videoList);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setOnScrollListener(this);
        _binding.listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {

                if (index == _videoList.size() - 1
                        && _videoList.get(index) == null) {
                    if (_adapter.getIsNetworkError()) {
                        _adapter.setIsNetworkError(false);
                        _adapter.notifyDataSetChanged();
                        searchMore();
                    }
                } else {
                    YoutubeInfo youtubeInfo = _videoList.get(index);
                    ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
                    for (YoutubeInfo y : _videoList) {
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

        _videoList = videoList;
        _adapter.setDataSource(_videoList);
    }

    private void resetDataSource() {
        _binding.listView.setSelectionFromTop(0, 0);

        _videoList = new ArrayList<>();
        _adapter.setDataSource(_videoList);
        _binding.textViewMsg.setVisibility(View.GONE);
        _nextPageToken = "";
    }

    public void search(String query) {
        if (Utils.stringIsNullOrEmpty(query)) {
            return;
        }
        if (_isLoading) {
            return;
        }
        _query = query;
        resetDataSource();
        cancelAllRequests();
        _isLoading = true;
        searchData();
    }

    private void search() {
        search(_query);
    }

    public void searchMore() {
        if (_isLoading || Utils.stringIsNullOrEmpty(_nextPageToken)) {
            return;
        }

        _isLoading = true;
        searchData();
    }

    private void searchData() {
        String url = String
                .format(PlayTubeController.getConfigInfo().searchVideoUrl,
                        Utils.urlEncode(_query), _nextPageToken);
        _httpOk = new CustomHttpOk(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils
                                .getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_videoList.size() != 0) {
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
                                    .getVideoListInfo(s);
                            _videoListLoading = searchResult.getValue();
                            _nextPageToken = searchResult.getKey();

                            if (_videoListLoading.size() == 0) {
                                if (_videoList.size() > 0
                                        && _videoList.get(_videoList
                                        .size() - 1) == null) {
                                    _videoList.remove(_videoList.size() - 1);
                                }

                                setDataSource(_videoList);
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

        if (_videoList.size() == 0) {
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
        _httpOk = new CustomHttpOk(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils.getString(R.string.network_error));
                        hideProgressBar();
                        _isLoading = false;
                        if (_videoList.size() != 0) {
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

                            if (_videoList.size() > 0
                                    && _videoList.get(_videoList.size() - 1) == null) {
                                _videoList.remove(_videoList.size() - 1);
                            }

                            _videoList.addAll(YoutubeHelper
                                    .getAvailableVideos(_videoListLoading));
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _videoList.add(null);
                            }
                            setDataSource(_videoList);

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

    private void cancelAllRequests() {
        if (_httpOk != null) {
            _httpOk.cancel();
        }
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
                if (_videoList.size() > 0
                        && _videoList.get(_videoList.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        searchMore();
                    }
                }
            }
        }
    }
}
