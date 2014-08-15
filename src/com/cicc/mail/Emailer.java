package com.cicc.mail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Emailer {

	public static final String from = "piControl@up-tech.net";

	public static void main(String args[]) throws IOException, InterruptedException {
		File email = new File("email.txt");
		if (email.exists())
			email.delete();
		BufferedWriter write = new BufferedWriter(new FileWriter(email));
		write.write("Hello Pi Control User,");
		write.newLine();
		write.write("	Please go to www.google.com/device and enter the code \"aknernaile\" to verify your google account.");
		write.newLine();
		write.write("Thank you");
		write.close();
		sendMail("buster52000@gmail.com", "Google OAuth User Code", email);
	}

	public static void sendMail(String sendTo, String subject, File messageFile) throws IOException, InterruptedException {
		String cmd = "";
		if (System.getProperty("user.name").equals("pi"))
			cmd = "mail -s \"" + subject + "\" -a \"FROM: " + from + "\" " + sendTo + " < " + messageFile.getName();
		else
			cmd = "mail -s \"" + subject + "\" -r " + from + " " + sendTo + " < " + messageFile.getName();
		File run = new File("run.sh");
		if (run.exists())
			run.delete();
		BufferedWriter write = new BufferedWriter(new FileWriter(run));
		write.write(cmd);
		write.close();
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("sh " + run.getName());
		pr.waitFor();
	}

}