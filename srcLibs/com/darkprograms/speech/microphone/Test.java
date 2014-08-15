package com.darkprograms.speech.microphone;

import javax.sound.sampled.AudioFileFormat;

public class Test {

	public static boolean started = false;
	public static MicrophoneAnalyzer mic;
	
	public static void main(String[] args) {
		mic = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE) {
			
			@Override
			public void soundHeard(float lvl) {
				if(!started) {
					try {
						mic.captureAudioToFile("f.wav");
					} catch (Exception e) {
						e.printStackTrace();
					}
					started = true;
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					mic.close();
//					System.exit(0);
				}
				
			}
		};
	}
}
