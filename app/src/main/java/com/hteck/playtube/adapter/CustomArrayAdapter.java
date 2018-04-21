package com.hteck.playtube.adapter;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hteck.playtube.R;

public class CustomArrayAdapter extends ArrayAdapter<String> {
    LayoutInflater flater;
    private String[] _list;
    public CustomArrayAdapter(Activity context, String[] list){

        super(context, R.layout.item_spinner, list);
        flater = context.getLayoutInflater();
        _list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String rowItem = getItem(position);
        View rowview = flater.inflate(R.layout.item_spinner_default, null);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.text1);
        txtTitle.setText(rowItem);

        return rowview;
    }
    @Override
    public int getCount() {
        return _list.length;
    }
}
