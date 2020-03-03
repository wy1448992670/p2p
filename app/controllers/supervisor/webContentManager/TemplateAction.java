package controllers.supervisor.webContentManager;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import business.TemplateEmail;
import business.TemplatePact;
import business.TemplateSms;
import business.TemplateSmsBlacklist;
import business.TemplateStation;
import business.UserV2;
import constants.Constants;
import constants.Constants.TemplateType;
import controllers.supervisor.SupervisorController;
import models.t_message_email_templates;
import models.t_message_sms_templates;
import models.t_message_station_templates;
import models.v_sms_blacklist;
import net.sf.json.JSONObject;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 系统通知模板管理
 * @author lzp
 * @version 6.0
 * @created 2014-6-25
 */
public class TemplateAction extends SupervisorController {
	/**
	 * 邮件模板列表
	 * @param currPage
	 * @param pageSize
	 * @param type
	 * @param keyword
	 */
	public static void emailTemplates(int currPage, int pageSize, int type, String keyword, long id) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_message_email_templates> page = TemplateEmail.query(currPage, pageSize, type, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		TemplateEmail template = null;
		
		if(id != 0){
			template = new TemplateEmail();
			template.id = id;
		}

		render(page, template, id);
	}
	
	/**
	 * 预览邮件模板
	 * @param id
	 */
	public static void previewEmailTemplate(long id) {
		TemplateEmail template = new TemplateEmail();
		template.id = id;
		
		render(template);
	}
	
	/**
	 * 编辑邮件模板页面
	 * @param id
	 */
	public static void editEmailTemplateInit(long id) {
		TemplateEmail template = new TemplateEmail();
		template.id = id;
		
		JSONObject json = new JSONObject();
		json.put("template", template);

		renderJSON(json);
	}
	
	/**
	 * 编辑邮件模板
	 * @param id
	 */
	public static void editEmailTemplate(long id, String scenarios, String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(0 == id || StringUtils.isBlank(scenarios) || StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
			error.code = -1;
			json.put("error", "数据有误!");
			
			renderJSON(json);
		}
			
		TemplateEmail template = new TemplateEmail();
		template.scenarios = scenarios;
		template.title = title;
		template.content = content;
		template.edit(id, error);
		
		json.put("error", error);

		renderJSON(json);
	}
	
	/**
	 * 更新邮件模板状态
	 * @param id
	 * @param status
	 */
	public static void updateEmailTemplateStatus(long id, boolean status) {
		ErrorInfo error = new ErrorInfo();
		TemplateEmail.updateStatus(id, status, error);
		
		renderJSON(error);
	}
	
	/**
	 * 短信模板列表
	 * @param currPage
	 * @param pageSize
	 * @param type
	 * @param keyword
	 */
	public static void smsTemplates(int currPage, int pageSize, String keyword, long id) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_message_sms_templates> page = TemplateSms.query(currPage, pageSize, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		TemplateSms template = null;
		
		if(id != 0){
			template = new TemplateSms();
			template.id = id;
		}
		
		render(page, template, id);
	}
	
	/**
	 * 预览短信模板
	 * @param id
	 */
	public static void previewSmsTemplate(long id) {
		TemplateSms template = new TemplateSms();
		template.id = id;
		
		render(template);
	}
	
	/**
	 * 编辑短信模板页面
	 * @param id
	 */
	public static void editSmsTemplateInit(long id) {
		TemplateSms template = new TemplateSms();
		template.id = id;
		
		JSONObject json = new JSONObject();
		json.put("template", template);

		renderJSON(json);
	}
	
