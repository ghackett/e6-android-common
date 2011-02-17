package com.episode6.android.common.util;

import java.util.Calendar;

public class DateUtils {
	
	public static boolean isDateToday(long timeInMillis) {
		Calendar d = Calendar.getInstance();
		d.setTimeInMillis(timeInMillis);
		Calendar today = Calendar.getInstance();
		
		return d.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) && d.get(Calendar.MONTH) == today.get(Calendar.MONTH) && d.get(Calendar.YEAR) == today.get(Calendar.YEAR);
		
	}
	
	public static boolean isDateThisYear(long timeInMillis) {
		Calendar d = Calendar.getInstance();
		d.setTimeInMillis(timeInMillis);
		Calendar today = Calendar.getInstance();
		
		return d.get(Calendar.YEAR) == today.get(Calendar.YEAR);		
	}

}
