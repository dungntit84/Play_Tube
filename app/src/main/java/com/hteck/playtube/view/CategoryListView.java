package com.hteck.playtube.view;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.adapter.CategoryAdapter;
import com.hteck.playtube.data.CategoryInfo;
import com.hteck.playtube.service.CategoryService;

import java.util.Vector;

public class CategoryListView extends FrameLayout implements
		AdapterView.OnItemClickListener {

	Vector<CategoryInfo> _itemList;
	CategoryAdapter _adapter;
	private int _categoryIndex;

	public CategoryListView(Context context, int genreIndex) {
		super(context);
		_categoryIndex = genreIndex;
		ListView listView = new ListView(MainActivity.getInstance());
		_itemList = CategoryService.getGenreListInfo().getValue();
		for (int i = 0; i < _itemList.size(); ++i) {
			_itemList.elementAt(i).isSelected = i == genreIndex;
		}
		_adapter = new CategoryAdapter(_itemList);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setAdapter(_adapter);
		listView.setSelection(genreIndex);
		listView.setOnItemClickListener(this);
		addView(listView);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		_categoryIndex = position;

		for (int i = 0; i < _itemList.size(); ++i) {
			_itemList.elementAt(i).isSelected = i == _categoryIndex;
		}
		_adapter.setDataSource(_itemList);
	}
	
	public int getSelectedIndex() {
		return _categoryIndex;
	}
}
