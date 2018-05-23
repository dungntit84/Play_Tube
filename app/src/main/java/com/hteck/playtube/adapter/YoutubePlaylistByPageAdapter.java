package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ItemLoadMoreBinding;
import com.hteck.playtube.databinding.ItemPlaylistBinding;
import com.hteck.playtube.fragment.UserActivityFragment;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.ArrayList;

public class YoutubePlaylistByPageAdapter extends YoutubePlaylistAdapter {
    private boolean _isNetworkError;

    public YoutubePlaylistByPageAdapter(Context context, ArrayList<YoutubePlaylistInfo> playlists) {
        super(context, playlists);
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
    public int getCount() {
        if (Utils.isLoadMorePlaylists(_playlists)) {
            int size = 0;
            for (YoutubePlaylistInfo p : _playlists) {
                if (p != null && !Utils.stringIsNullOrEmpty(p.title)) {
                    size++;
                } else {
                    break;
                }
            }
            return size + 1;
        }
        return _playlists.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getCount() - 1) {
            if (Utils.isLoadMorePlaylists(_playlists) || (_playlists.size() > 0 && _playlists.get(_playlists.size() - 1) == null)) {
                if (!_isNetworkError) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
        return 2;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
