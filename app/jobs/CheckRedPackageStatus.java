package jobs;

import business.Invest;
import business.RedPackageHistory;
import business.User;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

@Every("60min")
public class CheckRedPackageStatus extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		 //查看红包是否过期
		Logger.info("********************检查用户中红包是否过期，开始********************");
		RedPackageHistory.scanningRedPackageStatus();
		Logger.info("********************检查用户中红包是否过期，结束********************");
	}
}
