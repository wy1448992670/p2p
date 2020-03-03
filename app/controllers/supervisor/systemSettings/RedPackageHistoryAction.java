package controllers.supervisor.systemSettings;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_content_news;
import models.t_red_packages_history;
import models.t_red_packages_history_count;
import models.v_bid_repaymenting;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import play.mvc.Scope.Session;

import org.apache.commons.lang3.StringUtils;

import com.shove.Convert;

import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import business.News;
import business.Product;
import business.RedPackageHistory;
import business.User;
import constants.Constants;
import constants.Templets;
import controllers.supervisor.SupervisorController;


public class RedPackageHistoryAction extends SupervisorController{

	/**
	 * 查询发放红包统计
	 */
	public static void showList(Integer couponFlag,int isExport){
//		 List<t_red_packages_history_count> resultList = RedPackageHistory.showList();
//		 render(resultList);
		
		couponFlag=RedPackageHistory.storeCouponInfo(couponFlag);
		String couponName=RedPackageHistory.couponName;
		
		String typeStr = params.get("type");
		String dateTypeStr = params.get("dateType");
		String currPageStr = params.get("currPage"); // 当前页
		String pageSizeStr = params.get("pageSize"); // 分页行数
		String statusStr = params.get("status"); // 条件
		String keyword = params.get("keyword"); // 关键词
		String start = params.get("start"); // 开始时间
		String end = params.get("end"); // 结束时间
		String orderIndexStr = params.get("orderIndex"); // 排序索引
		String orderStatusStr = params.get("orderStatus"); // 升降标示
		String useTimeStart = params.get("useTimeStart"); //使用时间
		String useTimeEnd = params.get("useTimeEnd");
		
		int currPage = NumberUtil.isNumericInt(currPageStr)? Integer.parseInt(currPageStr): 1;
		int pageSize = NumberUtil.isNumericInt(pageSizeStr)? Integer.parseInt(pageSizeStr): 10;
		int type = NumberUtil.isNumericInt(typeStr)? Integer.parseInt(typeStr): -1;
		int dateType = NumberUtil.isNumericInt(dateTypeStr)? Integer.parseInt(dateTypeStr): 0;
		int status = NumberUtil.isNumericInt(statusStr)? Integer.parseInt(statusStr): -3;
		int orderIndex = NumberUtil.isNumericInt(orderIndexStr)? Integer.parseInt(orderIndexStr): 0;
		int orderStatus = NumberUtil.isNumericInt(orderStatusStr)? Integer.parseInt(orderStatusStr): 0;
		  
		PageBean<Map<String,Object>> pageBean = RedPackageHistory.getPage(type, dateType, start, end, keyword, status,
				pageSize, currPage, orderIndex, orderStatus,useTimeStart,useTimeEnd,isExport);
		
		if(isExport == Constants.IS_EXPORT){
			
			List<Map<String,Object>> list = pageBean.page;
			 
			if(list != null && list.size() > 0){
				for(Map<String,Object> map : list){
					map.put("time", DateUtil.dateToString((Date) map.get("time")));
					map.put("validate", DateUtil.dateToString((Date) map.get("validate")));
					map.put("useTime", DateUtil.dateToString((Date) map.get("useTime")));
					String title = (String) map.get("title");
					if(StringUtils.isBlank(title)){
						map.put("title", "");
					}
					String remark = (String) map.get("remark");
					if(StringUtils.isBlank(remark)){
						map.put("remark", "");
					}
				}
			}
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(list, jsonConfig);
			
			for (Object obj : arrList) {
				JSONObject bid = (JSONObject)obj;
				int redType = bid.getInt("type");
				String rt = "手动类";
				switch (redType) {
				case 1:
					rt = "注册";
					break;
				case 2:
					rt = "充值";
					break;
				case 3:
					rt = "投资";
					break;
				case 4:
					rt = "积分";
					break;
				case 5:
					rt = "实名认证";
					break;
				case 6:
					rt = "添加银行卡";
					break;
				case 7:
					rt = "累计投资";
					break;
				case 8:
					rt = "生日";
					break;
				case 9:
					rt = "自定义";
					break;
				case 10:
					rt = "积分兑换";
					break;
				default:
					break;
				}
				
				int st = bid.getInt("status");
				String statusValue = "";
				if(st == -1){
					statusValue = "已过期";
				}else if(st == 0){				
					statusValue = "未使用";
				}else{
					statusValue = "已使用";
				}
//				bid.put("time", DateUtil.dateToString((Date) bid.get("time")));
//				bid.put("validate", DateUtil.dateToString((Date) bid.get("validate")));
				bid.put("redType", rt);
				bid.put("status", statusValue);
			}
			
			File file = ExcelUtils.export(couponName+"统计列表",
			arrList,
			new String[] {
					couponName+"类型", couponName+"名称", "用户名", "发放时间", "过期时间", "使用标的","使用时间", couponName+"金额","状态","备注"},
			new String[] { "redType", "name", "userName","time", "validate", "title","useTime", "money","status", "remark"});
			   
			renderBinary(file, couponName+"统计列表.xls");
		}

		render(pageBean,couponFlag,couponName);
	}
	
