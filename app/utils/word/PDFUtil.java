/*
 * @(#)PDFUtil.java 2017年5月9日下午2:06:09
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package utils.word;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年5月9日下午2:06:09 TODO
 */

public class PDFUtil {

	public static final String SRC = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170510001.pdf";
    public static final String DEST = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170510001.pdf.pdf";
	/*public static final String SRC = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.pdf";
    public static final String DEST = "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.2.pdf";*/

    public static void main(String[] args) throws IOException, DocumentException {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        new PDFUtil().manipulatePdf(SRC, DEST);
    }
 
    public static void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        
        for (int i=1; i<=reader.getNumberOfPages(); i++){
        	PdfDictionary dict = reader.getPageN(i);
        	PdfObject object = dict.getDirectObject(PdfName.CONTENTS);
        	if (object instanceof PRStream) {
        		PRStream stream = (PRStream)object;
        		byte[] data = PdfReader.getStreamBytes(stream);
        		System.out.println(new String(data));
        		stream.setData(new String(data).replace("NAME", "HELLO WOR").getBytes());
        	}
        }  
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
        stamper.close();
        reader.close();
        System.out.println("success");
    }
}
