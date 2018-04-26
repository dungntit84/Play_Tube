package com.hteck.playtube.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.ChannelByPageAdapter;
import com.hteck.playtube.common.CustomHttpOk;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.ListViewBinding;

import java.util.ArrayList;

public class ChannelListViewTest extends FrameLayout implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<ChannelInfo> _channelList = new ArrayList<>();
    private ArrayList<ChannelInfo> _channelListSearching = new ArrayList<>();
    ChannelByPageAdapter _adapterChannel;
    private boolean _isNetworkError;
    String _query;
    private LoadingView _busyView;
    public boolean mIsSearched;
    private boolean _isLoading = false;
    private View _viewReload;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;
    public ChannelListViewTest(Context context) {
        super(context);

        createView();
        addView(_binding.getRoot());
    }

    public void createView() {
        _binding = DataBindingUtil.inflate(MainActivity.getInstance()
                .getLayoutInflater(), R.layout.list_view, null, false);
        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));

        _adapterChannel = new ChannelByPageAdapter(getContext(), _channelList);
        _binding.listView.setAdapter(_adapterChannel);
        _binding.listView.setOnItemClickListener(this);
        _binding.listView.setOnScrollListener(this);
    }

    public void setDataSource(ArrayList<ChannelInfo> channelList, boolean isInit) {

        if (isInit || channelList.size() > 0) {
            _binding.textViewMsg.setVisibility(GONE);
        } else {
            _binding.textViewMsg.setVisibility(VISIBLE);
            _binding.listView.setEmptyView(_binding.textViewMsg);
            _binding.textViewMsg.setText(Utils.getString(
                    R.string.no_channel_found));
        }
        _channelList = channelList;
        _adapterChannel.setDataSource(_channelList);
        _adapterChannel.notifyDataSetChanged();
    }

    public void search(String query) {
        if (Utils.stringIsNullOrEmpty(query)) {
            return;
        }
        if (_isLoading) {
            return;
        }
        _channelList = new ArrayList<>();
        setDataSource(_channelList, true);
        _isLoading = true;
        cancelAllRequests();
        _nextPageToken = "";
        _query = query;

        search();
    }

    private void cancelAllRequests() {
        if (_httpOk != null) {
            _httpOk.cancel();
        }
    }

    private void search() {
        String url = String
                .format(PlayTubeController.getConfigInfo()..getDevKeyInfo().searchChannelApiUrl,
                        Utils.encodeUrl(_query), _nextPageToken);
        IEventHandler eventHandler = new IEventHandler() {

            @Override
            public void returnResult(Object sender,
                                     final ResultType resultType, final byte[] data) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Success) {
                            try {
                                String s = new String(data);
                                KeyPairValue<String, Vector<ChannelInfo>> searchResult = YoutubeHelper
                                        .getChannels(s);
                                _nextPageToken = searchResult.getKey();
                                _channelListSearching = searchResult.getValue();

                                if (_channelListSearching.size() == 0) {
                                    if (_channelList.size() > 0
                                            && _channelList
                                            .elementAt(_channelList
                                                    .size() - 1) == null) {
                                        _channelList.remove(_channelList.size() - 1);
                                    }

                                    setDataSource(_channelList, false);
                                    hideBusyAnimation();
                                    _isLoading = false;
                                } else {
                                    loadChannelsInfo();
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                                hideBusyAnimation();
                                _isLoading = false;
                            }
                        }

                        if (resultType == ResultType.NetworkError) {
                            Utils.showMessageToast(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_channelList.size() == 0) {
                                initReloadEvent();
                            } else {
                                _isNetworkError = true;
                                _adapterChannel.mIsNetworkError = true;
                                _adapterChannel.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        };
        _httpGetFile = new HttpGetFile(url, eventHandler);
        _httpGetFile.start();
        if (_channelList.size() == 0) {
            showBusyAnimation();
        }
    }

    private void searchMore() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        search();
    }

    private void loadMoreChannelsInfo() {
        if (_isLoading) {
            return;
        }
        _isLoading = true;
        _channelListSearching = new Vector<ChannelInfo>();
        int count = 0;
        for (ChannelInfo c : _channelList) {
            if (Utils.isNullOrEmpty(c.title)) {
                count++;
                _channelListSearching.add(c);
                if (count == Constants.PAGE_SIZE) {
                    break;
                }
            }
        }
        loadChannelsInfo();
    }

    private void loadChannelsInfo() {
        String ids = "";
        for (ChannelInfo channelInfo : _channelListSearching) {
            if (ids == "") {
                ids = channelInfo.id;
            } else {
                ids = ids + "," + channelInfo.id;
            }
        }
        String url = String
                .format(MainContext.getDevKeyInfo().getChannelsDetailsApiUrl,
                        ids);

        IEventHandler eventHandler = new IEventHandler() {

            @Override
            public void returnResult(Object sender,
                                     final ResultType resultType, final byte[] data) {
                MainActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultType == ResultType.Success) {
                            try {

                                String s = new String(data);
                                if (Utils.isLoadMoreChannels(_channelList)) {
                                    YoutubeHelper.populateChannelsInfo(
                                            _channelList, s);
                                } else {
                                    YoutubeHelper.populateChannelsInfo(
                                            _channelListSearching, s);

                                    if (_channelList.size() > 0
                                            && _channelList
                                            .elementAt(_channelList
                                                    .size() - 1) == null) {
                                        _channelList.remove(_channelList.size() - 1);
                                    }
                                    _channelList.addAll(_channelListSearching);
                                    if (!Utils.isNullOrEmpty(_nextPageToken)) {
                                        _channelList.add(null);
                                    }
                                }
                                setDataSource(_channelList, false);

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            hideBusyAnimation();
                            _isLoading = false;
                        }

                        if (resultType == ResultType.NetworkError) {
                            Utils.showMessageToast(MainActivity.getInstance()
                                    .getString(R.string.network_error));
                            hideBusyAnimation();
                            _isLoading = false;
                            if (_channelList.size() == 0) {
                                initReloadEvent();
                            } else {
                                _isNetworkError = true;
                                _adapterChannel.mIsNetworkError = true;
                                _adapterChannel.notifyDataSetChanged();
                            }
                        }
                    }
                });
            }
        };
        _httpGetFile = new HttpGetFile(url, eventHandler);
        _httpGetFile.start();
    }

    private void hideBusyAnimation() {
        Utils.hideBusyAnimation(_contentView, _busyView);
    }

    private void showBusyAnimation() {
        _busyView = Utils.showBusyAnimation(_contentView, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        int index = (int) arg3;
        if (index == _channelList.size() - 1
                && _channelList.lastElement() == null) {
            if (_isNetworkError) {
                _isNetworkError = false;
                _adapterChannel.mIsNetworkError = false;
                _adapterChannel.notifyDataSetChanged();
                searchMore();
            }
        } else {
            ChannelInfo channelInfo = _channelList.elementAt(index);
            if (channelInfo == null) {
                return;
            }

            UserDetails userDetails = UserDetails
                    .newInstance(channelInfo);
            MainActivity.getInstance().launchFragment(userDetails);
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
                if (Utils.isLoadMoreChannels(_channelList)
                        || (_channelList.size() > 0 && _channelList
                        .lastElement() == null)) {
                    if (!_isNetworkError) {
                        if (Utils.isLoadMoreChannels(_channelList)) {
                            loadMoreChannelsInfo();
                        } else {
                            searchMore();
                        }
                    }
                }
            }
        }
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

                search();
                return false;
            }
        });
    }
}
