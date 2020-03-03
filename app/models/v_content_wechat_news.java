package models;

import java.util.Date;

import play.db.jpa.Model;

/**
 * 微信的财富资讯
 * @author zhs
 * @date 2015-3-10 下午03:00:27
 */
public class v_content_wechat_news extends Model{

	public String title;
	public String content;
	public String image_filename;
	public Date time;
}
