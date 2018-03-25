package com.hteck.playtube.adapter;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.hteck.playtube.common.Constants;
import com.hteck.playtube.common.ViewHelper;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.service.IYoutubeAction;

import java.util.Vector;

public class YoutubeAdapter extends BaseAdapter {
    public Vector<YoutubeInfo> _youtubeList;
    private Constants.YoutubeListType _youtubeListType = Constants.YoutubeListType.Normal;
    private boolean _isMenuOpening = false;
    private boolean _isPendingReload;
    private IYoutubeAction _youtubeAction;

    public YoutubeAdapter(Vector<YoutubeInfo> videoList, Constants.YoutubeListType type) {
        super();
        _youtubeList = videoList;
        _youtubeListType = type;
    }

    public YoutubeAdapter(Vector<YoutubeInfo> videoList, Constants.YoutubeListType type, IYoutubeAction youtubeAction) {
        super();
        _youtubeList = videoList;
        _youtubeListType = type;
        _youtubeAction = youtubeAction;
    }

    public void setDataSource(Vector<YoutubeInfo> videoList) {
        _youtubeList = videoList;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        if (_isMenuOpening) {
            _isPendingReload = true;
            return;
        }
        _isPendingReload = false;
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        if (_youtubeListType == Constants.YoutubeListType.Popular) {
            return ViewHelper.getGridYoutubeView(convertView, _youtubeList.elementAt(position), _youtubeListType, onClickListener);
        }
        return ViewHelper.getYoutubeView(convertView, _youtubeList.elementAt(position), _youtubeListType, onClickListener);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            _isMenuOpening = true;
            PopupMenu popup = ViewHelper.showYoutubeMenu(_youtubeListType, null, false, v);
            popup.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(PopupMenu menu) {
                    _isMenuOpening = false;
                    if (_isPendingReload) {
                        _isPendingReload = false;
                        notifyDataSetChanged();
                    }
                }
            });

        }
    };

    @Override
    public int getCount() {

        return _youtubeList.size();
    }

    @Override
    public Object getItem(int position) {
        return _youtubeList.elementAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
