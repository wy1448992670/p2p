package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

/**
 * 举报会员
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class t_user_report_users extends Model {

	public long user_id;
	
	public Date time;
	
	public long reported_user_id;
	
	public long relation_bid_id;
	
	public long relation_invest_transfer_id;
	
	public String reason;
	
	public String situation;
	
	@Transient
	public String name; // 举报用户名
	
	public t_user_report_users() {

	}

	/**
	 * 举报某个会员列表
	 * @param id ID
	 * @param name 用户名
	 * @param reason 原因
	 * @param time 时间
	 * @param situation 处理情况
	 */
	public t_user_report_users(String name, String reason, Date time, String situation) {
		this.name = name;
		this.reason = reason;
		this.time = time;
		this.situation = situation;
	}
}
