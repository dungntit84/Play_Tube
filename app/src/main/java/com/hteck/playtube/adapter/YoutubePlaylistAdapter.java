package com.hteck.playtube.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hteck.playtube.R;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.YoutubePlaylistInfo;
import com.hteck.playtube.databinding.ItemPlaylistBinding;
import com.hteck.playtube.holder.BaseViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class YoutubePlaylistAdapter extends BaseAdapter {
    protected Context _context;
    protected ArrayList<YoutubePlaylistInfo> _playlists = new ArrayList<>();

    public YoutubePlaylistAdapter(Context context, ArrayList<YoutubePlaylistInfo> playlists) {
        super();
        _context = context;
        _playlists = playlists;
    }

    public void setDataSource(ArrayList<YoutubePlaylistInfo> playlists) {
        _playlists = playlists;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {

        YoutubePlaylistInfo playlistInfo = _playlists.get(position);

        return getDetailsView(LayoutInflater.from(_context), convertView, group, playlistInfo);
    }

    public static View getDetailsView(LayoutInflater inflater, View convertView, ViewGroup group,
                                      YoutubePlaylistInfo playlistInfo) {
        BaseViewHolder holder;
        holder = ViewHelper.getViewHolder(inflater, convertView, group, R.layout.item_playlist);
        ItemPlaylistBinding binding = (ItemPlaylistBinding) holder.binding;

        binding.itemPlaylistTitle.setText(playlistInfo.title);
        ViewHelper.displayYoutubeThumb(binding.itemPlaylistImgThumb, playlistInfo.imgeUrl);
        binding.itemPlaylistCount.setText(playlistInfo.getDisplayNumOfVideos());
        binding.itemPlaylistImgAction.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public int getCount() {

        return _playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return _playlists.get(position);
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
