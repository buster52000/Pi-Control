package com.cicc.voiceCont;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;

import com.cicc.alarm.AlarmSpeechResponse;
import com.cicc.alpha.AlphaSpeechResponse;
import com.cicc.geoLoc.GeoLocSpeechResponse;
import com.cicc.google.GoogleSpeechResponse;
import com.cicc.speech.Speak;
import com.cicc.speech.TtsSpeechResponse;
import com.cicc.weather.WeatherSpeechResponse;

public abstract class Action {

	private boolean other;
	private final String emailFileName = "email.txt";
	private String wolframAlphaID;
	private ArrayList<String> said;
	private String email;
	private final SpeechResponce[] responders;
	private SpeechRequestListener requestListener;
	private Comparator<SpeechResponce> comparator;

	public Action(String wolframID) throws Exception {
		other = false;
		said = null;
		wolframAlphaID = wolframID;
		try {
			File emailFile = new File(emailFileName);
			String tmpEmail = null;
			if (emailFile.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(emailFile));
				tmpEmail = read.readLine();
				read.close();
				if (tmpEmail == null || tmpEmail.length() == 0 || !tmpEmail.contains("@")) {
					email = requestEmail();
					emailFile.delete();
					BufferedWriter write = new BufferedWriter(new FileWriter(emailFile));
					write.write(email);
					write.close();
				} else
					email = tmpEmail;
			} else {
				email = requestEmail();
				BufferedWriter write = new BufferedWriter(new FileWriter(emailFile));
				write.write(email);
				write.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		responders = new SpeechResponce[] { new SystemSpeechCommands(), new WeatherSpeechResponse(), new GoogleSpeechResponse(email), new TtsSpeechResponse(), new AlarmSpeechResponse(), new GeoLocSpeechResponse(), new AlphaSpeechResponse(wolframAlphaID) };
		requestListener = new SpeechRequestListener() {

			@Override
			public ArrayList<String> speechRecognition() {
				return speechRec();
			}
		};
		for (SpeechResponce res : responders)
			res.setSpeechRequestListener(requestListener);
		comparator = new Comparator<SpeechResponce>() {

			@Override
			public int compare(SpeechResponce o1, SpeechResponce o2) {
				return Integer.compare(o1.getPriority(), o2.getPriority());
			}
		};
	}

	public boolean doSomething(String text) throws IOException, InterruptedException {
		ArrayList<SpeechResponce> acceptsRes = new ArrayList<SpeechResponce>();
		for (SpeechResponce res : responders)
			if (res.acceptableResponse(text))
				acceptsRes.add(res);
		Collections.sort(acceptsRes, comparator);
		if (acceptsRes == null || acceptsRes.size() == 0)
			return false;
		else
			acceptsRes.get(0).action(text);
		return true;
	}

	public boolean isOther() {
		return other;
	}

	public void setSaid(ArrayList<String> said) {
		this.said = said;
	}

	public void setOther(boolean other) {
		this.other = other;
	}

	public ArrayList<String> speechRec() {
		said = null;
		other = true;
		start();
		while (said == null) {
			try {
				Thread.sleep(125);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		other = false;
		return said;
	}

	private String requestEmail() {
		Speak.say("Please type your email address");
		return JOptionPane.showInputDialog("Please type your email address");
	}

	public abstract void start();

}
