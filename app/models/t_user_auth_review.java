package models;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_user_auth_review extends Model {
	private static final long serialVersionUID = 1L;
	
	public long user_id;//用户ID
	public String real_name;//银行账户真实姓名
	public String company_name;//企业名称
	public String credit_code;//统一社会信用代码
	public String bank_name;//开户行名称
	public String bank_no;//企业对公账户
	public int status; //状态  0：待审核   1：审核不通过  2：审核通过  3：已重置
	public Date create_time;//创建时间
	public Date update_time;//更新时间
	public Long update_by;//更新人
}
