package com.hteck.playtube.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hteck.playtube.view.YoutubeListView;


public class SearchVideoFragment extends Fragment {

	private static YoutubeListView _youtubeListView;

	public static SearchVideoFragment newInstance() {
		SearchVideoFragment f = new SearchVideoFragment();
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (_youtubeListView == null) {
			_youtubeListView = new YoutubeListView(getContext());
		} else {
			ViewGroup parent = (ViewGroup) _youtubeListView.getParent();
			if (parent != null) {
				parent.removeView(_youtubeListView);
			}
		}
		return _youtubeListView;
	}

	public void search(String query) {
		_youtubeListView.search(query);
	}
	
	public YoutubeListView getView() {
		return _youtubeListView;
	}
}
