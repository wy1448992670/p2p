package business;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;

import constants.Constants;
import play.Logger;
import play.db.jpa.JPA;
import models.t_statistic_index_echart_data;
import reports.StatisticInvest;
import reports.StatisticRecharge;
import sun.print.resources.serviceui;
import utils.ECharts;
import utils.ErrorInfo;

/***
 * 
 * <Description functions in a word> ECharts折线报表数据获取 <Detail description>
 * 
 * @author ChenZhipeng
 * @version [Version NO, 2015年8月18日]
 * @see [Related classes/methods]
 * @since [product/module version]
 */
public class EChartsData implements Serializable {

	/***
	 * <Description functions in a word> 获取相关会员数目Echarts <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param error
	 * @param type
	 *            查询类型 1、昨日 2、最近7天 3、最近30天
	 * @return [Parameters description]
	 * @return ECharts [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static ECharts getMembersCount(ErrorInfo error, int type) {
		error.clear();
		List<Integer> financiaCount = null; // 新增理财会员
		List<Integer> RegisterCount = null; // 新增注册会员
		String sqlFinancial = "SELECT new_financial_members_count FROM t_statistic_index_echart_data WHERE type = ?"
				+ " ORDER BY id";
		String sqlRegister = "SELECT new_register_members_count FROM t_statistic_index_echart_data WHERE type = ?"
				+ " ORDER BY id";
		try {
			financiaCount = t_statistic_index_echart_data.find(sqlFinancial,
					type).fetch(); // 获取新增理财会员人数
			RegisterCount = t_statistic_index_echart_data.find(sqlRegister,
					type).fetch(); // 获取新增注册会员人数
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("获取Echarts数据时:" + e.getMessage());
			return null;
		}
		String[] axis = EChartsData.getAxisDate(type, "MM月dd日"); // 昨日时间点
		String[] legend = { "financia", "register" };
		Object[] financiaObj = financiaCount.toArray();
		Object[] registerObj = RegisterCount.toArray();
		String[] financiaArray = new String[financiaObj.length];
		String[] registerArray = new String[registerObj.length];
		for (int i = 0; i < financiaObj.length; i++) {
			financiaArray[i] = financiaObj[i].toString();
			registerArray[i] = registerObj[i].toString();
		}

		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("financia", financiaArray);
		map.put("register", registerArray);

		ECharts charts = new ECharts(axis, legend, map);
		return charts;
	}

	/***
	 * <Description functions in a word> 获取相关金额数目Echarts <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param error
	 * @param type
	 *            查询类型 1、昨日2、最近7天 3、最近30天
	 * @return [Parameters description]
	 * @return ECharts [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static ECharts getMoneyNumber(ErrorInfo error, int type) {
		error.clear();
		List<Double> investMoney = null; // 投资金额
		List<Double> rechargeMoney = null; // 充值金额

		EntityManager em = JPA.em();

		String sqlInvest = "SELECT invest_money FROM t_statistic_index_echart_data WHERE type = ?"
				+ " ORDER BY id";
		String sqlRecharger = "SELECT recharge_money FROM t_statistic_index_echart_data WHERE type = ?"
				+ " ORDER BY id";
		try {
			investMoney = em.createNativeQuery(sqlInvest).setParameter(1, type)
					.getResultList(); // 获取新增理财会员人数
			rechargeMoney = em.createNativeQuery(sqlRecharger)
					.setParameter(1, type).getResultList(); // 获取新增注册会员人数
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("获取Echarts数据时:" + e.getMessage());
			return null;
		}

		String[] axis = EChartsData.getAxisDate(type, "MM月dd日"); // 昨日时间点
		String[] legend = { "invest", "recharger" };
		Object[] investObj = investMoney.toArray();
		Object[] rechargeObj = rechargeMoney.toArray();
		String[] investArray = new String[investObj.length];
		String[] rechargeArray = new String[rechargeObj.length];
		for (int i = 0; i < investObj.length; i++) {
			investArray[i] = investObj[i].toString();
			rechargeArray[i] = rechargeObj[i].toString();
		}

		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put("invest", investArray);
		map.put("recharger", rechargeArray);

		ECharts charts = new ECharts(axis, legend, map);
		return charts;
	}

	/**
	 * <Description functions in a word> 获取相应时间数组 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param type
	 * @return [Parameters description]
	 * @return String[] [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private static String[] getAxisDate(int type, String formatType) {
		String[] axis = null;
		SimpleDateFormat format = new SimpleDateFormat(formatType);
		switch (type) {
		case Constants.YESTERDAY:
			axis = new String[] { "2:00", "4:00", "6:00", "8:00", "10:00",
					"12:00", "14:00", "16:00", "18:00", "20:00", "22:00",
					"00:00" };
			break;
		case Constants.LAST_7_DAYS:
			List<String> listA = new ArrayList<String>();
			axis = new String[7];
			for (int i = 0, j = -1; i < 7; i++, j--) {
				Calendar c = Calendar.getInstance();
				String historyDate = format.format(EChartsData
						.dateFactory(c, j).getTime());
				listA.add(historyDate);
			}
			Collections.reverse(listA);
			for (int i = 0; i < listA.size(); i++) {
				axis[i] = listA.get(i);
			}
			break;
		case Constants.LAST_30_DAYS:
			List<String> listB = new ArrayList<String>();
			axis = new String[10];
			int j = -1;
			for (int i = 0; i < 10; i++) {
				Calendar c = Calendar.getInstance();
				String historyDate = format.format(EChartsData
						.dateFactory(c, j).getTime());
				listB.add(historyDate);
				j = j - 3;
			}
			Collections.reverse(listB);
			for (int i = 0; i < listB.size(); i++) {
				axis[i] = listB.get(i);
			}
			break;
		default:
			break;
		}
		return axis;
	}

	/***
	 * <Description functions in a word> ECharts表 数据更新 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param error
	 *            [Parameters description]
	 * @return void [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static void saveOrUpdataEchartsData(ErrorInfo error) {
		error.clear();
		int rechargeCount = 0; // 新增注册会员数目
		int financialCount = 0; // 新增理财会员数目
		double RechargeMoney = 0; // 充值金额
		double investMoney = 0; // 理财金额
		for (int i = 1; i < 4; i++) { // 最外层for循环用于区分时间类型
			String[] date = EChartsData.getAxisDate(i, "MM月dd日"); // 时间点数组
			if (i == Constants.YESTERDAY) {
				for (int j = 0, timeType = 2; j < date.length; j++, timeType += 2) {
					rechargeCount = User
							.queryUserCount(error, null, date[j], i); // 昨日注册数据
					financialCount = User.queryFinancialUserCount(error, null,
							date[j], i);
					RechargeMoney = StatisticRecharge.totalRechargeByDate(
							error, null, date[j], i);
					investMoney = StatisticInvest.totalInvestByDate(error,
							null, date[j], i);
					EChartsData.saveOrUpdataDate(rechargeCount, financialCount,
							RechargeMoney, investMoney, i, timeType);
				}
			} else {
				int timeType = 0;
				if (i == Constants.LAST_7_DAYS) {
					timeType = 71;
					String[] timeA = EChartsData.getAxisDate(i, "yyyy-MM-dd");
					for (int j = 0; j < timeA.length; j++) {
						if (i == Constants.LAST_7_DAYS) {
							rechargeCount = User.queryUserCount(error, timeA[0]
									+ Constants.STARTTIME, timeA[j]
									+ Constants.ENDTIME, i); // 最近7天注册数据
							financialCount = User.queryFinancialUserCount(
									error, timeA[0] + Constants.STARTTIME,
									timeA[j] + Constants.ENDTIME, i);
							RechargeMoney = StatisticRecharge
									.totalRechargeByDate(error, timeA[0]
											+ Constants.STARTTIME, timeA[j]
											+ Constants.ENDTIME, i);
							investMoney = StatisticInvest.totalInvestByDate(
									error, timeA[0] + Constants.STARTTIME,
									timeA[j] + Constants.ENDTIME, i);
						}

						EChartsData.saveOrUpdataDate(rechargeCount,
								financialCount, RechargeMoney, investMoney, i,
								timeType);
						timeType += 1;
					}
				} else if (i == Constants.LAST_30_DAYS) {
					timeType = 301;
					String[] timeB = EChartsData.getAxisDate(i,
							"yyyy-MM-dd");
					for (int j = 0; j < timeB.length; j++) {
						rechargeCount = User.queryUserCount(error, timeB[0]
								+ Constants.STARTTIME, timeB[j]
								+ Constants.ENDTIME, i); // 最近30天注册数据
						financialCount = User.queryFinancialUserCount(error,
								timeB[0] + Constants.STARTTIME, timeB[j]
										+ Constants.ENDTIME, i);
						RechargeMoney = StatisticRecharge.totalRechargeByDate(
								error, timeB[0] + Constants.STARTTIME, timeB[j]
										+ Constants.ENDTIME, i);
						investMoney = StatisticInvest.totalInvestByDate(error,
								timeB[0] + Constants.STARTTIME, timeB[j]
										+ Constants.ENDTIME, i);

						EChartsData.saveOrUpdataDate(rechargeCount,
								financialCount, RechargeMoney, investMoney, i,
								timeType);
						timeType += 1;
					}
				}
			}
		}
	}

	/**
	 * <Description functions in a word> 将数据保存如数据库 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param rechargeCount
	 * @param financialCount
	 * @param RechargeMoney
	 * @param investMoney
	 * @param type
	 * @param timeType
	 *            [Parameters description]
	 * @return void [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static void saveOrUpdataDate(int rechargeCount, int financialCount,
			double RechargeMoney, double investMoney, int type, int timeType) {
		ErrorInfo error = new ErrorInfo();
		t_statistic_index_echart_data runData = t_statistic_index_echart_data
				.find("time_type = ?", timeType).first();

		if (runData == null) {
			runData = new t_statistic_index_echart_data();
		}
		runData.time = new Date();
		runData.new_financial_members_count = financialCount;
		runData.new_register_members_count = rechargeCount;
		runData.invest_money = investMoney;
		runData.recharge_money = RechargeMoney;
		runData.time_type = timeType;
		runData.type = type;
		try {
			runData.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("更新Echarts数据时：" + e.getMessage());
			error.code = -5;
			error.msg = "对不起，由于平台出现故障，此次数据更新失败！";
		}
	}

	/**
	 * <Description functions in a word> 获取相应条件的时间数组 <Detail description>
	 * 
	 * @author ChenZhipeng
	 * @param c
	 *            传入当前时间
	 * @param day
	 *            减少或增加的天数
	 * @return [Parameters description]
	 * @return Calendar [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private static Calendar dateFactory(Calendar calendar, int day) {
		calendar.add(Calendar.DATE, day);
		return calendar;
	}

}
