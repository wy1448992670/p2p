package models.file;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;

/** file和业务表的关系 */
@Entity
public class t_file_relation extends Model implements Comparable<t_file_relation>{
	private static final long serialVersionUID = 1L;
	
	/** t_file_dict_relation.relation_type */
	public int relation_type;
	
	/** 业务表外键 */
	public long relation_id;
	
	/** file.id */
	public long file_id;

	/**
	 * 由services.file.FileService封装
	 * @see  services.file.FileService
	 */
	@Transient
	public t_file file;
	
	/** 客户端,公开信息中是否显示 */
	public boolean is_visible;
	
	/** 这一类file_dict在这个业务主体中的顺序 */
	public int file_dict_sequence;
	
	/**
	 * 能否修改关系,(如果主体审核通过,且can_rewrite==false,则不能修改关系).\r\n如:实名通过后,用户表的身份证照片不能修改.\r\n如果用户表需要更新身份证,需要特殊流程,审核新的身份证照片后修改关系.
	 */
	public boolean can_rewrite;
	
	/** 是否是临时关系,主体还没生成,临时存放在用户下,正式关系确定时,直接修改关系 */
	public boolean is_temp;
	
	@Override
	public String toString() {
		return "t_file_relation [id=" + id + ", relation_type=" + relation_type + ", relation_id=" + relation_id + ", file_id="
				+ file_id + ", is_visible=" + is_visible + ", file_dict_sequence=" + file_dict_sequence
				+ ", can_rewrite=" + can_rewrite + ", is_temp=" + is_temp + "]";
	}
	
	@Override
	public int compareTo(t_file_relation file_relation) {
		//根据sequence升序排列，降序修改相减顺序即可
		return this.file_dict_sequence-file_relation.file_dict_sequence;
	}
	
}