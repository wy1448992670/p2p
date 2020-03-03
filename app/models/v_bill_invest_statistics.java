package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 理财子账户--理财情况统计
 * @author cp
 * @version 6.0
 * @created 2014年5月21日 下午4:33:10
 */
@Entity
public class v_bill_invest_statistics extends Model {

	public long user_id;
	public int year;
	public int month;
	public long invest_count;//投标数量
	public double invest_amount;
	public double average_loan_amount;
	public double average_invest_month;
	public double average_invest_amount;
	public double invest_fee_back;
	
}
