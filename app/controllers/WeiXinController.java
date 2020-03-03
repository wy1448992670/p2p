package controllers;

import com.shove.web.security.InjectionInterceptor;
import constants.Constants;
import play.mvc.Before;
import play.mvc.Controller;

public class WeiXinController extends Controller {

	@Before
	protected static void injectionInterceptor() throws Exception {
		InjectionInterceptor inject = new InjectionInterceptor();
		inject.setIsKeepStreamOpen(true);
		inject.run();
	}
}
