package models.core;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Transient;
import play.db.jpa.Model;

@Entity
public class t_org_project extends Model {
	private static final long serialVersionUID = 1L;

	/** 机构id */
	public long org_id;

	/** 父类目id,一级类目id为-1 */
	public int p_id;
	@Transient
	transient public t_org_project parentProject;
	@Transient
	public List<t_org_project> childProjectList;

	/** 类目名称 */
	public String name;

	/** 项目是否使用 */
	public boolean is_use;

	@Override
	public String toString() {
		return "t_org_project [id=" + id + ", org_id=" + org_id + ", p_id=" + p_id + ", name=" + name + ", is_use="
				+ is_use + "]";
	}

}
