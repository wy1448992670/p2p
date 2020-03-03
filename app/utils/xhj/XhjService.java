/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.xhj
 *
 *    Filename:    XhjService.java
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
 *    Create at:   2018年12月26日 下午4:57:35
 *
 *    Revision:
 *
 *    2018年12月26日 下午4:57:35
 *        - first revision
 *
 *****************************************************************/
package utils.xhj;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import models.t_interface_call_record;
import play.Logger;
import play.Play;
import services.business.CreditApplyService;
import services.business.InterfaceCallRecord;
import utils.DateUtil;
import utils.OrderNoFactory;

public class XhjService {
	static String RCVCODE = "-0000";

	/**
	 * @Description 星护甲查询个人征信基本信息
	 * @param personName
	 * @param idNumber
	 * @param mobileNo
	 * @author: zj
	 */
	public static Map<String, String> getPersonsCreditInfo(String personName, String idNumber, String mobileNo) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> header = new HashMap<String, Object>();
		Map<String, Object> body = new HashMap<String, Object>();
		Map<String, String> data = new HashMap<String, String>();
		header.put("reqFlag", 0);
		header.put("userName", Play.configuration.getProperty("xhj.username"));
		header.put("source", "server");
		header.put("reqTransID", OrderNoFactory.getNo());
		header.put("reqDateTime", DateUtil.dateToString(new Date()));
		header.put("reqDate", DateUtil.dateToString(new Date()));

		body.put("idNumber", idNumber);
		body.put("personName", personName);
		body.put("mobileNo", mobileNo);

		params.put("header", header);

