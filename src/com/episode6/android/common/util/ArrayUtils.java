package com.episode6.android.common.util;

public class ArrayUtils {
	
	public static int findValueInArray(Object[] array, Object value) {
		for (int i = 0; i<array.length; i++) {
			if (array[i].equals(value))
				return i;
		}
		return -1;
	}

}
