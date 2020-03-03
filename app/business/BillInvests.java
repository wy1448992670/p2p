package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bill_invests;
import models.t_user_cps_profit;
import models.t_wealthcircle_invite;
import models.v_bill_invest;
import models.v_bill_invest_detail;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.shove.Convert;
import com.timevale.tgtext.text.pdf.parser.t;

import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import constants.Constants;
import constants.PaymentTypeEnum;
import constants.SQLTempletes;

public class BillInvests implements Serializable{

	public long id;
	private long _id;
	
	public long userId;
	public long bidId;
	public int period;
	public String title;
	public Date receiveTime;
	public double receiveCorpus;
	public double receiveInterest;
	public int status;
	public double overdueFine;
	public Date realReceiveTime;
	public double realReceiveCorpus;
	public double realReceiveInterest;
	
	public double receiveIncreaseInterest;//应收加息利息
	public double realIncreaseInterest;//实际收款加息利息
	public double allAmount;//总金额
	
	public Bid bid;
	
	public void setId(long id){
		t_bill_invests invest = t_bill_invests.findById(id);
		
		if(invest.id < 0 || invest == null){
			this._id = -1;
			return;
		}
		
		this._id = invest.id;
		this.userId = invest.user_id;
		this.bidId = invest.bid_id;
		this.period = invest.periods;
		this.title = invest.title;
		this.receiveTime = invest.receive_time;
		this.receiveCorpus = invest.receive_corpus;
		this.receiveInterest = invest.receive_interest;
		this.status = invest.status;
		this.overdueFine = invest.overdue_fine;
		this.realReceiveCorpus = invest.real_receive_corpus;
		this.realReceiveInterest = invest.real_receive_interest;
		
		bid = new Bid();
   	    bid.id = invest.bid_id;
	}
	
	public long getId(){
		
		return this._id;
	}
	
	/**
	 * 查询我所有的理财账单
	 * @param error
	 * @return
	 */
	public static List<v_bill_invest> queryMyAllInvestBills(ErrorInfo error) {
		error.clear();
		
		List<v_bill_invest> bills = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);
		sql.append(" and c.id = ? group by a.id");
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            query.setParameter(1, User.currUser().id);
            bills = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我所有的理财账单:" + e.getMessage());
			error.code = -1;
			error.msg = "查询我所有的理财账单失败!";
			
			return null;
		}
		
		return bills;
	}
	
	/**
 	 * 查询我的理财账单
 	 * @param userId
 	 * @param info
 	 * @param currPage
 	 * @return
 	 */
 	public static PageBean<v_bill_invest> queryMyInvestBills(int payType, int isOverType,
			int keyType, String keyStr, int currPageStr, long userId, ErrorInfo error){
        error.clear();
		
 		int count = 0;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		
        Map<String,Object> conditionMap = new HashMap<String, Object>();
 		List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST);

		List<Object> params = new ArrayList<Object>();
 		
 		if((payType < 0) || (payType > 2)){
 			payType = 0;
 		}
 		
 		if((isOverType < 0) || (isOverType > 2)){
 			isOverType = 0;
 		}
 		
 		if((keyType < 0) || (keyType > 3)){
 			keyType = 0;
 		}
 		
 		if(currPageStr != 0){
 			currPage = currPageStr;
 		}
 		
 		if(StringUtils.isNotBlank(keyStr)) {
 			if(keyType == 2){  //微信查询用到了三个字段
 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
 				params.add("%"+keyStr.trim()+"%");
 				params.add("%"+keyStr.trim()+"%");
 			}else{
 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
 				params.add("%"+keyStr.trim()+"%");
 			}
		}
 		
 		sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
 		sql.append(SQLTempletes.LOAN_INVESTBILL_OVDUE[isOverType]);
 		sql.append("and c.id = ?");
 		params.add(userId);
 		
 		conditionMap.put("payType", payType);
		conditionMap.put("isOverType", isOverType);
		conditionMap.put("keyType", keyType);
		conditionMap.put("key", keyStr);
		
		try {
			sql.append(" group by id desc ");
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bills = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单失败";
			
			return null;
		}
		
		PageBean<v_bill_invest> page = new PageBean<v_bill_invest>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = bills;
		
		return page;
 	}
