package controllers.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import constants.Constants;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Request;
import utils.ErrorInfo;
import utils.Security;

/**
 * @ClassName YMDInterceptor
 * @Description 亿美贷app拦截器
 * @author zj
 * @Date Jan 22, 2019 7:22:22 PM
 * @version 1.0.0
 */
public class YMDInterceptor extends Controller {

	@Before(unless = { "ymd.FrontFileUploadController.getOperatorAuthH5URL" })
	protected static void fileUploadAuth() throws Exception {
		Request request = Request.current.get();
		Logger.debug("正在执行ation请求命令：" + request.action);
		Map<String, Object> jsoMap = new HashMap<String, Object>();
		jsoMap.put("error", "-1");
		jsoMap.put("msg", "成功");
		ErrorInfo error = new ErrorInfo();
		try {
			long userId = Security.checkSign(request.params.get("userId"), Constants.USER_ID_SIGN, Constants.VALID_TIME, error);
			if (-1 == userId) {
				jsoMap.put("error", error.code);
				jsoMap.put("msg", error.msg);
				throw new Exception(JSON.toJSONString(jsoMap));
			}
			Logger.info(userId + "");
		} catch (Exception e) {
			jsoMap.put("error", "-2");
			jsoMap.put("msg", "非法访问");
			Logger.info(e.getMessage(), e);
			throw new Exception(JSON.toJSONString(jsoMap));
		}
	}

}
