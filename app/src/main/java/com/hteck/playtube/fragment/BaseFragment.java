package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;

public abstract class BaseFragment extends Fragment {
    public abstract String getTitle();

    public String getRightTitle() {
        return null;
    }

    public Constants.RightTitleType getRightTitleType() {
        return Constants.RightTitleType.None;
    }

    public View.OnClickListener getGetRightEventListener() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        MainActivity.getInstance().setHeader();
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
