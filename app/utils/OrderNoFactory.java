/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils
 *
 *    Filename:    OrderCodeFactory.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月26日 下午5:25:19
 *
 *    Revision:
 *
 *    2018年12月26日 下午5:25:19
 *        - first revision
 *
 *****************************************************************/
package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * @ClassName OrderCodeFactory
 * @Description 生成订单号
 * @author zj
 * @Date 2018年12月26日 下午5:25:19
 * @version 1.0.0
 */
public class OrderNoFactory {
	public static String getNo() {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Calendar calendar = Calendar.getInstance();
		String dateName = df.format(calendar.getTime());

		Random ne = new Random();// 实例化一个random的对象ne
		int x = ne.nextInt(9999 - 1000 + 1) + 1000;// 为变量赋随机值1000-9999
		String random_order = String.valueOf(x);
		String order_id = dateName + random_order;
		return order_id;
	}

	public static void main(String[] args) {
		System.out.println(getNo());
	}
}
