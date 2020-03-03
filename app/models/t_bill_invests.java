package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 理财账单
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:42:20
 */
@Entity
public class t_bill_invests extends Model {
	public long user_id;
	public long bid_id;
	public long invest_id;
	public String mer_bill_no;
	public String title;
	public Date receive_time;
	public double receive_corpus;
	public double overdue_fine;
	public double receive_interest;
	public int status;
	public int periods;
	public Date real_receive_time;
	public double real_receive_corpus;
	public double real_receive_interest;
	
	public double receive_increase_interest;//应收加息利息
	public Double receive_increase_interest1;
	public Double receive_increase_interest2;


	public double real_increase_interest;//实际收款加息利息
//	public Double real_increase_interest1;
//	public Double real_increase_interest2;
	
	@Transient
	public double receive_amount;
	@Transient
	public String dxreceive_amount;//大写金额
	
	public t_bill_invests(){
		
	}
	
	public t_bill_invests(long id,String title, double receive_amount, int status, Date receive_time
			, Date real_receive_time){
		this.id = id;
		this.title = title;
		this.receive_amount = receive_amount;
		this.status = status;
		this.receive_time = receive_time;
		this.real_receive_time = real_receive_time;
	}
	
	public t_bill_invests(long id,String title, int status, Date receive_time,double receive_amount,Date real_receive_time){
		this.id = id;
		this.title = title;
		this.status = status;
		this.receive_time = receive_time;
		this.receive_amount = receive_amount;
		this.real_receive_time = real_receive_time;
	}
	
	public t_bill_invests(long id, long user_id, long bid_id, String title, int status, Date real_receive_time, double real_receive_corpus, double real_receive_interest){
		this.id = id;
		this.user_id = user_id;
		this.bid_id = bid_id;
		this.title = title;
		this.status = status;
		this.real_receive_time = real_receive_time;
		this.real_receive_corpus = real_receive_corpus;
		this.real_receive_interest = real_receive_interest;
	}
}
