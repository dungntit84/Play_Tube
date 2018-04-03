package com.hteck.playtube.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.HttpDownload;
import com.hteck.playtube.common.IHttplistener;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.YoutubeHelper;

import java.util.AbstractMap;
import java.util.ArrayList;


public class YoutubeListView extends FrameLayout implements OnScrollListener {

    private YoutubeByPageAdapter _adapter;
    private ArrayList<YoutubeInfo> _videoList = new ArrayList<>(),
            _videoListLoading;
    private String _nextPageToken = "";
    private boolean _isLoading = false;
    private LoadingView _busyView;
    private HttpDownload _httpDownload;
    private ListView _listView;
    private TextView _textViewMsg;
    private String _query;
    private ViewGroup _contentView;

    public YoutubeListView(Context context) {
        super(context);
        View v = createView();

        addView(v);
    }

    private View createView() {
        _contentView = (ViewGroup) MainActivity.getInstance()
                .getLayoutInflater().inflate(R.layout.list_view, null);

        _listView = _contentView.findViewById(R.id.list_view);
        _textViewMsg = _contentView.findViewById(R.id.text_view_msg);
        _textViewMsg.setText(Utils.getString(R.string.no_youtube));
        _listView.setEmptyView(_textViewMsg);
        _adapter = new YoutubeByPageAdapter(_videoList);
        _listView.setAdapter(_adapter);
        _listView.setOnScrollListener(this);
        _listView.setOnItemClickListener(new OnItemClickListener() {

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
                    YoutubeInfo videoInfo = _videoList.get(index);
                    ArrayList<YoutubeInfo> youtubeList = new ArrayList<>();
                    for (YoutubeInfo youtubeInfo : _videoList) {
                        if (youtubeInfo != null) {
                            youtubeList.add(youtubeInfo);
                        }
                    }
                    MainActivity.getInstance().playYoutube(videoInfo, youtubeList, true, true);
                }
            }
        });
        return _contentView;
    }

    private void setDataSource(ArrayList<YoutubeInfo> videoList) {

        if (videoList.size() > 0) {
            _textViewMsg.setVisibility(View.GONE);
        } else {
            _textViewMsg.setVisibility(View.VISIBLE);
        }

        _videoList = videoList;
        _adapter.setDataSource(_videoList);
    }

    private void resetDataSource() {
        _listView.setSelectionFromTop(0, 0);
        _textViewMsg.setVisibility(View.GONE);
        _videoList = new ArrayList<>();
        _adapter.setDataSource(_videoList);
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

        _httpDownload = new HttpDownload(url, new IHttplistener() {
            @Override
            public void returnResult(Object sender, final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Done) {
                            try {
                                String s = new String(data);

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

                        if (resultType != ResultType.Done) {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));
                            hideProgressBar();
                            _isLoading = false;
                            if (_videoList.size() != 0) {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });
        _httpDownload.start();
        if (_videoList.size() == 0) {
            showProgressBar();
        }
    }

    private void loadVideosInfo() {
        String videoIds = "";
        for (YoutubeInfo videoInfo : _videoListLoading) {
            if (videoIds == "") {
                videoIds = videoInfo.id;
            } else {
                videoIds = videoIds + "," + videoInfo.id;
            }
        }
        String url = String
                .format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                        videoIds);
        _httpDownload = new HttpDownload(url,
                new IHttplistener() {
                    @Override
                    public void returnResult(Object sender, final byte[] data, final ResultType resultType) {
                        MainActivity.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (resultType == ResultType.Done) {
                                    try {
                                        String s = new String(data);

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

                                if (resultType != ResultType.Done) {
                                    Utils.showMessage(Utils.getString(R.string.network_error));
                                    hideProgressBar();
                                    _isLoading = false;
                                    if (_videoList.size() != 0) {
                                        _adapter.setIsNetworkError(true);
                                        _adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                });
        _httpDownload.start();

    }

    private void cancelAllRequests() {
        if (_httpDownload != null) {
            _httpDownload.exit();
        }
    }

    private void hideProgressBar() {
        Utils.hideProgressBar(_contentView, _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(_contentView, _busyView);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (_listView.getLastVisiblePosition() == _listView.getAdapter()
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
