package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;
import utils.ServiceFee;

/**
 * 应收账单管理--垫付账单列表
 */
@Entity
public class v_bill_advance extends Model {
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
     public Date repayment_time;
     public long overdue_time;
     public long overdue_count;
     public String supervisor_name;
     public String supervisor_name2;
     public Double repayment_money;//当期应还
     
     public String reality_name;
     
     @Transient
 	public String sign;
 	
	public Double service_amount;
 	public Double repayment_interest;
 	public Double repayment_corpus;
	public String tag;
	public Boolean is_payment_on_company; //是否公司垫付
	public Boolean payment_on_company_need_repaid; //是否需要公司垫付后还款,true需要(未还),false不需要(已还)
	
 	/**
 	 * 获取加密ID
 	 */
 	public String getSign() {
 		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
     
}