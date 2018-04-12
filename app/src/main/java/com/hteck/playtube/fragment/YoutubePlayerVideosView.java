package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.HttpDownload;
import com.hteck.playtube.common.IHttplistener;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;

public class YoutubePlayerVideosView extends Fragment implements
        AdapterView.OnItemClickListener, OnScrollListener {
    private String pageToken = "";
    private boolean isLoading = false;
    ArrayList<YoutubeInfo> youtubeListTemp = new ArrayList<>(), youtubeList = new ArrayList<>();
    YoutubeByPageAdapter youtubeByPageAdapter;
    ListView listView;
    private LoadingView loadingView;
    private String uploaderId;
    public boolean mIsDataLoaded = false;
    private ViewGroup mainView;
    private View viewReload;

    public static YoutubePlayerVideosView newInstance(String uploaderId) {
        YoutubePlayerVideosView youtubePlayerVideosView = new YoutubePlayerVideosView();

        youtubePlayerVideosView.uploaderId = uploaderId;
        return youtubePlayerVideosView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView = new ListView(getActivity());
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(this);
        youtubeByPageAdapter = new YoutubeByPageAdapter(youtubeList);
        listView.setAdapter(youtubeByPageAdapter);

        mainView = (FrameLayout) inflater.inflate(
                R.layout.frame_layout_view, null);
        mainView.addView(listView);
        resetData(this.uploaderId);
        return mainView;
    }

    public void resetData(String uploaderId) {
        try {
            this.uploaderId = uploaderId;
            youtubeList = new ArrayList<>();

            pageToken = "";
            isLoading = false;
            mIsDataLoaded = false;
            youtubeByPageAdapter.setDataSource(youtubeList);
            loadData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        if (mainView == null) {
            return;
        }
        if (viewReload != null) {
            mainView.removeView(viewReload);
            viewReload = null;
        }
        if (mIsDataLoaded) {
            return;
        }
        mIsDataLoaded = true;
        loadDataMore();
    }

    public void loadDataMore() {
        if (isLoading) {
            return;
        }
        if (youtubeList.size() == 0) {
            showProgressBar();
        }
        isLoading = true;
        String url;
        if (Utils.stringIsNullOrEmpty(this.uploaderId)) {
            url = String.format(PlayTubeController.getConfigInfo().loadRelatedOfYoutubeUrl, PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo().id,
                    pageToken);
        } else {
            url = String.format(PlayTubeController.getConfigInfo().loadYoutubesOfChannelUrl, uploaderId,
                    pageToken);
        }
        HttpDownload httpGetFile = new HttpDownload(url, new IHttplistener() {

            @Override
            public void returnResult(Object sender,
                                     final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Done) {
                            try {
                                if (listView != null) {
                                    String s = new String(data);
                                    AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult = YoutubeHelper
                                            .getVideoListInfo(s);
                                    youtubeListTemp = searchResult.getValue();
                                    pageToken = searchResult.getKey();
                                    if (youtubeListTemp.size() == 0) {
                                        if (youtubeList.size() > 0
                                                && youtubeList.get(youtubeList.size() - 1) == null) {
                                            youtubeList.remove(youtubeList.size() - 1);
                                        }

                                        youtubeByPageAdapter
                                                .setDataSource(youtubeList);
                                        hideProgressBar();
                                        isLoading = false;
                                    } else {
                                        isLoading = false;
                                        loadVideosInfo();
                                    }

                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                hideProgressBar();
                                isLoading = false;
                            }

                        }
                        if (resultType != ResultType.Done) {

                            Utils.showMessage(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            isLoading = false;

                            hideProgressBar();
                            if (youtubeList.size() == 0) {
                                mIsDataLoaded = false;
                                initReloadEvent();
                            } else {
                                youtubeByPageAdapter.setIsNetworkError(true);
                                youtubeByPageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });
        httpGetFile.start();
    }

    private void hideProgressBar() {
        try {
            Utils.hideProgressBar(mainView, loadingView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void showProgressBar() {
        try {
            loadingView = Utils.showProgressBar(
                    (ViewGroup) mainView, loadingView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void loadVideosInfo() {
        String ids = "";
        for (YoutubeInfo y : youtubeListTemp) {
            if (ids == "") {
                ids = y.id;
            } else {
                ids = ids + "," + y.id;
            }
        }
        String url = String
                .format(PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                        ids);
        HttpDownload httpGetFile = new HttpDownload(url, new IHttplistener() {

            @Override
            public void returnResult(Object sender,
                                     final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Done) {
                            try {
                                String s = new String(data);

                                YoutubeHelper.populateYoutubeListInfo(
                                        youtubeListTemp, s);

                                if (youtubeList.size() > 0
                                        && youtubeList.get(youtubeList.size() - 1) == null) {
                                    youtubeList.remove(youtubeList.size() - 1);
                                }

                                youtubeList.addAll(YoutubeHelper
                                        .getAvailableVideos(youtubeListTemp));
                                if (!Utils.stringIsNullOrEmpty(pageToken)) {
                                    youtubeList.add(null);
                                }
                                youtubeByPageAdapter.setDataSource(youtubeList);
                                mIsDataLoaded = true;

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            hideProgressBar();
                            isLoading = false;
                        }

                        if (resultType != ResultType.Done) {
                            Utils.showMessage(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            hideProgressBar();
                            isLoading = false;
                            if (youtubeList.size() == 0) {
                                mIsDataLoaded = false;
                                initReloadEvent();
                            } else {
                                youtubeByPageAdapter.setIsNetworkError(true);
                                youtubeByPageAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        });
        httpGetFile.start();
    }

    private void initReloadEvent() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        viewReload = (ViewGroup) inflater.inflate(R.layout.retry_view, null);
        mainView.addView(viewReload);
        viewReload.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                loadData();
                return false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        try {
            if (index == youtubeList.size() - 1
                    && youtubeList.get(index) == null) {
                if (youtubeByPageAdapter.getIsNetworkError()) {
                    youtubeByPageAdapter.setIsNetworkError(false);
                    youtubeByPageAdapter.notifyDataSetChanged();
                    loadDataMore();

                }
            } else {
                MainActivity.getInstance().playYoutube(youtubeList.get(index),
                        youtubeList, true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
            if (listView.getLastVisiblePosition() == listView.getAdapter()
                    .getCount() - 1) {
                if (listView.getAdapter().getCount() > 0
                        && listView.getAdapter().getItem(
                        listView.getAdapter().getCount() - 1) == null) {
                    if (!youtubeByPageAdapter.getIsNetworkError()) {
                        loadDataMore();
                    }
                }
            }
        }

    }

}
