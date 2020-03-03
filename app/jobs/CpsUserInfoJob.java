package jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import business.BackstageSet;
import business.BillInvests;
import business.User;
import business.UserV2;
import constants.DealType;
import models.t_system_options;
import models.t_user_cps_income;
import models.t_user_cps_profit;
import models.t_user_details;
import models.t_users;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.jobs.On;
import play.jobs.Every;
import utils.DateUtil;

/**
 * 每天3点统计CPS用户信息
 * 
 * @author fei
 *
 */
@On("0 0 3 * * ?")
//@Every("3min")
public class CpsUserInfoJob extends BaseJob {
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
//		String is_cps = Play.configuration.getProperty("is_cps", "0");
//		if("0".equals(is_cps)){
//			Logger.info("未开启cps统计");
//			return;
//		}
		
		Logger.info("===> 统计CPS分成推广用户...");
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		Logger.info("===> [缓存]CPS收益分成比例："+backstageSet.rewardForRate);
		
		t_system_options system_options = t_system_options.findById(4003L);
		backstageSet.rewardForRate = Double.valueOf(system_options._value);
		Logger.info("===> [数据库]CPS收益分成比例："+backstageSet.rewardForRate);
		t_system_options cpsRelationStartDate = t_system_options.findById(10060L);
		try {
			backstageSet.cpsRelationStartDate =new SimpleDateFormat("yyyy-MM-dd").parse(cpsRelationStartDate._value);
		} catch (ParseException e1) {
			backstageSet.cpsRelationStartDate =new Date();
			e1.printStackTrace();
		}
		//2018-12-04 张琼麒删除,保存收益分成比例为0的数据,防止事后修改收益分成比例补发之前投资分成
		/*
		if(backstageSet.rewardForRate <= 0)
			return;
		*/
		if(backstageSet.rewardForRate < 0){
			backstageSet.rewardForRate = 0;
		}
		
		
		EntityManager em = JPA.em();

