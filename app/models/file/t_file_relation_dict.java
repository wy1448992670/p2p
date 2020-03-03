package models.file;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.EnumModel;

/** file_dict与业务表的关系 */
@Entity
public class t_file_relation_dict extends EnumModel implements Comparable<t_file_relation_dict> {
	private static final long serialVersionUID = 1L;

	/**
	 * 1.users身份证照片实名认证 2.users活体认证 3.users用户风控资料 4.credit_apply
	 * 5.increase_credit_apply 6.borrow_apply 7.users.user_type in (2,3)企业用户资料
	 * 8.orgnization合作机构资料 9.credit_apply第三方报告
	 */
	public int relation_type;

	/** file_dict.id */
	@Access(AccessType.PROPERTY)
	public Integer file_dict_id;
	@Transient
	public t_file_dict file_dict;
	@Transient
	public String file_dict_name;
	
	/**
	 * 由services.file.FileService封装
	 * @see  services.file.FileService
	 */
	@Transient
	public List<t_file_relation> file_relation_list;
	
	
	/** 至少文件数量,0不必填 */
	public int required_num;
	
	/** 最多文件数量,-1不限数量 */
	public int max_num;

	/** 客户端,公开信息中是否显示 */
	public boolean is_visible;

	/** file_dict在业务表中的顺序 */
	public int sequence;

	/**
	 * 决定了file_relation.can_rewrite
	 * 能否修改file_id,(如果主体审核通过,且can_rewrite==false,则不能修改关系) 如:实名通过后,用户表的身份证照片不能修改
	 * 如果用户表需要更新身份证,需要特殊流程,审核新的身份证照片后修改关系
	 */
	public boolean can_rewrite;

	public void setFile_dict_id(Integer file_dict_id) {
		this.file_dict = new t_file_dict().getEnumById(file_dict_id.longValue());
		if (this.file_dict == null) {
			throw new IllegalArgumentException("错误的file_dict！");
		}
		this.file_dict_name = this.file_dict.name;
		this.file_dict_id = file_dict_id;
	}

	@Override
	public String toString() {
		return "t_file_relation_dict [id=" + id + ", relation_type=" + relation_type + ", file_dict_id=" + file_dict_id
				+ ", required_num=" + required_num + ", max_num=" + max_num + ", is_visible=" + is_visible
				+ ", sequence=" + sequence + ", can_rewrite=" + can_rewrite + "]";
	}
	
	@Override
	public int compareTo(t_file_relation_dict frd) {
		//根据sequence升序排列，降序修改相减顺序即可
		return this.sequence-frd.sequence;
	}
}