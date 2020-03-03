package controllers.front.account;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.shove.Convert;
import com.shove.Xml;
import com.shove.security.Encrypt;

import annotation.InactiveUserCheck;
import annotation.RealNameCheck;
import annotation.SubmitCheck;
import annotation.SubmitOnly;
import baofu.rsa.RsaCodingUtil;
import baofu.rsa.RsaReadUtil;
import baofu.util.JXMConvertUtil;
import baofu.util.SecurityUtil;
import business.AuditItem;
import business.AuthReq;
import business.AuthResp;
import business.BackstageSet;
import business.CreditLevel;
import business.DictBanksDate;
import business.GatewayPay;
import business.News;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.Score;
import business.User;
import business.UserAuditItem;
import business.UserBankAccounts;
import business.Vip;
import constants.Constants;
import constants.Constants.RechargeType;
import constants.OptionKeys;
import constants.UserTypeEnum;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.app.AppController;
import controllers.interceptor.AccountInterceptor;
import controllers.wechat.account.WechatAccountHome;
import models.t_content_news;
import models.t_dict_audit_items;
import models.t_dict_payment_gateways;
import models.t_user_bank_accounts;
import models.t_user_over_borrows;
import models.t_user_recharge_details;
import models.v_credit_levels;
import models.v_user_account_statistics;
import models.v_user_audit_items;
import models.v_user_detail_credit_score_audit_items;
import models.v_user_detail_credit_score_invest;
import models.v_user_detail_credit_score_loan;
import models.v_user_detail_credit_score_normal_repayment;
import models.v_user_detail_credit_score_overdue;
import models.v_user_detail_score;
import models.v_user_details;
import models.v_user_withdrawals;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import payment.PaymentProxy;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.With;
import sun.util.logging.resources.logging;
import utils.AuthenticationUtil;
import utils.AuthenticationV2Util;
import utils.Base64;
import utils.CompressStringUtil;
import utils.Converter;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.GopayUtils;
import utils.JSONUtils;
import utils.JsonDateValueProcessor;
import utils.LLPayUtil;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;
import utils.baofoo.AgreementBanks;
import utils.baofoo.business.ConfirmPay;
import utils.baofoo.business.ConfirmSign;
import utils.baofoo.business.QueryBind;
import utils.baofoo.business.ReadyPay;
import utils.baofoo.business.ReadySign;
import utils.baofoo.util.Log;
import utils.pay.BankEn;
import utils.pay.bank.Authentication;

@With({AccountInterceptor.class, SubmitRepeat.class})
public class FundsManage extends BaseController {
	
	//普通key
	private static final String NORMAL_KEY = "name";
	
	//普通value
	private static final String NORMAL_VALUE = "value";

