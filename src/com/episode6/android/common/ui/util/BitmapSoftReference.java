package com.episode6.android.common.ui.util;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;

public class BitmapSoftReference extends SoftReference<Bitmap> {
	@SuppressWarnings("unused")
	private static final String TAG = "BitmapReference";

	public BitmapSoftReference(Bitmap referent) {
		super(referent);
	}

	@Override
	public void clear() {
		
		Bitmap b = get();
		if (b != null) {
			b.recycle();
		}
		super.clear();
	}

//	@Override
//	protected void finalize() throws Throwable {
////		clear();
//		super.finalize();
//	}  
//	
	

}