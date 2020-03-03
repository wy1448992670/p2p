package models.file;

import javax.persistence.Entity;

import models.EnumModel;

/** 同样类型的文件,在不同的主体中使用,规则不同时,请定义新的file_dict */
@Entity
public class t_file_dict extends EnumModel {
	private static final long serialVersionUID = 1L;
	
	public String name;
	
	/** 1,图片 2,PDF */
	public int type;
	
	/** 过期时间(秒),0只用一次,-1永不过期 */
	public long expire_time;
	
	/** 能否配置 1 能 2 否 */
	public boolean can_deploy;
	
	/** 是否敏感 1 是 2 否 */
	public boolean is_sensitive;
	
	public String description;

	@Override
	public String toString() {
		return "t_file_dict [id=" + id + ", name=" + name + ", type=" + type + ", expire_time=" + expire_time + ", can_deploy="
				+ can_deploy + ", is_sensitive=" + is_sensitive + ", description=" + description + "]";
	}

}