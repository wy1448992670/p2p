package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import models.invest_quota;
import models.t_bid_publish;
import models.t_bid_risk;
import models.t_bid_user_risk;
import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_debt_bill_invest;
import models.t_invest_transfers;
import models.t_invests;
import models.t_product_audit_items;
import models.t_red_packages_history;
import models.t_red_packages_type;
import models.t_system_options;
import models.t_user_attention_bids;
import models.t_user_audit_items;
import models.t_user_automatic_bid;
import models.t_user_automatic_invest_options;
import models.t_user_risk;
import models.t_users;
import models.v_bill_board;
import models.v_confirm_autoinvest_bids;
import models.v_debt_auction_records;
import models.v_front_all_bids;
import models.v_front_all_bids_v2;
import models.v_front_debt_bids;
import models.v_front_user_attention_bids;
import models.v_invest_records;
import models.v_receiving_invest_bids;
import models.v_user_for_details;
import models.v_user_success_invest_bids;
import models.v_user_waiting_full_invest_bids;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import payment.PaymentProxy;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.libs.WS;
import play.mvc.Scope.Params;
import services.activity.ActivityIncreaseRateService;
import sun.util.logging.resources.logging;
import utils.Arith;
import utils.CnUpperCaser;
import utils.Converter;
import utils.DataUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.JSONUtils;
import utils.NumberUtil;
import utils.PageBean;
import utils.PushMessage;
import utils.QueryUtil;
import utils.Security;
import utils.ServiceFee;

import com.shove.Convert;
import com.shove.security.Encrypt;
import com.sun.star.bridge.oleautomation.Decimal;
import com.timevale.tgtext.text.pdf.fo;
import com.timevale.tgtext.text.pdf.security.ae;

import business.Optimization.UserOZ;
import constants.Constants;
import constants.DealType;
import constants.IPSConstants;
import constants.IPSConstants.IPSOperation;
import constants.IPSConstants.IPSDealStatus;
import constants.MallConstants;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.Templets;
import constants.UserEvent;

/**
 * 投资业务实体类
 * 
 * @author lwh
 * @version 6.0
 * @created 2014-3-27 下午03:31:06
 */
public class Invest implements Serializable{
	private long _id;
	public long id;
	public String merBillNo;
	public String ipsBillNo;
	public long userId;
	public String userIdSign; // 加密ID
	public Date time;
	public long bidId;
	public double amount;
	public double fee;
	public int transferStatus;
	public String status;
	public long transfersId;
	public boolean isAutomaticInvest;
	public double redAmount;
	public User user;
	public Bid bid;

	public int client;//客户端    1 pc  2  app 3微信  4其他
	
	/**
	 * 获取加密投资者ID
	 * @return
	 */
	public String getUserIdSign() {
		return Security.addSign(this.userId, Constants.USER_ID_SIGN);
	}

	public void setUserId(long userId) {
		this.userId = userId;
		this.user = new User();
		this.user.id = userId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
		this.bid = new Bid();
		this.bid.id = bidId;
	}

	public static t_invests getModelByPessimisticWrite(long id) {
		return getModel(id,LockModeType.PESSIMISTIC_WRITE);
	}
	public static t_invests getModel(long id,LockModeType lockModeType) {
		try {
			return JPA.em().find(t_invests.class, id, lockModeType);
		} catch (Exception e) {
			Logger.error("投资->获得模型:" + e.getMessage());
			return null;
		}
	}
	

	public long getId() {
		return _id;
	}
	public void setId(long id) {
		this.setId(id, LockModeType.NONE);
	}
	public void setIdByPessimisticWrite(long id) {
		this.setId(id,LockModeType.PESSIMISTIC_WRITE);
	}
	
	
	public void setId(long id,LockModeType lockModeType) {
		
		t_invests invests= null;
		try {
			//invests = t_invests.findById(id);
			invests=JPA.em().find(t_invests.class, id, lockModeType);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
		}
		
		if(null==invests){
			this._id = -1;
			
			return;
		}
		this._id = invests.id;
		this.userId = invests.user_id;
		this.time = invests.time;
		this.bidId = invests.bid_id;
		this.amount = invests.amount;
		this.fee = invests.fee;
		this.transferStatus = invests.transfer_status;
		this.merBillNo = invests.mer_bill_no;
		this.client = invests.client;
		if(invests.transfer_status == 0){
			this.status = "正常";
		}
		
		if(invests.transfer_status == -1){
			this.status = "已转让出";
		}
		
		if(invests.transfer_status == 0){
			this.status = "转让中";
		}
		
		this.transfersId = invests.transfers_id;
		this.isAutomaticInvest = invests.is_automatic_invest;
		this.redAmount=invests.red_amount;
	}

	
	public Invest() {
		
	}
	
	/**
	 *针对某个标的投标记录
	 * @return
	 */
	public static PageBean<v_invest_records> queryBidInvestRecords(int currPage, int pageSize,long bidId,ErrorInfo error){
		
		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		List<v_invest_records> list = new ArrayList<v_invest_records>();
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_INVEST_RECORDS);
		sql.append(" and bid_id = ?");
		sql.append(" order by time desc");
		List<Object> params = new ArrayList<Object>();
		params.add(bidId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_invest_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            
			if (list != null && list.size() > 0) {
				for (v_invest_records r : list) {
					String mobile = r.mobile;
					if (StringUtils.isNotBlank(mobile)) {
						r.mobile = mobile.substring(0, 3) + "****" + mobile.substring(7, mobile.length());
					}
				}
			}
            
            pageBean.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return pageBean;
		}
		
