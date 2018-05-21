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
        BaseViewHolder holder;
        switch (getItemViewType(position)) {
            case 0:
            case 1: {
                if (convertView == null) {
                    if (!_isNetworkError) {
                        holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, group, R.layout.loading_view);
                    } else {
                        holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, group, R.layout.item_load_more);
                        ((ItemLoadMoreBinding) holder.binding).itemLoadMoreTvMsg.setText(Utils.getString(R.string.network_error_info));
                    }
                    convertView = holder.view;
                    convertView.setTag(holder);
                } else {
                    holder = (BaseViewHolder) convertView.getTag();
                }
                break;
            }
            default: {
                if (convertView == null) {
                    holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, group, R.layout.item_playlist);

                    convertView = holder.view;
                    convertView.setTag(holder);
                } else {
                    holder = (BaseViewHolder) convertView.getTag();
                }
                ItemPlaylistBinding binding = (ItemPlaylistBinding) holder.binding;
                YoutubePlaylistInfo playlistInfo = _playlists.get(position);
                binding.itemPlaylistTitle.setText(playlistInfo.title);
                ViewHelper.displayYoutubeThumb(binding.itemPlaylistImgThumb, playlistInfo.imgeUrl);
                binding.itemPlaylistCount.setText(playlistInfo.getDisplayNumOfVideos());
                binding.itemPlaylistImgAction.setVisibility(View.GONE);
                break;
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        if (Utils.isLoadMorePlaylists(_playlists)) {
            int size = 0;
            for (YoutubePlaylistInfo p : _playlists) {
                if (!Utils.stringIsNullOrEmpty(p.title)) {
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
