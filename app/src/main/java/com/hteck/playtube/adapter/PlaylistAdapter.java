package com.hteck.playtube.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.fragment.PlaylistsView;
import com.hteck.playtube.service.PlaylistService;
import com.hteck.playtube.view.PlaylistsDialogView;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {
    public ArrayList<PlaylistInfo> _playlistList;

    public PlaylistAdapter(ArrayList<PlaylistInfo> playlistList) {
        super();
        _playlistList = playlistList;
    }

    public void setDataSource(ArrayList<PlaylistInfo> videoList) {
        _playlistList = videoList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance().getLayoutInflater();
        PlaylistInfo playlistInfo = _playlistList.get(position);
        View v = playlistInfo.id == null ? inflater.inflate(R.layout.item_popup_playlist, null) : inflater.inflate(R.layout.item_playlist, null);
        try {
            TextView textViewTitle = v.findViewById(R.id.item_playlist_title);
            TextView textViewCount = v.findViewById(R.id.item_playlist_count);
            textViewTitle.setText(playlistInfo.title.toUpperCase());
            textViewTitle.setTextColor(MainActivity.getInstance().getResources().getColor(R.color.textColor));
            if (textViewCount != null) {
                textViewCount.setText(Utils.getDisplayVideos(playlistInfo.youtubeList.size()));
            }
            ImageView iv = v.findViewById(R.id.item_playlist_img_thumb);

            if (playlistInfo.id == null) {
                iv.setImageResource(R.drawable.ic_playlist_add);
                iv.setTag(null);
            } else {
                ViewHelper.displayYoutubeThumb(iv, playlistInfo.imageUrl);
            }
            ImageView imageViewAction = (ImageView) v
                    .findViewById(R.id.item_playlist_img_action);
            if (imageViewAction != null) {
                imageViewAction.setTag(playlistInfo);
                imageViewAction.setOnClickListener(onClickListener);
                if (playlistInfo.id.equals(Utils.buildFavouritesUUID())) {
                    imageViewAction.setVisibility(View.GONE);
                } else {
                    imageViewAction.setVisibility(View.VISIBLE);
                }
            }
            v.setTag(playlistInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(MainActivity.getInstance(), v);

            popup.getMenuInflater().inflate(R.menu.item_playlist, popup.getMenu());
            final PlaylistInfo playlistInfo = (PlaylistInfo) v.getTag();

            popup.show();

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.menu_item_rename) {
                        ViewHelper.showAddNewPlaylist(playlistInfo);
                    } else if (item.getItemId() == R.id.menu_item_remove) {
                        confirmRemovePlaylist(playlistInfo);
                    }
                    return false;
                }
            });
        }
    };

    private void confirmRemovePlaylist(final PlaylistInfo playlistInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.getInstance());
        String msg = String.format(Utils.getString(R.string.remove_playlist_confirm), playlistInfo.title);
        builder.setMessage(msg).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                PlaylistService.removePlaylist(playlistInfo);
                MainActivity.getInstance().refreshPlaylistData();
            }
        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public int getCount() {

        return _playlistList.size();
    }

    @Override
    public Object getItem(int position) {
        return _playlistList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
