package controllers.supervisor.financeManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang3.StringUtils;

import business.BillInvests;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.NumberUtil;
import utils.PageBean;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 
* Description: 加息统计
* @author xinsw
* @date 2017年8月1日
 */
public class IncreaseManager extends SupervisorController {
	
	public static void increaseSum(){
		String keyword = params.get("keyword");
		String fkStart = params.get("fkStart");
		String fkEnd = params.get("fkEnd");
		String fxStart = params.get("fxStart");
		String fxEnd = params.get("fxEnd");
		String statusStr = params.get("status");
		String orderTypeStr = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		String isExportStr = params.get("isExport");
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<Map<String,Object>> page = BillInvests.findIncreaseInterest(keyword, fkStart, fkEnd, fxStart, fxEnd, statusStr, 
				orderTypeStr, currPageStr, pageSizeStr, isExportStr, error);
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}	
		int isExport = NumberUtil.isNumericInt(isExportStr)? Integer.parseInt(isExportStr): 0;
		if(isExport == Constants.IS_EXPORT){
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(page.page, jsonConfig);

			File file = ExcelUtils.export("加息汇总列表",
			arrList,
			new String[] {
				"标的编号", "标的名称", "标的状态", "放款时间", "借款人真实姓名", "借款人电话", "加息年化利率","加息总利息","已付利息","待付利息","加息进度",
				"下次加息付息日期","下次加息付息金额","备注"},
			new String[] { "bidNo", "title", "statusValue","auditTime", "realityName", "mobile", "increaseRate","sumInterest", "realInterest","toPaid","periods","nextTime","nextAmt","remark"});
			   
			renderBinary(file, "加息汇总列表.xls");
		}
		render(page);
	}
	
	public static void increaseDetail(){
		String keyword = params.get("keyword");
		String start = params.get("start");
		String end = params.get("end");
		String statusStr = params.get("status");
		String orderTypeStr = params.get("orderType");
		String currPageStr = params.get("currPageStr");
		String pageSizeStr = params.get("pageSizeStr");
		String isExportStr = params.get("isExport");
		String overduestr = params.get("overdue");
		String realReceiveTimeStart = params.get("realReceiveTimeStart");
		String realReceiveTimeEnd = params.get("realReceiveTimeEnd");
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<Map<String,Object>> page = BillInvests.findIncreaseInterestDetail(keyword, start, end, statusStr, 
				overduestr,orderTypeStr, currPageStr, pageSizeStr, realReceiveTimeStart, realReceiveTimeEnd,isExportStr, error);
		if(page == null) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}	
		int isExport = NumberUtil.isNumericInt(isExportStr)? Integer.parseInt(isExportStr): 0;
		if(isExport == Constants.IS_EXPORT){
			
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
			jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
			JSONArray arrList = JSONArray.fromObject(page.page, jsonConfig);

			File file = ExcelUtils.export("加息明细列表",
			arrList,
			new String[] {
				"加息账单编号", "关联理财账单编号", "投资人", "账单期数", "应付加息金额", "标的名称", "关联借款标编号","借款人真实姓名","应付加息时间","实际支付时间","是否逾期",
				"账单状态","备注"},
			new String[] { "jxNo", "investNo", "investName","periods", "receiveIncreaseInterest", "title", "bidNo","realityName", "receiveTime","realReceiveTime","overdueValue","statusValue","remark"});
			   
			renderBinary(file, "加息明细列表.xls");
		}
		render(page);
	}
}
