package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 应收账单管理--所有账单列表
 */
@Entity
public class v_bill_all extends Model {
	public long bid_id;
 	public int year;
 	public int month;
 	public String bill_no;
 	public String name;
	public String mobile;
	public String bid_no;
	public double amount;
	public double apr;
	public String title;
	public String period;
	public String period_;
	public String period_unit;
	public Date repayment_time;
	public Date real_repayment_time;
	public Date release_time;//放款时间
	public long overdue_time;
	public long overdue_count;
	public String supervisor_name;
	public String supervisor_name2;
 	public Double repayment_money;//当期应还 
    
	public String reality_name;
	public String status;
	public String overdue_status;
	@Transient
	public String sign;
 	
	public Double service_amount;
	public Double repayment_interest;
	public Double repayment_corpus;
	public String tag;
	
 	/**
 	 * 获取加密ID
 	 */
	public String getSign() {
 		 return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
     
}