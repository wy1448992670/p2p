package payment.ips.util;
import org.w3c.dom.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.parsers.*;


public class XmlUtil {
	
	DocumentBuilderFactory factory;
	DocumentBuilder docBuilder;
	Document doc;
	public NodeList nodeList;

	public XmlUtil() {
	}

	public void SetDocument(String xml)
	{
		
		try
		{
			  factory = DocumentBuilderFactory.newInstance();
			  factory.setValidating(false);
			  docBuilder = factory.newDocumentBuilder();
			  xml = xml.trim();
			 
			
			  InputStream inputStream = new ByteArrayInputStream(xml.getBytes("utf-8"));//xml为要解析的字符串
			  docBuilder = factory.newDocumentBuilder();
			  doc = docBuilder.parse(inputStream);
			  
			 
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
		
	}
	                
	public int getNodeListCount(String NodeName)
	{
		NodeList list = doc.getDocumentElement().getElementsByTagName(NodeName);
	    int count = list.getLength();
	    return count;
	}
	
	public String getNodeValue(String NodeName)
	{
		
		try{
		NodeList list = doc.getDocumentElement().getElementsByTagName(NodeName);
		if(list == null || list.getLength() <= 0)
		{
			return "";
		}
	
	    String txt = list.item(0).getFirstChild().getNodeValue()== null ? "":list.item(0).getFirstChild().getNodeValue();

	    return txt;
		}catch(Exception ex)
		{
			System.out.println(ex.toString());
			return "";
		}
	    
	}
	
	
	public String getNodeXml(String... NodeNames)
	{
		java.lang.StringBuilder sb = new java.lang.StringBuilder();
				
		try{
			NodeList list  = doc.getDocumentElement().getChildNodes();
			System.out.println(list.getLength());
			
			if(list.getLength() >  0)
			{
				for(int i=0;i<list.getLength();i++){
					if(!RemoveChild(list.item(i).getNodeName(),NodeNames)){
					String txt = getNodeValue(list.item(i).getNodeName());
					if(txt=="")
						sb.append("<"+list.item(i).getNodeName()+"></"+list.item(i).getNodeName()+">");
					else
						sb.append("<"+list.item(i).getNodeName()+">"+txt+"</"+list.item(i).getNodeName()+">");
					
					System.out.println(sb.toString().trim());
					}
				}
				
			}
		String txt = sb.toString();
		
	    return txt;
		}catch(Exception ex)
		{
			System.out.println(ex.toString());
			return "";
		}
	    
	}
	
	private boolean RemoveChild(String NodeName,String... NodeNames)
	{
		for(int i=0;i<NodeNames.length;i++)
		{
			if(NodeName.equals(NodeNames[i]))
				return true;
			else
				return false;
		}
		return false;
	}
	
	public NodeList getNodeList(String NodeName)
	{
		NodeList list = doc.getDocumentElement().getElementsByTagName(NodeName);
	    return list;
	}
	
	

}
