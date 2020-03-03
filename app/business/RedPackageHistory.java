package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_red_packages_history;
import models.t_red_packages_history_count;
import models.t_red_packages_type;
import models.t_users;
import models.v_user_info;
import models.v_user_info_v1;
import net.sf.json.JSONArray;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Strings;

import com.alibaba.fastjson.JSONObject;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Session;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import constants.Constants;
import constants.CouponTypeEnum;
import constants.DealType;
import constants.Templets;

/**
 * 
 * @author lxiong 红包类型
 */
public class RedPackageHistory implements Serializable {
	public static String couponName = CouponTypeEnum.REDBAG.getName();
	public long id;
	private long _id;

	public String name; // '红包名称',
	public String type; // '红包类型 2 充值 3 投资',
	public Date time; // '创建时间',
	public Date useTime; // 使用时间
	public long validityTime;// '有效期',
	public long validateUnit;// '有效期单位 -1年 0月 1日',
	public double money; // '红包金额',
	public long sendType; // 获得方式 0自动 1手动,
	public long status; // '状态 0 有效 1已使用 -1已过期'
	public long userId; // '用户ID' ,
	public String userName; // '用户名称',
	public long investId; // 投资ID
	public double valid_money; // 起投点' ,
	public String sign; // 加密ID
	public int isNewUser; // 新手标是否可用
	public int bidPeriod; // 最低标的期限限制
	public int bidPeriodUnit; // 最低标的期限限制
	public String remark; // 投资红包，首投与再投、秒标与普通
	public int couponType;// 1红包 2加息劵

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
		t_red_packages_history trpt = null;

		try {
			trpt = t_red_packages_history.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时：" + e.getMessage());
			this._id = -1;
			return;
		}

		this._id = id;

