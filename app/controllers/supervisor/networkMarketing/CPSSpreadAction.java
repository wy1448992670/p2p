package controllers.supervisor.networkMarketing;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;
import com.sun.star.bridge.oleautomation.Date;

import business.BackstageSet;
import business.Bid.Repayment;
import business.Product;
import business.User;
import business.UserV2;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_bid_publish;
import models.t_cps_info;
import models.t_dict_bid_repayment_types;
import models.t_products;
import models.t_statistic_cps;
import models.t_user_details;
import models.v_user_cps_detail;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * GPS会员推广管理
 * 
 * @author bsr
 * 
 */
public class CPSSpreadAction extends SupervisorController {

	/**
	 * 查询所有的GPS会员推广列表
	 */
	public static void CPSAll() {
		int currPage = 1;
		
		currPage = Convert.strToInt(params.get("currPage"),1);
		
		String name = null;
		
		if(params.get("name") != null) {
			name = params.get("name");
		}
		
		int orderType = 0;
		
		if((params.get("orderType") != null) && !(params.get("orderType").equals("")) ) {
			orderType = Integer.parseInt(params.get("orderType"));
		}
		
		PageBean<t_cps_info> page = User.queryCpsUserInfo(name, orderType, currPage, Constants.PAGE_SIZE);
		
		render(page);
	}

	/**
	 * 查询CPS会员明细
	 */
	public static void CPSDetail(String sign, String beginTime, String endTime, int currPage, String name,int pageSize) {
		ErrorInfo error = new ErrorInfo();
		
		long userId = Security.checkSign(sign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			
			renderJSON(error);
		}
		
		PageBean<v_user_cps_detail> page = User.queryCpsDetail(userId, name, beginTime, endTime, currPage, pageSize);
		
		render(page);
	}

	/**
	 * 佣金发放明细
	 */
	public static void CPSRebateDetail() {
		int currPage = 1;
		
		if(params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
		String name = null;
		
		if(params.get("name") != null) {
			currPage = Integer.parseInt(params.get("name"));
		}
		
		PageBean<t_user_details> page = User.queryCpsCommissionDetail(1L, name, currPage, 2);
		
		render(page);
		
	}

	/**
	 * 佣金发放交易明细
	 */
	public static void CPSTransactionDetail() {
		render();
	}

	/**
	 * 佣金发放统计
	 */
	public static void CPSRebateStatistic(int year, int month, int currPage) {
		PageBean<t_statistic_cps> page = User.queryCpsOfferInfo(1L, year, month, currPage);
		
		render(page);
	}

	/**
	 * 推广规则设置
	 */
	public static void CPSSpreadRule() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}

	/**
	 * 保存CPS推广规则
	 */
	public static void saveRule(int cpsRewardType, double rewardForCounts, double rewardForRate, java.util.Date cpsRelationStartDate) {
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = new BackstageSet();
		backstageSet.cpsRewardType = cpsRewardType;
		backstageSet.rewardForCounts = rewardForCounts;
		backstageSet.rewardForRate = rewardForRate;
		backstageSet.cpsRelationStartDate = cpsRelationStartDate;
		
		backstageSet.CPSPromotion(error);
		
		if(error.code<0) {
			flash.error(error.msg);
			render("@CPSSpreadRule",backstageSet);
		}
		
		flash.success(error.msg);
		CPSSpreadRule();
	}
	
	/**
	 * 发标公告
	 */
	public static void bidPublish(){
		
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
		
		render(page,products);
	}
	
	/**
	 * 添加发标公告初始化
	 */
	public static void addBidPublishInit(){
		
		ErrorInfo error = new ErrorInfo();
		
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);
		
		/* 还款方式  */
		List<Repayment> repaymentTypes = Repayment.queryRepaymentType(null, error);
		
		
		render(products,repaymentTypes);
	}
	
	/**
	 * 添加发标公告
	 */
	public static void addBidPublish(){
		
		ErrorInfo error = new ErrorInfo();
		
		String bidTitle = params.get("bidTitle");
		
		String amountStr = params.get("amount");
		
		String productIdStr = params.get("productId");
		
		String periodUnitStr = params.get("periodUnit");
		
		String periodStr = params.get("period");
		
		String repaymentTypeIdStr = params.get("repaymentTypeId");
		
		String aprStr = params.get("apr");
		
		String startPublishTimeStr = params.get("startPublishTime");
		
		t_products product = t_products.findById(Long.parseLong(productIdStr));
		
		if(product == null){
			error.msg = "标的类型不存在";
			error.code = -1;
			renderJSON(error);
		}
		
		t_dict_bid_repayment_types repaymentTypes = t_dict_bid_repayment_types.findById(Long.parseLong(repaymentTypeIdStr));
		
		if(repaymentTypes == null){
			error.msg = "返还方式不存在";
			error.code = -1;
			renderJSON(error);
		}
		
		t_bid_publish bidPublish = new t_bid_publish();
		
		bidPublish.amount = Double.parseDouble(amountStr);
		
		bidPublish.apr = Double.parseDouble(aprStr);
		
		bidPublish.bid_title = bidTitle;
		
		bidPublish.period = Integer.parseInt(periodStr);
		
	    bidPublish.period_unit = Integer.parseInt(periodUnitStr);
	    
	    bidPublish.product_id = Integer.parseInt(productIdStr);
	    
	    bidPublish.product_name = product.name;
	    
	    bidPublish.repayment_type_id = Integer.parseInt(repaymentTypeIdStr);
	    
	    bidPublish.repayment_type_name = repaymentTypes.name;
	    
	    bidPublish.publish_time = DateUtil.strDateToStartDate(startPublishTimeStr);
		
	    bidPublish.create_time = DateUtil.currentDate();
	    
		User.addBidPublish(bidPublish, error);
		
		renderJSON(error);
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jul 25, 2018 2:18:46 PM 
	 * @description.  cps黑名单
	 *	insert into `p2p_dev`.`t_right_actions` ( `id`, `right_id`, `action`, `description`) values ( '6010', '121', 'supervisor.networkMarketing.CPSSpreadAction.cpsBlacklist', 'CPS推广管理');
	 */
	public static void cpsBlacklist(Integer userId, Integer isBlacklist) {
		
		if(userId != null) {
			isBlacklist = isBlacklist == null ? 0 : isBlacklist;
			renderText(UserV2.updateUserBlacklist(userId, isBlacklist));
		}
		
		int currPage = Convert.strToInt(params.get("currPage"), 1);
		int pageSize = Convert.strToInt(params.get("pageSize"), 10);
		
//		com.alibaba.fastjson.JSONObject userBlacklist = UserV2.findUserBlacklist(1);
//		int isBlacklist = userBlacklist.getIntValue("is_blacklist");
//		Date blacklistDt = (Date) userBlacklist.get("blacklist_dt");
//		System.out.println(isBlacklist);
//		System.out.println(blacklistDt);
		
		PageBean<Map<String, Object>> page = UserV2.findUser4CPSBlacklist(params.get("keyword"), pageSize, currPage);
		
		render(page);
	}
}
