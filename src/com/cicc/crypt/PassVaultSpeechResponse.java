package com.cicc.crypt;

import com.cicc.texttospeech.Speak;
import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class PassVaultSpeechResponse extends SpeechResponce {

	private static final String[][] secureStoreArgs = { { "password" }, { "vault" } };

	private static final String[][][] paramArrays = { secureStoreArgs };

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
					Speak.say("The password vault is not complete");
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
