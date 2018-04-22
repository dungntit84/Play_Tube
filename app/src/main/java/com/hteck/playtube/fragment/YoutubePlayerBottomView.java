package com.hteck.playtube.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PagerAdapter;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.databinding.TabsViewBinding;

public class YoutubePlayerBottomView extends Fragment {
    private PlayerYoutubeInfoView playerYoutubeInfoView;
    private YoutubePlayerVideosView youtubePlayerVideosRelatedView;
    private YoutubePlayerVideosView youtubePlayerVideosMoreView;

    public static YoutubePlayerBottomView newInstance() {

        return new YoutubePlayerBottomView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return createView(container);
    }

    public void refreshData() {
        if (playerYoutubeInfoView != null) {
            playerYoutubeInfoView.refreshData();
        }
        if (youtubePlayerVideosRelatedView != null) {
            youtubePlayerVideosRelatedView.resetData("");
        }
        if (youtubePlayerVideosMoreView != null) {
            youtubePlayerVideosMoreView.resetData(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo().uploaderId);
        }
    }

    private View createView(ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        TabsViewBinding binding = DataBindingUtil.inflate(inflater, R.layout.tabs_view, group, false);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());

        playerYoutubeInfoView = PlayerYoutubeInfoView.newInstance(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo());
        youtubePlayerVideosRelatedView = YoutubePlayerVideosView.newInstance("");
        youtubePlayerVideosMoreView = YoutubePlayerVideosView.newInstance(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo().uploaderId);
        pagerAdapter.addFragment(youtubePlayerVideosRelatedView, Utils.getString(R.string.related));
        pagerAdapter.addFragment(youtubePlayerVideosMoreView, Utils.getString(R.string.more));
        pagerAdapter.addFragment(playerYoutubeInfoView, Utils.getString(R.string.info));
        binding.pager.setAdapter(pagerAdapter);
        binding.tabs.setupWithViewPager(binding.pager);
        binding.tabs.setTabMode(TabLayout.MODE_FIXED);

        binding.pager.setOffscreenPageLimit(4);
        return binding.getRoot();
    }
}
