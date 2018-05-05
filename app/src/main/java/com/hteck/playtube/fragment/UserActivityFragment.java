package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

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
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Vector;

public class UserActivityFragment extends Fragment implements
        AdapterView.OnItemClickListener, OnScrollListener {
    private String _nextPageToken = "";
    private ViewGroup _contentView;
    ArrayList<PlaylistItemInfo> _playlistItemViewInfos = new ArrayList<>();
    ArrayList<PlaylistItemInfo> _playlistItemViewInfosLoading = new ArrayList<>();
    ArrayList<ChannelSectionInfo> _activityListAll = new ArrayList<>();
    ArrayList<ChannelSectionInfo> _activityListLoading = new ArrayList<>();
    YoutubePlaylistItemByPageAdapter _adapter;
    ListView _listView;
    private View _viewReload;
    private LoadingView _busyView;
    private boolean _isLoading;
    private int _loadingIndex = 0;
    private int _loadingIndexOld = 0;
    private int _loadingIndexPerPage = 0;
    private int _loadingIndexPerPageOld = 0;
    private final int PAGESIZEPRE = 20;
    private final int PAGESIZE = 4;
    private final int PAGESIZE_PLAYLIST = 50;
    private ChannelInfo _channelInfo;
    private boolean _isLoadCompleted;
    private boolean _isLoadCompletedPerPage;
    private boolean _isDataBinded;
    private boolean _isLoadedByPage;
    Vector<ChannelSectionInfo> _activityListToLoadData = new Vector<>();
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

        if (_contentView == null) {
            createView();
        }

        loadData();
        return _contentView;
    }

    public void setParentScrollable(boolean isScrollable) {
        _isParentScrollable = isScrollable;
    }

    private void loadData() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        _activityListToLoadData = new Vector<>();
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
        return  new CustomCallback() {
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
        String ids = "";
        if (isNewRound) {
            _activityListLoading = new ArrayList<>();
        }
        boolean isWaitting = false;
        for (int i = _loadingIndex; i < count; ++i) {
            ChannelSectionInfo activityInfo = _activityListAll.get(i);
            switch (activityInfo.activityType) {
                case Constants.UserActivityType.SINGLEPLAYLIST: {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) activityInfo.dataInfo;
                    if (ids == "") {
                        ids = playlistInfo.id;
                    } else {
                        ids = ids + "," + playlistInfo.id;
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
        if (!Utils.stringIsNullOrEmpty(ids)) {
            String url = String.format(
                    PlayTubeController.getConfigInfo().loadPlaylistsDetailsUrl, ids);
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
                Vector<YoutubePlaylistInfo> playlists = (Vector<YoutubePlaylistInfo>) activityInfo.dataInfo;
                int playlistCount = playlists.size() > PAGESIZE ? PAGESIZE
                        : playlists.size();
                if (count + playlistCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += playlistCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == Constants.UserActivityType.MULTIPLECHANNELS) {
                Vector<ChannelInfo> channels = (Vector<ChannelInfo>) activityInfo.dataInfo;
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
            _activityListToLoadData = new Vector<ChannelSectionInfo>();
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
                channelInfo.id);
        CustomCallback callback = buildAllPlaylistsCompletedListener();
        callback.setDataContext(activityInfo);
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    public void loadPlaylistsOfMulti(ChannelSectionInfo activityInfo) {
        Vector<YoutubePlaylistInfo> playlists = (Vector<YoutubePlaylistInfo>) activityInfo.dataInfo;

        int count = 0;
        String ids = "";
        for (YoutubePlaylistInfo playlistInfo : playlists) {
            ids = ids == "" ? playlistInfo.id : (ids + "," + playlistInfo.id);

            count++;
            if (count == Constants.MAX_SIZE_FOR_LOADING_YOUTUBE_DATA) {
                break;
            }
        }
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
        Vector<ChannelInfo> channels = (Vector<ChannelInfo>) activityInfo.dataInfo;

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
                            ArrayList<ChannelInfo> channels = YoutubeHelper.getChannels(s);

                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) callback.getDataContext();
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
                        "", channelInfo.id, "viewCount");
                break;
            }
            case Constants.SortBy.MOSTRECENT: {
                url = String.format(
                        PlayTubeController.getConfigInfo().loadVideosInChannelSortByUrl,
                        "", channelInfo.id, "date");
                break;
            }
            default: {
                url = String.format(
                        PlayTubeController.getConfigInfo().loadVideosInChannelUrl,
                        "", channelInfo.id);
                break;
            }
        }
        new CustomHttpOk(url, new CustomCallback(activityInfo) {
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
        }).start();
    }

    private void loadChannelActivities(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;

        String url = String.format(
                PlayTubeController.getConfigInfo().loadActivitiesInChannelUrl,
                channelInfo.id, "", PAGESIZE);
        CustomCallback callback = buildChannelActivitiesCompletedListener();
        callback.setDataContext(activityInfo);
        CustomHttpOk httpOk = new CustomHttpOk(url, callback);
        httpOk.start();
    }

    private void loadDataItems() {
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
            if (channelSectionInfo.activityType == Constants.UserActivityType.SINGLEPLAYLIST) {
                if (channelSectionInfo.youtubeState < Constants.YoutubeState.IDSLOADED) {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) channelSectionInfo.dataInfo;
                    if (playlistInfo.videoCount > 0) {
                        channelSectionInfo.youtubeState = Constants.YoutubeState.LOADINGIDS;
                        String url = String
                                .format(PlayTubeController.getConfigInfo().loadVideosInPlaylistUrl,
                                        ((YoutubePlaylistInfo) channelSectionInfo.dataInfo).id,
                                        "", PAGESIZEPRE);
                        HttpGetFile httpGetFile = Utils.download(url,
                                downloadCompletedVidesInfo);
                        httpGetFile.dataContext = channelSectionInfo;
                    } else {
                        channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                    }
                }
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.Uploads
                    || channelSectionInfo.activityType == Constants.UserActivityType.RecentActiviy) {
                channelSectionInfo.youtubeState = Constants.YoutubeState.LoadedIds;
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.MultiplePlaylists) {
                if (channelSectionInfo.youtubeState.getValue() < Constants.YoutubeState.LoadedIds
                        .getValue()) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.LoadedIds;
                    loadPlaylistsOfMulti(channelSectionInfo);
                }
            } else if (channelSectionInfo.activityType == Constants.UserActivityType.MultipleChannels) {
                if (channelSectionInfo.youtubeState.getValue() < Constants.YoutubeState.LoadedIds
                        .getValue()) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.LoadedIds;
                    loadChannelsOfMulti(channelSectionInfo);
                }
            } else {
                channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
            }
        }
        boolean isCompleted = true;
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
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
                    .elementAt(i);
            if (channelSectionInfo.youtubeState.getValue() < Constants.YoutubeState.LoadedIds
                    .getValue()) {
                return;
            }
        }

        loadVideosInfo();
    }

    private IEventHandler downloadCompletedVidesInfo = new IEventHandler() {

        @Override
        public void returnResult(final Object sender,
                                 final ResultType resultType, final byte[] data) {

        }
    };

    private void loadVideosInfo() {
        if (_hasVideoActivityOnly) {
            String videoIds = "";
            for (int i = 0; i < _playlistItemViewInfosLoading.size(); ++i) {
                VideoInfo videoInfo = (VideoInfo) _playlistItemViewInfosLoading
                        .elementAt(i).dataInfo;
                if (Utils.stringIsNullOrEmpty(videoIds)) {
                    videoIds = videoInfo.id;
                } else {
                    videoIds = videoIds + "," + videoInfo.id;
                }
            }
            if (!Utils.stringIsNullOrEmpty(videoIds)) {
                String url = String.format(
                        MainContext.getDevKeyInfo().getVideosDetailApiUrl,
                        videoIds);
                Utils.download(url, downloadVideoDetailsCompleted);
            } else {
                hideBusyAnimation();
                _isLoading = false;
            }
        } else {
            String videoIds = "";
            for (int i = 0; i < _activityListToLoadData.size(); ++i) {
                ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                        .elementAt(i);
                if (channelSectionInfo.activityType == Constants.UserActivityType.SinglePlaylist) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.LoadedIds) {
                        YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) channelSectionInfo.dataInfo;
                        for (VideoInfo videoInfo : playlistInfo.youtubeList) {
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
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.Uploads) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.LoadedIds) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (VideoInfo videoInfo : channelInfo.youtubeList) {
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
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.RecentActiviy) {
                    if (channelSectionInfo.youtubeState == Constants.YoutubeState.LoadedIds) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (PlaylistItemInfo itemInfo : channelInfo.activities) {
                            if (videoIds == "") {
                                videoIds = ((VideoInfo) itemInfo.dataInfo).id;
                            } else {
                                videoIds = videoIds + ","
                                        + ((VideoInfo) itemInfo.dataInfo).id;
                            }
                        }
                        if (channelInfo.activities.size() == 0) {
                            channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                        }
                    }
                } else if (channelSectionInfo.activityType == Constants.UserActivityType.AllPlaylists) {
                    channelSectionInfo.youtubeState = Constants.YoutubeState.DONE;
                }
            }

            if (Utils.stringIsNullOrEmpty(videoIds)) {
                // _isDataBinded = false;
                bindData();
                return;
            }
            String url = String
                    .format(MainContext.getDevKeyInfo().getVideosDetailApiUrl,
                            videoIds);
            Utils.download(url, downloadVideoDetailsCompleted);
        }
    }

    private IEventHandler downloadVideoDetailsCompleted = new IEventHandler() {

        @Override
        public void returnResult(Object sender, final ResultType resultType,
                                 final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);

                            Vector<VideoInfo> youtubeList = YoutubeHelper
                                    .getVideoList(s);
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

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {

                            _adapter.setIsNetworkError(true);
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void bindData() {
        if (!_hasVideoActivityOnly) {
            for (int i = 0; i < _activityListToLoadData.size(); ++i) {
                ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                        .elementAt(i);
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
                    && _playlistItemViewInfos.elementAt(_playlistItemViewInfos
                    .size() - 1) == null) {
                _playlistItemViewInfos
                        .remove(_playlistItemViewInfos.size() - 1);
            }

            Vector<PlaylistItemInfo> items = getItems(_activityListToLoadData);
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
                    && _playlistItemViewInfos.elementAt(_playlistItemViewInfos
                    .size() - 1) == null) {
                _playlistItemViewInfos
                        .remove(_playlistItemViewInfos.size() - 1);
            }

            _playlistItemViewInfos.addAll(_playlistItemViewInfosLoading);
            if (!_isLoadCompleted) {
                _playlistItemViewInfos.add(null);
            }

            _adapter.setDataSource(_playlistItemViewInfos);
            _listView.setDividerHeight(1);
        }
    }

    private void populatePlaylistsData(Vector<VideoInfo> youtubeList) {

        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
            if (channelSectionInfo.activityType == Constants.UserActivityType.SinglePlaylist
                    || channelSectionInfo.activityType == Constants.UserActivityType.Uploads
                    || channelSectionInfo.activityType == Constants.UserActivityType.RecentActiviy) {

                Vector<VideoInfo> activityVideoList;
                if (channelSectionInfo.activityType == Constants.UserActivityType.RecentActiviy) {
                    activityVideoList = new Vector<VideoInfo>();
                    Vector<PlaylistItemInfo> items = ((ChannelInfo) channelSectionInfo.dataInfo).activities;
                    for (PlaylistItemInfo item : items) {
                        activityVideoList.add((VideoInfo) item.dataInfo);
                    }
                } else {
                    activityVideoList = channelSectionInfo.activityType == Constants.UserActivityType.SinglePlaylist ? ((YoutubePlaylistInfo) channelSectionInfo.dataInfo).youtubeList
                            : ((ChannelInfo) channelSectionInfo.dataInfo).youtubeList;
                }
                for (int j = activityVideoList.size() - 1; j >= 0; --j) {
                    VideoInfo videoInfo = activityVideoList.elementAt(j);
                    boolean isExisted = false;
                    for (int k = 0; k < youtubeList.size(); ++k) {
                        if (youtubeList.elementAt(k).id.equals(videoInfo.id)) {
                            VideoInfo newVideoInfo = youtubeList.elementAt(k);

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

    private void populatePlaylistItemViewsData(Vector<VideoInfo> youtubeList) {
        for (int i = _playlistItemViewInfosLoading.size() - 1; i >= 0; --i) {
            VideoInfo videoInfo = (VideoInfo) _playlistItemViewInfosLoading
                    .elementAt(i).dataInfo;
            boolean isExisted = false;
            for (int k = 0; k < youtubeList.size(); ++k) {
                if (youtubeList.elementAt(k).id.equals(videoInfo.id)) {
                    VideoInfo newVideoInfo = youtubeList.elementAt(k);

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

    private void populateVideoInfo(VideoInfo videoInfo, VideoInfo newVideoInfo) {
        videoInfo.title = newVideoInfo.title;
        videoInfo.id = newVideoInfo.id;
        videoInfo.duration = newVideoInfo.duration;
        videoInfo.totalView = newVideoInfo.totalView;
        videoInfo.publishedDate = newVideoInfo.publishedDate;
        videoInfo.numLikes = newVideoInfo.numLikes;
        videoInfo.isLive = newVideoInfo.isLive;
        videoInfo.numDislikes = newVideoInfo.numDislikes;
        videoInfo.authorId = newVideoInfo.authorId;
        videoInfo.authorName = newVideoInfo.authorName;
        videoInfo.description = newVideoInfo.description;
        videoInfo.thumbnailUrl = newVideoInfo.thumbnailUrl;
        videoInfo.isDeleted = newVideoInfo.isDeleted;
    }

    public void hideBusyAnimation() {
        _isShowingBusy = false;
        Utils.hideBusyAnimation(_contentView, _busyView);
    }

    public void showBusyAnimation() {
        if (_isShowingBusy) {
            return;
        }
        _isShowingBusy = true;
        _busyView = Utils.showBusyAnimation(_contentView, _busyView);
    }

    private void initReloadEvent() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _viewReload = (ViewGroup) inflater.inflate(R.layout.reload_view, null);
        _contentView.addView(_viewReload);
        _viewReload.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_viewReload != null) {
                    _contentView.removeView(_viewReload);
                    _viewReload = null;
                }
                _adapter.checkChannelSubscribed(true);
                loadData();
                return false;
            }
        });
    }

    public void createView() {
        _contentView = (ViewGroup) MainActivity.getInstance()
                .getLayoutInflater().inflate(R.layout.layout_container, null);
        _listView = new ListView(getActivity());
        _adapter = new YoutubePlaylistItemByPageAdapter(_playlistItemViewInfos,
                _channelInfo, this);
        _listView.setAdapter(_adapter);
        _listView.setSmoothScrollbarEnabled(false);
        _listView.setOnItemClickListener(this);
        _listView.setOnScrollListener(this);
        _listView.setDividerHeight(0);
        _listView.setOnTouchListener(new OnTouchListener() {
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
        _contentView.addView(_listView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long position) {
        int index = (int) position - 1;
        if (index == _playlistItemViewInfos.size() - 1
                && _playlistItemViewInfos.lastElement() == null) {
            if (_isNetworkError) {
                _isNetworkError = false;
                _adapter.mIsNetworkError = false;
                _adapter.notifyDataSetChanged();
                loadMoreData();
            }
        } else {
            if (index >= 0) {
                PlaylistItemInfo playlistItemViewInfo = _playlistItemViewInfos
                        .elementAt(index);
                if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Video) {

                    MainActivity.getInstance().play(
                            (VideoInfo) playlistItemViewInfo.dataInfo,
                            playlistItemViewInfo.getVideoList(), true);
                } else if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.ShowMore) {
                    ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) playlistItemViewInfo.activityInfo;
                    if (channelSectionInfo.activityType == Constants.UserActivityType.SinglePlaylist) {
                        YoutubePlaylistVideosView youtubePlaylistDetails = YoutubePlaylistVideosView
                                .newInstance(
                                        (YoutubePlaylistInfo) channelSectionInfo.dataInfo,
                                        VideoListType.Normal);
                        MainActivity.getInstance().launchFragment(
                                youtubePlaylistDetails);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.Uploads) {
                        UserVideosView userVideosView = UserVideosView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(
                                userVideosView);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.RecentActiviy) {
                        MyVideosView channelVideosView = MyVideosView
                                .newInstance((ChannelInfo) channelSectionInfo.dataInfo);
                        MainActivity.getInstance().launchFragment(
                                channelVideosView);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.AllPlaylists
                            || channelSectionInfo.activityType == Constants.UserActivityType.MultiplePlaylists) {
                        UserPlaylistsView userPlaylistsView = UserPlaylistsView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(
                                userPlaylistsView);
                    } else if (channelSectionInfo.activityType == Constants.UserActivityType.MultipleChannels) {
                        UserChanelsView chanelsView = UserChanelsView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(chanelsView);
                    }
                } else if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Playlist) {
                    YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) playlistItemViewInfo.dataInfo;
                    YoutubePlaylistVideosView youtubePlaylistDetails = YoutubePlaylistVideosView
                            .newInstance(playlistInfo, VideoListType.Normal);
                    MainActivity.getInstance().launchFragment(
                            youtubePlaylistDetails);
                } else if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Channel) {
                    ChannelInfo channelInfo = (ChannelInfo) playlistItemViewInfo.dataInfo;
                    UserDetails userDetails = UserDetails
                            .newInstance(channelInfo);
                    MainActivity.getInstance().launchFragment(userDetails);
                } else if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Uploaded
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Commented
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.UploadedAndPosted
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Recommended
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Subscribed
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.OtherAction
                        || playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Liked) {
                    Vector<VideoInfo> youtubeList = new Vector<VideoInfo>();
                    if (_hasVideoActivityOnly) {
                        for (PlaylistItemInfo itemViewInfo : _playlistItemViewInfos) {
                            if (itemViewInfo != null
                                    && itemViewInfo.dataInfo instanceof VideoInfo) {
                                VideoInfo videoInfo = (VideoInfo) itemViewInfo.dataInfo;
                                youtubeList.add(videoInfo);
                            }
                        }
                    } else {
                        Vector<PlaylistItemInfo> items = ((ChannelInfo) playlistItemViewInfo.activityInfo.dataInfo).activities;
                        for (PlaylistItemInfo item : items) {
                            youtubeList.add((VideoInfo) item.dataInfo);
                        }
                    }

                    MainActivity.getInstance().play(
                            (VideoInfo) playlistItemViewInfo.dataInfo,
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
            if (_listView.getLastVisiblePosition() == _listView.getAdapter()
                    .getCount() - 1) {
                if (_playlistItemViewInfos.size() > 0
                        && _playlistItemViewInfos
                        .elementAt(_playlistItemViewInfos.size() - 1) == null) {
                    if (!_isNetworkError) {
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

    private Vector<PlaylistItemInfo> getItems(
            Vector<ChannelSectionInfo> activityList) {
        Vector<PlaylistItemInfo> results = new Vector<PlaylistItemInfo>();

        for (int i = 0; i < activityList.size(); ++i) {
            ChannelSectionInfo activityInfo = activityList.elementAt(i);
            if (activityInfo.activityType == Constants.UserActivityType.SinglePlaylist) {
                YoutubePlaylistInfo playlistInfo = (YoutubePlaylistInfo) activityInfo.dataInfo;
                if (playlistInfo.youtubeList.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = playlistInfo.title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    for (int k = 0; k < playlistInfo.youtubeList.size(); ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlistInfo.youtubeList
                                .elementAt(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Video;
                        results.add(playlistItemViewInfo);
                    }

                    if (playlistInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlistInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.ShowMore;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Separator;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.Uploads
                    || activityInfo.activityType == Constants.UserActivityType.RecentActiviy) {

                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                boolean hasData = activityInfo.activityType == Constants.UserActivityType.Uploads ? channelInfo.youtubeList
                        .size() > 0 : channelInfo.activities.size() > 0;
                if (hasData) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    String title = activityInfo.activityType == Constants.UserActivityType.Uploads ? (activityInfo.sortBy == SortBy.MostRecent ? Utils
                            .getString(R.string.recent_uploads) : Utils
                            .getString(R.string.popular_uploads))
                            : Utils.getString(R.string.recent_activities);
                    playlistItemViewInfo.dataInfo = title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    if (activityInfo.activityType == Constants.UserActivityType.RecentActiviy) {
                        for (int k = 0; k < channelInfo.activities
                                .size(); ++k) {
                            channelInfo.activities.elementAt(k).activityInfo = activityInfo;
                        }
                        results.addAll(channelInfo.activities);
                    } else {
                        for (int k = 0; k < channelInfo.youtubeList.size(); ++k) {
                            playlistItemViewInfo = new PlaylistItemInfo();
                            playlistItemViewInfo.dataInfo = channelInfo.youtubeList
                                    .elementAt(k);
                            playlistItemViewInfo.activityInfo = activityInfo;
                            playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Video;
                            results.add(playlistItemViewInfo);
                        }
                    }
                    if (channelInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.ShowMore;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Separator;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.AllPlaylists) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;

                if (channelInfo.playlists.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = Utils
                            .getString(R.string.created_playlists);
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    for (int k = 0; k < channelInfo.playlists.size(); ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = channelInfo.playlists
                                .elementAt(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Playlist;
                        results.add(playlistItemViewInfo);
                    }

                    if (channelInfo.hasMoreVideos) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.ShowMore;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Separator;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.MultiplePlaylists) {
                Vector<YoutubePlaylistInfo> playlists = (Vector<YoutubePlaylistInfo>) activityInfo.dataInfo;

                if (playlists.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = activityInfo.title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    int videoCount = playlists.size() > PAGESIZE ? PAGESIZE
                            : playlists.size();
                    for (int k = 0; k < videoCount; ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlists.elementAt(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Playlist;
                        results.add(playlistItemViewInfo);
                    }

                    if (playlists.size() > PAGESIZE) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.ShowMore;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Separator;
                        results.add(playlistItemViewInfo);
                    }
                }
            } else if (activityInfo.activityType == Constants.UserActivityType.MultipleChannels) {
                Vector<ChannelInfo> channels = (Vector<ChannelInfo>) activityInfo.dataInfo;

                if (channels.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = activityInfo.title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    int videoCount = channels.size() > PAGESIZE ? PAGESIZE
                            : channels.size();
                    for (int k = 0; k < videoCount; ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = channels.elementAt(k);
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Channel;
                        results.add(playlistItemViewInfo);
                    }

                    if (channels.size() > PAGESIZE) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = activityInfo;
                        playlistItemViewInfo.activityInfo = activityInfo;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.ShowMore;
                        results.add(playlistItemViewInfo);
                    } else {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = null;
                        playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Separator;
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
