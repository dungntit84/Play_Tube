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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.ChannelSectionInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.service.YoutubeHelper;
import com.hteck.playtube.view.LoadingView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Vector;

public class UserActivityFragment extends Fragment implements
        AdapterView.OnItemClickListener, OnScrollListener {
    private String _nextPageToken = "";
    private ViewGroup _contentView;
    Vector<PlaylistItemInfo> _playlistItemViewInfos = new Vector<>();
    Vector<PlaylistItemInfo> _playlistItemViewInfosLoading = new Vector<>();
    Vector<ChannelSectionInfo> _activityListAll = new Vector<>();
    Vector<ChannelSectionInfo> _activityListLoading = new Vector<>();
    YoutubePlaylistItemByPageAdapter _adapter;
    private boolean _isNetworkError;
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
    Vector<ChannelSectionInfo> _activityListToLoadData = new Vector<ChannelSectionInfo>();
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
        CustomHttpOk httpOk = new CustomHttpOk(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showMessage(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
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
                MainContext.getDevKeyInfo().getActivitiesInChannelApiUrl,
                _channelInfo.id, _nextPageToken, Constants.PAGE_SIZE);
        Utils.download(url, downloadChannelActivitiesCompleted);
    }

    private IEventHandler downloadChannelActivitiesCompleted = new IEventHandler() {

        @Override
        public void returnResult(final Object sender,
                                 final ResultType resultType, final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);
                            KeyPairValue<String, Vector<PlaylistItemInfo>> activityListInfos = YoutubeHelper
                                    .getChannelActivities(s);
                            if (_hasVideoActivityOnly) {
                                _playlistItemViewInfosLoading = activityListInfos
                                        .getValue();
                                _isLoadCompleted = Utils
                                        .isNullOrEmpty(activityListInfos
                                                .getKey());
                                _nextPageToken = activityListInfos.getKey();
                                if (_playlistItemViewInfosLoading.size() > 0) {
                                    loadVideosInfo();
                                } else {
                                    _isLoading = false;
                                    hideBusyAnimation();
                                }
                            } else {
                                HttpGetFile httpGetFile = (HttpGetFile) sender;
                                ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) httpGetFile.dataContext;
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.channelActivities = activityListInfos
                                        .getValue();
                                channelInfo.hasMoreVideos = !Utils
                                        .isNullOrEmpty(activityListInfos
                                                .getKey());

                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
                                loadDataByPage();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void loadActivityInfos(boolean isNewRound) {
        _isNewRound = isNewRound;
        int count = (_loadingIndex + PAGESIZE_PLAYLIST) > _activityListAll
                .size() ? _activityListAll.size() : _loadingIndex
                + PAGESIZE_PLAYLIST;
        boolean isPendingToLoadingInfo = false;
        for (int i = _loadingIndex; i < count; ++i) {
            ChannelSectionInfo activityInfo = _activityListAll.elementAt(i);
            if (activityInfo.loadingVideosDataState == LoadingVideosDataState.NotStarted) {
                if (activityInfo.activityType == ChannelActivityType.Likes) {
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
            _activityListLoading = new Vector<ChannelSectionInfo>();
        }
        boolean isWaitting = false;
        for (int i = _loadingIndex; i < count; ++i) {
            ChannelSectionInfo activityInfo = _activityListAll.elementAt(i);
            if (activityInfo.activityType == ChannelActivityType.SinglePlaylist) {
                PlaylistInfo playlistInfo = (PlaylistInfo) activityInfo.dataInfo;
                if (ids == "") {
                    ids = playlistInfo.id;
                } else {
                    ids = ids + "," + playlistInfo.id;
                }
            } else if (activityInfo.activityType == ChannelActivityType.Uploads) {
                loadChannelVideos(activityInfo);
                isWaitting = true;
            } else if (activityInfo.activityType == ChannelActivityType.RecentActiviy) {
                loadChannelActivities(activityInfo);
                isWaitting = true;
            } else if (activityInfo.activityType == ChannelActivityType.AllPlaylists) {
                loadAllPlaylistsOfChannel(activityInfo);
                isWaitting = true;
            }
            _activityListLoading.add(activityInfo);
        }
        if (!Utils.isNullOrEmpty(ids)) {
            String url = String.format(
                    MainContext.getDevKeyInfo().getPlaylistsDetailsApiUrl, ids);
            Utils.download(url, downloadCompletedPlaylistsDetails);
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
            ChannelSectionInfo activityInfo = _activityListLoading.elementAt(i);
            if (activityInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.loadedItemCount
                    .getValue()) {
                return;
            }
            if (activityInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.LoadedIds
                    .getValue()) {
                activityInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
            }
        }
        if (_isLoadedByPage) {
            return;
        }
        _isLoadedByPage = true;
        int count = 0;
        boolean isFinised = true;

        for (int i = _loadingIndexPerPage; i < _activityListLoading.size(); ++i) {
            ChannelSectionInfo activityInfo = _activityListLoading.elementAt(i);
            if (activityInfo.activityType == ChannelActivityType.SinglePlaylist) {
                PlaylistInfo playlistInfo = (PlaylistInfo) activityInfo.dataInfo;
                int videoCount = playlistInfo.numVideos > PAGESIZE ? PAGESIZE
                        : playlistInfo.numVideos;

                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);

            } else if (activityInfo.activityType == ChannelActivityType.Uploads) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int videoCount = channelInfo.videoList.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.videoList.size();
                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == ChannelActivityType.RecentActiviy) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int videoCount = channelInfo.channelActivities.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.channelActivities.size();
                if (count + videoCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += videoCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == ChannelActivityType.AllPlaylists) {
                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                int playlistCount = channelInfo.playlists.size() > PAGESIZE ? PAGESIZE
                        : channelInfo.playlists.size();
                if (count + playlistCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += playlistCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == ChannelActivityType.MultiplePlaylists) {
                Vector<PlaylistInfo> playlists = (Vector<PlaylistInfo>) activityInfo.dataInfo;
                int playlistCount = playlists.size() > PAGESIZE ? PAGESIZE
                        : playlists.size();
                if (count + playlistCount > Constants.PAGE_SIZE) {
                    isFinised = false;
                    break;
                }
                count += playlistCount;
                _activityListToLoadData.add(activityInfo);
            } else if (activityInfo.activityType == ChannelActivityType.MultipleChannels) {
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
                MainContext.getDevKeyInfo().getChannelsDetailsApiUrl,
                channelInfo.id);
        HttpGetFile httpGetFile = Utils.download(url,
                downloadChannelDetailsCompleted);
        httpGetFile.dataContext = activityInfo;
    }

    private IEventHandler downloadChannelDetailsCompleted = new IEventHandler() {

        @Override
        public void returnResult(final Object sender,
                                 final ResultType resultType, final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;
                            String s = new String(data);
                            Vector<ChannelInfo> channelList = YoutubeHelper
                                    .populateChannelsInfo(s);

                            if (channelList.size() > 0
                                    && !Utils.isNullOrEmpty(channelList
                                    .elementAt(0).likePlaylistId)) {
                                PlaylistInfo playlistInfo = new PlaylistInfo();
                                playlistInfo.id = channelList.elementAt(0).likePlaylistId;
                                channelSectionInfo.dataInfo = playlistInfo;
                                channelSectionInfo.activityType = ChannelActivityType.SinglePlaylist;
                            } else {
                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                            }
                            loadActivityInfos(_isNewRound);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    public void loadAllPlaylistsOfChannel(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
        String url = String.format(
                MainContext.getDevKeyInfo().getPlaylistsInChannelApiUrl,
                channelInfo.id, "", PAGESIZE);
        HttpGetFile httpGetFile = Utils.download(url,
                downloadAllPlaylistsCompleted);
        httpGetFile.dataContext = activityInfo;
    }

    public void loadPlaylistsOfMulti(ChannelSectionInfo activityInfo) {
        Vector<PlaylistInfo> playlists = (Vector<PlaylistInfo>) activityInfo.dataInfo;

        int count = 0;
        String ids = "";
        for (PlaylistInfo playlistInfo : playlists) {
            if (ids == "") {
                ids = playlistInfo.id;
            } else {
                ids = ids + "," + playlistInfo.id;
            }
            count++;
            if (count == Constants.PAGE_SIZE_FOR_LOAD_DATA) {
                break;
            }
        }
        String url = String.format(
                MainContext.getDevKeyInfo().getPlaylistsDetailsApiUrl, ids);
        HttpGetFile httpGetFile = Utils.download(url,
                downloadAllPlaylistsCompleted);
        httpGetFile.dataContext = activityInfo;
    }

    private void populatePlaylists(Vector<PlaylistInfo> source,
                                   Vector<PlaylistInfo> target) {
        for (PlaylistInfo p : source) {
            for (PlaylistInfo p1 : target) {
                if (p != null && p1 != null && p.id.equals(p1.id)) {
                    p1.title = p.title;
                    p1.thumbUrl = p.thumbUrl;
                    p1.numVideos = p.numVideos;
                    p1.isPrivate = p.isPrivate;
                    break;
                }
            }
        }
    }

    private IEventHandler downloadAllPlaylistsCompleted = new IEventHandler() {

        @Override
        public void returnResult(final Object sender,
                                 final ResultType resultType, final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);
                            KeyPairValue<String, Vector<PlaylistInfo>> searchResult = YoutubeHelper
                                    .getPlaylists(s, true, false);
                            Vector<PlaylistInfo> playlists = searchResult
                                    .getValue();

                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;
                            if (channelSectionInfo.activityType == ChannelActivityType.AllPlaylists) {
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.playlists = playlists;
                                channelInfo.hasMoreVideos = !Utils
                                        .isNullOrEmpty(searchResult.getKey());

                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
                                loadDataByPage();
                            } else {
                                populatePlaylists(
                                        playlists,
                                        (Vector<PlaylistInfo>) channelSectionInfo.dataInfo);
                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                                loadDataItems();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;
                        if (channelSectionInfo.activityType == ChannelActivityType.AllPlaylists) {
                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;
                        } else {
                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
                        }

                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

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
            if (count == Constants.PAGE_SIZE_FOR_LOAD_DATA) {
                break;
            }
        }
        String url = String.format(
                MainContext.getDevKeyInfo().getChannelsDetailsApiUrl, ids);
        HttpGetFile httpGetFile = Utils
                .download(url, downloadChannelsCompleted);
        httpGetFile.dataContext = activityInfo;
    }

    private void populateChannels(Vector<ChannelInfo> source,
                                  Vector<ChannelInfo> target) {
        for (int i = target.size() - 1; i >= 0; --i) {
            ChannelInfo c1 = target.elementAt(i);
            boolean isPopulated = false;
            for (ChannelInfo c : source) {
                if (c != null && c1 != null && c.id.equals(c1.id)) {
                    c1.title = c.title;
                    c1.thumbUrl = c.thumbUrl;
                    c1.numVideos = c.numVideos;
                    c1.uploadPlaylistId = c.uploadPlaylistId;
                    c1.numSubscribers = c.numSubscribers;
                    isPopulated = true;
                    break;
                }
            }
            if (!isPopulated) {
                target.removeElementAt(i);
            }
        }
    }

    private IEventHandler downloadChannelsCompleted = new IEventHandler() {

        @Override
        public void returnResult(final Object sender,
                                 final ResultType resultType, final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);
                            Vector<ChannelInfo> channels = YoutubeHelper
                                    .getChannelList(s, false);

                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;
                            populateChannels(
                                    channels,
                                    (Vector<ChannelInfo>) channelSectionInfo.dataInfo);

                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;

                            loadDataItems();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;

                        channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.pendingForLoadingItemCount;

                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void loadChannelVideos(ChannelSectionInfo activityInfo) {
        String url;
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
        if (activityInfo.sortBy == SortBy.MostViewed) {
            url = String.format(
                    MainContext.getDevKeyInfo().getVideosInChannelSortByApiUrl,
                    channelInfo.id, "", PAGESIZE, "viewCount");

        } else if (activityInfo.sortBy == SortBy.MostRecent) {
            url = String.format(
                    MainContext.getDevKeyInfo().getVideosInChannelSortByApiUrl,
                    channelInfo.id, "", PAGESIZE, "date");
        } else {
            url = String.format(
                    MainContext.getDevKeyInfo().getVideosInChannelApiUrl,
                    channelInfo.id, "", PAGESIZE);
        }

        HttpGetFile httpGetFile = Utils.download(url,
                downloadCompletedVidesInfo);
        httpGetFile.dataContext = activityInfo;
        Utils.println("loadChannelVideos:" + activityInfo.activityType + ":"
                + activityInfo.sortBy);
    }

    private void loadChannelActivities(ChannelSectionInfo activityInfo) {
        ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;

        String url = String.format(
                MainContext.getDevKeyInfo().getActivitiesInChannelApiUrl,
                channelInfo.id, "", PAGESIZE);

        HttpGetFile httpGetFile = Utils.download(url,
                downloadChannelActivitiesCompleted);
        httpGetFile.dataContext = activityInfo;
    }

    private IEventHandler downloadCompletedPlaylistsDetails = new IEventHandler() {

        @Override
        public void returnResult(Object sender, final ResultType resultType,
                                 final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {

                            String s = new String(data);
                            Vector<PlaylistInfo> playlists = YoutubeHelper
                                    .getPlaylists(s, true);

                            for (int i = 0; i < _activityListLoading.size(); ++i) {
                                if (_activityListLoading.elementAt(i).activityType == ChannelActivityType.SinglePlaylist) {
                                    for (int k = 0; k < playlists.size(); ++k) {

                                        PlaylistInfo activityPlaylistInfo = (PlaylistInfo) _activityListLoading
                                                .elementAt(i).dataInfo;
                                        if (activityPlaylistInfo.id
                                                .equals(playlists.elementAt(k).id)) {
                                            activityPlaylistInfo.title = playlists
                                                    .elementAt(k).title;
                                            activityPlaylistInfo.thumbUrl = playlists
                                                    .elementAt(k).thumbUrl;
                                            activityPlaylistInfo.numVideos = playlists
                                                    .elementAt(k).numVideos;

                                            break;
                                        }
                                    }
                                    _activityListLoading.elementAt(i).loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
                                }

                            }

                            loadDataByPage();

                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }

                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void loadDataItems() {
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
            if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist) {
                if (channelSectionInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.LoadingIds
                        .getValue()) {
                    PlaylistInfo playlistInfo = (PlaylistInfo) channelSectionInfo.dataInfo;
                    if (playlistInfo.numVideos > 0) {
                        channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.LoadingIds;
                        String url = String
                                .format(MainContext.getDevKeyInfo().getVideosInPlaylistApiUrl,
                                        ((PlaylistInfo) channelSectionInfo.dataInfo).id,
                                        "", PAGESIZEPRE);
                        HttpGetFile httpGetFile = Utils.download(url,
                                downloadCompletedVidesInfo);
                        httpGetFile.dataContext = channelSectionInfo;
                    } else {
                        channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                    }
                }
            } else if (channelSectionInfo.activityType == ChannelActivityType.Uploads
                    || channelSectionInfo.activityType == ChannelActivityType.RecentActiviy) {
                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.LoadedIds;
            } else if (channelSectionInfo.activityType == ChannelActivityType.MultiplePlaylists) {
                if (channelSectionInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.LoadedIds
                        .getValue()) {
                    channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.LoadedIds;
                    loadPlaylistsOfMulti(channelSectionInfo);
                }
            } else if (channelSectionInfo.activityType == ChannelActivityType.MultipleChannels) {
                if (channelSectionInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.LoadedIds
                        .getValue()) {
                    channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.LoadedIds;
                    loadChannelsOfMulti(channelSectionInfo);
                }
            } else {
                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
            }
        }
        boolean isCompleted = true;
        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
            if (channelSectionInfo.loadingVideosDataState != LoadingVideosDataState.Completed) {
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
            if (channelSectionInfo.loadingVideosDataState.getValue() < LoadingVideosDataState.LoadedIds
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
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);
                            ChannelSectionInfo channelSectionInfo = (ChannelSectionInfo) ((HttpGetFile) sender).dataContext;

                            Utils.println("returnResult:"
                                    + channelSectionInfo.activityType + ":"
                                    + channelSectionInfo.sortBy);

                            KeyPairValue<String, Vector<VideoInfo>> searchResult;
                            if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist) {
                                searchResult = YoutubeHelper
                                        .getVideosInPlaylist(s, PAGESIZE);
                            } else if (channelSectionInfo.activityType == ChannelActivityType.Uploads) {
                                searchResult = YoutubeHelper.getVideos(s);
                            } else {
                                searchResult = YoutubeHelper
                                        .getVideosInAccount(s);
                            }
                            if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist) {
                                PlaylistInfo playlistInfo = (PlaylistInfo) channelSectionInfo.dataInfo;
                                playlistInfo.videoList = searchResult
                                        .getValue();
                                Utils.println("ChannelActivityType.SinglePlaylist:"
                                        + playlistInfo.videoList.size());
                                playlistInfo.hasMoreVideos = !Utils
                                        .isNullOrEmpty(searchResult.getKey());
                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.LoadedIds;

                                loadDataItems();
                            } else {
                                ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                                channelInfo.videoList = searchResult.getValue();
                                Utils.println("ChannelInfo:"
                                        + channelInfo.videoList.size());
                                channelInfo.hasMoreVideos = !Utils
                                        .isNullOrEmpty(searchResult.getKey());

                                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.loadedItemCount;
                                loadDataByPage();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(Utils
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_playlistItemViewInfos.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
                            _adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void loadVideosInfo() {
        if (_hasVideoActivityOnly) {
            String videoIds = "";
            for (int i = 0; i < _playlistItemViewInfosLoading.size(); ++i) {
                VideoInfo videoInfo = (VideoInfo) _playlistItemViewInfosLoading
                        .elementAt(i).dataInfo;
                if (Utils.isNullOrEmpty(videoIds)) {
                    videoIds = videoInfo.id;
                } else {
                    videoIds = videoIds + "," + videoInfo.id;
                }
            }
            if (!Utils.isNullOrEmpty(videoIds)) {
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
                if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist) {
                    if (channelSectionInfo.loadingVideosDataState == LoadingVideosDataState.LoadedIds) {
                        PlaylistInfo playlistInfo = (PlaylistInfo) channelSectionInfo.dataInfo;
                        for (VideoInfo videoInfo : playlistInfo.videoList) {
                            if (videoIds == "") {
                                videoIds = videoInfo.id;
                            } else {
                                videoIds = videoIds + "," + videoInfo.id;
                            }
                        }
                        if (playlistInfo.videoList.size() == 0) {
                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                        }
                    }
                } else if (channelSectionInfo.activityType == ChannelActivityType.Uploads) {
                    if (channelSectionInfo.loadingVideosDataState == LoadingVideosDataState.LoadedIds) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (VideoInfo videoInfo : channelInfo.videoList) {
                            if (videoIds == "") {
                                videoIds = videoInfo.id;
                            } else {
                                videoIds = videoIds + "," + videoInfo.id;
                            }
                        }
                        if (channelInfo.videoList.size() == 0) {
                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                        }
                    }
                } else if (channelSectionInfo.activityType == ChannelActivityType.RecentActiviy) {
                    if (channelSectionInfo.loadingVideosDataState == LoadingVideosDataState.LoadedIds) {
                        ChannelInfo channelInfo = (ChannelInfo) channelSectionInfo.dataInfo;
                        for (PlaylistItemInfo itemInfo : channelInfo.channelActivities) {
                            if (videoIds == "") {
                                videoIds = ((VideoInfo) itemInfo.dataInfo).id;
                            } else {
                                videoIds = videoIds + ","
                                        + ((VideoInfo) itemInfo.dataInfo).id;
                            }
                        }
                        if (channelInfo.channelActivities.size() == 0) {
                            channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                        }
                    }
                } else if (channelSectionInfo.activityType == ChannelActivityType.AllPlaylists) {
                    channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
                }
            }

            if (Utils.isNullOrEmpty(videoIds)) {
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

                            Vector<VideoInfo> videoList = YoutubeHelper
                                    .getVideoList(s);
                            if (_hasVideoActivityOnly) {
                                populatePlaylistItemViewsData(videoList);
                            } else {
                                populatePlaylistsData(videoList);
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
                            _isNetworkError = true;
                            _adapter.mIsNetworkError = true;
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
                if (channelSectionInfo.loadingVideosDataState != LoadingVideosDataState.Completed) {
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

    private void populatePlaylistsData(Vector<VideoInfo> videoList) {

        for (int i = 0; i < _activityListToLoadData.size(); ++i) {
            ChannelSectionInfo channelSectionInfo = _activityListToLoadData
                    .elementAt(i);
            if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist
                    || channelSectionInfo.activityType == ChannelActivityType.Uploads
                    || channelSectionInfo.activityType == ChannelActivityType.RecentActiviy) {

                Vector<VideoInfo> activityVideoList;
                if (channelSectionInfo.activityType == ChannelActivityType.RecentActiviy) {
                    activityVideoList = new Vector<VideoInfo>();
                    Vector<PlaylistItemInfo> items = ((ChannelInfo) channelSectionInfo.dataInfo).channelActivities;
                    for (PlaylistItemInfo item : items) {
                        activityVideoList.add((VideoInfo) item.dataInfo);
                    }
                } else {
                    activityVideoList = channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist ? ((PlaylistInfo) channelSectionInfo.dataInfo).videoList
                            : ((ChannelInfo) channelSectionInfo.dataInfo).videoList;
                }
                for (int j = activityVideoList.size() - 1; j >= 0; --j) {
                    VideoInfo videoInfo = activityVideoList.elementAt(j);
                    boolean isExisted = false;
                    for (int k = 0; k < videoList.size(); ++k) {
                        if (videoList.elementAt(k).id.equals(videoInfo.id)) {
                            VideoInfo newVideoInfo = videoList.elementAt(k);

                            populateVideoInfo(videoInfo, newVideoInfo);
                            isExisted = true;
                            break;
                        }
                    }
                    if (!isExisted) {
                        activityVideoList.remove(j);
                    }
                }

                channelSectionInfo.loadingVideosDataState = LoadingVideosDataState.Completed;
            }
        }
    }

    private void populatePlaylistItemViewsData(Vector<VideoInfo> videoList) {
        for (int i = _playlistItemViewInfosLoading.size() - 1; i >= 0; --i) {
            VideoInfo videoInfo = (VideoInfo) _playlistItemViewInfosLoading
                    .elementAt(i).dataInfo;
            boolean isExisted = false;
            for (int k = 0; k < videoList.size(); ++k) {
                if (videoList.elementAt(k).id.equals(videoInfo.id)) {
                    VideoInfo newVideoInfo = videoList.elementAt(k);

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
                    if (channelSectionInfo.activityType == ChannelActivityType.SinglePlaylist) {
                        YoutubePlaylistVideosView youtubePlaylistDetails = YoutubePlaylistVideosView
                                .newInstance(
                                        (PlaylistInfo) channelSectionInfo.dataInfo,
                                        VideoListType.Normal);
                        MainActivity.getInstance().launchFragment(
                                youtubePlaylistDetails);
                    } else if (channelSectionInfo.activityType == ChannelActivityType.Uploads) {
                        UserVideosView userVideosView = UserVideosView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(
                                userVideosView);
                    } else if (channelSectionInfo.activityType == ChannelActivityType.RecentActiviy) {
                        MyVideosView channelVideosView = MyVideosView
                                .newInstance((ChannelInfo) channelSectionInfo.dataInfo);
                        MainActivity.getInstance().launchFragment(
                                channelVideosView);
                    } else if (channelSectionInfo.activityType == ChannelActivityType.AllPlaylists
                            || channelSectionInfo.activityType == ChannelActivityType.MultiplePlaylists) {
                        UserPlaylistsView userPlaylistsView = UserPlaylistsView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(
                                userPlaylistsView);
                    } else if (channelSectionInfo.activityType == ChannelActivityType.MultipleChannels) {
                        UserChanelsView chanelsView = UserChanelsView
                                .newInstance(_channelInfo.id,
                                        playlistItemViewInfo.activityInfo);
                        MainActivity.getInstance().launchFragment(chanelsView);
                    }
                } else if (playlistItemViewInfo.playlistItemType == YoutubePlaylistItemType.Playlist) {
                    PlaylistInfo playlistInfo = (PlaylistInfo) playlistItemViewInfo.dataInfo;
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
                    Vector<VideoInfo> videoList = new Vector<VideoInfo>();
                    if (_hasVideoActivityOnly) {
                        for (PlaylistItemInfo itemViewInfo : _playlistItemViewInfos) {
                            if (itemViewInfo != null
                                    && itemViewInfo.dataInfo instanceof VideoInfo) {
                                VideoInfo videoInfo = (VideoInfo) itemViewInfo.dataInfo;
                                videoList.add(videoInfo);
                            }
                        }
                    } else {
                        Vector<PlaylistItemInfo> items = ((ChannelInfo) playlistItemViewInfo.activityInfo.dataInfo).channelActivities;
                        for (PlaylistItemInfo item : items) {
                            videoList.add((VideoInfo) item.dataInfo);
                        }
                    }

                    MainActivity.getInstance().play(
                            (VideoInfo) playlistItemViewInfo.dataInfo,
                            videoList, true);
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
            if (activityInfo.activityType == ChannelActivityType.SinglePlaylist) {
                PlaylistInfo playlistInfo = (PlaylistInfo) activityInfo.dataInfo;
                if (playlistInfo.videoList.size() > 0) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    playlistItemViewInfo.dataInfo = playlistInfo.title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    for (int k = 0; k < playlistInfo.videoList.size(); ++k) {
                        playlistItemViewInfo = new PlaylistItemInfo();
                        playlistItemViewInfo.dataInfo = playlistInfo.videoList
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
            } else if (activityInfo.activityType == ChannelActivityType.Uploads
                    || activityInfo.activityType == ChannelActivityType.RecentActiviy) {

                ChannelInfo channelInfo = (ChannelInfo) activityInfo.dataInfo;
                boolean hasData = activityInfo.activityType == ChannelActivityType.Uploads ? channelInfo.videoList
                        .size() > 0 : channelInfo.channelActivities.size() > 0;
                if (hasData) {
                    PlaylistItemInfo playlistItemViewInfo = new PlaylistItemInfo();
                    String title = activityInfo.activityType == ChannelActivityType.Uploads ? (activityInfo.sortBy == SortBy.MostRecent ? Utils
                            .getString(R.string.recent_uploads) : Utils
                            .getString(R.string.popular_uploads))
                            : Utils.getString(R.string.recent_activities);
                    playlistItemViewInfo.dataInfo = title;
                    playlistItemViewInfo.playlistItemType = YoutubePlaylistItemType.Title;
                    results.add(playlistItemViewInfo);
                    if (activityInfo.activityType == ChannelActivityType.RecentActiviy) {
                        for (int k = 0; k < channelInfo.channelActivities
                                .size(); ++k) {
                            channelInfo.channelActivities.elementAt(k).activityInfo = activityInfo;
                        }
                        results.addAll(channelInfo.channelActivities);
                    } else {
                        for (int k = 0; k < channelInfo.videoList.size(); ++k) {
                            playlistItemViewInfo = new PlaylistItemInfo();
                            playlistItemViewInfo.dataInfo = channelInfo.videoList
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
            } else if (activityInfo.activityType == ChannelActivityType.AllPlaylists) {
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
            } else if (activityInfo.activityType == ChannelActivityType.MultiplePlaylists) {
                Vector<PlaylistInfo> playlists = (Vector<PlaylistInfo>) activityInfo.dataInfo;

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
            } else if (activityInfo.activityType == ChannelActivityType.MultipleChannels) {
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
