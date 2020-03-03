package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * 菜单实体类
 * @author fefrg
 *
 */
@Entity
public class t_wechat_menus extends Model {
	public String name;  //菜单名称
	public short type;  //菜单类型，为click(2),和view(1)
	public String key;  //菜单类型为click时才有key
	public String url;  //菜单类型为view才有url
	public long parent_id;  //父类菜单的id
}
