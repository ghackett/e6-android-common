package com.episode6.android.common.ui.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HandyPagedView extends FrameLayout {
	
	private StoppableScrollView mParentScrollview;
	private LinearLayout mInnerView;
	private ArrayList<FrameLayout> mContainerViews;
	private ArrayList<ArrayList<View>> mRecycledViews;
	private AdapterViewInfo[] mVisibleViews;
	private ListAdapter mAdapter;
	private int mCurrentPage;
	private Scroller mScroller;
	private int mTouchSlop;
	private int mMinVelocity;
	private int mMaxVelocity;
	private boolean mIsBeingDragged;
	private boolean mIsBeingScrolled;
	
	private int mScrollX;

	public HandyPagedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPagedView();
	}

	public HandyPagedView(Context context) {
		super(context);
		initPagedView();
	}

	private void initPagedView() {
		mParentScrollview = null;
		mContainerViews = new ArrayList<FrameLayout>(3);
		mVisibleViews = new AdapterViewInfo[3];
		mRecycledViews = null;
		mAdapter = null;
		mCurrentPage = 0;
		mScrollX = 0;
		mIsBeingDragged = false;
		mIsBeingScrolled = false;
		
		mInnerView = new LinearLayout(getContext());
		mInnerView.setOrientation(LinearLayout.HORIZONTAL);
		
		for (int i =0; i<3; i++) {
			mContainerViews.add(new FrameLayout(getContext()));
			mInnerView.addView(mContainerViews.get(i));
		}
		
		addView(mInnerView);
		
		mScroller = new Scroller(getContext());
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
		
	}
	
	private void updatePageLayout() {
		removeAllViews();
		if (mAdapter != null && getWidth() > 0) {
			int pageWidth = getWidth();
			int pageHeight = getHeight();
			mInnerView.setLayoutParams(new FrameLayout.LayoutParams(pageWidth*3, pageHeight));
			for (int i = 0; i<3; i++) {
				mContainerViews.get(i).setLayoutParams(new LinearLayout.LayoutParams(pageWidth, pageHeight));
			}
			
			int startIndex = 0;
			int startPosition = 0;
			
			if (mCurrentPage == 0) {
				startIndex = 1;
			} else {
				startPosition = mCurrentPage - 1;
			}
			
			int position = startPosition;
			for (int i = startIndex; i<3; i++) {
				
				if (position >= mAdapter.getCount())
					break;
				
				AdapterViewInfo info = getAdapterView(position);
				info.view.setLayoutParams(new FrameLayout.LayoutParams(pageWidth, pageHeight));
				mContainerViews.get(i).addView(info.view);
				mVisibleViews[i] = info;
				
				position++;
			}
		}
	}
	
	public void setParentScrollView(StoppableScrollView parentScrollView) {
		mParentScrollview = parentScrollView;
	}
	
	public void scrollTo(int scrollX, boolean invalidate) {
		mScrollX = scrollX;
		if (invalidate)
			invalidate();
	}
	
	public void setAdapter(ListAdapter adapter) {
		mAdapter = null;
		removeAllViews();
		mAdapter = adapter;
		if (mAdapter != null) {
			mRecycledViews = new ArrayList<ArrayList<View>>();
			for (int i = 0; i<mAdapter.getViewTypeCount(); i++) {
				mRecycledViews.add(new ArrayList<View>());
			}
		}
		updatePageLayout();
	}
	
	
	

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					updatePageLayout();
				}
			});
		}
	}

	public boolean canScrollBack() {
		return mAdapter != null && mCurrentPage > 0;
	}
	
	public boolean canScrollForward() {
		return mAdapter != null && mCurrentPage < mAdapter.getCount()-1;
	}
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mIsBeingScrolled = true;
		} else if (mIsBeingScrolled) {
			mIsBeingScrolled = false;
			
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.translate(-mScrollX, 0);
	}
	
	
	@Override
	public void removeAllViews() {
		for (int i = 0; i<3; i++) {
			FrameLayout container = mContainerViews.get(i);
			AdapterViewInfo info = mVisibleViews[i];
			if (mAdapter == null) {
				container.removeAllViews();
			} else {
				if (container.getChildAt(0) == info.view) {
					container.removeAllViews();
					mRecycledViews.get(info.type).add(info.view);
				} else {
					container.removeAllViews();
				}
			}
			mVisibleViews[i] = null;
		}
//		super.removeAllViews();
	}

	
	private AdapterViewInfo getAdapterView(int position) {
		if (mAdapter == null)
			return null;
		int viewType = mAdapter.getItemViewType(position);
		ArrayList<View> recycleArray = mRecycledViews.get(viewType);
		View v = null;
		if (recycleArray.size() > 0) {
			v = recycleArray.get(0);
			recycleArray.remove(0);
		}
		v = mAdapter.getView(position, v, this);
		AdapterViewInfo info = new AdapterViewInfo(viewType, v);
		return info;
	}


	private class AdapterViewInfo {
		public int type;
		public View view;
		public AdapterViewInfo(int type, View v) {
			this.type = type;
			this.view = v;
		}
	}
	
	
}
