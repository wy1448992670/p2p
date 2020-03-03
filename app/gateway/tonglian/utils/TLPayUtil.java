package gateway.tonglian.utils;

import gateway.tonglian.base.PropertyConfig;
import gateway.tonglian.bean.TOrder;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import constants.Constants;
import models.t_mmm_data;
import models.t_return_data;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;

/**
 * 常用工具函数
 * 
 * @author yuy
 * @date:2015-05-15 15:23:05
 * @version :1.0
 *
 */
public class TLPayUtil {
	/**
	 * str空判断
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isnull(String str) {
		if (null == str || str.equalsIgnoreCase("null") || str.equals("")) {
			return true;
		} else
			return false;
	}

	/**
	 * 获取当前时间str，格式yyyyMMddHHmmss
	 * 
	 * @return
	 */
	public static String getCurrentDateTimeStr() {
		SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date();
		String timeString = dataFormat.format(date);
		return timeString;
	}

	/**
	 * 生成待签名串
	 * 
	 * @param paramMap
	 * @return
	 */
	public static String genSignData(JSONObject jsonObject, String[] sortedArray) {
		StringBuffer content = new StringBuffer();

		for (int i = 0; i < sortedArray.length; i++) {
			String key = sortedArray[i];
			String value = jsonObject.getString(key);
			// 空串不参与签名
			if (isnull(value)) {
				continue;
			}
			content.append((i == 0 ? "" : "&") + key + "=" + value);

		}
		String signSrc = content.toString();
		if (signSrc.startsWith("&")) {
			signSrc = signSrc.replaceFirst("&", "");
		}
		return signSrc;
	}

	/**
	 * 加签
	 * 
	 * @param reqObj
	 * @param rsa_private
	 * @param md5_key
	 * @return
	 */
	public static String addSign(JSONObject reqObj, String md5_key, String[] sortedKeyArray) {
		if (reqObj == null) {
			return "";
		}
		return addSignMD5(reqObj, md5_key, sortedKeyArray);
	}

	/**
	 * 签名验证
	 * 
	 * @param reqStr
	 * @return
	 */
	public static boolean checkSign(String reqStr, String md5_key, String[] sortedKeyArray) {
		JSONObject reqObj = JSONObject.fromObject(reqStr);
		if (reqObj == null) {
			return false;
		}
		return checkSignMD5(reqObj, md5_key, sortedKeyArray);
	}

	public static String MD5Encode(String aData) throws SecurityException {
		String resultString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = bytes2HexString(md.digest(aData.getBytes("UTF-8")));
		} catch (Exception e) {
			Logger.error(e, log("MD5运算出现异常"));
			throw new SecurityException("MD5运算失败");
		}
		return resultString;
	}

	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}

	/**
	 * MD5签名验证
	 * 
	 * @param signSrc
	 * @param sign
	 * @return
	 */
	private static boolean checkSignMD5(JSONObject reqObj, String md5_key, String[] sortedKeyArray) {
		if (reqObj == null) {
			return false;
		}
		String sign = reqObj.getString("signMsg");
		String orderNo = reqObj.getString("orderNo");
		// 生成待签名串
		String sign_src = genSignData(reqObj, sortedKeyArray);
		Logger.info(log("[订单:" + orderNo + "]MD5签名验证，待签名原串：" + sign_src));
		Logger.info(log("[订单:" + orderNo + "]签名串：" + sign));
		sign_src += "&key=" + md5_key;
		try {
			if (sign.equals(MD5Encode(sign_src))) {
				Logger.info(log("[订单:" + orderNo + "]MD5签名验证通过"));
				return true;
			} else {
				Logger.info(log("[订单:" + orderNo + "]MD5签名验证未通过"));
				return false;
			}
		} catch (Exception e) {
			Logger.error("[订单:" + reqObj.getString("orderNo") + "]MD5签名验证异常" + e.getMessage());
			return false;
		}
	}

	/**
	 * MD5加签名
	 * 
	 * @param reqObj
	 * @param md5_key
	 * @return
	 */
	private static String addSignMD5(JSONObject reqObj, String md5_key, String[] sortedKeyArray) {
		if (reqObj == null) {
			return "";
		}
		String orderNo = reqObj.getString("orderNo");
		// 生成待签名串
		String sign_src = genSignData(reqObj, sortedKeyArray);
		Logger.info(log("[订单:" + orderNo + "]MD5加签名，加签原串：" + sign_src));
		sign_src += "&key=" + md5_key;
		try {
			return MD5Encode(sign_src);
		} catch (Exception e) {
			Logger.error(log("[订单:" + orderNo + "]MD5加签名异常" + e.getMessage()));
			return "";
		}
	}

	/**
	 * @param str
	 * @return
	 */
	public static String log(String str) {
		return "[" + PropertyConfig.name + "]：" + str;
	}
	
	public static void saveRequestLog(String mmmUserId, String mark, String type, TOrder order){
		JPAUtil.transactionBegin();
		t_mmm_data t_mmm_data = new t_mmm_data();
		t_mmm_data.mmmUserId = mmmUserId;
		t_mmm_data.orderNum = order.orderNo;
		t_mmm_data.parent_orderNum = order.orderNo;
		t_mmm_data.op_time = new Date();
		t_mmm_data.msg = mark;
		t_mmm_data.data = JSONObject.fromObject(order).toString();
		t_mmm_data.status = 1;
		t_mmm_data.type = type;		
		t_mmm_data.url = order.receiveUrl;
		t_mmm_data.save();
		JPAUtil.transactionCommit();
	}
	
	/**
	 * 更新请求日志状态，status=2
	 * @param orderNum
	 * @param error
	 */
	public static void updateRequestLog(String orderNum, ErrorInfo error){
		error.clear();
		
		String sql = "UPDATE t_mmm_data SET status = 2 WHERE orderNum = ? AND status = 1";
		
		int row = 0;
		
		try{
			row = JPA.em().createNativeQuery(sql).setParameter(1, orderNum).executeUpdate();
		}catch(Exception e){
			Logger.error("更新请求日志状态时，%s", e.getMessage());
			error.code = -1;
			error.msg = "更新请求日志状态异常";
			
			JPA.setRollbackOnly();
			return;
		}
		
		if(row == 0){
			Logger.info("更新请求日志状态时，%s", "已执行");
			error.code = Constants.ALREADY_RUN;
			error.msg = "更新请求日志状态时，已执行";
			
			JPA.setRollbackOnly();
			return;
		}	
	}
	
	public static void saveCallBackLog(Map<String, String> paramMap){
		JPAUtil.transactionBegin();
		t_return_data t_return_data = new t_return_data();
//		t_return_data.mmmUserId = t_mmm_data.get("mmmUserId") == null ? "" : t_mmm_data.get("mmmUserId").toString();
		t_return_data.orderNum = paramMap.get("orderNo") ;
//		t_return_data.parent_orderNum = paramMap.get("parentOrderno");
		t_return_data.op_time = new Date();
//		t_return_data.type = type;
		t_return_data.data = JSONObject.fromObject(paramMap).toString();				
		t_return_data.save();
		JPAUtil.transactionCommit();
	}
}
