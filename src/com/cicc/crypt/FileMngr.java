package com.cicc.crypt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileMngr {

	private File passwords, master, descriptions;

	public FileMngr() {
		try {
			initFiles();
			if (!passwords.exists()) {
				passwords.createNewFile();
			}
			if (!descriptions.exists()) {
				passwords.createNewFile();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initFiles() {
		passwords = new File("passwords.pwd");
		master = new File("master.pwd");
		descriptions = new File("description.pwd");
	}
	
	public String getMaster() {
		if (!master.exists())
			return null;
		String pwd = null;
		try {
			BufferedReader read = new BufferedReader(new FileReader(master));
			pwd = read.readLine();
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return pwd;
	}

	public boolean masterExists() {
		return master.exists();
	}
	
	public void setMaster(String pwd) {
		try {
			if (master.exists()) {
				master.delete();
			}
			passwords.delete();
			passwords.createNewFile();
			descriptions.delete();
			descriptions.createNewFile();
			master.createNewFile();
			BufferedWriter write = new BufferedWriter(new FileWriter(master));
			write.write(pwd);
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addPassword(String pwd, String des) {
		try {
			BufferedWriter writePwd = new BufferedWriter(new FileWriter(passwords, true));
			writePwd.write(pwd);
			writePwd.newLine();
			writePwd.close();
			BufferedWriter writeDes = new BufferedWriter(new FileWriter(descriptions, true));
			writeDes.write(des);
			writeDes.newLine();
			writeDes.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String[]> getPasswords() {
		ArrayList<String[]> pwds = new ArrayList<String[]>();
		try {
			initFiles();
			BufferedReader readPwd = new BufferedReader(new FileReader(passwords));
			BufferedReader readDes = new BufferedReader(new FileReader(descriptions));
			String tmp;
			while((tmp = readPwd.readLine()) != null) {
				if(tmp != null && !tmp.equals("")) {
					String tmp2 = readDes.readLine();
					String [] pwdDes = {tmp, tmp2};
					pwds.add(pwdDes);
				}
			}
			readPwd.close();
			readDes.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return pwds;
	}

}
