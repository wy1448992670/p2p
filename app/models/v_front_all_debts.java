package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;

@Entity
public class v_front_all_debts extends Model{
	
	public Date time;
	public String title;
	public String transfer_reason;
	public Double debt_amount;
	public Integer status;
	public Date end_time;
	public Double bid_amount;
	public Double apr;
	public Long user_id;
	public Integer product_id;
	public String product_name;
	public String product_image_filename;
	public String bid_image_filename;
	public Integer credit_level_id;
	public Boolean is_quality_debt;
	public Date repayment_time;
	public Double transfer_price;
	public Double max_price;
	public String credit_image_filename;
	public String small_image_filename;
	public int join_times;
	
	@Transient
	public CreditLevel creditLevel;
	
	public CreditLevel getCreditLevel() {
		
		ErrorInfo error = new ErrorInfo();
        
		return CreditLevel.queryUserCreditLevel(user_id, error);
	}
	
}
