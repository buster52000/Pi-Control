package com.cicc.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.cicc.mail.Emailer;
import com.cicc.speech.Speak;
import com.cicc.voiceCont.Utils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class OAuthHandler {

	public static final String TOKEN_FILE_NAME = "accessToken.key";

	public static final String OAUTH_TOKEN_REQUEST_URL = "https://accounts.google.com/o/oauth2/device/code";
	public static final String OAUTH_TOKEN_REQUEST_BODY = "client_id=999213704247-k6hd0gar0te2e7ot7pn0apl4h9uco7ee.apps.googleusercontent.com&scope=https://www.googleapis.com/auth/calendar%20email";
	public static final String OAUTH_TOKEN_CHECK_URL = "https://accounts.google.com/o/oauth2/token";
	public static final String OAUTH_TOKEN_CHECK_BODY = "client_id=999213704247-k6hd0gar0te2e7ot7pn0apl4h9uco7ee.apps.googleusercontent.com&client_secret=Pn1cFdRrNrcepopidyyrRjWu&grant_type=http://oauth.net/grant_type/device/1.0&code=";
	public static final String OAUTH_TOKEN_REFRESH_URL = "https://accounts.google.com/o/oauth2/token";
	public static final String OAUTH_TOKEN_REFRESH_BODY = "client_id=999213704247-k6hd0gar0te2e7ot7pn0apl4h9uco7ee.apps.googleusercontent.com&client_secret=Pn1cFdRrNrcepopidyyrRjWu&grant_type=refresh_token&refresh_token=";

	private long expireTime;
	private String accessToken, refreshToken, email;

	public OAuthHandler(String email) throws Exception {
		loadTokenFile();
		this.email = email;
		if (email == null || email.length() == 0)
			throw new NullPointerException("Email param cannot be null or empty");
		else if (!email.contains("@"))
			throw new Exception("\"" + email + "\" is not an email address");
	}

	public Credential getCredentials() throws IOException, InterruptedException {
		File tokenFile = new File(TOKEN_FILE_NAME);

		if (!tokenFile.exists() || refreshToken == null)
			requestUserAuth();
		else if (System.currentTimeMillis() >= expireTime - 10000 || accessToken == null)
			refreshAccessToken();

		if (accessToken == null) {
			Speak.say("Error, unable to authorize with the google api");
			return null;
		}

		return new GoogleCredential().setAccessToken(accessToken).setExpirationTimeMilliseconds(expireTime);
	}

	private void requestUserAuth() throws IOException, InterruptedException {
		Speak.say("User authentication required");
		accessToken = null;
		String response = oAuthUrlRequest(OAUTH_TOKEN_REQUEST_URL, OAUTH_TOKEN_REQUEST_BODY);
		response = response.replaceAll(" ", "");
		String deviceCode = Utils.substringBetween(response, "\"device_code\":\"", "\",");
		String userCode = Utils.substringBetween(response, "\"user_code\":\"", "\",");
		String verifyUrl = Utils.substringBetween(response, "\"verification_url\":\"", "\",");
		int expiresIn = Integer.parseInt(Utils.substringBetween(response, "\"expires_in\":", ","));
		long checkExpireTime = System.currentTimeMillis() + expiresIn * 1000;
		int interval = Integer.parseInt(Utils.substringBetween(response, "\"interval\":", "}"));
		File email = new File("email.txt");
		if (email.exists())
			email.delete();
		BufferedWriter write = new BufferedWriter(new FileWriter(email));
		write.write("Hello Pi Control User,");
		write.newLine();
		write.write("	Please go to " + verifyUrl + " and enter the code \"" + userCode + "\" to verify your google account.");
		write.newLine();
		write.write("Thank you");
		write.close();
		Emailer.sendMail(this.email, "Google OAuth User Code", email);
		String[] arr = { "An email has been sent to " + email, "Please follow the instructions in the email" };
		Speak.say(arr);
		System.out.println("Go to : " + verifyUrl);
		System.out.println("Enter : " + userCode);
		System.out.println("In the next " + expiresIn / 60 + " minutes");
		while (System.currentTimeMillis() <= checkExpireTime - 10000 && accessToken == null) {
			response = oAuthUrlRequest(OAUTH_TOKEN_CHECK_URL, OAUTH_TOKEN_CHECK_BODY + deviceCode);
			response = response.replaceAll(" ", "");
			if (!response.startsWith("{\"error\"")) {
				accessToken = Utils.substringBetween(response, "\"access_token\":\"", "\"");
				expireTime = System.currentTimeMillis() + Long.parseLong(Utils.substringBetween(response, "\"expires_in\":", ","));
				refreshToken = Utils.substringBetween(response, "\"refresh_token\":\"", "\"}");
				saveTokenFile();
				Speak.say("A User has been Authorized");
			} else if (Utils.substringBetween(response, "\"error\":\"", "\",") != null && Utils.substringBetween(response, "\"error\":\"", "\",").equals("access_denied")) {
				Speak.say("The user has denied access to their account");
				break;
			}
			Thread.sleep(interval * 1000);
		}
	}

	private void refreshAccessToken() throws IOException {
		accessToken = null;
		String response = oAuthUrlRequest(OAUTH_TOKEN_REFRESH_URL, OAUTH_TOKEN_REFRESH_BODY + refreshToken);
		response = response.replaceAll(" ", "");
		if (response != null && !response.startsWith("{\"error\"")) {
			accessToken = Utils.substringBetween(response, "\"access_token\":\"", "\"");
			try {
				expireTime = System.currentTimeMillis() + (Long.parseLong(Utils.substringBetween(response, "\"expires_in\":", "}")) * 1000);
			} catch (NumberFormatException e) {
				expireTime = System.currentTimeMillis() + (Long.parseLong(Utils.substringBetween(response, "\"expires_in\":", ",")) * 1000);
			}
			saveTokenFile();
		}
	}

	private String oAuthUrlRequest(String requestUrl, String requestBody) throws IOException {
		URLConnection urlConn;
		InputStream in;
		URL url = new URL(requestUrl);
		urlConn = url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setDoInput(true);
		urlConn.setUseCaches(false);
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		DataOutputStream writer = new DataOutputStream(urlConn.getOutputStream());
		writer.writeBytes(requestBody);
		writer.flush();
		writer.close();
		in = urlConn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));

		String response = br.readLine();
		boolean done = false;
		while (response != null && !done) {
			String tmp = br.readLine();
			if (tmp != null) {
				response += tmp;
			} else
				done = true;
		}
		br.close();
		return response;
	}

	private void saveTokenFile() throws IOException {
		File tokenFile = new File(TOKEN_FILE_NAME);
		if (tokenFile.exists())
			tokenFile.delete();
		BufferedWriter write = new BufferedWriter(new FileWriter(tokenFile));
		write.write(Long.toString(expireTime));
		write.newLine();
		write.write(accessToken);
		write.newLine();
		write.write(refreshToken);
		write.close();
	}

	private void loadTokenFile() throws NumberFormatException, IOException {
		File tokenFile = new File(TOKEN_FILE_NAME);
		if (!tokenFile.exists()) {
			accessToken = null;
			refreshToken = null;
			expireTime = 0;
			return;
		}
		BufferedReader read = new BufferedReader(new FileReader(tokenFile));
		expireTime = Long.parseLong(read.readLine());
		accessToken = read.readLine();
		refreshToken = read.readLine();
		read.close();
	}

	public int getExpiresInSeconds() {
		long millisLeft = expireTime - System.currentTimeMillis();
		if (millisLeft < 0)
			return 0;
		return (int) (millisLeft / 1000);
	}

}
