/*
 * @(#)Html2Pdf.java 2017年5月9日下午3:56:53
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package utils.word;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年5月9日下午3:56:53 TODO
 */

public class Html2Pdf {
	public static final String DEST = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.doc.html.pdf" ;
    public static final String HTML = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.doc.html";
 
    /**
     * Creates a PDF with the words "Hello World"
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void createPdf(String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document();
        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        // step 3
        document.open();
        // step 4
        XMLWorkerHelper.getInstance().parseXHtml(writer, document, new FileInputStream(HTML), Charset.forName("UTF-8"));
        // step 5
        document.close();
    }
 
    /**
     * Main method
     */
    public static void main(String[] args) throws IOException, DocumentException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        new Html2Pdf().createPdf(DEST);
    }
  
}
