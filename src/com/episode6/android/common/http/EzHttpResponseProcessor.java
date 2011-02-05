package com.episode6.android.common.http;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import com.episode6.android.common.http.EzHttpRequest.EzHttpResponse;

public class EzHttpResponseProcessor extends ThreadPoolExecutor {
	
	private final Handler mHandler = new Handler();

	public EzHttpResponseProcessor() {
		super(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public void processResponse(EzHttpResponse response) {
		execute(new ResponseProcessorThread(response));
	}
	
	
	private class ResponseProcessorThread implements Runnable {
		private EzHttpResponse mResponse;
		public ResponseProcessorThread(EzHttpResponse response) {
			mResponse = response;
		}
		@Override
		public void run() {
			mResponse.process(mHandler);
		}
	}

}
