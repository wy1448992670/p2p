package business;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import jregex.Matcher;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.DBPlugin;
import play.db.jpa.JPA;
import models.t_db_operations;
import models.v_db_operations;
import constants.Constants;
import constants.SQLTempletes;
import constants.Constants.DBOperationType;
import constants.SupervisorEvent;
import utils.DataUtil;
import utils.ErrorInfo;
import utils.FileEncrypt;
import utils.FileUtil;
import utils.MySQLTool;
import utils.PageBean;
import utils.QueryUtil;

/**
 * 数据库备份与还原
 * @author lzp
 * @version 6.0
 * @created 2014-7-22
 */
public class DBOperation  implements Serializable{
	private static String host = "";
	private static String port = "";
	private static String username = "";
	private static String password = "";
	private static String database = "";
	
	private static String clearFileName = Play.getFile("/").getAbsolutePath() + "clear.sql";
	private static String resetFileName = Play.getFile("/").getAbsolutePath() + "reset.sql";
	
	static{
		String datasourceName = DBPlugin.getDatasourceName();
		Matcher m = new jregex.Pattern("^mysql:(//)?(({user}[a-zA-Z0-9_]+)(:({pwd}[^@]+))?@)?(({host}[^/]+)/)?({name}[^\\s]+)$").matcher(datasourceName);
        if (m.matches()) {
        	host = m.group("host") == null ? "localhost" : m.group("host");
        	port = "3306";
        	if(host.contains(":")){
        		String [] split = host.split(":");
        		host = split[0];
        		port = split[1];
        	}
        	username = m.group("user");
        	password = m.group("pwd");
        	database = m.group("name");
        }
	}
	/**
	 * 添加操作记录
	 * @param type
	 * @param fileName
	 * @param error
	 * @return
	 */
	private static int createOperation(int type, String fileName, ErrorInfo error) {
		error.clear();
		t_db_operations op = new t_db_operations();
		op.supervisor_id = Supervisor.currSupervisor().id;
		op.time = new Date();
		op.ip = DataUtil.getIp();
		op.type = type;
		op.filename = fileName;
		
		try {
			op.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		
		return error.code;
	}
	
	/**
	 * 清空数据
	 * @param error
	 * @return
	 */
	public static int clearData(ErrorInfo error) {
		error.clear();
		
		String backupFileName = backup(false, error);
		
		if (null == backupFileName) {
			return error.code;
		}
		
		if (0 != createOperation(DBOperationType.CLEAR, backupFileName, error)) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DB_CLEAR, "清空数据库", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if (0 != MySQLTool.executeSqlFile(username, password, host, port, database, clearFileName, error)) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "清空数据成功";
		
		return error.code;
	}
	
	/**
	 * 重置(还原出厂设置)
	 * @param error
	 * @return
	 */
	public static int reset(ErrorInfo error) {
		error.clear();
		
		String backupFileName = backup(false, error);
		
		if (null == backupFileName) {
			return error.code;
		}
		
		if (0 != createOperation(DBOperationType.RESET, backupFileName, error)) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DB_RESET, "还原出厂初始数据", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if (0 != MySQLTool.executeSqlFile(username, password, host, port, database, resetFileName, error)) {
			JPA.setRollbackOnly();
			
			return error.code;
		}

		error.code = 0;
		error.msg = "还原出厂设置成功";
		
		return error.code;
	}
	
	/**
	 * 还原数据库
	 * @param fileName
	 * @param error
	 * @return
	 */
	public static int recover(String fileName, ErrorInfo error) {
		error.clear();
		
		if (StringUtils.isBlank(fileName)) {
			error.code = -1;
			error.msg = "恢复文件不能为空";
			
			return error.code;
		}
		
		String backupFileName = backup(false, error);
		
		if (null == backupFileName) {
			return error.code;
		}
		
		if (0 != createOperation(DBOperationType.RECOVER, backupFileName, error)) {
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DB_RECOVER, "还原运营数据", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		String decryptFileName = Constants.SQL_PATH + UUID.randomUUID().toString() + ".sql"; 
		
		if (!FileEncrypt.decrypt(fileName, decryptFileName, Constants.ENCRYPTION_KEY)) {
			error.code = -1;
			error.msg = "还原数据库失败";
			
			return error.code;
		}
		
		if (0 != MySQLTool.executeSqlFile(username, password, host, port, database, decryptFileName, error)) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if (!new File(decryptFileName).delete()) {
			error.code = -1;
			error.msg = "还原数据库失败";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "还原数据库成功";
		
		return error.code;
	}
	
	/**
	 * 从操作记录还原
	 * @param operationId
	 * @param error
	 * @return
	 */
	public static int recoverFromOperation(int operationId, ErrorInfo error) {
		error.clear();
		String fileName = null;
		
		try {
			fileName = t_db_operations.find("select filename from t_db_operations where id = ?", (long)operationId).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		recover(fileName, error);
		
		return error.code;
	}
	
	/**
	 * 备份数据库
	 * @param isVisual 是否在数据库操作记录表中可见
	 * @param error
	 * @return
	 */
	public static String backup(boolean isVisual, ErrorInfo error) {
		error.clear();
		
		String fileName = Constants.SQL_PATH + UUID.randomUUID().toString();
		FileUtil.getStore(Constants.SQL_PATH);
		
		if (0 != MySQLTool.dumpSqlFile(username, password, host, port, database, fileName, error)) {
			return null;
		}
		
		if (!FileEncrypt.encrypt(fileName, Constants.ENCRYPTION_KEY)) {
			error.code = -1;
			error.msg = "备份数据库失败";
			
			return null;
		}
		
		if (isVisual) {
			if (0 != createOperation(DBOperationType.BACKUP, fileName, error)) {
				return null;
			}
			
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DB_BACKUP, "备份数据", error);
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
				
				return null;
			}
		}
		
		error.code = 0;
		error.msg = "备份数据库成功";
		
		return fileName;
	}
	
	/**
	 * 查询数据库操作记录
	 * @param error
	 * @return
	 */
	public static PageBean<v_db_operations> queryOperations(int currPage, int pageSize, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_DB_OPERATIONS);
		
		List<v_db_operations> page = null;
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_db_operations.class);
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			page = query.getResultList();
			count = QueryUtil.getQueryCount(em);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		PageBean<v_db_operations> bean = new PageBean<v_db_operations>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = count;
		
		error.code = 0;

		return bean;
	}
}
