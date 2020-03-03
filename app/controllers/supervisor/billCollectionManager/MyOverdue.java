package controllers.supervisor.billCollectionManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import business.Bill;

import play.db.jpa.JPA;
import models.t_bills;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 给测试用的一个类，写的不是很标准，请勿指点!
 * @author bushaorong
 *
 */
public class MyOverdue extends SupervisorController {

	/**
	 * 账单列表
	 */
	public static void billList() {
		if(!Constants.DEV_PROD) {
			renderText("非法操作!");
		}
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		int currPage = null == currPageStr ? 1 : Integer.parseInt(currPageStr);
		int pageSize = null == pageSizeStr ? 10 : Integer.parseInt(pageSizeStr);
		int totalCount = 0;
		
		try {
			totalCount = (int) t_bills.count();
		} catch (Exception e) {
			renderText(e.getMessage());
		}
		
		if(0 == totalCount) {
			renderText("没有数据!");
		}
		
		List<t_bills> bills = null;
		
		try {
			bills = t_bills.find("").fetch(currPage, pageSize);
		} catch (Exception e) {
			renderText(e.getMessage());
		}
		
		if(null == bills || bills.size() == 0) {
			renderText("没有数据!");
		}
		
		PageBean<t_bills> page = new PageBean<t_bills>();
		page.totalCount = totalCount;
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.page = bills;

		render(page);
	}
	
	/**
	 * 修改还款时间
	 */
	public static void updateTime(long id, int day) {
		if(!Constants.DEV_PROD) {
			renderText("非法操作!");
		}
		
		if(day <= 0) {
			renderText("请输入有意义的数字");
		}
		
		if(id <= 0){
			renderText("账单编号为0!");
		}
		
		String sql = "update t_bills set repayment_time = ? where id = ? and status = -1";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, DateUtil.getDateMinusDay(day));
		query.setParameter(2, id);
		int row = 0;
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			renderText(e.getMessage());
		}
		
		if(0 == row) {
			renderText("执行失败!");
		}
		
		renderText("执行成功，请刷新页面查看效果!");
	}
	
	/**
	 * 开始逾期
	 */
	public static void startOverdue() {
		ErrorInfo error = new ErrorInfo();
		new Bill().systemMakeOverdue(error); //系统标记逾期
		flash.error(error.code == 0 ? "执行成功" : error.msg);
		
		billList();
	}
}
