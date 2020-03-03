/*
 * @(#)BaseJob.java 2017年6月22日下午3:18:44
 * Copyright 2015 Sopell, Inc. All rights reserved.
 */
package jobs;

import play.Play;
import play.jobs.Job;


 /**
 * @description.  
 *  
 * @modificationHistory.  
 * @author liulj 2017年6月22日下午3:18:44 TODO
 */

public class BaseJob extends Job{

	// 是否开启短信服务
	public final static String IS_JOB = Play.configuration.getProperty("is_job", "1");
	public final static String IS_SMS = Play.configuration.getProperty("is_sms", "1");

	@Override
	public void doJob() throws Exception {
		// TODO Auto-generated method stub
		super.doJob();
	}
	
}
