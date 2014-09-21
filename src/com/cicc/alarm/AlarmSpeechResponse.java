package com.cicc.alarm;

import java.util.ArrayList;

import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class AlarmSpeechResponse extends SpeechResponce {

	private static final String[][] stopAlarmArgs = {{"stop", "disable", "i'm", "i am"}, {"alarm", "awake", "up"}};
	private static final String[][] alarmArgs = { { "alarm" } };

	private static final String[][][] paramArrays = { stopAlarmArgs, alarmArgs };

	private AlarmController alarm;

	public AlarmSpeechResponse() {
		alarm = new AlarmController() {
			
			@Override
			public ArrayList<String> speechRec() {
				return speechRecognition();
			}
		};
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
					Speak.say("Unable to stop alarms");
					break;
				case 1:
					alarm.startInteractiveSpeech();
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
