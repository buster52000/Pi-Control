package com.cicc.alpha;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {

	public static String getPlaintextFromXML(Document doc) {
		NodeList list = doc.getElementsByTagName("plaintext");
		if(list.getLength() > 0) {
			Node node = list.item(0);
			return node.getTextContent();
		}
		return null;
	}
	
}
