package com.episode6.android.common.http.upload;

import com.episode6.android.common.http.EzHttpRequest;


public abstract class AbstractEzHttpPostUploadEntity implements EzHttpRequest.EzHttpPostUploadEntity {
	
	private String mParamName;
	private String mPostFilename;
	private String mContentType;
	
	public AbstractEzHttpPostUploadEntity(String paramName, String postFileName, String contentType) {
		mParamName = paramName;
		mPostFilename = postFileName;
		mContentType = contentType;
	}
	
	public AbstractEzHttpPostUploadEntity(String paramName, String contentType) {
		mParamName = paramName;
		mPostFilename = paramName;
		mContentType = contentType;
	}

	@Override
	public String getParamName() {
		return mParamName;
	}

	@Override
	public String getPostFileName() {
		return mPostFilename;
	}
	
	@Override
	public String getContentType() {
		return mContentType;
	}
	
	public void setParamName(String paramName) {
		mParamName = paramName;
	}
	
	public void setPostFilename(String postFilename) {
		mPostFilename = postFilename;
	}
	
	public void setContentType(String contentType) {
		mContentType = contentType;
	}

}
