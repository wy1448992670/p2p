package services.business;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.shove.Convert;

import business.LogCore;
import business.Supervisor;
import constants.Constants;
import constants.UserTypeEnum;
import dao.ymd.CreditApplyDao;
import models.t_borrow_apply;
import models.t_dict_bid_repayment_types;
import models.t_enum_map;
import models.t_interface_call_record;
import models.t_user_address_list;
import models.t_users;
import models.t_users_info;
import models.core.t_apply_org_project;
import models.core.t_credit_apply;
import models.core.t_credit_increase_apply;
import models.core.t_interest_rate;
import models.core.t_new_product;
import models.core.t_organization;
import models.core.t_service_cost_rate;
import models.file.t_file_relation;
import models.risk.t_risk_manage_type;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import services.file.FileService;
import services.ymd.FileHelperService;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.OrderNoFactory;
import utils.PageBean;
import utils.PageBeanForPlayJPA;
import utils.xhj.XhjService;

public class CreditApplyService {
	
	static String RCVCODE = "-0000";
	public static t_credit_apply getLastCreditApplyByProductId(long user_id,long product_id) {
		t_credit_apply creditApply=t_credit_apply.find("user_id=? and product_id=? order by apply_time desc", user_id,product_id).first();
		return creditApply;
	}
	/**
	 * product_id为空查询用户所有的额度申请
	 * @param user_id
	 * @param product_id
	 * @return
	 */
	public static List<t_credit_apply> getCreditApplyListByUserIdAndProductId(long user_id,Long product_id) {
		List<Object> paramsList = new ArrayList<Object>();
		String sql="user_id=? ";
		paramsList.add(user_id);
		
		if(product_id!=null) {
			sql=sql+" and product_id=? ";
			paramsList.add(product_id);
		}
		List<t_credit_apply> creditApplyList= t_credit_apply.find(sql, paramsList.toArray()).fetch();
		return creditApplyList;
	}
	
