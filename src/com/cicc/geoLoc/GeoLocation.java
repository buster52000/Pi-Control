package com.cicc.geoLoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.cicc.tts.Speak;
import com.cicc.tts.Utils;
import com.smartechz.geoloc.Geobytes;

public class GeoLocation {

	public static final String NETWORKS_FILE_NAME = "networks.txt";
	public static final String GOOGLE_GEOLOC_URL = "https://maps.googleapis.com/maps/api/browserlocation/json?browser=true&sensor=true";
	public static final String GOOGLE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?";

	public static void main(String args[]) {
		Speak.say("Your current location is " + getLoc());
	}

	public static String getLoc() {
		String os = System.getProperty("os.name");
		ArrayList<WLANAdapter> adapters = new ArrayList<WLANAdapter>();
		StringBuilder req = new StringBuilder("");
		if (os.contains("Windows")) {
			ArrayList<String> netshOutput = null;
			try {
				ProcessBuilder builder = new ProcessBuilder("netsh");
				File f = new File(NETWORKS_FILE_NAME);
				if (f.exists())
					f.delete();
				builder.redirectOutput(f);
				URL url = ClassLoader.getSystemResource("input.txt");
				File input = null;
				try {
					input = new File(url.toURI());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				builder.redirectInput(input);
				Process pr = builder.start();
				pr.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				return null;
			}
			netshOutput = Utils.readFileToArray(new File(NETWORKS_FILE_NAME));
			while (netshOutput.contains(""))
				netshOutput.remove("");
			ArrayList<String> tmp = new ArrayList<String>();
			for (String str : netshOutput)
				tmp.add(str.replaceAll(" ", ""));
			netshOutput = tmp;
			int i = 0;
			while (i < netshOutput.size()) {
				if (netshOutput.get(i).startsWith("SSID")) {
					String ssid = null;
					String mac = null;
					int signal = 0;
					int channel = 0;
					ssid = netshOutput.get(i).replaceFirst("SSID\\d+\\:", "");
					while (!netshOutput.get(i).startsWith("BSSID"))
						i++;
					mac = netshOutput.get(i).replaceFirst("BSSID\\d+\\:", "");
					while (!netshOutput.get(i).startsWith("Signal"))
						i++;
					signal = Integer.parseInt(netshOutput.get(i).replaceFirst("Signal\\:", "").replaceAll("%", ""));
					while (!netshOutput.get(i).startsWith("Channel"))
						i++;
					channel = Integer.parseInt(netshOutput.get(i).replaceFirst("Channel\\:", ""));
					int strength = convertQualityToStrength(signal);
					int snr = calcSNR(strength);
					adapters.add(new WLANAdapter(ssid, mac, strength, snr, channel));
				}
				i++;
			}
			for (int j = 0; j < adapters.size(); j++) {
				WLANAdapter a = adapters.get(j);
				req.append("&wifi=");
				req.append("mac:" + a.getMacAddress().replaceAll("\\:", "-") + "|ss:" + a.getSignalStrength() + "|ssid:" + a.getSSID());
			}
		} else if (os.contains("Linux")) {

		} else {
			return ipBasedLoc();
		}
		try {
			String unparsedJsonResponce = rawRequest(req.toString(), GOOGLE_GEOLOC_URL);
			float[] latLng = parseGeoLocResponce(unparsedJsonResponce);
			System.out.println(latLng[0] + ", " + latLng[1]);
			String location = getLocation(latLng);
			return location;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ipBasedLoc();
	}

	private static int convertQualityToStrength(int quality) {
		int dBm;
		if (quality <= 0)
			dBm = -100;
		else if (quality >= 100)
			dBm = -50;
		else
			dBm = (quality / 2) - 100;
		return dBm;
	}

	private static int calcSNR(int signalStrength) {
		return 100 - signalStrength;
	}

	private static String ipBasedLoc() {
		return Geobytes.getMyLocation();
	}

	private static float[] parseGeoLocResponce(String responce) {
		String locationStr = Utils.substringBetween(responce, "\"location\" : {", "}");
		locationStr = locationStr.replaceAll(" ", "");
		String[] latLongStrArr = locationStr.split(",");
		latLongStrArr[0] = latLongStrArr[0].replaceAll("\"lat\"\\:", "");
		latLongStrArr[1] = latLongStrArr[1].replaceAll("\"lng\"\\:", "");
		float[] coords = new float[2];
		coords[0] = Float.parseFloat(latLongStrArr[0]);
		coords[1] = Float.parseFloat(latLongStrArr[1]);
		return coords;
	}

	private static String parseGeoCodeResponce(String unparsedJson) {
		String addressComponent = Utils.substringBetween(unparsedJson, "\"address_components\" : [", "],");
		addressComponent = addressComponent.replaceAll("\\s{2,}+", "");
		String[] parts = addressComponent.split("\\},\\{");
		String cityComponent = null;
		String stateComponent = null;
		for (String str : parts) {
			if (str.contains("locality"))
				cityComponent = str;
			else if (str.contains("administrative_area_level_1"))
				stateComponent = str;
		}
		String city = Utils.substringBetween(cityComponent, "\"long_name\" : \"", "\",");
		String state = Utils.substringBetween(stateComponent, "\"long_name\" : \"", "\",");
		return city + ", " + state;
	}

	private static String getLocation(float[] latLng) {
		String urlStr = GOOGLE_GEOCODE_URL + "latlng=" + latLng[0] + "," + latLng[1] + "&key=AIzaSyBNPKlUBJVETYCcicuOtqkUZ3j4LZuH2e4";
		try {
			String unparsedJson = rawRequest("", urlStr);
			return parseGeoCodeResponce(unparsedJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String rawRequest(String requestData, String urlStr) throws IOException {
		URL url;
		URLConnection urlConn;
		BufferedReader br;

		StringBuilder sb = new StringBuilder(urlStr);
		sb.append(requestData);

		// URL of Remote Script.
		url = new URL(sb.toString());

		// Open New URL connection channel.
		urlConn = url.openConnection();

		// we want to do output.
		urlConn.setDoOutput(true);

		// No caching
		urlConn.setUseCaches(false);

		// Get response data.
		br = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8")));
		BufferedWriter write = new BufferedWriter(new FileWriter(new File("output.json")));
		String response = br.readLine();
		System.out.println(response);
		write.write(response);
		write.newLine();
		boolean done = false;
		while (response != null && !done) {
			String tmp = br.readLine();
			if (tmp != null) {
				response += tmp;
				System.out.println(tmp);
				write.write(tmp);
				write.newLine();
			} else
				done = true;
		}
		br.close();
		write.close();

		return response;

	}

}
