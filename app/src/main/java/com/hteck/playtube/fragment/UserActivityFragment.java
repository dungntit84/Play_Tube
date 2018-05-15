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

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubePlaylistItemByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.ChannelSectionInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
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
import static com.hteck.playtube.common.Constants.YoutubeField.DATE_SORTBY;
import static com.hteck.playtube.common.Constants.YoutubeField.VIEWCOUNT_SORTBY;

public class UserActivityFragment extends Fragment implements
        AdapterView.OnItemClickListener, OnScrollListener {
    private String _nextPageToken = "";
    ArrayList<PlaylistItemInfo> _playlistItemViewInfos = new ArrayList<>();
    ArrayList<PlaylistItemInfo> _playlistItemViewInfosLoading = new ArrayList<>();
    ArrayList<ChannelSectionInfo> _activityListAll = new ArrayList<>();
    ArrayList<ChannelSectionInfo> _activityListLoading = new ArrayList<>();
    YoutubePlaylistItemByPageAdapter _adapter;
    private View _viewReload;
    private LoadingView _busyView;
    private boolean _isLoading;
    private int _loadingIndex = 0;
    private int _loadingIndexOld = 0;
    private int _loadingIndexPerPage = 0;
    private int _loadingIndexPerPageOld = 0;
    private final int PAGESIZE = 4;
    private final int PAGESIZE_PLAYLIST = 50;
    private ChannelInfo _channelInfo;
    private boolean _isLoadCompleted;
    private boolean _isLoadCompletedPerPage;
    private boolean _isDataBinded;
    private boolean _isLoadedByPage;
    ArrayList<ChannelSectionInfo> _activityListToLoadData = new ArrayList<>();
    boolean _hasVideoActivityOnly;
    private boolean _isNewRound;
    private boolean _isParentScrollable = true;
    private boolean _isShowingBusy;

    public static UserActivityFragment newInstance(ChannelInfo channelInfo) {
        UserActivityFragment channelVideosTab = new UserActivityFragment();
        channelVideosTab._channelInfo = channelInfo;
        return channelVideosTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        createView(container);

        loadData();
        return _binding.getRoot();
    }

    public void setParentScrollable(boolean isScrollable) {
        _isParentScrollable = isScrollable;
    }

    private void loadData() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        _activityListToLoadData = new ArrayList<>();
        showBusyAnimation();
        String url = String.format(
                PlayTubeController.getConfigInfo().loadSectionsOfChannelUrl,
                _channelInfo.id);
        CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
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

                            _activityListAll = YoutubeHelper
                                    .getActivityInfos(s);
                            _hasVideoActivityOnly = _activityListAll.size() == 0;
                            if (_activityListAll.size() > 0) {
                                loadActivityInfos(true);
                            } else {
                                loadActivities(false);
                            }

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

    private void loadActivities(boolean isLoadMore) {
        if (!isLoadMore) {
            showBusyAnimation();
        }
        String url = String.format(
                PlayTubeController.getConfigInfo().loadActivitiesInChannelUrl, _nextPageToken, _channelInfo.id);

        CustomHttpOk httpOk = new CustomHttpOk(url, buildChannelActivitiesCompletedListener());
        httpOk.start();
    }

    private CustomCallback buildChannelActivitiesCompletedListener() {
        return new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils.getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final CustomCallback customCallback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();
                            AbstractMap.SimpleEntry<String, ArrayList<PlaylistItemInfo>> activityListInfos = YoutubeHelper
                                    .getUserActivities(s);
                            if (_hasVideoActivityOnly) {
                                _playlistItemViewInfosLoading = activityListInfos
                                        .getValue();
                                _isLoadCompleted = Utils
                                        .stringIsNullOrEmpty(activityListInfos
                                                .getKey());
                                _nextPageToken = activityListInfos.getKey();
                                if (_playlistItemViewInfosLoading.size() > 0) {
                                    loadVideosInfo();
                                } else {
                                    _isLoading = false;
                                    hideBusyAnimation();
                                }
                            } else {
                                ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) customCallback.getDataContext();
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.activities = activityListInfos
                                        .getValue();
                                channelInfo.hasMoreVideos = !Utils
                                        .stringIsNullOrEmpty(activityListInfos
                                                .getKey());

                                channelSectionInfo.youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
                                loadDataByPage();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            _isLoading = false;
                        }
                    }
                });
            }
        };
    }

    private void loadActivityInfos(boolean isNewRound) {
        _isNewRound = isNewRound;
        int count = (_loadingIndex + PAGESIZE_PLAYLIST) > _activityListAll
                .size() ? _activityListAll.size() : _loadingIndex
                + PAGESIZE_PLAYLIST;
        boolean isPendingToLoadingInfo = false;
        for (int i = _loadingIndex; i < count; ++i) {
            ChannelSectionInfo activityInfo = _activityListAll.get(i);
            if (activityInfo.youtubeState == Constants.YoutubeState.QUEUE) {
                if (activityInfo.activityType == Constants.UserActivityType.FAVOURITE) {
                    loadChannelDetails(activityInfo);
                    isPendingToLoadingInfo = true;
                }
            }
        }
        if (isPendingToLoadingInfo) {
            return;
        }
        StringBuilder ids = new StringBuilder();
        if (isNewRound) {
            _activityListLoading = new ArrayList<>();
        }
        boolean isWaitting = false;
        for (int i = _loadingIndex; i < count; ++i) {
            ChannelSectionInfo activityInfo = _activityListAll.get(i);
            switch (activityInfo.activityType) {
                case Constants.UserActivityType.SINGLEPLAYLIST: {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) activityInfo.dataInfo;
                    if (Objects.equals(ids.toString(), "")) {
                        ids = new StringBuilder(playlistInfo.id);
                    } else {
                        ids.append(",").append(playlistInfo.id);
                    }
                    break;
                }
                case Constants.UserActivityType.UPLOADS: {
                    loadChannelVideos(activityInfo);
                    isWaitting = true;
                    break;
                }
                case Constants.UserActivityType.RECENTACTIVIY: {
                    loadChannelActivities(activityInfo);
                    isWaitting = true;
                    break;
                }
                case Constants.UserActivityType.ALLPLAYLISTS: {
                    loadAllPlaylistsOfChannel(activityInfo);
                    isWaitting = true;
                    break;
                }
            }

            _activityListLoading.add(activityInfo);
        }
        if (!Utils.stringIsNullOrEmpty(ids.toString())) {
            String url = String.format(
                    PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl, ids.toString());
            CustomHttpOk httpOk = new CustomHttpOk(url, new CustomCallback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    MainActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Utils.showMessage(Utils
                                        .getString(R.string.network_error));
                                hideBusyAnimation();
                                _isLoading = false;
                                if (_playlistItemViewInfos.size() == 0) {
                                    initReloadEvent();
                                } else {
                                    _adapter.setIsNetworkError(true);
                                    _adapter.notifyDataSetChanged();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
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
                                ArrayList<YoutubePlaylistInfo> playlists = YoutubeHelper
                                        .getPlaylists(s, true);

                                for (int i = 0; i < _activityListLoading.size(); ++i) {
                                    if (_activityListLoading.get(i).activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                                        for (int k = 0; k < playlists.size(); ++k) {

                                            YoutubePlaylistInfo activityPlaylistInfo = (YoutubePlaylistInfo) _activityListLoading
                                                    .get(i).dataInfo;
                                            if (activityPlaylistInfo.id
                                                    .equals(playlists.get(k).id)) {
                                                activityPlaylistInfo.title = playlists
                                                        .get(k).title;
                                                activityPlaylistInfo.imgeUrl = playlists
                                                        .get(k).imgeUrl;
                                                activityPlaylistInfo.videoCount = playlists
                                                        .get(k).videoCount;

                                                break;
                                            }
                                        }
                                        _activityListLoading.get(i).youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
                                    }

                                }

                                loadDataByPage();

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
            isWaitting = true;
        }
        _isLoadCompleted = _activityListAll.size() <= count;
        _isLoadedByPage = false;
        if (!isWaitting) {
            loadDataByPage();
        }
    }

    private void loadDataByPage() {
        for (int i = _loadingIndexPerPage; i < _activityListLoading.size(); ++i) {
            ChannelSectionInfo activityInfo = _activityListLoading.get(i);
            if (activityInfo.youtubeState < Constants.YoutubeState.ITEMCOUNTLOADED) {
                return;
            }
            if (activityInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                activityInfo.youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
            }
        }
        if (_isLoadedByPage) {
            return;
        }
        _isLoadedByPage = true;
        int count = 0;
        boolean isFinised = true;

        for (int i = _loadingIndexPerPage; i < _activityListLoading.size(); ++i) {
            ChannelSectionInfo activityInfo = _activityListLoading.get(i);
            if (activityInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) activityInfo.dataInfo;
                int videoCount = playlistInfo.videoCount > PAGESIZE ? PAGESIZE
                        : playlistInfo.videoCount;

                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);

            } else if (activityInfo.activityType == Constants.UserActivityType.UPLOADS) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int videoCount = channelInfo.youtubeList.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.youtubeList.size();
                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int videoCount = channelInfo.activities.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.activities.size();
                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int playlistCount = channelInfo.playlists.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.playlists.size();
                if (count + playlistCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += playlistCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
                ArrayList<YoutubePlaylistInfo> playlists = (ArrayList<YoutubePlaylistInfo>) activityInfo.dataInfo;
                int playlistCount = playlists.size() > PAGESIZE ? PAGESIZE
                        : playlists.size();
                if (count + playlistCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += playlistCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == Constants.UserActivityType.MULTIPLECHANNELS) {
                ArrayList<ChannelInfo> channels = (ArrayList<ChannelInfo>) activityInfo.dataInfo;
                int channelCount = channels.size() > PAGESIZE ? PAGESIZE
                        : channels.size();
                if (count + channelCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += channelCount;
                _activityListToLoadData.add(activityInfo);
            }
        }

        _isLoadCompletedPerPage = isFinised;
        if (_isLoadCompletedPerPage && count < Constants.PAGE_SIZE) {
            if (_isLoadCompleted) {
                loadDataItems();
            } else {
                _loadingIndex += PAGESIZE_PLAYLIST;

                loadActivityInfos(false);
            }
        } else {
            loadDataItems();
        }
    }

    private void loadMoreData() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        if (!_hasVideoActivityOnly) {
            _isDataBinded = false;
            _isLoadedByPage = false;
            _activityListToLoadData = new ArrayList<ChannelSectionInfo>();
            _loadingIndex = _loadingIndexOld;
            _loadingIndexPerPage = _loadingIndexPerPageOld;
            loadDataByPage();
        } else {
            loadActivities(true);
        }
    }

    public void loadChannelDetails(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
        String url = String.format(
                PlayTubeController.getConfigInfo().loadChannelsInfoUrl,
                channelInfo.id);
        CustomCallback callback = new CustomCallback(activityInfo) {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {

                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();
                            String s = response.body().string();
                            ArrayList<ChannelInfo> channelList = YoutubeHelper.getChannelList(s);

                            if (channelList.size() > 0
                                    && !Utils.stringIsNullOrEmpty(channelList
                                    .get(0).likePlaylistId)) {
                                YoutubePlaylistInfo playlistInfo = new YoutubePlaylistInfo();
                                playlistInfo.id = channelList.get(0).likePlaylistId;
                                channelSectionInfo.dataInfo = playlistInfo;
                                channelSectionInfo.activityType = Constants.UserActivityType.SINGLEPLAYLIST;
                            } else {
                                channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                            }
                            loadActivityInfos(_isNewRound);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            _isLoading = false;
                        }
                    }
                });
            }
        };
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    public void loadAllPlaylistsOfChannel(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
        String url = String.format(
                PlayTubeController.getConfigInfo().loadPlaylistsInChannelUrl, "",
                channelInfo.id, PAGESIZE);
        CustomCallback callback = buildAllPlaylistsCompletedListener();
        callback.setDataContext(activityInfo);
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    public void loadPlaylistsOfMulti(ChannelSectionInfo activityInfo) {
        ArrayList<YoutubePlaylistInfo> playlists = (ArrayList<YoutubePlaylistInfo>) activityInfo.dataInfo;

        int count = 0;
        String ids = Utils.getPlaylistIds(playlists, Constants.MAX_SIZE_FOR_LOADING_YOUTUBE_DATA);
        String url = String.format(
                PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl, ids);
        CustomCallback callback = buildAllPlaylistsCompletedListener();
        callback.setDataContext(activityInfo);
        new CustomHttpOk(url, callback).start();
    }

    private void populatePlaylists(ArrayList<YoutubePlaylistInfo> source,
                                   ArrayList<YoutubePlaylistInfo> target) {
        for (YoutubePlaylistInfo p : source) {
            for (YoutubePlaylistInfo p1 : target) {
                if (p != null && p1 != null && p.id.equals(p1.id)) {
                    p1.title = p.title;
                    p1.imgeUrl = p.imgeUrl;
                    p1.videoCount = p.videoCount;
                    break;
                }
            }
        }
    }

    private CustomCallback buildAllPlaylistsCompletedListener() {

        return new CustomCallback() {
            @Override
            public void onFailure(Request request, IOException e) {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();
                            if (channelSectionInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS) {
                                channelSectionInfo.youtubeState = Constants.YoutubeState.WAITINGFORLOADINGITEMCOUNT;
                            } else {
                                channelSectionInfo.youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
                            }

                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();
                            AbstractMap.SimpleEntry<String, ArrayList<YoutubePlaylistInfo>> searchResult = YoutubeHelper
                                    .getPlaylists(s, true, false);
                            ArrayList<YoutubePlaylistInfo> playlists = searchResult
                                    .getValue();

                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();
                            if (channelSectionInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS) {
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.playlists = playlists;
                                channelInfo.hasMoreVideos = !Utils
                                        .stringIsNullOrEmpty(searchResult.getKey());

                                channelSectionInfo.youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
                                loadDataByPage();
                            } else {
                                populatePlaylists(
                                        playlists,
                                        (ArrayList<YoutubePlaylistInfo>) channelSectionInfo.dataInfo);
                                channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                                loadDataItems();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }

                    }
                });
            }
        };
    }

    public void loadChannelsOfMulti(ChannelSectionInfo activityInfo) {
        ArrayList<ChannelInfo> channels = (ArrayList<ChannelInfo>) activityInfo.dataInfo;

        String ids = "";
        int count = 0;
        for (ChannelInfo channelInfo : channels) {
            if (ids == "") {
                ids = channelInfo.id;
            } else {
                ids = ids + "," + channelInfo.id;
            }
            count++;
            if (count == Constants.MAX_SIZE_FOR_LOADING_YOUTUBE_DATA) {
                break;
            }
        }
        String url = String.format(
                PlayTubeController.getConfigInfo().loadChannelsInfoUrl, ids);
        new CustomHttpOk(url, new CustomCallback(activityInfo) {
            @Override
            public void onFailure(Request request, IOException e) {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();

                            channelSectionInfo.youtubeState = Constants.YoutubeState.WAITINGFORLOADINGITEMCOUNT;

                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {
                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();
                            ArrayList<ChannelInfo> channels = YoutubeHelper.getChannelList(s, (ArrayList<ChannelInfo>) channelSectionInfo.dataInfo);


                            populateChannels(
                                    channels,
                                    (ArrayList<ChannelInfo>) channelSectionInfo.dataInfo);

                            channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;

                            loadDataItems();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }
                });
            }
        }).start();
    }

    private void populateChannels(ArrayList<ChannelInfo> source,
                                  ArrayList<ChannelInfo> target) {
        for (int i = target.size() - 1; i >= 0; --i) {
            ChannelInfo c1 = target.get(i);
            boolean isPopulated = false;
            for (ChannelInfo c : source) {
                if (c != null && c1 != null && c.id.equals(c1.id)) {
                    c1.title = c.title;
                    c1.imageUrl = c.imageUrl;
                    c1.videoCount = c.videoCount;
                    c1.uploadPlaylistId = c.uploadPlaylistId;
                    c1.subscriberCount = c.subscriberCount;
                    isPopulated = true;
                    break;
                }
            }
            if (!isPopulated) {
                target.remove(i);
            }
        }
    }

    private void loadChannelVideos(ChannelSectionInfo activityInfo) {
        String url;
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
        switch (activityInfo.sortBy) {
            case Constants.SortBy.MOSTVIEWED: {
                url = String.format(
                        PlayTubeController.getConfigInfo().loadVideosInChannelSortByUrl,
                        "", channelInfo.id, PAGESIZE, VIEWCOUNT_SORTBY);
                break;
            }
            default: {
                url = String.format(
                        PlayTubeController.getConfigInfo().loadVideosInChannelSortByUrl,
                        "", channelInfo.id, PAGESIZE, DATE_SORTBY);
                break;
            }
        }
        new CustomHttpOk(url, getDownloadVideosInfoListener(activityInfo)).start();
    }

    private CustomCallback getDownloadVideosInfoListener(Object dataContext) {
        return new CustomCallback(dataContext) {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Utils.showMessage(Utils.getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_playlistItemViewInfos.size() == 0) {
                                initReloadEvent();
                            } else {

                                _adapter.setIsNetworkError(true);
                                _adapter.notifyDataSetChanged();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                final CustomCallback callback = this;
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = response.body().string();
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();


                            AbstractMap.SimpleEntry<String, ArrayList<YoutubeInfo>> searchResult;
                            if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                                searchResult = YoutubeHelper
                                        .getVideosInPlaylist(s, PAGESIZE);
                            } else if (channelSectionInfo.activityType == Constants.UserActivityType.UPLOADS) {
                                searchResult = YoutubeHelper.getVideoListInfo(s);
                            } else {
                                searchResult = YoutubeHelper.getVideosInAccount(s);
                            }
                            if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                                YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) channelSectionInfo.dataInfo;
                                playlistInfo.youtubeList = searchResult
                                        .getValue();

                                playlistInfo.hasMoreVideos = !Utils
                                        .stringIsNullOrEmpty(searchResult.getKey());
                                channelSectionInfo.youtubeState = Constants.YoutubeState.IDSLOADED;

                                loadDataItems();
                            } else {
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.youtubeList = searchResult.getValue();
                                channelInfo.hasMoreVideos = !Utils
                                        .stringIsNullOrEmpty(searchResult.getKey());

                                channelSectionInfo.youtubeState = Constants.YoutubeState.ITEMCOUNTLOADED;
                                loadDataByPage();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }

                    }
                });
            }
        };
    }

    private void loadChannelActivities(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;

        String url = String.format(
                PlayTubeController.getConfigInfo().loadActivitiesInChannelUrl, "",
                channelInfo.id, PAGESIZE);
        CustomCallback callback = buildChannelActivitiesCompletedListener();
        callback.setDataContext(activityInfo);
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    private void loadDataItems() {
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .get(i);
            if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                if (channelSectionInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) channelSectionInfo.dataInfo;
                    if (playlistInfo.videoCount > 0) {
                        channelSectionInfo.youtubeState = Constants.YoutubeState.LOADINGIDS;
                        String url = String
                                .format(PlayTubeController.getConfigInfo().loadVideosInPlaylistUrl, "",
                                        ((YoutubePlaylistInfo) channelSectionInfo.dataInfo).id, PAGE_SIZE);

                        new CustomHttpOk(url, getDownloadVideosInfoListener(channelSectionInfo)).start();
                    } else {
                        channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                    }
                }
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.UPLOADS
                    || channelSectionInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                channelSectionInfo.youtubeState = Constants.YoutubeState.IDSLOADED;
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
                if (channelSectionInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.IDSLOADED;
                    loadPlaylistsOfMulti(channelSectionInfo);
                }
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.MULTIPLECHANNELS) {
                if (channelSectionInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.IDSLOADED;
                    loadChannelsOfMulti(channelSectionInfo);
                }
            } else {
                channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
            }
        }
        boolean isCompleted = true;
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .get(i);
            if (channelSectionInfo.youtubeState != Constants.YoutubeState.DONE) {
                isCompleted = false;
                break;
            }
        }
        if (isCompleted) {
            bindData();
        }

        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .get(i);
            if (channelSectionInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                return;
            }
        }

        loadVideosInfo();
    }

    private void loadVideosInfo() {
        if (_hasVideoActivityOnly) {
            String videoIds = "";
            for (int i = 0; i < _playlistItemViewInfosLoading.size(); ++i) {
                YoutubeInfo videoInfo = (YoutubeInfo) _playlistItemViewInfosLoading
                        .get(i).dataInfo;
                if (Utils.stringIsNullOrEmpty(videoIds)) {
                    videoIds = videoInfo.id;
                } else {
                    videoIds = videoIds + "," + videoInfo.id;
                }
            }
            if (!Utils.stringIsNullOrEmpty(videoIds)) {
                String url = String.format(
                        PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                        videoIds);
                new CustomHttpOk(url, downloadVideoDetailsCompleted).start();
            } else {
                hideBusyAnimation();
                _isLoading = false;
            }
        } else {
            String videoIds = "";
            for (int i = 0; i < _activityListToLoadData.size(); ++i) {
                ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                        .get(i);
                if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.IDSLOADED) {
                        YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) channelSectionInfo.dataInfo;
                        for (YoutubeInfo videoInfo : playlistInfo.youtubeList) {
                            if (videoIds == "") {
                                videoIds = videoInfo.id;
                            } else {
                                videoIds = videoIds + "," + videoInfo.id;
                            }
                        }
                        if (playlistInfo.youtubeList.size() == 0) {
                            channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                        }
                    }
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.UPLOADS) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.IDSLOADED) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (YoutubeInfo videoInfo : channelInfo.youtubeList) {
                            if (videoIds == "") {
                                videoIds = videoInfo.id;
                            } else {
                                videoIds = videoIds + "," + videoInfo.id;
                            }
                        }
                        if (channelInfo.youtubeList.size() == 0) {
                            channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                        }
                    }
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.IDSLOADED) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (PlaylistItemInfo itemInfo : channelInfo.activities) {
                            if (videoIds == "") {
                                videoIds = ((YoutubeInfo) itemInfo.dataInfo).id;
                            } else {
                                videoIds = videoIds + ","
                                        + ((YoutubeInfo) itemInfo.dataInfo).id;
                            }
                        }
                        if (channelInfo.activities.size() == 0) {
                            channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                        }
                    }
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                }
            }

            if (Utils.stringIsNullOrEmpty(videoIds)) {
                // _isDataBinded = false;
                bindData();
                return;
            }

            String url = String.format(
                    PlayTubeController.getConfigInfo().loadVideosInfoUrl,
                    videoIds);
            new CustomHttpOk(url, downloadVideoDetailsCompleted).start();
        }
    }

    private CustomCallback downloadVideoDetailsCompleted = new CustomCallback() {
        @Override
        public void onFailure(Request request, IOException e) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.showMessage(Utils.getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {

                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
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

                        ArrayList<YoutubeInfo> youtubeList = YoutubeHelper
                                .getYoutubeList(s);
                        if (_hasVideoActivityOnly) {
                            populatePlaylistItemViewsData(youtubeList);
                        } else {
                            populatePlaylistsData(youtubeList);
                        }
                        bindData();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    hideBusyAnimation();
                    _isLoading = false;

                }
            });
        }
    };

    private void bindData() {
        if (!_hasVideoActivityOnly) {
            for (int i = 0; i < _activityListToLoadData.size(); ++i) {
                ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                        .get(i);
                if (channelSectionInfo.youtubeState != Constants.YoutubeState.DONE) {
                    return;
                }
            }
            if (_isDataBinded) {
                return;
            }
            hideBusyAnimation();
            _isDataBinded = true;
            if (_playlistItemViewInfos.size() > 0
                    && _playlistItemViewInfos.get(_playlistItemViewInfos
                    .size() - 1) == null) {
                _playlistItemViewInfos
                        .remove(_playlistItemViewInfos.size() - 1);
            }

            ArrayList<PlaylistItemInfo> items = getItems(_activityListToLoadData);
            _loadingIndexPerPage += _activityListToLoadData.size();
            _loadingIndexPerPageOld = _loadingIndexPerPage;
            _loadingIndexOld = _loadingIndex;
            _playlistItemViewInfos.addAll(items);
            if (!_isLoadCompleted || !_isLoadCompletedPerPage) {
                _playlistItemViewInfos.add(null);
            }

            _adapter.setDataSource(_playlistItemViewInfos);
        } else {
            hideBusyAnimation();
            if (_playlistItemViewInfos.size() > 0
                    && _playlistItemViewInfos.get(_playlistItemViewInfos
                    .size() - 1) == null) {
                _playlistItemViewInfos
                        .remove(_playlistItemViewInfos.size() - 1);
            }

            _playlistItemViewInfos.addAll(_playlistItemViewInfosLoading);
            if (!_isLoadCompleted) {
                _playlistItemViewInfos.add(null);
            }

            _adapter.setDataSource(_playlistItemViewInfos);
            _binding.listView.setDividerHeight(1);
        }
    }

    private void populatePlaylistsData(ArrayList<YoutubeInfo> youtubeList) {

        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .get(i);
            if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST
                    || channelSectionInfo.activityType == Constants.UserActivityType.UPLOADS
                    || channelSectionInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {

                ArrayList<YoutubeInfo> activityVideoList;
                if (channelSectionInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                    activityVideoList = new ArrayList<>();
                    ArrayList<PlaylistItemInfo> items = ((ChannelInfo) channelSectionInfo.dataInfo).activities;
                    for (PlaylistItemInfo item : items) {
                        activityVideoList.add((YoutubeInfo) item.dataInfo);
                    }
                } else {
                    activityVideoList = channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST ? ((YoutubePlaylistInfo) channelSectionInfo.dataInfo).youtubeList
                            : ((ChannelInfo) channelSectionInfo.dataInfo).youtubeList;
                }
                for (int j = activityVideoList.size() - 1; j >= 0; --j) {
                    YoutubeInfo videoInfo = activityVideoList.get(j);
                    boolean isExisted = false;
                    for (int k = 0; k < youtubeList.size(); ++k) {
                        if (youtubeList.get(k).id.equals(videoInfo.id)) {
                            YoutubeInfo newVideoInfo = youtubeList.get(k);

                            populateVideoInfo(videoInfo, newVideoInfo);
                            isExisted = true;
                            break;
                        }
                    }
                    if (!isExisted) {
                        activityVideoList.remove(j);
                    }
                }

                channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
            }
        }
    }

    private void populatePlaylistItemViewsData(ArrayList<YoutubeInfo> youtubeList) {
        for (int i = _playlistItemViewInfosLoading.size() - 1; i >= 0; --i) {
            YoutubeInfo videoInfo = (YoutubeInfo) _playlistItemViewInfosLoading
                    .get(i).dataInfo;
            boolean isExisted = false;
            for (int k = 0; k < youtubeList.size(); ++k) {
                if (youtubeList.get(k).id.equals(videoInfo.id)) {
                    YoutubeInfo newVideoInfo = youtubeList.get(k);

                    populateVideoInfo(videoInfo, newVideoInfo);
                    isExisted = true;
                    break;
                }
            }
            if (!isExisted) {
                _playlistItemViewInfosLoading.remove(i);
            }
        }

    }

    private void populateVideoInfo(YoutubeInfo videoInfo, YoutubeInfo newVideoInfo) {
        videoInfo.title = newVideoInfo.title;
        videoInfo.id = newVideoInfo.id;
        videoInfo.duration = newVideoInfo.duration;
        videoInfo.viewsNo = newVideoInfo.viewsNo;
        videoInfo.uploadedDate = newVideoInfo.uploadedDate;
        videoInfo.likesNo = newVideoInfo.likesNo;
        videoInfo.isLive = newVideoInfo.isLive;
        videoInfo.dislikesNo = newVideoInfo.dislikesNo;
        videoInfo.uploaderId = newVideoInfo.uploaderId;
        videoInfo.uploaderName = newVideoInfo.uploaderName;
        videoInfo.description = newVideoInfo.description;
        videoInfo.imageUrl = newVideoInfo.imageUrl;
    }

    public void hideBusyAnimation() {
        _isShowingBusy = false;
        Utils.hideProgressBar(_binding.layoutMain, _busyView);
    }

    public void showBusyAnimation() {
        if (_isShowingBusy) {
            return;
        }
        _isShowingBusy = true;
        _busyView = Utils.showProgressBar(_binding.layoutMain, _busyView);
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
                _adapter.checkChannelSubscribed(true);
                loadData();
                return false;
            }
        });
    }

    private ListViewBinding _binding;

    public void createView(ViewGroup group) {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, group, false);
        _adapter = new YoutubePlaylistItemByPageAdapter(getContext(), this, _channelInfo, _playlistItemViewInfos);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setSmoothScrollbarEnabled(false);
        _binding.listView.setOnItemClickListener(this);
        _binding.listView.setOnScrollListener(this);
        _binding.listView.setDividerHeight(0);
        _binding.listView.setOnTouchListener(new OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside
            // ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!_isParentScrollable) {
                    // Disallow the touch request for parent scroll on touch of
                    // child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long position) {
        int index = (int) position - 1;
        if (index == _playlistItemViewInfos.size() - 1
                && _playlistItemViewInfos.get(_playlistItemViewInfos.size() - 1) == null) {
            if (_adapter.getIsNetworkError()) {
                _adapter.setIsNetworkError(false);
                _adapter.notifyDataSetChanged();
                loadMoreData();
            }
        } else {
            if (index >= 0) {
                PlaylistItemInfo playlistItemViewInfo = _playlistItemViewInfos
                        .get(index);
                if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.YOUTUBE) {

                    MainActivity.getInstance().playYoutube(
                            (YoutubeInfo) playlistItemViewInfo.dataInfo,
                            playlistItemViewInfo.getYoutubeList(), true);
                } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SHOWMORE) {
                    ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) playlistItemViewInfo.activityInfo;
                    if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                        YoutubePlaylistVideosFragment youtubePlaylistDetails = YoutubePlaylistVideosFragment
                                .newInstance(
                                        (YoutubePlaylistInfo) channelSectionInfo.dataInfo);
                        MainActivity.getInstance().addFragment(
                                youtubePlaylistDetails);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.UPLOADS) {
                        UserVideosFragment userVideosView = UserVideosFragment
                                .newInstance(_channelInfo, channelSectionInfo.sortBy);
                        MainActivity.getInstance().addFragment(
                                userVideosView);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                        ChannelActivityVideosView channelVideosView = ChannelActivityVideosView
                                .newInstance((ChannelInfo) channelSectionInfo.dataInfo);
                        MainActivity.getInstance().addFragment(
                                channelVideosView);
                    }
