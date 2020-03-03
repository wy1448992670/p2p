package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;

@Entity
public class v_front_user_attention_bids extends Model{
	
	public Date time;
	public Long user_id;
	public Long bid_id;
	public String credit_name;
	public String credit_image_filename;
	public String product_filename;
	public String product_name;
	public String title;
	public Double amount;
	public Integer period;
	public Integer  period_unit;
	public Double apr;
	public Boolean is_hot;
	public Boolean is_agency;
	public String agency_name;
	public Double has_invested_amount;
	public String bid_image_filename;
	public String small_image_filename;
	public Double loan_schedule;
	public Integer  bonus_type;
	public Double bonus;
	public Double award_scale;
	public Date repayment_time;
	public String no;
	public String repay_name;
	public Boolean is_show_agency_name;
	public Integer product_id;
	public Long credit_level_id;

	@Transient
	public CreditLevel creditLevel;
	public CreditLevel getCreditLevel() {
		
		ErrorInfo error = new ErrorInfo();
		
		return CreditLevel.queryUserCreditLevel(this.user_id, error);
	}
}
