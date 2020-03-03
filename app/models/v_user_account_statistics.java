package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 账户统计(用于账户总览--温馨提示)
 * @author cp
 * @version 6.0
 * @created 2014年5月14日 上午8:55:56
 */
@Entity
public class v_user_account_statistics extends Model {

	public int auditing_count;
	public int repaymenting_count;
	public int untreated_bills_count;
	public int untreated_invest_bills_count;
	public int overdue_bills_count;
	public int receivable_invest_bids_count;
	public int lack_audit_item_count;
	
}
