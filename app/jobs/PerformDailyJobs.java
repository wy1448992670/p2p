package jobs;

import business.Bill;
import play.jobs.Job;
import play.jobs.On;
import utils.ErrorInfo;


/**
 * 每天00:00点执行程序
 * @author zhs
 * vesion: 6.0 
 * @date 2014-7-25 下午09:23:33
 */
 @On("0 01 00 * * ?")
public class PerformDailyJobs extends BaseJob {

	 public void doJob(){
		 if(!"1".equals(IS_JOB))return;
		 
		 ErrorInfo error = new ErrorInfo();
		 Bill bill = new Bill();		 
		 bill.systemMakeOverdue(error); //系统标记逾期
		 
	 }
}
