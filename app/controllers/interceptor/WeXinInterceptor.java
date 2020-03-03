package controllers.interceptor;

import play.Logger;
import play.mvc.Before;
import business.BackstageSet;
import business.User;

import com.shove.security.License;
import controllers.BaseController;
import controllers.wechat.service.RegistAndLogin;

public class WeXinInterceptor extends BaseController {
	@Before(unless={"wechat.account.AccountHome.errorShow"})
	public static void checkLogin() {
//		try {
//			License.update(BackstageSet.getCurrentBackstageSet().registerCode);
//			if (!(License.getDomainNameAllow() && License.getWebPagesAllow())) {
//				flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
//				RegistAndLogin.login();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Logger.info("进行正版校验时：" + e.getMessage());
//			flash.put("error", "此版本非正版授权，请联系晓风软件购买正版授权！");
//			RegistAndLogin.login();
//		}
		if (User.currUser() == null) {
			RegistAndLogin.login();
		}
	}
}
