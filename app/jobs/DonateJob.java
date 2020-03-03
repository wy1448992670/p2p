package jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import models.t_bill_invests;
import models.t_public_benefit;
import models.t_user_cps_income;
import models.t_user_cps_profit;
import models.t_user_details;
import models.t_user_donate;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import utils.DateUtil;
import business.BackstageSet;
import business.BillInvests;
import business.User;
import business.UserCpsProfit;

import com.alibaba.fastjson.JSON;

import constants.DealType;

/**
 * 
 * @description.  公益捐款
 *  
 * @modificationHistory.  
 * @author liulj 2017年3月20日下午1:35:47 TODO
 */
//@On("0 0 5 * * ?")
@Every("1min")
public class DonateJob extends BaseJob {
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		// 查询没有募集成功的公益活动
		t_public_benefit public_benefit = t_public_benefit.find("FROM t_public_benefit pb WHERE pb.actual_donate < pb.plan_donate ORDER BY pb.start_dt ASC LIMIT 1").first();
		if(public_benefit != null){
			Logger.info(String.format("发现正在募捐的公益活动：%s", public_benefit.name));
			// 查询没有使用的捐款
			List<t_user_donate> user_donates = t_user_donate.find("FROM t_user_donate WHERE benefit_id = 0 order by id asc").fetch(200);
			if(user_donates != null && user_donates.size() > 0){
				for(t_user_donate user_donate : user_donates){
					user_donate.benefit_id = public_benefit.id;
					user_donate.save();
					public_benefit.actual_donate += user_donate.user_donate + user_donate.admin_donate;
					public_benefit.donate_users += 1;
					Logger.info(String.format("正在给募捐的公益活动：%s；增加一笔捐款：%s", public_benefit.name, user_donate.user_donate + user_donate.admin_donate));
					if(public_benefit.actual_donate >= public_benefit.plan_donate){
						break;
					}
				}
				public_benefit.save();
				Logger.info(String.format("募捐的公益活动：%s；募捐成功 计划金额：%s；实际金额：%s", public_benefit.name, public_benefit.plan_donate, public_benefit.actual_donate));
			}
		}
	}
}
