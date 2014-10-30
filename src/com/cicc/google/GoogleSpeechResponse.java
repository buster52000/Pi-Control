package com.cicc.google;

import java.io.IOException;
import java.util.ArrayList;

import com.cicc.google.calendar.CalendarUI;
import com.cicc.speech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class GoogleSpeechResponse extends SpeechResponce {

	public static final String [][] schoolCalendarArgs = {{"phillipsburg", "school"}, {"calendar"}};
	public static final String [][] calendarArgs = {{"calendar"}};
	
	private static final String [][][] paramArrays = {schoolCalendarArgs, calendarArgs};
	
	private CalendarUI calUI;
	
	public GoogleSpeechResponse(String email) {
		try {
			calUI = new CalendarUI(email) {

				@Override
				public ArrayList<String> speechRec() {
					return speechRecognition();
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean acceptableResponse(String response) {
		if (response == null)
			return false;
		for (String[][] strArr : paramArrays)
			if (Utils.speechMatchesParams(response, strArr))
				return true;
		return false;
	}

	@Override
	public int getPriority() {
		return SpeechResponce.PRIORITY_NORMAL;
	}

	@Override
	public void action(String text) {
		for (int i = 0; i < paramArrays.length; i++)
			if (Utils.speechMatchesParams(text, paramArrays[i])) {
				switch (i) {
				case 0:
					Speak.say("The school calendar portion of this program is not complete");
					break;
				case 1:
					try {
						calUI.startInteractiveSpeech();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					break;
				default:
					Speak.say("The response for this command has not been created");
					break;
				}
				return;
			}
		Speak.say("I'm sorry, something went wrong when I was trying to respond");
	}

}
