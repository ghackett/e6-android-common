package com.episode6.android.common.service;

import java.util.concurrent.LinkedBlockingQueue;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

/*
 * The main difference between this and a normal IntentService is that you must explicitly call finishCurrentIntent
 * for the next intent in the queue to start getting processed (if you return true from onHandleIntent). 
 * This makes it easier to spawn sepereate threads and not let the service be stopped before the sepereate 
 * threads are completed.
 * 
 * 
 * Everything in onHandleIntent is still run in outside of the UI thread. You can do whatever syncronous ops you want in
 * it and return false to make it act like a normal IntentService. However if you return true, it is then your responsability
 * to call finishCurrentIntent() when you're done processing the intent, so that the next one can start up.
 */
public abstract class AdvancedIntentService extends Service {
	
	protected PowerManager mPowerManager;
	protected PowerManager.WakeLock mWakeLock;
	private LinkedBlockingQueue<Intent> mIntentQueue = new LinkedBlockingQueue<Intent>();
	private boolean mIsProcessingIntents;
	
	
	
	@Override
	public void onCreate() {
		if (shouldWakeOnCreate()) {
			mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
			mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getTag());
			mWakeLock.acquire();
		}
		mIsProcessingIntents = false;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (mWakeLock != null)
			mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		addNewIntent(intent);
		return START_NOT_STICKY;
	}

	private void addNewIntent(Intent i) {
		mIntentQueue.add(i);
		if (!mIsProcessingIntents) {
			mIsProcessingIntents = true;
			executeTopIntentOrStop();
		}
	}
	
	private void executeTopIntentOrStop() {
		if (mIntentQueue.size() > 0) {
			IntentProcessorThread t = new IntentProcessorThread(mIntentQueue.peek());
			t.start();
		} else {
			stopSelf();
		}
	}
	
	protected void finishCurrentIntent() {
		mIntentQueue.remove();
		executeTopIntentOrStop();
	}
	
	public abstract boolean shouldWakeOnCreate();
	public abstract String getTag();
	
	/**
	 * 
	 * @param intent
	 * @return true if onHandleIntent is going to call finishCurrentThread on its own (ie the thread is still running)
	 * or return false if processing is done
	 */
	public abstract boolean onHandleIntent(Intent intent);

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	
	private class IntentProcessorThread extends Thread {
		private Intent mIntent;
		public IntentProcessorThread(Intent i) {
			mIntent = i;
		}
		
		@Override
		public void run() {
			boolean dontExecuteFinished = false;
			try {
				dontExecuteFinished = onHandleIntent(mIntent);
			} catch (Exception e) {
				e.printStackTrace();
				dontExecuteFinished = false;
			}
			
			if (!dontExecuteFinished)
				finishCurrentIntent();
		}
	}
	
}
