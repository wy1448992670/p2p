package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.*;
import org.apache.commons.lang.StringUtils;
import org.h2.store.Page;

import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.QueryUtil;
import constants.Constants;
import constants.SQLTempletes;
import constants.Constants.SupervisorLevel;
import constants.Constants.SystemSupervisor;
import constants.DealType;
import utils.RegexUtils;

/**
 * 数据统计报表
 * @author lzp
 * @version 6.0
 * @created 2014-7-11
 */
public class StatisticalReport implements Serializable{
	/**
	 * 审核科目库统计分析表
	 * @param currPage
	 * @param pageSize
	 * @param keywordType
	 * @param keyword
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_audit_items> queryAuditItems(int currPage, int pageSize, int year, int month,  
			int keywordType, String keyword, int orderType, int isExport, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (keywordType < 0 || keywordType > Constants.AUDIT_ITEMS_CONDITION.length - 1) {
			keywordType = 0;
		}
		
		if (orderType < 0 || orderType > Constants.AUDIT_ITEMS_ORDER_TYPE.length - 1) {
			orderType = 0;
		}

		String searchCondition = "(1 = 1)";
		String orderCondition = Constants.AUDIT_ITEMS_ORDER_TYPE[orderType];
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> mapCondition = new HashMap<String, Object>();
		mapCondition.put("keywordType", keywordType);
		mapCondition.put("orderType", orderType);
		
		if (StringUtils.isNotBlank(keyword)) {
			mapCondition.put("keyword", keyword);
			searchCondition += Constants.AUDIT_ITEMS_CONDITION[keywordType];
			
			if (0 == keywordType) {
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			} else {
				params.add("%" + keyword + "%");
			}
		}
		
		if (year > 0) {
			mapCondition.put("year", year);
			searchCondition += " and year = ?";
			params.add(year);
		}
		
		if (month > 0) {
			mapCondition.put("month", month);
			searchCondition += " and month = ?";
			params.add(month);
		}
		
		String condition = searchCondition + orderCondition;
		
		List<t_statistic_audit_items> page = null;
		int count = 0;

		try {
			if(isExport == Constants.IS_EXPORT) {
				page = t_statistic_audit_items.find(condition, params.toArray()).fetch();
			} else {
				page = t_statistic_audit_items.find(condition, params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_audit_items.count(condition, params.toArray());
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		PageBean<t_statistic_audit_items> bean = new PageBean<t_statistic_audit_items>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = mapCondition;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 借款数据统计分析对比
	 * @param id
	 * @return
	 */
	public static t_statistic_borrow queryDateForLoan(long id, ErrorInfo error){
		t_statistic_borrow loans = new t_statistic_borrow();
		
		try {
			loans = t_statistic_borrow.findById(id);
		} catch (Exception e) {
			Logger.error("查询借款数据统计分析表时时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询借款数据统计分析表出现异常";
			return null;
		}
		
		return loans;
	}
	
	/**
	 * 借款情况统计分析表
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_borrow> queryBorrows(int currPage, int pageSize, 
			int year, int month, int orderType, int isExport, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (orderType < 0 || orderType > Constants.BORROW_ORDER_TYPE.length - 1) {
			orderType = 0;
		}

		String searchCondition = "(1 = 1)";
		String orderCondition = Constants.BORROW_ORDER_TYPE[orderType];
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> mapCondition = new HashMap<String, Object>();
		mapCondition.put("year", year);
		mapCondition.put("month", month);
		mapCondition.put("orderType", orderType);
		
		if (year > 0) {
			mapCondition.put("year", year);
			searchCondition += " and year = ?";
			params.add(year);
		}
		
		if (month > 0) {
			mapCondition.put("month", month);
			searchCondition += " and month = ?";
			params.add(month);
		}
		
		String condition = searchCondition + orderCondition;
		
		List<t_statistic_borrow> page = null;
		int count = 0;

		try {
			if(isExport == Constants.IS_EXPORT) {
				page = t_statistic_borrow.find(condition, params.toArray()).fetch();
			} else {
				page = t_statistic_borrow.find(condition, params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_borrow.count(condition, params.toArray());
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		PageBean<t_statistic_borrow> bean = new PageBean<t_statistic_borrow>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = mapCondition;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 借款标销售情况分析表
	 * @param currPage
	 * @param pageSize
	 * @param keywordType
	 * @param keyword
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_product> queryProducts(int currPage, int pageSize, int year, int month,
			int keywordType, String keyword, int orderType, int isExport, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (keywordType < 0 || keywordType > Constants.PRODUCT_CONDITION.length - 1) {
			keywordType = 0;
		}
		
		if (orderType < 0 || orderType > Constants.PRODUCT_ORDER_TYPE.length - 1) {
			orderType = 0;
		}
		
		String searchCondition = "(1 = 1)";
		String orderCondition = Constants.PRODUCT_ORDER_TYPE[orderType];
		List<Object> params = new ArrayList<Object>();

		if (StringUtils.isNotBlank(keyword)) {
			searchCondition += Constants.PRODUCT_CONDITION[keywordType];
			
			if (0 == keywordType) {
				params.add("%" + keyword + "%");
			} else {
				params.add("%" + keyword + "%");
			}
		}
		
		if (year > 0) {
			searchCondition += " and year = ?";
			params.add(year);
		}
		
		if (month > 0) {
			searchCondition += " and month = ?";
			params.add(month);
		}
		
		String condition = searchCondition + orderCondition;
		
		List<t_statistic_product> page = null;
		int count = 0;

		try {
			if(isExport == Constants.IS_EXPORT) {
				page = t_statistic_product.find(condition, params.toArray()).fetch();
			} else {
				page = t_statistic_product.find(condition, params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_product.count(condition, params.toArray());
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("keywordType", keywordType);
		map.put("orderType", orderType);
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<t_statistic_product> bean = new PageBean<t_statistic_product>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	
	/**
	 * 数据统计提现表条件查询
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param day
	 * @param startDateStr
	 * @param endDateStr
	 * @param order
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_withdraw> queryWIthdraw(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport, ErrorInfo error){
		
		String orderType[] = {" order by id desc "," order by id desc "," order by payment_sum desc", " order by payment_sum ",
				           " order by payment_number desc"," order by payment_number ","order by apply_withdraw_account desc","order by apply_withdraw_account ",
				            "order by apply_withdraw_sum desc","order by apply_withdraw_sum "};
		
		String monthCondition[] = { " ","and month = 1","and month = 2","and month = 3","and month = 4","and month = 5","and month = 6","and month = 7","and month = 8",
				                 "and month = 9","and month = 10","and month = 11","and month = 12","and month in (1,2,3)","and month in (4,5,6)","and month in (7,8,9)","and month in (10,11,12)"};
		
		PageBean<t_statistic_withdraw> pageBean = new PageBean<t_statistic_withdraw>();
		StringBuffer searchCondition = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		
		if(year > 0){
			searchCondition.append(" and year = ? ");
			params.add(year);
		}
		
		if(month > 0){
			searchCondition.append(monthCondition[month]);
		}
		
		if(day > 0){
			searchCondition.append(" and day = ? ");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)){
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(startDateStr);
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endDateStr);
		}
		
		if(order < 0 || order > 10){
			searchCondition.append(orderType[0]);
		}else{
			searchCondition.append(orderType[order]);
		}
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if(month < 0 || month > 16){
			month = 0;
		}
		
		if(day < 0 || day > 31){
			day = 0;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("day", day);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("orderType", order);
		
		List<t_statistic_withdraw> withdraws = new ArrayList<t_statistic_withdraw>();
		
		Integer count = 0;
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				withdraws = t_statistic_withdraw.find(searchCondition.toString(), params.toArray()).fetch();
			} else {
				withdraws = t_statistic_withdraw.find(searchCondition.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_withdraw.count(searchCondition.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		pageBean.page = withdraws;
		pageBean.conditions = map;
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		pageBean.totalCount = count;
		
		error.code = 0;
		
		return pageBean;
	}
	
	
	
	/**
	 * 数据统计浮存金表条件查询
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param day
	 * @param startDateStr
	 * @param endDateStr
	 * @param order
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_platform_float> queryFloat(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport, ErrorInfo error){
		
		String orderType[] = {" order by id desc "," order by id desc "," order by id desc", " order by id ",
				           " order by balance_float_sum desc"," order by balance_float_sum ","order by freeze_float_sum desc","order by freeze_float_sum ",
				            "order by float_sum desc","order by float_sum "};
		
		String monthCondition[] = { " ","and month = 1","and month = 2","and month = 3","and month = 4","and month = 5","and month = 6","and month = 7","and month = 8",
				                 "and month = 9","and month = 10","and month = 11","and month = 12","and month in (1,2,3)","and month in (4,5,6)","and month in (7,8,9)","and month in (10,11,12)"};
		
		PageBean<t_statistic_platform_float> pageBean = new PageBean<t_statistic_platform_float>();
		StringBuffer searchCondition = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		
		if(year > 0){
			searchCondition.append(" and year = ? ");
			params.add(year);
		}
		
		if(month > 0){
			searchCondition.append(monthCondition[month]);
		}
		
		if(day > 0){
			searchCondition.append(" and day = ? ");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)){
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(startDateStr);
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endDateStr);
		}
		
		if(order < 0 || order > 10){
			searchCondition.append(orderType[0]);
		}else{
			searchCondition.append(orderType[order]);
		}
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if(month < 0 || month > 16){
			month = 0;
		}
		
		if(day < 0 || day > 31){
			day = 0;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("day", day);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("orderType", order);
		
		List<t_statistic_platform_float> floats = new ArrayList<t_statistic_platform_float>();
		
		Integer count = 0;
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				floats = t_statistic_platform_float.find(searchCondition.toString(), params.toArray()).fetch();
			} else {
				floats = t_statistic_platform_float.find(searchCondition.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_platform_float.count(searchCondition.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		pageBean.page = floats;
		pageBean.conditions = map;
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		pageBean.totalCount = count;
		
		error.code = 0;
		
		return pageBean;
	}
	
	/**
	 * 理财数据统计分析对比
	 * @param id
	 * @return
	 */
	public static t_statistic_financial_situation queryDateForInvest(long id, ErrorInfo error){
		t_statistic_financial_situation invests = new t_statistic_financial_situation();
		
		try {
			invests = t_statistic_financial_situation.findById(id);
		} catch (Exception e) {
			Logger.error("查询理财数据统计分析表时时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询理财数据统计分析表出现异常";
			
			return null;
		}
		
		return invests;
	}
	
	/**
	 * 数据统计理财情况统计表条件查询
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param day
	 * @param startDateStr
	 * @param endDateStr
	 * @param order
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_financial_situation> queryInvest(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order, int isExport, ErrorInfo error){
		
		String orderType[] = {" order by id desc "," order by id desc "," order by id asc desc", " order by id ",
				           " order by invest_accoumt desc"," order by invest_accoumt ","order by increase_invest_user_account desc","order by increase_invest_user_account ",
				            "order by invest_user_conversion desc","order by invest_user_conversion "};
		
		String monthCondition[] = { " ","and month = 1","and month = 2","and month = 3","and month = 4","and month = 5","and month = 6","and month = 7","and month = 8",
				                 "and month = 9","and month = 10","and month = 11","and month = 12","and month in (1,2,3)","and month in (4,5,6)","and month in (7,8,9)","and month in (10,11,12)"};
		
		PageBean<t_statistic_financial_situation> pageBean = new PageBean<t_statistic_financial_situation>();
		StringBuffer searchCondition = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		
		if(year > 0){
			searchCondition.append(" and year = ? ");
			params.add(year);
		}
		
		if(month > 0){
			searchCondition.append(monthCondition[month]);
		}
		
		
		
		if(StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)){
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month),'%Y-%m') >= ?");
			params.add(startDateStr);
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month),'%Y-%m') <= ?");
			params.add(endDateStr);
		}
		
		if(order < 0 || order > 10){
			searchCondition.append(orderType[0]);
		}else{
			searchCondition.append(orderType[order]);
		}
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if(month < 0 || month > 16){
			month = 0;
		}
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("orderType", order);
		
		List<t_statistic_financial_situation> floats = new ArrayList<t_statistic_financial_situation>();
		
		Integer count = 0;
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				floats = t_statistic_financial_situation.find(searchCondition.toString(), params.toArray()).fetch();
			} else {
				floats = t_statistic_financial_situation.find(searchCondition.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_financial_situation.count(searchCondition.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		pageBean.page = floats;
		pageBean.conditions = map;
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		pageBean.totalCount = count;
		
		error.code = 0;
		
		return pageBean;
	}
	
	
	
	
	/**
	 * 数据统计债权转让情况统计表条件查询
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param day
	 * @param startDateStr
	 * @param endDateStr
	 * @param order
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_debt_situation> queryDebt(int currPage, int pageSize, int year, int month,
			String startDateStr,String endDateStr,int order, int isExport, ErrorInfo error){
		
		String orderType[] = {" order by id desc "," order by id desc "," order by id desc", " order by id ",
				           " order by deal_percent desc"," order by deal_percent ","order by transfer_percent desc","order by transfer_percent " };
				           
		
		String monthCondition[] = { " ","and month = 1","and month = 2","and month = 3","and month = 4","and month = 5","and month = 6","and month = 7","and month = 8",
				                 "and month = 9","and month = 10","and month = 11","and month = 12","and month in (1,2,3)","and month in (4,5,6)","and month in (7,8,9)","and month in (10,11,12)"};
		
		PageBean<t_statistic_debt_situation> pageBean = new PageBean<t_statistic_debt_situation>();
		StringBuffer searchCondition = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		
		if(year > 0){
			searchCondition.append(" and year = ? ");
			params.add(year);
		}
		
		if(month > 0){
			searchCondition.append(monthCondition[month]);
		}
		
		
		
		if(StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)){
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month),'%Y-%m') > ?");
			params.add(startDateStr);
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month),'%Y-%m') < ?");
			params.add(endDateStr);
		}
		
		if(order < 0 || order > 10){
			searchCondition.append(orderType[0]);
		}else{
			searchCondition.append(orderType[order]);
		}
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if(month < 0 || month > 16){
			month = 0;
		}
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("orderType", order);
		
		List<t_statistic_debt_situation> floats = new ArrayList<t_statistic_debt_situation>();
		
		Integer count = 0;
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				floats = t_statistic_debt_situation.find(searchCondition.toString(), params.toArray()).fetch();
			} else {
				floats = t_statistic_debt_situation.find(searchCondition.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_debt_situation.count(searchCondition.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		pageBean.page = floats;
		pageBean.conditions = map;
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		pageBean.totalCount = count;
		
		error.code = 0;
		
		return pageBean;
	}
	
	/**
	 * 数据统计平台收入表条件查询
	 * @param currPage
	 * @param pageSize
	 * @param year
	 * @param month
	 * @param day
	 * @param startDateStr
	 * @param endDateStr
	 * @param order
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_platform_income> queryIncome(int currPage, int pageSize, int year, int month,int day,
			String startDateStr,String endDateStr,int order, int isExport, ErrorInfo error){
		
		String orderType[] = {" order by id desc "," order by id desc "," order by income_sum desc", " order by income_sum "};
				           
		
		String monthCondition[] = { " ","and month = 1","and month = 2","and month = 3","and month = 4","and month = 5","and month = 6","and month = 7","and month = 8",
				                 "and month = 9","and month = 10","and month = 11","and month = 12","and month in (1,2,3)","and month in (4,5,6)","and month in (7,8,9)","and month in (10,11,12)"};
		
		PageBean<t_statistic_platform_income> pageBean = new PageBean<t_statistic_platform_income>();
		StringBuffer searchCondition = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		
		if(year > 0){
			searchCondition.append(" and year = ? ");
			params.add(year);
		}
		
		if(month > 0){
			searchCondition.append(monthCondition[month]);
		}
		
		if(day > 0){
			searchCondition.append(" and day = ? ");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(startDateStr) && StringUtils.isNotBlank(endDateStr)){
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(startDateStr);
			searchCondition.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endDateStr);
		}
		
		if(order < 0 || order > 10){
			searchCondition.append(orderType[0]);
		}else{
			searchCondition.append(orderType[order]);
		}
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if(month < 0 || month > 16){
			month = 0;
		}
		
		if(day < 0 || day > 31){
			day = 0;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("year", year);
		map.put("month", month);
		map.put("day", day);
		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("orderType", order);
		
		List<t_statistic_platform_income> withdraws = new ArrayList<t_statistic_platform_income>();
		
		Integer count = 0;
		
		try {
			if(isExport == Constants.IS_EXPORT) { 
				withdraws = t_statistic_platform_income.find(searchCondition.toString(), params.toArray()).fetch();
			} else {
				withdraws = t_statistic_platform_income.find(searchCondition.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_platform_income.count(searchCondition.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		pageBean.page = withdraws;
		pageBean.conditions = map;
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		pageBean.totalCount = count;
		
		error.code = 0;
		
		return pageBean;
	}
	
	/**
	 * 充值统计表
	 * @param currPage
	 * @param pageSizeTrans
	 * @param year
	 * @param month
	 * @param day
	 * @param beginTimeStr
	 * @param endTimeStr
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_recharge> queryRecharge(int currPage, int pageSizeTrans, int year, int month,int day,
			String beginTimeStr,String endTimeStr,int orderType, int isExport, ErrorInfo error){
        error.clear();
		
        Date beginTime = null;
        Date endTime = null;
        int pageSize = Constants.TEN;
        int count = 0;
        
        Map<String, Object> conditionsMap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		List<t_statistic_recharge> rechargeList = new ArrayList<t_statistic_recharge>();
		PageBean<t_statistic_recharge> page = new PageBean<t_statistic_recharge>();
		
		conditionsMap.put("year", year);
		conditionsMap.put("month", month);
		conditionsMap.put("day", day);
		conditionsMap.put("beginTime", beginTimeStr);
		conditionsMap.put("endTime", endTimeStr);
		conditionsMap.put("orderType", orderType);
		
		if(currPage < 1){
			currPage = 1;
		}
		
		if(year > 0){
			conditions.append(" and year = ?");
			params.add(year);
		}
		
		if(month > 0 && month < 13){
			conditions.append(" and month = ?");
			params.add(month);
		}
		
		if(month > 12 && month < 17){
			switch (month){
			case 13:
				conditions.append(" and month in(1,2,3)");
				break;
				
			case 14:
				conditions.append(" and month in(4,5,6)");
				break;
				
			case 15:
				conditions.append(" and month in(7,8,9)");
				break;
				
			case 16:
				conditions.append(" and month in(10,11,12)");
				break;
			}
		}
		
		if(day > 0){
			conditions.append(" and day = ?");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(beginTimeStr)){
			beginTime = DateUtil.strToYYMMDDDate(beginTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(beginTime);
		}
		
		if(StringUtils.isNotBlank(endTimeStr)){
			endTime = DateUtil.strToYYMMDDDate(endTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endTime);
		}
		
		if(orderType < 0 || orderType > 9){
			orderType = 0;
			conditions.append(Constants.RECHARGE_ORDER_TYPE[orderType]);
		}
		
		conditions.append(Constants.RECHARGE_ORDER_TYPE[orderType]);
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				rechargeList = t_statistic_recharge.find(conditions.toString(), params.toArray()).fetch();
			} else {
				count = (int) t_statistic_recharge.count(conditions.toString(), params.toArray());
				rechargeList = t_statistic_recharge.find(conditions.toString(), params.toArray()).fetch(currPage, pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询充值统计表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询充值统计表出现异常！";
			
			return null;
		}
		
		page.currPage = currPage;
		page.pageSize = pageSize ;
		page.totalCount = count;
		page.conditions = conditionsMap;
		page.page = rechargeList;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 保障本金统计分析表
	 * @param currPage
	 * @param pageSizeTrans
	 * @param year
	 * @param month
	 * @param day
	 * @param beginTimeStr
	 * @param endTimeStr
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_security> querySecurity(int currPage, int pageSizeTrans, int year, int month,int day,
			String beginTimeStr,String endTimeStr,int orderType, int isExport, ErrorInfo error){
        error.clear();
		
        Date beginTime = null;
        Date endTime = null;
        int pageSize = Constants.TEN;
        int count = 0;
        
        Map<String, Object> conditionsMap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		List<t_statistic_security> rechargeList = new ArrayList<t_statistic_security>();
		PageBean<t_statistic_security> page = new PageBean<t_statistic_security>();
		
		conditionsMap.put("year", year);
		conditionsMap.put("month", month);
		conditionsMap.put("day", day);
		conditionsMap.put("beginTime", beginTimeStr);
		conditionsMap.put("endTime", endTimeStr);
		conditionsMap.put("orderType", orderType);
		
		if(currPage < 1){
			currPage = 1;
		}
		
		if(year > 0){
			conditions.append(" and year = ?");
			params.add(year);
		}
		
		if(month > 0 && month <13){
			conditions.append(" and month = ?");
			params.add(month);
		}
		
		if(month > 12 && month < 17){
			switch (month){
			case 13:
				conditions.append(" and month in(1,2,3)");
				break;
				
			case 14:
				conditions.append(" and month in(4,5,6)");
				break;
				
			case 15:
				conditions.append(" and month in(7,8,9)");
				break;
				
			case 16:
				conditions.append(" and month in(10,11,12)");
				break;
			}
		}
		
		if(day > 0){
			conditions.append(" and day = ?");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(beginTimeStr)){
			beginTime = DateUtil.strToYYMMDDDate(beginTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(beginTime);
		}
		
		if(StringUtils.isNotBlank(endTimeStr)){
			endTime = DateUtil.strToYYMMDDDate(endTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endTime);
		}
		
		if(orderType < 0 || orderType > 9){
			orderType = 0;
			conditions.append(Constants.SECURITY_ORDER_TYPE[orderType]);
		}
		
		conditions.append(Constants.SECURITY_ORDER_TYPE[orderType]);
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				rechargeList = t_statistic_security.find(conditions.toString(), params.toArray()).fetch();
			} else {
				rechargeList = t_statistic_security.find(conditions.toString(), params.toArray()).fetch(currPage, pageSize);
				count = (int) t_statistic_security.count(conditions.toString(), params.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询保障本金统计分析表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询保障本金统计分析表出现异常！";
			
			return null;
		}
		
		page.currPage = currPage;
		page.pageSize = pageSize ;
		page.totalCount = count;
		page.conditions = conditionsMap;
		
		page.page = rechargeList;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 会员数据统计分析表
	 * @param currPage
	 * @param pageSizeTrans
	 * @param year
	 * @param month
	 * @param day
	 * @param beginTimeStr
	 * @param endTimeStr
	 * @param orderType
	 * @param error
	 * @return
	 */
	public static PageBean<t_statistic_member> queryMember(int currPage, int pageSizeTrans, int year, int month,int day,
			String beginTimeStr,String endTimeStr,int orderType, int isExport, ErrorInfo error){
        error.clear();
		
        Date beginTime = null;
        Date endTime = null;
        int pageSize = Constants.TEN;
        int count = 0;
        
        Map<String, Object> conditionsMap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		List<t_statistic_member> rechargeList = new ArrayList<t_statistic_member>();
		PageBean<t_statistic_member> page = new PageBean<t_statistic_member>();
		
		conditionsMap.put("year", year);
		conditionsMap.put("month", month);
		conditionsMap.put("day", day);
		conditionsMap.put("beginTime", beginTimeStr);
		conditionsMap.put("endTime", endTimeStr);
		conditionsMap.put("orderType", orderType);
		
		if(currPage < 1){
			currPage = 1;
		}
		
		if(year > 0){
			conditions.append(" and year = ?");
			params.add(year);
		}
		
		if(month > 0 && month <13){
			conditions.append(" and month = ?");
			params.add(month);
		}
		
		if(month > 12 && month < 17){
			switch (month){
			case 13:
				conditions.append(" and month in(1,2,3)");
				break;
				
			case 14:
				conditions.append(" and month in(4,5,6)");
				break;
				
			case 15:
				conditions.append(" and month in(7,8,9)");
				break;
				
			case 16:
				conditions.append(" and month in(10,11,12)");
				break;
			}
		}
		
		if(day > 0){
			conditions.append(" and day = ?");
			params.add(day);
		}
		
		if(StringUtils.isNotBlank(beginTimeStr)){
			beginTime = DateUtil.strToYYMMDDDate(beginTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
			params.add(beginTime);
		}
		
		if(StringUtils.isNotBlank(endTimeStr)){
			endTime = DateUtil.strToYYMMDDDate(endTimeStr);
			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
			params.add(endTime);
		}
		
		if(orderType < 0 || orderType > 7){
			orderType = 0;
			conditions.append(Constants.MEMBER_ORDER_TYPE[orderType]);
		}
		
		conditions.append(Constants.MEMBER_ORDER_TYPE[orderType]);
		
		try {
			if(isExport == Constants.IS_EXPORT) {
				rechargeList = t_statistic_member.find(conditions.toString(), params.toArray()).fetch();
			} else {
				count = (int) t_statistic_member.count(conditions.toString(), params.toArray());
				rechargeList = t_statistic_member.find(conditions.toString(), params.toArray()).fetch(currPage, pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询会员数据统计分析表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询会员数据统计分析表出现异常！";
			
			return null;
		}
		
		page.currPage = currPage;
		page.pageSize = pageSize ;
		page.totalCount = count;
		page.conditions = conditionsMap;
		
		page.page = rechargeList;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 会员数据统计分析对比
	 * @param id
	 * @return
	 */
	public static t_statistic_member queryDateForMember(long id, ErrorInfo error){
		t_statistic_member member = new t_statistic_member();
		
		try {
			member = t_statistic_member.findById(id);
		} catch (Exception e) {
			Logger.error("查询会员数据统计分析表时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询会员数据统计分析表出现异常";
			return null;
		}
		
		return member;
	}
	
	/**
	 * 获取所有提现总额以及最新日期
	 * @return
	 */
     public static List<Object[]> queryPaymentSum(){
		
		String sql = "SELECT `year`,`month`,`day`,payment_sum FROM t_statistic_withdraw ORDER BY id DESC";

		List<Object[]> records = new ArrayList<Object[]>();
		
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql);
		
		try {
			records = query.getResultList();
		} catch (Exception e) {
			Logger.error("提现统计表->查询提现总额时：" + e.getMessage());
			
			return null;
		}
		
		if (null == records || records.size() == 0) {
			
			return null;
		}
		
		return records;
	}
     
     
     /**
      * 平台收入总计以及最近时间
      * @return
      */
	 public static List<Object[]> queryPlatformAllIncomeAndTime(){
		
		 String sql = "SELECT `year`,`month`,`day`,income_sum FROM t_statistic_platform_income ORDER BY id DESC";
		
		 List<Object[]> records = new ArrayList<Object[]>();
		 
		 EntityManager em = JPA.em();
		 Query query = em.createNativeQuery(sql);
		 
		 try {
			records = query.getResultList();
		} catch (Exception e) {
			Logger.error("平台收入统计表->查询平台总收入时：" + e.getMessage());
			
			return null;
		}
		 
		 if (null == records || records.size() == 0) {
			
			 return null;
			 
		}
		
		return records;
	 }
	 

	 /**
	  * 查询浮存金报表页面相关参数
	  * @return
	  */
	 public static Map<String,Object>  queryFloatParamter(){
		Map<String,Object> map = new HashMap<String, Object>();
		double balance = 0.0d;//账户可用余额浮存
	    double freeze = 0.0d;//冻结资金浮存
		double sum = 0.0d;//浮存金总额
		 
		Object record = null;
		 
		List<Object[]> date;
		String sql2="SELECT balance_float_sum,freeze_float_sum,float_sum,year,month,day FROM `t_statistic_platform_float` ORDER BY id DESC ";
		Query query=JPA.em().createNativeQuery(sql2);
		try {
			date = query.getResultList();
		} catch (Exception e) {
			Logger.error("查询保障本金统计最新数据时间时:" + e.getMessage());			
			
			return null;
		}
		
	   if(null == date || date.size() == 0 || null == date.get(0) ){
		  
		  return null;
	   }
		  
	   record = date.get(0)[0];
	   if (null != record) {
		balance = Double.parseDouble(record.toString());
	  }	  
	 
	   record = date.get(0)[1];  
	   if (null != record) {
		freeze = Double.parseDouble(record.toString());
	  }
	   
	   record = date.get(0)[2];
	   if (null != record) {
		sum = Double.parseDouble(record.toString());
	 }
		  				 
	   map.put("balance", balance);
	   map.put("freeze", freeze);
	   map.put("sum", sum);
	   map.put("year", date.get(0)[3].toString()) ;
	   map.put("month",date.get(0)[4].toString());
	   map.put("day",date.get(0)[5].toString());	
		 
	   return map;
	 }


	public static PageBean<t_statistic_data_disclosure> queryDataDisclosure(int currPage, int pageSizeTrans, String beginTimeStr, String endTimeStr, int orderType, int isExport, ErrorInfo error){
		error.clear();

		Date beginTime = null;
		Date endTime = null;
		int pageSize = Constants.TEN;
		int count = 0;

		Map<String, Object> conditionsMap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" 1 = 1 ");
		List<Object> params = new ArrayList<Object>();
		List<t_statistic_data_disclosure> rechargeList = new ArrayList<t_statistic_data_disclosure>();
		PageBean<t_statistic_data_disclosure> page = new PageBean<t_statistic_data_disclosure>();


		if(RegexUtils.isDate(beginTimeStr)) {
			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
		}

		if(RegexUtils.isDate(endTimeStr)) {
			endTime = DateUtil.strDateToEndDate(endTimeStr);
		}

//		conditionsMap.put("year", year);
//		conditionsMap.put("month", month);
//		conditionsMap.put("day", day);

		conditionsMap.put("beginTime", beginTimeStr);
		conditionsMap.put("endTime", endTimeStr);
		conditionsMap.put("orderType", orderType);

		if(currPage < 1){
			currPage = 1;
		}

//		if(year > 0){
//			conditions.append(" and year = ?");
//			params.add(year);
//		}
//
//		if(month > 0 && month <13){
//			conditions.append(" and month = ?");
//			params.add(month);
//		}
//
//		if(month > 12 && month < 17){
//			switch (month){
//				case 13:
//					conditions.append(" and month in(1,2,3)");
//					break;
//
//				case 14:
//					conditions.append(" and month in(4,5,6)");
//					break;
//
//				case 15:
//					conditions.append(" and month in(7,8,9)");
//					break;
//
//				case 16:
//					conditions.append(" and month in(10,11,12)");
//					break;
//			}
//		}
//
//		if(day > 0){
//			conditions.append(" and day = ?");
//			params.add(day);
//		}
//
//		if(StringUtils.isNotBlank(beginTimeStr)){
//			beginTime = DateUtil.strToYYMMDDDate(beginTimeStr);
//			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') >= ?");
//			params.add(beginTime);
//		}
//
//		if(StringUtils.isNotBlank(endTimeStr)){
//			endTime = DateUtil.strToYYMMDDDate(endTimeStr);
//			conditions.append(" and STR_TO_DATE(CONCAT(year,'-',month,'-',day),'%Y-%m-%d') <= ?");
//			params.add(endTime);
//		}
//
//		if(orderType < 0 || orderType > 7){
//			orderType = 0;
//			conditions.append(Constants.MEMBER_ORDER_TYPE[orderType]);
//		}


		if(beginTime != null) {
			conditions.append("and the_date >= ? ");
			params.add(beginTime);
		}

		if(endTime != null) {
			conditions.append("and the_date <= ? ");
			params.add(endTime);
		}

		conditions.append(Constants.MEMBER_ORDER_TYPE[orderType]);

		try {
			if(isExport == Constants.IS_EXPORT) {
				rechargeList = t_statistic_data_disclosure.find(conditions.toString(), params.toArray()).fetch();
			} else {
				count = (int) t_statistic_data_disclosure.count(conditions.toString(), params.toArray());
				rechargeList = t_statistic_data_disclosure.find(conditions.toString(), params.toArray()).fetch(currPage, pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询信披数据统计分析表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询信披数据统计分析表出现异常！";

			return null;
		}

		page.currPage = currPage;
		page.pageSize = pageSize ;
		page.totalCount = count;
		page.conditions = conditionsMap;

		page.page = rechargeList;

		error.code = 0;

		return page;
	}
	
	
	
	public static PageBean<v_migrate_user> queryMigrateUser(int currPage, int pageSize, String keyword,String startDateStr,String endDateStr, ErrorInfo error){
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		Date beginTime = null;
	    Date endTime = null;
	 
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select users.id as id, \n" + 
				"	users.reality_name as realityName,\n" + 
				"	users.name,\n" + 
				"	users.mobile,\n" + 
				"	users.id_number AS  idNumber, \n" + 
				"	users.migration_time AS migrationTime ,\n" + 
				" 	ifnull((select sum(detail.amount) from t_user_details detail where detail.user_id = users.id and detail.operation = 330),0) as afterMigrationRepay,\n" + 
				" 	ifnull((select detail.amount from t_user_details detail where detail.user_id = users.id and detail.operation = 331),0) as migrationAmount,\n" + 
				" 	(select count(1) as count FROM migration_investment_bill_vo vo  WHERE vo.isReceiveBefore = 1 and vo.userId = users.id GROUP BY vo.userId) as  repayedBillCount,\n" + 
				" 	ifnull(repay_bill.receiveCorpus,0) AS receiveCorpus,\n" + 
				" 	ifnull(repay_bill.receiveInterest,0) AS receiveInterest,\n" + 
				"  	(select count(1)  from migration_investment_vo vo  where vo.userId = users.id GROUP BY userId ) investCount,\n" + 
				" 	repay_bill.count as  noRepayBillCount \n" + 
				" from t_users users \n" + 
				" 	left join (SELECT \n" + 
				"  	count(vo.id) as count,\n" + 
				"  	vo.userId as userId, \n" + 
				"	sum(ifnull(vo.receiveCorpus,0)) as receiveCorpus,\n" + 
				"	sum(ifnull(vo.receiveIncreaseInterest,0) + ifnull(vo.receiveInterest,0)) as receiveInterest\n" + 
				"FROM\n" + 
				"	migration_investment_bill_vo vo \n" + 
				"WHERE\n" + 
				"	vo.isReceiveBefore = 0\n" + 
				"GROUP BY vo.userId) repay_bill on repay_bill.userId = users.id  \n" +  
				"where users.is_migration = 1 ");
		
//		params.add(DealType.CHARGE_AUTO_REPAYMENT_WITHDRAW);
		
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and users.name like ? or users.reality_name like ? or users.mobile like ? ");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%");
			params.add("%" + keyword + "%"); 
		}
		if(StringUtils.isNotBlank(startDateStr)){
			beginTime = DateUtil.strToYYMMDDDate(startDateStr);
			sql.append(" and users.migration_time >= ? ");
			params.add(beginTime);
		}
		
		if(StringUtils.isNotBlank(endDateStr)){
			endTime = DateUtil.strToYYMMDDDate(endDateStr);
			sql.append(" and users.migration_time <= ? ");
			params.add(endTime);
		}
		int count = 0;
		List<v_migrate_user> page = null;
		try {
			EntityManager em = JPA.em();
			System.out.println(sql);
            Query query = em.createNativeQuery(sql.toString(), v_migrate_user.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("startDateStr", startDateStr);
		map.put("endDateStr", endDateStr);
		map.put("keyword", keyword);
		
		PageBean<v_migrate_user> bean = new PageBean<v_migrate_user>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		error.code = 0;
		return bean;
	}
}
