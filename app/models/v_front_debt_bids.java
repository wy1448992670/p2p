package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_front_debt_bids extends Model{
	public String no;
	public String debt_user_id;//申请债权用户ID
	public String title;
	public long invest_id;
	public double apr;
	public int period;//转让期限
	public int bid_period;//标的期限
	public double amount;//债权金额
	public String repayment_type;//还款方式
	public boolean is_only_new_user;//是否新手标
	public double loan_schedule;//借款进度比例(冗余)
	public double has_invested_amount;//已投总额(冗余)
	public int period_unit;
	public Date deadline;
	public int debtCount;
	public int status;
}