		this.name = trpt.name; // '红包类型',
		this.type = trpt.type; // '红包类型',
		this.time = trpt.time; // '创建时间',
		this.useTime = trpt.use_time; // 使用时间
		this.validityTime = trpt.validity_time; // '有效期',
		this.validateUnit = trpt.validate_unit; // '有效期单位 -1年 0月 1日',
		this.money = trpt.money; // '红包金额',
		this.sendType = trpt.send_type;
		this.userId = trpt.user_id;
		this.userName = trpt.user_name;
		this.status = trpt.status; // '状态
		this.valid_money = trpt.valid_money; // 起投点
		this.isNewUser = trpt.is_new_user;
		this.bidPeriod = trpt.bid_period;
		this.bidPeriodUnit = trpt.bid_period_unit;
		this.remark = trpt.remark;
		this.couponType = trpt.coupon_type;
	}

	/**
	 * 查询统计红包
	 * 
	 * @return
	 */
	public static List<t_red_packages_history_count> showList() {
		String sql = "SELECT id,type, NAME, ( SELECT count(a.id) FROM t_red_packages_history a WHERE a.type = b.type ) AS totalCount,(SELECT count(a.id) FROM t_red_packages_history a WHERE a.type = b.type and a.status = 1) AS userCount, SUM(money) AS totalAmount FROM t_red_packages_history b GROUP BY name ";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql, t_red_packages_history_count.class);
		List<t_red_packages_history_count> list = query.getResultList();
		return list;
	}

	/**
	 * 
	 * @param currPage
	 * @param pageSize
	 * @param keyWord
	 * @return
	 */
	public static PageBean<t_red_packages_history> showListByType(int currPage, int pageSize, String keyWord) {
		if (null == keyWord || StringUtils.isBlank(keyWord)) {
			return null;
		}

		PageBean<t_red_packages_history> pageBean = new PageBean<t_red_packages_history>();

		if (0 == currPage) {
			currPage = 1;
			pageBean.currPage = 1;
		} else {
			pageBean.currPage = currPage;
		}
		if (0 == pageSize) {
			pageSize = Constants.PAGE_SIZE;
			pageBean.pageSize = Constants.PAGE_SIZE;
		} else {
			pageBean.pageSize = pageSize;
		}

		String sql = "select * from t_red_packages_history where type = ? group by id desc";
		String sqlCount = "select * from t_red_packages_history where type = ? group by id desc";

		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql, t_red_packages_history.class);
		query.setParameter(1, keyWord);
		query.setFirstResult((currPage - 1) * pageSize);
		query.setMaxResults(pageSize);

		pageBean.page = query.getResultList();

		Query queryCount = em.createNativeQuery(sqlCount, t_red_packages_history.class);
		queryCount.setParameter(1, keyWord);
		List list = queryCount.getResultList();
		if (null == list) {
			pageBean.totalCount = 0;
		} else {
			pageBean.totalCount = list.size();
		}

		return pageBean;
	}

	/**
	 * 手动发放红包
	 * 
	 * @param usernames
	 * @param amount
	 * @param typeName
	 * @param validityMoney
	 * @param validityTime
	 * @param validateUnit
	 * @param noticeMessage
	 * @param noticeBox
	 * @param noticeEmail
	 */
	public static void saveRedPacketsUsers(String usernames, double amount, String typeName, int validityMoney,
			String validityTime, String validateUnit, String noticeMessage, String noticeEmail, String noticeBox, int bid_period,
			int bid_period_unit, int is_new_user, int balance_s,int balance_b, ErrorInfo error) {
		String[] username = null;
		if (StringUtils.isBlank(usernames)) {
			error.code = -1;
			error.msg = "发放失败!";
			return;
		}

		if ("#全部活跃用户#".equals(usernames.trim())) {
			for (int i = 1; i <= 9999999; i++) {
				PageBean<v_user_info_v1> page = User.queryActiveUser(i + "", "50", error);
				if (page.page != null && page.page.size() > 0) {
					for (v_user_info_v1 user_info : page.page) {
						addRedPackage(user_info.name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage,
								noticeEmail, noticeBox, bid_period, bid_period_unit, is_new_user, error);
					}
				} else {
					break;
				}
			}
		} else if ("#未实名认证用户#".equals(usernames.trim())) {
			List<t_users> us = User.findNoAuthUser();
			if (us != null && us.size() > 0) {
				for (t_users u : us) {
					addRedPackage(u.name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else if ("#未添加银行卡用户#".equals(usernames.trim())) {
			List<Map<String, Object>> us = User.findNoBankUser();
			if (us != null && us.size() > 0) {
				for (Map<String, Object> u : us) {
					String name = (String) u.get("name");
					addRedPackage(name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else if ("#未投资用户#".equals(usernames.trim())) {
			List<Map<String, Object>> us = User.findNoInvestUser();
			if (us != null && us.size() > 0) {
				for (Map<String, Object> u : us) {
					String name = (String) u.get("name");
					addRedPackage(name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else if ("#历史有投资过的用户#".equals(usernames.trim())) {
			List<Map<String, Object>> us = User.findInvestUser();
			if (us != null && us.size() > 0) {
				for (Map<String, Object> u : us) {
					String name = (String) u.get("name");
					addRedPackage(name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else if ("#半年内有投资过的用户#".equals(usernames.trim())) {
			List<Map<String, Object>> us = User.findhalfYearInvestUser();
			if (us != null && us.size() > 0) {
				for (Map<String, Object> u : us) {
					String name = (String) u.get("name");
					addRedPackage(name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else if ("#按账户余额范围选择用户#".equals(usernames.trim())) {
			List<Map<String, Object>> us = User.findByBalanceUser(balance_s, balance_b);
			if (us != null && us.size() > 0) {
				for (Map<String, Object> u : us) {
					String name = (String) u.get("name");
					addRedPackage(name, amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage, noticeEmail,
							noticeBox, bid_period, bid_period_unit, is_new_user, error);
				}
			}
		} else {
			username = usernames.split(",");
			for (int i = 0; i < username.length; i++) {
				addRedPackage(username[i], amount, typeName, validityMoney, validityTime, validateUnit, noticeMessage,
						noticeEmail, noticeBox, bid_period, bid_period_unit, is_new_user, error);
			}
		}

		/*
		 * username = usernames.split(","); t_users user = null; t_red_packages_history
		 * history = null; for (int i = 0; i < username.length; i++) { user =
		 * User.queryUserByUserName(username[i], error); if(null == user){
		 * Logger.error("手动发红包过程中第个发放"+(i+1)+"失败!"); continue; } history = new
		 * t_red_packages_history(); history.name = typeName; // '红包名称', history.type =
		 * Constants.RED_PACKAGE_TYPE_HAND; // '红包类型 2 充值 3 投资', history.time = new
		 * Date(); // '创建时间', history.validity_time = Long.valueOf(validityTime);//
		 * '有效期', history.validate_unit = Long.valueOf(validateUnit);// '有效期单位 -1年 0月
		 * 1日', history.money = amount; // '红包金额', history.send_type =
		 * Constants.RED_PACKAGE_SENDTYPE_HAND;; //获得方式 0自动 1手动, history.status =
		 * Constants.RED_PACKAGE_STATUS_UNUSED; //'状态 0 有效 1已使用 -1已过期' history.user_id =
		 * user.id; // '用户ID' , history.user_name =user.name; //'用户名称',
		 * history.notice_email = StringUtils.equals(noticeEmail, "false") ? false :
		 * true;// 通知方式邮件 history.notice_message = StringUtils.equals(noticeMessage,
		 * "false")? false : true;//'通知方式短信', history.notice_box =
		 * StringUtils.equals(noticeBox, "false") ? false : true;//'通知方式站内',
		 * history.invest_id = 0 ;//默认 已使用后此字段才会有值 history.valid_money = validityMoney;
		 * //事务不回滚 try { history.save(); } catch (Exception e) { e.printStackTrace();
		 * Logger.error("手动发红包过程中第个发放"+i+1+"失败!");
		 * Logger.error("错误信息如下："+e.getMessage()); continue; }
		 * 
		 * //根据通知方式通知用户 if(!StringUtils.isBlank(noticeEmail) &&
		 * StringUtils.equals("true", noticeEmail)){ senderEmail(user,history,"手动发放红包");
		 * } if(!StringUtils.isBlank(noticeMessage) && StringUtils.equals("true",
		 * noticeMessage)){ senderMessage(user,history,"手动发放红包"); }
		 * if(!StringUtils.isBlank(noticeBox) && StringUtils.equals("true", noticeBox)){
		 * senderBox(user,history,"手动发放红包"); }
		 * 
		 * }
		 */
	}

	private static void addRedPackage(String username, double amount, String typeName, int validityMoney, String validityTime,
			String validateUnit, String noticeMessage, String noticeEmail, String noticeBox, int bid_period, int bid_period_unit,
			int is_new_user, ErrorInfo error) {
		JPAUtil.transactionBegin();
		Logger.info(String.format("给用户【%s】发送%s元红包", username, amount));
		t_users user = User.queryUserByUserName(username, error);
		if (null == user) {
			Logger.error("手动发红包过程中用户名：【" + username + "】失败!");
			return;
		}
		t_red_packages_history history = new t_red_packages_history();
		history.name = typeName; // '红包名称',
		history.type = Constants.RED_PACKAGE_TYPE_HAND; // '红包类型 2 充值 3 投资',
		history.time = new Date(); // '创建时间',
		history.validity_time = Long.valueOf(validityTime);// '有效期',
		history.validate_unit = Long.valueOf(validateUnit);// '有效期单位 1 天 0-小时',
		history.money = amount; // '红包金额',
		history.send_type = Constants.RED_PACKAGE_SENDTYPE_HAND;
		; // 获得方式 0自动 1手动,
		history.status = Constants.RED_PACKAGE_STATUS_UNUSED; // '状态 0 有效 1已使用 -1已过期'
		history.user_id = user.id; // '用户ID' ,
		history.user_name = user.name; // '用户名称',
		history.notice_email = StringUtils.equals(noticeEmail, "false") ? false : true;// 通知方式邮件
		history.notice_message = StringUtils.equals(noticeMessage, "false") ? false : true;// '通知方式短信',
		history.notice_box = StringUtils.equals(noticeBox, "false") ? false : true;// '通知方式站内',
		history.invest_id = 0;// 默认 已使用后此字段才会有值
		history.valid_money = validityMoney;
		history.is_new_user = is_new_user;
		history.bid_period = bid_period;
		history.bid_period_unit = bid_period_unit;
		int couponFlag = Integer.parseInt(Session.current().get("couponFlag"));
		history.coupon_type = couponFlag;
		// 事务不回滚
		try {
			history.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("手动发红包过程中用户名：【" + username + "】失败!");
			Logger.error("错误信息如下：" + e.getMessage());
			return;
		}
		String desc = CouponTypeEnum.REDBAG.getName();
		if (CouponTypeEnum.REDBAG.getCode() == couponFlag) {
			desc = CouponTypeEnum.REDBAG.getName();
		} else {
			desc = CouponTypeEnum.INTEREST_RATE_COUPONS.getName();
		}
		// 根据通知方式通知用户
		if (!StringUtils.isBlank(noticeEmail) && StringUtils.equals("true", noticeEmail)) {
			senderEmail(user, history, "手动发放" + desc);
		}
		if (!StringUtils.isBlank(noticeMessage) && StringUtils.equals("true", noticeMessage)) {
			senderMessage(user, history, "手动发放" + desc);
		}
		if (!StringUtils.isBlank(noticeBox) && StringUtils.equals("true", noticeBox)) {
			senderBox(user, history, "手动发放" + desc);
		}
		JPAUtil.transactionCommit();
	}

	/**
	 * 手动发红包手机短息
	 * 
	 * @param user
	 * @param history
	 * @param string
	 */
	private static void senderMessage(t_users user, t_red_packages_history history, String desc) {
		try {
			BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Logger.info("******************发送" + desc + "短信开始*userName:%s*********************", user.name);
			TemplateSms sms = new TemplateSms();// 发送短信
			if (Constants.COUPON_TYPE_RED_PACKAGE == history.coupon_type) {
				sms.id = Templets.M_RED_PACKETS;// 发放注册红包;
			} else if (Constants.COUPON_TYPE_RATE == history.coupon_type) {
				sms.id = Templets.M_RED_RATE;// 发放注册红包;
			}
			String smscontent = sms.content;
//			smscontent = smscontent.replace("date", sdf.format(new Date()));
			if (StringUtils.isNotBlank(user.name)) {
				smscontent = smscontent.replace("userName", user.name);
			} else {
				smscontent = smscontent.replace("userName", user.mobile);
			}

			smscontent = smscontent.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
			smscontent = smscontent.replace("redPackageName", history.name);
			smscontent = smscontent.replace("money", history.money + "");
			sms.addSmsTask(user.mobile, smscontent);
			Logger.info("******************发送" + desc + "短信结束*userName:%s*********************", user.name);
		} catch (Exception e) {
			Logger.error("短信发送失败!");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 手动发红包站内信
	 * 
	 * @param user
	 * @param history
	 * @param string
	 */
	private static void senderBox(t_users user, t_red_packages_history history, String desc) {
		try {
			BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 发送注册站内信
			Logger.info("******************发送" + desc + "站内信开始*userName:%s*********************", user.name);
			TemplateStation message = new TemplateStation();
			if (Constants.COUPON_TYPE_RED_PACKAGE == history.coupon_type) {
				message.id = Templets.M_RED_PACKETS;
			} else if (Constants.COUPON_TYPE_RATE == history.coupon_type) {
				message.id = Templets.M_RED_RATE;
			}
			String mcontent = message.content;
			mcontent = mcontent.replace("date", sdf.format(new Date()));
			if (StringUtils.isNotBlank(user.name)) {
				mcontent = mcontent.replace("userName", user.name);
			} else {
				mcontent = mcontent.replace("userName", user.mobile);
			}

			mcontent = mcontent.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
			mcontent = mcontent.replace("money", history.money + "");
			if (message.status) {
				ErrorInfo error = new ErrorInfo();
				StationLetter letter = new StationLetter();
				letter.senderSupervisorId = 1;
				letter.receiverUserId = user.id;
				letter.title = message.title;
				letter.content = mcontent;
				letter.sendToUserBySupervisor(error);
			}
			Logger.info("******************发送" + desc + "站内信结束*userName:%s*********************", user.name);
		} catch (Exception e) {
			Logger.error("站内信发送失败!");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 手动发放红包邮件通知
	 * 
	 * @param user
	 * @param history
	 * @param string
	 */
	private static void senderEmail(t_users user, t_red_packages_history history, String desc) {
		try {
			BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Logger.info("******************发送" + desc + "邮件开始*userName:%s*********************", user.name);
			TemplateEmail t_email = new TemplateEmail();
			// 获取邮件模板
			if (Constants.COUPON_TYPE_RED_PACKAGE == history.coupon_type) {
				t_email.id = Templets.E_RED_PACKETS;
			} else if (Constants.COUPON_TYPE_RATE == history.coupon_type) {
				t_email.id = Templets.E_RED_RATE;
			}
			String econtent = t_email.content;
			econtent = econtent.replace("date", sdf.format(new Date()));
			if (StringUtils.isNotBlank(user.name)) {
				econtent = econtent.replace("userName", user.name);
			} else {
				econtent = econtent.replace("userName", user.mobile);
			}

			econtent = econtent.replace(Constants.EMAIL_PLATFORM, backstageSet.platformName);
			econtent = econtent.replace("money", history.money + "");
			t_email.addEmailTask(user.email, t_email.title, econtent);

			Logger.info("******************发送" + desc + "邮件结束*userName:%s*********************", user.name);
		} catch (Exception e) {
			Logger.error("邮件发送失败!");
			e.printStackTrace();
			return;
		}

	}

	/**
	 * 检查红包是否过期
	 */
	public static void scanningRedPackageStatus() {
		// 查询未使用的红包
		EntityManager em = JPA.em();

		String sql = "select * from t_red_packages_history where status = 0 group by id desc";
		Query query = em.createNativeQuery(sql, t_red_packages_history.class);
		List<t_red_packages_history> list = query.getResultList();
		if (null != list && list.size() > 0) {
			t_red_packages_history history = null;
			try {
				for (int i = 0; i < list.size(); i++) {
					history = list.get(i);
					checkGQ(history);
				}
			} catch (Exception e) {
				e.printStackTrace();
				JPA.setRollbackOnly();
			}
		} else {
			Logger.info("**********没有未使用的红包**********");
		}
	}

	/**
	 * 根据用户ID 红包状态查询所有红包(红包,加息券)
	 * 
	 * @param userId
	 * @param status:使用状态:-99全部状态,0    有效,1已使用,-1已过期
	 * @param coupon_type:红包类型:        -1:全部类型,1 红包,2 加息券
	 * @param orderby:排序规则:1,按金额正序(默认)
	 */
	public static List<t_red_packages_history> showListByStatus(long userId, int status, int coupon_type, int orderby) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from t_red_packages_history where  user_id = ? ");
		if (status != -99) {
			sql.append(" and status =:status ");
		}
		if (coupon_type != -1) {
			sql.append(" and coupon_type =:coupon_type ");
		}
		if (orderby == 1) {
			sql.append(" order by money ");
		} else {
			sql.append(" order by money ");
		}

		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_history.class).setParameter(1, userId);
		if (status != -99) {
			query.setParameter("status", status);
		}
		if (coupon_type != -1) {
			query.setParameter("coupon_type", coupon_type);
		}

		List<t_red_packages_history> list = query.getResultList();

		return findByUser(list);
	}

	/**
	 * 发送红包
	 * 
	 * @param user
	 * @param redPackageType
	 * @param desc
	 */
	public static long sendRedPackage(User user, t_red_packages_type redPackageType, String desc) {
		ErrorInfo error = new ErrorInfo();
		t_red_packages_history history = new t_red_packages_history();
		history.name = redPackageType.typeName; // '红包名称',
		history.type = redPackageType.name; // '红包类型 2 充值 3 投资',
		history.type_id = redPackageType.id;
		history.coupon_type = redPackageType.coupon_type;
		String remark = "";
		if (Constants.RED_PACKAGE_TYPE_INVEST.equals(redPackageType.name)) {
			String rules = redPackageType.rules;
			if (StringUtils.isNotBlank(rules)) {
				try {
					JSONObject json = JSONObject.parseObject(rules);
					int isAll = json.getIntValue("all");
					int first = json.getIntValue("first");
					if (first == 0) {
						remark += "再投 ";
					} else {
						remark += "首投 ";
					}
					if (isAll == 0) {
						remark += " 普通";
					} else {
						remark += " 秒标";
					}

				} catch (Exception e) {
				}
			}
		}
		if (Constants.RED_PACKAGE_TYPE_INVESTS.equals(redPackageType.name)
				|| Constants.RED_PACKAGE_TYPE_CUSTOM.equals(redPackageType.name)) {
			String rules = redPackageType.rules;
			if (StringUtils.isNotBlank(rules)) {
				try {
					JSONObject json = JSONObject.parseObject(rules);
					String start = json.getString("start");
					String end = json.getString("end");
					remark = start + "--" + end;
				} catch (Exception e) {
				}
			}
		}
		history.is_new_user = redPackageType.is_new_user;
		history.bid_period = redPackageType.bid_period;
		history.bid_period_unit = redPackageType.bid_period_unit;
		history.remark = remark;
		history.time = new Date(); // '创建时间',
		history.validity_time = redPackageType.validity_time;// '有效期',
		history.validate_unit = redPackageType.validate_unit;// '有效期单位 0小时 1日',
		history.money = redPackageType.money; // '红包金额',
		history.send_type = Constants.RED_PACKAGE_SENDTYPE_AUTO;
		; // 获得方式 0自动 1手动,

		history.status = Constants.RED_PACKAGE_STATUS_UNUSED; // '状态 0 有效 1已使用 -1已过期'

		history.user_id = user.id; // '用户ID' ,
		history.user_name = user.name; // '用户名称',
		history.notice_email = redPackageType.notice_email;// 通知方式邮件
		history.notice_message = redPackageType.notice_message;// '通知方式短信',
		history.notice_box = redPackageType.notice_box;// '通知方式站内',
		history.invest_id = 0;// 默认 已使用后此字段才会有值
		history.valid_money = redPackageType.validity_money;// 默认 已使用后此字段才会有值
		try {
			history.save();

			// 添加交易记录 及时查询用户的可用余额、冻结资金、待收金额，避免缓存/视图引起的数据误差
			Map<String, Double> detail = DealDetail.queryUserFund(user.id, error);
			DealDetail dealDetail = new DealDetail(user.id, DealType.RED_RELEASESYS, history.money, DealType.RED_RELEASE,
					detail.get("user_amount"), detail.get("freeze"), detail.get("receive_amount"), desc);
			dealDetail.addDealDetail(error);

			/* 更新自己的防篡改 */
			DataSafety data = new DataSafety(); // 借款会员数据防篡改对象
			data.updateSignWithLock(user.id, error);
			if (error.code < 0) {
				error.code = -1;
				error.msg = "放篡改更新失败!";
				Logger.error("放篡改更新失败!");
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("APP注册发红包失败!失败原因为：" + e.getMessage());
			return 0;
		}
		// 用户通知方式
		userNotice(user, history, desc);

		return history.id;

	}

	/**
	 * 发送红包通知
	 * 
	 * @param user
	 * @param history
	 * @param desc
	 */
	public static void userNotice(User user, t_red_packages_history history, String desc) {
		// 邮件通知
		t_users tuser = t_users.findById(user.id);
		if (history.notice_box) {// 站内信
			senderBox(tuser, history, desc);
		}
		if (history.notice_email && StringUtils.isNotBlank(user.email)) {// 邮件
			senderEmail(tuser, history, desc);
		}
		if (history.notice_message) {// 短信
			senderMessage(tuser, history, desc);
		}
	}

	/**
	 * 充值专用
	 * 
	 * @param user
	 * @param redPackageType
	 * @param desc
	 * @return
	 */
	public static long sendRedPackageTo(User user, t_red_packages_type redPackageType, String desc) {

		t_red_packages_history history = new t_red_packages_history();
		history.name = redPackageType.typeName; // '红包名称',
		history.type = redPackageType.name; // '红包类型 2 充值 3 投资',
		history.time = new Date(); // '创建时间',
		history.validity_time = redPackageType.validity_time;// '有效期',
		history.validate_unit = redPackageType.validate_unit;// '有效期单位 -1年 0月 1日',
		history.money = redPackageType.money; // '红包金额',
		history.send_type = Constants.RED_PACKAGE_SENDTYPE_AUTO;
		; // 获得方式 0自动 1手动,

		// 充值红包需要调用宝付接口，所以增加-2状态，
		history.status = Constants.RED_PACKAGE_STATUS_UN; // '状态 0 有效 1已使用 -1已过期'

		history.user_id = user.id; // '用户ID' ,
		history.user_name = user.name; // '用户名称',
		history.notice_email = redPackageType.notice_email;// 通知方式邮件
		history.notice_message = redPackageType.notice_message;// '通知方式短信',
		history.notice_box = redPackageType.notice_box;// '通知方式站内',
		history.invest_id = 0;// 默认 已使用后此字段才会有值
		history.valid_money = redPackageType.validity_money;// 默认 已使用后此字段才会有值
		try {

			history.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("APP注册发红包失败!失败原因为：" + e.getMessage());
			return 0;
		}
		return history.id;
	}

	/**
	 * 根据ID修改红包状态
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	public static int updateStatus(String id, String status, String investId, ErrorInfo error) {
		error.clear();
		int rows = -1;
		if (StringUtils.isBlank(id) || StringUtils.isBlank(status)) {
			error.code = -1;
			error.msg = "操作失败!";
			return rows;
		}

		String sql = "update t_red_packages_history set use_time = ?,status = ?, invest_id= ? where id = ?";
		try {
			EntityManager em = JPA.em();

			Query query = em.createNativeQuery(sql).setParameter(1, new Date()).setParameter(2, status).setParameter(3, investId)
					.setParameter(4, id);
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改红包状态时：" + e.getMessage());
			error.msg = "操作失败!";
			return -1;
		}
		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "操作失败!";

			return error.code;
		}
		return rows;
	}

	/**
	 * 积分兑换红包
	 */
	public static void scroeExchangeRedPack(User user, t_red_packages_type redPackageType, String desc, ErrorInfo error) {
		error.clear();
		t_red_packages_history history = new t_red_packages_history();
		history.name = redPackageType.typeName; // '红包名称',
		history.type = redPackageType.name; // '红包类型 2 充值 3 投资',
		history.time = new Date(); // '创建时间',
		history.validity_time = redPackageType.validity_time;// '有效期',
		history.validate_unit = redPackageType.validate_unit;// '有效期单位 -1年 0月 1日',
		history.money = redPackageType.money; // '红包金额',
		history.send_type = Constants.RED_PACKAGE_SENDTYPE_AUTO;
		; // 获得方式 0自动 1手动,
		history.status = Constants.RED_PACKAGE_STATUS_UNUSED; // '状态 0 有效 1已使用 -1已过期'
		history.user_id = user.id; // '用户ID' ,
		history.user_name = user.name; // '用户名称',
		history.notice_email = redPackageType.notice_email;// 通知方式邮件
		history.notice_message = redPackageType.notice_message;// '通知方式短信',
		history.notice_box = redPackageType.notice_box;// '通知方式站内',
		history.invest_id = 0;// 默认 已使用后此字段才会有值
		history.coupon_type = Constants.COUPON_TYPE_RED_PACKAGE;
		try {
			history.save();

			// 添加交易记录 及时查询用户的可用余额、冻结资金、待收金额，避免缓存/视图引起的数据误差
			Map<String, Double> detail = DealDetail.queryUserFund(user.id, error);
			DealDetail dealDetail = new DealDetail(user.id, DealType.RED_RELEASESYS, history.money, DealType.RED_RELEASE,
					detail.get("user_amount"), detail.get("freeze"), detail.get("receive_amount"), "APP端注册发放红包");
			dealDetail.addDealDetail(error);

			/* 更新自己的防篡改 */
			DataSafety data = new DataSafety(); // 借款会员数据防篡改对象
			data.updateSignWithLock(user.id, error);
			if (error.code < 0) {
				error.code = -1;
				error.msg = "放篡改更新失败!";
				Logger.error("放篡改更新失败!");
				return;
			}
		} catch (Exception e) {
			error.code = -1;
			error.msg = "积分兑换红包失败!";
			e.printStackTrace();
			Logger.error("APP注册发红包失败!失败原因为：" + e.getMessage());
			return;
		}

		// 邮件通知
		t_users tuser = t_users.findById(user.id);
		if (history.notice_box) {// 站内信
			senderBox(tuser, history, desc);
		}
		if (history.notice_email) {// 邮件
			senderEmail(tuser, history, desc);
		}
		if (history.notice_message) {// 短信
			senderMessage(tuser, history, desc);
		}
	}

	/**
	 * 根据投资记录ID回滚红包使用状态 从已使用变为未使用
	 * 
	 * @param investId
	 */
	public static void rollBackRedPack(long userId, long investId) {

		// b不存在红包直接返回
		if (investId <= 0 || userId <= 0) {
			return;
		}
		String sqlSelect = "select * from t_red_packages_history where user_id = ? and use_time is not NULL and status = ? and invest_id = ?";

		String sqlUpdate = "update t_red_packages_history set use_time = null,status = 0, invest_id= 0 where id = ?";
		try {
			EntityManager em = JPA.em();
			Query query = em.createNativeQuery(sqlSelect, t_red_packages_history.class).setParameter(1, userId)
					.setParameter(2, Constants.RED_PACKAGE_STATUS_USING).setParameter(3, investId);
			List<t_red_packages_history> list = query.getResultList();
			if (null != list && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					if (checkGQ(list.get(i))) {// 检查红包是否过期 过期直接改为过期
						query = em.createNativeQuery(sqlUpdate, t_red_packages_history.class).setParameter(1, list.get(0).id);
						int row = query.executeUpdate();
						if (row == 0) {
							Logger.error("更新红包状态失败!");
						} else {
							Logger.info("更新红包状态成功!");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改红包状态时：" + e.getMessage());
		}

	}

	/**
	 * 根据投资记录ID,用户ID查询红包 从已使用变为未使用
	 * 
	 * @param investId
	 */
	public static t_red_packages_history queryRedByUserIdAndInvestId(long userId, long investId) {

		// b不存在红包直接返回
		if (investId <= 0 || userId <= 0) {
			return null;
		}
		try {
			// 只查询红包，不查询加息劵。
			return t_red_packages_history
					.find(" user_id = ? and use_time is not NULL and status = ? and invest_id = ? and coupon_type = 1 ", userId,
							Constants.RED_PACKAGE_STATUS_USING, investId)
					.first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询失败：" + e.getMessage());
			return null;
		}

	}

	/**
	 * 放款更改红包状态
	 * 
	 * @param userId
	 * @param investId
	 */
	public static void updateRedPackagesHistory(long userId, long investId) {
		String sql = "UPDATE t_red_packages_history set status = 1 where user_id = ? and use_time is not NULL and status = 2 and invest_id = ?";
		EntityManager em = JPA.em();

		try {
			Query query = em.createNativeQuery(sql).setParameter(1, userId).setParameter(2, investId);
			query.executeUpdate();
		} catch (Exception e) {
			Logger.info("修改红包状态：" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 检查红包是否过期
	 * 
	 * @param history
	 */
	public static boolean checkGQ(t_red_packages_history history) {
		int year = 0;
		int monty = 1;
		int day = 1;
		int h = 0;
		int m = 0;
		int s = 0;
		boolean result = true;

		year = Integer.valueOf(history.time.toString().substring(0, 4));
		monty = Integer.valueOf(history.time.toString().substring(5, 7));
		day = Integer.valueOf(history.time.toString().substring(8, 10));
		h = Integer.valueOf(history.time.toString().substring(11, 13));
		m = Integer.valueOf(history.time.toString().substring(14, 16));
		s = Integer.valueOf(history.time.toString().substring(17, 19));

		GregorianCalendar mortgage = new GregorianCalendar(year, monty, day, h, m, s);
		int key = 0;
		Date d = new Date();
		;
		if (NumberUtils.isNumber(history.validate_unit + "")) {
			key = Integer.valueOf(history.validate_unit + "");
		}

		switch (key) {
		case Constants.YEAR:
			mortgage.add(Calendar.YEAR, Integer.valueOf(history.validity_time + ""));
			mortgage.add(Calendar.MONTH, -1);

			if (mortgage.getTime().before(d)) {//
//					history.status = Constants.RED_PACKAGE_STATUS_OVERDUE;
//					history.time = new Date();//修改时间
//					history.save();
				updateStatusExpired(history.id, Constants.RED_PACKAGE_STATUS_OVERDUE);
				result = false;
			}
			break;
		case 0:// 小时
			mortgage.add(Calendar.HOUR_OF_DAY, Integer.valueOf(history.validity_time + ""));
			mortgage.add(Calendar.MONTH, -1);
			if (mortgage.getTime().before(d)) {//
//					history.status = Constants.RED_PACKAGE_STATUS_OVERDUE;
//					history.time = new Date();//修改时间
//					history.save();
				updateStatusExpired(history.id, Constants.RED_PACKAGE_STATUS_OVERDUE);
				result = false;
			}
			break;
		case Constants.DAY:
			float days = 0.00f;
			long start = history.time.getTime();
			long end = d.getTime();
			// 一天的毫秒数1000 * 60 * 60 * 24=86400000
			days = (float) ((end - start) / 86400000.00);
			if (days > history.validity_time) {
//					history.status = Constants.RED_PACKAGE_STATUS_OVERDUE;
//					history.save();
				updateStatusExpired(history.id, Constants.RED_PACKAGE_STATUS_OVERDUE);
				result = false;
			}

			/*
			 * mortgage.add(Calendar.DAY_OF_MONTH,
			 * Integer.valueOf(history.validity_time+"")); mortgage.add(Calendar.MONTH, -1);
			 * if (mortgage.getTime().before(d)) {// history.status =
			 * Constants.RED_PACKAGE_STATUS_OVERDUE; // history.time = new Date();//修改时间
			 * history.save(); result = false; }
			 */
			break;
		}
		return result;
	}

	public static PageBean<Map<String, Object>> getPage(int type, int dateType, String start, String end, String keyword,
			int status, int pageSize, int currPage, int orderIndex, int orderStatus, String useTimeStart, String useTimeEnd,
			int isExport) {
		pageSize = pageSize == 0 ? 10 : pageSize;
		currPage = currPage == 0 ? 1 : currPage;
		StringBuffer sql = new StringBuffer();
		sql.append(" FROM t_red_packages_history h ");
		sql.append(" left join t_invests i on h.invest_id = i.id ");
		sql.append(" 	left join t_bids b on b.id = i.bid_id ");
		sql.append(" 	left join( ");
		sql.append(
				" 	select invest_id,sum(receive_increase_interest) receive_increase_interest from t_bill_invests group by invest_id ");
		sql.append(" 	)increase on increase.invest_id=i.id ");
		sql.append("  where  1=1 ");

		List<Object> p = new ArrayList<Object>();
		if (type > -1) {
			sql.append(" AND h.type = ?");
			p.add(type);
		}
		if (dateType == 0) {// 发放时间
			if (StringUtils.isNotBlank(start)) {
				sql.append(" AND h.time >= ?");
				p.add(DateUtil.strDateToStartDate(start));
			}
			if (StringUtils.isNotBlank(end)) {
				sql.append(" AND h.time <= ?");
				p.add(DateUtil.strDateToEndDate(end));
			}
		} else {
			if (StringUtils.isNotBlank(start)) {
				sql.append(" and case");
				sql.append(" when validate_unit=0 then DATE_ADD(h.time,INTERVAL validity_time HOUR) >=?");
				sql.append(" when validate_unit=1 then DATE_ADD(h.time,INTERVAL validity_time DAY) >=? end");
				p.add(DateUtil.strDateToStartDate(start));
				p.add(DateUtil.strDateToStartDate(start));
				p.add(DateUtil.strDateToStartDate(start));
			}
			if (StringUtils.isNotBlank(end)) {
				sql.append(" and case");
				sql.append(" when validate_unit=0 then DATE_ADD(h.time,INTERVAL validity_time HOUR) <=?");
				sql.append(" when validate_unit=1 then DATE_ADD(h.time,INTERVAL validity_time DAY) <=? end");
				p.add(DateUtil.strDateToEndDate(end));
				p.add(DateUtil.strDateToEndDate(end));
				p.add(DateUtil.strDateToEndDate(end));
			}
		}
		if (status > -3) {
			sql.append(" and h.status = ?");
			p.add(status);
		}
		if (StringUtils.isNotBlank(keyword)) {
			sql.append(" and (instr(h.user_name,?) > 0 or instr(h.name,?) > 0 or instr(b.title,?) > 0)");
			p.add(keyword);
			p.add(keyword);
			p.add(keyword);
		}

		String couponFlag = Session.current().get("couponFlag");
		if (couponFlag != null && couponFlag != "") {
			sql.append(" and h.coupon_type=? ");
			p.add(Integer.parseInt(couponFlag));

		}

		if (StringUtils.isNotBlank(useTimeStart)) {
			sql.append(" and h.use_time >= ? ");
			p.add(DateUtil.strDateToStartDate(useTimeStart));
		}

		if (StringUtils.isNotBlank(useTimeEnd)) {
			sql.append(" and h.use_time <= ? ");
			p.add(DateUtil.strDateToEndDate(useTimeEnd));
		}

		String listSql = "SELECT h.type,h.name,h.user_name as userName,h.time,b.title,h.money,h.status,h.remark,h.use_time as useTime,"
				+ " case validate_unit " + " WHEN 0 then DATE_ADD(h.time,INTERVAL validity_time HOUR) "
				+ " WHEN 1 then DATE_ADD(h.time,INTERVAL validity_time DAY) end as validate, "
				+ " ifnull(i.amount,0) amount,increase.receive_increase_interest".concat(sql.toString());

		if (orderIndex == 0) {
			String order_sql = Constants.RED_PACKAGE_ORDER_CONDITION[orderIndex];
			listSql += " " + String.format(order_sql, "h.");
		} else {
			String order_sql = Constants.RED_PACKAGE_ORDER_CONDITION[orderIndex];
			listSql += " " + String.format(order_sql, "h.");
			if (orderStatus == 1) {
				listSql += " ASC";
			} else {
				listSql += " DESC";
			}
		}
		if (isExport != Constants.IS_EXPORT) {
			listSql += " limit ?,?";
		}

		String cntSql = "SELECT count(*) as count".concat(sql.toString());

		String sumSql = "select sum(money) as sumAmount".concat(sql.toString());

		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, p.toArray());
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}

		double sumAmount = 0;
		List<Map<String, Object>> amountMap = JPAUtil.getList(new ErrorInfo(), sumSql, p.toArray());
		if (amountMap != null && amountMap.get(0) != null && amountMap.get(0).get("sumAmount") != null) {
			sumAmount = ((Double) amountMap.get(0).get("sumAmount")).doubleValue();
		}

		if (isExport != Constants.IS_EXPORT) {
			p.add((currPage - 1) * pageSize);
			p.add(pageSize);
			System.out.println(listSql + ";" + ((currPage - 1) * pageSize) + ";" + pageSize);
		}
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, p.toArray());

		Map<String, Object> conditionMap = new HashMap<String, Object>();

		conditionMap.put("type", type);
		conditionMap.put("start", start);
		conditionMap.put("end", end);
		conditionMap.put("dateType", dateType);
		conditionMap.put("keyword", keyword);
		conditionMap.put("status", status);
		conditionMap.put("orderIndex", orderIndex);
		conditionMap.put("orderStatus", orderStatus);
		conditionMap.put("sumAmount", sumAmount);
		conditionMap.put("useTimeStart", useTimeStart);
		conditionMap.put("useTimeEnd", useTimeEnd);

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
	 * @creationDate 2017年5月9日
	 * @description 获取用户可用红包
	 * @param error
	 * @param userId 用户ID
	 * @param bid    标的ID
	 * @return
	 */
	public static List<t_red_packages_history> findByUser(ErrorInfo error, long userId, long bid) {
		t_bids bids = t_bids.findById(bid);
		if (bids == null) {
			error.msg = "对不起！无效的标！";
			error.code = -2;
		}
		boolean is_user_new = bids.is_only_new_user;
		int period_unit = bids.period_unit;
		int period = bids.period;

		String sql = "select * from t_red_packages_history where user_id = ? and status = 0 and case "
				+ "when bid_period>0 then bid_period_unit = ? and bid_period<=? " + "when bid_period=0 then bid_period =0 end";
		if (is_user_new) {
			sql += " and is_new_user = ? ";
		}
		sql += "  order by money desc";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString(), t_red_packages_history.class);
		query.setParameter(1, userId);
		query.setParameter(2, period_unit);
		query.setParameter(3, period);
		if (is_user_new) {
			query.setParameter(4, is_user_new);
		}

		return findByUser(query.getResultList());
	}

	public static List<t_red_packages_history> findByUser(List<t_red_packages_history> reds) {
		if (reds != null && reds.size() > 0) {
			for (t_red_packages_history red : reds) {
				long validity_time = red.validity_time;
				long validate_unit = red.validate_unit;
				Date time = red.time;
				Date end = new Date();
				if (validate_unit == 0) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(time);
					calendar.add(Calendar.HOUR_OF_DAY, (int) validity_time);
					end = calendar.getTime();
				} else {
					end = DateUtil.dateAddDay(time, (int) validity_time);
				}
				long count = DateUtil.diffDays(time, end);
				long days = DateUtil.diffDays(time, new Date());

				red.days = (count - days) < 0 ? 0 : (count - days);
				red.endDate = end;
				if (Constants.RED_PACKAGE_TYPE_CUSTOM.equals(red.type)) {
					red.validate_unit = 1;
				}
			}
		}
		return reds;
	}

	public static RedPackageHistory findBySign(String redId) {
//		long redId =  Security.checkSign(sign, Constants.RID_ID_SIGN, Constants.VALID_TIME, new ErrorInfo());
		try {
			long id = Long.parseLong(redId);
			t_red_packages_history red = t_red_packages_history.findById(id);
			if (red == null) {
				return null;
			}
			RedPackageHistory pack = new RedPackageHistory();
			pack.id = red.id;
			return pack;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void sendAuthRed(User user) {
		// 实名认证发红包
		String redTypeName = Constants.RED_PACKAGE_TYPE_AUTH;// 实名认证

		long status = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;// 启用状态

		List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
		if (null != reds && reds.size() > 0) {
			for (t_red_packages_type redPackageType : reds) {
				String desc = "";
				if (redPackageType.coupon_type == CouponTypeEnum.REDBAG.getCode()) {
					desc = "实名认证发放红包";
				} else {
					desc = "实名认证发放加息券";
				}
				RedPackageHistory.sendRedPackage(user, redPackageType, desc);
			}
			Logger.error("实名认证发放优惠券短信通知成功");
		}
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年6月14日
	 * @description 用户是否领取生日红包
	 * @param redId
	 * @param userId
	 * @return
	 */
	public static boolean isExist(long redId, long userId) {
		String sql = "select count(1) as count from t_red_packages_history where type_id = ? and user_id = ? and time >=? and time<?";
		Date date = new Date();
		String start = DateUtil.getDate(date, "yyyy-MM-dd", 0);
		String end = DateUtil.getDate(date, "yyyy-MM-dd", 1);

		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), sql, redId, userId, start, end);
		int count = 0;
		if (countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null) {
			count = ((BigInteger) countMap.get(0).get("count")).intValue();
		}
		return count == 0 ? true : false;
	}

	/**
	 * 根据ID修改红包为失效状态
	 * 
	 * @param id
	 * @param status
	 * @return
	 */
	public static int updateStatusExpired(Long id, long status) {
		int rows = -1;
		String sql = "update t_red_packages_history set status = ? where id = ? and status = 0";
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql).setParameter(1, status).setParameter(2, id);
		rows = query.executeUpdate();

		return rows;
	}

	/**
	 * 通过flag 存储对应优惠券的标识 和 名称 方便前台页面 显示 相关信息
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param couponFlag 1 红包券 2 加息券
	 * @author: zj
	 */
	public static Integer storeCouponInfo(Integer couponFlag) {
		if (null == couponFlag) {
			couponFlag = Integer.parseInt(Session.current().get("couponFlag"));
		}
		if (CouponTypeEnum.REDBAG.getCode() == couponFlag) {
			Session.current().put("couponName", CouponTypeEnum.REDBAG.getName());
			Session.current().put("couponFlag", CouponTypeEnum.REDBAG.getCode());
			couponName = CouponTypeEnum.REDBAG.getName();
		}
		if (CouponTypeEnum.INTEREST_RATE_COUPONS.getCode() == couponFlag) {
			Session.current().put("couponName", CouponTypeEnum.INTEREST_RATE_COUPONS.getName());
			Session.current().put("couponFlag", CouponTypeEnum.INTEREST_RATE_COUPONS.getCode());
			couponName = CouponTypeEnum.INTEREST_RATE_COUPONS.getName();
		}
		return couponFlag;
	}
}
