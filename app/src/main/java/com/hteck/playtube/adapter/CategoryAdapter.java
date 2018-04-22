package com.hteck.playtube.adapter;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.CategoryInfo;
import com.hteck.playtube.databinding.ItemCategoryBinding;
import com.hteck.playtube.holder.ItemCategoryViewHolder;

import java.util.Vector;

public class CategoryAdapter extends BaseAdapter {
    private Vector<CategoryInfo> _categoryList;

    public CategoryAdapter(Vector<CategoryInfo> categoryList) {
        super();
        _categoryList = categoryList;
    }

    public void setDataSource(Vector<CategoryInfo> categoryList) {
        _categoryList = categoryList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup group) {
        ItemCategoryViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = MainActivity.getInstance()
                    .getLayoutInflater();
            ItemCategoryBinding itemBinding = DataBindingUtil.inflate(inflater, R.layout.item_category, group, false);
            holder = new ItemCategoryViewHolder(itemBinding);
            holder.view = itemBinding.getRoot();
            holder.view.setTag(holder);
        } else {
            holder = (ItemCategoryViewHolder) convertView.getTag();
        }

        CategoryInfo categoryInfo = _categoryList.elementAt(position);
        holder.binding.itemCategoryTextViewTitle.setText(categoryInfo.title);
        holder.binding.itemCategoryImageViewCheck.setVisibility(categoryInfo.isSelected ? View.VISIBLE : View.GONE);
        return holder.view;
    }

    @Override
    public int getCount() {

        return _categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return _categoryList.elementAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
