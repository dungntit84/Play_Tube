package com.hteck.playtube.holder;

import android.databinding.ViewDataBinding;
import android.view.View;

public class BaseViewHolder {

    public View view;
    public ViewDataBinding binding;

    public BaseViewHolder(ViewDataBinding binding) {
        this.view = binding.getRoot();
        this.binding = binding;
    }
}
