package com.cicc.alarm;

import java.util.TimerTask;

public abstract class CustomTimerTask extends TimerTask {

	private int id;
	
	public CustomTimerTask(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

}
