package com.cicc.alarm;

import java.io.File;

public class Alarm implements Cloneable {

	private String toneFileName, alarmName;
	private int [] alarmTime;
	private int mode;
	private final int alarmID;
	private CustomTimerTask task;
	private File alarmFile;
	private int [] repeat;
	
	public static final int ALM_MODE_SILENT = 0;
	public static final int ALM_MODE_CONT = 1;
	public static final int ALM_MODE_ONCE = 2;
	public static final int ALM_MODE_INFO = 3;
	public static final int ALM_MODE_MORNING = 4;
	
	public Alarm(String alarmName, int [] alarmTime, String toneFileName, int alarmID, CustomTimerTask task, File file, int [] repeat, int mode) {
		this.alarmName = alarmName;
		if(alarmTime.length != 2)
			throw new ArrayIndexOutOfBoundsException("alarmTime parameter must have an index size of 2");
		this.alarmTime = alarmTime;
		this.toneFileName = toneFileName;
		this.alarmID = alarmID;
		this.task = task;
		this.repeat = repeat;
		alarmFile = file;
		this.mode = mode;
	}
	
	public String getAlarmName() {
		return alarmName;
	}
	
	public int [] getRepeat() {
		return repeat;
	}
	
	public String getToneFileName() {
		return toneFileName;
	}
		
	public int [] getAlarmTime() {
		return alarmTime;
	}
	
	public int getAlarmID() {
		return alarmID;
	}
	
	public File getAlarmFile() {
		return alarmFile;
	}
	
	public CustomTimerTask getTimerTask() {
		return task;
	}
	
	public int getMode() {
		return mode;
	}
	
	public void setAlarmName(String name) {
		this.alarmName = name;
	}
	
	public void setAlarmTime(int [] time) {
		if(alarmTime.length != 2)
			throw new ArrayIndexOutOfBoundsException("alarmTime parameter must have an index size of 2");
		alarmTime = time;
	}
	
	public void setToneFileName(String name) {
		toneFileName = name;
	}
	
	public void setTimerTask(CustomTimerTask task) {
		this.task = task;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public void setRepeat(int [] repeat) {
		this.repeat = repeat;
	}
	
	@Override
	public Object clone() {
		return new Alarm(alarmName, alarmTime, toneFileName, alarmID, task, alarmFile, repeat, mode);
	}
	
}
