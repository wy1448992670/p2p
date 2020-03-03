package utils;

import java.util.UUID;

import play.Logger;
import play.Play;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dahantc.api.sms.json.JSONHttpClient;

/**
 * 
 * @description.  大汉三通短信通道
 *  
 * @modificationHistory.  
 * @author liulj 2017年8月2日下午3:01:51 TODO
 */
public class SMSV3 {

	//private static final Logger LOG = Logger.getLogger(SMSV3.class);
	public static String account = "dh63921";// 用户名（必填）
	public static String password = "Kz1SM01U";// 密码（必填）
	//private static String phone = "18758265108"; // 手机号码（必填,多条以英文逗号隔开）
	public static String sign = "【亿亿理财】"; // 短信签名（必填）
	public static String subcode = ""; // 子号码（可选）
	public static String msgid = UUID.randomUUID().toString().replace("-", ""); // 短信id，查询短信状态报告时需要，（可选）
	public static String sendtime = ""; // 定时发送时间（可选）
	
	public final static Integer SEND_TYPE_SYSTEM = 1; // 系统短信
	public final static Integer SEND_TYPE_MARKETING = 2; // 营销短信
	
	// 是否开启短信服务
	public final static String IS_SMS = Play.configuration.getProperty("is_sms", "1");
	
	public SMSV3(){}
	
	/*public SMSV3(Integer sendType){
		if(SEND_TYPE_SYSTEM.equals(sendType)){
			account = "dh63921";
			password = "Kz1SM01U";
		}
		else if(SEND_TYPE_MARKETING.equals(sendType)){
			account = "dh63922";
			password = "Bz8GKjpr";
		}
	}*/
	
	public static void send(String phone, String message, Integer sendType, Integer...retry){
		
		if("0".equals(IS_SMS))return;
		
		// 重发机制
		if(retry == null || retry.length == 0){
			retry = new Integer[]{1};
		}else{
			retry[0] += 1; 
		}
		
		if(retry[0] > 3){
			Logger.info(String.format("大汉sms：【%s】,【%s】；发送失败，超过重试次数%s", phone, message, retry[0]));
			return;
		}

		if(SEND_TYPE_SYSTEM.equals(sendType)){
			account = "dh63921";
			password = "Kz1SM01U";
		}
		else if(SEND_TYPE_MARKETING.equals(sendType)){
			account = "dh63922";
			password = "Bz8GKjpr";
		}
		
		try {
			JSONHttpClient jsonHttpClient = new JSONHttpClient("http://www.dh3t.com");
			jsonHttpClient.setRetryCount(1);
			JSONObject result = JSON.parseObject(jsonHttpClient.sendSms(account, password, phone, message, sign, subcode));
			
			Logger.info(String.format("大汉【%s】sms：【%s】,【%s】；大汉结果【%s】", account, phone, message, result));
			if(!"0".equals(result.get("result"))){
				send(phone, message, sendType, retry[0]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.info(String.format("大汉sms：【%s】,【%s】；发送失败【%s】", phone, message, e.getMessage()));
		}
	}

	public static void main(String[] args) {
		SMSV3.send("15068811192", "这是一条营销短信", SEND_TYPE_MARKETING);
	}
}
