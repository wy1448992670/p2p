package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bill_invests;
import models.t_content_news;
import models.t_invests;
import models.t_red_packages_type;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.Security;
import constants.Constants;
import constants.CouponTypeEnum;

/**
 * 
 * @author lxiong 红包类型
 */
public class RedPackage implements Serializable {

	public long id;
	private long _id;

	public String name; // '红包类型',
	public String typeName; // '红包类型',
	public Date time; // '创建时间',
	public long validityTime; // '有效期',
	public long validateUnit; // '有效期单位 -1年 0月 1日',
	public double money; // '红包金额',
	public double obtainMoney; // 获取条件',
	public double validityMoney; // 起投金额',
	public boolean noticeEmail; // 通知方式邮件
	public boolean noticeMessage; // '通知方式短信',
	public boolean noticeBox; // '通知方式站内',
	public boolean status; // '状态 -1 禁用中 0 启用中'
	public String rules; // 规则
	public int isNewUser; // 新手标是否可用
	public int bidPeriod; // 最低标的期限限制
	public int bidPeriodUnit;
	public String sign; // 加密ID
	public String itemCode;
	public Double received_money_start;//回款本金 起始
	public Double received_money_end;//回款本金 截止
	public Double all_invest_money;//累计投资金额
	public Integer all_invest_count;//累计投资笔数
	public Integer reg_time;//注册月数

	public long getId() {
		return this._id;
	}

	/**
	 * 获取加密ID
	 */
	public String getSign() {
		if (null == this.sign) {
			this.sign = Security.addSign(this.id, Constants.RID_ID_SIGN);
		}
		return this.sign;
	}

	public void setId(long id) {
		t_red_packages_type trpt = null;

		try {
			trpt = t_red_packages_type.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时：" + e.getMessage());
			this._id = -1;
			return;
		}

		this._id = id;

		this.name = trpt.name; // '红包类型',
		this.typeName = trpt.typeName; // '红包类型',
		this.time = trpt.time; // '创建时间',
		this.validityTime = trpt.validity_time; // '有效期',
		this.validateUnit = trpt.validate_unit; // '有效期单位 -1年 0月 1日',
		this.money = trpt.money; // '红包金额',
		this.obtainMoney = trpt.obtain_money; // 获取条件',
		this.validityMoney = trpt.validity_money; // 起投金额',
		this.noticeEmail = trpt.notice_email; // 通知方式邮件
		this.noticeMessage = trpt.notice_message; // '通知方式短信',
		this.noticeBox = trpt.notice_box; // '通知方式站内',
		this.status = trpt.status; // '状态 -1 禁用中 0 启用中'
		this.isNewUser = trpt.is_new_user;
		this.bidPeriod = trpt.bid_period;
		this.bidPeriodUnit = trpt.bid_period_unit;
		this.rules = trpt.rules == null ? "" : trpt.rules.replace("\"", "'");
		this.itemCode = trpt.item_code;
		this.received_money_start=trpt.received_money_start;//回款本金 起始
		this.received_money_end=trpt.received_money_end;//回款本金 截止
		this.all_invest_money=trpt.all_invest_money;//累计投资金额
		this.all_invest_count=trpt.all_invest_count;//累计投资笔数
		this.reg_time=trpt.reg_time;//注册月数
	}

	/**
	 * 保存红包
	 *
	 * @param params
	 */
	@Deprecated
	public static void addDetails(Params params, ErrorInfo error) {
		t_red_packages_type redType = validatForm(params, error);
		if (error.code >= 0) {//
			try {
				// 先查询是否存在该类型的红包
				if (checkType(redType.name) || StringUtils.endsWith(redType.name, Constants.RED_PACKAGE_TYPE_SCROE)) {
					redType.save();
				} else {
					error.code = -1;
					error.msg = "操作失败!已经存在该类型的红包设置了";
				}
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "红包类型保存失败";
			}
		}

	}

