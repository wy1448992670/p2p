package jobs;

import business.Invest;
import business.User;
import play.jobs.Every;
import play.jobs.Job;

@Every("15min")
public class EveryHourJob extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		Invest.automaticInvest();  //自动投标
		//User.payForCps();  //cps奖励发放
	}
}
