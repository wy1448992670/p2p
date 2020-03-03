package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 标的提问回答
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:24:42
 */
@Entity
public class t_bid_answers extends Model {
	public long bid_question_id;
	public Date time;
	public String content;
	public int read_count;
}
