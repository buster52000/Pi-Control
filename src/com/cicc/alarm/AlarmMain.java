package com.cicc.alarm;
import javax.swing.JOptionPane;

public class AlarmMain {

	public static void main(String args[]) {
		int hour = 0;
		int minute = 0;
		if (args.length == 2) {
			hour = Integer.parseInt(args[0]);
			minute = Integer.parseInt(args[1]);
		} else {
			String[] s1 = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
					"11", "12" };
			String[] s2 = new String[60];
			for (int i = 0; i < 60; i++)
				s2[i] = Integer.toString(i);
			String[] s3 = { "AM", "PM" };
			String hourS = null;
			String minS = null;
			String am = null;
			while (hourS == null)
				hourS = (String) JOptionPane.showInputDialog(null,
						"Set the hour for the Alarm", "Set Hour",
						JOptionPane.QUESTION_MESSAGE, null, s1, "12");
			while (minS == null)
				minS = (String) JOptionPane.showInputDialog(null,
						"Set the minute for the Alarm", "Set Minute",
						JOptionPane.QUESTION_MESSAGE, null, s2, "0");
			while (am == null)
				am = (String) JOptionPane.showInputDialog(null,
						"Set time of day for the Alarm", "Set AM/PM",
						JOptionPane.QUESTION_MESSAGE, null, s3, "AM");
			hour = Integer.parseInt(hourS);
			minute = Integer.parseInt(minS);
			hour = hour == 12 ? 0 : hour;
			hour = am.equals("PM") ? hour += 12 : hour;
				
		}

		Controller cont = new Controller(hour, minute);
		cont.start();
	}

}
