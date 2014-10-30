package com.cicc.voiceCont;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.cicc.gpio.Color;
import com.cicc.gpio.LCDController;
import com.cicc.speech.Speak;

public class SystemSpeechCommands extends SpeechResponce {

	private static final String[][] cancelArgs = { { "cancel" } };
	private static final String[][] helloArgs = { { "hello", "good morning" } };
	private static final String[][] shutdownArgs = { { "shut down", "power off", "turn off", "shutdown" } };
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
				LCDController lcd = Main.lcd;
				lcd.aquireLock();
				lcd.setMode(LCDController.LCD_MODE_WRITE);
				lcd.clear();
				switch (i) {
				case 0:
					lcd.setCursorPosition(0, 4);
					lcd.write("Stopping");
					lcd.setCursorPosition(1, 3);
					lcd.write("Program...");
					Speak.say("Terminating Speech Recognition");
					lcd.clear();
					lcd.setBacklight(Color.OFF);
					lcd.stop();
					System.exit(0);
					break;
				case 1:
					lcd.setCursorPosition(0, 5);
					lcd.write("Cancel");
					Speak.say("Cancel");
					break;
				case 2:
					lcd.setCursorPosition(0, 3);
					lcd.write("Hello Ryan");
					lcd.setCursorPosition(1, 2);
					lcd.write("How are you?");
					Speak.say("Hello Ryan, How are you doing?");
					break;
				case 3:
					lcd.setCursorPosition(0, 2);
					lcd.write("Powering Off");
					Speak.say("System Shutting Down");
					lcd.clear();
					lcd.setBacklight(Color.OFF);
					lcd.stop();
					Utils.runCmd("sudo halt -p");
					System.exit(0);
					break;
				case 4:
					lcd.setCursorPosition(0, 2);
					lcd.write("Rebooting...");
					Speak.say("System Rebooting");
					lcd.clear();
					lcd.setBacklight(Color.OFF);
					lcd.stop();
					Utils.runCmd("sudo reboot");
					System.exit(0);
					break;
				case 5:
					lcd.setMode(LCDController.LCD_MODE_MAIN);
					Utils.sayTime(true);
					break;
				case 6:
					boolean exit = false;
					while (!exit) {
						JOptionPane.showMessageDialog(null, "Click ok to start test");
						ArrayList<String> results = speechRecognition();
						System.out.println(results);
						for (String s : results)
							if (s.equalsIgnoreCase("exit"))
								exit = true;
					}
				default:
					Speak.say("The response for this command has not been created");
					break;
				}
				lcd.setMode(LCDController.LCD_MODE_MAIN);
				lcd.releaseLock();
				return;
			}
		Speak.say("I'm sorry, something went wrong when I was trying to respond");
	}

}
