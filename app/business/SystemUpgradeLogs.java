package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Logger;
import utils.ErrorInfo;
import utils.PageBean;
import models.t_system_upgrade_logs;

public class SystemUpgradeLogs implements Serializable{

	public long id;
	public Date time;
	public String title;
	public String content;
	public String upgradePacks;
	
	
	/**
	 * 分页查询系统升级日志
	 * @param currPage
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<t_system_upgrade_logs> queryLogs(ErrorInfo error) {
		error.clear();
		
		String condition = "1=1";
		List<Object> params = new ArrayList<Object>();

		int count = 0;
		List<t_system_upgrade_logs> page = null;

		try {
			count = (int) t_system_upgrade_logs.count(condition, params.toArray());
			page = t_system_upgrade_logs.find(condition, params.toArray()).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		PageBean<t_system_upgrade_logs> bean = new PageBean<t_system_upgrade_logs>();
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;

		error.code = 0;
		
		return bean;
	}
}
