package com.smartechz.geoloc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HTTPRequest {
	private static InputStream getInputStream(URL url)
			throws MalformedURLException, IOException {
		URLConnection gb = url.openConnection();
		return gb.getInputStream();
	}

	public static StreamReader getStreamReader(URL url)
			throws MalformedURLException, IOException {
		return new StreamReader(getInputStream(url));
	}

	public static String getString(URL url) throws MalformedURLException,
			IOException {
		StringBuffer sb = new StringBuffer();
		StreamReader in = HTTPRequest.getStreamReader(url);
		if (in.hasNextLine()) {
			sb.append(in.nextLine());
			while (in.hasNextLine()) {
				sb.append("\n");
				sb.append(in.nextLine());
			}
		}
		in.close();
		return sb.toString();
	}

	public static Document getXMLDoc(URL url)
			throws MalformedURLException, IOException,
			ParserConfigurationException, SAXException {
		InputStream ip = HTTPRequest.getInputStream(url);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(ip);
		doc.getDocumentElement().normalize();

		return doc;
	}

}
