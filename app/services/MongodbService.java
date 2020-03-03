/******************************************************************
 *
 *    Java Lib For China, Powered By Chinese Programmer.
 *
 *    Copyright (c) 2001-2099 Digital Telemedia Co.,Ltd
 *    http://www.china.com/
 *
 *    Package:     services
 *
 *    Filename:    MongodbDao.java
 *
 *    Description: give you  a little color see see  
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2019年5月7日 上午9:07:37
 *
 *    Revision:
 *
 *    2019年5月7日 上午9:07:37
 *        - first revision
 *
 *****************************************************************/
package services;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import utils.DateUtil;
import utils.MongoDBUtil;

/**
 * @ClassName MongodbDao
 * @这里用一句话描述这个方法的作用
 * @author zj
 * @Date 2019年5月7日 上午9:07:37
 * @version 1.0.0
 */
public class MongodbService {

	/**
	 * 将用户的运营商报告完整的数据存在mongodb 日后备用
	 * 
	 * @这里用一句话描述这个方法的作用
	 * @param mxData
	 * @param userId
	 * @author: zj
	 */
	public static void addMongodb(String mxData, long userId) {
		MongoDatabase database = MongoDBUtil.getConnect();
		MongoCollection<Document> docment = database.getCollection("t_report_mxdata");
		Map<String, Object> map = new HashMap<>();
		map.put("user_id", userId);
		map.put("mxdata", mxData);
		map.put("create_time", DateUtil.getDateTime());
		map.put("update_time", DateUtil.getDateTime());
		Document document = new Document(map);
		docment.insertOne(document);
	}

}
