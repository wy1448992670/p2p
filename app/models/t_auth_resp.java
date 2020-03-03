package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * @author chenqiao
 *	认证接口请求响应记录表
 */
@Entity
public class t_auth_resp extends Model {
	public String refID;
	public Long status;
	public String errorCode;
	public String errorMsg;
	public Date insert_time;
}
