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
	
}
