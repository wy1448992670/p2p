package jobs;

import business.UserAuditItem;
import play.jobs.Job;
import play.jobs.On;

/**
 * 每天检查一次资料是否过期
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-8-1 上午11:14:13
 */
@On("0 01 00 * * ?")
public class CheckAuditItemIsExpired extends BaseJob {

	@Override
	public void doJob() throws Exception {
		if(!"1".equals(IS_JOB))return;
		
		UserAuditItem.checkAuditItemIsExpired();
	}
}
