package jobs;

import business.TemplateSms;
import business.TemplateStation;
import play.jobs.Every;
import play.jobs.Job;

/**
 * 每5分钟扫描一次站短信缓存表，并发送短信
 *
 * @author hys
 * @createDate  2015年5月26日 下午3:36:03
 *
 */
@Every("2min")
public class SmsSend extends BaseJob {

	public void doJob() {
		
		if(!"1".equals(IS_JOB))return;
		
		TemplateSms.dealSmsTask();
		
	}
	
}
