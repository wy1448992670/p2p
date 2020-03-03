package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 合作伙伴
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:55:12
 */
@Entity
public class t_content_advertisements_partner extends Model {
	public String no;
	public String name;
	public Date time;
	public String location;
	public String image_filename;
	public String resolution;
	public String file_size;
	public String file_format;
	public String url;
	public String description;
	public int _order;
	
	public t_content_advertisements_partner() {
		
	}
	
	public t_content_advertisements_partner(long id, String name, 
			String image_filename, String url) {
		this.id = id;
		this.name = name;
		this.image_filename = image_filename;
		this.url = url;
	}
}
