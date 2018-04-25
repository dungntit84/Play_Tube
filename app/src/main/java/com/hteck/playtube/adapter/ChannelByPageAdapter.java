package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.venustech.playtube.MainActivity;
import com.venustech.playtube.R;
import com.venustech.playtube.common.Utils;
import com.venustech.playtube.info.ChannelInfo;

import java.util.Vector;

public class ChannelByPageAdapter extends ChannelAdapter {
	public boolean mIsNetworkError;

	public ChannelByPageAdapter(Vector<ChannelInfo> channelList) {
		super(channelList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		View v = null;
		LayoutInflater inflater = MainActivity.getInstance()
				.getLayoutInflater();
		if (position == getCount() - 1) {
			if (_channelList.lastElement() == null || Utils.isLoadMoreChannels(_channelList)) {
				if (!mIsNetworkError) {
					v = inflater.inflate(R.layout.loading_view, null);
				} else {
					v = inflater.inflate(R.layout.item_load_more, null);
				}
				return v;
			}
		}
		return super.getView(position, convertView, group);
	}

	@Override
	public int getCount() {

		if (Utils.isLoadMoreChannels(_channelList)) {
			int size = 0;
			for (ChannelInfo c : _channelList) {
				if (!Utils.isNullOrEmpty(c.title)) {
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
