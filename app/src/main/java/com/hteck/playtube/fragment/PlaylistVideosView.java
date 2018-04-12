package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class PlaylistVideosView extends BaseFragment implements
        AdapterView.OnItemClickListener {
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>();
    private YoutubeAdapter _adapter;
    private PlaylistInfo _playlistInfo;
    private ListViewBinding _binding;

    public static PlaylistVideosView newInstance(PlaylistInfo playlistInfo) {
        PlaylistVideosView playlistVideosView = new PlaylistVideosView();
        playlistVideosView._playlistInfo = playlistInfo;
        return playlistVideosView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _binding = DataBindingUtil.inflate(inflater, R.layout.list_view, container, false);

        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));
        _binding.listView.setOnItemClickListener(this);
        _youtubeList = _playlistInfo.youtubeList;
        _adapter = new YoutubeAdapter(_youtubeList, Constants.YoutubeListType.Playlist);
        _adapter.setDataContext(_playlistInfo);
        _binding.listView.setAdapter(_adapter);
        _binding.listView.setEmptyView(_binding.textViewMsg);
        _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);
        MainActivity.getInstance().updateHomeIcon();
        return _binding.getRoot();
    }

    public void refreshData() {
        try {
            PlaylistInfo playlistInfo = PlaylistService.getPlaylistInfoById(_playlistInfo.id);
            if (playlistInfo != null) {
                _playlistInfo = playlistInfo;

                _youtubeList = _playlistInfo.youtubeList;
                _adapter.setDataSource(_youtubeList);
                _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

        MainActivity.getInstance().playYoutube(_youtubeList.get(index),
                _youtubeList, true);
    }

    @Override
    public String getTitle() {
        return _playlistInfo.title;
    }
}
