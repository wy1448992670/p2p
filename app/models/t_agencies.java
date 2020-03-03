package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 合作机构
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:22:49
 */
@Entity
public class t_agencies extends EnumModel {
	public String name;
	public Date time;
	public long credit_level;
	public String introduction;
	public boolean is_use;
	public String id_number;
	public String imageFilenames;
	public String area;
	public Long supervisor_id;
	
	public t_agencies() {

	}

	/**
	 * 发布合作机构标,机构列表
	 * @param id ID
	 * @param name 名称
	 */
	public t_agencies(long id, String name) {
		this.id = id;
		this.name = name;
	}
}
