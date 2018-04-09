package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PlaylistAdapter;
import com.hteck.playtube.adapter.YoutubeByPageAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class SettingsView extends BaseFragment implements View.OnClickListener {

    public static SettingsView newInstance() {
        SettingsView settingsView = new SettingsView();
        return settingsView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.getInstance().updateHomeIcon();
        return createView();
    }

    private View createView() {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        View _mainView = inflater.inflate(R.layout.settings, null);
        View layoutShare = _mainView.findViewById(R.id.settings_layout_share);
        layoutShare.setOnClickListener(this);
        View layoutRate = _mainView.findViewById(R.id.settings_layout_rate);
        layoutRate.setOnClickListener(this);
        View layoutEmail = _mainView.findViewById(R.id.settings_layout_email);
        layoutEmail.setOnClickListener(this);
        return _mainView;
    }

    @Override
    public String getTitle() {
        return Utils.getString(R.string.settings);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_layout_share: {
                Utils.shareApp();
                break;
            }
            case R.id.settings_layout_rate: {
                Utils.rateApp();
                break;
            }
            case R.id.settings_layout_email: {
                Utils.contactUs();
                break;
            }
        }
    }
}
