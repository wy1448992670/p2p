package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.JPA;
import play.db.jpa.Model;
import utils.Security;

import com.shove.Convert;

import constants.Constants;

/**
 * 应付账单管理--待付款理财账单列表
 */
@Entity
public class v_bill_invests_pending_payment extends Model {
    public String bill_no;
    public int year;
    public int month;
    public String invest_name;
    public String period;
    public double pay_amount;
    public String title;
    public long bid_id;
    public String bid_no;
    public String name;
    public Date receive_time;
    public long overdue_time;
    
    @Transient
	public int unpaid_bills; // 未付账单数
    public int getUnpaid_bills() {
		String sql = "SELECT count(1) FROM t_bill_invests t1 WHERE t1.user_id = ? AND t1. STATUS IN (- 1 ,- 2 ,- 5 ,- 6)";
		Object count = JPA.em().createNativeQuery(sql).setParameter(1, this.user_id).getSingleResult();
		return Convert.strToInt(count.toString(), -1);
	}
    
	public int user_id;
    public int status;
    public String supervisor_name;
    public String supervisor_name2;
    
    @Transient
 	public String sign;
 	
 	/**
 	 * 获取加密ID
 	 */
 	public String getSign() {
 		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
}