package com.cicc.voiceCont;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer {

	private static final int EXTERNAL_BUFFER_SIZE = 128000;
	private static Mixer mixer = null;

	public static void playSound(final String name, boolean wait) {

		if (wait)
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
		if(mixer == null)
			for(Mixer.Info info : AudioSystem.getMixerInfo())
				if(info.getName().contains("default")) {
					mixer = AudioSystem.getMixer(info);
					break;
				}
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(AudioPlayer.class.getResourceAsStream(name)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		AudioFormat audioFormat = audioInputStream.getFormat();

		SourceDataLine line = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try {
			line = (SourceDataLine) mixer.getLine(info);

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