package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;

import constants.Constants;
import models.t_report_contact_detail;
import models.t_report_user_info;
import models.t_user_address_list;
import models.t_users;
import models.v_user_detail_credit_score_normal_repayment;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import services.RiskReportService;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;
import utils.QueryUtil;

public class ReportUserInfo implements Serializable {

	/**
	 * 运营商报告 新增
	 * @param requestParams
	 * @param userId
	 */
	@SuppressWarnings("unchecked")
	public static void saveReportUserInfo(String requestParams, long userId) {
		Map<String, Object> params = (Map<String, Object>) JSON.parse(requestParams);
		// 用户信息
		t_report_user_info userInfo = new t_report_user_info();
		userInfo.user_id=userId;
		
		// 用户基本信息
		List<Map<String, String>> userBasic = (List<Map<String, String>>) params.get("user_basic");
		if (userBasic != null && !userBasic.isEmpty()) {
			for (Map<String, String> m : userBasic) {
				String key = m.get("key");
				String value = m.get("value");
				if ("id_card".equals(key)) {
					userInfo.id_card = value;
				}
				if ("user_name".equals(key)) {
					userInfo.user_name = value;
				}
				if ("native_place".equals(key)) {
					userInfo.native_place = value;
				} else {
					continue;
				}
			}
		}
		
		// 手机号信息
		List<Map<String, String>> cellPhone = (List<Map<String, String>>) params.get("cell_phone");
		
		if (cellPhone != null && !cellPhone.isEmpty()) {
			for (Map<String, String> m : cellPhone) {
				String key = m.get("key");
				String value = m.get("value");
				if ("mobile".equals(key)) {
					userInfo.mobile = replaceMoblieSpace(value);
				}
				if ("carrier_name".equals(key)) {
					userInfo.carrier_name = value;
				}
				if ("reg_time".equals(key)) {
					userInfo.reg_time = DateUtil.strToDate(value);
				} else {
					continue;
				}
			}
		}
		userInfo.save();

		// 通话记录详情
		List<Map<String, String>> callContactDetail = (List<Map<String, String>>) params.get("call_contact_detail");
		if (callContactDetail != null && !callContactDetail.isEmpty()) {
			for (Map<String, String> m : callContactDetail) {
				t_report_contact_detail contactDetail = new t_report_contact_detail();
				contactDetail.user_info_id = userInfo.id;
				contactDetail.number = replaceMoblieSpace(m.get("peer_num"));
				contactDetail.group_name = m.get("group_name");
				contactDetail.company_name = m.get("company_name");
				contactDetail.save();
			}
		}
		
	}
	
	private static String replaceMoblieSpace(String sourceStr) {
		if (StringUtils.isBlank(sourceStr)) {
			return "";
		}
		return sourceStr.replace(" ","");
	}


	/**
	 * 通讯记录 分页数据
	 * @param requestParams
	 * @return
	 * @throws Exception
	 */
	public static PageBean<Map<String, Object>> getReportContactDetailData(Params requestParams) throws Exception {
		
		String currPageStr=requestParams.get("currPage");
		String pageSizeStr=requestParams.get("pageSize");

		String userId = requestParams.get("userId"); 
		String number = requestParams.get("number"); // 通话手机号(模糊查询)

		// 查询列
		String columnsSql = "a.id, a.user_info_id, a.number, a.group_name, a.company_name, a.create_time, c.name";
		// 查询表
		String tableSql = " from t_report_contact_detail a INNER JOIN t_report_user_info b on b.id=a.user_info_id ";
		tableSql += "LEFT JOIN t_user_address_list c ON c.user_id=b.user_id AND a.number=REPLACE(c.mobile,' ','')";
		// 查询条件
		String conditionSql = " where b.user_id=? ";
 		
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		if(StringUtils.isNotEmpty(number)) {
			conditionSql += " and a.number like ? ";
			params.add("%" + number +"%");
		} 	
		
		PageBean<Map<String, Object>> page = PageBeanForPlayJPA.getPageBeanMapBySQL(columnsSql, tableSql+conditionSql, "order by a.id desc",currPageStr, pageSizeStr, params.toArray());
		return page;
	}
	
	/**
	 * 通讯记录 列表(如果没有，先新增)
	 * @param requestParams
	 * @return
	 */
	public static PageBean<Map<String, Object>> getReportContactDetail(Params requestParams) {

		// 用户ID
		long userId = Long.parseLong(requestParams.get("userId")); 
		
		try {
			t_report_user_info reportUserInfo = t_report_user_info.find(" user_id = ? ", userId).first();
			if (reportUserInfo == null) { // 该用户没有运营商报告，先新增
				t_users users = t_users.find(" id = ? ", userId).first();
				RiskReportService.addMxData(users.id, users.mobile, users.id_number);	

				return getReportContactDetailData(requestParams);
			} else {
				return getReportContactDetailData(requestParams);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}

}