		pageBean.page = list;
		error.code = 1;
		return pageBean;
		
	}
	
	
	
	
	/**
	 * 前台借款标条件分页查询
	 * @param currPage
	 * @param pageSize
	 * @param _apr
	 * @param _amount
	 * @param _loanSchedule
	 * @param _startDate
	 * @param _endDate
	 * @param _loanType
	 * @param _creditLevel
	 * @return
	 */
	public static PageBean<v_front_all_bids> queryAllBids(int showType, int currPage,int pageSize,String _apr,String _amount,String _loanSchedule,String _startDate,String _endDate,String _loanType,String minLevelStr,String maxLevelStr,String _orderType,String _keywords,String _bidPeriod,ErrorInfo error){
		
		int apr = 0;
		int amount = 0;
		int loan_schedule = 0;
		int orderType = 0;
		int product_id = 0;
		int minLevel = 0;
		int maxLevel = 0;
		
		int bidPeriod = 0;
		
		if (showType == Constants.SHOW_TYPE_1) {
			
			showType = 1;
		}
		
		if (showType == Constants.SHOW_TYPE_2) {
			
			showType = 2;
		}
		
		if (showType == Constants.SHOW_TYPE_3) {
			
			showType = 4;
		}
		
		
		List<v_front_all_bids> bidList = new ArrayList<v_front_all_bids>();
		PageBean<v_front_all_bids> page = new PageBean<v_front_all_bids>();

		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
        Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", _keywords);
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		//加息
//		sql.append("`t_bids`.`is_increase_rate` AS `isIncreaseRate`,`t_bids`.`increase_rate` AS `increaseRate`,`t_bids`.`increase_rate_name` AS `increaseRateName`, ");

		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(_apr) && StringUtils.isBlank(_amount) && StringUtils.isBlank(_loanSchedule) && StringUtils.isBlank(_startDate) && StringUtils.isBlank(_endDate) && StringUtils.isBlank(_loanType) && StringUtils.isBlank(minLevelStr) && StringUtils.isBlank(maxLevelStr) && StringUtils.isBlank(_orderType) &&  StringUtils.isBlank(_keywords) &&StringUtils.isBlank(_bidPeriod)){
			
			try {
				sql.append(" AND t_bids.show_type&?<>0");
				params.add(showType);
				
				sql.append(" order by IF(loan_schedule>=100,0,IF(is_only_new_user > 0, 2, 1)) DESC,id desc");

				Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            bidList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition2(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
		}
		
			if (NumberUtil.isNumericInt(_apr)) {
				apr = Integer.parseInt(_apr);
			}
			
			if (apr < 0 || apr > 4) {
				sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
			}
			
			if (NumberUtil.isNumericInt(_amount)) {
				amount = Integer.parseInt(_amount);
			}
			
			if(!StringUtils.isBlank(_keywords)){
				sql.append(" and (t_bids.title like ? or t_bids.id like ?) ");
				params.add("%"+_keywords+"%");
				_keywords = _keywords.replace(obj + "", "");
				params.add("%"+_keywords+"%");
			}
			
			if (amount < 0 || amount > 5) {
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[amount]);
			}
			
			if( NumberUtil.isNumericInt(_loanSchedule)) {
				 loan_schedule = Integer.parseInt(_loanSchedule);
			}
			
			 if(loan_schedule < 0 || loan_schedule > 4){
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
			 }else{
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
			 }
				 
			if (NumberUtil.isNumericInt(_loanType)) {
				 product_id = Integer.parseInt(_loanType);
				if(product_id > 0){
					if(product_id > 5){
						
						sql.append(" and t_products.id = ? and t_bids.is_show_member_bid=1 ");
						
						params.add(5);
						
					}else{
						
						if(product_id == 5){
							
							sql.append(" and t_products.id = ? and t_bids.is_show_member_bid=0 ");
							
						}else{
							
							sql.append(" and t_products.id = ? ");
						}
						params.add(product_id);
						
					}
					
				}
				
			}
			
			
		
			if(NumberUtil.isNumericInt(minLevelStr)){
				 minLevel = Integer.parseInt(minLevelStr);
				if(minLevel > 0){
					 sql.append(" AND t_users.credit_level_id = ?");
					 params.add(minLevel);
				}
				
			}
			

			if(NumberUtil.isNumericInt(maxLevelStr)){
				 maxLevel = Integer.parseInt(maxLevelStr);
				if(maxLevel > 0){
					 sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
					 params.add(maxLevel);
				}
				
			}
		
			if( !StringUtils.isBlank(_startDate) &&  !StringUtils.isBlank(_endDate)){
				 sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
				 params.add(DateUtil.strDateToStartDate(_startDate));
				 params.add(DateUtil.strDateToEndDate(_endDate));
			}
			
			sql.append(" AND t_bids.show_type&?<>0");
			params.add(showType);
			
			if(NumberUtil.isNumericInt(_bidPeriod)){
				
				 bidPeriod = Integer.parseInt(_bidPeriod);
				 
				 if(bidPeriod >0){
					 
					 sql.append(SQLTempletes.BID_PERIOD_CONDITION[bidPeriod]);
				 }
			}
			
			if(NumberUtil.isNumericInt(_orderType)){
				 orderType = Integer.parseInt(_orderType);
			}
			
			if(orderType < 0 || orderType > 10){
				sql.append(Constants.BID_ORDER_CONDITION[0]);
			}else{
				sql.append(Constants.BID_ORDER_CONDITION[orderType]);
			}
			
			
			
			conditionMap.put("apr", apr);
			conditionMap.put("amount", amount);
			conditionMap.put("loanSchedule", loan_schedule);
			conditionMap.put("startDate", _startDate);
			conditionMap.put("endDate", _endDate);
			conditionMap.put("minLevel", minLevel);
			conditionMap.put("maxLevel", maxLevel);
			conditionMap.put("orderType", orderType);
			conditionMap.put("loanType", product_id);
			
			conditionMap.put("bidPeriod", bidPeriod);
			
		try {
            Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition2(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}
		
		error.code = 1;
		error.msg = "查询成功";
		page.page = bidList;
		page.conditions = conditionMap;
		
		return page;
	}




	/**
	 * 前台借款标条件分页查询 pc端禁止投资添加
	 * @param currPage
	 * @param pageSize
	 * @param _apr
	 * @param _amount
	 * @param _loanSchedule
	 * @param _startDate
	 * @param _endDate
	 * @param _loanType
	 * @param _creditLevel
	 * @return
	 */
	public static PageBean<v_front_all_bids> queryAllOverBids(int showType, int currPage,int pageSize,String _apr,String _amount,String _loanSchedule,String _startDate,String _endDate,String _loanType,String minLevelStr,String maxLevelStr,String _orderType,String _keywords,String _bidPeriod,ErrorInfo error){

		int apr = 0;
		int amount = 0;
		int loan_schedule = 0;
		int orderType = 0;
		int product_id = 0;
		int minLevel = 0;
		int maxLevel = 0;

		int bidPeriod = 0;

		if (showType == Constants.SHOW_TYPE_1) {

			showType = 1;
		}

		if (showType == Constants.SHOW_TYPE_2) {

			showType = 2;
		}

		if (showType == Constants.SHOW_TYPE_3) {

			showType = 4;
		}


		List<v_front_all_bids> bidList = new ArrayList<v_front_all_bids>();
		PageBean<v_front_all_bids> page = new PageBean<v_front_all_bids>();

		EntityManager em = JPA.em();
		String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
		obj = obj == null ? "" : obj;

		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", _keywords);

		page.pageSize = pageSize;
		page.currPage = currPage;

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		//加息
//		sql.append("`t_bids`.`is_increase_rate` AS `isIncreaseRate`,`t_bids`.`increase_rate` AS `increaseRate`,`t_bids`.`increase_rate_name` AS `increaseRateName`, ");

		String sss = " if(t_bids.`status` >2 ,CASE when activity1.rate is null THEN CAST(t_bids.is_increase_rate AS signed) else 1 end, CAST(t_bids.is_increase_rate AS signed)) isIncreaseRate,if(t_bids.`status` >2 ,IFNULL(activity1.rate,`t_bids`.`increase_rate`),`t_bids`.`increase_rate`) AS `increaseRate`,if(t_bids.`status` >2 ,CASE when activity1.rate is null then IFNULL(`t_bids`.`increase_rate_name`,'') else activity1.`NAME` end,`t_bids`.`increase_rate_name`)  AS `increaseRateName`,`t_bids`.`id` AS `id`,t_bids.min_invest_amount as min_invest_amount,t_bids.average_invest_amount as average_invest_amount,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`show_type` AS `show_type`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`status` AS `status`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`period` AS `period`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,t_bids.repayment_time AS repayment_time,concat (( SELECT `t_system_options`.`_value` AS `_value` FROM `t_system_options` WHERE (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_bids`.`repayment_type_id` AS `repayment_type_id`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,t_users.credit_level_id AS credit_level_id,`t_bids`.`time` AS `time`, activity1.`name` name1,activity1.rate rate1,activity2.`name`  name2,activity2.rate rate2,activity3.`name` name3,activity3.rate rate3 from `t_bids` LEFT JOIN `t_products` ON `t_products`.`id` = `t_bids`.`product_id` LEFT JOIN t_users ON t_bids.user_id = t_users.id LEFT JOIN `t_agencies` ON `t_agencies`.`id` = `t_bids`.`agency_id` LEFT JOIN `t_dict_bid_repayment_types` ON `t_dict_bid_repayment_types`.`id` = `t_bids`.`repayment_type_id` LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =1 LIMIT 1) activity1 on true LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =2 LIMIT 1) activity2 on true LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =3 LIMIT 1) activity3 on true  where `t_bids`.`status` IN (1, 2, 3, 4, 5, 14) ";
		sql.append(sss);

		sql.append(" and t_bids.id <= 2310 ");

		List<Object> params = new ArrayList<Object>();

		if(StringUtils.isBlank(_apr) && StringUtils.isBlank(_amount) && StringUtils.isBlank(_loanSchedule) && StringUtils.isBlank(_startDate) && StringUtils.isBlank(_endDate) && StringUtils.isBlank(_loanType) && StringUtils.isBlank(minLevelStr) && StringUtils.isBlank(maxLevelStr) && StringUtils.isBlank(_orderType) &&  StringUtils.isBlank(_keywords) &&StringUtils.isBlank(_bidPeriod)){

			try {
				sql.append(" AND t_bids.show_type&?<>0");
				params.add(showType);

				sql.append(" order by IF(loan_schedule>=100,0,IF(is_only_new_user > 0, 2, 1)) DESC,id desc");

				Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
				for(int n = 1; n <= params.size(); n++){
					query.setParameter(n, params.get(n-1));
				}
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
				bidList = query.getResultList();

				page.totalCount = QueryUtil.getQueryCountByCondition2(em, sql.toString(), params);

			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
		}

		if (NumberUtil.isNumericInt(_apr)) {
			apr = Integer.parseInt(_apr);
		}

		if (apr < 0 || apr > 4) {
			sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
		}else{
			sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
		}

		if (NumberUtil.isNumericInt(_amount)) {
			amount = Integer.parseInt(_amount);
		}

		if(!StringUtils.isBlank(_keywords)){
			sql.append(" and (t_bids.title like ? or t_bids.id like ?) ");
			params.add("%"+_keywords+"%");
			_keywords = _keywords.replace(obj + "", "");
			params.add("%"+_keywords+"%");
		}

		if (amount < 0 || amount > 5) {
			sql.append(SQLTempletes.BID_AMOUNT_CONDITION[0]);// 全部范围
		}else{
			sql.append(SQLTempletes.BID_AMOUNT_CONDITION[amount]);
		}

		if( NumberUtil.isNumericInt(_loanSchedule)) {
			loan_schedule = Integer.parseInt(_loanSchedule);
		}

		if(loan_schedule < 0 || loan_schedule > 4){
			sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
		}else{
			sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
		}

		if (NumberUtil.isNumericInt(_loanType)) {
			product_id = Integer.parseInt(_loanType);
			if(product_id > 0){
				if(product_id > 5){

					sql.append(" and t_products.id = ? and t_bids.is_show_member_bid=1 ");

					params.add(5);

				}else{

					if(product_id == 5){

						sql.append(" and t_products.id = ? and t_bids.is_show_member_bid=0 ");

					}else{

						sql.append(" and t_products.id = ? ");
					}
					params.add(product_id);

				}

			}

		}



		if(NumberUtil.isNumericInt(minLevelStr)){
			minLevel = Integer.parseInt(minLevelStr);
			if(minLevel > 0){
				sql.append(" AND t_users.credit_level_id = ?");
				params.add(minLevel);
			}

		}


		if(NumberUtil.isNumericInt(maxLevelStr)){
			maxLevel = Integer.parseInt(maxLevelStr);
			if(maxLevel > 0){
				sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
				params.add(maxLevel);
			}

		}

		if( !StringUtils.isBlank(_startDate) &&  !StringUtils.isBlank(_endDate)){
			sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
			params.add(DateUtil.strDateToStartDate(_startDate));
			params.add(DateUtil.strDateToEndDate(_endDate));
		}

		sql.append(" AND t_bids.show_type&?<>0");
		params.add(showType);

		if(NumberUtil.isNumericInt(_bidPeriod)){

			bidPeriod = Integer.parseInt(_bidPeriod);

			if(bidPeriod >0){

				sql.append(SQLTempletes.BID_PERIOD_CONDITION[bidPeriod]);
			}
		}

		if(NumberUtil.isNumericInt(_orderType)){
			orderType = Integer.parseInt(_orderType);
		}

		if(orderType < 0 || orderType > 10){
			sql.append(Constants.BID_ORDER_CONDITION[0]);
		}else{
			sql.append(Constants.BID_ORDER_CONDITION[orderType]);
		}



		conditionMap.put("apr", apr);
		conditionMap.put("amount", amount);
		conditionMap.put("loanSchedule", loan_schedule);
		conditionMap.put("startDate", _startDate);
		conditionMap.put("endDate", _endDate);
		conditionMap.put("minLevel", minLevel);
		conditionMap.put("maxLevel", maxLevel);
		conditionMap.put("orderType", orderType);
		conditionMap.put("loanType", product_id);

		conditionMap.put("bidPeriod", bidPeriod);

		try {
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			for(int n = 1; n <= params.size(); n++){
				query.setParameter(n, params.get(n-1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			bidList = query.getResultList();

			page.totalCount = QueryUtil.getQueryCountByCondition2(em, sql.toString(), params);

		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}

		error.code = 1;
		error.msg = "查询成功";
		page.page = bidList;
		page.conditions = conditionMap;

		return page;
	}
	
	/**
	 * 理财首页用户收藏的所有借款标
	 */
	public static PageBean<v_front_user_attention_bids> queryAllCollectBids(long userId,int currPage,int pageSize,ErrorInfo error){
		
		List<v_front_user_attention_bids> bidList = new ArrayList<v_front_user_attention_bids>();
		PageBean<v_front_user_attention_bids> page = new PageBean<v_front_user_attention_bids>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_FRONT_USER_ATTENTION_BIDS);
		sql.append(" and t_user_attention_bids.user_id = ?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_front_user_attention_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return page;
		}
		error.code = 1;
		page.page = bidList;
		
		return page;
	}
	
	
	/**
	 * 查询用户所有投资记录
	 * @param userId
	 * @return
	 */
	public static PageBean<v_invest_records> queryUserInvestRecords(long userId, String currPageStr,String pageSizeStr,String typeStr,String paramter,ErrorInfo error){
		
		int type = 0;
		String [] typeCondition = {" and (t_invests.bid_id like ? or bid_user.name like ?)"," and  t_invests.bid_id like ? "," and bid_user.name like ? "};
		
		List<v_invest_records> investRecords = new ArrayList<v_invest_records>();
		PageBean<v_invest_records> page = new PageBean<v_invest_records>();
		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
		
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		
		//在嵌套查询语句中，后者查询的结果必须要有关键字select
		sql.append(SQLTempletes.V_INVEST_RECORDS);
		sql.append(" and t_invests.user_id=? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
        Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", paramter);
		
		if(typeStr == null && paramter == null){
			sql.append(" order by time desc");
			try {
	            Query query = em.createNativeQuery(sql.toString(),v_invest_records.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            investRecords = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				
				return page;
			}
			page.page = investRecords;
			page.pageSize = pageSize;
			page.currPage = currPage;
			error.code = 1;
			return page;
		}
		
		if(StringUtils.isNotBlank(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if(type == 0){
			sql.append(typeCondition[0]);
			params.add("%"+paramter+"%");
			paramter = paramter.replace(obj + "", "");
			params.add("%"+paramter+"%");
		}else{
			sql.append(typeCondition[type]);
			if(type == 1){
				paramter = paramter.replace(obj + "", "");
			}
			params.add("%"+paramter+"%");
		}
		
		sql.append(" order by time desc");
		try {
            Query query = em.createNativeQuery(sql.toString(),v_invest_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            investRecords = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return page;
		}
		conditionMap.put("type", type);
		
		page.conditions = conditionMap;
		page.page = investRecords;
		page.pageSize = pageSize;
		page.currPage = currPage;
		error.code = 1;
		return page;
	}
	
	/**
	 * 查询用户所有投资记录(AJAX)
	 * @param userId
	 * @return
	 */
	public static PageBean<v_invest_records> queryInvestRecords(long userId, String currPageStr, String pageSizeStr, ErrorInfo error){
        error.clear();
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.TWO;
		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		
		PageBean<v_invest_records> page = new PageBean<v_invest_records>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		
		List<v_invest_records> recordDetails = new ArrayList<v_invest_records>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_INVEST_RECORDS);
		sql.append(" and user_id = ? ");
		sql.append(" order by time desc");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_invest_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            recordDetails = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询查询用户所有投资记录时："+e.getMessage());
			error.code = -1;
			error.msg = "用户所有投资记录查询失败";
		}
	
        page.page = recordDetails;
        error.code = 1;
		return page;
	}
	
	/**
	 * 查询理财交易总数
	 * @param error
	 * @return
	 */
	public static long queryTotalInvestCount(ErrorInfo error) {
		error.clear();

		long count = 0;
		Object transferCount = 0;
		Object investCount = 0;
		
		String sqlTransferCount = "SELECT COUNT(1) FROM t_invest_transfers WHERE status = ?";
		String sqlInvestCount = "SELECT COUNT(1) FROM t_invests LEFT JOIN t_bids ON t_invests.bid_id = t_bids.id WHERE t_bids.status IN (?, ?, ?, ?, ?, ?)";
		
		EntityManager em = JPA.em();

		//债权转让总数
		try {
			transferCount = em.createNativeQuery(sqlTransferCount)
					.setParameter(1, Constants.DEBT_SUCCESS)
					.getSingleResult();
		} catch (Exception e) {
			Logger.info("查询理财交易总数(债权转让)时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询理财交易总数失败";
			
			return -1;
		}
		
		try {
			investCount = em.createNativeQuery(sqlInvestCount)
					.setParameter(1, Constants.BID_ADVANCE_LOAN)
					.setParameter(2, Constants.BID_FUNDRAISE)
					.setParameter(3, Constants.BID_EAIT_LOAN)
					.setParameter(4, Constants.BID_REPAYMENT)
					.setParameter(5, Constants.BID_REPAYMENTS)
					.setParameter(6, Constants.BID_COMPENSATE_REPAYMENT)
					.getSingleResult();
		} catch (Exception e) {
			Logger.info("查询理财交易总数（投资）时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询理财交易总数失败";
			
			return -1;
		}
		
		count = Convert.strToLong(transferCount.toString(), 0) + Convert.strToLong(investCount.toString(), 0);
		
		error.code = 1;
		return count;
	}
	
	/**
	 * 查询理财交易总金额
	 * @param error
	 * @return
	 */
	public static double queryTotalDealAmount(ErrorInfo error) {
		error.clear();
		
		Object[] objs;
		Object investAmount;
		Object transferAmount;
		double amount = 0;
		
		String sql = "SELECT transferAmount,investAmount FROM((SELECT SUM(debt_amount) AS transferAmount FROM t_invest_transfers WHERE `status` = ?) AS transferAmount,(SELECT SUM(amount) AS investAmount FROM t_invests WHERE bid_id IN (?, ?, ?, ?, ?, ?)) AS investAmount)";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, Constants.DEBT_SUCCESS).setParameter(2, Constants.BID_ADVANCE_LOAN).setParameter(3, Constants.BID_FUNDRAISE).setParameter(4, Constants.BID_EAIT_LOAN).setParameter(5, Constants.BID_REPAYMENT).setParameter(6, Constants.BID_REPAYMENTS).setParameter(7, Constants.BID_COMPENSATE_REPAYMENT);
		
		try {
			objs = (Object[]) query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询理财交易总金额失败";
			
			return -1;
		}
		
		if (null != objs && objs.length > 1) {
			transferAmount = objs[0];
			investAmount = objs[1];
			
			if (null != transferAmount && null != investAmount) {
				amount = Double.parseDouble(transferAmount.toString()) + Double.parseDouble(investAmount.toString());
			}
		}
		
		error.code = 1;
		return amount;
	}
	
	
	/**
	 * 关闭投标机器人
	 * @param robotId
	 * @param error
	 * @return
	 */
	public static int closeRobot(long userId, long robotId,ErrorInfo error){
		
		EntityManager em = JPA.em();
		int rows = 0;
		
		try {
			rows = em.createNativeQuery("update t_user_automatic_invest_options set status = 0 where id = ?").setParameter(1, robotId).executeUpdate();
		} catch (Exception e) {
			error.msg = "关闭投标机器人失败！";
			error.code = -1;
			
			return error.code;
		} 
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.msg = "关闭投标机器人失败！";
			error.code = -1;
			
			return error.code;
		}
		
		try {
			rows = em.createNativeQuery("update t_users set ips_bid_auth_no = ? where id = ?").setParameter(1, null).setParameter(2, userId).executeUpdate();
		} catch (Exception e) {
			error.msg = "关闭投标机器人失败！";
			error.code = -1;
			
			return error.code;
		} 
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.msg = "关闭投标机器人失败！";
			error.code = -1;
			
			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.CLOSE_ROBOT, "关闭投标机器人", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.msg = "关闭投标机器人成功！";
		error.code = 1;
		
		return error.code;
		
	}
	
	
	
	
	
	
	
	/**
	 * 设置或修改自动投标机器人
	 * @param userId
	 * @param bidAmount
	 * @param rateStart
	 * @param rateEnd
	 * @param deadlineStart
	 * @param deadlineEnd
	 * @param creditStart
	 * @param creditEnd
	 * @param remandAmount
	 * @param borrowWay
	 */
	public static int saveOrUpdateRobot(long userId, int validType, int validDate, double minAmount, double maxAmount, String bidAmount,String rateStart,String rateEnd,String deadlineStart,String deadlineEnd,String creditStart,String creditEnd,
			                      String remandAmount,String borrowWay,ErrorInfo error){
	
		error.clear();
		String sql = "select balance from t_users where id = ?";
		Double balance = 0.0;
		
		t_user_automatic_invest_options robot = null;
		
		try {
			balance = t_users.find(sql, userId).first();
			
			/*查询用户是否设置过自动投标*/
		    robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
		} catch (Exception e) {
			error.msg = "对不起！系统异常!";
			error.code = -1;
			
			return error.code;
		}
		
		if (validType != 0 && validType != 1) {
			error.msg = "非法参数";
			error.code = -1;
			
			return error.code;
		}
		
		if (validDate <= 0) {
			error.msg = "请选择有效期";
			error.code = -1;
			
			return error.code;
		}
		
		if (minAmount < IPSConstants.MIN_AMOUNT) {
			error.msg = "借款额度必须大于"+IPSConstants.MIN_AMOUNT;
			error.code = -1;
			
			return error.code;
		}
		
		if (minAmount > maxAmount) {
			error.msg = "最高借款额度不能小于最低借款额度";
			error.code = -1;
			
			return error.code;
		}
		
		if(robot == null){
			t_user_automatic_invest_options robotNew = new t_user_automatic_invest_options();
			robotNew.user_id = userId;
			robotNew.min_interest_rate = Double.parseDouble(rateStart);
			robotNew.max_interest_rate = Double.parseDouble(rateEnd);
			
			if(Double.parseDouble(rateEnd) < Double.parseDouble(rateStart)){
				error.msg = "对不起！您设置的利率上限不能小于利率下限！";
				error.code = -1;
				
				return error.code;
			}
			
			if(null != deadlineStart){
				robotNew.min_period = Integer.parseInt(deadlineStart);
			}
			
			if(null != deadlineEnd){
				robotNew.max_period = Integer.parseInt(deadlineEnd);
				
				if( Integer.parseInt(deadlineEnd) < Integer.parseInt(deadlineStart)){
					error.msg = "对不起！您设置的借款期限上限不能小于借款期限下限！";
					error.code = -2;
					
					return error.code;
				}
			}
			
			if(null != creditStart){
				robotNew.min_credit_level_id = Integer.parseInt(creditStart);
			}
			
			if(null != creditEnd){
				robotNew.max_credit_level_id = Integer.parseInt(creditEnd);
				
				if(Integer.parseInt(creditEnd) >= Integer.parseInt(creditStart)){
					error.msg = "对不起！您设置的最高信用等级不能低于最低信用等级！";
					error.code = -3;
				}
			}
			
			if(balance < Double.parseDouble(remandAmount)){
				error.msg = "对不起！您预留金额不能大于您的可用余额！";
				error.code = -4;
				return error.code;
			}
			
			if(Double.parseDouble(bidAmount) > balance){
				error.msg = "对不起！您设置的投标金额不能大于您的可用余额！";
				error.code = -5;
				return error.code;
			}
			
			if(Double.parseDouble(bidAmount) + Double.parseDouble(remandAmount) > balance){
				error.msg = "对不起！您设置的投标金额和投标金额总和不能大于您的可用余额！";
				error.code = -5;
				return error.code;
			}
			
			if(null == remandAmount){
				error.msg = "对不起！您预留金额不能为空！";
				error.code = -6;
				return error.code;
			}
			
			if(null == bidAmount){
				error.msg = "对不起！每次投标金额不能为空！";
				error.code = -7;
				return error.code;
			}
			
			if(null == borrowWay){
				error.msg = "对不起！借款类型不能为空！";
				error.code = -8;
				
				return error.code;
			}
			
			if(Double.parseDouble(bidAmount) < 0){
				error.msg = "对不起！您设置的投标金额应该大于0！";
				error.code = -9;
				return error.code;
			}
			
			if(0 > Double.parseDouble(remandAmount)){
				error.msg = "对不起！您预留金额不能小于0！";
				error.code = -10;
				return error.code;
			}
			
			robotNew.retention_amout = Double.parseDouble(remandAmount);
			robotNew.amount = Double.parseDouble(bidAmount);
			
			// modify by 2015-07-11
//			robotNew.status = Constants.IPS_ENABLE ? false : true;
			robotNew.status = true;
			robotNew.loan_type = borrowWay;
			robotNew.time = new Date();
			robotNew.valid_type = validType;
			robotNew.valid_date = validDate;
			robotNew.min_amount = minAmount;
			robotNew.max_amount = maxAmount;
			
			try {
				robotNew.save();
			} catch (Exception e) {
				error.msg = "对不起！本次设置投标机器人失败！请您重试！";
				error.code = -9;
				return error.code;
			}
			
		}else{
			
			robot.user_id = userId;
			robot.min_interest_rate = Double.parseDouble(rateStart);
			robot.max_interest_rate = Double.parseDouble(rateEnd);
			
			if(Double.parseDouble(rateEnd) < Double.parseDouble(rateStart)){
				error.msg = "对不起！您设置的利率上限不能小于利率下限！";
				error.code = -1;
				
				return error.code;
			}
			
			if(null != deadlineStart){
				robot.min_period = Integer.parseInt(deadlineStart);
			}
			
			if(null != deadlineEnd){
				robot.max_period = Integer.parseInt(deadlineEnd);
				
				if( Integer.parseInt(deadlineEnd) < Integer.parseInt(deadlineStart)){
					error.msg = "对不起！您设置的借款期限上限不能小于借款期限下限！";
					error.code = -2;
					
					return error.code;
				}
			}
			
			if(null != creditStart){
				robot.min_credit_level_id = Integer.parseInt(creditStart);
			}
			
			if(null != creditEnd){
				robot.max_credit_level_id = Integer.parseInt(creditEnd);
				
				if(Integer.parseInt(creditEnd) >= Integer.parseInt(creditStart)){
					error.msg = "对不起！最高信用等级不能低于最低信用等级！";
					error.code = -3;
					return error.code;
				}
			}
			
			if(balance < Double.parseDouble(remandAmount)){
				error.msg = "对不起！您预留金额不能大于您的可用余额！";
				error.code = -4;
				return error.code;
			}
			
			if(Double.parseDouble(bidAmount) > balance){
				error.msg = "对不起！您设置的投标金额不能大于您的可用余额！";
				error.code = -5;
				return error.code;
			}
			
			if(null == remandAmount){
				error.msg = "对不起！您预留金额不能为空！";
				error.code = -6;
				return error.code;
			}
			
			if(null == bidAmount){
				error.msg = "对不起！每次投标金额不能为空！";
				error.code = -7;
				return error.code;
			}
			
			if(null == borrowWay){
				error.msg = "对不起！借款类型不能为空！";
				error.code = -8;
				
				return error.code;
			}
			
			if(Double.parseDouble(bidAmount) < 0){
				error.msg = "对不起！您设置的投标金额应该大于0！";
				error.code = -9;
				return error.code;
			}
			
			if(0 > Double.parseDouble(remandAmount)){
				error.msg = "对不起！您预留金额不能小于0！";
				error.code = -10;
				return error.code;
			}
			
			robot.retention_amout = Double.parseDouble(remandAmount);
			robot.amount = Double.parseDouble(bidAmount);
			
			// modify by 2015-07-11
//			robot.status = Constants.IPS_ENABLE ? false : true;
			robot.status = true;
			robot.loan_type = borrowWay;
			robot.time = new Date();
			robot.valid_type = validType;
			robot.valid_date = validDate;
			robot.min_amount = minAmount;
			robot.max_amount = maxAmount;
			
			try {
				robot.save();
			} catch (Exception e) {
				error.msg = "对不起！本次设置投标机器人失败！请您重试！";
				error.code = -9;
				return error.code;
			}
			
			
		}
			
			DealDetail.userEvent(userId, UserEvent.OPEN_ROBOT, "开启投标机器人", error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "设置成功";
			error.code = 1;	
			return 1;
		
	}
	
	/**
	 * 获取用户投标机器人
	 * @param userId
	 * @return
	 */
	public static t_user_automatic_invest_options getUserRobot(long userId,ErrorInfo error){
		
		t_user_automatic_invest_options robot = null;
		
		try {
			robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return null;
		}
		
		error.code = 1;
		return robot;
	}
	
	
	public static double getUserBalance(long userId){
		
		double balance = 0;
		
		try {
			balance = t_users.find(" select balance from t_users where id = ? ", userId).first();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return balance;
	}
	
	
	/**
	 *检查贷款用户是否开启了投标机器人，贷款用户在获得贷款时会自动关闭自动投标，以避免借款被用作自动投标资金
	 * @param bidId
	 */
	public static int closeUserBidRobot(long userId) {
		
		if(0 == userId) return -1;
		
		ErrorInfo error = new ErrorInfo();
		Boolean status = null;
		String hql1 = "select status from t_user_automatic_invest_options  where user_id = ?";
		
		try {
			status = t_user_automatic_invest_options.find(hql1, userId).first();
		} catch (Exception e) {
			Logger.error("理财->查询开启自动投标状态:" + e.getMessage());
			
			return -2;
		}

		if(null == status) return 1;
		
		/* 表示投标机器人开启,关闭投标机器人 */
		if (status) {
			String hql2 = "update t_user_automatic_invest_options set status = ? where user_id = ?";
			
			Query query = JPA.em().createQuery(hql2);
			query.setParameter(1, Constants.NOT_ENABLE);
			query.setParameter(2, userId);

			try {
				return query.executeUpdate();
			} catch (Exception e) {
				Logger.error(e.getMessage());
				
				return -3;
			}
		}
		
		DealDetail.userEvent(userId, UserEvent.CLOSE_ROBOT, "关闭投标机器人", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		return 1;
	}
	
	public long investUserId;
	public double investAmount;
	
	/**
	 * 查询对应标的的所有投资者以及投资金额
	 * @param bidId
	 * @return
	 */
	public static List<Invest> queryAllInvestUser(long bidId) {
		List<Map<Long, Object>> tamounts = null;
		List<Invest> amounts = new ArrayList<Invest>();

		String hql = "select new Map(i.id as id,i.user_id as userId, i.amount as amount, i.mer_bill_no as mer_bill_no, i.ips_bill_no as ips_bill_no, i.fee as fee,i.client as client) from t_invests i where i.bid_id=?  order by time";

		try {
			tamounts = t_invests.find(hql, bidId).fetch();
		} catch (Exception e) {
			Logger.error("查询对应标的的所有投资者以及投资金额:" + e.getMessage());

			return null;
		}
		
		if(null == tamounts) 
			return null;
		
		if(tamounts.size() == 0){
			return amounts;
		}
		
		Invest invest = null;

		for (Map<Long, Object> map : tamounts) {
			invest = new Invest();
			invest.id =  Long.parseLong(map.get("id") + "");
			invest.investUserId = Long.parseLong(map.get("userId") + "");
			invest.investAmount = Double.parseDouble(map.get("amount") + "");
			invest.merBillNo = (String) map.get("mer_bill_no");
			invest.ipsBillNo = (String) map.get("ips_bill_no");
			invest.fee = Convert.strToDouble(""+map.get("fee"), 0);
			invest.client = Integer.valueOf(map.get("client").toString());
			amounts.add(invest);
		}

		return amounts;
	}
	
	/**
	 * 查询对应标的的所有投资者以及投资金额
	 * @param bidId
	 * @return
	 */
	public static List<Invest> queryAllInvestUserForInvitation(long bidId) {
		List<Map<Long, Object>> tamounts = null;
		List<Invest> amounts = new ArrayList<Invest>();
		
		String hql = "select new Map(i.user_id as userId, i.amount as amount, i.mer_bill_no as mer_bill_no, i.ips_bill_no as ips_bill_no, i.fee as fee) from t_invests i where i.bid_id=?  order by time";
		
		try {
			tamounts = t_invests.find(hql, bidId).fetch();
		} catch (Exception e) {
			Logger.error("查询对应标的的所有投资者以及投资金额:" + e.getMessage());
			
			return null;
		}
		
		if(null == tamounts) 
			return null;
		
		if(tamounts.size() == 0){
			return amounts;
		}
		
		Invest invest = null;
		
		for (Map<Long, Object> map : tamounts) {
			invest = new Invest();
			
			invest.investUserId = Long.parseLong(map.get("userId") + "");
			invest.investAmount = Double.parseDouble(map.get("amount") + "");
			invest.merBillNo = (String) map.get("mer_bill_no");
			invest.ipsBillNo = (String) map.get("ips_bill_no");
			invest.fee = Convert.strToDouble(""+map.get("fee"), 0);
			
			amounts.add(invest);
		}
		
		return amounts;
	}
	
	/**
	 * 查询投标信息
	 * @param bidId
	 * @return
	 */
	public static List<Map<Object, Object>> queryInvestInfo(long bidId, ErrorInfo error) {
		error.clear();
		String sql = "select new Map(u.id as userId, u.ips_acct_no as ipsAcctNo, i.amount as amount) from t_invests i, t_users u where i.bid_id=? and u.id = i.user_id order by i.time";

		try {
			return t_invests.find(sql, bidId).fetch();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";

			return null;
		}
	}
	
	/**
	 * 更新标的浏览次数
	 * @param bidId
	 */
	public static void updateReadCount(long bidId,ErrorInfo error){
		EntityManager em = JPA.em();
		/*增加该借款标浏览次数*/
		int rows = em.createQuery("update t_bids set read_count = read_count + 1 where id = ?").setParameter(1, bidId).executeUpdate();
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
		}
		
		error.code = 1;
	}
	
	
	
	/**
	 * 等待满标的理财标
	 * @param userId
	 * @param type 1:全部 2：标题 3：借款标编号
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_waiting_full_invest_bids> queryUserWaitFullBids(long userId,String typeStr,String param,int currPage,int pageSize,ErrorInfo error){
		PageBean<v_user_waiting_full_invest_bids> page = new PageBean<v_user_waiting_full_invest_bids>();
		List<v_user_waiting_full_invest_bids> bidList = new ArrayList<v_user_waiting_full_invest_bids>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_WAITING_FULL_INVEST_BIDS);
		sql.append(" and t_invests.user_id = ? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		int type = 0;
		
		String [] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) "," and t_bids.title like ? "," and t_invests.bid_id like ? "};

		if(StringUtils.isNotBlank(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if(type == 0){
			param = param == null?"":param;
			sql.append(typeCondition[0]);
			params.add("%"+param+"%");
			param = param.replace(obj + "", "");
			params.add("%"+param+"%");
		}else{
			sql.append(typeCondition[type]);
			if(type == 2){
				param = param.replace(obj + "", "");
			}
			params.add("%"+param+"%");
		}
		
		try {
            Query query = em.createNativeQuery(sql.toString(),v_user_waiting_full_invest_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return page;
		}
		
		conditionMap.put("type", type);
		
		page.page = bidList;
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.conditions = conditionMap;
		error.code = 1;
		return page;
	}
	
	/**
	 * 等待放款的理财标
	 * @param userId
	 * @param typeStr
	 * @param param
	 * @param currPage
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<v_user_waiting_full_invest_bids> queryUserReadyReleaseBid(long userId,String typeStr,String param,int currPage,int pageSize,ErrorInfo error){
		PageBean<v_user_waiting_full_invest_bids> page = new PageBean<v_user_waiting_full_invest_bids>();
		List<v_user_waiting_full_invest_bids> bidList = new ArrayList<v_user_waiting_full_invest_bids>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_INVEST_READY_RELEASE_BID);
		sql.append(" and t_invests.user_id = ? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		int type = 0;
		
		String [] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) "," and t_bids.title like ? "," and t_invests.bid_id like ? "};

		if(StringUtils.isNotBlank(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if(type == 0){
			param = param == null?"":param;
			sql.append(typeCondition[0]);
			params.add("%"+param+"%");
			param = param.replace(obj + "", "");
			params.add("%"+param+"%");
		}else{
			sql.append(typeCondition[type]);
			if(type == 2){
				param = param.replace(obj + "", "");
			}
			params.add("%"+param+"%");
		}
		
		try {
            Query query = em.createNativeQuery(sql.toString(),v_user_waiting_full_invest_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return page;
		}
		
		conditionMap.put("type", type);
		
		page.page = bidList;
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.conditions = conditionMap;
		error.code = 1;
		return page;
	}
	
	
	/**
	 * 查询用户所有投资成功的借款标
	 * @param userId
	 * @param type
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_success_invest_bids> queryUserSuccessInvestBids(long userId,String typeStr,String param,int currPage,int pageSize,ErrorInfo error){
		
		int type = 0;
		String [] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) "," and t_bids.title like ? "," and t_invests.bid_id like ? "};

		PageBean<v_user_success_invest_bids> page = new PageBean<v_user_success_invest_bids>();
		List<v_user_success_invest_bids> list = new ArrayList<v_user_success_invest_bids>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer("select count(1) from ((`t_invests` left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) left join `t_users` on((`t_users`.`id` = `t_bids`.`user_id`))) where (`t_bids`.`status` in (4,5,14) and (select count(1) from t_bill_invests bi where bi.invest_id = t_invests.id and  bi.`status` IN(-1 ,-2 ,-5 ,-6)) = 0 and (`t_invests`.`transfer_status` <> -(1)))");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_USER_SUCCESS_INVEST_BIDS);
		sql.append(" and t_invests.user_id=? ");
		sqlCount.append(" and t_invests.user_id=? ");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		
		if(typeStr == null && param == null){
			sql.append(" order by id desc");
			try {
	            Query query = em.createNativeQuery(sql.toString(),v_user_success_invest_bids.class);
	            Query queryCount = em.createNativeQuery(sqlCount.toString());
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	                queryCount.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            list = query.getResultList();
	            page.totalCount = Integer.parseInt(queryCount.getSingleResult().toString());
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return page;
			}
			
			page.page = list;
			error.code = 1;
			return page;
		}
		
		if(StringUtils.isNotBlank(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
			
		}
		
		if(type == 0){
			param = param == null?"":param;
			sql.append(typeCondition[0]);
			sqlCount.append(typeCondition[0]);
			params.add("%"+param+"%");
			param = param.replace(obj + "", "");
			params.add("%"+param+"%");
		}else{
			sql.append(typeCondition[type]);
			sqlCount.append(typeCondition[type]);
			if(type == 2){
				param = param.replace(obj + "", "");
			}
			params.add("%"+param+"%");
		}
		sql.append(" order by id desc");
		
		try {
            Query query = em.createNativeQuery(sql.toString(),v_user_success_invest_bids.class);
            Query queryCount = em.createNativeQuery(sqlCount.toString());
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
                queryCount.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            page.totalCount = Integer.parseInt(queryCount.getSingleResult().toString());
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			return page;
		}
			
		conditionMap.put("type", type);
		
		page.conditions = conditionMap;
		page.page = list;
		error.code = 1;	
		return page;
	}
	
	
	
	
	/**
	 *  查询用户收款中的理财标
	 * @param userId
	 * @param type 1:全部 2：标题 3：借款标编号
	 * @param params
	 * @param currPage
	 * @return
	 */
	 
	public static PageBean<v_receiving_invest_bids> queryUserAllReceivingInvestBids(long userId,String typeStr,String param,int currPage,int pageSize,ErrorInfo error){
		
		int type = 0;
		String [] typeCondition = {" and ( t_bids.title like ? or t_invests.bid_id like ?) "," and t_bids.title like ? "," and t_invests.bid_id like ? "};
		PageBean<v_receiving_invest_bids> page = new PageBean<v_receiving_invest_bids>();
		List<v_receiving_invest_bids> bidList = new ArrayList<v_receiving_invest_bids>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		StringBuffer sqlCount = new StringBuffer("select count(1) from ((`t_invests` left join `t_bids` on((`t_bids`.`id` = `t_invests`.`bid_id`))) left join `t_users` on((`t_users`.`id` = `t_bids`.`user_id`))) where ((`t_bids`.`status` in (4,14)) and ((select id from t_bill_invests bi where bi.invest_id = t_invests.id and bi.`status` in (-1,-2,-5,-6) LIMIT 1) is not null) and (`t_invests`.`transfer_status` <> -(1)))");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_RECEIVING_INVEST_BIDS);
		sql.append(" and t_invests.user_id = ?");
		sqlCount.append(" and t_invests.user_id = ?");
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		
		if(typeStr == null && param == null){
			try {
				sql.append(" order by id desc");
	            Query query = em.createNativeQuery(sql.toString(),v_receiving_invest_bids.class);
	            Query queryCount = em.createNativeQuery(sqlCount.toString());
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	                queryCount.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            bidList = query.getResultList();
	            page.totalCount = Integer.parseInt(queryCount.getSingleResult().toString());
	            		
//	            page.totalCount = ;//QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return page;
			}
			page.page = bidList;
			error.code = 1;
			return page;
		}
		
		if(StringUtils.isNotBlank(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if(type == 0){
			param = param == null?"":param;
			sql.append(typeCondition[0]);
			sqlCount.append(typeCondition[0]);
			params.add("%"+param+"%");
			param = param.replace(obj + "", "");
			params.add("%"+param+"%");
		}else{
			sql.append(typeCondition[type]);
			sqlCount.append(typeCondition[type]);
			if(type == 2){
				param = param.replace(obj + "", "");
			}
			params.add("%"+param+"%");
		}
		
		try {
			sql.append(" order by id desc");
            Query query = em.createNativeQuery(sql.toString(),v_receiving_invest_bids.class);
            Query queryCount = em.createNativeQuery(sqlCount.toString());
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
                queryCount.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = Integer.parseInt(queryCount.getSingleResult().toString());
//            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			return page;
		}
		
		conditionMap.put("type", type);
		
		page.conditions = conditionMap;
		page.page = bidList;
		error.code = 1;	
		return page;
	}
	
	/**
	 * 获取t_bids表特定标版本号
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int getBidVersion(long bidId,ErrorInfo error){
		
		int version = 0;
		String sql = "select version from t_bids where id = ?";
		
		try {
			version = t_bids.find(sql, bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -1;
			return -1;
		}
		error.code = 1;
		return version ;
	}
	
	/**
	 * 已投总额增加,投标进度增加
	 * @param bidId
	 * @param amount
	 * @param schedule
	 * @param error
	 * @return
	 */
	public static void updateBidschedule(long bidId,double amount, double schedule,ErrorInfo error){
		EntityManager em = JPA.em();
		int rows = 0;
		
		try {
			/*rows = em.createQuery("update t_bids set loan_schedule=?,has_invested_amount= has_invested_amount + ? where id=? and amount >= has_invested_amount + ?")
			.setParameter(1, schedule).setParameter(2, amount).setParameter(3, bidId).setParameter(4, amount).executeUpdate();*/
			rows = em.createQuery("update t_bids set loan_schedule = truncate((((has_invested_amount + ?)/ amount) * 100), 2) , has_invested_amount= has_invested_amount + ? where id=? and amount >= has_invested_amount + ?") 
					.setParameter(1, amount).setParameter(2, amount).setParameter(3, bidId).setParameter(4, amount).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("更新已投总额,投标进度时,%s", e.getMessage());
			
			error.code = -1;
			error.msg = "更新已投总额,投标进度异常";
			
			
			JPA.setRollbackOnly();
			return ;
		}
		
		if(rows == 0){
			Logger.info("更新已投总额,投标进度时,已满标");

			error.code = Constants.OVERBIDAMOUNT;
			error.msg = "超额投资，请解冻投资金额";
			
			JPA.setRollbackOnly();
			return ;
		}
		
		
		error.code = 1;
		
		return ;
	}
	
	/**
	 * 更新借款标满标时间
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int updateBidExpiretime(long bidId, double service_fees, ErrorInfo error){
		
		Bid bid = new Bid();
		bid.id = bidId;	
		EntityManager em = JPA.em();
		try {
			int rows = em.createQuery("update t_bids set real_invest_expire_time = ? where id=?").setParameter(1, new Date())
																								 .setParameter(2, bidId).executeUpdate();		
			if(rows == 0){
				JPA.setRollbackOnly();
				error.code = -1;
				return error.code;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}
		error.code = 1;
		return 1;
	}
	
	
	/**
	 * 根据投资ID获取对应bidId,userId
	 * @param investId
	 * @param error
	 * @return
	 */
	public static t_invests queryUserAndBid(long investId){
		t_invests invest = null;
		try {
			invest = t_invests.find("select new t_invests(user_id,bid_id) from t_invests where id = ?", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return invest;
	}
	
	/**
	 * 根据投资debt_transfer_id获取对应t_invests
	 * @param debt_transfer_id
	 * @param error
	 * @return
	 */
	public static t_invests getInvestByDebtTransferId(long debt_transfer_id){
		t_invests invest = null;
		try {
			invest = t_invests.find("select invest "
									+ "from t_invests invest "
									+ ",t_debt_transfer debtTransfer "
									+ "where debtTransfer.invest_id=invest.id and debtTransfer.id = ? ", debt_transfer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return invest;
	}
	
	/**
	 * 根据投资debt_transfer_id获取对应t_invests
	 * @param debt_transfer_id
	 * @param error
	 * @return
	 */
	public static Long getInvestIdByDebtTransferId(long debt_transfer_id){
		long investId = 0;
		try {
			String sql = "select invest.id "
						+ "from t_invests invest "
						+ ",t_debt_transfer debtTransfer "
						+ "where debtTransfer.invest_id=invest.id and debtTransfer.id = ? ";
			Query query = JPA.em().createQuery(sql).setParameter(1, debt_transfer_id);
			investId = (Long) query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return investId;
	}	
	
	/**
	 * 即时查询借款标对象
	 * @param bidId
	 * @return
	 */
	public static Map<String,String> bidMap(long bidId,ErrorInfo error){
		error.clear();
		
		String sql = "select t_bids.id, title, min_invest_amount, average_invest_amount, amount, status, " 
				+ "invest_expire_time, has_invested_amount, user_id, product_id, version, ips_status, "
				+ "period_unit, period, apr, td.description, IF(is_only_new_user,1,0) AS is_only_new_user from t_bids,t_dict_bid_repayment_types td "
				+ "where t_bids.id = ? AND td.id = repayment_type_id limit 1";
		
		Object [] obj = null;
		
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).setParameter(1, bidId).getSingleResult();
					
		} catch (Exception e) {
			
			e.printStackTrace();
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -11;

			JPA.setRollbackOnly();
		}
		
		if(obj == null || obj.length == 0) {
			error.msg = "标的信息不存在";
			error.code = -11;
			
			return null; 
		}
		
		Map<String,String> map = new HashMap<String, String>();
		map.put("id", obj[0]+"");
		map.put("title", obj[1]+"");
		map.put("min_invest_amount", obj[2]+"");
		map.put("average_invest_amount", obj[3]+"");
		map.put("amount", obj[4]+"");
		map.put("status", obj[5]+"");
		map.put("invest_expire_time", obj[6]+"");
		map.put("has_invested_amount", obj[7]+"");
		map.put("user_id", obj[8]+"");
		map.put("product_id", obj[9]+"");
		map.put("version", obj[10]+"");
		map.put("ips_status", obj[11]+"");
		map.put("period_unit", obj[12]+"");
		map.put("period", obj[13]+"");
		map.put("apr", DataUtil.formatString(obj[14]));
		map.put("description", obj[15]+"");
		map.put("is_only_new_user", obj[16]+"");
		error.code = 1;
		return map;
	}
	public double doubleValue(Object value){
		return value == null ? 0.00 : Double.valueOf(value.toString());
	}
	/**
	 * 投标操作
	 * @param userId 投资人id
	 * @param bidId 标的id
	 * @param investTotal 投资金额
	 * @param dealpwdStr 交易密码
	 * @param isAuto 是否为自动投标
	 * @param redPackage 红包
	 * @param error
	 * @return 
	 */
	public static int invest(long userId, long bidId, int investTotal, String dealpwdStr, boolean isAuto, int client,RedPackageHistory redPackage, ErrorInfo error) {
		error.clear();	
//		double redAmount = 0.0;//红包金额
//		if(null != redPackage){
//			if(client == Constants.CLIENT_APP){//只有在app端才能使用红包
//				redAmount  = redPackage.money;
//			}
//		}
	
		User user = new User();
    	user.id = userId;
		
    	Bid bids = new Bid();
		bids.id = bidId;
    	
		if("安全型".equals(user.risk_result)) {
			
			Bid bids_ = new Bid();
			bids_.id = bidId;
			if((bids_.periodUnit == 0 && bids_.period > 6) || bids_.periodUnit == -1) {
				error.msg = "安全型用户不允许出借6个月以上标的";
				error.code = -3;

				return 0;
			}
		}
	    
    	
    	if (investTotal <= 0) {
    		error.msg = "对不起！请输入正确格式的数字!";
			error.code = -10;

			return 0;
		}

		t_users user1 = User.queryUserforInvest(userId, error);
		
		if(error.code < 0) {
			return 0;
		}

		if (user1.balance <= 0) {
			error.msg = "对不起！您余额不足，请及时充值！";
			error.code = -999;

			return 0;
		}
		
	
		double balance = user1.balance;
		boolean black = user1.is_blacklist;
		String dealpwd = user1.pay_password;
		
		if (black) {
			error.msg = "对不起！您已经被平台管理员限制操作！请您与平台管理员联系！";
			error.code = -1;

			return 0;
		}

		Map<String, String> bid = bidMap(bidId, error);

		if (error.code < 0) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return 0;
		}
		
		if(Convert.strToInt(bid.get("ips_status"), IPSDealStatus.NORMAL) == IPSDealStatus.BID_END_HANDING){  //标的结束处理中，不能投标
			error.msg = "标的撤销中，不能投标";
			error.code = -3;

			return 0;
		}
		
		double minInvestAmount = Double.parseDouble(bid.get("min_invest_amount") + "");
		double averageInvestAmount = Double.parseDouble(bid.get("average_invest_amount") + "");
		double amount = Double.parseDouble(bid.get("amount") + "");
		int status = Integer.parseInt(bid.get("status") + "");
		
		Date invest_expire_time = DateUtil.strToDate(bid.get("invest_expire_time").toString());
		
		double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");
		long bidUserId = Long.parseLong(bid.get("user_id") + "");// 借款者
		long product_id = Long.parseLong(bid.get("product_id") + "");
		long time = new Date().getTime();
		long time2 = invest_expire_time.getTime();
		
		
		//验证红包
		double redAmount = 0.0;
		if(redPackage != null){
			//查询最新红包信息，防止脏数据
			redPackage = RedPackageHistory.findBySign(redPackage.id + "");
			
			redAmount  = redPackage.money;
			// 判断优惠券类型 2为加息劵
			if(redPackage.couponType == 2) {
				redAmount = 0; //加息劵 红包金额为0
			}
			if(redPackage.status != Constants.RED_PACKAGE_STATUS_UNUSED){
				error.msg = "红包已经失效，或已使用!";
				error.code = -2;
				return 0;
			}
			//判断是否满足红包的起投点 输入的钱 + 红包钱 - 起投点的钱 >= 0
			if(investTotal < redPackage.valid_money){
				error.msg = "对不起！您的投标金额少于红包最低投资限制金额！";
				error.code = -2;
				return 0;
			}
			if(investTotal + redAmount > (amount - hasInvestedAmount)) {
				double money = amount - hasInvestedAmount;
				error.msg = "对不起！您的实际投资金额超过了该标的剩余金额,您最多只能投" + money + "元！";
				error.code = -6;

				return 0;
			}
			
		}

		//实际投资金额：输入的钱+红包金额
		investTotal += redAmount;
		if(bids.bidRiskId != null) {
			Map<String, Object> riskLimit = queryRiskLimit(error, Double.parseDouble(investTotal+"") ,bids,user);
			BigDecimal hasInvest = (BigDecimal) riskLimit.get("invesTotalSum");//已投资金额
			BigDecimal quota = (BigDecimal) riskLimit.get("quota");//限额
			boolean is_risk_invest = (boolean) riskLimit.get("is_risk_invest");
			if(!is_risk_invest) { //
				error.msg = "未达出借要求！";
				error.code = -20;
				return 0; 
			}
			
			// 累计'已投资'金额超过风险评估限制金额
	    	if(hasInvest.compareTo(quota) >= 0) {// 
				error.msg = "您的额度已用完！";
				error.code = -20;
				return 0; 
	    	}
	    	
	    	//quota.subtract(hasInvest) 剩余可投
	    	if(quota.subtract(hasInvest).compareTo(new BigDecimal(investTotal) ) ==-1) {
	    		error.msg = "您出借的额度超过剩余可投！";
				error.code = -20;
				return 0; 
	    	}
		}
		
		// 新手标
		int is_only_new_user = Integer.parseInt(bid.get("is_only_new_user") +"");
		if(is_only_new_user == 1){
			
			String new_invest_open = Play.configuration.getProperty("new_invest_open", "0");
			if("1".equals(new_invest_open)) {
				
				long new_invest_amount = Long.valueOf(Play.configuration.getProperty("new_invest_amount", "50000"));
				int new_invest_time_unit = Integer.valueOf(Play.configuration.getProperty("new_invest_time_unit", "3"));
				
				Date overDt = DateUtil.dateAddMonth(user.time, new_invest_time_unit);
				if(overDt.before(new Date())) {
					error.msg = "已超出可投资新手标时间，请选择其他类型标的";
					error.code = -2;
					return 0;
				}
				
				double investAmount = Double.parseDouble(JPAUtil.createNativeQuery("select IFNULL(SUM(i.amount),0) from t_invests i left join t_bids b on i.bid_id = b.id left join t_users u on u.id = i.user_id where b.is_only_new_user = 1 AND i.user_id = ?", userId).getSingleResult().toString());
				Logger.info(String.format("已投新手标: %s=>%s", userId, investAmount));
				if(investAmount >= new_invest_amount) {
					error.msg = "新手标额度已用完，请选择其他类型标的";
					error.code = -2;
					return 0;
				}else if(investTotal > (new_invest_amount - investAmount)) { // 投资金额超过可投金额
					error.msg = "可投新手标剩余额度"+(new_invest_amount - investAmount)+"元";
					error.code = -2;
					return 0;
				}
			}else {
				
				long investCount = Long.valueOf(JPAUtil.createNativeQuery("select count(*) from t_invests bi left join t_bids b on bi.bid_id = b.id where b.is_only_new_user = 1 AND bi.user_id = ?", userId).getSingleResult()+"");
				if(investCount > 0){
					error.msg = "每位用户限享有一次新手标投资机会";
					error.code = -2;
	
					return 0;
				}
				else if(investTotal > 20000){
					error.msg = "新手标上限为20000元";
					error.code = -2;
	
					return 0;
				}
			}
			
			
		}
		
		if( time> time2){
			error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
			error.code = -2;
			JPA.setRollbackOnly();
			Logger.error("-----------111Incest1767bidId:%s,time:%s ,invest_expire_time.getTime:%s-------------",bidId,time,time2);
			
			return 0;
		}
		User bidUser = new User();
		bidUser.id = bidUserId;
		if ((user.mobile).equals(bidUser.mobile)) { //借款端与投资端分离，不能根据用户ID判断。
			error.msg = "对不起！您不能投自己的借款标!";
			error.code = -10;

			return 0;
		}

		if (User.isInMyBlacklist(bidUserId, userId, error) < 0) {
			error.msg = "对不起！您已经被对方拉入黑名单，您被限制投资此借款标！";
			error.code = -2;

			return 0;
		}

		if (status != Constants.BID_ADVANCE_LOAN
				&& status != Constants.BID_FUNDRAISE) {
			error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
			error.code = -2;

			return 0;
		}

		if (amount <= hasInvestedAmount) {
			error.msg = "对不起！此借款标已经不处于招标状态，请投资其他借款标！谢谢！";
			error.code = -2;

			return 0;
		}

		DataSafety data = new DataSafety();// 数据防篡改(针对当前投标会员)
		data.setId(userId);
		boolean sign = data.signCheck(error);
		
		if (error.code < 0) {
			error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
			error.code = -2;
			JPA.setRollbackOnly();

			return 0;
		}

		if (!sign) {// 数据被异常改动
			error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
			error.code = -2;
			JPA.setRollbackOnly();
			return 0;
		}

		String sqlProduct = "select is_deal_password as is_deal_password from t_products where id = ?";
		boolean is_deal_password = (Boolean) JPA.em().createNativeQuery(sqlProduct).setParameter(1, product_id).getSingleResult();

		if (is_deal_password == true && !Constants.IPS_ENABLE) {
			if (StringUtils.isBlank(dealpwdStr)) {
				error.msg = "对不起！请输入交易密码!";
				error.code = -12;
				return 0;
			}
			if (!Encrypt.MD5(dealpwdStr + Constants.ENCRYPTION_KEY).equals(
					dealpwd)) {
				error.msg = "对不起！交易密码错误!";
				error.code = -13;
				return 0;
			}
		}

		/* 普通模式 */
		if (averageInvestAmount == 0) {

			if (amount - hasInvestedAmount >= minInvestAmount) {
				
				if (investTotal < minInvestAmount) {
					error.msg = "对不起！您最少要投" + minInvestAmount + "元";
					error.code = -3;

					return 0;
				}
			} else {

				if (investTotal < amount - hasInvestedAmount) {
					double money = amount - hasInvestedAmount;
					error.msg = "对不起！您最少要投" + money + "元";
					error.code = -4;

					return 0;
				}
			}

			if (balance < (investTotal - redAmount)) {
				error.msg = "对不起！您可用余额不足！根据您的余额您最多只能投" + balance + "元";
				error.code = Constants.BALANCE_NOT_ENOUGH;

				return 0;
			}

			if (investTotal > (amount - hasInvestedAmount)) {
				double money = amount - hasInvestedAmount;
				error.msg = "对不起！您的投资金额超过了该标的剩余金额,您最多只能投" + money + "元！";
				error.code = -6;

				return 0;
			}
		}
		
		/* 认购模式 */
		if (minInvestAmount == 0) {
			if (investTotal < 0) {
				error.msg = "对不起！您至少应该买一份！";
				error.code = -7;

				return 0;
			}
			if (investTotal > ((amount - hasInvestedAmount) / averageInvestAmount)) {
				error.msg = "对不起！您最多只能购买" + (amount - hasInvestedAmount)
						/ averageInvestAmount + "份！";
				error.code = -8;

				return 0;
			}

			investTotal = (int) (investTotal * averageInvestAmount);

			if (balance < investTotal) {
				error.msg = "对不起！您余额不足！您最多只能购买"
						+ (int) (balance / averageInvestAmount) + "份！";
				error.code = Constants.BALANCE_NOT_ENOUGH;

				return 0;
			}

		}

		if (error.code < 0) {
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -2;

			return 0;
		}
		
		String pMerBillNo = null;
		double pFee = 0;
		
		//资金托管，未开启自动投标，直接返回
		if (Constants.IPS_ENABLE && !isAuto) {			
				return 0;
		}		
		
		//资金托管，开启了自动投标, 调用接口
		if (Constants.IPS_ENABLE && isAuto) {
						
			//调用自动投标接口, 同步不处理业务逻辑，异步处理逻辑;
			t_bids t_bid = t_bids.findById(bidId);
			PaymentProxy.getInstance().autoInvest(error, client, t_bid, userId, investTotal);
			
			return 0;
	    }
		
		//普通网关的业务逻辑调用
		if (averageInvestAmount == 0) {
			// 判断是否满足红包的起投点 输入的钱 + 红包钱 - 起投点的钱 >= 0
//			if (null != redPackage && (investTotal - redPackage.sendType) >= 0) {
//				if(client == Constants.CLIENT_APP){
//					investTotal += redPackage.money;
//				}
//			}
		}

		if (minInvestAmount == 0) {
			// 判断是否满足红包的起投点
//			if (null != redPackage && redPackage.valid_money >= (investTotal * averageInvestAmount)) {
//				if(client == Constants.CLIENT_APP){
//					redAmount = redPackage.money;
//				}
//			}

			/*if (investTotal > ((amount - hasInvestedAmount) / averageInvestAmount)) {// 投资钱大于
				error.msg = "对不起！您最多只能购买" + (amount - hasInvestedAmount) / averageInvestAmount + "份！";
				error.code = -8;
				return;
			}*/
		}
		
		
		
		//计算投资时分摊到投资人身上的借款管理费及投标奖励
		Map<String, Double> map = Bid.queryAwardAndBidFee(bids, investTotal, error);
		double award = map.get("award");
		double bid_fee = map.get("bid_fee") ;
		String  investId = doInvest(user1, bidId, investTotal, pMerBillNo, pFee, client, award, bid_fee,redPackage, error);
		
		// 修改红包状态
		if (null != redPackage) {
			 RedPackageHistory.updateStatus(redPackage.id+"", Constants.RED_PACKAGE_STATUS_USING+"",investId, error);
		}
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("amount", investTotal + "");
			parameters.put("apr", bids.apr + "");
			parameters.put("period",bids.period + "");
			parameters.put("periodUnit", bids.periodUnit + "");
			parameters.put("paymentType", bids.repayment.id + "");
			parameters.put("increaseRate", bids.increaseRate + "");
			//投标送积分
			Score.sendScore(user1.id,bids.title, parameters, Integer.parseInt(investId), error);
			
			if(Score.isFirstInvest(user1.id, Integer.parseInt(investId))){
				//首投送积分
				Score.sendFirstScore(user1.id, Integer.parseInt(investId), error);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return investTotal;
	}
	/**
	 *  投标操作(写入数据库)
	 * @param user1 投资人
	 * @param bid 标的id
	 * @param investTotal  此次投资金额
	 * @param pMerBillNo投资流水号，第三方返回的
	 * @param pFee 理财管理费
	 * @param client pc/app/微信/手机网站
	 * @param award 投资奖励
	 * @param bid_fee 借款管理费分摊到每次投资的费用
	 * @param redPackage  红包
	 * @param error
	 */
	public static String  doInvest(t_users user1, long bidId, double investTotal, String pMerBillNo, double pFee, int client, double award, double bid_fee,RedPackageHistory redPackage, ErrorInfo error) {
		error.clear();
		double redMoney = 0.0;
		if (null != redPackage && redPackage.couponType == 1) { // 普通红包
			Logger.info("使用普通红包金额投资：" +  redPackage.money);
			redMoney = redPackage.money;
			 
		}
		Map<String, String> bid = Invest.bidMap(bidId, error);
		if(error.code < 0){
			
			return null;
		}
		
		long userId = user1.id;
		double amount = Double.parseDouble(bid.get("amount") + "");
		double hasInvestedAmount = Double.parseDouble(bid.get("has_invested_amount") + "");

		
		double schedule = Arith.divDown(hasInvestedAmount + investTotal, amount, 4) * 100;//

		/* 已投总额增加,投标进度增加, 超标控制 */
		updateBidschedule(bidId, investTotal, schedule, error); 
		if(error.code < 0){  //超标或更新失败

			return null;
		}
		
		/* 满标 */
		if (amount == (hasInvestedAmount + investTotal)) {

			// 更新满标时间
			int resulta = updateBidExpiretime(bidId, bid_fee,  error);

			if (resulta < 0) {
				error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
				error.code = -8;
				JPA.setRollbackOnly();
				
				return null;
			}
		}

		if (error.code < 0) {
			error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
			error.code = -8;
			JPA.setRollbackOnly();
			return null;
		}

		/* 可用金额减少,冻结资金增加 */
		DealDetail.freezeFund(userId, new BigDecimal(investTotal).subtract(new BigDecimal(redMoney)).doubleValue(), error);

		if (error.code < 0) {

			return null;
		}

		// 更新会员性质
		User.updateMasterIdentity(userId, Constants.INVEST_USER, client, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return null;
		}
		v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return null;
		}

		double user_amount = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receive_amount = forDetail.receive_amount;
		
		// 添加交易记录
		DealDetail dealDetail = new DealDetail(userId, DealType.FREEZE_INVEST, investTotal-redMoney, bidId, user_amount, 
				freeze, receive_amount, "冻结投标金额" + (investTotal - redMoney) + "元");
		dealDetail.addDealDetail(error);
		
		// 添加红包使用交易记录 有可能页面显示数据有问题 
		DealDetail redDealDetail = new DealDetail(userId, DealType.FREEZE_INVEST, redMoney, bidId, user_amount, 
				freeze, receive_amount, "扣除红包金额" + (redMoney) + "元");
		redDealDetail.addDealDetail(error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return null;
		}

		// 投标用户增加系统积分
		DealDetail.addScore(userId, 1, investTotal, bidId, "投标成功，添加系统积分", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return null;
		}
		
		// 投标添加用户事件
		DealDetail.userEvent(userId, UserEvent.INVEST, "成功投标", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			return null;
		}

		t_invests invest = new t_invests();


		//查询此时所有的活动
		List<Map<String, Object>>  activities =ActivityIncreaseRateService.getEnableActivitysForCurrent();

		for (Map<String, Object> activity : activities){
			if(activity.get("type").toString().equals("1")){
				invest.increase_activity_id1 = Long.parseLong(activity.get("id").toString());
				break;
			}
		}

		/* 满标 */
		if (amount == (hasInvestedAmount + investTotal)) {
			for (Map<String, Object> activity : activities){
				if(activity.get("type").toString().equals("3")){
					invest.increase_activity_id2 = Long.parseLong(activity.get("id").toString());
					break;
				}
			}
		}
		//首投  如果这笔投资即是首投也是尾投，并且活动也同时存在，则取首投的活动数据
		if(hasInvestedAmount==0){
			for (Map<String, Object> activity : activities){
				if(activity.get("type").toString().equals("2")){
					invest.increase_activity_id2 = Long.parseLong(activity.get("id").toString());
					break;
				}
			}
		}


		invest.client = client;
		invest.user_id = userId;
		invest.time = new Date();
		invest.bid_id = bidId;
		/* 0 正常(转让入的也是0) */
		invest.transfer_status = 0;
		invest.amount = investTotal;
		invest.fee = pFee;
		invest.mer_bill_no = pMerBillNo;
		invest.award = award;
		invest.bid_fee = bid_fee;
		invest.red_amount = redMoney;
		try {
			invest.save();
			
			// 标记为有效会员
			User.updateActive(userId, error);
		} catch (Exception e) {
			error.msg = "对不起！您此次投资失败！请您重试或联系平台管理员！";
			error.code = -10;
			JPA.setRollbackOnly();
			Logger.error("保存投资记录失败，事务回滚");

			return null;
		}
		
		// 投标一次增加信用积分
		DealDetail.addCreditScore(userId, 4, 1, invest.id, "成功投标一次，投资人添加信用积分",error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			Logger.error("增加信用积分失败，事务回滚");
			
			return null;
		}		
		v_user_for_details forDetail2 = DealDetail.queryUserBalance(userId, error);

		if (error.code < 0) {
			JPA.setRollbackOnly();
			Logger.error("查询用户余额失败，事务回滚");
			
			return null;
		}
		
		if (forDetail2.user_amount < 0) {
			error.msg = "对不起！您账户余额不足，请及时充值！";
			error.code = -10;
			JPA.setRollbackOnly();
			Logger.error("账户余额不足，事务回滚");
			
			return null;
		}

		DataSafety data = new DataSafety();
		data.updateSignWithLock(userId, error);

		if (error.code < 0) {
			error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
			error.code = -9;
			JPA.setRollbackOnly();
			Logger.error("更新数据防篡改字段，事务回滚");
			
			return null;
		}
		
		// 发送消息
		String username = user1.name;
		String title = bid.get("title") + "";

		TemplateEmail email = TemplateEmail.getEmailTemplate(Templets.E_INVEST, error);//发送邮件
		
		if(error.code < 0) {
			email = new TemplateEmail();
		}
		 
		if(email.status){
			 String econtent = email.content;
			 econtent = econtent.replace("date", DateUtil.dateToString((new Date())));
			 econtent = econtent.replace("userName", username);			
			 econtent = econtent.replace("title", title);
			 econtent = econtent.replace("investAmount",  DataUtil.formatString(investTotal));
			 
			 TemplateEmail.addEmailTask(user1.email, email.title, econtent);
		 }
		
		 
		TemplateStation station = TemplateStation.getStationTemplate(Templets.M_INVEST, error);//发送站内信
		 
		if(error.code < 0) {
			station = new TemplateStation();
		}
		 
		 if(station.status){
			 String stationContent = station.content;
			 stationContent = stationContent.replace("date", DateUtil.dateToString((new Date())));
			 stationContent = stationContent.replace("userName", username);			
			 stationContent = stationContent.replace("title", title);
			 stationContent = stationContent.replace("investAmount",  DataUtil.formatString(investTotal-redMoney));
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = userId;
			 letter.title = station.title;
			 letter.content = stationContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }
		 String unit = bid.get("period_unit");
		 String date = null;
		 if ("-1".equals(unit)) {
			 date="年";
		 }else if("0".equals(unit)){
			 date="月";
		 }else if("1".equals(unit)){
			 date="日";
		 }
		 String period = bid.get("period");
		 String apr = bid.get("apr");
		 String description = bid.get("description");
		 //尊敬的userName: 恭喜您投资成功！投资金额￥investAmount元，投资期限period date，年化收益率apr%，还款方式description.
		 TemplateSms sms = TemplateSms.getSmsTemplate(Templets.S_TENDER, error);//发送短信
		 if(error.code < 0) {
			 sms = new TemplateSms();
		 }
		 if(sms.status && StringUtils.isNotBlank(user1.mobile)){
			 String smscontent = sms.content;
			 smscontent = smscontent.replace("userName", username);			
			 smscontent = smscontent.replace("investAmount",  DataUtil.formatString(investTotal));
			 smscontent = smscontent.replace("period", period);
			 smscontent = smscontent.replace("date", date);
			 smscontent = smscontent.replace("apr", apr);
			 smscontent = smscontent.replace("description", description);
			 TemplateSms.addSmsTask(user1.mobile, smscontent);
		 }
		
		 if (amount == (hasInvestedAmount + investTotal)) {
		 
			List<Invest> investUser = Invest.queryAllInvestUser(bidId);
			
			if(investUser != null && investUser.size() > 0) {
				for(Invest userInvest : investUser) {
					t_users user = t_users.find("select new t_users(id, device_user_id, channel_id, device_type, is_bill_push, is_invest_push, is_activity_push) from t_users where id = ?", 
							userInvest.investUserId).first();
					
					if(user.is_invest_push) {
						String device = user.device_type == 1 ? "\"custom_content\":{\"bidId\":\""+bidId+"\",\"type\":\"3\"}" : "\"aps\": {\"alert\":\"test\",\"sound\":\"1\",\"badge\":\"1\"},\"bidId\":\""+bidId+"\",\"type\":\"3\"";
						device = "{\"title\":\"理财满标提醒通知\",\"description\":\"你有一条新的理财满标\","+ device+"}";
//						PushMessage.pushUnicastMessage(bill.device_user_id, bill.channel_id, bill.device_type, device);
						PushMessage.pushUnicastMessage(user.device_user_id, user.channel_id, user.device_type, device);
					}
				}
			}
		}
		 
		error.msg = "投资成功！";
		error.code = 1;
		
		return invest.id+"";
	}
	
	/**
	 * 获取用户减掉预留金额后的可用金额
	 * @param userId
	 * @param remandAmount
	 * @return
	 */
	public static double queryAutoUserBalance(long userId, double remandAmount){
		
		Double balance = null;
		String sql = "select balance from t_users where id = ?";
		
		try {
			balance = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		if(null == balance)
			return 0;
		
		return balance < remandAmount ? 0: balance - remandAmount;
	}
	
	/**
	 * 按时间倒序排序查出所有开启了投标机器人的用户ID
	 * @return
	 */
	public static List<Long> queryAllAutoUser(){
		List<Long> list = null;
		
		try {
			 list = t_user_automatic_invest_options.find("select user_id from t_user_automatic_invest_options where status = 1 and (case when valid_type = 0 then timestampdiff(DAY,time,NOW()) else timestampdiff(MONTH,time,NOW()) END) < valid_date order by time asc").fetch();
		} catch (Exception e) {
			return null;
		}
		
		return list;
	}
	
	
	
	/**
	 * 将用户排到自动投标队尾
	 * @param user_id
	 */
	public static void updateUserAutoBidTime(long user_id){
		String sql = "update t_user_automatic_invest_options set wait_time = ? where user_id = ?";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, new Date());
		query.setParameter(2, user_id);
		
		try {
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询符合用户设置条件的标的ID
	 * @param autoOptions
	 * @param unit
	 * @param bidId
	 * @return
	 */
	public static Map<String,Object> queryBiderByParam(t_user_automatic_invest_options autoOptions,int unit,long bidId){
		int min_period=0;
		int max_period=0;
		
		if(unit == -1){//单位为年
			min_period = autoOptions.min_period*12;
			max_period = autoOptions.max_period*12;
		}
		
		if(unit == 0 ){
			min_period = autoOptions.min_period;
			max_period = autoOptions.max_period;
		}
		
		StringBuffer condition = new StringBuffer();
		condition.append("select new Map(id as id) from v_confirm_autoinvest_bids where apr >= "
				+ autoOptions.min_interest_rate + " and apr <= " + autoOptions.max_interest_rate + " and min_invest_amount <= "+autoOptions.amount);
		
		if(autoOptions.min_period > 0 && autoOptions.max_period > 0){
			condition.append(" and period >="+min_period+" and period <="+ max_period);
		}
		
		/*信用等级排名越大，等级越低*/
		if(autoOptions.min_credit_level_id > 0 && autoOptions.max_credit_level_id > 0){
			condition.append(" and num >= " + autoOptions.max_credit_level_id +"  and num <= "+autoOptions.min_credit_level_id);
		}
		
		condition.append(" and  loan_type in ( "+ autoOptions.loan_type+" )  and id=?");
		Logger.info("自动投标\nbidId:%d\ncondition:%s", bidId, condition.toString());
		Map<String,Object> map = v_confirm_autoinvest_bids.find(condition.toString() , bidId).first();
		return map;
	}
	
	
	
	
	/**
	 * 扣除保留金额后，计算最后投标金额
	 * @param bidAmount
	 * @param schedule
	 * @param amount
	 * @param hasInvestedAmount
	 * @return
	 */
	public static int calculateBidAmount(double bidAmount, double schedule,double amount,double hasInvestedAmount) {
			
		int maxBidAmount = (int) (amount * 0.2);
		int invesAmount = 0;
		
		if (schedule < 95) {
			while (bidAmount > maxBidAmount) {
				bidAmount = bidAmount - 50;
			}

			do {
				invesAmount = (int) (hasInvestedAmount + bidAmount);
				schedule = invesAmount / amount;
				if (schedule > 95) {
					bidAmount = bidAmount - 50;
				}
			} while (schedule > 95);
		}
		
		return (int) bidAmount;
	}
	
	
	/**
	 * 计算自动投标份数
	 * @param amount
	 * @param averageAmount
	 * @return
	 */
	public static int calculateFinalInvestAmount(double amount,double averageAmount){
		int temp = 0;
		temp = (int) (amount/averageAmount);
		return temp;
	}
	
	/**
	 * 增加用户自动投标记录
	 * @param userId
	 * @param bidId
	 */
	public static void addAutoBidRecord(long userId,long bidId){
		
		t_user_automatic_bid bid = new t_user_automatic_bid();
		
		bid.bid_id=bidId;
		bid.time = new Date();
		bid.user_id=userId;
		
		bid.save();
	}
	
	/**
	 * 判断用户是否已经自动投过当前标
	 * @param bidId
	 * @param userId
	 * @return
	 */
	public static boolean hasAutoInvestTheBid(long bidId,long userId){
		
		boolean flag=false;
		t_user_automatic_bid bid = t_user_automatic_bid.find("user_id=? and bid_id =?", userId,bidId).first();
		if(bid != null){
			flag=true;
		}
		return flag;
	}
	
	public Map<String,Object> queryParamByBidId(long bidId){
		String sql="select new Map(user_id as user_id,amount as amount,min_invest_amount as min_invest_amount,average_invest_amount as average_invest_amount," +
				"has_invested_amount as has_invested_amount) from t_bids where id=?";
    	return t_bids.find(sql, bidId).first();
	}
	
	/**
	 * 查询所有投标进度小于且进入招标中十五分钟后的所有标的
	 * @return
	 * @throws ParseException 
	 */
	public static List<Map<String,Object>> queryAllBider() {
		
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		List<v_confirm_autoinvest_bids> bidList = null;
//		String dateTime = "";
//		try {
//			dateTime = DateUtil.getDateMinusMinutes(15);
//		} catch (ParseException e1) {
//			e1.printStackTrace();
//		}//当前时间减去15分钟的时间
//		
//		Date date = DateUtil.strToDate(dateTime);
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_CONFIRM_AUTOINVEST_BIDS);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_confirm_autoinvest_bids.class);
            bidList = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, Object> bidMap = null;
		
		for(v_confirm_autoinvest_bids bid : bidList) {
			bidMap = new HashMap<String, Object>();
			
			bidMap.put("bid_id", bid.id);
			bidMap.put("user_id", bid.user_id);
			bidMap.put("period_unit", bid.period_unit);
			bidMap.put("amount", bid.amount);
			bidMap.put("has_invested_amount", bid.has_invested_amount);
			bidMap.put("loan_schedule", bid.loan_schedule);
			bidMap.put("has_invested_amount", bid.has_invested_amount);
			bidMap.put("loan_schedule", bid.loan_schedule);
			bidMap.put("average_invest_amount", bid.average_invest_amount);
			
			mapList.add(bidMap);
		}
		
		return mapList;
	}
	
	/**
	 * 查询过期的机器人并关闭。
	 * @return
	 */
	public static void closeAutoUser(){
		EntityManager em = JPA.em();
		
		//查询过期的机器人的用户ids
		String sql = "select user_id from t_user_automatic_invest_options where status = 1 and (case when valid_type = 0 then timestampdiff(DAY,time,NOW()) else timestampdiff(MONTH,time,NOW()) END) > valid_date";
		List<Long> userIds = t_user_automatic_invest_options.find(sql).fetch(); 

		if(userIds == null || userIds.size() < 1){
			return;
		}
		
		String strUserIds = userIds.toString();
		strUserIds = strUserIds.substring(1, strUserIds.length()-1);

		try {
			em.createQuery("update t_user_automatic_invest_options set status = 0 where user_id in ("+ strUserIds+")").executeUpdate();
		} catch (Exception e) {
			Logger.error("关闭过期的投标机器人时(修改t_user_automatic_invest_options.status)：%s", e.getMessage());
		} 
		
		try {
			em.createNativeQuery("update t_users set ips_bid_auth_no = ? where id in ("+ strUserIds+")").setParameter(1, null).executeUpdate();
		} catch (Exception e) {
			Logger.error("关闭过期的投标机器人时(修改t_users.ips_bid_auth_no)：%s", e.getMessage());
		} 

	}
	
	
	/**
	 * 判断该借款标是否超过95%
	 * @param bidId
	 * @return
	 */
	public static boolean judgeSchedule(long bidId){
		Double schedule = 0.0;
		String sql = "select loan_schedule from t_bids where id = ? ";
		
		try {
			schedule = t_bids.find(sql, bidId).first();
		} catch (Exception e) {
			return false;
		}
		
		if(null == schedule){
			return false;
		}
		
		if(schedule >= 95){
			return false;
		}
		
		return true;
	}
	
	
	
	/**
	 * 资金托管模式下自动投标时额外条件判断
	 * @param userIdStr
	 * @param bidIdStr
	 * @return
	 */
	public static boolean additionalJudgment(long userId, long bidId){
		boolean flag = false;
		
		t_user_automatic_invest_options robot = null;
		double bidAmount = 0;
		
		try {
			robot = t_user_automatic_invest_options.find(" user_id = ? ", userId).first();
			bidAmount = t_bids.find(" select amount from t_bids where id = ? ", bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		
		if(null != robot && bidAmount > 0){
			int dateType = robot.valid_type;
			int date = robot.valid_date;
			double minAmount = robot.min_amount;
			double maxAmount = robot.max_amount;
			Date time = robot.time;
			Date overTime = null;
			if(dateType == 0){
				overTime = DateUtil.dateAddDay(time, date);
			}else{
				overTime = DateUtil.dateAddMonth(time, date);
			}
			
			boolean isOverTime = overTime.getTime() >= new Date().getTime() ?  true : false;
			boolean isOverAmount = false;
			if(bidAmount >= minAmount && bidAmount <= maxAmount){
				isOverAmount = true;
			}
			if(isOverTime && isOverAmount){
				flag = true;
			}
			
		}
		
		return flag;
	}

	
	/**
	 * 自动投标
	 * @throws ParseException 
	 */
	public static void automaticInvest() {
		List<Map<String, Object>> biderList =  Invest.queryAllBider();//查出所有符合自动投标条件的标的
		
		if(null == biderList || biderList.size() < 1) {
			return ;
		}
		
		closeAutoUser();  //查询过期的机器人并关闭
		
		List<Long> userIds = Invest.queryAllAutoUser();
		
		if (null == userIds || userIds.size() < 1) {
			return ;
		}
			
		int unit = -2;//标产品期限单位 -1：年  0：月   1：天
		long userId = -1;
		long bidId= -1;
		boolean over = false;
		t_user_automatic_invest_options userParam = null;
		Map m = null;
		ErrorInfo error = new ErrorInfo();
		
		//遍历所有的符合条件进度低于95%的招标中的借款
		for(Map<String, Object> map : biderList) {
			bidId = Convert.strToLong(map.get("bid_id") + "" , 0);
			userId = Convert.strToLong(map.get("user_id") + "" , 0);
			
			if(bidId < 1 || userId < 1) {
				continue ;
			}
			
			over = judgeSchedule(bidId);
			
			if(!over){
				continue ;
			}
			
			//遍历所有设置了投标机器人用户ID
			for(Long userId2 : userIds) {
				//资金托管模式下的额外判断
				over = additionalJudgment(userId2, bidId);
				
				/* 如果该借款是发布者的标,则发布者不能投标,用户自动排队到后面 */
				if(!over || userId == userId2) {
					Invest.updateUserAutoBidTime(userId2);//将该用户排到队尾
					
					continue ;
				}
				
				//获取用户设置的投标机器人参数
				userParam = t_user_automatic_invest_options.find("from t_user_automatic_invest_options where user_id=?", userId2).first();
					
				if(null == userParam) {
					continue ;
				}
				
				over = hasAutoInvestTheBid(bidId, userId2);
				
				/* 该用户已经投过该标的 */
				if(over) {
					Invest.updateUserAutoBidTime(userId2);//将该用户排到队尾
					
					continue ;
				}
				
				unit = Integer.parseInt(map.get("period_unit").toString());
				m = Invest.queryBiderByParam(userParam, unit, bidId);//查询符合用户条件的标的
				
				if(null == m) {
					Invest.updateUserAutoBidTime(userId2);//将该用户排到队尾
					
					continue ;
				}		
				
				double amount = Double.parseDouble(map.get("amount").toString());//标的借款总额
				double has_invested_amount =  Double.parseDouble(map.get("has_invested_amount").toString());//标的已投金额
				double balance = Invest.queryAutoUserBalance(userId2, userParam.retention_amout);//减去用户设置的保留余额后的用户可用余额
				double setAmount = userParam.amount;//用户设置的每次投标金额
				double loan_schedule = Double.parseDouble(map.get("loan_schedule").toString());
				double averageAmount = Double.parseDouble(map.get("average_invest_amount").toString());
				int bidAmount = Invest.calculateBidAmount(setAmount,loan_schedule, amount, has_invested_amount);//计算出投标金额
				
				/* 用户余额不足 */
				if(balance < bidAmount ){
					Invest.updateUserAutoBidTime(userId2); // 排到队尾
					
					continue ;
				}					
				if(averageAmount > 0){
					bidAmount = calculateFinalInvestAmount(bidAmount,averageAmount);
				}				
				// 关闭自动事务
				JPAPlugin.closeTx(false);
				// 开启自动投标事务
				JPAPlugin.startTx(false);
				
				try {
					invest(userId2, bidId, bidAmount, "", true, Constants.CLIENT_PC,null, error);
				} catch (Exception e) {
					Logger.error("自动投标失败：" + e.getMessage());
					continue ;
				} finally {
					//关闭自动投标事务
					JPAPlugin.closeTx(false);
					Logger.info("自动投标事务正常关闭，userId = %s, bidId = %s ", userId2, bidId);
				}
				
				// 开启自动投标事务
				JPAPlugin.startTx(false);
				
				if (error.code < 0 && Constants.ALREADY_RUN != error.code) {
					continue ;
				}				
				Invest.addAutoBidRecord(userId2, bidId);//添加自动投标记录
				Invest.updateUserAutoBidTime(userId2);//排到队尾
			}
		}
	}
	
	/**
	 * 前台显示的机构借款标
	 * @return
	 */
	public static List<v_front_all_bids> queryAgencyBids(){
		
		List<v_front_all_bids> agencyBids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		sql.append(" and t_bids.is_agency = 1 and t_bids.is_show_member_bid=0");
		sql.append(" order by t_bids.time desc");
		List<Object> params = new ArrayList<Object>();
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setMaxResults(Constants.HOME_BID_COUNT);
            agencyBids = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return agencyBids;
	}
	
	/**
	 * 前台显示的会员贷
	 * @return
	 */
	public static List<v_front_all_bids> queryMemberBids(){
		
		List<v_front_all_bids> agencyBids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		sql.append(" and t_bids.is_agency = 1 and t_bids.is_show_member_bid=1");
		sql.append(" order by IF(t_bids.loan_schedule>=100,0,1) DESC, t_bids.time desc");
		List<Object> params = new ArrayList<Object>();
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setMaxResults(2);
            agencyBids = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return agencyBids;
	}
	
	
	/**
	 * 前台显示的普通借款标
	 * @return
	 */
	public static List<v_front_all_bids> queryBids(){
		
		List<v_front_all_bids> bids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		sql.append(" and t_bids.is_agency = 0 and t_bids.status in(?, ?) order by t_bids.id desc");
		
		List<Object> params = new ArrayList<Object>();
		params.add(Constants.BID_ADVANCE_LOAN);
		params.add(Constants.BID_FUNDRAISE);
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setMaxResults(Constants.HOME_BID_COUNT);
            bids = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bids;
	}
	/**
	 * 根据产品id查询前台显示的借款标
	 * @return
	 */
	@Deprecated
	public static List<v_front_all_bids> queryBidsByProductid(int product_id){
		
		List<v_front_all_bids> bids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		sql.append(" and t_bids.product_id = ? order by IF(t_bids.loan_schedule>=100,0,1) DESC, t_bids.id desc");
		
		List<Object> params = new ArrayList<Object>();
		params.add(product_id);
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			for(int n = 1; n <= params.size(); n++){
				query.setParameter(n, params.get(n-1));
			}
			query.setMaxResults(2);
			bids = query.getResultList();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bids;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年4月5日
	 * @description 根据TAG查询前台显示的借款标
	 * @param tag
	 * @return
	 */
	public static List<v_front_all_bids> queryBidsByTag(String tag){
		
		List<v_front_all_bids> bids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		List<Object> params = new ArrayList<Object>();
		if(StringUtils.isNotBlank(tag)){
			sql.append(" and t_bids.tag = ? order by IF(t_bids.loan_schedule>=100,0,1) DESC, t_bids.id desc");
			params.add(tag);
		}else{
			sql.append(" and t_bids.tag is not null order by IF(t_bids.loan_schedule>=100,0,1) DESC,t_bids.is_only_new_user desc, t_bids.id desc");
		}
		
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			for(int n = 1; n <= params.size(); n++){
				query.setParameter(n, params.get(n-1));
			}
			query.setMaxResults(3);
			bids = query.getResultList();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bids;
	}


	/**
	 * pc端禁止投资修改新增方法
	 * @param tag
	 * @return
	 */
	public static List<v_front_all_bids> queryOverBids(String tag){

		List<v_front_all_bids> bids = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		String sql1 = " if(t_bids.`status` >2 ,CASE when activity1.rate is null THEN CAST(t_bids.is_increase_rate AS signed) else 1 end, CAST(t_bids.is_increase_rate AS signed)) isIncreaseRate,if(t_bids.`status` >2 ,IFNULL(activity1.rate,`t_bids`.`increase_rate`),`t_bids`.`increase_rate`) AS `increaseRate`,if(t_bids.`status` >2 ,CASE when activity1.rate is null then IFNULL(`t_bids`.`increase_rate_name`,'') else activity1.`NAME` end,`t_bids`.`increase_rate_name`)  AS `increaseRateName`,`t_bids`.`id` AS `id`,t_bids.min_invest_amount as min_invest_amount,t_bids.average_invest_amount as average_invest_amount,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`show_type` AS `show_type`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`status` AS `status`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`period` AS `period`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,t_bids.repayment_time AS repayment_time,concat (( SELECT `t_system_options`.`_value` AS `_value` FROM `t_system_options` WHERE (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_bids`.`repayment_type_id` AS `repayment_type_id`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,t_users.credit_level_id AS credit_level_id,`t_bids`.`time` AS `time`, activity1.`name` name1,activity1.rate rate1,activity2.`name`  name2,activity2.rate rate2,activity3.`name` name3,activity3.rate rate3 from `t_bids` LEFT JOIN `t_products` ON `t_products`.`id` = `t_bids`.`product_id` LEFT JOIN t_users ON t_bids.user_id = t_users.id LEFT JOIN `t_agencies` ON `t_agencies`.`id` = `t_bids`.`agency_id` LEFT JOIN `t_dict_bid_repayment_types` ON `t_dict_bid_repayment_types`.`id` = `t_bids`.`repayment_type_id` LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =1 LIMIT 1) activity1 on true LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =2 LIMIT 1) activity2 on true LEFT JOIN (SELECT a.rate,b.name,b.id from t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b on a.activity_id = b.id WHERE a.state=2 and a.start_time <= NOW() and NOW() < a.stop_time and b.type =3 LIMIT 1) activity3 on true  where `t_bids`.`status` IN (1, 2, 3, 4, 5, 14)";
		sql.append(sql1);
		List<Object> params = new ArrayList<Object>();
		sql.append(" and t_bids.id <= 2310 ");
		if(StringUtils.isNotBlank(tag)){
			sql.append(" and t_bids.tag = ? order by IF(t_bids.loan_schedule>=100,0,1) DESC, t_bids.id desc");
			params.add(tag);
		}else{
			sql.append(" and t_bids.tag is not null order by IF(t_bids.loan_schedule>=100,0,1) DESC,t_bids.is_only_new_user desc, t_bids.id desc");
		}


		try {
			EntityManager em = JPA.em();

			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			for(int n = 1; n <= params.size(); n++){
				query.setParameter(n, params.get(n-1));
			}
			query.setMaxResults(3);
			bids = query.getResultList();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bids;
	}
	
	/**
	 * 投资记录
	 * @param pageBean 分页对象
	 * @param bidId 标ID
	 */
	public static List<v_invest_records> bidInvestRecord(
							PageBean<v_invest_records> pageBean, 
							long bidId, 
							ErrorInfo error) {
		error.clear();

		int count = -1;
		List<v_invest_records> record_list = null;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_INVEST_RECORDS);
		sql.append(" and bid_id = ?");
		List<Object> params = new ArrayList<Object>();
		params.add(bidId);
		
		try {
			//count = (int) v_invest_records.count("bid_id = ?", bidId);
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_invest_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
            query.setMaxResults(pageBean.pageSize);
            record_list = query.getResultList();
            
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			Logger.error("理财->标投资记录,查询总记录数:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载投资记录失败!";

			return null;
		}

		if (count < 1)
			return new ArrayList<v_invest_records>();

		pageBean.totalCount = count;

		return record_list;
	}
	
	
	/**
	 * 理财风云榜
	 * @return
	 */
	public static List<v_bill_board> investBillboard(){
		
		List<v_bill_board> investBillboard = new ArrayList<v_bill_board>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_BILL_BOARD);
		sql.append(" group by t_bill_invests.user_id ");
		sql.append("order by sum((t_bill_invests.receive_corpus + t_bill_invests.receive_interest)) desc");
		
		try {
			//investBillboard = v_bill_board.find("").fetch(5);
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_board.class);
            query.setMaxResults(5);
            investBillboard = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			
			return investBillboard;
		}
		
		return investBillboard;
	}
	
	/**
	 * 理财风云榜(更多)
	 * @return
	 */
	public static PageBean<v_bill_board> investBillboards(int currPage, ErrorInfo error){
		error.clear();
		
		if(currPage < 1) {
			currPage = 1;
		}
		
		if(currPage > 5) {
			currPage = 5;
		}
		
		List<v_bill_board> investBillboard = new ArrayList<v_bill_board>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_BILL_BOARD);
		sql.append(" group by t_bill_invests.user_id ");
		sql.append("order by sum((t_bill_invests.receive_corpus + t_bill_invests.receive_interest)) desc");
		
		int count = 0;
		 
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_bill_board.class);
            query.setFirstResult((currPage - 1) * 10);
            query.setMaxResults(10);
            investBillboard = query.getResultList();
            
    		count = QueryUtil.getQueryCountByCondition(em, sql.toString(), new ArrayList<Object>());
    		
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询Top50投资金额排行时："+e.getMessage());
			
			error.code = 0;
			error.msg = "查询Top50投资金额排行失败";
			
			return null;
		}
		
		count = count > 50 ? 50 : count;
		
		PageBean<v_bill_board> page = new PageBean<v_bill_board>();
		
		page.pageSize = 10;
		page.currPage = currPage;
		page.totalCount = count;
		
		page.page = investBillboard;
		
		error.code = 0;
		
		return page;
	}
	
	
	/**
	 * 根据标产品资料ID查出用户提交的对应资料
	 * @param itemId
	 * @return
	 */
	public static UserAuditItem getAuditItem(long itemId,long userId){
		
		String hql = "select audit_item_id from t_product_audit_items where id = ?";
		String sql = "select id from t_user_audit_items where user_id = ? and audit_item_id = ?";
		Long userItemId = 0l;
		Long productItemId = 0l;
		
		try {
			productItemId = t_product_audit_items.find(hql, itemId).first();
			userItemId = t_user_audit_items.find(sql, userId,productItemId).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		UserAuditItem item = new UserAuditItem();
		if(null != userItemId){
			item.id = userItemId;
		}
		return item;
	}
	
	
	/**
	 * 根据投资ID查询账单
	 * @param investId
	 * @param error
	 * @return
	 */
	public static Long queryBillByInvestId(long investId,ErrorInfo error){
		
		Long billId = 0l;
		
		try {
			billId = t_bills.find("select id from t_bill_invests where invest_id = ? ", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		error.code = 1;
		return billId;
	}
	

	
	
	/**
	 * ajax分页查询债权竞拍记录
	 * @param debtId
	 */
	public static PageBean<v_debt_auction_records> viewAuctionRecords(int pageNum, int pageSize,long debtId,ErrorInfo error){
		
		PageBean<v_debt_auction_records> page = new PageBean<v_debt_auction_records>();

		int currPage = pageNum;
		
		page.currPage = currPage;
		page.pageSize = pageSize;

		List<v_debt_auction_records> list = new ArrayList<v_debt_auction_records>();

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_DEBT_AUCTION_RECORDS);
		sql.append(" and t_invest_transfer_details.transfer_id=?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(debtId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_debt_auction_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((page.currPage - 1) * page.pageSize);
            query.setMaxResults(page.pageSize);
            list = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
		}
		
		page.page = list;
		error.code = 1;
		return page;
	}

	/**
	 * 取消债权关注
	 * @param debtId
	 * @param error
	 */
	public static void canaleBid(Long attentionId,ErrorInfo error){
		
		t_user_attention_bids attentionBid = null;
		
		try {
			attentionBid = t_user_attention_bids.findById(attentionId);
		} catch (Exception e) {
			Logger.error("查询关注的借款标时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询关注的借款标异常";
			
			return;
		}
		
		if(null != attentionBid){
			attentionBid.delete();
			error.code = 1;
			error.msg = "取消收藏借款标成功";
			
			return;
		}
		
		error.code = -2;
		error.msg = "查询关注的借款标异常";
		
		return;
	}
		
	/* 2014-11-15 */
	/**
	 * 借款合同
	 */
	public static String queryPact(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select pact from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 居间服务协议
	 */
	public static String queryIntermediaryAgreement(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select intermediary_agreement from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 保障涵
	 */
	public static String queryGuaranteeBid(long id) {
		if(id < 1)
			return "查看失败!";
		
		try {
			return t_invests.find("select guarantee_invest from t_invests where id = ?", id).first();
		} catch (Exception e) {
			e.printStackTrace();

			return "查看失败!";
		}
	}

	/**
	 * 生成借款合同（理财人）
	 * @param bidId
	 * @param error
	 */
	public static void creatInvestPact(long bidId,ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_INVEST;
		if(pact.is_use){
			List<Long> investIds = new ArrayList<Long>();
			String sql = "select id from t_invests where bid_id = ? and transfer_status <> -1";
			
			try {
				investIds = t_invests.find(sql, bidId).fetch();
				
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常";
				error.code = -1;
				return;
			}
			
			
			if(investIds.size() > 0){
				for(Long investId : investIds){
					String pact_no = investId + DateUtil.simple(new Date());
					creatSingleInvestPact(investId, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
					creatSingleGuaranteeInvest(investId,pact_no, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
					creatSingleIntermediaryAgreement(investId, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "创建平台协议失败";
						error.code = -1;
						return;
					}
				}
			}
			error.msg = "创建成功";
			error.code = 1;
			return;
		}else{
			error.msg = "平台协议未开启";
			error.code = 1;
			return;
		}
		
	}
	
	
	/**
	 * 根据单个投资记录生成理财合同
	 * @param investId
	 * @param error
	 */
	public static void creatSingleInvestPact(long investId, ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.BID_PACT_INVEST;
		
		t_users investUser = new t_users();
		t_users bidUser = new t_users();
		t_invests invest = new t_invests();
		t_bids bid = new t_bids();
		Double amount = 0.0;
		String company_name = "";
		Double sum = 0.0;

		String hql = "select sum(receive_corpus + receive_interest) from t_bill_invests where invest_id = ?";
		String sql1 = "select _value from t_system_options where _key = ?";
		String sql2 = "select sum(repayment_corpus + repayment_interest) from t_bills where bid_id = ? ";
		try {
			invest = t_invests.findById(investId);
			bid = t_bids.findById(invest.bid_id);
			investUser = t_users.findById(invest.user_id);
			bidUser = t_users.findById(bid.user_id);
			amount = t_bill_invests.find(hql, investId).first();
			company_name = t_system_options.find(sql1, "company_name").first();
			sum = t_bills.find(sql2, bid.id).first();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(null == amount){
			amount = 0.0;
		}
		
		String no = investId + DateUtil.simple(new Date());
		StringBuffer investTable = new StringBuffer(" <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>投资人名称</td><td>投资金额(人民币)</td><td>年利率</td>" +
				"<td>投资日期</td><td>本息合计总金额(人民币)</td></tr>");
		
		investTable.append("<tr height=\"30\">");
		investTable.append("<td>" + investUser.name + "</td>");
		investTable.append("<td>￥" + invest.amount + "</td>");
		investTable.append("<td>" + bid.apr + "%</td>");
		investTable.append("<td>" + DateUtil.dateToString1(invest.time) + "</td>");
		investTable.append("<td>" + amount + "</td>");
		investTable.append("</tr></table>");
		
		String content = pact.content;
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.LOAN_NAME, bidUser.reality_name)
		.replace(Templets.ID_NUMBER, bidUser.id_number)
		.replace(Templets.PACT_NO,no)
		.replace(Templets.COMPANY_NAME,company_name)
		.replace(Templets.DATE,DateUtil.dateToString(new Date()))
		.replace(Templets.INVEST_LIST,investTable.toString());
		
		Bid bidbusiness = new Bid();
		bidbusiness.auditBid = true;
		bidbusiness.id = bid.id;
		
		
		String repayTime = bidbusiness.isSecBid ? 
		           DateUtil.simple(new Date()) : 
	               ServiceFee.repayTime(bidbusiness.periodUnit, bidbusiness.period, 
	               (int)bidbusiness.repayment.id);
		           
		content = content.replace(Templets.PURPOSE_NAME,bidbusiness.purpose.name)
		.replace(Templets.AMOUNT,bidbusiness.amount + "")
		.replace(Templets.APR,bidbusiness.apr + "%")
		.replace(Templets.PERIOD,bidbusiness.period + "")
		.replace(Templets.PERIOD_UNIT,bidbusiness.strPeriodUnit)
		.replace(Templets.REPAYMENT_NAME,bidbusiness.repayment.name)
		.replace(Templets.CAPITAL_INTEREST_SUM,sum + "")
		.replace(Templets.REPAYMENT_TIME, repayTime);
		
		
		
		StringBuffer billTable = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"> <tr height=\"36\"><td>期数</td><td>应还时间</td>" +
				"<td>应还本金</td><td>应还利息</td><td>应还本息合计</td></tr>");
		
		List<t_bill_invests> bills = new ArrayList<t_bill_invests>();
		String strsql = " invest_id = ? ";
		long periodCount = 0;
		
		try {
			bills = t_bill_invests.find(strsql, investId).fetch();
			periodCount = t_bill_invests.count(strsql, investId);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("##,##0.00");
		
		if(bills.size() > 0){
			for(t_bill_invests bill : bills){
				billTable.append("<tr height=\"30\">");
				billTable.append("<td>" + bill.periods + "/" + periodCount +"</td>");
				billTable.append("<td>" + DateUtil.dateToString1(bill.receive_time) + "</td>");
				billTable.append("<td>" + myformat.format(bill.receive_corpus)  + "</td>");
				billTable.append("<td>" + myformat.format(bill.receive_interest) + "</td>");
				String temp = myformat.format(bill.receive_corpus + bill.receive_interest);
				billTable.append("<td>" + temp + "</td>");
				billTable.append("</tr>");
			}
			
		}
		billTable.append("</table>");
		
		content = content.replace(Templets.INVEST_BILL_LIST,billTable);
	    hql = "update t_invests set pact = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	
	}
	

	
	
	/**
	 * 针对单挑投标记录创建居间服务协议（投资人）
	 * @param investId
	 * @param error
	 */
	public static void creatSingleIntermediaryAgreement(long investId,ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.INTERMEDIARY_AGREEMENT_INVEST;
		t_users investUser = new t_users();
		t_invests invest = new t_invests();
		
		try {
			invest = t_invests.findById(investId);
			investUser = t_users.findById(invest.user_id);
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		String investRealityName = investUser.reality_name == null ? "" : investUser.reality_name;
		String investRealityIdno = investUser.id_number == null ? "" : investUser.id_number;

		String content = pact.content;
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.ID_NUMBER, investRealityIdno)
		.replace(Templets.INVEST_REALITY_NAME, investRealityName)
		.replace(Templets.DATE, DateUtil.dateToString1(new Date()));
		
		String hql = "update t_invests set intermediary_agreement = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	}

		
	
	/**
	 * 针对单挑记录生成对应保障函
	 * @param investId
	 * @param error
	 */
	public static void creatSingleGuaranteeInvest(long investId,String pact_no, ErrorInfo error){
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.GUARANTEE_INVEST;
		t_users investUser = new t_users();
		t_bids bid = new t_bids();
		t_invests invest = new t_invests();
		String company_name = "";
		String sql1 = "select _value from t_system_options where _key = ?";
		try {
			invest = t_invests.findById(investId);
			bid = t_bids.findById(invest.bid_id);
			investUser = t_users.findById(invest.user_id);
			company_name = t_system_options.find(sql1, "company_name").first();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		int period = bid.period;
		int periodUnit = bid.period_unit;
		String periodStr = "";
		if(periodUnit == -1){
			periodStr = period + "年";
		}else if(periodUnit == 1){
			periodStr = "1个月";
		}else{
			periodStr = period +"个月";
		}
		
		String investRealityName = investUser.reality_name == null ? "" : investUser.reality_name;
		
		String content = pact.content;
		DecimalFormat df = new DecimalFormat();
        df.applyPattern("###.00");
		content = content.replace(Templets.INVEST_NAME, investUser.name)
		.replace(Templets.INVEST_REALITY_NAME,investRealityName)
		.replace(Templets.CHINESE_AMOUNT, new CnUpperCaser(df.format(invest.amount)).getCnString())
		.replace(Templets.COMPANY_NAME,company_name)
		.replace(Templets.DATE,DateUtil.dateToString(new Date()))
		.replace(Templets.PACT_NO,pact_no)
		.replace(Templets.PERIOD, periodStr);
		
		String hql = "update t_invests set guarantee_invest = ? where id = ? ";
		EntityManager em = JPA.em();
		int rows = 0;
		try {
			rows = em.createQuery(hql).setParameter(1, content).setParameter(2, investId).executeUpdate();
		} catch (Exception e) {
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		if(rows == 0){
			error.msg = "系统异常";
			error.code = -1;
			return;
		}
		
		error.msg = "生成协议成功";
		error.code = 1;
		return;
	}
	
	
	/**
	 * 定时执行生成借款合同，理财合同等协议
	 */
	public static void creatBidPactJob(){
		ErrorInfo error = new ErrorInfo();
		List<Object> bidIds = new ArrayList<Object>();
		String sql = "select id from t_bids where status in (4,5) and (ISNULL(pact) and ISNULL(guarantee_bid) and ISNULL(intermediary_agreement))";
		Query query = JPA.em().createNativeQuery(sql);
		
		try {
			bidIds = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if(null == bidIds || bidIds.size() == 0)
			return ;
		
		Bid bid = null;
		long _bidId = 0;
		
		for(Object bidId : bidIds){
			bid = new Bid();
			bid.auditBidPact = true;
			_bidId = Long.parseLong(bidId.toString());
			
			try {
				bid.id = _bidId;
				
				bid.createPact();//生成借款合同
				Invest.creatInvestPact(_bidId, error);//生成理财合同
			} catch (Exception e) {
				continue ;
			}
		}
	}
	
	
	/**
	 * 定时执行生成债权协议
	 */
	public static void creatDebtPactJob(){
		
		ErrorInfo error = new ErrorInfo();
		List<Object> investIds = new ArrayList<Object>();
		String sql = "select id from t_invests where transfers_id > 0 and ISNULL(pact) and ISNULL(guarantee_invest) and ISNULL(intermediary_agreement) ";
		t_invests invest = new t_invests();
		t_invests originalInvest = new t_invests();
		t_invest_transfers debt = new t_invest_transfers();
		Query query = JPA.em().createNativeQuery(sql);
		try {
			investIds = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if(investIds.size() > 0){
			for(Object invesId : investIds){
				invest = t_invests.findById(Long.parseLong(invesId.toString()));
				debt = t_invest_transfers.findById(invest.transfers_id);
				originalInvest = t_invests.findById(debt.invest_id);
				long presentInvestUserId = invest.user_id;
				long investId = invest.id;
				long debtId = invest.transfers_id;
				long originalInvestUserId = originalInvest.user_id;
				
				Debt.creatDebtAgreement(originalInvestUserId, presentInvestUserId, debtId, investId, error);
			}
			return;
		}else{
			return;
		}
	}
	
	
	/**
	 * 查询前台最新三条理财资讯
	 * @return
	 */
	public static List<Map<String,String>> queryNearlyInvest(ErrorInfo error){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<t_invests> invests = new ArrayList<t_invests>();
		invests = t_invests.find(" order by id desc").fetch(Constants.NEW_FUNDRAISEING_BID);
		
		String userName = "";
		Long count = 0l;
		Double apr = 0.0;
		Map<String,String> map = null;
		for(t_invests invest : invests){
			map = new HashMap<String, String>();
			try {
				userName = t_users.find("select name from t_users where id = ? ", invest.user_id).first();
				apr = t_bids.find("select apr from t_bids where id = ? ", invest.bid_id).first();
				count = t_invests.find("select count(*) from t_invests where user_id = ? ", invest.user_id).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "查询最新理财资讯异常";
				error.code = -1;
				
				return null;
			}
			
			map = new HashMap<String, String>();
			map.put("id", invest.bid_id + "");
			map.put("userName", userName);
			map.put("count", count + "");
			map.put("apr", apr + "");
			map.put("amount", invest.amount + "");
			list.add(map);
		}
		
		return list;
	}
	
	/**
	 * 根据订单流水号查询交易记录是否存在
	 * @param pMerBillNo
	 * @param error
	 */
	public static long queryIsInvest(String pMerBillNo, ErrorInfo error) {
		error.clear();
		
		String sql = "select count(1) from t_invests where mer_bill_no = ? limit 1";
		long rows = 0;
		
		try {
			rows = ((BigInteger)JPA.em().createNativeQuery(sql).setParameter(1, pMerBillNo).getSingleResult()).longValue();
		}catch(Exception e) {
			Logger.error("根据订单流水号查询交易记录是否存在时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "根据订单流水号查询交易记录是否存在时";
			
			return -1;
		}
		
		error.code = 1;
		return rows;
	}
	
	/**
	 * 解冻投资金额
	 * @param pMerBillNo
	 * @param error
	 */
	public static void unfreezeInvest(String pMerBillNo, ErrorInfo error) {
		error.clear();
		
		long row = queryIsInvest(pMerBillNo, error);
		
		if(error.code < 0) {
			return ;
		}
		
		if(row == 0){
			error.code = 2;
			error.msg = "记录不存在，无需解冻";
			return;
		}
		
		t_invests invest = null;
		
		try {
			invest = t_invests.find("mer_bill_no = ?", pMerBillNo).first();
		}catch(Exception e) {
			Logger.error("解冻投资保证金失败"+e.getMessage());
			
			error.code = -1;
			error.msg = "查询投资金额失败";
			
			return;
		}
		
		DataSafety data = new DataSafety();
		
		data.id = invest.user_id;
		
		if(!data.signCheck(error)){
			JPA.setRollbackOnly();
			
			return ;
		}
		
		DealDetail.relieveFreezeFund(invest.user_id, invest.amount, error);

		if (error.code < 0) {
			
			return;
		}
		
		v_user_for_details forDetail = DealDetail.queryUserBalance(invest.user_id, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return;
		}
		
		double balance = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receiveAmount = forDetail.receive_amount;
		
		DealDetail dealDetail = null;
		
		/* 添加交易记录 */
		dealDetail = new DealDetail(invest.user_id,
				DealType.THAW_FREEZE_INVESTAMOUNT, invest.amount,
				invest.bid_id, balance, freeze, receiveAmount,
				"解冻投资金额" + invest.amount + "元");

		dealDetail.addDealDetail(error);
		
		if (error.code < 0) {
			return;
		}
		
		data.updateSignWithLock(invest.user_id, error);
		
		if (error.code < 0) {
			return;
		}
		
		error.code = 1;
		error.msg = "解冻金额成功";
	}
	
	/**
	 * 前台借款标条件分页查询(不显示已还)
	 * @param currPage
	 * @param pageSize
	 * @param _apr
	 * @param _amount
	 * @param _loanSchedule
	 * @param _startDate
	 * @param _endDate
	 * @param _loanType
	 * @param _creditLevel
	 * @return
	 */
	public static PageBean<v_front_all_bids> queryAllBidsNotRepay(int showType, int currPage,int pageSize,String _apr,String _amount,String _loanSchedule,String _startDate,String _endDate,String _loanType,String minLevelStr,String maxLevelStr,String _orderType,String _keywords,ErrorInfo error,String type){
		
		int apr = 0;
		int amount = 0;
		int loan_schedule = 0;
		int orderType = 0;
		int product_id = 0;
		int minLevel = 0;
		int maxLevel = 0;
		
		if (showType == Constants.SHOW_TYPE_1) {
			
			showType = 1;
		}
		
		if (showType == Constants.SHOW_TYPE_2) {
			
			showType = 2;
		}
		
		if (showType == Constants.SHOW_TYPE_3) {
			
			showType = 4;
		}
		
		
		List<v_front_all_bids> bidList = new ArrayList<v_front_all_bids>();
		PageBean<v_front_all_bids> page = new PageBean<v_front_all_bids>();

		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
        Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", _keywords);
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		if(StringUtils.isBlank(type)){
			sql.append(SQLTempletes.V_FRONT_J_BIDS);
		}else{
			sql.append(SQLTempletes.V_FRONT_H_BIDS);
		}
		
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(_apr) && StringUtils.isBlank(_amount) && StringUtils.isBlank(_loanSchedule) && StringUtils.isBlank(_startDate) && StringUtils.isBlank(_endDate) && StringUtils.isBlank(_loanType) && StringUtils.isBlank(minLevelStr) && StringUtils.isBlank(maxLevelStr) && StringUtils.isBlank(_orderType) &&  StringUtils.isBlank(_keywords)){
			
			try {
				//sql.append(" AND t_bids.status <> 5");
				sql.append(" AND t_bids.show_type&?<>0");
				params.add(showType);
				
				sql.append(" order by IF(loan_schedule>=100,0,IF(is_only_new_user > 0, 2, 1)) DESC,time desc");
				// TODO
				Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            bidList = query.getResultList();
	            System.out.println(sql.toString());
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
		}
		
			if (NumberUtil.isNumericInt(_apr)) {
				apr = Integer.parseInt(_apr);
			}
			
			if (apr < 0 || apr > 4) {
				sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
			}
			
			if (NumberUtil.isNumericInt(_amount)) {
				amount = Integer.parseInt(_amount);
			}
			
			if(!StringUtils.isBlank(_keywords)){
				sql.append(" and (t_bids.title like ?) ");// or t_bids.id like ?
				params.add("%"+_keywords+"%");
//				_keywords = _keywords.replace(obj + "", "");
//				params.add("%"+_keywords+"%");
			}
			
			if (amount < 0 || amount > 5) {
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[amount]);
			}
			
			if( NumberUtil.isNumericInt(_loanSchedule)) {
				 loan_schedule = Integer.parseInt(_loanSchedule);
			}
			
			 if(loan_schedule < 0 || loan_schedule > 4){
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
			 }else{
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
			 }
				 
			if (NumberUtil.isNumericInt(_loanType)) {
				 product_id = Integer.parseInt(_loanType);
				if(product_id > 0){
					sql.append(" and t_products.id = ? ");
					params.add(product_id);
				}
				
			}
		
			if(NumberUtil.isNumericInt(minLevelStr)){
				 minLevel = Integer.parseInt(minLevelStr);
				if(minLevel > 0){
					 sql.append(" AND t_users.credit_level_id = ?");
					 params.add(minLevel);
				}
				
			}
			

			if(NumberUtil.isNumericInt(maxLevelStr)){
				 maxLevel = Integer.parseInt(maxLevelStr);
				if(maxLevel > 0){
					 sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
					 params.add(maxLevel);
				}
				
			}
		
			if( !StringUtils.isBlank(_startDate) &&  !StringUtils.isBlank(_endDate)){
				 sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
				 params.add(DateUtil.strDateToStartDate(_startDate));
				 params.add(DateUtil.strDateToEndDate(_endDate));
			}
			
			//sql.append(" AND t_bids.status <> 5");
			sql.append(" AND t_bids.show_type&?<>0");
			params.add(showType);
			
			if(NumberUtil.isNumericInt(_orderType)){
				 orderType = Integer.parseInt(_orderType);
			}
			
			if(orderType < 0 || orderType > 10){
				sql.append(Constants.BID_ORDER_CONDITION[0]);
			}else{
				sql.append(Constants.BID_ORDER_CONDITION[orderType]);
			}
			
			conditionMap.put("apr", apr);
			conditionMap.put("amount", amount);
			conditionMap.put("loanSchedule", loan_schedule);
			conditionMap.put("startDate", _startDate);
			conditionMap.put("endDate", _endDate);
			conditionMap.put("minLevel", minLevel);
			conditionMap.put("maxLevel", maxLevel);
			conditionMap.put("orderType", orderType);
			conditionMap.put("loanType", product_id);
			
		try {
            Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}
		
		error.code = 1;
		error.msg = "查询成功";
		page.page = bidList;
		page.conditions = conditionMap;
		
		return page;
	}
	public static PageBean<v_front_all_bids> queryAllBidsNotRepayV2(int showType, int currPage,int pageSize,String _apr,String _amount,String _loanSchedule,String _startDate,String _endDate,String _loanType,String minLevelStr,String maxLevelStr,String _orderType,String _keywords,ErrorInfo error,String type){
		
		int apr = 0;
		int amount = 0;
		int loan_schedule = 0;
		int orderType = 0;
		int product_id = 0;
		int minLevel = 0;
		int maxLevel = 0;
		
		if (showType == Constants.SHOW_TYPE_1) {
			
			showType = 1;
		}
		
		if (showType == Constants.SHOW_TYPE_2) {
			
			showType = 2;
		}
		
		if (showType == Constants.SHOW_TYPE_3) {
			
			showType = 4;
		}
		
		
		List<v_front_all_bids> bidList = new ArrayList<v_front_all_bids>();
		PageBean<v_front_all_bids> page = new PageBean<v_front_all_bids>();
		
		EntityManager em = JPA.em();
		String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
		obj = obj == null ? "" : obj;
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", _keywords);
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		if(StringUtils.isBlank(type)){
			
			sql.append(SQLTempletes.V_FRONT_J_BIDS);
		}else{
			sql.append(SQLTempletes.V_FRONT_H_BIDS);
		}
		
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(_apr) && StringUtils.isBlank(_amount) && StringUtils.isBlank(_loanSchedule) && StringUtils.isBlank(_startDate) && StringUtils.isBlank(_endDate) && StringUtils.isBlank(_loanType) && StringUtils.isBlank(minLevelStr) && StringUtils.isBlank(maxLevelStr) && StringUtils.isBlank(_orderType) &&  StringUtils.isBlank(_keywords)){
			
			try {
				//sql.append(" AND t_bids.status <> 5");
				sql.append(" AND t_bids.show_type&?<>0");
				params.add(showType);
				
				sql.append(" order by IF(loan_schedule>=100,0,1) DESC,time desc");
				
				Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
				for(int n = 1; n <= params.size(); n++){
					query.setParameter(n, params.get(n-1));
				}
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
				bidList = query.getResultList();
				System.out.println(sql.toString());
				
				page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
				
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
		}
		
		if (NumberUtil.isNumericInt(_apr)) {
			apr = Integer.parseInt(_apr);
		}
		
		if (apr < 0 || apr > 4) {
			sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
		}else{
			sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
		}
		
		if (NumberUtil.isNumericInt(_amount)) {
			amount = Integer.parseInt(_amount);
		}
		
		if(!StringUtils.isBlank(_keywords)){
			sql.append(" and (t_bids.title like ?) ");// or t_bids.id like ?
			params.add("%"+_keywords+"%");
//				_keywords = _keywords.replace(obj + "", "");
//				params.add("%"+_keywords+"%");
		}
		
		if (amount < 0 || amount > 5) {
			sql.append(SQLTempletes.BID_AMOUNT_CONDITION[0]);// 全部范围
		}else{
			sql.append(SQLTempletes.BID_AMOUNT_CONDITION[amount]);
		}
		
		if( NumberUtil.isNumericInt(_loanSchedule)) {
			loan_schedule = Integer.parseInt(_loanSchedule);
		}
		
		if(loan_schedule < 0 || loan_schedule > 4){
			sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
		}else{
			sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
		}
		
		if (NumberUtil.isNumericInt(_loanType)) {
			product_id = Integer.parseInt(_loanType);
			if(product_id > 0){
				sql.append(" and t_products.id = ? ");
				params.add(product_id);
			}
			
		}
		
		if(NumberUtil.isNumericInt(minLevelStr)){
			minLevel = Integer.parseInt(minLevelStr);
			if(minLevel > 0){
				sql.append(" AND t_users.credit_level_id = ?");
				params.add(minLevel);
			}
			
		}
		
		
		if(NumberUtil.isNumericInt(maxLevelStr)){
			maxLevel = Integer.parseInt(maxLevelStr);
			if(maxLevel > 0){
				sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
				params.add(maxLevel);
			}
			
		}
		
		if( !StringUtils.isBlank(_startDate) &&  !StringUtils.isBlank(_endDate)){
			sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
			params.add(DateUtil.strDateToStartDate(_startDate));
			params.add(DateUtil.strDateToEndDate(_endDate));
		}
		
		//sql.append(" AND t_bids.status <> 5");
		sql.append(" AND t_bids.show_type&?<>0");
		params.add(showType);
		
		if(NumberUtil.isNumericInt(_orderType)){
			orderType = Integer.parseInt(_orderType);
		}
		
		if(orderType < 0 || orderType > 10){
			sql.append(Constants.BID_ORDER_CONDITION[0]);
		}else{
			sql.append(Constants.BID_ORDER_CONDITION[orderType]);
		}
		
		conditionMap.put("apr", apr);
		conditionMap.put("amount", amount);
		conditionMap.put("loanSchedule", loan_schedule);
		conditionMap.put("startDate", _startDate);
		conditionMap.put("endDate", _endDate);
		conditionMap.put("minLevel", minLevel);
		conditionMap.put("maxLevel", maxLevel);
		conditionMap.put("orderType", orderType);
		conditionMap.put("loanType", product_id);
		
		try {
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			for(int n = 1; n <= params.size(); n++){
				query.setParameter(n, params.get(n-1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			bidList = query.getResultList();
			
			page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
			
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}
		
		error.code = 1;
		error.msg = "查询成功";
		page.page = bidList;
		page.conditions = conditionMap;
		
		return page;
	}
	
	/**
	 * app查询公告
	 * @return
	 */
	public static List<t_bid_publish> getPublicList(){
		List<t_bid_publish> list = new ArrayList<t_bid_publish>();
		
		try {
			Query query = JPA.em().createNativeQuery("SELECT p.id as id,p.bid_title as bid_title,p.product_id as product_id,p.product_name as product_name,p.amount as amount,p.period_unit as period_unit,p.period as period,p.apr as apr,p.repayment_type_id as repayment_type_id,p.repayment_type_name as repayment_type_name,p.publish_time as publish_time,p.create_time as create_time from t_bid_publish p where p.publish_time > ? and NOT EXISTS(SELECT s.id from t_bids s where s.title = p.bid_title and s.amount = p.amount and p.product_id = s.product_id and s.status IN (1, 2,3,4))",t_bid_publish.class).setParameter(1, new Date());
			list = query.getResultList();
		} catch (Exception e) {
			Logger.info("app查询借款公告错误",e);
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 修改冻结流水号
	 * @author yangxuan
	 * @date 2015-07-06
	 * @param ipsBillNo
	 */
	public static void modifyInvestIpsBillNo(String merBillNo,String ipsBillNo,ErrorInfo error){
		error.clear();
		
		try{
	
			t_invests invest = t_invests.find("mer_bill_no = ?", merBillNo).first();
			if(invest != null){
				invest.ips_bill_no = ipsBillNo;
				invest.save();
			} 
		}catch(Exception e){
			
			Logger.error("####修改冻结流水号时 : %s", e.getMessage());
			error.code = -1;
			error.msg = "系统异常,请联系管理员.";
		}
	}
	
	/**
	 * 通过BidId查询投资记录
	 * @param bidId
	 * @return
	 */
	public static List<t_invests> queryInvestByBidId(long bidId){
		List<t_invests> list = t_invests.find("bid_id = ? order by id", bidId).fetch();
		return list;
	}
	
	/**
	 * 该投资是否是债权转让所得
	 * @param investId  投资id
	 * @return true 是 ， false 否
	 */
	public static boolean isSecondCretansfer(long investId){
		
		String sql = "select it.id from t_invest_transfers it LEFT JOIN t_invests i on it.id = i.transfers_id where status = 3 AND i.id = ?";

		Query query = JPA.em().createNativeQuery(sql).setParameter(1, investId);
		
		List list = null;
		
		try{
			list = query.getResultList();
		}catch(Exception e){
			
			return false;
		}
		
		if(list == null || list.size() <= 0 || list.get(0) == null){
			return false;
		}

		return true;
	}
	
	/**
	 * 查询用户收益
	 * @param userId
	 * @return
	 */
	public static double getProfit(long userId){
		// 实际到账累计收益
		/*String sql ="select IFNULL(SUM(b.receive_interest) + SUM(overdue_fine),0) as money from t_bill_invests b where b.user_id = ? and b.status in (-3, -4, 0)";
		
		Object money = 0;
		
		try{
			money = JPAUtil.createNativeQuery(sql, userId).getSingleResult();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return Double.parseDouble(money.toString());*/
		
		// 实际到账累计收益+预计累计收益 
		// 实际到账收益 wangyun 20180802
		String sql ="select SUM(b.real_receive_interest) as money from t_bill_invests b where b.user_id = ? and b.status in (0,-3,-4,-7)";
		String sql_ = "select SUM(b.real_receive_interest) as money from t_debt_bill_invest b where b.user_id = ? and b.status in (0,-3,-4) ";
		double money = 0;
		double money_ = 0;//债权收益
		try{
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql).setParameter(1, userId);
			Query query_ = em.createNativeQuery(sql_).setParameter(1, userId);
			try {
				money = query.getResultList().get(0) == null ? 0 : Double.parseDouble(query.getResultList().get(0).toString());
				money_ = query_.getResultList().get(0) == null ? 0 : Double.parseDouble(query_.getResultList().get(0).toString());
			} catch (Exception e) {
				Logger.error("查询实际到账收益：" + e.getMessage());
				
				return 0d;
			}
 
		}catch(Exception e){
			return money + money_;
		}
		return Arith.round(money + money_, 2);
	}
	/**
	 * 查询用户当月收益
	 * @param userId
	 * @return
	 */
	public static String getCurrentMonthProfit(long userId){
		Date date = new Date();
 		String dateToString  = DateUtil.dateToString1(date);
		String sql ="select SUM(ts.receive_interest+ts.receive_corpus) as money from t_bill_invests ts LEFT JOIN t_bids b on ts.bid_id=b.id " +
				"where ts.user_id = ? and ts.status in(-1,-2,-5,-6) and (( LEFT (b.repayment_time, 7) = '"+dateToString.substring(0, 7)+"'  and (b.repayment_type_id=1 or b.repayment_type_id=2)) or (b.repayment_type_id=3)) " ;
		
		Object sum = null;
		try {
			sum = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		}catch(Exception e){
			Logger.error("查询用户当月收益时，%s", e.getMessage());
			return "";
		}
		if(sum==null){
			return "0";
		}
		return sum.toString();
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. 2016年12月5日 上午10:26:05 
	 * @description.  app-首页-热门推荐
	 * 
	 * @param length
	 * @return
	 */
	public static List<v_front_all_bids> queryHotBids(int length){
		
		List<v_front_all_bids> bids = null;
		StringBuffer sql = new StringBuffer(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_BIDS);
		sql.append(" and t_bids.loan_schedule<100 and t_bids.is_only_new_user = 0 order by IF(t_bids.loan_schedule>=100,0,1) DESC, t_bids.id asc");
		
		try {
			EntityManager em = JPA.em();
			
			Query query = em.createNativeQuery(sql.toString(),v_front_all_bids.class);
			query.setMaxResults(length);
			bids = query.getResultList();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bids != null ? bids : new ArrayList<v_front_all_bids>();
	}
	
	public static List<Map<String,Object>> findInvestsForRedPack(String start,String end,double amount){
		String sql = "select i.user_id,sum(i.amount) as amount from t_invests i join t_bids b on i.bid_id = b.id  "
				+ "where i.time >= ? and i.time<=? and b.status in(?,?) group by i.user_id having amount >= ?";
		
		List<Object> p = new ArrayList<Object>();
		p.add(DateUtil.strDateToStartDate(start));
		p.add(DateUtil.strDateToEndDate(end));
		p.add(Constants.BID_REPAYMENT);
		p.add(Constants.BID_COMPENSATE_REPAYMENT);
		p.add(amount);
		
		return JPAUtil.getList(new ErrorInfo(), sql, p.toArray());
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. 2017年5月24日 下午4:26:09 
	 * @description.  查询电子合同
	 * 
	 * @param id
	 * @return
	 */
	public static Map<String, Object> findInvest(long id){
		String sql = "select pact_location,certificate_url as certificateUrl from t_invests where id = ?";
		return JPAUtil.getMap(new ErrorInfo(), sql, id);
	}
	
	public static Map<String, Object> findInvestForId(long id){
		String sql = "select i.certificate_url as certificateUrl,u.id_number as idNumber from t_invests i join t_users u on i.user_id = u.id where i.id = ?";
		return JPAUtil.getMap(new ErrorInfo(), sql, id);
	}
	
	/**
	 * 前台借款标条件分页查询(不显示已还)
	 * @param currPage
	 * @param pageSize
	 * @param _apr
	 * @param _amount
	 * @param _loanSchedule
	 * @param _startDate
	 * @param _endDate
	 * @param _loanType
	 * @param _creditLevel
	 * @return
	 */
	public static PageBean<v_front_all_bids_v2> queryAllBidsNotRepayV3(int showType, int currPage,int pageSize,String _apr,String _amount,String _loanSchedule,String _startDate,String _endDate,String _loanType,String minLevelStr,String maxLevelStr,String _orderType,String _keywords,ErrorInfo error,String type){
		
		int apr = 0;
		int amount = 0;
		int loan_schedule = 0;
		int orderType = 0;
		int product_id = 0;
		int minLevel = 0;
		int maxLevel = 0;
		
		if (showType == Constants.SHOW_TYPE_1) {
			
			showType = 1;
		}
		
		if (showType == Constants.SHOW_TYPE_2) {
			
			showType = 2;
		}
		
		if (showType == Constants.SHOW_TYPE_3) {
			
			showType = 4;
		}
		
		
		List<v_front_all_bids_v2> bidList = new ArrayList<v_front_all_bids_v2>();
		PageBean<v_front_all_bids_v2> page = new PageBean<v_front_all_bids_v2>();

		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
        Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keywords", _keywords);
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		//加息
		sql.append("`t_bids`.`is_debt_transfer` AS `is_debt_transfer` , ");
		sql.append(" if(t_bids.`status` <3 ,CASE when activity1.rate is null THEN  CAST(t_bids.is_increase_rate AS signed)  else 1 end,  CAST(t_bids.is_increase_rate AS signed)) isIncreaseRate,if(t_bids.`status` <3 ,IFNULL(activity1.rate,`t_bids`.`increase_rate`),`t_bids`.`increase_rate`) AS `increaseRate`,if(t_bids.`status` <3 ,CASE when activity1.rate is null then IFNULL(`t_bids`.`increase_rate_name`,'') else activity1.`NAME` end,`t_bids`.`increase_rate_name`)  AS `increaseRateName`,activity1.`name` name1,activity1.rate rate1,activity2.`name` name2,IF (t_bids.`status` < 3,activity2.rate,0) rate2,activity3.`name` name3,IF (t_bids.`status` < 3,activity3.rate,0) rate3, ");
		if(StringUtils.isBlank(type)){
			String V_FRONT_J_BIDS = "`t_bids`.`id` AS `id`,`t_bids`.`tag` AS `tag`,`t_bids`.`is_only_new_user` AS `is_only_new_user`,t_bids.min_invest_amount as min_invest_amount,t_bids.average_invest_amount as average_invest_amount,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`show_type` AS `show_type`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`status` AS `status`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`period` AS `period`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,t_bids.repayment_time AS repayment_time,concat (( SELECT `t_system_options`.`_value` AS `_value` FROM `t_system_options` WHERE (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_bids`.`repayment_type_id` AS `repayment_type_id`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,t_users.credit_level_id AS credit_level_id,`t_bids`.`time` AS `time` from `t_bids` LEFT JOIN `t_products` ON `t_products`.`id` = `t_bids`.`product_id` LEFT JOIN t_users ON t_bids.user_id = t_users.id LEFT JOIN `t_agencies` ON `t_agencies`.`id` = `t_bids`.`agency_id` LEFT JOIN `t_dict_bid_repayment_types` ON `t_dict_bid_repayment_types`.`id` = `t_bids`.`repayment_type_id` LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 1 LIMIT 1 ) activity1 ON TRUE LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 2 LIMIT 1 ) activity2 ON TRUE LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 3 LIMIT 1 ) activity3 ON TRUE where `t_bids`.`status` IN (1, 2, 3, 4,5,14) and (t_bids.product_id != 5 OR (t_bids.product_id = 5 and t_bids.is_show_member_bid = 0))";
			sql.append(V_FRONT_J_BIDS);
		}else{
			String V_FRONT_H_BIDS = "`t_bids`.`id` AS `id`,`t_bids`.`tag` AS `tag`,`t_bids`.`is_only_new_user` AS `is_only_new_user`,t_bids.min_invest_amount as min_invest_amount,t_bids.average_invest_amount as average_invest_amount,`t_products`.`name_image_filename` AS `product_filename`,`t_products`.`name` AS `product_name`,`t_bids`.`show_type` AS `show_type`,`t_bids`.`title` AS `title`,`t_bids`.`amount` AS `amount`,`t_bids`.`status` AS `status`,`t_bids`.`user_id` AS `user_id`,`t_bids`.`period` AS `period`,`t_bids`.`apr` AS `apr`,`t_bids`.`is_hot` AS `is_hot`,`t_bids`.`period_unit` AS `period_unit`,`t_bids`.`is_agency` AS `is_agency`,`t_agencies`.`name` AS `agency_name`,`t_bids`.`has_invested_amount` AS `has_invested_amount`,`t_bids`.`image_filename` AS `bid_image_filename`,`t_products`.`small_image_filename` AS `small_image_filename`,`t_bids`.`loan_schedule` AS `loan_schedule`,`t_bids`.`bonus_type` AS `bonus_type`,`t_bids`.`bonus` AS `bonus`,t_bids.repayment_time AS repayment_time,concat (( SELECT `t_system_options`.`_value` AS `_value` FROM `t_system_options` WHERE (`t_system_options`.`_key` = 'loan_number')),(`t_bids`.`id` + '')) AS `no`,`t_bids`.`award_scale` AS `award_scale`,`t_bids`.`repayment_type_id` AS `repayment_type_id`,`t_dict_bid_repayment_types`.`name` AS `repay_name`,`t_bids`.`is_show_agency_name` AS `is_show_agency_name`,`t_products`.`id` AS `product_id`,t_users.credit_level_id AS credit_level_id,`t_bids`.`time` AS `time` from `t_bids` LEFT JOIN `t_products` ON `t_products`.`id` = `t_bids`.`product_id` LEFT JOIN t_users ON t_bids.user_id = t_users.id LEFT JOIN `t_agencies` ON `t_agencies`.`id` = `t_bids`.`agency_id` LEFT JOIN `t_dict_bid_repayment_types` ON `t_dict_bid_repayment_types`.`id` = `t_bids`.`repayment_type_id` LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 1 LIMIT 1 ) activity1 ON TRUE LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 2 LIMIT 1 ) activity2 ON TRUE LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 3 LIMIT 1 ) activity3 ON TRUE where `t_bids`.`status` IN (1, 2, 3, 4,5,14) and t_bids.product_id = 5 and t_bids.is_show_member_bid = 1";
			sql.append(V_FRONT_H_BIDS);
		}
		
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(_apr) && StringUtils.isBlank(_amount) && StringUtils.isBlank(_loanSchedule) && StringUtils.isBlank(_startDate) && StringUtils.isBlank(_endDate) && StringUtils.isBlank(_loanType) && StringUtils.isBlank(minLevelStr) && StringUtils.isBlank(maxLevelStr) && StringUtils.isBlank(_orderType) &&  StringUtils.isBlank(_keywords)){
			
			try {
				//sql.append(" AND t_bids.status <> 5");
				sql.append(" AND t_bids.show_type&?<>0");
				params.add(showType);
				
				sql.append(" order by IF(loan_schedule>=100,0,IF(is_only_new_user > 0, 2, 1)) DESC,time desc");
				// TODO
				Query query = em.createNativeQuery(sql.toString(),v_front_all_bids_v2.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            bidList = query.getResultList();
	            System.out.println(sql.toString());
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "系统异常，给您带来的不便敬请谅解！";
				error.code = -1;
			}
			page.page = bidList;
			error.code = 1;
			error.msg = "查询成功";
			return page;
		}
		
			if (NumberUtil.isNumericInt(_apr)) {
				apr = Integer.parseInt(_apr);
			}
			
			if (apr < 0 || apr > 4) {
				sql.append(SQLTempletes.BID_APR_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_APR_CONDITION[apr]);
			}
			
			if (NumberUtil.isNumericInt(_amount)) {
				amount = Integer.parseInt(_amount);
			}
			
			if(!StringUtils.isBlank(_keywords)){
				sql.append(" and (t_bids.title like ?) ");// or t_bids.id like ?
				params.add("%"+_keywords+"%");
//				_keywords = _keywords.replace(obj + "", "");
//				params.add("%"+_keywords+"%");
			}
			
			if (amount < 0 || amount > 5) {
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[0]);// 全部范围
			}else{
				sql.append(SQLTempletes.BID_AMOUNT_CONDITION[amount]);
			}
			
			if( NumberUtil.isNumericInt(_loanSchedule)) {
				 loan_schedule = Integer.parseInt(_loanSchedule);
			}
			
			 if(loan_schedule < 0 || loan_schedule > 4){
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[0]);//全部范围
			 }else{
				 sql.append(SQLTempletes.BID_LOAN_SCHEDULE_CONDITION[loan_schedule]);
			 }
				 
			if (NumberUtil.isNumericInt(_loanType)) {
				 product_id = Integer.parseInt(_loanType);
				if(product_id > 0){
					sql.append(" and t_products.id = ? ");
					params.add(product_id);
				}
				
			}
		
			if(NumberUtil.isNumericInt(minLevelStr)){
				 minLevel = Integer.parseInt(minLevelStr);
				if(minLevel > 0){
					 sql.append(" AND t_users.credit_level_id = ?");
					 params.add(minLevel);
				}
				
			}
			

			if(NumberUtil.isNumericInt(maxLevelStr)){
				 maxLevel = Integer.parseInt(maxLevelStr);
				if(maxLevel > 0){
					 sql.append(" and ? <= `f_credit_levels`(`t_bids`.`user_id`)");
					 params.add(maxLevel);
				}
				
			}
		
			if( !StringUtils.isBlank(_startDate) &&  !StringUtils.isBlank(_endDate)){
				 sql.append(" and t_bids.repayment_time >= ? and  t_bids.repayment_time <= ? ");
				 params.add(DateUtil.strDateToStartDate(_startDate));
				 params.add(DateUtil.strDateToEndDate(_endDate));
			}
			
			//sql.append(" AND t_bids.status <> 5");
			sql.append(" AND t_bids.show_type&?<>0");
			params.add(showType);
			
			if(NumberUtil.isNumericInt(_orderType)){
				 orderType = Integer.parseInt(_orderType);
			}
			
			if(orderType < 0 || orderType > 10){
				sql.append(Constants.BID_ORDER_CONDITION[0]);
			}else{
				sql.append(Constants.BID_ORDER_CONDITION[orderType]);
			}
			
			conditionMap.put("apr", apr);
			conditionMap.put("amount", amount);
			conditionMap.put("loanSchedule", loan_schedule);
			conditionMap.put("startDate", _startDate);
			conditionMap.put("endDate", _endDate);
			conditionMap.put("minLevel", minLevel);
			conditionMap.put("maxLevel", maxLevel);
			conditionMap.put("orderType", orderType);
			conditionMap.put("loanType", product_id);
			
		try {
            Query query = em.createNativeQuery(sql.toString(),v_front_all_bids_v2.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}
		
		error.code = 1;
		error.msg = "查询成功";
		page.page = bidList;
		page.conditions = conditionMap;
		
		return page;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2018年4月4日
	 * @description 置空pact_location
	 * @param id
	 * @param error
	 */
	public static void updatePact(long id,ErrorInfo error){
		EntityManager em = JPA.em();
		int rows = em.createQuery("update t_invests set pact_location = null where id = ?").setParameter(1, id).executeUpdate();
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
		}
		
		error.code = 1;
	}
	
	public static PageBean<Map<String, Object>> findUserAllInvests(Params params, Integer pageSize, Integer currPage) {
		/*
		 * select bi.invest_id,
			iuser.name, iuser.reality_name,iuser.mobile
			,ruser.name as rname, ruser.mobile as rmobile
			,(bi.receive_corpus+bi.receive_interest+bi.receive_increase_interest) as receive_corpus
			,bi.receive_time,bi.real_receive_time
			,IF(bi.status = 1 or bi.status = -3 or bi.status = -4,"已收款","未收款") as status,bi.status as s
			,CONCAT(bi.periods,"/",b.period) as periods
			,b.title,buser.name as bname,buser.reality_name as b_reality_name
			from t_bill_invests bi
			left join t_users iuser on iuser.id = bi.user_id
			left join t_users ruser on ruser.id = iuser.recommend_user_id
			left join t_bids b on b.id = bi.bid_id
			left join t_users buser on buser.id = b.user_id
			
			where iuser.name = '陈学贤'
			#order by bi.invest_id,bi.periods desc
			order by bi.receive_time desc
			limit 100
		 */
		pageSize = pageSize == null || pageSize == 0 ? 10 : pageSize;
		currPage = currPage == null || currPage == 0 ? 1 : currPage;
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		String name = params.get("name");
		String rname = params.get("rname");
		String receive_time_start = params.get("receive_time_start");
		String receive_time_end = params.get("receive_time_end");
		String real_receive_time_start = params.get("real_receive_time_start");
		String real_receive_time_end = params.get("real_receive_time_end");
		String status = params.get("status");
		String bname = params.get("bname");
		String title = params.get("title");
		
		conditionMap.put("name", name);
		conditionMap.put("rname", rname);
		conditionMap.put("receive_time_start", receive_time_start);
		conditionMap.put("receive_time_end", receive_time_end);
		
		conditionMap.put("real_receive_time_start", real_receive_time_start);
		conditionMap.put("real_receive_time_end", real_receive_time_end);
		
		conditionMap.put("status", status);
		conditionMap.put("bname", bname);
		conditionMap.put("title", title);
		
		String orderType = params.get("orderType");
		orderType = StringUtils.isBlank(orderType) ? "1" : orderType;
		conditionMap.put("orderType", orderType);
		String orderBySql = " order by bi.real_receive_time ".concat("1".equals(orderType) ? "desc" : "asc");
		
		
		List<Object> p = new ArrayList<Object>();

		StringBuffer sqlSelect = new StringBuffer("select bi.invest_id,iuser.name, iuser.reality_name,iuser.mobile,ruser.name as rname, ruser.mobile as rmobile");
		sqlSelect.append(",(bi.receive_corpus+bi.receive_interest+bi.receive_increase_interest) as receive_corpus");
		sqlSelect.append(",bi.receive_time,bi.real_receive_time");
		sqlSelect.append(",IF(bi.status = 0 or bi.status = -3 or bi.status = -4,\"已收款\",\"未收款\") as status,bi.status as s");
		//sqlSelect.append(",CONCAT(bi.periods,\"/\",b.period) as periods,b.title,buser.name as bname,buser.reality_name as b_reality_name");
		sqlSelect.append(",CONCAT(bi.periods,\"/\",(select periods from t_bill_invests where invest_id = bi.invest_id order by periods desc limit 1)) as periods,b.title,buser.name as bname,buser.reality_name as b_reality_name");

		StringBuffer sqlFrom = new StringBuffer(" from t_bill_invests bi");
		sqlFrom.append(" left join t_users iuser on iuser.id = bi.user_id");
		sqlFrom.append(" left join t_users ruser on ruser.id = iuser.recommend_user_id");
		sqlFrom.append(" left join t_bids b on b.id = bi.bid_id");
		sqlFrom.append(" left join t_users buser on buser.id = b.user_id");
		sqlFrom.append(" WHERE 1 = 1");
		
		if(StringUtils.isNotBlank(name)) {
			sqlFrom.append(" and iuser.name = ?");
			p.add(name);
		}
		if(StringUtils.isNotBlank(rname)) {
			sqlFrom.append(" and ruser.name = ?");
			p.add(rname);
		}
		if(StringUtils.isNotBlank(receive_time_start)){
			sqlFrom.append(" and bi.receive_time >= ?");
			p.add(DateUtil.strDateToStartDate(receive_time_start));
		}
		if(StringUtils.isNotBlank(receive_time_end)){
			sqlFrom.append(" and bi.receive_time <= ?");
			p.add(DateUtil.strDateToEndDate(receive_time_end));
		}
		// 
		if(StringUtils.isNotBlank(real_receive_time_start)){
			sqlFrom.append(" and bi.real_receive_time >= ?");
			p.add(DateUtil.strDateToStartDate(real_receive_time_start));
		}
		if(StringUtils.isNotBlank(real_receive_time_end)){
			sqlFrom.append(" and bi.real_receive_time <= ?");
			p.add(DateUtil.strDateToEndDate(real_receive_time_end));
		}
		//
		
		if(StringUtils.isNotBlank(status)) {
			if("1".equals(status)) {
				sqlFrom.append(" and bi.status in (0, -3, -4)");
			}else {
				sqlFrom.append(" and bi.status not in (0, -3, -4)");
			}
		}
		if(StringUtils.isNotBlank(bname)) {
			sqlFrom.append(" and buser.name = ?");
			p.add(bname);
		}
		if(StringUtils.isNotBlank(title)) {
			sqlFrom.append(" and b.title = ?");
			p.add(title);
		}
		
		
		String listSql = sqlSelect.toString().concat(sqlFrom.toString()).concat(orderBySql).concat(" LIMIT ?,?");
		String cntSql = "SELECT count(*) as count".concat(sqlFrom.toString());
		Logger.info("客服功能---->投资账单sql: " + listSql);
		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, p.toArray());
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		p.add((currPage - 1) * pageSize);
		p.add(pageSize);
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, p.toArray());

		PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = list;

		return page;
	}
	
	
	public static PageBean<v_front_debt_bids> queryDebtsNotRepay(Integer currPage, Integer pageSize, ErrorInfo error){
		pageSize = pageSize == null || pageSize == 0 ? 10 : pageSize;
		currPage = currPage == null || currPage == 0 ? 1 : currPage;
		List<Object> p = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT t.id as id, t.title AS title,t.debt_amount as amount, t.no as no, t.user_id as debt_user_id, ");
		sql.append("t.invest_id AS invest_id,");
		sql.append("t.apr AS apr,t.period as period, ");
		sql.append("b.period AS bid_period,");
		sql.append("b.period_unit AS period_unit,");
		sql.append("case when b.repayment_type_id = 1 then '按月还款、等额本息' when b.repayment_type_id = 2 then '按月付息、到期还本' when b.repayment_type_id = 3 then '一次性还款' end  as repayment_type,");
		sql.append("t.loan_schedule as loan_schedule,");
		sql.append("t.has_invested_amount as has_invested_amount,");
		sql.append("t.deadline as deadline,");
		sql.append("case when t.status=2 then 2 "
						+ "when b.status = 4 then 4 "
						+ "else b.status end as status,");//1,2:销售中,3:审核中,4:回款中,5:已回款
		//t.status标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）
		sql.append(" (select count(1) from t_debt_invest i where i.debt_id = t.id ) as debtCount,");
		sql.append("t.is_only_new_user as is_only_new_user ");

		StringBuffer sqlFrom = new StringBuffer();
		sqlFrom.append("from t_debt_transfer t ");
		sqlFrom.append("LEFT JOIN t_bids b on t.bid_id = b.id"); 
		sqlFrom.append(" where t.status in (2,3) "); 
		sqlFrom.append(" order by t.status asc, t.time desc"); 
		
		//String listSql = sql.toString().concat(" LIMIT ?,?");
		String cntSql = "SELECT count(*) as count ".concat(sqlFrom.toString());
		
		Logger.info("债权标的投资列表sql：" + sql.toString().concat(sqlFrom.toString()));
		EntityManager em = JPA.em();
	 
		List<v_front_debt_bids> bidList = new ArrayList<v_front_debt_bids>();
		PageBean<v_front_debt_bids> page = new PageBean<v_front_debt_bids>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		try {
            Query query = em.createNativeQuery(sql.toString().concat(sqlFrom.toString()),v_front_debt_bids.class);
            for(int n = 1; n <= p.size(); n++){
                query.setParameter(n, p.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            bidList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, cntSql.toString(), p);
            page.page = bidList;
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "系统异常，给您带来的不便敬请谅解！";
			error.code = -2;
		}
		return page; 
	}
	
	public static Map<String, Object> queryRiskLimit(ErrorInfo error, double investTotal, Bid bids,User user){
		Map<String, Object> map = new HashMap<>();
		try {
			//查询该用户，此类型级别标的，所有投资的总额 
	    	StringBuffer hql = new StringBuffer(); 
	    	hql.append("select sum(ifnull(invest.invest_amount,0)) as invest_amount,sum(ifnull(bill_invest.receive_corpus,0)) as receive_corpus ");
	    	hql.append("from t_users ");
	    	hql.append("left join t_bid_risk bid_risk on true ");
	    	hql.append("left join (select invest.user_id,bid.bid_risk_id,sum(ifnull(invest.amount,0)) invest_amount ");
	    	hql.append("from t_bids bid ");
	    	hql.append("inner join t_invests invest on bid.id=invest.bid_id ");
	    	hql.append("where bid.status in (1,2,3) and bid.bid_risk_id is not null ");
	    	hql.append("group by invest.user_id,bid.bid_risk_id ");
	    	hql.append(")invest on invest.user_id=t_users.id and invest.bid_risk_id=bid_risk.id ");
	    	hql.append("left join (select bill_invests.user_id,bid.bid_risk_id,sum(ifnull(bill_invests.receive_corpus,0)) receive_corpus ");
	    	hql.append("from t_bids bid ");
	    	hql.append("inner join t_bill_invests bill_invests on bid.id=bill_invests.bid_id ");
	    	hql.append("where bid.status in (4,14) and bid.bid_risk_id is not null and bill_invests.status in (-1,-2) ");
	    	hql.append("group by bill_invests.user_id,bid.bid_risk_id ");
	    	hql.append(")bill_invest on bill_invest.user_id=t_users.id and invest.bid_risk_id=bid_risk.id  ");
	    	hql.append("where 1=1 and t_users.id=? and bid_risk.id=? ");
	     
	    	Map<String, Object> map_ = JPAUtil.getMap(error, hql.toString(), user.id, bids.bidRiskId);
	    	// 查询标的和用户限额
			BigDecimal bidRiskQuato = BidUserRisk.getRiskByBidAndUser(bids.bidRiskId, user.riskType.longValue()).quota;
			//标的风险类型
			String bidTypeName = BidUserRisk.getRiskByBidAndUser(bids.bidRiskId, user.riskType.longValue()).bid_risk_name;
			
			BigDecimal quota = bidRiskQuato.multiply(new BigDecimal(10000));
			Double sumInvestRisk = Double.parseDouble(map_.get("invest_amount")+ "") + Double.parseDouble(map_.get("receive_corpus")+ "");
	    	BigDecimal invesTotalSum = new BigDecimal(sumInvestRisk);
	    	
	    	//返回投资信息的限额
	    	List<invest_quota> listQuota = new ArrayList<>();
	    	List<t_bid_risk> bidRiskList = t_bid_risk.getAllBidRiskList();
	    	
			for (t_bid_risk bidRisk : bidRiskList) {
				invest_quota investQuota = new invest_quota();
				//该用户该级别标的，已投金额
				Map<String, Object> bidInvest = JPAUtil.getMap(error, hql.toString(), user.id, bidRisk.id);
				double bidInvestAmount = Double.parseDouble(bidInvest.get("invest_amount")+ "") + Double.parseDouble(bidInvest.get("receive_corpus")+ "");
				//查看该级别标的的限额
				t_bid_user_risk bidRiskQuato_ = BidUserRisk.getRiskByBidAndUser(bidRisk.id, user.riskType.longValue());
				if(new BigDecimal(bidInvestAmount).compareTo(bidRiskQuato_.quota.multiply(new BigDecimal(10000))) == -1) {
					investQuota.remainInvestAmount = bidRiskQuato_.quota.multiply(new BigDecimal(10000)).subtract(new BigDecimal(bidInvestAmount));
					
				} else {
					investQuota.remainInvestAmount = BigDecimal.ZERO;
				}
				investQuota.name = bidRisk.name;
				investQuota.quota = bidRiskQuato_.quotaStr;
				investQuota.riskType = bidRiskQuato_.risk_type;
				listQuota.add(investQuota);
			}
			Logger.info("标的风险评估josn: " + JSONUtils.toJSONString(listQuota));
			
			//查询用户类型 能投资的标的
			List<String> canInvestBidName = new ArrayList<>(); 
		    List<t_bid_user_risk> bid_user = t_user_risk.getAllUserRiskMap().get(Long.valueOf(user.riskType)).getBidUserRiskList();
		    if(bid_user != null) {
		    	for (t_bid_user_risk bidUser : bid_user) {
					if(bidUser.quota.compareTo(BigDecimal.ZERO) > 0) { //可投资的
						canInvestBidName.add(bidUser.bid_risk_name);
					} 
				}
		    	Logger.info( user.id + " 可投资项目类型 canInvestBidName: "+ canInvestBidName);
		    	Logger.info( "正在投资的标的风险类型: "+ bidTypeName);
		    	
				if(canInvestBidName.contains(bidTypeName)) {
					map.put("is_risk_invest", true); //是否可投资
				} else {
					map.put("is_risk_invest", false);
				}
		    	//canInvestBidName.contains(bidTypeName);
		    }
		    
			map.put("listQuota", listQuota);//风险测评金额
			map.put("canInvestBidName", canInvestBidName);//可投标的级别
			map.put("invesTotalSum", invesTotalSum);//已投资金额
			map.put("quota", quota); //用户类型和标的类型 限额
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}

	/**
	 * 根据 userId，bidId，查询用户在该标的投资顺序
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param bidId
	 * @return
	 * @author: zj
	 */
	public static int getInvestOrder(long userId, long bidId) {
		String sql = "";
		User user = null;
		sql = "select    (select count(1)  from t_invests a  where  a.id<invest.id  and  a.bid_id=invest.bid_id)+1  as  ser     from  t_invests   invest  where    invest.bid_id=?  and  invest.user_id=?   ";
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, bidId);
		query.setParameter(2, userId);
		List order = query.getResultList();
		if (order == null || order.size() <= 0) {
			return 0;
		} else {
			Map<String, Object> objects = (Map<String, Object>) order.get(0);
			return Integer.parseInt(objects.get("ser").toString());
		}
	}
}
