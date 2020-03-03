package controllers;

import annotation.DebtCheck;
import constants.Constants;
import controllers.front.account.AccountHome;
import controllers.supervisor.managementHome.HomeAction;
import play.Logger;
import play.mvc.Before;

/**
 * 是否支持债权转让功能校验，不支持时，选择跳转相应页面
 *
 * @author hys
 * @createDate  2015年9月9日 上午11:41:17
 *
 */
public class DebtTransferCheck extends BaseController{

	@Before
	static void checkAccess() {
		
		if(Constants.DEBT_USE){  //支持债权转让，直接返回
			
			return;
		}
		
		DebtCheck unit = getActionAnnotation(DebtCheck.class);
		
		if(unit != null) {
			Logger.info("本系统环境不支持债权转让功能！");
			
			flash.error("本系统环境暂不支持债权转让功能！");
			
			int value = unit.value();
			
			if(value == 1) {
				AccountHome.home();
			}else if(value == 2) {
				HomeAction.showHome();
			}
			
		}
	}
}
