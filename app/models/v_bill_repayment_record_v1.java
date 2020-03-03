package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;
import play.db.jpa.Model;
import utils.Security;

/**
 * 借款账单历史还款情况
 * @author Administrator
 *
 */
@Entity
public class v_bill_repayment_record_v1 extends Model {

	public long bid_id;
	public double current_pay_amount;
	public String title;
	public int overdue_mark;
	public int status;
	public Date real_repayment_time;
	public Date repayment_time;
	public double repayment_corpus;//应还本金
	public double repayment_interest;//应还利息
	public BigDecimal service_amount;//应收服务费
	public int  loan_periods;
	public int current_period;
	 
	public v_bill_repayment_record_v1(){
		
	}
	
	public v_bill_repayment_record_v1( double current_pay_amount, String title, int overdue_mark
			, int status, Date real_repayment_time, Date repayment_time, double repayment_corpus,double repayment_interest,int loan_periods,int current_period){
		this.title = title;
		this.current_pay_amount = current_pay_amount;
		this.overdue_mark = overdue_mark;
		this.status = status;
	    this.real_repayment_time = real_repayment_time;
		this.repayment_time = repayment_time;
		this.repayment_corpus = repayment_corpus;
		this.repayment_interest = repayment_interest;
		this.loan_periods = loan_periods;
		this.current_period = current_period;
	}
	
	
	
}
