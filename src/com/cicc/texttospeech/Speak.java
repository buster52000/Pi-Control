package com.cicc.texttospeech;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.apache.commons.io.FileUtils;

public class Speak {

	public static boolean silent = false;
	public static final String TTS_URL = "http://translate.google.com/translate_tts?tl=en&q=";
	
	public static void main(String args[]) {
		say("");
	}

	public static void say(String[] toSay) {
		ArrayList<String> list = new ArrayList<String>();
		for (String str : toSay)
			list.add(str);
		say(list);
	}

	public static void say(ArrayList<String> toSay) {
		if (silent)
			return;
		if (toSay == null || toSay.size() == 0) {
			say("No text in speak request");
			return;
		}
		while (toSay.contains(null))
			toSay.remove(null);
		ArrayList<File> files = new ArrayList<File>();
		try {
			for (int i = 0; i < toSay.size(); i++) {
				String strUrl = TTS_URL + URLEncoder.encode(toSay.get(i), "UTF-8");
				URL url = new URL(strUrl);
				files.add(download(url, i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (files == null || files.size() == 0 || files.contains(null)) {
			System.err.println("Unable to retrieve tts mp3 from google");
			return;
		}

		try {
			ArrayList<Player> players = new ArrayList<Player>();
			for (File file : files)
				players.add(new Player(new FileInputStream(file)));
			for (Player pl : players) {
				pl.play();
			}
			for (Player pl : players)
				pl.close();
			for (File file : files)
				file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}

	}

	public static void say(String text) {
		if (silent)
			return;
		if (text == null || text.equals("")) {
			say("No text in speak request");
			return;
		}

		ArrayList<File> files = new ArrayList<File>();
		ArrayList<String> toSay = new ArrayList<String>();
		if (text.length() > 100) {
			int numOfReqs = (int) Math.ceil(text.length() / 100.0);
			for (int i = 0; i < numOfReqs - 1; i++) {
				char character = '*';
				int charNum = 100;
				while (character != ' ') {
					character = text.charAt(charNum);
					if (character != ' ')
						charNum--;
				}
				String tmp = text.substring(0, charNum);
				text = text.replaceFirst(tmp, "");
				toSay.add(tmp);
			}
			toSay.add(text);
		} else {
			toSay.add(text);
		}
		try {
			for (int i = 0; i < toSay.size(); i++) {
				String strUrl = TTS_URL + URLEncoder.encode(toSay.get(i), "UTF-8");
				URL url = new URL(strUrl);
				files.add(download(url, i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (files == null || files.size() == 0 || files.contains(null)) {
			System.err.println("Unable to retrieve tts mp3 from google");
			return;
		}

		try {
			ArrayList<Player> players = new ArrayList<Player>();
			// Runtime rt = Runtime.getRuntime();
			for (File file : files) {
				// try {
				// Process pr = rt.exec("mpg123 "+file.getName());
				// pr.waitFor();
				// } catch (IOException | InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
				players.add(new Player(new FileInputStream(file)));
			}
			for (Player pl : players) {
				pl.play();
			}
			for (Player pl : players)
				pl.close();
			for (File file : files)
				file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		}

	}

	private static File download(URL url, int fileNum) {
		File file = null;
		BufferedInputStream bis = null;
		try {
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

			String contentType = urlConn.getContentType();

			System.out.println("contentType:" + contentType);

			InputStream is = urlConn.getInputStream();
			bis = new BufferedInputStream(is, 4 * 1024);
			file = new File("tts" + fileNum + ".mp3");
			FileUtils.copyInputStreamToFile(bis, file);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void enableSilent() {
		silent = true;
	}

	public static void disableSilent() {
		silent = false;
	}

	public static boolean isSilent() {
		return silent;
	}

}
