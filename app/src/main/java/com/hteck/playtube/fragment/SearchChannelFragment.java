package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.view.ChannelListView;
import com.hteck.playtube.view.YoutubeListView;


public class SearchChannelFragment extends Fragment {

	private static ChannelListView _channelListView;

	public static SearchChannelFragment newInstance() {
		SearchChannelFragment f = new SearchChannelFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (_channelListView == null) {
			_channelListView = new ChannelListView(getContext());
		} else {
			ViewGroup parent = (ViewGroup) _channelListView.getParent();
			if (parent != null) {
				parent.removeView(_channelListView);
			}
		}
		return _channelListView;
	}

	public void search(String query) {
		_channelListView.search(query);
	}
	
	public ChannelListView getView() {
		return _channelListView;
	}
}
