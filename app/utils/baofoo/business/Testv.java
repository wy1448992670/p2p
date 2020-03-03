/**
 * Company: www.baofu.com
 * @author dasheng(大圣)
 * @date 2018年1月26日
 */
package utils.baofoo.business;

import utils.baofoo.rsa.SignatureUtils;
import utils.baofoo.util.Log;
import utils.baofoo.util.SecurityUtil;

public class Testv{
	public static void main(String[] args) throws Exception {
			
		Log.Write("AES"+SecurityUtil.AesEncrypt("6217002290013105146|彭辰|310115199007121635|15823781632||", "4f66405c4f66405c"));
		
		
		
		
		Log.Write(SecurityUtil.AesDecrypt("9dcfff2c441de985a14aebff7d3ccf08b1de1531fd42275d8115b1df59ccd718beeaf37f52d4b3ff5fb9d9a71582b2c1a7d302c68983fc229c3ee457bc98ad920aca77bc8665f7fbc94dcb3ada0bab11be385d6b16760c059f820bce3a68ba10","4f66405c4f66405c"));
		String  pfxpath ="D:\\CER_EN_DECODE\\AgreementPay\\bfkey_100025773@@200001173.pfx";//商户私钥        
        String  cerpath = "D:\\CER_EN_DECODE\\AgreementPay\\bfkey_100025773@@200001173.cer";//宝付公钥
		String HashStr = "acc_info=727e4e636efc15f013c43729734bffe8fe9d91d97f2f0348de9752569ef3a7ca1e4db735c98034303d97448c6eadde7886906d8ca94bbd1a67df9ffb72b43a4d90b47a9df4df96b5a443ec19228306cbe90719a1974f6cbe2d4c1d17e55510e2&card_type=101&dgtl_envlp=66c423493a79f02c6809094595f2caa47f0d18299aee093eba87f095db6d85c293ab32ee2288d15facd822751a476ff116629ef3a19e19fed3597e6f7de9c39d25f2d3925965f678ffa0f1059454963435ae65d492af4b55c68f277e5655d50089a097fa06161f9dac1f9d950f7ac4a48cdfd9a85b22449440d4a0ea1f6cf4ac&id_card_type=01&member_id=100025773&msg_id=TISN15169512484816441&send_time=2018-01-26 15:20:48&terminal_id=200001173&txn_type=01&user_id=000000&version=4.0.0.0";
		Log.Write("SHA-1摘要字串："+HashStr);
		String signature = SecurityUtil.sha1X16(HashStr, "UTF-8");//签名
		Log.Write("SHA-1摘要结果："+signature);
		String Sign = SignatureUtils.encryptByRSA(signature, pfxpath, "100025773_286941");
		Log.Write("RSA签名结果："+Sign);		
	}
}