package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import business.CreditLevel;
import play.db.jpa.Model;
import utils.ErrorInfo;
import utils.ServiceFee;

/**
 * 用户关注的借款标列表
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-21 上午10:47:08
 */
@Entity
public class v_bid_attention extends Model {
	public String bid_no;
	public String title;
	public Integer product_id;
	public String small_image_filename;
	public String product_name;
	public Long user_id;
	public Long bid_id;
	public Long bid_user_id;
	public String user_name;
	public Double amount;
	public Double apr;
	public Integer status;
	public String mark;
	@Transient
	public double capital_interest_sum;
	public Integer product_item_count;
	public Integer user_item_count_true;
	public Integer period;
	public Integer period_unit;
	public Integer repaymentId;
	
	@Transient
	public CreditLevel creditLevelApp;  //app有用到,用来查询借款人的信用等级
	
	public CreditLevel getCreditLevelApp() {
		
		ErrorInfo error = new ErrorInfo();
		
		return CreditLevel.queryUserCreditLevel(this.bid_user_id, error);
	}
	
	@Transient
	public CreditLevel creditLevel;
	
	public CreditLevel getCreditLevel() {
		
		ErrorInfo error = new ErrorInfo();
		
		return CreditLevel.queryUserCreditLevel(this.user_id, error);
	}
	
	public double getCapital_interest_sum() {
		double rate = ServiceFee.interestCompute(this.amount, this.apr, this.period_unit, this.period, this.repaymentId);
		
		return this.amount + rate;
	}
}