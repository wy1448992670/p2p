package controllers.supervisor.customerService;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.shove.Convert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import models.t_invest_user;
import models.t_user_actions;
import models.v_user_info;
import models.v_user_info_city;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import business.Invest;
import business.Supervisor;
import business.User;
import business.UserActions;
import constants.Constants;
import controllers.front.home.NewHomeAction;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.userManager.InvestUser;

public class AuthRecordAction extends SupervisorController{
	
	
	public static void modifyRecordInit(int id){
		ErrorInfo error = new ErrorInfo();
		
		t_user_actions actions = UserActions.findById(id, error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		json.put("actions", actions);

		renderJSON(json);
	}
	
	public static void modifyRecord(long id,int status,String record){
		ErrorInfo error = new ErrorInfo();
		UserActions.updateAction(id,status,record, error);
		
		JSONObject json = new JSONObject();
		json.put("msg", error.msg);

		renderJSON(json);
	}
	
	public static void pageList(ErrorInfo error){
		String keyword = params.get("keyword");
		String start = params.get("start");
		String end = params.get("end");
		String statusStr = params.get("status");
		String orderTypeStr = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		
		PageBean<Map<String,Object>> page = UserActions.getPage(start, end, keyword, statusStr, 
				orderTypeStr, currPageStr, pageSizeStr, error);
		
		render(page);
	}

	/**
	 * 所有会员列表
	 */
	public static void allUser(String viewer) {
		String name = params.get("name");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String beginLoginTime = params.get("beginLoginTime");
		String endLoginTime = params.get("endLoginTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String isBank = params.get("isBank");
		String isRealname = params.get("isRealname");
		String recommend_user_name = params.get("recommend_user_name");
		String risk_result = params.get("risk_result");
		String is_first_invest = params.get("is_first_invest");
		String user_type= params.get("user_type");
		String finance_type = params.get("finance_type");

		int isExport = Convert.strToInt(params.get("isExport"), 0);
		
		ErrorInfo error = new ErrorInfo(); 
		PageBean<v_user_info> page = User.queryUserBySupervisorV2(name, null, beginTime, endTime, beginLoginTime, endLoginTime, key, orderType,
				curPage, isExport == Constants.IS_EXPORT ? "999999" : pageSize, error, isBank, isRealname,recommend_user_name, risk_result,is_first_invest,user_type,finance_type);
		
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<v_user_info> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				bill.put("name", bill.getString("name"));
				bill.put("reality_name", bill.getString("reality_name"));
				bill.put("mobile", bill.getString("mobile"));
				bill.put("recommend_user_name", bill.getString("recommend_user_name"));
				bill.put("register_time", bill.getString("register_time"));
				bill.put("first_invest_time", bill.getString("first_invest_time"));
				bill.put("first_invest_amount", bill.getString("first_invest_amount"));
				bill.put("last_invest_time", bill.getString("last_invest_time"));
				bill.put("invest_amount", bill.getString("invest_amount"));
				bill.put("bank", bill.getInt("bank") > 0 ? "是" : "否");
			}
			
			//用户名、真实姓名、手机号
			
			File file = ExcelUtils.export("用户列表", arrList,
					new String[] {"用户名", "真实姓名", "手机号", "推荐人用户名","推荐人手机号", "注册时间","最近一笔投资时间","首投时间","首投金额","累计投资金额","是否绑卡","累计投标数量"}, 
					new String[] {"name", "reality_name", "mobile", "recommend_user_name","recommend_user_mobile", "register_time","last_invest_time","first_invest_time","first_invest_amount","invest_amount","bank","invest_count"});
			
			renderBinary(file, "用户列表" + ".xls");
		}
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		
		render(page);
	}
	public static void userAddress(ErrorInfo error){
		error = new ErrorInfo();
		String name = params.get("name");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String beginLoginTime = params.get("beginLoginTime");
		String endLoginTime = params.get("endLoginTime");
		String orderType = params.get("orderType");
		String key = params.get("key");
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String isBank = params.get("isBank");
		 
		String recommend_user_mobile = params.get("recommend_user_mobile");
		String risk_result = params.get("risk_result");
		String provinceId = params.get("provinceId");
		String cityId = params.get("cityId");
		 
	 
		//该城市下的所有用户
		PageBean<v_user_info_city> page = User.queryUserBySupviCity(name, provinceId, cityId,
				beginTime,  endTime,  beginLoginTime,  endLoginTime, key,  orderType,  curPage,
				pageSize,  error,  isBank, recommend_user_mobile,  risk_result);
		
		
		render(page);
	}
	
