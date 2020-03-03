package controllers.supervisor.bidManager;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import bean.AgencyBid;
import business.Agency;
import business.Bid;
import business.Bid.Purpose;
import business.BidImages;
import business.BidUserRisk;
import business.DebtInvest;
import business.Optimization;
import business.Product;
import business.ReportUserInfo;
import business.Supervisor;
import business.User;
import business.UserAuthReview;
import business.UserBankAccounts;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_bid_risk;
import models.t_bid_user_risk;
import models.t_bids;
import models.t_borrow_apply;
import models.t_user_city;
import models.t_user_risk;
import models.t_users;
import models.v_agencies;
import models.v_bids;
import models.v_user_info;
import models.core.t_new_product;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.CaptchaUtil;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;

/**
 * 机构合作/机构标管理
 * @author bsr
 * @version 6.0
 * @created 2014-6-26 下午01:52:09
 */
public class BidAgencyAction extends SupervisorController{
	
	/**
	 * 合作结构标列表
	 */
	public static void agencyBidList(int isExport){
		ErrorInfo error = new ErrorInfo();
		
		/* 删除机构标的缓存信息 */
		String key = "agencyBid_" + session.getId();
		Cache.delete(key);
		
		PageBean<AgencyBid> pageBean = new PageBean<AgencyBid>();
		pageBean.page = Optimization.BidOZ.queryAgencyBids(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, pageBean, error, BidPlatformAction.getParameter(pageBean, null));

		if(isExport == Constants.IS_EXPORT){
			
			List<AgencyBid> list = pageBean.page;

			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				
				String showPeriod = "";
				int period = bid.getInt("period");
				int period_unit = bid.getInt("period_unit");
				if(period_unit == -1){
					showPeriod = period + "[年 ]";
				}else if(period_unit == 1){
					showPeriod = period + "[日]";
				}else{		
					showPeriod = period + "[月]";
				}
				
				DecimalFormat df = new DecimalFormat("#0.0");
				double percent = 0.0;
				int productItem = bid.getInt("product_item_count");
				int userItem = bid.getInt("user_item_count_true");
				if(productItem == 0 || userItem / productItem >= 1){
					percent = 100.0;
				}else{
					percent = (userItem * 100.0 ) / productItem;
				}
				String auditStatus = df.format(percent) + "%";
				
				String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
				String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
				
				bid.put("period", showPeriod);
				bid.put("small_image_filename", productName);
				bid.put("credit_level_image_filename", creditLevel);
				bid.put("apr", String.format("%.1f", bid.getDouble("apr")) + "%");
				
				bid.put("loan_schedule", String.format("%.1f", bid.getDouble("loan_schedule")) + "%");
				bid.put("audit_status", auditStatus);
			}
			
			File file = ExcelUtils.export("机构合作标列表",
			arrList,
			new String[] {
			"编号", "标题", "发布者", "信用等级", "借款标金额", "借款标类型", "年利率",
			"借款期限", "发布时间", "合作机构", "借款进度",
			"借款状态", "审核状态"},
			new String[] { "bid_no", "title", "user_name",
			"credit_level_image_filename", "amount", "small_image_filename", "apr",
			"period", "time","agency_name",
			"loan_schedule", "strStatus", "audit_status"});
			   
			renderBinary(file, "机构合作标列表.xls");
		}
		
