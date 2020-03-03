package controllers.supervisor.webContentManager;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.OptionKeys;
import controllers.supervisor.SupervisorController;
import models.v_credit_levels;
import business.AuditItem;
import business.BackstageSet;
import business.CreditLevel;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 信用等级管理
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-6-11
 */
public class CreditLevelManager extends SupervisorController {
	/**
	 * 信用等级列表页
	 */
	public static void creditLevelList(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_credit_levels> pageBean = CreditLevel.queryCreditLevels(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(pageBean);
	}
	
	/**
	 * 添加信用等级初始化
	 */
	public static void addCreditLevelInit() {
		ErrorInfo error = new ErrorInfo();
		List<AuditItem> auditItems = AuditItem.queryEnableAuditItems(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(auditItems);
	}
	
	/**
	 * 添加信用等级
	 * @param name
	 * @param imageFilename
	 * @param minCreditScore
	 * @param minAuditItems
	 * @param isAllowOverdue
	 * @param mustItems
	 * @param suggest
	 */
	public static void addCreditLevel(String name, String imageFilename, int minCreditScore, int minAuditItems,
			boolean isAllowOverdue, String mustItems, String suggest) {
		ErrorInfo error = new ErrorInfo();
		CreditLevel creditLevel = new CreditLevel();
		creditLevel.name = name;
		creditLevel.imageFilename = imageFilename;
		creditLevel.minCreditScore = minCreditScore;
		creditLevel.minAuditItems = minAuditItems;
		creditLevel.isAllowOverdue = isAllowOverdue;
		creditLevel.isEnable = true;
		creditLevel.mustItems = mustItems;
		creditLevel.suggest = suggest;
		creditLevel.create(error);
		
		renderJSON(error);
	}
	
	/**
	 * 编辑信用等级初始化
	 */
	public static void editCreditLevelInit(long id) {
		ErrorInfo error = new ErrorInfo();
		List<AuditItem> auditItems = AuditItem.queryEnableAuditItems(error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		CreditLevel creditLevel = new CreditLevel();
		creditLevel.id = id;
		
		render(auditItems, creditLevel);
	}
	
	/**
	 * 编辑信用等级
	 * @param name
	 * @param imageFilename
	 * @param minCreditScore
	 * @param minAuditItems
	 * @param isAllowOverdue
	 * @param mustItems
	 * @param suggest
	 */
	public static void editCreditLevel(long id, String name, String imageFilename, int minCreditScore, int minAuditItems,
			boolean isAllowOverdue, String mustItems, String suggest) {
		ErrorInfo error = new ErrorInfo();
		CreditLevel creditLevel = new CreditLevel();
		creditLevel.id = id;
		creditLevel.name = name;
		creditLevel.imageFilename = imageFilename;
		creditLevel.minCreditScore = minCreditScore;
		creditLevel.minAuditItems = minAuditItems;
		creditLevel.isAllowOverdue = isAllowOverdue;
		creditLevel.isEnable = true;
		creditLevel.mustItems = mustItems;
		creditLevel.suggest = suggest;
		creditLevel.edit(error);
		
		renderJSON(error);
	}
	
	/**
	 * 暂停/启用信用等级
	 * @param id
	 * @param isEnable
	 */
	public static void enable(long id, boolean isEnable) {
		ErrorInfo error = new ErrorInfo();
		CreditLevel.enable(id, isEnable, error);
		
		renderJSON(error);
	}
	
	/**
	 * 信用规则设置初始化
	 */
	public static void creditRuleSetInit() {
		long auditItemCount = AuditItem.auditItemCount();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		render(backstageSet, auditItemCount);
	}

	/**
	 * 信用规则设置
	 */
	public static void creditRuleSet(Double initialAmount,
			Integer normalPayPoints, Integer fullBidPoints, Integer investpoints,
			Integer overDuePoints, Double creditToMoney) {

		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = new BackstageSet();
		
		if(creditToMoney == null){
			error.code = -1;
			error.msg = "信用积分兑换信用额度必须输入数字";
			
			renderText(error.msg);
		}
		
		if(overDuePoints == null){
			error.code = -1;
			error.msg = "账单逾期扣分必须输入数字";
			
			renderText(error.msg);
		}
		
		if(investpoints == null){
			error.code = -1;
			error.msg = "成功投标积分必须输入数字";
			
			renderText(error.msg);
		}
		
		if(fullBidPoints == null){
			error.code = -1;
			error.msg = "成功借款积分必须输入数字";
			
			renderText(error.msg);
		}
		
		if(normalPayPoints == null){
			error.code = -1;
			error.msg = "正常还款积分必须输入数字";
			
			renderText(error.msg);
		}
		
		if(initialAmount == null){
			error.code = -1;
			error.msg = "初始信用额度必须输入数字";
			
			renderText(error.msg);
		}
		
		backstageSet.initialAmount = initialAmount;
		backstageSet.normalPayPoints = normalPayPoints;
		backstageSet.fullBidPoints = fullBidPoints;
		backstageSet.investpoints = investpoints;
		backstageSet.overDuePoints = overDuePoints;
		backstageSet.creditToMoney = creditToMoney;
		backstageSet.creditRuleSet(error);

		renderText(error.msg);
	}

	/**
	 * 查看积分明细
	 * 
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void pointsDetail(String currPage, String pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> pageBean = AuditItem.queryAuditItems(currPage,
				pageSize, keyword, true, error);

		if (error.code < 0) {
			renderText("出现错误!");
		}
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		double creditToMoney = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

		render(pageBean, creditToMoney);
	}

}
