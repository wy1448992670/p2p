/*
 * @(#)Test.java 2017年5月9日下午4:54:22
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package utils.word;

import java.awt.Dimension;
import java.io.IOException;

import org.jsoup.Jsoup;

import gui.ava.html.image.generator.HtmlImageGenerator;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年5月9日下午4:54:22 TODO
 */

public class Test {

	public static void main(String[] args) throws Exception {
		HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
		imageGenerator.loadUrl("http://www.baidu.com"); 
        imageGenerator.saveAsImage("d:/hello-world.png");  
        imageGenerator.saveAsHtmlWithMap("hello-world.html", "hello-world.png");
	}
}
