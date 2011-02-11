package com.episode6.android.common.app.task;

import com.episode6.android.common.http.EzHttpRequest;
import com.episode6.android.common.http.EzHttpRequest.EzHttpResponse;

public abstract class EzHttpTask extends AbstractTask {
	
	protected EzHttpResponse mResponse;
	
	abstract public EzHttpRequest getRequest() throws Throwable;
	abstract public void processResponse(EzHttpResponse response) throws Throwable;

	@Override
	public void execute() throws Throwable {
		mResponse = getRequest().executeInSync();
		setTaskSuccesfull(mResponse.wasSuccess());
	}

	@Override
	public void postProcess() throws Throwable {
		processResponse(mResponse);
	}
	
	public EzHttpResponse getResponse() {
		return mResponse;
	}

}
