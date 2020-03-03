package controllers.interceptor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.mvc.Before;
import utils.DateUtil;
import utils.ErrorInfo;
import business.BackstageSet;
import business.Supervisor;

import com.shove.security.License;

import constants.OptionKeys;
import controllers.BaseController;
import controllers.supervisor.account.AccountAction;
import controllers.supervisor.login.LoginAction;
import controllers.supervisor.systemSettings.SoftwareLicensAction;

/**
 * 后台拦截器
 * @author lzp
 * @version 6.0
 * @created 2014-6-5
 */
public class SupervisorInterceptor extends BaseController{
	/**
	 * 登录拦截
	 */
	@Before(unless = {"supervisor.login.LoginAction.login",
			"supervisor.managementHome.HomeAction.showHome",
			"supervisor.systemSettings.SoftwareLicensAction.notRegister",
			"supervisor.systemSettings.SoftwareLicensAction.saveSoftwareLicens",
			"supervisor.financeManager.PlatformAccountManager.ipsOffSingleDeal",
			"ymd.BackFileUploadController.getOperatorAuthResultCallBack",
			"ymd.BackFileUploadController.getOperatorAuthCallBack"
			})
	public static void checkLogin() {
		try{
			/*License.update(BackstageSet.getCurrentBackstageSet().registerCode);
			if(!(License.getDomainNameAllow() && License.getAdminPagesAllow())) {
				flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
				SoftwareLicensAction.notRegister();
			}*/
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("进行正版校验时：" + e.getMessage());
			flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
			SoftwareLicensAction.notRegister();
		}
		if (Supervisor.isLogin()) {
			return;
		}
		
		LoginAction.loginInit();
	}
	
	/**
	 * 管理员对象放入renderArgs里边
	 */
	@Before
	public static void putSupervisor() {
		if (!Supervisor.isLogin()) {
			return;
		}
		
		renderArgs.put("supervisor", Supervisor.currSupervisor());
		
		renderArgs.put("systemOptions", BackstageSet.getCurrentBackstageSet());
	}
	
	/**
	 * 权限拦截
	 */
	@Before(unless = {
				"supervisor.account.AccountAction.home", 
				"supervisor.account.AccountAction.editSupervisor",
				"supervisor.financeManager.PlatformAccountManager.ipsOffSingleDeal",
				"supervisor.SupervisorController.getNewCity",
				"supervisor.SupervisorController.getAdCity",
				"ymd.BackFileUploadController.getOperatorAuthResultCallBack",
				"ymd.BackFileUploadController.getOperatorAuthCallBack",
				"ymd.BackFileUploadController.uploadFile",
				"ymd.BackFileUploadController.delFileRelation"
			})
	public static void checkRight() {
		String action = request.action;
		
		Supervisor currSupervisor = Supervisor.currSupervisor();
		if (null == currSupervisor) {
			LoginAction.loginInit();
			
			return;
		}
		if(currSupervisor.id==1L && "admin".equals(currSupervisor.name)){//admin账号
			return;
		}
		
		
		if (!currSupervisor.haveRight(action)) {
			Map<String ,String> map=new HashMap<String,	String>();
			//如果登陆管理员权限有对应二级菜单，则页面显示左侧菜单
			if(currSupervisor.haveLeft(action)){
				if(action.startsWith("supervisor.webContentManager")){
					map.put("leftbar", "supervisor/webContentManager/common/leftBar.control");
				}else if(action.startsWith("supervisor.bidManager")){
					map.put("leftbar", "supervisor/bidManager/bidManagerLeft.control");
				}else if(action.startsWith("supervisor.billCollectionManager")){
					map.put("leftbar", "supervisor/billCollectionManager/common/billCollectionManagerLeft.control");
				}else if(action.startsWith("supervisor.userManager")){
					map.put("leftbar", "supervisor/userManager/userLeft.control");
				}else if(action.startsWith("supervisor.financeManager")){
					map.put("leftbar", "supervisor/financeManager/common/financeManageLeft.control");
				}else if(action.startsWith("supervisor.networkMarketing")){
					map.put("leftbar", "supervisor/networkMarketing/CPSSpreadAction/networkMarketingLeft.control");
				}else if(action.startsWith("supervisor.dataStatistics")){
					map.put("leftbar", "supervisor/dataStatistics/statisticLeft.html");
				}else if(action.startsWith("supervisor.systemSettings")){
					map.put("leftbar", "supervisor/systemSettings/common/leftBar.control");
				}else if(action.startsWith("")){
					map.put("leftbar", "");
				}
			}
			renderTemplate("Application/insufficientRight.html",map);
		}
	}
	
	/**
	 * 未设密码拦截
	 */
	@Before(unless = {
				"supervisor.account.AccountAction.home", 
				"supervisor.account.AccountAction.editSupervisor",
				"supervisor.systemSettings.SoftwareLicensAction.notRegister",
				"supervisor.systemSettings.SoftwareLicensAction.saveSoftwareLicens",
				"supervisor.financeManager.PlatformAccountManager.ipsOffSingleDeal",
				"ymd.BackFileUploadController.getOperatorAuthResultCallBack",
				"ymd.BackFileUploadController.getOperatorAuthCallBack"
			})
	public static void goAccountHome() {
		Supervisor supervisor = Supervisor.currSupervisor();
		
		if (null == supervisor) {
			LoginAction.loginInit();
			
			return;
		}
		
		if (StringUtils.isBlank(supervisor.password)) {
			OptionKeys.siteValue(OptionKeys.PLATFORM_STARTUP_TIME, DateUtil.dateToString(new Date()), new ErrorInfo());
			
			AccountAction.home();
		}
	}
}
