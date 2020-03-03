package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 应付账单管理--逾期未付理财账单列表
 */
@Entity
public class v_bill_invests_overdue_unpaid extends Model {
    public String bill_no;
    public int bill_status;
    public long bill_id;
    public int bill_period;
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
    public int unpaid_bills;
    public String supervisor_name;
    public String supervisor_name2;
    public boolean is_register_guarantor;
    @Transient
 	public String sign;
    @Transient
    public String billSign;
 	
 	/**
 	 * 获取加密ID
 	 */
 	public String getSign() {
 		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
 	}
 	
 	/**
 	 * 获取加密ID
 	 */
 	public String getBillSign() {
 		return Security.addSign(this.bill_id, Constants.BILL_ID_SIGN);
 	}

}