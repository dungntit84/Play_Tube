package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hteck.playtube.view.YoutubePlaylistListView;


public class SearchPlaylistFragment extends Fragment {

	private static YoutubePlaylistListView _youtubePlaylistListView;

	public static SearchPlaylistFragment newInstance() {
		SearchPlaylistFragment f = new SearchPlaylistFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (_youtubePlaylistListView == null) {
			_youtubePlaylistListView = new YoutubePlaylistListView(getContext());
		} else {
			ViewGroup parent = (ViewGroup) _youtubePlaylistListView.getParent();
			if (parent != null) {
				parent.removeView(_youtubePlaylistListView);
			}
		}
		return _youtubePlaylistListView;
	}

	public void search(String query) {
		_youtubePlaylistListView.search(query);
	}
	
	public YoutubePlaylistListView getView() {
		return _youtubePlaylistListView;
	}
}
