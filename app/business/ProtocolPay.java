package business;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import models.t_user_recharge_details;
import play.Logger;
import play.db.jpa.JPA;

public class ProtocolPay {

	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 28, 2018 4:36:41 PM 
	 * @description.  查询处理中的充值记录
	 * 
	 * @return
	 */
	public static List<t_user_recharge_details> findRecharge4I() {
		return t_user_recharge_details.find("from t_user_recharge_details where payment_gateway_id = -4 and is_completed = 0 and completed_time is null and pay_number is not null ").fetch();
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Apr 28, 2018 4:38:55 PM 
	 * @description.  将处理中的充值记录修改为失败
	 * 
	 * @param payNumber
	 * @return
	 */
	public static int updateRecharge4F(String payNumber) {
		try {
			String sql = "update t_user_recharge_details set completed_time = time where pay_number = ? and is_completed = 0";
			Query query = JPA.em().createQuery(sql);
			query.setParameter(1, payNumber);
			return query.executeUpdate();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.error("更新充值状态时：%s", e.getMessage());
			return 0;
		}
	}
}