		try {
			params.put("body", RSAUtil.encrypt(Play.configuration.getProperty("zyy.xhj.privateKey"),
					JSONObject.toJSONString(body)));
			Logger.info("星护甲requestData:【%s】", JSONObject.toJSONString(params));
			data.put("reqData", JSONObject.toJSONString(body));
			
			String responseMsg = XhjDao.getPersonsCreditInfo(JSONObject.toJSONString(params));
			JSONObject object = JSON.parseObject(responseMsg);
			JSONObject responseHeader = object.getJSONObject("header");
			String rcvCode = responseHeader.getString("rcvCode");
			data.put("respCode", rcvCode);
			if (rcvCode.equals(RCVCODE)) {// 如果请求成功才解析body内容，否则会报错
				String encryptBody = object.getString("body");
				// 返回解密后的body内容
				String responseData = RSAUtil.decrypt(Play.configuration.getProperty("xhj.publicKey"), encryptBody);
				Logger.info("星护甲responseData:【%s】", responseData);
				data.put("respData", responseData);
				return data;
			} else {
				JSONObject errorBody = object.getJSONObject("body");
				data.put("respData", errorBody.toJSONString());
				Logger.info("星护甲接口返回结果信息==========>" + errorBody.getString("errorMsg"));
				Logger.info("星护甲接口返回结果code==========>" + errorBody.getString("errorCode"));
				return data;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * @Description 星护甲个人休息地验证
	 * @param personName
	 * @param idNumber
	 * @param mobileNo
	 * @param workOrRestAddress 休息地详细地址（推荐的格式为省+市+区+街道或小区楼号，<br>
	 *                          如：浙江省杭州市西湖区古墩路1号。注意，不可出现两个“-”符号）
	 * @return 返回空则表示查询失败
	 * @author: zj
	 */
	public static String rtAddrVer(String personName, String idNumber, String mobileNo, String workOrRestAddress) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> header = new HashMap<String, Object>();
		Map<String, Object> body = new HashMap<String, Object>();
		header.put("reqFlag", 0);
		header.put("userName", Play.configuration.getProperty("xhj.username"));
		header.put("source", "server");
		header.put("reqTransID", OrderNoFactory.getNo());
		header.put("reqDateTime", DateUtil.dateToString(new Date()));
		header.put("reqDate", DateUtil.dateToString(new Date()));

		body.put("idNumber", idNumber);
		body.put("personName", personName);
		body.put("mobileNo", mobileNo);
		body.put("workOrRestAddress", workOrRestAddress);
		params.put("header", header);

		try {
			params.put("body", RSAUtil.encrypt(Play.configuration.getProperty("zyy.xhj.privateKey"),
					JSONObject.toJSONString(body)));
			Logger.info("居住地requestData:【%s】", JSONObject.toJSONString(params));
			String responseMsg = XhjDao.rtAddrVer(JSONObject.toJSONString(params));
			JSONObject object = JSON.parseObject(responseMsg);
			JSONObject responseHeader = object.getJSONObject("header");
			String rcvCode = responseHeader.getString("rcvCode");
			if (rcvCode.equals(RCVCODE)) {// 如果请求成功才解析body内容，否则会报错
				String encryptBody = object.getString("body");
				// 返回解密后的body内容
				String responseData = RSAUtil.decrypt(Play.configuration.getProperty("xhj.publicKey"), encryptBody);
				Logger.info("居住地responseData:【%s】", responseData);
				return responseData;
			} else {
				JSONObject errorBody = object.getJSONObject("body");
				Logger.info("星护甲休息地验证接口返回结果信息==========>" + errorBody.getString("errorMsg"));
				Logger.info("星护甲休息地验证接口返回结果code==========>" + errorBody.getString("errorCode"));
				return errorBody.toJSONString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage(), e);
			return null;
		}
		// Logger.info("返回的解密后的征信信息============>" + encryptBody);
		// return XhjDao.getPersonsCreditInfo(JSONObject.toJSONString(params));

	}
	
	/**
	 * @Description 通用分
	 * @param idNumber  身份证号码
	 * @param mobileNo  手机号码
	 * @return
	 * @author: zj
	 */
	public static String getTdCommonScore(String idNumber, String mobileNo) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> header = new HashMap<String, Object>();
		Map<String, Object> body = new HashMap<String, Object>();
		header.put("reqFlag", 0);
		header.put("userName", Play.configuration.getProperty("xhj.username"));
		header.put("source", "server");
		header.put("reqTransID", OrderNoFactory.getNo());
		header.put("reqDateTime", DateUtil.dateToString(new Date()));
		header.put("reqDate", DateUtil.dateToString(new Date()));

		body.put("idNumber", idNumber);
		body.put("mobileNo", mobileNo);
		params.put("header", header);

		try {
			params.put("body", RSAUtil.encrypt(Play.configuration.getProperty("zyy.xhj.privateKey"),
					JSONObject.toJSONString(body)));
			Logger.info("同盾requestData:【%s】", JSONObject.toJSONString(params));
			String responseMsg = XhjDao.getTdCommonScore(JSONObject.toJSONString(params));
			JSONObject object = JSON.parseObject(responseMsg);
			JSONObject responseHeader = object.getJSONObject("header");
			String rcvCode = responseHeader.getString("rcvCode");
			if (rcvCode.equals(RCVCODE)) {// 如果请求成功才解析body内容，否则会报错
				String encryptBody = object.getString("body");
				// 返回解密后的body内容
				String responseData = RSAUtil.decrypt(Play.configuration.getProperty("xhj.publicKey"), encryptBody);
				Logger.info("同盾responseData:【%s】", responseData);
				return responseData;
			} else {
				JSONObject errorBody = object.getJSONObject("body");
				Logger.info("星护甲同盾分接口返回结果信息==========>" + errorBody.getString("errorMsg"));
				Logger.info("星护甲同盾分接口返回结果code==========>" + errorBody.getString("errorCode"));
				return errorBody.toJSONString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e.getMessage(), e);
			return null;
		}

	}
	public static void main(String[] args) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("idNumber", "3123123123");
		body.put("personName", "312的舒服撒地方");
		body.put("mobileNo", "3123213123");
		System.out.println(JSONObject.toJSONString(body));
	}
}
