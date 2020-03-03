package business;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;

import com.alibaba.fastjson.JSONObject;

import constants.MallConstants;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.JSONUtils;
import utils.Page;
import utils.PageBean;
import utils.SignTool;
import utils.TimeUtil;
import models.t_mall_scroe_record;
import models.t_red_packages_type;
import models.t_score_convert;
import models.t_system_options;
import models.t_user_cps_income;
import models.t_users;

public class Score implements Serializable{
	public static final String SCORE_CONFIG = "score_config";
	
	public static final String AUTH = "auth_score";
	public static final String BANK = "bank_score";
	public static final String INVEST = "invest_score";
	public static final String FIRST = "first_score";
	public static final String RECOMM_BANK = "recomm_bank_score";
	public static final String RECOMM_INVEST = "recomm_invest_score";
//	正式
	private static final String DUIBA_APP_KEY = "GRnef23zs3vDDLEqnVB5HcXhnpL";
	private static final String DUIBA_APP_SECRET = "2tyDfTCHCcx6FkaM5cAFZYQSemvR";
//	测试
//	private static final String DUIBA_APP_KEY = "3a7rbgp9GDm4YuA4gQbuoNYxDByb";
//	private static final String DUIBA_APP_SECRET = "3oVBM8ehC57fttKdqa9ZWsina5pD";
	
