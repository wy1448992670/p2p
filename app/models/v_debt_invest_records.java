package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 债权投标记录
 * @author wangyun
 * 上午11:57:58
 */

@Entity
public class v_debt_invest_records extends Model{
	public Date time;
	public String title;
	public String no;
	public Double invest_amount;
	public Double debt_amount;
	public Double apr;
	public Integer status; 
	public String name;  
	public Long user_id; 
	public String mobile;
	public Long bid_id;
}
