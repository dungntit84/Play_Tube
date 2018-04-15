package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.ArrayList;

public class HistoryAdapter extends YoutubeAdapter {

    public HistoryAdapter(ArrayList<YoutubeInfo> videoList) {
        super(videoList, Constants.YoutubeListType.Recent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (position == 0) {
            View v = inflater.inflate(R.layout.item_popup_playlist, null);
            TextView textViewTitle = v.findViewById(R.id.item_playlist_title);
            textViewTitle.setText(Utils.getString(R.string.clear_history).toUpperCase());
            textViewTitle.setTextColor(MainActivity.getInstance().getResources().getColor(R.color.textColor));
            ImageView iv = v.findViewById(R.id.item_playlist_img_thumb);
            iv.setImageResource(R.drawable.ic_clear);
            v.setTag(Constants.CUSTOM_TAG, null);
            return v;
        }
        return super.getView(position - 1, convertView, group);
    }

    @Override
    public int getCount() {

        return _youtubeList.size() == 0 ? 0 : _youtubeList.size() + 1;
    }
}
