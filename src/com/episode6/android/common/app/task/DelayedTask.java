package com.episode6.android.common.app.task;

public class DelayedTask extends AbstractTask {
	
	private AbstractTask mTask;
	private long mDelayInMillis;
	
	public DelayedTask(AbstractTask task, long delayInMillis) {
		mTask = task;
		mDelayInMillis = delayInMillis;
	}

	@Override
	public void execute() throws Throwable {
		Thread.sleep(mDelayInMillis);
		mTask.execute();
	}

	@Override
	public void postProcess() throws Throwable {
		mTask.postProcess();
	}

	@Override
	public boolean wasTaskSuccesful() {
		return mTask.wasTaskSuccesful();
	}

	@Override
	public void setTaskSuccesfull(boolean success) {
		mTask.setTaskSuccesfull(success);
	}
	
	public AbstractTask getTask() {
		return mTask;
	}

	@Override
	public boolean isBlockerTask() {
		return mTask.isBlockerTask();
	}
	
	
	

}
