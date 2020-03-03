package models;



import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import constants.Constants;

import play.db.jpa.Model;
import utils.Security;

@Entity
public class t_forum_posts extends Model{
	
	public long user_id;
	public String title;
	public String content;
	public String keywords;
	
	public Date add_time;
	public Date show_time;
	public int read_count;
	public int answers_count;
	public long type_id;
	
	public int status;
	public int show_image;
	public String name;
	
	public int rId;
	
	@Transient
	public String typeName;
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		if(null == this.sign)
			this.sign = Security.addSign(this.user_id, Constants.USER_ID_SIGN);
		
		return this.sign;
	}
}