	/**
	 * 是否存在该类型红包
	 *
	 * @param name2
	 * @return
	 */
	private static boolean checkType(String name) {
		String sql = "SELECT * from t_red_packages_type where name =  ?";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
		query.setParameter(1, name);
		List list = query.getResultList();
		if (null == list || list.size() < 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证红包设置字段
	 */
	@Deprecated
	public static t_red_packages_type validatForm(Params params, ErrorInfo error) {
		t_red_packages_type db = new t_red_packages_type();

		if (null == params.get("name") || StringUtils.isBlank(params.get("name"))) {
			error.code = -1;
			error.msg = "红包场景有误";
			return null;
		}
		db.name = params.get("name"); // '红包类型',

		if (null == params.get("typeName") || StringUtils.isBlank(params.get("typeName"))) {
			error.code = -1;
			error.msg = "红包名称有误";
			return null;
		}
		db.typeName = params.get("typeName");
		; // '红包类型',
		db.time = new Date(); // '创建时间',

		if (null == params.get("validity_time") || StringUtils.isBlank(params.get("validity_time"))) {
			error.code = -1;
			error.msg = "红包'有效期有误";
			return null;
		}
		db.validity_time = Long.valueOf(params.get("validity_time")); // '有效期',

		if (null == params.get("validate_unit") || StringUtils.isBlank(params.get("validate_unit"))) {
			error.code = -1;
			error.msg = "红包有效期单位有误";
			return null;
		}
		db.validate_unit = Long.valueOf(params.get("validate_unit")); // '有效期',

		if (null == params.get("amount") || StringUtils.isBlank(params.get("amount"))) {
			error.code = -1;
			error.msg = "红包金额有误";
			return null;
		}
		db.money = Double.valueOf(params.get("amount"));

		if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
			error.code = -1;
			error.msg = "红包获取条件有误";
			return null;
		}
		db.obtain_money = Double.valueOf(params.get("obtain_money"));

		if (null == params.get("validity_money") || StringUtils.isBlank(params.get("validity_money"))) {
			error.code = -1;
			error.msg = "红包起投金额'有误";
			return null;
		}
		db.validity_money = Double.valueOf(params.get("validity_money"));

		if (null == params.get("notice_email") || StringUtils.isBlank(params.get("notice_email"))) {
			db.notice_email = false;
		} else {
			db.notice_email = true;
		}

		if (null == params.get("notice_message") || StringUtils.isBlank(params.get("notice_message"))) {
			db.notice_message = false;
		} else {
			db.notice_message = true;
		}

		if (null == params.get("notice_box") || StringUtils.isBlank(params.get("notice_box"))) {
			db.notice_box = false;
		} else {
			db.notice_box = true;
		}

		db.status = true;// '状态 -1 禁用中 0 启用中'

		return db;
	}

	/**
	 * 验证表单信息（保存）
	 *
	 * @param params
	 * @param id
	 * @param error
	 * @return
	 */
	@Deprecated
	public static t_red_packages_type validatUpdateForm(Params params, long id, ErrorInfo error) {
		t_red_packages_type db = t_red_packages_type.findById(id);

		if (null == params.get("name") || StringUtils.isBlank(params.get("name"))) {
			error.code = -1;
			error.msg = "红包场景有误";
			return null;
		}
		db.name = params.get("name"); // '红包类型',

		if (null == params.get("typeName") || StringUtils.isBlank(params.get("typeName"))) {
			error.code = -1;
			error.msg = "红包名称有误";
			return null;
		}
		db.typeName = params.get("typeName");
		; // '红包类型',
		db.time = new Date(); // '创建时间',

		if (null == params.get("validity_time") || StringUtils.isBlank(params.get("validity_time"))) {
			error.code = -1;
			error.msg = "红包'有效期有误";
			return null;
		}
		db.validity_time = Long.valueOf(params.get("validity_time")); // '有效期',

		if (null == params.get("validate_unit") || StringUtils.isBlank(params.get("validate_unit"))) {
			error.code = -1;
			error.msg = "红包有效期单位有误";
			return null;
		}
		db.validate_unit = Long.valueOf(params.get("validate_unit")); // '有效期',

		if (null == params.get("amount") || StringUtils.isBlank(params.get("amount"))) {
			error.code = -1;
			error.msg = "红包金额有误";
			return null;
		}
		db.money = Double.valueOf(params.get("amount"));

		if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
			error.code = -1;
			error.msg = "红包获取条件有误";
			return null;
		}
		db.obtain_money = Double.valueOf(params.get("obtain_money"));

		if (null == params.get("validity_money") || StringUtils.isBlank(params.get("validity_money"))) {
			error.code = -1;
			error.msg = "红包起投金额有误";
			return null;
		}
		db.validity_money = Double.valueOf(params.get("validity_money"));

		if (null == params.get("notice_email") || StringUtils.isBlank(params.get("notice_email"))) {
			db.notice_email = false;
		} else {
			db.notice_email = StringUtils.equals(params.get("notice_email"), "on") ? true : false;
		}

		if (null == params.get("notice_message") || StringUtils.isBlank(params.get("notice_message"))) {
			db.notice_message = false;
		} else {
			db.notice_message = StringUtils.equals(params.get("notice_message"), "on") ? true : false;
		}

		if (null == params.get("notice_box") || StringUtils.isBlank(params.get("notice_box"))) {
			db.notice_box = false;
		} else {
			db.notice_box = StringUtils.equals(params.get("notice_box"), "on") ? true : false;
		}
		return db;
	}

