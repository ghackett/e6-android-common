package com.episode6.android.common.app.task;

public abstract class AbstractTask {

	protected boolean mTaskSucceded = true;
	
	public boolean wasTaskSuccesful() {
		return mTaskSucceded;
	}
	
	public void setTaskSuccesfull(boolean success) {
		mTaskSucceded = success;
	}
	
	
	

	abstract public void execute() throws Throwable;
	abstract public void postProcess() throws Throwable;
	
	/**
	 * 
	 * @return true if this task should do it's postProcessing in the same background thread as the 
	 * execution method is run (bypassing the postProcessing queue completely)
	 */
	abstract public boolean isBlockerTask();
	
}
