package jobs;

import business.DeductPact;
import play.Logger;
import play.jobs.On;

/**
 * 代扣协议合同定时
 * 
 * @ClassName DeductPactJob
 * @这里用一句话描述这个方法的作用
 * @author zj
 * @Date Apr 2, 2019 4:39:54 PM
 * @version 1.0.0
 */
@On("0 10 5 * * ?")
//@On("0 0/30 * * * ?")
public class DeductPactJob extends BaseJob {

	public void doJob() {
		if (!"1".equals(IS_JOB))
			return;
		try {
			DeductPact.createDeductPact();
		} catch (Exception e) {
			Logger.error("定时代扣合同出错==========>" + e.getMessage(), e);
		}
	}
}
