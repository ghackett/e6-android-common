package com.episode6.android.common.http.upload;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class EzHttpUriPostUploadEntity extends AbstractEzHttpPostUploadEntity {
	
	private Uri mUri;
	private ContentResolver mContentResolver;

	public EzHttpUriPostUploadEntity(Context c, Uri uri, String paramName, String postFileName) {
		super(paramName, postFileName);
		mUri = uri;
		mContentResolver = c.getContentResolver();
	}

	@Override
	public InputStream getInputStream() {
		try {
			return mContentResolver.openInputStream(mUri);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public long getSize() {
		try {
			InputStream in = getInputStream();
			long avail = in.available();
			in.close();
			return avail;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
