package com.cicc.voiceCont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import com.cicc.speech.Speak;

public class Utils {

	public static final String WINDOWS_FLAC_PATH = "\"C:/Program Files (x86)/FLAC/flac.exe\" --bps=16 --sign=signed --endian=little --channels=1 --sample-rate=";
	public static final String LINUX_FLAC_PATH = "/usr/bin/flac --bps=16 --sign=signed --endian=little --channels=1 --sample-rate=";

	public static void runCmd(String cmd) {
		ArrayList<String> cmds = new ArrayList<String>();
		for (String str : cmd.split(" "))
			cmds.add(str);
		ProcessBuilder pb = new ProcessBuilder(cmds);
		try {
			Process pr = pb.start();
			pr.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int arrContains(String text, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (includes(text, arr[i])) {
				return i;
			}
		}
		return -1;
	}

	//TODO: Remove as this is no longer needed
	public static void convertWavToFlac(File wavFile, File flacFile, int sampleRate) {
		String os = System.getProperty("os.name");
		if (flacFile.exists())
			flacFile.delete();
		if (includes(os, "Windows"))
			runCmd(WINDOWS_FLAC_PATH + sampleRate + " " + wavFile.getName());
		else if (includes(os, "linux"))
			runCmd(LINUX_FLAC_PATH + sampleRate + " " + wavFile.getName());
		else {
			Speak.say("This operating system is not supported");
			System.exit(0);
		}
	}

	public static boolean includes(String str1, String str2) {
		return str1.toLowerCase().contains(str2.toLowerCase());
	}

	public static String getStringAfter(String str1, String str2) {
		String str = "";
		if (str1.startsWith(str2)) {
			str = str1.substring(str2.length() + 1, str1.length());
		}
		return str;
	}

	public static ArrayList<String> readFileToArray(File f) {
		ArrayList<String> arr = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String tmp = null;
			do {
				tmp = reader.readLine();
				if (tmp != null)
					arr.add(tmp);
			} while (tmp != null);
			reader.close();
			return arr;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean speechMatchesParams(String response, String[] neededWords) {
		return speechMatchesParams(response, new String[][] { neededWords });
	}

	public static boolean speechMatchesParams(String response, String[][] neededWords) {
		for (String[] strList : neededWords) {
			boolean good = false;
			if (strList != null)
				for (String str : strList)
					if (str != null && includes(response, str)) {
						good = true;
						break;
					}
			if (!good)
				return false;
		}
		return true;
	}

	/**
	 * Returns the first instance of String found exclusively between part1 and
	 * part2.
	 * 
	 * @param s
	 *            The String you want to substring.
	 * @param part1
	 *            The beginning of the String you want to search for.
	 * @param part2
	 *            The end of the String you want to search for.
	 * @return The String between part1 and part2. If the s does not contain
	 *         part1 or part2, the method returns null.
	 */
	public static String substringBetween(String s, String part1, String part2) {
		String sub = null;

		int i = s.indexOf(part1);
		int j = s.indexOf(part2, i + part1.length());

		if (i != -1 && j != -1) {
			int nStart = i + part1.length();
			sub = s.substring(nStart, j);
		}

		return sub;
	}
	
	public static void sayTime(boolean sayDate) {
		int hour = Calendar.getInstance().get(Calendar.HOUR);
		int min = Calendar.getInstance().get(Calendar.MINUTE);
		int amPm = Calendar.getInstance().get(Calendar.AM_PM);
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		String sMonth = "";
		String sDow = "";
		switch (month) {
		case Calendar.JANUARY:
			sMonth = "January";
			break;
		case Calendar.FEBRUARY:
			sMonth = "February";
			break;
		case Calendar.MARCH:
			sMonth = "March";
			break;
		case Calendar.APRIL:
			sMonth = "April";
			break;
		case Calendar.MAY:
			sMonth = "May";
			break;
		case Calendar.JUNE:
			sMonth = "June";
			break;
		case Calendar.JULY:
			sMonth = "July";
			break;
		case Calendar.AUGUST:
			sMonth = "August";
			break;
		case Calendar.SEPTEMBER:
			sMonth = "September";
			break;
		case Calendar.OCTOBER:
			sMonth = "October";
			break;
		case Calendar.NOVEMBER:
			sMonth = "November";
			break;
		case Calendar.DECEMBER:
			sMonth = "December";
			break;
		default:
			sMonth = "Unknown Month";
			break;
		}
		switch (dow) {
		case Calendar.SUNDAY:
			sDow = "Sunday";
			break;
		case Calendar.MONDAY:
			sDow = "Monday";
			break;
		case Calendar.TUESDAY:
			sDow = "Tuesday";
			break;
		case Calendar.WEDNESDAY:
			sDow = "Wednesday";
			break;
		case Calendar.THURSDAY:
			sDow = "Thursday";
			break;
		case Calendar.FRIDAY:
			sDow = "Friday";
			break;
		case Calendar.SATURDAY:
			sDow = "Saturday";
			break;
		default:
			sDow = "Unknown day of week";
			break;
		}
		String am = "";
		if (amPm == Calendar.AM)
			am = "A.M.";
		else
			am = "P.M.";
		if (min > 0)
			Speak.say("the time is " + hour + " " + ((min < 10) ? "O " : "") + min + " " + am);
		else
			Speak.say("the time is " + hour + " o'clock");
		if(sayDate)
		Speak.say("the date is " + sDow + " " + sMonth + " " + date + ", " + year);
	}

}
