package controllers.supervisor.mall;

import java.io.File;
import java.util.Date;
import java.util.List;

import models.t_mall_scroe_record;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import utils.ExcelUtils;
import utils.JsonDateValueProcessor;
import utils.JsonDoubleValueProcessor;
import utils.PageBean;
import business.MallScroeRecord;
import constants.Constants;
import controllers.supervisor.SupervisorController;

/**
 * 积分商城：积分记录
 * 
 * @author yuy
 * @created 2015-10-14
 */
public class ScroeRecordAction extends SupervisorController {

	/**
	 * 积分记录列表
	 */
	public static void scroeRecordList() {
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
			new ScroeRecordAction().exportExcel(page.page, "兑换记录", new String[] { "用户名", "时间", "积分", "兑换数量/投资金额", "事件", "状态(1:成功,2:消费/赠送中)", "备注" },
					new String[] { "user_name", "time", "scroe", "quantity", "description", "status", "remark" });
		}

		render(page);
	}

	/**
	 * 导出Excel表格
	 * 
	 * @param list
	 *            数据集合
	 * @param name
	 *            导出表格名称
	 * @param arr1
	 *            必要参数1
	 * @param arr2
	 *            必要参数2
	 * @param key
	 *            需要转换的key标示
	 */
	public void exportExcel(List<?> list, String name, String[] arr1, String[] arr2) {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd HH:mm:ss"));
		jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
		JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

		File file = ExcelUtils.export(name, arrList, arr1, arr2);

		renderBinary(file, name + ".xls");
	}
}
