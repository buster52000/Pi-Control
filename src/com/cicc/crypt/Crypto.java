package com.cicc.crypt;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

public class Crypto {

	private char[] password;
	private BasicTextEncryptor crypter;
	private static StrongPasswordEncryptor pwCrypt = new StrongPasswordEncryptor();
	
	public Crypto(char[] password) {
		this.password = password;
		crypter = new BasicTextEncryptor();
		crypter.setPasswordCharArray(password);
	}
	
	public static String cryptMaster(String master) {
		return pwCrypt.encryptPassword(master);
	}
	
	public static boolean checkMaster(String pass, String encryptedPass) {
		return pwCrypt.checkPassword(pass, encryptedPass);
	}
	
	public String encrypt(String str) {
		return crypter.encrypt(str);
	}
	
	public String decrypt(String str, char[] password) {
		if(this.password.length != password.length)
			return null;
		for(int i = 0; i < password.length; i++)
			if(password[i] != this.password[i])
				return null;
		return crypter.decrypt(str);
	}
	
}
