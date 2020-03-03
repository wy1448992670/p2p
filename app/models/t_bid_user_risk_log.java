package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:32:02
 */
@Entity
public class t_bid_user_risk_log extends Model {
	private static final long serialVersionUID = -394500667079590439L;
	public Long bid_risk_id;
	public Long user_risk_id;
	public BigDecimal quota;
	public Long supervisor_id;
	public Date mtime;
}