	/**
	 * 是否所有额度申请都被拒绝
	 */
	public static boolean allCreditApplyIsNotThrough(List<t_credit_apply> creditApplyList) {
		for(t_credit_apply creditApply:creditApplyList) {
			if(!creditApply.isNotThrough()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 是否所有额度申请都被拒绝
	 */
	public static boolean allCreditApplyIsNotThrough(long user_id,Long product_id) {
		List<t_credit_apply> creditApplyList=CreditApplyService.getCreditApplyListByUserIdAndProductId(user_id,product_id);
		return CreditApplyService.allCreditApplyIsNotThrough(creditApplyList);
	}
	
	public static t_credit_increase_apply getCreditIncreaseApplyByCreditApplyId(long creditApplyId) {
		t_credit_increase_apply creditIncreaseApply=t_credit_increase_apply.find("credit_apply_id=? order by apply_time desc",creditApplyId).first();
		return creditIncreaseApply;
	}
	
	/**
	 * 已使用额度
	 * =有效借款申请的申请金额status=1,2,3,4,5
	 * +已关闭借款申请对应的标的借款金额status=6,bid.amount
	 * -对应标的已还款金额.
	 */
	public static BigDecimal getUseCreditByCreditApplyId(long creditApplyId) {
		String useCreditSQL=" select ifnull(borrow_a.use_amount,0)-ifnull(borrow_a.real_repayment_corpus,0) as useCredit " +
				" from t_credit_apply credit_a " +
				" left join ( " +
				"	select " +
				"	borrow_a.credit_apply_id, " +
				"	sum(case when borrow_a.status in(1,2,3,4,5) then ifnull(borrow_a.approve_amount,ifnull(borrow_a.apply_amount,0)) " +
				"		when borrow_a.status=6 then ifnull(bid_bill.bid_amount,0) else 0 end ) as use_amount, " +
				"	sum(ifnull(bid_bill.real_repayment_corpus, 0 )) as real_repayment_corpus " +
				"	from t_borrow_apply borrow_a " +
				"	left join ( " +
				"		select bid.borrow_apply_id,sum(bid.amount) bid_amount,sum(ifnull( bill.real_repayment_corpus, 0 )) as  real_repayment_corpus " +
				"		from  t_bids bid " +
				"		left JOIN ( SELECT bid_id, sum( real_repayment_corpus ) as real_repayment_corpus FROM t_bills WHERE STATUS IN ( -2, -3, 0 ) GROUP BY bid_id ) bill ON bill.bid_id = bid.id " +
				"		where bid.borrow_apply_id is not null " +
				"		and bid.status>=0 " +
				"		group by bid.borrow_apply_id " +
				"	)bid_bill on bid_bill.borrow_apply_id= borrow_a.id " +
				"	where true " +
				"	and borrow_a.status in(1,2,3,4,5,6) " +
				"	and borrow_a.credit_apply_id is not null " +
				"	group by borrow_a.credit_apply_id " +
				" )borrow_a on borrow_a.credit_apply_id=credit_a.id " +
				" where true " +
				" and credit_a.id=? ";

		BigDecimal useCredit=(BigDecimal)JPAUtil.createNativeQuery(useCreditSQL,creditApplyId).getSingleResult();
		return useCredit;
		
	}

	public static void creditApply(Long userId,Long orgId, String applyProjectIds,int period,
			 double applyCreditAmount,double itemsAmount,ErrorInfo error) throws Exception{
		//orgId 机构ID 找对应userId
		t_organization org = t_organization.findById(orgId);
		if(org == null) {
			error.code = -3;
			error.msg = "该机构不存在！";
			return;
		}
		
		//插入申请表
		t_credit_apply apply = new t_credit_apply();
		apply.user_id = userId;
		apply.apply_credit_amount = BigDecimal.valueOf(applyCreditAmount);
		apply.items_amount =  BigDecimal.valueOf(itemsAmount);
		if(apply.apply_credit_amount.compareTo(apply.items_amount)>0) {
			throw new Exception("申请额度大于项目额度");
		}
		apply.apply_period = period;
		apply.apply_period_unit = Constants.MONTH;
		apply.apply_time = new Date();
		//apply.product_id = ProductEnum.YI.getCode();
		t_new_product product=new t_new_product().getEnumByCode("YI");
		apply.product_id = product.id;
		if(apply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			if(apply.items_amount.compareTo(BigDecimal.valueOf(50000))>0){
				throw new Exception("项目金额不能大于伍万");
			}
			if(apply.apply_credit_amount.compareTo(BigDecimal.valueOf(1000))<0){
				throw new Exception("申请金额不能小于壹仟");
			}
			//最后一笔额度申请
			t_credit_apply creditApply=CreditApplyService.getLastCreditApplyByProductId(apply.user_id, apply.product_id);
			if(creditApply!=null && !creditApply.isClose()) {
				throw new Exception(product.name+"额度申请正在审核中,请等待");
			}
		}
		
		apply.consociation_user_id = org.user_id;
		apply.repayment_type_id = Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST;//等额本息
		apply.service_cost_rule = 1;//默认1
		apply.service_payment_model = 1;//默认1
		
		//查询机构利率,没有利率使用默认利率
		t_interest_rate rate = t_interest_rate.find(" org_id= ? and repayment_type_id = ? and  period = ? and period_unit = ? and product_id = ? ", 
				org.id, apply.repayment_type_id, apply.apply_period,apply.apply_period_unit, apply.product_id).first();
		if(rate == null) {
			rate = t_interest_rate.find(" org_id= ? and repayment_type_id = ? and  period = ? and period_unit = ? and product_id = ? ", 
					-1L, apply.repayment_type_id, apply.apply_period,apply.apply_period_unit, apply.product_id).first();
		}
		if(rate == null) {
			rate = t_interest_rate.find("org_id= -1").first();
		}
		apply.apply_apr = rate.interest_rate;
		
		//查询服务费费率,没有费率使用默认费率
		t_service_cost_rate serviceRate = t_service_cost_rate.find(" org_id= ? and service_cost_rule = ? and service_payment_model = ? and  period = ? and period_unit = ? and product_id = ? ", 
				org.id, apply.service_cost_rule,apply.service_payment_model, apply.apply_period,apply.apply_period_unit, product.id).first();
		if(serviceRate == null) {
			serviceRate = t_service_cost_rate.find(" org_id= ? and service_cost_rule = ? and service_payment_model = ? and  period = ? and period_unit = ? and product_id = ? ", 
					-1L, apply.service_cost_rule,apply.service_payment_model, apply.apply_period,apply.apply_period_unit, product.id).first();
		}
		if(serviceRate == null) {
			serviceRate = t_service_cost_rate.find("org_id= -1").first();
		}
		if(serviceRate == null) {
			serviceRate=new t_service_cost_rate();
			serviceRate.org_id=rate.org_id;
			serviceRate.product_id=rate.product_id;
			serviceRate.period_unit=rate.period_unit;
			serviceRate.period=rate.period;
			serviceRate.service_cost_rate=BigDecimal.ZERO;
			serviceRate.service_cost_rule=1;
			serviceRate.service_payment_model=1;
		}
		if(serviceRate.service_cost_rate==null) {
			serviceRate.service_cost_rate=BigDecimal.ZERO;
		}
		apply.service_cost_rate = serviceRate.service_cost_rate;
		apply.service_amount =  new BigDecimal(applyCreditAmount).multiply(serviceRate.service_cost_rate).multiply(new BigDecimal(0.01)); //总服务费=借款金额*服务费率
		
		
		//-----------------------------------*******机审开始*******------------------------------------------
		t_users user = t_users.findById(userId);
		
		t_users_info userInfo = t_users_info.find(" user_id = ? ", userId).first();
		if(userInfo == null) {
			error.code = -3;
			error.msg = "请先完善基础信息";
			return;
		}
		 
		//---------------------------------*******申请记录回写机审金额*******------------------------- 
		apply.status = 0;
		apply.save();
		
		//--------------------------------********插入申请额度图片关系*******-------------------------
		FileHelperService.creditApplyCopyFileRelation(3,userId,4, apply.id);
		
		//---------------------------------*******插入医院机构项目表*******------------------------- 		
		//apply_type = 1 为credit_apply额度申请信息
		String applyProjectIdArr[] = applyProjectIds.split(",");
		for (int i = 0; i < applyProjectIdArr.length; i++) {
			t_apply_org_project applyPro = new t_apply_org_project();
			applyPro.apply_id = apply.id;
			applyPro.apply_type = 1; //1.credit_apply 2.borrow_apply
			applyPro.org_project_id = Long.valueOf(applyProjectIdArr[i]);
			applyPro.save();
		}
		LogCore.create("t_credit_apply.status", apply.id, 1, apply.user_id, apply.status, new Date()
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(apply.status).enum_name
				, "亿美贷额度申请开始");
		
	}
	
	/**
	 *亿美贷申请下单
	 * @param userId
	 * @param applyId
	 * @param error
	 * @return
	 */
	public static Map<String, Object> useCredit(Long userId, Long applyId, ErrorInfo error) {
		Map<String, Object> resultMap = new HashMap<>();
		//t_credit_apply
		t_credit_apply apply = t_credit_apply.findById(applyId);
		if(apply == null || userId != apply.user_id) {
			error.code = -1;
			error.msg = "额度申请与订单不一致！";
			return null;
		}
		
		List<Map<String, Object>> list =CreditApplyDao.getApplyOrgItems(error, applyId, userId);
  
		resultMap.put("apply", apply);
		resultMap.put("orgList", list);
		return resultMap;
	}
	
	
	/**
	 * 
	 * @param userId
	 * @param applyId
	 * @param error
	 * @throws Exception
	 */
	public static t_borrow_apply createYMDBorrowApply(Long userId, Long applyId,BigDecimal useAmount,ErrorInfo error) throws Exception{
		//t_credit_apply
		t_credit_apply apply = t_credit_apply.findById(applyId);
		if(apply == null || userId != apply.user_id) {
			error.code = -1;
			error.msg = "额度申请与订单不一致！";
			return null;
		}
		
		BigDecimal useCredit=CreditApplyService.getUseCreditByCreditApplyId(applyId);
		
		if(useCredit.add(useAmount).compareTo(apply.audit_credit_amount) > 0) { //已用额度+使用额度 > 审批额度
			error.code = -1;
			error.msg = "额度不足！";
			return null;
		}
		List<t_apply_org_project> apply_projects = t_apply_org_project.find(" apply_id = ? ", apply.id).fetch();
		//apply_type =2 为borrow_apply 借款信息
		for (t_apply_org_project apply_project : apply_projects) {
			t_apply_org_project applyPro = new t_apply_org_project();
			applyPro.apply_id = apply.id;
			applyPro.apply_type = 2; //1.credit_apply 2.borrow_apply
			applyPro.org_project_id = Long.valueOf(apply_project.id);
			applyPro.save();
		}
		//orgId 机构ID 找对应userId
		t_organization org = t_organization.find(" user_id = ? ", apply.consociation_user_id).first();
		if(org == null) {
			error.code = -1;
			error.msg = "该机构不存在！";
			return null;
		}
		
		//插入借款申请表
		t_borrow_apply borrowApply = new t_borrow_apply();
		borrowApply.user_id = userId;
		borrowApply.accredit_pay_for_consociation = true;
		borrowApply.apply_amount =  useAmount;//申请金额
		borrowApply.apply_time = new Date();
		borrowApply.approve_amount =  useAmount;//使用金额
		borrowApply.consociation_user_id = org.user_id;
		borrowApply.credit_apply_id = applyId;
		borrowApply.period = apply.apply_period;
		borrowApply.interest_rate = apply.apply_apr;
		borrowApply.period_unit = apply.apply_period_unit;
		borrowApply.loan_property_id = UserTypeEnum.PERSONAL.getCode();//借款性质
		borrowApply.loan_purpose_id=5L;//借款用途,亿美贷的借款用途从[医疗美容]7L改为[个人消费]5L
		
		//borrowApply.product_id = ProductEnum.YI.getCode();
		//t_new_product product=new t_new_product().getEnumByCode("YI");
		//borrowApply.product_id = product.id.intValue();
		borrowApply.product_id = apply.product_id;
		borrowApply.repayment_type_id = apply.repayment_type_id;//等额本息
		borrowApply.service_amount=BigDecimal.ZERO;
		borrowApply.service_cost_rate = apply.service_cost_rate;
		borrowApply.service_cost_rule = apply.service_cost_rule;
		if(borrowApply.service_cost_rule.equals(1) && borrowApply.service_cost_rate!=null) {
			borrowApply.service_amount =borrowApply.approve_amount.multiply(borrowApply.service_cost_rate).multiply(BigDecimal.valueOf(0.01));
		}
		borrowApply.service_payment_model = apply.service_payment_model;
		borrowApply.status = 1;
		borrowApply.save();
		//根据ID回写borrow_no
		
		String borrowNo = Constants.BORROW_NO_PERFIX + borrowApply.id;
		borrowApply.borrow_no =  borrowNo + Constants.BORROW_NO_SUFFIX_MEI;
		borrowApply.save();
		
		LogCore.create("t_borrow_apply.status", borrowApply.id, 1, borrowApply.user_id, borrowApply.status, new Date()
				, t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status").get(apply.status).enum_name
				, "亿美贷额度使用下单成功！");
		return borrowApply;
	}
	
	
	@SuppressWarnings("unchecked")
	public static void getUserCreditInfo(String realName, String idCard, String mobile, String address,t_credit_apply apply) throws Exception {
		Logger.info("realName[%s],idCard[%s],mobile[%s]", realName,idCard,mobile);
		BigDecimal totalScore = BigDecimal.ZERO;
		//---------------------------******个人信息验证******---------------------------------
		//---------------------------同盾接口---------------------------------
		try {
			String TDScore = XhjService.getTdCommonScore(idCard, mobile);
			Logger.info("同盾返回数据: [%s]", TDScore);
			JSONObject TDScoreJson = JSONObject.parseObject(TDScore);
			if(TDScoreJson != null && TDScoreJson.get("success").equals("true")) {
				String result = TDScoreJson.getString("result");
				Map<String, String> maps = (Map<String, String>)JSON.parse(result);  
				String tdScore = maps.get("creditScore");
				Logger.info("同盾分数： 【%s】" ,tdScore);
				t_risk_manage_type td = t_risk_manage_type.findById(1L); 
				totalScore = totalScore.add(td.getScore(tdScore,apply.id));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("同盾接口数据解析出错！[%s]" , e.getMessage()+"");
			apply.interface_risk_status = -1;
			apply.interface_risk_msg = "同盾接口请求失败";	
			apply.status = -1;
			return;
			
		}
		
		//---------------------------星护甲接口---------------------------------
		Map<String, String> data = new HashMap<>();
		Date beginTime = new Date() ;
		try {
			data = XhjService.getPersonsCreditInfo(realName, idCard, mobile);
			Logger.info("调用星护甲接口耗时: 【%s】ms", new Date().getTime() - beginTime.getTime());
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("星护甲接口数据解析出错！[%s]" , e.getMessage()+"");
			apply.interface_risk_status = -2;
			apply.interface_risk_msg = "星护甲接口请求失败";	
			apply.status = -1;
			return;
			
		}
		
		String reqData =  data.get("reqData");
		String userCreditInfo = data.get("respData");
		String code = data.get("respCode");
		JSONObject arrayInfo = JSONObject.parseObject(userCreditInfo);
		
		//调用第三方后插入记录
		t_interface_call_record interfaceRecord = new t_interface_call_record();
		interfaceRecord.request_id = OrderNoFactory.getNo();
		interfaceRecord.api_id = "1";
		interfaceRecord.business_type = 3; //1,身份证照片实名 2,活体认证 3,防欺诈接口
		interfaceRecord.business_id = apply.id;//credit_apply.id
		interfaceRecord.request_time = beginTime;
		interfaceRecord.request_params = reqData;
		interfaceRecord.response_time = new Date();
		interfaceRecord.request_status = 2; //请求结果0,未处理 1,处理中 2,完成 3,异常
		interfaceRecord.response_params = userCreditInfo;
		if (code.equals(RCVCODE)) {
			interfaceRecord.business_status = 1;//业务状态1成功,0失败
			interfaceRecord.result_msg = "成功";
			interfaceRecord.save();
		} else {
			interfaceRecord.business_status = 0;//业务状态1成功,0失败
			interfaceRecord.result_msg = arrayInfo.getString("errorMsg");//业务状态失败返回
			interfaceRecord.save();
			
			apply.interface_risk_msg = "机审失败！";	
			apply.remark = "机审失败！";
			apply.interface_risk_status= 0;	//1成功,0失败			 
			apply.interface_risk_time = new Date();			 
			apply.status = -1;
			return;
		}
	
		try {

			JSONObject result = arrayInfo.getJSONObject("result"); 
			Logger.info("星护甲返回result: 【%s】", result);
		
//			JSONObject contactsInfo =  result.getJSONObject("contactsInfo");  
			JSONObject summaryInfo =  result.getJSONObject("summaryInfo");  
			
			if(summaryInfo != null && !summaryInfo.isEmpty()) {
				//智多分
				String score = summaryInfo.getString("score");
				Logger.info("获取智多分分数: 【%s】" ,score);
				t_risk_manage_type zhiduofen = t_risk_manage_type.findById(2L); 
				totalScore = totalScore.add(zhiduofen.getScore(score,apply.id));
				/*
				//是否存在黑名单
				String directContactsBlacklist = contactsInfo.getString("directContactsBlacklist");//直接联系人黑名单
				Logger.info("直接联系人黑名单： 【%s】" ,directContactsBlacklist);
				t_risk_manage_type blacklist = t_risk_manage_type.findById(3L); 
				totalScore = totalScore.add(blacklist.getScore(directContactsBlacklist,apply.id));
				
			*/	//通讯录人数
				/*String directContacts = contactsInfo.getString("directContacts") ;//直接联系人数
				t_risk_manage_type contacts = t_risk_manage_type.findById(11L); 
				totalScore = totalScore.add(contacts.getScore(directContacts,apply.id));
				Logger.info("直接联系人数： 【%s】" ,directContacts);*/
				
			}
			
			//通讯录人数(app原生获取)
			t_risk_manage_type contacts = t_risk_manage_type.findById(11L); 
			List<t_user_address_list> addressList = t_user_address_list.find(" user_id = ?", apply.user_id).fetch();
			totalScore = totalScore.add(contacts.getScore(addressList == null ? "0": addressList.size()+"", apply.id));
			Logger.info("直接联系人数： 【%s】" ,addressList.size());
			
			//不良行为等级
			JSONObject publicSecurityInfo = result.getJSONObject("publicSecurityInfo"); 
			JSONObject badbehaviorInfo =  publicSecurityInfo.getJSONObject("badbehaviorInfo");  
			String level = badbehaviorInfo.getString("level");
			
			t_risk_manage_type behavior = t_risk_manage_type.findById(4L); 
			totalScore = totalScore.add(behavior.getScore(level,apply.id));
			Logger.info("不良行为等级： 【%s】" ,level);

			//有无涉诉
			t_risk_manage_type securityInfo = t_risk_manage_type.findById(5L); 
			if(publicSecurityInfo != null && !publicSecurityInfo.isEmpty() && 
					!publicSecurityInfo.getJSONObject("negativeList").isEmpty()) {
				
				Logger.info("是否涉诉： 【%s】" ,!publicSecurityInfo.getJSONObject("negativeList").isEmpty());
				totalScore = totalScore.add(securityInfo.getScore("是",apply.id));
			} else {
				Logger.info("是否涉诉： 【%s】" ,!publicSecurityInfo.getJSONObject("negativeList").isEmpty());
				totalScore = totalScore.add(securityInfo.getScore("否",apply.id));
			}
			
			JSONObject telecomInfo =  result.getJSONObject("telecomInfo");  
			if(telecomInfo!=null && !telecomInfo.isEmpty()) {
				//号码归属地
			/*	String mobileOwnership = telecomInfo.getString("mobileOwnership");
				Logger.info("号码归属地： 【%s】" ,mobileOwnership);
				
				t_risk_manage_type ownership = t_risk_manage_type.findById(6); 
				totalScore = totalScore.add(ownership.getScore(level));*/
				
				
				//入网时间
				String inTime = telecomInfo.getString("inTime");
				Logger.info("入网时间： 【%s】" ,inTime);
				t_risk_manage_type time = t_risk_manage_type.findById(7L); 
				totalScore = totalScore.add(time.getScore(inTime,apply.id));
				
				//在网状态
				String onLineStatus = telecomInfo.getString("onLineStatus");
				Logger.info("在网状态： 【%s】" ,onLineStatus);
				t_risk_manage_type lineStatus = t_risk_manage_type.findById(8L); 
				totalScore = totalScore.add(lineStatus.getScore(onLineStatus,apply.id));
				
			}
			
			//姓名身份证号与手机号匹配情况
			JSONObject baseInfo = result.getJSONObject("baseInfo");  
			String nameMatchIdNumber = baseInfo.getString("nameMatchIdNumber");
			Logger.info("姓名身份证号与手机号匹配情况： 【%s】" ,nameMatchIdNumber);
			t_risk_manage_type nameMatchIdNumber_ = t_risk_manage_type.findById(9L); 
			totalScore = totalScore.add(nameMatchIdNumber_.getScore(nameMatchIdNumber,apply.id));
			
			
			//借款机构数
			JSONObject appInfo =  result.getJSONObject("appInfo");  
			String borrowingAppCount = appInfo.getString("borrowingAppCount");//贷款app数量
			Logger.info("借款机构数： 【%s】" ,borrowingAppCount);
			if(StringUtils.isEmpty(borrowingAppCount)) {
				borrowingAppCount = "0";
			}
			t_risk_manage_type borrowingAppCount_ = t_risk_manage_type.findById(13L); 
			totalScore = totalScore.add(borrowingAppCount_.getScore(borrowingAppCount,apply.id));
			
			//理财机构数
			String financialAppCount = appInfo.getString("financialAppCount");//理财app数量
			Logger.info("理财机构数： 【%s】" ,financialAppCount);
			if(StringUtils.isEmpty(financialAppCount)) {
				financialAppCount = "0";
			}
			t_risk_manage_type financialAppCount_ = t_risk_manage_type.findById(14L); 
			totalScore = totalScore.add(financialAppCount_.getScore(financialAppCount,apply.id));
			
		
			JSONObject borrowingInfo =  result.getJSONObject("borrowingInfo");
			BigDecimal borrowAmount = BigDecimal.ZERO;//借款金额
			BigDecimal overdueAmount = BigDecimal.ZERO;//逾期金额
			if(borrowingInfo!= null && !borrowingInfo.isEmpty()) {
				//借款金额
				String amountRange = borrowingInfo.getString("applyAmount");//[0,2000) 返回金额区间值
				if(!amountRange.isEmpty() && !"0".equals(amountRange)) {
					String[] split = amountRange.split(",");
					String amountMinStr = split[0].substring(1, split[0].length());
					String amountMaxStr = split[1].substring(0, split[1].length()-1);
					
					BigDecimal amountMin = new BigDecimal(amountMinStr);
					BigDecimal amountMax = new BigDecimal(amountMaxStr);
					//取中间值
					borrowAmount =  amountMin.add(amountMax).divide(new BigDecimal(20000),4); //单位万元
				} else {
					borrowAmount = BigDecimal.ZERO;
				}
				Logger.info("借款金额： 【%s】" ,borrowAmount);
				t_risk_manage_type borrowAmount_ = t_risk_manage_type.findById(15L); 
				totalScore = totalScore.add(borrowAmount_.getScore(borrowAmount.toString(),apply.id));
				 
				//逾期金额 （接口返回固定值）
				String overdueAmountRange = borrowingInfo.getString("overdueAmount");
				
				/*if(!overdueAmountRange.isEmpty() && !"0".equals(overdueAmountRange)) {
					String[] split = overdueAmountRange.split(",");
					String amountMinStr = split[0].substring(1, split[0].length());
					String amountMaxStr = split[1].substring(0, split[1].length()-1);
					
					BigDecimal amountMin = new BigDecimal(amountMinStr);
					BigDecimal amountMax = new BigDecimal(amountMaxStr);
					//取中间值
					overdueAmount =  amountMin.add(amountMax).divide(new BigDecimal(20000),4); //单位万元
				} else {
					overdueAmount = BigDecimal.ZERO;
				}*/
				if(!overdueAmountRange.isEmpty()&& !"0".equals(overdueAmountRange)) {
					overdueAmount = new BigDecimal(overdueAmountRange).divide(new BigDecimal(10000),4);//单位万元
				
				}else {
					overdueAmount = BigDecimal.ZERO;
				}
				Logger.info("逾期金额： 【%s】" ,overdueAmount);
				t_risk_manage_type overdueAmount_ = t_risk_manage_type.findById(17L); 
				totalScore = totalScore.add(overdueAmount_.getScore(overdueAmount.toString(),apply.id));
				
			} else {
				Logger.info("借款金额： 【%s】" ,borrowAmount);
				t_risk_manage_type borrowAmount_ = t_risk_manage_type.findById(15L); 
				totalScore = totalScore.add(borrowAmount_.getScore(borrowAmount.toString(),apply.id));
				
				Logger.info("逾期金额： 【%s】" ,overdueAmount);
				t_risk_manage_type overdueAmount_ = t_risk_manage_type.findById(17L); 
				totalScore = totalScore.add(overdueAmount_.getScore(overdueAmount.toString(),apply.id));
			}
			
			
			//逾期天数
			JSONObject overdueInfo =  result.getJSONObject("overdueInfo"); 
			if(overdueInfo != null && !overdueInfo.isEmpty()) {
				String isOverdue180 = overdueInfo.getString("isOverdue180");
				String isOverdue90 = overdueInfo.getString("isOverdue90");
				Logger.info("是否逾期180天： 【%s】" ,isOverdue180);
				Logger.info("是否逾期90天： 【%s】" ,isOverdue90);
				if("是".equals(isOverdue180)) {
					t_risk_manage_type overdueAmount_ = t_risk_manage_type.findById(16L); 
					totalScore = totalScore.add(overdueAmount_.getScore("180",apply.id));
					
				} else if("是".equals(isOverdue90)) {
					t_risk_manage_type overdueAmount_ = t_risk_manage_type.findById(16L); 
					totalScore = totalScore.add(overdueAmount_.getScore("90",apply.id));
				} else {
					t_risk_manage_type overdueAmount_ = t_risk_manage_type.findById(16L); 
					totalScore = totalScore.add(overdueAmount_.getScore("0",apply.id));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("星护甲数据解析出错！[%s]" , e.getMessage()+"");
			apply.interface_risk_msg = "星护甲数据解析失败";			
			apply.interface_risk_status = -2;
			apply.status = -1;
			return;
		}
		
		//-------------------------------******居住地验证******-----------------------------
		if(!StringUtils.isEmpty(address)) {
			//居住地址匹配度
			String addressVer = XhjService.rtAddrVer(realName, idCard, mobile, address);
			JSONObject liveAddress = JSONObject.parseObject(addressVer);
			Logger.info("居住地接口返回result: [%s] ", liveAddress);
			if(StringUtils.isEmpty(liveAddress.getString("errorCode"))) {//errorCode为空返回正确信息
				try {
					JSONObject resultAddress = liveAddress.getJSONObject("result");
					JSONObject addressData = resultAddress.getJSONObject("data");
					JSONArray RSL = addressData.getJSONArray("RSL");
					if(!RSL.isEmpty() || RSL.size() != 0) {
						JSONObject RSObject =  RSL.getJSONObject(0);
						JSONObject RS = null;
						t_risk_manage_type address_ = t_risk_manage_type.findById(10L); 
						if(RSObject != null) {
							RS = RSObject.getJSONObject("RS");
							String addressCode = (String) RS.get("code");
//							String addressDesc = (String) RS.get("desc");
							totalScore = totalScore.add(address_.getScore(addressCode,apply.id));
						}  
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					Logger.info("休息地验证数据解析出错！[%s]" , e.getMessage()+"");
					apply.interface_risk_msg = "休息地验证接口请求失败";			
					apply.interface_risk_status = -3;
					apply.status = -1;
					return;
				
				}
			} 
		}
		
		System.err.println("总分: " + totalScore +"--------------");
		//---------------------------------*******申请记录回写第三方机审字段*******------------------------- 
		apply.machine_score = totalScore; 
		apply.interface_risk_msg = "请求第三方接口成功";					 
		apply.interface_risk_status= 1;	//1成功,0失败			 
		apply.interface_risk_time = new Date();			 
		apply.status = 1;		 
	}
	
	public static PageBean<Map<String, Object>> queryCreditList(Params params) throws Exception {
		String currPageStr=params.get("currPage");
		String pageSizeStr=params.get("pageSize");
		
		String apply_status=params.get("apply_status");
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String user_mobile=params.get("user_mobile");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");
		
		String order_by_columns=params.get("order_by_columns");
		String asc_desc=params.get("asc_desc");
		
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		
		int currPage = 1;
		int pageSize = 10;
		if(!StringUtil.isBlank(currPageStr) && NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr) && NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr)>0?Integer.parseInt(pageSizeStr):pageSize;
		}
		
		String columns=" credit.id,product.name product_name,user.id_number,user.reality_name "
				+ " 	,user.mobile,user.time create_time,credit.apply_time,credit.audit_time,credit.update_time "
				+ "		,score.value score_value,credit.apply_credit_amount,credit.audit_credit_amount "
				+ " 	,status_enum.enum_name ";
		
		String table=" from t_credit_apply credit "
				+ " inner join t_users user on user.id=credit.user_id "
				+ " inner join t_new_product product on  product.id=credit.product_id "
				+ " left join ( "
				+ "		select enum_code,enum_name "
				+ "		from  t_enum_map "
				+ "		where enum_type=4 "
				+ " )status_enum on status_enum.enum_code=credit.status "
				+ " left join ( "
				+ "		select score.credit_apply_id,score.value "
				+ "		from t_risk_manage_score score "
				+ "		inner join ( "
				+ "			select credit_apply_id,max(id) id "
				+ "			from t_risk_manage_score "
				+ "			where type_id=1 "
				+ "			group by credit_apply_id "
				+ "	)last_score on last_score.id=score.id)score on score.credit_apply_id=credit.id  "
				+ " where true ";
		List<Object> paramsList = new ArrayList<Object>();
		String condition="";
		
		if(!StringUtil.isBlank(apply_status) && NumberUtil.isNumericInt(apply_status)
				&& t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(Integer.parseInt(apply_status))!=null ){
			condition+=" and credit.status =? ";
			paramsList.add(Integer.parseInt(apply_status));
			
		}
		if(!StringUtil.isBlank(reality_name)){
			condition+=" and user.reality_name like ? ";
			paramsList.add("%" + reality_name +"%");
		}
		if(!StringUtil.isBlank(user_idnumber)){
			condition+=" and user.id_number like ? ";
			paramsList.add("%" + user_idnumber +"%");
		}
		if(!StringUtil.isBlank(user_mobile) && NumberUtil.isNumericInt(user_mobile)){
			condition+=" and user.mobile like ? ";
			paramsList.add("%" + user_mobile +"%");
		}
		if(!StringUtil.isBlank(apply_star_date) ){
			try {
				Date starDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_star_date);
				condition+=" and credit.apply_time >= ? ";
				paramsList.add(starDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!StringUtil.isBlank(apply_end_date) ){
			try {
				Date endDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_end_date);
				condition+=" and credit.apply_time < ? ";
				paramsList.add(endDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//=======================================order_by=======================================
		String order_by="";
		if(StringUtil.isBlank(order_by_columns)){
			order_by_columns="credit.id";
		}
		if(StringUtil.isBlank(asc_desc)){
			asc_desc="desc";
		}
		order_by=" order by "+order_by_columns+" "+asc_desc;
		
		return PageBeanForPlayJPA.getPageBeanMapBySQL(columns, table+condition, order_by, currPage, isExport == Constants.IS_EXPORT ? 999999 : pageSize, paramsList.toArray());
	}
	
	public static PageBean<Map<String, Object>> queryIncreaseCreditList(Params params) throws Exception {
		String currPageStr=params.get("currPage");
		String pageSizeStr=params.get("pageSize");
		
		String apply_status=params.get("apply_status");
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String user_mobile=params.get("user_mobile");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");
		
		String order_by_columns=params.get("order_by_columns");
		String asc_desc=params.get("asc_desc");
		
		int currPage = 1;
		int pageSize = 10;
		if(!StringUtil.isBlank(currPageStr) && NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr) && NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr)>0?Integer.parseInt(pageSizeStr):pageSize;
		}
		
		String columns=" credit_increase.id,product.name product_name,user.id_number,user.reality_name,user.mobile,user.id as user_id, "
				+ " 	user.time,credit_increase.apply_time,credit_increase.audit_time,score.value, "
				+ "		credit_increase.prepare_credit_amount,credit_increase.apply_credit_amount, "
				+ "		credit_increase.apply_credit_amount-credit_increase.prepare_credit_amount as increse_amount, "
				+ " 	credit_increase.audit_credit_amount, "
				+ " 	status_enum.enum_name ";
		String table=" from t_credit_increase_apply credit_increase "
				+ " inner join t_credit_apply credit on credit_increase.credit_apply_id=credit.id "
				+ " inner join t_users user on user.id=credit.user_id "
				+ " inner join t_new_product product on  product.id=credit.product_id "
				+ " left join ( "
				+ "		select enum_code,enum_name "
				+ "		from  t_enum_map "
				+ "		where enum_type=5 "
				+ " )status_enum on status_enum.enum_code=credit_increase.status "
				+ " left join ( "
				+ "		select score.credit_apply_id,score.value "
				+ "		from t_risk_manage_score score "
				+ "		inner join ( "
				+ "			select credit_apply_id,max(id) id "
				+ "			from t_risk_manage_score "
				+ "			where type_id=1 "
				+ "			group by credit_apply_id "
				+ "	)last_score on last_score.id=score.id)score on score.credit_apply_id=credit.id  "
				+ " where true ";
		List<Object> paramsList = new ArrayList<Object>();
		String condition="";
		if(!StringUtil.isBlank(apply_status) && NumberUtil.isNumericInt(apply_status) 
				&& t_enum_map.getEnumCodeMapByTypeName("t_credit_increase_apply.status").get(Integer.parseInt(apply_status))!=null ){
			condition+=" and credit_increase.status =? ";
			paramsList.add(Integer.parseInt(apply_status));
		}
		if(!StringUtil.isBlank(reality_name)){
			condition+=" and user.reality_name like ? ";
			paramsList.add("%" + reality_name +"%");
		}
		if(!StringUtil.isBlank(user_idnumber)){
			condition+=" and user.id_number like ? ";
			paramsList.add("%" + user_idnumber +"%");
		}
		if(!StringUtil.isBlank(user_mobile) && NumberUtil.isNumericInt(user_mobile)){
			condition+=" and user.mobile like ? ";
			paramsList.add("%" + user_mobile +"%");
		}
		if(!StringUtil.isBlank(apply_star_date) ){
			try {
				Date starDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_star_date);
				condition+=" and credit_increase.apply_time >= ? ";
				paramsList.add(starDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!StringUtil.isBlank(apply_end_date) ){
			try {
				Date endDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_end_date);
				condition+=" and credit_increase.apply_time < ? ";
				paramsList.add(endDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//=======================================order_by=======================================
		String order_by="";
		if(StringUtil.isBlank(order_by_columns)){
			order_by_columns="credit_increase.id";
		}
		if(StringUtil.isBlank(asc_desc)){
			asc_desc="desc";
		}
		order_by=" order by "+order_by_columns+" "+asc_desc;
		
		return PageBeanForPlayJPA.getPageBeanMapBySQL(columns, table+condition, order_by, currPage, pageSize, paramsList.toArray());
	}
	
	 
	public static t_credit_apply getModelByPessimisticWrite(long id){
		//ESSIMISTIC_READ=lock in share mode  
		//PESSIMISTIC_WRITE=for update 
		return CreditApplyService.getModel(id,LockModeType.PESSIMISTIC_WRITE);
	}
	
	public static t_credit_apply getModel(long id,LockModeType lockModeType){
		t_credit_apply creditApply=null;
		try {
			creditApply=JPA.em().find(t_credit_apply.class, id, lockModeType);
		} catch (Exception e) {
			Logger.error("CreditApplyService.getModel"+id+e.getMessage());
			JPA.setRollbackOnly();
			e.printStackTrace();
			return null;
		}
		if (creditApply == null) {
			Logger.error("CreditApplyService.getModel"+id+"数据实体对象不存在!");
			JPA.setRollbackOnly();
			return null;
		}
		return creditApply;
	}
	public static void aduit(t_credit_apply creditApply) throws Exception {
		//--------------------------------------验证
		if(creditApply==null) {
			throw new Exception("额度申请审核失败,没有对应的额度申请单!");
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("额度申请审核失败,管理员没有登录");
		}
		if(!new ArrayList(){{add(4L);}}.contains(creditApply.product_id)) {
			throw new Exception("额度申请审核失败,产品类型错误");
		}
		if(creditApply.items_amount==null || creditApply.items_amount.compareTo(BigDecimal.ZERO)<=0) {
			throw new Exception("额度申请审核失败,没有项目总金额");
		}
		if(creditApply.apply_credit_amount==null || creditApply.apply_credit_amount.compareTo(BigDecimal.ZERO)<=0) {
			throw new Exception("额度申请审核失败,没有申请额度");
		}
		if(creditApply.audit_credit_amount==null || creditApply.audit_credit_amount.compareTo(BigDecimal.ZERO)<=0) {
			throw new Exception("额度申请审核失败,没有审批额度");
		}
		if(!new ArrayList(){{add(-1);add(0);add(1);}}.contains(creditApply.apply_period_unit)) {
			throw new Exception("额度申请审核失败,没有借款期限单位");
		}
		if(creditApply.apply_period==null || creditApply.apply_period<1) {
			throw new Exception("额度申请审核失败,没有借款期限");
		}
		if(creditApply.apply_apr==null || creditApply.apply_apr.compareTo(BigDecimal.ZERO)<=0) {
			throw new Exception("额度申请审核失败,没有审批利息");
		}
		if(creditApply.service_cost_rate==null || creditApply.service_cost_rate.compareTo(BigDecimal.ZERO)<0) {
			throw new Exception("额度申请审核失败,没有服务费率");
		}
		if(!new ArrayList(){{add(1);}}.contains(creditApply.service_cost_rule)) {
			throw new Exception("额度申请审核失败,错误的服务费计算规则");
		}
		if(!new ArrayList(){{add(1);}}.contains(creditApply.service_payment_model)) {
			throw new Exception("额度申请审核失败,错误的服务费支付方式");
		}
		if(new t_dict_bid_repayment_types().getEnumById(creditApply.repayment_type_id)==null) {
			throw new Exception("额度申请审核失败,错误的返款方式");
		}
		if(creditApply.audit_credit_amount.compareTo(creditApply.apply_credit_amount)>0) {
			throw new Exception("额度申请审核失败,审批额度不能大于申请金额");
		}
		if(!creditApply.canAudit()) {
			throw new Exception("额度申请审核失败,申请单当前状态不能审核");
		}
		
		//--------------------------------------操作
		//creditApply.remark;
		if(creditApply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			if(creditApply.machine_score==null) {
				throw new Exception("额度申请审核失败,没有机审分数");
			}
			creditApply.status=3;
			creditApply.update_time=new Date();
			creditApply.audit_time=creditApply.update_time;
		}else {
			throw new Exception("额度申请审核失败,操作失败");
		}
		
		if(creditApply.service_cost_rule==1) {
			creditApply.service_amount=creditApply.audit_credit_amount.multiply(creditApply.service_cost_rate).multiply(new BigDecimal(0.01));
		}
		creditApply.save();
		LogCore.create("t_credit_apply.status", creditApply.id, 2, Supervisor.currSupervisor().id, creditApply.status, creditApply.update_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(creditApply.status).enum_name
				, creditApply.remark);
	}
	public static void notThrough(t_credit_apply creditApply) throws Exception {
		//--------------------------------------验证
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("额度申请拒绝失败,管理员没有登录");
		}
		if(StringUtil.isBlank(creditApply.remark)) {
			throw new Exception("额度申请拒绝失败,没有拒绝理由");
		}
		if(!creditApply.canAudit()) {
			throw new Exception("额度申请拒绝失败,申请单当前状态不能拒绝");
		}
		//--------------------------------------操作
		if(creditApply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			creditApply.status=-3;
			creditApply.update_time=new Date();
			creditApply.audit_time=creditApply.update_time;
			
			//亿美贷产品,如果所有额度申请都被拒绝,可以从新上传图片
			boolean allCreditApplyIsNotThrough=CreditApplyService.allCreditApplyIsNotThrough(creditApply.user_id,null);
			if(allCreditApplyIsNotThrough) {
				t_users user=t_users.findById(creditApply.user_id);
				user.credit_apply_file_status=0;
				user.save();
				
				//清空用户风控资料
				//上传用户风控资料时,查看fileRelationType.3 users用户风控资料  上传情况
				Integer fileRelationType=3;
				List<t_file_relation> fileRelationList=FileService.getFileShowByRelation(fileRelationType,user.id);
				for(t_file_relation fileRelation:fileRelationList) {
					fileRelation.delete();
				}
			}
		}else {
			throw new Exception("额度申请拒绝失败,操作失败");
		}
		
		creditApply.save();
		LogCore.create("t_credit_apply.status", creditApply.id, 2, Supervisor.currSupervisor().id, creditApply.status, creditApply.update_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(creditApply.status).enum_name
				, creditApply.remark);
	}
 
	public static void freeze(t_credit_apply creditApply) throws Exception {
		//--------------------------------------验证
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("冻结额度失败,管理员没有登录");
		}
		if(!creditApply.canFreeze()) {
			throw new Exception("冻结额度失败,额度当前状态不能冻结");
		}
		//--------------------------------------操作
		if(creditApply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			creditApply.status=4;
			creditApply.update_time=new Date();
		}else {
			throw new Exception("冻结额度失败,操作失败");
		}
		
		creditApply.save();
		LogCore.create("t_credit_apply.status", creditApply.id, 2, Supervisor.currSupervisor().id, creditApply.status, creditApply.update_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(creditApply.status).enum_name
				, creditApply.remark);
	}
	
	public static void unfreeze(t_credit_apply creditApply) throws Exception {
		//--------------------------------------验证
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("解冻额度失败,管理员没有登录");
		}
		if(!creditApply.canUnfreeze()) {
			throw new Exception("解冻额度失败,额度当前状态不能冻结");
		}
		//--------------------------------------操作
		if(creditApply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			creditApply.status=3;
			creditApply.update_time=new Date();
		}else {
			throw new Exception("解冻额度失败,操作失败");
		}
		
		creditApply.save();
		LogCore.create("t_credit_apply.status", creditApply.id, 2, Supervisor.currSupervisor().id, creditApply.status, creditApply.update_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(creditApply.status).enum_name
				, creditApply.remark);
	}
	
	public static void close(t_credit_apply creditApply) throws Exception {
		//--------------------------------------验证
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("关闭额度失败,管理员没有登录");
		}
		if(!creditApply.canClose()) {
			throw new Exception("关闭额度失败,额度当前状态不能关闭");
		}
		//--------------------------------------操作
		if(creditApply.product_id.equals(new t_new_product().getEnumByCode("YI").id)) {
			creditApply.status=5;
			creditApply.update_time=new Date();
		}else {
			throw new Exception("关闭额度失败,操作失败");
		}
		
		creditApply.save();
		LogCore.create("t_credit_apply.status", creditApply.id, 2, Supervisor.currSupervisor().id, creditApply.status, creditApply.update_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_apply.status").get(creditApply.status).enum_name
				, creditApply.remark);
	}
	
	/**
	 * 提额申请提交
	 * @param creditApplyId
	 * @param increaseAmount
	 * @param images
	 * @param relationType
	 * @param error
	 */
	public static void increaseCreditApply(Long creditApplyId, BigDecimal increaseAmount, String images, int relationType,ErrorInfo error) {
		t_credit_apply apply = t_credit_apply.findById(creditApplyId);
		if(apply == null) {
			error.code = -1;
			error.msg = "该笔额度申请不存在！";
			return;
		}
		if(apply.status != 3) {
			error.code = -1;
			error.msg = "该笔额度申请未通过终审！";
			return;
		}
		
		t_credit_increase_apply creditIncreaseApply=CreditApplyService.getCreditIncreaseApplyByCreditApplyId(creditApplyId);
		if(creditIncreaseApply != null && !creditIncreaseApply.isNotPass()) {
			error.code = -1;
			error.msg = "该笔额度申请已存在提额申请！";
			return;
		}
		
		if(increaseAmount.add(apply.audit_credit_amount).compareTo(apply.apply_credit_amount) > 0 ) { //提额额度+ 审批额度 > 申请额度
			error.code = -1;
			error.msg = "提额额度超出申请额度！";
			return;
		}
		
		t_credit_increase_apply increase_apply = new t_credit_increase_apply();
		increase_apply.credit_apply_id = creditApplyId;
		increase_apply.apply_time = new Date();
		increase_apply.prepare_credit_amount = apply.audit_credit_amount;//提额前额度, 为申请时审批的额度
		increase_apply.apply_credit_amount = apply.audit_credit_amount.add(increaseAmount);//新申请额度:prepare_credit_amount+申请提额额度 
		increase_apply.status = 0; //待审
		increase_apply.save();
		
		//提额申请提交的照片
		try {
			if(!StringUtils.isEmpty(images)) {
				FileHelperService.saveImageInfo(images, increase_apply.id,  relationType);
			}
		} catch (Exception e) {
			error.code = -1;
			error.msg = "提额申请照片上传失败！";
			return;
		}
	}
	
	public static void aduitIncreaseApply(t_credit_increase_apply creditIncreaseApply,String audit_status) throws Exception {
		int status = Integer.parseInt(audit_status);
		//--------------------------------------验证
		if(creditIncreaseApply==null) {
			throw new Exception("额度申请审核失败,没有对应的额度申请单!");
		}
		Supervisor supervisor = Supervisor.currSupervisor();
		if(supervisor==null) {
			throw new Exception("额度申请审核失败,管理员没有登录");
		}
		
		Long creditApplyId = creditIncreaseApply.credit_apply_id;
		t_credit_apply creditApply = t_credit_apply.findById(creditApplyId);
		if(status > 0) {
			if(creditIncreaseApply.apply_credit_amount==null || creditIncreaseApply.apply_credit_amount.compareTo(BigDecimal.ZERO)<=0) {
				throw new Exception("提额申请审核失败,申请额度为为空");
			}
			if(creditIncreaseApply.audit_credit_amount==null || creditIncreaseApply.audit_credit_amount.compareTo(BigDecimal.ZERO)<=0) {
				throw new Exception("提额申请审核失败,确认提额额度为空");
			}
			if(creditIncreaseApply.audit_credit_amount==null || creditIncreaseApply.audit_credit_amount.compareTo(creditApply.audit_credit_amount)<=0) {
				throw new Exception("提额申请审核失败,提额后额度小于等于现有额度");
			}
			if(creditIncreaseApply.audit_credit_amount.compareTo(creditApply.apply_credit_amount)>0) {
				throw new Exception("提额申请审核失败,审批额度不能大于申请金额");
			}
			if(creditIncreaseApply.audit_credit_amount.compareTo(creditIncreaseApply.apply_credit_amount)>0) {
				throw new Exception("提额申请审核失败,审批额度不能大于提额申请金额");
			}
		}
		if(!creditIncreaseApply.canAudit()) {
			throw new Exception("提额申请审核失败,申请单当前状态不能审核");
		}
		//--------------------------------------操作 
		creditIncreaseApply.audit_time= new Date();
		creditIncreaseApply.status = status;
		creditIncreaseApply.save();
		if(creditIncreaseApply.isPass()) {
			creditApply.audit_credit_amount=creditIncreaseApply.audit_credit_amount;
			creditApply.save();
		}
		
		LogCore.create("t_credit_increase_apply.status", creditIncreaseApply.id, 2, Supervisor.currSupervisor().id, creditIncreaseApply.status, creditIncreaseApply.audit_time
				, t_enum_map.getEnumCodeMapByTypeName("t_credit_increase_apply.status").get(creditIncreaseApply.status).enum_name
				, creditIncreaseApply.remark);
	}
 
}
