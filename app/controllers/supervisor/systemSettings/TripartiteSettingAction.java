package controllers.supervisor.systemSettings;

import org.apache.commons.lang.StringUtils;
import constants.SupervisorEvent;
import controllers.supervisor.SupervisorController;
import business.BackstageSet;
import business.DealDetail;
import business.Supervisor;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 第三方通道设置
 * 
 * @author bsr
 * 
 */
public class TripartiteSettingAction extends SupervisorController {

	/**
	 * 短信通道
	 */
	public static void SMSPassage() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}
	
	/**
	 * 保存短信通道
	 */
	public static void saveSMS(String smsAccount, String smsPassword) {
        ErrorInfo error = new ErrorInfo();
        
        if(StringUtils.isBlank(smsAccount)) {
        	flash.error("请填写短信通道用户名");
        	
        	SMSPassage();
        }
        
        if(StringUtils.isBlank(smsAccount)) {
        	flash.error("请填写短信通道密码");
        	
        	SMSPassage();
        }
        
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.smsAccount = smsAccount;
		backstageSet.smsPassword = smsPassword;
		
        backstageSet.SMSChannels(error);
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		BackstageSet.setCurrentBackstageSet(backstageSet);
		
		flash.success(error.msg);
		
		SMSPassage();
	}

	/**
	 * 邮件通道
	 */
	public static void mailPassage() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}
	
	/**
	 * 保存邮件通道
	 */
	public static void saveMail() {
        ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.mailAccount = params.get("mailAccount");
		backstageSet.mailPassword = params.get("mailPassword");
		backstageSet.emailWebsite = params.get("emailWebsite");
		backstageSet.POP3Server = params.get("POP3Server");
		backstageSet.STMPServer = params.get("STMPServer");
		String isChargesChannels = params.get("isChargesChannels");
		String value = isChargesChannels == null ? "0" : "1";
		backstageSet.isChargesChannels = value;
		
        backstageSet.MAILChannels(error);
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		flash.success(error.msg);
		
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.MAIL_CHANNEL, 
				"修改短信通道设置", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();
			
			return ;
		}
		
		BackstageSet.setCurrentBackstageSet(backstageSet);
		
		mailPassage();
	}
}
