package controllers.supervisor.userManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.alibaba.fastjson.JSON;

import models.v_user_cps_info;
import utils.CharsetUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import business.BillInvests;
import business.Product;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
 * 类名:CPSUser
 * 功能:CPS会员列表
 */

public class ProfitUser extends SupervisorController {
	/**
	public static void profitUser(Integer year, Integer month, String userName, String bidTag, String orderType, int export){
		List<Map<String, Object>> result = BillInvests.findUserProfit(year, month, userName, bidTag, orderType);
		System.out.println(JSON.toJSONString(result, true));
		
		if(Constants.IS_EXPORT == export && result != null && result.size() > 0){
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(result, jsonConfig);
			
			String fileName = StringUtils.isBlank(bidTag) ? "推荐管理统计（全部）" : "推荐管理统计（"+bidTag+"）";
			
			for(Object obj : arrList){
				JSONObject bid = (JSONObject)obj;
				bid.put("invest_all", bid.getDouble("invest_1")+bid.getDouble("invest_2")+bid.getDouble("invest_3")+bid.getDouble("invest_4")+bid.getDouble("invest_5")+bid.getDouble("invest_6")+bid.getDouble("invest_12"));
			}
			
			File file = ExcelUtils.export(fileName,	arrList,
			new String[] {
			"推荐人姓名", "推荐人最大单笔投资", "新增有效用户数", "新手标（元）", "1月", "2月", "3月", "4月", "5月", "6月", "12月", "合计（元）"},
			new String[] { 
			"name", "max_invest_amount", "effective_user_account", "new_user_invest_amount", "invest_1", "invest_2", "invest_3", "invest_4", "invest_5","invest_6",	"invest_12", "invest_all"});
			   
			renderBinary(file, fileName+".xls");
		}
		
		render(result, year, month, userName, bidTag, orderType);
	}*/
	
	public static void profitUser(String start, String end, String userName, String bidTag, String orderType, int export){
		List<Map<String, Object>> result = BillInvests.findUserProfit(start, end, userName, bidTag, orderType);
		System.out.println(JSON.toJSONString(result, true));
		
		if(Constants.IS_EXPORT == export && result != null && result.size() > 0){
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(result, jsonConfig);
			
			String fileName = StringUtils.isBlank(bidTag) ? "推荐管理统计（全部）" : "推荐管理统计（"+bidTag+"）";
			
			for(Object obj : arrList){
				JSONObject bid = (JSONObject)obj;
				bid.put("invest_all", bid.getDouble("invest_1")+bid.getDouble("invest_2")+bid.getDouble("invest_3")+bid.getDouble("invest_4")+bid.getDouble("invest_5")+bid.getDouble("invest_6")+bid.getDouble("invest_12"));
			}
			
			File file = ExcelUtils.export(fileName,	arrList,
			new String[] {
			"推荐人姓名", "推荐人最大单笔投资", "新增被推荐用户数","首投新增被推荐用户数","复投被推荐人用户数","新增有效用户数", "新手标（元）", "1月", "2月", "3月", "4月", "5月", "6月", "12月", "合计（元）"},
			new String[] { 
			"name", "max_invest_amount","spread_user_account","first_user_count","more_user_count", "effective_user_account", "new_user_invest_amount", "invest_1", "invest_2", "invest_3", "invest_4", "invest_5","invest_6",	"invest_12", "invest_all"});
			   
			renderBinary(file, fileName+".xls");
		}
		
		render(result, start, end, userName, bidTag, orderType);
	}
	
}
