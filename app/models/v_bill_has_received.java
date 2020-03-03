package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 应收账单管理--已收款借款账单列表
 */
@Entity
public class v_bill_has_received extends Model {
     public long bid_id;
     public int year;
     public int month;
     public String bill_no;
     public String name;
     public String bid_no;
     public double amount;
     public double apr;
     public String title;
     public String period;
     public Date repayment_time;
     public long overdue_time;
     public Date real_repayment_time;
     public String supervisor_name;
     public String supervisor_name2;
     public Double current_pay_amount;//本期账单应还款
     public String reality_name;
     
     @Transient
 	public String sign;
     /* 2019-03-07 */
 	public Double service_amount;
  	public Double repayment_interest;//应还利息
  	public Double repayment_corpus;//应还本金
	public String tag;
	
 	/**
 	 * 获取加密ID
 	 */
 	public String getSign() {
 		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
}
