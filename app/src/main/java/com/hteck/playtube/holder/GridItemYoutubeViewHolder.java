package com.hteck.playtube.holder;

import android.view.View;

import com.hteck.playtube.databinding.GridItemYoutubeViewBinding;

public class GridItemYoutubeViewHolder {

    public View view;
    public GridItemYoutubeViewBinding binding;

    public GridItemYoutubeViewHolder(GridItemYoutubeViewBinding binding) {
        this.view = binding.getRoot();
        this.binding = binding;
    }
}
