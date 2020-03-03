package models;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
import utils.DateUtil;

/**
 * <p>
 * 会员通讯录用户表
 * </p>
 *
 * @author 张琼麒
 * @since 2018-11-21
 */
@Entity
public class t_user_address_list extends Model {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2894114229139028025L;
	/**
	 * 会员列表主键
	 */
	public Long user_id;
	/**
	 * 姓名
	 */
	public String name;
	/**
	 * 手机号码
	 */
	public String mobile;
	/**
	 * 创建时间
	 */
	@Access(AccessType.PROPERTY)
	public Date create_time;
	@Transient
	public String create_time_str;
	
	public void setCreate_time(Date create_time) {
		this.create_time_str=(DateUtil.dateToString(create_time));
		this.create_time = create_time;
	}
	
	/**
	 * 更新时间
	 */
	@Access(AccessType.PROPERTY)
	private Date update_time;
	@Transient
	public String update_time_str;

	public Date getUpdate_time() {
		return null;
	}
	public Date achieveUpdate_time() {
		return this.update_time;
	}
	public void setUpdate_time(Date update_time) {
		this.update_time_str=(DateUtil.dateToString(update_time));
		this.update_time = update_time;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(mobile, name, user_id);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//该对象规则:同一用户,同一联系人,同一电话号码只保存一条数据,所以不比较key,比较内容
		//if (!super.equals(obj)) return false;
		if (!(obj instanceof t_user_address_list))
			return false;
		t_user_address_list other = (t_user_address_list) obj;
		return possibilitiesEquals(id, other.id) && Objects.equals(user_id, other.user_id)
				&& Objects.equals(name, other.name) && Objects.equals(mobile, other.mobile);
	}
	
	private static boolean possibilitiesEquals(Object a, Object b) {
		if(a==null)
			return true;
		if(b==null)
			return true;
		return Objects.equals(a,b);
	}
	
	@Override
	public String toString() {
		return "t_user_address_list [user_id=" + user_id + ", name=" + name + ", mobile=" + mobile + ", id=" + id + "]";
	}
	
}
