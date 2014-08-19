package com.cicc.voiceCont;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;

import com.cicc.alarm.AlarmController;
import com.cicc.alpha.Search;
import com.cicc.crypt.MainCrpt;
import com.cicc.geoLoc.GeoLocation;
import com.cicc.google.calendar.CalendarUI;
import com.cicc.tts.Speak;
import com.cicc.tts.Utils;
import com.cicc.weather.Weather;
import com.smartechz.geoloc.GeoPlanetExplorer;

public abstract class Action {

	private boolean other;
	private Search waSearch;
	private final String[] searchKeywords = { "search", "google", "tell me", "question", "query" };
	private final String[] questionWords = { "who", "what", "where", "when", "why", "how" };
	private final String emailFileName = "email.txt";
	private AlarmController alarm;
	private String wolframAlphaID;
	private ArrayList<String> said;
	private String email;
	private CalendarUI calUI;

	public Action(String wolframID) throws Exception {
		other = false;
		said = null;
		wolframAlphaID = wolframID;
		waSearch = new Search(wolframAlphaID);
		try {
			File emailFile = new File(emailFileName);
			String tmpEmail = null;
			if(emailFile.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(emailFile));
				tmpEmail = read.readLine();
				read.close();
				if(tmpEmail == null || tmpEmail.length() == 0 || !tmpEmail.contains("@")) {
					email = requestEmail();
					emailFile.delete();
					BufferedWriter write = new BufferedWriter(new FileWriter(emailFile));
					write.write(email);
					write.close();
				} else
					email = tmpEmail;
			} else {
				email = requestEmail();
				BufferedWriter write = new BufferedWriter(new FileWriter(emailFile));
				write.write(email);
				write.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		alarm = new AlarmController() {

			@Override
			public ArrayList<String> speechRec() {
				return speechRecognition();
			}

			@Override
			public void sayTime() {
				time();
			}
		};
		calUI = new CalendarUI(email) {
			
			@Override
			public ArrayList<String> speechRec() {
				return speechRecognition();
			}
		};
	}

	public boolean doSomething(String text) throws IOException, InterruptedException {
		if (text == null) {
			Main.say("Command Timed out");
		} else if (Utils.includes(text, "cancel")) {
			Speak.say("Cancel");
			return true;
		} else if (text.equalsIgnoreCase("Hello")) {
			Main.say("Hello Ryan, How are you doing?");
		} else if ((Utils.includes(text, "speech recognition") || Utils.includes(text, "program")) && ((Utils.includes(text, "stop") || Utils.includes(text, "terminate") || Utils.includes(text, "disable")))) {
			Main.say("Terminating Speech Recognition");
			System.exit(0);
		} else if (Utils.includes(text, "shut down")) {
			Main.say("System Shutting Down");
			Utils.runCmd("sudo halt -p");
		} else if (Utils.includes(text, "restart") || Utils.includes(text, "reboot")) {
			Main.say("System Rebooting");
			Utils.runCmd("sudo reboot");
		} else if (Utils.includes(text, "enable") && Utils.includes(text, "silent")) {
			Speak.enableSilent();
		} else if (Utils.includes(text, "disable") && Utils.includes(text, "silent")) {
			Speak.disableSilent();
			Main.say("Silent mode disabled");
		} else if (Utils.includes(text, "silent") && !Utils.includes(text, "enable") && !Utils.includes(text, "disable")) {
			if (Speak.isSilent()) {
				Speak.disableSilent();
				Main.say("Silent mode disabled");
			} else
				Speak.enableSilent();
		} else if (Utils.includes(text, "weather")) {
			if (Utils.includes(text, "by location") || Utils.includes(text, "location") || Utils.includes(text, "for")) {
				Speak.say("Please say the location");
				ArrayList<String> possibles = speechRecognition();
				boolean good = false;
				for (String s : possibles) {
					if (GeoPlanetExplorer.getWOEID(s) != null) {
						Weather weather = new Weather(s);
						weather.start();
						good = true;
						break;
					}
				}
				if (!good) {
					Speak.say("Unable to get the weather on that location");
				}
			} else {
				Weather weather = new Weather();
				weather.start();
			}
		} else if (text.equalsIgnoreCase("where am I")) {
			Speak.say("You are in " + GeoLocation.getLoc());
		} else if (Utils.includes(text, "Phillipsburg") && Utils.includes(text, "calendar")) {
			Main.say("The school calendar portion of this program is not complete");
		} else if (Utils.includes(text, "calendar")) {
			calUI.startInteractiveSpeech();
		} else if (Utils.includes(text, "time")) {
			time();
		} else if (Utils.includes(text, "alarm")) {
			alarm.startInteractiveSpeech();
		} else if (Utils.includes(text, "password") && Utils.includes(text, "vault")) {
			Main.say("The password vault is not complete");
			MainCrpt.run();
		} else if (Utils.arrContains(text, searchKeywords) >= 0 && Utils.arrContains(text, searchKeywords) < searchKeywords.length) {
			text = text.toLowerCase();
			int i = Utils.arrContains(text, searchKeywords);
			text = text.replaceFirst("[a-zA-Z0-9]" + searchKeywords[i] + " ", "");
			Main.say(waSearch.getPlaintextForQuery(text));
		} else if (Utils.arrContains(text.split(" ")[0], questionWords) != -1) {
			Main.say(waSearch.getPlaintextForQuery(text));
		} else if (text.equalsIgnoreCase("test mode")) {
			while (true) {
				JOptionPane.showMessageDialog(null, "Click ok to start test");
				ArrayList<String> results = speechRecognition();
				System.out.println(results);
				for (String s : results)
					if (s.equalsIgnoreCase("exit"))
						return true;
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean isOther() {
		return other;
	}

	public void setSaid(ArrayList<String> said) {
		this.said = said;
	}
	
	public void setOther(boolean other) {
		this.other = other;
	}

	public ArrayList<String> speechRecognition() {
		said = null;
		start();
		other = true;
		while (said == null) {
			try {
				Thread.sleep(125);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		other = false;
		return said;
	}
	
	private String requestEmail() {
		Speak.say("Please type your email address");
		return JOptionPane.showInputDialog("Please type your email address");
	}

	public void time() {
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
			Main.say("the time is " + hour + " " + ((min < 10) ? "O " : "") + min + " " + am);
		else
			Main.say("the time is " + hour + " o'clock");
		Speak.say("the date is " + sDow + " " + sMonth + " " + date + ", " + year);
	}

	public abstract void start();

}
