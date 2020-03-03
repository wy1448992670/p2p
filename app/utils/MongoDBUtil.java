/******************************************************************
 *
 *    Java Lib For China, Powered By Chinese Programmer.
 *
 *    Copyright (c) 2001-2099 Digital Telemedia Co.,Ltd
 *    http://www.china.com/
 *
 *    Package:     utils
 *
 *    Filename:    MongoDB.java
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
 *    Create at:   2019年4月28日 上午10:48:15
 *
 *    Revision:
 *
 *    2019年4月28日 上午10:48:15
 *        - first revision
 *
 *****************************************************************/
package utils;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import play.Play;

/**
 * MongoDB工具类 Mongo实例代表了一个数据库连接池，即使在多线程的环境中，一个Mongo实例对我们来说已经足够了<br>
 * 注意Mongo已经实现了连接池，并且是线程安全的。 <br>
 * 设计为单例模式， 因 MongoDB的Java驱动是线程安全的，对于一般的应用，只要一个Mongo实例即可，<br>
 * Mongo有个内置的连接池（默认为10个） 对于有大量写和读的环境中，为了确保在一个Session中使用同一个DB时，<br>
 * DB和DBCollection是绝对线程安全的<br>
 * 
 * @author zj
 * @version 0.0.0
 */
public class MongoDBUtil {

	static String port = Play.configuration.getProperty("mongodb.port");
	static String username = Play.configuration.getProperty("mongodb.username");
	static String password = Play.configuration.getProperty("mongodb.password");
	static String databaseName = Play.configuration.getProperty("mongodb.databaseName");
	static String host = Play.configuration.getProperty("mongodb.host");

	// 需要密码认证方式连接
	public static MongoDatabase getConnect() {
		List<ServerAddress> adds = new ArrayList<>();
		// ServerAddress()两个参数分别为 服务器地址 和 端口
		ServerAddress serverAddress = new ServerAddress(host, Integer.parseInt(port));
		adds.add(serverAddress);

		List<MongoCredential> credentials = new ArrayList<>();
		// MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
		MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(username, databaseName,
				password.toCharArray());
		credentials.add(mongoCredential);

		// 通过连接认证获取MongoDB连接
		MongoClient mongoClient = new MongoClient(adds, credentials);

		// 连接到数据库
		MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

		// 返回连接数据库对象
		return mongoDatabase;
	}

	public static void main(String[] args) {
		System.out.println(MongoDBUtil.getConnect());
	}
}