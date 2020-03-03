package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 会员CPS推广收入
 * @author lwh
 *
 */

@Entity
public class t_user_cps_income extends Model{
	public int year ;
	public int month ;
	public long user_id;
	public long recommend_user_id;
	public long spread_user_account;
	public long effective_user_account;
	public long invalid_user_account;
	public double cps_reward;
	public double cps_reward_ed;
	public int status ;
	
	@Transient
	public  long spread_user_account_temp;
	@Transient
	public  long effective_user_account_temp;
	@Transient
	public  long invalid_user_account_temp;
	@Transient
	public  double cps_reward_temp;
	
	
	public  long getSpread_user_account_temp() {
		long temp = 0;
		String sql = "select max(spread_user_account) from t_user_cps_income where year = ? and month = ? and user_id = ?";
		
		try {
			temp = t_user_cps_income.find(sql, this.year,this.month,this.user_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
			
		}
		
		return temp;
	}
	
	
	public  long getEffective_user_account_temp() {
		long temp = 0;
		String sql = "select max(effective_user_account) from t_user_cps_income where year = ? and month = ? and user_id = ?";
		
		try {
			temp = t_user_cps_income.find(sql, this.year,this.month,this.user_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
			
		}
		
		return temp;
	}
	public  long getInvalid_user_account_temp() {
		return this.spread_user_account_temp - this.effective_user_account_temp;
	}


	public double getCps_reward_temp() {
		
		double temp = 0;
		String sql = "select sum(cps_reward) from t_user_cps_income where year = ? and month = ? and user_id = ?";
		
		try {
			temp = t_user_cps_income.find(sql, this.year,this.month,this.user_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			
			return 0;
			
		}
		
		return temp;
	}
	
	
}
