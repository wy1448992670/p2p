package controllers.supervisor.systemSettings;

import org.apache.commons.lang.StringUtils;
import utils.ErrorInfo;
import utils.WeChatUtil;
import business.BackstageSet;
import constants.Constants;
import constants.OptionKeys;
import controllers.supervisor.SupervisorController;

public class WeChatSettingAction extends SupervisorController {
	
	/**
	 * 查询微信欢迎语
	 */
	public static void weiXinWelcomeToLanguage(String weiXinWelcomeBind, String weiXinWelcomeUnBind) {
		ErrorInfo error = new ErrorInfo();
		
		String bind = OptionKeys.getvalue("bind", error);
		if (bind == null || error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		String unbound = OptionKeys.getvalue("unbound", error);
		if (unbound == null || error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		String languageBind = BackstageSet.getWeiXinLanguage("weixin_welcome_to_language_bind", error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		String languageUnBound = BackstageSet.getWeiXinLanguage("weixin_welcome_to_language_unbound", error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
	
		render(languageBind, languageUnBound, bind, unbound, weiXinWelcomeBind, weiXinWelcomeUnBind);
		
	}
	
	
	/**
	 * 设置微信欢迎语（绑定）
	 */
	public static void saveWeiXinWelcomeToLanguageBind() {
		ErrorInfo error = new ErrorInfo();
		
		String weiXinWelcomeBind = params.get("languageBind");
		String weiXinWelcomeUnBind = params.get("languageUnBound");
		String bind = OptionKeys.getvalue("bind", error);
		String unbound = OptionKeys.getvalue("unbound", error);
		if (bind == null || error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		if (unbound == null || error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		if (StringUtils.isBlank(weiXinWelcomeBind)) {
			flash.put("info", "微信欢迎语(绑定)不能为空");
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		if (StringUtils.isBlank(weiXinWelcomeUnBind)) {
			flash.put("info", "微信欢迎语(解绑)不能为空");
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		if ((WeChatUtil.getByteSize(weiXinWelcomeBind) >= Constants.TEXT_MAX_LENGTH)) {
			flash.put("info", "微信欢迎语(绑定)长度过长");
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		if (!weiXinWelcomeBind.contains(bind)) {
			flash.put("info", "微信欢迎语(绑定)必须包含" + bind);
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		
		if ((WeChatUtil.getByteSize(weiXinWelcomeUnBind) >= Constants.TEXT_MAX_LENGTH)) {
			flash.put("info", "微信欢迎语(解绑)长度过长");
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		if (!weiXinWelcomeUnBind.contains(unbound)) {
			flash.put("info", "微信欢迎语(解绑)必须包含" + unbound);
			flash.put("status", 1);
			weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
		}
		
		BackstageSet.editWeiXinLanguage("weixin_welcome_to_language_bind", weiXinWelcomeBind, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		BackstageSet.editWeiXinLanguage("weixin_welcome_to_language_unbound", weiXinWelcomeUnBind, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		flash.put("info", "编辑成功");
		weiXinWelcomeToLanguage(weiXinWelcomeBind, weiXinWelcomeUnBind);
	}
	
	/**
	 * 设置微信欢迎语（解绑）
	 */
	/*public static void saveWeiXinWelcomeToLanguageUnBound() {
		ErrorInfo error = new ErrorInfo();
		
		String weiXinWelcome = params.get("languageUnBound");
		String unbound = OptionKeys.getvalue("unbound", error);
		if (unbound == null || error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		if (StringUtils.isBlank(weiXinWelcome)) {
			flash.put("info", "微信欢迎语不能为空");
			flash.put("languageUnBound", weiXinWelcome);
			flash.put("statusUnBound", 1);
			weiXinWelcomeToLanguage();
		}
		
		if ((WeChatUtil.getByteSize(weiXinWelcome) >= Constants.TEXT_MAX_LENGTH)) {
			flash.put("info", "微信欢迎语长度过长");
			//flash.put("languageUnBound", weiXinWelcome.substring(0, 200));
			flash.put("statusUnBound", 1);
			weiXinWelcomeToLanguage();
		}
		
		if (!weiXinWelcome.contains(unbound)) {
			flash.put("info", "微信欢迎语必须包含" + unbound);
			flash.put("languageUnBound", weiXinWelcome);
			flash.put("statusUnBound", 1);
			weiXinWelcomeToLanguage();
		}
		
		BackstageSet.editWeiXinLanguage("weixin_welcome_to_language_unbound", weiXinWelcome, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		flash.put("info", "编辑成功");
		weiXinWelcomeToLanguage();
	}
	*/
	/**
	 * 查询微信在线咨询语
	 */
	public static void weiXinConsultingLanguage() {
		ErrorInfo error = new ErrorInfo();
		
		String weiXinConsultingLanguage = BackstageSet.getWeiXinLanguage("weixin_consulting_language", error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(weiXinConsultingLanguage);
	}
	
	/**
	 * 设置微信在线咨询语
	 */
	public static void saveWeiXinConsultingLanguage() {
		ErrorInfo error = new ErrorInfo();
		
		String weiXinConsultingLanguage = params.get("weiXinConsultingLanguage");
		if (StringUtils.isBlank(weiXinConsultingLanguage) || WeChatUtil.getByteSize(weiXinConsultingLanguage) >= Constants.TEXT_MAX_LENGTH) {
			flash.put("info", "微信咨询语不能为空或者长度过长");
		//	flash.put("language", weiXinConsultingLanguage);
			flash.put("status", 1);
			
			weiXinConsultingLanguage();
		}
		
		BackstageSet.editWeiXinLanguage("weixin_consulting_language", weiXinConsultingLanguage, error);
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.put("info", "编辑成功");
		weiXinConsultingLanguage();
	}
}
