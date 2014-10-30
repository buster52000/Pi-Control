package com.cicc.voiceCont;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javaFlacEncoder.FLACFileWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

import com.cicc.gpio.Color;
import com.cicc.gpio.LCDController;
import com.cicc.speech.Speak;
import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;
import com.darkprograms.speech.recognizer.Recognizer;

public class Main {
	private static MicrophoneAnalyzer microphone;
	private Recognizer recognizer;
	private GSpeechDuplex duplex;
	private GSpeechResponseListener micResponse;
	// private String audioFileName = "f.wav";
	private String flacFileName = "f.flac";
	private boolean started = false;
	private boolean responding = false;
	private boolean checking = false;
	private Timer micTimer;
	private TimerTask task;
	public static final String responces[] = { "I am listening", "Yes Sir?", "How may I help you?", "Yes Ryan?" };
	private SoundManager sndMngr;
	private Action action;
	public final String API_KEY;
	public final String WOLFRAM_ID;
	public final AudioFormat audioFormat;
	private boolean loaded = false;
	private final boolean testMode = false;
	private final boolean duplexEnabled = false;
	private boolean micWatcherEnabled = false;

	public static final int SAMPLE_RATE = (int) Microphone.getAudioFormat().getSampleRate();
	public static LCDController lcd = null;

	public Main() throws Exception {
		lcd = new LCDController(LCDController.LCD_MODE_WRITE);
		lcd.aquireLock();
		lcd.clear();
		lcd.setBacklight(Color.RED);
		lcd.setCursorPosition(0, 5);
		lcd.write("System");
		lcd.setCursorPosition(1, 4);
		lcd.write("Starting");
		lcd.releaseLock();
		Speak.say("System Starting");
		String[] IDs = readIDs();
		API_KEY = IDs[0];
		WOLFRAM_ID = IDs[1];
		audioFormat = Microphone.getAudioFormat();
		initMic();
		action = new Action(WOLFRAM_ID) {

			@Override
			public void start() {
				startListening();
			}
		};
		recognizer = new Recognizer(API_KEY);
		sndMngr = new SoundManager();
		lcd.aquireLock();
		lcd.clear();
		lcd.setCursorPosition(0, 5);
		lcd.write("System");
		lcd.setCursorPosition(1, 3);
		lcd.write("Started...");
		lcd.releaseLock();
		Speak.say("System Started");
		lcd.aquireLock();
		lcd.clear();
		lcd.setMode(LCDController.LCD_MODE_MAIN);
		lcd.releaseLock();
		loaded = true;
		if (duplexEnabled)
			startMicWatcher();
	}

