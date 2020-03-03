package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * @author chenqiao
 *	认证接口请求记录表
 */
@Entity
public class t_auth_req extends Model {
	public String refID;
	public Long uid;
	public Integer type;
	public Date insert_time;
}
