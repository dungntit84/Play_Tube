package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public class ChannelActivityVideosView extends BaseFragment implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>();
    YoutubeByPageAdapter _adapter;
    private LoadingView _busyView;
    private boolean _isLoading = false;
    private ChannelInfo _channelInfo;
    private View _viewReload;
    private boolean _isLoadMore;
    private ListViewBinding _binding;

    public static ChannelActivityVideosView newInstance(ChannelInfo channelInfo) {
        ChannelActivityVideosView myVideosView = new ChannelActivityVideosView();

        myVideosView._channelInfo = channelInfo;
        Bundle args = new Bundle();
        args.putString(Constants.PAGE_ID, channelInfo.id.toString());
        myVideosView.setArguments(args);

        return myVideosView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        createMainView(container);

        loadData();

        return _binding.getRoot();
    }

    private void loadData() {
        loadData(false);
    }

    private void createMainView(ViewGroup container) {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, container, false);

        _adapter = new YoutubeByPageAdapter(getContext(), _youtubeList);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setOnItemClickListener(this);
        _binding.listView.setOnScrollListener(this);
    }

    public void setDataSource(ArrayList<YoutubeInfo> songList) {

        _binding.listView.setEmptyView(_binding.textViewMsg);
        _binding.textViewMsg.setVisibility(songList.size() == 0 ? View.VISIBLE : View.GONE);
        _youtubeList = songList;
        _adapter.setDataSource(_youtubeList);
        _adapter.notifyDataSetChanged();
    }

    private void resetDataSource() {
        _binding.listView.setSelectionFromTop(0, 0);
        _binding.textViewMsg.setVisibility(View.GONE);
        _youtubeList = new ArrayList<>();
        _adapter.setDataSource(_youtubeList);
        _adapter.notifyDataSetChanged();
        _nextPageToken = "";
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

        String url = String
                .format(PlayTubeController.getConfigInfo().loadActivitiesInChannelUrl, _nextPageToken,
                        _channelInfo.id,
                        Constants.PAGE_SIZE);
        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideBusyAnimation();
                        Utils.showMessage(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        if (_youtubeList.size() == 0) {
                            initReloadEvent();
                        } else {
                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
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
                            processVideoData(s);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }
                });
            }
        });
        httpOk.start();
    }

    private void processVideoData(String s) {
        try {
            AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult = YoutubeHelper.getVideosInAccount(s);

            ArrayList<YoutubeInfo> songList = searchResult.getValue();
            _nextPageToken = searchResult.getKey();

            if (songList.size() == 0) {
                if (_youtubeList.size() > 0
                        && _youtubeList.get(_youtubeList.size() - 1) == null) {
                    _youtubeList.remove(_youtubeList.size() - 1);
                }

                setDataSource(_youtubeList);
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
                        if (_youtubeList.size() == 0) {
                            initReloadEvent();
                        } else {
                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
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

                            if (_youtubeList.size() > 0
                                    && _youtubeList.get(_youtubeList.size() - 1) == null) {
                                _youtubeList.remove(_youtubeList.size() - 1);
                            }
                            if (!_isLoadMore) {
                                _youtubeList = new ArrayList<>();
                            }
                            _youtubeList.addAll(YoutubeHelper
                                    .getAvailableVideos(youtubeList));
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _youtubeList.add(null);
                            }
                            setDataSource(_youtubeList);

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
        _busyView = Utils.showProgressBar(_binding.layoutMain, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        if (index == _youtubeList.size() - 1 && _youtubeList.get(_youtubeList.size() - 1) == null) {
            if (_adapter.getIsNetworkError()) {
                _adapter.setIsNetworkError(false);
                _adapter.notifyDataSetChanged();
                loadData(true);
            }
        } else {
            ArrayList<YoutubeInfo> playingSongList = new ArrayList<>();
            playingSongList.addAll(_youtubeList);
            if (playingSongList.size() > 0
                    && playingSongList.get(playingSongList.size() - 1) == null) {
                playingSongList.remove(playingSongList.size() - 1);
            }

            MainActivity.getInstance().playYoutube(_youtubeList.get(index),
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
                if (_youtubeList.size() > 0
                        && _youtubeList.get(_youtubeList.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
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
        return Utils.getString(R.string.recent_activities);
    }
}
