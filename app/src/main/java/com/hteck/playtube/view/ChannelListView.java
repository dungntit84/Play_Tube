package com.hteck.playtube.view;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
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
import com.hteck.playtube.fragment.UserDetailsFragment;
import com.hteck.playtube.service.CustomCallback;
import com.hteck.playtube.service.YoutubeHelper;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Objects;

public class ChannelListView extends FrameLayout implements
        AdapterView.OnItemClickListener, OnScrollListener {

    private String _nextPageToken = "";
    private ArrayList<ChannelInfo> _channelList = new ArrayList<>();
    ChannelByPageAdapter _adapterChannel;
    String _query;
    private LoadingView _busyView;
    public boolean mIsSearched;
    private boolean _isLoading = false;
    private View _viewReload;
    private ListViewBinding _binding;
    private CustomHttpOk _httpOk;

    public ChannelListView(Context context) {
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

    public void setDataSource(boolean isInit, ArrayList<ChannelInfo> channelList) {

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
        _isLoading = true;
        _channelList = new ArrayList<>();
        setDataSource(true, _channelList);

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
                .format(PlayTubeController.getConfigInfo().searchChannelUrl, _nextPageToken,
                        Utils.urlEncode(_query));
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
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
                            if (_channelList.size() == 0) {
                                handleNetworkError();
                            } else {
                                _adapterChannel.setIsNetworkError(true);
                                _adapterChannel.notifyDataSetChanged();
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
                            AbstractMap.SimpleEntry<ArrayList<ChannelInfo>, String> searchResult = YoutubeHelper
                                    .getChannelListInfo(s);
                            _nextPageToken = searchResult.getValue();
                            ArrayList<ChannelInfo> channels = searchResult.getKey();

                            if (channels.size() == 0) {
                                if (_channelList.size() > 0
                                        && _channelList
                                        .get(_channelList
                                                .size() - 1) == null) {
                                    _channelList.remove(_channelList.size() - 1);
                                }

                                setDataSource(false, _channelList);
                                hideBusyAnimation();
                                _isLoading = false;
                            } else {
                                loadChannelsInfo(channels);
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

        _httpOk.start();
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

//    private void loadMoreChannelsInfo() {
//        if (_isLoading) {
//            return;
//        }
//        _isLoading = true;
//        _channelListSearching = new ArrayList<>();
//        int count = 0;
//        for (ChannelInfo c : _channelList) {
//            if (Utils.stringIsNullOrEmpty(c.title)) {
//                count++;
//                _channelListSearching.add(c);
//                if (count == Constants.PAGE_SIZE) {
//                    break;
//                }
//            }
//        }
//        loadChannelsInfo();
//    }

    private void loadChannelsInfo(final ArrayList<ChannelInfo> channelList) {
        String url = String
                .format(PlayTubeController.getConfigInfo().loadChannelsInfoUrl,
                        Utils.getIds(channelList, 0));
        _httpOk = new CustomHttpOk(url, new CustomCallback() {
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
                            if (_channelList.size() == 0) {
                                handleNetworkError();
                            } else {
                                _adapterChannel.setIsNetworkError(true);
                                _adapterChannel.notifyDataSetChanged();
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
//                            if (Utils.haveMoreChannels(_channelList)) {
//                                _channelList = YoutubeHelper.getChannels(s);
//                            } else {
                            ArrayList<ChannelInfo> channels = YoutubeHelper.getChannelList(s, channelList);

                            if (_channelList.size() > 0
                                    && _channelList
                                    .get(_channelList
                                            .size() - 1) == null) {
                                _channelList.remove(_channelList.size() - 1);
                            }
                            _channelList.addAll(channels);
                            if (!Utils.stringIsNullOrEmpty(_nextPageToken)) {
                                _channelList.add(null);
                            }
//                            }
                            setDataSource(false, _channelList);

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        hideBusyAnimation();
                        _isLoading = false;
                    }
                });
            }
        });

        _httpOk.start();
    }

    private void hideBusyAnimation() {
        Utils.hideProgressBar(_binding.layoutMain, _busyView);
    }

    private void showBusyAnimation() {
        _busyView = Utils.showProgressBar(_binding.layoutMain, _busyView);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        if (index == _channelList.size() - 1
                && _channelList.get(_channelList.size() - 1) == null) {
            if (_adapterChannel.getIsNetworkError()) {
                _adapterChannel.setIsNetworkError(false);
                _adapterChannel.notifyDataSetChanged();
                searchMore();
            }
        } else {
            ChannelInfo channelInfo = _channelList.get(index);
            if (channelInfo == null) {
                return;
            }

            UserDetailsFragment userDetailsFragment = UserDetailsFragment.newInstance(channelInfo);
            MainActivity.getInstance().addFragment(userDetailsFragment);
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
            if (view.getLastVisiblePosition() == view.getAdapter()
                    .getCount() - 1) {
                if (Utils.haveMoreChannels(_channelList)
                        || (_channelList.size() > 0 && _channelList
                        .get(_channelList.size() - 1) == null)) {
                    if (!_adapterChannel.getIsNetworkError()) {
//                        if (Utils.haveMoreChannels(_channelList)) {
//                            loadMoreChannelsInfo();
//                        } else {
                        searchMore();
//                        }
                    }
                }
            }
        }
    }

    private void handleNetworkError() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (_viewReload == null) {
            _viewReload = inflater.inflate(R.layout.retry_view, null);
            _binding.layoutMain.addView(_viewReload);
        }
        _viewReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (_viewReload != null) {
                    _binding.layoutMain.removeView(_viewReload);
                    _viewReload = null;
                }
                search();
            }
        });
    }

}
