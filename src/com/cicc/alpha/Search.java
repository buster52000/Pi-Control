package com.cicc.alpha;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;

public class Search {

	public final String baseURL = "http://api.wolframalpha.com/v2/query?format=plaintext&podtitle=Result&appid=";
	public final String input = "&input=";
	public final String fullURL;
	private String apiID;
	private APICaller apiCaller;

	public Search(String apiID) {
		this.apiID = apiID;
		fullURL = baseURL + this.apiID + input;
		apiCaller = new APICaller();
	}

	public String getPlaintextForQuery(String query) {
		query = query.replaceAll(" ", "+");
		String plaintext = null;
		try {
			Document doc = apiCaller.getXMLDocForURL(new URL(fullURL+query));
			if(doc == null)
				return "I'm sorry I don't know the answer to that";
			plaintext = XMLParser.getPlaintextFromXML(doc);
			if(plaintext == null || plaintext.length() == 0)
				plaintext = "I'm sorry I don't know the answer to that";
			System.out.println(plaintext);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return plaintext;
	}

}
