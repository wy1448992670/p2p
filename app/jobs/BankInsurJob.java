package jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_user_insur;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import play.Logger;
import play.Play;
import play.jobs.Every;
import business.User;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhongan.scorpoin.biz.common.CommonRequest;
import com.zhongan.scorpoin.biz.common.CommonResponse;
import com.zhongan.scorpoin.common.ZhongAnApiClient;
import com.zhongan.scorpoin.common.ZhongAnOpenException;

/**
 * 
 * @description. 银行卡投保
 * 
 * @modificationHistory.
 * @author liulj 2017年2月24日下午5:17:21 TODO
 */

// @On("0 0 5 * * ?")
@Every("3min")
public class BankInsurJob extends BaseJob {

	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		// 是否购买保险
		String is_buy = Play.configuration.getProperty("is_buy_insur", "0");
		if("0".equals(is_buy)){
			Logger.info("未开启购买保险服务");
			return;
		}
		
		// 查询没有投保的银行卡
		List<Map<String, Object>> banks = User.findUserBankInsurExpr(null, false);
		if(banks != null && banks.size() > 0){
			for(Map<String, Object> bank : banks){
				// u.id, u.name,u.reality_name,ub.account,u.id_number,u.mobile
				Logger.info("开始购买保险："+JSON.toJSONString(bank));
				orderInsur(bank.get("id_number").toString(), bank.get("reality_name").toString(), bank.get("mobile").toString(), bank.get("bank_name").toString(), bank.get("account").toString());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/*public static void main(String[] args) throws ZhongAnOpenException {
		// env：环境参数，在dev-测试环境、iTest-开发环境、uat-预发环境、prd-生产环境 中取值
		// appKey：开发者的appKey
		// url：网关地址
		// privateKey：开发者私钥
		// version:版本号,众安技术人员提供 生产环境是1.0.0
		ZhongAnApiClient client = new ZhongAnApiClient(
				"dev",
				"9aec94b2cd97571cfab05005e5875301",
				"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANh61L+DYUsn3yw560NtiKh/8WBDYcnhO7OCCiGUayvl0pT0M2CitouP1Dc3JIYA7utlvJqxWIlT+pN58g3rg6lvKlnzUEur4VRXJhMeNmd1aq4IP+Zdq9Tizq1UvVYz/AzzY+Djc9fpauOjqDIHK07imNqF/oFE0ZjPT0B4MXZxAgMBAAECgYA0/9WIUbUHPmrAHCoCJxX3EuPYioatc0w3hZXPZNPcOncU6riNZyjEAGGXZxO1DxNvZEgJo3Omo33Mj2V4jPl9lYjgOoA7hWXDw2tGuv7R5nxQBhklaHhd2+UduYRnVpE136dkx8Z4LVVst4pmwEFhRHk9pEyAId+HMj5qx6VIsQJBAPbABwoh67NUT6Xh5yfFjqTwHqE4djJbwSfG8BIwkwqCbDBdgXuNDoSWrZ4mr1kh71l8vBhGxSp+imUumaDgxTUCQQDgmE9xTY0Eo+lPfh5M/RQE2yT1kcboSfYEmYcAnwiE+BC4mhghsemDwZ9bmQ46lU6oS0Ow7zQw7BzbivHD7b/NAkB10tAJuJTR9sppjWtRhHZOsBIQLePSvBmJoubz6JnuBMUgeyXfF0X9be3NfO9yAlBGTNeMSA7R8can9g6J0YqZAkApUNuMZE/EwsJwtSqtzwCXxBiQdDi7EqAHSJblLlxK2bd5vh8iU7A5ZK0EFKvhawYFP5M8QUTAmy7T1EOVX28hAkEAl1h2epeSBydMfXKeoxFs2ebJxZfDXs4bFSfiWswbQqnpjC+naFZutlLHXmL/wiUTIQxFyoJhmjE7cXyBoWrXvw==",
				"1.0.0.9618");
		client.setUrl("http://opengw-test.zhongan.com/Gateway.do");
		CommonRequest request = new CommonRequest("zhongan.open.common.addPolicy");// 设置调用的服务名称
		JSONObject params = new JSONObject();
		params.put("productMask", "1416c49773a6a09800560b933756b31375ed721e4d6b");// 众安提供的产品唯一的掩码
		params.put("policyHolderUserInfo", "{\"policyHolderCertiNo\":\"1234567890\",\"policyHolderCertiType\":\"102\",\"policyHolderUserName\":\"中亿云投资有限公司\",\"policyHolderPhone\":\"18616627851\"}");// 投保人信息
		params.put("insureUserInfo", "[{\"insureCertiNo\":\"421125198810235257\",\"insureCertiType\":\"I\",\"insureUserName\":\"刘朗君\",\"insurePhone\":\"18758265108\"}]");// 被保人信息
		params.put("premium", "2.4");//
		params.put("policyBeginDate", "20170228000000");// 保单起期，格式yyyyMMddHHmmss
		params.put("channelId", "1603");// 渠道id 由众安提供
		params.put("channelOrderNo", "123");
		params.put("extraInfo", "{\"debitCard\":\"123456\",\"relation\":\"其他\"}");// Json格式的业务扩展字符串
		request.setParams(params);
		CommonResponse response = (CommonResponse) client.call(request);
		System.out.println(response);
	}*/
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. 2017年2月27日 下午3:14:06 
	 * @description.  银行卡投保
	 * 
	 * @param userIdNo
	 * @param userName
	 * @param userPhone
	 * @param userBankAccount
	 */
	public static void orderInsur(String userIdNo, String userName, String userPhone, String userBankName, String userBankAccount){
		try {
			// env：环境参数，在dev-测试环境、iTest-开发环境、uat-预发环境、prd-生产环境 中取值
			// appKey：开发者的appKey
			// url：网关地址
			// privateKey：开发者私钥
			// version:版本号,众安技术人员提供 生产环境是1.0.0
			ZhongAnApiClient client = new ZhongAnApiClient(
					"prd",
					"d02e8514e29ac59035dac900c52fecf6",
					"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANuGq+xgu59ujTNSSQWQGhYkrYoDeEld2HhoGKnFzpGXd1PKmkVSij9Wa0t9hCvOplIfYAu0eJpig0bUhdX8uQUP0omoyjnCDN4yxYrXjX5k9B3RkvMEZ+mDb2NsSNvCWH+Ab6dgO30alttLoMRpKB7I6XmQYeTYHLD2h6qveHTRAgMBAAECgYAo9n88M2yvOkzEx9TyYcpMWXm8aBtY7VcIWLxJaay7BB1zGDojN1yupuyMgJZaKkxkxJACfyGGBV3jqKJ0pJzNWWcOO6dnjDPGCcsJWL8NKpw4iUk+muKeSywu4gUPYnGV1FpHXbdsGCL9Qy0SNuulstyW7wHo0pWX31TnsYgWBQJBAPnwRo27U91jqWFoagpSzhFECvF0cOKBnS33MWiTfWUtfb9jzU7iW5ZzcDPdEwj/9U2U56C4n1qbyecMJhpBqDsCQQDg2ZUL/lBvDEKfTzRGUr5cEvurjxJn4HQyP7GaHo1/FNpmVnHsBw2jZA2+zz18a6PJne0x+6D1+BcgTAoW/NJjAkBYusYxVsmFFPZy0ECk/ZSOaJuB8JaKsvz77n+p4oJnwuibY3DlilJ9bOnuX5N8ZHHPYwd/9UsNPbVFwvhb5ec1AkEArpWC+wr93RuB5zySdD/u+oLq0myGeA15a0K1Xdt3NkM0yN06G2/mkQUw/wTe0uqpA/URpQuiERoOqs8fznWDzQJAfUmkItpHFALjQkVmWValwa2w0r8j4DZGALtHFOOgngjxQW6W5R2Qemr+tBoJx7lQd35Mm9eU2NKZoh/ukPSAbw==",
					"1.0.0");
			client.setUrl("http://opengw.zhongan.com/Gateway.do");
			CommonRequest request = new CommonRequest("zhongan.open.common.addPolicy");// 设置调用的服务名称
			JSONObject params = new JSONObject();
			params.put("productMask", "ff633ad8736868f0b20fa5e24f48aa1524dcb8e813b9");// 众安提供的产品唯一的掩码
			params.put("policyHolderUserInfo", "{\"policyHolderCertiNo\":\"1234567890\",\"policyHolderCertiType\":\"102\",\"policyHolderUserName\":\"中亿云投资有限公司\",\"policyHolderPhone\":\"021-6438-0510\"}");// 投保人信息
			params.put("insureUserInfo", "[{\"insureCertiNo\":\"XNO\",\"insureCertiType\":\"I\",\"insureUserName\":\"XNAME\",\"insurePhone\":\"XPHONE\"}]".replace("XNO", userIdNo).replace("XNAME", userName).replace("XPHONE", userPhone));// 被保人信息
			params.put("premium", "2.4");//
			//params.put("policyBeginDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));// 保单起期，格式yyyyMMddHHmmss
			params.put("policyBeginDate", new SimpleDateFormat("yyyyMMdd000000").format(getDate(new Date(), 1, 0)));// 保单起期，格式yyyyMMddHHmmss
			params.put("channelId", "1603");// 渠道id 由众安提供
			//params.put("channelOrderNo", new SimpleDateFormat("yyyyMMDDHHmmssSSS").format(new Date())); // 渠道订单号
			params.put("channelOrderNo", String.format("%s%ty%tm%td-%s", userBankAccount, new Date(), new Date(), new Date(), RandomStringUtils.random(3, "abcdefghijklmnopqrstuvwxyz"))); // 渠道订单号
			params.put("extraInfo", "{\"debitCard\":\"XCARD\",\"relation\":\"其他\"}".replace("XCARD", userBankAccount));// Json格式的业务扩展字符串
			request.setParams(params);
			CommonResponse response = (CommonResponse) client.call(request);
			Logger.info(JSON.toJSONString(response));
			
			JSONObject bizContent = JSON.parseObject(response.getBizContent());
			
			if(StringUtils.isEmpty(bizContent.getString("errorCode"))){
				
				Logger.info(String.format("【%s】投保成功；保单号：%s, 保单连接：%s", userBankAccount, bizContent.getString("policyNo"), bizContent.getString("policyDownloadUrl")));
				// 业务操作
				
				t_user_insur user_insur = new t_user_insur();
				user_insur.bank_name = userBankName;
				user_insur.bank_account = userBankAccount;
				user_insur.insur_code = bizContent.getString("policyNo");
				user_insur.insur_price = bizContent.getString("premium");
				user_insur.insur_limit = bizContent.getString("sumInsured");
				user_insur.insur_start = new SimpleDateFormat("yyyyMMddHHmmss").parse(bizContent.getString("policyBeginDate"));
				user_insur.insur_end = new SimpleDateFormat("yyyyMMddHHmmss").parse(bizContent.getString("policyEndDate"));
				user_insur.insur_bill = bizContent.getString("policyDownloadUrl");
				user_insur.buy_dt = new Date();
				user_insur.save();
			}else if("307".equals(bizContent.getString("errorCode"))){
				
				// 业务操作
				
				t_user_insur user_insur = new t_user_insur();
				user_insur.bank_name = userBankName;
				user_insur.bank_account = userBankAccount;
				user_insur.insur_end = getDate(new Date(), 30, 0);
				user_insur.remark = "被保险人已经超过责任限额!";
				user_insur.save();
				
				Logger.error(String.format("【%s】投保失败[已记录]；errorCode：%s, errorMessage：%s", userBankAccount, bizContent.getString("errorCode"), bizContent.getString("errorMsg")));
			}else{
				Logger.error(String.format("【%s】投保失败；errorCode：%s, errorMessage：%s", userBankAccount, bizContent.getString("errorCode"), bizContent.getString("errorMsg")));
			}
			
		} catch (ZhongAnOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Date getDate(Date date, int day, int minute){
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(date);
		 calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + day);
		 calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + minute);
	     return calendar.getTime();
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("yyyyMMdd000000").format(getDate(new Date(), 1, 0)));
	}
}
