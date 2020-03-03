package jobs;

import business.TemplateEmail;
import business.TemplateSms;
import business.TemplateStation;
import play.jobs.Every;
import play.jobs.Job;

/**
 * 每5分钟扫描一次邮件缓存表，并发送邮件
 *
 * @author hys
 * @createDate  2015年5月26日 下午3:34:04
 *
 */
@Every("5min")
public class EmailSend extends BaseJob {

	public void doJob() {
		if(!"1".equals(IS_JOB))return;

		TemplateEmail.dealEmailTask();
		
	}
	
}
