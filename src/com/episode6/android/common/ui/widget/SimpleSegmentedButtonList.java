package com.episode6.android.common.ui.widget;

//com.episode6.android.common.ui.widget.SimpleSegmentedButtonList

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.episode6.android.common.R;
import com.episode6.android.common.util.PixelUtils;

public class SimpleSegmentedButtonList extends LinearLayout implements OnClickListener {
	
	public interface SimpleSegmentedButtonOnOnClickListener {
		public void onSegmentedButtonClick(View view, int index);
	}
	
	
	private ArrayList<SegmentedButtonView> mViewArray;
	private int mTwoDp;
	private CharSequence mTitle;
	

	public SimpleSegmentedButtonList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSimpleSegmentedButtonList();
	}

	public SimpleSegmentedButtonList(Context context) {
		super(context);
		initSimpleSegmentedButtonList();
	}
	
	private void initSimpleSegmentedButtonList() {
		mViewArray = new ArrayList<SegmentedButtonView>();
		mTwoDp = PixelUtils.convertToPx(getContext(), 2);
		mTitle = null;
		setOrientation(VERTICAL);
	}
	
	public void addButton(View v, SimpleSegmentedButtonOnOnClickListener listener) {
		mViewArray.add(new SegmentedButtonView(v, listener));
		postLayoutUpdate();
	}
	
	public void clearButtons() {
		mViewArray.clear();
		postLayoutUpdate();
	}
	
	public void setTitle(CharSequence title) {
		mTitle = title;
		postLayoutUpdate();
	}
	
	public void setTitle(int titleResId) {
		mTitle = getContext().getString(titleResId);
		postLayoutUpdate();
	}
	
	public void postLayoutUpdate() {
		getHandler().removeCallbacks(mLayoutUpdateRunnable);
		getHandler().post(mLayoutUpdateRunnable);
	}
	
	private void doLayoutUpdate() {
		Log.d("SimpleSegmentedButtonList", "DOING SIMPLE SEGMENT LAYOUT");
		removeAllViews();
		
		if (mViewArray.size() > 0) {
			
			if (mTitle != null) {
				TextView tv = new TextView(getContext());
				tv.setText(mTitle);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
				tv.setTextColor(Color.DKGRAY);
				tv.setTypeface(Typeface.DEFAULT_BOLD);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				lp.bottomMargin = PixelUtils.convertToPx(getContext(), 4);
				lp.leftMargin = PixelUtils.convertToPx(getContext(), 4);
				tv.setLayoutParams(lp);
				addView(tv);
			}
			
			for (int i = 0; i<mViewArray.size(); i++) {
				SegmentedButtonView sbv = mViewArray.get(i);
				
				LinearLayout newView = new LinearLayout(getContext());
				newView.addView(sbv.mView, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				
				LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				
				if (sbv.mOnClickListener != null) {
					newView.setTag(new Integer(i));
					newView.setOnClickListener(this);
					newView.setClickable(true);
					newView.setFocusable(true);
				}
				
				if (i == 0) {
					if (mViewArray.size() == 1) {
						//single
						newView.setBackgroundResource(R.drawable.simp_seg_bg_single);
					} else {
						//top
						newView.setBackgroundResource(R.drawable.simp_seg_bg_top);
					}
				} else if (i == mViewArray.size()-1) {
					//bottom
					newView.setBackgroundResource(R.drawable.simp_seg_bg_bottom);
					parentParams.topMargin = -mTwoDp;
				} else {
					//middle
					newView.setBackgroundResource(R.drawable.simp_seg_bg_middle);
					parentParams.topMargin = -mTwoDp;
				}
				
				addView(newView, parentParams);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag != null && tag instanceof Integer) {
			int index = (Integer)tag;
			SegmentedButtonView sbv = mViewArray.get(index);
			if (sbv.mOnClickListener != null) {
				sbv.mOnClickListener.onSegmentedButtonClick(sbv.mView, index);
			}
		}
	}

	
	private Runnable mLayoutUpdateRunnable = new Runnable() {
		
		@Override
		public void run() {
			doLayoutUpdate();
		}
	};
	
	public class SegmentedButtonView {
		private View mView;
		private SimpleSegmentedButtonOnOnClickListener mOnClickListener;
		
		public SegmentedButtonView(View view, SimpleSegmentedButtonOnOnClickListener onClickListener) {
			mView = view;
			mOnClickListener = onClickListener;
		}
	}


}
