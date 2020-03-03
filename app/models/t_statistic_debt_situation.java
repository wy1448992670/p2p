package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 债权转让情况统计分析表
 * @author lwh
 *
 */
@Entity
public class t_statistic_debt_situation extends Model{
	public int year ;
	public int month ;
	public long debt_account ;
	public double debt_amount_sum ;
	public long increase_debt_account ;
	public double increase_debt_amount_sum ;
	public long has_overdue_debt ;
	public double overdue_percent ;
	public double average_debt_amount ;
	public long success_debt_amount ;
	public double deal_percent ;
	public double transfer_percent ;
}