// 	/**
// 	 * 查询我的理财账单app
// 	 * @param userId
// 	 * @param info
// 	 * @param currPage
// 	 * @return
// 	 */
// 	public static PageBean<v_bill_invest> queryMyInvestBills1(int payType, int isOverType,
// 			int keyType, String keyStr, int currPageStr, long userId, ErrorInfo error){
// 		error.clear();
// 		
// 		int count = 0;
// 		int currPage = Constants.ONE;
// 		int pageSize = Constants.TEN;
// 		
// 		Map<String,Object> conditionMap = new HashMap<String, Object>();
// 		List<v_bill_invest> bills = new ArrayList<v_bill_invest>();
// 		StringBuffer sql = new StringBuffer("");
// 		Date date = new Date();
// 		String dateToString  = DateUtil.dateToString1(date);
// 		sql.append(SQLTempletes.SELECT);
// 		//dateToString.substring(0, 7)
// 		sql.append("  `a`.`id` AS `id`, `c`.`id` AS `user_id`, `a`.`bid_id` AS `bid_id`, `a`.`title` AS `title`, `b`.`repayment_type_id` AS `repayment_type_id`, " +
// 				"ifnull(( SELECT ( `tb`.`receive_corpus` + `tb`.`receive_interest` ) AS ss FROM t_bill_invests tb LEFT JOIN t_bids bs ON tb.bid_id = bs.id WHERE bs.repayment_time >= '"+dateToString.substring(0, 7)+" 00:00:00' AND bs.repayment_time <= '"+dateToString.substring(0, 7)+" 23:59:59' AND tb.`status` IN (-1 ,-2 ,-5 ,-6) AND ( b.repayment_type_id = 1 OR b.repayment_type_id = 2 ) AND a.id = tb.id ), '0' ) AS month_income, b.repayment_time AS repayment_time, (( `a`.`receive_corpus` + `a`.`receive_interest` ) + `a`.`overdue_fine` ) AS `income_amounts`, " +
// 				"`a`.`status` AS `status`, `a`.`receive_time` AS `receive_time`, concat( `d`.`_value`, cast(`b`.`id` AS CHAR charset utf8)) AS `bid_no`, `a`.`real_receive_time` AS `real_repayment_time` FROM " +
// 				"(( `t_bill_invests` `a` JOIN `t_bids` `b` ON ((`a`.`bid_id` = `b`.`id`))) JOIN `t_users` `c` ON ((`a`.`user_id` = `c`.`id`)) JOIN `t_system_options` `d` ) WHERE 1 = 1 " +
// 				"AND `d`.`_key` = 'loan_number' AND c.id = "+userId+" AND ( (`a`.`status` IN (0 ,-3 ,-4) OR ( `a`.`status` IN (-1 ,-2 ,-5 ,-6) AND LEFT (b.repayment_time, 7) = '"+dateToString.substring(0, 7)+"' ) ) OR ( b.repayment_type_id=3 ))"); 		
// 		List<Object> params = new ArrayList<Object>();
// 		
// 		if((payType < 0) || (payType > 2)){
// 			payType = 0;
// 		}
// 		
// 		if((isOverType < 0) || (isOverType > 2)){
// 			isOverType = 0;
// 		}
// 		
// 		if((keyType < 0) || (keyType > 3)){
// 			keyType = 0;
// 		}
// 		
// 		if(currPageStr != 0){
// 			currPage = currPageStr;
// 		}
// 		
// 		if(StringUtils.isNotBlank(keyStr)) {
// 			if(keyType == 2){  //微信查询用到了三个字段
// 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
// 				params.add("%"+keyStr.trim()+"%");
// 				params.add("%"+keyStr.trim()+"%");
// 			}else{
// 				sql.append(SQLTempletes.LOAN_INVESTBILL_ALL[keyType]);
// 				params.add("%"+keyStr.trim()+"%");
// 			}
// 		}
// 		
// 		sql.append(SQLTempletes.LOAN_INVESTBILL_RECEIVE[payType]);
// 		sql.append(SQLTempletes.LOAN_INVESTBILL_OVDUE[isOverType]);
// 		
// 		conditionMap.put("payType", payType);
// 		conditionMap.put("isOverType", isOverType);
// 		conditionMap.put("keyType", keyType);
// 		conditionMap.put("key", keyStr);
// 		
// 		try {
// 			sql.append(" group by id desc ");
// 			EntityManager em = JPA.em();
// 			Query query = em.createNativeQuery(sql.toString(),v_bill_invest.class);
// 			for(int n = 1; n <= params.size(); n++){
// 				query.setParameter(n, params.get(n-1));
// 			}
// 			query.setFirstResult((currPage - 1) * pageSize);
// 			query.setMaxResults(pageSize);
// 			bills = query.getResultList();
// 			
// 			count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
// 			
// 		}catch (Exception e) {
// 			e.printStackTrace();
// 			Logger.info("查询我的理财账单时："+e.getMessage());
// 			error.code = -1;
// 			error.msg = "由于数据库异常，查询我的理财账单失败";
// 			
// 			return null;
// 		}
// 		
// 		PageBean<v_bill_invest> page = new PageBean<v_bill_invest>();
// 		page.pageSize = pageSize;
// 		page.currPage = currPage;
// 		page.totalCount = count;
// 		page.conditions = conditionMap;
// 		
// 		page.page = bills;
// 		
// 		return page;
// 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, long userId, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ? and a.user_id = ?");

 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setParameter(2, userId);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的账单详情
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static v_bill_invest_detail queryMyInvestBillDetails(long id, ErrorInfo error){
 		error.clear();
		
 		v_bill_invest_detail investDetail = new v_bill_invest_detail();
 		List<v_bill_invest_detail> v_bill_invest_detail_list = null;
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_BILL_INVEST_DETAIL);
		sql.append(" and a.id = ?");
		
 		try {
 			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_invest_detail.class);
            query.setParameter(1, id);
            query.setMaxResults(1);
            v_bill_invest_detail_list = query.getResultList();
            
            if(v_bill_invest_detail_list.size() > 0){
            	investDetail = v_bill_invest_detail_list.get(0);
            }
            
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单详情时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
		}
		 
		if(null == investDetail){
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单详情失败";
			
			return null;
			
		}
		
		error.code = 1;
 		return investDetail;
 	}
 	
 	/**
 	 * 我的理财账单——-历史收款情况
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static PageBean<t_bill_invests> queryMyInvestBillReceivables(long bidId,long userId, long investId, int currPage, int pageSize, ErrorInfo error){
 		error.clear();
 		
 		String sql = "select new t_bill_invests(id as id,title as title, SUM(receive_corpus + receive_interest + ifnull(overdue_fine,0)) as receive_amount, " +
 				"status as status, receive_time as  receive_time, real_receive_time as real_receive_time )" +
 				"from t_bill_invests where bid_id = ? and user_id = ? and invest_id = ? group by id";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();
		PageBean<t_bill_invests> page = new PageBean<t_bill_invests>();
		page.pageSize = Constants.FIVE;
		page.currPage = Constants.ONE;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(pageSize != 0){
			page.pageSize = pageSize;
		}
		
		try {
			page.totalCount = (int) t_bill_invests.count("bid_id = ? and user_id = ? and invest_id = ?", bidId, userId, investId);
			investBills = t_bill_invests.find(sql, bidId, userId, investId).fetch(page.currPage, page.pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		
		page.page = investBills;

		return page;
 	}
 	
 	/**
 	 * 我的理财账单——-根据标的ID和投资人ID查询还款记录
 	 * @param id
 	 * @param currPage
 	 * @param info
 	 * @return
 	 */
 	public static List<t_bill_invests> queryMyInvestBillReceivablesBid(long bidId,long userId, ErrorInfo error){
 		error.clear();
 		String sql = "SELECT new t_bill_invests(id AS id,title AS title,status AS status, receive_time AS  receive_time,(receive_corpus+receive_interest) AS receive_amount,real_receive_time AS real_receive_time)" +
 				" FROM t_bill_invests WHERE bid_id = ? AND user_id = ? order by receive_time asc";
 		
		List<t_bill_invests> investBills = new ArrayList<t_bill_invests>();

		try {
			investBills = t_bill_invests.find(sql, bidId, userId).fetch();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询我的理财账单收款情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询我的理财账单收款情况失败";
			
			return null;
		}
		

		return investBills;
 	}
 	
 	/**
 	 * 查询累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double querySumIncome(long userId){
 		String sql ="select sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一月累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryMonthSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine)  from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 month)" ;
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 近一年累计收益
 	 * @param userId
 	 * @return
 	 */
 	public static double queryYearSumIncome(long userId){
 		String sql ="select  sum(real_receive_interest) + sum(overdue_fine) from t_bill_invests where user_id=? and  status  =0  and receive_time >date_add(now(), interval -1 year)";
        Query query = JPA.em().createNativeQuery(sql);
        query.setParameter(1, userId);
        List<Object> list = query.getResultList();
        if(list.size() == 0 )
        	return 0d;
        if(list.get(0) == null){
        	return 0d;
        }
        return  Double.valueOf(list.get(0).toString());
 	}
 	
 	/**
 	 * 计算理财管理费
 	 * @param receiveInterest  本期应收利息
 	 * @param managementRate 费率
 	 * @param investUserId  投资者id
 	 * @return
 	 */
 	public static double getInvestManagerFee(double receiveInterest, double managementRate, long investUserId){
 		
 		double manageFee = Arith.round(Arith.mul(receiveInterest, managementRate), 2);  //投资管理费;
 		
 		//财富圈，被邀请人享受服务费
		t_wealthcircle_invite invite = t_wealthcircle_invite.find("invited_user_id = ?", investUserId).first();
	
		if (null != invite){
			manageFee = manageFee * invite.invited_user_discount / 100;
		}
		
		return manageFee;
		
 	}
 	
 	/**
 	 * 获取投资账单列表
 	 * @param bidId		借款标ID
 	 * @param periods	期数
 	 * @return
 	 */
 	public static List findBillInvestsByBidIdAndPeriods(long bidId, int periods) {
 		List<Map<String, Object>> billInvestList = null;
 		
 		String sql = " select new Map(invest.id as id, invest.invest_id as investId, "
 				+ " invest.receive_corpus as receive_corpus,invest.receive_interest as " +
 				" receive_interest, invest.overdue_fine as overdue_fine, invest.user_id as user_id, "
 				+ " invest.overdue_fine) "
				+ " from t_bill_invests as invest where invest.bid_id = ? and invest.periods = ? "
				+ "and invest.status not in (?,?,?,?)";
 		try {
 			billInvestList = t_bill_invests.find(sql, bidId, periods, 
 					Constants.FOR_DEBT_MARK, 
 					Constants.NORMAL_RECEIVABLES, 
 					Constants.ADVANCE_PRINCIIPAL_RECEIVABLES, 
 					Constants.OVERDUE_RECEIVABLES).fetch();
 			
		} catch (Exception e) {
			Logger.error("------- 获取投资账单列表失败：", e);
			e.printStackTrace();
			return null;
		}
 		
		return billInvestList;
 	}
 	
 	/**
 	 * 
 	 * @author liulj
 	 * @creationDate. 2016年9月28日 下午4:55:24 
 	 * @description.  查询理财用户正常收款的理财账单的所得收益
 	 * 
 	 * @param user_id	理财用户id
 	 * @return
 	 */
 	public static List<Map<String, Object>> findBillInvestIncome(long user_id){
 		//String sql = "select new t_bill_invests(id, user_id, bid_id, title, status, real_receive_time, real_receive_corpus, real_receive_interest) from t_bill_invests where status = 0 and user_id = ?";
 		//String sql = "select bi.id, bi.user_id, bi.bid_id, bi.title, bi.status, bi.receive_time as real_receive_time, bi.receive_corpus as real_receive_corpus, bi.receive_interest as real_receive_interest from t_bill_invests bi left join t_bids b on (bi.bid_id = b.id) where (b.status = ? or b.status = ?) and bi.user_id = ? and b.audit_time > '2016-10-26 00:00:00'";
 		String sql =" SELECT invest.id as invest_id, invest.user_id, invest.bid_id, bid.title, bid.STATUS, ";
 		sql +=" bid.audit_time,invest.amount ,invest.correct_interest ";
 		sql +=" FROM t_invests invest ";
 		sql +=" LEFT JOIN t_bids bid ON ( invest.bid_id = bid.id ) ";
 		sql +=" WHERE ( bid.STATUS = ? OR bid.STATUS = ? )  AND bid.audit_time > '2016-10-26 00:00:00' ";
 		sql +=" AND invest.user_id = ? ";
 		//List<t_bill_invests> invests = t_bill_invests.find(sql, Constants.BID_REPAYMENT, Constants.BID_COMPENSATE_REPAYMENT, user_id).fetch();
 		/*EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());
        query.setParameter(1, Constants.BID_REPAYMENT);
        query.setParameter(2, Constants.BID_COMPENSATE_REPAYMENT);
        query.setParameter(3, user_id);
        System.out.println(JSON.toJSONString(query.getResultList(), true));*/
        //return query.getResultList();
        return JPAUtil.getList(new ErrorInfo(), sql.toString(), new Object[]{Constants.BID_REPAYMENT, Constants.BID_COMPENSATE_REPAYMENT, user_id});
 	}
 	
 	public static List<Map<String, Object>> findUserProfit(Integer year, Integer month, String userName, String bidTag, String orderBy){
 		Logger.info("year:"+year+"; month:"+month+"; userName:"+userName+"; bigTag:"+bidTag+"; orderBy:"+orderBy);
 		StringBuffer sql = new StringBuffer();
 		String sqlSelectBase = "SELECT"
					+" uci.user_id,"
					+" u.name,";
 		
 		String sqlSelect_effective_user_account = " (SELECT SUM(effective_user_account) FROM t_user_cps_income WHERE user_id = uci.user_id AND 1=1 AND 2=2) AS effective_user_account,";
 		String sqlSelect_max_invest_amount = " (SELECT IFNULL(amount,0) FROM t_invests i WHERE i.user_id = uci.user_id AND time > ? and time < ? ORDER BY amount DESC LIMIT 1) AS max_invest_amount,";
 		String sqlSelect_new_user_invest_amount = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 1 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ?) AS new_user_invest_amount,";
 		String sqlSelect_invest_1 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND b.period = 1 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_1,";
 		String sqlSelect_invest_2 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 2 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_2,";
 		String sqlSelect_invest_3 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 3 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_3,";
 		String sqlSelect_invest_4 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 4 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_4,";
 		String sqlSelect_invest_5 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 5 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_5,";
 		String sqlSelect_invest_6 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 6 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_6,";
 		String sqlSelect_invest_12 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 12 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_12";
 		
 		String sqlFrom = " FROM t_user_cps_income uci"
					+" LEFT JOIN t_users u ON u.id = uci.user_id"
					+" WHERE 1 = 1";
					/*+ "uci.year = 2016 AND uci.month = 11"*/
 		
 		String sqlGroupBy = " GROUP BY uci.user_id";
 		String sqlOrderBy = " ORDER BY ";
 		String sqlOrderBy_default = "(invest_1+invest_2+invest_3+invest_4+invest_5+invest_6+invest_12) DESC";
 		sqlOrderBy_default = StringUtils.isNotBlank(orderBy) ? orderBy : sqlOrderBy_default;
 		
 		String start = "2016-01-01 00:00:00", end = "2088-01-01 00:00:00";
 		
 		List<Object> params = new ArrayList<Object>();
 		
		if(year != null){
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month != null ? month-1 : 0);
			
			start = new SimpleDateFormat("yyyy-MM-01 00:00:00").format(cal.getTime());
			cal.set(Calendar.MONTH, month != null ? month : 12);
			end = year != null ? new SimpleDateFormat("yyyy-MM-01 00:00:00").format(cal.getTime()) : end;
		}
		
		if(year != null){
			sqlSelect_effective_user_account = StringUtils.replace(sqlSelect_effective_user_account, "1=1", " year = ?");
			params.add(year);
		}
		if(month != null){
			sqlSelect_effective_user_account = StringUtils.replace(sqlSelect_effective_user_account, "2=2", " month = ?");
			params.add(month);
		}
		
		params.add(start);
		params.add(end);
		params.add(start);
		params.add(end);
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_1 = StringUtils.replace(sqlSelect_invest_1, "TAG", " b.tag = ?");
			System.out.println(sqlSelect_invest_1);
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_2 = StringUtils.replace(sqlSelect_invest_2, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_3 = StringUtils.replace(sqlSelect_invest_3, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_4 = StringUtils.replace(sqlSelect_invest_4, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_5 = StringUtils.replace(sqlSelect_invest_5, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_6 = StringUtils.replace(sqlSelect_invest_6, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_12 = StringUtils.replace(sqlSelect_invest_12, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		if(year != null){
 			sqlFrom = sqlFrom.concat(" AND uci.year = ?");
 			params.add(year);
 		}
 		if(month != null){
 			sqlFrom = sqlFrom.concat(" AND uci.month = ?");
 			params.add(month);
 		}
 		if(StringUtils.isNotBlank(userName)){
 			sqlFrom = sqlFrom.concat(" AND u.name = ?");
 			params.add(userName);
 		}
	    
	    System.out.println(start);
	    System.out.println(end);
	    
	    sql.append(sqlSelectBase+sqlSelect_effective_user_account+sqlSelect_max_invest_amount+sqlSelect_new_user_invest_amount);
	    sql.append(sqlSelect_invest_1+sqlSelect_invest_2+sqlSelect_invest_3+sqlSelect_invest_4+sqlSelect_invest_5+sqlSelect_invest_6+sqlSelect_invest_12);
	    sql.append(sqlFrom).append(sqlGroupBy).append(sqlOrderBy+sqlOrderBy_default);
	    
	    String sqlList = sql.toString();
	    if(StringUtils.isBlank(bidTag)){
	    	sqlList = StringUtils.replace(sqlList, "TAG", " 1 = 1");
	    }
 		
	    System.out.println(sqlList);
 		return JPAUtil.getList(new ErrorInfo(), sqlList, params.toArray());
 	}
 	
 	public static List<Map<String, Object>> findUserProfit(String start,String end, String userName, String bidTag, String orderBy){
 		Logger.info("start:"+start+"; end:"+end+"; userName:"+userName+"; bigTag:"+bidTag+"; orderBy:"+orderBy);
 		StringBuffer sql = new StringBuffer();
 		String sqlSelectBase = "SELECT"
					+" uci.user_id,"
					+" u.name,uci.count as spread_user_account,";
 		
 		String sqlSelect_effective_user_account = " (select count(DISTINCT user_id) from t_user_cps_profit cp where cp.recommend_user_id = uci.user_id and invest_time >= ? and invest_time <=?) as effective_user_account,";
 		String sqlSelect_max_invest_amount = " (SELECT IFNULL(amount,0) FROM t_invests i WHERE i.user_id = uci.user_id AND time > ? and time < ? ORDER BY amount DESC LIMIT 1) AS max_invest_amount,";
 		String sqlSelect_new_user_invest_amount = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 1 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ?) AS new_user_invest_amount,";
 		String sqlSelect_invest_1 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND b.period = 1 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_1,";
 		String sqlSelect_invest_2 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 2 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_2,";
 		String sqlSelect_invest_3 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 3 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_3,";
 		String sqlSelect_invest_4 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 4 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_4,";
 		String sqlSelect_invest_5 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 5 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_5,";
 		String sqlSelect_invest_6 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 6 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_6,";
 		String sqlSelect_invest_12 = " (SELECT IFNULL(SUM(cp.invest_corpus), 0) FROM t_user_cps_profit cp,t_bids b WHERE b.id = cp.bid_id AND ifnull(b.is_only_new_user,0) = 0 AND  b.period = 12 AND cp.recommend_user_id = uci.user_id AND ins_dt > ? and ins_dt < ? AND TAG) AS invest_12";
 		
 		String sqlFrom = " FROM (SELECT recommend_user_id as user_id,count(1) as count from t_users where recommend_user_id>0 "
 					+ " and recommend_time >= ? and recommend_time<=? GROUP BY recommend_user_id) uci"
					+" LEFT JOIN t_users u ON u.id = uci.user_id"
 					+" where 1 = 1";
					/*+ "uci.year = 2016 AND uci.month = 11"*/
 		
 		String sqlGroupBy = " GROUP BY uci.user_id";
 		String sqlOrderBy = " ORDER BY ";
 		String sqlOrderBy_default = "(invest_1+invest_2+invest_3+invest_4+invest_5+invest_6+invest_12) DESC";
 		sqlOrderBy_default = StringUtils.isNotBlank(orderBy) ? orderBy : sqlOrderBy_default;
 		
 		if(StringUtils.isBlank(start)){
 			start = "2016-01-01 00:00:00";
 		}else{
 			start += " 00:00:00";
 		}
 		if(StringUtils.isBlank(end)){
 			end = "2088-01-01 00:00:00";
 		}else{
 			end += " 23:59:59";
 		}
 		
 		List<Object> params = new ArrayList<Object>();
 		
 		params.add(start);
		params.add(end);
		
		params.add(start);
		params.add(end);
		params.add(start);
		params.add(end);
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_1 = StringUtils.replace(sqlSelect_invest_1, "TAG", " b.tag = ?");
			System.out.println(sqlSelect_invest_1);
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_2 = StringUtils.replace(sqlSelect_invest_2, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_3 = StringUtils.replace(sqlSelect_invest_3, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_4 = StringUtils.replace(sqlSelect_invest_4, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_5 = StringUtils.replace(sqlSelect_invest_5, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_6 = StringUtils.replace(sqlSelect_invest_6, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
		if(StringUtils.isNotBlank(bidTag)){
			sqlSelect_invest_12 = StringUtils.replace(sqlSelect_invest_12, "TAG", " b.tag = ?");
			params.add(bidTag);
		}
		
		params.add(start);
		params.add(end);
 		if(StringUtils.isNotBlank(userName)){
 			sqlFrom = StringUtils.replace(sqlFrom, "1 = 1", " u.name = ?");

 			params.add(userName);
 		}
	    
	    System.out.println(start);
	    System.out.println(end);
	    
	    sql.append(sqlSelectBase+sqlSelect_effective_user_account+sqlSelect_max_invest_amount+sqlSelect_new_user_invest_amount);
	    sql.append(sqlSelect_invest_1+sqlSelect_invest_2+sqlSelect_invest_3+sqlSelect_invest_4+sqlSelect_invest_5+sqlSelect_invest_6+sqlSelect_invest_12);
	    sql.append(sqlFrom).append(sqlGroupBy).append(sqlOrderBy+sqlOrderBy_default);
	    
	    String sqlList = sql.toString();
	    if(StringUtils.isBlank(bidTag)){
	    	sqlList = StringUtils.replace(sqlList, "TAG", " 1 = 1");
	    }
 		
	    System.out.println(sqlList);
	    List<Map<String, Object>> lists = JPAUtil.getList(new ErrorInfo(), sqlList, params.toArray());
	    String first_user_sql = "SELECT count(1) from( SELECT user_id FROM t_user_cps_profit cp where cp.recommend_user_id = ? and invest_time >= ? and invest_time <=? GROUP BY user_id HAVING count(1) =1) fu";
	    String more_user_sql = "SELECT count(1) from( SELECT user_id FROM t_user_cps_profit cp where cp.recommend_user_id = ? and invest_time >= ? and invest_time <=? "
	    		+ " and bid_id <>(select bid_id from t_user_cps_profit cp1 where cp.user_id = cp1.user_id order by id limit 1) GROUP BY user_id HAVING count(1) >1) fu";
	    if(lists != null && lists.size() > 0){
 			EntityManager em = JPA.em();
 			for(Map<String,Object> map : lists){
 	            Query queryCount = em.createNativeQuery(first_user_sql);
 	            queryCount.setParameter(1, map.get("user_id"));
 	            queryCount.setParameter(2, start);
 	            queryCount.setParameter(3, end);
 	            int first_user_count =  Convert.strToInt(queryCount.getResultList().get(0)+"",0);
 				map.put("first_user_count", first_user_count);
 				
 	            queryCount = em.createNativeQuery(more_user_sql);
 	            queryCount.setParameter(1, map.get("user_id"));
	            queryCount.setParameter(2, start);
	            queryCount.setParameter(3, end);
	            int more_user_count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
 				map.put("more_user_count", more_user_count);
 			}
 		}
	    return lists;
 	}
 	
 	/**
 	 * 
 	 * @author xinsw
 	 * @creationDate 2017年8月1日
 	 * @description 加息汇总表
 	 * @param keyword 标的名称/借款人真实姓名
 	 * @param fkStart 放款日期-开始
 	 * @param fkEnd 放款日期-结束
 	 * @param fxStart 下次付息日期-开始
 	 * @param fxEnd 下次付息日期-结束
 	 * @param statusStr 标的状态
 	 * @param orderTypeStr
 	 * @param currPageStr
 	 * @param pageSizeStr
 	 * @param isExport
 	 * @param error
 	 * @return
 	 */
 	public static PageBean<Map<String, Object>> findIncreaseInterest(String keyword,String fkStart,String fkEnd,String fxStart,String fxEnd,String statusStr,
 			String orderTypeStr, String currPageStr, String pageSizeStr,String isExportStr, ErrorInfo error){
 		
 		int status = -100;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int orderType = 0;
 		int isExport = 0;
 		
 		if(NumberUtil.isNumericInt(statusStr)) {
 			status = Integer.parseInt(statusStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 			
 			if(orderType < 0 || orderType > 4) {
 	 			orderType = 0;
 	 		}
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(isExportStr)) {
 			isExport = Integer.parseInt(isExportStr);
 		}
 		
 		StringBuffer sql = new StringBuffer(" from t_bids b join t_users u on b.user_id = u.id join t_system_options g where b.is_increase_rate = 1 and g._key='loan_number'");
 		
 		List<Object> params = new ArrayList<Object>();
 		
 		StringBuffer _sql = new StringBuffer(" from t_bill_invests i join t_bids b on i.bid_id = b.id join t_users u on b.user_id = u.id where b.is_increase_rate = 1 ");
 		
 		StringBuffer next_sql = new StringBuffer(" from t_bids b join t_users u on b.user_id = u.id where b.is_increase_rate = 1 ");
 		
 		if(StringUtils.isNotBlank(keyword)){
 			sql.append(" and (instr(b.title,?) > 0 or instr(u.reality_name,?) > 0)");
 			_sql.append(" and (instr(b.title,?) > 0 or instr(u.reality_name,?) > 0)");
 			next_sql.append(" and (instr(b.title,?) > 0 or instr(u.reality_name,?) > 0)");
 			
 			params.add(keyword);
 			params.add(keyword);
 		}
 		
 		if(StringUtils.isNotBlank(fkStart)){
 			sql.append(" and b.audit_time >= ?");
 			_sql.append(" and b.audit_time >= ?");
 			next_sql.append(" and b.audit_time >= ?");
 			params.add(DateUtil.strDateToStartDate(fkStart));
 		}
 		
 		if(StringUtils.isNotBlank(fkEnd)){
 			sql.append(" and b.audit_time <= ?");
 			_sql.append(" and b.audit_time <= ?");
 			next_sql.append(" and b.audit_time <= ?");
 			params.add(DateUtil.strDateToEndDate(fkEnd));
 		}
 		if(status == -100){//全部
 			sql.append(" and b.status in(1,2,3,4,5,14)");
 			_sql.append(" and b.status in(1,2,3,4,5,14)");
 			next_sql.append(" and b.status in(1,2,3,4,5,14)");
 		}else if(status == -99){//热销中
			sql.append(" and b.status in(1,2)");
 			_sql.append(" and b.status in(1,2)");
 			next_sql.append(" and b.status in(1,2)");
 		}else if(status == -98){//回款中
			sql.append(" and b.status in(5,14)");
 			_sql.append(" and b.status in(5,14)");
 			next_sql.append(" and b.status in(5,14)");
 		}else if(status == -97){//其他
			sql.append(" and b.status in(0,10,11,12,20,21,22,-1,-2,-3,-4,-5,-10)");
 			_sql.append(" and b.status in(0,10,11,12,20,21,22,-1,-2,-3,-4,-5,-10)");
 			next_sql.append(" and b.status in(0,10,11,12,20,21,22,-1,-2,-3,-4,-5,-10)");
		}else{
			sql.append(" and b.status = ?");
 			_sql.append(" and b.status = ?");
 			next_sql.append(" and b.status = ?");
 			params.add(status);
		}
 		
 		String cntSql = "select count(1) as count " + sql.toString();
 		
 		if(StringUtils.isNotBlank(fxStart) || StringUtils.isNotBlank(fxEnd)){
 			sql.append(" group by b.id having 1=1 ");
 			
 			if(StringUtils.isNotBlank(fxStart)){
 	 			sql.append(" and nextTime >= ?");
 	 			_sql.append(" and (select receive_time from t_bill_invests where bid_id = b.id  and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) >= ?");
 	 			next_sql.append(" and (select receive_time from t_bill_invests where bid_id = b.id  and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) >= ?");
 	 			params.add(DateUtil.strDateToStartDate(fxStart));
 	 		}
 	 		
 	 		if(StringUtils.isNotBlank(fxEnd)){
 	 			sql.append(" and nextTime <= ?");
 	 			_sql.append(" and (select receive_time from t_bill_invests where bid_id = b.id  and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) <= ?");
 	 			next_sql.append(" and (select receive_time from t_bill_invests where bid_id = b.id  and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) <= ?");
 	 			params.add(DateUtil.strDateToEndDate(fxEnd));
 	 		}
 	 		
 	 		cntSql = "select count(1) as count from(SELECT b.id,(select receive_time from t_bill_invests where bid_id = b.id and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) as nextTime "
					+ sql.toString() + ") t";
 		}
		System.out.println(cntSql);
		
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, params.toArray());
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		double sumAmount = 0.0;
		String sumSql = "select sum(receive_increase_interest) as sumAmount " + _sql.toString();
		System.out.println(sumSql);
    	List<Map<String, Object>> amountMap = JPAUtil.getList(new ErrorInfo(), sumSql, params.toArray());
    	if(amountMap != null && amountMap.get(0) != null && amountMap.get(0).get("sumAmount") != null){
    		sumAmount = ((BigDecimal )amountMap.get(0).get("sumAmount")).doubleValue();
    	}
    	
		double nextAmount = 0.0;
		
		String nextSql = "select sum((select repayment_increase_interest from t_bills where bid_id = b.id and `status` in(-1,-2) ORDER BY periods limit 1)) as nextAmount "  + next_sql.toString();
    	List<Map<String, Object>> nextMap = JPAUtil.getList(new ErrorInfo(), nextSql, params.toArray());
    	if(nextMap != null && nextMap.get(0) != null && nextMap.get(0).get("nextAmount") != null){
    		nextAmount = ((BigDecimal )nextMap.get(0).get("nextAmount")).doubleValue();
    	}
		
    	String listSql = "select b.id,concat(`g`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bidNo`,b.title,b.`status`,b.audit_time as auditTime,u.`reality_name` AS realityName,u.mobile,"
			      + "(select sum(receive_increase_interest) from t_bill_invests where bid_id = b.id) as sumInterest,"
			      + "(select sum(real_increase_interest) from t_bill_invests where bid_id = b.id) as realInterest,"
			      + "(select count(1) from t_bills where bid_id = b.id and status in(-3,0)) as period,"
			      + "(select count(1) from t_bills where bid_id = b.id) as sumPeriod,b.increase_rate as increaseRate,"
			      + "(select receive_time from t_bill_invests where bid_id = b.id and `status` in(-1,-2,-5,-6) ORDER BY id limit 1) as nextTime,"
			      + "(select repayment_increase_interest from t_bills where bid_id = b.id and `status` in(-1,-2) ORDER BY periods limit 1) as nextAmt ";
    	
		listSql += sql.toString();
 		
 		if(orderType != 0) {
			listSql += Constants.INCREASE_SUM_ORDER_CONDITION[orderType];
		}else {
			listSql += " ORDER BY b.ID DESC ";
		}
 		
 		if(isExport != Constants.IS_EXPORT){
    		listSql += " limit ?,?";
    		params.add((currPage - 1) * pageSize);
    		params.add(pageSize);
    	}
 		
    	System.out.println(listSql);
    	System.out.println(params.size());
    	List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, params.toArray());
    	
    	if(list != null && list.size() > 0){
    		for(Map<String,Object> map : list){
    			double sumInterest = 0.0,realInterest = 0.0;
    			if(map.get("sumInterest") != null){
    				sumInterest = ((BigDecimal) map.get("sumInterest")).doubleValue();
    			}
    			if(map.get("realInterest") != null){
    				realInterest = ((BigDecimal) map.get("realInterest")).doubleValue();
    			}
    			map.put("toPaid", sumInterest - realInterest);
    			
    			int st = ((Byte)map.get("status")).intValue();
				String statusValue = "";
				String remark = "";
				if(st == 1){
					statusValue = "提前借款";
				}else if(st == 2){				
					statusValue = "借款中";
				}else if(st == 3){				
					statusValue = "待放款";
				}else if(st == 4){				
					statusValue = "还款中";
				}else if(st == 5){				
					statusValue = "已还款";
				}else if(st == 14){				
					statusValue = "本金垫付还款中";
				}else{
					statusValue = "其他";
					switch (st) {
					case 0:
						remark = "审核中";
						break;
					case 10:
						remark = "审核中待验证";
						break;
					case 11:
						remark = "提前借款待验证";
						break;
					case 12:
						remark = "借款中待验证";
						break;
					case 20:
						remark = "审核中待支付投标奖励";
						break;
					case 21:
						remark = "提前借款待支付投标奖励";
						break;
					case 22:
						remark = "借款中待支付投标奖励";
						break;
					case -1:
						remark = "审核不通过";
						break;
					case -2:
						remark = "借款中不通过";
						break;
					case -3:
						remark = "放款不通过";
						break;
					case -4:
						remark = "流标";
						break;
					case -5:
						remark = "撤销";
						break;
					case -10:
						remark = "未验证";
						break;
					default:
						break;
					}
				}
				map.put("statusValue", statusValue);
				map.put("remark", remark);
				map.put("periods", map.get("period") + "/" + map.get("sumPeriod"));
				
				if(isExport == 1){
					map.put("auditTime", DateUtil.dateToString((Date) map.get("auditTime")));
					map.put("nextTime", DateUtil.dateToString((Date) map.get("nextTime")));
					String title = (String) map.get("title");
					if(StringUtils.isBlank(title)){
						map.put("title", "");
					}
				}
    		}
    	}
    	
    	Map<String,Object> conditionMap = new HashMap<String, Object>();
    	
    	conditionMap.put("fkStart", fkStart);
		conditionMap.put("fkEnd", fkEnd);
		conditionMap.put("fxStart", fxStart);
		conditionMap.put("fxEnd", fxEnd);
		conditionMap.put("keyword", keyword);
		conditionMap.put("status", status);
		conditionMap.put("currPage", currPage);
		conditionMap.put("pageSize", pageSize);
		conditionMap.put("orderType", orderType);
		conditionMap.put("nextAmount", nextAmount);
		conditionMap.put("sumAmount", sumAmount);
		
 		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = list;
 		
        return page;
 	}
 	
 	/**
 	 * 
 	 * @author xinsw
 	 * @creationDate 2017年8月2日
 	 * @description 加息明细
 	 * @param keyword
 	 * @param start
 	 * @param end
 	 * @param statusStr
 	 * @param overduestr
 	 * @param orderTypeStr
 	 * @param currPageStr
 	 * @param pageSizeStr
 	 * @param isExportStr
 	 * @param error
 	 * @return
 	 */
 	public static PageBean<Map<String, Object>> findIncreaseInterestDetail(String keyword,String start,String end,String statusStr,
 			String overduestr,String orderTypeStr,String currPageStr, String pageSizeStr,String realReceiveTimeStart,String realReceiveTimeEnd,String isExportStr,ErrorInfo error){
 		int status = 0;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int orderType = 0;
 		int isExport = 0;
 		int overdue = 0;
 		
 		if(NumberUtil.isNumericInt(statusStr)) {
 			status = Integer.parseInt(statusStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 			if(orderType < 0 || orderType > 2) {
 	 			orderType = 0;
 	 		}
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(isExportStr)) {
 			isExport = Integer.parseInt(isExportStr);
 		}
 		
 		if(NumberUtil.isNumericInt(overduestr)) {
 			overdue = Integer.parseInt(overduestr);
 		}
 		
 		StringBuffer sql = new StringBuffer("from t_bill_invests i join t_bids b on i.bid_id = b.id join t_users u1 on i.user_id = u1.id join t_users u2 on b.user_id = u2.id where b.is_increase_rate = 1");
 		
 		List<Object> params = new ArrayList<Object>();
 		
 		if(StringUtils.isNotBlank(keyword)){
 			sql.append(" and (instr(b.title,?) > 0 or instr(u2.reality_name,?) > 0)");
 			params.add(keyword);
 			params.add(keyword);
 		}
 		if(StringUtils.isNotBlank(start)){
 			sql.append(" and i.receive_time >= ?");
 			params.add(DateUtil.strDateToStartDate(start));
 		}
 		
 		if(StringUtils.isNotBlank(end)){
 			sql.append(" and i.receive_time <= ?");
 			params.add(DateUtil.strDateToEndDate(end));
 		}
 		if(status != 0 && overdue != 0){
 			if(status == 1){
 				if(overdue == 1){
 					sql.append(" and i.status = -4");
 				}else{
 					sql.append(" and i.status in(-3,0)");
 				}
 			}
 			if(status == 2){
 				if(overdue == 1){
 					sql.append(" and i.status in(-2,-6)");
 				}else{
 					sql.append(" and i.status in(-1,-5)");
 				}
 			}
 		}else{
 			if(status == 1){
 	 			sql.append(" and i.status in(-4,-3,0)");
 	 		}
 	 		if(status == 2){
 	 			sql.append(" and i.status in(-1,-2,-5,-6)");
 	 		}
 	 		if(overdue == 1){
 	 			sql.append(" and i.status in(-2,-4,-6)");
 	 		}
 	 		if(overdue == 2){
 	 			sql.append(" and i.status not in(-2,-4,-6)");
 	 		}
 		}
 		
 		if(StringUtils.isNotBlank(realReceiveTimeStart)) {
			sql.append(" and real_receive_time >= ? ");
			params.add(DateUtil.strDateToStartDate(realReceiveTimeStart));
		}
 		
 		if(StringUtils.isNotBlank(realReceiveTimeEnd)) {
			sql.append(" and real_receive_time <= ? ");
			params.add(DateUtil.strDateToEndDate(realReceiveTimeEnd));
		}
 		
 		String cntSql = "select count(1) as count " + sql.toString();
 		
		System.out.println(cntSql);
		
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, params.toArray());
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		double sumAmount = 0.0;
		String sumSql = "select sum(receive_increase_interest) as sumAmount " + sql.toString();
		System.out.println(sumSql);
    	List<Map<String, Object>> amountMap = JPAUtil.getList(new ErrorInfo(), sumSql, params.toArray());
    	if(amountMap != null && amountMap.get(0) != null && amountMap.get(0).get("sumAmount") != null){
    		sumAmount = ((BigDecimal )amountMap.get(0).get("sumAmount")).doubleValue();
    	}
    	
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
    	
    	conditionMap.put("start", start);
		conditionMap.put("end", end);
		conditionMap.put("keyword", keyword);
		conditionMap.put("status", status);
		conditionMap.put("currPage", currPage);
		conditionMap.put("pageSize", pageSize);
		conditionMap.put("orderType", orderType);
		conditionMap.put("sumAmount", sumAmount);
		conditionMap.put("overdue", overdue);
		conditionMap.put("realReceiveTimeStart", realReceiveTimeStart);
		conditionMap.put("realReceiveTimeEnd", realReceiveTimeEnd);
		
		
		String listSql = "select i.id,concat((select _value from t_system_options where _key='increase_interest_number'),cast(i.id as char charset utf8)) AS jxNo,"
				+ "concat((select _value from t_system_options where _key='invests_bill_number'),cast(i.id as char charset utf8)) AS investNo,"
				+ "u1.name as investName,i.periods as period,(select count(1) from t_bills where bid_id = b.id) as sumPeriod,"
				+ "i.receive_increase_interest as receiveIncreaseInterest,i.status," 
				+ "b.title,concat((select _value from t_system_options where _key='loan_number'),cast(b.id as char charset utf8)) AS bidNo,"
				+ "u2.reality_name as realityName,receive_time as receiveTime,real_receive_time as realReceiveTime ";
  	
		listSql += sql.toString();
		
		if(orderType != 0) {
			listSql += Constants.INCREASE_DETAIL_ORDER_CONDITION[orderType];
		}else {
			listSql += " ORDER BY i.ID DESC ";
		}
		
		if(isExport != Constants.IS_EXPORT){
	  		listSql += " limit ?,?";
	  		params.add((currPage - 1) * pageSize);
	  		params.add(pageSize);
	  	}
			
	  	System.out.println(listSql);
	  	System.out.println(params.size());
	  	List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, params.toArray());
	  	
	  	if(list != null && list.size() > 0){
	  		for(Map<String,Object> map : list){
	  			double sumInterest = 0.0,realInterest = 0.0;
	  			if(map.get("sumInterest") != null){
	  				sumInterest = ((BigDecimal) map.get("sumInterest")).doubleValue();
	  			}
	  			if(map.get("realInterest") != null){
	  				realInterest = ((BigDecimal) map.get("realInterest")).doubleValue();
	  			}
	  			map.put("toPaid", sumInterest - realInterest);
	  			
	  			int st = ((Byte)map.get("status")).intValue();
	  			String statusValue = "";
	  			String overdueValue = "";
	  			if(st == -4 || st == -3 || st == 0){
	  				statusValue = "已付";
	  			}else{
	  				statusValue = "未付";
	  			}
	  			if(st == -2 || st == -4 || st == -6){
	  				overdueValue = "是";
	  			}else{
	  				overdueValue = "否";
	  			}
				map.put("statusValue", statusValue);
				map.put("overdueValue", overdueValue);
				map.put("remark", "");
				
				map.put("periods", map.get("period") + "/" + map.get("sumPeriod"));
				
				if(isExport == 1){
					map.put("receiveTime", DateUtil.dateToString((Date) map.get("receiveTime")));
					map.put("realReceiveTime", DateUtil.dateToString((Date) map.get("realReceiveTime")));
				}
	  		}
	  	}
  	
 		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = list;
 		
        return page;
 	}	
 	/**
	 * 查询对应标的的所有投资账单
	 * @param bidId
	 * @return
	 */
	public static List<t_bill_invests> queryBillInvestsByInvestId(long investId) throws Exception{
		List<t_bill_invests> t_bill_invest_list = null;

		String query = "select bill_invest from t_bill_invests bill_invest where bill_invest.invest_id=?  order by bill_invest.periods";
		try {
			t_bill_invest_list = t_bill_invests.find(query, investId).fetch();
			//t_bill_invest_list = t_bill_invests.find("invest_id", investId).fetch();
		} catch (Exception e) {
			Logger.error("查询投资"+investId+"的所有投资账单:" + e.getMessage());
			throw e;
		}
		
		if(null == t_bill_invest_list){
			return null;
		}
		
		return t_bill_invest_list;
	}
	
	/**
	 * 查询 投资,周期 对应的投资账单
	 * @param bidId
	 * @return
	 */
	public static t_bill_invests getBillInvestsByInvestIdAndPeriods(long investId,int periods) throws Exception{
		List<t_bill_invests> t_bill_invest_list = null;

		try {
			t_bill_invest_list = t_bill_invests.find("invest_id = ? and periods = ? ", investId,periods).fetch();
		} catch (Exception e) {
			Logger.error("查询投资"+investId+"的所有投资账单:" + e.getMessage());
			throw e;
		}
		
		if(null == t_bill_invest_list){
			return null;
		}
		if(t_bill_invest_list.size()!=1){
			return null;
		}
		
		return t_bill_invest_list.get(0);
	}
	/**
	 * 查询 借款账单 对应的所有投资账单
	 * @param bidId
	 * @return
	 */
	public static List<t_bill_invests> getBillInvestsByBidIdAndPeriod(long bidId,int period) throws Exception{
		List<t_bill_invests> t_bill_invest_list = null;

		try {
			t_bill_invest_list = t_bill_invests.find(" bid_id = ? and periods = ? order by invest_id ", bidId,period).fetch();
		} catch (Exception e) {
			Logger.error("查询标的"+bidId+"的第"+period+"期所有投资账单:" + e.getMessage());
			throw e;
		}
		
		if(null == t_bill_invest_list){
			return null;
		}

		return t_bill_invest_list;
	}
	
	/**
	 * 查询 借款账单 对应的所有投资账单
	 * @param Map<String(),t_bill_invests>
	 * @return
	 */
	public static Map<String,t_bill_invests> getInvestId_billInvestMapByBidIdAndPeriod(long bidId,int period) throws Exception{
		Map<String,t_bill_invests> investId_billInvestMap=new HashMap<String,t_bill_invests>();
		List<t_bill_invests> t_bill_invest_list=null;
		try {
			t_bill_invest_list=getBillInvestsByBidIdAndPeriod(bidId,period);
		} catch (Exception e) {
			Logger.error("查询标的"+bidId+"的第"+period+"期所有投资账单:" + e.getMessage());
			throw e;
		}
		
		if(null == t_bill_invest_list){
			return null;
		}
		for(t_bill_invests billInvest:t_bill_invest_list){
			investId_billInvestMap.put(billInvest.invest_id+"", billInvest);
		}
		
		return investId_billInvestMap;
	}
	
	/**
	 * 根据还款方式得到投资账单的还款信息 （供生成借款合同使用）
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param investId    投资id
	 * @param paymentType 还款方式 参考 t_dict_bid_repayment_types表 内容
	 * @return
	 * @throws Exception
	 * @author: zj
	 */
	public static BillInvests findBillInfo(int investId, int paymentType) throws Exception {
		ErrorInfo errorInfo=new ErrorInfo();
		String sql = "";
		if (PaymentTypeEnum.DQHB.getCode() == paymentType) {// 到期还本
			sql = " 	select b.receive_interest,b.receive_corpus as  all_amount  from t_bill_invests b   "
					+ "where  b.invest_id=?  order by b.periods  desc ";
		} else {// 等额本息
			sql = " select (b.receive_corpus+b.receive_interest) as  all_amount  from t_bill_invests b   where    b.invest_id=?  order by b.periods  desc ";
		}
		List<Map<String, Object>> list = JPAUtil.getList(errorInfo, sql, investId);
		if (!CollectionUtils.isEmpty(list)) {
			Map<String, Object> map = list.get(0);
			BillInvests billInvests = new BillInvests();
			billInvests.receiveInterest = Double.valueOf(EmptyUtil.obj20(map.get("receive_interest")).toString());
			billInvests.allAmount = Double.valueOf(EmptyUtil.obj20(map.get("all_amount")).toString());
			return billInvests;
		}
		return null;
	}

}
