package com.cicc.weather;

import java.util.ArrayList;

import com.cicc.speech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;
import com.smartechz.geoloc.GeoPlanetExplorer;

public class WeatherSpeechResponse extends SpeechResponce {

	private static final String[][] locWeatherArgs = { { "weather" }, { "location", "for" } };
	private static final String[][] weatherArgs = { { "weather" } };

	private static final String[][][] paramArrays = { locWeatherArgs, weatherArgs };

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
					Speak.say("Please say the location");
					ArrayList<String> possibles = speechRecognition();
					boolean good = false;
					for (String s : possibles)
						if (GeoPlanetExplorer.getWOEID(s) != null) {
							Weather weather = new Weather(s);
							weather.start();
							good = true;
							break;
						}
					if (!good)
						Speak.say("Unable to get the weather for that location");
					break;
				case 1:
					Weather weather = new Weather();
					weather.start();
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
