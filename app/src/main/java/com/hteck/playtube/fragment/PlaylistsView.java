package com.hteck.playtube.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PlaylistAdapter;
import com.hteck.playtube.adapter.PlaylistPopupAdapter;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class PlaylistsView extends BaseFragment implements AdapterView.OnItemClickListener {

    private ArrayList<PlaylistInfo> _playlists;
    private PlaylistAdapter _adapter;

    public static PlaylistsView newInstance() {
        PlaylistsView playlistsView = new PlaylistsView();
        return playlistsView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ListView listView = new ListView(getContext());
        _playlists = PlaylistService.getAllPlaylists();
        PlaylistInfo addNewPlaylist = new PlaylistInfo();
        addNewPlaylist.title = Utils.getString(R.string.create_new_playlist);
        _playlists.add(0, addNewPlaylist);
        _adapter = new PlaylistAdapter(_playlists);
        listView.setAdapter(_adapter);
        listView.setOnItemClickListener(this);

        MainActivity.getInstance().updateHomeIcon();
        return listView;
    }

    public void refreshData() {
        _playlists = PlaylistService.getAllPlaylists();
        PlaylistInfo addNewPlaylist = new PlaylistInfo();
        addNewPlaylist.title = Utils.getString(R.string.create_new_playlist);
        _playlists.add(0, addNewPlaylist);
        _adapter.setDataSource(_playlists);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
        try {
            if (index == 0) {
                ViewHelper.showAddNewPlaylist(null);
                return;
            }
            PlaylistInfo playlistInfo = _playlists.get(index);
            PlaylistVideosView playlistVideosView = PlaylistVideosView.newInstance(playlistInfo);
            MainActivity.getInstance().addFragment(playlistVideosView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getTitle() {
        return Utils.getString(R.string.playlists);
    }
}
