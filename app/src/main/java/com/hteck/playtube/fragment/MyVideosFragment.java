package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.view.LoadingView;

import java.util.ArrayList;

public class MyVideosFragment extends BaseFragment implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<YoutubeInfo> _songList = new ArrayList<YoutubeInfo>();
    VideoByPageAdapter _adapterSong;
    ListView _listView;
    private LoadingView _busyView;
    View _viewNoItem;
    private ViewGroup _contentView;
    private boolean _isLoading = false;
    private int _accountViewType;
    private ChannelInfo _channelInfo;
    String _playlistId;
    private View _viewReload;
    private boolean _isLoadMore;
    private boolean _isInMyAccount;
    private boolean _isNetworkError;

    public static MyVideosFragment newInstance(int accountViewType) {
        MyVideosFragment v = new MyVideosFragment();

        v._accountViewType = accountViewType;
        Bundle args = new Bundle();
        args.putString(Constants.PAGE_ID, Integer.toString(accountViewType));
        v.setArguments(args);

        return v;
    }

    public static MyVideosFragment newInstance(ChannelInfo channelInfo) {
        MyVideosFragment v = new MyVideosFragment();

        v._channelInfo = channelInfo;
        Bundle args = new Bundle();
        args.putString(Constants.PAGE_ID, channelInfo.id);
        v.setArguments(args);

        return v;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        _isInMyAccount = _channelInfo == null;
        if (_contentView == null) {
            createMainView();
        }

        loadData();

        return _contentView;
    }

    private void loadData() {
        if (_isInMyAccount) {
            if (_accountViewType == Constants.AccountViewType.WhatToWatch) {
                loadData(false);
            } else {
                loadChannelInfo();
            }
        } else {
            loadData(false);
        }
    }

    private void createMainView() {
        _contentView = (ViewGroup) MainActivity.getInstance()
                .getLayoutInflater().inflate(R.layout.layout_container, null);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) MainActivity
                .getInstance().getLayoutInflater()
                .inflate(R.layout.swipe_refresh_view, null);

        _listView = (ListView) swipeRefreshLayout
                .findViewById(R.id.list_video_view);
        _adapterSong = new VideoByPageAdapter(_songList);
        _listView.setAdapter(_adapterSong);
        _listView.setOnItemClickListener(this);
        _listView.setOnScrollListener(this);

        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        Utils.setSwipeStyle(swipeRefreshLayout);
        _contentView.addView(swipeRefreshLayout);
    }

    // public View createView() {
    // _listView = new ListView(MainActivity.getInstance());
    // _adapterSong = new AdapterSongLoadMore(_songList);
    // _listView.setAdapter(_adapterSong);
    // _listView.setOnItemClickListener(this);
    // _listView.setOnScrollListener(this);
    // _listView.setId(Utils.generateViewId());
    //
    // return _listView;
    // }

    public void setDataSource(ArrayList<YoutubeInfo> songList) {

        if (songList.size() == 0) {
            if (_viewNoItem == null) {
                _viewNoItem = Utils.createNoItem(getActivity(), MainActivity
                        .getInstance().getString(R.string.no_video));
            }
            _contentView.addView(_viewNoItem);
        } else {
            if (_viewNoItem != null) {
                _contentView.removeView(_viewNoItem);
            }
        }
        _songList = songList;
        _adapterSong.setDataSource(_songList);
        _adapterSong.notifyDataSetChanged();
    }

    private void resetDataSource() {
        if (_contentView == null) {
            createMainView();
        }
        _listView.setSelectionFromTop(0, 0);
        if (_viewNoItem != null) {
            _contentView.removeView(_viewNoItem);
        }
        _songList = new ArrayList<YoutubeInfo>();
        _adapterSong.setDataSource(_songList);
        _adapterSong.notifyDataSetChanged();
        _nextPageToken = "";
    }

    private void loadChannelInfo() {
        if (_isLoading) {
            return;
        }

        if (AccountContext.getInstance().getAccountInfo() != null) {
            _playlistId = getPlaylistId(AccountContext.getInstance().getAccountInfo());
            if (!Utils.isNullOrEmpty(_playlistId)) {
                loadData(false);
                return;
            }
        }
//		_isLoading = true;
//		showBusyAnimation();
//		_isLoadMore = false;
//		YoutubeService youtubeService = new YoutubeService(
//				new IYoutubeServiceListener() {
//
//					@Override
//					public void onServiceDoneSuccess(Object userToken,
//							int param1, final Object data) {
//						MainActivity.getInstance().runOnUiThread(
//								new Runnable() {
//									@Override
//									public void run() {
//										try {
//											String s = data.toString();
//											ArrayList<ChannelInfo> channelList = YoutubeHelper
//													.getChannelList(s, true);
//											if (channelList.size() > 0) {
//												AccountAppContext.getInstance().mMyChannelInfo = channelList
//														.elementAt(0);
//
//												_playlistId = getPlaylistId(channelList
//														.elementAt(0));
//											}
//											if (!Utils
//													.isNullOrEmpty(_playlistId)) {
//												_isLoading = false;
//												loadData(false);
//											} else {
//												hideBusyAnimation();
//												_isLoading = false;
//											}
//
//										} catch (Throwable e) {
//											e.printStackTrace();
//											hideBusyAnimation();
//											_isLoading = false;
//										}
//									}
//								});
//					}
//
//					@Override
//					public void onServiceDoneFailed(Object userToken, int code,
//							final String error) {
//						_isLoading = false;
//						MainActivity.getInstance().runOnUiThread(
//								new Runnable() {
//									@Override
//									public void run() {
//										hideBusyAnimation();
//										Utils.showMessageToast(error);
//										initReloadEvent();
//									}
//								});
//					}
//				});
//
//		youtubeService.loadMyChannelInfo();

    }

    private String getPlaylistId(ChannelInfo channelInfo) {
        if (_accountViewType == AccountViewType.Uploads) {
            return channelInfo.uploadPlaylistId;
        } else if (_accountViewType == AccountViewType.WatchHistory) {
            return channelInfo.watchHistoryPlaylistId;
        } else if (_accountViewType == AccountViewType.Favourites) {
            return channelInfo.favouritesPlaylistId;
        } else if (_accountViewType == AccountViewType.Likes) {
            return channelInfo.likePlaylistId;
        } else {
            return channelInfo.watchLaterPlaylistId;
        }
    }

    private void loadData(boolean isLoadMore) {
        if (_isLoading) {
            return;
        }

        if (isLoadMore && Utils.isNullOrEmpty(_nextPageToken)) {
            return;
        }
        _isLoading = true;
        if (!isLoadMore) {
            _nextPageToken = "";
            showBusyAnimation();
        }
        _isLoadMore = isLoadMore;
        if (_isInMyAccount) {
            YoutubeService youtubeService = new YoutubeService(
                    new IYoutubeService() {

                        @Override
                        public void onServiceDoneSuccess(Object userToken,
                                                         int param1, final Object data) {
                            MainActivity.getInstance().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                processVideoData(data
                                                        .toString());
                                            } catch (Throwable e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }

                        @Override
                        public void onServiceDoneFailed(Object userToken,
                                                        int code, final String error) {
                            _isLoading = false;
                            MainActivity.getInstance().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            hideBusyAnimation();
                                            Utils.showMessageToast(error);
                                            if (_songList.size() == 0) {
                                                initReloadEvent();
                                            } else {
                                                _isNetworkError = true;
                                                _adapterSong.mIsNetworkError = true;
                                                _adapterSong
                                                        .notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    });
            if (_accountViewType == AccountViewType.WhatToWatch) {
                youtubeService.loadWhatToWatch(_nextPageToken);
            } else {
                youtubeService
                        .loadMyPlaylistVideos(_playlistId, _nextPageToken);
            }
        } else {
            String url = String
                    .format(MainContext.getDevKeyInfo().getActivitiesInChannelApiUrl,
                            _channelInfo.id, _nextPageToken,
                            Constants.PAGE_SIZE);

            Utils.download(url, downloadCompletedVidesInfo);
        }
    }

    private IEventHandler downloadCompletedVidesInfo = new IEventHandler() {

        @Override
        public void returnResult(Object sender, final ResultType resultType,
                                 final byte[] data) {
            MainActivity.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultType == ResultType.Success) {
                        try {
                            String s = new String(data);
                            processVideoData(s);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            hideBusyAnimation();
                            _isLoading = false;
                        }
                    }

                    if (resultType != ResultType.Success) {
                        hideBusyAnimation();
                        Utils.showMessageToast(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        if (_songList.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapterSong.mIsNetworkError = true;
                            _adapterSong.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void processVideoData(String s) {
        try {
            ArrayList<YoutubeInfo> songList = new ArrayList<YoutubeInfo>();

            KeyPairValue<String, ArrayList<YoutubeInfo>> searchResult;
            if (_isInMyAccount) {
                if (_accountViewType == AccountViewType.WhatToWatch) {
                    searchResult = YoutubeHelper.getVideosInAccount(s);
                } else {
                    searchResult = YoutubeHelper.getVideosInPlaylist(s, 0);
                }
            } else {
                searchResult = YoutubeHelper.getVideosInAccount(s);
            }
            songList = searchResult.getValue();
            _nextPageToken = searchResult.getKey();

            if (songList.size() == 0) {
                if (_songList.size() > 0
                        && _songList.elementAt(_songList.size() - 1) == null) {
                    _songList.remove(_songList.size() - 1);
                }

                setDataSource(_songList);
                hideBusyAnimation();
                _isLoading = false;
            } else {
                String videoIds = Utils.getVideoIds(songList);
                loadVideosInfo(videoIds);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            hideBusyAnimation();
            _isLoading = false;
        }
    }

    private void loadVideosInfo(String videoIds) {
        String url = String
                .format(MainContext.getDevKeyInfo().getVideosDetailApiUrl,
                        videoIds);
        Utils.download(url, downloadVideoDetailsCompleted);
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
                            ArrayList<YoutubeInfo> songListSearching = YoutubeHelper
                                    .getVideoList(s);

                            if (_songList.size() > 0
                                    && _songList.elementAt(_songList.size() - 1) == null) {
                                _songList.remove(_songList.size() - 1);
                            }
                            if (!_isLoadMore) {
                                _songList = new ArrayList<YoutubeInfo>();
                            }
                            _songList.addAll(YoutubeHelper
                                    .getAvailableVideos(songListSearching));
                            if (!Utils.isNullOrEmpty(_nextPageToken)) {
                                _songList.add(null);
                            }
                            setDataSource(_songList);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideBusyAnimation();
                        _isLoading = false;
                    }

                    if (resultType != ResultType.Success) {

                        Utils.showMessageToast(MainActivity.getInstance()
                                .getString(R.string.network_error));
                        hideBusyAnimation();
                        _isLoading = false;
                        if (_songList.size() == 0) {
                            initReloadEvent();
                        } else {
                            _isNetworkError = true;
                            _adapterSong.mIsNetworkError = true;
                            _adapterSong.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    };

    private void hideBusyAnimation() {
        MainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.hideBusyAnimation(_contentView, _busyView);
            }
        });

    }

    private void showBusyAnimation() {
        MainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _busyView = Utils.showBusyAnimation(_contentView, _busyView);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        int index = (int) arg3;
        if (index == _songList.size() - 1 && _songList.lastElement() == null) {
            if (_isNetworkError) {
                _isNetworkError = false;
                _adapterSong.mIsNetworkError = false;
                _adapterSong.notifyDataSetChanged();
                loadData(true);
            }
        } else {
            ArrayList<YoutubeInfo> playingSongList = new ArrayList<YoutubeInfo>();
            playingSongList.addAll(_songList);
            if (playingSongList.size() > 0
                    && playingSongList.elementAt(playingSongList.size() - 1) == null) {
                playingSongList.remove(playingSongList.size() - 1);
            }

            MainActivity.getInstance().play(_songList.elementAt(index),
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
            if (_listView.getLastVisiblePosition() == _listView.getAdapter()
                    .getCount() - 1) {
                if (_songList.size() > 0
                        && _songList.elementAt(_songList.size() - 1) == null) {
                    if (!_isNetworkError) {
                        loadData(true);
                    }
                }
            }
        }
    }

    private void initReloadEvent() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (_viewReload == null) {
            _viewReload = (ViewGroup) inflater
                    .inflate(R.layout.reload_view, null);
            _contentView.addView(_viewReload);
        }
        _viewReload.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_viewReload != null) {
                    _contentView.removeView(_viewReload);
                    _viewReload = null;
                }
                loadData();
                return false;
            }
        });
    }

    @Override
    public String getTitle() {
        return null;
    }

//	@Override
//	public IPager getTab() {
//		// TODO Auto-generated method stub
//		return new IPager() {
//
//			@Override
//			public String getTitle() {
//				String title = "";
//				if (_isInMyAccount) {
//					if (_accountViewType == AccountViewType.WhatToWatch) {
//						title = Utils.getString(R.string.what_to_watch);
//					} else if (_accountViewType == AccountViewType.Uploads) {
//						title = Utils.getString(R.string.uploads);
//					} else if (_accountViewType == AccountViewType.Favourites) {
//						title = Utils.getString(R.string.favourites);
//					} else if (_accountViewType == AccountViewType.WatchHistory) {
//						title = Utils.getString(R.string.watch_history);
//					} else if (_accountViewType == AccountViewType.WatchLater) {
//						title = Utils.getString(R.string.watch_later);
//					} else if (_accountViewType == AccountViewType.Likes) {
//						title = Utils.getString(R.string.likes);
//					}
//				} else {
//					title = Utils.getString(R.string.recent_activities);
//				}
//
//				return title;
//			}
//
//			@Override
//			public OnClickListener getOnClickListener() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public LeftHomeTitleType getLeftTitleType() {
//				// TODO Auto-generated method stub
//				return LeftHomeTitleType.Back;
//			}
//
//			@Override
//			public String getLeftTitle() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		};
//	}
}
