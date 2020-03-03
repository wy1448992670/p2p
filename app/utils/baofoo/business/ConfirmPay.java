/**
 * Company: www.baofu.com
 * @author dasheng(大圣)
 * @date 2018年3月14日
 */
package utils.baofoo.business;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.print.PrintException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import utils.baofoo.rsa.RsaCodingUtil;
import utils.baofoo.rsa.SignatureUtils;
import utils.baofoo.util.FormatUtil;
import utils.baofoo.util.HttpUtil;
import utils.baofoo.util.Log;
import utils.baofoo.util.SecurityUtil;

public class ConfirmPay{
	/**
	 * 确认支付
	 * @param args
	 * @throws Exception
	 */
	public static Map<String, String> execute(String uniqueKey, String SMSStr) throws Exception {
		String send_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//报文发送日期时间		
        
        
        //String SMSStr="123456";//短信验证码，测试环境随机6位数;生产环境验证码预绑卡成功后发到用户手机。确认绑卡时回传。
        
        //String AesKey = "4f66405c4f66405c";//商户自定义（可随机生成  商户自定义(AES key长度为=16位)）
		String dgtl_envlp = "01|"+BaofooAPCons.AesKey;//使用接收方的公钥加密后的对称密钥，并做Base64转码，明文01|对称密钥，01代表AES[密码商户自定义]
		
		Log.Write("密码dgtl_envlp："+dgtl_envlp);		
		dgtl_envlp = RsaCodingUtil.encryptByPubCerFile(SecurityUtil.Base64Encode(dgtl_envlp), BaofooAPCons.cerpath);//公钥加密	
		String UniqueCode = "20180314105623013031195826960370|"+SMSStr;//预支付唯一码(预支付返回的值)[格式：预支付一码|短信验证码]
		UniqueCode = String.format("%s|%s", uniqueKey, SMSStr);
		Log.Write("预支付唯一码："+UniqueCode);	
		UniqueCode = SecurityUtil.AesEncrypt(SecurityUtil.Base64Encode(UniqueCode), BaofooAPCons.AesKey);//先BASE64后进行AES加密
		Log.Write("AES[UniqueCode]结果:"+UniqueCode);
		
		
		String CardInfo="";//信用卡（格式）：信用卡有效期|安全码；借记卡：传空
		
		//暂不支持信用卡
		//CardInfo = SecurityUtil.AesEncrypt(SecurityUtil.Base64Encode(CardInfo), AesKey);//先BASE64后进行AES加密
		

		Map<String,String> DateArry = new TreeMap<String,String>();
		DateArry.put("send_time", send_time);
		DateArry.put("msg_id", "TISN"+System.currentTimeMillis());//报文流水号
		DateArry.put("version", "4.0.0.0");
		DateArry.put("terminal_id", BaofooAPCons.terminal_id);
		DateArry.put("txn_type", "06");//交易类型
		DateArry.put("member_id", BaofooAPCons.member_id);
		DateArry.put("dgtl_envlp", dgtl_envlp);
		DateArry.put("unique_code", UniqueCode);//预支付唯一码
		DateArry.put("card_info", CardInfo);//卡信息
		
		String SignVStr = FormatUtil.coverMap2String(DateArry);
		Log.Write("SHA-1摘要字串："+SignVStr);
		String signature = SecurityUtil.sha1X16(SignVStr, "UTF-8");//签名
		Log.Write("SHA-1摘要结果："+signature);		
		String Sign = SignatureUtils.encryptByRSA(signature, BaofooAPCons.pfxpath, BaofooAPCons.priKeyPass);
		Log.Write("RSA签名结果："+Sign);		
		DateArry.put("signature", Sign);//签名域
			
		 String PostString  = HttpUtil.RequestForm(BaofooAPCons.POST_URL, DateArry);	
		 Log.Write("请求返回:"+PostString);
			
		 Map<String, String> ReturnData = FormatUtil.getParm(PostString);
			
		 if(!ReturnData.containsKey("signature")){
			 throw new Exception("缺少验签参数！");
		 }
		 String RSign=ReturnData.get("signature");
		 Log.Write("返回的验签值："+RSign);
		 ReturnData.remove("signature");//需要删除签名字段		
		 String RSignVStr = FormatUtil.coverMap2String(ReturnData);
		 Log.Write("返回SHA-1摘要字串："+RSignVStr);
		 String RSignature = SecurityUtil.sha1X16(RSignVStr, "UTF-8");//签名
		 Log.Write("返回SHA-1摘要结果："+RSignature);
			
		 if(SignatureUtils.verifySignature(BaofooAPCons.cerpath,RSignature,RSign)){
			 Log.Write("Yes");//验签成功
		 }
		 if(!ReturnData.containsKey("resp_code")){
			 throw new Exception("缺少resp_code参数！");
		 }
		 
		 if(ReturnData.get("resp_code").toString().equals("S")){
			 Log.Write("支付成功！[trans_id:"+ReturnData.get("trans_id")+"]");
			 return ReturnData;
		 }else if(ReturnData.get("resp_code").toString().equals("I")){	
			Log.Write("处理中！");
			 return ReturnData;
		 }/*else if(ReturnData.get("resp_code").toString().equals("F")){
			Log.Write("失败！");
		 }else{
			throw new Exception("反回异常！");//异常不得做为订单状态。
		 }*/
		 else {
			throw new Exception(ReturnData.get("biz_resp_msg").toString());
		 }
	}
}