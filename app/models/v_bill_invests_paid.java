package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 *应付账单管理--已付款理财账单列表
 */
@Entity
public class v_bill_invests_paid extends Model {
	public String bill_no;
	public Integer year;
    public Integer month;
    public String invest_name;
    public String period;
    public double pay_amount;
    public String title;
    public long bid_id;
    public String bid_no;
    public String name;
    public Date receive_time;
    public Date real_receive_time;
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