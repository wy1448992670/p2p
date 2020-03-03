package jobs;

import play.jobs.Job;
import play.jobs.On;
import business.Agency;
import business.Bill;
import constants.Constants;

@On("0 0 00 * * ?")
public class UpdateAgency extends BaseJob{
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		Agency.updateData();
	}
}
