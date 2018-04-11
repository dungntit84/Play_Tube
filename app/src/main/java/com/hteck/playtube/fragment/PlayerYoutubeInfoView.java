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
import com.hteck.playtube.common.HttpDownload;
import com.hteck.playtube.common.IHttplistener;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.CommentInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.FrameLayoutViewBinding;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;

public class PlayerYoutubeInfoView extends Fragment implements OnScrollListener {
    public YoutubeInfo _videoInfo;

    PlayerYoutubeInfoAdapter _adapterVideoInfo;
    private LoadingView _busyView;
    private String _nextPageToken = "";
    private boolean _isLoadingComments = false;
    private View _viewReload;
    private ListView _listView;
    private FrameLayoutViewBinding _binding;

    public static PlayerYoutubeInfoView newInstance(YoutubeInfo videoInfo) {
        PlayerYoutubeInfoView playerBottomInfoTab = new PlayerYoutubeInfoView();

        playerBottomInfoTab._videoInfo = videoInfo;

        return playerBottomInfoTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        _binding = DataBindingUtil.inflate(inflater, R.layout.frame_layout_view, container, false);
        _listView = new ListView(MainActivity.getInstance());
        _adapterVideoInfo = new PlayerYoutubeInfoAdapter(
                _videoInfo);
        _listView.setAdapter(_adapterVideoInfo);
        _listView.setSmoothScrollbarEnabled(false);
        _listView.setDividerHeight(0);
        _listView.setOnScrollListener(this);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                try {
                    if (index == _adapterVideoInfo.getCount() - 1 && _adapterVideoInfo.getCommentList().size() > 0 && _adapterVideoInfo.getCommentList().lastElement() == null) {
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
        ((ViewGroup) _binding.getRoot()).addView(_listView);

        loadData();
        return  _binding.getRoot();
    }

    public void loadData() {
        loadComments();

        showProgressBar();
    }

    public void loadMoreData() {
        loadComments();
    }


    public void resetData() {
        try {
            _listView.setVisibility(View.VISIBLE);
            if (_viewReload != null) {
                ((ViewGroup) _binding.getRoot()).removeView(_viewReload);
                _viewReload = null;
            }
            _videoInfo = PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo();
            _nextPageToken = "";
            Vector<CommentInfo> commentList = new Vector<>();
            if (_adapterVideoInfo == null) {
                return;
            }
            _adapterVideoInfo.setDataSource(_videoInfo, commentList);
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
        Utils.hideProgressBar(((ViewGroup) _binding.getRoot()), _busyView);
    }

    private void showProgressBar() {
        _busyView = Utils.showProgressBar(((ViewGroup) _binding.getRoot()), _busyView);
    }

    private void loadComments() {
        if (_isLoadingComments) {
            return;
        }
        _isLoadingComments = true;
        String url = String
                .format(PlayTubeController.getConfigInfo().loadCommentsOfYoutubeUrl,
                        _videoInfo.id, _nextPageToken);
        HttpDownload _httpGetFile = new HttpDownload(url, new IHttplistener() {

            @Override
            public void returnResult(Object sender,
                                     final byte[] data, final ResultType resultType) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (_adapterVideoInfo.getCommentList().size() == 0) {
                            hideProgressBar();
                        }
                        if (resultType == ResultType.Done) {
                            try {
                                String s = new String(data);

                                AbstractMap.SimpleEntry<String, ArrayList<CommentInfo>> commentListInfo = YoutubeHelper
                                        .getCommentList(s);
                                ArrayList<CommentInfo> commentList = commentListInfo
                                        .getValue();
                                if (_adapterVideoInfo.getCommentList().size() > 0
                                        && _adapterVideoInfo.getCommentList().lastElement() == null) {
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
                        if (resultType == ResultType.ConnectionError) {
                            _isLoadingComments = false;
                            if (_adapterVideoInfo.getCommentList().size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapterVideoInfo.setIsNetworkError(true);
                                _adapterVideoInfo.notifyDataSetChanged();
                            }
                        }
                        _isLoadingComments = false;
                    }
                });
            }
        });
        _httpGetFile.start();
    }

    private void initReloadEvent() {
        try {
            LayoutInflater inflater = MainActivity.getInstance()
                    .getLayoutInflater();
            if (_viewReload != null) {
                ((ViewGroup) _binding.getRoot()).removeView(_viewReload);
            }
            _listView.setVisibility(View.GONE);
            _viewReload = (ViewGroup) inflater.inflate(R.layout.retry_view,
                    null);
            ((ViewGroup) _binding.getRoot()).addView(_viewReload);
            _viewReload.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    resetData();
                    return false;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
