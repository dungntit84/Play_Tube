package com.hteck.playtube.adapter;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.View.OnClickListener;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.YoutubeInfo;

import java.util.ArrayList;

public class YoutubeAdapter extends BaseAdapter {
    public ArrayList<YoutubeInfo> _youtubeList;
    private Constants.YoutubeListType _youtubeListType = Constants.YoutubeListType.Normal;
    private Object _dataContext;

    public YoutubeAdapter(ArrayList<YoutubeInfo> videoList, Constants.YoutubeListType type) {
        super();
        _youtubeList = videoList;
        _youtubeListType = type;
    }

    public void setDataSource(ArrayList<YoutubeInfo> videoList) {
        _youtubeList = videoList;
        notifyDataSetChanged();
    }

    public void setDataContext(Object dataContext) {
        _dataContext = dataContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        if (_youtubeListType == Constants.YoutubeListType.Popular) {
            return ViewHelper.getGridYoutubeView(convertView, _youtubeList.get(position), _youtubeListType, onClickListener);
        }
        return ViewHelper.getYoutubeView(convertView, _youtubeList.get(position), _youtubeListType, onClickListener);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHelper.showYoutubeMenu(_youtubeListType, _dataContext, v);
        }
    };

    @Override
    public int getCount() {

        return _youtubeList.size();
    }

    @Override
    public Object getItem(int position) {
        return _youtubeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
