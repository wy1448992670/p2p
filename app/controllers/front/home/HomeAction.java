package controllers.front.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_bid_publish;
import models.t_content_advertisements;
import models.t_content_advertisements_partner;
import models.t_content_news;
import models.t_content_news_types;
import models.v_bill_board;
import models.v_front_all_bids;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import annotation.LoginCheck;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.jpa.Blob;
import play.mvc.With;
import play.mvc.Scope.Session;
import utils.Arith;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import business.Ads;
import business.AdsEnsure;
import business.AdsPartner;
import business.Agency;
import business.AuditItem;
import business.BackstageSet;
import business.Bid;
import business.Bid.Purpose;
import business.Bid.Repayment;
import business.Bill;
import business.BorrowApply;
import business.CreditLevel;
import business.Invest;
import business.News;
import business.NewsType;
import business.Product;
import business.User;
import constants.Constants;
import constants.FinanceTypeEnum;
import constants.OptionKeys;
import constants.ProductEnum;
import constants.UserTypeEnum;
import controllers.BaseController;
import controllers.SubmitRepeat;
import controllers.interceptor.AccountInterceptor;

/**
 * 
 * @author liuwenhui
 *
 */
public class HomeAction extends BaseController{

	/*网站首页*/
	public static void home(){
		redirect("/");
		
		/**
		ErrorInfo error = new ErrorInfo();
				
		List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条
		
		List<v_front_all_bids> bidLEList = Invest.queryBidsByProductid(2);//首页最新3个信易贷
		List<v_front_all_bids> bidCHList = Invest.queryBidsByProductid(1);//首页最新3个车房贷
		List<v_front_all_bids> bidCashList = Invest.queryBidsByProductid(3);//首页最新3个变现宝
		
		List<v_front_all_bids> agencyBids = Invest.queryAgencyBids();//机构借款标
		
		List<v_front_all_bids> memberBids = Invest.queryMemberBids();//前台显示的2个会员贷
		
		List<v_bill_board> investBillboard = Invest.investBillboard();//理财风云榜
		
		List<t_content_news> successStorys = News.queryNewForFront(12l, 2, error) ;//首页成功故事
		
		List<t_content_news> investSkills = News.queryNewForFront(10l, 5, error) ;//首页借款技巧
		
		List<t_content_news> loanSkills = News.queryNewForFront(11l, 5, error) ;//首页理财技巧
		
		List<t_content_news> news = News.queryNewForFront(7l, 5, error) ;//首页官方公告
		
		List<Bid> bids = Bid.queryAdvertisement(error); // 最新投资资讯
		
		List<Map<String,String>> maps = Invest.queryNearlyInvest(error);
		
		List<AdsEnsure> adsEnsure = AdsEnsure.queryEnsureForFront(error); //四大安全保障
		
		List<AdsPartner>  adsPartner = AdsPartner.qureyPartnerForFront(error);//合作伙伴
		
		List<NewsType> types = NewsType.queryChildTypes(1, error);
	  
		List<Map<String, Object>> bidPublishs = User.queryBidsPublishListForFront(false);//查询投标公告
		
		List<t_content_advertisements_partner> partnerList = News.queryPartners(error);
				
		render(homeAds, bidLEList,bidCHList,bidCashList,agencyBids,investBillboard,successStorys,investSkills,loanSkills,news, 
				bids, adsEnsure, adsPartner, types ,maps,memberBids,bidPublishs,partnerList);
				*/
	}
	/**
	 * 借款端首页
	 */
	public static void LoanIndex(int id){
		/*ErrorInfo error = new ErrorInfo();
				//1. 借款人服务协议BORROWER_SERVICE_ID
				String service = News.queryContent(Constants.NewsTypeId.BORROWER_SERVICE_ID, error);
				service = service.replaceAll("\\n|\\r", "");
				// 2. //电子签章自动签署授权协议
				String authorization = News.queryContent(Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID, error);
				if(authorization !=null) {
					authorization = authorization.replaceAll("\\n|\\r", "");
				}
			
				// 3.咨询与管理服务协议 
				String manage  = News.queryContent(Constants.NewsTypeId.CONSULT_MANAGE_ID, error);
				manage = manage.replaceAll("\\n|\\r", "");*/
		
		render();
	}
/*	*//**
	 * 申请借款页
	 *//*
	public static void applyLoan(int id){
		ErrorInfo error = new ErrorInfo();
				//1. 借款人服务协议BORROWER_SERVICE_ID
				String service = News.queryContent(Constants.NewsTypeId.BORROWER_SERVICE_ID, error);
				service = service.replaceAll("\\n|\\r", "");
				// 2. //电子签章自动签署授权协议
				String authorization = News.queryContent(Constants.NewsTypeId.SIGNATURE_AUTHORIZATION_ID, error);
				authorization = authorization.replaceAll("\\n|\\r", "");
				// 3.咨询与管理服务协议 
				String manage  = News.queryContent(Constants.NewsTypeId.CONSULT_MANAGE_ID, error);
				manage = manage.replaceAll("\\n|\\r", "");
		
		render(service, authorization, manage);
	}*/
	
