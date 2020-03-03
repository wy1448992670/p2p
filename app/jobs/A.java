/*
 * @(#)A.java 2017年3月6日上午9:30:14
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package jobs;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年3月6日上午9:30:14 TODO
 */

public class A {

	public static void main(String[] args) {
		System.out.println(String.format("%08d%ty%tm%td", 1, new Date(), new Date(), new Date()));
		System.out.println(String.format("%s%ty%tm%td-%s", 123, new Date(), new Date(), new Date(), RandomStringUtils.random(3, "abcdefghijklmnopqrstuvwxyz")));
	}
}
