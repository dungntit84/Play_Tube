package com.hteck.playtube.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

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
}
