package com.cicc.speech;

import com.cicc.voiceCont.SpeechResponce;
import com.cicc.voiceCont.Utils;

public class TtsSpeechResponse extends SpeechResponce {

	private static final String[][] enableArgs = { { "enable" }, { "silent" } };
	private static final String[][] disableArgs = { { "disable" }, { "silent" } };
	private static final String[][] toggleArgs = { { "silent" } };

	private static final String[][][] paramArrays = { enableArgs, disableArgs, toggleArgs };


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
					Speak.enableSilent();
					break;
				case 1:
					Speak.disableSilent();
					Speak.say("Silent mode disabled");
					break;
				case 2:
					if (Speak.isSilent()) {
						Speak.disableSilent();
						Speak.say("Silent mode disabled");
					} else
						Speak.enableSilent();
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
