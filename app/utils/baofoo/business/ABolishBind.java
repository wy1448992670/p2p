package utils.baofoo.business;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import utils.baofoo.rsa.RsaCodingUtil;
import utils.baofoo.rsa.SignatureUtils;
import utils.baofoo.util.FormatUtil;
import utils.baofoo.util.HttpUtil;
import utils.baofoo.util.Log;
import utils.baofoo.util.SecurityUtil;
/**
 * 解绑接口
 * Company: www.baofu.com
 * @author dasheng(大圣)2
 * @date 2018年3月2日
 */
public class ABolishBind{
	
	public static String execute(String ProtocolNo, long userId) throws Exception {
		
		String send_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//报文发送日期时间		
   
        //String AesKey = "4f66405c4f66405c";//商户自定义(可随机生成  AES key长度为=16位)
		String dgtl_envlp = "01|"+BaofooAPCons.AesKey;//使用接收方的公钥加密后的对称密钥，并做Base64转码，明文01|对称密钥，01代表AES[密码商户自定义]
		Log.Write("密码dgtl_envlp："+dgtl_envlp);	
		dgtl_envlp = RsaCodingUtil.encryptByPubCerFile(SecurityUtil.Base64Encode(dgtl_envlp), BaofooAPCons.cerpath);//公钥加密	
		//String ProtocolNo = "2018012510553720000117300110";//签约协议号（确认绑卡返回）
		Log.Write("签约协议号："+ProtocolNo);		
		ProtocolNo = SecurityUtil.AesEncrypt(SecurityUtil.Base64Encode(ProtocolNo), BaofooAPCons.AesKey);//先BASE64后进行AES加密
		Log.Write("签约协议号AES结果:"+ProtocolNo);

		Map<String,String> DateArry = new TreeMap<String,String>();
		DateArry.put("send_time", send_time);
		DateArry.put("msg_id", "TISN"+System.currentTimeMillis());//报文流水号
		DateArry.put("version", "4.0.0.0");
		DateArry.put("terminal_id", BaofooAPCons.terminal_id);
		DateArry.put("txn_type", "04");//交易类型
		DateArry.put("member_id", BaofooAPCons.member_id);
		DateArry.put("dgtl_envlp", dgtl_envlp);
		DateArry.put("user_id", userId+"");//用户在商户平台唯一ID (和绑卡时要一致)
		DateArry.put("protocol_no", ProtocolNo);//签约协议号（密文）
		
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
			Log.Write("解绑成功！");
			return "解绑成功！";
		}else{
			Log.Write("解绑失败！");
			throw new Exception(ReturnData.get("biz_resp_msg").toString());
		}
	}
}

