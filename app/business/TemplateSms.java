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

import com.shove.gateway.sms.EimsSMS;

import constants.Constants;
import constants.SupervisorEvent;
import constants.Templets;
import models.t_message_sms_templates;
import models.t_system_mobile_sms_sending;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.PageBean;
import utils.SMSUtil;
import utils.ymsms.SMS;

public class TemplateSms implements Serializable{

	public long id;
	private long _id;

	public void setId(long id) {
		t_message_sms_templates sms = null;
		try {
			sms = t_message_sms_templates.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId,根据id查询短信模板时" + e.getMessage());
			this._id = -1;

			return;
		}

		if (sms == null) {
			this._id = -1;

			return;
		}

		this._id = sms.id;
		this.time = sms.time;
		this.title = sms.title;
		this.scenarios = sms.scenarios;
		this.content = sms.content;
		this.size = sms.size;
		this.status = sms.status;
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
	 * 添加短信模板
	 * 
	 * @param supervisorId
	 * @param info
	 * @return
	 */
	public int create(long supervisorId, ErrorInfo error) {
		error.clear();

		t_message_sms_templates sms = new t_message_sms_templates();

		sms.time = new Date();
		sms.scenarios = this.scenarios;
		sms.content = this.content;
		sms.size = this.size;
		sms.status = Constants.TRUE;

		try {
			sms.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("create,添加短信模板时" + e.getMessage());
			error.code = -1;
			error.msg = "根据短信模板id查询失败";
			JPA.setRollbackOnly();

			return error.code;
		}

		this._id = sms.id;
		error.code = 0;

		return 0;
	}

	/**
	 * 编辑短信模板
	 * 
	 * @param supervisorId
	 * @param id
	 * @param info
	 * @return
	 */
	public int edit(long id, ErrorInfo error) {
		error.clear();

		t_message_sms_templates sms = null;

		try {
			sms = t_message_sms_templates.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("edit,根据短信模板查询短信时" + e.getMessage());
			error.code = -1;
			error.msg = "根据短信模板id查询失败";

			return error.code;
		}

		if (sms == null) {
			error.code = -2;
			error.msg = "该短信模板不存在";

			return error.code;
		}

		sms.title = this.title;
		sms.content = Templets.replaceAllHTML(this.content); 

		try {
			sms.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("edit,保存短信模板时" + e.getMessage());
			error.code = -3;
			error.msg = "保存短信模板失败";
			JPA.setRollbackOnly();

			return error.code;
		}
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_SMS_TEMPLATE, "编辑短信模板", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "短信模板编辑成功！";

		return 0;
	}

	/**
	 * 更新短信模板状态
	 * 
	 * @param supervisorId
	 * @param id
	 * @param status
	 *            true使用 false暂停
	 * @param info
	 * @return
	 */
	public static int updateStatus(long id, boolean status,
			ErrorInfo error) {
		error.clear();

		EntityManager em = JPA.em();
		Query query = em
				.createQuery(
						"update t_message_sms_templates set status = ? where id = ?")
				.setParameter(1, status).setParameter(2, id);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("updateStatus,根据短信模板查询短信时" + e.getMessage());
			error.code = -1;
			error.msg = "更新短信模板状态失败";
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
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_SMS_TEMPLATE, "启用短信模板", error);
		} else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DISABLE_SMS_TEMPLATE, "暂停短信模板", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "更新短信模板状态成功";
		
		return error.code;
	}

	/**
	 * 查询短信模板
	 * @param currPage
	 * @param pageSize
	 * @param type 1 标题 2 内容
	 * @param keyword
	 * @param error
	 * @return
	 */
	public static PageBean<t_message_sms_templates> query(int currPage, int pageSize, String keyword, ErrorInfo error) {
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
			condition += " and content like ?";
			params.add("%" + keyword + "%");
		}
		
		int count = 0;
		List<t_message_sms_templates> page = null;
		
		try {
			count = (int) t_message_sms_templates.count(condition, params.toArray());
			page = t_message_sms_templates.find(condition, params.toArray()).fetch(currPage, pageSize);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("查询短信模板时"+e.getMessage());
			error.code = -1;
			error.msg = "查询短信模板失败";
			
			return null;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		PageBean<t_message_sms_templates> bean = new PageBean<t_message_sms_templates>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = (int) count;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 发送系统短信
	 * @param mobile
	 * @param content
	 */
	public static void addSmsTask(String mobile, String content) {
		t_system_mobile_sms_sending sending = new t_system_mobile_sms_sending();
		
		sending.time = new Date();
		sending.mobile = mobile;
		sending.body = content;
		
		try {
			sending.save();
			Logger.info("sms content:"+content);
		}catch(Exception e){
			Logger.error("发送系统短信时："+e.getMessage());
		}
	}
	
	/**
	 * 定时任务发送系统短信
	 */
	public static void dealSmsTask() {
		List<t_system_mobile_sms_sending> sendings = null;
		
		//查询所有未发送的短信
		try {
			sendings = t_system_mobile_sms_sending.find("is_sent = 0").fetch(Constants.JOB_MSG_AMOUNT);
		} catch (Exception e) {
			Logger.error("定时任务发送系统短信" + e.getMessage());
			
			return ;
		}
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		String account = backstageSet.smsAccount;
		String password = backstageSet.smsPassword;
		
		EntityManager em = JPA.em();
		SMS sms = new SMS();
		for(t_system_mobile_sms_sending sending : sendings) {
			
			try {
				//EimsSMS.send(account, password, sending.body, sending.mobile);
				//SMSUtil.sendSMS(account, password, sending.body, sending.mobile);
				//sms.send(sending.mobile, sending.body);
				SMS.send(sending.mobile, sending.body, SMS.SEND_TYPE_SYSTEM);
			} catch (Exception e) {
				Logger.info("定时发送短信时："+e.getMessage());
			}
			
			//无论短信发送成功或失败，短信只发送一次
			try {
				em.createNativeQuery("update t_system_mobile_sms_sending set is_sent = 1 where id = ?")
				.setParameter(1, sending.id)
				.executeUpdate();
			} catch (Exception e) {
				Logger.error("定时发送短信时（更新发送状态is_sent=1）："+e.getMessage());
			}
		}
	}
	
	/**
	 * 获得缓存中的短信模板信息
	 * @param id
	 * @param error
	 * @return
	 */
	public static TemplateSms getSmsTemplate(long id, ErrorInfo error) {
		error.clear();
		TemplateSms sms = (TemplateSms) Cache.get("SMS_"+id);
		t_message_sms_templates smsTemplate = null;
		
		if(sms == null) {
			try{
				smsTemplate = t_message_sms_templates.findById(id);
			}catch(Exception e) {
				Logger.error("查询短信模板时：%s", e.getMessage());
				error.code = -1;
				error.msg = "查询短信模板失败";
				
				return null;
			}
			
			if(error.code < 0) {
				return null;
			}
			
			sms = new TemplateSms();
			
			sms.setInfo(smsTemplate);
			
			Cache.set("SMS_"+smsTemplate.id, sms);
		}
		
		return sms;
	}
	
	/**
	 * 赋值信息
	 */
	public void setInfo(t_message_sms_templates sms) {
		this._id = sms.id;
		this.time = sms.time;
		this.title = sms.title;
		this.scenarios = sms.scenarios;
		this.content = sms.content;
		this.size = sms.size;
		this.status = sms.status;
	}
}
