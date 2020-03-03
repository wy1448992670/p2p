package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import constants.Constants;
import constants.SupervisorEvent;
import constants.Templets;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.PageBean;
import utils.Security;
import models.t_message_email_templates;
import models.t_system_email_sending;

public class TemplateEmail implements Serializable{

	public long id;
	private long _id;
	
	public void setId(long id) {
		t_message_email_templates email = null;
		try {
			email = t_message_email_templates.findById(id); 
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("setId,根据id查询邮件模板时"+e.getMessage());
			this._id = -1;
			
			return ;
		}
		
		if(email==null){
			this._id = -1;
			
			return ;
		}
		
		this._id = email.id;
		this.time = email.time;
		this.scenarios = email.scenarios;
		this.title = email.title;
		this.content = email.content;
		this.size = email.size;
		this.status = email.status;
	}
	
	public long getId() {
		return _id;
	}
	
	public Date time;
	public String scenarios;
	public String title;
	public String content;
	public double size;
	public boolean status;
	
	/**
	 * 添加邮件模板
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public int create(long supervisorId, ErrorInfo error) {
		error.clear();
		
		t_message_email_templates email = new t_message_email_templates();
		
		email.time = new Date();
		email.scenarios = this.scenarios;
		email.title = this.title;
		email.content = this.content;
		email.size = this.size;
		email.status = Constants.TRUE;
		
		try {
			email.save(); 
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("create,添加邮件模板时"+e.getMessage());
			error.code = -1;
			error.msg = "根据邮件模板id查询失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		this._id = email.id;
		error.code = 0;
		
		return 0;
	}
	
	/**
	 * 编辑邮件模板
	 * @param id
	 * @param error
	 * @return
	 */
	public int edit(long id, ErrorInfo error) {
		error.clear();
		
		t_message_email_templates email = null;
		
		try {
			email = t_message_email_templates.findById(id); 
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("edit,根据邮件模板查询邮件时"+e.getMessage());
			error.code = -1;
			error.msg = "根据邮件模板id查询失败";
			
			return error.code;
		}
		
		if(email==null){
			error.code = -2;
			error.msg = "该邮件模板不存在";
			
			return error.code;
		}
		
		email.scenarios = this.scenarios;
		email.title = this.title;
		email.content = Templets.replaceAllHTML(this.content); 
		
		try {
			email.save(); 
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("edit,保存邮件模板时"+e.getMessage());
			error.code = -3;
			error.msg = "保存邮件模板失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_EMAIL_TEMPLATE, "编辑邮件模板", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "邮件模板编辑成功！";
		
		return 0;
	}
	
	/**
	 * 更新邮件模板状态
	 * @param supervisorId
	 * @param id
	 * @param status true使用 false暂停
	 * @param error
	 * @return
	 */
	public static int updateStatus(long id, boolean status, ErrorInfo error) {
		error.clear();
		
		EntityManager em = JPA.em();
		Query query = em.createQuery("update t_message_email_templates set status = ? where id = ?")
				.setParameter(1, status).setParameter(2, id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate(); 
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("updateStatus,根据邮件模板查询邮件时"+e.getMessage());
			error.code = -1;
			error.msg = "更新邮件模板状态失败";
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		if (status) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_EMAIL_TEMPLATE, "启用邮件模板", error);
		} else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DISABLE_EMAIL_TEMPLATE, "暂停邮件模板", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "更新邮件模板状态成功";
		
		return 0;
	}
	