		render(pageBean);
	}
	

	/**
	 * 发布合作机构标 页面跳转
	 * @param applyFlag 0是一次性上标,1是分标
	 * @（注意）参数 id 是 借款申请表主键
	 */
	public static void addAgencyBid(Long borrow_apply_id,String is_split_bid){
		ErrorInfo error = new ErrorInfo();

		/* 得到所有借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		
		/* 机构标产品 */
		Product product = Product.queryAgencyProduct(error);
		
		if(null == product){
			flash.error(error.msg);
			redirect(Constants.HTTP_PATH + "/supervisor/BidApplicationAction/applicationCheckList");
		}
		
		/* 机构列表 */
		List<Agency> agencys = Agency.queryAgencys(error);
		
		String key = "agencyBid_" + session.getId();
		Bid loanBid = (Bid) Cache.get(key);  // 获取用户输入的临时数据
		if(loanBid==null) {
			loanBid=new Bid();
		}
		

		t_borrow_apply borrowApply=null;
		t_users user=null;
		t_users consociation_user=null;
		UserBankAccounts bankCard=null;
		t_user_city userCity=null;
		if(borrow_apply_id!=null) {
			
			borrowApply=t_borrow_apply.findById(borrow_apply_id);
			borrowApply.bidOverPlusMoney=Bid.getBidOverPlusMoney(borrow_apply_id);
			loanBid.isSplitBid="true".equals(is_split_bid)?true:false;
			if(borrowApply.user_id==null) {
				flash.error("该借款申请没有用户！");
				redirect(Constants.HTTP_PATH + "/supervisor/BidApplicationAction/applicationCheckList");
			}
			user=t_users.findById(borrowApply.user_id);
			if(user==null) {
				flash.error("该借款申请没有用户！");
				redirect(Constants.HTTP_PATH + "/supervisor/BidApplicationAction/applicationCheckList");
			}
			if(user.reality_name==null || user.reality_name.isEmpty()) {
				flash.error("未实名认证的用户不能发布借款！");
				redirect(Constants.HTTP_PATH + "/supervisor/BidApplicationAction/applicationCheckList");
			}
			userCity = t_user_city.find(" user_id = ? ", user.id).first();
			
			List<UserBankAccounts> bankList=UserBankAccounts.queryUserAllBankAccount(user.id);
			if(bankList.size()>0) {
				bankCard=bankList.get(0);
			}
			user.bankCardCount=bankList.size();
			
			if(borrowApply.accredit_pay_for_consociation!=null 
					&& borrowApply.accredit_pay_for_consociation
					&& borrowApply.consociation_user_id!=null) {
				consociation_user=t_users.findById(borrowApply.consociation_user_id);
			}
			
			/**
			 * 填充bid临时信息
			 */
			if(borrowApply.getAllot_area()!=null) {
				loanBid.setAgencyId(borrowApply.getAllot_area().intValue());
			}
			loanBid.userName=user.reality_name;
			loanBid.borrowApplyId=borrow_apply_id;
			if(borrowApply.period_unit!=null) {
				loanBid.period_unit=borrowApply.period_unit;
			}
			loanBid.period=borrowApply.period;
			loanBid.tag=borrowApply.product_name;
		}
		Cache.delete(key); // 删除缓存中的bid对象
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		int once_repayment = Constants.ONCE_REPAYMENT;  //一次性还款方式
		
		List<t_bid_risk> bidRiskList=t_bid_risk.getAllBidRiskList();
		List<t_new_product> newProductList=new t_new_product().fetchEnumList();
		render(purpose, product, agencys, uuid, loanBid, once_repayment
				,borrowApply,user,bankCard,userCity,consociation_user
				,newProductList,bidRiskList);
	}
	
	/**
	 * 发布合作机构标
	 */
	public static void addingAgencyBid(Bid bid, long productId, String uuid){
		/* 有效表单验证  */
		checkAuthenticity(); 
		
		/* 将合作机构标信息放在cache中,如果错误带会到页面中 */
		Cache.set("agencyBid_" + session.getId(), bid);
		String isSplitBid=bid.isSplitBid==null?"":(bid.isSplitBid+"");
		if(!CaptchaUtil.checkUUID(uuid)){
			flash.error("请求已提交或请求超时!");
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		if(bid.agencyId <= 0) { 
			flash.error("机构名称有误!"); 
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		if(StringUtils.isBlank(bid.auditSuggest)) { 
			flash.error("请填写保障措施!"); 	
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		if(StringUtils.isBlank(bid.description)) { 
			flash.error("请填写项目简!"); 	
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		if(StringUtils.isBlank(bid.tag)) { 
			flash.error("请填选择标类型!"); 	
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		String userName = params.get("userName");
		String signUserId = params.get("sign");
		
		if(StringUtils.isBlank(signUserId) && StringUtils.isBlank(userName)){
			flash.error("直接借款人有误!");
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		/*if(bid.isDebtTransfer && bid.isIncreaseRate){
			flash.error("加息标的不可做债权转让!");
			addAgencyBid(null,null);
		}
		if(bid.isDebtTransfer && bid.repayment.id == 1){
			flash.error("等额本息标的不可做债权转让!");
			addAgencyBid(null,null);
		}*/
		ErrorInfo error = new ErrorInfo();
		long userId = 0;
		
		if(StringUtils.isNotBlank(userName)){
			userId = User.queryIdByUserName(userName, error);
		}else{
			userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		}
		
		if(userId < 1){
			flash.error(error.msg);
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		bid.createBid = true;
		bid.productId = productId;  // 填充产品对象
		bid.userId = userId;

		/* 非友好提示 */
		if( bid.user.id < 1 ||
			null == bid.product || 
			!bid.product.isUse || 
			!bid.product.isAgency 
		){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR); 
		}
		
		if(!(bid.user.isEmailVerified || bid.user.isMobileVerified)){
			flash.error("借款人未激活!");
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		/* 需要填写基本资料 */
		/*if(!bid.user.isAddBaseInfo) {
			flash.error("借款人未填写基本资料!");
			
			addAgencyBid(null,null);
		}*/
		
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && bid.product.loanType == Constants.S_REPAYMENT_BID && StringUtils.isBlank(bid.user.ipsRepayAuthNo)) {
			flash.error("直接借款人未自动还款签约!");
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		/* 发布借款 */
		t_bids tbid = new t_bids();
		
		if(bid.isIncreaseRate){
			if(bid.increaseRate <= 0.0){
				flash.error("加息年化利率填写大于0数字!");
				
				addAgencyBid(bid.borrowApplyId,isSplitBid);
			}
			if(StringUtils.isBlank(bid.increaseRateName)){
				bid.increaseRateName = "标的加息";
			}
		}
		//标的发布前校验， 及组装标的信息，不插入数据库
		bid.createBid(Constants.CLIENT_PC, tbid, error);
		if(error.code < 0){
			flash.error(error.msg);
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		
		//资金托管发布机构合作标的，业务逻辑在回调方法中处理
		if(Constants.IPS_ENABLE){
			
			PaymentProxy.getInstance().bidCreate(error, Constants.CLIENT_PC, tbid, bid);
			
			flash.error(error.msg);				
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}
		/* 发布借款 */
		bid.afterCreateBid(tbid, null, Constants.CLIENT_PC, Supervisor.currSupervisor().getId(), error);		
		if(error.code < 0) {
			flash.error(error.msg);
			
			addAgencyBid(bid.borrowApplyId,isSplitBid);
		}		
		flash.error(tbid.id > 0 ? "发布成功!" : error.msg);
		
		agencyBidList(0);
	}

	/**
	 * 检查数据
	 */
	@Deprecated
	public static boolean checkAgencyBid(Bid bid){
		if(bid.agencyId <= 0) { flash.error("机构名称有误!"); return true; }
		if(StringUtils.isBlank(bid.title) || bid.title.length() > 24){ flash.error("借款标题有误!"); return true; }
		int _amount = (int)bid.amount;
		if(bid.amount <= 0 || bid.amount != _amount || bid.amount < bid.product.minAmount || bid.amount > bid.product.maxAmount){flash.error("借款金额有误!"); return true; }
		if (bid.apr <= 0 || bid.apr > 100 || bid.apr < bid.product.minInterestRate || bid.apr > bid.product.maxInterestRate) { flash.error("年利率有误!"); return true; }
		if (bid.product.loanImageType == Constants.USER_UPLOAD && (StringUtils.isBlank(bid.imageFilename) || bid.imageFilename.contains(Constants.DEFAULT_IMAGE))) { flash.error("借款图片有误!"); return true; }
		if(bid.purpose.id < 0){ flash.error("借款用途有误!"); return true; }
		if(bid.repayment.id < 0){ flash.error("借款类型有误!"); return true; }
		if(bid.period <= 0){ flash.error("借款期限有误!"); return true; }
		switch (bid.periodUnit) {
		case Constants.YEAR:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			break;
		case Constants.MONTH:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT * 12){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			break;
		case Constants.DAY:
			if(bid.period > Constants.YEAR_PERIOD_LIMIT * 12 * 30){ flash.error("借款期限超过了" + Constants.YEAR_PERIOD_LIMIT + "年"); return true; }
			if(bid.investPeriod > bid.period){ flash.error("天标满标期限不能大于借款期限 !"); return true; }
			break;
		default: flash.error("借款期限单位有误!"); return true;
		}
		
		if((bid.minInvestAmount > 0 && bid.averageInvestAmount > 0) || (bid.minInvestAmount <= 0 && bid.averageInvestAmount <= 0)){ flash.error("最低投标金额和平均招标金额有误!"); return true; }
		if(bid.averageInvestAmount > 0 && bid.amount % bid.averageInvestAmount != 0){ flash.error("平均招标金额有误!"); return true;}
		if(bid.investPeriod <= 0) { flash.error("投标期限有误!"); return true; }
		if(StringUtils.isBlank(bid.description)) { flash.error("借款描述有误!"); return true; }
		if (bid.minInvestAmount > 0 && (bid.minInvestAmount < bid.product.minInvestAmount)){ flash.error("最低投标金额不能小于产品最低投标金额!"); return true; }
		if (bid.averageInvestAmount > 0 && (bid.amount / bid.averageInvestAmount > bid.product.maxCopies)){ flash.error("平均投标份数不能大于产品的最大份数限制 !"); return true; }
	
		return false;
	}
	
	/**
	 * 异步选择用户
	 */	
	public static void selectUsersInit(String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_info> pageBean = User.queryActiveUser(null, null, null, null, keyword, "0", currPage, Constants.PAGE_SIZE_EIGHT+"", error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		render(pageBean);
	}
	/**
	 * 异步选择借款用户
	 */	
	public static void selectBorrowUsersInit(String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_user_info> pageBean = User.queryActiveBorrowUser(null, null, null, null, keyword, "0", currPage, Constants.PAGE_SIZE_EIGHT+"", error);
		
		if(error.code < 0) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		render(pageBean);
	}
	
	/**
	 * 合作结构列表
	 */
	public static void agencyList() {
		ErrorInfo error = new ErrorInfo();

		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String condition = params.get("condition"); // 条件
		String keyword = params.get("keyword"); // 关键词
		
		PageBean<v_agencies> pageBean = new PageBean<v_agencies>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Agency.queryAgencies(pageBean, error, condition, keyword);

		if (null == pageBean.page) render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  

		render(pageBean);
	}
	/**
	 * 标的用户风险等级显示
	 */
	public static void bidUserRiskShow() {
		t_bid_user_risk bid_user_risk =new t_bid_user_risk();
		t_bid_risk bid_risk =new t_bid_risk();
		t_user_risk user_risk =new t_user_risk();
		
		String sign=Security.addSign(Supervisor.currSupervisor().id, Constants.SUPERVISOR_ID_SIGN);
		render(bid_user_risk,bid_risk,user_risk,sign);
	}
	/**
	 * 标的用户风险等级批量维护
	 */
	public static void bidUserRiskUpdateBatch(String sign) {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();
		Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1 ){
			flash.error(error.msg);
			bidUserRiskShow();
		}
		
		String input_index_arr[]=params.getAll("input_index");
		for(String input_index:input_index_arr){
			Long id=Long.parseLong(params.get("id_"+input_index));
			BigDecimal quota=new BigDecimal(params.get("quota_"+input_index)).setScale(2, RoundingMode.HALF_UP);
			if(quota.compareTo(BigDecimal.ZERO) < 0 ){
				JPA.setRollbackOnly();
				flash.error("限额必须大于等于0");
				bidUserRiskShow();
			}
			BidUserRisk.bidUserRiskUpdate(id,quota);
		}
		bidUserRiskShow();
	}
	
	
	/**
	 * 启用合作机构
	 */
	public static void enanleAgency(long aid){
		ErrorInfo error = new ErrorInfo();
	    Agency.editStatus(aid, Constants.ENABLE, error);
		flash.error(error.msg);
	    
		agencyList();
	}
	
	/**
	 * 暂停合作机构
	 */
	public static void notEnanleAgency(long aid){
		ErrorInfo error = new ErrorInfo();
		Agency.editStatus(aid, Constants.NOT_ENABLE, error);
		flash.error(error.msg);
	    
		agencyList();
	}
	
	/**
	 * 添加合作机构 页面跳转
	 */
	public static void addAgency(){
		//ErrorInfo error = new ErrorInfo();
		
		/* 信用等级名称 */
		//List<CreditLevel> creditLevels = CreditLevel.queryCreditName(error);
		
		render();
	}
	
	/**
	 * 添加合作机构
	 */
	public static void addingAgency(Agency agency){

		if( StringUtils.isBlank(agency.name) ||
			Agency.checkName(agency.name) ||
			Constants.AGENCY_NAME_REPEAT.equals(agency.name) ||
			StringUtils.isBlank(agency.introduction) ||
			StringUtils.isBlank(agency.id_number) ||
			Constants.SEAL_NAME_REPEAT.equals(agency.id_number) ||
			Agency.checkIdNumber(agency.id_number) ||
			StringUtils.isBlank(agency.imageFilenames) ||
			agency.creditLevel <= 0 
		  ){
			flash.error("数据有误!");
			
			agencyList();
		}
		
		ErrorInfo error = new ErrorInfo();
		agency.createAgency(error);
		flash.error(error.msg);
		
		agencyList();
	}
	
	/**
	 * 检查名称是否唯一
	 */
	public static void checkName(String name){
		renderJSON(Agency.checkName(name));
	}
	
	/**
	 * 检查营业执照号是否唯一
	 */
	public static void checkIdNumber(String idNumber){
	    renderJSON(Agency.checkIdNumber(idNumber));
	}
	
	/**
	 * 合作机构标详情(审核操作等,需要去借款标管理中进行)
	 */
	public static void detail(long bidid, int flag) { 
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = flag;
		bid.id = bidid;
		List<BidImages> bidImagesList=new ArrayList<>();
		try {
			bidImagesList = BidImages.getBidImagesByBidId(bidid);
		} catch (Exception e) {
			Logger.info("查看标详情---查询标关联图片出错");
			e.printStackTrace();
		}
		render(bid, flag,bidImagesList);
	}
	
	/**
	 * 合作机构详情(对应的标列表)
	 */
	public static void agencyDetail(long agencyId){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bids> pageBean = new PageBean<v_bids>();
		pageBean.page = Bid.queryAgencyBid(pageBean, agencyId, error, BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page) render(Constants.ERROR_PAGE_PATH_SUPERVISOR);  

		render(pageBean, agencyId);
	}
	
}
