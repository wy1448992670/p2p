package reports;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.persistence.Query;

import com.shove.Convert;

import constants.Constants;
import models.t_statistic_recharge;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DataUtil;
import utils.ErrorInfo;

/**
 * 充值统计表
 * @author zhs
 * @version 6.0
 * @created 2014-7-18
 *
 */
public class StatisticRecharge {
	
	/**
	 * 周期性执行
	 * @param error
	 * @return
	 */
	public static int executeUpdate(ErrorInfo error) {
		error.clear();
		boolean isAdd = isAdd(error);
		
		if (error.code < 0) {
			return error.code;
		}
		
		if (isAdd) {
			update(error);
		} else {
			add(error);
		}
		
		error.code = 0;
		return error.code;
	}
	
	/**
	 * 添加本日统计数据
	 * @param error
	 * @return
	 */
	private static int add(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		t_statistic_recharge entity = new t_statistic_recharge();
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.recharge_amount = queryRechargeAmount(error);
		entity.recharge_count = queryRechargeAccount(error);
		entity.recharge_menber = queryRechargeMember(error);
		entity.new_recharge_menber = queryNewRechargeMember(error);
		entity.average_recharge = entity.recharge_menber == 0 ? 0.0d : Arith.div(entity.recharge_amount, entity.recharge_menber, 2); 
		entity.average_each_recharge = entity.recharge_count == 0 ? 0.0d : Arith.div(entity.recharge_amount, entity.recharge_count, 2);
		entity.max_recharge_amount = queryMaxRechargeMount(error);
		entity.min_recharge_amount = queryMinRechargeMount(error);

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		error.code = 0;
		return error.code;
	}
	
