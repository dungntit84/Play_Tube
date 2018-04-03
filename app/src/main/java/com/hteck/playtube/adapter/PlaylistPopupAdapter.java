package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
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
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        View v = inflater.inflate(R.layout.item_popup_playlist, null);
        try {
            PlaylistInfo playlistInfo = _playlistList.get(position);
            TextView textViewTitle = v.findViewById(R.id.item_playlist_title);
            textViewTitle.setText(playlistInfo.title.toUpperCase());
            ImageView iv = (ImageView) v.findViewById(R.id.item_playlist_img_thumb);
            if (playlistInfo.id == null) {
                iv.setImageResource(R.drawable.ic_playlist_add_black);
                iv.setTag(null);
            } else {
                ViewHelper.displayYoutubeThumb(iv, playlistInfo.imageUrl);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

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
