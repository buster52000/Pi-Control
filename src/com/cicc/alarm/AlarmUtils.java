package com.cicc.alarm;

import java.util.Calendar;

import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.Utils;

public class AlarmUtils {

	public static void main(String args[]) {
		saySchoolDay();
	}
	
	public static String[] getRSSTitles(String url) {
		RSSFeedParser parser = new RSSFeedParser(url);
		Feed feed = parser.readFeed();
		int size = feed.getMessages().size();
		String[] msgs = new String[size];
		for (int i = 0; i < size; i++) {
			msgs[i] = feed.getMessages().get(i).getTitle();
		}
		return msgs;
	}
	
	public static int parseDayOfWeek(String strDay) {
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
	

	public static String dayOfWeekToText(int i) {
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
	
	public static void saySchoolDay() {
		String [] rssTitles = getRSSTitles("http://www.pburgsd.net/site/RSS.aspx?DomainID=152&ModuleInstanceID=2054&PageID=1207");
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		System.out.println(month + "/" + day + "/" + year);
		for (String s : rssTitles) {
			System.out.println(s);
			if (Utils.includes(s, month + "/" + day + "/" + year)) {
				if (Utils.includes(s, "Day 1")) {
					Speak.say("Today is a day 1");
				} else if (Utils.includes(s, "Day 2")) {
					Speak.say("Today is a day 2");
				} else if (Utils.includes(s, "Day 3")) {
					Speak.say("Today is a day 3");
				} else if (Utils.includes(s, "Day 4")) {
					Speak.say("Today is a day 4");
				}
				if ((Utils.includes(s, "1/2") && Utils.includes(s, "Dismissal")) || (Utils.includes(s, "Half") && Utils.includes(s, "Dismissal"))) {
					Speak.say("Today is a half day");
				}
				if (Utils.includes(s, "School Closed")) {
					Speak.say("School is closed today");
				}
			}
		}
	}
	
}