	//-------------------------------资金管理-------------------------
	/**
	 * 账户信息
	 */
	public static void accountInformation(){
		User user = User.currUser();
		long userId = user.id;
		
		ErrorInfo error = new ErrorInfo();
		v_user_account_statistics accountStatistics = User.queryAccountStatistics(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		UserOZ accountInfo = new UserOZ(userId);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<v_user_details> userDetails = User.queryUserDetail(userId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		if(userBanks != null && user.user_type != UserTypeEnum.COMPANY.getCode()
				&& user.user_type != UserTypeEnum.INDIVIDUAL.getCode()) {
			for(UserBankAccounts bankAccounts : userBanks) {
				bankAccounts.isProtocol = AgreementBanks.isAvailable(bankAccounts.bankCode) && StringUtils.isBlank(bankAccounts.protocolNo) ? 1 : 0;
			}
		}
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String content = News.queryContent(Constants.NewsTypeId.VIP_AGREEMENT, error);
		content = content.replaceAll("\\n|\\r", "");
		
		List<t_content_news> news = News.queryNewForFront(Constants.NewsTypeId.MONEY_TIPS, 3,error);

		boolean isIps = Constants.IPS_ENABLE;
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		if(user.financeType == 0) { //借款人只显示协议支付银行
			Object[] bankCodeIds = bankCodeNameTable.keySet().toArray(); 
			//查找是否支持协议支付
			for (int i = 0; i < bankCodeIds.length; i++) {
				if(!AgreementBanks.isAvailable(bankCodeIds[i])) {
					bankCodeNameTable.remove(bankCodeIds[i]);
				}
			}
		}
		
		String protocol = News.queryContent(Constants.NewsTypeId.AOTO_PROTOCOL_ID, error);
		if(protocol!=null) {
			protocol = protocol.replaceAll("\\n|\\r", "");
		}
		
		boolean isNotSign =  UserBankAccounts.isNotSign(user.id);
		Logger.info( "【%s】 是否未签署:【%s】", userId, isNotSign);
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		renderArgs.put("childId", "child_25");
		renderArgs.put("labId", "lab_5");
		render(user, accountStatistics, accountInfo, userDetails, userBanks, backstageSet, content, news, isIps,bankCodeNameTable,provinceCodeNameTable,protocol,isNotSign);
	}
	
	public static void main(String[] args) {
		System.out.println(Constants.NewsTypeId.VIP_AGREEMENT);
	}
	
	
	public static void signProtocol() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		long userId = user.id;
		try {
			UserBankAccounts.updateBankIsSign(userId, error);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "确认签署扣款协议异常";
			renderJSON(error);
		}
		error.code = 1;
		error.msg = "签署成功！";
		renderJSON(error);
	}
	
	
	/**
	 * 添加银行账号
	 */
	public static void addBank(Long editAccountId, int addBankCode, int addProviceCode, int addCityCode, String addBranchBankName, String addAccount, String addMobile, String addAccountName, String verfyCode, String verfyToken,boolean isSign){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(StringUtils.isBlank(user.idNumber)){
			error.code = -1;
			error.msg = "请先完善个人基本资料";
			json.put("error", error);		
			renderJSON(json);
		}
		if(!isSign && user.financeType == 0) { //只有借款人签协议
			error.code = -1;
			error.msg = "请先同意委托扣款授权协议";
			json.put("error", error);		
			renderJSON(json);
		}
		String bankName = DictBanksDate.queryBankByCode(addBankCode);
		String provice = DictBanksDate.queryProvinceByCode(addProviceCode);
		String city = DictBanksDate.queryCityByCode(addCityCode);
		
		String refID = Codec.UUID();
		new AuthReq().create(refID, User.currUser().id, 2);
		//绑卡认证
		/*String res = AuthenticationUtil.requestForBankInfo(addAccountName, user.idNumber, addAccount, addMobile, error);
		
		if(error.code < 0) {
			json.put("error", error);		
			renderJSON(json);
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(res)) {
			result = Xml.extractSimpleXMLResultMap(res);
			new AuthResp().create(refID, Long.parseLong((String)result.get("status")), (String)result.get("errorCode"), (String)result.get("errorMessage"));
		}
		if ("1".equals((String)result.get("status"))){
			String returnVal = (String)result.get("returnValue");
			String resXml = CompressStringUtil.decompress(new Base64().decode(returnVal));
			Logger.info(resXml);
			Map<String, Object> tt = AuthenticationUtil.extractMultiXMLResult(resXml, 1);
			if("1".equals(tt.get("treatResult"))){
				if (!"1".equals((String)tt.get("status")) || !bankName.equals((String)tt.get("accountBankName"))){
					error.code = -1;
					error.msg = "银行卡信息错误，请检查";
				}
			}else{
				error.code = -1;
				error.msg = "暂无法核查该卡数据信息，请更换银行卡";
			}
		}else{
			error.code = -1;
			error.msg = "银行卡认证系统出错";
		}*/
		
		// 绑卡认证-国政通+++++++++++++++++++++++++++++++++++++++++++++
		/*com.alibaba.fastjson.JSONObject authResult = AuthenticationV2Util.requestForBankInfo(addAccountName, user.idNumber, addAccount, addMobile);
		if(authResult == null) {
			error.code = -1;
			error.msg = "银行卡认证系统出错";
		}
		if(authResult.getJSONObject("message").getIntValue("status") != 0){
			error.code = -1;
			error.msg = authResult.getJSONObject("message").getString("value");
		}else if(authResult.getJSONObject("acctnoInfo").getIntValue("code") != 7){
			error.code = -1;
			error.msg = authResult.getJSONObject("acctnoInfo").getString("message");
		}*/
		// 绑卡认证-国政通+++++++++++++++++++++++++++++++++++++++++++++
		
		
		// ----协议绑卡介入-180425----
		String protocolNo = null;
		
		/** 判断是否支持协议绑卡 **/
		if(AgreementBanks.isAvailable(addBankCode)) { //协议绑卡流程
			
			// 宝付协议绑卡++++++++++++++++++++
			if(StringUtils.isBlank(verfyCode)){ // 发送验证码
				try {
					// 预绑卡-发送验证码
					String uniqueKey = ReadySign.execute(addAccount, addMobile, addAccountName, user.idNumber, user.id);
					// 验证码发送成功
					error.code = 99;
					error.msg = "验证码发送成功";
					json.put("error", error);
					json.put("verfyToken", new Base64().encode(uniqueKey.getBytes()));
					renderJSON(json);
				} catch (Exception e) {
					error.code = -1;
					error.msg = e.getMessage();
					json.put("error", error);		
					renderJSON(json);
				}
			}else { // 验证验证码
				try {
					verfyToken = new String(new Base64().decode(verfyToken));
					protocolNo = ConfirmSign.execute(verfyToken, verfyCode);
				} catch (Exception e) {
					error.code = -1;
					error.msg = e.getMessage();
					json.put("error", error);		
					renderJSON(json);
				}
			}
		}else { //老的绑卡流程
			// 绑卡认证-京东金融+++++++++++++++++++++++++++++++++++++++++++++
			
			com.alibaba.fastjson.JSONObject bankInfo = new com.alibaba.fastjson.JSONObject();
		    	bankInfo.put("realName", addAccountName);
		    	bankInfo.put("idNumber", user.idNumber);
		    	bankInfo.put("cardPan", addAccount);
		    	bankInfo.put("cardType", "D");
		    	bankInfo.put("bankCode", BankEn.toCodeEnMap().get(addBankCode));
		    	bankInfo.put("mobile", addMobile);
			
			//String verfyCode = parameters.get("verfyCode");
			//String verfyToken = parameters.get("verfyToken");
			
			// 获取token
			if(StringUtils.isBlank(verfyToken)){
				try {
					verfyToken = Authentication.getToken();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					error.code = -1;
					error.msg = "银行卡认证系统出错";
				}
			}else{
				verfyToken = new String(new Base64().decode(verfyToken));
			}
			
			if(StringUtils.isBlank(verfyCode)){ // 发送验证码
				try {
					com.alibaba.fastjson.JSONObject msg = Authentication.getMsg(verfyToken, bankInfo);
					if(!msg.getString("resultCode").equals("000")){
						error.code = -1;
						error.msg = msg.getString("resultInfo");
					}else{
						// 验证码发送成功
						error.code = 99;
						error.msg = "验证码发送成功";
						json.put("error", error);
						json.put("verfyToken", new Base64().encode(verfyToken.getBytes()));
						renderJSON(json);
						return;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					error.code = -1;
					error.msg = "银行卡认证系统出错";
				}
			}else{ // 验证验证码
				try {
					com.alibaba.fastjson.JSONObject confirm = Authentication.confirm(verfyToken, verfyCode, bankInfo);
					if(!confirm.getString("resultCode").equals("000")){
						error.code = -1;
						error.msg = confirm.getString("resultInfo");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					error.code = -1;
					error.msg = "银行卡认证系统出错";
				}
			}
			
			// 绑卡认证-京东金融+++++++++++++++++++++++++++++++++++++++++++++
		}
		
		if (error.code < 0) {
			json.put("error", error);		
			renderJSON(json);
		}
		
		// 存量数据协议绑卡
		if(editAccountId != null && editAccountId != 0 && StringUtils.isNotBlank(protocolNo)) {
			UserBankAccounts.updateBankProtocolNo(editAccountId, protocolNo, isSign,error);
		}else {
			
			UserBankAccounts bankUser =  new UserBankAccounts();
			
			bankUser.userId = user.id;
			bankUser.bankName = bankName;
			bankUser.bankCode = addBankCode+"";
			bankUser.provinceCode = addProviceCode;
			bankUser.cityCode = addCityCode;
			bankUser.branchBankName = "支行";
			bankUser.province = provice;
			bankUser.city = city;
			bankUser.account = addAccount;
			bankUser.accountName = addAccountName;
			bankUser.mobile = addMobile;
			bankUser.protocolNo = protocolNo;
			bankUser.isValid = Boolean.TRUE;
			bankUser.isSign = isSign;
			bankUser.addUserBankAccount(error, true);
		}
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	//保存银行账号
	public static void saveBank(){
		render();
	}
	
	/**
	 * 编辑银行账号
	 */

	public static void editBank(Long editAccountId, int eidtBankCode, int eidtProviceCode, int eidtCityCode, String eidtBranchBankName, String editAccount, String eidtMobile, String editAccountName,boolean isSign){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(!isSign && user.financeType == 0) { //只有借款人签协议
			error.code = -1;
			error.msg = "请先同意委托扣款授权协议";
			json.put("error", error);		
			renderJSON(json);
		}
		String bankName = DictBanksDate.queryBankByCode(eidtBankCode);
		String provice = DictBanksDate.queryProvinceByCode(eidtProviceCode);
		String city = DictBanksDate.queryCityByCode(eidtCityCode);

		
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = bankName;
		userAccount.bankCode = eidtBankCode+"";
		userAccount.provinceCode = eidtProviceCode;
		userAccount.cityCode = eidtCityCode;
		userAccount.branchBankName = eidtBranchBankName;
		userAccount.province = provice;
		userAccount.city = city;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;
		userAccount.mobile = eidtMobile;
		userAccount.isSign = isSign;
		userAccount.editUserBankAccount(editAccountId, user.id, true, error);
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 删除银行账号
	 */
	public static void deleteBank(long accountId){
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts.deleteUserBankAccount(User.currUser().id, accountId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 我的信用等级
	 */
	public static void myCredit(){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		v_user_detail_score creditScore = User.queryCreditScore(user.id);

		List<t_user_over_borrows> overBorrows = OverBorrow.queryUserOverBorrows(user.id, error);
		
		if(error.code < 0) {
			render(user, Constants.ERROR_PAGE_PATH_FRONT);
		}

//		double creditInitialAmount = BackstageSet.queryCreditInitialAmount();
		double creditInitialAmount = BackstageSet.getCurrentBackstageSet().initialAmount;
		
		renderArgs.put("childId", "child_26");
		renderArgs.put("labId", "lab_5");
		render(user,creditScore,overBorrows,creditInitialAmount);
	}
	
	/**
	 * 信用积分明细(成功借款)
	 */
	public static void creditDetailLoan(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_loan> page = User.queryCreditDetailLoan(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(审核资料)
	 */
	public static void creditDetailAuditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_audit_items> page = User.queryCreditDetailAuditItem(user.id, currPage, 0, key, error);
		
//		if(error.code < 0){
//			renderJSON(error);
//		}
		
		render(page);
	}
	
	/**
	 * 信用积分明细(成功投标)
	 */
	public static void creditDetailInvest(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_invest> page = User.queryCreditDetailInvest(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(正常还款)
	 * @param key
	 */
	public static void creditDetailRepayment(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_normal_repayment> page = User.queryCreditDetailRepayment(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 信用积分明细(逾期扣分)
	 * @param key
	 */
	public static void creditDetailOverdue(String key, int currPage){
		User user = User.currUser();
		
		PageBean<v_user_detail_credit_score_overdue> page = User.queryCreditDetailOverdue(user.id, currPage, 0, key);
		
		render(page);
	}
	
	/**
	 * 查看信用等级规则
	 */
	public static void viewCreditRule(){
		ErrorInfo error = new ErrorInfo();
		List<v_credit_levels> CreditLevels = CreditLevel.queryCreditLevelList(error);
		
		render(CreditLevels);
	}
	
	/**
	 * 查看信用积分规则
	 */
	public static void creditintegral(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		long auditItemCount = AuditItem.auditItemCount();
		
		ErrorInfo error = new ErrorInfo();

		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(backstageSet, auditItemCount, amountKey);
	}
	
	/**
	 * 查看科目积分规则
	 */
	public static void creditItem(String key, int currPage){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_dict_audit_items> page = AuditItem.queryEnableAuditItems(key, currPage, 0, error); // 审核资料
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		render(page, amountKey);
	}
	
	/**
	 * 审核资料
	 */
	
	/**
	 * 审核资料积分明细（信用积分规则弹窗）
	 */
	public static void auditItemScore(String keyword, String currPage, String pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> page = AuditItem.queryAuditItems(currPage, pageSize, keyword, true, error);
		
		render(page, error);
	}
	
	//申请超额借款
	public static void applyOverBorrow(){
		render();
	}

	//提交申请
	public static void submitApply(){
		render();
	}
	
	/**
	 * 查看超额申请详情
	 */
	public static void viewOverBorrow(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 查看超额申请详情(IPS)
	 */
	public static void viewOverBorrowIps(long overBorrowId){
		ErrorInfo error = new ErrorInfo();
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(overBorrowId, error);
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(overBorrowId, error);
		render(overBorrows, auditItems);
	}
	
	/**
	 * 提交资料
	 */  
	public static void userAuditItem(long overBorrowId, long useritemId, long auditItemId, String filename){
		
		ErrorInfo error = new ErrorInfo();

		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = User.currUser().id;
		item.id = useritemId;
		item.auditItemId = auditItemId;
		item.imageFileName = filename;
		item.overBorrowId = overBorrowId;
		item.createUserAuditItem(error);

		JSONObject json = new JSONObject();
		
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 充值
	 */
//	@InactiveUserCheck
//	@RealNameCheck
//	@SubmitCheck
	public static void recharge(){
//		ErrorInfo error = new ErrorInfo();
//
//		User user = new User();
//		user.id = User.currUser().id;
//
//		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
//		int rechargeLowest = backstageSet.rechargeLowest; //最低充值金额
//		int rechargeHighest = backstageSet.rechargeHighest; //最高充值金额
//		renderArgs.put("childId", "child_27");
//		renderArgs.put("labId", "lab_5");
//
//		Map<String,String> map  = new HashMap<String, String>();
//
//        map.put("102", "ICBC");
//        map.put("103", "ABC");
//        map.put("105", "CCB");
//        map.put("104", "BOC");
//        map.put("301", "BCOM");
//        map.put("309", "CIB");
//        map.put("302", "CITIC");
//        map.put("303", "CEB");
//        map.put("307", "PAB");
//        map.put("403", "PSBC");
//        map.put("322", "SHB");
//        map.put("310", "SPDB");
//        map.put("308", "CMB");
//
//        map.put("305", "CMBC");
//        map.put("306", "GDB");
//        map.put("304", "HXB");
//
//        map.put("404", "BOB");//北京银行
//
//		//查询用户绑定的银行卡，只绑定一张
//		t_user_bank_accounts bank = UserBankAccounts.queryById(User.currUser().id);
//
//		int bankType=0;
//
//		if(bank != null) {
//			if(!map.containsValue(bank.bank_code)){
//				bank.bank_code = map.get(bank.bank_code);
//			}
//			for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
//				if((Constants.BAOFU_TYPE[i]).equals(bank.bank_code)) {
//					bankType = i;
//					break;
//				}
//			}
//		}else{
//			flash.error("请先绑定银行卡");
//			accountInformation();
//		}
//
//		// 多张银行实现-----start
//		List<t_user_bank_accounts> banks = UserBankAccounts.queryMoreById(User.currUser().id);
//		if(banks != null) {
//			for(t_user_bank_accounts bank_account : banks) {
//				if(!map.containsValue(bank_account.bank_code)){
//					bank_account.bank_code = map.get(bank_account.bank_code);
//				}
//				for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
//					if((Constants.BAOFU_TYPE[i]).equals(bank_account.bank_code)) {
//						bank_account.bankType = i;
//						break;
//					}
//				}
//				// 是否支持协议支付
//				// 是否开通协议支付与是否支持协议支付无关
//				if(user.user_type == UserTypeEnum.COMPANY.getCode()
//					|| user.user_type == UserTypeEnum.INDIVIDUAL.getCode()) {
//					continue;
//				}
//				if(AgreementBanks.isAvailable(bank_account.bank_code) /*&& StringUtils.isNotBlank(bank_account.protocol_no)*/) {
//					bank_account.isProtocol = 1;
//				}
//			}
//		}else {
//			flash.error("请先绑定银行卡");
//			accountInformation();
//		}
//		// 多张银行实现-----end
//
//		/*if(bank != null) {
//			for (int i = 0; i < Constants.GO_CODE.length; i++) {
//				if((Constants.GO_CODE[i]).equals(bank.bank_code+"")) {
//					bank.bank_code = i;
//					break;
//				}
//			}
//		}*/
//
//		/*if (Constants.IPS_ENABLE) {
//
//			//是否需要选择银行，环讯，富友
//			boolean isNeedSelectBank = false;
//			List<Map<String, Object>> bankList = null;
//			if(Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_HX)
//					|| Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_FY)){
//
//				isNeedSelectBank = true;
//				bankList = PaymentProxy.getInstance().queryBanks(error, 0);
//			}
//
//			//是否支持闪电快充功能,环讯不支持
//			boolean isFastRecharge = true;
//			if(Constants.TRUST_FUNDS_TYPE.equals(Constants.TRUST_FUNDS_HX)){
//				isFastRecharge = false;
//			}
//
//			render("@front.account.FundsManage.rechargeIps",user, isNeedSelectBank, bankList, isFastRecharge, rechargeLowest,rechargeHighest);
//		}*/
//
//		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
//		// 当前是否有协议绑卡
//		// jsonMap.put("hasProtocolBank", Score.hasProtocolBank(user.id));
//		Boolean hasProtocolBank = Score.hasProtocolBank(user.id);
//		render(user, payType, rechargeLowest,rechargeHighest,bank,bankType, banks,hasProtocolBank);
		render();
	}
	
	/**
	 * app充值
	 */
	public static void rechargeApp(String id){
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = id;

		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			render();
		}

		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		//用户失效跳转app登入页面
		if (error.code < 0) {
			AppController.unUser();
		}

		User user = new User();
		user.id = userId;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int rechargeLowest = backstageSet.rechargeLowest; //最低充值金额
		int rechargeHighest = backstageSet.rechargeHighest; //最高充值金额
		renderArgs.put("childId", "child_27");
		renderArgs.put("labId", "lab_5");

		Map<String,String> map  = new HashMap<String, String>();

        map.put("102", "ICBC");
        map.put("103", "ABC");
        map.put("105", "CCB");
        map.put("104", "BOC");
        map.put("301", "BCOM");
        map.put("309", "CIB");
        map.put("302", "CITIC");
        map.put("303", "CEB");
        map.put("307", "PAB");
        map.put("403", "PSBC");
        map.put("322", "SHB");
        map.put("310", "SPDB");
        map.put("308", "CMB");

        map.put("305", "CMBC");
        map.put("306", "CDB");
        map.put("304", "HXB");

        map.put("404", "BOB");//北京银行
		//查询用户绑定的银行卡，只绑定一张
		t_user_bank_accounts bank = UserBankAccounts.queryById(userId);

		int bankType=0;

		if(bank != null) {
			if(!map.containsValue(bank.bank_code)){
				bank.bank_code = map.get(bank.bank_code);
			}
			for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
				if((Constants.BAOFU_TYPE[i]).equals(bank.bank_code+"")) {
					bankType = i;
					break;
				}
			}
		}

		/*if (Constants.IPS_ENABLE) {

			List<Map<String, Object>> bankList = PaymentProxy.getInstance().queryBanks(error, Constants.APP);

			render("@front.account.FundsManage.rechargeIps",user, bankList);
		}*/

		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);

		render(user, payType,bankType,rechargeLowest,rechargeHighest,bank,userIdStr);
	}
	
	/**
	 * app确认充值
	 */
	public static void submitRechargeApp(int type, double money, int bankType, String payPassword, String card_no){
		ErrorInfo error = new ErrorInfo();
		
		String userIdStr = params.get("userId");
		
		
		flash.put("type", type);
		flash.put("money", money);
		flash.put("bankType",bankType);
		
		if(StringUtils.isBlank(userIdStr)){
			rechargeApp(userIdStr);
		}
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		User user = new User();
		user.id = userId;
		//long userId = Long.parseLong(userIdStr);
		if(error.code < 0){
			flash.error("用户过期");
			rechargeApp(userIdStr);
		}
		if(money == 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp(userIdStr);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.01")) < 0) {
			flash.error("请输入正确的充值金额");
			rechargeApp(userIdStr);
		}
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		if(money < backstageSet.rechargeLowest){
			flash.error("最低充值" + backstageSet.rechargeLowest + "元");
			rechargeApp(userIdStr);
		}
		
		if (money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
			rechargeApp(userIdStr);
		}
		
		if(StringUtils.isBlank(card_no)){
			flash.error("请填写银行卡号");
			rechargeApp(userIdStr);
		}
		
		if(StringUtils.isBlank(user.payPassword)){
			flash.error("请设置交易密码");
			rechargeApp(userIdStr);
		}
		
		if(!Encrypt.MD5(payPassword+Constants.ENCRYPTION_KEY).equalsIgnoreCase(user.payPassword)) {
			flash.error("对不起，交易密码错误");
			rechargeApp(userIdStr);
		}
		
		t_user_bank_accounts bank = UserBankAccounts.queryById(User.currUser().id);
		
		if(bank != null) {
			for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
				if((Constants.BAOFU_TYPE[i]).equals(bank.bank_code+"")) {
					bankType = i;
					break;
				}
			}
		}
		
		 Map<String, String> args = User.baofuPay(user,moneyDecimal,bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_APP,card_no, error);
		 
		 
		 if(error.code < 0) {
				flash.error(error.msg);
				rechargeApp(userIdStr);
		  }
		 render("@front.account.FundsManage.submitBaoFuAuthPayRecharge",args);
		/*if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_APP, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}*/
		
	}
	
	/**
	 * 支付vip，资料审核,投标奖励等服务费
	 */
	public static void rechargePay() {
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		List<t_dict_payment_gateways> payType = user.gatewayForUse(error);
		
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay"+user.id);
		
		if(null == map) 
			renderText("请求过时或已提交!");
			
		double fee = (Double) map.get("fee");
		double amount = 0;
		boolean isPay = false;
		amount = user.balanceDetail.user_amount;
		
		render(user, payType, fee, amount, isPay);
	}
	
	/**
	 * 支付发标保证金
	 */
	@SubmitCheck
	public static void rechargePayIps(){
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>)Cache.get("rechargePayIps"+user.id);
		
		if(null == map || map.size() == 0)
			renderText("请求超时!");
		
		double fee = (Double) map.get("fee");		
		List<Map<String, Object>> bankList = null; 
		bankList = PaymentProxy.getInstance().queryBanks(error, 0); 		
		render("@front.account.FundsManage.rechargePayIps",user, bankList, fee);
	}
	
	/**
	 * 确认充值
	 */
	@InactiveUserCheck
	@RealNameCheck
	@SubmitOnly
	public static void submitRecharge(int type, double money, String payPassword, int bankType,String rechargeType,String card_no){
		ErrorInfo error = new ErrorInfo();

	
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		User user = User.currUser();
		user.id = user.id;
		flash.put("type", type);
		flash.put("bankType",bankType);	
		flash.put("rechargeType", rechargeType);


		String uniqueKey = params.get("uniqueKey").toString();
		t_user_recharge_details recharge_detail = t_user_recharge_details.find(" unique_key = ? ", uniqueKey).first();
		if(recharge_detail!=null){
			money = recharge_detail.amount;
		}

		BigDecimal moneyDecimal = new BigDecimal(money+"").setScale(2, RoundingMode.DOWN);
		money = moneyDecimal.doubleValue();
		flash.put("money", money);

		if(type<1 || type >4) {
			flash.error("请选择正确的充值方式");
			recharge();
		}
		
		if(money < backstageSet.rechargeLowest){
			flash.error("最低充值" + backstageSet.rechargeLowest + "元");
			recharge();
		}
		
		if (money > Constants.MAX_VALUE) {
			flash.error("充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
			recharge();
		}

		if(moneyDecimal.compareTo(new BigDecimal("0.01")) < 0) {
			flash.error("请输入正确的充值金额");
			recharge();
		}
		
		if(StringUtils.isBlank(payPassword)){
			flash.error("请输入交易记录");
			recharge();
		}
		
		if(StringUtils.isBlank(user.payPassword)){
			flash.error("请设置交易密码");
			recharge();
		}
		
		if(!Encrypt.MD5(payPassword+Constants.ENCRYPTION_KEY).equalsIgnoreCase(user.payPassword)) {
			flash.error("对不起，交易密码错误");
			recharge();
		}
		
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				recharge();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
			
		}else if(type == 3){
		    
			
			/******宝付充值开始********/
			if(StringUtils.isBlank(rechargeType)){
				
				flash.error("您选择的是宝付支付，请选择认证支付方式！");
				
				recharge();
			 }
			
		    
			if(StringUtils.isBlank(card_no)){
			    
			    flash.error("您选择的是宝付认证支付，请填写银行卡号！！！");
				
			    recharge(); 
			}
			
			//查询用户绑定的银行卡，只绑定一张
			t_user_bank_accounts bank = UserBankAccounts.queryById(User.currUser().id);
			
			// 多张银行卡实现--180412
			List<t_user_bank_accounts> banks = UserBankAccounts.queryMoreById(User.currUser().id);
	    		if(banks != null) {
	    			for(t_user_bank_accounts bank_account : banks) {
	    				if(card_no.equals(bank_account.account)) {
	    					bank = bank_account;
	    					break;
	    				}
	    			}
	    		}
			
			if(bank != null) {
				for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
					if((Constants.BAOFU_TYPE[i]).equals(bank.bank_code+"")) {
						bankType = i;
						break;
					}
				}
			}
			
			/** 协议支付介入 **/
			if(AgreementBanks.isAvailable(bank.bank_code)) {
				
				// 去绑卡
				if(StringUtils.isBlank(bank.protocol_no)) {
					flash.error("has_bind_"+card_no);
					recharge();
				}else { //去支付
					if(recharge_detail==null){
						flash.error("充值参数有误");
						flash.put("money", money);

						recharge();
					}
					try {
						Map<String, String> payResult = ConfirmPay.execute(uniqueKey, params.get("smscode"));
						if(payResult.get("resp_code").toString().equals("S")){

							 //认证充值前操作
//							 User.sequence(user.id,-4, payResult.get("trans_id"), money, Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, card_no,error);

							recharge_detail.pay_number = payResult.get("trans_id");
							recharge_detail.bank_card_no = card_no;
							recharge_detail.save();


							money = new BigDecimal(payResult.get("succ_amt")).divide(new BigDecimal("100")).doubleValue();

							Log.Write("支付成功！[trans_id:"+payResult.get("trans_id")+"]");

							 User.recharge(payResult.get("trans_id"), money, error);
							 if (error.code < 0) { 
								flash.error(error.msg);
								recharge();
							 }
							 render("app/views/front/account/FundsManage/rechargeStatus.html", payResult);
						}else if(payResult.get("resp_code").toString().equals("I")){	
							Log.Write("处理中！");
							 //认证充值前操作
//							 User.sequence(user.id,-4, payResult.get("trans_id"), money, Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, card_no,error);

							recharge_detail.pay_number = payResult.get("trans_id");
							recharge_detail.bank_card_no = card_no;
							recharge_detail.save();

							 render("app/views/front/account/FundsManage/rechargeStatus.html", payResult);
						}else {
							flash.error("充值异常;请联系客服人员");
							recharge();
						}
					} catch (Exception e) {
						flash.error(e.getMessage());
						recharge();
					}
				}
			}else {// 老版本支付
				
				
				Map<String, String> args = User.baofuPay(user,moneyDecimal,bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC,card_no, error);
				
				if(error.code < 0) {
					flash.error(error.msg);
					recharge();
				}
				render("@front.account.FundsManage.submitBaoFuAuthPayRecharge",args);
			}
			 
			/******宝付充值结束********/
		   /* if(StringUtils.isBlank(rechargeType)){
			
			flash.error("您选择的是连连支付，请选择网银或者认证支付方式！");
			
			recharge();
		    }
		    
		    		    
		    int llRecharge = Integer.parseInt(rechargeType);
		    
		    if(llRecharge == 1){
		    	//认证支付需要手机号（风控参数）
			    User user = User.currUser();
			    
			    user.id = user.id;
			    
			    if(!user.isMobileVerified){
			    	
			    	flash.error("认证支付需要绑定手机，你未绑定手机！");
					
					recharge();
					
			    }
				if(StringUtils.isBlank(card_no)){
				    
				    flash.error("您选择的是连连认证支付，请填写银行卡号！！！");
					
				    recharge(); 
				}
		    }
		    
		    Map<String, String> args = User.LLpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC,llRecharge,card_no, error);
		    
		    if(error.code != 0) {
				flash.error(error.msg);
				recharge();
		    }
		    //连连网银支付
		    if(llRecharge == 0){
			
			render("@front.account.FundsManage.submitLLWebPayRecharge",args);
			
		    }else{
		     //连连认证支付
			render("@front.account.FundsManage.submitLLAuthPayRecharge",args);
		    }*/
		} else if(type == 4){
			Logger.info("宝付网关充值开始......");
			try {
				//连接宝付网关支付
				Map<String, String> args = GatewayPay.gateway(money+"", "", error);
				
				if (error.code < 0) { 
					flash.error(error.msg);
					recharge();
				}
				render("@front.account.FundsManage.submitBaoFuGatewayRecharge",args);
			} catch (Exception e) {
				flash.error(e.getMessage());
				recharge();
			}
			Logger.info("宝付网关充值结束......");
		}
		
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 27, 2018 3:39:02 PM 
	 * @description.  获取协议支付验证码
	 *
	 */
	public static void getProtocolPayCode(long bankId, double money, String payPassword) {
		JSONObject result = new JSONObject();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		User user = User.currUser();
		user.id = user.id;
		t_user_bank_accounts bank = t_user_bank_accounts.findById(bankId);
		
		if(money < backstageSet.rechargeLowest){
			result.put("code", 0);
			result.put("msg", "最低充值" + backstageSet.rechargeLowest + "元");
			renderJSON(result);
		}
		
		if (money > Constants.MAX_VALUE) {
			result.put("code", 0);
			result.put("msg", "充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
			renderJSON(result);
		}
		
		if(!Encrypt.MD5(payPassword+Constants.ENCRYPTION_KEY).equalsIgnoreCase(user.payPassword)) {
			result.put("code", 0);
			result.put("msg", "对不起，交易密码错误");
			renderJSON(result);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money+"").setScale(2, RoundingMode.DOWN);
		try {
			String protocolNo = bank.protocol_no;
			long theUserId = user.id;

			List<t_user_bank_accounts> userBanAccountVerify = t_user_bank_accounts
					.find(" user_id = ? and protocol_no = ? ", theUserId , protocolNo ).fetch();
			if(userBanAccountVerify.size()<1) {
				result.put("code", 0);
				result.put("msg", "用户持有的卡异常");
				renderJSON(result);
			}
			
			//TODO 取卡需要优化
			// 对批量迁移的用户以及后面重复绑定的卡的协议号 用户协议号需要用到有效的协议号和userid
			/**
			 * 用户分离时,一个老用户可能产生 借款人id 投资人id
			 * 协议号一样,调用宝付需要协议绑定时的user_id验证
			 *
			 * 同一银行卡号,如果协议绑两次,会有两个协议号,只有最后一次的绑定数据有用
			 * 调用宝付需要使用最后一次协议号和用户id
			 */
			List<t_user_bank_accounts> banks = t_user_bank_accounts.find(" protocol_no = ? ",protocolNo).fetch();
			if(banks == null){
				Log.Write("查找协议支付号绑定银行卡信息出错！protocolNo:"+protocolNo);
			}else {
				if (banks.size() == 1) {
					//查找同一银行卡号是否绑定过多次
					List<t_user_bank_accounts> banks_same_account = t_user_bank_accounts.find(" account = ? ", banks.get(0).account).fetch();

					if (banks_same_account != null && banks_same_account.size() > 1) {
						//取最后绑定的userid
						long uid=0;
						long max_bank_account_id=0;
						for (t_user_bank_accounts account : banks_same_account){
							if(account.id>max_bank_account_id) {
								max_bank_account_id=account.id;
								uid = account.user_id;
								protocolNo = account.protocol_no;
							}
						}
						theUserId=uid;
					}
				} else if (banks.size() > 1) {//迁移过来的数据
					//取最小的userid,因为这个协议号是之前认证的
					theUserId = Math.min(banks.get(0).user_id, banks.get(1).user_id);
				}
			}

			//QueryBind.execute("", user.id);
			
			//2019.4.1++++++++多张卡绑定协议卡时, 未开通协议支付,没有协议支付号
			if(StringUtils.isEmpty(protocolNo)) {
				result.put("code", 0);
				result.put("msg", "该账号没有开通协议账号！");
				renderJSON(result);
			}
			// 预支付->发送验证码
			String uniqueKey = ReadyPay.execute(protocolNo, theUserId, moneyDecimal);

			ErrorInfo errorInfo =new ErrorInfo();
			//认证充值前操作
			User.sequence_new(user.id,-4, uniqueKey,"", money, Constants.GATEWAY_RECHARGE, Constants.CLIENT_PC, "",errorInfo);

			if(errorInfo.code == 0) {
				result.put("code", 1);
				result.put("msg", uniqueKey);
			}else{
				result.put("code", 0);
				result.put("msg", errorInfo.msg);
			}
		} catch (Exception e) {
			result.put("code", 0);
			result.put("msg", e.getMessage());
		}
		
		renderJSON(result);
	}
	
	/**
	 * 确认支付
	 */
	public static void submitRechargePay(int type, int bankType, boolean isUse){
		ErrorInfo error = new ErrorInfo();
		flash.put("type", type);
		flash.put("bankType",bankType);
		
		if(type<1 || type >2) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
		double fee = (Double) map.get("fee");
		int rechargeType = (Integer) map.get("rechargeType");
		double amount = 0;
		amount = user.balanceDetail.user_amount;
		double money = isUse ? (fee - amount) : fee;
		
		if(money <= 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(money);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.02")) < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		if(type == 2) {
			Map<String, String> args = User.ipay(moneyDecimal, bankType, rechargeType, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_FRONT);
			}
			
			render("@front.account.FundsManage.submitRecharge",args);
		}
		
		if(type == 1) {
			Map<String, String> args = User.gpay(moneyDecimal, bankType, rechargeType, Constants.RECHARGE_PC, Constants.CLIENT_PC, error);
			
			if(error.code != 0) {
				flash.error(error.msg);
				rechargePay();
			}
			
			render("@front.account.FundsManage.submitRecharge2",args);
		}
		
	}
	
	/**
	 * 环迅回调
	 */
	public static void callback(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种
		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		User.recharge(billno, Double.parseDouble(amount), error);
		int rechargeType = Convert.strToInt(billno.split("X")[0], RechargeType.Normal);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN) {
			flash.error(error.msg);
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}		
		User user = User.currUser();		
		if (Constants.IPS_ENABLE) {
			
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}			
				Cache.delete("rechargePay" + user.id);			
				AccountHome.home();
			}
		} else {	
			
			//申请vip
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("提交成功！");
					
					AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
				}
				
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("提交成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请超额借款成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请超额借款成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}
		 
		if(error.code < 0) {
			if (Constants.RECHARGE_WECHAT.equals(attach)) {
				WechatAccountHome.recharge();

			}else if(Constants.RECHARGE_APP.equals(attach)) {
				//rechargeApp();
				
			}else {
				info = error.msg;
				int code = error.code;
				render(info, code);	
			}
		}
		
		if(Constants.RECHARGE_APP.equals(attach)) {
			//rechargeApp();
		}
		
		if(Constants.RECHARGE_WECHAT.equals(attach)) {
			WechatAccountHome.recharge();
		}
		
		info = "交易成功";
		render(info);
	}	
	
	/**
	 * 环迅回调（异步）
	 */
	public static void callbackSys(String billno, String mercode, String Currency_type, String amount, String date, String succ,
			String msg, String attach, String ipsbillno, String retencodetype, String signature) {
		ErrorInfo error = new ErrorInfo();
		
		//返回订单加密的明文:billno+【订单编号】+currencytype+【币种】+amount+【订单金额】+date+【订单日期】+succ+【成功标志】+ipsbillno+【IPS订单编号】+retencodetype +【交易返回签名方式】+【商户内部证书】 
		String content="billno"+billno + "currencytype"+Currency_type+"amount"+amount+"date"+date+"succ"+succ+"ipsbillno"+ipsbillno+"retencodetype"+retencodetype;  //明文：订单编号+订单金额+订单日期+成功标志+IPS订单编号+币种

		boolean verify = false;

		//验证方式：16-md5withRSA  17-md5
		if(retencodetype.equals("16")) {
			cryptix.jce.provider.MD5WithRSA a=new cryptix.jce.provider.MD5WithRSA();
			a.verifysignature(content, signature, "D:\\software\\publickey.txt");

			//Md5withRSA验证返回代码含义
			//-99 未处理
			//-1 公钥路径错
			//-2 公钥路径为空
			//-3 读取公钥失败
			//-4 验证失败，格式错误
			//1： 验证失败
			//0: 成功
			if (a.getresult() == 0){
				verify = true;
			}	
		} else if(retencodetype.equals("17")) {
			User.validSign(content, signature, error);
			
			if(error.code == 0) {
				verify = true;
			}
		}
		String info = "";
		if(!verify) {
			info = "验证失败";
			render(info);
		}
		
		if (succ == null) {
			info = "交易失败";
			render(info);
		}
		
		if(!succ.equalsIgnoreCase("Y")) {
			info = "交易失败";
			render(info);
		} 
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, billno).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		long userId = Long.parseLong(obj.toString());
		User user = new User();
		user.id = userId;
		
		User.recharge(billno, Double.parseDouble(amount), error);
		
		if(error.code < 0) {
			return;
		}

	}
	
	/**
	 * 国付宝回调
	 */
	public static void gCallback(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		String info = "";
		
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			
			info = "验证失败，支付失败！";
			render(info);
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			info = "支付失败！";
			render(info);
		}
		
		if ("9999".equals(respCode)) {
			info = "订单处理中，请耐心等待！";
			render(info);
		}
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if(error.code < 0 && error.code != Constants.ALREADY_RUN) {
			flash.error(error.msg);
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		User user = User.currUser();
		
		if (Constants.IPS_ENABLE) {
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}						
		} else {
			
			if (rechargeType == RechargeType.VIP) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请vip成功！");
					
					AccountHome.home();
				}
				
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请vip成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("提交成功！");
					
					AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
				}
				
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("提交成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.auditMaterialsIPS(null, null, null, null, null, null, null);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				if (error.code == Constants.ALREADY_RUN) {
					flash.error("申请超额借款成功！");
					
					AccountHome.home();
				}			
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				if (error.code < 0) {
					flash.error(error.msg);
				} else {
					flash.error("申请超额借款成功！");
				}
				
				Cache.delete("rechargePay" + user.id);
				
				AccountHome.home();
			}
		}		 
		if(error.code < 0) {
			 flash.error(error.msg);
			 if (Constants.RECHARGE_WECHAT.equals(merRemark1)) {
					WechatAccountHome.recharge();

				}else if(Constants.RECHARGE_APP.equals(merRemark1)) {
					rechargeApp("");
					
				}else {

					render(error);
				}
		}
		
		if(Constants.RECHARGE_APP.equals(merRemark1)) {
			rechargeApp("");
		}
		
		if(Constants.RECHARGE_WECHAT.equals(merRemark1)) {
			WechatAccountHome.recharge();
		}
		
		info = "交易成功";
		render(info);
	}
	
	/**
	 * 国付宝回调（异步）
	 */
	public static void gCallbackSys(String version,String charset,String language,String signType,String tranCode
			,String merchantID,String merOrderNum,String tranAmt,String feeAmt,String frontMerUrl,String backgroundMerUrl
			,String tranDateTime,String tranIP,String respCode,String msgExt,String orderId
			,String gopayOutOrderId,String bankCode,String tranFinishTime,String merRemark1,String merRemark2,String signValue) {
		ErrorInfo error = new ErrorInfo();
		t_dict_payment_gateways gateway = User.gateway(Constants.GO_GATEWAY, error);
		
		if(GopayUtils.validateSign(version,tranCode, merchantID, merOrderNum,
	    		tranAmt, feeAmt, tranDateTime, frontMerUrl, backgroundMerUrl,
	    		orderId, gopayOutOrderId, tranIP, respCode,gateway._key, signValue)) {
			Logger.info("---------------验证失败，支付失败！------------");
			return ;
		}
		
		Logger.info("respCode:"+respCode);
		
		if (!"0000".equals(respCode) && !"9999".equals(respCode)) {
			Logger.info("---------------支付失败！------------");
			return ;
		}
		
		if ("9999".equals(respCode)) {
			Logger.info("---------------订单处理中，请耐心等待！------------");
			return ;
		}
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;
		
		try {
			obj = t_user_recharge_details.find(sql, merOrderNum).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			
			return ;
		}
		
		if(null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			
			return ;
		}

		long userId = Long.parseLong(obj.toString());
		User user = new User();
		user.id = userId;
		
		User.recharge(merOrderNum, Double.parseDouble(tranAmt), error);
		
		if(error.code < 0) {
			return;
		}
		
		int rechargeType = Convert.strToInt(merOrderNum.split("X")[0], RechargeType.Normal);
		
		if (Constants.IPS_ENABLE) {
			if (rechargeType == RechargeType.VIP) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.isPay = true;
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}	
			if (rechargeType == RechargeType.UploadItemsOB) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		} else {
						
			if (rechargeType == RechargeType.VIP) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int serviceTime = (Integer) map.get("serviceTime");
				Vip vip = new Vip();
				vip.serviceTime = serviceTime;
				vip.renewal(user, Constants.CLIENT_PC, error);
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if(rechargeType == RechargeType.UploadItems){
				UserAuditItem.submitUploadedItems(user.id, user.balance, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
			
			/* 2014-11-18把普通提交修改为资金托管模式下的提交 */
			if (rechargeType == RechargeType.UploadItemsOB) {
				Map<String, Object> map = (Map<String, Object>) Cache.get("rechargePay" + user.id);
				int _amount = (Integer) map.get("amount");
				String reason = (String) map.get("reason");
				List<Map<String,String>> auditItems = (List<Map<String, String>>) map.get("auditItems");
				OverBorrow overBorrow = new OverBorrow();
				overBorrow.isPay = true;
				overBorrow.applyFor(user, _amount, reason, auditItems, error);
				
				Cache.delete("rechargePay" + user.id);
				
				return;
			}
		}
	}
	
    /**
     * 连连网银支付同步回调 2015年12月12日 desctription
     */
    public static void llWebCallback(String oid_partner, String sign_type, String sign, String dt_order, String no_order, String oid_paybill, String money_order, String result_pay,
	    String settle_date, String info_order, String pay_type, String bank_code) {
	
	ErrorInfo error = new ErrorInfo();

	String info = "";
	Logger.info("-----------------连连支付同步回调开始-----------------");

	String sql = "select user_id from t_user_recharge_details where pay_number = ?";
	Object obj = null;

	try {
	    obj = t_user_recharge_details.find(sql, no_order).first();
	} catch (Exception e) {
	    e.printStackTrace();
	    error.code = -1;
	    error.msg = "根据pay_number查询用户ID出现错误!";
	    info = "充值失败，请联系管理员";
	    render(info);
	}

	if (null == obj) {
	    error.code = -1;
	    Logger.info("根据pay_number查询用户ID为null");
	    info = "充值失败，请联系管理员";
	    render(info);
	}
	
	Map<String, String> args = new HashMap<String, String>();

	args.put("oid_partner", oid_partner);
	args.put("sign_type", sign_type);
	args.put("sign", sign);
	args.put("dt_order", dt_order);
	args.put("no_order", no_order);
	args.put("oid_paybill", oid_paybill);
	args.put("money_order", money_order);
	args.put("result_pay", result_pay);
	args.put("settle_date", settle_date);
	args.put("info_order", info_order);
	args.put("pay_type", pay_type);
	args.put("bank_code", bank_code);
	Logger.info("*****************打印连连充值同步回调的所有参数开始**********************");
	Logger.info("args:"+params.allSimple().toString());
	Logger.info("*****************打印连连充值同步回调的所有参数结束**********************");
	// 验证签名
	//boolean flag = LLPayUtil.checkSign(buildJSONObject(params.allSimple()).toString(), Constants.LL_PAY_PRIMARY_KEY, Constants.LL_PAY_MD5_KEY);
	boolean flag = LLPayUtil.checkSignMD5(JSON.parseObject(JSON.toJSONString(args)), Constants.LL_PAY_MD5_KEY);
	if (!flag) {
	    info = "验证签名失败";
	    render(info);
	}

	if (result_pay == null) {
	    info = "交易失败";
	    render(info);
	}

	if (!result_pay.equalsIgnoreCase("SUCCESS")) {
	    info = "交易失败";
	    render(info);
	}

	User.recharge(no_order, Double.parseDouble(money_order), error);

	if (error.code < 0) {
	    flash.error(error.msg);
	    info = error.msg;
	    render(error, info);
	}

	// 认证支付需要绑定银行卡
	if ("D".equals(pay_type)) {

	    t_user_recharge_details user_recharge = User.queryByPaynumber(no_order, error);

	    if (user_recharge == null) {
		flash.error("交易失败");
		info = "交易失败";
		render(info);
	    }

	    t_user_bank_accounts bank = UserBankAccounts.queryByAccount(user_recharge.bank_card_no, user_recharge.user_id);

	    if (bank != null) {
		UserBankAccounts.updateBankNo(user_recharge.bank_card_no, bank_code, error);
	    }
	}

	Logger.info("-----------------连连支付同步回调结束-----------------");

	info = "交易成功";
	render(info);

    }

    /**
     * 连连支付异步回调 2015年12月14日 desctription
     */
    public static void llWebCallbackSys() {

	ErrorInfo error = new ErrorInfo();

	Logger.info("-----------------连连支付（异步）回调开始-----------------");

	String sql = "select user_id from t_user_recharge_details where pay_number = ?";

	Object obj = null;

	Logger.info("************************连连支付回调参数打印********************************");

	Logger.info(request.params.allSimple().toString());
	
	String reqStr = request.params.allSimple().get("body");

	JSONObject jsonObject = JSONObject.fromObject(reqStr);

	if (LLPayUtil.isnull(jsonObject.getString("oid_partner"))) {
	    JSONObject json = new JSONObject();
	    json.put("ret_code", "9999");
	    json.put("ret_msg", "交易失败");
	    Logger.error("参数oid_partner为空");
	    response.print(json.toString());
	    return;
	}

	// 验证签名
	boolean flag = LLPayUtil.checkSign(jsonObject.toString(), Constants.LL_PAY_PRIMARY_KEY, Constants.LL_PAY_MD5_KEY);

	if (!flag) {

	    JSONObject json = new JSONObject();
	    json.put("ret_code", "9999");
	    json.put("ret_msg", "交易失败");
	    Logger.info("************************验签失败*************");
	    response.print(json.toString());
	    return;
	}

	if (!jsonObject.getString("result_pay").equalsIgnoreCase("SUCCESS")) {

	    JSONObject json = new JSONObject();
	    json.put("ret_code", "9999");
	    json.put("ret_msg", "交易失败");
	    Logger.info("************************返回状态不是success*************");
	    response.print(json.toString());
	    return;
	}

	try {
	    obj = t_user_recharge_details.find(sql, jsonObject.getString("no_order")).first();

	} catch (Exception e) {
	    e.printStackTrace();
	    error.code = -1;
	    error.msg = "根据pay_number查询用户ID出现错误!";

	    return;
	}

	if (null == obj) {
	    error.code = -1;
	    Logger.info("根据pay_number查询用户ID为null");
	    return;
	}

	User.recharge(jsonObject.getString("no_order"), Double.parseDouble(jsonObject.getString("money_order")), error);
        
	if(error.code < 0){
	    
	    JSONObject json = new JSONObject();
	    if(error.code == Constants.ALREADY_RUN){
	    	
	    	json.put("ret_code", "0000");
		    json.put("ret_msg", "交易成功");
		    
	    }else{
	    	
	    	json.put("ret_code", "9999");
		    json.put("ret_msg", "交易失败");
	    }
	    
	    Logger.info("************************业务处理失败或者已经充值*************");
	    response.print(json.toString());
	    return;
	}
	// 认证支付
	if ("D".equals(jsonObject.getString("pay_type"))) {

	    t_user_recharge_details user_recharge = User.queryByPaynumber(jsonObject.getString("no_order"), error);

	    t_user_bank_accounts bank = UserBankAccounts.queryByAccount(user_recharge.bank_card_no, user_recharge.user_id);

	    if (bank != null) {
		UserBankAccounts.updateBankNo(user_recharge.bank_card_no, jsonObject.getString("bank_code"), error);
	    }
	}
	
	JSONObject json = new JSONObject();
	json.put("ret_code", "0000");
	json.put("ret_msg", "交易成功");
	Logger.info("************************异步回调交易成功*************");
	response.print(json.toString());

    }
	
	/**
	 * 提现
	 */
	@InactiveUserCheck
	@RealNameCheck
	@SubmitCheck
	public static void withdrawal(){
		User user = new User();
		user.id = User.currUser().id;
		ErrorInfo error = new ErrorInfo();
		
		//add 是否绑定银行卡判断	begin
		boolean bindedBankCard = true;
		if(Constants.IPS_ENABLE){
			Map<String, Object> map = PaymentProxy.getInstance().queryBindedBankCard(error, Constants.PC, user.ipsAcctNo);
			if(map != null && StringUtils.isBlank(map.get("result").toString())){
				bindedBankCard = false;  //未绑卡
			}
		}
		//add 是否绑定银行卡判断	end		version:8.0.2
		
		String type = params.get("type");
		String currPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String beginTime = params.get("startDate");
		String endTime = params.get("endDate");
		
		double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		double withdrawalAmount = user.balance - amount;
		
		if (!Constants.IS_WITHDRAWAL_INNER || (!Constants.IPS_ENABLE && Constants.WITHDRAWAL_PAY_TYPE == Constants.ONE)) {
			
			withdrawalAmount = ServiceFee.maxWithdralAmount(withdrawalAmount);
		}
		
		//资金托管模式下平台账户可提现金额
		double withdrawalAmount2 = ServiceFee.maxWithdralAmount(user.balance);
		
		//最多提现金额上限
		double maxWithDrawalAmount = Constants.MAX_VALUE;
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = null;
		
		
		banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		if(banks == null){
			//无效的也查出来
			banks = UserBankAccounts.queryUserAllVerifiedBankAccount(user.id);
		}
		if(banks == null || banks.size() < 1){
			flash.error("请先绑定银行卡");
			accountInformation();
		}
		
		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
				beginTime, endTime, currPage, pageSize, error);
		boolean ipsEnable = Constants.IPS_ENABLE;
		
		renderArgs.put("childId", "child_28");
		renderArgs.put("labId", "lab_5");
		render(user, withdrawalAmount, maxWithDrawalAmount, withdrawalAmount2, banks, page, ipsEnable, bindedBankCard);
	}
	
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}
	
	
//	/**
//	 * 提现记录
//	 */
//	public static void withdrawalRecord() {
//		User user = User.currUser();
//		
//		String type = params.get("type");
//		String currPage = params.get("currPage");
//		String pageSize = params.get("pageSize");
//		String beginTime = params.get("startDate");
//		String endTime = params.get("endDate");
//		
//		ErrorInfo error = new ErrorInfo();
//		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(user.id, type, 
//				beginTime, endTime, currPage, pageSize, error);
//		
//		render(page);
//	}
	
	//申请提现
	public static void applyWithdrawal(){
		render();
	}
	
	//申请提现
	public static void ipsWithDrawApply(){
		render();
	}
	
	/**
	 * 确认提现
	 */
	@SubmitOnly
	public static void submitWithdrawal(double amount, long bankId, String payPassword, int type, String ipsSelect){
		ErrorInfo error = new ErrorInfo();
		boolean flag = false;
		
		if(StringUtils.isNotBlank(ipsSelect) && ipsSelect.equals("1")) {
			flag = true;
		}
		
		if(amount <= 0) {
			flash.error("请输入提现金额");
			
			withdrawal();
		}
		
		if(amount > Constants.MAX_VALUE) {
			flash.error("已超过最大充值金额" +Constants.MAX_VALUE+ "元");
			
			withdrawal();
		}
		
		if (!(Constants.IPS_ENABLE && flag)) {
			if(StringUtils.isBlank(payPassword)) {
				flash.error("请输入交易密码");
				
				withdrawal();
			}
			
			if(type !=1 && type != 2) {
				flash.error("传入参数有误");
				
				withdrawal();
			}
			
			if(bankId <= 0) {
				flash.error("请选择提现银行");
				
				withdrawal();
			}
		}
		
		User user = new User();
		user.id = User.currUser().id;
		
		long withdrawalId = user.withdrawal(amount, bankId, payPassword, type, flag, error);
		
		if(error.code  < 0){
			flash.error(error.msg);			
			withdrawal();
		}
		if(Constants.IPS_ENABLE && flag) {
			PaymentProxy.getInstance().withdraw(error, 0, withdrawalId, amount);
		}
		
		flash.error(error.msg);
		
		withdrawal();
	}
	
	//转账
	public static void transfer(){
		render();
	}
	
	//确认转账
	public static void submitTransfer(){
		render();
	}
	
	/**
	 * 交易记录
	 */
	public static void dealRecord(int type, String beginTime, String endTime, int currPage, int pageSize){
	
		User user = User.currUser();
		PageBean<v_user_details> page = User.queryUserDetails(user.id, type, beginTime, endTime,currPage, pageSize);
		
		renderArgs.put("childId", "child_29");
		renderArgs.put("labId", "lab_5");
		render(page);
	}
	
	//交易详情
	public static void dealDetails(){
		render();
	}
	
	/**
	 * 导出交易记录
	 */
	public static void exportDealRecords(){
		ErrorInfo error = new ErrorInfo();
		
    	List<v_user_details> details = User.queryAllDetails(error);
    	
    	if (error.code < 0) {
			renderText("下载数据失败");
		}
    	
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
    	JSONArray arrDetails = JSONArray.fromObject(details, jsonConfig);
    	
    	for (Object obj : arrDetails) {
			JSONObject detail = (JSONObject)obj;
			int type = detail.getInt("type");
			double amount = detail.getDouble("amount");
			
			switch (type) {
			case 1:
				detail.put("inAmount", amount);
				detail.put("outAmount", "");
				break;
			case 2:
				detail.put("inAmount", "");
				detail.put("outAmount", amount);
				break;
			default:
				detail.put("inAmount", "");
				detail.put("outAmount", "");
				break;
			}
		}
    	
    	File file = ExcelUtils.export(
    			"交易记录", 
    			arrDetails,
				new String[] {"时间", "收入", "支出", "账户总额", "可用余额", "冻结金额", "待收金额", "科目", "明细"}, 
				new String[] {"time", "inAmount", "outAmount", "user_balance", "balance", "freeze", "recieve_amount", "name", "summary"});
    	
    	renderBinary(file, "交易记录.xls");
	}
	
	/**
	 * 支付账号登录
	 */
	public static void loginAccount() {
		ErrorInfo error = new ErrorInfo();
		PaymentProxy.getInstance().loginAccount(error, Constants.PC, User.currUser());
	}
	
	/**
	 * 查看(异步)
	 */
	public static void showitem(String mark, String signUserId){
		/* 解密userId */
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(userId < 1){
			renderText(error.msg);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.mark = mark;
		
		render(item);
	}
	
	/**
	 * 通过省份code获取城市code-name
	 */
	public static void queryCityCode2NameByProvinceCode(int provinceCode){
		ErrorInfo error = new ErrorInfo();
		Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(provinceCode, error);
		JSONArray array = buildJSONArrayByMaps(cityMaps);
		renderJSON(array);
	}
	
	/**
	 * 通过组合条件搜索银行支行名称(条件中最少需要提供银行code,否则数据库数据量查询缓慢,耗时会在6S左右检索出数据)
	 * @param cityCode
	 * @param bankCode
	 * @param searchValue
	 */
	public static void queryBankCode2NameByCondition(int cityCode,int bankCode,String searchValue){
		if (0 == cityCode || 0 == bankCode){
			return;
		}
		Map<String,Object> maps = new HashMap<String, Object>();
		maps.put("cityCode", cityCode);
		maps.put("bankCode", bankCode);
		maps.put("searchValue", searchValue);
		ErrorInfo error = new ErrorInfo();
		Map<String,String> bankMaps  = DictBanksDate.queryBankCode2NameByCondition(maps, error);
		JSONObject object = buildJSONObject(bankMaps);
		renderJSON(object);
	}
	
	private static JSONObject buildJSONObject(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put("title", entry.getValue());
			array.add(obj);
		}
		JSONObject object = new JSONObject();
		object.put("data", array);
		return object;
	}
	
	/**
	 * Map集合组装公用JSONArray(name-value键值对)
	 * @param maps
	 * @return
	 */
	private static JSONArray buildJSONArrayByMaps(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put(NORMAL_KEY,  entry.getKey());
			obj.put(NORMAL_VALUE, entry.getValue());
			array.add(obj);
		}
		return array;
	}
	