		String deleteSql = "delete from  t_cps_info";
		String insertSql = "INSERT INTO t_cps_info ( id, NAME, time,  register_length,credit_level_image_filename, recommend_count, recharge_count, bid_amount, commission_amount ) SELECT `t_users`.`id` AS `id`, `t_users`.`name` AS `name`, `t_users`.`time` AS `time`, timestampdiff(DAY, `t_users`.`time`, now()) AS `register_length`, ( SELECT t_credit_levels.image_filename AS credit_level_image_filename FROM t_credit_levels WHERE ( t_credit_levels.id = t_users.credit_level_id ) ) AS credit_level_image_filename, ( SELECT count(`user`.`id`) AS `COUNT(user.id)` FROM `t_users` `user` WHERE ( `user`.`recommend_user_id` = `t_users`.`id` ) ) AS `recommend_count`, ( SELECT count(`user`.`id`) AS `COUNT(user.id)` FROM `t_users` `user` WHERE ( ( `user`.`recommend_user_id` = `t_users`.`id` ) AND (`user`.`is_active` = 1) ) ) AS `recharge_count`, ( SELECT ifnull(sum(`t_bids`.`amount`), 0) AS `bid_amount` FROM `t_bids` WHERE ( `t_bids`.`user_id` IN ( SELECT DISTINCT `user`.`id` AS `id` FROM `t_users` `user` WHERE `user`.`recommend_user_id` IN ( SELECT DISTINCT `users`.`id` AS `id` FROM `t_users` `users` WHERE ( `users`.`recommend_user_id` = `t_users`.`id` ) ) ) AND (`t_bids`.`status` IN(4, 5)) ) ) AS `bid_amount`,  ( SELECT ifnull(sum(`ucp`.`cps_reward_ed`), 0) AS `cps_reward` FROM `t_user_cps_income` `ucp` WHERE ( `ucp`.`user_id` = `t_users`.`id` ) ) AS `commission_amount` FROM `t_users` WHERE `t_users`.`id` IN ( SELECT DISTINCT `t_users`.`recommend_user_id` AS `recommend_user_id` FROM `t_users` WHERE ( `t_users`.`recommend_user_id` > 0 ) ) ";
		String updateRate = "update t_cps_info t set t.active_rate = t.recharge_count /t.recommend_count";
		String updateInvestAmount = "UPDATE t_cps_info t SET t.invest_amount = ( SELECT ifnull( sum(`t_invests`.`amount`), 0 ) AS `IFNULL(sum(t_invests.amount),0)` FROM (`t_invests`, `t_bids`) WHERE (  `t_invests`.`bid_id` = `t_bids`.`id`  AND `t_bids`.`status` IN(3, 4, 5) AND `t_invests`.`user_id` IN ( SELECT `user`.`id` AS `id` FROM `t_users` `user` WHERE  `user`.`recommend_user_id` = t.id  ) ) )";
		
		
		try {
			

			
			
			// 查询所有通过cps推广注册的用户
			List<t_users> users = User.cpsUsers();
			
			if(users != null && users.size() > 0){
				Logger.info("===> 查询所有通过CPS分成推广注册的用户数："+users.size());
				for(t_users user : users){
					
					// 查询用户正常收款的理财账单的所得收益
					// 补充需求-1012-只要投资-预估收益
					List<Map<String, Object>> invests = BillInvests.findBillInvestIncome(user.id);
					//Logger.info("===> 处理用户【"+user.id+"】的理财账单"/*+JSON.toJSONString(invests)*/);
					
					//System.out.println("------："+JSON.toJSONString(t_user_cps_profit.findAll(), true));
					
					 // 已发放奖金
				    //Object cps_reward_ed = t_user_details.find("select sum(amount) from t_user_details where user_id = ? and operation = ?", user.recommend_user_id, DealType.CPS_RATE_COUNT).first();
				    //cps_reward_ed = cps_reward_ed == null ? 0 : cps_reward_ed;
				    
				   // Logger.info("===> 用户【"+user.recommend_user_id+"】已发放的奖励金额：" + cps_reward_ed);
					
					Map<Long, Object[]> userInfo = new HashMap<Long, Object[]>();
					
					if(invests != null && invests.size() > 0){
						for(Map<String, Object> invest : invests){
							
							t_user_cps_profit user_cps_profit = new t_user_cps_profit();
							user_cps_profit.user_id = user.id;
							user_cps_profit.user_mobile = user.mobile;
							user_cps_profit.recommend_user_id = user.recommend_user_id;
							user_cps_profit.bid_id = Long.valueOf(invest.get("bid_id").toString());
							user_cps_profit.bid_title = (String) invest.get("title");
							user_cps_profit.bid_is_loan = 1;
							user_cps_profit.invest_id =  Long.valueOf(invest.get("invest_id").toString());//投资ID
							user_cps_profit.invest_corpus = Double.valueOf(invest.get("amount").toString());//投资本金
							user_cps_profit.invest_interest = Double.valueOf(invest.get("correct_interest").toString());//投资应收利息
							user_cps_profit.invest_time = (Date) invest.get("audit_time");//放款时间
							user_cps_profit.cps_rate = backstageSet.rewardForRate;
							if(user.recommend_time ==null || user.recommend_time.getTime()<backstageSet.cpsRelationStartDate.getTime() ) {
								user_cps_profit.cps_reward = 0;
							}else {
								user_cps_profit.cps_reward = new BigDecimal(user_cps_profit.invest_interest*(backstageSet.rewardForRate/100.0)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
							}
							
							user_cps_profit.ins_dt = new Date();
							
							String investInfo = String.format("投资账单【%s】, 投资金额：%s元, 收益：%s元； 推荐人【%s】, CPS分成比例：%s, 银行家舍入法计算CPS分成：%s元", user_cps_profit.invest_id, user_cps_profit.invest_corpus, user_cps_profit.invest_interest, user.recommend_user_id, backstageSet.rewardForRate, user_cps_profit.cps_reward);
							
							long count = t_user_cps_profit.find("select count(id) from t_user_cps_profit where invest_id = ?", user_cps_profit.invest_id).first();
							
							// TODO-CPS黑名单处理
							try {
								com.alibaba.fastjson.JSONObject userBlacklist = UserV2.findUserBlacklist(user.recommend_user_id);
								int isBlacklist = userBlacklist.getIntValue("is_cps_blacklist");
								Date blacklistDt = (Date) userBlacklist.get("cps_blacklist_dt");

								if(isBlacklist == 1) {
									user_cps_profit.cps_reward = 0;
								}else if(user.recommend_time !=null && blacklistDt!=null && user.recommend_time.getTime()<blacklistDt.getTime()) {
									user_cps_profit.cps_reward = 0;
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
				    		if(count == 0){
				    			user_cps_profit.save();
				    			Logger.info("新增CPS分成账单："+investInfo);
				    			
				    			Calendar cal = Calendar.getInstance();//使用日历类  
							    int year = cal.get(Calendar.YEAR); 
							    int month = cal.get(Calendar.MONTH) + 1;
							    
							    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
							    
							    String start = String.format("%s-%s-01 00:00:00", year, month);
							    String end = String.format("%s-%s-01 00:00:00", cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1));
							    
							    t_user_cps_income cpsIncome = t_user_cps_income.find(" year = ? and month = ? and user_id = ? and recommend_user_id =? ", year, month, user.recommend_user_id, user.id).first();
							    //cpsIncome = cpsIncome == null ? new t_user_cps_income() : cpsIncome;
							    
							    // 该推荐用户全部cps奖金
							    //user_id,recommend_user_id 本月全部奖金
							    double cps_reward_all = doubleValue(t_users.find("select sum(cps_reward) from t_user_cps_profit where user_id = ? and recommend_user_id = ? and  ins_dt > ? and ins_dt < ?", user.id, user.recommend_user_id, DateUtil.strToDate(start), DateUtil.strToDate(end)).first());
							    // 待发放奖金
							    double cps_reward=0;
							    // 已发cps奖金
							    double cps_reward_ed = 0;
							    //cpsIncome = cpsIncome == null ? new t_user_cps_income() : cpsIncome;
							    if(cpsIncome != null){
							    	cps_reward_ed = doubleValue(t_user_details.find("select sum(amount) from t_user_details where user_id = ? and operation = ? and relation_id = ?", user.recommend_user_id, DealType.CPS_RATE_COUNT, cpsIncome.id).first());
							    	// 待发放奖金=总奖金-已发放的奖金
							    	cps_reward = cps_reward_all - cps_reward_ed;
							    }else{
							    	cps_reward=cps_reward_all;
							    	cpsIncome = new t_user_cps_income();
							    }
							    
							    // 待发放奖金=总奖金-已发放的奖金
							    cpsIncome.cps_reward = cps_reward;
							    cpsIncome.cps_reward_ed = cps_reward_ed;
							    
							    if(cpsIncome.cps_reward > 0.00){
							    	
							    	long spread_user_account = 0; // 推荐会员数
							    	long effective_user_account = 0; // 推荐会员数
							    	if(!userInfo.containsKey(user.recommend_user_id)){
							    		
							    		spread_user_account = t_users.find("select count(id) from t_users where recommend_user_id = ? ", user.recommend_user_id).first(); // 推荐会员数
							    		effective_user_account = t_users.find("SELECT COUNT(DISTINCT user_id) FROM t_user_cps_profit WHERE recommend_user_id = ?", user.recommend_user_id).first(); // 推荐会员数
							    		
							    		userInfo.put(user.recommend_user_id, new Object[]{spread_user_account, effective_user_account});
							    	}else{
							    		spread_user_account = (long) userInfo.get(user.recommend_user_id)[0]; // 推荐会员数
							    		effective_user_account = (long) userInfo.get(user.recommend_user_id)[1]; // 推荐会员数
							    	}
							    	
							    	
							    	cpsIncome.status = 0;
							    	cpsIncome.year = year;
							    	cpsIncome.month = month;
							    	cpsIncome.user_id = user.recommend_user_id;
							    	cpsIncome.recommend_user_id = user.id;
							    	cpsIncome.spread_user_account = spread_user_account; // 推荐会员数
							    	cpsIncome.effective_user_account = effective_user_account; // 有效推广会员数
							    	cpsIncome.invalid_user_account = spread_user_account - effective_user_account; // 无效会员数
							    	//cpsIncome.cps_reward = cps_reward;
							    	cpsIncome.save();
							    	Logger.info("===> 新增用户【"+user.recommend_user_id+"】的奖励金额：" + cps_reward);
							    }
							    
				    		}else{
				    			//Logger.info("历史CPS分成账单： "+investInfo);
				    		}
						}
						
					}
				}
			}
			
			em.createNativeQuery(deleteSql).executeUpdate();
			em.createNativeQuery(insertSql).executeUpdate();
			em.createNativeQuery(updateRate).executeUpdate();
			em.createNativeQuery(updateInvestAmount).executeUpdate();
			
			//原推广佣金的基础上 添加按收益分成的佣金
			//String updateCommissionAmount = "UPDATE t_cps_info t SET t.commission_amount = t.commission_amount+(SELECT ifnull(sum(`ucp`.`cps_reward`), 0) AS `cps_reward` FROM `t_user_cps_profit` `ucp` WHERE  `ucp`.`recommend_user_id` = t.id) ";
			//em.createNativeQuery(updateCommissionAmount).executeUpdate();
			//Logger.info("原推广佣金的基础上 添加按收益分成的佣金");
			
			User.payForCps();  //cps奖励发放
			
		} catch (Exception e) {
			Logger.error("update t_cps_info exception", e);
			e.printStackTrace();
			JPA.setRollbackOnly();
		}
		Logger.info("===> 统计CPS分成推广用户完成...");
		
	}
	
	public double doubleValue(Object value){
		return value == null ? 0.00 : Double.valueOf(value.toString());
	}
	
	public static void main(String[] args) {
		double x = new BigDecimal(206.1*(10.0/100.0)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
		System.out.println(x);
		x = 0.00;
		System.out.println(x == 0);
		
	   // int year = cal.get(Calendar.YEAR); 
	    //int month = cal.get(Calendar.MONTH) + 1;
	   // month = 12;
		
		 //calendar.setTime(date);
		// calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + day);
	    
	    //System.out.println(month < 10 ? "0"+month : month >= 12 ? "01" : month);
	    //System.out.println(cal.get(Calendar.YEAR) +","+(cal.get(Calendar.MONTH) + 1));
		
		String start = "2016-01-01 00:00:00", end = "2088-01-01 00:00:00";
		
		Integer year = 2016, month = null;
		
		if(year != null){
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month != null ? month-1 : 0);
			
			start = new SimpleDateFormat("yyyy-MM-01 00:00:00").format(cal.getTime());
			cal.set(Calendar.MONTH, month != null ? month : 12);
			end = year != null ? new SimpleDateFormat("yyyy-MM-01 00:00:00").format(cal.getTime()) : end;
		}
		
	    
	    System.out.println(start);
	    System.out.println(end);
	    
	    System.out.println(year+"-"+month);
	}
}
