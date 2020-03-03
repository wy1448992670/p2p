package jobs;

import business.Pact;
import play.jobs.Every;

/**
 * 
 * @description. 电子合同
 * 
 * @modificationHistory.
 * 
 * @author liulj 2017年3月20日下午1:35:47 TODO
 */
//@On("0 0 5 * * ?")
//@Every("8h")
public class PactSignJob extends BaseJob {

	public void doJob() {
		if (!"1".equals(IS_JOB))
			return;

		Pact.doHistoryJob(null, null, 50, "借款协议签署");
	}
}
