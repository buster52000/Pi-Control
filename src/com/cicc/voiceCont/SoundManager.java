package com.cicc.voiceCont;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

public class SoundManager {
	
	private String[] sounds = new String[2];
	private AudioInputStream in;
	private Clip clip;

	public SoundManager() {
		sounds[0] = "listening.wav";
		sounds[1] = "notListening.wav";
	}

	public void playListening() {
		playSound(0, true);
	}

	public void playStopListening() {
		playSound(1, false);
	}

	public void playSound(int i, boolean wait) {
		AudioPlayer.playSound(sounds[i], wait);
	}

	public void closeClip() {
		if (clip != null && clip.isOpen()) {
			clip.close();
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
