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

    public YoutubePlaylistItemByPageAdapter(Context context, UserActivityFragment channelHomeTab, ChannelInfo channelInfo, ArrayList<PlaylistItemInfo> items) {
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
    public int getItemViewType(int position) {
        if (position == getCount() - 1) {
            if (_items.size() > 0 && _items.get(_items.size() - 1) == null) {
                if (!_isNetworkError) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
        if (position == 0) {
            return 2;
        }
        PlaylistItemInfo playlistItemViewInfo = _items.get(position - 1);
        if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.NAME) {
            return 3;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.YOUTUBE) {
            return 4;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.PLAYLIST) {
            return 5;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.CHANNEL) {
            return 6;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SHOWMORE) {
            return 7;
        } else if (playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.COMMENTED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.UPLOADEDANDPOSTED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.RECOMMENDED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.SUBSCRIBED
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.OTHERACTION
                || playlistItemViewInfo.playlistItemType == Constants.PlaylistItemType.LIKED) {
            return 8;
        } else {
            return 9;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 10;
    }
}
