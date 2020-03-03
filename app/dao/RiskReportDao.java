/******************************************************************
 *
 *    Java Lib For China, Powered By Chinese Programmer.
 *
 *    Copyright (c) 2001-2099 Digital Telemedia Co.,Ltd
 *    http://www.china.com/
 *
 *    Package:     dao
 *
 *    Filename:    UserCityDao.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2019年1月3日 下午4:12:36
 *
 *    Revision:
 *
 *    2019年1月3日 下午4:12:36
 *        - first revision
 *
 *****************************************************************/
package dao;

import models.risk.t_risk_report;

/**
 * @ClassName UserCityDao
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2019年1月3日 下午4:12:36
 * @version 1.0.0
 */
public class RiskReportDao {

	/**
	 * @Description 新增记录
	 * @param risk_report
	 * @return {@link t_risk_report}
	 * @author: zj
	 */
	public static t_risk_report addRiskReport(t_risk_report risk_report) {
		t_risk_report report = new t_risk_report();
		report.user_id = risk_report.user_id;
		report.is_valid = risk_report.is_valid;
		report.report_response = risk_report.report_response;
		report.status = risk_report.status;
		report.h5_response_task_id=risk_report.h5_response_task_id;
		return report.save();
	}

	public static t_risk_report updateRiskReport(t_risk_report risk_report) {
		t_risk_report report = new t_risk_report();
		report.user_id = risk_report.user_id;
		report.is_valid = risk_report.is_valid;
		report.report_response = risk_report.report_response;
		report.status = risk_report.status;
		return report.save();
	}

	/**
	 * @Description 查询资信报告
	 * @param userId
	 * @param status
	 * @param isValid
	 * @return
	 * @author: zj
	 */
	public static t_risk_report getRiskReport(long userId, int status, int isValid) {
		t_risk_report report = (t_risk_report) t_risk_report
				.find(" user_id=? and status=? and is_valid=? ", userId, status, isValid).first();
		return report;
	}

	/**
	 * @Description 根据状态 删除记录
	 * @param userId
	 * @param status
	 * @param isValid
	 * @author: zj
	 */
	public static void delRiskReport(long userId, int status, int isValid) {
		t_risk_report.delete(" user_id=? and status=? and is_valid=? ", userId, status, isValid);
	}

	/**
	 * @Description 根据状态 删除记录
	 * @param userId
	 * @param status
	 * @param isValid
	 * @author: zj
	 */
	public static void delRiskReport(long userId, int status) {
		t_risk_report.delete(" user_id=? and status=? ", userId, status);
	}
}
