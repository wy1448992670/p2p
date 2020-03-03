package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 合作机构
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:22:49
 */
@Entity
public class v_user_report_list extends Model {
	public Date time;
	public String name;
	public String mobile;
	public String reason;
	public long reported_user_id;
	public String bid_title;
	public String invest_transfer_title;
	
}
