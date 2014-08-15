package com.cicc.crypt;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class MainCrpt {

	private Crypto crypt;
	private FileMngr mngr;
	private char[] pass;
	public static boolean exit = false;

	public static void main(String args[]) {
		MainCrpt mainCrpt = new MainCrpt();
		if (!exit)
			mainCrpt.runWithUI();
	}

	public static void run() {
		MainCrpt mainCrpt = new MainCrpt();
		if (!exit)
			mainCrpt.runWithUI();
	}

	public MainCrpt() {
		mngr = new FileMngr();
		if (!mngr.masterExists()) {
			JPasswordField pf = new JPasswordField();
			Object[] obj = { "Set Master Password", pf };
			JOptionPane.showMessageDialog(null, obj);
			pass = pf.getPassword();
			String pwd = "";
			for (char c : pf.getPassword())
				pwd += c;
			mngr.setMaster(Crypto.cryptMaster(pwd));
		} else {
			JPasswordField pf = new JPasswordField();
			Object[] obj = { "Enter Master Password", pf };
			JOptionPane.showMessageDialog(null, obj);
			pass = pf.getPassword();
			String pwd = "";
			for (char c : pf.getPassword())
				pwd += c;
			if (!Crypto.checkMaster(pwd, mngr.getMaster())) {
				JOptionPane.showMessageDialog(null, "Incorrect Password");
				exit = true;
			}
		}
		if (!exit)
			crypt = new Crypto(pass);
	}

	public void runWithUI() {
		String[] test = { "Add new password", "Decrypt Passwords", "Cancel" };
		int i = -1;
		while (i != 2) {
			i = JOptionPane.showOptionDialog(null, "What would you like to do?", "Title", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, test, "Cancel");
			if (i == 2)
				return;
			if (i == 0) {
				JPasswordField pf = new JPasswordField();
				Object[] obj = { "Enter New Password", pf };
				JOptionPane.showMessageDialog(null, obj);
				pass = pf.getPassword();
				String pwd = "";
				for (char c : pf.getPassword())
					pwd += c;
				String des = JOptionPane.showInputDialog("Enter Password Description");
				mngr.addPassword(crypt.encrypt(pwd), crypt.encrypt(des));
			} else if (i == 1) {
				showPwds();
			} else
				return;
		}
	}

	public void addPwd(String pwd, String description) {
		mngr.addPassword(crypt.encrypt(pwd), crypt.encrypt(description));
	}

	public void showPwds() {
		ArrayList<String[]> pwds = mngr.getPasswords();
		for (String[] strs : pwds) {
			strs[0] = crypt.decrypt(strs[0], pass);
			strs[1] = crypt.decrypt(strs[1], pass);
		}
		String arr[] = new String[pwds.size()];
		int i = 0;
		for (String[] strs : pwds) {
			arr[i] = strs[1] + " - " + strs[0];
			i++;
		}
		JOptionPane.showMessageDialog(null, arr);
	}

}
