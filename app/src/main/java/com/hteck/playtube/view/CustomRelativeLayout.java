package com.hteck.playtube.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CustomRelativeLayout extends FrameLayout {
	public interface ISizeChangedListener {
		void onLayoutChanged(int xNew, int yNew, int xOld, int yOld);
	}
	private ISizeChangedListener _sizeChangedListener;

	public CustomRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		if (_sizeChangedListener != null) {
			_sizeChangedListener.onLayoutChanged(xNew, yNew, xOld, yOld);
		}
	}

	public void setSizeChangedListener(ISizeChangedListener sizeChangedListener) {
		_sizeChangedListener = sizeChangedListener;
	}
}


