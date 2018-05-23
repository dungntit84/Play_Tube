package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

import static com.hteck.playtube.common.Constants.PAGE_SIZE;
import static com.hteck.playtube.common.Constants.YoutubeField.DATE_SORTBY;

public class YoutubePlayerVideosView extends Fragment implements
        AdapterView.OnItemClickListener, OnScrollListener {
    private String pageToken = "";
    private boolean isLoading = false;
    ArrayList<YoutubeInfo> youtubeListTemp = new ArrayList<>(), youtubeList = new ArrayList<>();
    YoutubeByPageAdapter _adapter;
    private LoadingView loadingView;
    private String uploaderId;
    public boolean mIsDataLoaded = false;
    private View viewReload;
    private ListViewBinding _binding;

    public static YoutubePlayerVideosView newInstance(String uploaderId) {
        YoutubePlayerVideosView youtubePlayerVideosView = new YoutubePlayerVideosView();

        youtubePlayerVideosView.uploaderId = uploaderId;
        return youtubePlayerVideosView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = DataBindingUtil.inflate(inflater, R.layout.list_view, container, false);
        _binding.listView.setOnScrollListener(this);
        _binding.listView.setOnItemClickListener(this);
        _adapter = new YoutubeByPageAdapter(getContext(), youtubeList);
        _binding.listView.setAdapter(_adapter);
        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));
        resetData(this.uploaderId);
        return _binding.getRoot();
    }

    private void setDataSource(ArrayList<YoutubeInfo> youtubeList) {
        _binding.listView.setEmptyView(_binding.textViewMsg);
        _binding.textViewMsg.setVisibility(youtubeList.size() == 0 ? View.GONE : View.VISIBLE);
        _adapter.setDataSource(youtubeList);
    }

    public void resetData(String uploaderId) {
        try {
            this.uploaderId = uploaderId;
            youtubeList = new ArrayList<>();

            pageToken = "";
            isLoading = false;
            mIsDataLoaded = false;
            _adapter.setDataSource(youtubeList);
            _binding.textViewMsg.setVisibility(View.GONE);
            loadData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadData() {

        if (viewReload != null) {
            _binding.layoutMain.removeView(viewReload);
            viewReload = null;
        }
        if (mIsDataLoaded) {
            return;
        }
        mIsDataLoaded = true;
        _adapter.setIsNetworkError(false);
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
            url = String.format(
                    PlayTubeController.getConfigInfo().loadVideosInChannelSortByUrl,
                    "", uploaderId, PAGE_SIZE, DATE_SORTBY);
        }

        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        isLoading = false;

                        hideProgressBar();
                        if (youtubeList.size() == 0) {
                            mIsDataLoaded = false;
                            initReloadEvent();
                        } else {
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
                            youtubeListTemp = searchResult.getValue();
                            pageToken = searchResult.getKey();
                            if (youtubeListTemp.size() == 0) {
                                if (youtubeList.size() > 0
                                        && youtubeList.get(youtubeList.size() - 1) == null) {
                                    youtubeList.remove(youtubeList.size() - 1);
                                }

                                setDataSource(youtubeList);
                                hideProgressBar();
                                isLoading = false;
                            } else {
                                isLoading = false;
                                loadVideosInfo();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideProgressBar();
                            isLoading = false;
                        }

                    }
                });

            }
        });
        httpOk.start();
    }

    private void hideProgressBar() {
        try {
            Utils.hideProgressBar(_binding.layoutMain, loadingView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void showProgressBar() {
        try {
            loadingView = Utils.showProgressBar(
                    (ViewGroup) _binding.layoutMain, loadingView);
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

        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        hideProgressBar();
                        isLoading = false;
                        if (youtubeList.size() == 0) {
                            mIsDataLoaded = false;
                            initReloadEvent();
                        } else {
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
                            setDataSource(youtubeList);
                            mIsDataLoaded = true;

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideProgressBar();
                        isLoading = false;
                    }
                });

            }
        });
        httpOk.start();
    }

    private void initReloadEvent() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        viewReload = inflater.inflate(R.layout.retry_view, null);
        _binding.layoutMain.addView(viewReload);
        viewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        try {
            if (index == youtubeList.size() - 1
                    && youtubeList.get(index) == null) {
                if (_adapter.getIsNetworkError()) {
                    _adapter.setIsNetworkError(false);
                    _adapter.notifyDataSetChanged();
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
            if (_binding.listView.getLastVisiblePosition() == _binding.listView.getAdapter()
                    .getCount() - 1) {
                if (_adapter.getCount() > 0
                        && _adapter.getItem(_adapter.getCount() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadDataMore();
                    }
                }
            }
        }

    }

}
