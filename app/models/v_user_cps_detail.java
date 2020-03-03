package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 后台—>推广员列表—>详情
 * @author cp
 * @version 6.0
 * @created 2014年5月22日 下午5:35:11
 */
@Entity
public class v_user_cps_detail extends Model{

	public String name;
	public Date time;
	public long recommend_user_id;
	public long register_length;
	public double recharge_amount;
	public double bid_amount;
	public double repayment_amount;
	public double invest_amount;
	public String credit_level_image_filename;
	@Transient
	public double commission_amount;
	
	public double getCommission_amount() {
		Double cps_award = 0.0;
		String sql = "select sum(cps_reward_ed) from t_user_cps_income where user_id = ? and recommend_user_id = ?";
		
		try {
			cps_award = t_user_details.find(sql, this.recommend_user_id,this.id).first();
		} catch (Exception e) {
			e.printStackTrace();
			cps_award = 0.0;
			
			return cps_award;
		}
		
		if(null == cps_award){
			cps_award = 0.0;
		}
		return cps_award;
	}
}
