package business;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_debt_invest;
import models.t_invests;
import models.t_mall_goods;
import models.t_mall_scroe_record;
import models.t_red_packages_type;
import models.t_users;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import constants.Constants;
import constants.MallConstants;

/**
 * 积分商城 积分记录业务逻辑
 * 
 * @author yuy
 * @time 2015-10-13 17:17
 *
 */
public class MallScroeRecord {

	/**
	 * 查询积分记录列表
	 * 
	 * @param name
	 * @param status
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_mall_scroe_record> queryScroeRecordByPage(long user_id, String user_name, int type, String orderTypeStr,
			String orderStatus, int currPage, int pageSize, int isExport, ErrorInfo error) {
		error.clear();

		int orderType = 0;

		PageBean<t_mall_scroe_record> page = new PageBean<t_mall_scroe_record>();
		if (currPage == 0) {
			currPage = 1;
		}
		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (orderType < 0 || orderType > 10) {
			orderType = 0;
		}

		StringBuffer sql_count = new StringBuffer("select count(*) from t_mall_scroe_record where 1 = 1");
		StringBuffer sql_list = new StringBuffer("select new t_mall_scroe_record(id, user_id, user_name, time, type, relation_id, scroe, status, "
				+ "quantity, description, remark) from t_mall_scroe_record t where 1=1");
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("user_name", user_name);
		conditionMap.put("type", type);
		conditionMap.put("orderType", orderType);
		page.conditions = conditionMap;
		page.currPage = currPage;
		page.pageSize = pageSize;
		if (user_id != 0) {
			sql_count.append(" and user_id = ? ");
			sql_list.append(" and user_id = ? ");
			params.add(user_id);
		}
		if (!StringUtils.isBlank(user_name)) {
			sql_count.append(" and user_name like ? ");
			sql_list.append(" and user_name like ? ");
			params.add("%" + user_name + "%");
		}
		if (type != 0) {
			sql_count.append(" and type = ? ");
			sql_list.append(" and type = ? ");
			params.add(type);
		}

		sql_list.append(MallConstants.MALL_RECORD_ORDER[orderType]);
		/* 升降序 */
		if (StringUtils.isNotBlank(orderStatus) && orderType > 0) {
			if (Integer.parseInt(orderStatus) == 1)
				sql_list.append(" ASC");
			else
				sql_list.append(" DESC");
			conditionMap.put("orderStatus", orderStatus);
		}

