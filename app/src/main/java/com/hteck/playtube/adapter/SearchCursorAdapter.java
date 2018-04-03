package com.hteck.playtube.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.fragment.SearchView;

public class SearchCursorAdapter extends CursorAdapter implements Filterable {
	SearchView _searchView;
	private Cursor _cursor;

	public SearchCursorAdapter(Context context, Cursor cursor,
                               SearchView searchView) {
		super(MainActivity.getInstance(), cursor);
		_searchView = searchView;
		_cursor = cursor;
	}

	public void setDataSource(Cursor cursor) {
		_cursor = cursor;
		changeCursor(cursor);
		notifyDataSetChanged();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.item_suggestion,
				parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textView = (TextView) view.findViewById(R.id.row_suggestion);
		textView.setText(cursor.getString(COLUMN_DISPLAY_NAME));
		TextView textViewAction = (TextView) view
				.findViewById(R.id.button_select);

		textViewAction.setTag(cursor.getString(COLUMN_DISPLAY_NAME));
		textViewAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String query = v.getTag().toString();
					_searchView.selectQuery(query);
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}
		});
	}

	private static final int COLUMN_DISPLAY_NAME = 1;

	@Override
	public int getCount() {
		return _cursor.getCount();
	}
}
