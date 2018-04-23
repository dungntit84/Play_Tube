package com.hteck.playtube.adapter;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.databinding.ItemSpinnerDefaultBinding;

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
        ItemSpinnerDefaultBinding binding = DataBindingUtil.inflate(flater, R.layout.item_spinner_default, parent, false);
        binding.text1.setText(rowItem);

        return binding.getRoot();
    }
    @Override
    public int getCount() {
        return _list.length;
    }
}
