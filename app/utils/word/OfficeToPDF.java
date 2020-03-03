/*
 * @(#)OfficeToPDF.java 2017年5月10日下午1:56:54
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package utils.word;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

import play.Play;
import utils.NumberUtil;
  
public class OfficeToPDF {  
      
    /**  
     * 将Office文档转换为PDF. 运行该函数需要用到OpenOffice, OpenOffice下载地址为  
     * http://www.openoffice.org/  
     *   
     * <pre>  
     * 方法示例:  
     * String sourcePath = "F:\\office\\source.doc";  
     * String destFile = "F:\\pdf\\dest.pdf";  
     * Converter.office2PDF(sourcePath, destFile);  
     * </pre>  
     *   
     * @param sourceFile  
     *            源文件, 绝对路径. 可以是Office2003-2007全部格式的文档, Office2010的没测试. 包括.doc,  
     *            .docx, .xls, .xlsx, .ppt, .pptx等. 示例: F:\\office\\source.doc  
     * @param destFile  
     *            目标文件. 绝对路径. 示例: F:\\pdf\\dest.pdf  
     * @return 操作成功与否的提示信息. 如果返回 -1, 表示找不到源文件, 或url.properties配置错误; 如果返回 0,  
     *         则表示操作成功; 返回1, 则表示转换失败  
     */    
    public static int office2PDF(String sourceFile, String destFile) throws FileNotFoundException {    
        try {    
            File inputFile = new File(sourceFile);    
            if (!inputFile.exists()) {    
                return -1;// 找不到源文件, 则返回-1    
            }    
    
            // 如果目标路径不存在, 则新建该路径    
            File outputFile = new File(destFile);    
            if (!outputFile.getParentFile().exists()) {    
                outputFile.getParentFile().mkdirs();    
            }
            String portStr=Play.configuration.getProperty("openoffice.port");
            int port=8888;//2019-05-09 zqq 生产环境是8888端口
            if(NumberUtil.isNumeric(portStr)) {
            	port=Integer.parseInt(portStr);
            }
            // connect to an OpenOffice.org instance running on port 8100   
            OpenOfficeConnection connection = new SocketOpenOfficeConnection("127.0.0.1", port);    
            connection.connect();    
    
            // convert    
            DocumentConverter converter = new OpenOfficeDocumentConverter(    
                    connection);    
            converter.convert(inputFile, outputFile);    
    
            // close the connection    
            connection.disconnect();    
    
            return 0;    
        } catch (Exception e) {    
            e.printStackTrace();    
        }    
    
        return 1;    
    }    
  
    public static void main(String[] args) throws FileNotFoundException {
		office2PDF("D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.doc", "D:/yiyilc/2017/20170502电子合同需求与标的图片问题/借款协议20170508-无标注版.doc.pdf");
	}
}  