	/**
	 * 编辑短信模板
	 * @param id
	 */
	public static void editSmsTemplate(long id, String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(0 == id || StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
			error.code = -1;
			json.put("error", "数据有误!");
			
			renderJSON(json);
		}
		
		TemplateSms template = new TemplateSms();
		template.title = title;
		template.content = content;
		template.edit(id, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 更新短信模板状态
	 * @param id
	 * @param status
	 */
	public static void updateSmsTemplateStatus(long id, boolean status) {
		ErrorInfo error = new ErrorInfo();
		TemplateSms.updateStatus(id, status, error);
		
		renderJSON(error);
	}
	
	/**
	 * 系统站内信模板列表
	 * @param currPage
	 * @param pageSize
	 * @param type
	 * @param keyword
	 */
	public static void stationTemplates(int currPage, int pageSize, int type, String keyword, long id) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_message_station_templates> page = TemplateStation.query(TemplateType.SYSTEM, currPage, pageSize, type, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		TemplateStation template = null;
		
		if(id != 0){
			template = new TemplateStation();
			template.id = id;
		}
		
		render(page, template, id);
	}
	
	

	
	
	/**
	 * 自定义站内信模板列表
	 * @param currPage
	 * @param pageSize
	 * @param type
	 * @param keyword
	 */
	public static void myStationTemplates(int currPage, int pageSize, int type, String keyword, long id) {
		ErrorInfo error = new ErrorInfo();
		PageBean<t_message_station_templates> page = TemplateStation.query(TemplateType.CUSTOM, currPage, pageSize, type, keyword, error);
		
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		TemplateStation template = null;
		
		if(id != 0){
			template = new TemplateStation();
			template.id = id;
		}
		
		render(page, template, id);
	}
	
	/**
	 * 添加自定义站内信模板
	 * @param scenarios
	 * @param title
	 * @param content
	 */
	public static void createStationTemplate(String scenarios, String title, String content) {
		if( StringUtils.isBlank(scenarios) ||
			StringUtils.isBlank(title) ||
			StringUtils.isBlank(content) ||	
			scenarios.length() > 50 || 
			title.length() > 200 
		  ){
			flash.error("提交数据有误!");
			
			myStationTemplates(1, 10, 0, "", 0);
		}
		
		if (StringUtils.length(content) > 10000){
			flash.error("内容不能大于10000个字符!");
			myStationTemplates(1, 10, 0, "", 0);
		}
		
		ErrorInfo error = new ErrorInfo();
		TemplateStation template = new TemplateStation();
		template.scenarios = scenarios;
		template.title = title;
		template.content = content;
		template.type = TemplateType.CUSTOM;
		template.create(error);
		
		myStationTemplates(1, 10, 0, "", 0);
	}
	
	/**
	 * 预览站内信模板
	 * @param id
	 */
	public static void previewStationTemplate(long id) {
		TemplateStation template = new TemplateStation();
		template.id = id;
		
		render(template);
	}
	
	/**
	 * 编辑站内信模板页面
	 * @param id
	 */
	public static void editStationTemplateInit(long id) {
		TemplateStation template = new TemplateStation();
		template.id = id;
		
		render(template);
	}
	
	/**
	 * 编辑站内信模板
	 * @param id
	 */
	public static void editStationTemplate(long id, String scenarios, String title, String content) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(0 == id || StringUtils.isBlank(scenarios) || StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
			error.code = -1;
			json.put("error", "数据有误!");
			
			renderJSON(json);
		}
		
		TemplateStation template = new TemplateStation();
		template.scenarios = scenarios;
		template.title = title;
		template.content = content;
		template.edit(id, error);
		
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 更新站内信模板状态
	 * @param id
	 * @param status
	 */
	public static void updateStationTemplateStatus(long id, boolean status) {
		ErrorInfo error = new ErrorInfo();
		TemplateStation.updateStatus(id, status, error);
		
		renderJSON(error);
	}
	
	
	/**
	 * 平台协议模板列表
	 */
	public static void platformProtocol(){
		ErrorInfo error = new ErrorInfo();
		List<TemplatePact> pacts = TemplatePact.queryAllPacts(error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(pacts);
	}
	
	/**
	 * 更新平台协议模板状态
	 * @param id
	 * @param status
	 */
	public static void updatePactStatus(int id,boolean status){
		ErrorInfo error = new ErrorInfo();
		TemplatePact.editStatus(id, status, error);
		
		renderJSON(error);
	}
	
	
	/**
	 * 编辑平台协议模板
	 * @param id
	 */
	public static void updatePact(int id,String title,String content ){
		ErrorInfo error = new ErrorInfo();
		TemplatePact pact = new TemplatePact();
		pact.id = id;
		pact.title = title;
		pact.content = content;
		TemplatePact.updatePact(pact, error);
		flash.error(error.msg);
		platformProtocol();
	}
	
	
	/**
	 * 浏览平台协议模板
	 * @param id
	 */
	public static void viewPact(int pactId){
		TemplatePact pact = new TemplatePact();
		pact.id = pactId;
		
		render(pact);
	}
	
	/**
	 * 短信黑名单列表
	 * @param currPage
	 * @param pageSize
	 * @param mobile
	 * @param id
	 */
	public static void smsBlacklistTemplates(int currPage, int pageSize, String mobile, long id) {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_sms_blacklist> page = TemplateSmsBlacklist.querySmsBlacklist(currPage, pageSize, mobile, error);
		if(currPage > 1 && page.page.size() == 0) {
			page = TemplateSmsBlacklist.querySmsBlacklist(currPage - 1, pageSize, mobile, error);
		}
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(page);
	}
	
	/**
	 * 将用户添加到短信黑名单
	 * @param userId
	 * @param isBlacklist 0：从短信黑名单移除  1：添加到短信黑名单  
	 */
	public static void addOrRemoveSmsBlacklist(Long userId, Integer isBlacklist) {
		if(userId != null) {
			isBlacklist = isBlacklist == null ? 0 : isBlacklist;
			renderText(UserV2.updateUserSmsBlacklist(userId, isBlacklist));
		}
	}
}
