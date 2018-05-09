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
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.TabsSearchViewBinding;

public class UserDetailsFragment extends BaseFragment {
    private static ChannelVideosFragment _channelVideosFragment;
    private static UserActivityFragment _userActivityFragment;
    private TabsSearchViewBinding _binding;
    private ChannelInfo _channelInfo;
    private boolean _isPlaylistsTabLoaded;
    private boolean _isVideosTabLoaded;

    public static UserDetailsFragment newInstance(
            ChannelInfo channelInfo) {
        UserDetailsFragment v = new UserDetailsFragment();
        v._channelInfo = channelInfo;
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

        _channelVideosFragment = ChannelVideosFragment.newInstance(_channelInfo);
        _userActivityFragment = UserActivityFragment.newInstance(_channelInfo);

        pagerAdapter.addFragment(_channelVideosFragment, Utils.getString(R.string.video));
        pagerAdapter.addFragment(_userActivityFragment, Utils.getString(R.string.channel));
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
        if (_binding.pager.getCurrentItem() == 0) {
            if (!_isPlaylistsTabLoaded) {
                _isPlaylistsTabLoaded = true;
                _channelVideosFragment.loadData();
            }
        }
    }

    @Override
    public String getTitle() {
        return _channelInfo.title;
    }
}
