package com.hteck.playtube.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
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
import com.hteck.playtube.databinding.GridItemYoutubeViewBinding;
import com.hteck.playtube.databinding.ItemPopupPlaylistBinding;

import java.util.ArrayList;

public class HistoryAdapter extends YoutubeAdapter {

    public HistoryAdapter(Context context, ArrayList<YoutubeInfo> videoList) {
        super(context,videoList, Constants.YoutubeListType.Recent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        if (position == 0) {
            ItemPopupPlaylistBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_popup_playlist, group, false);
            binding.itemPlaylistTitle.setText(Utils.getString(R.string.clear_history).toUpperCase());
            binding.itemPlaylistTitle.setTextColor(MainActivity.getInstance().getResources().getColor(R.color.textColor));
            binding.itemPlaylistImgThumb.setImageResource(R.drawable.ic_clear);
            return binding.getRoot();
        }
        return super.getView(position - 1, convertView, group);
    }

    @Override
    public int getCount() {

        return _youtubeList.size() == 0 ? 0 : _youtubeList.size() + 1;
    }
}
