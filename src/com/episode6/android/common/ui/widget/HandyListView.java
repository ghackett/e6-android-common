package com.episode6.android.common.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HandyListView extends ListView {
	
	private View mEmptyListView = null;
	private View mLoadingListView = null;

	public HandyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHandyListView();
	}

	public HandyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandyListView();
	}

	public HandyListView(Context context) {
		super(context);
		initHandyListView();
	}
	
	private void initHandyListView() {
		
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (mLoadingListView != null)
			mLoadingListView.setVisibility(View.GONE);
		setVisibility(View.VISIBLE);
		setEmptyView(mEmptyListView);
		mEmptyListView = null;
		
	}

	@Override
	public View getEmptyView() {
		if (mEmptyListView != null)
			return mEmptyListView;
		return super.getEmptyView();
	}

	@Override
	public void setEmptyView(View emptyView) {
		mEmptyListView = emptyView;
		if (mEmptyListView != null)
			mEmptyListView.setVisibility(View.GONE);
	}
	
	public void setLoadingListView(View loadingView) {
		mLoadingListView = loadingView;
		if (mLoadingListView != null) {
			setVisibility(View.GONE);
			mLoadingListView.setVisibility(View.VISIBLE);
		}
	}

	
}
