package business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_content_news;
import models.t_mall_goods;
import models.t_mall_scroe_record;
import models.v_mall_goods_views;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;

import com.shove.Convert;

import constants.Constants;
import constants.MallConstants;

/**
 * 积分商城 商品业务逻辑
 * 
 * @author yuy
 * @time 2015-10-13 17:17
 * 
 */
public class MallGoods {

	/**
	 * 查询商品列表
	 * 
	 * @param name
	 * @param status
	 * @param currPage
	 * @param pageSize
	 * @return
	 */
	public static PageBean<t_mall_goods> queryMallGoodsByPage(String name, int status, String orderTypeStr, String orderStatus, int currPage,
			int pageSize, ErrorInfo error) {
		error.clear();

		int orderType = 0;

		PageBean<t_mall_goods> page = new PageBean<t_mall_goods>();
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

		StringBuffer sql_count = new StringBuffer("select count(*) from t_mall_goods where 1 = 1 and visible = 1 ");
		StringBuffer sql_list = new StringBuffer("select new t_mall_goods(id,name,time,pic_path,introduction,total,"
				+ "max_exchange_count,surplus,exchange_scroe,status) from t_mall_goods where 1 = 1 and visible = 1 ");
		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("name", name);
		conditionMap.put("status", status);
		conditionMap.put("orderType", orderType);
		page.conditions = conditionMap;
		page.currPage = currPage;
		page.pageSize = pageSize;
		if (!StringUtils.isBlank(name)) {
			sql_count.append(" and name like ? ");
			sql_list.append(" and name like ? ");
			params.add("%" + name.trim() + "%");
		}
		if (status != 0) {
			sql_count.append(" and status = ? ");
			sql_list.append(" and status = ? ");
			params.add(status);
		}

		sql_list.append(MallConstants.MALL_GOODS_ORDER[orderType]);
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
			Logger.error("查询商品记录数时：:" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
			return page;
		}

		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());
		if (count < 1) {
			return page;
		}

		List<t_mall_goods> mallGoods = new ArrayList<t_mall_goods>();
		try {
			query = em.createQuery(sql_list.toString(), t_mall_goods.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			mallGoods = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询商品列表时：" + e.getMessage());
			error.code = MallConstants.DML_ERROR_CODE;
		}
		page.totalCount = count;
		page.page = mallGoods;
		return page;
	}

	/**
	 * 查询商品信息
	 * 
	 * @param id
	 * @return
	 */
	public static t_mall_goods queryGoodsDetailById(long id) {
		t_mall_goods goods = null;
		try {
			goods = t_mall_goods.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询商品信息时：" + e.getMessage());
		}
		return goods;
	}

