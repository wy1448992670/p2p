package utils.baofoo;

import java.util.HashMap;
import java.util.Map;

public enum AgreementBanks {
	
	BOC("BOC", "中国银行", 104, 1),
	ICBC("ICBC", "中国工商银行", 102, 1),
	PAB("PAB", "平安银行", 307, 1),
	CEB("CEB", "中国光大银行", 303, 1),
	GDB("GDB", "广发银行", 306, 1),
	HXB("HXB", "华夏银行", 304, 0),
	CITIC("CITIC", "中信银行", 302, 1),
	CCB("CCB", "中国建设银行", 105, 1),
	SHB("SHB", "上海银行", 322, 1),
	ABC("ABC", "中国农业银行", 103, 1),
	BCOM("BCOM", "交通银行", 301, 1),
	CIB("CIB", "兴业银行", 309, 1),
	PSBC("PSBC", "中国邮政储蓄银行", 403, 0),
	SPDB("SPDB", "浦东发展银行", 310, 1),
	CMBC("CMBC", "中国民生银行", 305, 1),
	CMB("CMB", "招商银行", 308, 1),
	HZB("HZB", "杭州银行", 0, 0),
	BOB("BOB", "北京银行", 404, 0),
	NBCB("NBCB", "宁波银行", 0, 0),
	JSB("JSB", "江苏银行", 0, 0),
	ZSB("ZSB", "浙商银行", 16, 0),

	;
	
	public String en, cn;
	public Integer code, pay;
	AgreementBanks(String en, String cn, Integer code, Integer pay){
		this.en = en;
		this.cn = cn;
		this.code = code;
		this.pay = pay;
	}
	
	public static Map<Integer, String> toCodeEnMap(){
		Map<Integer, String> result = new HashMap<Integer, String>();
		AgreementBanks[] ems = values();
		for(AgreementBanks em : ems){
			if(em.code != 0){
				result.put(em.code, em.en);
			}
		}
		return result;
	}
	
	public static boolean isAvailable(Object code) {
		AgreementBanks[] ems = values();
		for(AgreementBanks em : ems){
			if((em.code.toString().equals(code.toString()) || em.en.equals(code)) && em.pay == 1){
				return true;
			}
		}
		return false;
	}
	
	public static String getBankCode(Object code) {
		AgreementBanks[] ems = values();
		for(AgreementBanks em : ems){
			if(em.code.toString().equals(code.toString()) || em.en.equals(code)){
				return em.code.toString();
			}
		}
		return "";
	}
}
