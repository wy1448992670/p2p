package bean;

import java.util.Date;

/**
 * 企业认证业务Bean
 * 
 * @author HYL
 */
public class AuthReviewBean {
	// ID
	private long id;
	// 用户ID
	private long user_id;
	// 银行账户真实姓名
	private String real_name;
	// 企业名称
	private String company_name;
	// 统一社会信用代码
	private String credit_code;
	// 开户行名称
	private String bank_name;
	// 企业对公账户
	private String bank_no;
	// 状态 0：待审核 1：审核不通过 2：审核通过 3：已重置
	private int status;
	// 创建时间
	private Date create_time;
	// 更新时间
	private Date update_time;
	// 更新人
	private Long update_by;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUser_id() {
		return user_id;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public String getReal_name() {
		return real_name;
	}

	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getCredit_code() {
		return credit_code;
	}

	public void setCredit_code(String credit_code) {
		this.credit_code = credit_code;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_no() {
		return bank_no;
	}

	public void setBank_no(String bank_no) {
		this.bank_no = bank_no;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Date getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}

	public Long getUpdate_by() {
		return update_by;
	}

	public void setUpdate_by(Long update_by) {
		this.update_by = update_by;
	}
}
