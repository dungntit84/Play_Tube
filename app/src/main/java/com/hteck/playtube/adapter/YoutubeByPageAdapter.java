package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.ArrayList;

public class YoutubeByPageAdapter extends YoutubeAdapter {
    private boolean _isNetworkError;

    public YoutubeByPageAdapter(ArrayList<YoutubeInfo> videoList) {
        super(videoList, Constants.YoutubeListType.Normal);
    }

    public YoutubeByPageAdapter(ArrayList<YoutubeInfo> videoList, Constants.YoutubeListType youtubeListType) {
        super(videoList, youtubeListType);
    }

    public void setIsNetworkError(boolean isNetworkError) {
        _isNetworkError = isNetworkError;
    }

    public boolean getIsNetworkError() {
        return _isNetworkError;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        View v = super.getView(position, convertView, group);;

        if (position == _youtubeList.size() - 1) {
            if (_youtubeList.get(position) == null) {
                if (!_isNetworkError) {
                    v.findViewById(R.id.layout_loading_view).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.layout_item_load_more).setVisibility(View.GONE);
                    v.findViewById(R.id.layout_youtube).setVisibility(View.GONE);
                } else {
                    TextView textView = v.findViewById(R.id.item_load_more_tv_msg);
                    textView.setText(Utils.getString(R.string.network_error_info));
                    v.findViewById(R.id.layout_loading_view).setVisibility(View.GONE);
                    v.findViewById(R.id.layout_item_load_more).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.layout_youtube).setVisibility(View.GONE);
                }
                return v;
            }
        }
        v.findViewById(R.id.layout_loading_view).setVisibility(View.GONE);
        v.findViewById(R.id.layout_item_load_more).setVisibility(View.GONE);
        v.findViewById(R.id.layout_youtube).setVisibility(View.VISIBLE);
        return v;
    }
}
