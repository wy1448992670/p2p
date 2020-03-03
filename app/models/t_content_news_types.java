package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 新闻类型
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:12:34
 */
@Entity
public class t_content_news_types extends Model {
	public long parent_id;
	public String name;
	public String description;
	public int _order;
	public boolean status;
	
	public t_content_news_types() {
		
	}
	
	public t_content_news_types(long id, String name) {
		this.id = id;
		this.name = name;
	}
}
