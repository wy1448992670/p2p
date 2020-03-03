package controllers.supervisor.account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import business.Supervisor;
import play.Logger;
import utils.ErrorInfo;
import utils.RegexUtils;

/**
 * 账户
 * @author lzp
 * @version 6.0
 * @created 2014-5-30
 */
public class AccountAction extends SupervisorController {
	/**
	 * 用户中心
	 */
	public static void home() {
		render();
	}
	
	/**
	 * 编辑管理员
	 */
	public static void editSupervisor(String oldPassword, String password, String realityName, int sex,
			String birthday, String mobile1, String email) {
		ErrorInfo error = new ErrorInfo();
	
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if (null == supervisor) {
			redirect(Constants.HTTP_PATH + "/supervisor");
		}
		
		if(StringUtils.isBlank(supervisor.password)) {
			if(StringUtils.isBlank(password)) {
				error.code = -1;
				error.msg = "请输入密码";

				renderJSON(error);
			}else{
				supervisor.password = password;
			}
		}else {
			if (StringUtils.isNotBlank(oldPassword) || StringUtils.isNotBlank(password)) {
				if (StringUtils.isBlank(oldPassword)) {
					error.code = -1;
					error.msg = "请输入原始密码";

					renderJSON(error);
				}
				
				if (!supervisor.isMyPassword(oldPassword)) {
					error.code = -1;
					error.msg = "原始密码不正确";

					renderJSON(error);
				}

				if (StringUtils.isBlank(password)) {
					error.code = -1;
					error.msg = "请输入密码";

					renderJSON(error);
				}
				
				supervisor.password = password;
			}
		}
		
		supervisor.realityName = realityName;
		supervisor.mobile1 = mobile1;
		supervisor.email = email;
		supervisor.edit(error);
		
		if (error.code < 0) {
			renderJSON(error);
		}
		
		Supervisor.setCurrSupervisor(supervisor);
		renderJSON(error);
	}
}
