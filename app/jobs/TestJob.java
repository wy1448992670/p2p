/*
 * @(#)TestJob.java 2017年6月22日下午3:20:44
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package jobs;

import play.jobs.OnApplicationStart;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年6月22日下午3:20:44 TODO
 */
@OnApplicationStart
public class TestJob extends BaseJob {

	@Override
	public void doJob() throws Exception {
		// TODO Auto-generated method stub
		System.out.println(IS_JOB);
		super.doJob();
	}
}
