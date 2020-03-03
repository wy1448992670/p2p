package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 4大安全保障
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:50:34
 */
@Entity
public class t_content_advertisements_ensure extends Model {
	public String no;
	public Date time;
	public String title;
	public String location;
	public String image_filename;
	public String resolution;
	public String file_size;
	public String file_format;
	public String url;
	public boolean is_link_enabled;
	public int target;
	public boolean is_use;
	
	public t_content_advertisements_ensure() {
		
	}
	
	public t_content_advertisements_ensure(long id, String title,  
			String url,  String image_filename,boolean is_link_enabled, int target) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.image_filename = image_filename;
		this.is_link_enabled = is_link_enabled;
		this.target = target;
	}
}