// else if (channelSectionInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS
//                            || channelSectionInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
//                        UserPlaylistsView userPlaylistsView = UserPlaylistsView
//                                .newInstance(_channelInfo.id,
//                                        playlistItemViewInfo.activityInfo);
//                        MainActivity.getInstance().launchFragment(
//                                userPlaylistsView);
//                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.MULTIPLECHANNELS) {
//                        UserChanelsView chanelsView = UserChanelsView
//                                .newInstance(_channelInfo.id,
//                                        playlistItemViewInfo.activityInfo);
//                        MainActivity.getInstance().launchFragment(chanelsView);
//                    }
                } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.PLAYLIST) {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) playlistItemViewInfo.dataInfo;
                    YoutubePlaylistVideosFragment youtubePlaylistDetails = YoutubePlaylistVideosFragment
                            .newInstance(playlistInfo);
                    MainActivity.getInstance().addFragment(
                            youtubePlaylistDetails);
                } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.CHANNEL) {
                    ChannelInfo channelInfo = (ChannelInfo) playlistItemViewInfo.dataInfo;
                    UserDetailsFragment userDetails = UserDetailsFragment
                            .newInstance(channelInfo);
                    MainActivity.getInstance().addFragment(userDetails);
                } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADED
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.COMMENTED
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADEDANDPOSTED
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.RECOMMENDED
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SUBSCRIBED
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.OTHERACTION
                        || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.LIKED) {
                    ArrayList<YoutubeInfo> youtubeList = new ArrayList<YoutubeInfo>();
                    if (_hasVideoActivityOnly) {
                        for (PlaylistItemInfo itemViewInfo : _playlistItemViewInfos) {
                            if (itemViewInfo != null
                                    && itemViewInfo.dataInfo instanceof YoutubeInfo) {
                                YoutubeInfo videoInfo = (YoutubeInfo) itemViewInfo.dataInfo;
                                youtubeList.add(videoInfo);
                            }
                        }
                    } else {
                        ArrayList<PlaylistItemInfo> items = ((ChannelInfo) playlistItemViewInfo.activityInfo.dataInfo).activities;
                        for (PlaylistItemInfo item : items) {
                            youtubeList.add((YoutubeInfo) item.dataInfo);
                        }
                    }

                    MainActivity.getInstance().playYoutube(
                            (YoutubeInfo) playlistItemViewInfo.dataInfo,
                            youtubeList, true);
                }
            }
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
                if (_playlistItemViewInfos.size() > 0
                        && _playlistItemViewInfos
                        .get(_playlistItemViewInfos.size() - 1) == null) {
                    if (!_adapter.getIsNetworkError()) {
                        loadMoreData();
                    }
                }
            }
        }
    }

    public void doPendingAction() {
        if (_adapter != null) {
            _adapter.doPendingAction();
        }
    }

    private ArrayList<PlaylistItemInfo> getItems(
            ArrayList<ChannelSectionInfo> activityList) {
        ArrayList<PlaylistItemInfo> results = new ArrayList<>();

        for (int i = 0; i < activityList.size(); ++i) {
            ChannelSectionInfo activityInfo = activityList.get(i);
            if (activityInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) activityInfo.dataInfo;
                if (playlistInfo.youtubeList.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = playlistInfo.title;
                    playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.NAME;
                    results.add(playlistItemViewInfo);
                    for (int k = 0; k < playlistInfo.youtubeList.size(); ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlistInfo.youtubeList
                                .get(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.YOUTUBE;
                        results.add(playlistItemViewInfo);
                    }

                    if (playlistInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlistInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.SHOWMORE;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.DIVIDER;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.UPLOADS
                    || activityInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {

                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                boolean hasData = activityInfo.activityType == Constants.UserActivityType.UPLOADS ? channelInfo.youtubeList
                        .size() > 0 : channelInfo.activities.size() > 0;
                if (hasData) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    String title = activityInfo.activityType == Constants.UserActivityType.UPLOADS ? (activityInfo.sortBy == Constants.SortBy.MOSTRECENT ? Utils
                            .getString(R.string.recent_uploads) : Utils
                            .getString(R.string.popular_uploads))
                            : Utils.getString(R.string.recent_activities);
                    playlistItemViewInfo.dataInfo = title;
                    playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.NAME;
                    results.add(playlistItemViewInfo);
                    if (activityInfo.activityType == Constants.UserActivityType.RECENTACTIVIY) {
                        for (int k = 0; k < channelInfo.activities
                                .size(); ++k) {
                            channelInfo.activities.get(k).activityInfo = activityInfo;
                        }
                        results.addAll(channelInfo.activities);
                    } else {
                        for (int k = 0; k < channelInfo.youtubeList.size(); ++k) {
                            playlistItemViewInfo = new PlaylistItemInfo();
                            playlistItemViewInfo.dataInfo = channelInfo.youtubeList
                                    .get(k);
                            playlistItemViewInfo.activityInfo = activityInfo;
                            playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.YOUTUBE;
                            results.add(playlistItemViewInfo);
                        }
                    }
                    if (channelInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.SHOWMORE;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.DIVIDER;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.ALLPLAYLISTS) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;

                if (channelInfo.playlists.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = Utils
                            .getString(R.string.created_playlists);
                    playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.NAME;
                    results.add(playlistItemViewInfo);
                    for (int k = 0; k < channelInfo.playlists.size(); ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = channelInfo.playlists
                                .get(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.PLAYLIST;
                        results.add(playlistItemViewInfo);
                    }

                    if (channelInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.SHOWMORE;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.DIVIDER;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.MULTIPLEPLAYLISTS) {
                ArrayList<YoutubePlaylistInfo> playlists = (ArrayList<YoutubePlaylistInfo>) activityInfo.dataInfo;

                if (playlists.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = activityInfo.title;
                    playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.NAME;
                    results.add(playlistItemViewInfo);
                    int videoCount = playlists.size() > PAGESIZE ? PAGESIZE
                            : playlists.size();
                    for (int k = 0; k < videoCount; ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlists.get(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.PLAYLIST;
                        results.add(playlistItemViewInfo);
                    }

                    if (playlists.size() > PAGESIZE) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.SHOWMORE;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.DIVIDER;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.MULTIPLECHANNELS) {
                ArrayList<ChannelInfo> channels = (ArrayList<ChannelInfo>) activityInfo.dataInfo;

                if (channels.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = activityInfo.title;
                    playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.NAME;
                    results.add(playlistItemViewInfo);
                    int videoCount = channels.size() > PAGESIZE ? PAGESIZE
                            : channels.size();
                    for (int k = 0; k < videoCount; ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = channels.get(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.CHANNEL;
                        results.add(playlistItemViewInfo);
                    }

                    if (channels.size() > PAGESIZE) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.SHOWMORE;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = Constants.PlaylistItemType.DIVIDER;
                        results.add(playlistItemViewInfo);
                    }
                }
            }
        }
        return results;
    }

    public void notifyDataSetChanged() {
        if (_adapter != null) {
            _adapter.notifyDataSetChanged();
        }
    }
}