	/**
	 * 保存商品信息
	 * 
	 * @param goods
	 * @return
	 */
	public static int saveGoodsDetail(t_mall_goods goods) {
		if (goods == null)
			return MallConstants.COM_ERROR_CODE;
		// update
		if (goods.id != null) {
			goods = clone(goods);
		}
		try {
			goods.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("保存商品信息时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 克隆
	 * 
	 * @param goods
	 * @return
	 */
	private static t_mall_goods clone(t_mall_goods goods) {
		if (goods == null)
			return null;
		if (goods.id == null)
			return goods;

		t_mall_goods goods_ = queryGoodsDetailById(goods.id);
		goods_.name = goods.name;
		goods_.introduction = goods.introduction;
		goods_.pic_path = goods.pic_path;
		goods_.total = goods.total;
		goods_.max_exchange_count = goods.max_exchange_count;
		goods_.surplus = goods.surplus;
		goods_.exchange_scroe = goods.exchange_scroe;
		return goods_;
	}

	/**
	 * 删除商品信息
	 * 
	 * @param id
	 * @return
	 */
	public static int deleteGoodsDetail(long id) {
		t_mall_goods goods = queryGoodsDetailById(id);
		if (goods == null)
			return MallConstants.COM_ERROR_CODE;
		try {
			goods.visible = MallConstants.INVISIBLE;// 隐藏
			goods.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("删除商品信息时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 暂停商品兑换
	 * 
	 * @param id
	 * @return
	 */
	public static int stopGoodsExchange(long id, int status) {
		t_mall_goods goods = queryGoodsDetailById(id);
		if (goods == null)
			return MallConstants.COM_ERROR_CODE;
		try {
			goods.status = status;// 暂停/开启
			goods.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("暂停/开启商品兑换时：" + e.getMessage());
			return MallConstants.DML_ERROR_CODE;
		}
		return MallConstants.SUCCESS_CODE;
	}

	/**
	 * 用户已兑换商品信息
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> queryHasExchangedGoodsByList(long user_id, ErrorInfo error) {
		String sql = "select r.id rId,g.id,g.name,g.pic_path,r.scroe,r.quantity,r.time,r.remark from t_mall_scroe_record r,t_mall_goods g "
				+ "where r.relation_id = g.id and r.type in (?,?) and r.user_id = ? order by r.time desc";

		return JPAUtil.getList(error, sql, MallConstants.EXCHANGE, MallConstants.EXCHANGE_RED, user_id);
	}

	/**
	 * 用户已兑换商品信息
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> queryHasExchangedRecordByList(long user_id, ErrorInfo error) {

		String sql = "select r.id rid,g.id as gid,g.name as name,r.scroe as scroe,r.time as time from t_mall_scroe_record r,t_mall_goods g where r.relation_id = g.id and r.type = ? and r.user_id = ? union select r.id rid,g.id as gid,g.typeName as name,r.scroe as scroe,r.time as time  from t_mall_scroe_record r,t_red_packages_type g where r.relation_id = g.id and r.type = ? and r.user_id = ? order by time desc";

		return JPAUtil.getList(error, sql, MallConstants.EXCHANGE, user_id, MallConstants.EXCHANGE_RED, user_id);
	}

	/**
	 * 兑换商品排行
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> queryHasExchangedGoodsOrder(Integer num, ErrorInfo error) {
		StringBuilder sql = new StringBuilder("select g.id,g.name,g.max_exchange_count-g.surplus as count from t_mall_goods g ");
		sql.append("order by g.max_exchange_count-g.surplus desc,g.surplus desc ");
		if (num != null && num > 0) {
			sql.append("limit ?,?");
			return JPAUtil.getList(error, sql.toString(), 0, num);
		}
		return JPAUtil.getList(error, sql.toString());
	}
	
	/**
	 * app查询用户兑换物品信息
	 * @param userId
	 * @param error
	 * @return
	 */
	public static List<Map<String, Object>> queryAppExchangedInfo(long userId, ErrorInfo error) {
		String sql = "SELECT r.id,r.time,CASE r.status WHEN 1 then 1 ELSE 0 end as status ,g.pic_path,g.name from t_mall_scroe_record r LEFT JOIN t_mall_goods g ON r.relation_id = g.id where  r.user_id = ? and r.type in (5,6) ORDER BY r.time desc";
		
		return JPAUtil.getList(error, sql.toString(),userId);
	}

	/**
	 * 兑换商品排行
	 * 
	 * @return
	 */
	public static PageBean<v_mall_goods_views> queryHasExchangedGoodsOrder(Integer currPage, Integer pageSize, ErrorInfo error) {
		StringBuilder sql = new StringBuilder(
				"select g.id,g.name,g.max_exchange_count-g.surplus as count,pic_path,surplus,exchange_scroe from t_mall_goods g where g.visible = 1 ");
		sql.append("order by id asc");
		PageBean<v_mall_goods_views> pageBean = new PageBean<v_mall_goods_views>();
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		List<v_mall_goods_views> mgv = new ArrayList<v_mall_goods_views>();

		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), v_mall_goods_views.class);
			query.setFirstResult((pageBean.currPage - 1) * pageBean.pageSize);
			query.setMaxResults(pageBean.pageSize);
			mgv = query.getResultList();

			StringBuilder sql_count = new StringBuilder();
			sql_count.append("select count(*) from ( ").append(sql).append(" )c");
			Query queryCount = em.createNativeQuery(sql_count.toString());
			pageBean.totalCount = Convert.strToInt(queryCount.getResultList().get(0) + "", 0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询查询推广会员列表详情时：" + e.getMessage());

			return pageBean;
		}

		pageBean.page = mgv;
		return pageBean;
	}

	/**
	 * 兑换动态
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> queryHasExchangedGoodsNow(Integer num, ErrorInfo error) {
		StringBuilder sql = new StringBuilder("select t.user_name,g.name from t_mall_scroe_record t,t_mall_goods g ");
		sql.append("where t.relation_id = g.id and t.type = ? order by t.time desc ");
		if (num != null && num > 0) {
			sql.append("limit ?,?");
			return JPAUtil.getList(error, sql.toString(), MallConstants.EXCHANGE, 0, num);
		}

		return JPAUtil.getList(error, sql.toString(), MallConstants.EXCHANGE);
	}

	/**
	 * 立即兑换，兑换商品逻辑处理（锁机制）
	 * 
	 * @param exchangeNum
	 * @param user
	 * @param goods
	 * @param postDetail
	 * @param error
	 */
	public static synchronized void exchangeHandle(int exchangeNum, User user, t_mall_goods goods, String postDetail, ErrorInfo error) {
		// 更新剩余数量
		MallGoods.updateSurplus(exchangeNum, user.id, goods.id, error);
		// 更新失败
		if (error.code < 0) {
			return;
		}
		// 保存积分兑换记录
		MallScroeRecord.saveScroeExchangeRecord(exchangeNum, user, goods, postDetail, error);
	}

	/**
	 * 修改剩余量(数据库锁机制保证数据安全，又可做商品剩余数量、积分的校验)
	 * 
	 * @param exchangeNum
	 * @param userScroe
	 * @param user_id
	 * @param error
	 */
	public static void updateSurplus(int exchangeNum, long user_id, long goods_id, ErrorInfo error) {
		String sql = "update t_mall_goods g set g.surplus = g.surplus - ? where g.surplus >= ? "
				+ "and g.exchange_scroe * ? <= (SELECT SUM(scroe) FROM t_mall_scroe_record WHERE user_id = ?) and g.id = ?";
		JPAUtil.executeUpdate(error, sql, exchangeNum, exchangeNum, exchangeNum, user_id, goods_id);
	}

	/**
	 * 当前商品兑换动态
	 * 
	 * @return
	 */
	public static List<Map<String, Object>> currentExchangeNews() {
		ErrorInfo error = new ErrorInfo();

		Object exchangeNewsObj = Cache.get("exchangeNews");
		if (exchangeNewsObj == null) {
			List<Map<String, Object>> goodsList = MallGoods.queryHasExchangedGoodsOrder(Constants.PAGE_SIZE, error);
			Cache.set("exchangeNews", goodsList);
			return goodsList;
		}

		return (List<Map<String, Object>>) exchangeNewsObj;
	}

	/**
	 * 当前常见问题
	 * 
	 * @return
	 */
	public static PageBean<t_content_news> currentQuestionNews() {
		ErrorInfo error = new ErrorInfo();

		Object obj = Cache.get("cpage");
		if (obj == null) {
			PageBean<t_content_news> cpage = News.queryNewsByTypeId(String.valueOf(MallConstants.COMMEN_QUESTION), "0", "10", null, error);
			Cache.set("cpage", cpage);
			return cpage;
		}

		return (PageBean<t_content_news>) obj;
	}

	public static List<t_mall_goods> queryGoods() {
		List<t_mall_goods> mgs = null;
		try {
			mgs = t_mall_goods.find(" status=? and visible=? and surplus>0 ", 1, 1).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return mgs;
	}

	public static void updateSurplusOfCJ(int exchangeNum, long user_id, long goods_id, ErrorInfo error) {
		String sql = "update t_mall_goods g set g.surplus = g.surplus - ? where g.surplus >= ?  and g.id = ?";
		JPAUtil.executeUpdate(error, sql, exchangeNum, exchangeNum, goods_id);
	}

	/**
	 * 用户已兑换商品信息
	 * 
	 * @return
	 */
	public static PageBean<t_mall_scroe_record> queryHasExchangedGoodsForPage(long user_id, int currPage, int pageSize, ErrorInfo error) {

		error.clear();

		PageBean<t_mall_scroe_record> page = new PageBean<t_mall_scroe_record>();

		page.pageSize = pageSize;
		if (currPage <= 0) {
			currPage = 1;
		}
		page.currPage = currPage;

		String sql = "select new t_mall_scroe_record(r.time,r.scroe,r.quantity,g.id,g.name,g.pic_path) from t_mall_scroe_record r,t_mall_goods g "
				+ "where r.relation_id = g.id and r.type in (?,?) and r.user_id = ? order by r.time desc";
		String countSql = "select count(*) from t_mall_scroe_record r,t_mall_goods g where r.relation_id = g.id and r.type  in (?,?) and r.user_id = ?";

		List<Object> params = new ArrayList<Object>();

		params.add(MallConstants.EXCHANGE);
		params.add(MallConstants.EXCHANGE_RED);

		params.add(user_id);

		int count = 0;

		EntityManager em = JPA.em();

		Query query = em.createNativeQuery(countSql);

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
			query = em.createQuery(sql.toString(), t_mall_scroe_record.class);
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
}
