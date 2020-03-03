package controllers.front.debt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_invests;
import models.v_debt_auction_records;
import models.v_front_all_debts;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.mvc.With;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import annotation.DebtCheck;
import annotation.InactiveUserCheck;
import annotation.IpsAccountCheck;
import business.CreditLevel;
import business.Debt;
import business.Invest;
import business.Product;
import business.User;
import business.UserAuditItem;
import constants.Constants;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.DebtTransferCheck;
import controllers.front.account.CheckAction;
import controllers.front.account.InvestAccount;
import controllers.interceptor.UserStatusInterceptor;

/**
 * 
 * @author liuwenhui
 * 
 */
@With({DebtTransferCheck.class,UserStatusInterceptor.class})
public class DebtAction extends BaseController {
	/**
	 * 前台债权转让首页
	 */
	@DebtCheck(1)
	public static void debtHome() {
		
		ErrorInfo error = new ErrorInfo();
		Long totle = Debt.getAllDebtCount(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<Product> products = Product.queryProductNames(true, error);
		List<CreditLevel> creditLevels = CreditLevel.queryAllCreditLevels(error);
		
		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		if(NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
 		}
		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		
 		String apr = params.get("apr");
 		String loanType = params.get("loanType");
 		String debtAmount = params.get("debtAmount");
 		String orderType = params.get("orderType");
 		String keywords = params.get("keywords");
 		
 		 PageBean<v_front_all_debts>  page= Debt.queryAllDebtTransfers( currPage,pageSize,loanType, debtAmount, apr, orderType,keywords,error);
         
         if(error.code < 0){
         	render(Constants.ERROR_PAGE_PATH_FRONT);
         }


		render(totle,products, creditLevels,page);
	}

	
	
	/**
	 * 前台债权列表分页
	 * @param pageNum
	 */
	@DebtCheck(1)
	public static void debtHomeDebtList(int pageNum,int pageSize,String loanType,String debtAmount,String apr,String orderType,String keywords){
		
		ErrorInfo error = new ErrorInfo();
        int currPage = pageNum;
		
		if(params.get("currPage")!=null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		
        PageBean<v_front_all_debts>  page= Debt.queryAllDebtTransfers( currPage,pageSize,loanType, debtAmount, apr, orderType,keywords,error);
         
        if(error.code < 0){
        	render(Constants.ERROR_PAGE_PATH_FRONT);
        }
         render(page);
		
	}
	
	
	/* 前台债权转让详情页面 */
	public static void debtTransferDetails() {
		render();
	}

	/**
	 * 前台债权详情页面
	 * @param debtId
	 * @param success
	 */
	@DebtCheck(1)
	public static void debtDetails(long debtId, int success,String descrption) {
		
		ErrorInfo error = new ErrorInfo();
		Debt debt = new Debt();
		debt.id = debtId;
		
		User user = User.currUser();
		Long bidUserId = Debt.getBidUserId(debtId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Map<String,String> historySituationMap = User.historySituation(bidUserId,error);//借款者历史记录情况
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Long investUserId = Debt.getInvestUserId(debtId, error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		Map<String,String> debtUserhistorySituationMap = new HashMap<String, String>();
		
		debtUserhistorySituationMap = User.debtUserhistorySituation(investUserId,error);//债权者历史记录情况
		
		Map<String,String> investMap = Debt.debtBidReceiveSituation(debt.investId);//债权转让借款标资金情况
		
		Invest invest = new Invest();
		
		int currPage = 1;
		int pageSize = 10;
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		PageBean<v_debt_auction_records> page = Debt.queryDebtAllAuctionRecords( currPage,  pageSize, debtId,error);
		page = Debt.queryAllAuctionRecords(currPage,pageSize, error);
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		List<UserAuditItem> uItems = UserAuditItem.queryUserAllAuditItem(bidUserId, debt.invest.bid.mark); // 用户正对产品上传的资料集合
		
		render(debt,user,historySituationMap,debtUserhistorySituationMap, success,descrption,uItems,page,investMap);
	}
	
	
	
	
	
	/**
	 * 确认转让债权
	 * @param investId
	 */
	@DebtCheck(1)
	public static void confirmTransfer(long investId){
		User user = User.currUser();
		
		if(user.simulateLogin != null){
        	if(user.simulateLogin.equalsIgnoreCase(user.encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }
		
		ErrorInfo error = new ErrorInfo();
		String specifiedUserName = "";
		
		t_invests invest = Invest.queryUserAndBid(investId);
		
		if(null == invest){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		String transferTitle = params.get("transferTitle");
		String period = params.get("period");
		String transferReason = params.get("transferReason");
		String price = params.get("transferPrice");
		String transerType = params.get("type");
		
		double debtAmount = Debt.getDebtAmount(investId,error);
		
		boolean b = price.matches("^[1-9][0-9]*$");
    	if(!b){
    		error.msg = "对不起！转让底价只能输入正整数!";
			error.code = -10;
			InvestAccount.repayingInvestBid(error.code,error.msg);
    	} 
    	
    	if(StringUtils.isBlank(transferTitle) || StringUtils.isBlank(period) || StringUtils.isBlank(transferReason) || StringUtils.isBlank(price) ||
    			StringUtils.isBlank(transerType)){
    		error.msg = "对不起！请正确设置各种参数!";
			error.code = -10;
			InvestAccount.repayingInvestBid(error.code,error.msg);
    	}
    	
    	if(transferTitle != null && transferTitle.length() > 30) {
    		error.msg = "对不起！转让标题长度不能大于30!";
			error.code = -10;
			InvestAccount.repayingInvestBid(error.code,error.msg);
    	}
    	
    	if(transferReason != null && transferReason.length() > 250) {
    		error.msg = "对不起！转让原因长度不能大于250!";
			error.code = -10;
			InvestAccount.repayingInvestBid(error.code,error.msg);
    	}
    	
    	int periods = Integer.parseInt(period);
		int type = Integer.parseInt(transerType);
		double transferPrice = Double.parseDouble(price);
		
		if(type == Constants.DIRECTIONAL_MODE){//定向转让
			 specifiedUserName = params.get("specifiedUserName");
		}
		
		Debt.transferDebt(user.id,investId, transferTitle, transferReason, periods, debtAmount, transferPrice, type, specifiedUserName, error);
		
		InvestAccount.repayingInvestBid(error.code,error.msg);
		
		
	}

	/**
	 * 竞拍债权
	 * @param debtId
	 */
	@InactiveUserCheck
	@IpsAccountCheck
	@DebtCheck(1)
	public static void auction(long debtId) {
		
		User user = User.currUser();
		
		if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }

		String offerPriceStr =  params.get("offerPrice");
		String dealpwd = params.get("dealpwd");
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(offerPriceStr)){
			error.msg = "对不起！竞拍价格不能为空！";
			error.code = -1;
			debtDetails(debtId,  error.code,error.msg);
		}
		
		boolean b=offerPriceStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		error.msg = "对不起！竞拍价格只能是正整数！";
			error.code = -1;
			debtDetails(debtId,  error.code,error.msg);
    	} 
		
		int offerPrice = Integer.parseInt(offerPriceStr);
		Debt.auctionDebt(user.id, offerPrice, debtId, dealpwd, Constants.CLIENT_PC, error);
		
		if (Constants.BALANCE_NOT_ENOUGH == error.code) {
			error.msg = "债权转让竞拍余额不足,请充值！";
		}
		
		debtDetails(debtId, error.code,error.msg);
	}
	
	/**
	 * 收藏债权
	 * @param bidId
	 */
	@DebtCheck(1)
	@IpsAccountCheck(true)
	@InactiveUserCheck(true)
	public static void collectDebt(long debtId){
		
		ErrorInfo error = new ErrorInfo();
		User user = User.currUser();
		
		Debt.collectDebt(user.id, debtId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * ajax分页查询债权竞拍记录
	 * @param debtId
	 */
	@DebtCheck(1)
	public static void viewDebtAllAuctionRecords(int pageNum, int pageSize,long debtId){
		
		ErrorInfo error = new ErrorInfo();
		int currPage = pageNum;
		
		if (params.get("currPage") != null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		PageBean<v_debt_auction_records> page = Debt.queryDebtAllAuctionRecords( currPage,  pageSize, debtId,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		
		render(page);
		
	}
	
	/**
	 * 举报用户
	 * @param userName
	 * @param reason
	 * @param bidId
	 * @param investTransferId
	 */
	@IpsAccountCheck(true)
	@InactiveUserCheck(true)
	public static void reportUser(String userIdSign, String reason, String bidIdSign, long investTransferId ){
		
		User user = User.currUser();
		
		if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }
		
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		long bidId = 0;
		
		if(StringUtils.isBlank(reason) || reason.length() > 240){
			error.code = -1;
			error.msg = "举报内容有误";
			json.put("msg", error.msg); 
			
			renderJSON(json);
		}

		if(!StringUtils.isBlank(bidIdSign)){
			
			 bidId = Security.checkSign(bidIdSign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
			
			if(error.code < 0){
				json.put("msg", error.msg); 
				renderJSON(json);
			}
		}
		
		
		long userId = Security.checkSign(userIdSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			json.put("msg", error.msg); 
			renderJSON(json);
		}
		
		if(userId == user.id){
			error.msg = "对不起！您不能举报自己";
			json.put("msg", error.msg); 
			renderJSON(json);
		}
		
		String userName = User.queryUserNameById( userId,error);
		user.addReportAUser(userName, reason, bidId, investTransferId, error);
		
		
		json.put("msg", error.msg);
		
		renderJSON(json);
	}
	
	@DebtCheck(1)
	public static void judgeUserNameExist(String userName){
		
		ErrorInfo error = new ErrorInfo();
		int result = User.isNameExist(userName, error);
		int isBlackOrUnActiveUser = User.isBlackOrUnActiveUser(userName);
		
		JSONObject json = new JSONObject();
		json.put("result", result);
		json.put("isBlackOrUnActiveUser", isBlackOrUnActiveUser);
		renderJSON(json);
	}
	
	/**
	 * 查看资料图片
	 * @param itemId
	 */
	@DebtCheck(1)
	public static void showitem(long itemId,String signUserId){
		ErrorInfo error = new ErrorInfo();
		long userId = Security.checkSign(signUserId, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		UserAuditItem item = Invest.getAuditItem(itemId, userId);
		
		render(item);
	}
	
}
