package jobs;

import javax.persistence.EntityManager;

import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import business.TemplateEmail;
import business.TemplateSms;
import business.TemplateStation;

/**
 * 备份并删除已发送的短信，每天凌晨0点10分执行
 *
 * @author hys
 * @createDate  2015年5月26日 下午5:13:32
 *
 */
@On("0 10 00 * * ?")
public class SmsBakupAndClear extends BaseJob {

	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		 
		EntityManager em = JPA.em();

		String bakupSql = "insert into t_system_mobile_sms_send (mobile, body) select mobile, body from t_system_mobile_sms_sending where is_sent = 1";
		try {
			em.createNativeQuery(bakupSql).executeUpdate();
		} catch (Exception e) {
			Logger.error("备份已发送的短信时，%s", e.getMessage());
		}
		
		String clearSql = "delete from t_system_mobile_sms_sending where is_sent = 1";
		try {
			em.createNativeQuery(clearSql).executeUpdate();
		} catch (Exception e) {
			Logger.error("删除已发送的短信时，%s", e.getMessage());
		}
	}
}
