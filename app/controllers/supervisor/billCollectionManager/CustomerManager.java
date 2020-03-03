package controllers.supervisor.billCollectionManager;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.Supervisor;
import models.v_supervisors;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;

/**
 * 客服管理
 * @author lzp
 * @version 6.0
 * @created 2014-6-6
 */
public class CustomerManager extends SupervisorController {
	
	/**
	 * 显示客服列表
	 * @param currPage
	 * @param pageSize
	 * @param lockType
	 * @param keywordType
	 * @param keyword
	 */
	public static void customers(int currPage, int pageSize, int lockType, int keywordType, String keyword){
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.queryCustomers(currPage, pageSize, lockType, keywordType, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(pageBean);
	}
	
	public static void addCustomerInit() {
		render();
	}
	
	/**
	 * 添加客服
	 * @param supervisorIds
	 */
	public static void addCustomers(String signs) {
		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isBlank(signs) || StringUtils.split(signs, ",").length < 1) {
			error.code = -1;
			error.msg = "请选择客服";
			
			renderJSON(error);
		}
		
		String[] arr = StringUtils.split(signs, ",");
		List<Long> supervisorIds = new ArrayList<Long>();
		String sign = null;
		
		for (int i = 0; i < arr.length; i++) {
			sign = arr[i];
			sign = com.shove.security.Encrypt.decrypt3DES(sign, Constants.SUPERVISOR_ID_SIGN);
			
			if(StringUtils.isBlank(sign)) {
				error.code = -1;
				error.msg = "请求非法!";
				
				return;
			}
			
			long supervisorId = Long.parseLong(sign);
			
			supervisorIds.add(supervisorId);
		}
		
		Supervisor.addCustomers(supervisorIds, error);
		
		renderJSON(error);
	}
	
	/**
	 * 选择管理员初始化
	 * @param currPage
	 * @param pageSize
	 * @param keyword
	 */
	public static void selectSupervisorInit(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
				Supervisor.queryCandidateCustomers(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}

		render(pageBean);
	}
	
	/**
	 * 查询选择的管理员
	 * @param supervisorIds
	 */
	public static void selectedSupervisors(String signs) {
		ErrorInfo error = new ErrorInfo();
		
		if (StringUtils.isBlank(signs) || StringUtils.split(signs, ",").length < 1) {
			error.code = -1;
			error.msg = "管理员列表不能为空";
			
			renderJSON(error);
		}
		
		String[] arr = StringUtils.split(signs, ",");
		List<Long> supervisorIds = new ArrayList<Long>();
		String sign = null;

		for (int i = 0; i < arr.length; i++) {
			sign = arr[i];
			sign = com.shove.security.Encrypt.decrypt3DES(sign, Constants.SUPERVISOR_ID_SIGN);
			
			if(StringUtils.isBlank(sign)) {
				error.code = -1;
				error.msg = "请求非法!";
				
				return;
			}
			
			long supervisorId = Long.parseLong(sign);
			
			supervisorIds.add(supervisorId);
		}
		
		List<v_supervisors> supervisors = Supervisor.querySupervisors(supervisorIds, error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		render(supervisors);
	}
	
	/**
	 * 重置密码
	 */
	public static void resetPassword(String sign) {
		ErrorInfo error = new ErrorInfo();
		long supervisorId = Security.checkSign(sign, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		
		if (error.code < 0) {
			render(error);
		}
		
		Supervisor.resetPassword(supervisorId, error);

		renderText(error.msg);
	}

}
