package utils;

import org.w3c.dom.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.*;

public class XmlUtil {
	Document doc;

	public XmlUtil setDocument(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			xml = xml.trim();
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes("utf-8"));
			docBuilder = factory.newDocumentBuilder();
			doc = docBuilder.parse(inputStream);
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

		return this;
	}

	public int getCount(String NodeName) {
		NodeList list = doc.getDocumentElement().getElementsByTagName(NodeName);
		int count = list.getLength();
		
		return count;
	}

	public String getNodeValue(String NodeName) {
		NodeList list = doc.getDocumentElement().getElementsByTagName(NodeName);
		
		if (list == null || list.getLength() <= 0) {
			return "";
		}

		return (list.item(0).getFirstChild().getNodeValue() == null ? "" : list.item(0).getFirstChild().getNodeValue());
	}
}
