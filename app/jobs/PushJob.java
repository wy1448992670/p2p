package jobs;

import constants.Constants;
import business.Bill;
import play.jobs.Job;

/**
 * 每天定时定点任务
 * @author lwh
 *
 */
/*正式测试或上线请打开此任务*/
//@On("0 30 12 * * ?")
public class PushJob extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		Bill.queryRecentlyBillsForCast(Constants.PUSH_BILL);
	}
}