	/**
	 * 查询红包类型
	 *
	 * @param error
	 * @return
	 */
	public static PageBean<t_red_packages_type> redTypeList(ErrorInfo error) {
		List<t_red_packages_type> redpacks = null;
		StringBuffer sql = new StringBuffer("select * from t_red_packages_type where 1=1 ");
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
			redpacks = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询红包类型设置时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询红包类型操作失败";
			return null;
		}

		PageBean<t_red_packages_type> page = new PageBean<t_red_packages_type>();
		page.page = redpacks;
		error.code = 0;
		return page;
	}

	/**
	 * 禁用红包
	 *
	 * @param sign2
	 * @param error
	 */
	public static void disableRedType(String sign, ErrorInfo error) {
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		t_red_packages_type trpt = t_red_packages_type.findById(redTypeId);
		if (null == trpt) {
			error.code = -1;
			error.msg = "操作失败!";
			return;
		} else {
			trpt.status = false;
			try {
				trpt.save();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "操作失败!";
			}
		}
	}

	/**
	 * 启用红包类型
	 *
	 * @param sign
	 * @param error
	 */
	public static void enableRedType(String sign, ErrorInfo error) {
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		t_red_packages_type trpt = t_red_packages_type.findById(redTypeId);
		if (null == trpt) {
			error.code = -1;
			error.msg = "操作失败!";
			return;
		} else {
			trpt.status = true;
			try {
				trpt.save();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "操作失败!";
			}
		}
	}

	/**
	 * 物理删除红包类型
	 *
	 * @param sign
	 * @param error
	 */
	public static void deleteRedType(String sign, ErrorInfo error) {
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
			return;

		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		try {
			String sql = "delete from t_red_packages_type where id = ?";
			t_red_packages_type.delete(sql, redTypeId);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "操作失败!";
		}
	}

	/**
	 * 查询详细
	 *
	 * @param sign
	 * @param error
	 * @return
	 */
	public static RedPackage details(String sign, ErrorInfo error) {
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		RedPackage redPack = new RedPackage();
		redPack.id = redTypeId;
		return redPack;

	}

	/**
	 * 修改红包类型状态
	 *
	 * @param params
	 * @param error
	 */
	public static void updateDetails(Params params, ErrorInfo error) {
		String sign = params.get("sign");
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		t_red_packages_type trpt = validatUpdateForm(params, redTypeId, error);
		if (null == trpt) {
			error.code = -1;
			error.msg = "操作失败!";
			return;
		}
		trpt.save();

	}

	/**
	 * 查询是否存在某个红包类型是否存在并启用
	 *
	 * @param redTypeName
	 * @param error
	 * @return
	 */
	public static t_red_packages_type isExist(String redTypeName, long status) {
		List<t_red_packages_type> redpacks = null;
		String sql = "select * from t_red_packages_type where name = ? and status = ? order by time desc ";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
		query.setParameter(1, redTypeName);
		query.setParameter(2, status);
		redpacks = query.getResultList();
		if (null == redpacks || redpacks.size() < 1) {
			return null;
		} else {
			return redpacks.get(0);
		}
	}

	public static List<t_red_packages_type> getRegRed(String redTypeName, long status) {
		List<t_red_packages_type> redpacks = null;
		String sql = "select * from t_red_packages_type where name = ? and status = ? order by time desc ";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
		query.setParameter(1, redTypeName);
		query.setParameter(2, status);
		redpacks = query.getResultList();
		return redpacks;
	}

	/**
	 * 修改个人用户红包状态
	 *
	 * @param id
	 * @return
	 */
	public static int updateRedPackagesHistory(long id, long status) {
		String sql = "update t_red_packages_history set status = ? where id = ? and status = -2";
		int row = 0;
		try {

			Query query = JPA.em().createQuery(sql).setParameter(1, status).setParameter(2, id);
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.info("修改用户红包状态时出现问题id====" + id);

			e.printStackTrace();
			return row;
		}
		return row;
	}

	/**
	 * 积分商城红包展示
	 * 
	 * @param name2
	 * @return
	 */
	public static List<t_red_packages_type> queryRedPackagesList() {
		List<t_red_packages_type> list = null;
		try {
			EntityManager em = JPA.em();
			StringBuffer sql = new StringBuffer();
			sql.append(
					"select * from t_red_packages_type where name = ? and status = ?  and (case when validate_unit = -1 then date_add(time, interval validity_time year) when validate_unit = 0 then date_add(time, interval validity_time month) when validate_unit = 1 then date_add(time, interval validity_time day) else time end) > now() order by money ");
			Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
			query.setParameter(1, Constants.RED_PACKAGE_TYPE_SCROE);
			query.setParameter(2, true);
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	public static PageBean<t_red_packages_type> queryPage(String start, String end, String type, String name,
			String currPageStr, String pageSizeStr, int orderIndex, int orderStatus, ErrorInfo error) {
		int currPage = Constants.ONE;
		if (StringUtils.isNotBlank(currPageStr)) {
			try {
				currPage = Integer.parseInt(currPageStr);
			} catch (NumberFormatException e) {
			}
		}
		int pageSize = Constants.TEN;
		if (StringUtils.isNotBlank(currPageStr)) {
			try {
				pageSize = Integer.parseInt(pageSizeStr);
			} catch (NumberFormatException e) {
			}
		}

		int count = 0;
		List<t_red_packages_type> packs = new ArrayList<t_red_packages_type>();

		StringBuffer sql = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		sql.append(" 1=1 ");
		if (StringUtils.isNotBlank(start)) {
			sql.append(" and time >= ?");
			params.add(DateUtil.strDateToStartDate(start));
		}

		if (StringUtils.isNotBlank(end)) {
			sql.append(" and time <= ?");
			params.add(DateUtil.strDateToEndDate(end));

		}

		if (StringUtils.isNotBlank(type)) {
			sql.append(" and typeName = ?");
			params.add(type);
		}

		if (StringUtils.isNotBlank(name) && !"0".equals(name)) {
			sql.append(" and name = ?");
			params.add(name);
		}

		Integer couponFlag=Integer.parseInt(Session.current().get("couponFlag"));
		couponFlag=RedPackageHistory.storeCouponInfo(couponFlag);
		sql.append(" and coupon_type = ?");
		params.add(couponFlag);
		PageBean<t_red_packages_type> page = new PageBean<t_red_packages_type>();

		String sqlOrderBy = "";
		if (orderIndex == 0) {
			String order_sql = Constants.RED_PACKAGE_ORDER_CONDITION[orderIndex];
			sqlOrderBy += " " + String.format(order_sql, "");
		} else {
			String order_sql = Constants.RED_PACKAGE_ORDER_CONDITION[orderIndex];
			sqlOrderBy += " " + String.format(order_sql, "");
			if (orderStatus == 1) {
				sqlOrderBy += " ASC";
			} else {
				sqlOrderBy += " DESC";
			}
		}

		try {
			count = (int) t_red_packages_type.count(sql.toString(), params.toArray());
			packs = t_red_packages_type.find(sql.toString() + sqlOrderBy, params.toArray()).fetch(currPage, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询自动红包列表时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，查询红包类型操作失败";
			return page;
		}

		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = packs;
		page.totalCount = count;
		error.code = 0;
		return page;

	}

	public static t_red_packages_type validatFormAdd(Params params, ErrorInfo error) {
		t_red_packages_type db = new t_red_packages_type();
		
		String couponName=Session.current().get("couponName");
		int couponFlag=Integer.parseInt(Session.current().get("couponFlag"));

			db.coupon_type=couponFlag;//优惠券类型

		if (null == params.get("name") || StringUtils.isBlank(params.get("name"))) {
			error.code = -1;
			error.msg = couponName+"类型有误";
			return null;
		}
		db.name = params.get("name"); // '红包类型',
		db.item_code = params.get("item_code");

		if (null == params.get("typeName") || StringUtils.isBlank(params.get("typeName"))) {
			error.code = -1;
			error.msg = couponName+"名称有误";
			return null;
		}
		db.typeName = params.get("typeName");
		; // '红包类型',
		db.time = new Date(); // '创建时间',

		if (null == params.get("validity_time") || StringUtils.isBlank(params.get("validity_time"))) {
			error.code = -1;
			error.msg = couponName+"'有效期有误";
			return null;
		}
		db.validity_time = Long.valueOf(params.get("validity_time")); // '有效期',

		if (null == params.get("validate_unit") || StringUtils.isBlank(params.get("validate_unit"))) {
			error.code = -1;
			error.msg = couponName+"有效期单位有误";
			return null;
		}
		db.validate_unit = Long.valueOf(params.get("validate_unit")); // '有效期',

		if (null == params.get("amount") || StringUtils.isBlank(params.get("amount"))) {
			error.code = -1;
			error.msg = couponName+"金额有误";
			return null;
		}
		db.money = Double.valueOf(params.get("amount")); // 红包金额

		String rules = params.get("rules");
		JSONObject rule = JSONObject.parseObject(rules);
		// 投资 累计投资 自定义红包
		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVEST) || db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS)
				|| db.name.equals(Constants.RED_PACKAGE_TYPE_CUSTOM)) {
			if (rule == null || rule.size() == 0) {
				error.code = -1;
				error.msg = couponName+"获取规则有误";
				return null;
			}
			db.received_money_end=rule.getDouble("received_money_end");
			db.received_money_start=rule.getDouble("received_money_start");
			db.rules = rules;
		}

		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVEST)) {
			if (rule.getInteger("first") == null) {
				error.code = -1;
				error.msg = "是否首投"+couponName+"有误";
				return null;
			}
			Integer all = rule.getInteger("all");
			if (all == null) {
				error.code = -1;
				error.msg = "是否秒标"+couponName+"有误";
				return null;
			}
			if (all == 0) {
				if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
					error.code = -1;
					error.msg = couponName+"获取条件有误";
					return null;
				}
				db.obtain_money = Double.valueOf(params.get("obtain_money"));
			}
		}
		// 回款红包判断
		if (db.name.equals(Constants.RED_PACKAGE_TYPE_RECEIVED)) {
			if (rule.getDouble("received_money_start") == null || rule.getDouble("received_money_end") == null) {
				error.code = -1;
				error.msg = "回款本金设置有误";
				return null;
			}
			db.received_money_end=rule.getDouble("received_money_end");
			db.received_money_start=rule.getDouble("received_money_start");
			Double all_invest_money = rule.getDouble("all_invest_money");
			if (all_invest_money == null) {
				error.code = -1;
				error.msg = "累计投资金额设置有误";
				return null;
			}
			db.all_invest_money=all_invest_money;
			Integer all_invest_count = rule.getInteger("all_invest_count");
			if (all_invest_count == null) {
				error.code = -1;
				error.msg = "累计投资笔数设置有误";
				return null;
			}
			db.all_invest_count=all_invest_count;
			Integer reg_time = rule.getInteger("reg_time");
			if (reg_time == null) {
				error.code = -1;
				error.msg = "注册时间设置有误";
				return null;
			}
			db.reg_time=reg_time;

		}

		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS) || db.name.equals(Constants.RED_PACKAGE_TYPE_CUSTOM)) {
			String start = rule.getString("start");
			String end = rule.getString("end");
			if (StringUtils.isBlank(start)) {
				error.code = -1;
				error.msg = "起始日期有误";
				return null;
			}
			if (StringUtils.isBlank(end)) {
				error.code = -1;
				error.msg = "结束日期有误";
				return null;
			}
			if (DateUtil.daysBetween(DateUtil.strDateToEndDate(start), DateUtil.strDateToEndDate(end)) < 0) {
				error.code = -1;
				error.msg = "结束日期不能小于起始日期有误";
				return null;
			}
			if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS)) {
				if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
					error.code = -1;
					error.msg = couponName+"获取条件有误";
					return null;
				}
				db.obtain_money = Double.valueOf(params.get("obtain_money"));
			}
		}

		if (null == params.get("validity_money") || StringUtils.isBlank(params.get("validity_money"))) {
			error.code = -1;
			error.msg = couponName+"最低投资金额限制有误";
			return null;
		}
		db.validity_money = Double.valueOf(params.get("validity_money"));

		if (StringUtils.isBlank(params.get("is_new_user"))) {
			error.code = -1;
			error.msg = "新手标是否使用有误";
			return null;
		}
		db.is_new_user = Integer.parseInt(params.get("is_new_user"));

		if (StringUtils.isBlank(params.get("bid_period"))) {
			error.code = -1;
			error.msg = "最低标的期限限制有误";
			return null;
		}
		db.bid_period = Integer.parseInt(params.get("bid_period"));
		if (params.get("bid_period_unit") == null) {
			db.bid_period_unit = 0;
		} else {
			db.bid_period_unit = Integer.parseInt(params.get("bid_period_unit"));
		}

		if (null == params.get("notice_email") || StringUtils.isBlank(params.get("notice_email"))) {
			db.notice_email = false;
		} else {
			db.notice_email = true;
		}

		if (null == params.get("notice_message") || StringUtils.isBlank(params.get("notice_message"))) {
			db.notice_message = false;
		} else {
			db.notice_message = true;
		}

		if (null == params.get("notice_box") || StringUtils.isBlank(params.get("notice_box"))) {
			db.notice_box = false;
		} else {
			db.notice_box = true;
		}

		db.status = true;// '状态 -1 禁用中 0 启用中'

		return db;
	}

	public static t_red_packages_type validatFormUpdate(Params params, long id, ErrorInfo error) {
		t_red_packages_type db = t_red_packages_type.findById(id);

		if (null == params.get("typeName") || StringUtils.isBlank(params.get("typeName"))) {
			error.code = -1;
			error.msg = "红包名称有误";
			return null;
		}
		db.item_code = params.get("item_code");
		db.typeName = params.get("typeName");
		; // '红包类型',
		db.time = new Date(); // '创建时间',

		if (null == params.get("validity_time") || StringUtils.isBlank(params.get("validity_time"))) {
			error.code = -1;
			error.msg = "红包'有效期有误";
			return null;
		}
		db.validity_time = Long.valueOf(params.get("validity_time")); // '有效期',

		if (null == params.get("validate_unit") || StringUtils.isBlank(params.get("validate_unit"))) {
			error.code = -1;
			error.msg = "红包有效期单位有误";
			return null;
		}
		db.validate_unit = Long.valueOf(params.get("validate_unit")); // '有效期',

		if (null == params.get("amount") || StringUtils.isBlank(params.get("amount"))) {
			error.code = -1;
			error.msg = "红包金额有误";
			return null;
		}
		db.money = Double.valueOf(params.get("amount"));

		String rules = params.get("rules");
		db.rules = rules;
		JSONObject rule = JSONObject.parseObject(rules);
		if (rule == null || rule.size() == 0) {
			db.obtain_money = 0.0;
		}
		// 投资 累计投资 自定义红包
		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVEST) || db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS)
				|| db.name.equals(Constants.RED_PACKAGE_TYPE_CUSTOM)) {
			if (rule == null || rule.size() == 0) {
				error.code = -1;
				error.msg = "红包获取规则有误";
				return null;
			}
		}

		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVEST)) {
			if (rule.getInteger("first") == null) {
				error.code = -1;
				error.msg = "是否首投红包有误";
				return null;
			}
			Integer all = rule.getInteger("all");
			if (all == null) {
				error.code = -1;
				error.msg = "是否秒标红包有误";
				return null;
			}
			if (all == 0) {
				if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
					error.code = -1;
					error.msg = "红包获取条件有误";
					return null;
				}
				db.obtain_money = Double.valueOf(params.get("obtain_money"));
			}
		}

		if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS) || db.name.equals(Constants.RED_PACKAGE_TYPE_CUSTOM)) {
			String start = rule.getString("start");
			String end = rule.getString("end");
			if (StringUtils.isBlank(start)) {
				error.code = -1;
				error.msg = "起始日期有误";
				return null;
			}
			if (StringUtils.isBlank(end)) {
				error.code = -1;
				error.msg = "结束日期有误";
				return null;
			}
			if (DateUtil.daysBetween(DateUtil.strDateToEndDate(start), DateUtil.strDateToEndDate(end)) < 0) {
				error.code = -1;
				error.msg = "结束日期不能小于起始日期有误";
				return null;
			}
			if (db.name.equals(Constants.RED_PACKAGE_TYPE_INVESTS)) {
				if (null == params.get("obtain_money") || StringUtils.isBlank(params.get("obtain_money"))) {
					error.code = -1;
					error.msg = "红包获取条件有误";
					return null;
				}
				db.obtain_money = Double.valueOf(params.get("obtain_money"));
			}
		}

		// 回款红包判断
		if (db.name.equals(Constants.RED_PACKAGE_TYPE_RECEIVED)) {
			if (rule.getDouble("received_money_start") == null || rule.getDouble("received_money_end") == null) {
				error.code = -1;
				error.msg = "回款本金设置有误";
				return null;
			}
			db.received_money_end=rule.getDouble("received_money_end");
			db.received_money_start=rule.getDouble("received_money_start");
			Double all_invest_money = rule.getDouble("all_invest_money");
			if (all_invest_money == null) {
				error.code = -1;
				error.msg = "累计投资金额设置有误";
				return null;
			}
			db.all_invest_money=all_invest_money;
			Integer all_invest_count = rule.getInteger("all_invest_count");
			if (all_invest_count == null) {
				error.code = -1;
				error.msg = "累计投资笔数设置有误";
				return null;
			}
			db.all_invest_count=all_invest_count;
			Integer reg_time = rule.getInteger("reg_time");
			if (reg_time == null) {
				error.code = -1;
				error.msg = "注册时间设置有误";
				return null;
			}
			db.reg_time=reg_time;

		}

		
		if (null == params.get("validity_money") || StringUtils.isBlank(params.get("validity_money"))) {
			error.code = -1;
			error.msg = "红包最低投资金额限制有误";
			return null;
		}
		db.validity_money = Double.valueOf(params.get("validity_money"));

		if (StringUtils.isBlank(params.get("is_new_user"))) {
			error.code = -1;
			error.msg = "新手标是否使用有误";
			return null;
		}
		db.is_new_user = Integer.parseInt(params.get("is_new_user"));

		if (StringUtils.isBlank(params.get("bid_period"))) {
			error.code = -1;
			error.msg = "最低标的期限限制有误";
			return null;
		}
		db.bid_period = Integer.parseInt(params.get("bid_period"));
		if (params.get("bid_period_unit") == null) {
			db.bid_period_unit = 0;
		} else {
			db.bid_period_unit = Integer.parseInt(params.get("bid_period_unit"));
		}

		if (null == params.get("notice_email") || StringUtils.isBlank(params.get("notice_email"))) {
			db.notice_email = false;
		} else {
			db.notice_email = StringUtils.equals(params.get("notice_email"), "on") ? true : false;
		}

		if (null == params.get("notice_message") || StringUtils.isBlank(params.get("notice_message"))) {
			db.notice_message = false;
		} else {
			db.notice_message = StringUtils.equals(params.get("notice_message"), "on") ? true : false;
		}

		if (null == params.get("notice_box") || StringUtils.isBlank(params.get("notice_box"))) {
			db.notice_box = false;
		} else {
			db.notice_box = StringUtils.equals(params.get("notice_box"), "on") ? true : false;
		}
		return db;
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月2日
	 * @description 添加红包
	 * @param params
	 * @param error
	 */
	public static void addRedPackage(Params params, ErrorInfo error) {
		t_red_packages_type redType = validatFormAdd(params, error);
		if (error.code >= 0) {//
			try {
				// TODO 红包重复校验
				redType.save();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				error.msg = "红包保存失败";
			}
		}
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月3日
	 * @description 修改红包
	 * @param params
	 * @param error
	 */
	public static void updateRedPackage(Params params, ErrorInfo error) {
		String sign = params.get("sign");
		if (StringUtils.isBlank(sign)) {
			error.code = -1;
			error.msg = "ID不能为空";
		}
		long redTypeId = Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, error);
		t_red_packages_type trpt = validatFormUpdate(params, redTypeId, error);
		if (null != trpt) {
			trpt.save();
		}

	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月9日
	 * @description 查询累计投资红包
	 * @param date
	 * @return
	 */
	public static List<t_red_packages_type> findInvestsRedPack(String date) {
		String sql = "from t_red_packages_type where name = ? and status = ? and INSTR(rules,?) > 0";
		date = "\"end\":\"" + date + "\"";
		return t_red_packages_type.find(sql, Constants.RED_PACKAGE_TYPE_INVESTS, true, date).fetch();
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月9日
	 * @description 查询自定义红包
	 * @return
	 */
	public static List<t_red_packages_type> findCustomRedPack() {
		String sql = "from t_red_packages_type where name = ? and status = ?";
		return t_red_packages_type.find(sql, Constants.RED_PACKAGE_TYPE_CUSTOM, true).fetch();
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月9日
	 * @description 获取投资红包
	 * @param user_id     用户ID
	 * @param amount      标的总额
	 * @param investTotal 投资金额
	 * @return
	 */
	public static List<t_red_packages_type> findInvestRedPack(long user_id, double amount, double investTotal,
			long investId) {
		String sql = "select count(1) as count from t_invests i join t_bids b on i.bid_id = b.id where i.user_id = ? and b.status in(?,?,?) and i.id <>? ";
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, user_id, Constants.BID_REPAYMENT,
				Constants.BID_COMPENSATE_REPAYMENT, Constants.BID_REPAYMENTS, investId);
		int count = 0;
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		sql = "select * from t_red_packages_type where name = ? and status = ? ";
		String first = "";
		if (count > 0) {// 再投
			first = "\"first\":\"0\"";
		} else {// 首投
			first = "\"first\":\"1\"";
		}
		sql += " and instr(rules,?) > 0";

		List<Object> params = new ArrayList<Object>();
		params.add(Constants.RED_PACKAGE_TYPE_INVEST);
		params.add(Constants.RED_PACKAGE_TYPE_STATUS_ENABLED);
		params.add(first);

		String all = "";
		if (investTotal - amount <= 0.01 && investTotal - amount >= 0) {// 秒标
			all = "\"all\":\"1\"";
		} else {// 普通
			all = "\"all\":\"0\"";
			sql += " and obtain_money <= ?";
			params.add(investTotal);
		}
		sql += " and instr(rules,?) > 0";
		params.add(all);
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_type.class);
		int i = 1;
		for (Object p : params) {
			query.setParameter(i, p);
			i++;
		}
		return query.getResultList();
	}

}
