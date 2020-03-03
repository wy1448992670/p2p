package controllers.supervisor.networkMarketing;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import models.t_statistic_cps;
import models.t_statistic_invitation;
import models.t_users;
import models.t_statistic_invitation_details;
import models.t_wealthcircle_income;
import models.t_wealthcircle_invite;
import models.v_bill_department_month_maturity;
import models.v_user_info;
import play.Logger;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import business.BackstageSet;
import business.User;
import business.Wealthcircle;
import constants.Constants;
import controllers.supervisor.SupervisorController;

public class WealthCircleAction extends SupervisorController{
	
	/**
	 * 财富圈邀请码设置
	 */
	public static void invitationCodeSetting() {
		BackstageSet ds = BackstageSet.queryBackstageSet();
		render(ds);
	};
	
	/**
	 * 保存邀请推广规则设置
	 */
	public static void saveInvitationCode() {
		ErrorInfo error = new ErrorInfo();

		BackstageSet bs = BackstageSet.getCurrentBackstageSet();

		try {
			bs.invite_code_amount = Long.parseLong(params.get("amount"));
			bs.invite_income_rate = Double.parseDouble(params.get("rate"));
			bs.invited_user_discount = Integer.parseInt(params.get("discount"));
			bs.invite_code_period = Integer.parseInt(params.get("period"));

		} catch (Exception e) {
			Logger.info("参数有误：" + e.getMessage());
			error.code = -1;
			error.msg = "";

			renderJSON(error);
		}

		bs.invitationCodeRuleSave(error);

		renderJSON(error);
	};
	
    /**
     * 财富圈邀请码
     */
	public static void inviteCodeInfoList(String userName, int status, int type, 
			String beginTimeStr, String endTimeStr, int currPage, int pageSize, int isExport){
		ErrorInfo error = new ErrorInfo();
		
		PageBean<t_wealthcircle_invite> page = Wealthcircle.queryInviteCodeList(userName, status, type, beginTimeStr, endTimeStr, currPage, pageSize, isExport, error);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<t_wealthcircle_invite> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			File file = ExcelUtils.export("财富圈邀请码",
					arrList,
					new String[] {
					"邀请码", "状态", "来源", "会员","受邀人","受邀理财金额","返佣金额"},
					new String[] {"invite_code", "statusString", "typeString",
					"user_name", "invited_user_name", "total_invest_amount", "total_income"});
			
			renderBinary(file, "财富圈邀请码列表.xls");
		}
		
		
		render(page);
	}
	
	/**
	 * 邀请码详情
	 * @param id
	 */
	public static void inviteCodeDetails(long id){
		t_wealthcircle_invite invite = Wealthcircle.queryInviteCodeInfo(id);
		render(invite);
	}
	
	/**
	 * 查询返佣明细
	 * @param userId
	 * @param invitedUserId
	 * @param beginTime
	 * @param endTime
	 * @param currPageStr
	 * @param pageSizeStr
	 */
	public static void investDetails (long userId, long invitedUserId, String beginTime, String endTime, String currPageStr, String pageSizeStr, int isExport){
		ErrorInfo error = new ErrorInfo();
		PageBean<t_wealthcircle_income> page = Wealthcircle.queryMyInviteMemberDetails(userId, invitedUserId, beginTime, endTime, currPageStr, pageSizeStr, isExport, error);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<t_wealthcircle_income> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			File file = ExcelUtils.export("返佣记录",
					arrList,
					new String[] {
					"投资时间", "投资金额", "返佣金额"},
					new String[] {"invest_time", "invest_amount", "invite_income"});
			
			renderBinary(file, "财富圈邀请码返佣记录.xls");
		}
		
		render(page, userId, invitedUserId);
	}
	
	/**
	 * 佣金发放统计
	 */
	public static void invitationStatistic(int isExport, int year, int month, int currPage) {
		PageBean<t_statistic_invitation> page = Wealthcircle.queryInvitationStatistic(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, year, month, currPage);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<t_statistic_invitation> list = page.page;
			 
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			File file = ExcelUtils.export("财富圈统计表",
			arrList,
			new String[] {
			"年", "月", "新增邀请码数", "新增受邀会员数",
			"新增充值会员数", "新增理财会员数 ", "受邀理财金额 ", "返佣金额"},
			new String[] {"year", "month", "invite_code_count",
			"invited_user_count", "invited_recharge_user_count", 
			"invited_invest_user_count", "invest_amount",
			"invitation_income"});
			   
			renderBinary(file, "财富圈统计表.xls");
		}
		
		render(page);
	}
	
	/**
	 * 佣金发放统计-返佣明细
	 */
	public static void invitationStatisticDetails(int isExport, int year, int month, String userName, int currPage) {
		
		userName = StringUtils.trim(userName);
		
		PageBean<t_statistic_invitation_details> page = Wealthcircle.queryInvitationStatisticDetails(isExport==Constants.IS_EXPORT?Constants.NO_PAGE:0, year, month, userName, currPage);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<t_statistic_invitation_details> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			File file = ExcelUtils.export("财富圈统计表-返佣明细",
					arrList,
					new String[] {
					"用户名", "邀请码数量", "受邀理财金额", "返佣金额"},
					new String[] {"user_name", "total_invite_code", "invited_user_invest_amount",
					"invitation_income"});
			
			renderBinary(file, "财富圈统计表-返佣明细.xls");
		}
		
		render(page);
	}
	
	/**
	 * 更新邀请是否启用
	 * @param id
	 * @param status
	 */
	public static void updateCodeActive(long id, int acvite){
		int result = Wealthcircle.updateCodeActive(id, acvite);
		renderText(result);
	}
	
	/**
	 * 赠送邀请码
	 * @param userId
	 */
	public static void giveInviteCodeToUser(long userId){
		ErrorInfo error = new ErrorInfo();
		
		if (userId <= 0){
			error.code = -1;
			error.msg = "用户id非法";
			
			renderJSON(error);
		}
		
		User user = new User();
		user.id = userId;
		
		if(Constants.IPS_ENABLE && StringUtils.isBlank(user.ipsAcctNo)){
			error.code = -2;
			error.msg = "该用户未开通托管账户，不能赠送邀请码";
			
			renderJSON(error);
		}
		
		Wealthcircle.giveInviteCodeToUser(user, error);
		
		renderJSON(error);
	}
	
	/**
	 * 查询用户信息
	 * @param userName
	 */
	public static void searchUserInfo(String userName){
		ErrorInfo error = new ErrorInfo();
		v_user_info info = new v_user_info();
		if (StringUtils.isEmpty(userName)){
			render(info);
		}
		String sql = " name = ? or mobile = ?";
		t_users user = t_users.find(sql, userName, userName).first();
		if (null != user){
			PageBean<v_user_info> page = User.queryUserBySupervisor(user.name, null, null, null, null, null, null, null, null, null, error);
			if (null != page && null != page.page)
			info = page.page.get(0);
		}
		render(info);
	}
	
}
