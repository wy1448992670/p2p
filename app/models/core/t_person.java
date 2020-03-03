package models.core;

import java.io.Serializable;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_person extends Model {
	private static final long serialVersionUID = 1L;

	/** 姓名 */
	public String name;

	/** 身份证 */
	public String id_card;

	/** 电话 */
	public String phone;

	/** 邮箱 */
	public String email;

	@Override
	public String toString() {
		return "t_person [id=" + id + ", name=" + name + ", id_card=" + id_card + ", phone=" + phone + ", email=" + email + "]";
	}

	
}
