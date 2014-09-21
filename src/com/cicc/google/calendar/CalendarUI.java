package com.cicc.google.calendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.Utils;

public abstract class CalendarUI {

	private CalendarControler calCont;

	public static void main(String args[]) throws Exception {
		CalendarUI cUI = new CalendarUI("buster52000@gmail.com") {

			@Override
			public ArrayList<String> speechRec() {
				ArrayList<String> arr = new ArrayList<String>();
				arr.add(JOptionPane.showInputDialog("Speech Recognition"));
				return arr;
			}
		};
		cUI.testing();
	}

	public CalendarUI(String email) throws Exception {
		calCont = new CalendarControler(email);
	}

	public void startInteractiveSpeech() throws IOException, InterruptedException {
		Speak.say("The interactive calendar is not complete");
		boolean done = false;
		while (!done) {
			Speak.say("Please say a Calendar command or help for more options");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null))
				done = true;
			else
				for (String s : saidArr) {
					if (Utils.includes(s, "list") && Utils.includes(s, "calendar")) {
						listCals();
					}
				}
		}
	}

	private void listCals() throws IOException, InterruptedException {
		ArrayList<String[]> calNames = calCont.getCalendarNames();
		ArrayList<String> toSay = new ArrayList<String>();
		toSay.add("The calendars are:");
		for (String[] str : calNames) {
			toSay.add(str[0]);
		}
		Speak.say(toSay);
	}

	public void testing() throws IOException, InterruptedException {
		System.out.println(selectCal());
	}

	private String selectCal() throws IOException, InterruptedException {
		ArrayList<String[]> calNames = calCont.getCalendarNames();
		boolean validID = false;
		int timesAsked = 0;
		String cID = null;
		while (!validID) {
			if (timesAsked >= 3) {
				ArrayList<String> toSay = new ArrayList<String>();
				toSay.add("I'm sorry, I can't seem to understand you.");
				toSay.add("Please reposition the microphone and try again.");
				Speak.say(toSay);
				return null;
			}
			ArrayList<String> toSay = new ArrayList<String>();
			toSay.add("Please say the number of the corresponding calendar. The calendars are:");
			int i = 1;
			for (String[] str : calNames) {
				toSay.add(i + ". " + str[0]);
				i++;
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
						int id = Integer.parseInt(m.group());
						cID = calNames.get(id)[1];
						validID = true;
						break;
					}
				}
				timesAsked++;
				if (!validID && timesAsked < 3)
					Speak.say("I'm sorry, That I.D. is invalid. Please try again");
			}
		}
		
		return cID;
	}

	public abstract ArrayList<String> speechRec();

}
