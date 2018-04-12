package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.databinding.SettingsBinding;

public class SettingsView extends BaseFragment implements View.OnClickListener {

    private SettingsBinding _binding;

    public static SettingsView newInstance() {
        SettingsView settingsView = new SettingsView();
        return settingsView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.getInstance().updateHomeIcon();
        return createView(container);
    }

    private View createView(ViewGroup container) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        _binding = DataBindingUtil.inflate(inflater, R.layout.list_view, container, false);

        _binding.settingsLayoutShare.setOnClickListener(this);
        _binding.settingsLayoutRate.setOnClickListener(this);
        _binding.settingsLayoutEmail.setOnClickListener(this);
        return _binding.getRoot();
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
