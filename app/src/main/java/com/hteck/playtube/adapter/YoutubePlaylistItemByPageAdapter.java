package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.data.PlaylistItemInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ItemLoadMoreBinding;
import com.hteck.playtube.fragment.UserActivityFragment;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.ArrayList;
import java.util.Vector;

public class YoutubePlaylistItemByPageAdapter extends YoutubePlaylistItemAdapter {
    private boolean _isNetworkError;

    public YoutubePlaylistItemByPageAdapter(Context context, UserActivityFragment channelHomeTab, ChannelInfo channelInfo, Vector<PlaylistItemInfo> items) {
        super(context, channelHomeTab, channelInfo, items);
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
        if (position == getCount() - 1) {
            if (_items.get(_items.size() - 1) == null) {
                if (!_isNetworkError) {
                    holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, group, R.layout.loading_view);
                } else {
                    holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, group, R.layout.item_load_more);
                    ((ItemLoadMoreBinding) holder.binding).itemLoadMoreTvMsg.setText(Utils.getString(R.string.network_error_info));
                }
                return holder.view;
            }
        }
        return super.getView(position, convertView, group);
    }
}
