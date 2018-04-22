package com.hteck.playtube.holder;

import android.view.View;
import com.hteck.playtube.databinding.ItemPopupPlaylistBinding;

public class ItemPopupPlaylistViewHolder {

    public View view;
    public ItemPopupPlaylistBinding binding;

    public ItemPopupPlaylistViewHolder(ItemPopupPlaylistBinding binding) {
        this.view = binding.getRoot();
        this.binding = binding;
    }
}
