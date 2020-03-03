package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 合作机构列表
 * @author bsr
 * @version 6.0
 * @created 2014-5-19 上午10:34:32
 */
@Entity
public class v_agencies extends Model {
	public String no;
	public String name;
	public Integer credit_level;
	public Integer bid_count;
	public Double bid_avg_apr;
	public Integer success_bid_count;
	public Integer overdue_bid_count;
	public Integer bad_bid_count;
	public Date time;
	public Integer cooperation_length;
	public Boolean is_use;
	
//	@Transient
//	public CreditLevel creditLevel;
//	/**
//	 * 信用积分
//	 */
//	public CreditLevel getCreditLevel() {
//		return CreditLevel.queryUserCreditLevel(this.id, new ErrorInfo());
//	}
}
