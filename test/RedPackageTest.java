import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import controllers.app.RequestData;
import play.test.UnitTest;



public class RedPackageTest extends UnitTest{
	
	//注册送红包APP
	@Test
	public void registAPP(){
		Map<String, String> parameters =  new HashMap<String, String>();
		parameters.put("name", "xl0220");
		parameters.put("email", "xl0220@qq.com");
		parameters.put("pwd", "123456");
		parameters.put("referrerName", "");
		try {
			RequestData.register(parameters);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	//充值送红包
	@Test
	public void czAPP(){
		Map<String, String> parameters =  new HashMap<String, String>();
		parameters.put("id", "1c5ddd4f493192508e013db0d61e7053");
		parameters.put("amount", "1000.00");
		parameters.put("bankType", "0");//银行类型
		parameters.put("rechargeType", "1");//充值类型
		parameters.put("card_no", "");//银行卡号
		
		
		
		try {
			RequestData.recharge(parameters);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//投资发红包
	@Test
	public void investAPP(){
		Map<String, String> parameters =  new HashMap<String, String>();
		parameters.put("borrowId", "10");//请传入借款标ID
		parameters.put("userId", "4");//用户ID
		parameters.put("amount", "500");//投标金额
		parameters.put("dealPwd", "");//交易密码
		parameters.put("redPackageId", "43");//红包ID
		
		try {
			RequestData.invest(parameters); 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	//投资使用红包
	@Test
	public void userRedPackage(){
		Map<String, String> parameters =  new HashMap<String, String>();
		parameters.put("borrowId", "8");//请传入借款标ID
		parameters.put("userId", "4");//用户ID
		parameters.put("amount", "1000");//投标金额
		parameters.put("dealPwd", "");//交易密码
		parameters.put("redPackageId", "43");//红包ID
		
		try {
			RequestData.invest(parameters); 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
