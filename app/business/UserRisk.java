package business;

import java.util.Date;

import javax.persistence.Query;

import constants.UserRiskTypeEnum;
import play.Logger;
import play.db.jpa.JPA;

public class UserRisk {

	/**
	 * 
	 * @author liulj
	 * @creationDate. Mar 15, 2018 2:28:08 PM 
	 * @description.  更新用户风险评测信息
	 * 
	 * @param userId
	 * @param riskResult
	 * @param riskAnswer
	 * @return
	 */
	public static int updateUserRisk(long userId, String riskResult, String riskAnswer) {
		try {
			String sql = "update t_users set risk_type = ?,risk_result = ?, risk_answer = ? where id = ?";
			Query query = JPA.em().createQuery(sql);
			query.setParameter(1, UserRiskTypeEnum.get(riskResult).getCode());
			query.setParameter(2, riskResult);
			query.setParameter(3, riskAnswer);
			query.setParameter(4, userId);
			return query.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("更新用户风险评测信息：%s", e.getMessage());
			return 0;
		}
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Mar 15, 2018 2:28:08 PM 
	 * @description.  更新用户风险评测信息
	 * 
	 * @param userId
	 * @param riskResult
	 * @param riskAnswer
	 * @return
	 */
	public static int updateUserRiskOld(long userId, String riskResult, String riskAnswer) {
		//api版本<1.2
		try {
			String sql = "update t_users set risk_type = ?,risk_result = ?, risk_answer = ? where id = ?";
			Query query = JPA.em().createQuery(sql);
			query.setParameter(1, UserRiskTypeEnum.getByOldName(riskResult).getCode());
			query.setParameter(2, riskResult);
			query.setParameter(3, riskAnswer);
			query.setParameter(4, userId);
			return query.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("更新用户风险评测信息：%s", e.getMessage());
			return 0;
		}
	}
}
