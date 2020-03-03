package models;

import javax.persistence.Entity;
import play.db.jpa.Model;


/**
 * 理财风云榜
* @author lwh
* @version 6.0
* @created 2014年6月10日 下午8:29:18
 */

@Entity
public class v_bill_board extends Model{
	
	public String name;
	public Double corpus;
	public Double interest;
	public int bid_count;
}
