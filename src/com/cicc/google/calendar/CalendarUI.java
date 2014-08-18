package com.cicc.google.calendar;

import java.io.IOException;
import java.util.ArrayList;

import com.cicc.tts.Speak;
import com.cicc.tts.Utils;

public abstract class CalendarUI {

	@SuppressWarnings("unused")
	private CalendarControler calCont;
	
	public CalendarUI(String email) throws Exception {
		calCont = new CalendarControler(email);
	}
	
	public void startInteractiveSpeech() throws IOException, InterruptedException {
		Speak.say("The interactive calendar is not complete");
		boolean done = false;
		while(!done) {
			Speak.say("Please say a Calendar command or help for more options");
			ArrayList<String> saidArr = speechRec();
			if (saidArr == null || saidArr.size() == 0 || saidArr.contains(null))
				done = true;
			else
				for(String s : saidArr) {
					if(Utils.includes(s, "list") && Utils.includes(s, "calendar")) {
						listCals();
					}
				}
		}
	}
	
	private void listCals() throws IOException, InterruptedException {
//		ArrayList<String[]> calNames = calCont.getCalendarNames();
//		ArrayList<String> toSay = new ArrayList<String>();
	}
	
	public abstract ArrayList<String> speechRec();
	
}
