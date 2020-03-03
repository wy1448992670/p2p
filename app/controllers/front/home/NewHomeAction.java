package controllers.front.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import business.*;
import models.t_agencies;
import models.t_bid_publish;
import models.t_bills;
import models.t_content_advertisements;
import models.t_content_advertisements_partner;
import models.t_content_news;
import models.t_content_news_types;
import models.t_user_bank_accounts;
import models.t_user_msg;
import models.t_users;
import models.v_bill_board;
import models.v_front_all_bids;
import models.v_user_msg;
import models.core.t_new_product;
import models.core.t_org_project;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.shove.Convert;
import com.yiyilc.http.gateway.GatewayTest;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.jpa.GenericModel.JPAQuery;
import play.mvc.Scope.Session;
import services.file.FileService;
import services.ymd.OrganizationService;
import utils.Arith;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import business.Bid.Repayment;
import constants.Constants;
import constants.FinanceTypeEnum;
import constants.OptionKeys;
import controllers.BaseController;
import exception.BalanceIsEnoughException;

/**
 *
* Description:
* @author xinsw
* @date 2017年4月3日
 */
public class NewHomeAction extends BaseController{

	/*网站首页*/
	public static void home(){

//		String isH5Page = Play.configuration.getProperty("is_h5_page", "0");
//		if("1".equals(isH5Page)) {
//			String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"
//					+"|windows (phone|ce)|blackberry"
//					+"|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"
//					+"|laystation portable)|nokia|fennec|htc[-_]"
//					+"|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
//			Pattern checkPhone = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);
//			Matcher matcher = checkPhone.matcher(request.headers.get("user-agent").value());
//			if(matcher.find()) {
//				//redirect("/h5home");
//				String content = News.queryContent(Constants.NewsTypeId.REGISTER_AGREEMENT2, new ErrorInfo());
//				content = content.replaceAll("\\n|\\r", "");
//				render("app/views/front/account/LoginAndRegisterAction/h5Index.html", content);
//			}
//		}


		ErrorInfo error = new ErrorInfo();

		List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条

		List<t_content_advertisements> homeAdvise = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC_ADVISE, error); // 活动宣传位
		t_content_advertisements advise = new t_content_advertisements();
		if(homeAdvise != null && homeAdvise.size() > 0){
			advise = homeAdvise.get(0);
		}

		List<v_front_all_bids> bidList = Invest.queryOverBids(null);//首页最新3个推荐项目
		List<v_front_all_bids> bidLEList = Invest.queryOverBids("信亿贷");//首页最新3个信亿贷
		List<v_front_all_bids> bidCHList = Invest.queryOverBids("房亿贷");//首页最新3个房亿贷
		List<v_front_all_bids> bidCashList = Invest.queryOverBids("车亿贷");//首页最新3个车亿贷



//		List<v_front_all_bids> agencyBids = Invest.queryAgencyBids();//机构借款标

//		List<v_front_all_bids> memberBids = Invest.queryMemberBids();//前台显示的2个会员贷

		List<v_bill_board> investBillboard = Invest.investBillboard();//理财风云榜

//		List<t_content_news> successStorys = News.queryNewForFront(12l, 2, error) ;//首页成功故事
//
//		List<t_content_news> investSkills = News.queryNewForFront(10l, 5, error) ;//首页借款技巧
//
//		List<t_content_news> loanSkills = News.queryNewForFront(11l, 5, error) ;//首页理财技巧

//		List<t_content_news> news = News.queryNewForFront(7l, 5, error) ;//首页官方公告

		List<Bid> bids = Bid.queryAdvertisement(error); // 最新投资资讯

		List<t_content_news> news10 = News.queryNewsByTypeId("10", "1", "5", "", error).page;//公告

//		List<Map<String,String>> maps = Invest.queryNearlyInvest(error);

//		List<AdsEnsure> adsEnsure = AdsEnsure.queryEnsureForFront(error); //四大安全保障

		List<t_content_advertisements_partner> adsPartner = News.queryPartners(error);//合作伙伴

//		List<NewsType> types = NewsType.queryChildTypes(1, error);

		List<Map<String, Object>> bidPublishs = User.queryBidsPublishListForFront(false,5);//查询投标公告

		List<t_content_news> news8 = News.queryNewsByTypeId("8", "1", "5", "", error).page;//行业新闻
		List<t_content_news> news11 = News.queryNewsByTypeId("11", "1", "5", "", error).page;//理财百科
		List<t_content_news> news7 = News.queryNewsByTypeId("7", "1", "5", "", error).page;//公司动态

		List<v_user_msg> msgs = UserMsg.queryUserMsg(1, 6, error).page;//投资心声

