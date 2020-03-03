package utils.baofoo.business;

import java.math.BigDecimal;

import play.Play;

public class T {

	public static void main(String[] args) {
		
		try {
			// 解绑
			//ABolishBind.execute("1201804261049362490000041768", 100000L);
			
			// 预绑卡
			//String uniqueKey = ReadySign.execute("6212262309006279753", "18689262768", "何豪雨", "310115199007129776", 100000L);
			
			// 确认绑卡
			//String protocolNo = ConfirmSign.execute(uniqueKey, "123456");
			
			// 查询绑卡状态
			QueryBind.execute("", 1485);
			
			// 预支付
			//String payUniqueKey = ReadyPay.execute(protocolNo, 100000L, new BigDecimal(10));
			
			// 确认支付
			// 20180426130836013031103402945159
			//ConfirmPay.execute(payUniqueKey, "123456");
			
			// 查询支付状态
			// TID1524719315304
			//QueryOrder.execute("TID1524719315304");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
	}
}