		int count = 0;
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql_count.toString());

		for (int n = 1; n <= params.size(); n++) {
			query.setParameter(n, params.get(n - 1));
		}

		List<?> list = null;
		try {
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录数时：:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return page;
		}

		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		if (count < 1) {
			return page;
		}

		List<t_mall_scroe_record> mallScroeRecordList = new ArrayList<t_mall_scroe_record>();
		try {
			query = em.createQuery(sql_list.toString(), t_mall_scroe_record.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			if (isExport != Constants.IS_EXPORT) {
				query.setFirstResult((currPage - 1) * pageSize);
				query.setMaxResults(pageSize);
			}
			mallScroeRecordList = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录列表时：" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}
		page.totalCount = count;
		page.page = mallScroeRecordList;
		return page;
	}

	/***
	 * app查询积分记录列表
	 * 
	 * @param user_id
	 * @param currPage
	 * @param pageSize
	 * @param type 0:积分收入记录 1：积分消耗记录
	 * @param error
	 * @return
	 */
	public static PageBean<t_mall_scroe_record> queryScroeRecordByApp(long user_id, int currPage, int pageSize, ErrorInfo error) {
		error.clear();


		PageBean<t_mall_scroe_record> page = new PageBean<t_mall_scroe_record>();
		if (currPage == 0) {
			currPage = 1;
		}
		if (pageSize == 0) {
			pageSize = Constants.PAGE_SIZE;
		}


		StringBuffer sql_count = new StringBuffer("select count(*) from t_mall_scroe_record where 1 = 1");
		StringBuffer sql_list = new StringBuffer("select new t_mall_scroe_record(id, user_id, user_name, time, type, relation_id, scroe, status, "
				+ "quantity, description, remark) from t_mall_scroe_record t where 1=1");
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		page.conditions = conditionMap;
		page.currPage = currPage;
		page.pageSize = pageSize;
		if (user_id != 0) {
			sql_count.append(" and user_id = ? ");
			sql_list.append(" and user_id = ? ");
			params.add(user_id);
		}

		sql_count.append(" and  type not in (?,?) ");
		sql_list.append(" and  type not in (?,?) ");
		params.add(MallConstants.EXCHANGE);
		params.add(MallConstants.EXCHANGE_RED);

		int count = 0;
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql_count.toString());

		for (int n = 1; n <= params.size(); n++) {
			query.setParameter(n, params.get(n - 1));
		}

		List<?> list = null;
		try {
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录数时：:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return page;
		}

		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		if (count < 1) {
			return page;
		}

		List<t_mall_scroe_record> mallScroeRecordList = new ArrayList<t_mall_scroe_record>();
		try {
			query = em.createQuery(sql_list.toString(), t_mall_scroe_record.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			mallScroeRecordList = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录列表时：" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}
		page.totalCount = count;
		page.page = mallScroeRecordList;
		return page;
	}

	/**
	 * 兑换：保存消费积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static void saveScroeExchangeRecord(int exchangeNum, User user, t_mall_goods goods, String postDetail, ErrorInfo error) {
		error.clear();
		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.EXCHANGE;
		record.relation_id = goods.id;
		record.scroe = -goods.exchange_scroe * exchangeNum;
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = Double.parseDouble(String.valueOf(exchangeNum));
		record.description = MallConstants.STR_EXCHANGE + goods.name;
		record.remark = postDetail;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 积分兑换红包记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static void saveScroeExchangeRedRecord(int exchangeNum, User user, t_red_packages_type red, String postDetail, ErrorInfo error) {
		error.clear();
		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.EXCHANGE_RED;
		record.relation_id = red.id;
		record.scroe = (int) (-red.obtain_money * exchangeNum);
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = Double.parseDouble(String.valueOf(exchangeNum));
		record.description = MallConstants.STR_EXCHANGE_RED + "-" + red.typeName;
		record.remark = postDetail;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 注册：保存赠送积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static void saveScroeRegistereRecord(User user, int scroe, ErrorInfo error) {
		error.clear();
		if (user == null || scroe == 0)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.REGISTER;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_SUCCESS;
		record.description = MallConstants.STR_REGISTER;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 每日签到：保存签到积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static void saveScroeSignRecord(User user, int[] scroe, ErrorInfo error) {
		error.clear();
		if (user == null || user.id <= 0)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.SIGN;
		record.scroe = scroe[0];
		record.levelDay = scroe[1];
		record.status = MallConstants.STATUS_SUCCESS;
		record.description = MallConstants.STR_SIGN;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 投资：保存赠送积分记录(初始化)
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param invest
	 * @param productName
	 * @param error
	 */
	public static void saveScroeInvestRecord(t_users user, t_invests invest, int scroe, String productName, ErrorInfo error) {
		error.clear();
		if (user == null || invest == null || productName == null)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.INVEST;
		record.relation_id = invest.id;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_FAIL;
		record.quantity = invest.amount;
		record.description = MallConstants.STR_INVEST + productName;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 放款回调：批量修改赠送积分记录状态为'成功'
	 * 
	 * @param invests
	 * @param error
	 */
	public static void updateScroeInvestRecordStatusByBatch(List<t_invests> invests, ErrorInfo error) {
		if (invests == null || invests.size() == 0)
			return;

		for (t_invests invest : invests) {
			t_mall_scroe_record record = queryRecordDetailByInvestId(invest.id);
			updateScroeInvestRecordStatus(record, MallConstants.STATUS_SUCCESS);
		}
	}

	/**
	 * 放款回调：修改赠送积分记录状态为'成功'
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static void updateScroeInvestRecordStatus(t_mall_scroe_record record, int status) {
		if (record == null)
			return;

		try {
			record.status = status;
			record.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("修改赠送积分记录状态为'成功'时：" + e.getMessage());
		}
	}

	/**
	 * 根据id查询积分记录信息
	 * 
	 * @param id
	 * @return
	 */
	public static t_mall_scroe_record queryRecordDetailById(long id) {
		t_mall_scroe_record record = null;
		try {
			record = t_mall_scroe_record.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录信息时：" + e.getMessage());
		}
		return record;
	}

	/**
	 * 根据投资id查询积分记录信息
	 * 
	 * @param id
	 * @return
	 */
	public static t_mall_scroe_record queryRecordDetailByInvestId(long invest_id) {
		t_mall_scroe_record record = null;
		try {
			record = t_mall_scroe_record.find(" relation_id = ? and type = ?", invest_id, MallConstants.INVEST).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询积分记录信息时：" + e.getMessage());
		}
		return record;
	}

	/**
	 * 保存积分记录
	 * 
	 * @param goods
	 * @return
	 */
	public static int insertScroeRecordDetail(t_mall_scroe_record record) {
		if (record == null)
			return MallConstants.COM_ERROR_CODE;
		try {
			record.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("保存积分记录时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 查询用户可用积分
	 * 
	 * @param userId
	 *            用户
	 * @return
	 */
	public static int queryScoreRecordByUser(long userId, ErrorInfo error) {
		String sql = "SELECT SUM(scroe) FROM t_mall_scroe_record WHERE user_id = ? and status = ?";
		Object sum = null;
		try {
			sum = JPA.em().createNativeQuery(sql).setParameter(1, userId).setParameter(2, MallConstants.STATUS_SUCCESS).getSingleResult();
		} catch (Exception e) {
			Logger.error("查询用户积分时，%s", e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}

		return sum == null ? 0 : ((BigDecimal) sum).intValue();
	}

	/**
	 * 根据日期查询积分记录条数
	 * 
	 * @param userId
	 *            用户
	 * @return
	 */
	public static int queryScoreRecordByDate(long userId, String dateStr, int type, ErrorInfo error) {
		String sql = "SELECT count(*) FROM t_mall_scroe_record t Where t.time like ? and t.type = ? and t.user_id = ?";
		Object count = null;
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, dateStr + "%").setParameter(2, type).setParameter(3, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("根据日期查询积分记录条数时，%s", e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}

		return count == null ? 0 : ((BigInteger) count).intValue();
	}

	/**
	 * 根据日期查询积分记录条数
	 * 
	 * @param userId
	 *            用户
	 * @return
	 */
	public static int queryScoreSignCount(long userId, int type, ErrorInfo error) {
		String sql = "SELECT count(*) FROM t_mall_scroe_record t Where t.type = ? and t.user_id = ?";
		Object count = null;
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, type).setParameter(2, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("根据日期查询积分记录条数时，%s", e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}

		return count == null ? 0 : ((BigInteger) count).intValue();
	}

	/**
	 * 当前兑换动态
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> currentScroeNews() {
		ErrorInfo error = new ErrorInfo();

		Object scroeNewsObj = Cache.get("scroeNews");
		if (scroeNewsObj == null) {
			List<Map<String, Object>> newsList = MallGoods.queryHasExchangedGoodsNow(Constants.PAGE_SIZE, error);
			Cache.set("scroeNews", newsList);
			return newsList;
		}

		return (List<Map<String, Object>>) scroeNewsObj;
	}

	/**
	 * 当前积分
	 * 
	 * @return
	 */
	public static int currentMyScroe(long user_id) {
		ErrorInfo error = new ErrorInfo();

		int scroe = MallScroeRecord.queryScoreRecordByUser(user_id, error);
		return ((Integer) scroe).intValue();
	}

	public static int updateAddess(String rid, String address3) {
		ErrorInfo error = new ErrorInfo();
		String sql = " update t_mall_scroe_record set remark=? where id=? ";
		try {
			int row = JPA.em().createQuery(sql).setParameter(1, address3).setParameter(2, Long.parseLong(rid)).executeUpdate();
			return row;
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			return error.code;
		}

	}

	/***
	 * 用户积分收入记录
	 * 
	 * @param user_id
	 * @return
	 */
	public static List<t_mall_scroe_record> queryMallScroeRecord(long user_id) {
		List<t_mall_scroe_record> list = null;
		list = t_mall_scroe_record.find(" type not in (?,?) and user_id = ? ", MallConstants.EXCHANGE, MallConstants.EXCHANGE_RED, user_id).fetch();

		return list;
	}

	/***
	 * 查询下次签到获取积分数
	 * 
	 * @param total
	 * @return
	 */
	public static int[] queryScroeRecord(long user_id, ErrorInfo error) {
		// 查询用户总签到次数
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();

		// int total = queryScoreSignCount(user_id, MallConstants.SIGN, error);

		int init_sign_scroe = backstageSet.init_sign_scroe;
		int mul_sign_scroe = backstageSet.mul_sign_scroe;
		
		int[] res = new int[2];
		int score = backstageSet.init_sign_scroe;
		int levelDay = Constants.ONE;
		res[0] = score;
		res[1] = levelDay;

		t_mall_scroe_record t_mall_scroe_record = queryScoreSignNear(user_id, MallConstants.SIGN, error);

		if (error.code < 0) {
			return res;
		}

		// 如果t_mall_scroe_record为null,则说明用户没有签到
		if (t_mall_scroe_record == null) {
			return res;
		}

		if (t_mall_scroe_record != null && t_mall_scroe_record.id > 0) {
			// 当前年月日
			String simpleDate = utils.DateUtil.dateToString1(new Date());

			Date startDate = utils.DateUtil.strDateToStartDate(simpleDate);
			Date endDate = utils.DateUtil.strDateToEndDate(simpleDate);

			// 获取签到积分的时间加一天的值
			Date addDay = utils.DateUtil.dateAddDay(t_mall_scroe_record.time, 1);

			// addDay如果不在startDate~endDate区间内，则签到积分从头开始
			if (startDate.getTime() > addDay.getTime() ) {
				
				return res;
			} else if(endDate.getTime() < addDay.getTime()){
				res[0] = t_mall_scroe_record.scroe;
				res[1] = t_mall_scroe_record.levelDay;
				return res;
			}else {
				int yesScore = t_mall_scroe_record.scroe;
				// 积分倍增为0(不能作为被除数)
				if (mul_sign_scroe == 0) {
					return res;
				}else {
					if(t_mall_scroe_record.levelDay == Constants.FIVE)
					{
						return res;
					}
					// 依据计算公式获得的值和昨天签到积分是否相等
					int yscore = init_sign_scroe + (t_mall_scroe_record.levelDay - 1) * mul_sign_scroe;
					if (yesScore != yscore) {
						//签到积分规则已改变;
						score = init_sign_scroe + t_mall_scroe_record.levelDay * mul_sign_scroe;
						res[0] = score;
						res[1] = t_mall_scroe_record.levelDay + 1;
						return res;
					} else {
						int mul = (yesScore - init_sign_scroe) / (mul_sign_scroe);
						// 连续5天循环结束
						if ((mul + 1) >= Constants.FIVE) {
							return res;
						}
						score = init_sign_scroe + (mul + 1) * (mul_sign_scroe);
						res[0] = score;
						res[1] = (mul + 1) + 1;
						return res;
					}
				}
			}
		}
		return res;
	}


	/**
	 * 根据最近签到获取积分记录条数
	 * 
	 * @param userId
	 *            用户
	 * @return
	 */
	public static t_mall_scroe_record queryScoreSignNear(long userId, int type, ErrorInfo error) {
		String sql = "select * from t_mall_scroe_record where type = ? and user_id = ? order by time desc";
		List<t_mall_scroe_record> mallScroeRecordList = new ArrayList<t_mall_scroe_record>();
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), t_mall_scroe_record.class);
			query.setParameter(1, type).setParameter(2, userId).getResultList();
			query.setMaxResults(1);
			mallScroeRecordList = query.getResultList();
		} catch (Exception e) {
			Logger.error("根据日期查询积分记录条数时，%s", e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}

		if (mallScroeRecordList != null && mallScroeRecordList.size() > 0) {
			return mallScroeRecordList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 借款：借款积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param invest
	 * @param productName
	 * @param error
	 */
	public static void saveBidScore(User user, Bid bid, int scroe, String productName, ErrorInfo error) {
		error.clear();
		if (user == null || bid == null || productName == null)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.BID_SCORE;
		record.relation_id = bid.id;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = bid.amount;
		record.description = MallConstants.STR_BID_SCORE + productName;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 投资：投标积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param invest
	 * @param productName
	 * @param error
	 */
	public static void saveScroeInvest(User user, Invest invest, int scroe, String productName, ErrorInfo error) {
		error.clear();
		if (user == null || invest == null || productName == null)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.INVEST;
		record.relation_id = invest.id;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = invest.amount;
		record.description = MallConstants.STR_INVEST + productName;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}

	/**
	 * 债权投资：投标积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param invest
	 * @param productName
	 * @param error
	 */
	public static void saveScroeInvest(User user, t_debt_invest invest, int scroe, String productName, ErrorInfo error) {
		error.clear();
		if (user == null || invest == null || productName == null)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.INVEST;
		record.relation_id = invest.id;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = invest.amount;
		record.description = MallConstants.STR_INVEST + productName;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}
	
	/**
	 * 还款：添加正常还款积分记录
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param invest
	 * @param productName
	 * @param payment
	 * @param error
	 */
	public static void saveRepayScore(User user, Bill bill, int scroe, String productName, double payment, ErrorInfo error) {
		error.clear();
		if (user == null || bill == null || productName == null)
			return;

		t_mall_scroe_record record = new t_mall_scroe_record();
		record.user_id = user.id;
		record.user_name = user.name;
		record.time = new Date();
		record.type = MallConstants.REPAY_SCORE;
		record.relation_id = bill.id;
		record.scroe = scroe;
		record.status = MallConstants.STATUS_SUCCESS;
		record.quantity = payment;
		record.description = MallConstants.STR_REPAY_SCORE + productName;
		error.code = MallScroeRecord.insertScroeRecordDetail(record);
	}
}
