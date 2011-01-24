package com.episode6.android.common.http.upload;

import com.episode6.android.common.http.EzHttpRequest;


public abstract class AbstractEzHttpPostUploadEntity implements EzHttpRequest.EzHttpPostUploadEntity {
	
	private String mParamName;
	private String mPostFilename;
	
	public AbstractEzHttpPostUploadEntity(String paramName, String postFileName) {
		mParamName = paramName;
		mPostFilename = postFileName;
	}
	
	public AbstractEzHttpPostUploadEntity(String paramName) {
		mParamName = paramName;
		mPostFilename = paramName;
	}

	@Override
	public String getParamName() {
		return mParamName;
	}

	@Override
	public String getPostFileName() {
		return mPostFilename;
	}
	
	public void setParamName(String paramName) {
		mParamName = paramName;
	}
	
	public void setPostFilename(String postFilename) {
		mPostFilename = postFilename;
	}

}
