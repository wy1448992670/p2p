package controllers.supervisor.scoreManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import models.t_mall_scroe_record;
import models.t_users;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import business.MallScroeRecord;
import business.Score;


import constants.Constants;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.mall.ScroeRecordAction;

/**
 * 
* Description: 积分管理
* @author xinsw
* @date 2017年9月4日
 */
public class ScoreAction extends SupervisorController {
	
	public static void showRule() {
		ErrorInfo error = new ErrorInfo();
		Map<String,Object> map = Score.getConfig(error);
		render(map);
	}
	
	public static void saveScoreRule() {
		ErrorInfo error = new ErrorInfo();
		Score.saveScoreRule(params, error);
		
		renderJSON(error);
	}
	
	public static void addScore(){
		render();
	}
	
	public static void getUserInfo(String mobile){
		t_users user = t_users.find("mobile", mobile).first();
		renderJSON(user);
	}
	
	public static void saveScore(Integer score,String mobile,Long userId,String remark){
		ErrorInfo error = new ErrorInfo();
		Score.saveScore(score, mobile, userId, remark, error);
		renderJSON(error);
	}
	
	/**
	 * 积分记录列表
	 */
	public static void scoreList() {
		ErrorInfo error = new ErrorInfo();
		String orderType = params.get("orderType");
		String orderStatus = params.get("orderStatus");
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		String user_name = params.get("user_name");
		String typeStr = params.get("type");
		String isExportStr = params.get("isExport");
		int currPage = 0;
		int pageSize = 0;
		int type = 0;
		int isExport = 0;
		if (StringUtils.isNotBlank(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}
		if (StringUtils.isNotBlank(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		if (StringUtils.isNotBlank(typeStr)) {
			type = Integer.parseInt(typeStr);
		}
		if (StringUtils.isNotBlank(isExportStr)) {
			isExport = Integer.parseInt(isExportStr);
		}
		PageBean<t_mall_scroe_record> page = MallScroeRecord.queryScroeRecordByPage(0, user_name, type, orderType, orderStatus, currPage, pageSize,
				isExport, error);
		if (error.code < 0) {
			flash.error("抱歉，系统出错，请联系管理员");
		}
		
		/* 导出excel */
		if (isExport == Constants.IS_EXPORT) {
			List<t_mall_scroe_record> list = page.page;

			if(list == null || list.size() == 0){
				list = new ArrayList<t_mall_scroe_record>();
			}
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class,
					new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class,
					new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject) obj;
				int ty = bid.getInt("type");
				String typeString = "";
				switch (ty) {
				case 1:
					typeString = "注册";
					break;
				case 2:
					typeString = "签到";
					break;
				case 3:
					typeString = "投标";
					break;
				case 4:
					typeString = "抽奖";
					break;
				case 15:
					typeString = "兑换";
					break;
				case 9:
					typeString = "实名认证";
					break;
				case 10:
					typeString = "添加银行卡";
					break;
				case 11:
					typeString = "首次投资";
					break;
				case 12:
					typeString = "成功推荐用户添加银行卡";
					break;
				case 13:
					typeString = "成功推荐用户首次投资";
					break;
				case 14:
					typeString = "手动";
					break;
				default:
					break;
				}

				bid.put("typeStr", typeString);
			}
			File file = ExcelUtils.export("积分统计", arrList, new String[] {
					"用户名", "时间", "积分", "类型", "事件", "状态(1:成功,2:消费/赠送中)", "备注" },
					new String[] { "user_name", "time", "scroe", "typeStr",
							"description", "status", "remark" });
			renderBinary(file, "积分统计表.xls");
		}
		render(page);
	}
}
