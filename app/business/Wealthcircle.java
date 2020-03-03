package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_invests;
import models.t_statistic_cps;
import models.t_statistic_invitation;
import models.t_statistic_invitation_details;
import models.t_wealthcircle_income;
import models.t_wealthcircle_invite;
import models.v_bill_department_month_maturity;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.RegexUtils;

import com.shove.Convert;

import constants.Constants;


/**
 * 财富圈，邀请码业务
 * 
 * @author hys
 *
 */
public class Wealthcircle implements Serializable{
	
	/**
	 * 检测邀请码是否存在
	 * 
	 * @return
	 */
	public static int isInvitationExist(String invitationCode, ErrorInfo error) {
		error.clear();
		if (StringUtils.isBlank(invitationCode)) {
			error.code = -3;
			error.msg = "邀请码错误!";
			return error.code;
		}

		String sql = "invite_code = ? and status = 1 and is_active = 1";
		t_wealthcircle_invite invite = null;
		try {
			invite = t_wealthcircle_invite.find(sql, invitationCode).first();

		} catch (Exception e) {
			Logger.error("检测邀请码是否存在时出错:" + e.getMessage());
			error.code = -1;
			error.msg = "检测邀请码出错!";
			return error.code;
		}
		
		if (null == invite){
			error.code = -3;
			error.msg = "无效邀请码";

			return error.code;
		}

		error.code = -2;

		return error.code;
	}
	
	/**
	 * 根据用户名查询可用邀请码
	 * @param userId
	 * @return
	 */
	public static long getActiveCodeByUserId(long userId){
		String sql = "user_id = ? and status = 1 and is_active = 1";
		return t_wealthcircle_invite.count(sql, userId);
	}
	
	/**
	 * 查询用户当前获取邀请码用掉的投资金额
	 * @param userId
	 * @return
	 */
	public static double getInvestmentUseTotal(long userId){
		String sql = "user_id = ? order by id desc";
		t_wealthcircle_invite invite = null;
		try {
			invite = t_wealthcircle_invite.find(sql, userId).first();
		} catch (Exception e) {
			Logger.error("查询用户当前获取邀请码用掉的投资金额出错:" + e.getMessage());
		}
		if (null == invite){
			return 0;
		}
		else{
			return invite.current_total_invist_amount;
		}
	}
	
	/**
	 * 根据用户名查询已邀请的注册人数
	 * @param userId
	 * @return
	 */
	public static long getRegistedUserNum(long userId){
		String sql = "user_id = ? and invited_user_id != 0";
		return t_wealthcircle_invite.count(sql, userId);
	}
	
	public static void addInviteCodeToUser(User user, double investmentTotal){
		double investmentUseTotal = Wealthcircle.getInvestmentUseTotal(user.id);//累计获取邀请码理财金额
		
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		
		long amount = set.invite_code_amount;
		
		double mius = investmentTotal - investmentUseTotal;
		if (mius >= amount){
			double count = Math.floor(Arith.divDown(mius, amount, 2));
			int num = 1;
			for (int index = 0; index < count; index++){
				t_wealthcircle_invite invite = new t_wealthcircle_invite();
				invite.user_id = user.id;
				invite.user_name = user.name;
				invite.time = new Date();
				invite.invite_code = createInviteCode();
				invite.status = 1;
				invite.effective_time = set.invite_code_period;
				invite.type = Constants.INVITE_CODE_TYPE_INVEST;
				invite.current_total_invist_amount = amount * num + investmentUseTotal;
				invite.qual_amount = amount;
				invite.invite_income_rate = set.invite_income_rate;
				invite.invited_user_discount = set.invited_user_discount;
				invite.is_active = Constants.INVITE_CODE_ACTIVE;
				try {
					JPAUtil.transactionBegin();
					invite.save();
					num ++;
					JPAUtil.transactionCommit();
				} catch (Exception e) {
					Logger.error("给用户邀请码失败" + e.getMessage());
				}
				
			}
		}
	}
	
	
	public static String createInviteCode(){
		DecimalFormat format = new DecimalFormat("####.0000");
		String code = format.format(Math.random()) + format.format(Math.random());
		
		return "YQ" + StringUtils.replace(code, ".", "-");
	}
	