	public static void investList(ErrorInfo error){
		error = new ErrorInfo();
		String name = params.get("name");
		String beginTime = params.get("beginTime");
		String endTime = params.get("endTime");
		String is_only_new_user = params.get("is_only_new_user");
		String red_amount = params.get("red_amount");
		String orderType = params.get("orderType");
		 
		String curPage = params.get("currPage");
		String pageSize = params.get("pageSize");
		String is_increase_rate = params.get("is_increase_rate");
		
		String recommend_user_name = params.get("recommend_user_name");
		String period_unit = params.get("period_unit");
		String repayment_type_name = params.get("repayment_type_name");
		String is_valid = params.get("is_valid"); 
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		 
		PageBean<t_invest_user> page = User.queryUserInvest(name,recommend_user_name, beginTime, endTime, 
				 is_only_new_user, red_amount,is_increase_rate, period_unit,
				 repayment_type_name, is_valid, curPage, isExport == Constants.IS_EXPORT ? "999999" : pageSize, orderType, error);
		
		//直接从sql查询出，以免数据量大了，计算时间过长
//		List<t_invest_user> invests = page.page;
/*		for (t_invest_user invest : invests) {
			double receive_increase_interest = 0;
			double income = calculate(invest);
			if(invest.is_increase_rate == 1){ //是否加息
				receive_increase_interest = calculate(invest.apr, invest.increase_rate, invest.amount,
						Integer.parseInt(invest.period),Integer.parseInt(invest.period_unit),invest.repayment_type_id);
			}
			
			invest.correct_interest =  receive_increase_interest;
			invest.income_dt = income;
		}*/
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<t_invest_user> list = page.page;
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bill = (JSONObject)obj;
				
				bill.put("name", bill.getString("name"));
				bill.put("reality_name", bill.getString("reality_name"));
				bill.put("mobile", bill.getString("mobile"));
				bill.put("recommend_user_name", bill.getString("recommend_user_name"));
				bill.put("recommend_user_mobile", bill.getString("recommend_user_mobile"));
				bill.put("invest_time", bill.getString("invest_time"));
				bill.put("period", bill.getString("period"));
				String period_unit_ = "";
				if(bill.getString("period_unit").equals("-1")){
					period_unit_ = "年";
				} else if(bill.getString("period_unit").equals("0")){
					period_unit_ = "月";
				} else if(bill.getString("period_unit").equals("1")){
					period_unit_ = "日";
				}
				bill.put("period_unit", period_unit_);
				bill.put("title", bill.getString("title"));
				bill.put("is_only_new_user", bill.getString("is_only_new_user").equals("1") ? "新手标" : "非新手标");
				bill.put("repayment_type_name", bill.getString("repayment_type_name"));
				bill.put("amount", bill.getString("amount"));
				bill.put("red_amount", bill.getString("red_amount"));
				bill.put("correct_interest", bill.getString("correct_interest"));
				bill.put("is_valid", bill.getString("is_valid"));
				bill.put("income_dt", bill.getString("income_dt"));
				 
			}
			
			//用户名、真实姓名、手机号
			
