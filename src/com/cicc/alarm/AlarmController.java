package com.cicc.alarm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cicc.tts.Speak;
import com.cicc.tts.Utils;
import com.cicc.voiceCont.AudioPlayer;
import com.cicc.weather.Weather;

public abstract class AlarmController {

	public static void main(String args[]) {
		AlarmController alm = new AlarmController(true) {
			
			@Override
			public ArrayList<String> speechRec() {
				ArrayList<String> arr = new ArrayList<String>();
				arr.add("1");
				return arr;
			}
			
			@Override
			public void sayTime(boolean schoolDay) {
				System.out.println("Time");
			}
		};
		alm.alarmInfo();
	}

	public static final String ALARM_FILES_DIRECTORY = "alarms";
	public static final int ALARM_FILE_LENGTH = 5;
	public static final int TOD_AM = 0;
	public static final int TOD_PM = 1;

	// Alarm file format
	// FileName: ID.alm
	// FileContents:
	// AlmName:name
	// AlmTime:HH-MM
	// Repeat:1-2-3-4-5-6-7
	// AlmMode:int

	private final boolean speechEnabled;
	private ArrayList<Alarm> alarms;
	private Timer timer;

	public AlarmController(boolean speechEnabled) {
		this.speechEnabled = speechEnabled;
		alarms = new ArrayList<Alarm>();
		timer = new Timer();
		checkAlarmFiles();
	}

