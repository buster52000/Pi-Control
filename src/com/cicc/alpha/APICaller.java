package com.cicc.alpha;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

public class APICaller {

	private File file;

	public APICaller() {
		file = new File("wolfram.xml");
	}

	public Document getXMLDocForURL(URL url) {
		if (file.exists())
			file.delete();
		download(url);
		if (!file.exists())
			return null;
		return convertFileToXMLDoc();
	}

	private File download(URL url) {
		file = null;
		BufferedInputStream bis = null;
		try {
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");

			String contentType = urlConn.getContentType();

			System.out.println("contentType:" + contentType);

			InputStream is = urlConn.getInputStream();
			bis = new BufferedInputStream(is, 4 * 1024);
			file = new File("wolfram.xml");
			FileUtils.copyInputStreamToFile(bis, file);
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	private Document convertFileToXMLDoc() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
