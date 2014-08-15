package com.cicc.alarm;

import java.util.TimerTask;

public class CustomTimerTask extends TimerTask {

	private int id;
	
	public CustomTimerTask(int id) {
		this.id = id;
	}
	
	@Override
	public void run() {
		
	}
	
	public int getId() {
		return id;
	}

}
