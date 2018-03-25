package com.hteck.playtube.adapter;

import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;

import java.util.ArrayList;

public class PlaylistPopupAdapter extends BaseAdapter {
    public ArrayList<PlaylistInfo> _playlistList;

    public PlaylistPopupAdapter(ArrayList<PlaylistInfo> playlistList) {
        super();
        _playlistList = playlistList;
    }

    public void setDataSource(ArrayList<PlaylistInfo> videoList) {
        _playlistList = videoList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        View v = ViewHelper.getConvertView(convertView, R.layout.item_playlist);

        try {
            PlaylistInfo playlistInfo = _playlistList.get(position);
            TextView textViewTitle = v.findViewById(R.id.item_youtube_tv_title);
            textViewTitle.setText(playlistInfo.title.toUpperCase());

            ImageView iv = (ImageView) v.findViewById(R.id.item_playlist_img_thumb);
            ViewHelper.displayYoutubeThumb(iv, playlistInfo.imageUrl);

            ImageView imageViewAction = (ImageView) v
                    .findViewById(R.id.item_playlist_img_action);

            imageViewAction.setTag(playlistInfo);
            imageViewAction.setOnClickListener(onClickListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.getInstance(), v);

                popup.getMenuInflater().inflate(R.menu.item_video, popup.getMenu());
                PlaylistInfo videoInfo = (PlaylistInfo) v.getTag();

                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        return false;
                    }
                });


        }
    };

    @Override
    public int getCount() {

        return _playlistList.size();
    }

    @Override
    public Object getItem(int position) {
        return _playlistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
