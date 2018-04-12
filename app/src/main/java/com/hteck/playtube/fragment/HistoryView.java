package com.hteck.playtube.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.YoutubeAdapter;
import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.PlaylistInfo;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.HistoryService;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class HistoryView extends BaseFragment implements
        AdapterView.OnItemClickListener {
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>();
    private YoutubeAdapter _adapter;

    private ListViewBinding _binding;

    public static HistoryView newInstance() {
        HistoryView historyView = new HistoryView();
        return historyView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = DataBindingUtil.inflate(inflater, R.layout.list_view, container, false);

        _binding.textViewMsg.setText(Utils.getString(R.string.no_youtube));
        _binding.listView.setOnItemClickListener(this);
        _youtubeList = HistoryService.getAllHistory();
        _adapter = new YoutubeAdapter(_youtubeList, Constants.YoutubeListType.Recent);
        _binding.listView.setAdapter(_adapter);
        _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);
        MainActivity.getInstance().updateHomeIcon();
        return _binding.getRoot();
    }

    public void refreshData() {
        try {
            _youtubeList = HistoryService.getAllHistory();
            _adapter.setDataSource(_youtubeList);
            _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);

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
        return Utils.getString(R.string.history);
    }
}
