package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
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
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PlayerYoutubeInfoAdapter;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.CommentInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.FrameLayoutViewBinding;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;

public class PlayerYoutubeInfoView extends Fragment implements OnScrollListener {
    public YoutubeInfo _youtubeInfo;

    PlayerYoutubeInfoAdapter _adapterVideoInfo;
    private LoadingView _busyView;
    private String _nextPageToken = "";
    private boolean _isLoadingComments = false;
    private View _viewReload;
    private ListView _listView;
    private FrameLayoutViewBinding _binding;

    public static PlayerYoutubeInfoView newInstance(YoutubeInfo youtubeInfo) {
        PlayerYoutubeInfoView playerBottomInfoTab = new PlayerYoutubeInfoView();

        playerBottomInfoTab._youtubeInfo = youtubeInfo;

        return playerBottomInfoTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        _binding = DataBindingUtil.inflate(inflater, R.layout.frame_layout_view, container, false);
        _listView = new ListView(MainActivity.getInstance());
        _adapterVideoInfo = new PlayerYoutubeInfoAdapter(getContext(),
                _youtubeInfo);
        _listView.setAdapter(_adapterVideoInfo);
        _listView.setSmoothScrollbarEnabled(false);
        _listView.setDividerHeight(0);
        _listView.setOnScrollListener(this);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                try {
                    if (index == _adapterVideoInfo.getCount() - 1 && _adapterVideoInfo.getCommentList().size() > 0 && _adapterVideoInfo.getCommentList().get(index) == null) {
                        if (_adapterVideoInfo.getIsNetworkError()) {
                            _adapterVideoInfo.setIsNetworkError(false);
                            _adapterVideoInfo.notifyDataSetChanged();
                            loadMoreData();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        _binding.layoutContainer.addView(_listView);

        loadData();
        return _binding.getRoot();
    }

    public void loadData() {
        loadComments();

        showProgressBar();
    }

    public void loadMoreData() {
        loadComments();
    }


    public void refreshData() {
        try {
            _listView.setVisibility(View.VISIBLE);
            if (_viewReload != null) {
                _binding.layoutContainer.removeView(_viewReload);
                _viewReload = null;
            }
            _youtubeInfo = PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo();
            _nextPageToken = "";
            ArrayList<CommentInfo> commentList = new ArrayList<>();
            _adapterVideoInfo.setIsNetworkError(false);
            _adapterVideoInfo.setDataSource(_youtubeInfo, commentList);
            _adapterVideoInfo.notifyDataSetChanged();

            loadData();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (((ListView) view).getLastVisiblePosition() == _adapterVideoInfo
                    .getCount() - 1) {
                if (_adapterVideoInfo.getCount() > 0
                        && _adapterVideoInfo.getItem(
                        _adapterVideoInfo.getCount() - 1) == null) {
                    loadComments();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

    private void hideProgressBar() {
        Utils.hideProgressBar(_binding.layoutContainer, _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(_binding.layoutContainer, _busyView);
    }

    private void loadComments() {
        if (_isLoadingComments) {
            return;
        }
        _isLoadingComments = true;
        String url = String
                .format(PlayTubeController.getConfigInfo().loadCommentsOfYoutubeUrl,
                        _youtubeInfo.id, _nextPageToken);

        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (_adapterVideoInfo.getCommentList().size() == 0) {
                            hideProgressBar();
                        }
                        _isLoadingComments = false;
                        if (_adapterVideoInfo.getCommentList().size() == 0) {
                            initReloadEvent();
                        } else {
                            _adapterVideoInfo.setIsNetworkError(true);
                            _adapterVideoInfo.notifyDataSetChanged();
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
                            if (_adapterVideoInfo.getCommentList().size() == 0) {
                                hideProgressBar();
                            }
                            _isLoadingComments = false;

                            String s = response.body().string();

                            AbstractMap.SimpleEntry<String, ArrayList<CommentInfo>> commentListInfo = YoutubeHelper
                                    .getCommentList(s);
                            ArrayList<CommentInfo> commentList = commentListInfo
                                    .getValue();
                            if (_adapterVideoInfo.getCommentList().size() > 0
                                    && _adapterVideoInfo.getCommentList().get(_adapterVideoInfo.getCommentList().size() - 1) == null) {
                                _adapterVideoInfo.getCommentList()
                                        .remove(_adapterVideoInfo.getCommentList()
                                                .size() - 1);
                            }
                            _nextPageToken = commentListInfo.getKey();
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                commentList.add(null);
                            }
                            _adapterVideoInfo.getCommentList().addAll(commentList);
                            _adapterVideoInfo.notifyDataSetChanged();

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });
        httpOk.start();
    }

    private void initReloadEvent() {
        try {
            LayoutInflater inflater = MainActivity.getInstance()
                    .getLayoutInflater();
            if (_viewReload != null) {
                _binding.layoutContainer.removeView(_viewReload);
            }
            _listView.setVisibility(View.GONE);
            _viewReload = inflater.inflate(R.layout.retry_view,
                    null);
            _binding.layoutContainer.addView(_viewReload);
            _viewReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refreshData();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