	/**
	 * 用户绑卡
	 */
	public static void userBindCard() {		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		PaymentProxy.getInstance().userBindCard(error, Constants.PC, user.ipsAcctNo);
	}
	
	
	
	/**
	 * 宝付网银支付(同步)
	 */
	public static void baofuWebCallback(){
		
		Logger.info("-----------------宝付支付同步回调开始-----------------");
		
		ErrorInfo error = new ErrorInfo();
		
		Logger.info(request.params.allSimple().toString());
		
		String data_content = params.get("data_content");
		
		String info = "";
		
		if(data_content.isEmpty()){//判断参数是否为空
			Logger.info("宝付充值返回数据为空");
			info = "充值失败";
			render(info);
		}
		
		//获取宝付公钥文件
		String pfxpath = Play.configuration.getProperty("baofu.public.key.file.path");
		
		File cerfile=new File(pfxpath);
		if(!cerfile.exists()){//判断宝付公钥是否为空
			Logger.info("宝付充值同步回调获取公钥文件失败");
			info="系统获取公钥文件失败，充值失败";
			render(info);
		}
		
		data_content = RsaCodingUtil.decryptByPubCerFile(data_content,pfxpath);
		
		if(StringUtils.isBlank(data_content)){
			Logger.info("宝付公钥不正确");
			info="宝付公钥不正确，充值失败";
			render(info);
		}
		
		try{
			data_content = SecurityUtil.Base64Decode(data_content);	
		}catch(Exception e){
			e.printStackTrace();
			Logger.info("解析返回数据异常");
			info="解析返回数据异常,充值失败";
			render(info);
		}
		
		Logger.info("解析返回数据:"+data_content);
			 
		Map<String,String> ArrayData = JXMConvertUtil.JsonConvertHashMap((Object)data_content);//将JSON转化为Map对象。
		
		String resp_code = ArrayData.get("resp_code");
		
        String trans_id = ArrayData.get("trans_id");
        
        
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		
		Object obj = null;

		try {
		    obj = t_user_recharge_details.find(sql, trans_id).first();
		} catch (Exception e) {
		    e.printStackTrace();
		    error.code = -1;
		    error.msg = "根据pay_number查询用户ID出现错误!";
		    info = "充值失败，请联系管理员";
		    render(info);
		}

		if (null == obj) {
		    error.code = -1;
		    Logger.info("根据pay_number查询用户ID为null");
		    info = "充值失败，请联系管理员";
		    render(info);
		}
		
		Logger.info("*****************打印宝付充值同步回调的所有参数开始**********************");
		Logger.info("args:"+ArrayData.toString());
		Logger.info("*****************打印宝付充值同步回调的所有参数结束**********************");
		
		String money_order = ArrayData.get("additional_info").split("_")[0];
		
		String bank_code = ArrayData.get("additional_info").split("_")[1];
		//取出充值红包id
		String redId = ArrayData.get("req_reserved").split("_")[0];
		
		
		String transId = ArrayData.get("trans_id");
		t_user_recharge_details rechargeD = t_user_recharge_details.find(" pay_number = ? ", transId).first();
		//红包id日志
		User user = new User();
		user.id = rechargeD.user_id;
		Logger.info("############################用户=="+user.name+"取出红包id======"+redId+"$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		
		String userIdStr = Security.addSign(rechargeD.user_id,Constants.USER_ID_SIGN);
		
		if(resp_code.equals("0000")){//充值成功
			
			User.recharge(trans_id, Double.parseDouble(money_order), error);
			
			if (error.code < 0) {
			    flash.error(error.msg);
			    info = error.msg;
			    if (Constants.CLIENT_WECHAT == rechargeD.client) {
					WechatAccountHome.recharge();

				}else if(Constants.CLIENT_APP == rechargeD.client) {
					rechargeApp(userIdStr);
					
				}else {

					render(error, info);
				}
			    
			}
			
			//app充值完成，修改充值红包状态
			if(Constants.CLIENT_APP == rechargeD.client){
				User.updateCallbackAppRedPackage(Long.parseLong(redId),user,error);
			}
			
			
			 t_user_recharge_details user_recharge = User.queryByPaynumber(trans_id, error);

		    if (user_recharge == null) {
				flash.error("交易失败");
				info = "交易失败";
				
				if (Constants.CLIENT_WECHAT == rechargeD.client) {
					WechatAccountHome.recharge();

				}else if(Constants.CLIENT_APP == rechargeD.client) {
					rechargeApp(userIdStr);
					
				}else {

					render(info);
				}
				
		    }
		    
		    int bankType = Integer.parseInt(ArrayData.get("req_reserved").split("_")[1]);
		    
		    //充值成功后增加银行卡
		    UserBankAccounts bankUser =  new UserBankAccounts();
			
			//查询是否有已经验证的银行卡
			t_user_bank_accounts bankt = UserBankAccounts.queryById(user.id);

			if(bankt == null) {
			        //查询用户是否已经有该未验证的银行账号
				t_user_bank_accounts banks = UserBankAccounts.queryByIds(user.id,user_recharge.bank_card_no);
				if(banks == null) {
					bankUser.userId = user.id;
					bankUser.bankName = Constants.BAOFU_BANK_NAME[bankType];
					bankUser.bankCode = Constants.BAOFU_TYPE[bankType];
					//bankUser.bankCode = 10023;
					bankUser.account = user_recharge.bank_card_no;
					bankUser.accountName = user.realityName;
					bankUser.addBankAccount(error);
				}else {
					if(!banks.account.equals(user_recharge.bank_card_no.trim())) {
						bankUser.userId = user.id;
						//bankUser.bankName = Constants.BAOFU_BANK_NAME[bankType];
						//bankUser.bankCode = Constants.BAOFU_TYPE[bankType];
						//bankUser.bankCode = 10023;
						bankUser.account = user_recharge.bank_card_no;
						bankUser.accountName = user.realityName;
						bankUser.addBankAccount(error);
					}
				}
				
				
			}
		    

		    t_user_bank_accounts bank = UserBankAccounts.queryByAccount(user_recharge.bank_card_no, user_recharge.user_id);

		    if (bank != null) {
		    	UserBankAccounts.updateBankNo(user_recharge.bank_card_no, bank_code, error);
		    }
		    
		    if (error.code < 0) {
			    flash.error(error.msg);
			    info = error.msg;
			    
			    if (Constants.CLIENT_WECHAT == rechargeD.client) {
					WechatAccountHome.recharge();

				}else if(Constants.CLIENT_APP == rechargeD.client) {
					rechargeApp(userIdStr);
					
				}else {

					 render(error, info);
				}
			   
			}
			
		    info = "充值成功";
		    
		    if (Constants.CLIENT_WECHAT == rechargeD.client) {
				WechatAccountHome.recharge();

			}else if(Constants.CLIENT_APP == rechargeD.client) {
				flash.error(info);
				rechargeApp(userIdStr);
				
			}else {

				 render(info);
			}
		    
		   
		}else{
			
			info = "充值失败:"+ArrayData.get("resp_msg");
			if (Constants.CLIENT_WECHAT == rechargeD.client) {
				WechatAccountHome.recharge();

			}else if(Constants.CLIENT_APP == rechargeD.client) {
				flash.error(info);
				rechargeApp(userIdStr);
				
			}else {

				 render(info);
			}
			
			
		}
		
	}
	
	
	public static void baofuWebCallbackSys(){
		
		ErrorInfo error = new ErrorInfo();
		
		Logger.info("-----------------宝付支付（异步）回调开始-----------------");
		
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		
		Logger.info("************************宝付支付回调参数打印********************************");
		
		Logger.info(request.params.allSimple().toString());
		
		String data_content = params.get("data_content");
		
		if(data_content.isEmpty()){//判断参数是否为空
			 Logger.info("宝付充值返回数据为空");
			 response.print("NO");
			 return;
		}
		
		//获取宝付公钥文件
		String pfxpath = Play.configuration.getProperty("baofu.public.key.file.path");
		
		File cerfile=new File(pfxpath);
		if(!cerfile.exists()){//判断宝付公钥是否为空
			Logger.info("宝付充值同步回调获取公钥文件失败");
			response.print("NO");
			return;
		}
		
		data_content = RsaCodingUtil.decryptByPubCerFile(data_content,pfxpath);
		
		if(StringUtils.isBlank(data_content)){
			Logger.info("宝付公钥不正确");
			response.print("NO");
			return;
		}
		
		try{
			data_content = SecurityUtil.Base64Decode(data_content);	
		}catch(Exception e){
			e.printStackTrace();
			Logger.info("解析返回数据异常");
			response.print("NO");
			return;
		}
		
		Logger.info("解析返回数据:"+data_content);
			 
		Map<String,String> ArrayData = JXMConvertUtil.JsonConvertHashMap((Object)data_content);//将JSON转化为Map对象。
		
		String resp_code = ArrayData.get("resp_code");
		
        String trans_id = ArrayData.get("trans_id");
        
        //取出充值红包id
        String redId = ArrayData.get("req_reserved").split("_")[0];
      	
		Object obj = null;

		try {
		    obj = t_user_recharge_details.find(sql, trans_id).first();
		} catch (Exception e) {
		    e.printStackTrace();
		    Logger.info("根据pay_number查询用户ID出现错误");
		    response.print("NO");
			return;
		}

		if (null == obj) {
		    error.code = -1;
		    Logger.info("根据pay_number查询用户ID为null");
		    response.print("NO");
			return;
		}
		
		Logger.info("*****************打印宝付充值同步回调的所有参数开始**********************");
		Logger.info("args:"+ArrayData.toString());
		Logger.info("*****************打印宝付充值同步回调的所有参数结束**********************");
		
		String money_order = ArrayData.get("additional_info").split("_")[0];
		
		String bank_code = ArrayData.get("additional_info").split("_")[1];
		
		String transId = ArrayData.get("trans_id");
		t_user_recharge_details rechargeD = t_user_recharge_details.find(" pay_number = ? ", transId).first();
		//红包id日志
		User user = new User();
		user.id = rechargeD.user_id;
		
		if(resp_code.equals("0000")){//充值成功
			
			User.recharge(trans_id, Double.parseDouble(money_order), error);
			
			if (error.code < 0) {
				if(error.code == Constants.ALREADY_RUN){
					Logger.info("宝付异步回调进行充值业务处理已经完成");
					response.print("OK");
				}else{
					Logger.info("宝付异步回调进行充值业务处理失败");
					response.print("NO");
				}
				return;
			}
			
			//app充值完成，修改充值红包状态
			if(Constants.CLIENT_APP == rechargeD.client){
				User.updateCallbackAppRedPackage(Long.parseLong(redId),user,error);
			}
			 t_user_recharge_details user_recharge = User.queryByPaynumber(trans_id, error);

		    if (user_recharge == null) {
		    	Logger.info("宝付异步回调根据订单号查询充值明细记录失败");
				response.print("NO");
				return;
		    }
		    
		    int bankType = Integer.parseInt(ArrayData.get("req_reserved").split("_")[1]);
		    
		    //充值成功后增加银行卡
		    UserBankAccounts bankUser =  new UserBankAccounts();
			
			//查询是否有已经验证的银行卡
			t_user_bank_accounts bankt = UserBankAccounts.queryById(user.id);

			if(bankt == null) {
			        //查询用户是否已经有该未验证的银行账号
				t_user_bank_accounts banks = UserBankAccounts.queryByIds(user.id,user_recharge.bank_card_no);
				if(banks == null) {
					bankUser.userId = user.id;
					bankUser.bankName = Constants.BAOFU_BANK_NAME[bankType];
					bankUser.bankCode = Constants.BAOFU_TYPE[bankType];
					//bankUser.bankCode = 10023;
					bankUser.account = user_recharge.bank_card_no;
					bankUser.accountName = user.realityName;
					bankUser.addBankAccount(error);
				}else {
					if(!banks.account.equals(user_recharge.bank_card_no.trim())) {
						bankUser.userId = user.id;
						//bankUser.bankName = Constants.BAOFU_BANK_NAME[bankType];
						//bankUser.bankCode = Constants.BAOFU_TYPE[bankType];
						//bankUser.bankCode = 10023;
						bankUser.account = user_recharge.bank_card_no;
						bankUser.accountName = user.realityName;
						bankUser.addBankAccount(error);
					}
				}
				
				if(error.code < 0){
		       	    error.msg="该认证银行卡号已经存在，请选择别的银行卡号";
		       	 	
		        }
			}

		    t_user_bank_accounts bank = UserBankAccounts.queryByAccount(user_recharge.bank_card_no, user_recharge.user_id);

		    if (bank != null) {
		    	UserBankAccounts.updateBankNo(user_recharge.bank_card_no, bank_code, error);
		    }
		    
		    if (error.code < 0) {
		    	Logger.info("宝付异步回调查询绑定银行卡失败");
			    response.print("NO");
				return;
			}
		    response.print("OK");
		}else{
			
			response.print("NO");
			
		}
		
	}
	/**
	 * 宝付APP回调
	 * @param data_content
	 * @return
	 * @throws Exception
	 */
	public static String bfAppCallback(String data_content) throws Exception { 
		ErrorInfo error = new ErrorInfo();
		Logger.info("bfAppCallback begin [%s]", data_content);
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		PublicKey publicKey = RsaReadUtil.getPublicKeyFromFile(Play.configuration.getProperty("cer.path"));
		if (publicKey == null) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "回调参数有误！");
			Logger.info("回调参数有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String aaa = RsaCodingUtil.decryptByPubCerFile(data_content,
				Play.configuration.getProperty("cer.path"));
		String coding = SecurityUtil.Base64Decode(aaa);
		Logger.info("APP回调返回参数[%s]",coding);
		net.sf.json.JSON jsonResult = Converter.xmlToObj(coding);
		JSONObject jsonObject = JSONObject.fromObject(jsonResult);
		String trans_id = jsonObject.get("trans_id").toString();
		String biz_type = jsonObject.get("biz_type").toString();
		String succ_amt = jsonObject.get("succ_amt").toString();
		String resp_code = jsonObject.get("resp_code").toString();
		String resp_msg = jsonObject.get("resp_msg").toString();
		String trans_no = jsonObject.get("trans_no").toString();

		if (StringUtils.isBlank(trans_id)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "订单号有误！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if (!"交易成功".equals(resp_msg) || !"0000".equals(resp_code)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "交易失败！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		User.recharge(trans_id, Double.parseDouble(succ_amt), error);
//		int rechargeType = Convert.strToInt(trans_id.split("type")[0],
//				RechargeType.Normal);

		if (error.code < 0) { 
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}
		jsonMap.put("msg", "充值成功！");
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	
	/**
	 * 宝付网关支付同步回调
	 */
	public static void baofooGwSynCallback(String Result, String ResultDesc,
			String FactMoney, String AdditionalInfo, String SuccTime, String BankID,
			String Md5Sign, String MemberID, String TerminalID,
			String TransID) {

		ErrorInfo error = new ErrorInfo();

		String info = "";
		Logger.info("-----------------宝付支付同步回调开始-----------------");

		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;

		try {
			obj = t_user_recharge_details.find(sql, TransID).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			info = "充值失败，请联系管理员";
			render(info);
		}

		if (null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			info = "充值失败，请联系管理员";
			render(info);
		}

		Map<String, String> args = new HashMap<String, String>();

		args.put("Result", Result);
		args.put("ResultDesc", ResultDesc);
		args.put("FactMoney", FactMoney);
		args.put("AdditionalInfo", AdditionalInfo);
		args.put("SuccTime", SuccTime);
		args.put("BankID", BankID);
		args.put("Md5Sign", Md5Sign);
		args.put("MemberID", MemberID);
		args.put("TerminalID", TerminalID);
		args.put("TransID", TransID);
		String Md5key = Play.configuration.getProperty("gateway.md5.key");//md5密钥（KEY）
		String MARK = "~|~";
		
		Logger.info("*****************宝付网关支付同步回调的所有参数开始**********************");
		Logger.info("args:" + params.allSimple().toString());
		Logger.info("*****************宝付网关支付同步回调的所有参数结束**********************");
		// 验证签名
		
		Logger.info("接收返回MD5值："+Md5Sign);
		String md5 = "MemberID=" + MemberID + MARK + "TerminalID=" + TerminalID + MARK + "TransID=" + TransID + MARK + "Result=" + Result + MARK + "ResultDesc=" + ResultDesc + MARK
				+ "FactMoney=" + FactMoney + MARK + "AdditionalInfo=" + AdditionalInfo + MARK + "SuccTime=" + SuccTime
				+ MARK + "Md5Sign=" + Md5key;
		Logger.info("本地MD5验签字串:" + md5);
		String WaitSign =SecurityUtil.MD5(md5);		
		Logger.info("本地MD5验签值:"+WaitSign);
		
		
		if(!WaitSign.equals(Md5Sign)){//将本地的MD5值和接收的MD5值比较
			Logger.info("本地MD5验签失败！");	
			info="MD5验签失败！";
			render(info);
			
		} 
		if (Result == null) {
			info = "交易失败";
			render(info);
		}

		if (!Result.equalsIgnoreCase("1")) {
			info = "交易失败";
			render(info);
		}


		if (error.code < 0) {
			flash.error(error.msg);
			info = error.msg;
			render(error, info);
		}

		Logger.info("-----------------宝付网关支付同步回调结束-----------------");

		info = "交易成功";
		render(info);

	}

	/**
	 * 宝付网关支付异步回调
	 */
	public static void baofooGwAsynCallback(String Result, String ResultDesc,
			String FactMoney, String AdditionalInfo, String SuccTime, String BankID,
			String Md5Sign, String MemberID, String TerminalID,
			String TransID) {
		
		Logger.info("*****************宝付网关支付异步回调的所有参数**********************");
		Logger.info("异步回调的所有参数args:" + request.params.allSimple().toString());
		
		ErrorInfo error = new ErrorInfo();
		Map<String, String> resultMap = request.params.allSimple();
		/*String TransID = resultMap.get("TransID");
		String Result = resultMap.get("Result");
		String ResultDesc = resultMap.get("ResultDesc");
		String FactMoney = resultMap.get("FactMoney");
		String AdditionalInfo = resultMap.get("AdditionalInfo");
		String SuccTime = resultMap.get("SuccTime");
		//String BankID = resultMap.get("BankID");
		String Md5Sign = resultMap.get("Md5Sign");
		String MemberID = resultMap.get("MemberID");
		String TerminalID = resultMap.get("TerminalID");*/
		 
		String info = "";
		Logger.info("-----------------宝付支付异步回调开始-----------------");
		Logger.info("TransID: " + TransID);
		String sql = "select user_id from t_user_recharge_details where pay_number = ?";
		Object obj = null;

		try {
			obj = t_user_recharge_details.find(sql, TransID).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "根据pay_number查询用户ID出现错误!";
			info = "交易失败";
			Logger.info("info: " + info);
			response.print(info);
			return;
		}

		if (null == obj) {
			error.code = -1;
			Logger.info("根据pay_number查询用户ID为null");
			info = "交易失败";
			Logger.info("info: " + info);
			response.print(info);
			return;
		}

		Map<String, String> args = new HashMap<String, String>();

		args.put("Result",  resultMap.get("Result"));
		args.put("ResultDesc", resultMap.get("ResultDesc"));
		args.put("FactMoney", resultMap.get("FactMoney"));
		args.put("AdditionalInfo", resultMap.get("AdditionalInfo"));
		args.put("SuccTime", resultMap.get("SuccTime"));
		args.put("BankID", resultMap.get("BankID"));
		args.put("Md5Sign", resultMap.get("Md5Sign"));
		args.put("MemberID", resultMap.get("MemberID"));
		args.put("TerminalID", resultMap.get("TerminalID"));
		args.put("TransID", TransID);
		String Md5key = Play.configuration.getProperty("gateway.md5.key");//md5密钥（KEY）
		String MARK = "~|~";
		/*
		Logger.info("*****************宝付网关支付异步回调的所有参数开始**********************");
		Logger.info("args:" + params.allSimple().toString());
		Logger.info("*****************宝付网关支付异步回调的所有参数结束**********************");*/
		// 验证签名
		
		Logger.info("接收返回MD5值："+resultMap.get("Md5Sign"));
		String md5 = "MemberID=" + MemberID + MARK + "TerminalID=" + TerminalID + MARK + "TransID=" + TransID + MARK + "Result=" + Result + MARK + "ResultDesc=" + ResultDesc + MARK
				+ "FactMoney=" + FactMoney + MARK + "AdditionalInfo=" + AdditionalInfo + MARK + "SuccTime=" + SuccTime
				+ MARK + "Md5Sign=" + Md5key;
		//Logger.info("本地MD5验签字串:" + md5);
		String WaitSign =SecurityUtil.MD5(md5);		
		Logger.info("本地MD5验签值:"+WaitSign);
		
		
		if(!WaitSign.equals(Md5Sign)){//将本地的MD5值和接收的MD5值比较
			Logger.info("本地MD5验签失败！");	
			info="MD5验签失败！";
			Logger.info("info: " + info);
			response.print(info);
			return;
		} 
		if (Result == null) {
			info = "交易失败";
			Logger.info("info: " + info);
			response.print(info);
			return;
		}

		if (!Result.equalsIgnoreCase("1")) {
			info = "交易失败";
			Logger.info("info: " + info);
			response.print(info);
			return;
		}
		
		if (Result.equals("1")) { //支付成功
			//异步回调成功时，更新用户账户金额
			User.recharge(TransID, Double.parseDouble(FactMoney)/100, error);

			if (error.code < 0) {
				flash.error(error.msg);
				info = error.msg;
				Logger.info("info: " + info);
				response.print(info);
				return;
			}

			Logger.info("-----------------宝付网关支付异步回调结束-----------------");

			info = "OK";
			Logger.info("info: " + info);
			response.print(info);
			
		} else {
			info = "NO";
			Logger.info("info: " + info);
			response.print(info);
		}
		

	}
}