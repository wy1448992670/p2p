package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 
 * @author 2015年3月25日20:31:34
 * 
 */
@SuppressWarnings("serial")
@Entity
public class t_return_data extends Model {

	public String mmmUserId;
	public String orderNum;
	public String parent_orderNum;
	public Date op_time;
	public String type;
	public String data;
}
