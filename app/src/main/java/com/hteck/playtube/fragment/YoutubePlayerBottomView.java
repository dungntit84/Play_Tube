package com.hteck.playtube.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PagerAdapter;
import com.hteck.playtube.common.PlayTubeController;
import com.hteck.playtube.common.Utils;

public class YoutubePlayerBottomView extends Fragment {
    private PagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private PlayerYoutubeInfoView playerYoutubeInfoView;
    private YoutubePlayerVideosView youtubePlayerVideosRelatedView;
    private YoutubePlayerVideosView youtubePlayerVideosMoreView;
    private TabLayout tabLayout;

    public static YoutubePlayerBottomView newInstance() {

        YoutubePlayerBottomView fragment = new YoutubePlayerBottomView();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return createView();
    }

    public void resetData() {
        if (playerYoutubeInfoView != null) {
            playerYoutubeInfoView.resetData();
        }
        if (youtubePlayerVideosRelatedView != null) {
            youtubePlayerVideosRelatedView.resetData("");
        }
        if (youtubePlayerVideosMoreView != null) {
            youtubePlayerVideosMoreView.resetData(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo().uploaderId);
        }
    }

    private View createView() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        View v = inflater.inflate(R.layout.tabs_view, null);
        try {
            viewPager = (ViewPager) v.findViewById(R.id.pager);
            pagerAdapter = new PagerAdapter(getChildFragmentManager());

            playerYoutubeInfoView = PlayerYoutubeInfoView.newInstance(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo());
            youtubePlayerVideosRelatedView = YoutubePlayerVideosView.newInstance("");
            youtubePlayerVideosMoreView = YoutubePlayerVideosView.newInstance(PlayTubeController.getPlayingInfo().getCurrentYoutubeInfo().uploaderId);
            pagerAdapter.addFragment(youtubePlayerVideosRelatedView, Utils.getString(R.string.related));
            pagerAdapter.addFragment(youtubePlayerVideosMoreView, Utils.getString(R.string.more));
            pagerAdapter.addFragment(playerYoutubeInfoView, Utils.getString(R.string.info));
            viewPager.setAdapter(pagerAdapter);
            tabLayout = (TabLayout) v.findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);

            viewPager.setOffscreenPageLimit(4);
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }
}
