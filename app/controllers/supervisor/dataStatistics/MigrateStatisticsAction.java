package controllers.supervisor.dataStatistics;

import business.StatisticalReport;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.v_migrate_user;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * date 2019年10月23日
 * author wangyun
 */
public class MigrateStatisticsAction extends SupervisorController{
	/**
	 * @author wangyun
	 * @date 2019年10月23日
	 * @param id
	 *  迁移用户统计
	 */
	public static void migrateUser(int currPage, int pageSize,String keyword,String startDateStr,String endDateStr) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_migrate_user> page = StatisticalReport.queryMigrateUser(currPage, pageSize, keyword, startDateStr, endDateStr, error);
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		render(page);
	}
}
