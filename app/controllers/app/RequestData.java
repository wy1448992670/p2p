package controllers.app;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.*;
import models.*;
import models.channel.t_appstore;
import models.core.t_new_product;
import models.file.t_file_relation_dict;
import models.core.t_interest_rate;
import models.core.t_org_project;
import models.core.t_service_cost_rate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.libs.Codec;
import play.libs.WS;
import play.mvc.Finally;
import services.activity.ActivityIncreaseRateService;
import services.trade.BidService;
import sun.java2d.pipe.SpanShapeRenderer.Simple;
import services.business.CreditApplyService;
import services.file.FileService;
import services.ymd.BorrowerInfoService;
import services.ymd.FileHelperService;
import services.ymd.OrganizationService;
import utils.Arith;
import utils.AuthenticationUtil;
import utils.Base64;
import utils.CharUtil;
import utils.CompressStringUtil;
import utils.DateUtil;
import utils.EmailUtil;
import utils.ErrorInfo;
import utils.FileUtil;
import utils.IDCardValidate;
import utils.IDNumberUtil;
import utils.JPAUtil;
import utils.JSONUtils;
import utils.NumberUtil;
import utils.PageBean;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import utils.ServiceFee;
import utils.TimeUtil;
import utils.TransferUtil;
import utils.baofoo.AgreementBanks;
import utils.baofoo.business.ConfirmPay;
import utils.baofoo.business.ConfirmSign;
import utils.baofoo.business.ReadyPay;
import utils.baofoo.business.ReadySign;
import utils.baofoo.util.Log;
import utils.evi.util.SceneHelper;
import utils.pay.BankEn;
import utils.pay.bank.Authentication;
import business.Ads;
import business.Agency;
import business.AuditItem;
import business.AuthReq;
import business.AuthResp;
import business.BackstageSet;
import business.Bid;
import business.Bid.Purpose;
import business.Bid.Repayment;
import business.BidImages;
import business.BidQuestions;
import business.BidUserRisk;
import business.Bill;
import business.BillInvests;
import business.BorrowApply;
import business.CompanyUserAuthReviewBusiness;
import business.CreditLevel;
import business.Debt;
import business.DebtTransfer;
import business.DictBanksDate;
import business.Invest;
import business.MallAddress;
import business.MallGoods;
import business.MallScroeRecord;
import business.NewCity;
import business.NewProvince;
import business.News;
import business.NewsType;
import business.Optimization.UserOZ;
import business.OverBorrow;
import business.Posts;
import business.Product;
import business.RedPackage;
import business.RedPackageHistory;
import business.Score;
import business.SecretQuestion;
import business.StationLetter;
import business.TemplateEmail;
import business.User;
import business.UserActions;
import business.UserAddressList;
import business.UserAuditItem;
import business.UserAuthReview;
import business.UserBankAccounts;
import business.UserCitys;
import business.UserCpsProfit;
import business.UserRisk;
import business.Vip;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.mchange.v2.async.StrandedTaskReporting;
import com.shove.Xml;
import com.shove.security.Encrypt;

import constants.Constants.DeleteType;
import constants.Constants.NewsTypeId;
import constants.Constants.RechargeType;
import constants.Constants.SystemSupervisor;
import controllers.front.account.CompanyAuth;
import dao.ymd.OrganizationDao;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * APP数据处理类
 * Description:对app端传过来的参数进行处理并返回数据
 * @author zhs
 * vesion: 6.0 
 * @date 2014-10-29 上午11:34:47
 */
public class RequestData {
	/**
	 * 借款人标识
	 */
	static String BORROW = "0";
	/**
	 * 投资人标识
	 */
	static String INVEST = "1";
    /**
     * 判断系统是否授权
     * @return 
     * @throws IOException
     */
	public static String checkAuthorize() throws IOException{
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		jsonMap.put("error", "-4");
		jsonMap.put("msg", "此版本非正版授权，请联系晓风软件购买正版授权！");
		

		return JSONUtils.printObject(jsonMap+"");
	}
	
	/**
	 * 查询借款标非会员列表
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryAllbids(Map<String, String> parameters) throws IOException{
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		
		String apr = (String)parameters.get("apr");
		String amount = (String)parameters.get("amount");
		String loanSchedule = (String)parameters.get("loanSchedule");
		String startDate = (String)parameters.get("startDate");
		String endDate = (String)parameters.get("endDate");
		String loanType = (String)parameters.get("loanType");
		String minLevelStr = (String)parameters.get("minLevelStr");
		String maxLevelStr = (String)parameters.get("maxLevelStr");
		String orderType = (String)parameters.get("orderType");
		String keywords = (String)parameters.get("keywords");
		
		PageBean<v_front_all_bids> bids = Invest.queryAllBidsNotRepay(Constants.SHOW_TYPE_2, currPage, pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevelStr, maxLevelStr, orderType, keywords, error,null);

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		List<t_bid_publish> t = new ArrayList<t_bid_publish>();
		//是否查询显示公告标
		if(currPage == 1){
			t= Invest.getPublicList();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("totalNum", bids.totalCount);
		map.put("list",bids.page);
		map.put("publishList", t);
		return  JSONObject.fromObject(map).toString();
	}
	
	/**
	 * 查询会员代列表
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryUserbids(Map<String, String> parameters) throws IOException{
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		
		String apr = (String)parameters.get("apr");
		String amount = (String)parameters.get("amount");
		String loanSchedule = (String)parameters.get("loanSchedule");
		String startDate = (String)parameters.get("startDate");
		String endDate = (String)parameters.get("endDate");
		String loanType = (String)parameters.get("loanType");
		String minLevelStr = (String)parameters.get("minLevelStr");
		String maxLevelStr = (String)parameters.get("maxLevelStr");
		String orderType = (String)parameters.get("orderType");
		String keywords = (String)parameters.get("keywords");
		
		PageBean<v_front_all_bids_v2> bids = Invest.queryAllBidsNotRepayV3(Constants.SHOW_TYPE_2, currPage, pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevelStr, maxLevelStr, orderType, keywords, error,"1");
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("totalNum", bids.totalCount);
		map.put("list",bids.page);
		
		return  JSONObject.fromObject(map).toString();
	}
	
	
	/**
	 * 借款标详情
	 * @param parameters
	 * @return
	 */
	public static String bidDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String borrowIdStr = parameters.get("borrowId");
		String userIdStr = parameters.get("userId");
		
		if(StringUtils.isBlank(borrowIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "借款id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		long bidId = Long.parseLong(borrowIdStr);
		
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
		Bid bid = new Bid();
		bid.id=bidId;
		
		long userId = 0;
		t_user_attention_users attentionUser = null;
		long attentionCode = 0;
		Long bidRiskId=bid.bidRiskId;;
		Long userRiskId=null;
		if(StringUtils.isNotBlank(userIdStr)) {
			userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0 || userId < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析用户id有误");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			attentionUser = User.queryAttentionUser(userId, bid.userId, error);
			attentionCode = Bid.isAttentionBid(userId, bidId);
			
			User user=new User();
			user.id=userId;
			userRiskId=user.riskType==null?null:user.riskType.longValue();
		}
		Map<String,Object> riskMap=new HashMap<String,Object>();
		List<String> userRiskFitList=new ArrayList<String>();
		String bidRiskName="";
		String userRiskName="";
		String adviseQuota="";
		if(bidRiskId!=null){
			t_bid_risk bidRisk=t_bid_user_risk.getAllBidRiskMap().get(bidRiskId);
			bidRiskName=bidRisk.name;
			for(t_bid_user_risk bidUserRisk:bidRisk.getBidUserRiskList()){
				if(bidUserRisk.quota.compareTo(BigDecimal.ZERO)>0){
					userRiskFitList.add(bidUserRisk.user_risk_name);
				}
			}
			if(userRiskId!=null){
				t_bid_user_risk bidUserRisk=BidUserRisk.getRiskByBidAndUser(bidRiskId, userRiskId);
				userRiskName= bidUserRisk.user_risk_name;//用户风险等级
				adviseQuota=bidUserRisk.quota.toString();//建议   0:不可投,1~99999,>=100000无限额
			}
		}
		riskMap.put("bidRiskName", bidRiskName);//标的风险等级
		riskMap.put("bidRiskFitList", userRiskFitList);//适配的用户风险等级
		riskMap.put("userRiskName",userRiskName);//用户风险等级
		riskMap.put("adviseQuota", adviseQuota);//建议   0:不可投,1~99999,>=100000无限额
		jsonMap.put("riskMap", riskMap);
		
		Map<String,String> historySituationMap = User.historySituation(bid.userId,error);//借款者历史记录情况
		//List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bid.userId, bid.mark); // 用户正对产品上传的资料集合
		List<v_user_audit_items> uaItems = UserAuditItem.queryUserAuditItem(bid.userId, bid.mark, error);
		
		List<BidImages> bidImageList=null;
		try {
			bidImageList=BidImages.getBidImagesByBidId(bidId);
		} catch (Exception e) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "获取标的图片失败");
			e.printStackTrace();
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(bidImageList!=null && bidImageList.size()>0){//新的标的图片表BidImages
			for(BidImages bidImage:bidImageList){
				Map<String,Object> itemMap = new HashMap<String, Object>();
				itemMap.put("AuditSubjectName", bidImage.title);
				itemMap.put("auditStatus", "通过审核");
				itemMap.put("imgpath", bidImage.bid_image_url);
				itemMap.put("statusNum", 2);//老表UserAuditItem.status,2:通过审核
				itemMap.put("isVisible", true);
				items.add(itemMap);
			}
		}else if(uaItems != null && uaItems.size() > 0){//老的用户产品审核表UserAuditItem
			for(v_user_audit_items audit_item : uaItems){
				if(audit_item.is_visible && (audit_item.status == 2 || audit_item.status == 3)){
					//查询是否投资
					UserAuditItem item = new UserAuditItem();
					item.lazy = true;
					item.userId = bid.userId;
					item.mark = audit_item.mark;
					if(item.auditItem.type == 1){
						List<t_user_audit_items> tuaItems=item.getObjectItems();
						if(tuaItems != null && tuaItems.size() != 0) {
							for(t_user_audit_items tuaItem:tuaItems){
								UserAuditItem uItem = new UserAuditItem();
								uItem.id=tuaItem.id;
								Map<String,Object> itemMap = new HashMap<String, Object>();
								itemMap.put("AuditSubjectName", uItem.auditItem.name);
								itemMap.put("auditStatus", uItem.strStatus);
								itemMap.put("imgpath", uItem.imageFileName);
								itemMap.put("statusNum", uItem.status);
								itemMap.put("isVisible", uItem.isVisible);
								items.add(itemMap);
							}
						}
					}
				}
			}
		}
		PageBean<v_invest_records> pageBean = Invest.queryBidInvestRecords(1, Constants.APP_PAGESIZE, bidId,error);
		
		jsonMap.put("attentionId", attentionUser == null ? "" : attentionUser.id);
		jsonMap.put("attentionBidId", attentionCode <= 0 ? "" : attentionCode);
		jsonMap.put("borrowid", bidId);
		jsonMap.put("borrowTitle", bid.title);
		jsonMap.put("borrowStatus", bid.status);
		jsonMap.put("purpose", bid.purpose.name);
		jsonMap.put("borrowtype", bid.product.smallImageFilename);//图片
		jsonMap.put("schedules", bid.loanSchedule);
		jsonMap.put("borrowAmount", bid.amount);
		jsonMap.put("annualRate", bid.apr);
		jsonMap.put("period", bid.period);
		jsonMap.put("periodUnit", bid.periodUnit);
		jsonMap.put("imageFilename", bid.imageFilename);
		
		if(bid.periodUnit == -1){
			jsonMap.put("deadline", bid.period+"年");
		}else if(bid.periodUnit == 0){
			jsonMap.put("deadline", bid.period+"个月");
		}else{
			jsonMap.put("deadline", bid.period+"天");
		}
		jsonMap.put("isQuality", bid.isQuality);
		jsonMap.put("paymentType", bid.repayment.id);
		jsonMap.put("paymentMode", bid.repayment.name);
		jsonMap.put("paymentTime", bid.recentRepayTime+"");
		
		if(bid.isAgency && bid.isShowAgencyName){
			jsonMap.put("associates", bid.agency.name);
		}else{
			jsonMap.put("associates", "");
		}
		
		jsonMap.put("remainTime", bid.investExpireTime+"");//结束时间
		jsonMap.put("borrowerId", bid.userId);
		jsonMap.put("borrowerheadImg", bid.user.photo);
		jsonMap.put("creditRating", bid.user.myCredit.imageFilename);//图片
		jsonMap.put("borrowername", bid.user.name);
		jsonMap.put("vipStatus", bid.user.vipStatus);
		
		jsonMap.put("borrowSuccessNum", historySituationMap.get("successBidCount"));
		jsonMap.put("borrowFailureNum",historySituationMap.get("flowBids"));
		jsonMap.put("repaymentNormalNum",historySituationMap.get("normalRepaymentCount"));
		jsonMap.put("repaymentOverdueNum",historySituationMap.get("overdueRepaymentCount"));
		jsonMap.put("borrowDetails", bid.description);
		jsonMap.put("CBOAuditDetails", bid.auditSuggest==null?"":bid.auditSuggest);
		
		jsonMap.put("registrationTime", bid.user.time+"");
		jsonMap.put("SuccessBorrowNum",historySituationMap.get("successBidCount"));
		jsonMap.put("NormalRepaymentNum", historySituationMap.get("normalRepaymentCount"));
		jsonMap.put("OverdueRepamentNum", historySituationMap.get("overdueRepaymentCount"));//图片
		jsonMap.put("reimbursementAmount", historySituationMap.get("pendingRepaymentAmount"));
		jsonMap.put("BorrowingAmount", historySituationMap.get("loanAmount"));
		
		jsonMap.put("FinancialBidNum", historySituationMap.get("financialCount"));
		jsonMap.put("paymentAmount", historySituationMap.get("receivingAmount"));
		jsonMap.put("BorrowingAmount", historySituationMap.get("loanAmount"));
		
		jsonMap.put("bonusType",bid.bonusType);//奖励方式
		jsonMap.put("bonus",bid.bonus);//固定奖金
		jsonMap.put("awardScale",bid.awardScale);//比列奖金
		
		jsonMap.put("no",bid.no);
		jsonMap.put("bidIdSign",bid.sign);
		jsonMap.put("bidUserIdSign",bid.signUserId);
		
		jsonMap.put("minInvestAmount",bid.minInvestAmount);
		jsonMap.put("averageInvestAmount",bid.averageInvestAmount);
		jsonMap.put("hasInvestedAmount",bid.hasInvestedAmount);
		jsonMap.put("bidCount",pageBean.totalCount);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", items);
		
		jsonMap.put("tag", bid.tag);
		jsonMap.put("is_only_new_user", bid.isOnlyNewUser);

		
		//投资详情信息披露
		jsonMap.put("guaranteePath", Constants.NewsTypeId.GUARANTEE_ID);//安全保障
		jsonMap.put("riskWarningPath", Constants.NewsTypeId.RISK_WARNING_ID);//风险提示
		jsonMap.put("debitCreditPath", Constants.NewsTypeId.DEBIT_CREDIT_ID);//借款协议
		
		jsonMap.put("repaymentSource",bid.repayment_source);//还款来源
		//jsonMap.put("relatedCosts",bid.related_costs);//相关费用
		jsonMap.put("relatedCosts","只针对借款人收取");//相关费用
		Long creditAmount = 0L;
		if(bid.creditAmount!=null || bid.user.creditAmount != null) {
			creditAmount = bid.creditAmount!=null?bid.creditAmount.longValue() :bid.user.creditAmount.longValue();
		}
		jsonMap.put("creditAmount",creditAmount);//授信金额


		Map<String, Object> activityInfo = new HashMap<>();
		List<Map<String, Object>> activityInfos = BidService.getActivityInfoForCurrentBid(bid.id);
		if(activityInfos!=null && activityInfos.size()>0){
			activityInfo = activityInfos.get(0);
			String s = activityInfo.get("isIncreaseRate").toString();
			if( StringUtils.isEmpty(s.trim())){
				bid.isIncreaseRate= false;
			}else {
				if(s.trim().equals("1")){
					bid.isIncreaseRate= true;
				}else{
					bid.isIncreaseRate= false;
				}
			}
			bid.increaseRate = Double.parseDouble(activityInfo.get("increaseRate").toString());
			bid.increaseRateName =  activityInfo.get("increaseRateName").toString();
		}


		//加息
		jsonMap.put("isIncreaseRate",bid.isIncreaseRate);
		jsonMap.put("increaseRate",bid.increaseRate);
		jsonMap.put("increaseRateName",bid.increaseRateName);
		jsonMap.put("is_debt_transfer",bid.isDebtTransfer);


		JSONObject jsonObject=JSONObject.fromObject(jsonMap);
		jsonObject.put("borrower", borrowerDetail(bid.user));//借款人信息

		jsonObject.put("activityInfo", activityInfo);//借款人信息
		return jsonObject.toString();
	}
	
	public static String borrowerDetail(Long borrowerUserId){
		User borrower=new User();
		borrower.id=borrowerUserId;
		return borrowerDetail(borrower);
	}
	public static String borrowerDetail(User borrower){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		//借款人信息
		jsonMap.put("userTypeStr",UserTypeEnum.getEnumByCode(borrower.user_type).getName());//主体性质
		/*
		String borrowerRealityName=borrower.realityName;
		if(borrowerRealityName==null || borrowerRealityName.length()==0){
			borrowerRealityName="*";
		}else if(borrowerRealityName.length()==1){
			borrowerRealityName=borrowerRealityName+"*";
		}else{
            if(UserTypeEnum.getEnumByCode(borrower.user_type)==UserTypeEnum.COMPANY || UserTypeEnum.getEnumByCode(borrower.user_type) == UserTypeEnum.INDIVIDUAL) {
        		borrowerRealityName = borrowerRealityName.substring(0, 2) + "***"
						+ (borrowerRealityName.contains("有限")
								? borrowerRealityName.substring(borrowerRealityName.lastIndexOf("有限"))
								: "");
			} else {
				borrowerRealityName = borrowerRealityName.substring(0, 1) + "*" + borrowerRealityName.substring(2);
			}
		}
		*/
		
		jsonMap.put("realityNameX",User.hideRealityName(borrower.realityName));//姓名
		jsonMap.put("sexStr",borrower.sex);//性别
		jsonMap.put("age",borrower.age<0?"":(borrower.age+""));//年龄
		
		String borrowerMaritalStr="";
		List<t_dict_maritals> maritals=User.queryAllMaritals();
		for(t_dict_maritals marital:maritals){
			if(marital.id==borrower.maritalId){
				borrowerMaritalStr=marital.name;
			}
		}
		jsonMap.put("maritalStr",borrowerMaritalStr);//婚姻状况
		jsonMap.put("idNumberX",User.hideIdNumber(borrower.idNumber));//身份证号
		
		
		// add by wangyun 20181108
		jsonMap.put("educationName",borrower.educationName);//文化程度
		jsonMap.put("birthday",borrower.birthday);//企业注册时间
		jsonMap.put("houseName",borrower.houseName);//购房情况 
		jsonMap.put("carName",borrower.carName);//购车
		System.err.println("borrower.cityId： " + borrower.cityId+"------------------");
		if(StringUtils.isNotBlank(borrower.cityId+"") && borrower.cityId != 0) {
			t_dict_ad_citys citys = t_dict_ad_citys.findById(Long.valueOf(borrower.cityId));
			t_dict_ad_provinces provinces =  t_dict_ad_provinces.findById(Long.valueOf(citys.province_id));
			jsonMap.put("liveCity", (provinces!= null?provinces.name:"") + (citys!= null?citys.name:""));//居住地
			
		}
		
		t_user_city userCity = t_user_city.find(" user_id = ? " , borrower.id).first() ;
		String city =userCity==null?"":(userCity.province==null?"":userCity.province) + (userCity.city==null?"":userCity.city);
		jsonMap.put("city", city);//所在地
		jsonMap.put("overdueCnt", borrower.overdueCnt);//历史逾期数
		jsonMap.put("overdueAmount", borrower.overdueAmount);//历史逾期额
		jsonMap.put("currentOverdueAmount", borrower.currentOverdueAmount);//当前逾期额
		jsonMap.put("lawSuit", borrower.lawSuit);//涉诉情况
		jsonMap.put("creditReport", borrower.creditReport);//征信情况
		
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[]{"industry"}); 
		jsonConfig.setIgnoreDefaultExcludes(false);
		//jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
		t_users_info userInfo = borrower.getUserInfo();
		userInfo.legal_person = User.hideRealityName(userInfo.legal_person);//2:法人代表 3:经营者 带星隐藏		
		JSONObject jsonString = JSONObject.fromObject(userInfo, jsonConfig);
		jsonMap.put("userInfo", jsonString);
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 查询借款标投标记录
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryBidInvestRecords(Map<String, String> parameters) throws IOException{
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		String bidIdStr = parameters.get("borrowId");
		
		if(StringUtils.isBlank(bidIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "借款标id参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		long bidId = Long.parseLong(bidIdStr);
		
		PageBean<v_invest_records> pageBean = Invest.queryBidInvestRecords(currPage, pageSize, bidId,error);
		List<v_invest_records> page = pageBean.page;
		if(null != page) {
			for(v_invest_records record : page) {
				String name = record.name;
				if(null != name && name.length() > 1) {
					if(StringUtils.isNumeric(name) && name.length() == 11){
						record.name = record.name.substring(0, 3) + "***"+record.name.substring(7);
					}else{
						record.name = record.name.substring(0, 1) + "***";
					}
					
				}
			}
		}
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg","查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("totalNum",  pageBean.totalCount);
		map.put("list",page);
		return  JSONObject.fromObject(map).toString();
	}
	
	/**
	 * 查询借款标提问记录
	 * @param parameters
	 * @return
	 */
	public static String addQuestion(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String userIdStr = parameters.get("id");
		String bidIdStr = parameters.get("borrowId");
		String content = parameters.get("questions");
		String toUserIdStr = parameters.get("bidUserIdSign");
		
		if (StringUtils.isBlank(userIdStr)) {
			map.put("error", "-2");
			map.put("msg", "请传入用户ID");
			return JSONObject.fromObject(map).toString();
		}
		if (StringUtils.isBlank(bidIdStr)) {
			map.put("error", "-3");
			map.put("msg", "请传入借款标ID");
			return JSONObject.fromObject(map).toString();
		}
		if (StringUtils.isBlank(toUserIdStr)) {
			map.put("error", "-3");
			map.put("msg", "请传入被提问用户ID");
			return JSONObject.fromObject(map).toString();
		}
		if (StringUtils.isBlank(content)) {
			map.put("error", "-3");
			map.put("msg", "请输入提问内容");
			return JSONObject.fromObject(map).toString();
		}
		
		long bidId = Long.parseLong(bidIdStr);
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			map.put("error", "-2");
			map.put("msg", "用户的id解析有误");
			return JSONObject.fromObject(map).toString();
		}
		long toUserId = Security.checkSign(toUserIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || toUserId < 0){
			map.put("error", "-3");
			map.put("msg", "被提问用户ID解析有误");
			return JSONObject.fromObject(map).toString();
		}
		
		BidQuestions question = new BidQuestions();
		question.bidId = bidId;
		question.userId = userId;
		question.time = new Date();
		question.content = content;
		question.questionedUserId = toUserId;
		
		int result = question.addQuestion(userId, error);
		
		if(result < 0){
			map.put("error", -8);
			map.put("msg",error.msg);
		}else{
			map.put("error", -1);
			map.put("msg",error.msg);
		}
		
		return  JSONObject.fromObject(map).toString(); 
	}
	
	/**
	 * 投标操作
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String invest(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String borrowIdStr = parameters.get("borrowId");
		String userIdStr = parameters.get("userId");
		String amountStr = parameters.get("amount");
		String dealPwd = parameters.get("dealPwd");
		RedPackageHistory redPackage = null;
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		User user = new User();
		user.id = userId;
		if(user.financeType!=null&&user.financeType==FinanceTypeEnum.BORROW.getCode()) {
			map.put("error", "-8");
			map.put("msg", "您当前身份是借款用户，不能投标！");
			return JSONObject.fromObject(map).toString();
		}

		//红包ID
		String redPackageId = parameters.get("redPackageId");
		
		if (StringUtils.isBlank(amountStr)) {
			map.put("error", "-3");
			map.put("msg", "请输入投标金额");
			return JSONObject.fromObject(map).toString();
		}
		
		if (StringUtils.isBlank(borrowIdStr)) {
			map.put("error", "-3");
			map.put("msg", "请传入借款标ID");
			return JSONObject.fromObject(map).toString();
		}
		if (StringUtils.isBlank(userIdStr)) {
			map.put("error", "-2");
			map.put("msg", "请传入用户ID");
			return JSONObject.fromObject(map).toString();
		}
		
		boolean b=amountStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		map.put("error", "-3");
    		map.put("msg", "对不起！投标金额只能是正整数！");
			return JSONObject.fromObject(map).toString();
    	} 
    	

		
    	if(error.code < 0 || userId < 0){
			map.put("error", "-2");
			map.put("msg", "解析用户id有误");
			return JSONObject.fromObject(map).toString();
		}
    	double redAmount = 0;
    	//解析红包ID
    	if (!StringUtils.isBlank(redPackageId)) {
    		
    		//long redId =  Security.checkSign(redPackageId, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
    		long redId =  Long.parseLong(redPackageId);
        	
    		if(redId < 0){
    			map.put("error", "-3");
    			map.put("msg", "解析红包id有误");
    			return JSONObject.fromObject(map).toString();
    		}
        	
        	redPackage = new RedPackageHistory();
        	
        	redPackage.id = redId;
        	redAmount = redPackage.money;
        	if(redPackage.status != Constants.RED_PACKAGE_STATUS_UNUSED){
        		map.put("error", "-3");
    			map.put("msg", "红包已经失效，或已使用!");
    			return JSONObject.fromObject(map).toString();
        	}
        	//平均分数金额
        	String averageInvestAmount = parameters.get("averageInvestAmount");
        	
        	if(Double.parseDouble(averageInvestAmount) > 0){
        		//如果投标金额小于红包金额则投资失败
            	if(redPackage.money > new BigDecimal(amountStr).multiply(new BigDecimal(averageInvestAmount)).doubleValue()){
            		map.put("error", "-3");
        			map.put("msg", "投资金额必须大于红包金额");
        			return JSONObject.fromObject(map).toString();
            	}
        	}else{
        		//如果投标金额小于红包金额则投资失败
            	if(redPackage.money > Double.valueOf(amountStr)){
            		map.put("error", "-3");
        			map.put("msg", "投资金额必须大于红包金额");
        			return JSONObject.fromObject(map).toString();
            	}
        	}
        	
        	 
		}
    	
    	

		double investTotal = Double.parseDouble(amountStr);
		if (!(user.isEmailVerified || user.isMobileVerified)) {
			map.put("error", "-888");
			map.put("msg", "用户未激活账号");
			return JSONObject.fromObject(map).toString();
		}
		
		long bidId = Long.parseLong(borrowIdStr);
		int amount = Integer.parseInt(amountStr);
		
		dealPwd = Encrypt.decrypt3DES(dealPwd, Constants.ENCRYPTION_KEY);
		
		Bid bids = new Bid();
		bids.id = bidId;
		investTotal += redAmount;
		
		if(bids.bidRiskId !=null) {
			Map<String, Object> riskLimit = Invest.queryRiskLimit(error, Double.parseDouble(investTotal+"") ,bids,user);
			BigDecimal hasInvest = (BigDecimal) riskLimit.get("invesTotalSum");//已投资金额
			BigDecimal quota = (BigDecimal) riskLimit.get("quota"); 
			List<String> canInvestBidName = (List<String>) riskLimit.get("canInvestBidName");
			List<invest_quota> listQuota = (List<invest_quota>) riskLimit.get("listQuota");
			boolean is_risk_invest = (boolean) riskLimit.get("is_risk_invest");
//			List<t_bid_user_risk> bid_user = t_user_risk.getAllUserRiskMap().get(Long.valueOf(user.riskType)).getBidUserRiskList();
			if(!is_risk_invest) {
				map.put("error", "-20");
				map.put("msg", "未达出借要求！");
				map.put("canInvestBidName", canInvestBidName);
				map.put("list", listQuota);
				map.put("risk_result", user.risk_result);//风险测评結果
				return JSONObject.fromObject(map).toString(); 
			}
			
			/*// 累计投资金额超过风险评估限制金额
	    	if(invesTotalSum.compareTo(quota) == 1) {// invesTotalSum > quota
	    		map.put("error", "-20");
				map.put("msg", "您的额度已用完！");
				map.put("canInvestBidName", canInvestBidName);
				map.put("list", listQuota);
				map.put("risk_result", user.risk_result);//风险测评結果
				return JSONObject.fromObject(map).toString(); 
	    	}*/
	    	
	    	// 累计'已投资'金额超过风险评估限制金额
	    	if(hasInvest.compareTo(quota) >= 0) {// 已投资金额 大于等于 限额
	    		map.put("error", "-20");
				map.put("msg", "您的额度已用完！");
				map.put("canInvestBidName", canInvestBidName);
				map.put("list", listQuota);
				map.put("risk_result", user.risk_result);//风险测评結果
				return JSONObject.fromObject(map).toString(); 
	    	}
	    	
	    	//quota.subtract(hasInvest)剩余金额 
	    	if(quota.subtract(hasInvest).compareTo(new BigDecimal(investTotal) ) == -1) {//剩余金额大于投资金额
	    		map.put("error", "-20");
				map.put("msg", "您出借的额度超过剩余可投！");
				map.put("canInvestBidName", canInvestBidName);
				map.put("list", listQuota);
				map.put("risk_result", user.risk_result);//风险测评結果
				return JSONObject.fromObject(map).toString(); 
				 
	    	}
		} 
		
		
		Invest.invest(userId, bidId, amount, dealPwd, false, Constants.CLIENT_APP,redPackage, error);

		if(error.code == -999){
			map.put("error", "-999");
			map.put("msg", "您余额不足，请充值");
		} else if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", error.msg);
		}else{
			map.put("error", -1);
			map.put("msg", "投标成功");
		} 
		
		return JSONObject.fromObject(map).toString(); 
	}
	
	
	/**
	 * 查询借款标提问以及回答列表
	 * @return
	 */
	public static String queryAllQuestions(Map<String, String> parameters){
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		Map<String,Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String borrowIdStr = parameters.get("borrowId");
		
		if(StringUtils.isBlank(borrowIdStr)){
			map.put("error", "-3");
    		map.put("msg", "请传入借款标ID！");
			return JSONObject.fromObject(map).toString();
		}
		
		long bidId = Long.parseLong(borrowIdStr);
		PageBean<BidQuestions> page = BidQuestions.queryQuestion(currPage, pageSize, bidId, "", Constants.SEARCH_ALL, -1, error);
		List<BidQuestions> list = page.page;
		if(null != list) {
			for(BidQuestions question : list) {
				String name = question.name;
				if(null != name && name.length() > 1) {
					question.name = question.name.substring(0, 1) + "***";
				}
			}
		}
		if(error.code < 0){
			map.put("error", -4);
			map.put("msg", "查询失败");
			return JSONObject.fromObject(map).toString();
		}
		
		map.put("questionList", list);
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("questionsNum", page.totalCount);
		return JSONObject.fromObject(map).toString();
	}
	
	
	/**
	 * 投标详情
	 * @param parameters
	 * @return
	 */
	public static String investDetail(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String borrowIdStr = parameters.get("borrowId");
		String idStr = parameters.get("id");
		
		if(StringUtils.isBlank(borrowIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入借款标ID！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(idStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long bidId = Long.parseLong(borrowIdStr);
		
		long id = Security.checkSign(idStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || id < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		/*APP进入详情页面增加浏览次数*/
		Invest.updateReadCount(bidId, error);
		
		Bid bid = new Bid();
		bid.createBid = true;
		bid.id = bidId;
		
		if(bid.id < 0) {
			jsonMap.put("isDealPassword", false);
		} else {
			jsonMap.put("isDealPassword", bid.product.isDealPassword);
		}
		
		User user = new User();
		user.id = id;
		
		jsonMap.put("borrowid", bidId);
		
		if(null != bid.userName && bid.userName.length() > 1) {
			bid.userName = bid.userName.substring(0,1) + "***";
		}
		jsonMap.put("Name", bid.userName);
		jsonMap.put("creditRating", user.myCredit.imageFilename);//图片
		jsonMap.put("accountAmount", user.balance + user.freeze);
		jsonMap.put("availableBalance", user.balance);
		jsonMap.put("schedules", bid.loanSchedule);
		
		jsonMap.put("borrowAmount", bid.amount);
		jsonMap.put("apr", bid.apr);
		jsonMap.put("period", bid.period);
		jsonMap.put("periodUnit", bid.periodUnit);
		jsonMap.put("repayType", bid.repayment.id);
		
		if(bid.periodUnit == -1){
			jsonMap.put("deadline", bid.period+"年");
		}else if(bid.periodUnit == 0){
			jsonMap.put("deadline", bid.period+"个月");
		}else{
			jsonMap.put("deadline", bid.period+"天");
		}
		
		if(user.payPassword != null){
			jsonMap.put("payPassword", true);
		}else{
			jsonMap.put("payPassword", false);
		}
		//查询当前用户红包可用数量
		List<t_red_packages_history> tRed = new ArrayList<t_red_packages_history>();
		tRed = t_red_packages_history.find(" user_id = ? and status = 0", id).fetch();
		
		jsonMap.put("error","-1");
		jsonMap.put("redCount",tRed.size());
		jsonMap.put("msg","投标详情查询成功");
		jsonMap.put("title",bid.title);
		jsonMap.put("paymentMode",bid.repayment.name);
		jsonMap.put("paymentTime", bid.recentRepayTime + "");
		jsonMap.put("InvestmentAmount", bid.hasInvestedAmount);
		jsonMap.put("needAmount",bid.amount - bid.hasInvestedAmount);
		jsonMap.put("minTenderedSum", bid.minAllowInvestAmount);
		jsonMap.put("investNum", bid.investCount);
		jsonMap.put("views", bid.readCount);
		jsonMap.put("isDealPassword", bid.product.isDealPassword);
		jsonMap.put("averageInvestAmount",bid.averageInvestAmount);
		jsonMap.put("needAccount",bid.averageInvestAmount > 0 ? Arith.round((bid.amount-bid.hasInvestedAmount)/bid.averageInvestAmount,0) :  0);

		List<Map<String, Object>> activityInfos =  BidService.getActivityInfoForCurrentBid(bid.id);
		Map<String, Object> activityInfo =new HashMap<>();

		if(activityInfos!=null && activityInfos.size()>0){
			activityInfo = activityInfos.get(0);
			String s = activityInfo.get("isIncreaseRate").toString();
			if( StringUtils.isEmpty(s.trim())){
				bid.isIncreaseRate= false;
			}else {
				if(s.trim().equals("1")){
					bid.isIncreaseRate= true;
				}else{
					bid.isIncreaseRate= false;
				}
			}
			bid.increaseRate = Double.parseDouble(activityInfo.get("increaseRate").toString());
			bid.increaseRateName =  activityInfo.get("increaseRateName").toString();
		}

		jsonMap.put("activityInfo",activityInfo);

		//加息
		jsonMap.put("isIncreaseRate",bid.isIncreaseRate);
		jsonMap.put("increaseRate",bid.increaseRate);
		jsonMap.put("increaseRateName",bid.increaseRateName);
		
		jsonMap.put("debitCreditPath", Constants.NewsTypeId.DEBIT_CREDIT_ID);//借款协议
		jsonMap.put("disclosureCommitmentPath", Constants.NewsTypeId.DISCLOSURE_COMMITMENT_ID);//风险揭示书及禁止行为承诺
		jsonMap.put("signatureAuthorizationPath", Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID);//电子签章自动签署授权协议
		jsonMap.put("lenderServicePath", Constants.NewsTypeId.LENDER_SERVICE_ID);//出借人服务协议
		/*
		jsonMap.put("debitCreditPath", "/front/principal/principalGuaranteeAppHome");//借款协议
		jsonMap.put("disclosureCommitmentPath", "/front/principal/principalGuaranteeAppHome");//风险揭示书及禁止行为承诺
		jsonMap.put("signatureAuthorizationPath", "/front/principal/principalGuaranteeAppHome");//电子签章自动签署授权协议
		*/
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 查询所有债权
	 * @param parameters
	 * @return
	 */
	public static String queryAllDebts(Map<String, String> parameters){
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		
		String apr = (String)parameters.get("apr");
		String debtAmount = (String)parameters.get("debtAmount");
		String loanType = (String)parameters.get("loanType");
		String orderType = (String)parameters.get("orderType");
		String keywords = (String)parameters.get("keywords");
		
		if(StringUtils.isBlank(apr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "年利率有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(debtAmount)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "金额有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		if(StringUtils.isBlank(loanType)){
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "类型有误！");
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		if(StringUtils.isBlank(orderType)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "排序有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		if(StringUtils.isBlank(keywords)){
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "关键字有误");
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		PageBean<v_front_all_debts>  page = Debt.queryAllDebtTransfersNotSuccess( currPage,pageSize,loanType, debtAmount, apr, orderType,keywords,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Map<String, Object>  map = new HashMap<String, Object>();
		map.put("list", page.page);
		map.put("totalNum", page.totalCount);
		map.put("error", -1);
		map.put("msg", "查询成功");
		
		return JSONObject.fromObject(map).toString();
	}
	
	/**
	 * 债权转让标详情
	 * @param parameters
	 * @return
	 */
	public static String debtDetail(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("id");
		String userIdStr = parameters.get("userId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "请传入债权ID！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		long debtId = Long.parseLong(debtIdStr);
		
		Debt debt = new Debt();
		debt.id = debtId;
		
		Long investUserId = Debt.getInvestUserId(debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", -4);
			jsonMap.put("msg", "查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		Map<String,String>  debtUserhistorySituationMap = User.debtUserhistorySituation(investUserId,error);//债权者历史记录情况
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Long bidUserId = Debt.getBidUserId(debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Map<String,String> historySituationMap = User.historySituation(bidUserId,error);//借款者历史记录情况
		
		if(error.code < 0){
			jsonMap.put("error", -4);
			jsonMap.put("msg", "查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = 0;
		t_user_attention_users attentionUser = null;
		long attentionDebtId = 0;
		
		if(StringUtils.isNotBlank(userIdStr)) {
			userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0 || userId < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析用户id出现错误");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			attentionUser = User.queryAttentionUser(userId, investUserId, error);
			attentionDebtId = Debt.isAttentionDebt(userId, debtId, error);
		}
		
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bidUserId, debt.invest.bid.mark); // 用户正对产品上传的资料集合
		
		if(uItems.size() > 0){
			for(int i = 0;i < uItems.size();i++){
				Map<String,Object> itemMap = new HashMap<String, Object>();
				itemMap.put("AuditSubjectName", uItems.get(i).auditItem.name);
				itemMap.put("auditStatus", uItems.get(i).strStatus);
				itemMap.put("imgpath", uItems.get(i).imageFileName);
				itemMap.put("isVisible", uItems.get(i).isVisible);
				items.add(itemMap);
			}
		}
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("attentionId", attentionUser == null ? "" : attentionUser.id);
		map.put("attentionDebtId", attentionDebtId <= 0 ? "" : attentionDebtId);
		map.put("creditorid", debt.id);
		map.put("creditorTitle", debt.title);
		map.put("creditorStatus", debt.status);
		map.put("remainTime", debt.endTime + "");
		map.put("principal", debt.debtAmount);
		map.put("auctionBasePrice", debt.transferPrice);
		map.put("creditorReason", debt.transerReason);
		
		map.put("borrowerId", debt.invest.bid.user.sign);
		map.put("borrowerheadImg", debt.invest.user.photo);
		map.put("creditRating", debt.invest.user.myCredit.imageFilename);
		map.put("borrowername", debt.invest.user.name);
		map.put("vipStatus",  debt.invest.user.vipStatus);
		
		map.put("borrowSuccessNum", debtUserhistorySituationMap.get("successBidCount"));
		map.put("borrowFailureNum", debtUserhistorySituationMap.get("flowBids"));
		map.put("repaymentNormalNum", debtUserhistorySituationMap.get("normalRepaymentCount"));
		map.put("repaymentOverdueNum", debtUserhistorySituationMap.get("overdueRepaymentCount"));
		
		map.put("borrowDetails", debt.invest.bid.description);
		map.put("CBOAuditDetails", debt.invest.bid.auditSuggest);
		map.put("registrationTime", debt.invest.user.time + "");
		
		map.put("SuccessBorrowNum", historySituationMap.get("successBidCount"));
		map.put("NormalRepaymentNum", historySituationMap.get("normalRepaymentCount"));
		map.put("OverdueRepamentNum", historySituationMap.get("overdueRepaymentCount"));
		map.put("reimbursementAmount", historySituationMap.get("pendingRepaymentAmount"));
		
		map.put("BorrowingAmount", historySituationMap.get("loanAmount"));
		map.put("FinancialBidNum", historySituationMap.get("financialCount"));
		map.put("paymentAmount", historySituationMap.get("receivingAmount"));
		
		map.put("bonusType",debt.invest.bid.bonusType);//奖励方式
		map.put("bonus",debt.invest.bid.bonus);//固定奖金
		map.put("awardScale",debt.invest.bid.awardScale);//比列奖金
		
		map.put("amount",debt.invest.bid.amount);
		map.put("corpus",debt.invest.amount);//
		map.put("maxOfferPrice",debt.maxOfferPrice);//目前拍价
		map.put("apr",debt.invest.bid.apr);
		map.put("receiveMoney",debt.map.get("receive_money"));
		map.put("hasReceiveMoney",debt.map.get("has_receive_money"));//
		map.put("remainReceiveMoney",debt.map.get("remain_receive_money"));
		map.put("receiveCorpus",debt.map.get("receive_corpus"));
		map.put("hasOverdue",debt.invest.bid.hasOverdue);//
		map.put("receiveTime",debt.map.get("receive_time"));
		
		map.put("sign",debt.sign);//债权加密ID
		map.put("debtUserIdSign",debt.invest.userIdSign);//债权所有者加密ID
		
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("list", items);
		
		map.put("debtNo", debt.no);
		
		return JSONObject.fromObject(map).toString();
	}
	
	/**
	 * 债权竞拍记录
	 * @param parameters
	 * @return
	 */
	public static String debtAuctionRecords(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String debtIdStr = parameters.get("creditorId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "请传入债权ID！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		long debtId = Long.parseLong(debtIdStr);
		
		PageBean<v_debt_auction_records> page = Debt.queryDebtAllAuctionRecords( currPage,  pageSize, debtId,error);
		List<v_debt_auction_records> list = page.page;
		if(null != list) {
			for(v_debt_auction_records record : list) {
				String name = record.name;
				if(null != name && name.length() > 1) {
					record.name = record.name.substring(0, 1) + "***";
				}
			}
		}
		if(error.code < 0){
			jsonMap.put("error", -4);
			jsonMap.put("msg", "查询出现异常，给您带来的不便敬请谅解！");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum",page.totalCount);
		jsonMap.put("list", list);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	
	/**
	 * 获取竞拍相关信息接口
	 * @param parameters
	 * @return
	 */
	public static String auctionDebtDetail(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("creditorId");
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", -2);
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		long debtId = Long.parseLong(debtIdStr);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		jsonMap.put("isDealPassword", debtBussiness.invest.bid.product.isDealPassword);
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", -2);
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debt = new Debt();
		debt.id = debtId;
		
		
		User user = new User();
		user.id = userId;
		
		
		//判断用户是否需要交易密码
		if(user.payPassword != null){
			jsonMap.put("payPassword", true);
		}else{
			jsonMap.put("payPassword", false);
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("creditorid", debtId);
		
		if(null != debt.invest.user.name && debt.invest.user.name.length() > 1) {
			debt.invest.user.name = debt.invest.user.name.substring(0, 1) + "***";
		}
		
		jsonMap.put("Name", debt.invest.user.name);
		jsonMap.put("creditRating", user.myCredit.imageFilename);
		jsonMap.put("accountAmount", user.balance + user.freeze);
		jsonMap.put("availableBalance", user.balance);
		jsonMap.put("principal",debt.debtAmount);
		//jsonMap.put("isDealPassword", bid.isDealPassword);
		jsonMap.put("auctionBasePrice", debt.transferPrice);
		
		jsonMap.put("title", debt.title);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	
	/**
	 * 债权竞拍
	 * @param parameters
	 * @return
	 */
	public static String auction(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String debtIdStr = parameters.get("creditorId");
		String userIdStr = parameters.get("id");
		String amountStr = parameters.get("amount");
		String dealpwdStr = parameters.get("dealPwd");
		
		if (StringUtils.isBlank(amountStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入竞拍金额");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if (StringUtils.isBlank(debtIdStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if (StringUtils.isBlank(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		boolean b=amountStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！竞拍价格只能是正整数！");
			return JSONObject.fromObject(jsonMap).toString();
    	} 
		
		int amount = Integer.parseInt(amountStr);
		
		long debtId = Long.parseLong(debtIdStr);
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);;
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		User user = new User();
		user.id = userId;
		
		if (!(user.isEmailVerified || user.isMobileVerified)) {
			jsonMap.put("error", "-888");
			jsonMap.put("msg", "用户未激活账号");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		dealpwdStr = Encrypt.decrypt3DES(dealpwdStr, Constants.ENCRYPTION_KEY);
		
		Debt.auctionDebt(userId, amount, debtId,dealpwdStr, Constants.CLIENT_APP, error);
		
		if(error.code == -999 ){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONObject.fromObject(jsonMap).toString();
		} else if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		} else{
			jsonMap.put("error", "-1");
			jsonMap.put("msg","竞拍成功");
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 理财子账户--理财账单
	 * @return
	 */
	public static String investBills(Map<String, String> parameters){
		
		
		int currPage = 1;
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String payTypeStr = parameters.get("payType");
		String isOverTypeStr = parameters.get("isOverType");
		String keyTypeStr = parameters.get("keyType");
		String key = parameters.get("key");
		String userIdStr = parameters.get("id");
		
		if(!(NumberUtil.isNumericInt(payTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传人参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!(NumberUtil.isNumericInt(payTypeStr) || NumberUtil.isNumericInt(isOverTypeStr) || NumberUtil.isNumericInt(keyTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传人参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!(NumberUtil.isNumericInt(payTypeStr) || NumberUtil.isNumericInt(isOverTypeStr) || NumberUtil.isNumericInt(keyTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传人参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(NumberUtil.isNumericInt(parameters.get("currPage"))) {
 			currPage = Integer.parseInt(parameters.get("currPage"));
 		}
		
		int payType = Integer.parseInt(payTypeStr);
		int isOverType = Integer.parseInt(isOverTypeStr);
		int keyType = Integer.parseInt(keyTypeStr);
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_bill_invest> page = BillInvests.queryMyInvestBills(payType, isOverType, keyType, key, currPage,userId, error);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		
		
		jsonMap.put("list", page.page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 账单详情
	 * @param parameters
	 * @return
	 */
	public static String billDetail(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		String userIdStr = parameters.get("user_id");
		String billIdStr = parameters.get("billId");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(billIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入账单ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long billId = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || billId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析账单id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(billId, userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String userName = User.queryUserNameById(userId, error);

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("userName", userName);
		jsonMap.put("billTitle", investDetail.title);
		jsonMap.put("dueDate", investDetail.receive_time + "");
		jsonMap.put("billId", investDetail.sign);
		jsonMap.put("billDate", investDetail.audit_time + "");
		jsonMap.put("platformName", currBackstageSet.platformName);
		jsonMap.put("hotline", currBackstageSet.platformTelephone);
		jsonMap.put("user_id", investDetail.user_id);
		jsonMap.put("billNo", investDetail.invest_number);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 本期账单明细
	 * @param parameters
	 * @return
	 */
	public static String currentBillDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		if(StringUtils.isBlank(parameters.get("user_id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "传入用户ID有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String userIdStr = parameters.get("user_id");
		String billIdStr = parameters.get("billId");
		
		if(StringUtils.isBlank(billIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入账单ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		long userId = Long.(userIdStr);
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long billId = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || billId < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "解析账单id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(billId, userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}

		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("repayAmount", investDetail.should_received_amount);
		jsonMap.put("expiryDate", investDetail.receive_time + "");
		jsonMap.put("repayWay", investDetail.repayment_type);
		jsonMap.put("repayCapital", investDetail.invest_amount);
		jsonMap.put("annualRate", investDetail.apr);
		
		jsonMap.put("interestSum", investDetail.current_receive_amount);
		jsonMap.put("receivedAmount", investDetail.has_received_amount);
		jsonMap.put("receivedNum", investDetail.has_received_periods);
		jsonMap.put("remainNum", investDetail.loan_periods - investDetail.has_received_periods);
		jsonMap.put("remainAmount", investDetail.should_received_amount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 账单借款标详情
	 * @param parameters
	 * @return
	 */
	public static String  billBidDetail(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String userIdStr = parameters.get("id");
		String billIdStr = parameters.get("billId");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(billIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入账单ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long billId = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || billId < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "解析账单id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(billId, userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("borrowTitle", investDetail.title);
		jsonMap.put("borrowAmount", investDetail.amount);
		jsonMap.put("interestSum", investDetail.current_receive_amount);
		jsonMap.put("borrowNum", investDetail.loan_periods);
		jsonMap.put("annualRate", investDetail.apr);
		jsonMap.put("eachPayment", investDetail.should_received_amount);
		jsonMap.put("paidPeriods", investDetail.has_received_periods);
		jsonMap.put("remainPeriods", investDetail.loan_periods - investDetail.has_received_periods);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 账单历史收款情况
	 * @return
	 */
	public static String historicalRepayment(Map<String, String> parameters){
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		List<Map<String,Object>> investBills = new ArrayList<Map<String,Object>>();
		
		String userIdStr = parameters.get("user_id");
		String billIdStr = parameters.get("billId");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(billIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入账单ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long billId = Security.checkSign(billIdStr, Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || billId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析账单id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(billId, userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<t_bill_invests> page = BillInvests.queryMyInvestBillReceivables(investDetail.bid_id,investDetail.user_id, investDetail.invest_id, currPage, pageSize, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int totle = page.page.size();
		
		if(totle > 0){
			for(int i = 0;i < totle;i++){
				t_bill_invests bill = page.page.get(i);
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("borrowTitle", bill.title);
				map.put("repayAmount", bill.receive_amount);
				map.put("isOverdue", bill.status);
				map.put("isRepay", bill.status);
				investBills.add(map);
			}
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum",  page.totalCount);
		jsonMap.put("list", investBills);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 理财子账户--投标记录
	 * @param parameters
	 * @return
	 */
	public static String investRecords(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		
		int pageSize = Constants.APP_PAGESIZE;
		String type = "0";
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String userIdStr = parameters.get("id");
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_invest_records> page = Invest.queryUserInvestRecords(userId, currPage + "",pageSize + "", type, "",error);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 理财子账户--等待满标的理财标
	 * @return
	 */
	public static String queryUserAllloaningInvestBids(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		Map<String,Object> jsonMap = new JSONObject();
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_user_waiting_full_invest_bids> page = Invest.queryUserWaitFullBids(userId, null, null, currPage, pageSize,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 理财子账户---收款中的理财标列表
	 * @param parameters
	 * @return
	 */
	public static String queryUserAllReceivingInvestBids(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		PageBean<v_receiving_invest_bids> page = Invest.queryUserAllReceivingInvestBids(userId, null, null, currPage, pageSize,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "系统异常，给您带来的不便敬请谅解");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", page.page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 转让债权
	 * @return
	 */
	public static String transferDebt(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "传入用户id参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("transferTitle"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标题有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("transferBP"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入参数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("transferWay"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "转让方式有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("transferPeriods"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "转让期数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("transferReason"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "转让原因有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("invest_id"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "投资id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		System.out.println("invest_id"+parameters.get("invest_id"));
		String userIdStr = parameters.get("id");
		String transferTitle = parameters.get("transferTitle");
		String transferBPStr = parameters.get("transferBP");
		String transferWayStr = parameters.get("transferWay");
		String transferPeriodsStr = parameters.get("transferPeriods");
		String transferReason = parameters.get("transferReason");
		String assigneeName = parameters.get("assigneeName");
		long investId = Long.parseLong(parameters.get("invest_id"));
		
		boolean b = transferBPStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		jsonMap.put("error", -3);
    		jsonMap.put("msg", "对不起！转让底价只能输入正整数");
			return JSONObject.fromObject(jsonMap).toString();
			
    	} 
    	
    	if(StringUtils.isBlank(transferTitle) || StringUtils.isBlank(transferPeriodsStr) || StringUtils.isBlank(transferReason) || StringUtils.isBlank(transferBPStr) ||
    			StringUtils.isBlank(transferWayStr)){
    		jsonMap.put("error", -3);
    		jsonMap.put("msg", "对不起！请正确设置各种参数");
			return JSONObject.fromObject(jsonMap).toString();
    	}
    	
    	if(error.code < 0 || investId < 0){
    		jsonMap.put("error", -3);
    		jsonMap.put("msg", "投资id有误");
    		
			return JSONObject.fromObject(jsonMap).toString();
    	}
    	
    	long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
    	if(error.code < 0){
    		jsonMap.put("error", "-2");
    		jsonMap.put("msg", "解析用户id有误");
    		
			return JSONObject.fromObject(jsonMap).toString();
    	}
    	
		double transferBP = Double.parseDouble(transferBPStr);
		int transferWay = Integer.parseInt(transferWayStr);
		int transferPeriods = Integer.parseInt(transferPeriodsStr);
		
		double debtAmount = Debt.getDebtAmount(Long.parseLong(parameters.get("invest_id")),error);
		
		Debt.transferDebt(userId, investId, transferTitle, transferReason, transferPeriods, debtAmount, transferBP, transferWay, assigneeName, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
    		jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
    		jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 理财子账户--已成功的理财标
	 * @param parameters
	 * @return
	 */
	public static String queryUserSuccessInvestBids(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error","-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_user_success_invest_bids> page = Invest.queryUserSuccessInvestBids(userId, null, null, currPage, pageSize,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", page.page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让管理
	 * @param parameters
	 * @return
	 */
	public static String queryUserAllDebtTransfers(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error","-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_debt_user_transfer_management> page = Debt.queryUserAllDebtTransfersByConditions( userId, null, null, null, currPage,pageSize);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", page.page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让成功详情页面
	 * @return
	 */
	public static String debtDetailsSuccess(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.APP_VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(debtId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("transferStatus", debt.status);
		jsonMap.put("transferType", debt.type);
		jsonMap.put("assigneeName", debtBussiness.invest.user.name);
		jsonMap.put("successTransferTime", debtBussiness.transactionTime + "");
		
		if(debt.remain_received_corpus == null){
			jsonMap.put("collectCapital", 0);
		}else{
			jsonMap.put("collectCapital", debt.remain_received_corpus);
		}
		
		jsonMap.put("collectBid", debtBussiness.transactionPrice);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让中详情页面
	 * @param parameters
	 * @return
	 */
	public static String debtDetailsTransfering(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(debtId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("bidRemainTime", debt.end_time + "");
		jsonMap.put("hightestBid", debtBussiness.maxOfferPrice);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让不通过详情页面
	 * @param parameters
	 * @return
	 */
	public static String debtDetailsNoPass(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(debtId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("auditResult", debt.status);
		jsonMap.put("auditTime", debtBussiness.startTime +"");
		jsonMap.put("nopassReason", debtBussiness.noThroughReason);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让详情
	 * @param parameters
	 * @return
	 */
	public static String debtTransferDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(debtId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("transferTitle", debtBussiness.title);
		jsonMap.put("transferBP", debt.transfer_price);
		jsonMap.put("transferDeadline", debt.end_time + "");
		jsonMap.put("transferReason", debtBussiness.transerReason);
		jsonMap.put("receiveCorpus",debtBussiness.map.get("receive_corpus"));
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权转让借款标详情页面
	 * @param parameters
	 * @return
	 */
	public static String debtTransferBidDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_transfer_management debt = Debt.transferDetails(debtId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debtId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("borrowid", debt.bid_id);
		jsonMap.put("borrowerName", debt.name);
		jsonMap.put("borrowType", debtBussiness.invest.bid.product.name);
		jsonMap.put("borrowTitle", debt.title);
		jsonMap.put("bidCapital", debtBussiness.invest.amount);
		jsonMap.put("annualRate", debt.apr);
		jsonMap.put("interestSum", debt.receiving_amount);
		jsonMap.put("receivedAmount", debt.has_received_amount);
		jsonMap.put("expiryDate", debtBussiness.invest.bid.recentRepayTime + "");
		jsonMap.put("collectCapital", debt.remain_received_corpus);
		jsonMap.put("remain_received_amount", debt.remain_received_amount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 成交债权
	 * @param parameters
	 * @return
	 */
	public static String transact(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		String dealpwd = parameters.get("dealpwd");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error","-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}

		Debt.dealDebtTransfer(null, debtId, dealpwd,false,error);
		
		if(error.code == -999){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONObject.fromObject(jsonMap).toString();
		}else if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 债权用户初步成交债权，之后等待竞拍方确认成交
	 * @param sign
	 */
	public static String firstDealDebt(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String creditorIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(creditorIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(creditorIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error","-2");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}

		Debt.firstDealDebt(debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 查询债权竞拍记录
	 * @param parameters
	 * @return
	 */
	public static String queryAuctionRecords(Map<String, String> parameters){
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String sign = parameters.get("sign");
		
		if(StringUtils.isBlank(sign)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error","-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_debt_auction_records> page = Invest.viewAuctionRecords(currPage,pageSize, debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list",page.page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 查询用户受让债权管理列表
	 * @param parameters
	 * @return
	 */
	public static String queryUserAllReceivedDebtTransfers(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg","解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_debt_user_receive_transfers_management> page = Debt.queryUserAllReceivedDebtTransfersByConditions(userId, null, null,  currPage,pageSize);
		
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询正常");
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 受让债权的详情 [竞拍成功]
	 * @return
	 */
	public static String receiveDebtDetailSuccess(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("signId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(debtId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("assigneeStatus", debt.status);
		jsonMap.put("assigneeWay", debt.type);
		jsonMap.put("assigneeName", debtBussiness.invest.user.name);
		jsonMap.put("successTransferTime", debt.transaction_time + "");
		jsonMap.put("collectCapital", debtBussiness.debtAmount);
		jsonMap.put("collectBid", debtBussiness.transactionPrice);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 受让债权的详情 [竞拍中]
	 * @return
	 */
	public static String receiveDebtDetailAuction(Map<String, String> parameters){
		
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("signId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(debtId);
		
		//目前我的竞拍出价
		Double offerPrice = Debt.getMyAuctionPrice(debt.transer_id, debt.user_id, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("assigneeStatus", debt.status);
		jsonMap.put("assigneeWay", debt.type);
		jsonMap.put("collectCapital", debtBussiness.debtAmount);
		jsonMap.put("hightestBid", debtBussiness.maxOfferPrice);
		jsonMap.put("offerPrice", offerPrice);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 债权受让详情 [竞拍成功,竞拍中,定向转让]
	 * @return
	 */
	public static String receiveDebtDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("signId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(debtId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("transferName", debtBussiness.invest.user.name);
		jsonMap.put("transferTitle", debtBussiness.title);
		jsonMap.put("transferBP", debt.transfer_price);
		jsonMap.put("transferDeadline", debtBussiness.endTime + "");
		jsonMap.put("transferReason", debtBussiness.transerReason);
		jsonMap.put("debtAmount", debtBussiness.debtAmount);
		jsonMap.put("sign", debt.sign);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 受让的借款标详情 [竞拍成功,竞拍中,定向转让]
	 * @param parameters
	 * @return
	 */
	public static String receiveDebtBidDetail(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("signId");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error","-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		v_debt_user_receive_transfers_management debt = Debt.details(debtId);
		
		Debt debtBussiness = new Debt();
		debtBussiness.id = debt.transer_id;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("borrowid",  OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error).concat(debtBussiness.invest.bidId + ""));
		jsonMap.put("borrowerName", debt.name);
		jsonMap.put("borrowType", debtBussiness.invest.bid.product.name);
		jsonMap.put("borrowTitle", debt.title);
		jsonMap.put("bidCapital", debtBussiness.invest.amount);
		jsonMap.put("annualRate", debt.apr);
		jsonMap.put("expiryDate", debtBussiness.invest.bid.recentRepayTime + "");
		
		if (debt.status != 1) {
			jsonMap.put("interestSum", debt.receiving_amount);
			jsonMap.put("remain_received_amount", debt.remain_received_amount);
			jsonMap.put("receivedAmount", debt.has_received_amount);
			jsonMap.put("collectCapital", debt.remain_received_corpus);
		} else {
			jsonMap.put("interestSum", debt.receiving_amount_success);
			jsonMap.put("remain_received_amount", debt.remain_received_amount_success);
			jsonMap.put("receivedAmount", debt.has_received_amount_success);
			jsonMap.put("collectCapital", debt.remain_received_corpus_success);
		}
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 加价竞拍
	 * @param parameters
	 * @return
	 */
	public static String increaseAuction(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
 		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("sign");
		String userIdStr = parameters.get("id");
		String offerPriceStr = parameters.get("NewBid");
		String dealpwdStr = parameters.get("dealpwd");
		
//		if(StringUtils.isBlank(dealpwdStr)) {
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "请输入交易密码");
//			
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(offerPriceStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起，出价不能为空");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		boolean b=offerPriceStr.matches("^[1-9][0-9]*$");
		
    	if(!b){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起，出价只能输入正整数");
			
			return JSONObject.fromObject(jsonMap).toString();
    	} 
    	
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int offerPrice = Integer.parseInt(offerPriceStr);
		
		Debt.auctionDebt(userId, offerPrice, debtId, dealpwdStr, Constants.CLIENT_APP, error);
		
		if(error.code == -999 ){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONObject.fromObject(jsonMap).toString();
		}else if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg",error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg",error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 接受定向转让债权
	 * @param parameters
	 * @return
	 */
	public static String acceptDebts(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("sign");
		String dealpwd = parameters.get("dealpwd");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt.dealDebtTransfer(null, debtId,dealpwd,false, error);
		
		if(error.code == -999){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONObject.fromObject(jsonMap).toString();
		}else if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
			
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 拒绝接受定向债权转让
	 * @param parameters
	 * @return
	 */
	public static String notAccept(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt.refuseAccept(debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 理财情况统计表
	 */
	public static String investStatistics(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = parameters.get("id");
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		List<v_bill_invest_statistics> statistic = v_bill_invest_statistics.find(" user_id = ?", userId).fetch(1, 100);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", statistic);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 进入自动投标页面
	 * @param parameters
	 * @return
	 */
	public static String autoInvest(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		List<Map<String,Object>> creditLevelList = new ArrayList<Map<String,Object>>();
		String userIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		double balance = Invest.getUserBalance(userId);// 个人可用余额
		
		t_user_automatic_invest_options robot = Invest.getUserRobot(userId,error);
		
		if(null == robot){
			
			jsonMap.put("loanType", "");
		}else{
		
		    jsonMap.put("loanType", robot.loan_type.split(","));
		}
		
		if(null == robot){
			jsonMap.put("robot", robot);
			jsonMap.put("robotStatus", 2);
		}else{
			jsonMap.put("robot", robot);
			jsonMap.put("robotStatus", robot.status);
			if(robot.status){
				jsonMap.put("robotStatus", 1);
			}else{
				jsonMap.put("robotStatus", 0);
			}
		}
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
        List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);//获取所有信用等级
		
		if(error.code < 0){
			jsonMap.put("error","-4");
			jsonMap.put("msg", "对不起，系统异常");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int totle = creditLevels.size();
		
		if(totle > 0){
			for(int i = 0;i < totle;i++){
			  CreditLevel creditLevel = creditLevels.get(i);
			  Map<String,Object> map = new HashMap<String, Object>();
			  map.put("optionValue", creditLevel.order_sort);
			  map.put("optionText", creditLevel.name);
			  creditLevelList.add(map);
			}
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("creditLevelList",creditLevelList);
		jsonMap.put("balance", balance);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 设置投标机器人
	 * @param parameters
	 * @return
	 */
	public static String saveOrUpdateRobot(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = parameters.get("id");
		String bidAmountStr = parameters.get("bidAmount");
		String rateStartStr = parameters.get("rateStart");
		String rateEndStr = parameters.get("rateEnd");
		String deadlineStartStr = parameters.get("deadlineStart");
		String deadlineEndStr = parameters.get("deadlineEnd");
		
		String creditStartStr = parameters.get("creditStart");
		String creditEndStr = parameters.get("creditEnd");
		String remandAmountStr = parameters.get("remandAmount");
		String borrowWay = parameters.get("borrowWay");
		 
        if(StringUtils.isBlank(parameters.get("validType"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请正确设置有效期类型");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
        if(StringUtils.isBlank(parameters.get("validDate"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请正确设置有效期");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
        
        if(StringUtils.isBlank(parameters.get("minAmount"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请正确设置最小投资金额");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
        
        if(StringUtils.isBlank(parameters.get("maxAmount"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请正确设置最大投资金额");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
        
        int validType = Integer.parseInt(parameters.get("validType"));
        int validDate = Integer.parseInt(parameters.get("validDate"));
        double minAmount = Double.parseDouble(parameters.get("minAmount"));
        double maxAmount = Double.parseDouble(parameters.get("maxAmount"));
		
		if(StringUtils.isBlank(bidAmountStr) || StringUtils.isBlank(rateStartStr) || StringUtils.isBlank(rateEndStr) || StringUtils.isBlank(borrowWay)){
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请正确设置各种参数");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		boolean b=bidAmountStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！投标金额只能输入正整数");
			
			return JSONObject.fromObject(jsonMap).toString();
    	} 
		
    	
    	if(!NumberUtil.isNumericDouble(rateStartStr)){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！设置的最低利率必须是数字");
			
			return JSONObject.fromObject(jsonMap).toString();
    	}
    	
    	if(!NumberUtil.isNumericDouble(rateEndStr)){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！设置的最高利率必须是数字");
			
			return JSONObject.fromObject(jsonMap).toString();
    	}
    	
    	if(!NumberUtil.isNumeric(remandAmountStr)){
    		jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！设置的保留金额必须是数字");
			
			return JSONObject.fromObject(jsonMap).toString();
    	}
		
    	long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Invest.saveOrUpdateRobot(userId, validType, validDate, minAmount, maxAmount, bidAmountStr, rateStartStr, rateEndStr, deadlineStartStr, deadlineEndStr, creditStartStr, creditEndStr, remandAmountStr, borrowWay, error);
		
		t_user_automatic_invest_options robot = Invest.getUserRobot(userId,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "对不起！设置自动投标机器人失败");
			jsonMap.put("robotId", robot.id);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "设置自动投标机器人成功");
			jsonMap.put("robotId", robot.id);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关闭投标机器人
	 * @param parameters
	 * @return
	 */
	public static String closeRobot(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String bobotIdStr = parameters.get("robotId");
		
		if(StringUtils.isBlank(bobotIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入机器人ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("user_id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户ID有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long robotId = Long.parseLong(bobotIdStr);
		Invest.closeRobot(userId, robotId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			jsonMap.put("robotId", robotId);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			jsonMap.put("robotId", robotId);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 收藏的债权列表
	 * @param parameters
	 * @return
	 */
	public static String attentionDebts(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String keywords = parameters.get("keywords");
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
			
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_user_attention_invest_transfers> page = new PageBean<v_user_attention_invest_transfers>();
		if(StringUtils.isBlank(keywords)){
		    page = Debt.queryUserAttentionDebtTransfers(userId, currPage, null, null, pageSize,error);
			
			if(error.code < 0){
				jsonMap.put("error", "-4");
				jsonMap.put("msg", "对不起，系统异常");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}else{
			page = Debt.queryUserAttentionDebtTransfers(userId, currPage, "1", keywords, pageSize,error);
			
			if(error.code < 0){
				jsonMap.put("error", "-4");
				jsonMap.put("msg", "对不起，系统异常");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString(); 
	}
	
	/**
	 * 收藏的借款标
	 * @param parameters
	 * @return
	 */
	public static String attentionBids(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String keywords = parameters.get("keywords");
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
			
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_bid_attention> page = new PageBean<v_bid_attention>();
		
		if(StringUtils.isBlank(keywords)){
			page.totalCount = (int) v_bid_attention.count("  user_id = ?  ",userId);
			page.page = v_bid_attention.find("  user_id = ?  ",userId).fetch(currPage,pageSize);
		}else{
			page.totalCount = (int) v_bid_attention.count("  user_id = ? and  title like ?",userId,"%"+keywords+"%");
			page.page = v_bid_attention.find("  user_id = ? and  title like ?",userId,"%"+keywords+"%").fetch(currPage,pageSize);
		}
		jsonMap.put("error",-1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum",page.totalCount);
		jsonMap.put("list",page.page);
		
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 查询用户关注用户列表
	 * @param parameters
	 * @return
	 */
	public static String myAttentionUser(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
			
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_user_attention_info> page= User.queryAttentionUsers(userId, currPage, pageSize, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg","查询成功");
		jsonMap.put("totalNum",page.totalCount);
		jsonMap.put("list",page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 用户黑名单
	 * @param parameters
	 * @return
	 */
	public static String blackList(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		
		int currPage = 1;
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
			
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		PageBean<v_user_blacklist> page = User.queryBlacklist(userId, "", currPage,pageSize, error);
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 举报用户
	 * @param parameters
	 * @return
	 */
	public static String reportUser(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String bidIdSign = parameters.get("bidIdSign");
		String reportedUserIdSign = parameters.get("bidUserIdSign");
		String investTransferIdSign = parameters.get("sign");
		String reason = parameters.get("reason");
		
		if(StringUtils.isBlank(reason)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "举报原因不能为空");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "举报人ID为空");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(reportedUserIdSign)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "被举报人ID为空");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error","-2");
			jsonMap.put("msg", "解析举报人ID出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long reportedUserId = Security.checkSign(reportedUserIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || reportedUserId < 0){
			jsonMap.put("error","-2");
			jsonMap.put("msg", "解析被举报人ID出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long investTransferId = 0;
		
		if(!StringUtils.isBlank(investTransferIdSign)){
		    investTransferId = Security.checkSign(investTransferIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0 || investTransferId < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析债权id出现错误");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		
		long bidId = 0;
		if(!StringUtils.isBlank(bidIdSign)){
		    bidId = Security.checkSign(bidIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0 || bidId < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析标id出现错误");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		
		String userName = User.queryUserNameById(reportedUserId,error);
		
		User user = new User();
		user.id = userId;
		user.addReportAUser(userName, reason, bidId, investTransferId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
	}
	
	/**
	 * 拉黑对方
	 * @param parameters
	 * @return
	 */
	public static String addBlack(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String bidIdStr = parameters.get("bid_id");
		String reason = parameters.get("reason");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(bidIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入借款标ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long bidId = Long.parseLong(bidIdStr);
		
		if(error.code < 0 || bidId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		User user = new User();
		user.id = userId;
		user.addBlacklist(bidId, reason, error);
		
		if(error.code < 0){
			jsonMap.put("error", -4);
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关注用户
	 * @return
	 */
	public static String attentionUser(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		
		long attentionUserId = 0;
		
		String attentionBidUserIdStr = parameters.get("bidUserIdSign");
		String attentionDebtUserIdStr = parameters.get("debtUserIdSign");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!StringUtils.isBlank(attentionBidUserIdStr)){
			 attentionUserId = Security.checkSign(attentionBidUserIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
				
				if(error.code < 0 || attentionUserId < 0){
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "解析关注用户id出现错误");
					
					return JSONObject.fromObject(jsonMap).toString();
				}
		}
		
		if(!StringUtils.isBlank(attentionDebtUserIdStr)){
			 attentionUserId = Security.checkSign(attentionDebtUserIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
				
				if(error.code < 0 || attentionUserId < 0){
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "解析关注用户id出现错误");
					
					return JSONObject.fromObject(jsonMap).toString();
				}
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(attentionUserId == userId){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "您不能关注您自己");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long attentionId = User.attentionUser(userId, attentionUserId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("attentionId", attentionId);
			jsonMap.put("msg", "关注成功");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 收藏借款标
	 * @return
	 */
	public static String collectBid(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String bidIdStr = parameters.get("bidIdSign");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(bidIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入借款标ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long bidId = Security.checkSign(bidIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		 
		if(error.code < 0 || bidId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析标id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long attentionBidId = Bid.collectBid(userId, bidId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error",-1);
			jsonMap.put("msg", error.msg);
			jsonMap.put("attentionBidId", attentionBidId);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 收藏债权
	 * @param parameters
	 * @return
	 */
	public static String collectDebt(Map<String, String> parameters){
		
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("id");
		String debtIdStr = parameters.get("sign");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long debtId = Security.checkSign(debtIdStr, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		 
		if(error.code < 0 || debtId < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析债权id出现错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long attentionDebtId = Debt.collectDebt(userId, debtId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", error.msg);
			jsonMap.put("attentionDebtId", attentionDebtId);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 进入帮助中心页面
	 * @return
	 */
	public static String helpCenter(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String finance_type = parameters.get("financeType");
		List<v_news_types> types = NewsType.queryTypeAndCount(NewsTypeId.HELP_CENTER, error);
		
		List <t_content_news> list1 = News.queryNewsByTypeIds("13",error);
		
		List <t_content_news> list2 = News.queryNewsByTypeIds("14", error);
		
		List <t_content_news> list3 = null;
		if ( StringUtils.isEmpty(finance_type) || Integer.parseInt(finance_type) == FinanceTypeEnum.INVEST.getCode()
				 ) { //1为投资端
			list3 = News.queryNewsByTypeIds("15", error);
		}
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", types);
		jsonMap.put("list1", list1);
		jsonMap.put("list2", list2);
		jsonMap.put("list3", list3);
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 帮助中心内容列表
	 * @param parameters
	 * @return
	 */
	public static String helpCenterContent(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		int currPage = 1;
		
		String typeId = parameters.get("id");
		String currPageStr = parameters.get("currPage");
		
		if(StringUtils.isBlank(typeId)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入帮助中心栏目ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isNotBlank(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		PageBean <t_content_news> pageBean = News.queryNewsByTypeId(typeId+"", currPage +  "", pageSize + "", null, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("list", pageBean.page);
		jsonMap.put("totleNum", pageBean.totalCount);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 帮助中心列表详情
	 * @param parameters
	 * @return
	 */
	public static String helpCenterDetail(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String newsIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(newsIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入新闻ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long newsId = Long.parseLong(newsIdStr);
		News news = new News();
		news.id = newsId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("title", news.title);
		jsonMap.put("time", news.time);
		jsonMap.put("content", news.content);
		jsonMap.put("author", news.author);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 关于我们---公司介绍
	 * @param parameters
	 * @return
	 */
	public static String companyIntroduction(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String content = News.queryContent(-1004, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", "查询成功");
			jsonMap.put("content", content);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关于我们---管理团队
	 * @param parameters
	 * @return
	 */
	public static String managementTeam(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		List<String> contentList = News.queryContentList(17, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", "查询成功");
			jsonMap.put("content", contentList);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关于我们---专家顾问
	 * @param parameters
	 * @return
	 */
	public static String expertAdvisor(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		List<String> contentList = News.queryContentList(18, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", "查询成功");
			jsonMap.put("content", contentList);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关于我们---招贤纳士
	 * @param parameters
	 * @return
	 */
	public static String recruitment(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String content = News.queryContent(-1007, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", "查询成功");
			jsonMap.put("content", content);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
	}
	
	/**
	 * 关于我们---合作伙伴
	 * @param parameters
	 * @return
	 */
	public static String partners(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String currPageStr = parameters.get("currPage");
		
		int currPage = 1;
		
		if(StringUtils.isNotBlank(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		PageBean<t_content_advertisements_partner> page = News.queryPartners(currPage, pageSize, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totle", page.totalCount);
		jsonMap.put("list", page.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 获取APP版本
	 * @param parameters
	 * @return
	 */
	public static String appVersion(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String deviceTypeStr = parameters.get("deviceType");
		
		if(StringUtils.isBlank(deviceTypeStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入设备参数");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!NumberUtil.isNumeric(parameters.get("deviceType"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入正确的设备参数");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int deviceType = Integer.parseInt(parameters.get("deviceType"));
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		jsonMap.put("version", deviceType == 1 ? backstageSet.androidVersion : backstageSet.iosVersion);
		jsonMap.put("isForceAppUpdate",  deviceType == 1 ? backstageSet.androidForcedUpdate : backstageSet.iosForcedUpdate);
		jsonMap.put("code", deviceType == 1 ? backstageSet.androidCode : backstageSet.iosCode);
		jsonMap.put("path", deviceType == 1 ? "/public/yiyilicai.apk" : "/public/yiyilicai.apk");
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 获取客服热线
	 * @param parameters
	 * @return
	 */
	public static String serviceHotline(Map<String, String> parameters){
		BackstageSet  currBackstageSet = BackstageSet.getCurrentBackstageSet();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("hotline", currBackstageSet.platformTelephone);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 财富资讯新闻详情
	 * @return
	 */
	public static String newsDetail(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		String newsIdStr = parameters.get("id");
		
		if(StringUtils.isBlank(newsIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入新闻ID");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long newsId = Long.parseLong(newsIdStr);
		News news = new News();
		news.id = newsId;
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("title", news.title);
		jsonMap.put("time", news.time);
		jsonMap.put("content", news.content);
		jsonMap.put("author", news.author);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 财富资讯首页
	 * @param parameters
	 * @return
	 */
	public static String wealthinfoHome(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		List<NewsType> types = NewsType.queryChildTypes(1L, error);//获取财富资讯首页所有栏目
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		List<t_content_news> homeNews = News.queryNewForFrontHome(error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("types",types);
		jsonMap.put("ads",homeNews);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 财富资讯各个栏目下的新闻列表
	 * @param parameters
	 * @return
	 */
	public static String wealthinfoNewsList(Map<String, String> parameters){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		
		if(StringUtils.isBlank(parameters.get("currPage"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "当前页数有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		int pageSize = Constants.APP_PAGESIZE;
		String typeId = parameters.get("id");
		PageBean <t_content_news>  newsList = News.queryNewsByTypeId(typeId, parameters.get("currPage"), pageSize + "", "", error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("totleNum",newsList.totalCount);
		jsonMap.put("list", newsList.page);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 首页
	 * @param parameters
	 * @return
	 */
	public static String home(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		List<t_content_advertisements> homeAds = Ads.queryAdsByTag("APPBanner", error); // 广告条
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询广告条失败");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		List<t_content_advertisements> noticeAds = Ads.queryAdsByTag("公告", error); // 广告条-公告
		List<t_content_advertisements> popAds = Ads.queryAdsByTag("app插屏", error); // 广告条-插屏
		jsonMap.put("noticeAds", noticeAds);
		jsonMap.put("popAds", popAds);
		
		/*List<Bid> bids = Bid.queryAdvertisement(error); 
		if(null != bids) {
			for(Bid bid : bids) {
				String name = bid.userName;
				if(null != name && name.length() > 1) {
					bid.userName = bid.userName.substring(0, 1) + "***";
				}
			}
		}*/
		
		Bid bids = Bid.getFiastBids(error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询最新三条借款资讯失败");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		//List<QualityBid> qualityBids = BidOZ.queryQualityBid(3, error);//三个优质借款标
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询三个优质借款标失败");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		List<Map<String,String>> maps = Invest.queryNearlyInvest(error); // 最新投资资讯
		if(null != maps) {
			for(Map<String, String> map : maps) {
				String userName = map.get("userName");
				if(null != userName && userName.length() > 1) {
					map.put("userName", map.get("userName").substring(0, 1) + "***");
				}
			}
		}
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询 最新投资资讯失败");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		List<FullBidsApp> fullBids = BidOZ.queryFullBid(error); // 最新满标借款标
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询最新满标借款标失败");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Bid newUserBid = new Bid();
		try {
			newUserBid = Bid.getOnlyNewUserBid(error);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.info("没有新手标！");
		}

		List<v_front_all_bids> hotBids = new ArrayList<v_front_all_bids>();

//		boolean isLogin =false;
//		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
//		if(error.code ==1){
//			isLogin=true;
//		}
//
//		boolean isShow=false;
//		if(isLogin){
//
//			//查询当前用户资产
//			String sql = " SELECT aa.balance+bb.money money from(SELECT balance from t_users where id = ?) aa,(SELECT sum(receive_corpus+receive_interest) money from t_users u INNER JOIN t_bill_invests b on u.id = b.user_id where b.`status` = -1 and u.id = ?) bb ";
//			Query query = JPAUtil.createNativeQuery(sql, userId, userId);
//
//			long money = 0L;
//			Object record = null;
//			try {
//				record = query.getSingleResult();
//			} catch (Exception e) {
//				Logger.error("标->查询是否有逾期还款:" + e.getMessage());
//			}
//
//			if(record != null) {
//				money = Long.parseLong(record.toString());
//			}
//
//			if (money > 0) {
//				isShow=true;
//			}
//		}


		boolean isShow=true;
		try {
			if(isShow){
				hotBids = Invest.queryHotBids(5);//正常显示
			}else{
				hotBids = new ArrayList<v_front_all_bids>();//隐藏标的
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.info("获取人标失败！");
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("homeAds", homeAds);
		jsonMap.put("bids", bids);
		//jsonMap.put("qualityBids", qualityBids);
		//jsonMap.put("investInfo",maps);
//		jsonMap.put("fullBids", fullBids);
		//jsonMap.put("invests", maps);
		
		// 新手标
		jsonMap.put("onlyNewUserBid", newUserBid.getId() == 0 ? "" : newUserBid);
		
		// 热门标
		jsonMap.put("hotBids", hotBids);
		return JSONObject.fromObject(jsonMap).toString();
	}

	/**
	 * 用户登录(opt=1)
	 * @param name 用户名
	 * @param pwd 密码
	 * @throws IOException
	 */
	public static String login(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String name = parameters.get("name");
		String password = parameters.get("pwd");
		String userId = parameters.get("userId");
		String channelId = parameters.get("channelId");
		String deviceType = parameters.get("deviceType");
		String financeType=parameters.get("financeType");//新增参数   借款端app传值0


		if (StringUtils.isBlank(name)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入用户名");
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(password)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入密码");
			return JSONUtils.printObject(jsonMap);
		}
		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
//		if (StringUtils.isBlank(userId)) {
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "无法获取设备参数");
//			return JSONUtils.printObject(jsonMap);
//		}
//		
//		if (StringUtils.isBlank(channelId)) {
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "无法获取设备参数");
//			return JSONUtils.printObject(jsonMap);
//		}
//		
//		if (!NumberUtil.isNumeric(deviceType)) {
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "无法获取设备参数");
//			return JSONUtils.printObject(jsonMap);
//		}
		
		int device = Integer.parseInt(deviceType);
		
		if(device < 0 || device > 2) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "获取设备参数有误");
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isEmail(name)){
			user.findUserByEmail(name);
		}else if(RegexUtils.isMobileNum(name)){
			// 不为空，新版借款app登录
			if (!StringUtils.isBlank(financeType)&&BORROW.equals(financeType)) {
				user.findUserByMobile(name, FinanceTypeEnum.BORROW.getCode());
			} else if(StringUtils.isBlank(financeType)||INVEST.equals(financeType)){// 为空，旧版投资app登录
				user.findUserByMobile(name, FinanceTypeEnum.INVEST.getCode());
				financeType = INVEST;
			}

		}else{
			user.name = name;
		}
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		String isSmsCode = parameters.get("isSmsCode");
		if(StringUtils.equals(isSmsCode, "1")){
			String smsCode = parameters.get("smsCode");
			if (StringUtils.isBlank(smsCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入短信验证码");
				return JSONUtils.printObject(jsonMap);
			}
			
			String cCode = (String) Cache.get(user.mobile+"_"+financeType);
			
			if(cCode == null) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "短信校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!smsCode.equals(cCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "您输入的短信验证码错误");
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		t_users userEntity=t_users.findById(user.id);
		if(userEntity.is_virtual_user){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "此账号为虚拟账号,不能登录");
			return JSONUtils.printObject(jsonMap);
		}
		
		if (user.login(password,false, Constants.CLIENT_APP, error) < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(channelId) && NumberUtil.isNumeric(deviceType)) {
			user.updateChannel(userId, channelId, device, error);
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		boolean existBankAcc = (banks != null && banks.size() > 0);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "登录成功");
		jsonMap.put("id", user.sign);
		jsonMap.put("existBankAcc", existBankAcc);
		jsonMap.put("username", user.name);
		jsonMap.put("headImg", user.photo);
		jsonMap.put("vipStatus", user.vipStatus);
		jsonMap.put("isEmailVerified", user.isEmailVerified);
		jsonMap.put("isAddBaseInfo", user.isAddBaseInfo);
		jsonMap.put("creditRating", user.myCredit.imageFilename);
		jsonMap.put("creditLimit", user.balanceDetail.credit_line);
		jsonMap.put("accountAmount", user.balance + user.freeze);
		jsonMap.put("availableBalance", user.balance);
		
		jsonMap.put("hasEmail", StringUtils.isNotBlank(user.email));
		jsonMap.put("hasMobile",StringUtils.isNotBlank(user.mobile));
		jsonMap.put("mobile",user.mobile);
		jsonMap.put("realName",user.realityName);
		jsonMap.put("payPassword",user.payPassword == null ? false : true);
		jsonMap.put("risk_result",user.risk_result == null ? "" : user.risk_result);
		jsonMap.put("user_type",user.user_type );  //0 未知 1 个人用户  2 企业用户 3个体工商户
		
		//是否已绑卡
		jsonMap.put("isBank", user.is_bank);
		// 当前是否有卡
		jsonMap.put("hasBank", Score.hasBank(user.id));
		
		if(user.user_type == 1) { //个人用户
			jsonMap.put("hasProtocolBank", Score.hasProtocolBank(user.id));// 当前是否有协议绑卡
		
			jsonMap.put("isNotSign", UserBankAccounts.isNotSign(user.id));	//是否有未签约银行卡
		} else { //企业和个体工商户不判断,不弹框
			jsonMap.put("hasProtocolBank", true);// 当前是否有协议绑卡
			
			jsonMap.put("isNotSign", false);//是否有未签约银行卡
		}
		
		//登录成功记录用户信息
		Cache.set("userId_"+user.id, user, Constants.CACHE_TIME_HOURS_144);
		
		Cache.delete(user.mobile);
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 账户基本信息(opt=2)
	 * @param id 用户id
	 * @throws IOException
	 */
	public static String queryBaseInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		User user = new User();
		
		if (StringUtils.isBlank(parameters.get("id"))) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请求用户id参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		List<t_dict_cars> cars = t_dict_cars.findAll();
		List<t_dict_ad_provinces> provinces = t_dict_ad_provinces.findAll();
		List<t_dict_educations> educations = t_dict_educations.findAll();
		List<t_dict_houses> houses = t_dict_houses.findAll();
		List<t_dict_maritals> maritals = t_dict_maritals.findAll();
		List<t_dict_ad_citys> cityList = t_dict_ad_citys.findAll();
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "该用户存在");
		jsonMap.put("realName", user.realityName);
		jsonMap.put("email", user.email);
		jsonMap.put("sex", user.sex);
		
		if(user.age <= 0) {
			jsonMap.put("age", "");
		} else {
			jsonMap.put("age", user.age);
		}
		jsonMap.put("idNo", user.idNumber);
		jsonMap.put("higtestEdu", user.educationId);
		jsonMap.put("registedPlacePro", user.provinceId);
		jsonMap.put("maritalStatus", user.maritalId);
		jsonMap.put("housrseStatus", user.houseId);
		jsonMap.put("CarStatus", user.carId);
		jsonMap.put("cellPhone1", user.mobile);
		jsonMap.put("randomCode1", null);
		jsonMap.put("randomCode2", null);
		jsonMap.put("registedPlaceCity", user.cityId);
		jsonMap.put("carList", cars);
		jsonMap.put("provinceList", provinces);
		jsonMap.put("educationsList", educations);
		jsonMap.put("housesList", houses);
		jsonMap.put("maritalsList", maritals);
		jsonMap.put("cityList", cityList);
		jsonMap.put("isAddBaseInfo", user.isAddBaseInfo);
		
		return JSONUtils.printObject(jsonMap);
	}

	/*
	 * 注册用户(opt=3)
	 * name 用户名
	 * email 邮箱
	 * pwd 密码
	 * referrerName
	 */
	public static String register(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String name = (String)parameters.get("name");
		//String email = (String)parameters.get("email");
		String smsCode = parameters.get("verifyCode");
		String mobile = parameters.get("mobile");
		String password = (String)parameters.get("pwd");
		String referrerName = (String)parameters.get("referrerName");
		String financeType=parameters.get("financeType");//新增参数 financeType   借款端传递  传值为0
		String channel=parameters.get("channel");

		User.isNameExist(name, error);
		
		if (error.code == -2) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户名已存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		if (!RegexUtils.isValidUsername(name)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请填写符合要求的用户名");
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(mobile)) {
	           
            jsonMap.put("error", "-3");
    		jsonMap.put("msg", "请输入手机号码");
            return JSONUtils.printObject(jsonMap);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            
            jsonMap.put("error", "-3");
    		jsonMap.put("msg", "请输入正确的手机号码");
            return JSONUtils.printObject(jsonMap);
        }
		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {// 借款人
			User.isMobileExist(mobile, null, FinanceTypeEnum.BORROW.getCode(), error);
		} else if (StringUtils.isBlank(financeType) || INVEST.equals(financeType)) {// 投资人
			User.isMobileExist(mobile, null, FinanceTypeEnum.INVEST.getCode(), error);
			financeType = INVEST;
		}

		if (error.code == -2) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "手机号码已注册");
			return JSONUtils.printObject(jsonMap);
		}
		
		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
		
		if (!RegexUtils.isValidPassword(password)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请填写符合要求的密码");
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(smsCode+"_"+financeType)) {
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入短信校验码");
			return JSONUtils.printObject(jsonMap);
		}
		
		
			String cCode = (String) Cache.get(mobile+"_"+financeType);
			
			if(cCode == null) {
				
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "短信校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!smsCode.equals(cCode)) {
				
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "短信校验码错误");
				return JSONUtils.printObject(jsonMap);
			}
		
		
		

		/*if (!RegexUtils.isEmail(email)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请填写正确的邮箱地址");
			return JSONUtils.printObject(jsonMap);
		}*/
		
		if(StringUtils.isNotBlank(referrerName)) {
			
			if (name.equalsIgnoreCase(referrerName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "推荐人不能为自己");
				return JSONUtils.printObject(jsonMap);
			}
			
			if(mobile.equalsIgnoreCase(referrerName)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "推荐人不能为自己的号码");
				return JSONUtils.printObject(jsonMap);
			}
			
			//判断推荐人是否是手机号码
			if (RegexUtils.isMobileNum(referrerName)) {
				t_users t = t_users.find(" mobile = ? order by (case  finance_type when 1 then 1 when 0 then 2 else 3 end) ", referrerName).first();
				if(t == null){
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "推荐人不存在");
					return JSONUtils.printObject(jsonMap);
				}
				referrerName = t.name;
	        }else{
	        	jsonMap.put("error", "-3");
				jsonMap.put("msg", "推荐人手机号不正确");
				return JSONUtils.printObject(jsonMap);
	        }
			
			User.isNameExist(referrerName, error);
			if (-2 != error.code) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "推荐人不存在");
				return JSONUtils.printObject(jsonMap);
			}
			
		}
		
		/*User.isEmailExist(email, null, error);

		if (error.code == -2) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该邮箱已注册");
			return JSONUtils.printObject(jsonMap);
		}*/
		
		
		
		User user = new User();

		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = mobile;
		user.isMobileVerified = true;
		//user.email = email;

		if(StringUtils.isNotEmpty(channel)) {
			List<t_appstore> appstores = t_appstore.find("num = ? and state=1 ",channel).fetch();
			if(appstores!=null && appstores.size()>0) {
				user.store_id = appstores.get(0).id;
			}
		}

		user.recommendUserName = referrerName;
		if(!StringUtils.isBlank(financeType)&&BORROW.equals(financeType)) {
			user.financeType=FinanceTypeEnum.BORROW.getCode();
		}else if(StringUtils.isBlank(financeType)||INVEST.equalsIgnoreCase(financeType)) {
			user.financeType=FinanceTypeEnum.INVEST.getCode();
		}
		user.register(Constants.CLIENT_APP, error);

		if (error.code < 0 ) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "数据库异常");
			return JSONUtils.printObject(jsonMap);
		}
		
		//user.name = name;
		
		if (user.id < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "数据库异常");
			return JSONUtils.printObject(jsonMap);
		}
		
		//注册发红包
//		String redTypeName = Constants.RED_PACKAGE_TYPE_REGIST;//注册类型
//		
//		long status  = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态
//		
//		t_red_packages_type redPackageType = RedPackage.isExist(redTypeName, status);//红包类型是否存在
//		if(null != redPackageType){
//			
//			String desc = "APP注册发放红包";
//			RedPackageHistory.sendRedPackage(user, redPackageType,desc);
//			Logger.error("APP充值发放红包短信通知成功");
//		}else{
//			Logger.error("APP注册送红包发放失败!");
//		}	
		
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "注册成功");
		jsonMap.put("id", user.sign);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	public static String appMobileExist(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String mobile = parameters.get("mobile");
		String financeType=parameters.get("financeType");//新增参数    0  借款人
		ErrorInfo error = new ErrorInfo();
		if (StringUtils.isBlank(mobile)) {
           
            jsonMap.put("error", "-3");
    		jsonMap.put("msg", "请输入手机号码");
            return JSONUtils.printObject(jsonMap);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            
            jsonMap.put("error", "-3");
    		jsonMap.put("msg", "请输入正确的手机号码");
            return JSONUtils.printObject(jsonMap);
        }
		// 如果此参数不为空，说明是借款端app传来 值为0
		if (!StringUtils.isBlank(financeType)&&BORROW.equals(financeType)) {
			User.isMobileExist(mobile, null, FinanceTypeEnum.BORROW.getCode(), error);
		} else if(StringUtils.isBlank(financeType)||INVEST.equals(financeType)){// 为空说明从老版，投资端app传过来 值为 1
			User.isMobileExist(mobile, null, FinanceTypeEnum.INVEST.getCode(), error);
		}

		
		if(error.code < 0){
			
			jsonMap.put("mobileExist", true);
			jsonMap.put("msg", "手机号码已被注册");
			//是否需要短信验证码
			jsonMap.put("isSmsCode", Constants.SMS_MSG_CODE ? 1 : 0);
			jsonMap.put("error", "-1");
			return JSONUtils.printObject(jsonMap);
		}else{
			
			jsonMap.put("mobileExist", false);
			jsonMap.put("msg", "系统异常...");

			jsonMap.put("error", "-10");
			return JSONUtils.printObject(jsonMap);
		}
	}
	
	/*
	 * 发送短信验证码(opt=4)
	 * cellPhone 手机号码
	 */
	public static String findPwdBySms(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String mobile = parameters.get("cellPhone");
		String financeType = parameters.get("financeType");//新增参数financeType   借款端 传递过来 值 为0

		if(!RegexUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {
			SMSUtil.sendCode(mobile, FinanceTypeEnum.BORROW.getCode(), error);
		} else if (StringUtils.isBlank(financeType) || INVEST.equals(financeType)) {
			SMSUtil.sendCode(mobile, FinanceTypeEnum.INVEST.getCode(), error);
		}
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 找回密码-验证码确认(opt=5)
	 * cellPhone 手机号码
	 * randomCode 验证码
	 */
	public static String confirmCode(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String mobile = parameters.get("cellPhone");
		String randomCode = parameters.get("randomCode");
		String financeType=parameters.get("financeType");


		if(StringUtils.isBlank(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(randomCode)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入验证码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入正确的手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {
			User.queryIdByMobile(mobile, FinanceTypeEnum.BORROW.getCode(), error);
		} else if (StringUtils.isBlank(financeType) || INVEST.equals(financeType)) {
			User.queryIdByMobile(mobile, FinanceTypeEnum.INVEST.getCode(), error);
			financeType =  INVEST;
		}

		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该手机号码不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (Cache.get(mobile + "_" +financeType )).toString();
			
			if(cCode == null) {
				jsonMap.put("error", "-4");
				jsonMap.put("msg","验证码已失效，请重新点击发送验证码");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!randomCode.equals(cCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg","手机验证错误");
				
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","手机验证成功");
		
		return JSONUtils.printObject(jsonMap);
		
	}
	
	/*
	 * 重置密码-提交新密码(opt=6)
	 * cellPhone 手机号码
	 * newpwd 新密码
	 */
	public static String commitPassword(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String financeType=parameters.get("financeType");
		if(StringUtils.isBlank(parameters.get("cellPhone"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "手机号码有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("newpwd"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "新密码有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String mobile = parameters.get("cellPhone");
		String password = parameters.get("newpwd");
		
		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
		
		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {
			User.updatePasswordByMobileApp(mobile, password, error, FinanceTypeEnum.BORROW.getCode());
		} else if (StringUtils.isBlank(financeType) || INVEST.equals(financeType)) {
			User.updatePasswordByMobileApp(mobile, password, error, FinanceTypeEnum.INVEST.getCode());
		}

		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg",error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 绑定手机号码接口(opt=7)
	 * cellPhone 手机号码
	 * randomCode 验证码
	 * id 用户id
	 */
	public static String saveCellphone(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		if(StringUtils.isBlank(parameters.get("cellPhone"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "手机号码有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("randomCode"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "验证码有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String mobile = parameters.get("cellPhone");
		String code = parameters.get("randomCode");
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
        user.checkMoible(mobile, code, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg",error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 注册服务协议(opt=8)
	 */
	public static String ServiceAgreement(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","获取协议成功");
		jsonMap.put("content",content);
		
		return JSONUtils.printObject(jsonMap);
	}
		
	static private List<Long> openContent=Arrays.asList(Constants.NewsTypeId.ABOUT_YIYI,
			Constants.NewsTypeId.GUARANTEE_ID,
			Constants.NewsTypeId.RISK_WARNING_ID,
			Constants.NewsTypeId.DEBIT_CREDIT_ID,
			Constants.NewsTypeId.DISCLOSURE_COMMITMENT_ID,
			Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID,
			Constants.NewsTypeId.BORROW_STRATEGY_ID,
			Constants.NewsTypeId.CREDIT_LOAN_ID,
			Constants.NewsTypeId.HOUSE_LOAN_ID,
			Constants.NewsTypeId.AGRICULTURE_LOAN_ID,
			Constants.NewsTypeId.LENDER_SERVICE_ID,
			Constants.NewsTypeId.BORROWER_SERVICE_ID,
			Constants.NewsTypeId.CONSULT_MANAGE_ID,
			Constants.NewsTypeId.YMD_SERVIVE_COST_ID,
			Constants.NewsTypeId.YMD_DEBIT_CREDIT_ID,
			Constants.NewsTypeId.AOTO_PROTOCOL_ID
			

			);

	/*
	 * 内容服务(opt=1049)
	 */
	public static String ContentService(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String contentId = parameters.get("contentId");
		if(!NumberUtil.isNumericDouble(contentId)){
			jsonMap.put("error", "-4");
			jsonMap.put("msg","内容ID有误");
			return JSONUtils.printObject(jsonMap);
		}
		Long contentIdL=Long.parseLong(contentId);
		String content ="";
		if(openContent.contains(contentIdL)){
			content=News.queryContent(contentIdL, error);
		}else{
			jsonMap.put("error", "-4");
			jsonMap.put("msg","该内容不对外开放");
			return JSONUtils.printObject(jsonMap);
		}
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);
			return JSONUtils.printObject(jsonMap);
		}
		jsonMap.put("error", "-1");
		jsonMap.put("msg","获取协议成功");
		jsonMap.put("content",content);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 还款计算器(opt=9)
	 */
	public static String RepaymentCalculator(Map<String, String> parameters) throws IOException{
		
		List<Map<String, Object>> payList = new ArrayList<Map<String, Object>>();
//		List<Object> listOrder = new ArrayList<Object>();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		double dayRate = 0;
		double monRate = 0;
		double monPay = 0;
		double paySum = 0;
		
		if(StringUtils.isBlank(parameters.get("borrowSum"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款金额有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("yearRate")) || Double.parseDouble(parameters.get("yearRate")) <= 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "年利率有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("borrowTime")) || !NumberUtil.isNumeric(parameters.get("borrowTime"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款期限有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(Integer.parseInt(parameters.get("borrowTime")) > 60){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "最大借款期限为60个月，请重新输入");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("isDay"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "是否为天标有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("repayWay"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款方式有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		double amount = Double.parseDouble(parameters.get("borrowSum"));
		double apr = Double.parseDouble(parameters.get("yearRate"));
		int period = Integer.valueOf(parameters.get("borrowTime"));
		int periodUnit = Integer.valueOf(parameters.get("isDay"));
		int repaymentType = Integer.valueOf(parameters.get("repayWay"));
		monRate = apr /1200;
		
		if(periodUnit == 1){//天标
			dayRate = Arith.div(apr, 36000, 2);
			paySum = amount + Arith.mul(dayRate, period); 
			monPay = paySum;
		}else{
			if(repaymentType == Constants.PAID_MONTH_EQUAL_PRINCIPAL_INTEREST){//等额本息
				double monPays = Double.valueOf(Arith.mul(amount, monRate) * Math.pow((1 + monRate), period))/ 
				Double.valueOf(Math.pow((1 + monRate), period) - 1);//每个月要还的本金和利息
				paySum = Arith.round(Arith.mul(monPays, period),2);
				monPay = monPays;
			}
			
			else if(repaymentType == Constants.PAID_MONTH_ONCE_REPAYMENT){// 先息后本
				monPay = Arith.round(Arith.mul(amount, monRate), 2);
				paySum = Arith.add(amount, Arith.mul(monPay, period));
				
			}
			
			else if(repaymentType == Constants.ONCE_REPAYMENT){
				double payMon = Arith.round(Arith.mul(amount, monRate), 2);
				paySum = Arith.add(amount, Arith.mul(payMon, period));
				monPay = paySum;
				
			}
		}
		
		payList = Bill.repaymentCalculate(amount, apr, period, periodUnit, repaymentType);
		
		jsonMap.put("monRate", Arith.round(monRate*100,2));
		jsonMap.put("allPay", paySum);
		jsonMap.put("monPay", monPay);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "计算成功");
		jsonMap.put("list", payList);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/*
	 * 借款产品列表
	 */
	public static String loanProduct(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_2, error);
		
        JsonConfig config = new JsonConfig();  
		
		config.setExcludes(new String[]{
				"sign", 
				"repaymentType",
				"strLoanType",
				"periodYearArray",
				"periodMonthArray",
				"periodDayArray",
				"investPeriodArray",
				"requiredAuditItem",
				"selectAuditItem",
				"lables"
		}); 
		JSONArray array = JSONArray.fromObject(products, config);
		
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "数组数据异常");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询借款产品列表成功");
		jsonMap.put("totalNum", products.size());
		jsonMap.put("list", array);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/*
	 * 借款标产品详情
	 * productId 产品id
	 */
	public static String productInfo(Map<String, String> parameters) throws IOException{
		String productId = parameters.get("productId");
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		if(StringUtils.isBlank(parameters.get("productId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传参借款产品id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(parameters.get("id") == null) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "传入参数id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long userId = 0;
		
		if(!"".equals(parameters.get("id"))) {
			userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		}
		
		Product product = new Product();
		product.id = Long.valueOf(productId);
		
		User user = null;
		
		if(userId > 0) {
			user = new User();
			user.id = userId;
		}
		
		/* 手续费常量值 */
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	    double strfee = backstageSet.borrowFee;
	    double borrowFeeMonth = backstageSet.borrowFeeMonth;
	    double borrowFeeRate = backstageSet.borrowFeeRate;

		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询借款产品详情成功");
		jsonMap.put("isLogin", userId > 0 ? true : false);
		jsonMap.put("isEmailVerified", userId > 0 ? user.isEmailVerified : false);
		jsonMap.put("isAddBaseInfo", userId > 0 ? user.isAddBaseInfo : false);
		jsonMap.put("productFeatures", product.characteristic);
		jsonMap.put("suitsCrowd", product.fitCrowd);
		jsonMap.put("limitRange", new DecimalFormat("###,##0.00").format(product.minAmount)+"-"+new DecimalFormat("###,##0.00").format(product.maxAmount));
		jsonMap.put("loanRate", Double.toString(product.minInterestRate)+"-"+ Double.toString(product.maxInterestRate));
		jsonMap.put("monRate", Double.toString(product.monthMinApr)+"-"+ Double.toString(product.monthMaxApr));
		jsonMap.put("periodYear", product.periodYear);
		jsonMap.put("periodYearArray", product.periodYearArray);
		jsonMap.put("periodMonth", product.periodMonth);
		jsonMap.put("periodMonthArray", product.periodMonthArray);
		jsonMap.put("periodDay", product.periodDay);
		jsonMap.put("periodDayArray", product.periodDayArray);
		jsonMap.put("tenderTime", product.investPeriod);
		jsonMap.put("tenderTimeArray", product.investPeriodArray);
		jsonMap.put("uditTime", product.auditCycle);
		jsonMap.put("repayWay", product.repaymentType);
		jsonMap.put("poundage", "借款期限"+borrowFeeMonth+"个月（含）以下，借款成功后，收取本金的"+strfee+"%；借款期限"+borrowFeeMonth+"个月以上，借款成功后，收取本金的"+strfee+"%以外，还另外收取超过月份乘本金的"+borrowFeeRate+"%（不成功不收取成交服务费）");
		jsonMap.put("reviewMaterial", product.requiredAuditItem);
		jsonMap.put("optReviewMaterial", product.selectAuditItem);
		jsonMap.put("applyconditons", product.applicantCondition);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/*
	 * 获取借款产品信息
	 * productId 产品id
	 */
	public static String productDetails(Map<String, String> parameters) throws IOException{
		String productId = parameters.get("productId");
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		Product product = new Product();
		
		if(StringUtils.isBlank(parameters.get("productId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传参有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		product.id = Long.valueOf(productId);

		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询借款产品详情成功");
		jsonMap.put("maxCopies", product.maxCopies);
		jsonMap.put("periodYear", product.periodYear);
		jsonMap.put("periodYearArray", product.periodYearArray);
		jsonMap.put("periodMonth", product.periodMonth);
		jsonMap.put("periodMonthArray", product.periodMonthArray);
		jsonMap.put("periodDay", product.periodDay);
		jsonMap.put("periodDayArray", product.periodDayArray);
		/* 借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		jsonMap.put("purpose", purpose);
		jsonMap.put("minInvestAmount", product.minInvestAmount);
		jsonMap.put("fullyTimeLimit", product.investPeriod);//满标期限
		jsonMap.put("tenderTimeArray", product.investPeriodArray);
		jsonMap.put("minInterestRate", product.minInterestRate);
		jsonMap.put("maxInterestRate", product.maxInterestRate);
		jsonMap.put("repaymentTypeId", product.repaymentTypeId);
		jsonMap.put("repayWay", product.repaymentType);
		jsonMap.put("loanImageType", product.loanImageType);
		jsonMap.put("loanImageFilename", product.loanImageFilename);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/*
	 * 发布借款
	 * productId 产品id
	 * purposeId 借款目的id
	 * title 标题
	 * amount 借款金额
	 * periodUnit 期限单位
	 * period 期限
	 * investPeriod  天标满标期限
	 * repaymentId 还款方式id
	 * minInvestAmount 最小投资金额
	 * averageInvestAmount 平均投资金额
	 * apr 年利率
	 * bonusType 奖励方式
	 */
	public static String createBid(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		Bid bid = new Bid();
		
		bid.purpose = new Purpose();
		bid.repayment = new Repayment();
		
		bid.productId = Long.valueOf(parameters.get("productId"));  // 填充产品对象
		
		if(StringUtils.isBlank(parameters.get("purposeId")) || Long.valueOf(parameters.get("purposeId")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款用途有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("title")) || parameters.get("title").length() > 24 ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款标题有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		int _amount = Integer.valueOf(parameters.get("amount"));
		
//		if(StringUtils.isBlank(parameters.get("amount")) || Double.parseDouble(parameters.get("amount")) <= 0 ||
//				 Double.parseDouble(parameters.get("amount"))
//				< bid.product.minAmount || Double.parseDouble(parameters.get("amount")) > bid.product.maxAmount){
//			jsonMap.put("error", "1");
//			jsonMap.put("msg", "借款金额有误!");
//			
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		if(StringUtils.isBlank(parameters.get("periodUnit"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款期限单位有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("period"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款期限有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		switch (Integer.valueOf(parameters.get("periodUnit"))) {
		case Constants.YEAR:
			
			if (Integer.valueOf(parameters.get("period")) > Constants.YEAR_PERIOD_LIMIT) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			break;
		case Constants.MONTH:
			
			if (Integer.valueOf(parameters.get("period")) > Constants.YEAR_PERIOD_LIMIT * 12) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			break;
		case Constants.DAY:
			
			if (Integer.valueOf(parameters.get("period")) > Constants.YEAR_PERIOD_LIMIT * 12 * 30) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "借款期限超过了" + "借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			if (Integer.valueOf(parameters.get("investPeriod")) > Integer.valueOf(parameters.get("period"))) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "天标满标期限不能大于借款期限 !");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			break;
		default:
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款期限单位有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("repaymentId")) || Long.valueOf(parameters.get("repaymentId")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款方式id有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!StringUtils.isBlank(parameters.get("minInvestAmount"))){
			bid.minInvestAmount = Double.parseDouble(parameters.get("minInvestAmount"));
			
			if (Double.parseDouble(parameters.get("minInvestAmount")) > 0 && (Double.parseDouble(parameters.get("minInvestAmount")) 
					< bid.product.minInvestAmount)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "最低投标金额不能小于产品最低投标金额!");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		
//		if ((Double.parseDouble(parameters.get("minInvestAmount")) > 0 && Double.parseDouble(parameters.get("averageInvestAmount")) 
//				> 0) || (Double.parseDouble(parameters.get("minInvestAmount")) <= 0 && Double.parseDouble(parameters.get("averageInvestAmount")) <= 0)) {
//			jsonMap.put("error", "1");
//			jsonMap.put("msg", "最低投标金额和平均招标金额有误!");
//			
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		if(!StringUtils.isBlank(parameters.get("averageInvestAmount"))){
			bid.averageInvestAmount = Double.parseDouble(parameters.get("averageInvestAmount"));
			
			if (Double.parseDouble(parameters.get("averageInvestAmount")) > 0 && Double.parseDouble(parameters.get("amount"))
					% Double.parseDouble(parameters.get("averageInvestAmount")) != 0) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "平均招标金额有误!");
				
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		
		if(StringUtils.isBlank(parameters.get("investPeriod")) || Integer.valueOf(parameters.get("investPeriod")) <= 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "投标期限有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("apr")) || Double.parseDouble(parameters.get("apr")) <= 0 || 
				Double.parseDouble(parameters.get("apr")) > 100 || Double.parseDouble(parameters.get("apr"))
				< bid.product.minInterestRate || Double.parseDouble(parameters.get("apr")) > bid.product.maxInterestRate){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "年利率有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
//		if(StringUtils.isBlank(parameters.get("imageFilename"))){
//			jsonMap.put("error", "1");
//			jsonMap.put("msg", "图片有误");
//			
//			return JSONObject.fromObject(jsonMap).toString();
//		}
		
		if(StringUtils.isBlank(parameters.get("description"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "内容描述有误!");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!StringUtils.isBlank(parameters.get("bonusType"))){
			bid.bonusType = Integer.valueOf(parameters.get("bonusType"));
		}
		
		if(!StringUtils.isBlank(parameters.get("awardScale")) && !StringUtils.isBlank(parameters.get("bonusType"))){
			
			bid.awardScale = Double.parseDouble(parameters.get("awardScale"));
		}
		
		if(!StringUtils.isBlank(parameters.get("bonus")) && !StringUtils.isBlank(parameters.get("bonusType"))){
			
			bid.bonus = Double.parseDouble(parameters.get("bonus"));
		}
		
		if(StringUtils.isBlank(parameters.get("productId")) || Long.valueOf(parameters.get("productId")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传参productId有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("userId"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "传参用户userId有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
        long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		bid.purpose.id = Long.valueOf(parameters.get("purposeId"));
		bid.title = parameters.get("title");
		bid.amount = Double.parseDouble(parameters.get("amount"));
		bid.periodUnit = Integer.valueOf(parameters.get("periodUnit"));
		bid.period = Integer.valueOf(parameters.get("period"));
		bid.repayment.id = Long.valueOf(parameters.get("repaymentId"));
		bid.investPeriod = Integer.valueOf(parameters.get("investPeriod"));
		bid.apr = Double.parseDouble(parameters.get("apr"));
		bid.imageFilename = parameters.get("imageFilename");
		bid.description = parameters.get("description");
		
		bid.createBid = true; // 优化加载
		bid.userId = userId; // 填充用户对象
		
		t_bids tbid = new t_bids();
		bid.createBid(Constants.CLIENT_APP, tbid, error);
		bid.createBid(Constants.CLIENT_PC, tbid, error);
		
		if(error.code < 0){
			
			if(error.code == Constants.BALANCE_NOT_ENOUGH) {
				jsonMap.put("error", "-999");
				jsonMap.put("msg", "您余额不足，请充值！");
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String bidNo = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询数据有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "发布借款成功");
		jsonMap.put("bidNo", bidNo + bid.id);
		jsonMap.put("requiredAuditItem", bid.product.requiredAuditItem);
		jsonMap.put("selectAuditItem", bid.product.selectAuditItem);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/*
	 * 获取完善用户资料状态接口
	 * id 用户id
	 */
	public static String UserStatus(Map<String, String> parameters) throws IOException{
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        ErrorInfo error = new ErrorInfo();
        
		User user = new User();
		
		if (StringUtils.isBlank(parameters.get("id"))) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!user.isAddBaseInfo){
			jsonMap.put("error", -1);
			jsonMap.put("msg", "未激活");
			jsonMap.put("accountStates", 2);
			
			return JSONUtils.printObject(jsonMap);
			
		}else{
			if((user.isEmailVerified || user.isMobileVerified) && user.isAddBaseInfo){
				jsonMap.put("error", -1);
				jsonMap.put("msg", "已激活已完善资料");
				jsonMap.put("accountStates", 2);
				
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 完善用户资料接口
	 * @param id 用户id
	 * registedPlaceCity 城市id
	 * higtestEdu 学历
	 * @throws IOException
	 */
	public static String saveBaseInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		
		if (StringUtils.isBlank(parameters.get("id")) ) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("realName"))){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "姓名不能为空");
			return JSONUtils.printObject(jsonMap);
		}
		user.realityName = parameters.get("realName").trim();
		
		if (!CharUtil.isChinese(user.realityName)) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "真实姓名必须是中文");
			return JSONUtils.printObject(jsonMap);
		}
		
		
		if(StringUtils.isBlank(parameters.get("sex")) || Integer.valueOf(parameters.get("sex")) < 0 ){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "性别有误");
			return JSONUtils.printObject(jsonMap);
		}
		user.setSex(Integer.valueOf(parameters.get("sex")));
		
		if(StringUtils.isBlank(parameters.get("age")) || Integer.valueOf(parameters.get("age")) < 0 ){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "年龄有误");
			return JSONUtils.printObject(jsonMap);			
		}
		user.age = Integer.valueOf(parameters.get("age"));
		
		if(StringUtils.isBlank(parameters.get("registedPlaceCity")) || Integer.valueOf(parameters.get("registedPlaceCity")) < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "城市有误");
			return JSONUtils.printObject(jsonMap);
		}
		user.cityId =  Integer.valueOf(parameters.get("registedPlaceCity"));
		
		if(StringUtils.isBlank(parameters.get("idNo"))){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "身份证号有误");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!"".equals(IDCardValidate.chekIdCard(0, parameters.get("idNo")))) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "身份证号有误");
			return JSONUtils.printObject(jsonMap);
		}
		user.idNumber = parameters.get("idNo");
		
		if(StringUtils.isBlank(parameters.get("higtestEdu")) || Integer.valueOf(parameters.get("higtestEdu")) < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "学历有误");
			return JSONUtils.printObject(jsonMap);
		}
		user.educationId = Integer.valueOf(parameters.get("higtestEdu"));
		
		if(StringUtils.isBlank(parameters.get("maritalStatus")) || Integer.valueOf(parameters.get("maritalStatus")) < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "请选择婚姻情况");
			return JSONUtils.printObject(jsonMap);
		}
		user.maritalId = Integer.valueOf(parameters.get("maritalStatus"));
		
		if(StringUtils.isBlank(parameters.get("housrseStatus")) || Integer.valueOf(parameters.get("housrseStatus")) < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "请选择购房情况");
			return JSONUtils.printObject(jsonMap);
		}
		user.houseId = Integer.valueOf(parameters.get("housrseStatus"));
		
		if(StringUtils.isBlank(parameters.get("CarStatus")) || Integer.valueOf(parameters.get("CarStatus")) < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "请选择购车情况");
			return JSONUtils.printObject(jsonMap);
		}
		user.carId = Integer.valueOf(parameters.get("CarStatus"));
		
		user.updateBaseInfo(error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "保存成功");
		
		return JSONUtils.printObject(jsonMap);
	}

	/*
	 *通过后台发送激活邮件
	 *id 用户id
	 */
	public static String activeEmail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		
		if (StringUtils.isBlank(parameters.get("id"))) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		TemplateEmail.activeEmail(user, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "邮箱激活成功");
		jsonMap.put("activationLink", EmailUtil.emailUrl(user.email));
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *申请vip
	 */
	public static String vipApply(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		
		if(StringUtils.isBlank(parameters.get("openTime")) || Integer.valueOf(parameters.get("openTime")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请时间有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("id")) ) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Vip vip = new Vip();
		vip.serviceTime = Integer.valueOf(parameters.get("openTime"));
		vip.renewal(user, Constants.CLIENT_APP, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "申请vip成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *VIP会员服务条款接口
	 */
	public static String vipAgreement(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String agreement = News.queryVipAgreement();
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "VIP会员服务条款查询成功");
		jsonMap.put("content", agreement);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *二维码
	 */
	public static String TwoDimensionalCode(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "二维码查询成功");
		jsonMap.put("promoteImg", Constants.HTTP_PATH + "/images?uuid="+user.qrcode);
		jsonMap.put("spreadLink", user.spreadLink);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *我推广的会员列表接口
	 *id 用户id
	 */
	public static String spreadUser(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_cps_users> page = User.queryCpsSpreadUsers(userId ,"", "", 
				"", "", parameters.get("currPage"), "36", error);
		
		if(page.page != null) {
			for(v_user_cps_users e : page.page) {
				e.mobile = e.mobile.replaceAll("(\\d{3})\\d{8}","$1********");
			}
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("page", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/* 我推广的收入接口
	 * id 用户id
	 */
	public static String spreadUserIncome(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<t_user_cps_income> page = User.queryCpsSpreadIncome(userId, 
				"","",parameters.get("currPage"),"36", error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("page", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/* 发送站内信
	 */
	public static String sendStation(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("receiverName"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "接收人名称有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("title"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标题不能为空");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("content"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "内容不能为空");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		StationLetter message = new StationLetter();
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		message.senderUserId = userId;
		message.receiverUserName = parameters.get("receiverName");
		message.title = parameters.get("title");
		message.content = parameters.get("content");
		
		message.sendToUserByUser(error); 
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "发送成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *系统信息接口
	 *id 用户id
	 */
	public static String systemSms(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数当前页currPage有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_system> page = 
			StationLetter.queryUserSystemMsgs(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "", 5, error);
	
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		List<Object> list = StationLetter.getMessageList(userId);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("page", page);
		jsonMap.put("unReadSize", list.size());
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *删除系统信息接口
	 *id 用户id
	 */
	public static String deleteSystemSmgs(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("ids"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择要删除的站内信");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}

		String arr[] = parameters.get("ids").split(",");
		
		for (String str : arr) {
			long id = Long.parseLong(str);
			StationLetter.deleteInboxMsgByUser(userId, id, DeleteType.DELETE, error);
			
			if (error.code < 0) {
				break;
			}
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "删除失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "删除成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *收件箱信息
	 *id 用户id
	 */
	public static String inboxMsgs(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_user_inbox> page = 
			StationLetter.queryUserInboxMsgs(userId , Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "", 5, error);
	
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("page", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *删除收件箱信息接口
	 */
	public static String deleteInboxMsgs(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("ids"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择要删除的站内信");
			
			return JSONUtils.printObject(jsonMap);
		}

        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String arr[] = parameters.get("ids").split(",");

		for (String str : arr) {
			long id = Long.parseLong(str);
			StationLetter.deleteInboxMsgByUser(userId, id, DeleteType.DELETE, error);
			if (error.code < 0) {
				break;
			}
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "删除失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "删除成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *删除发件箱信息接口
	 */
	public static String deleteOutboxMsgByUser(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("ids"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择要删除的站内信");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		String arr[] = parameters.get("ids").split(",");

		for (String str : arr) {
			long id = Long.parseLong(str);
			StationLetter.deleteOutboxMsgByUser(userId, id, DeleteType.DELETE, error);
			if (error.code < 0) {
				break;
			}
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "删除失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "删除成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *标记为已读
	 *id 用户id
	 */
	public static String markMsgsReaded(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("ids"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择要标记为已读的站内信");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}

		String arr[] = parameters.get("ids").split(",");

		for (String str : arr) {
			long id = Long.parseLong(str);
			StationLetter.markUserMsgReaded(userId, id, error);
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "标记为已读失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "标记为已读成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *标记为未读
	 *id 用户id
	 */
	public static String markMsgsUnread(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if (StringUtils.isBlank(parameters.get("ids"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择要标记为未读的站内信");
			
			return JSONUtils.printObject(jsonMap);
		}

		String arr[] = parameters.get("ids").split(",");

		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		for (String str : arr) {
			long id = Long.parseLong(str);
			StationLetter.markUserMsgUnread(userId, id, error);
		}
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "标记为未读失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "标记为未读成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *借款账单 87接口
	 *id 用户id
	 */
	public static String queryMyLoanBills(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		//String payTypeStr = parameters.get("payType");
		//String isOverTypeStr = parameters.get("isOverType");
		//String keyTypeStr = parameters.get("keyType");
		//String key = parameters.get("key");
		
	/*	if(!(NumberUtil.isNumericInt(isOverTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入参数是否逾期有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(!(NumberUtil.isNumericInt(payTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入参数还款方式有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}*/
		
		/*if(!(NumberUtil.isNumericInt(keyTypeStr))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入参数关键字有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		*/
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "参数用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		int currPage = 0;
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			currPage = 1;
		} else {
			currPage = Integer.valueOf(parameters.get("currPage"));
		}
		
		/*int payType = Integer.parseInt(payTypeStr);
		int isOverType = Integer.parseInt(isOverTypeStr);
		int keyType = Integer.parseInt(keyTypeStr);*/
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		//PageBean<v_bill_loan> page = Bill.queryMyLoanBills(userId, payType, isOverType, keyType, key, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, error);
		PageBean<v_bill_loan_v1> page = Bill.queryMyLoanBillsV1(userId, currPage, Constants.APP_PAGESIZE, error);
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("page", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *借款账单详情 88接口
	 */
	public static String loanBillDetails(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bidId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标的id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long bidId = Security.checkSign(parameters.get("bidId"), Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析标的id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		v_bill_detail_v1 billDetail = Bill.queryBillDetailsV1(bidId, userId, error);
        
        if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		List<v_bill_repayment_record_v1> repayment_record = Bill.queryBillReceivablesV1(bidId, billDetail.status, error);
		System.err.println("借款账单期数：" + repayment_record.size()+"---------------------------------");
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		//BackstageSet backSet = BackstageSet.getCurrentBackstageSet();
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("repayment_record", repayment_record);
		//jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("billDetail", billDetail);
		/*jsonMap.put("platformName", backSet.platformName);
		jsonMap.put("hotline", backSet.platformTelephone);*/
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *还款
	 **id 用户id
	 *billId 账单id
	 *dealPwd 交易密码
	 *
	 */
	public static String submitRepayment(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("billId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "账单id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
//		String dealPwd = parameters.get("dealPwd");
//		
//		dealPwd = Encrypt.decrypt3DES(dealPwd, Constants.ENCRYPTION_KEY);
//		
//		if(StringUtils.isBlank(dealPwd)){
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "交易密码有误");
//			
//			return JSONUtils.printObject(jsonMap);
//		}
		
		if(StringUtils.isBlank(parameters.get("payAmount")) || Double.valueOf(parameters.get("payAmount")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long billId = Security.checkSign(parameters.get("billId"), Constants.BILL_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析账单id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
//		int code = user.verifyPayPassword(dealPwd, error);
//		
//		if(code < 0) {
//			jsonMap.put("error", "-4");
//			jsonMap.put("msg",error.msg);
//			
//			return JSONUtils.printObject(jsonMap);
//		}
		
		Bill bill = new Bill();
		bill.setId(billId);

		t_debt_transfer transfer = t_debt_transfer.find(" bid_id = ? ", bill.bidId).first();
		List<t_debt_bill_invest> debtBill = new ArrayList<>();

		if(transfer != null ) { //债权转让标可能没有满标。
			debtBill = t_debt_bill_invest.find(" debt_id = ? ", transfer.id).fetch();
			Logger.info("该标的有债权转让，执行债权转让还款逻辑！ 账单信息: " + debtBill);
			bill.repaymentV1(user.id, error);

		} else {
			bill.repayment(user.id, error);
		}
		/*
		if(debtBill != null && debtBill.size() > 0){//存在债权转让账单，走债权还款
			Logger.info("该标的有债权转让，执行债权转让还款逻辑！ 账单信息: " + bill);
			bill.repaymentV1(user.id, error);

		} */


		
		if(error.code == -999) {
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONUtils.printObject(jsonMap);
		} else if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "还款成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *审核中的借款标列表
	 **id 用户id
	 *currPage 分页数据
	 */
	public static String auditingLoanBids(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_bid_auditing> pageBean = new PageBean<v_bid_auditing>();
		pageBean.currPage = parameters.get("currPage") == null ? 1 : Integer.valueOf(parameters.get("currPage"));
		pageBean.pageSize = Constants.APP_PAGESIZE;
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("userId", String.valueOf(userId));
		
		pageBean.page = Bid.queryBidAuditing(pageBean, error, map);

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "审核中的借款标查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "审核中的借款标查询成功");
		jsonMap.put("page", pageBean.page);
		jsonMap.put("totalNum", pageBean.page.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *等待满标的借款标列表
	 **id 用户id
	 *currPage 分页数据
	 */
	public static String loaningBids(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_bid_fundraiseing> pageBean = new PageBean<v_bid_fundraiseing>();
		pageBean.currPage = parameters.get("currPage") == null ? 1 : Integer.valueOf(parameters.get("currPage"));
		pageBean.pageSize = Constants.APP_PAGESIZE;
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		pageBean.page = Bid.queryBidFundraiseing(pageBean, -1, error,  String.valueOf(userId),"","","","","");
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "招标中的借款标查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "招标中的借款标查询成功");
		jsonMap.put("page", pageBean.page);
		jsonMap.put("totalNum", pageBean.page.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *还款中的借款标列表
	 **id 用户id
	 *currPage 分页数据
	 */
	public static String repaymentBids(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.currPage = parameters.get("currPage") == null ? 1 : Integer.valueOf(parameters.get("currPage"));
		pageBean.pageSize = Constants.APP_PAGESIZE;
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error,  String.valueOf(userId),"","","","","");
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "还款中的借款标查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "还款中的借款标查询成功");
		jsonMap.put("page", pageBean.page);
		jsonMap.put("totalNum", pageBean.page.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *已成功的借款标列表
	 *id 用户id
	 *currPage 分页数据
	 */
	public static String successBids(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.currPage = parameters.get("currPage") == null ? 1 : Integer.valueOf(parameters.get("currPage"));
		pageBean.pageSize = Constants.APP_PAGESIZE;
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error,  String.valueOf(userId),"","","","","");
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "已成功的借款标查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "已成功的借款标查询成功");
		jsonMap.put("page", pageBean.page);
		jsonMap.put("totalNum", pageBean.page.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *审核资料认证
	 *id 用户id
	 *currPage 分页数据
	 */
	public static String auditMaterials(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		PageBean<v_user_audit_items> pageBean = UserAuditItem.queryUserAuditItem(parameters.get("currPage"), Constants.APP_PAGESIZE2, userId, error,
				parameters.get("status"),null,null,parameters.get("productId"),null);
        
		List<Product> products = Product.queryProductNames(true, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "审核资料认证查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "审核资料认证查询成功");
		jsonMap.put("page", pageBean.page);
		jsonMap.put("totalNum", pageBean.page.size());
		jsonMap.put("products", products);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *审核资料认证详情
	 *id 用户id
	 *mark 唯一标识
	 */
	public static String auditMaterialsSameItem(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("mark"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "唯一标识有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        UserAuditItem item = new UserAuditItem();
		item.userId = userId;
		item.mark = parameters.get("mark");
        List<v_user_audit_items> items = UserAuditItem.querySameAuditItem(userId, item.auditItemId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "审核资料认证详情查询成功");
		jsonMap.put("items", items);
		jsonMap.put("auditItemName", item.auditItem.name);
	    jsonMap.put("creditScore", item.auditItem.creditScore);
	    jsonMap.put("period", item.auditItem.period);
	    jsonMap.put("auditCycle", item.auditItem.auditCycle);
	    jsonMap.put("suggestion", item.suggestion);
	    jsonMap.put("productNames", item.productNames);
	    jsonMap.put("status", item.status);
	    jsonMap.put("expireTime", item.expireTime);
	    jsonMap.put("time", item.time);
	    jsonMap.put("auditTime", item.auditTime);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *交易记录
	 *id 用户id
	 *purpose 借款用途
	 *startTime 开始查询时间
	 *lastTime   结束查询时间
	 *
	 */
	public static String dealRecord(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String beginTime = null;
		String endTime = null;
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("purpose")) ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款用途有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
//		if(StringUtils.isNotBlank(parameters.get("startTime")) && DateUtil.){
//			jsonMap.put("error", "1");
//			jsonMap.put("msg", "查询初始时间有误");
//			
//			return JSONUtils.printObject(jsonMap);
//		}
//		
//		if(StringUtils.isBlank(parameters.get("lastTime")) ){
//			jsonMap.put("error", "1");
//			jsonMap.put("msg", "查询终止时间有误");
//			
//			return JSONUtils.printObject(jsonMap);
//		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "当前页有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        if(RegexUtils.isDate(parameters.get("startTime"))) {
 			beginTime = parameters.get("startTime");
 		}
 		
 		if(RegexUtils.isDate(parameters.get("lastTime"))) {
 			endTime = parameters.get("lastTime");
 		}
        
        PageBean<v_user_details> page = User.queryUserDetails(userId, Long.valueOf(parameters.get("purpose"))
        		,beginTime, endTime,Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE);
		
        if(page == null ){
        	jsonMap.put("error", "-4");
    		jsonMap.put("msg", "交易记录查询失败");
    		
    		return JSONUtils.printObject(jsonMap);
        }
        
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "交易记录查询成功");
		jsonMap.put("page", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *银行卡管理
	 *id 用户id
	 */
	public static String bankInfos(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		String ss = parameters.get("id");
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
        
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "银行卡查询成功");
		jsonMap.put("userBanks", userBanks);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *添加银行卡
	 *id 用户id
	 *bankName 银行名称
	 *bankCardNum 银行卡号
	 *cardUserName 银行卡持有人
	 */
	public static String addBank(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bankName"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行名称有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bankCardNum"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡账号有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("cardUserName"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "收款人有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = userId;
		bankUser.bankName = parameters.get("bankName");
		bankUser.account = parameters.get("bankCardNum");
		bankUser.accountName = parameters.get("cardUserName");
		
		bankUser.addUserBankAccount(error, false);  //目前不支持提现银行卡支行信息
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "银行卡添加成功");
		
		try {
			//添加银行卡发红包
			String redTypeName = Constants.RED_PACKAGE_TYPE_BANK;//注册类型
			
			long status  = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态
			
			List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
			if(null != reds && reds.size() > 0){
				for(t_red_packages_type redPackageType : reds){
					String desc = "";
					if(redPackageType.coupon_type == CouponTypeEnum.REDBAG.getCode() ) {
						desc = "添加银行卡发放红包";
					}else{
						desc = "添加银行卡发放加息券";
					}

					User user = new User();
					user.setId(userId);
					RedPackageHistory.sendRedPackage(user, redPackageType,desc);
				}
				Logger.info("添加银行卡发放优惠券短信通知成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
				
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *编辑银行卡
	 *bankId 银行卡ID
	 *bankName 银行名称
	 *bankCardNum 银行卡号
	 *cardUserName 银行卡持有人
	 */
	public static String editBank(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String bankId = parameters.get("bankId");
		if(StringUtils.isBlank(parameters.get("bankId")) || Long.valueOf(parameters.get("bankId")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡ID有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bankName"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行名称有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bankCardNum"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡账号有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("cardUserName"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "收款人有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		String branchBankName = parameters.get("branchBankName");
		if(StringUtils.isBlank(branchBankName)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "支行名称有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        
        int addBankCode = Integer.valueOf(parameters.get("bankCode"));
		int addProviceCode = Integer.valueOf(parameters.get("proviceCode"));
		int addCityCode = Integer.valueOf(parameters.get("cityCode"));
		
		String provice = DictBanksDate.queryProvinceByCode(addProviceCode);
		String city = DictBanksDate.queryCityByCode(addCityCode);
        
		UserBankAccounts bank = new UserBankAccounts();
		bank.id = Long.valueOf(bankId);
		if(bank.verified){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该卡已充值认证，不能修改");
			
			return JSONUtils.printObject(jsonMap);
		}
        UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = parameters.get("bankName");
		userAccount.account = parameters.get("bankCardNum");
		userAccount.accountName = parameters.get("cardUserName");
		userAccount.branchBankName = branchBankName;
		
		userAccount.bankCode = addBankCode+"";
		userAccount.provinceCode = addProviceCode;
		userAccount.cityCode = addCityCode;
	
		userAccount.province = provice;
		userAccount.city = city;
		
		 UserBankAccounts uAccount = new UserBankAccounts();
		 uAccount.bankName = parameters.get("bankName");
		 uAccount.account = parameters.get("bankCardNum");
		 uAccount.accountName = parameters.get("cardUserName");
		 uAccount.branchBankName = branchBankName;
			
		 uAccount.bankCode = addBankCode+"";
		 uAccount.provinceCode = addProviceCode;
		 uAccount.cityCode = addCityCode;
		
		 uAccount.province = provice;
		 uAccount.city = city;
		
		userAccount.appEditUserBankAccount(Long.valueOf(parameters.get("bankId")), userId, false, error,uAccount);  
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "银行卡编辑成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *校验安全问题
	 *id 用户id
	 *answer1 问题1
	 *answer2 问题2
	 *answer3 问题3
	 */
	public static String verifySafeQuestion(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer1 = parameters.get("answer1");
		answer1 = Encrypt.decrypt3DES(answer1, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer1)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "问题1有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer2 = parameters.get("answer2");
		answer2 = Encrypt.decrypt3DES(answer2, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer2)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "问题2有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer3 = parameters.get("answer3");
		answer3 = Encrypt.decrypt3DES(answer3, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer3)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "问题3有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User  user = new User();
		user.id = userId;
		user.verifySafeQuestion(answer1, answer2, answer3, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "安全问题回答正确");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *查询安全问题
	 *id 用户id
	 */
	public static String queryAnswers(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User  user = new User();
		user.id = userId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询安全问题成功");
		jsonMap.put("question1",user.questionName1);
		jsonMap.put("question2",user.questionName2);
		jsonMap.put("question3",user.questionName3);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *保存交易密码
	 *id 用户id
	 *newdealpwd 新交易密码
	 */
	public static String savePayPassword(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String financeType=parameters.get("financeType");

		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String newdealpwd = parameters.get("newdealpwd");
		newdealpwd = Encrypt.decrypt3DES(newdealpwd, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(newdealpwd)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "新密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}		
		
		String mobile = parameters.get("cellPhone");
		//String randomCode = parameters.get("randomCode");
		
		if(StringUtils.isBlank(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		/*if(StringUtils.isBlank(randomCode)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入验证码");
			
			return JSONUtils.printObject(jsonMap);
		}*/
		
		if(!RegexUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入正确的手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}

		long id=0;
		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {
			 id = User.queryIdByMobile(mobile, FinanceTypeEnum.BORROW.getCode(), error);
		} else if (StringUtils.isBlank(financeType) || INVEST.equals(financeType)) {
			 id = User.queryIdByMobile(mobile, FinanceTypeEnum.INVEST.getCode(), error);
		}

		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该手机号码不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        if(id != userId) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的绑定手机");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		/*if(Constants.CHECK_MSG_CODE) {
			String cCode = (Cache.get(mobile)).toString();
			
			if(cCode == null) {
				jsonMap.put("error", "-4");
				jsonMap.put("msg","验证码已失效，请重新点击发送验证码");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!randomCode.equals(cCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg","手机验证错误");
				
				return JSONUtils.printObject(jsonMap);
			}
		}*/
		
		User  user = new User();
		user.id = userId;
		
		user.addPayPassword(true, newdealpwd, newdealpwd, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "交易密码保存成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *修改交易密码
	 *id 用户id
	 *currentdealpwd 原交易密码
	 *newdealpwd 新交易密码
	 */
	public static String editPayPassword(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String newdealpwd = parameters.get("newdealpwd");
		newdealpwd = Encrypt.decrypt3DES(newdealpwd, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(newdealpwd)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "新交易密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String currentdealpwd = parameters.get("currentdealpwd");
		currentdealpwd = Encrypt.decrypt3DES(currentdealpwd, Constants.ENCRYPTION_KEY);
		if(StringUtils.isBlank(currentdealpwd)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "原交易密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		/*String mobile = parameters.get("cellPhone");
		String randomCode = parameters.get("randomCode");
		
		if(StringUtils.isBlank(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(randomCode)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入验证码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入正确的手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        User.queryIdByMobile(mobile, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该手机号码不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (Cache.get(mobile)).toString();
			
			if(cCode == null) {
				jsonMap.put("error", "-4");
				jsonMap.put("msg","验证码已失效，请重新点击发送验证码");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!randomCode.equals(cCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg","手机验证错误");
				
				return JSONUtils.printObject(jsonMap);
			}
		}*/
		
		User  user = new User();
		user.id = userId;
		
		user.editPayPassword(currentdealpwd,newdealpwd,newdealpwd,error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "交易密码修改成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *保存登录密码
	 *id 用户id
	 *oldloginpwd 原登录密码
	 *newloginpwd 新登录密码
	 */
	public static String savePassword(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String oldloginpwd = parameters.get("oldloginpwd");
		
		
		
		if(StringUtils.isBlank(oldloginpwd)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "原登录密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		oldloginpwd = Encrypt.decrypt3DES(oldloginpwd, Constants.ENCRYPTION_KEY);
		
		
		
		String newloginpwd = parameters.get("newloginpwd");
		
		if(StringUtils.isBlank(newloginpwd)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "新登录密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		newloginpwd = Encrypt.decrypt3DES(newloginpwd, Constants.ENCRYPTION_KEY);
		
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        
        t_users t = t_users.findById(userId);
		User  user = new User();
		user.id = userId;
		
		/*String mobile = parameters.get("cellPhone");
		String randomCode = parameters.get("randomCode");
		
		if(StringUtils.isBlank(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(randomCode)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入验证码");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg","请输入正确的手机号码");
			
			return JSONUtils.printObject(jsonMap);
		}*/
		
       // long id = User.queryIdByMobile(mobile, error);
		
		/*if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该手机号码不存在");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(id != userId) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的绑定手机");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (Cache.get(mobile)).toString();
			
			if(cCode == null) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg","验证码已失效，请重新点击发送验证码");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!randomCode.equals(cCode)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg","手机验证错误");
				
				return JSONUtils.printObject(jsonMap);
			}
		}*/
		
        user.editPasswordApp(oldloginpwd, newloginpwd,newloginpwd,error,t);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "登录密码修改成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *安全问题设置的状态
	 *id 用户id
	 */
	public static String questionStatus(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User user = new User();
		user.id = userId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "安全问题设置状态查询成功");
		jsonMap.put("questionStatus", user.isSecretSet);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *获取安全问题内容
	 *id 用户id
	 */
	public static String questionContent(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		List<SecretQuestion> questions = SecretQuestion.queryUserQuestion();
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "获取安全问题内容成功");
		jsonMap.put("questionArr", questions);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *保存安全问题
	 *id 用户id
	 *question1 安全问题1
	 *question2 安全问题2
	 *question3 安全问题3
	 *answer1 安全问题答案1
	 *answer2 安全问题答案2
	 *answer3 安全问题答案3
	 */
	public static String saveSafeQuestion(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("question1")) || Long.valueOf(parameters.get("question1")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题1有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("question2")) || Long.valueOf(parameters.get("question2")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题2有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("question3")) || Long.valueOf(parameters.get("question3")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题3有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer1 = parameters.get("answer1");
		answer1 = Encrypt.decrypt3DES(answer1, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer1)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题答案1有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer2 = parameters.get("answer2");
		answer2 = Encrypt.decrypt3DES(answer2, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer2)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题答案2有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String answer3 = parameters.get("answer3");
		answer3 = Encrypt.decrypt3DES(answer3, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(answer3)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "安全问题答案3有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User  user = new User();
		user.id = userId;
		
		user.secretQuestionId1 = Long.valueOf(parameters.get("question1"));
		user.secretQuestionId2 = Long.valueOf(parameters.get("question2"));
		user.secretQuestionId3 = Long.valueOf(parameters.get("question3"));
		user.answer1 = answer1;
		user.answer2 = answer2;
		user.answer3 = answer3;
		
		user.updateSecretQuestion(true, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "安全问题设置成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *邮箱激活状态
	 *id 用户id
	 */
	public static String emailStatus(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User user = new User();
		user.id = userId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "邮箱激活状态查询成功");
		jsonMap.put("status", user.isEmailVerified);
		jsonMap.put("emailaddress", user.email);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *修改邮箱
	 *id 用户id
	 *emailaddress 邮箱地址
	 */
	public static String saveEmail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String emailaddress = parameters.get("emailaddress");
		//emailaddress = Encrypt.decrypt3DES(emailaddress, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(emailaddress)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "邮箱地址有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User  user = new User();
		user.id = userId;
		user.email = emailaddress;
		
		if(user.editEmail(error) < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		TemplateEmail.activeEmail(user, error);
		//发送失败不提示
//		if(error.code < 0){
//			jsonMap.put("error", "-4");
//			jsonMap.put("msg", error.msg);
//			
//			return JSONUtils.printObject(jsonMap);
//		}
		
		String emailUrl = EmailUtil.emailUrl(emailaddress);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "修改邮箱成功");
		jsonMap.put("emailUrl", emailUrl);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *安全手机详情及状态
	 *id 用户id
	 */
	public static String phoneStatus(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User user = new User();
		user.id = userId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "安全问题设置状态查询成功");
		jsonMap.put("status", user.isMobileVerified);
		jsonMap.put("phoneNum", user.mobile);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *查看信用等级规则
	 */
	public static String viewCreditRule(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		List<v_credit_levels> CreditLevels = CreditLevel.queryCreditLevelList(error);
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "规则查询成功");
		jsonMap.put("list", CreditLevels);
		jsonMap.put("totalNum", CreditLevels.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *我的信用等级
	 *id 用户id
	 */
	public static String myCredit(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
		User user = new User();
		user.id = userId;
		
		//是否有未审核的超额借款
//		boolean isOverBorrow = OverBorrow.haveAuditingOverBorrow(user.id, error);
//		
//		if (error.code < 0) {
//			jsonMap.put("error", "-2");
//			jsonMap.put("msg", error.msg);
//			
//			return JSONUtils.printObject(jsonMap);
//		}
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "我的信用等级查询成功");
		jsonMap.put("creditRating", user.myCredit.imageFilename);
		jsonMap.put("creditScore", user.userScore.credit_score);
		jsonMap.put("creditLimit", user.balanceDetail.credit_line);
		jsonMap.put("lastCreditLine", user.lastCreditLine);
		jsonMap.put("overCreditLine", user.balanceDetail.credit_line - user.lastCreditLine);
		//jsonMap.put("isOverBorrow", isOverBorrow);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *查看信用积分规则
	 */
	public static String creditintegral(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
        long auditItemCount = AuditItem.auditItemCount();
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看信用积分规则成功");
		jsonMap.put("auditItemCount", auditItemCount);
		jsonMap.put("normalPayPoints", backstageSet.normalPayPoints);
		jsonMap.put("fullBidPoints", backstageSet.fullBidPoints);
		jsonMap.put("investpoints", backstageSet.investpoints);
		jsonMap.put("overDuePoints", backstageSet.overDuePoints);
		jsonMap.put("creditLimit", amountKey);//积分对应的信用额度
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *审核科目积分明细
	 */
	public static String creditItem(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Double.parseDouble(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "当前页数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        PageBean<t_dict_audit_items> page = AuditItem.queryEnableAuditItems("", Integer.parseInt(parameters.get("currPage")), Constants.APP_PAGESIZE, error); // 审核资料
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看信用积分规则成功");
		jsonMap.put("list", page);
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("creditLimit", amountKey);//积分对应的信用额度
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *审核资料积分明细
	 */
	public static String auditItemScore(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_detail_credit_score_audit_items> page = User.queryCreditDetailAuditItem(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "", error);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查看审核资料积分明细成功");
		jsonMap.put("list", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *正常还款积分明细
	 */
	public static String creditDetailRepayment(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_detail_credit_score_normal_repayment> page = User.queryCreditDetailRepayment(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "");
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看正常还款积分明细成功");
		jsonMap.put("list", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *成功借款积分明细
	 */
	public static String creditDetailLoan(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_detail_credit_score_loan> page = User.queryCreditDetailLoan(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "");
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看成功借款积分明细成功");
		jsonMap.put("list", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *成功投标积分明细
	 */
	public static String creditDetailInvest(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_detail_credit_score_invest> page = User.queryCreditDetailInvest(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "");
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看成功投标积分明细成功");
		jsonMap.put("list", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *逾期扣分积分明细
	 */
	public static String creditDetailOverdue(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_detail_credit_score_overdue> page = User.queryCreditDetailOverdue(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "");
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "查看逾期扣分积分明细成功");
		jsonMap.put("list", page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *申请超额借款
	 *excessAmount 申请金额
	 *applyReason 申请原因
	 *jsonAuditItems 审核资料
	 */
	public static String applyForOverBorrow(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("excessAmount")) || Double.parseDouble(parameters.get("excessAmount")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("applyReason"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请原因有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        User user = new User();
        user.id = userId;
        
		JSONArray jsonArray = JSONArray.fromObject(parameters.get("jsonAuditItems"));
		
		List<Map<String,String>> auditItems = (List)jsonArray;
		
//System.out.println(auditItems.get(0).get("id"));
		
		if(Long.valueOf(parameters.get("excessAmount")) >= Integer.MAX_VALUE) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请超额借款已超过最大申请金额");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		new OverBorrow().applyFor(user, Integer.valueOf(parameters.get("excessAmount")), parameters.get("applyReason"), auditItems, error);
		
		if(error.code == -999){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONUtils.printObject(jsonMap);
		} else if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "您的超额借款申请已提交，请耐心等待审核结果。");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *申请超额借款记录列表
	 *id 用户id
	 */
	public static String overBorrowLists(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		List<t_user_over_borrows> overBorrows = OverBorrow.queryUserOverBorrows(userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "申请超额借款记录列表查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "申请超额借款记录列表查询成功");
		jsonMap.put("list", overBorrows);
		jsonMap.put("totalNum", overBorrows.size());
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *选择超额借款审核资料库
	 *id 用户id
	 */
	public static String selectAuditItemsInit(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		List<AuditItem> auditItems = UserAuditItem.queryAuditItemsOfOverBorrow(userId, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "超额借款审核资料查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); 
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value); // 积分对应额度
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "超额借款审核资料查询成功");
		jsonMap.put("list", auditItems);
		jsonMap.put("totalNum", auditItems.size());
		jsonMap.put("creditLimit", amountKey);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *信用计算器规则
	 */
	public static String wealthToolkitCreditCalculator(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
        List<AuditItem> auditItems = AuditItem.queryAuditItems(error);
        
        if(error.code < 0){
        	jsonMap.put("error", "-4");
			jsonMap.put("msg", "信用计算器规则查询失败");
			
			return JSONUtils.printObject(jsonMap);
        }
		
		 String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		
		 if(error.code < 0){
        	jsonMap.put("error", "-4");
			jsonMap.put("msg", "信用计算器规则查询失败");
			
			return JSONUtils.printObject(jsonMap);
	        }
		
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "信用计算器规则查询成功");
		jsonMap.put("list", auditItems);
		jsonMap.put("amountKey", amountKey);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *利率计算器
	 *amount 借款金额
	 *deadline 借款期限
	 *apr 年利率
	 *repayType 还款方式
	 *awardScale 奖金比例
	 *bonus 固定奖金
	 */
	public static String aprCalculator(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("apr")) || Double.parseDouble(parameters.get("apr")) < 0 || Double.parseDouble(parameters.get("apr")) > 100){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "年利率有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("amount")) || Double.parseDouble(parameters.get("amount")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("loadType")) || Integer.valueOf(parameters.get("loadType")) < 0 || Integer.valueOf(parameters.get("loadType")) > 2){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款类型有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("deadline")) || Integer.valueOf(parameters.get("deadline")) < 0 || Integer.valueOf(parameters.get("deadline")) > 1000){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "借款期限有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("repayType")) || Integer.valueOf(parameters.get("repayType")) < 0 ) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款方式有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		double monthRate = Double.parseDouble(parameters.get("apr")) / 1200;
		double amount = Double.parseDouble(parameters.get("amount"));
		int deadline = Integer.valueOf(parameters.get("deadline"));
		int repayType = Integer.valueOf(parameters.get("repayType"));
		int loadType = Integer.valueOf(parameters.get("loadType"));
		
		double monPay = 0;
		double dayRate = 0;//日利率
		double award = 0;//奖金
		double interest = 0;
//		double earning = 0;
		double sum = 0;
		DecimalFormat df = new DecimalFormat("#.00");
		
		if(loadType == 1){
			//dayRate = Arith.div(Double.parseDouble(parameters.get("apr")), 36000,4);
			dayRate = Double.parseDouble(parameters.get("apr"))/36000;
			
			if(repayType == 1){//等额本息还款
				monPay = Double.valueOf(Arith.mul(amount, monthRate) * Math.pow((1 + monthRate), 1))/ 
						Double.valueOf(Math.pow((1 + monthRate), 1) - 1);//每个月要还的本金和利息
						monPay = Arith.round(monPay, 2);
		        interest = Arith.sub(Arith.mul(monPay, 1), amount); 	
		        
	//	        earning = Arith.excelRate((amount - award),
	//					Double.parseDouble(df.format(monPay)), deadline, 200, 15)*12*100;
						
			}else if(repayType == 2){//先息后本
				interest = Arith.round(Arith.mul(amount, dayRate * deadline), 2);
				monPay = interest;
	//			earning = Arith.rateTotal(interest + amount,
	//					(amount - award), deadline)*100;
			}else{
				interest = Arith.round(Arith.mul(amount, dayRate * deadline), 2);
				monPay = interest + amount;
	//			earning = Arith.rateTotal(interest + amount,
	//					(amount - award), deadline)*100;
			}
			sum = interest + amount;
		} else {
			if(repayType == 1){//等额本息还款
				monPay = Double.valueOf(Arith.mul(amount, monthRate) * Math.pow((1 + monthRate), deadline))/ 
						Double.valueOf(Math.pow((1 + monthRate), deadline) - 1);//每个月要还的本金和利息
						monPay = Arith.round(monPay, 2);
		        interest = Arith.sub(Arith.mul(monPay, deadline), amount); 	
	//	        earning = Arith.excelRate((amount - award),
	//					Double.parseDouble(df.format(monPay)), deadline, 200, 15)*12*100;
			}else if(repayType == 2){//先息后本
				interest = Arith.round(Arith.mul(amount, monthRate * deadline), 2);
				monPay = Arith.round(Arith.mul(amount, monthRate), 2);
	//			earning = Arith.rateTotal(interest + amount,
	//					(amount - award), deadline)*100;
				
			}else{
				interest = Arith.round(Arith.mul(amount, monthRate * deadline), 2);
				monPay = interest + amount;
	//			earning = Arith.rateTotal(interest + amount,
	//					(amount - award), deadline)*100;
			}
			
			sum = interest + amount;
		}
		
//		earning = Double.parseDouble(df.format(earning)+"");
		
		if(!StringUtils.isBlank(parameters.get("awardScale"))){
			award = Double.parseDouble(parameters.get("awardScale")) * amount;
		}
		
		if(!StringUtils.isBlank(parameters.get("bonus"))){
			award = Double.parseDouble(parameters.get("bonus"));
		}
		
		double serviceFee  = interest * BackstageSet.getCurrentBackstageSet().investmentFee / 100; // 服务费
		
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "利率计算器查询成功");
		jsonMap.put("amount", amount);
		jsonMap.put("monPay", monPay);
		jsonMap.put("serviceFee", serviceFee);
		jsonMap.put("award", award);
		jsonMap.put("interest", interest);
		jsonMap.put("earning", monthRate*1200);
		jsonMap.put("sum", sum - Arith.round(serviceFee,2)+award);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *通过邮箱重置安全问题
	 *id 用户id
	 */
	public static String resetSafeQuestion(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			jsonMap.put("error", "-2"); 
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 4;

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.SECRET_QUESTION);
		String url = Constants.RESET_QUESTION_EMAIL + sign;

		String content = tEmail.content;

		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.companyTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));

		TemplateEmail.sendEmail(0, user.email, tEmail.title, content, error);

		if (error.code < 0) {
			jsonMap.put("error", "-4"); 
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "通过邮箱重置安全问题成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *删除银行卡
	 *accountId 银行卡id
	 */
	public static String deleteBank(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("accountId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		UserBankAccounts.deleteUserBankAccount(userId, Long.parseLong(parameters.get("accountId")), error);
		
		if (error.code < 0) {
			jsonMap.put("error", "-4"); 
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1"); 
		jsonMap.put("msg", "删除银行卡成功");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *发件箱信息
	 *id 用户id
	 */
	public static String outboxMsgs(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.valueOf(parameters.get("currPage")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "分页数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_user_outbox> page = 
			StationLetter.queryUserOutboxMsgs(userId, Integer.valueOf(parameters.get("currPage")), Constants.APP_PAGESIZE, "", error);
	
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "发件箱信息查询成功");
		jsonMap.put("page", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *发件箱详情信息
	 *id 用户id
	 *index 当前邮件索引
	 *status 操作状态(上一条，下一条)
	 */
	public static String outboxMsgDetail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("index")) || Integer.valueOf(parameters.get("index")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "数据索引index有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("status"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "状态数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		int mark = Integer.parseInt(parameters.get("status"));
		int index = Integer.parseInt(parameters.get("index"));
		
		if(mark == 1){
			index += 1;
			
		}
		
		if(mark == 2){
			index -= 1;
			
		}
		
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_user_outbox> page = StationLetter.queryUserOutboxMsgDetail(userId, index, "", error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(page.totalCount == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", false);
		}else if(page.currPage == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", true);
		}else if(page.currPage == page.totalCount){
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", false);
		}else{
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", true);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "发件箱详情查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("index", index);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *系统邮件详情信息
	 *id 用户id
	 *currPage 当前邮件索引
	 *status 操作状态(上一条，下一条)
	 */
	public static String systemMsgDetail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("index")) || Integer.valueOf(parameters.get("index")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "数据索引index有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("status"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "状态数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		int mark = Integer.parseInt(parameters.get("status"));
		int index = Integer.parseInt(parameters.get("index"));
		
		if(mark == 1){
			index += 1;
			
		}
		
		if(mark == 2){
			index -= 1;
			
		}
		
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_system> page = 
			StationLetter.queryUserSystemMsgDetail(userId, index, "",0, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(page.totalCount == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", false);
		}else if(page.currPage == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", true);
		}else if(page.currPage == page.totalCount){
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", false);
		}else{
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", true);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "系统邮件详情信息查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("index", index);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *收件箱消息详情
	 *id 用户id
	 *index 当前邮件索引
	 *status 操作状态(上一条，下一条)
	 */
	public static String inboxMsgDetail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("index")) || Integer.valueOf(parameters.get("index")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "索引数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("status"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "状态数据有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		int mark = Integer.parseInt(parameters.get("status"));
		int index = Integer.parseInt(parameters.get("index"));
		
		if(mark == 1){
			index += 1;
			
		}
		
		if(mark == 2){
			index -= 1;
			
		}
		
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_messages_user_inbox> page = 
			StationLetter.queryUserInboxMsgDetail(userId, index, "",0, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(page.totalCount == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", false);
		}else if(page.currPage == 1){
			jsonMap.put("page", page.page.get(0));
			jsonMap.put("up", false);
			jsonMap.put("down", true);
		}else if(page.currPage == page.totalCount){
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", false);
		}else{
			jsonMap.put("page", page.page.get(1));
			jsonMap.put("up", true);
			jsonMap.put("down", true);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "收件箱详情信息查询成功");
		jsonMap.put("totalNum", page.totalCount);
		jsonMap.put("index", index);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *用户邮箱，手机，安全问题，交易密码状态
	 *id 用户id
	 */
	public static String userInfoStatus(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
		if(StringUtils.isBlank(user.payPassword)){
			jsonMap.put("payPasswordStatus", false);
		}else{
			jsonMap.put("payPasswordStatus", true);
		}
		
		jsonMap.put("email", user.email);
		jsonMap.put("emailStatus", user.isEmailVerified);
		
		jsonMap.put("mobile", user.mobile);
		jsonMap.put("emailStatus", user.isMobileVerified);


		jsonMap.put("error", "-1");
		jsonMap.put("msg", "用户状态查询成功");
		jsonMap.put("teleStatus", user.isMobileVerified);
		jsonMap.put("SecretStatus", user.isSecretSet);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *净值计算器
	 */
	public static String kitNetValueCalculator(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		double bailScale = Product.queryNetValueBailScale(error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "获取净值产品的保证金比例有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("balance")) || Double.parseDouble(parameters.get("balance")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "可用金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("receive")) || Double.parseDouble(parameters.get("receive")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "待收金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("pay")) || Double.parseDouble(parameters.get("pay")) < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "待付金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		double balance = Double.parseDouble(parameters.get("balance"));
		double receive = Double.parseDouble(parameters.get("receive"));
		double pay = Double.parseDouble(parameters.get("pay"));
		
		double amount = Arith.round(((balance + receive - pay) * 0.7)/(1 + (bailScale/100)), 2);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "借款金额查询成功");
		jsonMap.put("amount", amount < 0 ? 0 :amount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *针对当前用户的所有借款提问
	 *id 用户id
	 *currPage 当前页
	 */
	public static String bidQuestions(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("currPage")) || Integer.parseInt(parameters.get("currPage")) <= 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "当前页数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		PageBean<BidQuestions> page = BidQuestions.queryQuestion(Integer.parseInt(parameters.get("currPage")),
				Constants.APP_PAGESIZE, 0, "", 0, userId, error);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "借款提问查询成功");
		jsonMap.put("list", page);
		jsonMap.put("totalNum", page.totalCount);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *提问详情
	 *Id 提问id
	 */
	public static String bidQuestionDetail(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "提问id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		BidQuestions bidQuestion = BidQuestions.queryBidQuestionDetail(Long.parseLong(parameters.get("id")), error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(null == bidQuestion){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidQuestion.bidId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提问详情查询成功");
		jsonMap.put("bidQuestion", bidQuestion);
		jsonMap.put("bidNo", bid.no);
		jsonMap.put("bidAmont", bid.amount);
		jsonMap.put("bidApr", bid.apr);
		jsonMap.put("bidPeriod", bid.period);
		jsonMap.put("bidRepayName", bid.repayment.name);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *提问详情
	 *Id 提问id
	 */
	public static String creditItemd(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("Id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "提问id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		BidQuestions bidQuestion = BidQuestions.queryBidQuestionDetail(Long.parseLong(parameters.get("Id")), error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(null == bidQuestion){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "查询失败");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.id = bidQuestion.bidId;
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提问详情查询成功");
		jsonMap.put("bidQuestion", bidQuestion);
		jsonMap.put("bidNo", bid.no);
		jsonMap.put("bidAmont", bid.amount);
		jsonMap.put("bidApr", bid.apr);
		jsonMap.put("bidPeriod", bid.period);
		jsonMap.put("bidRepayName", bid.repayment.name);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *查看超额申请详情
	 */
	public static String viewOverBorrow(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("overBorrowId")) ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		List<v_user_audit_items> auditItems = OverBorrow.queryAuditItems(Long.parseLong(parameters.get("overBorrowId")), error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		t_user_over_borrows overBorrows = OverBorrow.queryOverBorrowById(Long.parseLong(parameters.get("overBorrowId")), error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "超额申请详情查询成功");
		jsonMap.put("auditItems", auditItems);
		jsonMap.put("overBorrows", overBorrows);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *申请提现
	 */
	public static String submitWithdrawal(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("amount"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请金额有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("bankId"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String payPassword = parameters.get("payPassword");
		payPassword = Encrypt.decrypt3DES(payPassword, Constants.ENCRYPTION_KEY);
		
		if(StringUtils.isBlank(payPassword)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "交易密码有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("type"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数type有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(Integer.parseInt(parameters.get("type")) != 1 && Integer.parseInt(parameters.get("type")) != 2  ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数type有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg","用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
		user.withdrawal(Double.parseDouble(parameters.get("amount")), Integer.parseInt(parameters.get("bankId")), payPassword, Integer.parseInt(parameters.get("type")), false, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		 double amount = User.queryRechargeIn(user.id, error);
		
		 if(error.code < 0) {
				jsonMap.put("error", "-4");
				jsonMap.put("msg", error.msg);
				
				return JSONUtils.printObject(jsonMap);
			}
		 
		double withdrawalAmount = user.balance - amount;//（最高）可提现余额
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提现申请成功");
		jsonMap.put("withdrawalAmount",withdrawalAmount);
		jsonMap.put("userBalance", user.balanceDetail.user_amount + user.balanceDetail.freeze);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *提现初始信息
	 */
	public static String withdrawal(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg","解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		
        double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		double withdrawalAmount = user.balance - amount;//（最高）可提现余额
		
		withdrawalAmount = ServiceFee.maxWithdralAmount(withdrawalAmount);
		
		if(withdrawalAmount < 0) {
			withdrawalAmount = 0;
		}
		
		List<UserBankAccounts> banks = UserBankAccounts.queryUserAllBankAccount(user.id);
		
		List<UserBankAccounts> toBanks = new ArrayList<UserBankAccounts>();
		
		for(UserBankAccounts vo : banks){
			//if(vo.verified){ ----180404 原来的逻辑充值后银行卡为已审核通过状态, 审核后台的卡才能提现, 现在添加多卡忽略审核状态
				toBanks.add(vo);
			//}
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提现初始信息查询成功");
		jsonMap.put("withdrawalAmount",withdrawalAmount);
		jsonMap.put("bankList", toBanks.size() > 0 ? toBanks : banks);
		jsonMap.put("userBalance", user.balanceDetail.user_amount + user.balanceDetail.freeze);
		
		if(StringUtils.isBlank(user.payPassword)){
			jsonMap.put("payPasswordStatus", false);
		}else{
			jsonMap.put("payPasswordStatus", true);
		}
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *提现记录
	 */
	public static String withdrawalRecords(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id错误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		PageBean<v_user_withdrawals> page = User.queryWithdrawalRecord(userId, parameters.get("type"), 
				parameters.get("beginTime"), parameters.get("endTime"), parameters.get("currPage"), "18", error);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提现记录查询成功");
		jsonMap.put("records",page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 上传文件
	 */
	public static String uploadFile(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(parameters.get("id"))){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("type"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入 文件类型有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("imgStr"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入 文件参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(parameters.get("fileExt"))){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入 文件后缀有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String type = parameters.get("type");
		String imgStr = parameters.get("imgStr");
		String fileExt = parameters.get("fileExt");
		
		byte [] imgByte = null;
		
		try {
			imgByte = new sun.misc.BASE64Decoder().decodeBuffer(imgStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File file = FileUtil.strToFile(imgByte, "tmp\\uploads\\"+System.currentTimeMillis()+"."+fileExt); 
		
		Map<String, Object> fileInfo = FileUtil.uploadFile(file, Integer.parseInt(type), error);
		
		if (error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		user.photo = fileInfo.get("fileName").toString();
		user.editPhoto(error);
		
		if (error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		jsonMap.put("imgStr",user.photo);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 *官方活动
	 */
	public static String queryOfficialActivity(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_content_news> page = News.queryOfficialActivity(parameters.get("currPageStr"), null, error);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "官方活动查询成功");
		jsonMap.put("records",page.page);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 取消关注
	 */
	public static String cancelAttentionUser(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String attention = parameters.get("attentionId");
		
		if(StringUtils.isBlank(attention) || !NumberUtil.isNumeric(attention)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入关注用户id参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		User.cancelAttentionUser(Long.parseLong(attention), error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 获取vip相关信息
	 */
	public static String vipInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "vip信息获取 成功");
		jsonMap.put("vipFee", backstageSet.vipFee);
		jsonMap.put("vipTimeType", backstageSet.vipTimeType);
		jsonMap.put("vipMinTimeType", backstageSet.vipMinTimeType);
		jsonMap.put("vipMinTimeLength", backstageSet.vipMinTimeLength);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 删除黑名单
	 */
	public static String deleteBlackList(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String blacklistId = parameters.get("blacklistId");
		
		if(StringUtils.isBlank(blacklistId) || !NumberUtil.isNumeric(blacklistId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入黑名单用户id参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id错误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		User user = new User();
		
		user.deleteBlacklist(userId, Long.parseLong(blacklistId), error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 删除收藏标
	 */
	public static String deleteAttentionBid(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String bidId = parameters.get("bidId");
		
		if(StringUtils.isBlank(bidId) || !NumberUtil.isNumeric(bidId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入标id参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");

			jsonMap.put("msg", "解析用户id有误");

			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt.cancleBid(Long.parseLong(bidId), userId, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 删除收藏债权
	 */
	public static String deleteAttentionBebt(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String attentionId = parameters.get("attentionDebtId");
		
		if(StringUtils.isBlank(attentionId) || !NumberUtil.isNumeric(attentionId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入关注债权id参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Debt.canaleDebt(Long.parseLong(attentionId), error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-4");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 保存推送设置
	 */
	public static String pushSetting(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String billPush = parameters.get("billPush");
		
		if(StringUtils.isBlank(billPush) || !NumberUtil.isNumeric(billPush)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入账单设置参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String investPush = parameters.get("investPush");
		
		if(StringUtils.isBlank(investPush) || !NumberUtil.isNumeric(investPush)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入满标设置参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		String activityPush = parameters.get("activityPush");
		
		if(StringUtils.isBlank(activityPush) || !NumberUtil.isNumeric(activityPush)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "传入活动单设置参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		User user = new User();
		
		user.isBillPush = billPush.equals("1") ? true : false;
		user.isInvestPush = investPush.equals("1") ? true : false;
		user.isActivityPush = activityPush.equals("1") ? true : false;
		
		user.pushSetting(userId, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 获取推送推送设置
	 */
	public static String queryPushSetting(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		t_users user = User.queryPushSetting(userId, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", error.msg);
		jsonMap.put("billPush", user.is_bill_push ? 1 : 0);
		jsonMap.put("investPush", user.is_invest_push ? 1 : 0);
		jsonMap.put("activityPush", user.is_activity_push ? 1 : 0);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 提交用户未交费资料
	 */
	public static String createUserAuditItem(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String items = parameters.get("items");
		
		if(StringUtils.isBlank(items)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择上传的资料");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userItemId = Security.checkSign(parameters.get("sign"), Constants.USER_ITEM_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "上传资料参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		UserAuditItem item = new UserAuditItem();
		item.lazy = true;
		item.userId = userId;
		item.id = userItemId;
		item.imageFileNames = items;
		item.createUserAuditItem(error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提交资料成功，请等待管理员审核");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 提交用户资料
	 */
	public static String submitUploadedItems(Map<String, String> parameters) throws IOException {
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Map<String, Object> info = UserAuditItem.queryUploadItems(userId, error);
 		if (error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择上传的资料");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		long userItemId = Security.checkSign(parameters.get("sign"), Constants.USER_ITEM_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析用户资料id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		double balance = 0;
		
		if(info.get("fees") == null) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户没有上传未付款的资料");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		double fees = (Double) info.get("fees");
		User user = new User();
		user.id = userId;
		v_user_for_details details = user.balanceDetail;
		
		if(null == details) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "查询用户资金出现错误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(Constants.IPS_ENABLE){
			balance = details.user_amount2;
		}else{
			balance = details.user_amount;
		}
		
		if(fees > balance){
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "对不起，您可用余额不足");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		
		UserAuditItem item = new UserAuditItem();
		item.id = userItemId;
		
		item.submitUploadedItems(userId, balance, error);
		
		if(error.code == -999) {
			jsonMap.put("error", "-999");
			jsonMap.put("msg", "您余额不足，请充值");
			
			return JSONUtils.printObject(jsonMap);
		} else if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "提交成功！");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/*
	 * 清空用户上传未付款的资料
	 */
	public static String clearAuditItem(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(parameters.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		UserAuditItem.clearUploadedItems(userId, error);
		
		if( error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功清空用户上传未付款的资料");
		
		return JSONUtils.printObject(jsonMap);
	}

	/**
	 * APP端用户启动图
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String getStartMap(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		List<String> fileNames = Ads.queryAdsImageNamesByLocation(Constants.STARTUP_BOOT_APP, error);
		
		if(error.code < 0) {
			jsonMap.put("error", -2);
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("fileNames", fileNames);
		
		// 是否自动登录
		jsonMap.put("isAutoLogin", Play.configuration.getProperty("app_is_auto_login", "1"));
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 查询所有的借款标产品(APP)
	 * @return
	 * @throws IOException
	 */
	public static String queryAllProducts() throws IOException{
		ErrorInfo error = new ErrorInfo();
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<t_products> products = Product.queryAllProducts(error);
		
		if (error.code < 0) {
			jsonMap.put("error", -2);
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询所有借款产品成功！");
		jsonMap.put("products", products);
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 查询所有的返款方式
	 * @return
	 * @throws IOException 
	 */
	public static String queryAllRepaymentTypes() throws IOException{
		ErrorInfo error = new ErrorInfo();
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<Repayment> repayments = Bid.Repayment.queryRepaymentTypeApp(error);
		
		if (error.code < 0) {
			
			jsonMap.put("error", -2);
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询所有的还款方式成功！");
		jsonMap.put("repayments", repayments);
		
		return JSONUtils.printObject(jsonMap);
	}
	/**
	 * 查询账户资产详情
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryAccountCapital(Map<String, String> parameters) throws IOException {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = new HashMap<String, Object>();
		String userIdStr = parameters.get("id");
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0 || userId < 0){
			map.put("error", "-2");
			map.put("msg", "解析用户id有误");
			
			return JSONObject.fromObject(map).toString();
		}
		User user = new User();
		user.id = userId;
		
        double amount = User.queryRechargeIn(user.id, error);
		
		if(error.code < 0) {
			map.put("error", "-4");
			map.put("msg", error.msg);
			
			return JSONUtils.printObject(map);
		}
		
		double withdrawalAmount = user.balance - amount;//（最高）可提现余额
		
		UserOZ accountInfo = new UserOZ(userId);
		
		map.put("balance", accountInfo.user_account-accountInfo.freeze);
		map.put("amount", accountInfo.user_account);
		map.put("frozen_amount", accountInfo.freeze);
		map.put("repay_amount", accountInfo.receive_amount);
		map.put("back_amount", accountInfo.repayment_amount);
		map.put("sum_income", BillInvests.querySumIncome(userId));
		map.put("withdrawalAmount", withdrawalAmount);
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		
		map.put("hasBanks", userBanks.size() > 0);
		
		map.put("error", "-1");
		map.put("msg", "查询成功");
		
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 查询账户收益
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryAccountIncome(Map<String, String> parameters) throws IOException {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = new HashMap<String, Object>();
		String userIdStr = parameters.get("id");
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		map.put("sum_income", BillInvests.querySumIncome(userId));
		map.put("month_income", BillInvests.queryMonthSumIncome(userId));
		map.put("year_income", BillInvests.queryYearSumIncome(userId));
		map.put("share_income_url", "");
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}

	/**
	 * 手机号码注册
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String registerMobile(Map<String, String> parameters) throws IOException {
		ErrorInfo error = new ErrorInfo();

		String name = parameters.get("userName");		
		String mobile = parameters.get("mobile");
		String smsCode = parameters.get("smsCode");
		String password = parameters.get("password");
		password = Encrypt.decrypt3DES(password, Constants.ENCRYPTION_KEY);
		String recommendName = parameters.get("recommendName");
		String financeType=parameters.get("financeType");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", "-3");
		
		if (StringUtils.isBlank(name)) {
			map.put("msg", "请填写用户名");
			return JSONUtils.printObject(map);
		}

		if (StringUtils.isBlank(password)) {
			map.put("msg", "请输入密码");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isValidUsername(name)) {
			map.put("msg", "请填写符合要求的用户名");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			map.put("msg", "请填写正确的手机号码");
			return JSONUtils.printObject(map);
		}
		
		if (StringUtils.isBlank(smsCode)) {
			map.put("msg", "请输入短信校验码");
			return JSONUtils.printObject(map);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);
			
			if(cCode == null) {
				map.put("msg", "校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(map);
			}
			
			if(!smsCode.equals(cCode)) {
				map.put("msg", "校验码错误");
				return JSONUtils.printObject(map);
			}
		}
		
		if (!StringUtils.isBlank(financeType)) {// 借款人
			User.isMobileExist(mobile, null, FinanceTypeEnum.BORROW.getCode(), error);
		} else {// 投资人
			User.isMobileExist(mobile, null, FinanceTypeEnum.INVEST.getCode(), error);
		}


		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		if (!RegexUtils.isValidPassword(password)) {
			map.put("msg", "请填写符合要求的密码");
			return JSONUtils.printObject(map);
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		String recoName = "";
		
		if(StringUtils.isNotBlank(recommendName)) {
			User.isNameExist(recommendName, error);
			if (error.code == -2) {
				recoName = recommendName;
		    }
		}
		
		User user = new User();
		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = mobile;
		user.isMobileVerified = true;
		user.recommendUserName = recoName;
		user.register(Constants.CLIENT_PC, error);
		
		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		/*
		 *判断发放红包
		 *
		 */
	// 1、判断是否有注册红包类型红包设置
//		String redTypeName = Constants.RED_PACKAGE_TYPE_REGIST;
//		long status  = Constants.RED_PACKAGE_STATUS_OVERDUE;
//		t_red_packages_type redPackageType = RedPackage.isExist(redTypeName, status);
//		if(null != redPackageType){
//			String desc = "APP注册发放红包";
//			RedPackageHistory.sendRedPackage(user, redPackageType,desc);
//			Logger.error("APP注册发放红包短信通知失败");
//		}	

		map.put("msg", "注册成功");
		map.put("error", "-1");
		return JSONUtils.printObject(map);
	}

	public static String getBankList() throws IOException {
		Map<String,Object> map =new HashMap<String, Object>();
		
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		Object[] bankCodeIds = bankCodeNameTable.keySet().toArray();
		String[] bankCodeNames = new String[bankCodeIds.length];
		for (int i = 0; i < bankCodeNames.length; i++) {
			bankCodeNames[i] = bankCodeNameTable.get(bankCodeIds[i]);
		}
		map.put("bankCodeIds", bankCodeIds);
		map.put("bankCodeNames", bankCodeNames);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}
	
	public static String getProvinceList() throws IOException {
		Map<String,Object> map =new HashMap<String, Object>();
		
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		Object[] provinceIds = provinceCodeNameTable.keySet().toArray();
		String[] provinceNames = new String[provinceIds.length];
		for (int i = 0; i < provinceNames.length; i++) {
			provinceNames[i] = provinceCodeNameTable.get(provinceIds[i]);
		}
		map.put("provinceIds", provinceIds);
		map.put("provinceNames", provinceNames);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 获取银行，省份列表
	 */
	public static String getBankAndProvinceList() throws IOException {
		Map<String,Object> map =new HashMap<String, Object>();
		 
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		Object[] provinceIds = provinceCodeNameTable.keySet().toArray();
		String[] provinceNames = new String[provinceIds.length];
		for (int i = 0; i < provinceNames.length; i++) {
			provinceNames[i] = provinceCodeNameTable.get(provinceIds[i]);
		}
		map.put("provinceIds", provinceIds);
		map.put("provinceNames", provinceNames);
		
		map.put("citylist", DictBanksDate.citys);
		
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		Object[] bankCodeIds = bankCodeNameTable.keySet().toArray();
		String[] bankCodeNames = new String[bankCodeIds.length];
		for (int i = 0; i < bankCodeNames.length; i++) {
			bankCodeNames[i] = bankCodeNameTable.get(bankCodeIds[i]);
			 
			
		}
		map.put("bankCodeIds", bankCodeIds);
		map.put("bankCodeNames", bankCodeNames);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 借款端  获取协议支付银行，省份列表
	 */
	public static String getProtocolBankAndProvinceList() throws IOException {
		Map<String,Object> map =new HashMap<String, Object>();
		Map<String,String> provinceCodeNameTable = DictBanksDate.provinceCodeNameTable;
		Object[] provinceIds = provinceCodeNameTable.keySet().toArray();
		String[] provinceNames = new String[provinceIds.length];
		for (int i = 0; i < provinceNames.length; i++) {
			provinceNames[i] = provinceCodeNameTable.get(provinceIds[i]);
		}
		map.put("provinceIds", provinceIds);
		map.put("provinceNames", provinceNames);
		
		map.put("citylist", DictBanksDate.citys);
		
		Map<String,String> bankCodeNameTable = DictBanksDate.bankCodeNameTable;
		
		
		Object[] bankCodeIds = bankCodeNameTable.keySet().toArray();
		List<Object> protocolBankCodeIds = new ArrayList<Object>(); 
		//查找是否支持协议支付
		for (int i = 0; i < bankCodeIds.length; i++) {
			if(AgreementBanks.isAvailable(bankCodeIds[i])) {
				protocolBankCodeIds.add(bankCodeIds[i]);
			}
		}
		//支持协议支付的银行名称
		String[] bankCodeNames = new String[protocolBankCodeIds.size()];//
		for (int i = 0; i < bankCodeNames.length; i++) {
			bankCodeNames[i] = bankCodeNameTable.get(protocolBankCodeIds.get(i));
			
		}
		map.put("bankCodeIds", protocolBankCodeIds.toArray());
		map.put("bankCodeNames", bankCodeNames);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 按省份ID获取城市
	 */
	public static String getCityeList(Map<String, String> parameters) throws IOException {
		Map<String,Object> map =new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String provinceCode = parameters.get("provinceCode");
		Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(Integer.valueOf(provinceCode), error);
		
		Object[] cityIds = cityMaps.keySet().toArray();
		String[] cityNames = new String[cityIds.length];
		for (int i = 0; i < cityNames.length; i++) {
			cityNames[i] = cityMaps.get(cityIds[i]);
		}
		map.put("cityIds", cityIds);
		map.put("cityNames", cityNames);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}

	/**
	 * 添加银行卡
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String addBankcardInfo(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(parameters.get("id")) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		Long bankId = null;
		try {
			bankId = Long.parseLong(parameters.get("bankId"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			Logger.info("新卡");
		}
		
		String addbankCode = parameters.get("addBankCode");
		int addBankCode = Integer.valueOf(parameters.get("addBankCode"));
		String bankName = DictBanksDate.queryBankByCode(addBankCode);
		String account = parameters.get("addAccount");
		String mobile = parameters.get("mobile");
		String addAccountName = parameters.get("addAccountName");		
		String isSign = parameters.get("isSign");
		if(StringUtils.isEmpty(isSign)) { //不传字段默认老APP,不签署协议
			isSign = "0";
		}
		
		ErrorInfo error = new ErrorInfo();
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
        t_users user = t_users.findById(userId);
		// 新增银行卡-数据校验
		if(bankId == null || bankId == 0) {
			
			if(StringUtils.isBlank(addbankCode)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "银行不能为空");
				
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(account)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "银行卡账号有误");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(StringUtils.isBlank(mobile)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "预留手机号不能为空");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(!RegexUtils.isMobileNum(mobile)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "预留手机号格式错误");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			if(StringUtils.isBlank(addAccountName)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "真实姓名不能为空");
				
				return JSONUtils.printObject(jsonMap);
			}
			
			boolean flag= new UserBankAccounts().isReuseBank(0,account,user.finance_type, "");
			
			if(flag){
				
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "该银行账户已存在，请重新输入!");
				
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		

        
        if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
        
        //0 未知 1 个人用户  2 企业用户 3个体工商户
  		if(user.user_type!=1 || user.reality_name==null || "".equals(user.reality_name)){
  			if(user.user_type==2){
  				jsonMap.put("error", "-3");
  				jsonMap.put("msg", "企业用户请到PC官网完成企业认证");
  				return JSONUtils.printObject(jsonMap);
  			}else if(user.user_type==3){
  				jsonMap.put("error", "-3");
  				jsonMap.put("msg", "个体工商户用户请到PC官网完成个体工商户认证");
  				return JSONUtils.printObject(jsonMap);
  			}else if(user.user_type==0){
  				jsonMap.put("error", "-3");
  				jsonMap.put("msg", "用户尚未实名认证");
  				return JSONUtils.printObject(jsonMap);
  			}else if(user.reality_name==null || "".equals(user.reality_name)){
  				jsonMap.put("error", "-3");
  				jsonMap.put("msg", "用户尚未实名认证");
  				return JSONUtils.printObject(jsonMap);
  			}else{
  				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户实体类型有误");
				return JSONUtils.printObject(jsonMap);
  			}
  		}
  		
        String refID = Codec.UUID();
		new AuthReq().create(refID, userId, 2);
		
		String protocolNo = null;
		
		// 适配老版本的四要素验证
		if(!parameters.containsKey("verfyCode") && !parameters.containsKey("verfyToken")){
			//绑卡认证-begin
			
			// 宝付协议绑卡++++++++++++++++++++
			if("1".equals(parameters.get("protoBind")) && AgreementBanks.isAvailable(addBankCode)) { //协议绑卡流程
				
				String uniqueKey = parameters.get("uniqueKey");
				String smscode = parameters.get("smscode");
				String getSmscode = parameters.get("getSms");
				
				if("1".equals(getSmscode)) {
					try {
						// 预绑卡-发送验证码
						uniqueKey = ReadySign.execute(account, mobile, addAccountName, user.id_number, user.id);
						jsonMap.put("error", "-1");
						jsonMap.put("msg", "短信发送成功");
						jsonMap.put("uniqueKey", uniqueKey);
						return JSONUtils.printObject(jsonMap);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						jsonMap.put("error", "-3");
						jsonMap.put("msg", e.getMessage());
						return JSONUtils.printObject(jsonMap);
					}
				}
				
				if(StringUtils.isNotBlank(uniqueKey) && StringUtils.isNotBlank(smscode)) {
					try {
						protocolNo = ConfirmSign.execute(uniqueKey, smscode);
					} catch (Exception e) {
						jsonMap.put("error", "-3");
						jsonMap.put("msg", e.getMessage());
						return JSONUtils.printObject(jsonMap);
					}
				}else {
					jsonMap.put("error", "-33");
					jsonMap.put("msg", "支持协议绑卡, 请先获取短信验证码");
					return JSONUtils.printObject(jsonMap);
				}
			}else {
				
				//绑卡认证
				String res = AuthenticationUtil.requestForBankInfo(addAccountName, user.id_number, account, mobile, error);
				
				if(error.code < 0) {
					jsonMap.put("error", "-3");
					jsonMap.put("msg", error.msg);
					
					//添加错误记录
					UserActions.insertAction(userId, 2,error.msg, error);
					
					return JSONUtils.printObject(jsonMap);
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
						if (!"1".equals((String)tt.get("status"))){// || !bankName.equals((String)tt.get("accountBankName")) fix-180530
							jsonMap.put("error", "-3");
							jsonMap.put("msg", "银行卡信息错误，请检查");
							
							//添加错误记录
							UserActions.insertAction(userId, 2,"银行卡信息错误，请检查", error);
							
							return JSONUtils.printObject(jsonMap);
						}
					}else{
						jsonMap.put("error", "-3");
						jsonMap.put("msg", "暂无法核查该卡数据信息，请更换银行卡");
						
						//添加错误记录
						UserActions.insertAction(userId, 2,"暂无法核查该卡数据信息，请更换银行卡", error);
						
						return JSONUtils.printObject(jsonMap);
					}
				}else{
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "银行卡认证系统出错");
					
					//添加错误记录
					UserActions.insertAction(userId, 2,"银行卡认证系统出错", error);
					
					return JSONUtils.printObject(jsonMap);
				}
			}
			//绑卡认证-end
			
		}
		// 京东金融四要素验证
		else{
			
			String verfyCode = parameters.get("verfyCode");
			String verfyToken = parameters.get("verfyToken");
			
			// ----协议绑卡介入-180425----
			
			/** 判断是否支持协议绑卡 **/
			if(AgreementBanks.isAvailable(addBankCode)) { //协议绑卡流程
				
				// 宝付协议绑卡++++++++++++++++++++
				if(StringUtils.isBlank(verfyCode)){ // 发送验证码
					try {
						// 预绑卡-发送验证码
						String uniqueKey = ReadySign.execute(account, mobile, addAccountName, user.id_number, user.id);
						// 验证码发送成功
						jsonMap.put("error", "-1");
						jsonMap.put("msg", "验证码发送成功");
						jsonMap.put("verfyToken", new Base64().encode(uniqueKey.getBytes()));
						return JSONUtils.printObject(jsonMap);
					} catch (Exception e) {
						jsonMap.put("error", "-3");
						jsonMap.put("msg", e.getMessage());
						return JSONUtils.printObject(jsonMap);
					}
				}else { // 验证验证码
					try {
						verfyToken = new String(new Base64().decode(verfyToken));
						protocolNo = ConfirmSign.execute(verfyToken, verfyCode);
					} catch (Exception e) {
						jsonMap.put("error", "-3");
						jsonMap.put("msg", e.getMessage());
						return JSONUtils.printObject(jsonMap);
					}
				}
			}else { //老的绑卡流程
				
				// 绑卡认证-京东金融+++++++++++++++++++++++++++++++++++++++++++++
				
				com.alibaba.fastjson.JSONObject bankInfo = new com.alibaba.fastjson.JSONObject();
				bankInfo.put("realName", addAccountName);
				bankInfo.put("idNumber", user.id_number);
				bankInfo.put("cardPan", account);
				bankInfo.put("cardType", "D");
				bankInfo.put("bankCode", BankEn.toCodeEnMap().get(addBankCode));
				bankInfo.put("mobile", mobile);
				
				//String verfyCode = parameters.get("verfyCode");
				//String verfyToken = parameters.get("verfyToken");
				
				// 获取token
				if(StringUtils.isBlank(verfyToken)){
					try {
						verfyToken = Authentication.getToken();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						jsonMap.put("error", "-3");
						jsonMap.put("msg", "银行卡认证系统出错");
						
						//添加错误记录
						UserActions.insertAction(userId, 2,"银行卡认证系统出错", error);
						return JSONUtils.printObject(jsonMap);
					}
				}else{
					verfyToken = new String(new Base64().decode(verfyToken));
				}
				
				if(StringUtils.isBlank(verfyCode)){ // 发送验证码
					try {
						com.alibaba.fastjson.JSONObject msg = Authentication.getMsg(verfyToken, bankInfo);
						if(!msg.getString("resultCode").equals("000")){
							jsonMap.put("error", "-3");
							jsonMap.put("msg", msg.getString("resultInfo"));
							return JSONUtils.printObject(jsonMap);
						}else{
							jsonMap.put("error", "-1");
							jsonMap.put("msg", "验证码发送成功");
							jsonMap.put("verfyToken", new Base64().encode(verfyToken.getBytes()));
							return JSONUtils.printObject(jsonMap);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						jsonMap.put("error", "-3");
						jsonMap.put("msg", "银行卡认证系统出错");
						
						//添加错误记录
						UserActions.insertAction(userId, 2,"银行卡认证系统出错", error);
						return JSONUtils.printObject(jsonMap);
					}
				}else{ // 验证验证码
					try {
						com.alibaba.fastjson.JSONObject confirm = Authentication.confirm(verfyToken, verfyCode, bankInfo);
						if(!confirm.getString("resultCode").equals("000")){
							jsonMap.put("error", "-3");
							jsonMap.put("msg", confirm.getString("resultInfo"));
							return JSONUtils.printObject(jsonMap);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						jsonMap.put("error", "-3");
						jsonMap.put("msg", "银行卡认证系统出错");
						
						//添加错误记录
						UserActions.insertAction(userId, 2,"银行卡认证系统出错", error);
						return JSONUtils.printObject(jsonMap);
					}
				}
				
				// 绑卡认证-京东金融+++++++++++++++++++++++++++++++++++++++++++++
			}
		}
			
		
		
		// 绑卡认证-国政通+++++++++++++++++++++++++++++++++++++++++++++姓名，银行卡号，身份证号，手机号码
		/*com.alibaba.fastjson.JSONObject authResult = AuthenticationV2Util.requestForBankInfo(addAccountName, user.id_number, account, mobile);
		if(authResult == null) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡认证系统出错");
			return JSONUtils.printObject(jsonMap);
		}
		if(authResult.getJSONObject("message").getIntValue("status") != 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", authResult.getJSONObject("message").getString("value"));
			return JSONUtils.printObject(jsonMap);
		}else if(authResult.getJSONObject("acctnoInfo").getIntValue("code") != 7){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", authResult.getJSONObject("acctnoInfo").getString("message"));
			return JSONUtils.printObject(jsonMap);
		}*/
		// 绑卡认证-国政通+++++++++++++++++++++++++++++++++++++++++++++
		
		int addProviceCode = Integer.valueOf(parameters.get("addProviceCode"));
		int addCityCode = Integer.valueOf(parameters.get("addCityCode"));
		String addBranchBankName = parameters.get("addBranchBankName");
		String addAccount = parameters.get("addAccount");
		
		
		String provice = DictBanksDate.queryProvinceByCode(addProviceCode);
		String city = DictBanksDate.queryCityByCode(addCityCode);
		
		
		if(bankId != null && bankId != 0 && StringUtils.isNotBlank(protocolNo)) {
			UserBankAccounts.updateBankProtocolNo(bankId, protocolNo, "1".equals(isSign)  ? true : false, error);
			if(error.code < 0 ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);
			}else{
				jsonMap.put("error", "-1");
				jsonMap.put("msg", "绑定成功");
			}
		}else {
			
			UserBankAccounts bankUser =  new UserBankAccounts();
			
			bankUser.userId = userId;
			bankUser.bankName = bankName;
			bankUser.bankCode = addBankCode+"";
			bankUser.provinceCode = addProviceCode;
			bankUser.cityCode = addCityCode;
			bankUser.branchBankName = "支行";
			bankUser.province = provice;
			bankUser.city = city;
			bankUser.account = addAccount;
			bankUser.accountName = addAccountName;
			bankUser.mobile = mobile;
			bankUser.protocolNo = protocolNo;
			bankUser.isSign = "1".equals(isSign)  ? true : false;
			bankUser.addUserBankAccount(error, true);
			if(error.code < 0 ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);
			}else{
				jsonMap.put("error", "-1");
				jsonMap.put("msg", "添加成功");
			}
		}
		
		return JSONUtils.printObject(jsonMap);
	}

	/**
	 * 修改邮箱
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String updateEmail(Map<String, String> parameters) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		if(StringUtils.isBlank(parameters.get("id")) ){
			map.put("error", "-2");
			map.put("msg", "用户id有误");
			
			return JSONUtils.printObject(map);
		}
		String email = parameters.get("email");
		if(StringUtils.isBlank(email) ){
			map.put("error", "-3");
			map.put("msg", "邮箱不能为空");
			
			return JSONUtils.printObject(map);
		}
		
		ErrorInfo error = new ErrorInfo();
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        
        if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(map);
		}
        User.updateEmail(userId, email, error);
        
        if(error.code < 0){
        	map.put("error", "-3");
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
        }
        map.put("error", "-1");
        return JSONUtils.printObject(map);
	}

	/**
	 * 修改手机 
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String updateMobile(Map<String, String> parameters) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		String financeType=parameters.get("financeType");
		if(StringUtils.isBlank(parameters.get("id")) ){
			map.put("error", "-2");
			map.put("msg", "用户id有误");
			
			return JSONUtils.printObject(map);
		}
		String mobile = parameters.get("mobile");
		if(StringUtils.isBlank(mobile) ){
			map.put("error", "-3");
			map.put("msg", "手机不能为空");
			return JSONUtils.printObject(map);
		}
		if (!RegexUtils.isMobileNum(mobile)) {
            
			map.put("error", "-3");
			map.put("msg", "请输入正确的手机号码");
            return JSONUtils.printObject(map);
        }
		
		String smsCode = parameters.get("verifyCode");
		if (StringUtils.isBlank(smsCode)) {
			
			map.put("error", "-3");
			map.put("msg", "请输入短信校验码");
			return JSONUtils.printObject(map);
		}


		if (!StringUtils.isBlank(financeType) && BORROW.equals(financeType)) {
			financeType= BORROW;
		} else {
			financeType= INVEST;
		}
		
		
			String cCode = (String) Cache.get(mobile+"_"+financeType);
			
			if(cCode == null) {
				
				map.put("error", "-3");
				map.put("msg", "短信校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(map);
			}
			
			if(!smsCode.equals(cCode)) {
				
				map.put("error", "-3");
				map.put("msg", "短信校验码错误");
				return JSONUtils.printObject(map);
			}
		
		ErrorInfo error = new ErrorInfo();
        long userId = Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
        
        if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(map);
		}
        User.updateMobile(userId, mobile, error);
        
        if(error.code < 0){
        	map.put("error", "-3");
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
        }
        map.put("error", "-1");
        return JSONUtils.printObject(map);
	}

	/**
	 * 获取平台信息
	 * @return
	 * @throws IOException 
	 */
	public static String getPlatformInfo(Map<String, String> parameters) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		String deviceTypeStr = parameters.get("deviceType");
		
		if(StringUtils.isBlank(deviceTypeStr)) {
			map.put("error", "-3");
			map.put("msg", "请传入设备参数");
			
			return JSONObject.fromObject(map).toString();
		}
		
		if(!NumberUtil.isNumeric(parameters.get("deviceType"))) {
			map.put("error", "-3");
			map.put("msg", "请传入正确的设备参数");
			
			return JSONObject.fromObject(map).toString();
		}
		int deviceType = Integer.parseInt(parameters.get("deviceType"));
		BackstageSet bs = BackstageSet.getCurrentBackstageSet();
		map.put("platformName",bs.platformName);
		map.put("platformTelephone",bs.platformTelephone);
		map.put("version", deviceType == 1 ? bs.androidVersion : bs.iosVersion);
		map.put("code", deviceType == 1 ? bs.androidCode : bs.iosCode);
		map.put("error", "-1");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 论坛搜索
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String postsEarch(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		String content = parameters.get("content");
		String currPage = parameters.get("currPage");
		String pageSize = parameters.get("pageSize");
		//String typeId = parameters.get("typeId");
		if(StringUtils.isBlank(content)) {
			map.put("error", "-3");
			map.put("msg", "请传入搜索内容");
			
			return JSONObject.fromObject(map).toString();
		}
		/*List<t_forum_posts> list = Posts.earchPostsByName(content, 0,currPage,pageSize);
		map.put("list", list);*/
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 论坛首页显示
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String postsFirst(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String postsType = parameters.get("typeId");
		String currPage = parameters.get("currPage");
		String pageSize = parameters.get("pageSize");
		List<t_forum_type> typeList = Posts.getForumTypeList(1);
		
		if(StringUtils.isBlank(postsType)) {
			map.put("error", "-3");
			map.put("msg", "论坛类型错误");
			
			return JSONObject.fromObject(map).toString();
		}
		
		PageBean<v_forum_posts> page = Posts.queryForumPosts(Integer.parseInt(postsType),  null,
				"0", "0", currPage, pageSize, error,0);
		
		map.put("typeList", typeList);
		map.put("postsList", page.page);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 发帖
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appPosts(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userId = parameters.get("userId");
		String typeIdStr = parameters.get("typeId");
		String title = parameters.get("title");
		String content = parameters.get("content");
		List<t_forum_type> typeList = Posts.getForumTypeList(2);
		if(StringUtils.isBlank(userId)) {
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		long id = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if (StringUtils.length(content) > 20000){
			map.put("error", "-3");
			map.put("msg", "对不起,新闻内容字数不能超过20000个字符");
			return JSONUtils.printObject(map);
		}
		
		
		if(StringUtils.isBlank(typeIdStr)) {
		
			map.put("error", "-3");
			map.put("msg", "类别不能为空");
			return JSONUtils.printObject(map);
		}
				
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			
			map.put("error", "-3");
			map.put("msg", "类别类型有误");
			return JSONUtils.printObject(map);
		}
		
		Posts tPosts = new Posts();
		
		
		tPosts.show_time = new Date();
		
		
		tPosts.user_id = id;
		tPosts.type_id = Integer.parseInt(typeIdStr);
		tPosts.show_image = 0;
		tPosts.title = title;
		tPosts.content = content;
		//tPosts.name = "";
		//tPosts.keywords = keywords;
//		news.order = Integer.parseInt(order);
		
		tPosts.addPostsInfo(0, error);
		if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", "发帖失败");
		}else{
			map.put("error", "-1");
			map.put("msg", "发帖成功");
			map.put("typeList", typeList);
		}
		
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 查询用户昵称
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryForumName(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userId = parameters.get("userId");
		if(StringUtils.isBlank(userId)) {
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		long id = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		String name = Posts.queryForumName(id);
		
		map.put("error", "-1");
		map.put("msg", "查询昵称成功");
		map.put("name", name);
	
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 修改用户昵称
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appUpdateName(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String name = parameters.get("name");
		String userId = parameters.get("userId");
		
		if(StringUtils.isBlank(name)) {
			
			map.put("error", "-3");
			map.put("msg", "昵称为空");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(userId)) {
			
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		
		long id = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		Posts.saveForumName(name,id,error);
		if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", "修改昵称失败");
		}else{
			map.put("error", "-1");
			map.put("msg", "修改昵称成功");
		}
		
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 我的帖子
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryUserPosts(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
	
		String userId = parameters.get("userId");
		
		String currPage  = parameters.get("currPage");
		String pageSize = parameters.get("pageSize");
		
		if(StringUtils.isBlank(userId)) {
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		long id = Security.checkSign(userId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		PageBean<v_forum_posts> page = Posts.queryForumPosts(0,  null,
				"0", "0", currPage, pageSize, error,id);
		
		map.put("error", "-1");
		map.put("msg", "修改昵称成功");
		map.put("list", page.page);
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 用户收藏帖子
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String addUserPostsCollection(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String postsIdStr  = parameters.get("postsId");
		String userIdStr  = parameters.get("userId");
		
		if(StringUtils.isBlank(postsIdStr)) {
			map.put("error", "-3");
			map.put("msg", "参数错误");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(userIdStr)) {
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		Posts t = new Posts();
		t.posts_id = Long.parseLong(postsIdStr);
		t.user_id = userId;
		t.saveCollection(error);
		if(error.code < 0){
			map.put("error", error.code);
			map.put("msg", error.msg);
		}else{
			map.put("error", "-1");
			map.put("msg", "修改昵称成功");
		}
		
		
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 删除收藏
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String deletePostscollection(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		
		map.put("error", "-1");
		map.put("msg", "修改昵称成功");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 查询用户收藏列表
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryUserPostsCollection(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr  = parameters.get("userId");
		String currPageStr  = parameters.get("currPageStr");
		String pageSizeStr  = parameters.get("pageSizeStr");
		
		
		if(StringUtils.isBlank(userIdStr)) {
			map.put("error", "-3");
			map.put("msg", "用户为空");
			return JSONUtils.printObject(map);
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		List<v_forum_posts_collection> list = Posts.queryUserPostsCollection(userId, currPageStr, pageSizeStr, error).page;
		
		map.put("error", "-1");
		map.put("msg", "修改昵称成功");
		map.put("list", list);
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 显示帖子内容
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appShowPosts(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String idStr  = parameters.get("id");
		String currPageStr  = parameters.get("currPageStr");
		String pageSizeStr  = parameters.get("pageSizeStr");
		
		
		if(StringUtils.isBlank(idStr)){
			map.put("error", "-3");
			map.put("msg", "参数错误");
			return JSONUtils.printObject(map);
		}
		
		t_forum_posts tPosts= Posts.queryPosts(Long.parseLong(idStr));
		
		List<t_forum_posts_questions> list = Posts.queryUserPostsListById(Long.parseLong(idStr), currPageStr, pageSizeStr, error).page;
		
		//增加流程次数
		Posts.updatePostReadCount(Long.parseLong(idStr));
		map.put("error", "-1");
		map.put("msg", "修改昵称成功");
		map.put("tPosts", tPosts);
		map.put("list", list);
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 用户回帖
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appPostsAnswers(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		String postsIdStr  = parameters.get("postsId");
		String content  = parameters.get("content");
		String userIdStr  = parameters.get("userId");
		String toAnswersIdStr  = parameters.get("toAnswersId");
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(content)){
			map.put("error", "-3");
			map.put("msg","回复内容不能为空");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(postsIdStr)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(userIdStr)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		
		
		Posts p = new Posts();
		
		if(StringUtils.isNotBlank(toAnswersIdStr)){
			long toAnswersId = Security.checkSign(toAnswersIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			User user = new User();
			user.id = toAnswersId;
			p.to_answer_user = user.name;
		}
		p.posts_id = Long.parseLong(postsIdStr);
		p.content = content;
		p.user_id = userId;
		p.savePostsAnswers(error);
		if(error.code < 0){
			map.put("error", "-3");
			map.put("msg", "回帖失败");
		}else{
			map.put("error", "-1");
			map.put("msg", "回帖成功");
		}
		//增加回帖次数
		Posts.updateAppAnswersCount(Long.parseLong(postsIdStr));
		
		return JSONUtils.printObject(map);
	}
	
	
	
	/**
	 * 用户回复用户
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	/*public static String appAnswersPosts(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		String questionId  = parameters.get("questionId");//用户回帖id
		String postsId  = parameters.get("postsId");//帖子id
		String content  = parameters.get("content");
		String userIdStr  = parameters.get("userId");//回复用户人
		String questionUserId  = parameters.get("questionUserId");//回复帖子用户
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(content)){
		
			map.put("error","-3");
			map.put("msg","回复内容不能为空");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(questionId)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(postsId)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(questionUserId)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		if(StringUtils.isBlank(userIdStr)){
			map.put("error","-3");
			map.put("msg","参数错误");
			return JSONUtils.printObject(map);
		}
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		User user = new User();
		user.id = Long.parseLong(questionUserId);
		
		Posts p = new Posts();
		p.question_id = Long.parseLong(questionId);
		p.content = content;
		p.type = 0;
		p.user_id = userId;
		p.posts_id = Long.parseLong(postsId);
		p.question_user_name = user.name;
		p.saveAdminAnswers(error);
		
		map.put("error", "-1");
		map.put("msg", "回帖成功");
		return JSONUtils.printObject(map);
	}*/

	/**
	 * 根据状态查询我的红包
	 * @param parameters
	 * @return
	 */
	public static String getRedPackageList(Map<String, String> parameters)  throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		String status = parameters.get("redStatus");
		if(StringUtils.isBlank(status)) {
			map.put("error", "-3");
			map.put("msg", "请传入红包状态");
			return JSONObject.fromObject(map).toString();
		}
		
		if(StringUtils.isBlank(parameters.get("user_id")) ){
			map.put("error", "-2");
			map.put("msg", "用户id有误");
			
			return JSONObject.fromObject(map).toString();
		}
		ErrorInfo error = new ErrorInfo();
        long userId = Security.checkSign(parameters.get("user_id"), Constants.USER_ID_SIGN, Constants.VALID_TIME,error );
		
		return JSONUtils.printObject(map);
		 
	}
	
	
	/**
	 * 充值页面显示
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String showRecharge(Map<String, String> parameters) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		String userIdStr  = parameters.get("userId");
		
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(userIdStr)){
			map.put("error", "-3");
			map.put("msg", "参数错误");
			return JSONUtils.printObject(map);
		}
		
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0){
			map.put("error", "-2");
			map.put("msg", "用户id有误");
			
			return JSONUtils.printObject(map);
		}
		
		t_users user = t_users.findById(userId);
		/*
		//0 未知 1 个人用户  2 企业用户 3个体工商户
		if(user.user_type!=1 || user.reality_name==null || "".equals(user.reality_name)){
  			if(user.user_type==2){
  				map.put("error", "-3");
  				map.put("msg", "企业用户请到PC官网充值");
  				return JSONUtils.printObject(map);
  			}else if(user.user_type==3){
  				map.put("error", "-3");
  				map.put("msg", "个体工商户用户请到PC官网充值");
  				return JSONUtils.printObject(map);
  			}else if(user.user_type==0){
  				map.put("error", "-3");
  				map.put("msg", "用户尚未实名认证");
  				return JSONUtils.printObject(map);
  			}else if(user.reality_name==null || "".equals(user.reality_name)){
  				map.put("error", "-3");
  				map.put("msg", "用户尚未实名认证");
  				return JSONUtils.printObject(map);
  			}else{
  				map.put("error", "-3");
  				map.put("msg", "用户实体类型有误");
				return JSONUtils.printObject(map);
  			}
  		}
		*/
		Map<String,String> mapb  = new HashMap<String, String>();
		
		mapb.put("102", "ICBC");
		mapb.put("103", "ABC");
		mapb.put("105", "CCB");
		mapb.put("104", "BOC");
		mapb.put("301", "BCOM");
		mapb.put("309", "CIB");
		mapb.put("302", "CITIC");
		mapb.put("303", "CEB");
		mapb.put("307", "PAB");
		mapb.put("403", "PSBC");
		mapb.put("401", "SHB");
		mapb.put("310", "SPDB");
		mapb.put("308", "CMB");
		
		mapb.put("305", "CMBC");
		mapb.put("306", "CDB");
		mapb.put("304", "HXB");
		mapb.put("404", "BOB");//北京银行
		//查询用户绑定的银行卡，只绑定一张
		t_user_bank_accounts bank = UserBankAccounts.queryById(userId);
		
		int bankType=0;
		
		if(bank != null) {
			if(user.user_type!=1 && bank.bank_code==null ){
				bank.bank_code="999";
			}else{
				bank.bank_code = AgreementBanks.getBankCode(bank.bank_code);
				for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
					if((Constants.BAOFU_TYPE[i]).equals(bank.bank_code+"")) {
						bankType = i;
						break;
					}
				}
			}
			
		}
		
		// 多张银行实现-----start
		List<t_user_bank_accounts> banks = UserBankAccounts.queryMoreById(userId);
		if(banks != null) {
			for(t_user_bank_accounts bank_account : banks) {
				
				if(user.user_type!=1 && ( bank_account.bank_code==null || bank_account.bank_code.equals("999")) ){
					bank_account.bank_code="999";
				}else{
				
					// 是否支持协议支付
					// 是否开通协议支付与是否支持协议支付无关
					if(AgreementBanks.isAvailable(bank_account.bank_code) /*&& StringUtils.isNotBlank(bank_account.protocol_no)*/) {
						bank_account.isProtocol = 1;
					}
					bank_account.bank_code = AgreementBanks.getBankCode(bank_account.bank_code);
					for (int i = 0; i < Constants.BAOFU_TYPE.length; i++) {
						if((Constants.BAOFU_TYPE[i]).equals(bank_account.bank_code)) {
							bank_account.bankType = i;
							break;
						}
					}
				}
			}
		}
		map.put("banks", banks);
		// 多张银行实现-----end
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int rechargeLowest = backstageSet.rechargeLowest; //最低充值金额
		int rechargeHighest = backstageSet.rechargeHighest; //最高充值金额
		
		
		map.put("idNumber", user.id_number);
		map.put("bank", bank);
		map.put("bankType", bankType);
		map.put("rechargeLowest", rechargeLowest);
		map.put("rechargeHighest", rechargeHighest);
		map.put("error", "-1");
		map.put("msg", "查询成功");
		
		return JSONUtils.printObject(map);
	}
	
	/**
     * 充值
     * @return 
     * @throws IOException
     */
	public static String recharge(Map<String, String> parameters) throws IOException{
		
		ErrorInfo error = new ErrorInfo();
		
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		User user = new User();
		
		int bankType = 0;
		String bankTypeStr = parameters.get("bankType");
		
		String card_no  =  parameters.get("card_no");
		
		
		//String rechargeType = parameters.get("rechargeType");
		
		if(StringUtils.isNotBlank(bankTypeStr)){
			try {
				bankType =  Integer.valueOf(bankTypeStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡账号参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		if(StringUtils.isBlank(card_no)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "银行卡账号参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		/*if (StringUtils.isBlank(rechargeType)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "充值类型参数有误");
			
			return JSONUtils.printObject(jsonMap);
		} */
		
		
		long userId =  Security.checkSign(parameters.get("id"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		user.id = userId;
		if (user.id < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "该用户不存在");
			return JSONUtils.printObject(jsonMap);
		}
		
		String amount = parameters.get("amount");
		if (StringUtils.isBlank(amount)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "充值金额参数有误");
			
			return JSONUtils.printObject(jsonMap);
		}
		
		BigDecimal moneyDecimal = new BigDecimal(amount);
		
		if(moneyDecimal.compareTo(new BigDecimal("0.01")) < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的充值金额");
			return JSONUtils.printObject(jsonMap);
		}
		
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
        map.put("401", "SHB");
        map.put("310", "SPDB");
        map.put("308", "CMB");
        
        map.put("305", "CMBC");
        map.put("306", "CDB");
        map.put("304", "HXB");
        map.put("404", "BOB");//北京银行
		//查询用户绑定的银行卡，只绑定一张
		t_user_bank_accounts bank = UserBankAccounts.queryById(User.currUser().id);
		
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
		
		// Map<String, String> args = User.baofuPay(moneyDecimal,bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_PC,card_no, error);
		 if(error.code < 0) {
			 jsonMap.put("error", "-3");
			 jsonMap.put("msg", "宝付请求错误");
			 return JSONUtils.printObject(jsonMap);
		  }
		 
		 
		 
		
//		if (Integer.valueOf(amount) > Constants.MAX_VALUE) {
//			jsonMap.put("error", "-3");
//			jsonMap.put("msg", "充值金额范围需在[" + backstageSet.rechargeLowest + "~" + Constants.MAX_VALUE + "]之间");
//			return JSONUtils.printObject(jsonMap);
//		}
		
	
		
		
		
		/*if(StringUtils.equals(rechargeType, "1")) {
			
			Map<String, String> args = User.gpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_APP, Constants.CLIENT_APP, error);
			
			if(error.code != 0) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);
			}
			
			//充值送红包
			String redTypeName = Constants.RED_PACKAGE_TYPE_RECHARGE;//红包类型
			
			long status  = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态
			
			t_red_packages_type redPackageType = RedPackage.isExist(redTypeName, status);//红包类型是否存在
			if(null != redPackageType && redPackageType.validity_money <= moneyDecimal.doubleValue()){
				String desc = "APP充值发放红包";
				RedPackageHistory.sendRedPackage(user, redPackageType,desc);
				Logger.error("APP充值发放红包短信通知成功");
			}else{
				Logger.error("APP充值发放红包发放失败!造成原因数据库中不存在充值类型红包或者未达到条件");
			}
			
			
			return JSONUtils.printObject(jsonMap);
			
		}else if(StringUtils.equals(rechargeType, "3")){*/
		    
		   /* if(StringUtils.isBlank(rechargeType)){
		    	jsonMap.put("error", "-3");
				jsonMap.put("msg", "您选择的是连连支付，请选择网银或者认证支付方式！");
		    	return JSONUtils.printObject(jsonMap);
			
		    }
		    
		    		    
		    int llRecharge = Integer.parseInt(rechargeType);
		    
		    if(llRecharge == 1){
		    	//认证支付需要手机号（风控参数）
			    User users = User.currUser();
			    user.id = user.id;
			    
			    if(!user.isMobileVerified){
			    	jsonMap.put("error", "-3");
					jsonMap.put("msg", "认证支付需要绑定手机，你未绑定手机！");
			    	return JSONUtils.printObject(jsonMap);
					
			    }
				if(StringUtils.isBlank(card_no)){
				    jsonMap.put("error", "-3");
					jsonMap.put("msg", "您选择的是连连认证支付，请填写银行卡号！！！");
			    	return JSONUtils.printObject(jsonMap);
				}
		    }
		    
		    Map<String, String> args = User.LLpay(moneyDecimal, bankType, RechargeType.Normal, Constants.RECHARGE_PC, Constants.CLIENT_PC,llRecharge,card_no, error);
		    
		    if(error.code != 0) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);
			    return JSONUtils.printObject(jsonMap);
		    }*/
		    //连连网银支付
//		    if(llRecharge == 0){
//		    	render("@front.account.FundsManage.submitLLWebPayRecharge",args);
//		    }else{
//		    	//连连认证支付
//		    	render("@front.account.FundsManage.submitLLAuthPayRecharge",args);
//		    }
		
		
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 充值前信息
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String rechargeInfo(Map<String,String> parameters) throws IOException{
		  ErrorInfo error = new ErrorInfo();

	        // Map<String, Object> jsonMap = new HashMap<String, Object>();
	        BigDecimal moneyDecimal = new BigDecimal(parameters.get("money")).setScale(2, RoundingMode.DOWN);
	        JSONObject json = new JSONObject();

			//关闭充值业务10.28
			if(true) {
				json.put("error", "-155");
				json.put("msg", "充值错误，请联系客服。");
				return JSONUtils.printObject(json);
			}


			User user = new User();

	        if (StringUtils.isBlank(parameters.get("id"))) {
	            json.put("error", "-2");
	            json.put("msg", "请求用户id参数有误");

	            return JSONUtils.printObject(json);
	        }

	        long userId = Security.checkSign(parameters.get("id"),Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
	        User.setCurrUser(userId);
	        if (error.code < 0) {
	            json.put("error", "-2");
	            json.put("msg", error.msg);

	            return JSONUtils.printObject(json);
	        }

	        user.id = userId;
	        String userName="";
	        
	        if (user.id < 0) {
	            json.put("error", "-3");
	            json.put("msg", "该用户不存在");
	            return JSONUtils.printObject(json);
	        }else{
	            userName=user.name;
	        
	        }
	        
			//0 未知 1 个人用户  2 企业用户 3个体工商户
			if(user.user_type!=1 || user.realityName==null || "".equals(user.realityName)){
	  			if(user.user_type==2){
	  				json.put("error", "-3");
	  				json.put("msg", "企业用户请到PC官网充值");
	  				return JSONUtils.printObject(json);
	  			}else if(user.user_type==3){
	  				json.put("error", "-3");
	  				json.put("msg", "个体工商户用户请到PC官网充值");
	  				return JSONUtils.printObject(json);
	  			}else if(user.user_type==0){
	  				json.put("error", "-3");
	  				json.put("msg", "用户尚未实名认证");
	  				return JSONUtils.printObject(json);
	  			}else if(user.realityName==null || "".equals(user.realityName)){
	  				json.put("error", "-3");
	  				json.put("msg", "用户尚未实名认证");
	  				return JSONUtils.printObject(json);
	  			}else{
	  				json.put("error", "-3");
	  				json.put("msg", "用户实体类型有误");
					return JSONUtils.printObject(json);
	  			}
	  		}
	        String dealpwd =Encrypt.decrypt3DES(parameters.get("dealpwd"), Constants.ENCRYPTION_KEY);
	        if (!user.payPassword.equals(Encrypt.MD5(dealpwd+Constants.ENCRYPTION_KEY))) {
	            json.put("error", "-3");
	            json.put("msg", "支付密码错误！");
	            return JSONUtils.printObject(json);
	        }
	        
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
	       // map.put("322", "SHB");
	        map.put("310", "SPDB");
	        map.put("305", "CMBC");
	        map.put("308", "CMB");
	        map.put("306", "GDB");
	        map.put("401", "SHB");
	        map.put("304", "HXB");
	        map.put("404", "BOB");//北京银行
	        
	        List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
	        String pay_code="";//银行编码
	        if(userBanks.get(0).verified){
	        	 pay_code=userBanks.get(0).bankCode+"";//银行编码
	        }else{
        		 pay_code=map.get(userBanks.get(0).bankCode+"");//银行编码
	        }
	        
	        String realityName=user.realityName;//姓名
	        String bank_card=userBanks.get(0).account;//银行卡号
	        String mobile=userBanks.get(0).mobile;//电话号码
	        String id_card=user.idNumber;//身份证号
	        String protocolNo = userBanks.get(0).protocolNo;
	        
	        // 多张银行卡实现--180412
	        if(StringUtils.isNotBlank(parameters.get("bankId"))) {
		        	List<t_user_bank_accounts> banks = UserBankAccounts.queryMoreById(userId);
		    		if(banks != null) {
		    			for(t_user_bank_accounts bank_account : banks) {
		    				if(parameters.get("bankId").equals(bank_account.id+"")) {
		    					if(bank_account.verified){
		    			        	 	pay_code = bank_account.bank_code+"";//银行编码
		    			        }else{
		    		        		 	pay_code = map.get(bank_account.bank_code+"");//银行编码
		    			        }
		    			        
		    			        bank_card = bank_account.account;//银行卡号
		    			        mobile = bank_account.mobile;//电话号码
		    			        protocolNo = bank_account.protocol_no;
		    					break;
		    				}
		    			}
		    		}
	        }

	        /** 协议支付介入 **/
			if("1".equals(parameters.get("protoPay")) && AgreementBanks.isAvailable(pay_code)) {

				String uniqueKey = parameters.get("uniqueKey");

				// 去绑卡
				if(StringUtils.isBlank(protocolNo)) {
					json.put("error", "-33");
		            json.put("msg", "需要协议绑卡");
		            return JSONUtils.printObject(json);
				}else { //去支付
					try {
						String smscode = parameters.get("smscode");
						if(StringUtils.isBlank(uniqueKey) || StringUtils.isBlank(smscode)) {
							json.put("error", "-34");
				            json.put("msg", "请先获取短信验证码");
				            return JSONUtils.printObject(json);
						}

						t_user_recharge_details recharge_detail = t_user_recharge_details.find(" unique_key = ? ", uniqueKey).first();
						if(recharge_detail!=null){
							moneyDecimal = new BigDecimal(recharge_detail.amount);
						}else{
							json.put("msg","充值参数有误");
							json.put("error", "-3");
							return JSONUtils.printObject(json);
						}
						// 协议支付处理
						Map<String, String> payResult = ConfirmPay.execute(uniqueKey, smscode);
						if(payResult.get("resp_code").toString().equals("S")){

							 //认证充值前操作
//							 User.sequence(user.id,-4, payResult.get("trans_id"), moneyDecimal.doubleValue(), Constants.GATEWAY_RECHARGE, Constants.CLIENT_APP, bank_card,error);

							 recharge_detail.pay_number = payResult.get("trans_id");
							 recharge_detail.bank_card_no = bank_card;
							 recharge_detail.save();

							moneyDecimal = new BigDecimal(payResult.get("succ_amt")).divide(new BigDecimal("100"));

							Log.Write("支付成功！[trans_id:"+payResult.get("trans_id")+"]");

							 User.recharge(payResult.get("trans_id"), moneyDecimal.doubleValue(), error);
							 if (error.code < 0) { 
								 json.put("error", "-3");
								 json.put("msg", "数据错误;请联系客服人员");
								 return JSONUtils.printObject(json);
							 }
							 json.put("error", 30);
						     json.put("retMsg", "充值成功");
						     json.put("msg", "充值成功");
						     return JSONUtils.printObject(json);
						        
						}else if(payResult.get("resp_code").toString().equals("I")){	
							Log.Write("处理中！");
							 //认证充值前操作
//							 User.sequence(user.id,-4, payResult.get("trans_id"), moneyDecimal.doubleValue(), Constants.GATEWAY_RECHARGE, Constants.CLIENT_APP, bank_card,error);

							recharge_detail.pay_number = payResult.get("trans_id");
							recharge_detail.bank_card_no = bank_card;
							recharge_detail.save();

							 json.put("error", "-35");
					         json.put("msg", payResult.get("biz_resp_msg").toString());
					         return JSONUtils.printObject(json);
						}else {
							json.put("error", "-3");
							json.put("msg", "第三方未知错误;请联系客服人员");
							return JSONUtils.printObject(json);
						}
					} catch (Exception e) {
						json.put("error", "-3");
						json.put("msg", e.getMessage());
						return JSONUtils.printObject(json);
					}
				}
			}
	        
	        Map<String, String> args = User.bfAppPay(userId,moneyDecimal, pay_code,realityName,userName,mobile,bank_card,id_card,RechargeType.Normal, true, error);
	        
	        String strJson = WS.url(Constants.BF_APP_URL).setParameters(args).post().getString();
	        Logger.info("app发送宝付参数[%s]和请求地址[%s]",args.toString(),Constants.BF_APP_URL);
	        if (strJson == null) {
	            error.code = -4;
	            error.msg = "获取的返回参数有误!";

	            json.put("error", error.code);
	            json.put("msg", error.msg);
	            return JSONUtils.printObject(json);
	        }
	        Logger.info("返回的strJson结果是:\n%s", strJson);
	        
	        String[] resp = strJson.split(",");

	        String[] retCode1 = resp[0].split(":");
	        String retCode2= retCode1[1];
	        String retCode=retCode2.substring(1, (retCode2.length()-1));
	        
	        String[] retMsg1 = resp[1].split(":");
	        String retMsg2= retMsg1[1];
	        String retMsg=retMsg2.substring(1, (retMsg2.length()-1));
	        
	        if(resp.length>2){
	            String[] tradeNo1 = resp[2].split(":");
	            String tradeNo2 = tradeNo1[1];
	            String tradeNo=tradeNo2.substring(1, (tradeNo2.length()-2));
	            json.put("tradeNo", tradeNo);
	            
	        }
	            json.put("retCode", retCode);
	            Logger.info("retMsg:%s", retMsg);
	        if(retMsg=="" && !retCode.equals("0000")){
	            error.code = -4;
	            error.msg = "传入信息有误，请仔细查看您填的信息是否正确";
	            json.put("error", error.code);
	            json.put("retMsg", retMsg);
	            json.put("msg", error.msg);
	            return JSONUtils.printObject(json);
	        }
	        
	        if (!retCode.equals("0000")) {
	            error.code = -5;
//	            error.msg = "不能进行继续充值";
	            json.put("error", error.code);
	            json.put("retMsg", error.msg);
	            json.put("msg", retMsg);
	            return JSONUtils.printObject(json);
	        }
	        
	        json.put("error", -1);
	        json.put("retMsg", "充值成功");
	        json.put("msg", "充值成功");
	        
	        return JSONUtils.printObject(json);
	        //return "";
	}
	
	
	/**
	 * 查询借款人详细
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appBidUserInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String borrowIdStr = parameters.get("borrowId");
		
		if(StringUtils.isBlank(borrowIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
		    return JSONUtils.printObject(jsonMap);
		}
		
		
		long bidId = Long.parseLong(borrowIdStr);
		
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = Constants.BID_SHZ;
		bid.id = bidId;
		
		t_bids t = t_bids.findById(bidId);
		
		
		String userIdStr = parameters.get("userId");
		
		
		long userId = 0;
		int type = 0;
		if(StringUtils.isBlank(userIdStr)){
			type = 2;
		}else{
			userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			type= t_invests.find(" user_id = ? and bid_id = ? ", userId,bidId).fetch().size() == 0 ? 2 : 1 ;
		}
		
		List<String> list = new ArrayList<String>();
		/*List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bid.userId, bid.mark); // 用户正对产品上传的资料集合
		if(uItems != null && uItems.size() > 0){
			for(UserAuditItem uItem : uItems){
				if(uItem.isVisible && (uItem.status == 2 || uItem.status == 3)){
					//查询是否投资
					UserAuditItem item = new UserAuditItem();
					item.lazy = true;
					item.userId = bid.userId;
					item.mark = uItem.mark;
					if(item.auditItem.type == 1){
						list.addAll(item.getItems());
					}
				}
			}
		}*/
		
		List<v_user_audit_items> items = UserAuditItem.queryUserAuditItem(bid.userId, bid.mark, error);
		
		List<BidImages> bidImageList=null;
		try {
			bidImageList=BidImages.getBidImagesByBidId(bidId);
		} catch (Exception e) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "获取标的图片失败");
			e.printStackTrace();
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		if(bidImageList!=null && bidImageList.size()>0){//新的标的图片表BidImages
			for(BidImages bidImage:bidImageList){
				list.add(bidImage.bid_image_url);
			}
		}else if(items != null && items.size() > 0){
			for(v_user_audit_items audit_item : items){
				if(audit_item.is_visible && (audit_item.status == 2 || audit_item.status == 3)){
					//查询是否投资
					UserAuditItem item = new UserAuditItem();
					item.lazy = true;
					item.userId = bid.userId;
					item.mark = audit_item.mark;
					if(item.auditItem.type == 1){
						list.addAll(item.getItems());
					}
				}
			}
		}
		
		
		//List<String> list = item.getItems();//.getUserAppItems(t.user_id,type);
	/*	String userAddress = StringUtils.isNotBlank(bid.user.provinceName) ? bid.user.provinceName : "";
		userAddress += StringUtils.isNotBlank(bid.user.cityName) ? bid.user.cityName : "";*/
		t_user_city userCity = t_user_city.find(" user_id = ? " , t.user_id).first() ;
		if(userCity!=null){
			String userAddress =(userCity.province==null?"":userCity.province) + (userCity.city==null?"":userCity.city);
			jsonMap.put("userAddress", userAddress);
		}else{
			jsonMap.put("userAddress", "");
		}
		jsonMap.put("description", bid.description);
		jsonMap.put("imgList", list);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 投标红包显示
	 * @param parameters
	 * @return
	 * @throws IOException
	 * 2018-09-17,apiversion=1.1
	 * 之前app调接口没有传apiversion
	 * if(apiversion==null){apiversion=1.0}
	 * if(apiversion<=1.0){仅老红包}else{所有红包}
	 */
	public static String getRedList(Map<String, String> parameters) throws IOException{
		String userIdStr = parameters.get("userId");
		String redStatusStr = parameters.get("redStatus");
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(redStatusStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		String apiversion=parameters.get("apiversion");
		if(StringUtils.isBlank(apiversion)){
			apiversion="1";
		}
		if(!NumberUtil.isNumericDouble(apiversion)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","版本号有误");
			return JSONUtils.printObject(jsonMap);
		}
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		BigDecimal apiversionBd=new BigDecimal(apiversion);
		List<t_red_packages_history> tRed = new ArrayList<t_red_packages_history>();
		if(apiversionBd.compareTo(new BigDecimal("1.1"))>=0){
			tRed = RedPackageHistory.showListByStatus(userId,Integer.parseInt(redStatusStr),-1,1);
		}else{
			tRed = RedPackageHistory.showListByStatus(userId,Integer.parseInt(redStatusStr),1,1);//仅老红包
		}
		
		jsonMap.put("list", tRed);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 还款计算
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appRepaymentCalculate(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<Map<String, Object>> payList = null;
		String amount = parameters.get("amount");
		String apr = parameters.get("apr");
		String period = parameters.get("period");
		String periodUnit = parameters.get("periodUnit");
		String repaymentType = parameters.get("paymentType");
		String increaseRate = parameters.get("increaseRate");//加息利息
		if(StringUtils.isBlank(increaseRate)){
			increaseRate = "0";
		}
		
		if(StringUtils.isBlank(amount)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "金额错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(apr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "利率错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(period)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "日期错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(periodUnit)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "日期错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(repaymentType)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款方式错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		payList = Bill.repaymentCalculate(Double.parseDouble(amount), Double.parseDouble(apr), Integer.parseInt(period), 
				Integer.parseInt(periodUnit), Integer.parseInt(repaymentType),Double.parseDouble(increaseRate));
		
		BigDecimal profit = BigDecimal.ZERO;
		BigDecimal income = BigDecimal.ZERO;
		
		for(Map<String, Object> vo : payList){
			
			profit = profit.add(new BigDecimal(Double.parseDouble(vo.get("monPayInterest").toString())));
			income = income.add(new BigDecimal(Double.parseDouble(vo.get("monPayIncreaseInterest").toString())));
		}
		
		jsonMap.put("list", payList);
		jsonMap.put("profit", profit.doubleValue());
		jsonMap.put("income", income.doubleValue());
		jsonMap.put("isIncreaseRate", Double.parseDouble(increaseRate) > 0 ? 1 : 0);
		jsonMap.put("score", Score.investScore(profit.add(income).doubleValue()));
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户中心
	 * @param parameters
	 * @return
	 * @throws IOException
	 * 2018-09-17,apiversion=1.1
	 * 之前app调接口没有传apiversion
	 * if(apiversion==null){apiversion=1.0}
	 * if(apiversion<=1.0){仅老红包}else{所有红包}
	 */
	public static String appUserHome(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("userId");
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		String apiversion=parameters.get("apiversion");
		if(StringUtils.isBlank(apiversion)){
			apiversion="1";
		}
		if(!NumberUtil.isNumericDouble(apiversion)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg","版本号有误");
			return JSONUtils.printObject(jsonMap);
		}
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		BigDecimal apiversionBd=new BigDecimal(apiversion);
		List<t_red_packages_history> tRed = new ArrayList<t_red_packages_history>();
		if(apiversionBd.compareTo(new BigDecimal("1.1"))>=0){
			tRed = RedPackageHistory.showListByStatus(userId,Constants.RED_PACKAGE_STATUS_UNUSED_INT,-1,1);
		}else{
			tRed = RedPackageHistory.showListByStatus(userId,Constants.RED_PACKAGE_STATUS_UNUSED_INT,1,1);//仅老红包
		}
		
		UserOZ accountInfo = new UserOZ(userId);
		
		double profit = Invest.getProfit(userId);
		
		int totalScore = MallScroeRecord.currentMyScroe(userId);
		//可用红包个数
		jsonMap.put("redPackageSize",tRed.size());
		//账户总额 (总额  + 未到账本金)
		jsonMap.put("userAccount",accountInfo.user_account + accountInfo.receive_corpus);
		//应收金额
//		String userReceiveAmount = Invest.getCurrentMonthProfit(userId);
		//待收(只显示待收本金，利息不显示)
		jsonMap.put("userReceiveAmount",accountInfo.receive_corpus);
		//可用余额
		jsonMap.put("userAccountY", new BigDecimal(accountInfo.user_account).subtract(new BigDecimal(accountInfo.freeze)).doubleValue());
		//冻结金额（ 已用）
		jsonMap.put("freeze", accountInfo.freeze);
		//用户收益
		jsonMap.put("profit", profit);
		//积分
		jsonMap.put("totalScore", totalScore);
		//实名认证积分
		jsonMap.put("authScore", Score.authScore());
		//是否已实名
		jsonMap.put("isAuth", Score.isAuth(userId,null));
		//绑定银行卡积分
		jsonMap.put("bankScore", Score.bankScore());
		//是否已绑卡// fix:是否绑过卡-180327-llj
		jsonMap.put("isBank", Score.isBank(userId,null));
		
		// 当前是否有卡
		jsonMap.put("hasBank", Score.hasBank(userId));
		
		//推广所得金额
		//double totalCpsIncome = User.queryTotalCpsIncome(userId);
		double money = 0;
		Object moneyObject = t_users.find("select sum(cps_reward) from t_user_cps_profit where recommend_user_id = ? ", userId).first();
		moneyObject = moneyObject == null ? 0 : moneyObject;
		money = Double.valueOf(moneyObject.toString());
		
		jsonMap.put("cpsCount", money);
		v_bill_loan_v1 loan = v_bill_loan_v1.find(" user_id = ? ", userId).first(); //还款中的状态
	 
		jsonMap.put("isLoan", loan != null ? 1 : 0);
		// 用户的银行卡保险购买记录
//		jsonMap.put("bankInsur", User.findUserBankInsur(userId));
        jsonMap.put("bankInsur", 0);
		
		User user = new User();
		user.id = userId;
		jsonMap.put("risk_result",user.risk_result == null ? "" : user.risk_result);
		
		//String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, error);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("user_type",user.user_type );  //0 未知 1 个人用户  2 企业用户 3个体工商户
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户兑换信息
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appExchangeInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("userId");
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		//查询兑换信息
		List<Map<String, Object>> list = MallGoods.queryAppExchangedInfo(userId, error);
		
		jsonMap.put("error", "-1");
		jsonMap.put("list", list);
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * cps个人信息查询
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String getUserCpsInfo(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("userId");
		
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		long userId =  Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(userId < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		//推广的有效会员
		v_user_cps_user_count cpsCount = User.queryCpsCount(userId, error);
		//推广所得金额
		//double totalCpsIncome = User.queryTotalCpsIncome(userId);
		double totalCpsIncome = 0;
		Object cps_reward_ed = t_user_details.find("select sum(amount) from t_user_details where user_id = ? and operation = ?", userId, DealType.CPS_RATE_COUNT).first();
		cps_reward_ed = cps_reward_ed == null ? 0 : cps_reward_ed;
		totalCpsIncome = Double.valueOf(cps_reward_ed.toString());
		
		double money = 0;
		Object moneyObject = t_users.find("select sum(cps_reward) from t_user_cps_profit where recommend_user_id = ? ", userId).first();
		moneyObject = moneyObject == null ? 0 : moneyObject;
		money = Double.valueOf(moneyObject.toString());
		
		Logger.info("用户userId:"+userId+", 金额："+totalCpsIncome);
		jsonMap.put("money", money);
		jsonMap.put("cpsCount", cpsCount.cps_count);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
		
	}
	
	/**
	 * cps奖励金额查询
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryCpsReward(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		jsonMap.put("rewardForCounts", backstageSet.rewardForCounts);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 查询积分商城的地址列表选项
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String getMallProvinceList(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		//ErrorInfo error = new ErrorInfo();
		
		List<t_dict_ad_citys> citylist =  t_dict_ad_citys.findAll();
		
		List<t_dict_ad_provinces> provinceList = t_dict_ad_provinces.findAll();
		
		jsonMap.put("provinceList", provinceList);
		
		jsonMap.put("citylist", citylist);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "保存成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户增加/编辑收货地址
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appUserSaveAddress(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		
		//收货人姓名
		String receiver = parameters.get("receiver");
		//电话
		String tel = parameters.get("tel");
		//地址
		String where = parameters.get("address");
		//省份id
		String province_id = parameters.get("provinceId");
		//城市id
		String city_id = parameters.get("cityId");
		//是否默认
		String isDefault = parameters.get("is_default");
		//用户id
		String user_id = parameters.get("userId");
	
		
		boolean is_default = false;
		if (isDefault != null && "1".equals(isDefault)) {
			is_default = true;
		}
		
		long userId = Security.checkSign(user_id, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		
		t_mall_address address = new t_mall_address();
		
		address.user_id = userId;
		address.time = new Date();
		address.receiver = receiver;
		address.tel = tel;
		address.address = where;
		address.province_id = Integer.parseInt(province_id);
		address.city_id = Integer.parseInt(city_id);
		address.is_default = is_default;
		if (is_default) {
			MallAddress.updateMallDefaultStatue(userId);
		}
		//id为空新增地址
		String id = parameters.get("id");
		if(StringUtils.isBlank(id)){
			MallAddress.saveAddress(address);
			jsonMap.put("msg", "新增成功");
		}else{
			address.id = Long.parseLong(id);
			MallAddress.eidtUserAddress(address);
			jsonMap.put("msg", "编辑成功");
		}
		
		
		jsonMap.put("error", "-1");
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 查询用户收货地址
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String queryUseraddress(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String id = parameters.get("userId");
		long userId = Security.checkSign(id, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		List<t_mall_address> list = MallAddress.queryAddressByList(userId, error);
		jsonMap.put("list", list);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	
	/**
	 * 删除用户地址
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String deleteUserAddress(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		//id
		String idStr = parameters.get("id");
		String userIdStr = parameters.get("userId");
		
		
		if(StringUtils.isBlank(idStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		//判断用户是否过期
		Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		MallAddress.deleteUserAddress(Long.parseLong(idStr));
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "删除成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户标记全读
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String userReadAll(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("userId");
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		//判断用户是否过期
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		List<Object> list = StationLetter.getMessageList(userId);
		for (int a = 0;a < list.size();a++) {
			//long id = Long.parseLong(str);
			StationLetter.markUserMsgReaded(userId, Long.parseLong(list.get(a).toString()), error);
		}
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标记失败");
		}else{
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "标记成功");
		}
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户反馈意见
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appUserSendMsg(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = parameters.get("userId");
		String title = parameters.get("title");
		String content = parameters.get("content");
		if(StringUtils.isBlank(userIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(title)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "标题信息有误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(content)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "内容不能为空");
			return JSONUtils.printObject(jsonMap);
		}
		
		//判断用户是否过期
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		StationLetter msg = new StationLetter();
		msg.senderUserId = userId;
		msg.receiverSupervisorId = SystemSupervisor.ID;
		msg.title = title;
		msg.content = content;
		msg.sendToSupervisorByUser(error,userId);
		if(error.code < 0){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
		}else{
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "反馈成功");
		}
		
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 查询投资信息
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appInvestBillDetails(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String billId = parameters.get("billId");
		if(StringUtils.isBlank(billId)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "参数错误");
			return JSONUtils.printObject(jsonMap);
		}
		long id = Security.checkSign(billId, Constants.BILL_ID_SIGN, 3600, error);
		String userIdStr = parameters.get("userId");
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
	
		v_bill_invest_detail investDetail = BillInvests.queryMyInvestBillDetails(id, userId, error);
		t_bill_invests tBill = t_bill_invests.findById(id);
		t_bids tBids = t_bids.findById(tBill.bid_id);
		
		jsonMap.put("receiveInterest", tBill.receive_interest);
		jsonMap.put("period_unit", tBids.period_unit);
		jsonMap.put("period", tBids.period);
		jsonMap.put("investDetail", investDetail);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 个人银行卡个数
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appUserAccountHome(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		String userIdStr = parameters.get("userId");
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		t_users user = t_users.findById(userId);	
		boolean isAuth = StringUtils.isNotBlank(user.reality_name) && StringUtils.isNotBlank(user.id_number);
		List<UserBankAccounts> userBanks = UserBankAccounts.queryUserAllBankAccount(userId);
		jsonMap.put("bankSize", userBanks.size());
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功");
		jsonMap.put("isAuth", isAuth);
		if(isAuth){
			jsonMap.put("realName", user.reality_name);
		}
		
		return JSONUtils.printObject(jsonMap);
	}

	/***
	 * 签到送积分
	 * 
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String appUserSignScore(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();

		String userIdStr = parameters.get("userId");

		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		User user = new User();
		user.id = userId;
		
		Date today = new Date();

		// 验证签到积分规则
//		t_mall_scroe_rule signRule = MallScroeRule.queryRuleDetailByType(MallConstants.SIGN);
//		if (signRule == null) {
//			jsonMap.put("error", -6);
//			jsonMap.put("msg", "对不起，签到送积分活动暂停");
//			return JSONUtils.printObject(jsonMap);
//		}

		// 验证当日是否签过
		String dateStr = TimeUtil.dateToStrDate(today);// yyyy-MM-dd
		int count = MallScroeRecord.queryScoreRecordByDate(user.id, dateStr, MallConstants.SIGN, error);
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，签到失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		}

		if (count > 0) {
			jsonMap.put("error", -4);
			jsonMap.put("msg", "亲，今天您已签过到了");
			return JSONUtils.printObject(jsonMap);
		}

		// 下次获取积分数获取
		int[] score = MallScroeRecord.queryScroeRecord(userId, error);

		if (error.code < 0) {
			jsonMap.put("error", -5);
			jsonMap.put("msg", "获取积分数失败");
			return JSONUtils.printObject(jsonMap);
		}

		MallScroeRecord.saveScroeSignRecord(user, score, error);
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，签到失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		} else {
			jsonMap.put("error", -1);
			jsonMap.put("msg", "亲，签到成功");
			return JSONUtils.printObject(jsonMap);
		}
	}
	
	/**
	 * 红包使用规则
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String getRedPackageRule(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		News news = new News();
		news.id = -2;//初始化红包规则
		
		jsonMap.put("content", news.content);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户是否实名（新版‘我的’界面）
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String userRealityName(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = parameters.get("userId");
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		User user = new User();
		user.id = userId;
		
		boolean tyep = true;
		if (StringUtils.isBlank(user.realityName)) {  //未实名认证
			tyep = false;
			if(user.user_type == UserTypeEnum.COMPANY.getCode() ||
					user.user_type == UserTypeEnum.INDIVIDUAL.getCode()) { // 企业或者个体工商户
				t_user_auth_review auth = t_user_auth_review.find("user_id = ? and status = 0 ", userId).first();
				if(auth != null) {
					jsonMap.put("authing", auth);//实名审核中
				}
			}
		}
		
		jsonMap.put("type", tyep);//是否实名
		jsonMap.put("realName", user.realityName);
		jsonMap.put("userType", user.user_type);//用户类型
		jsonMap.put("balance", user.balance);//余额
		jsonMap.put("isBank", Score.isBank(userId,null));//曾经有绑卡
		// 当前是否有卡
		jsonMap.put("hasBank", Score.hasBank(userId));
		jsonMap.put("payPassword",user.payPassword == null ? false : true);//是否设置交易密码
		
		
		if(user.user_type == 1) { //个人用户
			jsonMap.put("hasProtocolBank", Score.hasProtocolBank(user.id));// 当前是否有协议绑卡
		
			jsonMap.put("isNotSign", UserBankAccounts.isNotSign(user.id));	//是否有未签约银行卡
		} else { //企业和个体工商户不判断,不弹框
			jsonMap.put("hasProtocolBank", true);// 当前是否有协议绑卡
			
			jsonMap.put("isNotSign", false);//是否有未签约银行卡
		}
		 
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		return JSONUtils.printObject(jsonMap);
	}
	
	/**
	 * 用户增加实名
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String addUserRealityName(Map<String, String> parameters) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = parameters.get("userId");
		String realName = parameters.get("realName");
		String idNumber = parameters.get("idNumber");
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		User user = new User();
		user.id = userId;
		//0 未知 1 个人用户  2 企业用户 3个体工商户
		if(user.user_type!=0){
			if(user.realityName!=null && !"".equals(user.realityName)){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户已经实名认证");
				return JSONUtils.printObject(jsonMap);
			}else if(user.user_type==1){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户已经实名认证");
				return JSONUtils.printObject(jsonMap);
			}else if(user.user_type==2){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "企业用户请到PC官网完成企业认证");
				return JSONUtils.printObject(jsonMap);
			}else if(user.user_type==3){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "个体工商户用户请到PC官网完成个体工商户认证");
				return JSONUtils.printObject(jsonMap);
			}else{
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户实体类型有误");
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		if (StringUtils.isBlank(realName)) {
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "真实姓名不能为空");
			return JSONUtils.printObject(jsonMap);
		}

		if (StringUtils.isBlank(idNumber)) {
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "身份证不能为空");
			return JSONUtils.printObject(jsonMap);
		}

		if (!CharUtil.isChinese(realName)) {
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "真实姓名必须是中文");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!"".equals(IDCardValidate.chekIdCard(0, idNumber))) {
			
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的身份证号");
			return JSONUtils.printObject(jsonMap);
		}

		User.isIDNumberExist(idNumber, null,user.financeType, error);

		if(error.code < 0) {

			jsonMap.put("error", "-3");
			jsonMap.put("msg", "此身份证已开户，请重新输入");
			return JSONUtils.printObject(jsonMap);
		}

		String refID = Codec.UUID();
		new AuthReq().create(refID, userId, 1);
		if(!Constants.IS_LOCALHOST) {
			//实名认证
			String res = AuthenticationUtil.requestForIdNo(realName, idNumber, error);

			if(error.code < 0) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);

				//添加错误记录
				UserActions.insertAction(userId, 1,error.msg, error);

				return JSONUtils.printObject(jsonMap);
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
				Map<String, Object> tt = AuthenticationUtil.extractMultiXMLResult(resXml, 0);
				if (!"1".equals((String)tt.get("result"))){
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "姓名与身份证号码不一致");

					//添加错误记录
					UserActions.insertAction(userId, 1,"姓名与身份证号码不一致", error);

					return JSONUtils.printObject(jsonMap);
				}
			}else{
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "身份认证系统出错");

				//添加错误记录
				UserActions.insertAction(userId, 1,"身份认证系统出错", error);

				return JSONUtils.printObject(jsonMap);
			}
		}

		user.updateCertification(realName, idNumber,user.id, error);
		if(error.code < 0){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "认证失败");
		}else{
			jsonMap.put("error", -1);
			jsonMap.put("msg", "认证成功");
			//实名认证发红包
			RedPackageHistory.sendAuthRed(user);
			
			//实名认证后添加地址
			//截取身份证前6位
			String province = idNumber.substring(0, 2);
			String city = idNumber.substring(0, 4);
			province = province + "0000"; //补全省 ，6位数
			city = city + "00"; //补全区 ，6位数
			
			t_user_city userCity = new t_user_city();
			userCity.city_id = city;
			userCity.province_id = province;
			userCity.user_id = user.id;
			UserCitys.addUserCity(error, userCity);
		}
		
		return JSONUtils.printObject(jsonMap);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2016年9月30日 上午10:53:17 
	 * @description.  CSP分成记录
	 * 
	 * @param parameters
	 * @return
	 */
	public static String findUserCPSProfitOld(Map<String, String> parameters) throws IOException{
		//老版app使用 18年4月份后新app不使用
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				PageBean<user_cps_profit> page = UserCpsProfit.cpsUsersProfitOld(userId, Integer.valueOf(parameters.get("currPage")), 20);
				if(page.page != null) {
					for(user_cps_profit ucp : page.page) {
						ucp.user_mobile = ucp.user_mobile.replaceAll("(\\d{3})\\d{8}","$1********");
					}
				}
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("page", page);
				result.put("totalNum", page.totalCount);
				
				Logger.info("分成记录："+JSON.toJSONString(page, true));
			}
		}
		
		return JSON.toJSONString(result);
	}
	/**
	 * @author zqq
	 * @creationDate. 2018年12月05日 
	 * @description.  CSP分成记录 新app使用.老版app使用findUserCPSProfitOld 
	 * 
	 * @param parameters
	 * @return
	 */
	public static String findUserCPSProfit(Map<String, String> parameters) throws IOException{
		
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
			}else{
				
				PageBean<Map<String, Object>> page=null;
				try {
					System.out.println("userId:"+userId+" currPage:"+Integer.valueOf(parameters.get("currPage")));
					page = UserCpsProfit.cpsUsersProfit(userId, Integer.valueOf(parameters.get("currPage")), 20);
				} catch (NumberFormatException e) {
					result.put("error", "-2");
					result.put("msg", e.getMessage());
					e.printStackTrace();
					return JSON.toJSONString(result);
				} catch (Exception e) {
					result.put("error", "-2");
					result.put("msg", e.getMessage());
					e.printStackTrace();
					return JSON.toJSONString(result);
				}
				
				if(page.page != null) {
					for(Map ucp : page.page) {
						ucp.put("name",User.hideString(ucp.get("name")==null?"":ucp.get("name").toString()));
					}
				}
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("page", page);
				result.put("totalNum", page.totalCount);
				
				Logger.info("分成记录："+JSON.toJSONString(page, true));
			}
		}
		
		return JSON.toJSONString(result);
	}
	/**
	 * 
	 * @author liulj
	 * @creationDate. 2017年2月27日 上午10:51:09 
	 * @description.  用户银行卡投保详情
	 * 
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String findUserBankInusrDetail(Map<String, String> parameters) throws IOException{
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				Map<String, Object> data = User.findUserBankInsurDetail(userId);
				
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("data", data); // 保险详情
				result.put("protocolClause", News.queryContent(Constants.NewsTypeId.INSUR_PROTOCOL_CLAUSE, error));// 保险条款
				result.put("protocolFlow", News.queryContent(Constants.NewsTypeId.INSUR_PROTOCOL_FLOW, error));// 理赔流程
				
				Logger.info("保险详情："+JSON.toJSONString(data, true));
				
				
				// 多张银行卡实现
				List<Map<String, Object>> bankInsurs = User.findUserALLBankInsurDetail(userId);
				result.put("bankInsurs", bankInsurs); // 保险详情
			}
		}
		
		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年3月20日 下午2:35:23 
	 * @description.  公益账户
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findPublicBenefit(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			JSONObject data = User.findPublicBenefitAccount(null);
			result.put("error", "-1");
			result.put("msg", "查询成功");
			result.put("data", data); // 保险详情
			
			Logger.info("公益账户："+JSON.toJSONString(data, true));
			return JSON.toJSONString(result);
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				JSONObject data = User.findPublicBenefitAccount(userId);
				
				
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("data", data); // 保险详情
				
				Logger.info("公益账户："+JSON.toJSONString(data, true));
			}
		}
		
		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年3月20日 下午2:35:53 
	 * @description.  公益列表
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findPublicBenefitList(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		PageBean<Map<String, Object>> page = User.findPublicBenefit(null, null, null, 0, 20, Integer.valueOf(parameters.get("currPage")));
		
		result.put("error", "-1");
		result.put("msg", "查询成功");
		result.put("page", page);
		result.put("totalNum", page.totalCount);
		
		Logger.info("公益列表："+JSON.toJSONString(page, true));
		
		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年3月20日 下午2:36:25 
	 * @description.  公益详情
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findPublicBenefitDetail(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("error", "-1");
		result.put("msg", "查询成功");
		result.put("data", t_public_benefit.findById(Long.valueOf(parameters.get("id"))));
		
		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年3月23日 上午10:56:19 
	 * @description.  公益活动规则
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findPublicBenefitRule(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		result.put("error", "-1");
		result.put("msg", "查询成功");
		result.put("data", News.queryContent(Constants.NewsTypeId.PUBLIC_BENEFIT, new ErrorInfo()));
		
		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年5月22日 下午4:28:00 
	 * @description.  理财账单
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findUserInvestList(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				PageBean<Map<String, Object>> page = User.findUserSimpleInvests(userId, 20, Integer.valueOf(parameters.get("currPage")));
				
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("page", page);
				result.put("totalNum", page.totalCount);
				
				Logger.info("理财账单："+JSON.toJSONString(page, true));
			}
		}
		
		return JSON.toJSONString(result);
	}

	/**
	 *
	 * @param parameters:map{userIdSign,status,orderby,asc_or_desc,pageSize,currPage}
	 * status->default:0:全部,1:还款中,2:已还款,10:可转让,11:转让中,20:已转让,30:不可转让
	 * orderby->default:0:放款时间,1:最后还款时间,2:下次还款时间,3:待收金额
	 * asc_or_desc->"asc"|"desc"
	 * @return 筛选投资列表
	 * @author zqq
	 * @creationDate. 2018年8月4日下午6:08:34
	 */
	public static String findUserInvestListByCondition(Map<String, String> parameters) {
		// TODO Auto-generated method stub
		Map<String, Object> result = new HashMap<String, Object>();

		String userIdSign = parameters.get("id");

		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");

			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");

				//return JSONUtils.printObject(result);
			}else{

				PageBean<Map<String, Object>> page = User.findUserSimpleInvestsByCondition(userId,parameters, 20, Integer.valueOf(parameters.get("currPage")));

				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("page", page);
				result.put("totalNum", page.totalCount);

				Logger.info("理财账单："+JSON.toJSONString(page, true));
			}
		}

		return JSON.toJSONString(result);
	}

	
	/**
	 * @author liulj
	 * @creationDate. 2017年5月22日 下午4:34:37 
	 * @description.  回款计划
	 * 
	 * @param parameters
	 * @return
	 */
	
	public static String findUserInvestReturn(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				List<Map<String, Object>> data = User.findUserInvestsReturned(userId, Long.valueOf(parameters.get("invest_id")));
				t_debt_transfer transfer = null;

				try {
					transfer = t_debt_transfer.find(" invest_id = ? and status = 3 ", Long.valueOf(parameters.get("invest_id"))).first();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(transfer != null){
					result.put("period", transfer.period);
					result.put("debtAmount", transfer.debt_amount);
				}

				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("data", data);
				
				Logger.info("回款计划："+JSON.toJSONString(data, true));
			}
		}
		
		return JSON.toJSONString(result);
	}
	
	public static String findUserScoreReturn(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				Map<String, Object> data = Score.getUserScoreData(userId,error);
				if(error.code < 0){
					result.put("error", "-2");
					result.put("msg", error.msg);
				}else{
					result.put("error", "-1");
					result.put("msg", "查询成功");
					result.put("data", data);
					
					Logger.info("我的积分："+JSON.toJSONString(data, true));
				}
			}
		}
		
		return JSON.toJSONString(result);
	}
	
	public static String findUserScoreRecord(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		int currPage = 1;
		
		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				PageBean<Map<String, Object>> page = Score.getUserScoreRecord(userId,currPage,pageSize);
				
				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("totalNum", page.totalCount);
				result.put("list", page.page);
				
				Logger.info("获取积分记录："+JSON.toJSONString(page.page, true));
			}
		}
		return JSON.toJSONString(result);
	}
	
	public static String appUserSign(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();

		String userIdStr = parameters.get("userId");

		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		User user = new User();
		user.id = userId;

		// 验证当日是否签过
		boolean flag = Score.isSign(userId,error);
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，签到失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(flag){
			jsonMap.put("error", -4);
			jsonMap.put("msg", "亲，今天您已签过到了");
			return JSONUtils.printObject(jsonMap);
		}
		JPAUtil.transactionBegin();
		Score.saveScroeSignRecord(user, error);
		JPAUtil.transactionCommit();
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，签到失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		} else {
			jsonMap.put("error", -1);
			jsonMap.put("msg", "亲，签到成功!");
			Map<String,Object> map = Score.getUserScoreData(userId,error);
			map.put("isSign", true);
			jsonMap.put("data", map);
			return JSONUtils.printObject(jsonMap);
		}
	}
	
	public static String duibaUrl(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();

		String userIdStr = parameters.get("userId");

		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		User user = new User();
		user.id = userId;

		
		String url = Score.getDuiBaUrl(user,null, error);
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，积分商城打开失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		} else {
			jsonMap.put("error", -1);
			jsonMap.put("url", url);
			return JSONUtils.printObject(jsonMap);
		}
	}
	
	public static String certUrl(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String url = null;
		try {
			Map<String, Object> result = Invest.findInvestForId(Long.valueOf(parameters.get("invest_id")));
			String cert = (String) result.get("certificateUrl");
			url = SceneHelper.getViewCertificateInfoUrl(cert,(String) result.get("idNumber"));
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		if(StringUtils.isBlank(url)){
			error.code = -1;
		}
		if (error.code < 0) {
			jsonMap.put("error", -3);
			jsonMap.put("msg", "对不起，获取电子存证地址失败，请联系客服");
			return JSONUtils.printObject(jsonMap);
		} else {
			jsonMap.put("error", -1);
			jsonMap.put("url", url);
			return JSONUtils.printObject(jsonMap);
		}
	}

	public static String saveRiskResult(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				String apiversion=parameters.get("apiversion");
				if(StringUtils.isBlank(apiversion)){
					apiversion="1";
				}
				if(!NumberUtil.isNumericDouble(apiversion)){
					result.put("error", "-3");
					result.put("msg","版本号有误");
					return JSON.toJSONString(result);
				}
				BigDecimal apiversionBd=new BigDecimal(apiversion);
				
				try {
					if(apiversionBd.compareTo(new BigDecimal("1.2"))>=0){
						UserRisk.updateUserRisk(userId, parameters.get("riskResult"), parameters.get("riskAnswer"));
					}else{
						UserRisk.updateUserRiskOld(userId, parameters.get("riskResult"), parameters.get("riskAnswer"));
					}
					result.put("error", "-1");
					result.put("msg", "保存成功");
				} catch (Exception e) {
					result.put("error", -3);
					result.put("msg", "参数错误");
				}
			}
		}
		
		return JSON.toJSONString(result);
	}

	/**
	 * 
	 * @author liulj
	 * @creationDate. May 2, 2018 2:52:07 PM 
	 * @description.  获取协议支付验证码
	 * 
	 * @param parameters
	 * @return
	 */
	public static String getProtoPaySms(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();

		//关闭充值业务10.28
		if(true) {
			result.put("error", "-155");
			result.put("msg", "充值错误，请联系客服。");

			return JSON.toJSONString(result);
		}
		
		String userIdSign = parameters.get("id");
		
		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
			
			//return JSONUtils.printObject(result);
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			long currentUserId = userId;

			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				
				//return JSONUtils.printObject(result);
			}else{
				
				BigDecimal moneyDecimal = new BigDecimal(parameters.get("money")).setScale(2, RoundingMode.DOWN);
				String protocolNo = parameters.get("protocolNo");
				
				try {
					List<t_user_bank_accounts> userBanAccountVerify = t_user_bank_accounts
							.find(" user_id = ? and protocol_no = ? ", userId , protocolNo ).fetch();
					if(userBanAccountVerify.size()<1) {
						result.put("error", "-2");
						result.put("msg", "用户持有的卡异常");
						return JSON.toJSONString(result);
					}
					
					//TODO 取卡需要优化
					//对批量迁移的用户以及后面重复绑定的卡的协议号 用户协议号需要用到有效的协议号和userid
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
								userId=uid;
							}
						} else if (banks.size() > 1) {//迁移过来的数据
							//取最小的userid,因为这个协议号是之前认证的
							userId = Math.min(banks.get(0).user_id, banks.get(1).user_id);
						}
					}


					// 预支付->发送验证码
					String uniqueKey = ReadyPay.execute(protocolNo, userId, moneyDecimal);


					ErrorInfo errorInfo =new ErrorInfo();
					//认证充值前操作
					User.sequence_new(currentUserId,-4, uniqueKey,"", moneyDecimal.doubleValue(), Constants.GATEWAY_RECHARGE, Constants.CLIENT_APP, "",errorInfo);

					if(errorInfo.code == 0) {
						result.put("error", -1);
						result.put("msg", "短信发送成功");
						result.put("uniqueKey", uniqueKey);
					}else{
						result.put("error", 0);
						result.put("msg", errorInfo.msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.put("error", 0);
					result.put("msg", e.getMessage());
				}
			}
		}
		
		return JSON.toJSONString(result);
	}

	/**
	 * 
	 * @author liulj
	 * @creationDate. Jun 27, 2018 4:19:13 PM
	 * @description.  银行卡限额
	 * 
	 * @param parameters
	 * @return
	 */
	public static String getBankPayLimit(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();
		String financeType = parameters.get("financeType");//0 借款端，1投资端 ，不传默认投资端
		
		try {
			List<Map<String, Object>> list = DictBanksDate.findBankLimit();
			
			Map<Object, Object> banks = new HashMap<Object, Object>();
			for(Map<String, Object> map : list) {
				if(!StringUtils.isEmpty(financeType) && financeType == BORROW &&
						AgreementBanks.isAvailable(map.get("bank_code"))) { //借款端只显示协议支付
					banks.put(map.get("bank_code")+"", map);
				} else {
					banks.put(map.get("bank_code")+"", map);
				}
				
			}
			System.out.println(JSON.toJSONString(banks));
			
			result.put("error", -1);
			result.put("msg", "成功");
			//result.put("bankLimit", list);
			result.put("banks", banks);
		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", e.getMessage());
		}
		
		return JSON.toJSONString(result);
	}
	 
	/**
	 *
	 * @author wangyun
	 * 2018年6月29日
	 * RequestData.java
	 * @description 申请债权转让
	 */
	public static String getApplyDebtTransfer(Map<String, String> parameters){
		Map<String, Object> result = new HashMap<String, Object>();
		String userIdSign = parameters.get("id");
		String bidId = parameters.get("bid_id");
		String investId = parameters.get("invest_id");
		ErrorInfo error = new ErrorInfo();
		try {
			if(StringUtils.isBlank(userIdSign) ){
				result.put("error", "-2");
				result.put("msg", "用户id有误");
				return JSONUtils.printObject(result);
			}

			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				return JSONUtils.printObject(result);
			}

			if(StringUtils.isBlank(investId)){
				result.put("error", "-2");
				result.put("msg", "投资ID不能为空！");
				return JSONUtils.printObject(result);
			}

			if(StringUtils.isBlank(bidId)){
				result.put("error", "-2");
				result.put("msg", "标的ID不能为空！");
				return JSONUtils.printObject(result);
			}

			//关联标的信息
			Map<String, Object> bidInfo = DebtTransfer.getBidInfo(Long.valueOf(bidId), error);
			result = DebtTransfer.getTransferInfo(Long.valueOf(investId), error);

			if(error.code<1){
				result.put("error", "-11");
				result.put("msg", error.msg);
			}else{
				result.put("error", "-1");
				result.put("msg", "查询成功！");
				result.put("bidInfo", bidInfo);
			}

		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", e.getMessage());

		}
		return JSON.toJSONString(result);
	}

	/**
	 * 债权转让支付
	 * @author wangyun
	 * 2018年7月3日
	 * @description
	 */
	public static String getDebtTransferPay(Map<String, String> params){
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> result = new HashMap<String, Object>();
		String userIdSign = params.get("userId");
		String dealPassword = params.get("dealPassword");//交易密码
		String investId = params.get("investId");
		String transferDays = params.get("transferDays");
		/*String red_amount = params.get("red_amount");//红包
		String increase_interest = params.get("increase_interest");//加息金额
		String principal_fee= params.get("principal_fee");//本金转让费
		String transfer_fee = params.get("transfer_fee"); //总的转让费用
*/		try {

			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				return JSONUtils.printObject(result);
			}
			if(StringUtils.isBlank(dealPassword)){
				result.put("error", "-3");
				result.put("msg", "交易密码为空！");
				return JSONUtils.printObject(result);
			}

		/*	//校验传过来的金额是否正确
			Map<String, Object> map = DebtTransfer.getTransferInfo(Long.valueOf(investId), error);
			*/
			/*Map<String, Object> transferInfo = (Map<String, Object>) map.get("transferInfo");
			if(!(Double.parseDouble(increase_interest) == Double.parseDouble(transferInfo.get("increase_interest")+""))){
				result.put("error", "-3");
				result.put("msg", "加息利息金额错误");
				return JSON.toJSONString(result);
			}
			if(!(Double.parseDouble(red_amount) == Double.parseDouble(transferInfo.get("red_amount")+""))){
				result.put("error", "-3");
				result.put("msg", "红包金额错误");
				return JSON.toJSONString(result);
			}
			if(!(Double.parseDouble(principal_fee) == Double.parseDouble(transferInfo.get("principal_fee")+""))){
				result.put("error", "-3");
				result.put("msg", "本金转让费金额错误");
				return JSON.toJSONString(result);
			}*/
			String dealPwd = Encrypt.decrypt3DES(dealPassword, Constants.ENCRYPTION_KEY);
			DebtTransfer.transferPay(userId, Long.valueOf(investId), dealPwd,Integer.valueOf(transferDays),error);

			if(error.code < 1){
				result.put("error", "-11");
				result.put("msg", error.msg);
				return JSON.toJSONString(result);
			}

			result.put("error", -1);
			result.put("msg", "债权转让支付成功！");

		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", e.getMessage());
		}
		return JSON.toJSONString(result);

	}

	/**
	 * 我的债权转让账单
	 * @author wangyun
	 * 2018年7月6日
	 * @description
	 */
	public static String getDebtTransferList(Map<String, String> params){
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> result = new HashMap<String, Object>();
		String userIdSign =  params.get("userId");
		try {
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				return JSONUtils.printObject(result);
			}

			PageBean<Map<String, Object>> page = DebtTransfer.getDebtTransferList(userId, 20, Integer.valueOf(params.get("currPage")), error);

			if(error.code < 0){
				result.put("error", -3);
				result.put("msg", "查询失败！");
				return JSON.toJSONString(result);
			}

			result.put("error", -1);
			result.put("msg", "查询成功！");
			result.put("page", page);

		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", e.getMessage());
		}

		return JSON.toJSONString(result);
	}

	/**
	 * 债权转让详情
	 * @author wangyun
	 * 2018年7月9日
	 * @description
	 */
	public static String getDebtTransferDetail(Map<String, String> params){
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> result = new HashMap<String, Object>();
		String userIdSign =  params.get("userId");
		String investId =  params.get("investId");
		String debt_id = params.get("debtId");
		try {
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				return JSONUtils.printObject(result);
			}

			Map<String, Object> map = DebtTransfer.getDebtTransferDetail(userId, Long.valueOf(investId),Long.valueOf(debt_id), error);
			result.put("error", -1);
			result.put("msg", "查询成功！");
			result.put("transferDetail", map);

		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", error.msg);
		}

		return JSON.toJSONString(result);
	}

	/**
	 * 投资账单
	 * @author wangyun
	 * 2018年7月9日
	 * @description
	 */
	public static String queryUserbidsV1(Map<String, String> parameters) throws IOException{
		Map<String, Object> map = new HashMap<String, Object>();
		int currPage = 1;
		ErrorInfo error = new ErrorInfo();
		try {
			if (parameters.get("currPage") != null) {
				currPage = Integer.parseInt(parameters.get("currPage"));
			}
			if(parameters.get("type") == null){
				map.put("error", "-2");
				map.put("msg", "传入类型有误");
				return JSONUtils.printObject(map);
			}

			int pageSize = Constants.APP_PAGESIZE;

			Map<String, Object>  jsonMap = new HashMap<String, Object>();

			String apr = (String)parameters.get("apr");
			String amount = (String)parameters.get("amount");
			String loanSchedule = (String)parameters.get("loanSchedule");
			String startDate = (String)parameters.get("startDate");
			String endDate = (String)parameters.get("endDate");
			String loanType = (String)parameters.get("loanType");
			String minLevelStr = (String)parameters.get("minLevelStr");
			String maxLevelStr = (String)parameters.get("maxLevelStr");
			String orderType = (String)parameters.get("orderType");
			String keywords = (String)parameters.get("keywords");
			PageBean<v_front_all_bids_v2> bids = null;
			PageBean<v_front_debt_bids> debts = null;
			if(parameters.get("type").equals("1")){ //普通标的
				bids = Invest.queryAllBidsNotRepayV3(Constants.SHOW_TYPE_2, currPage, pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevelStr, maxLevelStr, orderType, keywords, error,"1");

			} else if(parameters.get("type").equals("2")) {//债权标的
				debts = Invest.queryDebtsNotRepay(currPage, pageSize, error);
			}

			if(error.code < 0){
				jsonMap.put("error", "-4");
				jsonMap.put("msg",error.msg);
				return JSONUtils.printObject(jsonMap);
			}

			map.put("error", -1);
			map.put("msg", "查询成功");
			if(parameters.get("type").equals("1")){ //普通标的
				map.put("totalNum", bids.totalCount);
				map.put("list",bids.page);

			} else if(parameters.get("type").equals("2")){//债权转让标的
				map.put("totalNum", debts.totalCount);
				map.put("list",debts.page);
			}

		} catch (Exception e) {
			map.put("error", 0);
			map.put("msg", error.msg);
		}
		return  JSONObject.fromObject(map).toString();
	}


	public static String queryUserBidsDebtDetail(Map<String, String> params){
		Map<String, Object> result = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		//String userIdSign = params.get("userId");
		String investId = params.get("invest_id");
		String debt_user_id = params.get("debt_user_id");//申请债权的用户ID
		String debt_id = params.get("debtId");//申请债权的用户ID
		try {
			/*long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
				return JSONUtils.printObject(result);
			}*/
			//标的和债权信息
			Map<String, Object> map = DebtTransfer.getDebtTransferDetail(Long.valueOf(debt_user_id), Long.valueOf(investId),Long.valueOf(debt_id), error);

			// 项目背景，安全保障
			Map<String, Object> bidMap = (Map<String, Object>) map.get("bidMap");
			long bid_id = Long.valueOf(bidMap.get("bid_id")+"");

			Bid bid = new Bid();
			bid.bidDetail = true;
			bid.upNextFlag = Constants.BID_SHZ;
			bid.id = bid_id;

			t_bids t = t_bids.findById(bid_id);
			List<String> list = new ArrayList<String>();

			List<v_user_audit_items> items = UserAuditItem.queryUserAuditItem(bid.userId, bid.mark, error);
			if(items != null && items.size() > 0){
				for(v_user_audit_items audit_item : items){
					if(audit_item.is_visible && (audit_item.status == 2 || audit_item.status == 3)){
						//查询是否投资
						UserAuditItem item = new UserAuditItem();
						item.lazy = true;
						item.userId = bid.userId;
						item.mark = audit_item.mark;
						if(item.auditItem.type == 1){
							list.addAll(item.getItems());
						}
					}
				}
			}
			result.put("map", map);
			result.put("description", bid.description);//项目背景
			result.put("imgList", list);//相关图片
			result.put("CBOAuditDetails", bid.auditSuggest);//安全保障
			result.put("error", "-1");
			result.put("msg", "查询成功");

		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", error.msg);
		}
		return  JSONObject.fromObject(result).toString();

	}
	/**
	 * 投资债权标的详细信息
	 * @author wangyun
	 * 2018年8月1日
	 * @description
	 */
	public static String queryUserInvestDebtDetail(Map<String, String> params){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		String debtIdStr = params.get("debt_id");//债权标的ID
		String idStr = params.get("id");
		String bid_id = params.get("bid_id");
		if(StringUtils.isBlank(debtIdStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入债权标的ID！");
			return JSONObject.fromObject(jsonMap).toString();
		}

		if(StringUtils.isBlank(idStr)){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "请传入用户ID！");
			return JSONObject.fromObject(jsonMap).toString();
		}

		long debtId = Long.parseLong(debtIdStr);

		long id = Security.checkSign(idStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0 || id < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}


		User user = new User();
		user.id = id;
		t_debt_transfer transfer = t_debt_transfer.find(" id = ? ", debtId).first();
		jsonMap.put("debtId", debtId);
		Logger.info("transferInfo: " + transfer);
		/*
		if(null != bid.userName && bid.userName.length() > 1) {
			bid.userName = bid.userName.substring(0,1) + "***";
		}*/


		jsonMap.put("creditRating", user.myCredit.imageFilename);//图片
		jsonMap.put("accountAmount", user.balance + user.freeze);
		jsonMap.put("availableBalance", user.balance);
		/*jsonMap.put("schedules", bid.loanSchedule);

		jsonMap.put("borrowAmount", bid.amount);
		jsonMap.put("apr", bid.apr);
		jsonMap.put("period", bid.period);
		jsonMap.put("periodUnit", bid.periodUnit);
		jsonMap.put("repayType", bid.repayment.id);

		if(bid.periodUnit == -1){
			jsonMap.put("deadline", bid.period+"年");
		}else if(bid.periodUnit == 0){
			jsonMap.put("deadline", bid.period+"个月");
		}else{
			jsonMap.put("deadline", bid.period+"天");
		}
		*/

		if(user.payPassword != null){
			jsonMap.put("payPassword", true);
		}else{
			jsonMap.put("payPassword", false);
		}
		//可投金额
		/*double need_amount = transfer.debt_amount - transfer.has_invested_amount;
		jsonMap.put("need_amount", need_amount);*/
		//查询当前用户红包可用数量
		List<t_red_packages_history> tRed = new ArrayList<t_red_packages_history>();
		tRed = t_red_packages_history.find(" user_id = ? and status = 0", id).fetch();

		jsonMap.put("transferInfo", transfer);

		jsonMap.put("error","-1");
		jsonMap.put("redCount",tRed.size());
		jsonMap.put("msg","投标详情查询成功");
		/*jsonMap.put("title",bid.title);
		jsonMap.put("paymentMode",bid.repayment.name);
		jsonMap.put("paymentTime", bid.recentRepayTime + "");
		jsonMap.put("InvestmentAmount", bid.hasInvestedAmount);
		jsonMap.put("needAmount",bid.amount - bid.hasInvestedAmount);
		jsonMap.put("minTenderedSum", bid.minAllowInvestAmount);
		jsonMap.put("investNum", bid.investCount);
		jsonMap.put("views", bid.readCount);
		jsonMap.put("isDealPassword", bid.product.isDealPassword);
		jsonMap.put("averageInvestAmount",bid.averageInvestAmount);
		jsonMap.put("needAccount",bid.averageInvestAmount > 0 ? Arith.round((bid.amount-bid.hasInvestedAmount)/bid.averageInvestAmount,0) :  0);
		//加息
		jsonMap.put("isIncreaseRate",bid.isIncreaseRate);
		jsonMap.put("increaseRate",bid.increaseRate);
		jsonMap.put("increaseRateName",bid.increaseRateName);*/

		return JSONObject.fromObject(jsonMap).toString();
	}


	/**
	 * 债权转让计算器
	 * 按比例计算
	 * @author wangyun
	 * 2018年7月11日
	 * @description
	 */
	public static String appDebtRepaymentCalculate(Map<String, String> params){
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> result = new HashMap<String, Object>();

		try {
			String investId = params.get("investId");
			String amountStr = params.get("amount");
			String sql = "select amount from t_invests where id = ?";
			//根据投资ID 查询该投资记录的金额及利息， 按比例计算
			double amount_1 = t_invests.find(sql, Long.valueOf(investId)).first(); //原始标的的投资记录

			t_bill_invests billInvest = t_bill_invests.find(" invest_id = ? ", Long.valueOf(investId)).first();

			//double receive_corpus =  billInvest.receive_corpus;//本期应收款金额
			double receive_interest = billInvest.receive_interest;//本期应收利息

			double amount = Double.parseDouble(amountStr);
			double income =  Arith.round(amount / amount_1 * receive_interest, 2);//按比例   （债权投资金额/债权金额 = 债权投资利息/债权利息）

			long bid_id = billInvest.bid_id;
			t_bids bid=t_bids.findById(bid_id);//获得标的
			t_debt_transfer transfer = t_debt_transfer.find(" invest_id = ? ", Long.valueOf(investId)).first();//获得债权标的
			//只查询剩下未还款的,债权转让标的没有加息
			List<t_bill_invests> billInvestList = t_bill_invests.find(" bid_id = ? and status in(-7,-8) and invest_id = ? order by id asc ", bid_id, Long.valueOf(investId)).fetch();
			result.put("income", income);//投资收益
			List<Map<String, Object>> list = new ArrayList<>();
			/*for (int i = 0; i < investList.size(); i++) {
				t_bill_invests invest = investList.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				double receive_corpus_ =  invest.receive_corpus;//本期应收款金额
				double receive_interest_ = invest.receive_interest;//本期应收利息

				double corpus_ = 0;
				double interest_ = 0;
//				if(i == 0){ //如果本期账单不是全部属于受让人，则本金全部还给出让人，利息按比例分配
//					corpus_ = 0;
//					interest_ =  Arith.round(amount / amount_1 * receive_interest_, 2);//债权投资利息  ，按比例   （债权投资金额/债权金额 = 债权投资利息/债权利息）
//
//				} else {
				corpus_ = Arith.round(amount / amount_1 * receive_corpus_, 2);//债权投资本金
				interest_ =  Arith.round(amount / amount_1 * receive_interest_, 2);
				//}

				map.put("debt_corpus", corpus_); //每一期的本金
				map.put("debt_interest", interest_); //每一期的利息
				map.put("periods", invest.periods);

				list.add(map);
			}*/
			//Date last_repay_time = null;//最后还款时间
			//BigDecimal debtInterestAcmountDec=BigDecimal.ZERO;//转让的所有利息
			int scale=BigDecimal.valueOf(amount_1).setScale(2,BigDecimal.ROUND_HALF_EVEN).precision();//比例计算进度,最终金额需要精确到分,如果计算的原金额整数位为X,比例的小数位需要精确到小数点后X+2位,计算结果可以精确到分
			double profit = 0;
			for(t_bill_invests billInvest_ : billInvestList){
				BigDecimal assigneeRate=BigDecimal.ZERO;//受让人所占比例
				Map<String, Object> map = new HashMap<String, Object>();

				if(transfer.current_period ==  billInvest_.periods){//转让第一期
					Date currentPeriodBeginDate = null;//当期开始时间
					if(bid.repayment_type_id == Constants.ONCE_REPAYMENT){//一次性还款
						currentPeriodBeginDate = bid.audit_time;//当期开始时间=放款时间
					}else{
						currentPeriodBeginDate = DateUtil.dateAddMonth(billInvest_.receive_time,-1);//当期开始时间=本期还款时间-1month
					}
					int currentPeriodAccrualDate = DateUtil.daysBetween(currentPeriodBeginDate, billInvest_.receive_time);//当期总天数
					int accrualDate = DateUtil.daysBetween(transfer.accrual_time, billInvest_.receive_time);//当期受让人计息天数
					assigneeRate = BigDecimal.valueOf(accrualDate).divide(BigDecimal.valueOf(currentPeriodAccrualDate), scale, BigDecimal.ROUND_HALF_EVEN);//受让人所占比例

				} else {
					assigneeRate = BigDecimal.ONE;
				}

				BigDecimal currentPeriodInterest = BigDecimal.valueOf(billInvest_.receive_interest).multiply(assigneeRate).setScale(2, BigDecimal.ROUND_HALF_EVEN);//所有债权转让投资人可获得的当期利息
				//Arith.round(amount / amount_1 * currentPeriodInterest, 2);
				currentPeriodInterest = BigDecimal.valueOf(amount).divide(new BigDecimal(amount_1), scale, BigDecimal.ROUND_HALF_EVEN).multiply(currentPeriodInterest);
				map.put("debt_interest", currentPeriodInterest);//本期应收利息
				map.put("debt_corpus", billInvest_.receive_corpus);//本期应收款金额
				map.put("periods", billInvest_.periods);
				list.add(map);
				profit = new BigDecimal(profit).add(new BigDecimal(Double.parseDouble(currentPeriodInterest + ""))).doubleValue();
			}

			result.put("error","-1");
			result.put("msg","查询成功！");
			result.put("profit", profit);
			result.put("score", Score.investScore(profit));
			result.put("list", list);
		} catch (Exception e) {
			result.put("error", 0);
			result.put("msg", error.msg);
		}
		return JSONObject.fromObject(result).toString();
	}

	public static String debtInvest(Map<String, String> params){


		ErrorInfo error = new ErrorInfo();
		Map<String, Object> map = new HashMap<String, Object>();

		//String investId = params.get("investId");
		String detbIdStr = params.get("debtId");
		String userIdStr = params.get("userId");
		String amountStr = params.get("amount");
		String dealPwd = params.get("dealPwd");
		RedPackageHistory redPackage = null;

		//红包ID
		String redPackageId = params.get("redPackageId");

		if (StringUtils.isBlank(amountStr)) {
			map.put("error", "-3");
			map.put("msg", "请输入投标金额");
			return JSONObject.fromObject(map).toString();
		}

		if (StringUtils.isBlank(detbIdStr)) {
			map.put("error", "-3");
			map.put("msg", "请传入债权转让标的ID");
			return JSONObject.fromObject(map).toString();
		}
		if (StringUtils.isBlank(userIdStr)) {
			map.put("error", "-2");
			map.put("msg", "请传入用户ID");
			return JSONObject.fromObject(map).toString();
		}

		boolean b=amountStr.matches("^[+]{0,1}(\\d+)$|^[+]{0,1}(\\d+\\.\\d+)$");
    	if(!b){
    		map.put("error", "-3");
    		map.put("msg", "对不起！投标金额只能是正数！");
			return JSONObject.fromObject(map).toString();
    	}

    	long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

    	if(error.code < 0 || userId < 0){
			map.put("error", "-2");
			map.put("msg", "解析用户id有误");
			return JSONObject.fromObject(map).toString();
		}

    	//解析红包ID
    	if (!StringUtils.isBlank(redPackageId)) {

    		//long redId =  Security.checkSign(redPackageId, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
    		long redId =  Long.parseLong(redPackageId);

    		if(redId < 0){
    			map.put("error", "-3");
    			map.put("msg", "解析红包id有误");
    			return JSONObject.fromObject(map).toString();
    		}

        	redPackage = new RedPackageHistory();

        	redPackage.id = redId;

        	if(redPackage.status != Constants.RED_PACKAGE_STATUS_UNUSED){
        		map.put("error", "-3");
    			map.put("msg", "红包已经失效，或已使用!");
    			return JSONObject.fromObject(map).toString();
        	}
        	//平均分数金额
        	String averageInvestAmount = params.get("averageInvestAmount");

        	if(Double.parseDouble(averageInvestAmount) > 0){
        		//如果投标金额小于红包金额则投资失败
            	if(redPackage.money > new BigDecimal(amountStr).multiply(new BigDecimal(averageInvestAmount)).doubleValue()){
            		map.put("error", "-3");
        			map.put("msg", "投资金额必须大于红包金额");
        			return JSONObject.fromObject(map).toString();
            	}
        	}else{
        		//如果投标金额小于红包金额则投资失败
            	if(redPackage.money > Double.valueOf(amountStr)){
            		map.put("error", "-3");
        			map.put("msg", "投资金额必须大于红包金额");
        			return JSONObject.fromObject(map).toString();
            	}
        	}


		}

		User user = new User();
		user.id = userId;

		if (!(user.isEmailVerified || user.isMobileVerified)) {
			map.put("error", "-888");
			map.put("msg", "用户未激活账号");
			return JSONObject.fromObject(map).toString();
		}

		long detbId = Long.parseLong(detbIdStr);
		double amount=Double.valueOf(amountStr);

		dealPwd = Encrypt.decrypt3DES(dealPwd, Constants.ENCRYPTION_KEY);

		DebtTransfer.invest(userId, detbId, amount,dealPwd, redPackage,error);

		if(error.code == Constants.BALANCE_NOT_ENOUGH){
			map.put("error", Constants.BALANCE_NOT_ENOUGH+"");
			map.put("msg", error.msg);

		} else if(error.code < 1){
			map.put("error", "-3");
			map.put("msg", error.msg);

		}else{
			map.put("error", -1);
			map.put("msg", "投标成功");
		}

		return JSONObject.fromObject(map).toString();
	}

	public static String debtInvestDetail(Map<String, String> params) throws IOException{
		int currPage = 1;

		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		int pageSize = Constants.APP_PAGESIZE;
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();
		String bidIdStr = params.get("debtId");

		if(StringUtils.isBlank(bidIdStr)){
			jsonMap.put("error", -3);
			jsonMap.put("msg", "债权标id参数有误");

			return JSONObject.fromObject(jsonMap).toString();
		}
		long bidId = Long.parseLong(bidIdStr);

		PageBean<v_debt_invest_records> pageBean = DebtTransfer.getDebrInvestDetail(currPage, pageSize, bidId, error);
		List<v_debt_invest_records> page = pageBean.page;
		if(null != page) {
			for(v_debt_invest_records record : page) {
				String name = record.name;
				if(null != name && name.length() > 1) {
					if(StringUtils.isNumeric(name) && name.length() == 11){
						record.name = record.name.substring(0, 3) + "***"+record.name.substring(7);
					}else{
						record.name = record.name.substring(0, 1) + "***";
					}

				}
			}
		}
		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg","查询出现异常，给您带来的不便敬请谅解！");

			return JSONUtils.printObject(jsonMap);
		}


		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("totalNum",  pageBean.totalCount);
		map.put("list",page);
		return  JSONObject.fromObject(map).toString();
	}


	/**
	 * 债权转让回款账单
	 * @author wangyun
	 * 2018年7月30日
	 * @description
	 */

	public static String findUserDebtInvestReturn(Map<String, String> parameters) {
		Map<String, Object> result = new HashMap<String, Object>();

		String userIdSign = parameters.get("id");

		if(StringUtils.isBlank(userIdSign) ){
			result.put("error", "-2");
			result.put("msg", "用户id有误");
		}else{
			ErrorInfo error = new ErrorInfo();
			long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

			if(error.code < 0){
				result.put("error", "-2");
				result.put("msg", "解析用户id有误");
			}else{
				List<Map<String, Object>> data = DebtTransfer.findUserDebtInvestReturn(Long.valueOf(parameters.get("invest_id")),userId);
				t_debt_invest debt_invest = t_debt_invest.find(" id = ? ",Long.valueOf(parameters.get("invest_id"))).first();
				t_debt_transfer transfer =  t_debt_transfer.find(" id = ? ",debt_invest.debt_id).first();

				if(transfer != null){
					result.put("period", transfer.period);
					result.put("debtAmount", transfer.debt_amount);
				}


				result.put("error", "-1");
				result.put("msg", "查询成功");
				result.put("data", data);

				Logger.info("回款计划："+JSON.toJSONString(result, true));

			}
		}

		return JSON.toJSONString(result);
	}

	/*
	 * 用户债权服务协议(opt=1048)
	 */
	public static String DebtAgreement(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		String content = News.queryContent(Constants.NewsTypeId.DEBT_AGREEMENT, error);

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);

			return JSONUtils.printObject(jsonMap);
		}

		jsonMap.put("error", "-1");
		jsonMap.put("msg","获取债权协议成功");
		jsonMap.put("content",content);

		return JSONUtils.printObject(jsonMap);
	}
	 
	public static String bidUserRisk(Map<String, String> parameters) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String bidUserRisk = "";
		try {
			bidUserRisk = BidUserRisk.bidUserRisk(error);
		} catch (Exception e) {
			Logger.error("获取标的风险评估报错！" );
			e.printStackTrace();
		}
		jsonMap.put("error", "-1");
		jsonMap.put("msg","查询成功");
		JSONObject jo=JSONObject.fromObject(jsonMap);
		jo.put("bidUserRisk", bidUserRisk);
		//jsonMap.put("bidUserRisk",bidUserRisk);
		return jo.toString();
	}

	/**
	 * 获取借款申请列表接口
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public static String getMyBorrowApplyList(Map<String, String> parameters) throws IOException{

		int currPage = 1;

		if (parameters.get("currPage") != null) {
			currPage = Integer.parseInt(parameters.get("currPage"));
		}
		ErrorInfo error = new ErrorInfo();
		Map<String, Object>  jsonMap = new HashMap<String, Object>();

		String userIdStr = parameters.get("userId");

		if(StringUtils.isBlank(userIdStr) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			return JSONUtils.printObject(jsonMap);
		}


		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

		if(error.code < 0){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "解析用户id有误");
			return JSONUtils.printObject(jsonMap);
		}

		PageBean<v_borrow_apply> applys = BorrowApply.queryMyBorrowApply(userId,currPage,Constants.APP_PAGESIZE,error);

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg",error.msg);

			return JSONUtils.printObject(jsonMap);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("error", -1);
		map.put("msg", "查询成功");
		map.put("totalNum", applys.totalCount);
		map.put("list",applys.page);
		return  JSONObject.fromObject(map).toString();
	}



	/**
	 * 借款申请接口
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String borrowApply(Map<String, String> params) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		try {
			List<Agency> agency = Agency.queryAgencys(error);
			List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);

			List<Map<String, Object>> userTypeList = new ArrayList<>();

			for (UserTypeEnum type : UserTypeEnum.values()) {
				Map<String, Object> map = new HashMap<>();
				map.put(String.valueOf(type.getCode()), type.getName());
				userTypeList.add(map);
			}

			List<Map<String, Object>> productTypeList = new ArrayList<>();

			/*
			for (ProductEnum type : ProductEnum.values()) {
				Map<String, Object> map = new HashMap<>();
				map.put(String.valueOf(type.getCode()), type.getName());
				productTypeList.add(map);
			}*/
			List<t_new_product> product_list=new t_new_product().fetchEnumList();
			for (t_new_product product:product_list) {
				if(product.borrow_app_can_use) {
					Map<String, Object> map = new HashMap<>();
					map.put(String.valueOf(product.id), product.name);
					productTypeList.add(map);
				}
			}

			jsonMap.put("error", "-1");
			jsonMap.put("msg","查询成功");
			jsonMap.put("userType", userTypeList);
			jsonMap.put("productType", productTypeList);
			jsonMap.put("purpose", purpose);

			JSONObject jo = JSONObject.fromObject(jsonMap);
			jo.put("agency", agency);

			return  jo.toString();

		} catch (Exception e) {
			Logger.error("借款申请接口报错！" );
			e.printStackTrace();
			jsonMap.put("error", "-3");
			jsonMap.put("msg","查询失败");
			return JSONUtils.printObject(jsonMap);
		}

	}

	/**
	 *    借款申请提交
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String borrowApplySubmit(Map<String, String> params) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		try {
			String userTypeId  = params.get("userTypeId");
			String agencyId = params.get("agencyId");
			String purposeId = params.get("purposeId");
			String productId = params.get("productId");
			String userIdStr = params.get("userId");
			String applyAmount = params.get("applyAmount");
			String period = params.get("period");
			String pastrAddressList= params.get("phones");
			
			if(StringUtils.isBlank(userIdStr) ){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "用户id有误");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(userTypeId) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户类型有误");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(agencyId) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "申请地区不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(purposeId) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "借款用途不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(productId) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "产品类型不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(applyAmount) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "申请金额不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if(!NumberUtil.isNumeric(applyAmount)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "申请金额必须为数字类型");
				return JSONUtils.printObject(jsonMap);
			}
			if(StringUtils.isBlank(period) ){
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "借款期限不能为空");
				return JSONUtils.printObject(jsonMap);
			}

			long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);

			if(error.code < 0){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "解析用户id有误");
				return JSONUtils.printObject(jsonMap);
			}

			if(!StringUtils.isBlank(pastrAddressList)) {
				pastrAddressList=new String(Base64.safeUrlDecode(pastrAddressList), "utf-8");
				pastrAddressList=pastrAddressList.replaceAll("[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]", "*");
				List<t_user_address_list> paramAddressList;
				try {
					paramAddressList=JSON.parseArray(pastrAddressList, t_user_address_list.class);
				}catch (Exception e) {
					Logger.debug("解析通讯录有误："+pastrAddressList);
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "解析通讯录有误!");
					return JSONUtils.printObject(jsonMap);
				}
				
				try {
					UserAddressList.pushUserAddressList(userId, paramAddressList);
				} catch (Exception e) {
					jsonMap.put("error", "-2");
					jsonMap.put("msg", e.getMessage());
					return JSONUtils.printObject(jsonMap);
				}
			}
			
			BorrowApply.addBorrowApply(userId, Integer.parseInt(userTypeId), Long.parseLong(agencyId), Long.parseLong(purposeId),
					Double.parseDouble(applyAmount), Integer.parseInt(period), Long.parseLong(productId));
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "借款申请成功！");

			return JSONUtils.printObject(jsonMap);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("借款申请接口报错！" );
			e.printStackTrace();
		}
		return JSONUtils.printObject(jsonMap);
	}

	/**
	 *  借款端 个人实名认证
	 * @param params
	 * @return
	 */
	public static String certificationPerson(Map<String, String> params) throws IOException{
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		try {
			String userIdStr = params.get("userId");
			String realName = params.get("realName");
			String idNumber = params.get("idNumber");
			String provinceId = params.get("provinceId");
			String cityId = params.get("cityId");
			String maritalId = params.get("maritalId");
			long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if (error.code < 0) {
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "用户信息错误");
				return JSONUtils.printObject(jsonMap);
			}

			User user = new User();
			user.id = userId;
			if(user.user_type != 0 && StringUtils.isNotBlank(user.realityName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "用户已经实名认证");
				return JSONUtils.printObject(jsonMap);
			}
			if (StringUtils.isBlank(realName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "真实姓名不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if (StringUtils.isBlank(maritalId)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "婚姻状况不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if (StringUtils.isBlank(provinceId)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "省份不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if (StringUtils.isBlank(cityId)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "市不能为空");
				return JSONUtils.printObject(jsonMap);
			}
			if (StringUtils.isBlank(idNumber)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "身份证不能为空");
				return JSONUtils.printObject(jsonMap);
			}

			if (!CharUtil.isChinese(realName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "真实姓名必须是中文");
				return JSONUtils.printObject(jsonMap);
			}

			if(!"".equals(IDCardValidate.chekIdCard(0, idNumber))) {

				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入正确的身份证号");
				return JSONUtils.printObject(jsonMap);
			}

			User.isIDNumberExist(idNumber, null,user.financeType, error);

			if(error.code < 0) {

				jsonMap.put("error", "-3");
				jsonMap.put("msg", "此身份证已开户，请重新输入");
				return JSONUtils.printObject(jsonMap);
			}

			String refID = Codec.UUID();
			new AuthReq().create(refID, userId, 1);
			//实名认证
			if(!Constants.IS_LOCALHOST) {
				String res = AuthenticationUtil.requestForIdNo(realName, idNumber, error);

				if(error.code < 0) {
					jsonMap.put("error", "-3");
					jsonMap.put("msg", error.msg);
					//添加错误记录
					UserActions.insertAction(userId, 1,error.msg, error);

					return JSONUtils.printObject(jsonMap);
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
					Map<String, Object> tt = AuthenticationUtil.extractMultiXMLResult(resXml, 0);
					if (!"1".equals((String)tt.get("result"))){
						jsonMap.put("error", "-3");
						jsonMap.put("msg", "姓名与身份证号码不一致");

						//添加错误记录
						UserActions.insertAction(userId, 1,"姓名与身份证号码不一致", error);

						return JSONUtils.printObject(jsonMap);
					}
				}else{
					jsonMap.put("error", "-3");
					jsonMap.put("msg", "身份认证系统出错");

					//添加错误记录
					UserActions.insertAction(userId, 1,"身份认证系统出错", error);

					return JSONUtils.printObject(jsonMap);
				}
			}
			//借款端实名认证，app端传值的cityId为居住地
			user.updateBorrowCertification(realName, idNumber,Integer.parseInt(maritalId),Integer.parseInt(cityId), user.id, error);
			if(error.code < 0){
				jsonMap.put("error", -3);
				jsonMap.put("msg", "认证失败");
			}else{
				jsonMap.put("error", -1);
				jsonMap.put("msg", "认证成功");
				//实名认证发红包
				RedPackageHistory.sendAuthRed(user);

				//实名认证后添加地址
				//截取身份证前6位
				String province = idNumber.substring(0, 2);
				String city = idNumber.substring(0, 4);
				province = province + "0000"; //补全省 ，6位数
				city = city + "00"; //补全区 ，6位数

				t_user_city userCity = new t_user_city();
				userCity.city_id = city;
				userCity.province_id = province;
				userCity.user_id = user.id;
				UserCitys.addUserCity(error, userCity);
			}
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("个人实名认证出错！" );
			e.printStackTrace();
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "个人实名认证出错！");
		}
		return JSONUtils.printObject(jsonMap);
	}


	public static String certificationCompany(Map<String, String> params) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo  error = new ErrorInfo();
		String companyName = params.get("companyName");
		String creditCode = params.get("creditCode");
		String bankName = params.get("bankName");
		String bankNo = params.get("bankNo");
		String realName = params.get("realName");
		String authType = params.get("authType");
		String provinceId = params.get("provinceId");
		String cityId = params.get("cityId");
		String userIdStr = params.get("userId");
		try {
			if(StringUtils.isBlank(userIdStr) ){
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "用户id有误");
				return JSONUtils.printObject(jsonMap);
			}

			long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if (error.code < 0) {
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "用户信息错误");
				return JSONUtils.printObject(jsonMap);
			}

			CompanyUserAuthReviewBusiness.addOrUpdateAuthReview(companyName,creditCode,bankName,bankNo,realName,
					StringUtils.isEmpty(authType)? null:Integer.parseInt(authType),
					StringUtils.isEmpty(provinceId)? null:Integer.parseInt(provinceId),
					StringUtils.isEmpty(cityId)? null:Integer.parseInt(cityId),userId,error);

			if(error.code < 0) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", error.msg);
				return JSONUtils.printObject(jsonMap);
			}
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "企业实名认证成功");
			return JSONUtils.printObject(jsonMap);

		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("企业实名认证失败！" );
			e.printStackTrace();
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			return JSONUtils.printObject(jsonMap);
		}


	}

	public static String getNewArea(Map<String, String> params) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		List<t_new_province> provices = NewProvince.getProvinceList();

		for (t_new_province provice : provices) {
			List<t_new_city> citys = NewProvince.getCityList(provice.province_id);
			provice.citys = citys;
		}
		jsonMap.put("area", provices);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功！");
		return JSONUtils.printObject(jsonMap);
	}

/*	public static String getNewCity(Map<String, String> params) throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();

		List<t_new_city> city = t_new_city.findAll();
		jsonMap.put("cityList", city);
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "查询成功！");
		return JSONUtils.printObject(jsonMap);
	}
*/

	/**
	 * 借款app首页
	 * @param parameters
	 * @return
	 */
	public static String borrowHome(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();

		List<t_content_advertisements> homeAds = Ads.queryAdsByTag("借款APPBanner", error); // 广告条

		if(error.code < 0){
			jsonMap.put("error", "-4");
			jsonMap.put("msg", "查询广告条失败");

			return JSONObject.fromObject(jsonMap).toString();
		}

		List<t_content_advertisements> noticeAds = Ads.queryAdsByTag("公告", error); // 广告条-公告
		List<t_content_advertisements> popAds = Ads.queryAdsByTag("app插屏", error); // 广告条-插屏
		jsonMap.put("noticeAds", noticeAds);
		jsonMap.put("popAds", popAds);

		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		jsonMap.put("homeAds", homeAds);

		return JSONObject.fromObject(jsonMap).toString();

	}

	/**
	 * 获取借款APP版本
	 * @param parameters
	 * @return
	 */
	public static String borrowAppVersion(Map<String, String> parameters){
		Map<String,Object> jsonMap = new HashMap<String, Object>();

		String deviceTypeStr = parameters.get("deviceType");

		if(StringUtils.isBlank(deviceTypeStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入设备参数");

			return JSONObject.fromObject(jsonMap).toString();
		}

		if(!NumberUtil.isNumeric(parameters.get("deviceType"))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请传入正确的设备参数");

			return JSONObject.fromObject(jsonMap).toString();
		}

		int deviceType = Integer.parseInt(parameters.get("deviceType"));

		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		jsonMap.put("version", deviceType == 1 ? backstageSet.borrowAndroidVersion : backstageSet.borrowIosVersion);
		jsonMap.put("isForceAppUpdate",  deviceType == 1 ? backstageSet.borrowAndroidForcedUpdate : backstageSet.borrowIosForcedUpdate);
		jsonMap.put("code", deviceType == 1 ? backstageSet.borrowAndroidCode : backstageSet.borrowIosCode);
		jsonMap.put("path", deviceType == 1 ? "/public/yiyilicai_finance.apk" : "/public/yiyilicai_finance.apk");
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");

		return JSONObject.fromObject(jsonMap).toString();
	}

	/**
	 *  用户基本信息
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String userBasicInfo(Map<String, String> params) throws IOException {
		Map<String,Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = params.get("userId");
		String userType = params.get("userType");

		if(StringUtils.isBlank(userIdStr) ){
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户id有误");
			return JSONUtils.printObject(jsonMap);
		}
		if(StringUtils.isBlank(userType) ){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "用户类型不能为空");
			return JSONUtils.printObject(jsonMap);
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		t_user_city userCity = t_user_city.find(" user_id = ? ", userId).first();

		t_users user = t_users.findById(userId);
		Map<String, Object> company = new HashMap<>();
		if(StringUtils.isNotBlank(user.reality_name)) { //已经实名
		    if(user.user_type == UserTypeEnum.PERSONAL.getCode()) { //个人账户
		    	Map<String, Object> userInfo = new HashMap<>();
				userInfo.put("realityName", user.reality_name);
				userInfo.put("maritalId", user.marital_id);
				userInfo.put("idNumber", user.id_number);
				jsonMap.put("userInfo", userInfo);
				
		    } else { //企业和个体工商户
		    	t_user_bank_accounts accounts = t_user_bank_accounts.find(" user_id =? ", userId).first();
		    	company.put("real_name", accounts.account_name);
		    	company.put("company_name", user.reality_name);
		    	company.put("credit_code", user.id_number);
		    	company.put("bank_name", accounts.bank_name);
		    	company.put("bank_no", accounts.account);
		    	jsonMap.put("company", company);
		    }

		} else { //未实名
			t_user_auth_review auth = t_user_auth_review.find("user_id = ? and status = 0  ", userId).first();
			jsonMap.put("company", auth);
		}
		if(userCity != null) {
			jsonMap.put("area", StringUtils.isNotBlank(userCity.province) ? userCity.province:"" + (StringUtils.isNotBlank(userCity.city) ? userCity.city:""));
		}
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * APP端用户启动图
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String getBorrowStartMap(Map<String, String> parameters) throws IOException {
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		
		List<String> fileNames = Ads.queryAdsImageNamesByLocation(Constants.STARTUP_BOOT_APP_BORROW, error);
		
		if(error.code < 0) {
			jsonMap.put("error", -2);
			jsonMap.put("msg", error.msg);
			
			return JSONUtils.printObject(jsonMap);
		}
		
		jsonMap.put("error", -1);
		jsonMap.put("fileNames", fileNames);
		
		// 是否自动登录
		//jsonMap.put("isAutoLogin", Play.configuration.getProperty("app_is_auto_login", "1"));
		
		return JSONUtils.printObject(jsonMap);
	}
	/**
	 * 获取ios版本审核状态,是否通过
	 * @param parameters
	 * @return
	 * @author Sxy
	 */
	public static String getIosAudit(Map<String, String> parameters){
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String appTypeStr = parameters.get("appType");
		
		if(StringUtils.isBlank(appTypeStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入app类型参数");
			
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		/*if(StringUtils.isNumeric(appTypeStr)){
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入正确的app类型参数");
			
			return JSONObject.fromObject(jsonMap).toString();
		}*/
		
		int appType = Integer.parseInt(appTypeStr);
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		jsonMap.put("iosAudit", appType == 1 ? backstageSet.iosAudit : backstageSet.borrowIosAudit);
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 *  基础信息枚举
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static String basicInfoEnums(Map<String, String> params)  throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		//加载用户居住地所在省市数据  user.cityId
	 
		List<t_new_province> provices = NewProvince.getProvinceList();

		for (t_new_province provice : provices) {
			List<t_new_city> citys = NewProvince.getCityList(provice.province_id);
			provice.citys = citys;
		}
		//--------------------------------------------enums----------------------------------------------- 
		List<t_dict_cars> cars = t_dict_cars.findAll();
		List<t_dict_educations> educations = t_dict_educations.findAll();
		List<t_dict_houses> houses = t_dict_houses.findAll();
		List<t_dict_maritals> maritals = t_dict_maritals.findAll();
		List<t_dict_relation> relation = t_dict_relation.findAll();
		
	 
		jsonMap.put("area", provices);
		jsonMap.put("cars", cars);
		jsonMap.put("educations", educations);
		jsonMap.put("houses", houses);
		jsonMap.put("maritals", maritals); 
		jsonMap.put("relations", relation); 
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		return JSONObject.fromObject(jsonMap).toString();	
	}
	
	public static String creditApplyUserInfoSubmit(Map<String, String> params)  throws IOException{
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		ErrorInfo error = new ErrorInfo();
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONUtils.printObject(jsonMap);
		}
		
		String educationId = params.get("educationId");
		if(StringUtils.isEmpty(educationId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择学历！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String maritalId = params.get("maritalId");
		if(StringUtils.isEmpty(maritalId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择婚姻状况！");
			return JSONObject.fromObject(jsonMap).toString();
		}

		String workIndustry = params.get("workIndustry");//工作行业0自由职业者；1其他行业
		if(StringUtils.isEmpty(workIndustry)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选工作行业！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String cityId = params.get("cityId");
		String companyCityId =params.get("companyCityId");
		String companyName = params.get("companyName");
		String salary = params.get("salary");
		String accumulationFund = params.get("accumulationFund");
		if(Integer.parseInt(workIndustry) == 1) { //选择其他行业则显示：工作城市；公司全称；月工资；公积金汇缴数额
			if(StringUtils.isEmpty(companyCityId)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请选择工作城市！");
				return JSONObject.fromObject(jsonMap).toString();
			}
			if(StringUtils.isEmpty(companyName)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入公司全称！");
				return JSONObject.fromObject(jsonMap).toString();
			}
			if(StringUtils.isEmpty(salary)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入薪水！");
				return JSONObject.fromObject(jsonMap).toString();
			}
			if(StringUtils.isEmpty(accumulationFund)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入公积金！");
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		if(StringUtils.isEmpty(cityId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择居住城市！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String houseId = params.get("houseId");
		String rent = params.get("rent");
		if(StringUtils.isEmpty(houseId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择购房情况！");
			return JSONObject.fromObject(jsonMap).toString();
		}  
		if(Integer.parseInt(houseId) == 10) {//租房
			if(StringUtils.isEmpty(rent)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "请输入租房房租！");
				return JSONObject.fromObject(jsonMap).toString();
			}
		}
		String qq = params.get("QQ");
		if(StringUtils.isEmpty(qq)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入QQ！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String carId = params.get("carId");
		if(StringUtils.isEmpty(carId)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请选择购车情况！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		//居住地址
		String address = params.get("address");  
		if(StringUtils.isEmpty(address)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入居住地址！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		//常用联系人
		String relationId1 = params.get("relationId1");
		if(StringUtils.isEmpty(relationId1)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人1！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String mobile1 = params.get("mobile1");
		if(StringUtils.isEmpty(mobile1)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人1联系方式！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String realName1 = params.get("realName1");
		if(StringUtils.isEmpty(realName1)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人1真实姓名！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		//常用联系人
		String relationId2 = params.get("relationId2");//与用户关系
		if(StringUtils.isEmpty(relationId2)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人2！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String mobile2 = params.get("mobile2");
		if(StringUtils.isEmpty(mobile2)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人2联系方式！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String realName2 = params.get("realName2");
		if(StringUtils.isEmpty(realName2)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "请输入常用联系人真实姓名！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(StringUtils.trim(mobile2).equals(StringUtils.trim(mobile1))) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "常用联系人1和常用联系人2不能相同！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		//解析通讯录
		String pastrAddressList= params.get("phones");
		if(StringUtils.isEmpty(pastrAddressList)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "联系人有误！");
			return JSONUtils.printObject(jsonMap);
		}
		
		if(!StringUtils.isBlank(pastrAddressList)) {
			pastrAddressList=new String(Base64.safeUrlDecode(pastrAddressList), "utf-8");
			//D800..0xDBFF
			//0xDC00..0xDFFF
			pastrAddressList=t_users.userName4ByteToStar(pastrAddressList);
			List<t_user_address_list> paramAddressList;
			try {
				paramAddressList=JSON.parseArray(pastrAddressList, t_user_address_list.class);
			}catch (Exception e) {
				Logger.debug("解析通讯录有误："+pastrAddressList);
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "解析通讯录有误!");
				return JSONUtils.printObject(jsonMap);
			}
			
			try {
				UserAddressList.pushUserAddressList(userId, paramAddressList);
			} catch (Exception e) {
				jsonMap.put("error", "-2");
				jsonMap.put("msg", e.getMessage());
				return JSONUtils.printObject(jsonMap);
			}
		}
		
		BorrowerInfoService.saveUserInfo(params, userId,error);
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "保存失败");
			JPA.setRollbackOnly();
			return JSONObject.fromObject(jsonMap).toString();	
		}
		jsonMap.put("error", -1);
		jsonMap.put("msg", "保存成功");
		return JSONObject.fromObject(jsonMap).toString();	
	}

	/**
	 * 亿美贷额度申请接口
	 * userId 用户ID
	 * orgId 医院机构ID
	 * applyProjectIds 医院申请项目详细ID，多个用英文,逗号分割
	 * itemsAmount 项目总金额
	 * applyCreditAmount 使用金额
	 * period 期数
	 * @param params
	 * @return
	 */
	public static String creditApply(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		User user = new User();
		user.id = userId;
		
		if(StringUtils.isEmpty(user.instance.reality_name)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户未完成实名认证！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(StringUtils.isEmpty(user.instance.id_number)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户未完成实名认证！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(user.instance.id_picture_authentication_status != 1) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户未完成实名认证！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(user.instance.living_authentication_status != 1) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户未完成活体认证！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(!Score.hasBank(userId)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户未绑卡！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		/*	
		user.instance.reality_name 不为空,
		user.instance.id_number 不为空,
		user.instance.id_picture_authentication_status==1;
		user.instance.living_authentication_status==1;
		Score.hasBank(user_id)==true;
		*/
		
		String orgIdStr = params.get("orgId");//医院机构ID;
		if(StringUtils.isEmpty(orgIdStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "医院机构ID不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		Long orgId = Long.valueOf(orgIdStr);
		
		String applyProjectIds = params.get("applyProjectIds");//申请项目ID,多个用英文,逗号分割
		if(StringUtils.isEmpty(applyProjectIds)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请项目ID不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		String itemsAmountStr = params.get("itemsAmount");//项目总金额
		if(StringUtils.isEmpty(itemsAmountStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "项目总金额不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		double itemsAmount = Double.parseDouble(itemsAmountStr);
		
		String applyCreditAmountStr = params.get("applyCreditAmount");//使用金额
		if(StringUtils.isEmpty(applyCreditAmountStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请金额不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		double applyCreditAmount = Double.parseDouble(applyCreditAmountStr);
		
		Integer period = Integer.parseInt(params.get("period"));//期数
		BigDecimal apr = null;
		 
		try {
			CreditApplyService.creditApply(userId,orgId, applyProjectIds,period,
					 applyCreditAmount, itemsAmount,error);
			
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			jsonMap.put("error", -3);
			jsonMap.put("msg", e.getMessage());
			return  JSONObject.fromObject(jsonMap).toString();	
		}
		if(error.code < 0) {
			JPA.setRollbackOnly();
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			return JSONObject.fromObject(jsonMap).toString();	
		}
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "申请成功！");
		return JSONObject.fromObject(jsonMap).toString();	
	}
	
	

	/**
	 *  用户使用额度
	 * userId 用户ID
	 * applyId 申请ID
	 * @param params
	 * @return
	 */
	public static String useCredit(Map<String, String> params) {
		Gson gson=new Gson();
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return gson.toJson(jsonMap);
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return gson.toJson(jsonMap);
		}
		
		String applyIdStr = params.get("applyId");//申请ID;
		if(StringUtils.isEmpty(applyIdStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请ID不能为空！");
			return gson.toJson(jsonMap);
		}
		Long applyId = Long.valueOf(applyIdStr);
		
		try {
			Map<String, Object> map = CreditApplyService.useCredit(userId,applyId,error);
			//使用额度时 查询fileRelationType.6 borrow_apply 借款申请需要上传的资料类型
			int fileRelationType = 6;
			List<t_file_relation_dict> borrowApplyFileDictList=FileService.getFileDictByRelationType(fileRelationType);
			BigDecimal useCredit=CreditApplyService.getUseCreditByCreditApplyId(applyId);
			
			jsonMap.put("debitCreditPath", Constants.NewsTypeId.DEBIT_CREDIT_ID);//借款协议
			jsonMap.put("disclosureCommitmentPath", Constants.NewsTypeId.DISCLOSURE_COMMITMENT_ID);//风险揭示书及禁止行为承诺
			jsonMap.put("signatureAuthorizationPath", Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID);//电子签章自动签署授权协议
			jsonMap.put("lenderServicePath", Constants.NewsTypeId.LENDER_SERVICE_ID);//出借人服务协议
			
			jsonMap.put("borrowApplyFileDictList", borrowApplyFileDictList);
			jsonMap.put("apply", map.get("apply"));
			jsonMap.put("useCredit", useCredit);
			jsonMap.put("orgList", map.get("orgList"));
		} catch (Exception e) {
			e.printStackTrace();
			jsonMap.put("error", -3);
			jsonMap.put("msg", "下单失败！");
			return gson.toJson(jsonMap);
		}
		jsonMap.put("error", -1);
		jsonMap.put("msg", "查询成功");
		return gson.toJson(jsonMap);
	}
	
	/**
	 * 亿美贷下单接口
	 * userId 用户ID
	 * applyId 申请ID
	 * useAmount 使用额度
	 * @param params
	 * @return
	 */
	public static String submitCreditApply(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String applyIdStr = params.get("applyId");//申请ID;
		if(StringUtils.isEmpty(applyIdStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "申请ID不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		Long applyId = Long.valueOf(applyIdStr);
		
		String useAmountStr = params.get("useAmount");//使用额度
		if(StringUtils.isEmpty(useAmountStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "使用金额不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
	
		BigDecimal useAmount = new BigDecimal(useAmountStr);

		String imgs = params.get("imgs");
		if(StringUtils.isEmpty(imgs)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "图片信息不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		Logger.info("imgs=========>"+imgs);
		String relationTypeStr = params.get("relationType");
		if(StringUtils.isEmpty(relationTypeStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "图片类型不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		try {
			t_borrow_apply borrowApply = CreditApplyService.createYMDBorrowApply(userId,applyId,useAmount,error);
			if(error.code < 0) {
				jsonMap.put("error", -3);
				jsonMap.put("msg", error.msg);
				return  JSONObject.fromObject(jsonMap).toString();	
			}
			
			FileHelperService.saveImageInfo(imgs, borrowApply.id, Integer.parseInt(relationTypeStr));
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			jsonMap.put("error", -3);
			jsonMap.put("msg", "下单失败！");
			return  JSONObject.fromObject(jsonMap).toString();	
		}
		
		
		jsonMap.put("error", -1);
		jsonMap.put("msg", "下单成功！");
		return  JSONObject.fromObject(jsonMap).toString();	
	}
	/**
	 * 亿美贷机构列表
	 * @param params
	 * @return
	 * @author Sxy
	 * @throws Exception 
	 */
	public static String ymdOrganizationList(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		Boolean isUse = true;
		PageBean<v_organization>  orgPage=null;
		try {
			orgPage = OrganizationDao.getOrganizationList(null, null, null, null, null, null, isUse, error);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			jsonMap.put("error", "-2");
			jsonMap.put("msg", e.getMessage());
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","查询成功");
		jsonMap.put("orgList", orgPage.page);
		return  JSONObject.fromObject(jsonMap).toString();
		
	}
	
	/**
	 * 亿美贷机构项目
	 * @param params
	 * @return
	 * @author Sxy
	 */
	public static String ymdOrgProject(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String orgIdStr = params.get("orgId");
		
		if(StringUtils.isEmpty(orgIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "机构Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long orgId = Long.parseLong(orgIdStr);
		
		List<t_org_project> orgProject = OrganizationService.getOrgProject(orgId);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","查询成功");
		jsonMap.put("orgProject", orgProject);
		
		return new Gson().toJson(jsonMap).toString();
	}
	
	/**
	 * 机构项目利息
	 * @param params
	 * @return
	 * @author Sxy
	 */
	public static String ymdProInterestAndServiceRate(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String orgIdStr = params.get("orgId");
		String repaymentTypeStr = params.get("repaymentType");
		String productIdStr = params.get("productId");
		String periodUnitStr = params.get("periodUnit");
		
		if(StringUtils.isEmpty(orgIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "机构Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		if(StringUtils.isEmpty(repaymentTypeStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "还款方式类型有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		long orgId = Long.parseLong(orgIdStr);
		long repaymentType = Long.parseLong(repaymentTypeStr);
		long product_id=4;
		long period_unit=0;
		
		List<t_interest_rate> interestRateList = OrganizationService.getInterestRate(product_id,orgId,repaymentType,period_unit);
		if(interestRateList.size()==0){
			interestRateList = OrganizationService.getInterestRate(product_id,-1L,repaymentType,period_unit);
		}
		List<t_service_cost_rate> serviceCostRateList = OrganizationService.getServiceCostRate(product_id,orgId,period_unit);
		List<t_service_cost_rate> defultServiceCostRateList = OrganizationService.getServiceCostRate(product_id,-1L,period_unit);
		OrganizationService.installInterestRateList(interestRateList,serviceCostRateList,defultServiceCostRateList);
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","查询成功");
		jsonMap.put("interestRate", interestRateList);
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	/**
	 * 4007
	 * userId 用户ID
	 * creditApplyId 额度申请ID
	 * increaseAmount 提额金额
	 * imgs 图片
	 * relationType 图片类型
	 * @param params
	 * @return
	 */
	public static String increaseCredit(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		String creditApplyIdStr = params.get("creditApplyId");
		if(StringUtils.isEmpty(creditApplyIdStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "额度申请ID不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		Long  creditApplyId = Long.valueOf(creditApplyIdStr);
		
		String increaseAmountStr = params.get("increaseAmount");
		if(StringUtils.isEmpty(increaseAmountStr)) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", "提额金额不能为空！");
			return JSONObject.fromObject(jsonMap).toString();
		}
		BigDecimal increaseAmount = new BigDecimal(increaseAmountStr);
		
		String images = params.get("imgs");
		Logger.info("imgs=========>" + images);
		String relationTypeStr = params.get("relationType");
		int relationType = 0;
		if(!StringUtils.isEmpty(images)) {
			if(StringUtils.isEmpty(relationTypeStr)) {
				jsonMap.put("error", "-3");
				jsonMap.put("msg", "图片类型不能为空！");
				return JSONObject.fromObject(jsonMap).toString();
			}
			
			relationType = Integer.parseInt(relationTypeStr);
		}
		CreditApplyService.increaseCreditApply(creditApplyId, increaseAmount, images, relationType, error);
		
		if(error.code < 0) {
			jsonMap.put("error", "-3");
			jsonMap.put("msg", error.msg);
			JPA.setRollbackOnly();
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg","提额成功");
		
		return JSONObject.fromObject(jsonMap).toString();
	}
	
	
	/**
	 * @Description app端选择图片并保存提交
	 * @param saveResultJson app端保存结果
	 * @return
	 * @author: zj
	 */
	public static String creditApplyUserFileSubmit(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		
		String userIdStr = params.get("userId");
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		
		try {
			t_users user=t_users.findById(userId);
			user.credit_apply_file_status=1;
			user.credit_apply_file_time=new Date();
			user.save();
			String imgs = params.get("imgs");
			Logger.info("imgs=========>" + imgs);
			int relationType = Integer.parseInt(params.get("relationType"));

			FileHelperService.saveImageInfo(imgs, userId, relationType);
			jsonMap.put("error", "-1");
			jsonMap.put("msg", "成功");
			return JSON.toJSONString(jsonMap);
		} catch (Exception e) {
			JPA.setRollbackOnly();
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			Logger.info(e.getMessage(), e);
			return JSON.toJSONString(jsonMap);
		}
	}
	
	/**
	 *  扣款签约接口
	 * @author wangyun
	 * @date 2019年4月2日
	 * @param params
	 * @return
	 */
	public static String signProtocal(Map<String, String> params) {
		ErrorInfo error = new ErrorInfo();
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		String userIdStr = params.get("userId");
		
		if(StringUtils.isEmpty(userIdStr)) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户Id有误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		long userId = Security.checkSign(userIdStr, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if (error.code < 0) {
			jsonMap.put("error", "-2");
			jsonMap.put("msg", "用户信息错误");
			return JSONObject.fromObject(jsonMap).toString();
		}
		try {
			UserBankAccounts.updateBankIsSign(userId,error);
			if(error.code < 0) {
				jsonMap.put("error", "-2");
				jsonMap.put("msg", "扣款签约失败！");
				return JSONObject.fromObject(jsonMap).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			jsonMap.put("error", "0");
			jsonMap.put("msg", "失败");
			return JSON.toJSONString(jsonMap);
		}
		
		jsonMap.put("error", "-1");
		jsonMap.put("msg", "成功");
		return JSON.toJSONString(jsonMap);
	}
}
