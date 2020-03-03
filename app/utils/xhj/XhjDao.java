/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.xhj
 *
 *    Filename:    XhjDao.java
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
 *    Create at:   2018年12月26日 下午4:24:14
 *
 *    Revision:
 *
 *    2018年12月26日 下午4:24:14
 *        - first revision
 *
 *****************************************************************/
package utils.xhj;

import com.alibaba.fastjson.JSONObject;

import play.Play;
import utils.net.HttpUtil;

/**
 * @ClassName XhjDao
 * @Description 星护甲查询接口封装
 * @author zj
 * @Date 2018年12月26日 下午4:24:14
 * @version 1.0.0
 */
public class XhjDao {

	
	/**
	 * @Description 个人征信信息查询
	 * @param jsonStr
	 * @return
	 * @author: zj
	 */
	public static String getPersonsCreditInfo(String jsonStr) {
		JSONObject jsonObject = HttpUtil.doPost(Play.configuration.getProperty("xhj.url"), jsonStr);
		return jsonObject.toString();
	}
	
	/**
	 * @Description 休息地验证
	 * @param jsonStr
	 * @return
	 * @author: zj
	 */
	public static String rtAddrVer(String jsonStr) {
		JSONObject jsonObject = HttpUtil.doPost(Play.configuration.getProperty("xhj.xxdyz.url"), jsonStr);
		return jsonObject.toString();
	}
	
	/**
	 * @Description 通用分
	 * @param jsonStr
	 * @return
	 * @author: zj
	 */
	public static String getTdCommonScore(String jsonStr) {
		JSONObject jsonObject = HttpUtil.doPost(Play.configuration.getProperty("xhj.tyf.url"), jsonStr);
		return jsonObject.toJSONString();
	}
}
