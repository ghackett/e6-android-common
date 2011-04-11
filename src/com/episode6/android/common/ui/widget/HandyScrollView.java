package com.episode6.android.common.ui.widget;

//com.episode6.android.common.ui.widget.HandyScrollView

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class HandyScrollView extends ScrollView implements StoppableScrollView {
	
	public interface OnSizeChangedListener {
		public void onSizeChanged(HandyScrollView scrollView, int w, int h, int oldw, int oldh);
	}
	
	private int mFadingEdgeColor = -1;
	private OnSizeChangedListener mSizeListener = null;
	private boolean mPreventScrolling = false;

	public HandyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHandyScrollView();
	}

	public HandyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandyScrollView();
	}

	public HandyScrollView(Context context) {
		super(context);
		initHandyScrollView();
	}
	
	private void initHandyScrollView() {
		
	}
	
	public void setFadingEdgeColor(int color) {
		mFadingEdgeColor = color;
	}
	
	@Override
	public int getSolidColor() {
		if (mFadingEdgeColor == -1)
			return super.getSolidColor();
		return mFadingEdgeColor;
	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mSizeListener = listener;
	}
	
	public void stopScrolling() {
		mPreventScrolling = true;
	}
	
	public void allowScrolling() {
		mPreventScrolling = false;
	}
	
	public boolean isScrollingAllowed() {
		return !mPreventScrolling;
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mSizeListener != null)
			mSizeListener.onSizeChanged(this, w, h, oldw, oldh);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mPreventScrolling)
			return false;
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mPreventScrolling)
			return false;
		return super.onTouchEvent(ev);
	}
	
	

}
