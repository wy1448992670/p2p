package models;

import java.util.Date;

import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jsoup.helper.StringUtil;

import play.db.jpa.Model;

@Entity
public class t_log extends Model{
	private static final long serialVersionUID = -8850848831364833171L;
	
	
	/**
	 * @see business.LogCore#getRelationType(String relation_type)
	 * 外键类型:
	 * select * from t_enum_map where enum_type=-1
	 * 1.t_bids 2.t_borrow_apply t_activity_increase_rate_detail.state 4.t_credit_apply 5.t_credit_increase_apply.status
	 */
	@javax.persistence.Access(AccessType.PROPERTY)
	public Integer relation_type;
	public Long relation_id;//外键id
	public Integer user_type;//1.t_users 2.t_supervisors 如果是job,存2
	public Long user_id;//操作用户id 如果是job,存1
	
	@javax.persistence.Access(AccessType.PROPERTY)
	public Integer status;
	@Transient
	public String status_name;
	
	public Date time;
	public String description_title;
	public String description;

	public String result;
	@Transient
	public String user_name;
	
	public t_log() {
	}
	
	public t_log(t_log log,String user_name,String supervisors_name) {
		super();
		this.id=log.id;
		this.setRelationType(log.relation_type);
		this.relation_id=log.relation_id;
		this.user_type=log.user_type;
		this.user_id=log.user_id;
		this.setStatus(log.status);
		this.time=log.time;
		this.description_title=log.description_title;
		this.description=log.description;
		
		this.setUser_name(user_name,supervisors_name);
	}
	
	public t_log(Long id
			,Integer relation_type
			,Long relation_id
			,Integer user_type
			,Long user_id
			,Integer status
			,Date time,String description_title
			,String description
			,String user_name,String supervisors_name) {
		super();
		this.id=id;
		this.setRelationType(relation_type);
		this.relation_id=relation_id;
		this.user_type=user_type;
		this.user_id=user_id;
		this.setStatus(status);
		this.time=time;
		this.description_title=description_title;
		this.description=description;
		
		this.setUser_name(user_name,supervisors_name);
	}
	
	public t_log setUser_name(String user_name,String supervisors_name) {
		if(this.user_type==1) {
			this.user_name=user_name;
		}else {
			if(StringUtil.isBlank(supervisors_name)) {
				this.user_name="SYSTEM";
			}else {
				this.user_name=supervisors_name;
			}
		}
		
		return this;
	}
	public t_log setT_log(t_log t_log) {
		return this;
	}
	
	public void setRelationType(Integer relation_type) {
		t_enum_map relationTypeMapKey=t_enum_map.getEnumCodeMapByTypeName("t_log.relation_type").get(relation_type);
		if(relationTypeMapKey==null){
			throw new IllegalArgumentException("关系类型不存在");
		}
		this.relation_type=relation_type;
		if(this.status != null){
			setStatusName();
		}
	}
	
	public void setStatus(Integer status) {
		this.status = status;
		if(this.relation_type != null){
			setStatusName();
		}
	}
	
	private void setStatusName() {
		t_enum_map relationTypeMapKey=t_enum_map.getEnumCodeMapByTypeName("t_log.relation_type").get(this.relation_type);
		t_enum_map enumObject=t_enum_map.getEnumCodeMapByTypeCode(Integer.parseInt(relationTypeMapKey.enum_name)).get(this.status);
		if(enumObject==null){
			throw new IllegalArgumentException("日志类型不存在");
		}
		this.status_name=enumObject.enum_name;
	}
}
