package business;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import constants.Constants;
import play.Logger;
import play.Play;
import utils.ErrorInfo;
import utils.baofoo.util.SecurityUtil;
import utils.reapal.agent.utils.RandomUtil;
 

public class GatewayPay {
	public static Map<String, String> gateway(String orderAmt, String payId, ErrorInfo error){
		String orderNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + RandomUtil.getRandom(5);
		orderNo = orderNo.toUpperCase();
		//提交宝付前操作入库
		User user = User.currUser();
		user.id = user.id;
		User.sequence(user.id,Constants.BF_GATEWAY, orderNo, Double.parseDouble(orderAmt), Constants.GATEWAY_RECHARGE,Constants.CLIENT_PC, "", error);
		
		//组装网关参数
		System.out.println("支付金额："+ orderAmt);
		String orderAmt_fen = "";
		if (!orderAmt.isEmpty()) {	
			BigDecimal a = new BigDecimal(orderAmt).multiply(BigDecimal.valueOf(100)); //使用分进行提交
			orderAmt_fen = String.valueOf(a.setScale(0));
		} else {
			orderAmt_fen = ("0");
		}
		
		//Logger.info("支付金额（转换成分为单位）：" + orderAmt);
		
		if (payId.isEmpty()) {//PayID传空跳转宝付收银台，传功能ID跳转对应的银行		
			payId="";
			System.out.println("链接类型：跳转宝付收银台");
		} else {
			System.out.println("链接类型：直链银行");
		}
		
		Map<String, String> resultMap = new HashMap<>();
		String TransID = orderNo;//商户订单号（不能重复）
		String TradeDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());//下单日期		
		String MemberID = Play.configuration.getProperty("gateway.member.id");//商户号
		String TerminalID = Play.configuration.getProperty("gateway.terminal.id");//终端号
		String ProductName = "网关支付";//商品名称
		String Amount = "1";//商品数量
		String Username = user.realityName;//支付用户名称
		String AdditionalInfo = "";//订单附加信息
		String PageUrl = Play.configuration.getProperty("gateway.page.url");//页面跳转地址
		String ReturnUrl = Play.configuration.getProperty("gateway.return.url");//服务器底层通知地址
		String NoticeType = Play.configuration.getProperty("gateway.notice.type");//通知类型	
		String Md5key = Play.configuration.getProperty("gateway.md5.key");//md5密钥（KEY）
		String MARK = "|";
		
		String md5 =new String(MemberID+MARK+payId+MARK+TradeDate+MARK+TransID+MARK+orderAmt_fen+MARK+PageUrl+MARK+ReturnUrl+MARK+NoticeType+MARK+Md5key);//MD5签名格式
		//Logger.info("请求（MD5）拼接字串："+ md5);//商户在正式环境不要输出此项以免泄漏密钥，只在测试时输出以检查验签失败问题
		
		String Signature = SecurityUtil.MD5(md5);//计算MD5值
		String payUrl= Play.configuration.getProperty("gateway.baofoo.payUrl");//请求地址		
		String InterfaceVersion = "4.0";
		String KeyType = "1";//加密类型(固定值为1)
		
		resultMap.put("payUrl", payUrl);
		resultMap.put("MemberID", MemberID);
		resultMap.put("TerminalID", TerminalID);
		resultMap.put("InterfaceVersion", InterfaceVersion);
		resultMap.put("KeyType", KeyType);
		resultMap.put("PayID", payId);
		resultMap.put("TradeDate", TradeDate);
		resultMap.put("TransID", TransID);
		resultMap.put("OrderMoney", orderAmt_fen);
		resultMap.put("ProductName", ProductName);
		resultMap.put("Amount", Amount);
		resultMap.put("Username", Username);
		resultMap.put("AdditionalInfo", AdditionalInfo);
		resultMap.put("PageUrl", PageUrl);
		resultMap.put("ReturnUrl", ReturnUrl);
		resultMap.put("Signature", Signature);
		resultMap.put("NoticeType", NoticeType);
		
		Logger.info("提交表单：" + resultMap.toString());	
		 
		return resultMap;
	}
}

