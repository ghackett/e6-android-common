package com.episode6.android.common.app.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.episode6.android.common.app.task.AbstractTask;

public abstract class TaskExecutorService extends Service {
	private static final String TAG = "TaskExecutorService";
	
	private static boolean mIsRunning = false;
	public static boolean isRunning() {
		return mIsRunning;
	}
	
	private RejectedExecutionHandler mRejectionHandler = new RejectedExecutionHandler() {
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			if (r instanceof TaskExecutionThread) {
				AbstractTask task = ((TaskExecutionThread)r).getTask();
				task.setTaskSuccesfull(false);
				mPostProcessor.execute(new TaskPostProcessorThread(task));
			} else if (r instanceof TaskPostProcessorThread) {
				AbstractTask task = ((TaskPostProcessorThread)r).getTask();
				task.setTaskSuccesfull(false);
				finishTask(task);

			}
		}
	};
	
	private PowerManager.WakeLock mWakeLock = null;
	private final Handler mHandler = new Handler();
	private TaskPoolExecutor mDefaultExecutor = null;
	private ThreadPoolExecutor mPostProcessor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), mRejectionHandler);
	private LinkedBlockingQueue<AbstractTask> mRequestQueue = new LinkedBlockingQueue<AbstractTask>();
	
	
	abstract public boolean shouldHoldWakeLock();
	abstract public AbstractTask getTask(Intent intent);
	abstract public TaskPoolExecutor getExecutor(AbstractTask task);
	

	@Override
	public void onCreate() {
		mIsRunning = true;
		if (shouldHoldWakeLock()) {
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
		super.onCreate();
	}




	@Override
	public void onDestroy() {
		mIsRunning = false;
		if (mWakeLock != null) {
			mWakeLock.release();
		}
		super.onDestroy();
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		addTask(getTask(intent));
		
		return START_NOT_STICKY;
	}

	




	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void addTask(AbstractTask task) {
		if (task == null)
			return;
		TaskPoolExecutor exe = getExecutor(task);
		if (exe == null)
			exe = getDefaultExecutor();
		
		
		mRequestQueue.add(task);
		exe.executeTask(task);
	}
	
	
	public void finishTask(AbstractTask task) {
		try {
			mRequestQueue.remove(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
		safeStop();
	}
	
	public void safeStop() {
		if (mRequestQueue.size() == 0) {
			stopSelf();
		}
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public TaskPoolExecutor getDefaultExecutor() {
		if (mDefaultExecutor == null) {
			mDefaultExecutor = new TaskPoolExecutor(2, 3, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10, true));
		}
		return mDefaultExecutor;
	}
	
	
	
	
	
	
	
	
	
	

	public class TaskPoolExecutor extends ThreadPoolExecutor {

		public TaskPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, mRejectionHandler);
		}

		public TaskPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
					threadFactory, mRejectionHandler);
		}
		
		public void executeTask(AbstractTask task) {
			execute(new TaskExecutionThread(task));
		}
	}
	
 
	
	private class TaskExecutionThread implements Runnable {		
		private AbstractTask mTask;
		
		public TaskExecutionThread(AbstractTask task) {
			mTask = task;
		}

		@Override
		public void run() {
			try {
				mTask.execute();
			} catch (Throwable t) {
				t.printStackTrace();
				mTask.setTaskSuccesfull(false);
			}
			
			
			mPostProcessor.execute(new TaskPostProcessorThread(mTask));
		}
		
		public AbstractTask getTask() {
			return mTask;
		}
		
	}
	
	private class TaskPostProcessorThread implements Runnable {
		private AbstractTask mTask;
		
		public TaskPostProcessorThread(AbstractTask task) {
			mTask = task;
		}

		@Override
		public void run() {
			try {
				mTask.postProcess();
			} catch (Throwable t) {
				t.printStackTrace();
				mTask.setTaskSuccesfull(false);
			}
			
			
			
			finishTask(mTask);
		}
		
		public AbstractTask getTask() {
			return mTask;
		}
	}
	

	
}
