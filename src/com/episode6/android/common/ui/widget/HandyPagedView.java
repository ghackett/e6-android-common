package com.episode6.android.common.ui.widget;

//com.episode6.android.common.ui.widget.HandyPagedView

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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
	private float mLastMotionX;
	private VelocityTracker mVelocityTracker;
	
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
		mVelocityTracker = null;
		
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
			scrollTo(pageWidth, true);
		}
	}
	
	private void pageChanged() {
		if (mScrollX == 0) {
			//go one back
			mCurrentPage--;
		} else if (mScrollX == getWidth()) {
			//same page
		} else if (mScrollX == getWidth()*2) {
			//next page
			mCurrentPage++;
		}
		updatePageLayout();
	}
	
	public void setParentScrollView(StoppableScrollView parentScrollView) {
		mParentScrollview = parentScrollView;
	}
	
	public void scrollTo(int scrollX, boolean invalidate) {
		if (scrollX < 0) {
			scrollTo(0, invalidate);
			return;
		}
		if (scrollX > getWidth()*2) {
			scrollTo(getWidth()*2, invalidate);
			return;
		}
		if (scrollX < getWidth() && !canScrollBack()) {
			scrollTo(getWidth(), invalidate);
			return;
		}
		if (scrollX > getWidth() && !canScrollForward()) {
			scrollTo(getWidth(), invalidate);
			return;
		}
		mIsBeingScrolled = true;
		mScrollX = scrollX;
		if (invalidate)
			invalidate();
	}
	
	public void scrollBy(int dx, boolean invalidate) {
		scrollTo(mScrollX+dx, invalidate);
	}
	
	public void smoothScrollTo(int x) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.startScroll(mScrollX, 0, x - mScrollX, 0);
		invalidate();
	}
	
	public void fling(int initVelocity) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.fling(mScrollX, 0, initVelocity, 0, -getWidth(), getWidth()*3, 0, 0);
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
	
	public ListAdapter getAdapter() {
		return mAdapter;
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
	
	private void setParentScrollingAllowed(boolean allowed) {
		if (mParentScrollview != null) {
			if (allowed)
				mParentScrollview.allowScrolling();
			else 
				mParentScrollview.stopScrolling();
		}
	}
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
			return true;
		}
		
		switch(action & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_MOVE: {
			final float x = ev.getX();
			final int dx = (int)Math.abs(x - mLastMotionX);
			if (dx > mTouchSlop) {
				mIsBeingDragged = true;
				mLastMotionX = x;
				setParentScrollingAllowed(false);
			}
			break;
		}
		
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			mLastMotionX = x;
			mIsBeingDragged = !mScroller.isFinished();
			break;
		}
		
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mIsBeingDragged = false;
			setParentScrollingAllowed(true);
			break;
		}
		
		return mIsBeingDragged;

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		
		final int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN: {
	            final float x = event.getX();
	            mIsBeingDragged = true;
	            if (!mScroller.isFinished()) {
	                mScroller.abortAnimation();
	            }
	            mLastMotionX = x;
	            break;
	        }
	        
	        case MotionEvent.ACTION_MOVE: {
	        	final float x = event.getX();
	        	final int deltaX = (int) (mLastMotionX - x);
	        	mLastMotionX = x;
	        	scrollBy(deltaX, true);
	        	break;
	        }
	        
	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_UP: {
	        	final VelocityTracker velocityTracker = mVelocityTracker;
	            velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
	            
	            int initialVelocity = (int) velocityTracker.getXVelocity();
	            
	            mIsBeingDragged = false;
	            if (Math.abs(initialVelocity) > mMinVelocity) {
	            	fling(-initialVelocity);
	            } else {
	            	finishScroll();
	            }
	            
	            
	            if (mVelocityTracker != null) {
	            	mVelocityTracker.recycle();
	            	mVelocityTracker = null;
	            }
	        	
	            setParentScrollingAllowed(true);
	            
	        	break;
	        }
		}
		return true;

	}
	
	private void finishScroll() {
		if (mIsBeingScrolled && !mIsBeingDragged) {
			mIsBeingScrolled = false;
			
			if (mScrollX % getWidth() == 0) {
				pageChanged();
				return;
			}
			
			if (mScrollX < getWidth()/2) {
				smoothScrollTo(0);
			} else if (mScrollX < getWidth()*1.5) {
				smoothScrollTo(getWidth());
			} else {
				smoothScrollTo(getWidth()*2);
			}
		}
	}

	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			boolean finishScroll = false;
			int curX = mScroller.getCurrX();
			if (curX > getWidth()*2) {
				curX = getWidth()*2;
				finishScroll = true;
			}
			if (curX < 0) {
				curX = 0;
				finishScroll = true;
			}
			
			scrollTo(curX, false);
			postInvalidate();
			
			if (finishScroll) {
				mScroller.abortAnimation();
				finishScroll();
			}
		} else if (mIsBeingScrolled) {
			finishScroll();
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
				if (info != null && container.getChildAt(0) == info.view) {
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
