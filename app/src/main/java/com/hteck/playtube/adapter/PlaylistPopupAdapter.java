package com.hteck.playtube.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.databinding.ItemAddPlaylistBinding;
import com.hteck.playtube.databinding.ItemPopupPlaylistBinding;
import com.hteck.playtube.holder.BaseViewHolder;
import com.hteck.playtube.holder.ItemPopupPlaylistViewHolder;

import java.util.ArrayList;

public class PlaylistPopupAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<PlaylistInfo> _playlistList;

    public PlaylistPopupAdapter(Context context, ArrayList<PlaylistInfo> playlistList) {
        super();
        _context = context;
        _playlistList = playlistList;
    }

    public void setDataSource(ArrayList<PlaylistInfo> videoList) {
        _playlistList = videoList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        PlaylistInfo playlistInfo = _playlistList.get(position);
        if (playlistInfo.id == null) {
            convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.item_add_playlist);
            ((ItemAddPlaylistBinding) DataBindingUtil.getBinding(convertView)).itemPlaylistTitle.setText(playlistInfo.title.toUpperCase());
        } else {
            convertView = ViewHelper.getViewHolder1(LayoutInflater.from(_context), convertView, group, R.layout.item_popup_playlist);
            ((ItemPopupPlaylistBinding) DataBindingUtil.getBinding(convertView)).itemPlaylistTitle.setText(playlistInfo.title.toUpperCase());
            ViewHelper.displayYoutubeThumb(((ItemPopupPlaylistBinding) DataBindingUtil.getBinding(convertView)).itemPlaylistImgThumb, playlistInfo.imageUrl);
        }
        return convertView;
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
