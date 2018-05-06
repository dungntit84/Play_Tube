package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hteck.playtube.R;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.ItemChannelBinding;
import com.hteck.playtube.holder.BaseViewHolder;

import java.util.ArrayList;

import static com.hteck.playtube.common.ViewHelper.displayChannelThumb;

public class ChannelAdapter extends BaseAdapter {
    protected Context _context;
    public ArrayList<ChannelInfo> _channelList;

    public ChannelAdapter(Context context, ArrayList<ChannelInfo> channelList) {
        super();
        _context = context;
        _channelList = channelList;
    }

    public void setDataSource(ArrayList<ChannelInfo> channelList) {
        _channelList = channelList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        return getDetailsView(LayoutInflater.from(_context), convertView, group, _channelList.get(position));
    }

    public static View getDetailsView(LayoutInflater inflater, View convertView, ViewGroup group, ChannelInfo channelInfo) {
        BaseViewHolder holder;
        holder = ViewHelper.getViewHolder(inflater, convertView, group, R.layout.item_channel);
        ItemChannelBinding binding = (ItemChannelBinding) holder.binding;

        binding.textViewTitle.setText(channelInfo.title);
        displayChannelThumb(binding.imageViewThumb, channelInfo.imageUrl);
        binding.textViewVideoCount.setText(channelInfo.getDisplayVideoCount());
        binding.textViewVideoCount.setVisibility(channelInfo.videoCount > 0 ? View.VISIBLE
                : View.GONE);
        binding.textViewSubscriberCount.setText(channelInfo.getDisplaySubscriberCount());

        return holder.view;
    }

    @Override
    public int getCount() {

        return _channelList.size();
    }

    @Override
    public Object getItem(int position) {
        return _channelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