	/**
	 * 查询用户邀请码
	 * @param userId
	 * @param status
	 * @param type
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_wealthcircle_invite> queryMyInvitation(long userId, int status, int type,
					int currPage, int pageSize){
		PageBean<t_wealthcircle_invite> page = new PageBean<t_wealthcircle_invite>();
		if (currPage == 0) {
			currPage = 1;
		}
		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}

		StringBuffer sql = new StringBuffer("select count(*) from t_wealthcircle_invite where 1 = 1");
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("status", status);
		conditionMap.put("type", type);
		page.conditions = conditionMap;
		if (status != 0) {
			sql.append(" and status = ? ");
			params.add(status);
		}
		if (type != 0) {
			sql.append(" and type = ? ");
			params.add(type);
		}
		sql.append(" and user_id= ? ");
		params.add(userId);

		sql.append(" order by id desc");
		
		
		int count = 0;
		EntityManager em = JPA.em();
        Query query = em.createNativeQuery(sql.toString());

        for(int n = 1; n <= params.size(); n++){
            query.setParameter(n, params.get(n-1));
        }

        List<?> list = null;
		
		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("查询邀请码时：:" + e.getMessage());
			return page;
		}
		
		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		
		if (count < 1){
			return page;
		}

		page.currPage = currPage;
		page.pageSize = pageSize;
		

		List<t_wealthcircle_invite> wealthcircle = new ArrayList<t_wealthcircle_invite>();
		sql = new StringBuffer("select new models.t_wealthcircle_invite (t.id as id , t.invite_code as invite_code, t.status as status, t.time as time, t.type as type, t.invited_register_time as invited_register_time, t.effective_time as effective_time) from t_wealthcircle_invite t where 1=1");
		params = new ArrayList<Object>();
		if (status != 0) {
			sql.append(" and status = ? ");
			params.add(status);
		}
		if (type != 0) {
			sql.append(" and type = ? ");
			params.add(type);
		}
		sql.append(" and user_id= ? ");
		params.add(userId);

		sql.append(" order by id desc");
		try {
			query = em.createQuery(sql.toString(), t_wealthcircle_invite.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			wealthcircle = query.getResultList();

			page.totalCount = count;

		} catch (Exception e) {
			Logger.error("查询邀请码时：" + e.getMessage());
		}
		page.page = wealthcircle;
		return page;
	}
	
	
	/**
	 * 查询成功邀请的理财用户数
	 * 
	 * @param userId  邀请人
	 * @return
	 */
	public static long queryFinanceMember(long userId, ErrorInfo error){
		
		String sql = "SELECT COUNT(1) from t_wealthcircle_invite wi WHERE wi.user_id = ? AND EXISTS (SELECT 1 FROM t_users u WHERE u.id = wi.invited_user_id AND u.master_identity IN (2, 3))";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("查询成功邀请的理财用户数时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询成功邀请的理财用户数异常!";
			
			return error.code;
		}
		
		return count == null ? 0 : ((BigInteger)count).intValue();
	}
	
	/**
	 * 查询用户返佣收益
	 * 
	 * @param userId  邀请人
	 * @return
	 */
	public static double queryAccumulatedEarnings(long userId, ErrorInfo error){
		
		String sql = "SELECT SUM(invite_income) FROM t_wealthcircle_income WHERE user_id = ? AND status = 1";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("查询用户返佣收益时，%s", e.getMessage());
			error.code = -1;
			error.msg = "查询用户返佣收益异常!";
			
			return error.code;
		}
		
