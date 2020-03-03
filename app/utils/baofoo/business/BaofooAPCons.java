package utils.baofoo.business;

import play.Play;

public class BaofooAPCons {

//	public static String  pfxpath = "/work/workspace/com.shovesoft.sp2p/conf/bfcer/bfkey_100025773@@200001173.pfx"; //商户私钥文件
//	public static String  cerpath = "/work/workspace/com.shovesoft.sp2p/conf/bfcer/bfkey_100025773@@200001173.cer"; //宝付公钥文件
//	public static String  AesKey = "4f66405c4f66405c"; // AESKey
//	public static String  terminal_id = "200001173"; //终端号
//	public static String  member_id = "100025773"; //商户ID
//	public static String  priKeyPass = "100025773_286941";//私钥
//	public static String  POST_URL = "https://vgw.baofoo.com/cutpayment/protocol/backTransRequest";
	
	public static String  pfxpath = Play.configuration.getProperty("bf_ap_pfx_path", "/opt/webapp/WEB-INF/application/conf/bfcer/bfkey_100025773@@200001173.pfx"); //商户私钥文件
	public static String  cerpath = Play.configuration.getProperty("bf_ap_cer_path", "/opt/webapp/WEB-INF/application/conf/bfcer/bfkey_100025773@@200001173.cer"); //宝付公钥文件
	public static String  AesKey = Play.configuration.getProperty("bf_ap_aes_key", "4f66405c4f66405c"); // AESKey
	public static String  terminal_id = Play.configuration.getProperty("bf_ap_terminal_id", "200001173"); //终端号
	public static String  member_id = Play.configuration.getProperty("bf_ap_member_id", "100025773"); //商户ID
	public static String  priKeyPass = Play.configuration.getProperty("bf_ap_pri_key_pass", "100025773_286941");//私钥
	public static String  POST_URL = Play.configuration.getProperty("bf_ap_post_url", "https://vgw.baofoo.com/cutpayment/protocol/backTransRequest");//私钥
	
}
