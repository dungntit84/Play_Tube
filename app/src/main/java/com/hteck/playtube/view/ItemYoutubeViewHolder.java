package com.hteck.playtube.view;

import android.view.View;

import com.hteck.playtube.databinding.ItemYoutubeViewBinding;

public class ItemYoutubeViewHolder {

    public View view;
    public ItemYoutubeViewBinding binding;

    public ItemYoutubeViewHolder(ItemYoutubeViewBinding binding) {
        this.view = binding.getRoot();
        this.binding = binding;
    }
}
