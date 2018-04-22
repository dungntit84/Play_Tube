package com.hteck.playtube.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.PlaylistPopupAdapter;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class PlaylistsDialogView extends FrameLayout {

    private YoutubeInfo _youtubeInfo;
    public AlertDialog mDialogParent;

    public PlaylistsDialogView(Context context, YoutubeInfo youtubeInfo) {
        super(context);
        _youtubeInfo = youtubeInfo;
        ListView listView = new ListView(context);
        final ArrayList<PlaylistInfo> playlists = PlaylistService.getAllPlaylists();
        PlaylistInfo addNewPlaylist = new PlaylistInfo();
        addNewPlaylist.title = Utils.getString(R.string.create_new_playlist);
        playlists.add(0, addNewPlaylist);
        PlaylistPopupAdapter adapter = new PlaylistPopupAdapter(playlists);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                try {
                    if (index == 0) {
                        ViewHelper.showAddNewPlaylist(_youtubeInfo, mDialogParent);
                    } else {
                        PlaylistInfo playlistInfo = playlists.get(index);
                        PlaylistService.addPlaylist(playlistInfo, _youtubeInfo);
                        Utils.showMessage(Utils.getString(R.string.added));
                        mDialogParent.dismiss();
                        MainActivity.getInstance().refreshPlaylistData();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        addView(listView);
    }
}
