package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 广告
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:58:18
 */
@Entity
public class t_content_advertisements extends Model {
	public String no;
	public Date time;
	public String location;
	public String image_filename;
	public String resolution;
	public String file_size;
	public String file_format;
	public String url;
	public boolean is_link_enabled;
	public int target;
	public boolean is_use;
	
	public String tag, txt;
	public Integer is_top;
	
	public t_content_advertisements() {
		
	}
	
	public t_content_advertisements(long id, String image_filename, String url, boolean is_link_enabled,
			int target) {
		this.id = id;
		this.image_filename = image_filename;
		this.url = url;
		this.is_link_enabled = is_link_enabled;
		this.target = target;
	}
	
	public t_content_advertisements(long id, String image_filename, String url, boolean is_link_enabled,
			int target, String txt) {
		this.id = id;
		this.image_filename = image_filename;
		this.url = url;
		this.is_link_enabled = is_link_enabled;
		this.target = target;
		this.txt = txt;
	}
}
