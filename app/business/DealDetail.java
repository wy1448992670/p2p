package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_merchant_deal_details;
import models.t_platform_detail_types;
import models.t_platform_details;
import models.t_supervisor_events;
import models.t_user_bank_accounts;
import models.t_user_details;
import models.t_user_details_credit_score;
import models.t_user_details_score;
import models.t_user_events;
import models.t_user_recharge_details;
import models.t_users;
import models.v_platform_detail;
import models.v_supervisor_events;
import models.v_user_for_details;
import models.v_user_withdrawals;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;

import com.shove.Convert;

import constants.Constants;
import constants.DealType;
import constants.IPSConstants;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.SupervisorEvent;

/**
 * 交易记录实体类
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 下午04:07:43
 */
public class DealDetail implements Serializable{
	public long id;
	public long _id;
	public long userId;
	public Date time;
	public int operation;
	public double amount;
	/**
	 * 1 收入2 支出3 冻结4 解冻
	 */
	public int type;
	public long relationId;
	public String summary;
	public double balance;
	public double freeze;
	public double recieveAmount;
	
	public DealDetail() {
		
	}
	
	public DealDetail(long userId, int operation, double amount, long relationId,
			 double balance, double freeze, double recieveAmount, String summary) {
		this.userId = userId;
		this.operation = operation;
		this.amount= amount;
		this.relationId = relationId;
		this.balance = balance;
		this.freeze = freeze;
		this.recieveAmount = recieveAmount;
		this.summary = summary;
	}
	
	/**
	 * 被动改变时，查询需填充的金额
	 * @param userId
	 * @param error
	 * @return
	 */
	public static v_user_for_details queryUserBalance(long userId, ErrorInfo error) {
		error.clear();
		
		v_user_for_details forDetail = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_FOR_DETAILS);
		sql.append(" and t_users.id = ?");
		
		Map<String, Object> map = JPAUtil.getMap(error, sql.toString(), userId);
		
		if(map == null) {
			error.code = -1;
			error.msg = "用户id不存在!";
			
			return null;
		}
		
		JSONObject json = JSONObject.fromObject(map);
		
		forDetail = new v_user_for_details();
		forDetail.id = json.getLong("id");
		forDetail.user_amount = json.getDouble("user_amount");
		forDetail.user_amount2 = json.getDouble("user_amount2");
		forDetail.freeze = json.getDouble("freeze");
		forDetail.credit_line = json.getDouble("credit_line");
		forDetail.receive_amount = json.getDouble("receive_amount");
		
