package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.R;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.ItemLoadMoreBinding;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.ArrayList;
import java.util.Vector;

public class ChannelByPageAdapter extends ChannelAdapter {
    public boolean _isNetworkError;

    public ChannelByPageAdapter(Context context, ArrayList<ChannelInfo> channelList) {
        super(context, channelList);
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
            if (_channelList.get(_channelList.size() - 1) == null || Utils.haveMoreChannels(_channelList)) {
                if (!_isNetworkError) {
                    holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, R.layout.loading_view, group);
                } else {
                    holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, R.layout.item_load_more, group);
                    ((ItemLoadMoreBinding) holder.binding).itemLoadMoreTvMsg.setText(Utils.getString(R.string.network_error_info));
                }
                return holder.view;
            }
        }
        return super.getView(position, convertView, group);
    }

    @Override
    public int getCount() {

        if (Utils.haveMoreChannels(_channelList)) {
            int size = 0;
            for (ChannelInfo c : _channelList) {
                if (!Utils.stringIsNullOrEmpty(c.title)) {
                    size++;
                } else {
                    break;
                }
            }
            return size + 1;
        }
        return super.getCount();
    }
}