	public static Map<String,Object> getConfig(ErrorInfo error){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			error.code = -1;
			error.msg = "未找到配置";
			return null;
		}
		Map<String,Object> map = new HashMap<String, Object>();
		String val = so._value;
		if(StringUtils.isNotBlank(val)){
			JSONObject json = JSONObject.parseObject(val);
			Set<String> keys = json.keySet();
			for(String k : keys){
				map.put(k, json.get(k));
			}
		}
		return map;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 保存积分规则
	 * @param params
	 * @param error
	 */
	public static void saveScoreRule(Params params,ErrorInfo error){
		JSONObject json = new JSONObject();
		
		Map<String,String> map = params.allSimple();
		
		Set<String> keys = map.keySet();
		for(String k : keys){
			json.put(k, map.get(k));
		}
		json.remove("body");
		
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		so._value = json.toJSONString();
		so.save();
		
		
		error.code = 0;
		error.msg = "保存成功";
		
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 手动添加积分
	 * @param score
	 * @param mobile
	 * @param userId
	 * @param remark
	 * @param error
	 */
	public static void saveScore(Integer score,String mobile,Long userId,String remark,ErrorInfo error){
		t_users user = t_users.find("mobile", mobile).first();
		if(user == null){
			error.code = -1;
			error.msg = "查询不到用户，无法添加积分";
			return;
		}
		if(!user.id.equals(userId)){
			error.code = -1;
			error.msg = "查询不到用户，无法添加积分";
			return;
		}
		
		t_mall_scroe_record record = new t_mall_scroe_record();
		record.description = remark;
		record.scroe = score;
		record.user_id = userId;
		record.status = 1;
		record.time = new Date();
		record.type = 14;//手动添加
		record.user_name = user.name;
		
		record.save();
		error.code = 0;
		error.msg = "添加成功";
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 投标积分
	 * @param amount
	 * @return
	 */
	public static int investScore(double amount){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("invest_status");
			if(status == 1){
				int invest = json.getIntValue("invest_score");
				double a = Math.ceil(amount * invest);
				return new BigDecimal(a).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw 
	 * @creationDate 2017年9月5日
	 * @description 获取实名认证积分
	 * @return
	 */
	public static int authScore(){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("auth_status");
			if(status == 1){
				return json.getIntValue(AUTH);
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 获取绑卡积分
	 * @return
	 */
	public static int bankScore(){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("bank_status");
			if(status == 1){
				return json.getIntValue(BANK);
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 首投积分
	 * @return
	 */
	public static int firstScore(){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("first_status");
			if(status == 1){
				return json.getIntValue(FIRST);
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 成功推荐用户添加银行卡
	 * @return
	 */
	public static int recommBankScore(){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("recomm_bank_status");
			if(status == 1){
				return json.getIntValue(RECOMM_BANK);
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 成功推荐用户首次投资
	 * @return
	 */
	public static int recommInvestScore(){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		try {
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			int status = json.getIntValue("recomm_invest_status");
			if(status == 1){
				return json.getIntValue(RECOMM_INVEST);
			}
		} catch (Exception e) {
		}
		return 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 发放积分：实名认证、绑定银行卡
	 * @param userId
	 * @param type
	 */
	public static void sendScore(long userId,String type,String param,ErrorInfo error){
		t_users user = t_users.findById(userId);
		if(user == null){
			return;
		}
		int t = 0;
		String remark = "";
		int score = 0;
		if(AUTH.equals(type)){
			t = 9;
			score = authScore();
			remark = "新手任务：完成实名认证";
			if(isAuth(userId,param)){
				score = 0;
			}
		}
		if(BANK.equals(type)){
			t = 10;
			score = bankScore();
			remark = "新手任务：完成添加银行卡";
			sendRecomm(user.recommend_user_id,user.reality_name,RECOMM_BANK,error);
		}
		if(score > 0){
			t_mall_scroe_record record = new t_mall_scroe_record();
			record.description = remark;
			record.scroe = score;
			record.user_id = userId;
			record.status = 1;
			record.time = new Date();
			record.type = t;
			record.user_name = user.name;
			
			record.save();
		}
		
		error.code = 0;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 发放首次投标积分
	 * @param userId
	 * @param invest_id
	 * @param error
	 */
	public static void sendFirstScore(long userId,long invest_id,ErrorInfo error){
		t_users user = t_users.findById(userId);
		if(user == null){
			return;
		}
		int t = 11;
		String remark = "新手任务：完成首次投资";
		int score = firstScore();
		
		sendRecomm(user.recommend_user_id,user.reality_name,RECOMM_INVEST,error);
		
		if(score > 0){
			t_mall_scroe_record record = new t_mall_scroe_record();
			record.description = remark;
			record.scroe = score;
			record.user_id = userId;
			record.status = 1;
			record.time = new Date();
			record.type = t;
			record.user_name = user.name;
			record.relation_id = invest_id;
			
			record.save();
		}
		
		error.code = 0;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 发放成功推荐用户添加银行卡、成功推荐用户首次投资
	 * @param userId
	 * @param name
	 * @param type
	 * @param error
	 */
	public static void sendRecomm(long userId,String name,String type,ErrorInfo error){
		t_users user = t_users.findById(userId);
		if(user == null){
			return;
		}
		int t = 0;
		String remark = "";
		int score = 0;
		if(RECOMM_BANK.equals(type)){
			t = 12;
			score = recommBankScore();
			remark = "成功推荐用户" + name + "添加银行卡";
		}
		if(RECOMM_INVEST.equals(type)){
			t = 13;
			score = recommInvestScore();
			remark = "成功推荐用户" + name + "首次投资";
		}
		
		if(score > 0){
			t_mall_scroe_record record = new t_mall_scroe_record();
			record.description = remark;
			record.scroe = score;
			record.user_id = userId;
			record.status = 1;
			record.time = new Date();
			record.type = t;
			record.user_name = user.name;
			
			record.save();
		}
		
		error.code = 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月5日
	 * @description 发放投标积分
	 * @param userId
	 * @param amount
	 * @param invest_id
	 * @param error
	 */
	public static void sendScore(long userId,String bidName,Map<String, String> parameters,long invest_id,ErrorInfo error){
		t_users user = t_users.findById(userId);
		if(user == null){
			return;
		}
		int score = appRepaymentgCalculate(parameters);
		
		if(score > 0){
			t_mall_scroe_record record = new t_mall_scroe_record();
			record.description = "投资" + bidName + "￥" + parameters.get("amount");
			record.scroe = score;
			record.user_id = userId;
			record.status = 1;
			record.time = new Date();
			record.type = 3;
			record.user_name = user.name;
			record.relation_id = invest_id;
			
			record.save();
		}
		
		error.code = 0;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月8日
	 * @description 我的积分数据
	 * @param userId
	 * @param error
	 * @return
	 */
	public static Map<String,Object> getUserScoreData(long userId,ErrorInfo error){
		t_users user = t_users.findById(userId);
		if(user == null){
			error.code = -1;
			error.msg = "无效的用户";
			return null;
		}
		Map<String,Object> map = new HashMap<String, Object>();
		//获取用户累计签到数
		String sql = "select count(1) as count from t_mall_scroe_record where user_id = ? and type = ?";
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId,2);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		map.put("signCount", count);
		//用户总积分
		map.put("score", getUserScore(userId));
		boolean isSign = isSign(userId,error);
		//今天是否已签到
		map.put("isSign", isSign);
		//实名认证送积分
		map.put("authScore", authScore());
		//是否已实名
		map.put("isAuth", StringUtils.isBlank(user.reality_name) ? false : true);
		//添加银行卡送积分
		map.put("bankScore", bankScore());
		//是否已经绑卡
		map.put("isBank", isBank(userId,null));

		//首次投资送积分
		map.put("firstInvestScore", firstScore());
		//是否已完成首投
		map.put("isFirst", !isFirstInvest(userId, 0));
		//昨日积分
//		map.put("yesterday", getYesterday(userId));
//		//今日积分
//		map.put("today", getSignScore(userId,0));
//		//明日积分
//		map.put("tomorrow", getSignScore(userId,1));
		
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so != null){
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			map.put("signScore", new int[]{json.getIntValue("day1"),json.getIntValue("day2"),json.getIntValue("day3"),json.getIntValue("day4"),json.getIntValue("day5"),
					json.getIntValue("day6"),json.getIntValue("day7"),json.getIntValue("day_more")});
		}
		map.put("days",signDays(userId));
		
		return map;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年10月12日
	 * @description 连续签到天数
	 * @param userId
	 * @return
	 */
	public static int signDays1(long userId){
		int day = getDays(userId,7);
		int count = 0;
		if(day > 0){
			String sql = "select levelDay as count from t_mall_scroe_record where user_id = ? and type = ? order by time desc limit 1";
			List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, userId,2);
	    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
	    		count = ((Integer)countMap.get(0).get("count")).intValue();
	    	}
	    	if(getDays(count, userId)){
	    		boolean flag = true;
	    		while(flag){
	    			count += 1;
	    			if(!getDays(count, userId)){
	    				flag = false;
	    				count -= 1;
	    			}
	    		}
	    	}else{
	    		boolean flag = true;
	    		while(flag){
	    			day += 1;
	    			if(!getDays(day, userId)){
	    				flag = false;
	    				day -= 1;
	    			}
	    			count = day;
	    		}
	    	}
		}
		if(isSign(userId, new ErrorInfo())){
			count += 1;
		}
		return count;
	}
	
	public static int signDays(long userId){
		boolean flag = false;
		if(isSign(userId, new ErrorInfo())){
			flag = true;
		}else{
			if(getDays(1, userId)){
				flag = true;
			}
		}
		int count = 0;
		if(flag){
			String sql = "from t_mall_scroe_record where user_id = ? and type = ? order by time desc";
			List<t_mall_scroe_record> lists = t_mall_scroe_record.find(sql,userId,2).fetch();
			
			if(lists != null && lists.size() > 0){
				String dt = null;
				String zt = null;
				count = 1;
				for(t_mall_scroe_record record : lists){
					dt = DateUtil.dateToString(record.time, "yyyy-MM-dd");
					if(zt != null){
						if(zt.equals(dt)){
							count += 1;
						}else{
							break;
						}
					}
					zt = DateUtil.getDate(record.time, "yyyy-MM-dd", -1);
				}
			}
		}
		return count;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月6日
	 * @description 用户总积分
	 * @param userId
	 * @return
	 */
	public static long getUserScore(long userId){
		String sql = "select sum(scroe) as count from t_mall_scroe_record where user_id = ? and status = 1";
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigDecimal)countMap.get(0).get("count")).intValue();
		}
		return count;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月6日
	 * @description 判断今天是否已签到
	 * @param userId
	 * @return
	 */
	public static boolean isSign(long userId,ErrorInfo error){
		String sql = "select count(1) as count from t_mall_scroe_record where user_id = ? and type = ? and time > ?";
		String dateStr = TimeUtil.dateToStrDate(new Date());
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId,2,dateStr);
		int count = 0;
		try {
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		} catch (Exception e) {
			error.code = -3;
		}
		return count == 0 ? false : true;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月6日
	 * @description 是否首投
	 * @param userId
	 * @param investId
	 * @return
	 */
	public static boolean isFirstInvest(long userId,int investId){
//		String sql = "select count(1) as count from t_invests i join t_bids b on i.bid_id = b.id where i.user_id = ? and b.status in(?,?,?) and i.id <>? ";
//		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,user1.id,Constants.BID_REPAYMENT,Constants.BID_COMPENSATE_REPAYMENT,Constants.BID_REPAYMENTS,investId);
		String sql = "select count(1) as count from t_invests i join t_bids b on i.bid_id = b.id where i.user_id = ? and i.id <>? ";
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId,investId);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		return count == 0 ? true : false;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月6日
	 * @description 是否完成绑卡任务
	 * @param userId
	 * @return
	 */
	public static boolean isBank(long userId,String account){
		t_users user = t_users.findById(userId);
		boolean isBank = user.is_bank;
//		if(!isBank){
//			user.is_bank = true;
//			user.save();
//		}
//		
//		String sql = "select count(1) as count from t_user_bank_accounts where user_id = ? ";
//		if(StringUtils.isNotBlank(account)){
//			sql += " and account <> '" + account + "'";
//		}
//		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId);
//		int count = 0;
//		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
//			count = ((BigInteger)countMap.get(0).get("count")).intValue();
//		}
//		boolean flag = count == 0 ? false : true;
//		if(!flag){
//			sql = "select count(1) as count from t_mall_scroe_record where user_id = ? and type = ?";
//			countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId,10);
//			count = 0;
//			if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
//				count = ((BigInteger)countMap.get(0).get("count")).intValue();
//			}
//			flag = count == 0 ? false : true;
//		}
		return isBank;
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Mar 27, 2018 10:46:47 AM 
	 * @description.  是否绑卡
	 * 
	 * @param userId
	 * @return
	 */
	public static boolean hasBank(long userId){
		
		String sql = "select count(1) as count from t_user_bank_accounts where user_id = ? ";
		
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		return count == 0 ? false : true;
	}
	/**
	 * @author wangyun
	 * @date 2019年3月28日
	 * @param userId
	 * @return
	 */
	public static boolean hasProtocolBank(long userId){
		
		String sql = "select count(1) as count from t_user_bank_accounts where user_id = ? and protocol_no is not null ";
		
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId);
		int count = 0;
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		return count == 0 ? false : true;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月7日
	 * @description 是否已完成实名任务
	 * @param userId
	 * @return
	 */
	public static boolean isAuth(long userId,String name){
		boolean flag = false;
		if(StringUtils.isNotBlank(name)){
			flag = false;
		}else{
			t_users user = t_users.findById(userId);
			flag = StringUtils.isBlank(user.reality_name) ? false : true;
		}
		if(!flag){
			String sql = "select count(1) as count from t_mall_scroe_record where user_id = ? and type = ?";
			List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql ,userId,9);
			int count = 0;
			if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
				count = ((BigInteger)countMap.get(0).get("count")).intValue();
			}
			flag = count == 0 ? false : true;
		}
		return flag;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月7日
	 * @description 用户获取积分记录
	 * @param userId
	 * @return
	 */
	public static PageBean<Map<String,Object>> getUserScoreRecord(long userId,int currPage,int pageSize){
		int count = 0;
		String cntSql = "select count(id) as count from t_mall_scroe_record where user_id = ? and scroe > 0";
    	List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, userId);
    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
    		count = ((BigInteger)countMap.get(0).get("count")).intValue();
    	}
		
		String sql = "select id,date_format(time,'%Y-%m-%d %H:%i:%s') as time,description,scroe as score from t_mall_scroe_record "
				+ "where user_id = ? and scroe > 0 order by time desc limit ?,?";
		List<Map<String,Object>> data = JPAUtil.getList(new ErrorInfo(), sql ,userId,(currPage - 1) * pageSize,pageSize);
		
		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		
		page.page = data;
 		
        return page;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月8日
	 * @description 获取昨天签到积分数
	 * @param userId
	 * @return
	 */
	public static int getYesterday(long userId){
		String sql = "select scroe as score from t_mall_scroe_record where user_id = ? and type = ? and time >= ? and time <= ?";
		Date date = DateUtil.dateAddDay(new Date(), -1);
		String day = DateUtil.dateFormat(date, "yyyy-MM-dd");
		
    	List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, userId,2 ,
    			DateUtil.strDateToStartDate(day),DateUtil.strDateToEndDate(day));
    	int score = 0;
    	try {
			score = ((Integer)countMap.get(0).get("score")).intValue();
		} catch (Exception e) {
		}
		
		return score;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月8日
	 * @description 获取签到积分数
	 * @param userId
	 * @param type 1-明天 0-今天
	 * @return
	 */
	public static int getSignScore(long userId,int type){
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so == null){
			return 0;
		}
		String val = so._value;
		JSONObject json = JSONObject.parseObject(val);
		
		int day = getDays(userId,7) + 1;
		if(type == 1){
			day += 1;
		}
		if(day > 7){
			return json.getIntValue("day_more");
		}else{
			return json.getIntValue("day" + day); 
		}
	}
	
	public static int getDays(long userId,int day){
		//是否已连续签到n天
		for(int i = day;i > 0;i--){
			if(getDays(i, userId)){
				return i;
			}
		}
		return 0;
	}
	
//	public static void test(){
//		Date date = new Date();
//		int j = 0;
//		for(int i = 500;i>0;i--){
//			j++;
//			t_mall_scroe_record record = new t_mall_scroe_record();
//			record.user_id = 999;
//			record.user_name = "sdq03";
//			record.time = DateUtil.strToDate(DateUtil.getDate(date, "yyyy-MM-dd  HH:mm:ss", -i));
//			record.type = MallConstants.SIGN;
//			record.scroe = 1;
//			record.levelDay = j;
//			record.status = MallConstants.STATUS_SUCCESS;
//			record.description = "签到连续" + record.levelDay + "天";
//			record.save();
//		}
//		
//	}
	public static boolean getDays(int day,long userId){
		Date date = new Date();
		Date start = DateUtil.strDateToStartDate(DateUtil.dateFormat(DateUtil.dateAddDay(date, -day), "yyyy-MM-dd"));
		Date end = DateUtil.strDateToEndDate(DateUtil.dateFormat(DateUtil.dateAddDay(date, -1), "yyyy-MM-dd"));

		String sql = "select count(1) as count from t_mall_scroe_record where user_id = ? and type = ? and time >= ? and time <= ?";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, userId,2 ,start,end);
    	int count = 0;
    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
    		count = ((BigInteger)countMap.get(0).get("count")).intValue();
    	}
		
		return count >= day;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月8日
	 * @description 签到
	 * @param user
	 * @param error
	 */
	public static void saveScroeSignRecord(User user, ErrorInfo error) {
		error.clear();
		if (user == null || user.id <= 0)
			return;
		int score = 0;
		int day = signDays(user.id) + 1;
		
		t_system_options so = t_system_options.find("_key", SCORE_CONFIG).first();
		if(so != null){
			String val = so._value;
			JSONObject json = JSONObject.parseObject(val);
			if(day > 7){
				score = json.getIntValue("day_more");
			}else{
				score = json.getIntValue("day" + day); 
			}
		}
		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.SIGN;
		record.scroe = score;
		record.levelDay = day;
		record.status = MallConstants.STATUS_SUCCESS;
		record.description = "签到连续" + record.levelDay + "天";
		
		try {
			if(score > 0){
				record.save();
			}
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("保存积分记录时：" + e.getMessage());
		}
		error.code = 0;
		error.msg = "签到成功";
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月11日
	 * @description 获取兑吧URL
	 * @param userId
	 * @param error
	 * @return
	 */
	public static String getDuiBaUrl(User user,String dbredirect,ErrorInfo error){
		error.clear();
		String uid = "not_login";
		String credits = "0";
		if (user != null && user.id > 0){
			uid = user.id + "";
			credits = getUserScore(user.id) + "";
		}
		String url="https://www.duiba.com.cn/autoLogin/autologin?";
		
		Map<String, String> params=new HashMap<String, String>();
		params.put("appKey", DUIBA_APP_KEY);
		params.put("appSecret", DUIBA_APP_SECRET);
		params.put("credits", credits);
		params.put("timestamp", System.currentTimeMillis()+"");
		params.put("uid", uid);
		if(StringUtils.isNotBlank(dbredirect)){
			params.put("redirect", dbredirect);
		}
		params.put("sign", SignTool.sign(params));
		
		params.remove("appSecret");
		
		if(!url.endsWith("?")){
			url+="?";
		}
		for(String key:params.keySet()){
			try {
				if(params.get(key)==null || params.get(key).length()==0){
					url+=key+"="+params.get(key)+"&";
				}else{
					url+=key+"="+URLEncoder.encode(params.get(key), "utf-8")+"&";
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return url;
	}
	
	public static t_mall_scroe_record findByRelation(long id,long uid){
		String sql = "from t_mall_scroe_record where user_id = ? and relation_id = ? and type = ? ";
		t_mall_scroe_record record = t_mall_scroe_record.find(sql, uid,id,15).first();
		return record;
	}
	/**
	 * 
	 * @author xinsw 积分扣除
	 * @creationDate 2017年9月12日
	 * @description
	 */
	public static Map<String,Object> deduction(t_score_convert convert,Map<String,String> params){
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("credits", 0);
		if(!SignTool.signVerify(DUIBA_APP_SECRET, params)){
			map.put("status", "fail");
			map.put("errorMessage", "签名错误");
			return map;
		}
		
		t_users user = t_users.findById(convert.user_id);
		if(user == null){
			map.put("status", "fail");
			map.put("errorMessage", "用户信息错误");
			return map;
		}
		long score = getUserScore(user.id);
		if(convert.credits > score){
			map.put("status", "fail");
			map.put("errorMessage", "积分不足！");
			map.put("credits", score);
			return map;
		}
		t_score_convert sc = t_score_convert.find("order_num", convert.order_num).first();
		if(sc == null){
			convert.save();
		}else{
			convert = sc;
		}
		t_mall_scroe_record record = findByRelation(convert.id, convert.user_id);
		if(record == null){
			record = new t_mall_scroe_record();
			record.relation_id = convert.id;
			record.scroe = -convert.credits;
			record.user_id = user.id;
			record.time = new Date();
			record.description = convert.description;
			
			record.type = 15;//兑换
			record.user_name = user.name;
		}
		record.status = 1;
		record.save();
		
		map.put("credits", getUserScore(user.id) + "");
		map.put("bizId", convert.id + "_" + convert.order_num);
		map.put("status", "ok");
		map.put("errorMessage", "");
		return map;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月13日
	 * @description 处理兑吧通知
	 * @param params
	 * @return
	 */
	public static String convert(Params params){
		String FAIL = "fail";
		String OK = "ok";
		
		String appKey = params.get("appKey");
		String time = params.get("timestamp");
		String success = params.get("success");
		String order_num = params.get("orderNum");
		String errorMessage = params.get("errorMessage");
		String bizId = params.get("bizId");
		String sign = params.get("sign");
		
		Map<String,String> data = new HashMap<String, String>();
		data.put("appKey", appKey);
		data.put("timestamp", time);
		data.put("success", success);
		data.put("orderNum", order_num);
		data.put("errorMessage", errorMessage);
		data.put("bizId", bizId);
		data.put("sign", sign);
		
		if(!SignTool.signVerify(DUIBA_APP_SECRET, params.allSimple())){
			return FAIL;
		}
		boolean flag = Boolean.parseBoolean(success);
		
		t_score_convert convert = t_score_convert.find("order_num", order_num).first();
		if(convert == null){
			return FAIL;
		}
		if(convert.status){
			return OK;
		}
		if(!flag){//兑换失败，积分扣除标记为失败
			t_mall_scroe_record record = findByRelation(convert.id, convert.user_id);
			if(record != null){
				record.status = 2;
				record.save();
				return OK;
			}
		}
//		if(StringUtils.isNotBlank(convert.item_code)){
//			t_red_packages_type red = t_red_packages_type.find("item_code", convert.item_code).first();
//			//发送红包
//			if(red != null){
//				User user = new User();
//				user.id = convert.user_id;
//				RedPackageHistory.sendRedPackage(user, red,"积分兑换红包");
//			}
//		}
		convert.status = true;
		convert.save();
		
		return OK;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年9月18日
	 * @description 积分换红包
	 * @param params
	 * @return
	 */
	public static Map<String,Object> recharge(Params params){
		Map<String,Object> map = new HashMap<String, Object>();
		
		try {
			String appKey = params.get("appKey");
			String order_num = params.get("orderNum");
			String developBizId = params.get("developBizId");
			String uid = params.get("uid");
			String param = params.get("params");
			String time = params.get("timestamp");
			String sign = params.get("sign");
			String description = params.get("description");
			String account = params.get("account");
			
			Map<String,String> data = new HashMap<String, String>();
			data.put("appKey", appKey);
			data.put("orderNum", order_num);
			data.put("developBizId", developBizId);
			data.put("uid", uid);
			data.put("param", param);
			data.put("timestamp", time);
			data.put("description", description);
			data.put("account", account);
			data.put("sign", sign);
			
			if(!SignTool.signVerify(DUIBA_APP_SECRET, params.allSimple())){
				map.put("status", "fail");
				map.put("errorMessage", "签名错误");
				map.put("supplierBizId", developBizId);
				return map;
			}
			
			t_score_convert convert = t_score_convert.find("order_num", order_num).first();
			if(convert == null){
				convert = new t_score_convert();
				convert.description = description;
				convert.order_num = order_num;
				convert.params = param;
				convert.type = "htool";
				convert.user_id = Integer.parseInt(uid);
				convert.credits = 0;
				convert.actual_price = 0;
				convert.face_price = 0;
				convert.wait_audit = false;
				convert.time = new Date();
				convert.save();
			}
			if(convert.status){
				map.put("status", "success");
				map.put("supplierBizId", developBizId);
				map.put("credits", getUserScore(convert.user_id));
				return map;
			}
			if(StringUtils.isNotBlank(param)){
				t_red_packages_type red = t_red_packages_type.find("item_code", param).first();
				//发送红包
				if(red != null){
					User user = new User();
					user.id = Long.parseLong(uid);
					RedPackageHistory.sendRedPackage(user, red,"积分兑换红包");
				}
			}
			convert.status = true;
			convert.mark = param;
			convert.save();
			
			map.put("status", "success");
			map.put("supplierBizId", developBizId);
			map.put("credits", getUserScore(convert.user_id));
		} catch (Exception e) {
			e.printStackTrace();
			map.put("status", "fail");
			map.put("errorMessage", "内部错误");
		}
		return map;
	}
	
	public static int appRepaymentgCalculate(Map<String, String> parameters){
		List<Map<String, Object>> payList = null;
		String amount = parameters.get("amount");
		String apr = parameters.get("apr");
		String period = parameters.get("period");
		String periodUnit = parameters.get("periodUnit");
		String repaymentType = parameters.get("paymentType");
		String increaseRate = parameters.get("increaseRate");//加息利息
		if(StringUtils.isBlank(increaseRate)){
			increaseRate = "0";
		}
		
		payList = Bill.repaymentCalculate(Double.parseDouble(amount), Double.parseDouble(apr), Integer.parseInt(period), 
				Integer.parseInt(periodUnit), Integer.parseInt(repaymentType),Double.parseDouble(increaseRate));
		
		double profit = 0; 
		double income = 0; 
		
		for(Map<String, Object> vo : payList){
			
			profit = new BigDecimal(profit).add(new BigDecimal(Double.parseDouble(vo.get("monPayInterest").toString()))).doubleValue();
			income = new BigDecimal(income).add(new BigDecimal(Double.parseDouble(vo.get("monPayIncreaseInterest").toString()))).doubleValue();
		}
		return Score.investScore(profit + income);
	}
}
