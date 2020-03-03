package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 用户投标记录
* @author lwh
* @version 6.0
* @created 2014年4月24日 下午4:14:14
 */

@Entity
public class v_invest_records extends Model{
	
	public Date time;
	public String title;
	public String no;
	public Double invest_amount;
	public Double bid_amount;
	public Double apr;
	public Double increaseRate;
	public Double rate1;
	public Double rate2;
	public Integer status;
	public Integer transfer_status;
	public String name;
	public String bid_user_name;
	public Long bid_id;
	public Long user_id;
	public Integer question_count;
	public Integer answer_count;
	public String mobile;
}
