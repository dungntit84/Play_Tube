package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.ArrayList;

public class YoutubeByPageAdapter extends YoutubeAdapter {
	private boolean _isNetworkError;
	public YoutubeByPageAdapter(ArrayList<YoutubeInfo> videoList) {
		super(videoList, Constants.YoutubeListType.Normal);
	}
	public YoutubeByPageAdapter(ArrayList<YoutubeInfo> videoList, Constants.YoutubeListType youtubeListType) {
		super(videoList, youtubeListType);
	}

	public void setIsNetworkError(boolean isNetworkError) {
		_isNetworkError = isNetworkError;
	}

	public boolean getIsNetworkError() {
		return _isNetworkError;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		View v = null;
		LayoutInflater inflater = MainActivity.getInstance()
				.getLayoutInflater();
		if (position == _youtubeList.size() - 1) {
			if (_youtubeList.get(position) == null) {
				
				if (!_isNetworkError) {
					v = inflater.inflate(R.layout.loading_view, null);
				} else {
					v = inflater.inflate(R.layout.item_load_more, null);
					TextView textView = v.findViewById(R.id.item_load_more_tv_msg);
					textView.setText(Utils.getString(R.string.network_error_info));
				}
				v.setTag(Constants.CUSTOM_TAG, null);
				return v;
			}
		}
		return super.getView(position, convertView, group);
	}
}
