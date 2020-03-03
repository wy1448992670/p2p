package controllers.supervisor.systemSettings;

import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.v_db_operations;
import business.DBOperation;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 数据库备份与还原
 * @author lzp
 * @version 6.0
 * @created 2014-7-22
 */
public class DBOperationAction extends SupervisorController {
	/**
	 * 数据库管理页面
	 */
	public static void db(int currPage, int pageSize) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_db_operations> page = DBOperation.queryOperations(currPage, pageSize, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 清空数据
	 */
	public static void clearData() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.clearData(error);
		
		renderJSON(error);
	}
	
	/**
	 * 重置(还原出厂设置)
	 */
	public static void reset() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.reset(error);
		
		renderJSON(error);
	}
	
	/**
	 * 从操作记录还原
	 * @param operationId
	 */
	public static void recoverFromOperation(int operationId) {
		ErrorInfo error = new ErrorInfo();
		DBOperation.recoverFromOperation(operationId, error);
		
		renderJSON(error);
	}
	
	/**
	 * 备份
	 */
	public static void backup() {
		ErrorInfo error = new ErrorInfo();
		DBOperation.backup(true, error);
		
		renderJSON(error);
	}
}
