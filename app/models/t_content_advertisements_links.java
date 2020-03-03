package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 底部连接
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:53:22
 */
@Entity
public class t_content_advertisements_links extends Model {
	public Date time;
	public String _key;
	public String title;
	public String url;
	public int target;
	public int _order;
	public int status;
	
	public t_content_advertisements_links() {
		
	}
	
	public t_content_advertisements_links(String _key, String title, String url, int target) {
		this._key = _key;
		this.title = title;
		this.url = url;
		this.target = target;
	}
}
