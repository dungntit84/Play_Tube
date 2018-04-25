package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.venustech.playtube.common.Utils;
import com.venustech.playtube.info.ChannelInfo;

import java.util.Vector;

public class ChannelAdapter extends BaseAdapter {
    public Vector<ChannelInfo> _channelList;

    public ChannelAdapter(Vector<ChannelInfo> channelList) {
        super();
        _channelList = channelList;
    }

    public void setDataSource(Vector<ChannelInfo> channelList) {
        _channelList = channelList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        LayoutInflater inflater = MainActivity.getInstance()
                .getLayoutInflater();
        TextView textView = null;

        View v;
        if (convertView == null) {
            v = inflater.inflate(R.layout.item_user, null);
            v.setTag(Constants.TAG_ID, R.layout.item_user);
        } else {
            v = convertView;
            if (v.getTag(Constants.TAG_ID) == null || !(v.getTag(Constants.TAG_ID) instanceof Integer) || (int) v.getTag(Constants.TAG_ID) != R.layout.item_user) {
                v = inflater.inflate(R.layout.item_user, null);
                v.setTag(Constants.TAG_ID, R.layout.item_user);
            }
        }

        textView = (TextView) v.findViewById(R.id.tv_title);
        textView.setText(channelInfo.title);

        ImageView iv = (ImageView) v.findViewById(R.id.image_view_thumb);
        ImageLoader.getInstance().displayImage(channelInfo.thumbUrl, iv);

        textView = (TextView) v.findViewById(R.id.text_view_video_count);
        textView.setText(channelInfo.getDisplayNumOfVideos());
        textView.setVisibility(channelInfo.numVideos > 0 ? View.VISIBLE
                : View.GONE);

        textView = (TextView) v.findViewById(R.id.text_view_num_subscribers);
        textView.setText(channelInfo.getDisplayNumOfSubscribers());

        return v;

    }

    @Override
    public int getCount() {

        return _channelList.size();
    }

    @Override
    public Object getItem(int position) {
        return _channelList.elementAt(position);
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
