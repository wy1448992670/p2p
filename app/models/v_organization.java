package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author Sxy
 *	2018-12-24
 */
@Entity
public class v_organization extends Model {
	
	public String org_name;
	public String contacts_name;
	public String contacts_phone;
	public String bd_name;
	public Boolean is_use;
	public String face_photo;
	public long user_id;

}
