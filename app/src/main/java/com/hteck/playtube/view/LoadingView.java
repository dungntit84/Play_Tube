package com.hteck.playtube.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.hteck.playtube.R;

public class LoadingView extends FrameLayout {

	public LoadingView(Context context, boolean... visibleTitle) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.loading_view, this);

		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		setClickable(true);
		
		if (visibleTitle.length > 0 && !visibleTitle[0]) {
			View tvTitle = findViewById(R.id.loading_tv_loading);
			tvTitle.setVisibility(View.GONE);
		}
	}
}
