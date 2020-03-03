package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 部门账单管理--已还款账单列表
 */
@Entity
public class v_bill_department_haspayed extends Model {
    public long bid_id;
    public long user_id;
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
    public Date real_repayment_time;
    public String supervisor_name;
    public String supervisor_name2;
	
    @Transient
    public String overdue_time;
    
    @Transient
	public String sign;
    
    public v_bill_department_haspayed(){
		
	}
	
	public v_bill_department_haspayed(long bid_id, String bill_no, String name, String bid_no, double amount,
			double apr, String title, String period, Date repayment_time, String overdue_time, Date real_repayment_time
			,String supervisor_name){
		this.bid_id = bid_id;
		this.bill_no = bill_no;
		this.name = name;
		this.bid_no = bid_no;
		this.amount = amount;
		this.apr = apr;
		this.title = title;
		this.period = period;
		this.repayment_time = repayment_time;
		this.overdue_time = overdue_time;
		this.real_repayment_time = real_repayment_time;
		this.supervisor_name = supervisor_name;
	}
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}

	public String getOverdue_time() {
		if(null == this.repayment_time)
			return "";
			
		String timeStr = "";
		Date current = new Date();
		double distance = 0.0d;
		int day = 0;
		
		/*  */
		if (this.repayment_time.compareTo(current) >= 0) {
			timeStr = 0 + "";
		}else {
			/* */
			distance = current.getTime() - this.repayment_time.getTime();
			if (null == this.real_repayment_time) {
				day = (int) (distance / (24 * 60 * 60 * 1000));
				timeStr = day + "天";
			}else {
				/*  */
				if (this.real_repayment_time.compareTo(this.repayment_time) <= 0) {
					timeStr = 0 + "";
				/*  */
				}else {
					distance = real_repayment_time.getTime() - this.repayment_time.getTime();
					day = (int) (distance / (24 * 60 * 60 * 1000));
					timeStr = day + "天";
				}
			}
		}
			
		return timeStr;
	}
}