	public void startInteractiveSpeech() {
		if (!speechEnabled)
			return;
		boolean done = false;
		while (!done) {
			Speak.say("Please Say a Command to edit alarms");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null))
				done = true;
			else
				for (String s : saidArr) {
					if (Utils.includes(s, "new") || Utils.includes(s, "create")) {
						newAlarm();
						break;
					} else if (Utils.includes(s, "delete") || Utils.includes(s, "remove")) {
						deleteAlarm();
						break;
					} else if (Utils.includes(s, "edit")) {
						editAlarm();
						break;
					} else if (Utils.includes(s, "list")) {
						listAlarms();
						break;
					} else if (Utils.includes(s, "info")) {
						alarmInfo();
						break;
					} else if (Utils.includes(s, "exit") || Utils.includes(s, "cancel") || saidArr == null || saidArr.size() == 0) {
						done = true;
						break;
					}
				}
		}
		Speak.say("Exiting alarm configuration mode.");
	}

	private void checkAlarmFiles() {
		File fileDirectory = new File(ALARM_FILES_DIRECTORY);
		if (!fileDirectory.exists())
			return;
		File[] alarmFiles = fileDirectory.listFiles();
		for (File f : alarmFiles) {
			String fileName = f.getName();
			fileName = fileName.replaceAll(".alm", "");
			try {
				int almID = Integer.parseInt(fileName);
				if (!idInUse(Integer.parseInt(fileName))) {
					ArrayList<String> fileContents = Utils.readFileToArray(f);
					try {
						if (fileContents.size() != ALARM_FILE_LENGTH)
							throw new Exception("File not formatted correctly");
						else {
							String almName = fileContents.get(0).replaceFirst("AlmName:", "");
							String almTimeStr = fileContents.get(1).replaceFirst("AlmTime:", "");
							String almRepeatStr = fileContents.get(2).replaceFirst("Repeat:", "");
							String almSound = fileContents.get(3).replaceFirst("AlmSoundFile:", "");
							String almModeStr = fileContents.get(4).replaceFirst("AlmMode:", "");
							String[] almTimeArrStr = almTimeStr.split("-");
							if (almTimeArrStr.length != 2)
								throw new Exception("File not formatted correctly");
							int[] almTimeArr = new int[2];
							almTimeArr[0] = Integer.parseInt(almTimeArrStr[0]);
							almTimeArr[1] = Integer.parseInt(almTimeArrStr[1]);
							int[] almRepeatArr = new int[0];
							if (almRepeatStr.length() != 0) {
								String[] almRepeatArrStr = almRepeatStr.split("-");
								if (almRepeatArrStr.length > 7)
									throw new Exception("File not formatted correctly");
								almRepeatArr = new int[almRepeatArrStr.length];
								for (int i = 0; i < almRepeatArr.length; i++)
									almRepeatArr[i] = Integer.parseInt(almRepeatArrStr[i]);
							}
							int almMode = Integer.parseInt(almModeStr);
							Alarm alarm = new Alarm(almName, almTimeArr, almSound, almID, null, f, almRepeatArr, almMode);
							alarms.add(alarm);
							scheduelAlarm(alarm);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("Improperly formatted file found in alarms directory.");
						System.err.println("The file \"" + f.getName() + "\" has been deleted");
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("Improperly formatted file found in alarms directory.");
				System.err.println("The file \"" + f.getName() + "\" has been deleted");
			}
		}
	}

	private void alarmInfo() {
		Alarm alarm = requestAlarm();
		if (alarm == null)
			return;
		ArrayList<String> toSay = new ArrayList<String>();
		toSay.add("The alarm is named " + alarm.getAlarmName() + " with an I.D. of " + alarm.getAlarmID() + ".");
		int[] almTimeArr = alarm.getAlarmTime();
		int hour = almTimeArr[0] > 12 ? almTimeArr[0] - 12 : almTimeArr[0];
		int min = almTimeArr[1];
		String TOD = almTimeArr[0] >= 12 ? "p.m." : "a.m.";
		toSay.add("The alarm is set for " + hour + ":" + min + " " + TOD);
		int[] repeatDays = alarm.getRepeat();
		if (repeatDays.length != 0) {
			String days = "";
			for (int i : repeatDays)
				days += (dayOfWeekToText(i) + ", ");
			toSay.add("repeating every " + days);
		} else
			toSay.add("repeating never");
		Speak.say(toSay);
	}

	private void editAlarm() {

		Alarm alarm = requestAlarm();
		if (alarm == null)
			return;

		Speak.say("Alarm " + alarm.getAlarmID() + ", " + alarm.getAlarmName() + " selected");
		boolean editing = true;
		while (editing) {
			boolean valid = false;
			int timesAsked = 0;
			while (!valid) {
				if (timesAsked >= 3) {
					ArrayList<String> toSay = new ArrayList<String>();
					toSay.add("I'm sorry, I can't seem to understand you.");
					toSay.add("Please reposition the microphone and try again.");
					Speak.say(toSay);
					return;
				}
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("Please select an option to edit the alarm. The options are:");
				toSay.add("Rename,");
				toSay.add("Change Time,");
				toSay.add("Change Repeat,");
				toSay.add("and Change Mode");
				Speak.say(toSay);
				ArrayList<String> saidArr = speechRec();
				if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
					timesAsked++;
					if (timesAsked < 3)
						Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
				} else {
					for (String str : saidArr) {
						if (str.toLowerCase().contains("cancel")) {
							Speak.say("Cancel");
							return;
						}
					}
					for (String str : saidArr) {
						String lcStr = str.toLowerCase();
						valid = true;
						if (lcStr.contains("mode")) {
							int mode = requestMode();
							if (mode == -1)
								return;
							alarm.setMode(mode);
						} else if (lcStr.contains("name")) {
							String name = requestName();
							if (name == null)
								return;
							alarm.setAlarmName(name);
						} else if (lcStr.contains("time")) {
							int[] timeArr = requestTime();
							int hour = timeArr[0];
							int min = timeArr[1];
							int timeOfDay = timeArr[2];
							int[] alarmTime = new int[2];
							if (timeOfDay == TOD_AM)
								alarmTime[0] = hour == 12 ? 0 : hour;
							else
								alarmTime[0] = hour == 12 ? 12 : hour + 12;
							alarmTime[1] = min;
							alarm.setAlarmTime(alarmTime);
						} else if (lcStr.contains("repeat")) {
							ArrayList<Integer> repeatDays = requestRepeat();
							int[] repeat = new int[repeatDays.size()];
							for (int i = 0; i < repeat.length; i++)
								repeat[i] = repeatDays.get(i);
							alarm.setRepeat(repeat);
						} else
							valid = false;
						if (valid) {
							removeAlarmFile(alarm);
							createAlarmFile(alarm);
							alarms.add(alarm);
							scheduelAlarm(alarm);
						}
					}
					timesAsked++;
					if (!valid && timesAsked < 3)
						Speak.say("I'm sorry, I didn't understand you. Could you repeat that");
				}
			}
		}
	}

	private void deleteAlarm() {

		Alarm alarm = requestAlarm();
		if (alarm == null)
			return;

		removeAlarmFile(alarm);
		Speak.say("Alarm " + alarm.getAlarmID() + " deleted");
	}

	private void listAlarms() {
		if (alarms.size() == 0) {
			Speak.say("There are no alarms to list.");
			return;
		}
		ArrayList<String> toSay = new ArrayList<String>();
		toSay.add("The alarms and their ID's are");
		for (Alarm alm : alarms) {
			toSay.add("Alarm " + alm.getAlarmID() + ", " + alm.getAlarmName());
		}
		Speak.say(toSay);
	}

	private void createAlarmFile(Alarm alm) {
		File f = alm.getAlarmFile();
		File dir = new File(ALARM_FILES_DIRECTORY);
		if (!dir.exists())
			dir.mkdir();
		if (f.exists()) {
			try {
				throw new FileAlreadyExistsException("New alarm file attempted to be created with ID of " + alm.getAlarmID());
			} catch (FileAlreadyExistsException e) {
				e.printStackTrace();
				Speak.say("Unable to create new alarm file because the file already exists");
				return;
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write("AlmName:" + alm.getAlarmName());
			writer.newLine();
			writer.write("AlmTime:" + alm.getAlarmTime()[0] + "-" + alm.getAlarmTime()[1]);
			writer.newLine();
			int[] almRepeat = alm.getRepeat();
			String almRepeatStr = "";
			for (int i = 0; i < almRepeat.length; i++) {
				almRepeatStr += almRepeat[i];
				if (i != almRepeat.length - 1)
					almRepeatStr += "-";
			}
			writer.write("Repeat:" + almRepeatStr);
			writer.newLine();
			writer.write("AlmSoundFile:" + alm.getToneFileName());
			writer.newLine();
			writer.write("AlmMode:" + alm.getMode());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeAlarmFile(Alarm alm) {
		alarms.remove(alm);
		alm.getAlarmFile().delete();
		alm.getTimerTask().cancel();
	}

	private void scheduelAlarm(Alarm alarm) {
		if (alarm.getTimerTask() != null)
			alarm.getTimerTask().cancel();
		CustomTimerTask task = new CustomTimerTask(alarm.getAlarmID()) {

			@Override
			public void run() {
				soundAlarm(getId());
			}
		};
		alarm.setTimerTask(task);
		Calendar cal = Calendar.getInstance();
		int cHour = cal.get(Calendar.HOUR_OF_DAY);
		int cMin = cal.get(Calendar.MINUTE);
		int aHour = alarm.getAlarmTime()[0];
		int aMin = alarm.getAlarmTime()[1];
		if (alarm.getRepeat().length == 0) {
			if (cHour > aHour || (cHour == aHour && cMin >= aMin))
				cal.add(Calendar.DAY_OF_WEEK, 1);
		} else {
			int cDay = cal.get(Calendar.DAY_OF_WEEK);
			int aDay = -1;
			ArrayList<Integer> repeats = new ArrayList<Integer>();
			for (int i : alarm.getRepeat())
				repeats.add(i);
			Collections.sort(repeats);
			for (int rDay : repeats) {
				if (rDay == cDay) {
					if (cHour < aHour || (cHour == aHour && cMin < aMin)) {
						aDay = cDay;
						break;
					}
				} else if (rDay > cDay) {
					aDay = rDay;
					break;
				}
			}
			if (aDay == -1)
				aDay = repeats.get(0);
			if (aDay < cDay) {
				cal.add(Calendar.DAY_OF_WEEK, (7 - cDay) + aDay);
			} else
				cal.set(Calendar.DAY_OF_WEEK, aDay);
		}
		cal.set(Calendar.HOUR_OF_DAY, aHour);
		cal.set(Calendar.MINUTE, aMin);
		cal.set(Calendar.SECOND, 0);
		Date date = new Date(cal.getTimeInMillis());
		timer.schedule(task, date);
	}

	public void newAlarm() {
		int[] timeArr = requestTime();
		if (timeArr == null)
			return;
		if (timeArr.length != 3) {
			Speak.say("Incorrectly formatted time. Bug Alert!");
			return;
		}
		int hour = timeArr[0];
		int min = timeArr[1];
		int timeOfDay = timeArr[2];
		ArrayList<Integer> repeatDays = requestRepeat();
		if (repeatDays == null)
			return;
		int almMode = requestMode();
		if (almMode == -1)
			return;
		String almName = requestName();
		if (almName == null)
			return;
		Speak.say("alarm schedueled for " + hour + ":" + min + " " + (timeOfDay == TOD_AM ? "a.m." : "p.m."));
		if (repeatDays.size() != 0) {
			String days = "";
			for (int i : repeatDays)
				days += (dayOfWeekToText(i) + ", ");
			Speak.say("repeating every" + days);
		}
		int[] alarmTime = new int[2];
		if (timeOfDay == TOD_AM)
			alarmTime[0] = hour == 12 ? 0 : hour;
		else
			alarmTime[0] = hour == 12 ? 12 : hour + 12;
		alarmTime[1] = min;
		String toneFileName = "alarm.wav";
		int almID = 1;
		while (idInUse(almID))
			almID++;
		int[] repeat = new int[repeatDays.size()];
		for (int i = 0; i < repeat.length; i++)
			repeat[i] = repeatDays.get(i);
		Alarm alm = new Alarm(almName, alarmTime, toneFileName, almID, null, new File(ALARM_FILES_DIRECTORY + "/" + almID + ".alm"), repeat, almMode);
		createAlarmFile(alm);
		alarms.add(alm);
		scheduelAlarm(alm);
	}

	private void soundAlarm(int id) {
		Alarm alm = null;
		for (Alarm a : alarms)
			if (a.getAlarmID() == id)
				alm = a;
		if (alm == null)
			try {
				throw new Exception("Unable to locate alarm");
			} catch (Exception e) {
				e.printStackTrace();
			}
		int almMode = alm.getMode();
		if (almMode == Alarm.ALM_MODE_INFO) {
			AudioPlayer.playSound(alm.getToneFileName(), true);
			sayTime(true);
			Weather weather = new Weather();
			weather.start();
		} else if (almMode == Alarm.ALM_MODE_SILENT) {
			System.out.println("Silent Alarm");
		} else if (almMode == Alarm.ALM_MODE_ONCE) {
			AudioPlayer.playSound(alm.getToneFileName(), true);
		} else if (almMode == Alarm.ALM_MODE_CONT) {
			AudioPlayer.playSound(alm.getToneFileName(), true);
			Speak.say("Continuous mode is not currently supported");
		}
		if (alm.getRepeat().length == 0)
			removeAlarmFile(alm);
	}

	private String dayOfWeekToText(int i) {
		if (i == Calendar.SUNDAY)
			return "Sunday";
		else if (i == Calendar.MONDAY)
			return "Monday";
		else if (i == Calendar.TUESDAY)
			return "Tuesday";
		else if (i == Calendar.WEDNESDAY)
			return "Wednesday";
		else if (i == Calendar.THURSDAY)
			return "Thursday";
		else if (i == Calendar.FRIDAY)
			return "Friday";
		else if (i == Calendar.SATURDAY)
			return "Saturday";
		return null;
	}

	private int parseDayOfWeek(String strDay) {
		strDay = strDay.replaceAll(" ", "");
		if (strDay.equalsIgnoreCase("monday"))
			return Calendar.MONDAY;
		else if (strDay.equalsIgnoreCase("tuesday"))
			return Calendar.TUESDAY;
		else if (strDay.equalsIgnoreCase("wednesday"))
			return Calendar.WEDNESDAY;
		else if (strDay.equalsIgnoreCase("thursday"))
			return Calendar.THURSDAY;
		else if (strDay.equalsIgnoreCase("friday"))
			return Calendar.FRIDAY;
		else if (strDay.equalsIgnoreCase("saturday"))
			return Calendar.SATURDAY;
		else if (strDay.equalsIgnoreCase("sunday"))
			return Calendar.SUNDAY;
		else
			return 0;
	}

	private boolean idInUse(int id) {
		for (Alarm a : alarms)
			if (a.getAlarmID() == id)
				return true;
		return false;
	}

	private int[] requestTime() {
		int timesAsked = 0;
		boolean validTime = false;
		String strTime = "", strAmPm = "";
		while (!validTime) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone and try again.");
				Speak.say(toSay);
				return null;
			}
			Speak.say("Please say the alarm time");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
				timesAsked++;
				if (timesAsked < 3)
					Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
			} else {
				for (String str : saidArr) {
					if (str.toLowerCase().contains("cancel")) {
						Speak.say("Cancel");
						return null;
					}
				}
				Pattern pTime = Pattern.compile("\\d{1,2}?\\:\\d{2}?");
				Pattern pAMPM = Pattern.compile("[ap]\\.m\\.");
				boolean timeFound = false, ampmFound = false;
				for (String str : saidArr) {
					Matcher match = pTime.matcher(str);
					if (match.find()) {
						strTime = match.group();
						timeFound = true;
						break;
					}
				}
				for (String str : saidArr) {
					Matcher match = pAMPM.matcher(str);
					if (match.find()) {
						strAmPm = match.group();
						ampmFound = true;
						break;
					}
				}
				timesAsked++;
				validTime = timeFound && ampmFound;
				if (!validTime && timesAsked < 3)
					Speak.say("I'm sorry, I didn't understand you. Could you repeat that?");
			}
		}
		Speak.say("Understood: " + strTime + " " + strAmPm);
		if (!Pattern.matches("\\d{1,2}?\\:\\d{2}?", strTime) || !Pattern.matches("[ap]\\.m\\.", strAmPm)) {
			Speak.say("Incorrectly formatted time. Bug Alert!");
			return null;
		}
		String[] hourAndMin = strTime.split(":");
		int hour = Integer.parseInt(hourAndMin[0]);
		int min = Integer.parseInt(hourAndMin[1]);
		int timeOfDay;
		if (strAmPm.equals("a.m."))
			timeOfDay = TOD_AM;
		else if (strAmPm.equals("p.m."))
			timeOfDay = TOD_PM;
		else {
			Speak.say("Incorrectly formatted time. Bug Alert!");
			return null;
		}
		int[] time = { hour, min, timeOfDay };
		return time;
	}

	@SuppressWarnings("unchecked")
	private ArrayList<Integer> requestRepeat() {
		boolean validRepeat = false;
		int timesAsked = 0;
		ArrayList<Integer> repeatDays = new ArrayList<Integer>();
		while (!validRepeat) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone and try again.");
				Speak.say(toSay);
				return null;
			}
			Speak.say("Please say when you would like the alarm to repeat");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
				timesAsked++;
				if (timesAsked < 3)
					Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
			} else {
				for (String str : saidArr) {
					if (str.toLowerCase().contains("cancel")) {
						Speak.say("Cancel");
						return null;
					}
				}
				for (String said : saidArr) {
					if (!validRepeat) {
						String[] splitSaid = said.split(" ");
						ArrayList<Integer> notDay = new ArrayList<Integer>();
						for (int i = 0; i < splitSaid.length; i++) {
							if (parseDayOfWeek(splitSaid[i]) == 0)
								notDay.add(i);
						}
						ArrayList<String> notDayWords = new ArrayList<String>();
						ArrayList<String> allWords = new ArrayList<String>();
						ArrayList<Integer> notDayClone = (ArrayList<Integer>) notDay.clone();
						for (String str : splitSaid)
							allWords.add(str.toLowerCase());
						for (int i : notDayClone) {
							String word = allWords.get(i);
							notDayWords.add(word);
							if (word.equalsIgnoreCase("accept") || word.equalsIgnoreCase("but")) {
								word = "except";
								allWords.set(i, word);
								notDayWords.set(notDay.indexOf(i), word);
							} else if (word.equalsIgnoreCase("daily")) {
								word = "everyday";
								allWords.set(i, word);
								notDayWords.set(notDay.indexOf(i), word);
							} else if (word.equalsIgnoreCase("never") || word.equals("1")) {
								word = "once";
								allWords.set(i, word);
								notDayWords.set(notDay.indexOf(i), word);
							} else if (word.equalsIgnoreCase("every") || word.equalsIgnoreCase("and")) {
								allWords.remove(i);
								notDayWords.remove(notDay.indexOf(i));
								notDay.remove(new Integer(i));
							} else if (!word.equalsIgnoreCase("once") && !word.equalsIgnoreCase("everyday") && !word.equalsIgnoreCase("through") && !word.equalsIgnoreCase("except") && !Utils.includes(word, "weekday") && !Utils.includes(word, "weekend")) {
								allWords.remove(i);
								notDayWords.remove(notDay.indexOf(i));
								notDay.remove(new Integer(i));
							}
						}
						if (allWords.size() != 0) {
							boolean exceptParam = false;
							boolean done = false;
							ArrayList<String> exceptArr = new ArrayList<String>();
							if (allWords.contains("except") && allWords.size() >= 3) {
								exceptParam = true;
								exceptArr = (ArrayList<String>) allWords.subList(allWords.indexOf("except"), allWords.size());
								exceptArr.remove("except");
								int exceptLoc = allWords.indexOf("except");
								while (allWords.size() > exceptLoc) {
									allWords.remove(exceptLoc);
								}
							} else if (allWords.contains("except") && allWords.size() < 3)
								done = true;
							if (!done) {
								if (allWords.contains("through") && allWords.size() == 3) {
									if (allWords.get(1).equalsIgnoreCase("through") && parseDayOfWeek(allWords.get(0)) != 0 && parseDayOfWeek(allWords.get(2)) != 0) {
										int day1 = parseDayOfWeek(allWords.get(0));
										int day2 = parseDayOfWeek(allWords.get(2));
										if (day1 > day2) {
											int i = day1;
											while (i <= 7) {
												repeatDays.add(i);
												i++;
											}
											i = 1;
											while (i <= day2) {
												repeatDays.add(i);
												i++;
											}
										} else if (day1 < day2) {
											int i = day1;
											while (i <= day2) {
												repeatDays.add(i);
												i++;
											}
										}
										validRepeat = true;
									} else
										done = true;
								} else if ((allWords.contains("weekdays") || allWords.contains("weekday")) && allWords.size() == 1) {
									repeatDays.add(Calendar.MONDAY);
									repeatDays.add(Calendar.TUESDAY);
									repeatDays.add(Calendar.WEDNESDAY);
									repeatDays.add(Calendar.THURSDAY);
									repeatDays.add(Calendar.FRIDAY);
									validRepeat = true;
								} else if ((allWords.contains("weekends") || allWords.contains("weekend")) && allWords.size() == 1) {
									repeatDays.add(Calendar.SATURDAY);
									repeatDays.add(Calendar.SUNDAY);
									validRepeat = true;
								} else if (allWords.contains("once"))
									validRepeat = true;
								else {
									for (String str : allWords)
										if (parseDayOfWeek(str) != 0)
											repeatDays.add(parseDayOfWeek(str));
									if (repeatDays.size() > 0)
										validRepeat = true;
									else
										done = true;
								}
								if (exceptParam && !done)
									for (String str : exceptArr)
										if (parseDayOfWeek(str) != 0)
											repeatDays.remove(parseDayOfWeek(str));
							}
						}
					}
				}
				timesAsked++;
				if (!validRepeat && timesAsked < 3)
					Speak.say("I'm sorry, I didn't understand you. Could you repeat that");
			}
		}
		return repeatDays;
	}

	private int requestMode() {
		boolean validMode = false;
		int timesAsked = 0;
		int almMode = 0;
		while (!validMode) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone and try again.");
				Speak.say(toSay);
				return -1;
			}
			ArrayList<String> toSay = new ArrayList<String>();
			toSay.add("Please say the alarm mode. The options are:");
			toSay.add("Silent,");
			toSay.add("Continuous,");
			toSay.add("Short,");
			toSay.add("and Info");
			Speak.say(toSay);
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
				timesAsked++;
				if (timesAsked < 3)
					Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
			} else {
				for (String str : saidArr) {
					if (str.toLowerCase().contains("cancel")) {
						Speak.say("Cancel");
						return -1;
					}
				}
				for (String str : saidArr) {
					String lcStr = str.toLowerCase();
					if (lcStr.contains("silent")) {
						almMode = Alarm.ALM_MODE_SILENT;
						Speak.say("Silent mode selected");
						validMode = true;
						break;
					} else if (lcStr.contains("continuous")) {
						almMode = Alarm.ALM_MODE_CONT;
						Speak.say("Continuous mode selected");
						ArrayList<String> toSay2 = new ArrayList<String>();
						toSay2.add("Warning! Continuous mode is not yet supported.");
						toSay2.add("Short mode will be used instead");
						Speak.say(toSay2);
						almMode = Alarm.ALM_MODE_ONCE;
						validMode = true;
						break;
					} else if (lcStr.contains("short")) {
						almMode = Alarm.ALM_MODE_ONCE;
						Speak.say("Short mode selected");
						validMode = true;
						break;
					} else if (lcStr.contains("info")) {
						almMode = Alarm.ALM_MODE_INFO;
						Speak.say("Info mode selected");
						validMode = true;
						break;
					}
				}
				timesAsked++;
				if (!validMode && timesAsked < 3)
					Speak.say("I'm sorry, I didn't understand you. Could you repeat that");
			}
		}
		return almMode;
	}

	private String requestName() {
		boolean validName = false;
		int timesAsked = 0;
		String almName = null;
		while (!validName) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone or choose a different name and try again.");
				Speak.say(toSay);
				return null;
			}
			Speak.say("Please say the alarm name");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
				timesAsked++;
				if (timesAsked < 3)
					Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
			} else {
				for (String str : saidArr) {
					if (str.toLowerCase().contains("cancel")) {
						Speak.say("Cancel");
						return null;
					}
				}
				almName = saidArr.get(0);
				Speak.say("Is " + saidArr.get(0) + " the name you would like to use?");
				saidArr = speechRec();
				if (saidArr != null && saidArr.size() > 0 && !saidArr.contains(null)) {
					for (String str : saidArr) {
						if (str.toLowerCase().contains("cancel")) {
							Speak.say("Cancel");
							return null;
						}
					}
					for (String str : saidArr)
						if (str.toLowerCase().contains("yes")) {
							Speak.say("Alarm Name set as " + almName);
							validName = true;
							break;
						}
				}
				timesAsked++;
				if (!validName && timesAsked < 3)
					Speak.say("Let's try again.");
			}
		}
		return almName;
	}

	private Alarm requestAlarm() {
		boolean validID = false;
		int timesAsked = 0;
		int id = -1;
		Alarm alarm = null;
		while (!validID) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone and try again.");
				Speak.say(toSay);
				return null;
			}
			if (alarms.size() == 0) {
				Speak.say("There are no alarms saved");
				return null;
			}
			ArrayList<String> toSay = new ArrayList<String>();
			toSay.add("Please say the alarm I.D.");
			for (Alarm alm : alarms) {
				toSay.add("Alarm " + alm.getAlarmID() + ", " + alm.getAlarmName());
			}
			Speak.say(toSay);
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null)) {
				timesAsked++;
				if (timesAsked < 3)
					Speak.say("I'm sorry, I couldn't hear you. Could you repeat that?");
			} else {
				for (String str : saidArr) {
					if (str.toLowerCase().contains("cancel")) {
						Speak.say("Cancel");
						return null;
					}
				}
				for (String str : saidArr) {
					Pattern p = Pattern.compile("\\d+");
					Matcher m = p.matcher(str);
					if (m.find()) {
						id = Integer.parseInt(m.group());
						for (Alarm alm : alarms) {
							if (alm.getAlarmID() == id) {
								alarm = alm;
								break;
							}
						}
						if (alarm != null)
							validID = true;
						break;
					}
				}
				timesAsked++;
				if (!validID && timesAsked < 3)
					Speak.say("I'm sorry, That I.D. is invalid. Please try again");
			}
		}
		return alarm;
	}

	public abstract void sayTime(boolean schoolDay);

	public abstract ArrayList<String> speechRec();

}
