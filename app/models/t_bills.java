package models;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 账单
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:45:42
 */
@Entity
public class t_bills extends Model {
	public long bid_id;
	public String title;
	public Date repayment_time;
	public double repayment_corpus;
	public double repayment_interest;
	public int status;
	public String mer_bill_no;
	public String repayment_bill_no;
	public int periods;
	public Date real_repayment_time;
	public double real_repayment_corpus;
	public double real_repayment_interest;
	public int overdue_mark;
	public Date mark_overdue_time;
	public double overdue_fine;
	public Date mark_bad_time;
	public int notice_count_message;
	public int notice_count_mail;
	public int notice_count_telphone;
	@Transient
	public double current_pay_amount;
	
	// v7.2.6 add 
    public int ips_status;  //资金托管处理状态
	
    public double repayment_increase_interest;//加息利息-与借款人无关，方便纠偏
	public Double repayment_increase_interest1;
	public Double repayment_increase_interest2;
    public BigDecimal service_amount;//服务费
    public BigDecimal  real_service_amount;//实际付服务费
    
    public boolean is_payment_on_company;//是否公司垫付
    public boolean payment_on_company_need_repaid;//是否需要公司垫付后还款,true需要(未还),false不需要(已还)
    public Date payment_on_company_repaid_time;//公司垫付后还款时间
    public boolean is_offline;//是否线下收款
	public t_bills() {
		
	}
	
	/**
	 * 用于借款账单的历史还款情况
	 */
	public t_bills(String title, double real_repayment_corpus, double real_repayment_interest,
			double overdue_fine, int overdue_mark, int status, Date repayment_time,
			Date real_repayment_time) {
		
		this.title = title;
		this.current_pay_amount = real_repayment_corpus + real_repayment_interest + overdue_fine;
		this.overdue_mark = overdue_mark;
		this.status = status;
		this.repayment_time = repayment_time;
		this.real_repayment_time = real_repayment_time;
		
	}
	
	/**
	 * 用于系统标记逾期
	 */
	public t_bills(long id, long bid_id, int periods,
			double repayment_corpus, double repayment_interest) {
		
		this.id = id;
		this.bid_id = bid_id ;
		this.periods = periods;
		this.repayment_corpus = repayment_corpus;
		this.repayment_interest = repayment_interest;
	}
}
