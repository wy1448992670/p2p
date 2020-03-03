package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 新闻
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午04:14:02
 */
@Entity
public class t_content_news extends Model {
	public Date time;
	public long type_id;
	public String title;
	public String author;
	public String content;
	public String keywords;
	public String descriptions;
	public int read_count;
//	public boolean is_homepage;
//	public boolean is_top;
//	public int is_marquee;
	
	/**
	 * 1 PC 
	 * 2 APP
	 * 3 PC和APP
	 */
	public int show_type;
	/**
	 * 0 不推荐
	 * homepage 1 推荐至首页
	 * marquee 2 荐跑马灯效果区
	 * top 3 推荐首页头条
	 */
	public int location_pc;
	public int location_app;
	public String image_filename;
	public String image_filename2;
	public boolean is_use;
	public Date start_show_time;
	public int _order;
	public int support;
	public int opposition;
	
	public t_content_news() {
		
	}
	
	/**
	 * 推荐至首页(含首页头条)
	 * @param id
	 * @param title
	 * @param image_filename
	 */
	public t_content_news(long id, String title, String image_filename, int _order) {
		this.id = id;
		this.title = title;
		this.image_filename = image_filename;
		this._order = _order;
	}
	
	public t_content_news(long id, String title, String content) {
		this.id = id;
		this.title = title;
		this.content = content;
	}
	
	public t_content_news(long id, String title, Date start_show_time, String image_filename) {
		this.id = id;
		this.title = title;
		this.start_show_time = start_show_time;
		this.image_filename = image_filename;
	}
	
	/**
	 * 跑马灯
	 * @param id
	 * @param title
	 */
	public t_content_news(long id, String title) {
		this.id = id;
		this.title = title;
	}
	
	public t_content_news(long id, String title,String content, int read_count,String image_filename, Date time) {
		this.id = id;
		this.title = title;
		this.read_count = read_count;
		this.content = content;
		this.image_filename = image_filename;
		this.time = time;
	}
	
	public t_content_news(long id, String title,String content, int read_count,String image_filename, Date time, Date start_show_time) {
		this.id = id;
		this.title = title;
		this.read_count = read_count;
		this.content = content;
		this.image_filename = image_filename;
		this.time = time;
		this.start_show_time=start_show_time;
	}
	
	public t_content_news(long id, Date time, String title, String content, int read_count) {
		this.id = id;
		this.time = time;
		this.title = title;
		this.content = content;
		this.read_count = read_count;
	}
	
	public t_content_news(long id, Date time, long type_id, String title, int location_pc,	int read_count,
			boolean is_use, Date start_show_time, int _order) {
		this.id = id;
		this.time = time;
		this.type_id = type_id;
		this.title = title;
		this.location_pc = location_pc;
		this.read_count = read_count;
		this.is_use = is_use;
		this.start_show_time = start_show_time;
		this._order = _order;
	}
}
