package com.hteck.playtube.fragment;

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
import com.hteck.playtube.service.HistoryService;
import com.hteck.playtube.service.PlaylistService;

import java.util.ArrayList;

public class HistoryView extends BaseFragment implements
        AdapterView.OnItemClickListener {
    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>();
    private YoutubeAdapter _adapter;
    private ViewGroup _mainView;
    private ListView _listView;
    private TextView _textViewMsg;

    public static HistoryView newInstance() {
        HistoryView historyView = new HistoryView();
        return historyView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _mainView = (FrameLayout) inflater.inflate(
                R.layout.list_view, null);

        _listView = _mainView.findViewById(R.id.list_view);
        _textViewMsg = _mainView.findViewById(R.id.text_view_msg);
        _textViewMsg.setText(Utils.getString(R.string.no_youtube));
        _listView.setOnItemClickListener(this);
        _youtubeList = HistoryService.getAllHistory();
        _adapter = new YoutubeAdapter(_youtubeList, Constants.YoutubeListType.Recent);
        _listView.setAdapter(_adapter);
        _textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);
        MainActivity.getInstance().updateHomeIcon();
        return _mainView;
    }

    public void resetData() {
        try {
            _youtubeList = HistoryService.getAllHistory();
            _adapter.setDataSource(_youtubeList);
            _textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {

        MainActivity.getInstance().playYoutube(_youtubeList.get(index),
                _youtubeList, true, true);
    }

    @Override
    public String getTitle() {
        return Utils.getString(R.string.history);
    }
}