			File file = ExcelUtils.export("投资记录列表", arrList,
					new String[] {"会员名", "真实姓名", "手机号", "推荐人","推荐人手机号", "投资时间","投资标的期限","期限单位",
					"投资标的名称","标的类型","还款方式","投资金额","预计利息收益","使用红包金额","加息奖励","投资是否有效"}, 
					new String[] {"name", "reality_name", "mobile", "recommend_user_name","recommend_user_mobile", "invest_time","period",
					"period_unit","title","is_only_new_user","repayment_type_name","amount","income_dt","red_amount","correct_interest","is_valid"});
			
			renderBinary(file, "投资记录列表" + ".xls");
		}
		
		render(page);
	}
	
	
	
	public static double calculate(t_invest_user invest){
			double amount = invest.amount;
			double apr = invest.apr;
			int period = Integer.parseInt(invest.period) * 1;
			int periodUnit = Integer.parseInt(invest.period_unit) * 1;
			int repaymentType = invest.repayment_type_id * 1;
			double awardScale = invest.award * 1;
			double bonus = invest.bonus * 1;

			if(amount <= 0 || amount > 100000000 ){
				return 0;
			}
				
			if(apr > 100 || apr <= 0 ){
				return 0;
			}
			
			if(period < 1 || period > 1000){
				return 0;
			}
			
			if(1 == periodUnit && period > 30){
				return 0;
			}

			double award = 0;
			
			if(awardScale > 100 || awardScale < 0){
				return 0;
			}else if(awardScale != 0){
				award = amount * awardScale / 100;
			}
 
			if(bonus < 0 || bonus > amount){
				return 0;
			}else if(bonus != 0){
				award = bonus;
			} 

			double interest = 0; // 年、月、日 年利率
			double monthApr = apr / 12 / 100 ; // 月利率
			int rperiod = 0; // 还款期数
			
			/* 根据借款期限算出利息 */
			switch(periodUnit){
				/* 年 */
				case -1:
					interest = apr/100*period*amount;
					rperiod = period * 12; 
					break;
				/* 月 */
				case 0: 
					interest = apr/12/100*period*amount;
					rperiod = period; 
					break;
				/* 日 */
				case 1: 
					interest = apr/360/100*period*amount;
					rperiod = 1; 
					break;
			}
			
			double monthSum = 0;
			
			/* 根据还款方式算出利息 */
			switch(repaymentType){
				/* 按月还款、等额本息 */
				case 1: 
					monthSum = (amount * monthApr * Math.pow((1 + monthApr), rperiod)) / (Math.pow((1 + monthApr), rperiod) - 1); 
					interest = monthSum * rperiod - amount;
					break;
					
				/* 按月付息、一次还款 */
				case 2:
					monthSum = interest / rperiod;
				 	break;
				 	
				/* 一次还款 */
				case 3: 
					monthSum = interest + amount;
					break;
			}
			Map map = NewHomeAction.aprCalculatorReturn(amount, apr, repaymentType, award, rperiod);
			double serviceFee = Double.parseDouble(map.get("managementRate")+"") * interest;
			double income = interest + award - serviceFee;
			BigDecimal bg = new BigDecimal(income);
			income = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			 
			return income;
	}
	
	public static void allInvest(Integer pageSize, Integer currPage) {
		
		int isExport = Convert.strToInt(params.get("isExport"), 0);
		
		ErrorInfo error = new ErrorInfo();
		pageSize = pageSize == null || pageSize == 0 ? 10 : pageSize;
		currPage = currPage == null || currPage == 0 ? 1 : currPage;
		
		pageSize = isExport == 0 ? pageSize : pageSize.MAX_VALUE;
		
		PageBean<Map<String, Object>> page = Invest.findUserAllInvests(params, pageSize, currPage);
		
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<Map<String, Object>> list = page.page;
			
			com.alibaba.fastjson.JSONArray arrList = com.alibaba.fastjson.JSONArray.parseArray(JSON.toJSONString(list));
			for(int i = 0; i < arrList.size(); i++) {
				com.alibaba.fastjson.JSONObject bill = arrList.getJSONObject(i);
				bill.put("name", bill.getString("name"));
				bill.put("reality_name", bill.getString("reality_name"));
				bill.put("mobile", bill.getString("mobile") == null ? "" : bill.getString("mobile"));
				bill.put("rname", bill.getString("rname") == null ? "" : bill.getString("rname"));
				bill.put("rmobile", bill.getString("rmobile") == null ? "" : bill.getString("rmobile"));
				bill.put("receive_corpus", bill.getString("receive_corpus"));
				bill.put("receive_time", DateUtil.dateFormat(bill.getDate("receive_time"), "yyyy-MM-dd HH:mm"));
				bill.put("real_receive_time", DateUtil.dateFormat(bill.getDate("real_receive_time"), "yyyy-MM-dd HH:mm"));
				bill.put("status", bill.getString("status"));
				bill.put("periods", bill.getString("periods"));
				bill.put("title", bill.getString("title"));
				bill.put("bname", bill.getString("bname"));
				bill.put("b_reality_name", bill.getString("b_reality_name"));
			}
			
			String temp = com.alibaba.fastjson.JSONArray.toJSONString(arrList, SerializerFeature.WriteNullStringAsEmpty);
			arrList = com.alibaba.fastjson.JSONArray.parseArray(temp);
			
			//用户名、真实姓名、手机号
			
			File file = ExcelUtils.export("投资账单", arrList,
					new String[] {"投资人", "投资人真实姓名", "投资人手机号", "推荐人","推荐人手机", "本期应收金额","最后还款日期","实际还款日期","状态","账单期数","投资标的名称","借款人","借款人真实姓名"}, 
					new String[] {"name", "reality_name", "mobile", "rname","rmobile", "receive_corpus","receive_time","real_receive_time","status","periods","title","bname","b_reality_name"});
			
			renderBinary(file, "投资账单" + ".xls");
		}
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		
		render(page);
	}
	
	
	//加息利息计算
	public static double calculate(double apr, double increase_rate, double amount,int period,int periodUnit, int repaymentType){
		 
		if(amount <= 0 || amount > 100000000 ){
			return 0;
		}
			
		if(apr > 100 || apr <= 0 ){
			return 0;
		}
		
		if(period < 1 || period > 1000){
			return 0;
		}
		
		if(1 == periodUnit && period > 30){
			return 0;
		}

		double interest = 0; // 年、月、日 年利率
		double monthApr = apr / 12 / 100 ; // 月利率
		double monthApr_increase = increase_rate / 12 / 100 ; // 加息月利率
		int rperiod = 0; // 还款期数
		
		/* 根据借款期限算出利息 */
		switch(periodUnit){
			/* 年 */
			case -1:
				interest = apr/100*period*amount;
				rperiod = period * 12; 
				break;
			/* 月 */
			case 0: 
				interest = apr/12/100*period*amount;
				rperiod = period; 
				break;
			/* 日 */
			case 1: 
				interest = apr/360/100*period*amount;
				rperiod = 1; 
				break;
		}
		
		double monthSum = 0;
		double monthSum_increase = 0;
		
		/* 根据还款方式算出利息 */
		switch(repaymentType){
			/* 按月还款、等额本息 */
			case 1: 
				//本息不加息 9.6
				monthSum = (amount * monthApr * Math.pow((1 + monthApr), rperiod)) / (Math.pow((1 + monthApr), rperiod) - 1);
				//本息加息 9.6 + 1.2
				double total = monthApr + monthApr_increase ;
 				monthSum_increase = (amount * total * Math.pow((1 + total), rperiod)) / (Math.pow((1 + total), rperiod) - 1);
				
				interest = (monthSum_increase - monthSum) * rperiod ;
//				interest = monthApr_increase;
				break;
				
			/* 按月付息、一次还款 */
			case 2:
				monthSum = interest / rperiod;
			 	break;
			 	
			/* 一次还款 */
			case 3: 
				monthSum = interest + amount;
				break;
		}
		 
		double income = interest  ;
		BigDecimal bg = new BigDecimal(income);
		income = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		 
		return income;
}
	
}
