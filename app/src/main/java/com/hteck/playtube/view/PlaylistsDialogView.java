package com.hteck.playtube.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class PlaylistsDialogView extends FrameLayout {

	public PlaylistsDialogView(Context context) {
		super(context);

		ListView listView = new ListView(context);
		ArrayList<PlaylistInfo> playlists = PlaylistService.addPlaylist();
	}
}
