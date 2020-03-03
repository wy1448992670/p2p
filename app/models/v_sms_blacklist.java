package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
/**
 * 短信黑名单模板
 * @author hyl
 * @version 6.0
 * @created 2018年8月22日
 */
@Entity
public class v_sms_blacklist extends Model {
	//用户昵称
	public String name;
	//真实姓名
	public String reality_name;
	//手机号码
	public String mobile;
}
