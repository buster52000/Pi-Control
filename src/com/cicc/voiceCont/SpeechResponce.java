package com.cicc.voiceCont;

import java.util.ArrayList;

public abstract class SpeechResponce {

	public static final int PRIORITY_HIGH = 0;
	public static final int PRIORITY_NORMAL = 1;
	public static final int PRIORITY_LOW = 2;

	private SpeechRequestListener listener;

	public ArrayList<String> speechRecognition() {
		if (listener == null)
			return null;
		return listener.speechRecognition();
	}

	public void setSpeechRequestListener(SpeechRequestListener listener) {
		this.listener = listener;
	}

	public void removeSpeechRequestListener() {
		listener = null;
	}
	
	
	public abstract boolean acceptableResponse(String response);

	public abstract int getPriority();

	public abstract void action(String text);

}
