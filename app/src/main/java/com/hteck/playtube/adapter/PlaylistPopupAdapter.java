package com.hteck.playtube.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.databinding.ItemPopupPlaylistBinding;
import com.hteck.playtube.holder.ItemPopupPlaylistViewHolder;

import java.util.ArrayList;

public class PlaylistPopupAdapter extends BaseAdapter {
    private ArrayList<PlaylistInfo> _playlistList;

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
        ItemPopupPlaylistViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = MainActivity.getInstance()
                    .getLayoutInflater();
            ItemPopupPlaylistBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_popup_playlist, group, false);
            holder = new ItemPopupPlaylistViewHolder(itemBinding);
            holder.view = itemBinding.getRoot();
            holder.view.setTag(holder);
        } else {
            holder = (ItemPopupPlaylistViewHolder) convertView.getTag();
        }

        try {
            holder.view.setBackgroundResource(0);
            PlaylistInfo playlistInfo = _playlistList.get(position);
            holder.binding.itemPlaylistTitle.setText(playlistInfo.title.toUpperCase());
            if (playlistInfo.id == null) {
                holder.binding.itemPlaylistImgThumb.setImageResource(R.drawable.ic_playlist_add_black);
                holder.binding.itemPlaylistImgThumb.setTag(null);
            } else {
                ViewHelper.displayYoutubeThumb(holder.binding.itemPlaylistImgThumb, playlistInfo.imageUrl);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return holder.view;
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
