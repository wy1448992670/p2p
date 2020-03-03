package utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Http.Request;
import constants.Constants;

public class DataUtil {

	/**
	 * 更新数据（只能用于单表，且条件为“=”的更新）
	 * @param table 表名
	 * @param params 更新的字段
	 * @param conditions 条件字段
	 * @param values 所有字段的值 
	 * @param info 错误信息
	 * @return -1异常 0正常
	 */
	public static int update(String table, String[] params, String[] conditions,
			Object[] values, ErrorInfo info) {
		
		info.clear();
		
		StringBuffer sql = new StringBuffer("update ");
		sql.append(table + " set");
		
		for (String param : params) {
			sql.append(" " + param + " = ?,");
		}
		String mysql = sql.substring(0, sql.length() - 1);

		if (conditions != null && conditions.length > 0) {
			sql = new StringBuffer(mysql);
			sql.append(" where");
			
			for (String condition : conditions) {
				sql.append(" " + condition + " = ?,");
			}
			mysql = sql.substring(0, sql.length() - 1);
		}
		
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql.toString());
		
		for (int i = 0; i < values.length; i++) {
			query.setParameter(i + 1, values[i]);
		}

		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			Logger.info("update:%s", e.getMessage());
			info.code = -1;
			info.msg = "更新失败";
			
			return -1;
		}
		
		return rows;
	}
	
	/**
	 * 获取请求的ip地址
	 * @return
	 */
	public static String getIp() {
		Request request = Request.current();
		
		if(null == request) {
			return Constants.LOCALHOST_IP;
		}
		
		return StringUtils.isBlank(request.remoteAddress.toString()) ? Constants.LOCALHOST_IP : request.remoteAddress.toString();
	}
	
	/**
	 * 返回格式化后字符串，保留2位小数，每3位以“,”隔开，如： 80,100.09
	 * 参数如果不是数字类型，则返回其字符串形式
	 * @param obj
	 * @return
	 */
	public static String formatString(Object obj) {
		if (null == obj) {
			return StringUtils.EMPTY;
		}
		try {
			return String.format("%,.2f", Double.valueOf(String.valueOf(obj)));
		} catch (Exception e) {
			return obj.toString();
		}
	}
	
}
