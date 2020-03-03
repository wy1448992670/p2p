package reports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.shove.Convert;

import models.t_statistic_cps;
import models.t_statistic_invitation;
import models.t_statistic_invitation_details;
import play.Logger;
import play.db.jpa.JPA;

/**
 * 财富圈报表统计
 *
 * @author hys
 * @createDate  2015年7月31日 下午3:50:05
 *
 */
public class StatisticInvitation {
	
	/**
	 * 财富圈统计，插入记录
	 */
	public static void saveOrUpdateRecord(int year, int month){
		
		int invite_code_count = queryInviteCodeCount(year, month);
		int invited_user_count = queryInvitedUserCount(year, month);
		int invited_recharge_user_count = queryRechargeUserCount(year, month);
		int invited_invest_user_count = queryInvestUserCount(year, month);
		double invest_amount = queryInvestAmount(year, month);
		double invitation_income = queryInvitaionIncome(year, month);

		t_statistic_invitation invitation = null;
		
		try {
			invitation = t_statistic_invitation.find(" year = ? and month = ? ", year, month).first();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		
		if(null == invitation){
			invitation = new t_statistic_invitation();
		}
		
		invitation.time = new Date();
		invitation.year = year;
		invitation.month = month;
		invitation.invite_code_count = invite_code_count;
		invitation.invited_user_count = invited_user_count;
		invitation.invited_recharge_user_count = invited_recharge_user_count;
		invitation.invited_invest_user_count = invited_invest_user_count;
		invitation.invest_amount = invest_amount;
		invitation.invitation_income = invitation_income;
		
		try {
			invitation.save();
		} catch (Exception e) {
			Logger.error("财富圈统计，插入记录时，%s", e.getMessage());
		}
	}
	
	/**
	 * 财富圈统计-返佣明细，插入记录
	 */
	public static void saveOrUpdateDetailRecord(int year, int month){
		
		String sql = "SELECT DISTINCT(user_id), user_name FROM t_wealthcircle_invite";
		
		List<Object[]> list = null;
		
		try {
			list = JPA.em().createNativeQuery(sql).getResultList();
		} catch (Exception e) {
			Logger.error("查询所有拥有邀请码的会员时，%s", e.getMessage());
			
			return ;
		}
		
		t_statistic_invitation_details incomeDetail = null;
		for(Object[] o : list){
			long userId = Convert.strToLong(o[0].toString(), 0);
			String userName = o[1].toString();
			incomeDetail = t_statistic_invitation_details.find(" year = ? and month = ? and user_name = ?", year, month, userName).first();
			
			if(null == incomeDetail){
				incomeDetail = new t_statistic_invitation_details();
			}
			
			
			int total_invite_code = queryTotalInviteCode(userName);
			double invited_user_invest_amount = queryInvitedUserInvestAmount(year, month, userId);
			double invitation_income = queryInvitedUserInvitaionIncome(year, month, userId);
			
			
			incomeDetail.time = new Date();
			incomeDetail.year = year;
			incomeDetail.month = month;
			incomeDetail.user_name = userName;
			incomeDetail.total_invite_code = total_invite_code;
			incomeDetail.invited_user_invest_amount = invited_user_invest_amount;
			incomeDetail.invitation_income = invitation_income;
			
			try {
				incomeDetail.save();
			} catch (Exception e) {
				Logger.error("财富圈统计-返佣明细，插入记录时，%s", e.getMessage());
			}
		}
	}

	private static double queryInvitedUserInvestAmount(int year, int month, long userId) {
		String sql = "SELECT SUM(invest_amount) FROM t_wealthcircle_income WHERE YEAR(time) = ? AND MONTH(time) = ? AND user_id = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).setParameter(3, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计会员[%s]%s年%s月受邀理财金额时，%s", userId, year, month, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigDecimal)count).intValue();
	}
	
	private static double queryInvitedUserInvitaionIncome(int year, int month, long userId) {
		String sql = "SELECT SUM(invite_income) FROM t_wealthcircle_income WHERE YEAR(time) = ? AND MONTH(time) = ? AND user_id = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).setParameter(3, userId).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计会员[%s]%s年%s月受邀理财金额时，%s", userId, year, month, e.getMessage());
			
			return 0;
		}
		
		return count==null?0:((BigDecimal)count).intValue();
	}

	private static int queryTotalInviteCode(String userName) {
		String sql = "SELECT COUNT(1) FROM t_wealthcircle_invite WHERE user_name = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, userName).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计会员【%s】邀请码个数时，%s", userName, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigInteger)count).intValue();
	}

	private static double queryInvitaionIncome(int year, int month) {
		String sql = "SELECT SUM(invite_income) FROM t_wealthcircle_income WHERE YEAR(time) = ? AND MONTH(time) = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增返佣金额时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigDecimal)count).intValue();
	}

	private static double queryInvestAmount(int year, int month) {
		String sql = "SELECT SUM(invest_amount) FROM t_wealthcircle_income WHERE YEAR(time) = ? AND MONTH(time) = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增受邀理财金额时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigDecimal)count).intValue();
	}

	private static int queryInvestUserCount(int year, int month) {
		String sql = "SELECT u.master_time_invest FROM t_wealthcircle_invite wi INNER JOIN t_users u ON u.id = wi.invited_user_id WHERE YEAR(u.master_time_invest) = ? AND MONTH(u.master_time_invest) = ?";

		List<Object> list = null;
		
		try {
			list = JPA.em().createNativeQuery(sql)
					.setParameter(1, year)
					.setParameter(2, month)
					.getResultList();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增受邀理财会员数数时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return list==null?0:list.size();
	}

	private static int queryRechargeUserCount(int year, int month) {
		String sql = "SELECT MIN(urd.time) As time FROM t_user_recharge_details urd INNER JOIN t_wealthcircle_invite wi ON urd.user_id = wi.invited_user_id GROUP BY urd.user_id HAVING YEAR(time) = ? AND MONTH(time) = ?";

		List<Object> list = null;
		
		try {
			list = JPA.em().createNativeQuery(sql)
					.setParameter(1, year)
					.setParameter(2, month)
					.getResultList();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增受邀充值会员数数时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return list==null?0:list.size();
	}

	private static int queryInvitedUserCount(int year, int month) {
		String sql = "SELECT COUNT(1) FROM t_wealthcircle_invite WHERE YEAR(invited_register_time) = ? AND MONTH(invited_register_time) = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增受邀会员数数时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigInteger)count).intValue();
	}

	private static int queryInviteCodeCount(int year, int month) {
		
		String sql = "SELECT COUNT(1) FROM t_wealthcircle_invite WHERE YEAR(time) = ? AND MONTH(time) = ?";
		
		Object count = null;
		
		try {
			count = JPA.em().createNativeQuery(sql).setParameter(1, year).setParameter(2, month).getSingleResult();
		} catch (Exception e) {
			Logger.error("统计%s年%s月新增邀请码数时，%s", year, month, e.getMessage());
			
			return 0;
		}
	
		return count==null?0:((BigInteger)count).intValue();
	}

}
