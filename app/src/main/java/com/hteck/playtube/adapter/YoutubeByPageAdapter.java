package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.ArrayList;

public class YoutubeByPageAdapter extends YoutubeAdapter {
    private boolean _isNetworkError;

    public YoutubeByPageAdapter(Context context, ArrayList<YoutubeInfo> videoList) {
        super(context, videoList, Constants.YoutubeListType.Normal);
    }

    public YoutubeByPageAdapter(Context context, ArrayList<YoutubeInfo> videoList, Constants.YoutubeListType youtubeListType) {
        super(context, videoList, youtubeListType);
    }

    public void setIsNetworkError(boolean isNetworkError) {
        _isNetworkError = isNetworkError;
    }

    public boolean getIsNetworkError() {
        return _isNetworkError;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        switch (getItemViewType(position)) {
            case 0:
            case 1: {
                return ViewHelper.getNetworkErrorView(getItemViewType(position), _context, convertView, group);
            }
            default: {
                return super.getView(position, convertView, group);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHelper.getItemViewType(position, getCount(), _youtubeList, _isNetworkError);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
