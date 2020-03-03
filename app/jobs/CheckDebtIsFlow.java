package jobs;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import reports.StatisticCPS;
import business.Debt;
import constants.Constants;

//h 小时, mn 分钟, s 秒


/**
 * 每五分钟判断正在转让的债权是否到达流拍时间
 */

@Every("5min")
public class CheckDebtIsFlow extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		if(Constants.DEBT_USE) {
			Logger.info("--------------定时判断债权流拍,开始---------------------");
			Debt.judgeDebtFlow();
			Logger.info("--------------定时判断债权流拍,结束---------------------");
		}
	  
		StatisticCPS.saveOrUpdateRecord(); // 每5分钟统计一次CPS推广统计
	}
}
