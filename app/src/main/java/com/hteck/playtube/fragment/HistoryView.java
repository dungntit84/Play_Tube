package com.hteck.playtube.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.HistoryAdapter;
import com.hteck.playtube.common.Utils;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.databinding.ListViewBinding;
import com.hteck.playtube.service.HistoryService;

import java.util.ArrayList;

public class HistoryView extends BaseFragment implements
        AdapterView.OnItemClickListener {

    private ArrayList<YoutubeInfo> _youtubeList = new ArrayList<>();
    private HistoryAdapter _adapter;
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
        _adapter = new HistoryAdapter(getContext(), _youtubeList);
        _binding.listView.setAdapter(_adapter);
        _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);
        MainActivity.getInstance().setHeader();
        return _binding.getRoot();
    }

    public void refreshData() {
        try {
            _youtubeList = HistoryService.getAllHistory();
            _adapter.setDataSource(_youtubeList);
            _binding.listView.setEmptyView(_binding.textViewMsg);
            _binding.textViewMsg.setVisibility(_youtubeList.size() == 0 ? View.VISIBLE : View.GONE);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
        if (index == 0) {
            clearAll();
            return;
        }

        MainActivity.getInstance().playYoutube(_youtubeList.get(index - 1),
                _youtubeList, true);
    }

    private void clearAll() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.getInstance(),
                AlertDialog.THEME_HOLO_LIGHT);
        alert.setMessage(Utils.getString(R.string.clear_history_confirm));

        alert.setPositiveButton(Utils.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                HistoryService.clearHistory();
                refreshData();
            }
        });

        alert.setNeutralButton(Utils.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alert.show();
    }
    @Override
    public String getTitle() {
        return Utils.getString(R.string.history);
    }
}
