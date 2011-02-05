package com.episode6.android.common.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import com.episode6.android.common.util.concurrent.LinkedBlockingStack;

public class EzHttpThreadExecutor extends ThreadPoolExecutor {
	private static EzHttpThreadExecutor QUEUE_EXECUTOR = null;
	private static EzHttpThreadExecutor STACK_EXECUTOR = null;
	
	public static synchronized void initInstances() {
		queueInstance();
		stackInstance();
	}
	
	public static synchronized EzHttpThreadExecutor queueInstance() {
		if (QUEUE_EXECUTOR == null) {
			QUEUE_EXECUTOR = new EzHttpThreadExecutor(1, 2, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10, true));
		}
		return QUEUE_EXECUTOR;
	}
	
	public static synchronized EzHttpThreadExecutor stackInstance() {
		if (STACK_EXECUTOR == null) {
			STACK_EXECUTOR = new EzHttpThreadExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingStack<Runnable>());
		}
		return STACK_EXECUTOR;
	}
	
	public static void executeQueueRequest(EzHttpRequest request) {
		queueInstance().executeRequest(request);
	}
	public static void executeStackRequest(EzHttpRequest request) {
		stackInstance().executeRequest(request);
	}
	
	public static void shutdownInstances() {
		if (QUEUE_EXECUTOR != null) {
			try {
				queueInstance().shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (STACK_EXECUTOR != null) {
			try {
				stackInstance().shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private final Handler mHandler = new Handler();

	public EzHttpThreadExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}


	public void executeRequest(EzHttpRequest request) {
		execute(new EasyHttpExecutorRunnable(request));
	}
	
	private class EasyHttpExecutorRunnable implements Runnable {
		private EzHttpRequest mRequest;
//		private EzHttpRequest.EzHttpResponse mResponse;
		
		public EasyHttpExecutorRunnable(EzHttpRequest req) {
			mRequest = req;
//			mResponse = null;
		}

		@Override
		public void run() {
			mRequest.executeAndRespondInSync(mHandler);
			
//			try {
//				mResponse = mRequest.execute();
//			} catch (Throwable t) {
//				t.printStackTrace();
//				mResponse = mRequest.generateExceptionResponse(t);
//			}
//			
//			if (mRequest.getRequestFinishedListener() != null) {
//				if (mResponse.wasSuccess()) {
//					try {
//						mRequest.getRequestFinishedListener().onHttpRequestSucceededInBackground(mResponse);
//					} catch (Exception e) {
//						e.printStackTrace();
//						mResponse.mSuccess = false;
//						try {
//							mRequest.getRequestFinishedListener().onHttpRequestFailedInBackground(mResponse);
//						} catch (Exception e2) {
//							e2.printStackTrace();
//						}
//					}
//				} else {
//					try {
//						mRequest.getRequestFinishedListener().onHttpRequestFailedInBackground(mResponse);
//					} catch (Exception e2) {
//						e2.printStackTrace();
//					}
////					mRequest.getRequestFinishedListener().onHttpRequestFailedInBackground(mResponse);
//				}
//			
//			
//				mHandler.post(new Runnable() {
//					
//					@Override
//					public void run() {
//						if (mResponse.wasSuccess()) {
//							mRequest.getRequestFinishedListener().onHttpRequestSucceeded(mResponse);
//						} else {
//							mRequest.getRequestFinishedListener().onHttpRequestFailed(mResponse);
//						}
//					}
//				});
//			}
		}
		
	}
}
