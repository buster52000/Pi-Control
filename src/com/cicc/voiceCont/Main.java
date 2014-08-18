package com.cicc.voiceCont;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFileFormat;

import com.cicc.tts.Speak;
import com.cicc.tts.Utils;
import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;

//import com.darkprograms.speech.recognizer.FlacEncoder;

public class Main {
	private static MicrophoneAnalyzer microphone;
	private Recognizer recognizer;
	private String audioFileName = "f.wav";
	private String flacFileName = "f.flac";
	private boolean started = false;
	private boolean checking = false;
	private boolean responding = false;
	private Timer micTimer;
	private TimerTask task;
	public static final String responces[] = { "I am listening", "Yes Sir?", "How may I help you?", "Yes Ryan?" };
	private SoundManager sndMngr;
	private Action action;
	public final String API_KEY;
	public final String WOLFRAM_ID;
	public static final int SAMPLE_RATE = 16000;
	private boolean loaded = false;

	public Main() throws Exception {
		String [] IDs = readIDs();
		API_KEY = IDs[0];
		WOLFRAM_ID = IDs[1];
		say("System Starting");
		initMic();
		action = new Action(WOLFRAM_ID) {

			@Override
			public void start() {
				startListening();
			}
		};
		recognizer = new Recognizer(API_KEY);
		sndMngr = new SoundManager();
		say("System Started");
		loaded = true;
	}

	public void initMic() {
		microphone = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE) {

			@Override
			public void soundHeard(float lvl) {
				if (lvl > 0.1 && !checking && (!responding || action.isOther()) && loaded) {
					System.out.println("SoundHeard lvl - " + lvl);
					if (started) {
						micTimer.cancel();
						task = new TimerTask() {

							@Override
							public void run() {
								sndMngr.playStopListening();
								try {
									stop();
								} catch (IOException | InterruptedException e) {
									Speak.say("Error");
									e.printStackTrace();
								}
							}
						};
						micTimer = new Timer(true);
						micTimer.schedule(task, 1000);
					} else {
						checking = true;
						try {
							File audioFile = new File(audioFileName);
							if (audioFile.exists())
								audioFile.delete();
							microphone.captureAudioToFile(audioFileName);
						} catch (Exception e1) {
							e1.printStackTrace();
							say("Unable To Capture Audio to file");
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (checking) {
							microphone.close();
							microphone.open();
							checking = false;
							GoogleResponse gr = null;
							try {
								System.out.println("Pre Recognize");
								File audioFile = new File(audioFileName);
								File flacFile = new File(flacFileName);
								if (flacFile.exists())
									flacFile.delete();
								Utils.convertWavToFlac(audioFile, flacFile, SAMPLE_RATE);
								gr = recognizer.getRecognizedDataForFlac(flacFileName, 5, SAMPLE_RATE);
							} catch (Exception e1) {
								e1.printStackTrace();
								say("Unable to Recognize Sound");
							}
							System.out.println("Post Recognize");
							if (gr != null) {
								ArrayList<String> response = gr.getAllPossibleResponses();
								System.out.println("Responce - " + response.toString());
								for (String str : response)
									if (str != null && (Utils.includes(str, "computer") || Utils.includes(str, "pi") || Utils.includes(str, "Jarvis"))) {
										if (!started)
											startListening();
										break;
									}
							} else
								System.out.println("Responce - null");
						}
					}
				}
			}
		};
	}

	public void startListening() {
		if (started)
			return;
		started = true;
		checking = false;
		microphone.close();
		microphone.open();
		sndMngr.playListening();
		System.out.println("Started");
		try {
			File audioFile = new File(audioFileName);
			if (audioFile.exists())
				audioFile.delete();
			microphone.captureAudioToFile(audioFileName);
		} catch (Exception e1) {
			e1.printStackTrace();
			say("Unable To Capture Audio to file");
		}
		task = new TimerTask() {

			@Override
			public void run() {
				sndMngr.playStopListening();
				try {
					stop();
				} catch (IOException | InterruptedException e) {
					Speak.say("Error");
					e.printStackTrace();
				}
			}
		};
		micTimer = new Timer(true);
		micTimer.schedule(task, 5000);
	}

	public void stop() throws IOException, InterruptedException {
		if (responding && !action.isOther())
			return;
		System.out.println("Stopped");
		responding = true;
		action.setOther(false);
		started = false;
		microphone.close();
		microphone.open();
		System.out.println("Process");
		GoogleResponse gr = null;
		try {
			File flacFile = new File(flacFileName);
			File audioFile = new File(audioFileName);
			Utils.convertWavToFlac(audioFile, flacFile, SAMPLE_RATE);
			gr = recognizer.getRecognizedDataForFlac(flacFileName, 5, SAMPLE_RATE);
		} catch (Exception e1) {
			e1.printStackTrace();
			say("Unable to Recognize Sound");
		}
		System.out.println("Complete");
		if (gr != null) {
			ArrayList<String> response = gr.getAllPossibleResponses();
			System.out.println(response);
			if (!action.isOther()) {
				action(response);
				responding = false;
			} else {
				action.setSaid(response);
			}
		} else {
			System.out.println("null");
			if (action.isOther())
				action.setSaid(null);
		}
	}

	public void action(ArrayList<String> text) throws IOException, InterruptedException {
		int i = 0;
		boolean didSomething = false;
		while (i < text.size() && !didSomething) {
			didSomething = action.doSomething(text.get(i));
			i++;
		}
		if (!didSomething)
			say("I'm sorry I did not recognize that command");
	}

	public static void say(String text) {
		boolean restart = false;
		if (microphone != null) {
			restart = true;
			microphone.close();
		}
		Speak.say(text);
		if (restart)
			microphone.open();
	}
	
	private String [] readIDs() throws IOException {
		String [] ids = new String[2];
		BufferedReader read = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("ids.key")));
		ids[0] = read.readLine();
		ids[1] = read.readLine();
		read.close();
		return ids;
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}

}