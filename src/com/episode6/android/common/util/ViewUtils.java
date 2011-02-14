package com.episode6.android.common.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class ViewUtils {
	
	public static void makeTextViewFakeLink(TextView tv, final View.OnClickListener onClickListener) {
		makeTextViewFakeLink(tv, onClickListener, android.R.drawable.list_selector_background);
	}
	
	public static void makeTextViewFakeLink(TextView tv, final View.OnClickListener onClickListener, int selectorBgResId) {
		String text = tv.getText().toString();
		SpannableString spanString = new SpannableString(text);
		ClickableSpan clickSpan = new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				onClickListener.onClick(widget);
			}
		};
		spanString.setSpan(clickSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		tv.setLinksClickable(true);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setBackgroundResource(selectorBgResId);
		tv.setText(spanString);
	}

}
