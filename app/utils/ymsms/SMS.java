package utils.ymsms;

import java.net.URLEncoder;

import business.TemplateSmsBlacklist;
import play.Play;
import utils.SMSV3;
import utils.ymsms.httpclient.SDKHttpClient;

/**
 * 
 * @description.  亿美短信
 *  
 * @modificationHistory.  
 * @author liulj 2017年1月11日上午9:55:11 TODO
 */
public class SMS {

	public static String sn = "8SDK-EMY-6699-RKZLL";// 软件序列号,请通过亿美销售人员获取
	public static String key = "fuck-you-sister";// 序列号首次激活时自己设定
	public static String password = "278015";// 密码,请通过亿美销售人员获取
	public static String baseUrl = "http://hprpt2.eucp.b2m.cn:8080/sdkproxy/";
	public static String sendMethod = "get";// 发送请求方式get / post
	
	public final static Integer SEND_TYPE_SYSTEM = 1; // 系统短信
	public final static Integer SEND_TYPE_MARKETING = 2; // 营销短信
	
	public final static String MESSAGE_KEY = "【亿亿理财】"; // 短信签名
	public final static String MESSAGE_MARKETING_KEY = "回Td退订"; // 营销短信后缀
	
	public Integer sendType = SEND_TYPE_SYSTEM;
	
	// 是否开启短信服务
	public final static String IS_SMS = Play.configuration.getProperty("is_sms", "1");
	
	public SMS(){}
	
	public SMS(Integer sendType){
		if(SEND_TYPE_SYSTEM.equals(sendType)){
			baseUrl = "http://hprpt2.eucp.b2m.cn:8080/sdkproxy/";
			sn = "8SDK-EMY-6699-RKZLL";
			password = "278015";
		}
		else if(SEND_TYPE_MARKETING.equals(sendType)){
			baseUrl = "http://sdktaows.eucp.b2m.cn:8080/sdkproxy/";
			sn = "6SDK-EMY-6666-RKWSQ";
			password = "281173";
		}
	}
	
	public static void send(String phone, String message, Integer sendType, Integer...retry){
		if(!TemplateSmsBlacklist.isMobileInSmsBlacklist(phone)) {
			SMSV3.send(phone, message, sendType, retry);
		}
		
		/*if("0".equals(IS_SMS)){
			return;
		}
		
		if(retry == null || retry.length == 0){
			retry = new Integer[]{1};
		}else{
			retry[0] += 1; 
		}
		
		if(retry[0] > 3){
			System.out.println(String.format("sms：【%s】,【%s】；发送失败，超过重试次数%s", phone, message, retry[0]));
			return;
		}
		
		baseUrl = "http://hprpt2.eucp.b2m.cn:8080/sdkproxy/";
		sn = "8SDK-EMY-6699-RKZLL";
		password = "278015";
		
		if(SEND_TYPE_MARKETING.equals(sendType)){
			baseUrl = "http://sdktaows.eucp.b2m.cn:8080/sdkproxy/";
			sn = "6SDK-EMY-6666-RKWSQ";
			password = "281173";
		}
		try {
			message = message.startsWith(MESSAGE_KEY) ? message : MESSAGE_KEY.concat(message);
			message = (SEND_TYPE_MARKETING.equals(sendType) && !message.endsWith(MESSAGE_MARKETING_KEY)) ? message.concat(MESSAGE_MARKETING_KEY) : message;
			
			String code = "";
			String param = "cdkey=" + sn + "&password=" + password + "&phone=" + phone + "&message=" + URLEncoder.encode(message, "UTF-8") + "&addserial=" + code + "&seqid=" + System.currentTimeMillis();
			String url = baseUrl + "sendsms.action";
			String ret = "";
			if ("get".equalsIgnoreCase(sendMethod)) {
				ret = SDKHttpClient.sendSMS(url, param);
			} else {
				ret = SDKHttpClient.sendSMSByPost(url, param);
			}
			
			System.out.println("发送结果:" + ret);
			if("0".equals(ret)){
				System.out.println(String.format("sms：【%s】,【%s】；发送成功", phone, message));
			}else{
				System.out.println(String.format("sms：【%s】,【%s】；发送失败【%s】", phone, message, ret));
				
				send(phone, message, sendType, retry[0]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(String.format("sms：【%s】,【%s】；发送失败【%s】", phone, message, e.getMessage()));
		}*/
	}

	public static void main(String[] args) {
		//new SMS().send("18758265108", "你妹");
		SMS.send("18758265108", "你妹", SMS.SEND_TYPE_MARKETING);
	}
}
