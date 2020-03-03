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

import com.alibaba.fastjson.JSON;

import utils.baofoo.rsa.SignatureUtils;
import utils.baofoo.util.FormatUtil;
import utils.baofoo.util.HttpUtil;
import utils.baofoo.util.Log;
import utils.baofoo.util.SecurityUtil;

public class QueryOrder{
	/**
	 * 支付订单查询。
	 * @param args
	 * @throws Exception
	 */
	public static String execute(String transId) throws Exception {
		String send_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//报文发送日期时间		


		Map<String,String> DateArry = new TreeMap<String,String>();
		DateArry.put("send_time", send_time);
		DateArry.put("msg_id", "TISN"+System.currentTimeMillis());//报文流水号
		DateArry.put("version", "4.0.0.0");
		DateArry.put("terminal_id", BaofooAPCons.terminal_id);
		DateArry.put("txn_type", "07");//交易类型
		DateArry.put("member_id", BaofooAPCons.member_id);
		DateArry.put("orig_trans_id", transId);//交易时的trans_id
		DateArry.put("orig_trade_date", send_time);//
		
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
			Log.Write("交易成功！");
			return "S";
		}else if(ReturnData.get("resp_code").toString().equals("I")){	
			Log.Write("处理中！");
			return "I";
		}else if(ReturnData.get("resp_code").toString().equals("F")){	
			Log.Write("失败！");
			return "F";
		}else{
			Log.Write(JSON.toJSONString(ReturnData));
			throw new Exception("反回异常！");//异常不得做为订单状态。
		}
	}
}