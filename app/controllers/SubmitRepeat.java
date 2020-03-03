package controllers;

import org.apache.commons.lang.StringUtils;

import annotation.SubmitCheck;
import annotation.SubmitOnly;
import play.Logger;
import play.cache.Cache;
import play.mvc.Before;
import utils.CaptchaUtil;

public class SubmitRepeat extends BaseController{

	@Before
	static void checkAccess() {
		
		SubmitOnly check = getActionAnnotation(SubmitOnly.class);
		SubmitCheck addCheck = getActionAnnotation(SubmitCheck.class);
		
		if(addCheck != null) {
			 String uuid = CaptchaUtil.getUUID();
			 Logger.info("重复提交生成的校验码:"+uuid);
			 flash.success(uuid);
		}
		
		if(check != null) {
			String uuid = params.get("uuidRepeat");
			Logger.info("重复提交校验:"+uuid);
			if(StringUtils.isBlank(uuid) || Cache.get(uuid) == null) {
				String url = request.headers.get("referer").value();
				flash.error("请勿重复提交");
				redirect(url);
			}
			
			Cache.delete(uuid);
	    }
	}
}
