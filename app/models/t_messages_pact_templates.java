package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import play.db.jpa.GenericModel;

/**
 * 平台协议模板
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-11-14 下午01:55:45
 */
@Entity
public class t_messages_pact_templates extends GenericModel {
	@Id
	public int id;
	public Date time;
	public String title;
	public String content;
	public boolean is_use;
}
