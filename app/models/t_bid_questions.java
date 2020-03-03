package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 标的提问
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:31:32
 */
@Entity
public class t_bid_questions extends Model {
	public long user_id;
	public long questioned_user_id;
	public Date time;
	public long bid_id;
	public String content;
	public boolean is_answer;
}