	/**
	 * 更新本日统计数据
	 * @param error
	 * @return
	 */
	private static int update(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		t_statistic_recharge entity = null;
		
		try {
			entity = t_statistic_recharge.find("year = ? and month = ? and day = ?", year, month, day).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if (entity == null) {
			error.code = -1;
			error.msg = "本日充值统计不存在";
			
			return error.code;
		}
		
		entity.year = cal.get(Calendar.YEAR);
		entity.month = cal.get(Calendar.MONTH) + 1;
		entity.day = cal.get(Calendar.DAY_OF_MONTH);
		entity.recharge_amount = queryRechargeAmount(error);
		entity.recharge_count = queryRechargeAccount(error);
		entity.recharge_menber = queryRechargeMember(error);
		entity.new_recharge_menber = queryNewRechargeMember(error);
		entity.average_recharge = entity.recharge_menber == 0 ? 0.0d : Arith.div(entity.recharge_amount, entity.recharge_menber, 2);
		entity.average_each_recharge = entity.recharge_count == 0 ? 0.0d : Arith.div(entity.recharge_amount, entity.recharge_count, 2);
		entity.max_recharge_amount = queryMaxRechargeMount(error);
		entity.min_recharge_amount = queryMinRechargeMount(error);

		try {
			entity.save();
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}

		error.code = 0;
		return error.code;
	}
	
	/**
	 * 是否添加了本日数据
	 * @return
	 */
	private static boolean isAdd(ErrorInfo error) {
		error.clear();
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		int count = 0;
		
		try {
			count = (int)t_statistic_recharge.count("year = ? and month = ? and day = ?", year, month, day);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return false;
		}
		
		return (count > 0);
	}

	/**
	 * 充值金额
	 * @return
	 */
	public static double queryRechargeAmount(ErrorInfo error){
		error.clear();
		String sql = "select sum(a.amount) from t_user_recharge_details as a where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d') "+
					 "AND is_completed = TRUE ";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询充值金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询充值金额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 充值笔数
	 * @return
	 */
	public static int queryRechargeAccount(ErrorInfo error){
		error.clear();
		String sql = "select count(a.id) from t_user_recharge_details as a where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d')"+
					 "AND is_completed = TRUE ";
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询充值笔数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询充值笔数出现异常！";
			
			return 0;
		}
		
		return obj == null ? 0 : Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 充值会员数
	 * @return
	 */
	public static int queryRechargeMember(ErrorInfo error){
		error.clear();
		String sql = "SELECT COUNT(distinct a.user_id) FROM t_user_recharge_details as a where" +
				" date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d')" +
				"AND is_completed = TRUE ";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询充值会员数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询充值会员数出现异常！";
			
			return 0;
		}
		
		return obj == null ? 0 : Convert.strToInt(obj+"", 0);
	}
	
	/**
	 * 新增充值会员数
	 * @return
	 */
	public static int queryNewRechargeMember(ErrorInfo error){
		error.clear();
		String sql = "SELECT COUNT(t.a) FROM (SELECT COUNT(id) AS a,(SELECT COUNT(b.id) AS b FROM t_user_recharge_details b WHERE b.user_id = a.user_id) AS b FROM t_user_recharge_details AS a WHERE	date_format(a.time, '%y%m%d') = date_format(CURDATE(), '%y%m%d')AND is_completed = TRUE GROUP BY user_id) t WHERE t.a = t.b;";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		try {
			obj = query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询充值笔数时："+e.getMessage());
			error.code = -1;
			error.msg = "查询充值笔数出现异常！";
			
			return 0;
		}
		
		return obj == null ? 0 : Integer.parseInt(obj.toString());
	}
	
	/**
	 * 人均充值金额
	 * @return
	 */
	public static double queryAverageRecharge(ErrorInfo error){
		error.clear();
		String sql = "SELECT IFNULL((ROUND((SUM(a.amount))/COUNT(distinct a.user_id),2)),0) as apr FROM t_user_recharge_details as a" +
				" where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d') "+ 
				" and is_completed = true";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询人均充值金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询人均充值金额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 平均每笔充值金额
	 * @return
	 */
	public static double queryAverageEachRecharge(ErrorInfo error){
		error.clear();
		String sql = "SELECT (ROUND((SUM(a.amount))/COUNT(a.id),2)) as apr FROM t_user_recharge_details as a" +
				" where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d') "+
				" and is_completed = true ";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询平均每笔充值金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询平均每笔充值金额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 最高充值金额
	 * @return
	 */
	public static double queryMaxRechargeMount(ErrorInfo error){
		error.clear();
		String sql = "SELECT MAX(a.amount) from t_user_recharge_details AS a where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d')"+
					 " and is_completed = true ";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询最高充值金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询最高充值金额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 最低充值金额
	 * @return
	 */
	public static double queryMinRechargeMount(ErrorInfo error){
		error.clear();
		String sql = "SELECT MIN(a.amount) from t_user_recharge_details AS a where date_format(a.time, '%y%m%d') = date_format(curdate(), '%y%m%d') "+
					 "and is_completed = true ";
		
		Query query = JPA.em().createNativeQuery(sql);
		Object obj = null;
		
		if(query.getResultList().size() == 0){
			return 0;
		}		
		
		try {
			obj = query.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询最低充值金额时："+e.getMessage());
			error.code = -1;
			error.msg = "查询最低充值金额出现异常！";
			
			return 0;
		}
		
		return (obj == null) ? 0 : Convert.strToDouble(obj+"", 0);
	}
	
	/**
	 * 统计充值数据
	 * @param error
	 * @return
	 */
	public static double totalRecharge(ErrorInfo error){
		error.clear();
		
		String sql = "select sum(recharge_amount) from t_statistic_recharge";
		Double totalAmount = null;
		
		try {
			totalAmount = t_statistic_recharge.find(sql).first();//获取投资用户的余额
		 } catch(Exception e) {
				e.printStackTrace();
				Logger.info("统计充值总额时："+e.getMessage());
				error.code = -1;
				error.msg = "数据库异常，导致统计充值总额失败";
				
				return error.code;
			}
		
		return (totalAmount == null) ? 0 : Convert.strToDouble(totalAmount+"", 0);
	}
	
	/**
	 * Echarts获取充值金额数据
	 * 
	 */
	public static double totalRechargeByDate(ErrorInfo error,String startTime,String endTime,int type){
		error.clear();
		
		String sql = null;
		int hour = 0;
		
		if (type == Constants.YESTERDAY) {
			sql = "SELECT SUM(amount) FROM t_user_recharge_details WHERE is_completed = 1 AND TO_DAYS(NOW()) - TO_DAYS(completed_time) = 1 AND HOUR(completed_time) <= ?";
			if (endTime.contains(":")) {
				hour = Integer.parseInt(endTime.substring(0, endTime.indexOf(":")));
			}
			if(0==hour){
				hour = 24;
			}
		}else{
			sql="SELECT SUM(amount) FROM t_user_recharge_details WHERE is_completed = 1 AND completed_time >= ? AND completed_time <= ? ";
		}
		
		Double totalAmount = null;
		
		try {
			if (type == Constants.YESTERDAY) {
				totalAmount = t_statistic_recharge.find(sql,hour).first();
			}else {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				totalAmount = t_statistic_recharge.find(sql,format.parse(startTime),format.parse(endTime)).first();
			}
		 } catch(Exception e) {
				e.printStackTrace();
				Logger.info("统计充值总额时："+e.getMessage());
				error.code = -1;
				error.msg = "数据库异常，导致统计充值总额失败";
				
				return error.code;
			}
		
		return (totalAmount == null) ? 0 : Convert.strToDouble(totalAmount+"", 0);
	}
}
