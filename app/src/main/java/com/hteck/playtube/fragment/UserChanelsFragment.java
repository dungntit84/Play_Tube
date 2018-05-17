package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.ChannelSectionInfo;
import com.hteck.playtube.view.ChannelListView;

import java.util.ArrayList;

public class UserChanelsFragment extends BaseFragment {
    public ChannelListView _channelListView;
    private ChannelSectionInfo _activityInfo;

    public static UserChanelsFragment newInstance(String channelId,
                                                  ChannelSectionInfo activityInfo) {
        UserChanelsFragment userChanelsFragment = new UserChanelsFragment();

        userChanelsFragment._activityInfo = activityInfo;
        Bundle args = new Bundle();
        args.putString(Constants.PAGE_ID, channelId);
        userChanelsFragment.setArguments(args);
        return userChanelsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        _channelListView = new ChannelListView(getActivity());
        _channelListView.setDataSource(true, (ArrayList<ChannelInfo>) _activityInfo.dataInfo);

        return _channelListView;
    }

    @Override
    public String getTitle() {
        return _activityInfo.title;
    }
}