	/**
	 * 查询邮件模板
	 * @param currPage
	 * @param pageSize
	 * @param type 1 标题 2 内容
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<t_message_email_templates> query(int currPage, int pageSize, int type, String keyword, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		String condition = "1 = 1";
		List<Object> params = new ArrayList<Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			if(type == 1) {
				condition += " and title like ?";
				params.add("%" + keyword + "%");
			}else if(type == 2) {
				condition += " and content like ?";
				params.add("%" + keyword + "%");
			}
		}
		
		int count = 0;
		List<t_message_email_templates> page = null;
		
		try {
			count = (int) t_message_email_templates.count(condition, params.toArray());
			page = t_message_email_templates.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("查询邮件模板时"+e.getMessage());
			error.code = -1;
			error.msg = "查询邮件模板失败";
			
			return null;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<t_message_email_templates> bean = new PageBean<t_message_email_templates>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 发送邮件 
	 * @param type 0 不加事件 1普通邮件 2 重置密码邮件 3 催收邮件
	 * @param email
	 * @param title
	 * @param content
	 * @param error
	 */
	public static void sendEmail(int type, String email, String title, String content, ErrorInfo error) {

		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入邮件内容";
			
			return;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		try{
			org.apache.commons.mail.HtmlEmail sendEmail = new org.apache.commons.mail.HtmlEmail();
			sendEmail.setHostName(backstageSet.emailWebsite);
			sendEmail.setAuthentication(backstageSet.mailAccount, backstageSet.mailPassword);
			Logger.info("form email"+backstageSet.mailAccount);
			sendEmail.setFrom(backstageSet.mailAccount);
			sendEmail.addTo(email);
			if(title == null) {
				title = "";
			}
			sendEmail.setSubject(title);
			sendEmail.setCharset("utf-8");
			sendEmail.setMsg(content);
			sendEmail.send();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("邮件发送失败"+e.getMessage());
			error.code = -1;
			error.msg = "邮件发送失败";
			
			return ;
		}
		
		if(type != 0) {
			switch (type) {
			case 1 :
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.SEND_EMAIL, "发送邮件", error);
				break;
			case 2 :
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.RESET_PASSWORD, "发送重置用户密码邮件", error);
				break;
			case 3 :
				DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.BILL_COLLECTION, "邮件账单催收", error);
				break;
			}
			
			if (error.code < 0) {
				JPA.setRollbackOnly();
			}
		}
		
		error.code = 0;
		error.msg = "邮件发送成功";
	}
	
	/**
	 * 发送邮件 (不加事件)
	 * @param email
	 * @param title
	 * @param content
	 * @param error
	 */
	public static void sendEmail(String email, String title, String content, ErrorInfo error) {

		if(StringUtils.isBlank(content)) {
			error.code = -1;
			error.msg = "请输入邮件内容";
			
			return;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		try{
			org.apache.commons.mail.HtmlEmail sendEmail = new org.apache.commons.mail.HtmlEmail();
			sendEmail.setHostName(backstageSet.emailWebsite);
			sendEmail.setAuthentication(backstageSet.mailAccount, backstageSet.mailPassword);
			Logger.info("form email"+backstageSet.mailAccount);
			sendEmail.setFrom(backstageSet.mailAccount);
			sendEmail.addTo(email);
			if(title == null) {
				title = "";
			}
			sendEmail.setSubject(title);
			sendEmail.setCharset("utf-8");
			sendEmail.setMsg(content);
			sendEmail.send(); 
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("邮件发送失败"+e.getMessage());
			error.code = -1;
			error.msg = "邮件发送失败";
			
			return ;
		}
		
		error.code = 0;
		error.msg = "邮件发送成功";
	}
	
	/**
	 * 发送激活邮件
	 * @param user
	 * @param error
	 */
	public static void activeEmail(User user, ErrorInfo error) {
		error.clear();
		
		if(user == null) {
			error.code = -1;
			error.msg = "无法获取当前用户，请稍后再试";
			
			return ;
		}
		
		TemplateEmail tEmail = new TemplateEmail();
		tEmail.id = 2;
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String sign = Security.addSign(user.id, Constants.ACTIVE);
		String url = Constants.ACTIVE_EMAIL + sign;
		
		String content = new String(tEmail.content);
		
		content = content.replace("<p","<div");
		content = content.replace("</p>","</div>");
		content = content.replace(Constants.EMAIL_NAME, user.name);
		content = content.replace(Constants.EMAIL_LOGIN, "<a href = "+Constants.LOGIN+">登录</a>");
		content = content.replace(Constants.EMAIL_EMAIL, user.email);
		content = content.replace(Constants.EMAIL_TELEPHONE, backstageSet.platformTelephone == null ? (backstageSet.platformTelephone2==null?"":backstageSet.platformTelephone2) : backstageSet.platformTelephone);
		content = content.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
		content = content.replace(Constants.EMAIL_URL, "<a href = "+url+">"+url+"</a>");
		content = content.replace(Constants.EMAIL_TIME, DateUtil.dateToString(new Date()));
		
		TemplateEmail.sendEmail(0, user.email, tEmail.title, content, error);
	}
	
	/**
	 * 发送系统邮件
	 * @param email
	 * @param title
	 * @param content
	 */
	public static void addEmailTask(String email, String title, String content) {
		
		//加上email是否为空的判断 yaoyi
		if(null==email || "".equals(email.trim())){
			return;
		}
		
		t_system_email_sending sending = new t_system_email_sending();
		
		sending.time = new Date();
		sending.email = email;
		sending.title = title;
		sending.body = content;
		
		try {
			sending.save();
		}catch(Exception e){
			Logger.info("发送系统邮件时："+e.getMessage());
		}
	}
	
	/**
	 * 定时任务发送系统邮件
	 */
	public static void dealEmailTask() {
		List<t_system_email_sending> sendings = null;
		
		//查询所有未发送的邮件
		try {
			sendings = t_system_email_sending.find("is_sent = 0").fetch(Constants.JOB_EMAIL_AMOUNT);
		} catch (Exception e) {
			Logger.error("定时任务发送系统邮件" + e.getMessage());
			
			return ;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		org.apache.commons.mail.HtmlEmail sendEmail = null;
		String email = backstageSet.mailAccount;
		
		String title = "";
		
		EntityManager em = JPA.em();
		for(t_system_email_sending sending : sendings) {
			sendEmail = new org.apache.commons.mail.HtmlEmail();
			sendEmail.setHostName(backstageSet.emailWebsite);
			sendEmail.setAuthentication(backstageSet.mailAccount, backstageSet.mailPassword);
			if(StringUtils.isNotBlank(sending.email)) {
				try {
					sendEmail.setFrom(email,"亿亿理财");
					sendEmail.addTo(sending.email);
				} catch (EmailException e) {
					Logger.info("定时发送邮件时（设置接收方）："+e.getMessage());
					
					continue;
				}
				
				sendEmail.setCharset("utf-8");
				
				title = sending.title;
				if(title == null) {
					title = "";
				}
				
				sendEmail.setSubject(title);
				
				try {
					sendEmail.setMsg(sending.body);
					sendEmail.send();
				} catch (EmailException e) {
					Logger.info("定时发送邮件时（设置发送内容）："+e.getMessage());
					
				}

				//无论邮件发送成功或失败，邮件只发送一次
				try {
					em.createNativeQuery("update t_system_email_sending set is_sent = 1 where id = ?")
					.setParameter(1, sending.id)
					.executeUpdate();
				} catch (Exception e) {
					Logger.error("定时发送邮件时（更新发送状态is_sent=1）："+e.getMessage());
				}
			}
		}
	}
	
	/**
	 * 获得邮件的模板信息
	 * @param id
	 * @param error
	 * @return
	 */
	public static TemplateEmail getEmailTemplate(long id, ErrorInfo error) {
		error.clear();
		TemplateEmail email = (TemplateEmail) Cache.get("EMAIL_"+id);
		
		t_message_email_templates emailTemplate = null;
		
		if(email == null) {
			try{
				emailTemplate = t_message_email_templates.findById(id);
			}catch(Exception e) {
				Logger.error("查询邮件模板时：%s", e.getMessage());
				error.code = -1;
				error.msg = "查询邮件模板失败";
				
				return null;
			}
			
			if(error.code < 0) {
				return null;
			}
			
			email = new TemplateEmail();
			
			email.setInfo(emailTemplate);
			
			Cache.set("EMAIL_"+emailTemplate.id, email);
		}
		
		return email;
	}
	
	/**
	 * 赋值信息
	 */
	public void setInfo(t_message_email_templates email) {
		this._id = email.id;
		this.time = email.time;
		this.scenarios = email.scenarios;
		this.title = email.title;
		this.content = email.content;
		this.size = email.size;
		this.status = email.status;
	}
}
