package com.cicc.voiceCont;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

	private static final int EXTERNAL_BUFFER_SIZE = 128000;
	
	public static void playSound(final String name, boolean wait) {

		if(wait)
			play(name);
		else {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					play(name);
				}
			});
			thread.start();
		}

	}
	
	private static void play(String name) {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(AudioPlayer.class.getResourceAsStream(name));
		} catch (Exception e) {
			e.printStackTrace();
		}

		AudioFormat audioFormat = audioInputStream.getFormat();

		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);

			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		line.start();

		int nBytesRead = 0;
		byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];
		while (nBytesRead != -1) {
			try {
				nBytesRead = audioInputStream.read(abData, 0, abData.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (nBytesRead >= 0) {
				line.write(abData, 0, nBytesRead);
			}
		}

		line.drain();
		line.close();
	}
	
}