	public static void banner(){
		//ErrorInfo error = new ErrorInfo();
		//List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE, error); // 广告条
		
		//renderJSON(homeAds);
	}
	
	/**
	 * 财富工具箱
	 */
	public static void wealthToolkit(int key){
		ErrorInfo error = new ErrorInfo();
		List<Product> products = Product.queryProductNames(true, error);
		
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		render(key, products, creditLevels);
	}
	
	/**
	 * 信用计算器
	 */
	public static void wealthToolkitCreditCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = AuditItem.queryAuditItems(error);
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);
		
		render(auditItems, amountKey);
	}
	
	/**
	 * 还款计算器
	 */
	public static void wealthToolkitRepaymentCalculator(){
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, new ErrorInfo()); // 还款类型
		
		render(rtypes);
	}
	
	/**
	 * 还款明细(异步)
	 */
	public static void repaymentCalculate(double amount, double apr, int period, int periodUnit, int repaymentType){
		List<Map<String, Object>> payList = null;
		
		payList = Bill.repaymentCalculate(amount, apr, period, periodUnit, repaymentType);
		
		render(payList);
	}
	
	/**
	 * 净值计算器
	 */
	public static void wealthToolkitNetValueCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		double bailScale = Product.queryNetValueBailScale(error); // 得到净值产品的保证金比例
		
		render(bailScale);
	}
	
	/**
	 * 利率计算器
	 */
	public static void wealthToolkitAPRCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error); // 还款类型
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double serviceFee = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

		render(rtypes, serviceFee);
	}
	
	/**
	 * 利率计算器,计算年华收益、总利益(异步)
	 */
	public static void aprCalculator(double amount, double apr,int repaymentType,double award,int rperiod){
		ErrorInfo error = new ErrorInfo();
		DecimalFormat df = new DecimalFormat("#.00");
		
		double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;//系统管理费费率
		double earning = 0;


		if(repaymentType == 1){/* 按月还款、等额本息 */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;
			double val1 = amount * monRate * Math.pow((1 + monRate), monTime);
			double val2 = Math.pow((1 + monRate), monTime) - 1;
			double monRepay = val1 / val2;// 每月偿还金额
			
			/**
			 * 年化收益
			 */
			 earning = Arith.excelRate((amount - award),
					Double.parseDouble(df.format(monRepay)), monTime, 200, 15)*12*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		if(repaymentType == 2 || repaymentType == 3){ /* 按月付息、一次还款   */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;// * 12;借款期限填月
			double borrowSum = Double.parseDouble(df.format(amount));
			double monRepay = Double.parseDouble(df.format(borrowSum * monRate));// 每月偿还金额
			double allSum = Double.parseDouble(df.format((monRepay * monTime)))
					+ borrowSum;// 还款本息总额
			 earning = Arith.rateTotal(allSum,
					(borrowSum - award), monTime)*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		
		JSONObject obj = new JSONObject();
		obj.put("managementRate", managementRate < 0 ? 0 : managementRate); 
		obj.put("earning", earning); 
		
		renderJSON(obj);
	}



	/**
	 * 利率计算器,计算年华收益、总利益(异步)
	 */
	public static void aprCalculator2(double amount, double apr,double apr1, double apr2, double apr3, int repaymentType,double award,int rperiod,int periodUnit){
		ErrorInfo error = new ErrorInfo();
		DecimalFormat df = new DecimalFormat("#.00");
		// periodUnit=0;
		double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;//系统管理费费率

		JSONObject obj = new JSONObject();
		obj.put("managementRate", managementRate < 0 ? 0 : managementRate);

		if (apr > 0) {
			BigDecimal normal_interest = BigDecimal.ZERO;
			List<Map<String, Object>> normal = Bill.repaymentCalculate(amount, apr, rperiod, periodUnit, repaymentType, 0);
			for (Map<String, Object> map : normal) {
				normal_interest = normal_interest.add(new BigDecimal(map.get("monPayInterest") + ""));
			}
			obj.put("earning", normal_interest.doubleValue());
		}else{
			obj.put("earning",0d);
		}

		if (apr1 > 0) {
			BigDecimal increase_interest1 = BigDecimal.ZERO;
			List<Map<String, Object>> increase1 = Bill.repaymentCalculate(amount, apr, rperiod, periodUnit, repaymentType, apr1);
			for (Map<String, Object> map : increase1) {
				increase_interest1 = increase_interest1.add(new BigDecimal(map.get("monPayIncreaseInterest") + ""));
			}
			obj.put("earning1", increase_interest1.doubleValue());
		}else{
			obj.put("earning1",0d);
		}
		if (apr2 > 0) {
			BigDecimal increase_interest2 = BigDecimal.ZERO;
			List<Map<String, Object>> increase2 = Bill.repaymentCalculate(amount, apr1>0?apr+apr1:apr1, rperiod, periodUnit, repaymentType, apr2);
			for (Map<String, Object> map : increase2) {
				increase_interest2 = increase_interest2.add(new BigDecimal(map.get("monPayIncreaseInterest") + ""));
			}
			obj.put("earning2", increase_interest2.doubleValue());
		}else{
			obj.put("earning2",0d);
		}
		if (apr3 > 0) {
			BigDecimal increase_interest3 = BigDecimal.ZERO;
			List<Map<String, Object>> increase3 = Bill.repaymentCalculate(amount, apr1>0?apr+apr1:apr1, rperiod, periodUnit, repaymentType, apr3);
			for (Map<String, Object> map : increase3) {
				increase_interest3 = increase_interest3.add(new BigDecimal(map.get("monPayIncreaseInterest") + ""));
			}
			obj.put("earning3", increase_interest3.doubleValue());
		}else{
			obj.put("earning3",0d);
		}

		renderJSON(obj);
	}
	
	/**
	 * 服务手续费
	 */
	public static void wealthToolkitServiceFee(){
		ErrorInfo error = new ErrorInfo();
		String content = News.queryContent(-1011L, error);
		flash.error(error.msg);
		
		renderText(content);
	}
	
	/**
	 * 超额借款
	 */
	public static void wealthToolkitOverLoad(){
		ErrorInfo error = new ErrorInfo();
		
		List<AuditItem> auditItems = AuditItem.queryAuditItems(error);
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double amountKey = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);
		
		render(auditItems, amountKey);
	}
	
	/**
	 * 新手入门
	 */
	public static void getStart(int id){
		ErrorInfo error = new ErrorInfo();
		
		String content = News.queryContentById(id, error);
		
		List<Product> products = Product.queryProductNames(true, error);
		
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		List<t_content_news_types> newsTypes = NewsType.queryNewsTypeByPid(id);
		
		render(content, products, creditLevels, id, newsTypes);              
	}
	
	/**
	 * 关于我们
	 */
	public static void aboutUs(int id){
		ErrorInfo error = new ErrorInfo();

		List<Product> products = Product.queryProductNames(true, error);
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		Object [] investData =  News.queryInvestDataSum();
		
		NewsType parent = new NewsType();
		parent.id = 3;
		List<NewsType> types = NewsType.queryChildTypes(3, error);
		List<String> contentList = News.queryContentList(id, error);
		
		String titleName = NewsType.queryName(id);
		
		render(contentList, investData, products, creditLevels, parent, types, id,titleName);
	}
	
	/**
	 * 关于我们(Rest风格)
	 */
	public static void restAboutUs(int id){
		ErrorInfo error = new ErrorInfo();

		List<Product> products = Product.queryProductNames(true, error);
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		Object [] investData =  News.queryInvestDataSum();
		
		NewsType parent = new NewsType();
		parent.id = 3;
		List<NewsType> types = NewsType.queryChildTypes(3, error);
		List<String> contentList = News.queryContentList(id, error);
		
		String titleName = NewsType.queryName(id);
		
		render(contentList, investData, products, creditLevels, parent, types, id,titleName);
	}
	
	public static void senior(int id){
		ErrorInfo error = new ErrorInfo();

		List<Product> products = Product.queryProductNames(true, error);
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		Object [] investData =  News.queryInvestDataSum();
		
		NewsType parent = new NewsType();
		parent.id = 3;
		List<NewsType> types = NewsType.queryChildTypes(3, error);
		List<String> contentList = News.queryContentList(id, error);
		
		String titleName = NewsType.queryName(id);
		titleName = "公司高管";
		id = 0;
		
		render(contentList, investData, products, creditLevels, parent, types, id,titleName);
	}
	
	/**
	 * 理财风云榜（更多）
	 */
	public static void moreInvest(int currPage) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bill_board> page = Invest.investBillboards(currPage, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(page);
	}
	
	/**
	 * 招贤纳士
	 */
	public static void careers(){

	}
	
	/**
	 * 管理团队
	 */
	public static void managementTeam(){

	}
	
	/**
	 *专家顾问
	 */
	public static void expertAdvisor(){

	}
	
	
	/**
	 * 禁止收录
	 */
	public static void robots(){
		boolean is_robot = Convert.strToBoolean(Play.configuration.getProperty("is.robots"), true);
		String path = Play.configuration.getProperty("trust.funds.path") + "/robots.txt";
    	InputStream is = null;
		try {
			is = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(!is_robot){			
			renderBinary(is);
		}else{
			renderText("百度收录已开启");
		}
	}
	
	
	/**
	 * 查看具体的发标公告详情
	 * @param year
	 * @param month
	 * @param day
	 */
	public static void bidPublishDetails(int year,int month,int day){
		
		ErrorInfo error = new ErrorInfo();
		
		//查询所有的发标公告
		List<Map<String, Object>> allBidPublish = User.queryBidsPublishListForFront(true);
		
		//根据年月日查询具体的公告内容
		List<t_bid_publish> resultList = User.queryBidPublishByYearMonthDay(year, month, day);
		
		//查询前一条，后一条公告
		Map<String, Object> lastBidPublish = null;
		
		Map<String, Object> nextBidPublish = null;
		
		int index = -1;
		
		int lastIndex = -1;
		
		int nextIndex = -1;
		
		if(allBidPublish!=null && allBidPublish.size()>0){
			
			for(int i = 0 ;i<allBidPublish.size();i++){
				
				Map<String, Object> bidPublish = allBidPublish.get(i);
			    
				Integer tYear = (Integer)bidPublish.get("year");
				Integer tMonth = (Integer)bidPublish.get("month");
				Integer tDay = (Integer)bidPublish.get("day");
				
				if(year == tYear && month == tMonth && day == tDay){
					
					index = i;
					
					break;
				}
			}
			
		}
		
		if(index > -1){
			
			if(index -1 >=0){
				
				lastIndex = index-1;
			}
			
			if(index+1 <= allBidPublish.size()-1){
				
				nextIndex = index+1;
			}
		}
		
		if(lastIndex > -1){
			
			lastBidPublish = allBidPublish.get(lastIndex);
		}
		if(nextIndex > -1){
			
			nextBidPublish = allBidPublish.get(nextIndex);
		}
		
		if(resultList!=null && resultList.size()>0){
			
			t_bid_publish record = resultList.get(0);
			
			renderArgs.put("publishTime", record.create_time);
		}
		
		
		render(resultList,lastBidPublish,nextBidPublish,year,month,day);
		
	}
	
	/**
	 * 发标公告
	 */
	public static void moreBidPublish(){
		
		ErrorInfo error = new ErrorInfo();
		
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);
		
		int currPage = Convert.strToInt(params.get("currPage"),1);
		
		int orderType = 0;//排序方式
		
		if((params.get("orderType") != null) && !(params.get("orderType").equals("")) ) {
			orderType = Integer.parseInt(params.get("orderType"));
		}
		
		int productId = 0;//产品类型
		
		if(StringUtils.isNotBlank(params.get("productId"))) {
			productId = Integer.parseInt(params.get("productId"));
		}
		
		String bidTitle = params.get("bidTitle");//标的名称
		
		String startDate = params.get("startDate");
		
		String endDate = params.get("endDate");
		
		
		PageBean<t_bid_publish> page = User.queryBidsPublishList(orderType, productId,bidTitle,startDate,endDate,currPage, Constants.PAGE_SIZE);
		
		Date nowDate = DateUtil.currentDate();
		
		render(page,products,nowDate);
	}
	
	
}
