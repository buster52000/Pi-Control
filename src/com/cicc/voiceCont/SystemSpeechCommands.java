package com.cicc.voiceCont;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.cicc.texttospeech.Speak;

public class SystemSpeechCommands extends SpeechResponce {

	private static final String[][] cancelArgs = { { "cancel" } };
	private static final String[][] helloArgs = { { "hello" } };
	private static final String[][] shutdownArgs = { { "shut down", "power off", "turn off" } };
	private static final String[][] rebootArgs = { { "reboot", "restart" } };
	private static final String[][] exitArgs = { { "speech recognition", "program" }, { "stop", "terminate", "disable", "exit" } };
	private static final String[][] timeArgs = { { "time" } };
	private static final String[][] testArgs = { { "test mode" } };

	private static final String[][][] paramArrays = { exitArgs, cancelArgs, helloArgs, shutdownArgs, rebootArgs, timeArgs, testArgs };

	public static void main(String args[]) {
		SystemSpeechCommands cmds = new SystemSpeechCommands() {

			@Override
			public ArrayList<String> speechRecognition() {
				return null;
			}
		};
		System.out.println(cmds.acceptableResponse("stop program"));
		System.out.println(cmds.acceptableResponse("stop progra"));
		System.out.println(cmds.acceptableResponse("hello"));
	}

	@Override
	public boolean acceptableResponse(String response) {
		if (response == null)
			return true;
		for (String[][] strArr : paramArrays)
			if (Utils.speechMatchesParams(response, strArr))
				return true;
		return false;
	}

	@Override
	public int getPriority() {
		return SpeechResponce.PRIORITY_HIGH;
	}

	@Override
	public void action(String text) {
		if (text == null) {
			Speak.say("Command Timed out");
			return;
		}
		for (int i = 0; i < paramArrays.length; i++)
			if (Utils.speechMatchesParams(text, paramArrays[i])) {
				switch (i) {
				case 0:
					Speak.say("Terminating Speech Recognition");
					System.exit(0);
					break;
				case 1:
					Speak.say("Cancel");
					break;
				case 2:
					Speak.say("Hello Ryan, How are you doing?");
					break;
				case 3:
					Speak.say("System Shutting Down");
					Utils.runCmd("sudo halt -p");
					break;
				case 4:
					Speak.say("System Rebooting");
					Utils.runCmd("sudo reboot");
					break;
				case 5:
					Utils.sayTime(true);
					break;
				case 6:
					while (true) {
						JOptionPane.showMessageDialog(null, "Click ok to start test");
						ArrayList<String> results = speechRecognition();
						System.out.println(results);
						for (String s : results)
							if (s.equalsIgnoreCase("exit"))
								return;
					}
				default:
					Speak.say("The response for this command has not been created");
					break;
				}
				return;
			}
		Speak.say("I'm sorry, something went wrong when I was trying to respond");
	}

}
