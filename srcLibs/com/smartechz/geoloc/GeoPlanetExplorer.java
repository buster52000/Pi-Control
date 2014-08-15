package com.smartechz.geoloc;

import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoPlanetExplorer {
	public static String getWOEID(String start) {
		String res = null;
		try {
			String urlStr = "http://isithackday.com/geoplanet-explorer/index.php?start=" + URLEncoder.encode(start, "UTF-8");
			URL url = new URL(urlStr);

			String regwoeid = "woeid=[0-9]+";
			Pattern woeidPtrn = Pattern.compile(regwoeid);
			String line;

			StreamReader in = HTTPRequest.getStreamReader(url);
			while (in.hasNextLine()) {
				line = in.nextLine();
				Matcher woeidMatchr = woeidPtrn.matcher(line);
				if (woeidMatchr.find()) {
					res = woeidMatchr.group(0).replaceFirst("woeid=", "");
					break;
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
