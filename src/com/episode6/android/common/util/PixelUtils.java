package com.episode6.android.common.util;

import android.content.Context;

public class PixelUtils {
	
	public static int convertToPx(Context c, int dp) {
		float pixelDensity = c.getResources().getDisplayMetrics().density;
		return (int) (dp * pixelDensity + 0.5f);
	}
	
}
