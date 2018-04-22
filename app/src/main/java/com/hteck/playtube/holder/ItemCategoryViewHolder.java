package com.hteck.playtube.holder;

import android.view.View;
import com.hteck.playtube.databinding.ItemCategoryBinding;

public class ItemCategoryViewHolder {

    public View view;
    public ItemCategoryBinding binding;

    public ItemCategoryViewHolder(ItemCategoryBinding binding) {
        this.view = binding.getRoot();
        this.binding = binding;
    }
}