		return count == null ? 0 : ((BigDecimal)count).doubleValue();
	}
	
	/**
	 * 我邀请的会员
	 * 
	 * @param userId
	 * @param userName
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<t_wealthcircle_invite> queryMyInviteMembers(long userId, String userName, String currPageStr, String pageSizeStr, ErrorInfo error){
		error.clear();

 		int currPage = 1;
 		int pageSize = 5;  //默认每页5条
 		userName = StringUtils.trim(userName);
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}

 		StringBuffer sql = new StringBuffer("SELECT new t_wealthcircle_invite(invited_user_id, invited_user_name, invite_code, invited_register_time) FROM t_wealthcircle_invite WHERE user_id = ? AND invited_user_id <> 0 ");
		StringBuffer sqlCount = new StringBuffer("SELECT COUNT(1) FROM t_wealthcircle_invite WHERE user_id = ? AND invited_user_id <> 0 ");

		List<Object> params = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		conditionMap.put("userName", userName);
		params.add(userId);

		if(StringUtils.isNotBlank(userName)){
			sql.append(" AND invited_user_name like ?");
			sqlCount.append(" AND invited_user_name like ?");
			params.add("%" + userName + "%");
		}

		List<t_wealthcircle_invite> invitation = new ArrayList<t_wealthcircle_invite>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createQuery(sql.toString(),t_wealthcircle_invite.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            invitation = query.getResultList();
            
            Query queryCount = em.createNativeQuery(sqlCount.toString());
    		for(int n = 1; n <= params.size(); n++){
    			queryCount.setParameter(n, params.get(n-1));
            }
    		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
    		
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询我邀请的会员时："+e.getMessage());
			error.code = -1;
			error.msg = "查询我邀请的会员时出现异常！";
			
			return null;
		}
		
		PageBean<t_wealthcircle_invite> page = new PageBean<t_wealthcircle_invite>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = invitation;
		
		error.code = 0;
		
		return page;
	}

	/**
	 * 保存返佣记录，为发放佣金做准备
	 * 
	 * @param invests
	 */
	public static void addInviteIncome(long bidId) {
		
		List<t_invests> invests = null;

		try {
			invests = t_invests.find("bid_id = ? ", bidId).fetch();
		} catch (Exception e) {
			Logger.error("查询对应标的的所有投资者" + e.getMessage());
			
			return ;
		}
		
		if(null == invests || invests.size() == 0){
			
			return ;
		}

		
		for(t_invests invest : invests){
			
			//查找邀请码信息
			t_wealthcircle_invite weaInvite = Wealthcircle.queryInviter(invest.user_id);
			
			if(weaInvite == null){
				continue;
			}
			
			Bid bid = Bid.queryPeriodByBidId(bidId);
			
			if(bid == null){
				continue;
			}
			
			t_wealthcircle_income weaIncome = new t_wealthcircle_income();
			weaIncome.user_id = weaInvite.user_id;
			weaIncome.invite_code = weaInvite.invite_code;
			weaIncome.time = new Date();
			weaIncome.invited_user_id = invest.user_id;
			weaIncome.invest_time = invest.time;
			weaIncome.invest_amount = invest.amount;
			weaIncome.invite_income = Wealthcircle.getInviteIncome(invest.amount, weaInvite.invite_income_rate, bid.period, bid.periodUnit);
			weaIncome.status = 0;  //未发放
			
			try {
				weaIncome.save();
			} catch (Exception e) {
				Logger.error("保存返佣记录[%s-%s]时，%s", invest.user_id, weaInvite.invite_code, e.getMessage());
				
				continue;
			}
		}
	}

	/**
	 * 计算返佣金额
	 * 
	 * @param amount
	 * @param invite_income_rate
	 * @param period
	 * @param periodUnit
	 * @return
	 */
	private static double getInviteIncome(double amount, double invite_income_rate, int period, int periodUnit) {
		
		double monthRate = Double.valueOf(invite_income_rate * 0.01)/12.0;//通过年利率得到月利率
		
		if(periodUnit == -1 ){  //年标
			return Arith.round(Arith.mul(Arith.mul(Arith.mul(amount, monthRate), period), 12), 2);
		}
		
		if(periodUnit == 0 ){  //月标
			return Arith.round(Arith.mul(Arith.mul(amount, monthRate), period), 2);
		}
		
		if(periodUnit == 1 ){  //天标
			return Arith.div(Arith.mul(Arith.mul(amount, monthRate), period), 30, 2);
		}
		
		return 0;
	}

	/**
	 * 查询有效邀请码信息
	 * 
	 * @param userId
	 * @return
	 */
	private static t_wealthcircle_invite queryInviter(long userId) {
		t_wealthcircle_invite wi = null;
		
		try {
			wi = t_wealthcircle_invite.find("is_active = 1 and status <> 3 and invited_user_id = ?", userId).first();
		} catch (Exception e) {
			Logger.error("查询有效邀请码信息时，%s", e.getMessage());
		}
		
		return wi;
	}
	
	/**
	 * 返佣明细
	 * 
	 * @param id
	 * @param beginTime
	 * @param endTime
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<t_wealthcircle_income> queryMyInviteMemberDetails(long userId, long invitedUserId, 
			String beginTime, String endTime, String currPageStr, String pageSizeStr, int isExport, ErrorInfo error) {
		error.clear();

 		int currPage = 1;
 		int pageSize = 5;  //默认每页5条
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}

 		StringBuffer sql = new StringBuffer("SELECT new t_wealthcircle_income(invest_time, invest_amount, invite_income) FROM t_wealthcircle_income WHERE user_id = ? AND invited_user_id = ?");
		StringBuffer sqlCount = new StringBuffer("SELECT COUNT(1) FROM t_wealthcircle_income WHERE user_id = ? AND invited_user_id = ?");

		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		params.add(invitedUserId);
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("beginTime", beginTime);
		conditionMap.put("endTime", endTime);

		if(StringUtils.isNotBlank(beginTime)){
			sql.append(" AND invest_time >= ?");
			sqlCount.append(" AND invest_time >= ?");
			params.add(DateUtil.strDateToStartDate(beginTime));
		}
		
		if(StringUtils.isNotBlank(endTime)){
			sql.append(" AND invest_time <= ?");
			sqlCount.append(" AND invest_time <= ?");
			params.add(DateUtil.strDateToEndDate(endTime));
		}

		List<t_wealthcircle_income> iviteIncome = new ArrayList<t_wealthcircle_income>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createQuery(sql.toString(),t_wealthcircle_income.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            if (isExport == 1){
            	iviteIncome = query.getResultList();
            }
            else{
            	query.setFirstResult((currPage - 1) * pageSize);
                query.setMaxResults(pageSize);
                iviteIncome = query.getResultList();
            }
           
            Query queryCount = em.createNativeQuery(sqlCount.toString());
    		for(int n = 1; n <= params.size(); n++){
    			queryCount.setParameter(n, params.get(n-1));
            }
    		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
    		
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询返佣详情时："+e.getMessage());
			error.code = -1;
			error.msg = "查询返佣详情出现异常！";
			
			return null;
		}
		
		PageBean<t_wealthcircle_income> page = new PageBean<t_wealthcircle_income>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = iviteIncome;
		
		error.code = 0;
		
		return page;
	}

	/**
	 * 查询邀请码列表
	 * @param userName
	 * @param status
	 * @param type
	 * @param beginTimeStr
	 * @param endTimeStr
	 * @param currPage
	 * @param pageSize
	 * @param error
	 * @return
	 */
	public static PageBean<t_wealthcircle_invite> queryInviteCodeList(String userName, int status, int type, 
			String beginTimeStr, String endTimeStr, int currPage, int pageSize, int isExport, ErrorInfo error){
 		Date beginTime = null;
 		Date endTime = null;
 		
 		if(currPage == 0 ) {
 			currPage = 1;
 		}
 		
 		if(pageSize == 0) {
 			pageSize = Constants.PAGE_SIZE;
 		}
 		
 		if(RegexUtils.isDate(beginTimeStr)) {
 			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
 		}
 		
 		if(RegexUtils.isDate(endTimeStr)) {
 			endTime = DateUtil.strDateToEndDate(endTimeStr);
 		}

 		StringBuffer sql = new StringBuffer("SELECT new t_wealthcircle_invite(id, invite_code, status, type, user_name, user_id, invited_user_id, invited_user_name,is_active) FROM t_wealthcircle_invite WHERE 1=1");
		StringBuffer sqlCount = new StringBuffer("SELECT COUNT(*) FROM t_wealthcircle_invite WHERE 1 = 1");

		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		conditionMap.put("userName", userName);
		conditionMap.put("status", status);
		conditionMap.put("type", type);
		conditionMap.put("userName", userName);
		
		List<Object> params = new ArrayList<Object>();

		if(StringUtils.isNotBlank(userName)){
			sql.append(" AND user_name like ?");
			sqlCount.append(" AND user_name like ?");
			params.add("%" + userName + "%");
			
		}
		
		if (status != 0){
			sql.append(" and status = ?");
			sqlCount.append(" and status = ?" );
			params.add(status);
		}
		
		if (type != 0){
			sql.append(" and type = ?");
			sqlCount.append(" and type = ?");
			params.add(type);
		}
		if (null != beginTime){
			sql.append(" and time > ?");
			sqlCount.append(" and time >= ?");
			params.add(beginTime);
		}
		
		if (null != endTime){
			sql.append(" and time < ?");
			sqlCount.append(" and time <= ?");
			params.add(endTime);
		}
		
		sql.append("order by id desc");
		List<t_wealthcircle_invite> invite = new ArrayList<t_wealthcircle_invite>();
		int count = 0;
		PageBean<t_wealthcircle_invite> page = new PageBean<t_wealthcircle_invite>();
		try {
			EntityManager em = JPA.em();
            Query query = em.createQuery(sql.toString(),t_wealthcircle_invite.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            if (isExport == 1){
            	
            	invite = query.getResultList();
            }else{
            	query.setFirstResult((currPage - 1) * pageSize);
                query.setMaxResults(pageSize);
                invite = query.getResultList();
            }
            
            
            Query queryCount = em.createQuery(sqlCount.toString());
    		for(int n = 1; n <= params.size(); n++){
    			queryCount.setParameter(n, params.get(n-1));
            }
    		count = Convert.strToInt(queryCount.getResultList().get(0) + "",0);
    		
		} catch (Exception e) {
			Logger.error("查询财富圈邀请码："+e.getMessage());
			error.code = -1;
			error.msg = "查询财富圈邀请码出现异常！";
			return page;
		}
		
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = invite;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 单个邀请码详情
	 * @param id
	 * @return
	 */
	public static t_wealthcircle_invite queryInviteCodeInfo(long id){
		return t_wealthcircle_invite.findById(id);
	}
	
	/**
	 * 佣金发放统计表
	 * @param year
	 * @param month
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_statistic_invitation> queryInvitationStatistic(int noPage, int year, int month, int currPage) {
		
		StringBuffer conditions = new StringBuffer("1=1 ");
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		conditionMap.put("year", year);
		conditionMap.put("month", month);
		
		PageBean<t_statistic_invitation> page = new PageBean<t_statistic_invitation>();
		page.currPage = Constants.ONE;
		page.pageSize = Constants.TEN;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(year != 0){
			conditions.append("and year = ? ");
			values.add(year);
		}
		
        if(month != 0){
        	conditions.append("and month = ? ");
			values.add(month);
		}
		
		List<t_statistic_invitation> offerInfo = new ArrayList<t_statistic_invitation>();
		
		try {
			page.totalCount =  (int) t_statistic_invitation.count(conditions.toString(), values.toArray());
			
			if(noPage != Constants.NO_PAGE){
				offerInfo = t_statistic_invitation.find(conditions.toString(), values.toArray()).fetch(page.currPage, page.pageSize);
			}else{
				offerInfo = t_statistic_invitation.find(conditions.toString(), values.toArray()).fetch();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询佣金发放统计时："+e.getMessage());

			return null;
		}
		
		page.page = offerInfo;
		page.conditions = conditionMap;
		
		return page;
	}
	
	/**
	 * 佣金发放统计表
	 * @param year
	 * @param month
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_statistic_invitation_details> queryInvitationStatisticDetails(int noPage, int year, int month, String userName, int currPage) {
		
		StringBuffer conditions = new StringBuffer("1=1 ");
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		conditionMap.put("year", year);
		conditionMap.put("month", month);
		conditionMap.put("userName", userName);
		
		PageBean<t_statistic_invitation_details> page = new PageBean<t_statistic_invitation_details>();
		page.currPage = Constants.ONE;
		page.pageSize = Constants.FIVE;
		
		if(currPage != 0){
			page.currPage = currPage;
		}
		
		if(year != 0){
			conditions.append("and year = ? ");
			values.add(year);
		}
		
		if(month != 0){
			conditions.append("and month = ? ");
			values.add(month);
		}
		
		if(StringUtils.isNotBlank(userName)){
			conditions.append("and user_name = ? ");
			values.add(userName);
		}
		
		List<t_statistic_invitation_details> offerInfo = new ArrayList<t_statistic_invitation_details>();
		
		try {
			page.totalCount =  (int) t_statistic_invitation_details.count(conditions.toString(), values.toArray());
			
			if(noPage != Constants.NO_PAGE){
				offerInfo = t_statistic_invitation_details.find(conditions.toString(), values.toArray()).fetch(page.currPage, page.pageSize);
			}else{
				offerInfo = t_statistic_invitation_details.find(conditions.toString(), values.toArray()).fetch();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询佣金发放明细表时："+e.getMessage());
			
			return null;
		}
		
		page.page = offerInfo;
		page.conditions = conditionMap;
		
		return page;
	}
	
	/**
	 * 更新邀请码是否启用
	 * @param id
	 * @param status
	 * @return
	 */
	public static int updateCodeActive(long id, int isActive){
		t_wealthcircle_invite invite = t_wealthcircle_invite.findById(id);
		if (invite == null ){
			return 0;
		}
		try {
			invite.is_active = isActive;
			invite.save();
		} catch (Exception e) {
			return 0;
		}
		return 1;
	}
	
	/**
	 * 赠送邀请码
	 * @param userId
	 * @return
	 */
	public static void giveInviteCodeToUser(User user, ErrorInfo error){
		error.clear();
		
		t_wealthcircle_invite invite = new t_wealthcircle_invite();
		Supervisor supervisor = Supervisor.currSupervisor();
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		
		invite.user_id = user.id;
		invite.user_name = user.name;
		invite.time = new Date();
		invite.status = 1;
		invite.effective_time = set.invite_code_period;
		invite.type = 2;
		invite.current_total_invist_amount = getInvestmentUseTotal(user.id);
		invite.qual_amount = set.invite_code_amount;
		invite.invite_income_rate = set.invite_income_rate;
		invite.invited_user_discount = set.invited_user_discount;
		invite.distribution_id = supervisor.id;
		invite.is_active = 1;
		invite.invite_code = createInviteCode();
			
		try {
			invite.save();
		} catch (Exception e) {
			Logger.error("赠送邀请码失败："+e.getMessage());
			error.code = -1;
			error.msg = "赠送邀请码时数据异常";
			
			return ;
		}
		
	}

}
