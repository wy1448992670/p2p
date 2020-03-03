package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

@Entity
public class v_user_cps_users extends Model{

	public long recommend_user_id;
	public Date time;
	public int year;
	public int month;
	public String name;
	public boolean is_active;
	public double bid_amount;
	public double invest_amount;
	public String mobile;
	@Transient
	public double cps_award;
	
	
	
	public double getCps_award() {
		Double cps_award = 0.0;
		String sql = "select sum(cps_reward) from t_user_cps_income where user_id = ? and recommend_user_id = ?";
		
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

	@Transient
	public String sign;//加密ID
	
	public String getSign() {
		return Security.addSign(this.id, Constants.USER_ID_SIGN);
	}
}