	private void startMicWatcher() {
		if (micWatcherEnabled)
			return;
		micWatcherEnabled = true;
		microphone.close();
		try {
			duplex.recognize(microphone.getTargetDataLine(), audioFormat);
		} catch (IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	private void stopMicWatcher() {
		if (!micWatcherEnabled)
			return;
		micWatcherEnabled = false;
		microphone.getTargetDataLine().close();
	}

	public void initMic() {
		if (!testMode) {
			if (duplexEnabled) {
				duplex = new GSpeechDuplex(API_KEY);
				micResponse = new GSpeechResponseListener() {

					@Override
					public void onResponse(GoogleResponse gr) {
						if (gr != null) {
							ArrayList<String> responses = gr.getAllPossibleResponses();
							if (responses != null && responses.size() > 0)
								for (String res : responses) {
									res = res.toLowerCase();
									if (res.contains("computer") || res.contains("jarvis") || res.contains("pi") || res.contains("cuter") || res.contains("pewter") || res.contains("peter")) {
										stopMicWatcher();
										startListening();
									}
								}
						}
					}
				};
				duplex.addResponseListener(micResponse);
			}
			microphone = new MicrophoneAnalyzer(FLACFileWriter.FLAC) {

				@SuppressWarnings("unused")
				@Override
				public void soundHeard(float lvl) {
					if (lvl > 0.1) {
						if ((duplexEnabled && started) || (!checking && (!responding || action.isOther()) && loaded)) {
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
							} else if (!duplexEnabled) {
								checking = true;
								TargetDataLine tdl = microphone.getTargetDataLine();
								final AudioInputStream ais = new AudioInputStream(tdl);
								Thread thread = new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											Thread.sleep(1000);
											ais.close();
										} catch (InterruptedException | IOException e) {
											e.printStackTrace();
										}
									}

								});
								thread.start();
								GoogleResponse gr = null;
								try {
									gr = recognizer.getRecognizedDataForAudioStream(ais, 5, SAMPLE_RATE);
								} catch (IOException e2) {
									e2.printStackTrace();
									Speak.say("Unable to Recognize Sound");
								}
//								try {
//									File flacFile = new File(flacFileName);
//									if (flacFile.exists())
//										flacFile.delete();
//									microphone.captureAudioToFile(flacFile);
//								} catch (Exception e1) {
//									e1.printStackTrace();
//									Speak.say("Unable To Capture Audio to file");
//								}
//								try {
//									Thread.sleep(1000);
//								} catch (InterruptedException e) {
//									e.printStackTrace();
//								}
								microphone.close();
								microphone.open();
								checking = false;
//								GoogleResponse gr = null;
//								try {
//									System.out.println("Pre Recognize");
//									gr = recognizer.getRecognizedDataForFlac(flacFileName, 5, SAMPLE_RATE);
//								} catch (Exception e1) {
//									e1.printStackTrace();
//									Speak.say("Unable to Recognize Sound");
//								}
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
		} else {
			Thread testThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						if (loaded && !action.isOther()) {
							startListening();
						}
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}, "TestMode");
			testThread.start();
		}

	}

	private void startListening() {
		if (started)
			return;
		started = true;
		checking = false;
		lcd.aquireLock();
		lcd.setMode(LCDController.LCD_MODE_WRITE);
		lcd.clear();
		lcd.setCursorPosition(0, 2);
		lcd.write("Listening...");
		lcd.releaseLock();
		if (testMode) {
			try {
				stop();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}
		microphone.close();
		microphone.open();
		try {
			File flacFile = new File(flacFileName);
			if (flacFile.exists())
				flacFile.delete();
			sndMngr.playListening();
			System.out.println("Started");
			microphone.captureAudioToFile(flacFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			Speak.say("Unable To Capture Audio to file");
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

	private void stop() throws IOException, InterruptedException {
		if (responding && !action.isOther())
			return;
		GoogleResponse gr = null;
		responding = true;
		started = false;
		if (!testMode) {
			System.out.println("Stopped");
			microphone.close();
			microphone.open();
			System.out.println("Process");
			lcd.aquireLock();
			lcd.clear();
			lcd.setCursorPosition(0, 1);
			lcd.write("Processing....");
			lcd.releaseLock();
			try {
				gr = recognizer.getRecognizedDataForFlac(flacFileName, 5, SAMPLE_RATE);
			} catch (Exception e1) {
				e1.printStackTrace();
				Speak.say("Unable to Recognize Sound");
			}
			System.out.println("Complete");
			lcd.setMode(LCDController.LCD_MODE_MAIN);
		} else {
			gr = new GoogleResponse();
			gr.setResponse(JOptionPane.showInputDialog("Test mode - Please type text"));
		}
		if (gr != null) {
			ArrayList<String> response = gr.getAllPossibleResponses();
			System.out.println(response);
			if (!action.isOther()) {
				action(response);
				responding = false;
			} else {
				action.setSaid(response);
				action.setOther(false);
			}
		} else {
			System.out.println("null");
			if (action.isOther())
				action.setSaid(null);
		}
		if (duplexEnabled)
			startMicWatcher();
	}

	public void action(ArrayList<String> text) throws IOException, InterruptedException {
		int i = 0;
		boolean didSomething = false;
		while (i < text.size() && !didSomething) {
			didSomething = action.doSomething(text.get(i));
			i++;
		}
		if (!didSomething)
			Speak.say("I'm sorry I did not recognize that command");
	}

	private String[] readIDs() throws IOException {
		String[] ids = new String[2];
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