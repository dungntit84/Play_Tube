package com.hteck.playtube.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.CategoryInfo;

import java.util.Vector;

public class CategoryAdapter extends BaseAdapter {
	public Vector<CategoryInfo> _categoryList;

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
		View v = null;

		if (convertView == null) {
			LayoutInflater inflater = MainActivity.getInstance()
					.getLayoutInflater();
			v = inflater.inflate(R.layout.item_category, null);
		} else {
			v = convertView;
		}
		CategoryInfo categoryInfo = _categoryList.elementAt(position);
		TextView tv = (TextView) v.findViewById(R.id.item_category_text_view_title);
		tv.setText(categoryInfo.title);
		ImageView img = (ImageView) v.findViewById(R.id.item_category_image_view_check);
		img.setVisibility(categoryInfo.isSelected ? View.VISIBLE : View.GONE);
		return v;
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

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
