package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PagerAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.TabsSearchViewBinding;

public class UserDetailsFragment extends BaseFragment {
    private UserVideosFragment _userVideosFragment;
    private UserActivityFragment _userActivityFragment;
    private UserPlaylistsFragment _userPlaylistsFragment;
    private TabsSearchViewBinding _binding;
    private ChannelInfo _channelInfo;
    private boolean _isPlaylistsTabLoaded;
    private boolean _isVideosTabLoaded;

    public static UserDetailsFragment newInstance(
            ChannelInfo channelInfo) {
        UserDetailsFragment v = new UserDetailsFragment();
        v._channelInfo = channelInfo;
        Bundle args = new Bundle();
        args.putString(Constants.PAGE_ID, channelInfo.id.toString());
        v.setArguments(args);
        return v;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.getInstance().setHeader();

        return createView(container);
    }

    private View createView(ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _binding = DataBindingUtil.inflate(inflater, R.layout.tabs_search_view, group, false);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());

        _userVideosFragment = UserVideosFragment.newInstance(_channelInfo, Constants.SortBy.MOSTRECENT);
        _userActivityFragment = UserActivityFragment.newInstance(_channelInfo);
        _userPlaylistsFragment = UserPlaylistsFragment.newInstance(_channelInfo);
        pagerAdapter.addFragment(_userVideosFragment, Utils.getString(R.string.video));
        pagerAdapter.addFragment(_userActivityFragment, Utils.getString(R.string.channel));
        pagerAdapter.addFragment(_userPlaylistsFragment, Utils.getString(R.string.playlist));
        _binding.pager.setAdapter(pagerAdapter);
        _binding.tabs.setupWithViewPager(_binding.pager);
        _binding.tabs.setTabMode(TabLayout.MODE_FIXED);
        _binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        _binding.pager.setOffscreenPageLimit(3);
        _binding.pager.setCurrentItem(1);
        return _binding.getRoot();
    }


    private void loadData() {
        if (_binding.tabs.getSelectedTabPosition() == 0) {
            if (!_isVideosTabLoaded) {
                _isVideosTabLoaded = true;
                _userVideosFragment.loadData();
            }
        }
        else if (_binding.tabs.getSelectedTabPosition() == 2) {
            if (!_isPlaylistsTabLoaded) {
                _isPlaylistsTabLoaded = true;
                _userPlaylistsFragment.loadData();
            }
        }
    }

    @Override
    public String getTitle() {
        return _channelInfo.title;
    }
}
