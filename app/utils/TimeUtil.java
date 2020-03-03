package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import play.Logger;

/**
 * 时间工具类
 * 
 * @author yuy
 * @date 2015-05-19 20:41
 */
public class TimeUtil {

	/**
	 * Date 转成 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToStr(Date date) {
		if (date == null)
			return null;
		String str = null;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		str = format.format(date);
		return str;
	}

	/**
	 * Date 转成 yyyy-MM-dd
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToStrDate(Date date) {
		if (date == null)
			return null;
		String str = null;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		str = format.format(date);
		return str;
	}

	/**
	 * Date 转成 yyyy年MM月dd日
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToStrCurr(Date date) {
		if (date == null)
			return null;
		String str = null;
		DateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
		str = format.format(date);
		return str;
	}

	/**
	 * Date 转成 yyyyMMddHHmmss
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToStr_yyyyMMddHHmmss(Date date) {
		if (date == null)
			return null;
		String str = null;
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		str = format.format(date);
		return str;
	}

	/**
	 * Date 转成 yyyyMMdd
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToStr_yyyyMMdd(Date date) {
		if (date == null)
			return null;
		String str = null;
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		str = format.format(date);
		return str;
	}

	/**
	 * yyyy-MM-dd HH:mm:ss 转成 Date
	 * 
	 * @param str
	 * @return
	 */
	public static Date strToDate(String str) {
		if (StringUtils.isBlank(str))
			return null;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			Logger.error(e, "yyyy-MM-dd HH:mm:ss转Date出现异常");
			return null;
		}
		return date;
	}

	/**
	 * yyyyMMdd 转成 Date
	 * 
	 * @param str
	 * @return
	 */
	public static Date strToDate_yyyyMMdd(String str) {
		if (StringUtils.isBlank(str))
			return null;
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			Logger.error(e, "yyyyMMdd转Date出现异常");
			return null;
		}
		return date;
	}

	/**
	 * yyyyMMddHHmmss 转成 Date
	 * 
	 * @param str
	 * @return
	 */
	public static Date strToDate_yyyyMMddHHmmss(String str) {
		if (StringUtils.isBlank(str))
			return null;
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			Logger.error(e, "yyyyMMddHHmmss转Date出现异常");
			return null;
		}
		return date;
	}

	/**
	 * 比较是否在时间区间内
	 * 
	 * @param begintime
	 * @param endtime
	 * @param time
	 * @return
	 */
	public static boolean compareTime(Date begintime, Date endtime, Date time) {
		if (begintime == null || endtime == null || time == null)
			return false;
		if (begintime.before(time) && endtime.after(time)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		Date begintime = strToDate("2015-06-29 13:00:00");
		Date endtime = strToDate("2015-06-29 16:00:00");
		Date testtime = strToDate("2015-06-29 15:59:59");
		System.out.println(compareTime(begintime, endtime, testtime));
	}

}