	/**
	 * 根据类型查询详细
	 * @param type
	 * @param currPage
	 * @param pageCount
	 */
	public static void showListByType(String type,int currPage,int pageCount){
		PageBean<t_red_packages_history> pageBean = RedPackageHistory.showListByType(currPage,pageCount,type);
		render(pageBean,type);
	}
	
	public static void manualRed(Integer couponFlag){
		couponFlag=	RedPackageHistory.storeCouponInfo(couponFlag);
		String couponName=RedPackageHistory.couponName;
		render(couponFlag,couponName);
	}
	
	/**
	 * 手动发红包
	 * @param usernames
	 * @param amount
	 * @param typeName
	 * @param validity_money
	 * @param validity_time
	 * @param validate_unit
	 * @param notice_message
	 * @param notice_box
	 * @param notice_email
	 */
	public static void saveRedPacketsUsers(String usernames,double amount,String typeName,int validity_money,String validity_time, String validate_unit,
			String notice_message,String notice_box,String notice_email,int bid_period,int bid_period_unit,int is_new_user,int balance_s,int balance_b){
		ErrorInfo error = new ErrorInfo();
		if (StringUtils.isBlank(usernames)) {
			error.code = -1;
			error.msg = "请选择用户";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
       if (amount < 0) {
			error.code = -1;
			error.msg = "红包金额不能小于0元";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
		if ((StringUtils.isBlank(validity_time) || StringUtils.isBlank(validate_unit))) {
			error.code = -1;
			error.msg = "请输入红包期限";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
		if (StringUtils.isBlank(typeName)) {
			error.code = -1;
			error.msg = "请输入红包名称";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
		if (validity_money < 0) {
			error.code = -1;
			error.msg = "使用红包金额必须大于0";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
		if (StringUtils.isBlank(notice_message) && StringUtils.isBlank(notice_box) && StringUtils.isBlank(notice_email)) {
			error.code = -1;
			error.msg = "请选择至少一种通知方式";
			JSONObject json = new JSONObject();
			json.put("error", error);
			renderJSON(json);
		}
		RedPackageHistory.saveRedPacketsUsers(usernames,amount,typeName,validity_money,validity_time,validate_unit,notice_message,notice_email,notice_box,bid_period,bid_period_unit,is_new_user,balance_s,balance_b,error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 红包规则设置
	 */
	public static void redRules(ErrorInfo error){
		News news = new News();
		news.id = -2;//初始化红包规则
		if(null != error && error.code < 0){
			flash.put("error",error);
		}
		render(news);
	}
	
	/**
	 * 
	 */
	public static void redRulesUpdate(File imageFile){
		ErrorInfo error = new ErrorInfo();
		
		String idStr = params.get("id");
		long id = Long.parseLong(idStr);
		
		String title = params.get("edittitle");
		
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			redRules(error);
		}
		
		String content = params.get("editcontent");
		
		if(StringUtils.isBlank(content)) {
			flash.error("内容不能为空");
			redRules(error);
		}
		
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			redRules(error);
		}
		
		 t_content_news news =  t_content_news.findById(id);
		 news.title = title;
		 news.content = Templets.replaceAllHTML(content);
		 
		 try {
			 news.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.code =-1;
			error.msg ="操作失败!";
		}
		
		redRules(error);
	}
	
}
