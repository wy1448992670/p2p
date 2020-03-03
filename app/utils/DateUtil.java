package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.shove.Convert;

/**
 * 时间工具类
 * 
 * @author Administrator
 *
 */
public class DateUtil {
	/**
	 * 时间转换为yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		if (null == date)
			return "";

		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	public static String dateToString1(Date date) {
		if (null == date)
			return "";

		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}

	public static String dateToString2(Date date) {
		if (null == date)
			return "";

		return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
	}

	public static String dateFormat(Date date, String format) {
		if (null == date)
			return "";
		return new SimpleDateFormat(format).format(date);
	}

	public static String simple(Date date) {
		if (null == date)
			return "";

		return new SimpleDateFormat("yyyyMMdd").format(date);
	}

	public static String simple2(Date date) {
		if (null == date)
			return "";

		return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
	}

	public static Date strToDate(String dateString) {
		if (null == dateString)
			return new Date();

		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		} catch (ParseException e) {
			return new Date();
		}
	}

	public static Date strToYYMMDDDate(String dateString) {
		if (null == dateString)
			return new Date();

		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			return new Date();
		}
	}

	// 获取系统时间并返回时间格式
	public static Date currentDate() {
		DateFormat YYYY_MM_DD_MM_HH_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			String currentDates = YYYY_MM_DD_MM_HH_SS.format(new Date());

			return YYYY_MM_DD_MM_HH_SS.parse(currentDates);
		} catch (ParseException e) {
			return new Date();
		}
	}

	/**
	 * 得到当前时间距2013-11-01 00:00:00的小时数
	 * 
	 * @return
	 * @throws ParseException
	 */
	public long getHours() {
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = simple.parse("2013-11-01 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long millisecond = System.currentTimeMillis() - date.getTime();
		long temp = 1000 * 60 * 60;
		return millisecond / temp;
	}

	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate 较小的时间
	 * @param bdate  较大的时间
	 * @return 相差天数
	 * @throws ParseException
	 */
	public static int daysBetween(Date smdate, Date bdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			smdate = sdf.parse(sdf.format(smdate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			bdate = sdf.parse(sdf.format(bdate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

	/**
	 * 计算两个时间之间相差的天数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long diffDays(Date startDate, Date endDate) {
		long days = 0;
		long start = startDate.getTime();
		long end = endDate.getTime();
		// 一天的毫秒数1000 * 60 * 60 * 24=86400000
		days = (end - start) / 86400000;
		return days;
	}

	/**
	 * 计算两个时间之间相差的天数，不满一天按一天算
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long diffDays2(Date startDate, Date endDate) {
		long start = startDate.getTime();
		long end = endDate.getTime();
		// 一天的毫秒数1000 * 60 * 60 * 24=86400000
		double days = Math.ceil((end - start) / 86400000d); // 不满一天按一天算
		return Convert.strToLong(String.format("%.0f", days), 0);
	}

	/**
	 * 计算两个时间之间相差的分钟数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long diffMinutes(Date startDate, Date endDate) {
		return (endDate.getTime() - startDate.getTime()) / 60000;
	}

	/**
	 * 计算两个时间之间相差的分钟数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static long diffSeconds(Date startDate, Date endDate) {
		return (endDate.getTime() - startDate.getTime()) / 1000;
	}

	/**
	 * 日期加上月数的时间
	 * 
	 * @param date
	 * @param month
	 * @return
	 */
	public static Date dateAddMonth(Date date, int month) {
		return add(date, Calendar.MONTH, month);
	}

	/**
	 * 日期加上天数的时间
	 * 
	 * @param date
	 * @param month
	 * @return
	 */
	public static Date dateAddDay(Date date, int day) {
		return add(date, Calendar.DAY_OF_YEAR, day);
	}

	/**
	 * 日期加上年数的时间
	 * 
	 * @param date
	 * @param year
	 * @return
	 */
	public static Date dateAddYear(Date date, int year) {
		return add(date, Calendar.YEAR, year);
	}

	/**
	 * 计算剩余时间 (多少天多少时多少分)
	 * 
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 */
	public static String remainDateToString(Date startDate, Date endDate) {
		StringBuilder result = new StringBuilder();
		if (endDate == null) {
			return "过期";
		}
		long times = endDate.getTime() - startDate.getTime();
		if (times < -1) {
			result.append("过期");
		} else {
			long temp = 1000 * 60 * 60 * 24;
			// 天数
			long d = times / temp;

			// 小时数
			times %= temp;
			temp /= 24;
			long m = times / temp;
			// 分钟数
			times %= temp;
			temp /= 60;
			long s = times / temp;

			result.append(d);
			result.append("天");
			result.append(m);
			result.append("小时");
			result.append(s);
			result.append("分");
		}
		return result.toString();
	}

	private static Date add(Date date, int type, int value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(type, value);
		return calendar.getTime();
	}

	/**
	 * @MethodName: getLinkUrl
	 * @Param: DateUtil flag ： true 转换 false 不转换
	 * @Author: gang.lv
	 * @Date: 2013-5-8 下午02:52:44
	 * @Return:
	 * @Descb:
	 * @Throws:
	 */
	public static String getLinkUrl(boolean flag, String content, String id) {
		if (flag) {
			content = "<a href='finance.do?id=" + id + "'>" + content + "</a>";
		}
		return content;
	}

	/**
	 * 时间转换为时间戳
	 * 
	 * @param format
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static long getTimeCur(String format, String date) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(format);
		return sf.parse(sf.format(date)).getTime();
	}

	/**
	 * 时间转换为时间戳
	 * 
	 * @param format
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static long getTimeCur(String format, Date date) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(format);
		return sf.parse(sf.format(date)).getTime();
	}

	/**
	 * 将时间戳转为字符串
	 * 
	 * @param cc_time
	 * @return
	 */
	public static String getStrTime(String cc_time) {
		String re_StrTime = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		long lcc_time = Long.valueOf(cc_time);
		re_StrTime = sdf.format(new Date(lcc_time * 1000L));
		return re_StrTime;
	}

	public static int getAge(Date birthday) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(birthday);
		Calendar nowDate = Calendar.getInstance();

		return nowDate.get(Calendar.YEAR) - calendar.get(Calendar.YEAR);
	}

	/**
	 * 当前时间减去分钟数得到的时间
	 * 
	 * @param minutes
	 * @return
	 * @throws ParseException
	 */
	public static String getDateMinusMinutes(int minutes) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date nowTime = new Date();
		String datetest = formatter.format(nowTime);
		Date date = formatter.parse(datetest);
		long Time1 = (date.getTime() / 1000) - 60 * minutes;// 减去30分
		date.setTime(Time1 * 1000);
		return formatter.format(date);
	}

	/**
	 * 和当前时间比较是否在给定的时长内
	 * 
	 * @param validTime 给定的时间
	 * @param time      给定的时长（s）
	 * @return true 有效 false 无效
	 */
	public static boolean inValidTime(Date validTime, int time) {
		long currTime = System.currentTimeMillis();
		long valid = validTime.getTime();

		return ((currTime - valid) < time * 1000);
	}

	/**
	 * 获取年
	 * 
	 * @param time
	 * @return
	 */
	public static int getYear(Date time) {
		if (time == null) {
			return -1;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);

		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取月
	 * 
	 * @param time
	 * @return
	 */
	public static int getMonth(Date time) {
		if (time == null) {
			return -1;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);

		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取日
	 * 
	 * @param time
	 * @return
	 */
	public static int getDay(Date time) {
		if (time == null) {
			return -1;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);

		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取年
	 * 
	 * @return
	 */
	public static int getYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 获取月
	 * 
	 * @return
	 */
	public static int getMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取日
	 * 
	 * @return
	 */
	public static int getDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 将yyyy-MM-dd拼接成yyyy-MM-dd :HH:mm:ss
	 * 
	 * @param startDateStr
	 * @return
	 */
	public static Date strDateToStartDate(String startDateStr) {

		return DateUtil.strToDate(startDateStr + " 00:00:00");
	}

	/**
	 * 将yyyy-MM-dd拼接成yyyy-MM-dd :HH:mm:ss
	 * 
	 * @param startDateStr
	 * @return
	 */
	public static Date strDateToEndDate(String endDateStr) {

		return DateUtil.strToDate(endDateStr + " 23:59:59");
	}

	/**
	 * 当前时间减去天数得到的时间
	 * 
	 * @param minutes
	 * @return
	 * @throws ParseException
	 */
	public static Date getDateMinusDay(int day) {
		Date time = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		c.set(Calendar.DATE, c.get(Calendar.DATE) - day);
		return c.getTime();
	}

	public static String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(new Date());
	}

	/**
	 * 得到当前时间
	 * 
	 * @这里用一句话描述这个方法的作用
	 * @return
	 * @author: zj
	 */
	public static String getDateTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date());
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年5月7日
	 * @description 转换指定日期指定格式的日期为string
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		if (null == date)
			return "";

		return new SimpleDateFormat(format).format(date);
	}

	/***
	 * 给出指定日期、指定格式，获取日期偏移量
	 * 
	 * @param d      日期
	 * @param format 格式
	 * @param n      偏移量
	 * @return
	 */
	public static String getDate(Date d, String format, int n) {
		Calendar calendar = Calendar.getInstance();
		if (d == null) {
			calendar.add(Calendar.DATE, n);
			return new SimpleDateFormat(format == null ? "yyyy-MM-dd HH:mm:ss" : format).format(calendar.getTime());
		}
		calendar.setTime(d);
		calendar.add(Calendar.DATE, n);
		return new SimpleDateFormat(format == null ? "yyyy-MM-dd HH:mm:ss" : format).format(calendar.getTime());
	}

	/**
	 * @Description 月份增加
	 * @param date  日期
	 * @param month 增加月份
	 * @return 增加后的月份 日期类型
	 * @author: zj
	 */
	public static Date addMonth(Date date, int month) {
		try {
			Calendar ct = Calendar.getInstance();
			ct.setTime(date);
			ct.add(Calendar.MONTH, +6);
			return ct.getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Date();
	}

	public static void main(String[] args) {
		System.out.println(addMonth(new Date(), 6));
	}
}
