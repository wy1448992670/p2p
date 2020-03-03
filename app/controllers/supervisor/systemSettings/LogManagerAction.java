package controllers.supervisor.systemSettings;

import models.v_supervisor_events;
import business.DealDetail;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 操作日志管理
 * @author lzp
 * @version 6.0
 * @created 2014-7-10
 */

public class LogManagerAction extends SupervisorController {
	
	/**
	 * 操作日志列表
	 */
	public static void logs(int currPage, int pageSize, int keywordType, String keyword, String beginTime, String endTime) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisor_events> page = 
				DealDetail.querySupervisorEvents(currPage, pageSize, keywordType, keyword, beginTime, endTime, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
				
		render(page);
	}

	/**
	 * 删除操作日志页面
	 */
	public static void deleteLogsInit(int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisor_events> page = DealDetail.querySupervisorDeleteEvents(currPage, pageSize, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 删除操作日志
	 * @param type
	 */
	public static void deleteLogs(int type) {
		ErrorInfo error = new ErrorInfo();
		DealDetail.deleteEvents(type, error);
		
		renderJSON(error);
	}
}
