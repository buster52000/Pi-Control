package com.cicc.geoLoc;

import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class GeoLocSpeechResponse extends SpeechResponce {

	private static final String[][] geoLocArgs = { { "am i", "i am", "get" }, { "where", "location" } };

	private static final String[][][] paramArrays = { geoLocArgs };

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
					Speak.say("You are in " + GeoLocation.getLoc());
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
