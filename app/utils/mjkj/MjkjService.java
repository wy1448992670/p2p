/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.mjkj
 *
 *    Filename:    MjkjService.java
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
 *    Create at:   Jan 24, 2019 11:46:14 AM
 *
 *    Revision:
 *
 *    Jan 24, 2019 11:46:14 AM
 *        - first revision
 *
 *****************************************************************/
package utils.mjkj;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import models.t_users;
import models.risk.t_risk_report;
import play.Logger;
import services.RiskReportService;
import services.UserService;

/**
 * @ClassName MjkjService
 * @Description 摩羯科技第三方接口
 * @author zj
 * @Date Jan 24, 2019 11:46:14 AM
 * @version 1.0.0
 */
public class MjkjService {
	static String THEMECOLOR = "555aff";

	/**
	 * @Description 得到运营商h5页面url地址
	 * @param userId
	 * @param themeColor 界面颜色 例如 #ff0000
	 * @param idcard     身份证号码
	 * @return url地址
	 * @author: zj
	 */
	public static String getOperatorH5Url(long userId, String themeColor, String calbackUrl) {
		if (StringUtils.isEmpty(themeColor)) {
			themeColor = THEMECOLOR;
		}
		t_users users = UserService.getUserById(userId);
		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("phone", "17731522429");
//		map.put("name", "刘达");
//		map.put("idcard", "130221199504295133");

		map.put("phone", users.mobile);
		map.put("name", users.reality_name);
		map.put("idcard", users.id_number);

		String loginParams = URLEncoder.encode(JSON.toJSONString(map));
		return MjkjDao.getOperatorH5Url(userId, themeColor, calbackUrl).concat("&loginParams=").concat(loginParams)
				.concat("&quitOnLoginDone=YES&showTitleBar=NO");
	}

	/**
	 * @Description 获取自信报告message
	 * @param json
	 * @return 空 则 获取资信报告失败
	 * @author: zj
	 */
	public static void parseReportResult(String json) {
		try {

			JSONObject jObject = JSONObject.parseObject(json);

			boolean result = (Boolean) jObject.get("result");
			long userId = Long.parseLong(jObject.getString("user_id"));
			t_risk_report report = new t_risk_report();
			report.user_id = userId;
			if (result) {// 获取报告成功
				report.status = 1;
				report.is_valid = 1;
			} else {// 失败
				report.status = 2;
				report.is_valid = 2;
				UserService.updateUser(userId, -1);
			}
			report.report_response = json;
			RiskReportService.addRiskReport(report);
		} catch (Exception e) {
			Logger.error(e, "连接资信报告接口异常======>" + e.getMessage());
		}
	}
}