		String encryString = Session.current().getId();
		if (!StringUtils.isBlank(encryString)) {
			String userId = Cache.get("front_" + encryString) + "";
			if (!StringUtils.isBlank(userId)) {
				User user = (User) Cache.get("userId_" + userId);
				if(user != null && user.financeType == FinanceTypeEnum.BORROW.getCode()) { //如果是借款端用户默认登出
					user.removeCurrUser();
				}
			}
		}

//		render(homeAds,advise,bidList, bidLEList,bidCHList,bidCashList,agencyBids,investBillboard,successStorys,investSkills,loanSkills,
//				bids, adsEnsure, adsPartner, types ,maps,memberBids,bidPublishs,friendLinks,news8,news11,news7,msgs);
		render(homeAds,advise,bidList, bidLEList,bidCHList,bidCashList,investBillboard,bidPublishs,news10,
				bids, adsPartner,news8,news11,news7,msgs);
	}
	/**
	 *
	 * @author xinsw
	 * @creationDate 2017年4月18日
	 * @description 进入更多留言心声
	 */
	public static void msg(){
		render();
	}

	public static void addUserMsg(String msg){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		if(user == null){
			flash.error("请先登录后再提交");
			msg();
		}
		if(msg == null || msg.length() == 0 || msg.length() > 100) {
			flash.error("请输入您的心声(请输入100字以内)");
			msg();
		}
		t_user_msg userMsg = new t_user_msg();
		userMsg.msg = msg;
		userMsg.user_id = user.getId();

		if(UserMsg.addMsg(userMsg, error) == 0){
			msg();
		}
		flash.error(error.msg);
	}
	/**
	 *
	 * @author xinsw
	 * @creationDate 2017年4月18日
	 * @description 获取留言心声分页数据
	 * @param currPage
	 * @param pageSize
	 */
	public static void msgdata(int currPage, int pageSize){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_msg> msgs = UserMsg.queryUserMsg(currPage, pageSize, error);
		if(pageSize == 0 ){
			pageSize = 5;
		}
		msgs.totalPageCount = (msgs.totalCount - 1) / pageSize + 1;
		List<v_user_msg> list = msgs.page;
		if(list != null && list.size() > 0){
			for(v_user_msg msg : list){
				String src = msg.photo;
				if (StringUtils.contains(src, "images?uuid=")){
		        	if(!src.startsWith("http")){
		        		String uuid = StringUtils.substring(src, StringUtils.lastIndexOf(src, '=') + 1);
		        		String attachmentPath = Play.configuration.getProperty("attachments.path");
		        		src = (new StringBuilder().append(Play.ctxPath).append('/').append(attachmentPath).append('/').append(uuid)).toString();
		        	}
		        }
				msg.date = DateUtil.dateToString(msg.ins_dt);
				msg.photo = src;
				msg.name = msg.name.substring(0,1) + "***" + msg.name.substring(msg.name.length()-1,msg.name.length());
			}
		}
		renderJSON(msgs);
	}

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
	 * 下载借款端app链接
	 */
	public static void download(){
		render();
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
	 * 利率计算器,计算年华收益、总利益(同步)
	 */
	public static Map<String, String> aprCalculatorReturn(double amount, double apr,int repaymentType,double award,int rperiod){
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
		Map map = new HashMap<>();

		map.put("managementRate", managementRate < 0 ? 0 : managementRate);
		map.put("earning", earning);
		return  map;
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

	public static void test() throws UnsupportedEncodingException{
		String password=params.get("password");
		if(password.equals("passwordABC")){
			try {
				AutoReturnMoney.payForBill(7516L);
			} catch (BalanceIsEnoughException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		List<Long> ids=new ArrayList<Long>() {{add(8009L);}};
		JPAQuery jq=t_bills.find("select b from t_bills b where b.id in(:ids)");
		jq.setParameter("ids",ids);
		List<t_bills> billList=jq.fetch();
		
		for(t_bills bill:billList) {
			
			System.out.println("autoReturnMoneyForBill begin:"+bill.id);
			
			try {
				AutoReturnMoney.autoReturnMoneyForBill(bill.id);
			} catch (Exception e) {
				Logger.error("billId:"+bill.id+" 自动回款异常");
				e.printStackTrace();
			}
			System.out.println("autoReturnMoneyForBill end:"+bill.id);
			
		}
		
		Set<String> keys = request.params.data.keySet();
		for(String key:keys) {
			System.out.println(key+" "+request.params.get(key));
		}
		System.out.println("request end");

		GatewayTest.doTest2();
		*/
		/*
		User user=new User();
		user.id=21;
		System.out.println(com.alibaba.fastjson.JSON.toJSONString(user));
		renderJSON(BorrowApply.queryMyBorrowApply(1501,1,5, new ErrorInfo()));*/

		//String result=GatewayTest.doTest();
		//renderJSON(result);

		//GatewayTest.doInsert2();

	}
	
}
