package controllers.supervisor.systemSettings;

import org.apache.commons.lang.StringUtils;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import models.v_supervisors;
import business.BackstageSet;
import business.SecretQuestion;
import business.Supervisor;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 安全设置
 * 
 * @author bsr
 * 
 */
public class SecuritySettingAction extends SupervisorController {

	/**
	 * 安全云盾列表(首页)
	 */
	public static void UKeyList(int currPage, int pageSize, String keyword) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_supervisors> pageBean = 
			Supervisor.querySupervisors(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
	
		render(pageBean);
	}
	
	/**
	 * 安全参数设置
	 */
	public static void safeParam(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}
	
	/**
	 * 保存安全参数
	 */
	public static void saveSafeParam(Integer isOpenPasswordErrorLimit,Integer passwordErrorCounts,Integer lockingTime){
		ErrorInfo error = new ErrorInfo();
		
		if(isOpenPasswordErrorLimit == null){
			flash.error("传入数据类型有误");
			
			safeParam();
		}
		
		if(passwordErrorCounts == null){
			flash.error("密码错误次数必须是数字");
			
			safeParam();
		}
		
		if(lockingTime == null){
			
			flash.error("锁定时间必须是数字");
			
			safeParam();
		}
		
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.isOpenPasswordErrorLimit = isOpenPasswordErrorLimit;
		backstageSet.passwordErrorCounts = passwordErrorCounts;
		backstageSet.lockingTime = lockingTime;
		backstageSet.keywords = params.get("keywords");
		
		backstageSet.editSystemParameter(error);
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		flash.success(error.msg);
		
		safeParam();
	}
	
	/**
	 * 安全问题库设置
	 */
	public static void safeQuestion(String name, int currPage){
		
		PageBean<SecretQuestion> page = SecretQuestion.query(name, currPage,Constants.PAGE_SIZE );
		
		render(page); 
	}
	
	/**
	 * 添加安全问题
	 */
	public static void addSafeQuestion(String name, String type) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(name) || name.length() > 15) {
			error.code = -1;
			error.msg = "安全问题不能为空，且需在15字符以内!";
			json.put("error", error);

			renderJSON(json);
		}

		SecretQuestion question = new SecretQuestion();

		question.name = name;
		question.type = type;

		question.addSafeQuestion(error);
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 更新安全问题的状态
	 * @param id
	 * @param status
	 */
	public static void updateSafeQuestion(long id, boolean status) {
		ErrorInfo error = new ErrorInfo();
		SecretQuestion.updateStatus(id, status, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		 
		renderJSON(json);
	}
	
	/**
	 * 详情
	 */
	public static void UKeyListDetail() {
		render();
	}

	/**
	 * 编辑
	 */
	public static void updateUKey() {
		render();
	}

	/**
	 * 保存编辑后的UKey
	 */
	public static void saveUpdateUKey() {

	}

	/**
	 * 删除
	 */
	public static void delete() {

	}

	/**
	 * 添加安全云盾
	 */
	public static void addUKey() {
		render();
	}

	/**
	 * 添加安全云盾时选择管理员
	 */
	public static void selectSupervisor() {
		render();
	}

	/**
	 * 保存添加的安全云盾
	 */
	public static void saveUKey() {

	}

	/**
	 * 安全参数设置
	 */
	public static void paramSite() {
		render();
	}

	/**
	 * 保存安全参数设置
	 */
	public static void saveParamSite() {

	}

	/**
	 * 安全问题列表(首页)
	 */
	public static void questionList() {
		render();
	}

	/**
	 * 暂停/启用 安全问题
	 */
	public static void pauseOrStart() {

	}

	/**
	 * 添加新的安全问题
	 */
	public static void addQuestion() {
		render();
	}

	/**
	 * 保存添加后的新安全问题
	 */
	public static void saveQuestion() {

	}

}
