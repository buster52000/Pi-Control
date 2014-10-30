package com.cicc.alpha;

import com.cicc.speech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class AlphaSpeechResponse extends SpeechResponce {

public static final String [][] calendarArgs = {{"calendar"}};
	
	private static final String[][] searchKeywords = { {"search", "google", "tell me", "question", "query" }};
	private static final String[][] questionWords = { {"who", "what", "where", "when", "why", "how" }};
	
	private static final String [][][] paramArrays = {searchKeywords, questionWords};
	
	private Search waSearch;
	
	public AlphaSpeechResponse(String wolframID) {
		waSearch = new Search(wolframID);
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
		return SpeechResponce.PRIORITY_LOW;
	}

	@Override
	public void action(String response) {
		for (int i = 0; i < paramArrays.length; i++)
			if (Utils.speechMatchesParams(response, paramArrays[i])) {
				switch (i) {
				case 0:
					response = response.toLowerCase();
					int j = Utils.arrContains(response, searchKeywords[0]);
					response = response.replaceFirst("[a-zA-Z0-9]" + searchKeywords[j] + " ", "");
					Speak.say(waSearch.getPlaintextForQuery(response));
					break;
				case 1:
					Speak.say(waSearch.getPlaintextForQuery(response));
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
