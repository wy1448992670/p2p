package models;

import javax.persistence.Entity;
import javax.persistence.Transient;
import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 应收账单管理--应收款借款账单统计表
 */
@Entity
public class v_bill_receviable_statistical extends Model {
    public int year;
    public int month;
    public long bill_accounts;
    public double amounts_receivable;
    public double bids_amount;
    public long bills_received;
    public double amount_received;
    public double bills_timely_completion_rate;
    public long overdue_counts;
    public double bills_overdue_rate;
    public double bills_completed_rate;
    public long bills_overdue_noreceive;
	public double uncollected_amount;
	public double uncollected_rate;

	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
}