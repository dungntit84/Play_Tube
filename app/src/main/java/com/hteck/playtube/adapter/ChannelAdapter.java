package com.hteck.playtube.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.ChannelInfo;
import com.hteck.playtube.databinding.ItemChannelBinding;
import com.hteck.playtube.databinding.ItemPlaylistBinding;
import com.hteck.playtube.holder.BaseViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

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
        BaseViewHolder holder;
        holder = ViewHelper.getViewHolder(LayoutInflater.from(_context), convertView, R.layout.item_channel, group);
        ItemChannelBinding binding = (ItemChannelBinding) holder.binding;

        ChannelInfo channelInfo = _channelList.get(position);

        binding.textViewTitle.setText(channelInfo.title);
        ImageLoader.getInstance().displayImage(channelInfo.imageUrl, binding.imageViewThumb);
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
