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
package services;

import static org.hamcrest.CoreMatchers.nullValue;

import com.alibaba.fastjson.JSONObject;

import business.ReportUserInfo;
import constants.RiskReportIsValidStatusEnum;
import constants.RiskReportStatusEnum;
import dao.RiskReportDao;
import models.t_report_user_info;
import models.t_users;
import models.risk.t_risk_report;
import play.Logger;
import play.Play;
import utils.mjkj.MjkjDao;

/**
 * @ClassName UserCityDao
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2019年1月3日 下午4:12:36
 * @version 1.0.0
 */
public class RiskReportService {

	/**
	 * @Description 新增记录
	 * @param risk_report
	 * @return {@link t_risk_report}
	 * @author: zj
	 */
	public static t_risk_report addRiskReport(t_risk_report risk_report) {
		updateRiskReport(risk_report.user_id, RiskReportStatusEnum.SUCCESS.getCode(),
				RiskReportIsValidStatusEnum.VALID.getCode());
		return RiskReportDao.addRiskReport(risk_report);
	}

	/**
	 * @Description 更新记录状态 将之前用户id有效的记录设置为无效
	 * @param userId
	 * @param status
	 * @param isValid
	 * @author: zj
	 */
	public static void updateRiskReport(long userId, int status, int isValid) {
		t_risk_report report = RiskReportDao.getRiskReport(userId, status, isValid);
		if (report != null) {
			report.is_valid = RiskReportIsValidStatusEnum.INVALID.getCode();
			report.save();
		}
	}

	/**
	 * @Description 获取一条有效的report 数据
	 * @param userId
	 * @param status  1 成功 2 失败 3 创建
	 * @param isValid 1 有效 2 无效
	 * @return {@link t_risk_report}
	 * @author: zj
	 */
	public static t_risk_report getRiskReport(long userId, int status, int isValid) {
		t_risk_report report = RiskReportDao.getRiskReport(userId, status, isValid);
		if (report != null && report.report_response != null) {
			JSONObject jObject = JSONObject.parseObject(report.report_response);
			if (jObject.containsKey("message")) {
				String message = jObject.getString("message");
				report.reportUrl = Play.configuration.getProperty("zyy.mjkj.report.domainUrl").concat(message);
			}
		}
		return report;
	}

	/**
	 * @Description 新增记录（首次创建report记录）
	 * @param risk_report
	 * @return {@link t_risk_report}
	 * @author: zj
	 */
	public static t_risk_report addRiskReportFirst(t_risk_report risk_report) {
		RiskReportDao.delRiskReport(risk_report.user_id, RiskReportStatusEnum.CREATE.getCode());
		return RiskReportDao.addRiskReport(risk_report);
	}

	/**
	 * 用户查询运营商报告，同时生成原始数据并做相应的处理
	 * 
	 * @这里用一句话描述这个方法的作用
	 * @param mobile   手机号
	 * @param idcardNo 身份证号码
	 * @author: zj
	 */
	public static void addMxData(String mobile, String idcardNo) {
		try {

		
			// 查出用户基本信息
			t_users users = t_users.find(" mobile=? and id_number=? ", mobile, idcardNo).first();
			
				// 根据id 查询有效的task_id
				t_risk_report risk_report = t_risk_report.find(" user_id=? and status=3 and is_valid=2 ", users.id).first();
				String allMxData = MjkjDao.getMxData(mobile, risk_report.h5_response_task_id);
				//ReportUserInfo.saveReportUserInfo(allMxData, users.id);		
				// 将完整的运营商报告数据存入mongodb
				MongodbService.addMongodb(allMxData, users.id);

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("========处理运营商原始数据报告出错=====>" + e.getMessage(), e);
		}
	}
	

	public static void addMxData(long userId, String mobile, String idcardNo) {
		try {
			// 根据id 查询有效的task_id
			t_risk_report risk_report = t_risk_report.find(" user_id=? and status=3 and is_valid=2 ", userId).first();
			String allMxData = MjkjDao.getMxData(mobile, risk_report.h5_response_task_id);
			ReportUserInfo.saveReportUserInfo(allMxData, userId);		
			// 将完整的运营商报告数据存入mongodb
			MongodbService.addMongodb(allMxData, userId);
		//	}
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("========处理运营商原始数据报告出错=====>" + e.getMessage(), e);
		}
	}
}
