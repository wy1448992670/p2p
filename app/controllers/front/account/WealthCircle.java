package controllers.front.account;

import models.t_wealthcircle_income;
import models.t_wealthcircle_invite;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import business.BackstageSet;
import business.Optimization.UserOZ;
import business.User;
import business.Wealthcircle;
import constants.Constants;
import controllers.BaseController;
import controllers.interceptor.AccountInterceptor;

/**
 * 财富圈action
 * 
 * @author hys
 *
 */
@With({AccountInterceptor.class})
public class WealthCircle extends BaseController {

	/**
	 * 我的财富圈（首页）
	 * @param type
	 * @param keyword
	 * @param beginTime
	 * @param endTime
	 * @param currPage
	 * @param pageSize
	 */
	public static void wealthHome(int type, int status ,int currPage,int pageSize){
		
		User user = User.currUser();
		if (user == null) {
			LoginAndRegisterAction.login();
		}
		
		long userId = user.id;
		UserOZ accountInfo = new UserOZ(userId);
		
		double investmentTotal =  accountInfo.getInvest_amount();//累计理财
		
		Wealthcircle.addInviteCodeToUser(user, investmentTotal);
		
		long CodeNum = Wealthcircle.getActiveCodeByUserId(userId);
		
		double investmentUseTotal = Wealthcircle.getInvestmentUseTotal(userId);//累计获取邀请码理财金额
		
		long amount = BackstageSet.getCurrentBackstageSet().invite_code_amount;
		double PoorMoney= (amount - (investmentTotal - investmentUseTotal) < 0 ? 0 : amount - (investmentTotal - investmentUseTotal));
		
		PageBean<t_wealthcircle_invite> page = Wealthcircle.queryMyInvitation(user.id, status, type, currPage, pageSize);
		
		renderArgs.put("childId", "child_50");
		renderArgs.put("labId", "lab_8");
		render(investmentTotal, CodeNum, PoorMoney, page, amount);
	}
	
	/**
	 * 我邀请的会员
	 * @param keyWord
	 * @param beginTime
	 * @param endTime
	 * @param currPageStr
	 * @param pageSizeStr
	 */
	public static void invitationMember(String userName, String currPage,String currSize){
		ErrorInfo error=new ErrorInfo();
		User user = User.currUser();
		
		//查询用户成功推广的会员(理财会员)
		PageBean<t_wealthcircle_invite> page = Wealthcircle.queryMyInviteMembers(user.id, userName, currPage, currSize, error);
		
		//查询用户成功邀请的理财用户数
		long totalFinancialMember = Wealthcircle.queryFinanceMember(user.id,error);
		
		//查询用户返佣收益
		double cumulativeFinancialGain = Wealthcircle.queryAccumulatedEarnings(user.id, error);
		
		renderArgs.put("childId", "child_51");
		renderArgs.put("labId", "lab_8");
		render(page, totalFinancialMember, cumulativeFinancialGain);
	}

	
	/**
	 * 我邀请的会员-返佣明细
	 * @param keyWord
	 * @param beginTime
	 * @param endTime
	 * @param currPageStr
	 * @param pageSizeStr
	 */
	public static void myInvitationUserDetails(String invitedUserSign, String beginTime, String endTime, String currPage,String currSize){
		ErrorInfo error=new ErrorInfo();
		User user = User.currUser();

		long invitedUserId = Security.checkSign(invitedUserSign, Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 0){
			renderJSON(error);
		}
		
		//查询用户成功推广的会员(理财会员)
		PageBean<t_wealthcircle_income> page = Wealthcircle.queryMyInviteMemberDetails(user.id, invitedUserId, beginTime, endTime, currPage, currSize, 0,error);
		
		if(error.code < 0){
			renderJSON(error);
		}
		
		invitedUserSign = Security.addSign(invitedUserId, Constants.USER_ID_SIGN);
		
		render(page, invitedUserSign);
	}

}