		return forDetail;
	}
	
	/**
	 * 添加交易记录
	 * @param error
	 */
	public void addDealDetail(ErrorInfo error) {
		error.clear();
		t_user_details detail = new t_user_details();

		detail.user_id = this.userId;
		detail.time = new Date();
		detail.operation = this.operation;
		detail.amount = this.amount;
		detail.relation_id = this.relationId;
		detail.summary = this.summary;
		detail.balance = this.balance;
		detail.freeze = this.freeze;
		detail.recieve_amount = this.recieveAmount;
		
		if(Constants.IPS_ENABLE) {
			v_user_for_details forDetail = queryUserBalance(userId, error);
			
			if(error.code < 0) {
				return ;
			}
			
			detail.balance += forDetail.user_amount2;
		}
		
		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加交易记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加交易记录时出现异常!";
			
			return ;
		}
		DataSafety dataSafety = new DataSafety();
		dataSafety.updateSignWithLock(this.userId, error);
		error.code = 0;
	}
	
	/**
	 * 添加交易记录（用于balance2改变，普通网关金额变化，如vip费，cps推广费）
	 * @param error
	 */
	public void addDealDetail2(ErrorInfo error) {
		error.clear();
		t_user_details detail = new t_user_details();

		detail.user_id = this.userId;
		detail.time = new Date();
		detail.operation = this.operation;
		detail.amount = this.amount;
		detail.relation_id = this.relationId;
		detail.summary = this.summary;
		detail.balance = this.balance;
		detail.freeze = this.freeze;
		detail.recieve_amount = this.recieveAmount;
		
		v_user_for_details forDetail = queryUserBalance(userId, error);
		
		if(error.code < 0) {
			return ;
		}
		
		detail.balance += forDetail.user_amount;
		
		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加交易记录2时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加交易记录2时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 平台交易记录
	 * @param operation  操作类型
	 * @param relationId 关联id
	 * @param from 支付方
	 * @param to   接收方
	 * @param payment 支付方式
	 * @param amount  交易金额
	 * @param type  交易类型 1 收入 2 支出
	 * @param summary 摘要
	 */
	public static void addPlatformDetail(int operation, long relationId, long from, long to, 
			String payment, double amount, int type, String summary, ErrorInfo error) {
		error.clear();
		error.code = -1;
		
		double balance = 0;
		String sql = "select IFNULL(SUM(case when type=1 then amount end),0) - "
				+ "IFNULL(SUM(case when type=2 then amount end),0) as balance "
				+ "from t_platform_details";
		
		try{
			balance = ((BigDecimal)JPA.em().createNativeQuery(sql).getSingleResult()).doubleValue();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障余额时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障余额失败";
			
			return;
		}
		
		String from_pay_name = null;
		String to_receive_name = null;
		
		try {
			from_pay_name = t_users.find("SELECT t.name FROM t_users t WHERE t.id = ?", from).first();
			to_receive_name = t_users.find("SELECT t.name FROM t_users t WHERE t.id = ?", to).first();
		} catch (Exception e) {
			Logger.error("查询用户用户名时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询用户用户名失败";
			
			return;
		}
		
		if (null == from_pay_name ) {
			
			from_pay_name = "平台";
		}
		
		if (null == to_receive_name) {
			
			to_receive_name = "平台";
		}
		
		t_platform_details detail = new t_platform_details();

		detail.time = new Date();
		detail.operation = operation;
		detail.relation_id = relationId;
		detail.from_pay = from;
		detail.to_receive = to;
		detail.from_pay_name = from_pay_name;
		detail.to_receive_name = to_receive_name;
		detail.payment = payment;
		detail.amount = amount;
		detail.type = type;
		detail.balance = balance;
		detail.summary = summary;
		
		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加交易记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加交易记录时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 添加商户交易记录
	 */
	public static void addMerChantDetail(double amount, double fee, int type, double balance, long merBillNO, ErrorInfo error) {
		error.clear();

		t_merchant_deal_details detail = new t_merchant_deal_details();
		
		detail.time = new Date();
		detail.amount = amount;
		detail.fee = fee;
		detail.type = type;
		detail.balance = balance;
		detail.status = 0;  //已提交
		detail.mer_bill_no = merBillNO;

		try {
			detail.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加商户交易记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "添加商户交易记录时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新商户交易状态，防重复回调
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 */
	public static void updateMerDealStatus(long merBillNO, int status, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set status = ? where mer_bill_no = ? and status != ?";
		Query query = JpaHelper.execute(sql, status, merBillNO, status);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("更新商户交易状态时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易状态时出现异常!";
			
			return ;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = Constants.ALREADY_RUN;
			error.msg = "已执行";
			
			return ;
		}
		
		error.code = 1;
	}
	
	/**
	 * 更新商户交易手续费
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 * @param error
	 */
	public static void updateMerDealFee(long merBillNO, double fee, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set  fee = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql, fee, merBillNO);
		
		try {
			 query.executeUpdate();
		} catch (Exception e) {

			Logger.info("更新商户交易手续费时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易手续费时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 更新商户交易详情
	 * @param merBillNO
	 * @param fee
	 * @param balance
	 * @param error
	 */
	public static void updateMerDatail(long merBillNO, double fee, double amount, ErrorInfo error){
		error.clear();
		
		String sql = "update t_merchant_deal_details set balance = balance - ? - ? , fee = ? , amount = ? where mer_bill_no = ?";
		Query query = JpaHelper.execute(sql, fee, amount, fee, amount, merBillNO);
		
		try {
			 query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info("更新商户交易详情时："+e.getMessage());
			
			error.code = -1;
			error.msg = "更新商户交易详情时出现异常!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 添加信用积分记录
	 * @param userId
	 * @param type 信用积分类别 (1 审核资料积分 2 正常还款积分3 成功借款积分4 成功投标积分-1账单逾期扣分)
	 * score 此字段只在type为1时有效
	 * @param relationId 当类别为：1 t_user_audit_items中的id,2  账单的id, 3 借款标的id, 4 t_invest理财id 
	 * -1 逾期账单id
	 * @param summary 摘要说明
	 * @param error
	 */
	public static int addCreditScore(long userId, int type, int score, long relationId, String summary, ErrorInfo error) {
		error.clear();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		if((type <1 || type > 4) && type != -1) {
			error.code = -1;
			error.msg = "传入类别有误";
			return error.code;
		}
		
		int creditTypeScore = 0;
		
		switch (type) {
		case Constants.AUDIT_ITEM:
			creditTypeScore = score;
			break;
			
		case Constants.PAYMENT:
			creditTypeScore = backstageSet.normalPayPoints;
			break;
			
		case Constants.BID:
			creditTypeScore = backstageSet.fullBidPoints;
			break;
			
		case Constants.INVEST:
			creditTypeScore = backstageSet.investpoints;
			break;
			
		case Constants.OVERDUE:
			creditTypeScore = -backstageSet.overDuePoints;
			break;
		}
		
		t_user_details_credit_score creditScore = new t_user_details_credit_score();
		
		creditScore.user_id = userId;
		creditScore.time = new Date();
		creditScore.operation = type;
		creditScore.score = creditTypeScore;
		creditScore.relation_id = relationId;
		creditScore.summary = summary;
		String sql = "update t_users set credit_score = credit_score + ?, credit_line = credit_line + ? where id = ?";
		
		int rows = 0;
		
		try {
			creditScore.save();
			rows = JPA.em().createNativeQuery(sql).setParameter(1, creditTypeScore).setParameter(2, creditTypeScore*backstageSet.creditToMoney).setParameter(3, userId).executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加用户信用积分记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加用户信用积分失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return creditTypeScore;
	}
	
	/**
	 * 添加系统积分（每投一元积分）
	 * @param userId
	 * @param type 目前只有投标才增加积分，统一为1
	 * @param amount 投标的金额
	 * @param relationId t_invest理财id
	 * @param summary 摘要
	 * @param error
	 */
	public static int addScore(long userId, int type, double amount, long relationId, String summary, ErrorInfo error) {
		error.clear();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		int money = (int)amount;
		
		if(money <= 0) {
			error.code = -1;
			error.msg = "传入金额参数有误";
			
			return error.code;
		}
		
		int score = (int)(backstageSet.moneyToSystemScore*money);
		
		t_user_details_score detailScore = new t_user_details_score();
		
		detailScore.user_id = userId;
		detailScore.time = new Date();
		detailScore.operation = type;
		detailScore.score = score;
		detailScore.relation_id = relationId;
		detailScore.summary = summary;
		
		String sql = "update t_users set score = score + ? where id = ?";
		
		int rows = 0;
		
		try {
			detailScore.save();
			rows = JpaHelper.execute(sql,score,userId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加用户系统积分记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加用户系统积分记录失败!";
			
			return error.code;
		}
		
		if(rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		
		return score;
	}
	
	/**
	 * 添加用户事件记录
	 * @param userId
	 * @param type  事件类型
	 * @param ip    ip
	 * @param descrption  描述
	 * @param error
	 */
	public static void userEvent(long userId, int type, String descrption, ErrorInfo error) {
		error.clear();
		
		t_user_events userEvent = new t_user_events();
		
		userEvent.user_id = userId;
		userEvent.time = new Date();
		userEvent.type_id = type;
		userEvent.ip = DataUtil.getIp();
		userEvent.type_id = type;
		userEvent.descrption = descrption;
		
		try {
			userEvent.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加用户事件记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加用户事件记录失败!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 添加管理员事件记录
	 * @param userId
	 * @param type  事件类型
	 * @param ip    ip
	 * @param descrption  描述
	 * @param error
	 */
	public static void supervisorEvent(long supervisorId, int type, String descrption, ErrorInfo error) {
		error.clear();
		
		t_supervisor_events supervisorEvent = new t_supervisor_events();
		
		supervisorEvent.supervisor_id = supervisorId;
		supervisorEvent.time = new Date();
		supervisorEvent.type_id = type;
		supervisorEvent.ip = DataUtil.getIp();
		supervisorEvent.type_id = type;
		supervisorEvent.descrption = descrption;
		
		try {
			supervisorEvent.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("增加管理员事件记录时:" + e.getMessage());
			
			error.code = -1;
			error.msg = "增加管理员事件记录失败!";
			
			return ;
		}
		
		error.code = 0;
	}
	
	/**
	 * 查询后台事件(操作日志)
	 * @param currPage
	 * @param pageSize
	 * @param keywordType
	 * @param keyword
	 * @param startTime
	 * @param endTime
	 * @param error
	 * @return
	 */
	public static PageBean<v_supervisor_events> querySupervisorEvents(int currPage, int pageSize,
			int keywordType, String keyword, String beginTime, String endTime, ErrorInfo error) {
		error.clear();
				
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		if (keywordType < 0 || keywordType > 3) {
			keywordType = 0;
		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_SUPERVISOR_EVENTS);
		
		List<Object> params = new ArrayList<Object>();
		
		//在对关键字搜索中，QUERY_EVENT_KEYWORD语句模板有错误。
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(SQLTempletes.QUERY_EVENT_KEYWORD[keywordType]);
			
			if (0 == keywordType) {
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
				params.add("%" + keyword + "%");
			} else {
				params.add("%" + keyword + "%");
			}
		}
		
		
		if(beginTime != null&&!"".equals(beginTime)) {
			
			sql.append("and t_supervisor_events.time > ? ");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}
		
		if(endTime != null&&!"".equals(endTime)) {
			sql.append("and t_supervisor_events.time < ? ");
			params.add(DateUtil.strDateToEndDate(endTime));
		}

		Date minDate = null;
		int count = 0;
		List<v_supervisor_events> page = null;
		
		try {
			EntityManager em = JPA.em();
			
			Query queryMinDate = em.createNativeQuery("select min(t_supervisor_events.time) time from `t_supervisor_events` left join `t_supervisor_event_types` on`t_supervisor_events`.`type_id` = `t_supervisor_event_types`.`id` left join `t_supervisors` on`t_supervisor_events`.`supervisor_id` = `t_supervisors`.`id`");
			queryMinDate.setMaxResults(1);
            List<v_supervisor_events> v_supervisor_events_list = queryMinDate.getResultList();
            
            if(v_supervisor_events_list.size() > 0){
            	minDate = Convert.strToDate(v_supervisor_events_list.get(0) + "", new Date());
            }
            
            Query query = em.createNativeQuery(sql.toString(),v_supervisor_events.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}

		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("keywordType", keywordType);

		if (StringUtils.isNotBlank(keyword)) {
			map.put("keyword", keyword);
		}
		
		if(beginTime != null) {
			map.put("beginTime", beginTime);
		}
		
		if(endTime != null) {
			map.put("endTime", endTime);
		}
		
		map.put("days", (minDate==null)? 0 : DateUtil.daysBetween(minDate, new Date()));
		
		PageBean<v_supervisor_events> bean = new PageBean<v_supervisor_events>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = map;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 查询删除操作日志记录
	 * @param currPage
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<v_supervisor_events> querySupervisorDeleteEvents(int currPage, int pageSize, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		int count = 0;
		List<v_supervisor_events> page = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_SUPERVISOR_EVENTS);
		sql.append(" and type_id = ? ");
		
		List<Object> params = new ArrayList<Object>();
		params.add(SupervisorEvent.DELETE_EVENT);
		
		try {
			EntityManager em = JPA.em();
			
            Query query = em.createNativeQuery(sql.toString(),v_supervisor_events.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            page = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
		
		PageBean<v_supervisor_events> bean = new PageBean<v_supervisor_events>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.totalCount = count;
		bean.page = page;
		bean.conditions = null;
		
		error.code = 0;

		return bean;
	}
	
	/**
	 * 删除操作日志
	 * @param type 0 全部、 1 一周前、 2 一月前 
	 * @param error
	 */
	public static int deleteEvents(int type, ErrorInfo error) {
		error.clear();

		if (type < 0 || type > 2) {
			error.code = -1;
			error.msg = "删除操作日志,参数有误";
			
			return error.code;
		}
		
		Date date = null;
		String description = null;
		
		if (1 == type) {
			date = DateUtils.addWeeks(new Date(), -1);
			description = "删除一周前操作日志";
		} else if (2 == type) {
			date = DateUtils.addMonths(new Date(), -1);
			description = "删除一个月前操作日志";
		} else {
			description = "删除全部操作日志";
		}
		
		try {
			if (0 == type) {
				t_supervisor_events.deleteAll();
			} else {
				t_supervisor_events.delete("time < ?", date);
			}
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.DELETE_EVENT, description, error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除操作日志成功";
		
		return error.code;
	}
	
	/**
	 * 本金保障账户概要
	 * @param error
	 * @return
	 */
	public static Map<String, Object> accountSummary(ErrorInfo error) {
		String sql = "select IFNULL(SUM(case when type=1 then amount end),0) as income,"
				+ "IFNULL(SUM(case when type=2 then amount end),0) as expense,"
				+ "count(case when operation = 4 then id end) as advance,"
				+ "IFNULL(SUM(case when operation = 4 then amount end),0) as payment,"
				+ "(IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0)) as balance,"
				+ "(IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0) - "
				+ "IFNULL(SUM(case when operation=1 then amount end),0)) as real_balance from t_platform_details";
		
		Object[] obj = null;
		try{
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障账户概要时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障账户概要";
			
			return null;
		}
		
		if(obj == null) {
			return null;
		}
		
		Map<String, Object> account = new HashMap<String, Object>();
		account.put("income", Double.parseDouble(obj[0].toString()));
		account.put("expense", Double.parseDouble(obj[1].toString()));
		account.put("advance", Integer.parseInt(obj[2].toString()));
		account.put("payment", Double.parseDouble(obj[3].toString()));
		account.put("balance", Double.parseDouble(obj[4].toString()));
		account.put("real_balance", Double.parseDouble(obj[5].toString()));
		
		error.code = 0;
		
		return account;
	}
	
	public static void addCapital(double amount, String summary, ErrorInfo error) {
		error.clear();
		
		if(amount <= 0) {
			error.code = -1;
			error.msg = "请输入添加金额";
			
			return ;
		}
		
		if(StringUtils.isBlank(summary)) {
			error.code = -1;
			error.msg = "请输入备注信息";
			
			return ;
		}
		
		if(summary.length() > 100) {
			error.code = -1;
			error.msg = "输入信息过大，请减少信息";
			
			return ;
		}
		
		long supervisorId = Supervisor.currSupervisor().id;
		
		DealDetail.addPlatformDetail(DealType.ADD_CAPITAL, supervisorId, -1, -1, DealType.HAND, amount, 1, summary, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ADD_MONEY, "添加保障金", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 0;
		error.msg = "保证金添加成功";
	}
	
	/**
	 * 本金保障收支记录
	 * @param type
	 * @param operation
	 * @param side
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param currPage
	 * @param error
	 * @return
	 */
	public static PageBean <v_platform_detail> platformDetail(int noPage, int type, int operation, int side,
			String beginTime, String endTime, String name, int currPage, ErrorInfo error) {
		error.clear();
 		
		if(currPage == 0) {
			currPage = 1;
		}
		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		conditionMap.put("type", type);
		conditionMap.put("operation", operation);
		conditionMap.put("side", side);
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);
		conditionMap.put("name", name);
		conditionMap.put("currPage", currPage);
 		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_PLATFORM_DETAIL);
		
		List<Object> params = new ArrayList<Object>();
		
		sql.append(Constants.TYPE[type]);
		
		if(operation != 0) {
			sql .append(" and operation = ? ");
			params.add(operation);
		}
		
		if(StringUtils.isNotBlank(name)) {
			sql.append(Constants.SIDE[side]);
			
			if(side == 0 ) {
				params.add("%"+name+"%");
				params.add("%"+name+"%");
			}else {
				params.add("%"+name+"%");
			}	
		}
		
		if(StringUtils.isNotBlank(beginTime)) {
			sql.append("and time >= ? ");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}
		
		if(StringUtils.isNotBlank(endTime) ){
			sql.append("and time <= ? ");
			params.add(DateUtil.strDateToEndDate(endTime));
		}
		
		sql.append(" order by time DESC");
		
		List<v_platform_detail> details = new ArrayList<v_platform_detail>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_platform_detail.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * Constants.PAGE_SIZE);
            	query.setMaxResults(Constants.PAGE_SIZE);
            }
            details = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障收支记录时："+e.getMessage());
			error.code = -1;
			error.msg = "本金保障收支记录查询失败";
			
			return null;
		}
		
		PageBean<v_platform_detail> page = new PageBean<v_platform_detail>();
		
		page.pageSize = Constants.PAGE_SIZE;
		page.currPage = currPage;
		page.page =details;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 查询所有类型
	 * @param error
	 * @return
	 */
	public static List<t_platform_detail_types> queryType(int type, ErrorInfo error) {
		error.clear();
		error.code = -1;
		
		List<t_platform_detail_types> detialTypes = null;
			
		try {
			detialTypes = (List<t_platform_detail_types>) (type == 0 ? t_platform_detail_types.findAll() : t_platform_detail_types.find(" type = ? or type = ?", type, DealType.INCOME_OR_PAY).fetch());
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询所以交易类型时："+e.getMessage());
			error.code = -1;
			error.msg = "查询所以交易类型失败";
			
			return null;
		}
		error.code = 0;
		
		return detialTypes;
	}
	
	/**
	 * 本金保障收支记录下面的统计
	 * @param error
	 * @return
	 */
	public static Map<String, Double> total(ErrorInfo error) {
		String sql = "select IFNULL(SUM(case when type=1 then amount end),0) as income, "
				+ "IFNULL(SUM(case when type=2 then amount end),0) as expense "
				+"from t_platform_details";
		
		Object[] obj = null;
		
		try{
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障账户概要时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障账户概要";
			
			return null;
		}
		
		if(obj == null) {
			return null;
		}
		
		Map<String, Double> account = new HashMap<String, Double>();
		account.put("income", Double.parseDouble(obj[0].toString()));
		account.put("expense", Double.parseDouble(obj[1].toString()));
		
		error.code = 0;
		
		return account;
	}
	
	/**
	 * 本金保障收支记录详情
	 */
	public static v_platform_detail detail(long id, ErrorInfo error) {
		error.clear();
		
		v_platform_detail detail = null;
		List<v_platform_detail> v_platform_detail_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_PLATFORM_DETAIL);
		sql.append(" and t_platform_details.id = ?");
			
		try{
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_platform_detail.class);
            query.setParameter(1, id);
            query.setMaxResults(1);
            v_platform_detail_list = query.getResultList();
            
            if(v_platform_detail_list.size() > 0){
            	detail = v_platform_detail_list.get(0);
            }
            
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障收支记录详情时："+e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障收支记录详情失败";
			
			return null;
		}
		
		error.code = 0;
		
		return detail;
	}
	
	/**
	 * 前台--本金保障
	 * @param error
	 * @return
	 */
	public static Map<String, Double> currTotal(ErrorInfo error) {
		error.clear();
		
		String sql = "select (IFNULL(SUM(case when type=1 then amount end),0) - IFNULL(SUM(case when type=2 then amount end),0)) as balance, "
				+ "IFNULL(SUM(case when type=1 and TO_DAYS(time)=TO_DAYS(NOW()) then amount end),0) as income, "
				+ "IFNULL(SUM(case when type=2 and TO_DAYS(time)=TO_DAYS(NOW()) then amount end),0) as expense "
				+"from t_platform_details";
		
		Object[] obj = null;
		
		try{
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询本金保障账户概要时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询本金保障账户概要";
			
			return null;
		}
		
		if(obj == null) {
			return null;
		}
		
		Map<String, Double> account = new HashMap<String, Double>();
		account.put("balance", Double.parseDouble(obj[0].toString()));
		account.put("income", Double.parseDouble(obj[1].toString()));
		account.put("expense", Double.parseDouble(obj[2].toString()));
		
		error.code = 0;
		
		return account;
	}

	/**
	 * 及时查询用户的可用余额、冻结资金、待收金额，避免缓存/视图引起的数据误差
	 */
	public static Map<String, Double> queryUserFund(long userId, ErrorInfo error){
		error.clear();
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_FOR_DETAILS);
		sql.append(" and t_users.id = ?");
		
		JSONObject json = JPAUtil.getJSONObject(error, sql.toString(), userId);
		
		if(json == null) {
			error.code = -1;
			error.msg = "用户id不存在!";
			
			return null;
		}
		
		Map<String, Double> userDeal = new HashMap<String, Double>();
		userDeal.put("user_amount", json.getDouble("user_amount"));
		userDeal.put("user_amount2", json.getDouble("user_amount2"));
		userDeal.put("freeze", json.getDouble("freeze"));
		userDeal.put("receive_amount", json.getDouble("receive_amount"));
	
		error.code = 0;
		
		return userDeal;
	}
	
	/******************************以上为全部过程，下面为另外一种方法****************************/
	
	/**
	 * 增加用户信用积分
	 * @param userId 用户ID 
	 * @param score 分值
	 * @param error 信息值
	 */
	public static void addCreditScore(long userId, String key, ErrorInfo error){
		error.clear();
		String value = OptionKeys.getvalue(key, error);
		
		if(!NumberUtil.isNumericInt(value)){
			Logger.error("交易记录->根据常量查询积分出现错误!");
			error.msg = "增加用户信用积分失败!";
			
			return; 
		}
		
		String hql = "update t_users set credit_score = credit_score + ? where id = ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Double.parseDouble(value));
		query.setParameter(2, userId);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("交易记录->增加用户信用积分:" + e.getMessage());
			error.msg = "增加用户信用积分失败!";
		}
		
		error.msg = error.code > 0 ? "增加成功!" : "增加失败!";
	}
	
	
	/**
	 * 增加用户系统积分
	 * @param userId 用户ID 
	 * @param score 分值
	 * @param error 信息值
	 */
	public static void addSystemScore(long userId, double investAmount,String key, ErrorInfo error){
		error.clear();
		String value = OptionKeys.getvalue(key, error);
		
		if(!NumberUtil.isNumericInt(value)){
			Logger.error("交易记录->根据常量查询积分出现错误!");
			error.msg = "增加用户系统积分失败!";
			
			return; 
		}
		
		String hql = "update t_users set score = score + ? where id = ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, Double.parseDouble(value)*investAmount);
		query.setParameter(2, userId);
		
		try {
			error.code = query.executeUpdate();
		} catch (Exception e) {
			Logger.error("交易记录->增加用户系统积分:" + e.getMessage());
			error.code = -1;
			error.msg = "增加用户系统积分失败!";
			
			return;
		}
		
		error.msg = error.code > 0 ? "增加成功!" : "增加失败!";
	}
	
	/**
	 * 冻结用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @return -1:失败; >0:成功
	 */
	public static void freezeFund(long userId, double balance, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_users set balance = balance - ?, freeze = freeze + ? where id = ? and balance >= ?";

		int row = 0;
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, balance);
		query.setParameter(3, userId);
		query.setParameter(4, balance);
		
		try {
			 row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "冻结用户资金时异常";
			Logger.error("冻结用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();

			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "冻结用户资金失败";
			Logger.info("冻结用户资金失败，可能是账户余额不足");
			
			JPA.setRollbackOnly();
		}
	}
	
	/**
	 * 解除冻结用户资金
	 * @param userId 用户ID
	 * @param balance 费用
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void relieveFreezeFund(long userId, double balance, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_users set balance = balance + ?, freeze = freeze - ? where id = ? and freeze >= ?";
		
		int row = 0;
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, balance);
		query.setParameter(3, userId);
		query.setParameter(4, balance);

		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "解除冻结用户资金时异常";
			Logger.error("解除冻结用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "解除冻结用户资失败";
			Logger.error("解除冻结用户资金失败，可能是冻结余额不足");
			
			JPA.setRollbackOnly();
		}

	}	
	/**
	 * 增加用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static int addUserFund(long userId, double balance){
		String hql = "update t_users set balance = balance + ? where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		
		try {
			Logger.info("增加用户资金 userId:" + userId+", 金额："+balance);
			return query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("增加用户资金时，" + e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 增加balance2
	 * @param userId 用户ID
	 * @param balance 金额
	 * @return -1:失败;  >0:成功;
	 */
	public static int addUserFund2(long userId, double balance){
		String hql = "update t_users set balance2 = balance2 + ? where id = ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		
		try {
			return query.executeUpdate();
		} catch (Exception e) {
			Logger.error("增加用户资金:" + e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 减少用户资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void minusUserFund(long userId, double balance, ErrorInfo error){
		error.clear();
		
		int row = 0;
		String hql = "update t_users set balance = balance - ? where id = ? and balance >= ?";
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, balance);
		query.setParameter(2, userId);
		query.setParameter(3, balance);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "减少用户资金时数据异常";
			Logger.error("减少用户资金:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "减少用户资金失败";
			Logger.info("减少用户资金失败，可能是用于余额不足");
			
			JPA.setRollbackOnly();
			
			return ;
		}
	}
	
	/**
	 * 减少用户冻结资金
	 * @param userId 用户ID
	 * @param balance 金额
	 * @param info 错误信息
	 * @return -1:失败;  >0:成功;
	 */
	public static void minusUserFreezeFund(long userId, double freeze, ErrorInfo error){
		error.clear();
		
		int row = 0;
		String hql = "update t_users set freeze = freeze - ? where id = ? and freeze >= ?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, freeze);
		query.setParameter(2, userId);
		query.setParameter(3, freeze);
		
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "减少用户冻结资金时异常";
			Logger.error("减少用户冻结资金时:" + e.getMessage());
			
			JPA.setRollbackOnly();
			
			return ;
		}
		
		if(row == 0){
			error.code = -2;
			error.msg = "减少用户冻结资金失败";
			Logger.info("减少用户冻结资金失败,可能是用户冻结余额不足"+String.format("user_id:%s; freeze:%s; freeze:%s;", userId, freeze, freeze));
			
			JPA.setRollbackOnly();
		}
	}
	
	/**
	 * 充值记录
	 * @param type
	 * @param status
	 * @param name
	 * @param startDate
	 * @param endDate
	 * @param currPage
	 * @param error
	 * @return
	 */
	public static PageBean<t_user_recharge_details> queryUserRechargeDetails(int noPage,
			int type, int status, String name, String startDate, String endDate,
			int currPage) {
		PageBean<t_user_recharge_details> pageBean = new PageBean<t_user_recharge_details>();
		Map<String, Object> conditionmap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		if(type > 0) {
			type--;
			conditions.append(" and urd.type = ?");
			values.add(type);
			conditionmap.put("type", type);
		}
			
		if(status > 0) {
			status--;
			conditions.append(" and urd.is_completed = ?");
			values.add(status);
			conditionmap.put("status", status);
		}	
		
		if(StringUtils.isNotBlank(name)) {
			conditions.append(" and u.name like ? or u.email like ? or u.mobile like ?");
			values.add("%" + name + "%");
			values.add("%" + name + "%");
			values.add("%" + name + "%");
			conditionmap.put("name", name);
			conditionmap.put("name", name);
			conditionmap.put("name", name);
		}
		
		if(null != startDate && !"".equals(startDate)) {
			conditions.append(" and urd.time >= ?");
			values.add(DateUtil.strDateToStartDate(startDate));
			conditionmap.put("startDate", startDate);
		}
		
		if(null != endDate && !"".equals(endDate)) {
			conditions.append(" and urd.time <= ?");
			values.add(DateUtil.strDateToEndDate(endDate));
			conditionmap.put("endDate", endDate);
		}
		
		pageBean.conditions = conditionmap;
		Object count = 0;
		int size = values.size();
		String sql = "select count(1) from t_user_recharge_details urd JOIN t_users u ON urd.user_id = u.id" + conditions.toString();
		Query query = JPA.em().createNativeQuery(sql);
		
		for (int i = 0; i < size; i++) 
			query.setParameter((i + 1), values.get(i));
		
		try {
			count = query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("交易记录->充值记录，查询总数" + e.getMessage());
			
			return null;
		}
		
		if(null == count)
			return pageBean;
		
		int count2 = Integer.parseInt(count.toString());
		
		if(count2 < 1)
			return pageBean;
		
		sql = "select urd.* from t_user_recharge_details urd JOIN t_users u ON urd.user_id = u.id "
				+ conditions.toString() + " order by time desc";
		query = JPA.em().createNativeQuery(sql, t_user_recharge_details.class);
	    
	    for (int i = 0; i < size; i++) 
			query.setParameter((i + 1), values.get(i));

	    currPage = currPage == 0 ? 1 : currPage;
	    
	    if(noPage != Constants.NO_PAGE){
	    	query.setFirstResult((currPage - 1) * Constants.PAGE_SIZE);
	    	query.setMaxResults(Constants.PAGE_SIZE);
	    }

        List<t_user_recharge_details> rechargeDetails = null;
	    
		try {
			rechargeDetails = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("交易记录->充值记录，查询数据" + e.getMessage());
			
			return null;
		}
		
		pageBean.totalCount = count2;
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.PAGE_SIZE;
		pageBean.page = rechargeDetails;
		
		return pageBean;
	}
	
	/**
	 * 提现记录
	 * @param currPage
	 * @param pageSize
	 * @param name
	 * @param status #0 请选择  #1成功  #2失败
	 * @param startDate
	 * @param endDate
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_withdrawals> queryWithdrawRecords(int noPage, int currPage, int pageSize, String name, int status, String startDate, String endDate, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_USER_WITHDRAWALS);

		List<Object> params = new ArrayList<Object>();
			
		if(StringUtils.isNotBlank(name)) {
			sql.append(" and user.name like ?");
			params.add("%"+name+"%");
			conditionMap.put("name", name);
		}
		
		if (status == 1) {
			sql.append(" and w.status = 2");
		} else if (status == 2) {
			sql.append(" and w.status != 2");
		}
		
		conditionMap.put("status", status);
		
		if(StringUtils.isNotBlank(startDate)) {
			sql.append(" and w.time > ? ");
			params.add(startDate);
			conditionMap.put("startDate", startDate);
		}
		
		if(StringUtils.isNotBlank(endDate)) {
			sql.append(" and w.time < ? ");
			params.add(endDate);
			conditionMap.put("endDate", endDate);
		}
		
		sql.append(" ORDER BY w.time DESC");
		
		List<v_user_withdrawals> withdrawals = new ArrayList<v_user_withdrawals>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_user_withdrawals.class);
            
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * pageSize);
            	query.setMaxResults(pageSize);
            }
            
            withdrawals = query.getResultList();
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
		} catch (Exception e) {
			Logger.error("查询提现记录时："+e.getMessage());
			error.code = -1;
			error.msg = "查询提现记录时出现异常！";
			
			return null;
		}
		
		PageBean<v_user_withdrawals> page = new PageBean<v_user_withdrawals>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = withdrawals;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 查询商户交易记录
	 * @param noPage
	 * @param type
	 * @param status
	 * @param name
	 * @param startDate
	 * @param endDate
	 * @param currPage
	 * @return
	 */
	public static PageBean<t_merchant_deal_details> queryMerDealDetails(
			int type, String startDate, String endDate, int currPage) {
		
		PageBean<t_merchant_deal_details> pageBean = new PageBean<t_merchant_deal_details>();
		
		Map<String, Object> conditionmap = new HashMap<String, Object>();
		StringBuffer conditions = new StringBuffer(" where 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		
		if (currPage < 1) {
			currPage = 1;
		}
		
		if(type > 0) {
			conditions.append(" and type = ?");
			values.add(type);
			conditionmap.put("type", type);
		}
			
		if(null != startDate && !"".equals(startDate)) {
			conditions.append(" and time >= ?");
			values.add(DateUtil.strDateToStartDate(startDate));
			conditionmap.put("startDate", startDate);
		}
		
		if(null != endDate && !"".equals(endDate)) {
			conditions.append(" and time <= ?");
			values.add(DateUtil.strDateToEndDate(endDate));
			conditionmap.put("endDate", endDate);
		}
		
		pageBean.conditions = conditionmap;
		
		Object count = 0;
		int size = values.size();
		String sql = "select count(1) from t_merchant_deal_details " + conditions.toString();
		Query query = JPA.em().createNativeQuery(sql);
		
		for (int i = 0; i < size; i++) 
			query.setParameter((i + 1), values.get(i));
		
		try {
			count = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("交易记录->充值记录，查询总数" + e.getMessage());
			
			return null;
		}
		
		if(null == count)
			return pageBean;
		
		int count2 = Integer.parseInt(count.toString());
		
		if(count2 < 1)
			return pageBean;
		
		sql = "select * from t_merchant_deal_details " + conditions.toString() + " order by time desc";
		query = JPA.em().createNativeQuery(sql, t_merchant_deal_details.class);
	    
	    for (int i = 0; i < size; i++){ 
			query.setParameter((i + 1), values.get(i));
	    }
	    
    	query.setFirstResult((currPage - 1) * Constants.PAGE_SIZE);
    	query.setMaxResults(Constants.PAGE_SIZE);

        List<t_merchant_deal_details> merDealDetails = null;
	    
		try {
			merDealDetails = query.getResultList();
		} catch (Exception e) {
			Logger.error("商户管理-交易记录，查询数据" + e.getMessage());
			
			return null;
		}
		
		pageBean.totalCount = count2;
		pageBean.currPage = currPage;
		pageBean.pageSize = Constants.PAGE_SIZE;
		pageBean.page = merDealDetails;
		
		return pageBean;
	}
	
	/**
	 * 用户银行账户记录
	 * @param currPage
	 * @param pageSize
	 * @param name
	 * @param status #0 请选择  #1成功  #2失败
	 * @param startDate
	 * @param endDate
	 * @param error
	 * @return
	 */
	public static PageBean<t_user_bank_accounts> queryBankAccounts(int noPage, int currPage, int pageSize,String userName,String accountName,String accountNo, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		Map<String, Object> conditionMap = new HashMap<String, Object>();
		//StringBuffer sql = new StringBuffer("select * from t_user_bank_accounts where 1=1 ");
		StringBuffer sql = new StringBuffer(" select tub.*,u.name as userName from t_user_bank_accounts as tub  left join t_users as u on tub.user_id = u.id  where 1=1 ");

		List<Object> params = new ArrayList<Object>();
			
		/*if(StringUtils.isNotBlank(name)) {
			sql.append(" and user.name like ?");
			params.add("%"+name+"%");
			conditionMap.put("name", name);
		}*/
		
		
		if(StringUtils.isNotBlank(userName)){
			
			sql.append(" and u.name like ?");
			params.add("%"+userName.trim()+"%");
			conditionMap.put("userName", userName);
			
		}
		
		if(StringUtils.isNotBlank(accountName)){
			
			sql.append(" and tub.account_name like ?");
			params.add("%"+accountName.trim()+"%");
			conditionMap.put("accountName", accountName);
			
		}
		
		if(StringUtils.isNotBlank(accountNo)){
			
			sql.append(" and tub.account = ?");
			params.add(accountNo.trim());
			conditionMap.put("accountNo", accountNo);
			
		}
		
		/*if(StringUtils.isNotBlank(startDate)) {
			sql.append(" and w.time > ? ");
			params.add(startDate);
			conditionMap.put("startDate", startDate);
		}
		
		if(StringUtils.isNotBlank(endDate)) {
			sql.append(" and w.time < ? ");
			params.add(endDate);
			conditionMap.put("endDate", endDate);
		}*/
		
		//sql.append(" ORDER BY w.time DESC");
		
		List<t_user_bank_accounts> bankAccounts = new ArrayList<t_user_bank_accounts>();
		
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),t_user_bank_accounts.class);
            
			
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * pageSize);
            	query.setMaxResults(pageSize);
            }
            
            bankAccounts = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
		} catch (Exception e) {
			Logger.error("查询客户银行记录时："+e.getMessage());
			error.code = -1;
			error.msg = "查询客户银行记录出现异常！";
			
			return null;
		}
		
		PageBean<t_user_bank_accounts> page = new PageBean<t_user_bank_accounts>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = bankAccounts;
		
		error.code = 0;
		
		return page;
	}
}
