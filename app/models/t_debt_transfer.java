package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.User;
import constants.AuditTypeEnum;
import constants.Constants;
import play.Logger;
import play.db.jpa.Model;
import utils.Arith;

@Entity
public class t_debt_transfer extends Model {
	public long invest_id;
	public long bid_id;
	public String no;
	public Date time;
	public Date accrual_time;
	public String title;
	public String transfer_reason;
	public double debt_amount;
	public long user_id;
	public double apr;
	public int period;
	public int bid_period;
	public int complete_period;
	public double remain_amount;
	// public Date apply_time;
	public int audit_status;
	public Date audit_time;
	public long audit_admin;
	public Date recheck_time;
	public long recheck_admin;
	public Date withdraw_time;
	public Long withdraw_admin;
	public boolean is_only_new_user;
	public double transfer_rate;
	public double red_amount;
	public double increase_rate;
	public Date deadline;
	public String reason;
	public int status;
	public double loan_schedule;
	public double has_invested_amount;
	public int repayment_type;
	public Date real_invest_expire_time;
	public int current_period;
	public double min_invest_amount;
@Transient
	public String reality_name;// 债权人真实姓名
	@Transient
	public String auditTypeName;// 审核状态名称
	@Transient
	public String applyTimeString;
	@Transient
	public Integer id_number;
	@Transient
	public String withdrawTimeStr;// 撤标时间
	
	
	public String getAuditTypeName() {
		return AuditTypeEnum.fromCode(audit_status).getName();
	}

	public void setAuditTypeName(String auditTypeName) {
		this.auditTypeName = auditTypeName;
	}

	public int getAudit_status() {
		return audit_status;
	}

	public void setAudit_status(int audit_status) {
		this.audit_status = audit_status;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getApplyTimeString() {
		return utils.DateUtil.dateToString(time);
	}
	public String getApplyAccrualTimeString() {
		return utils.DateUtil.dateToString(accrual_time);
	}
	public void setApplyTimeString(String applyTimeString) {
		this.applyTimeString = applyTimeString;
	}
	/**
	 * 返回债权转让服务费
	 * @return transfer_rate+red_amount+increase_rate
	 */
	public double getAllCommissions(){
		return Arith.add(Arith.add(transfer_rate,red_amount),increase_rate);
	}
	public enum Status{
		AUDITING	("AUDITING",	"待审核",1),
		FUNDRAISE	("FUNDRAISE",	"转让中",2),
		SUCCESS		("SUCCESS",		"成功的",3),			//成功的：包括：还款中、已还款
		NOT_THROUGH	("NOT_THROUGH",	"未通过",4),
		FLOW		("FLOW",		"失败的",5);			//失败的：即流标
		
		public String en, cn;
		public Integer code;
		Status(String en, String cn, Integer code){
			this.en = en;
			this.cn = cn;
			this.code = code;
		}
	}
	public enum AuditStatus{
		AUDITING			("AUDITING",			"未审核",		1),
		AUDIT_PASS			("AUDIT_PASS",			"初审通过",		2),
		AUDIT_NOT_PASS		("AUDIT_NOT_PASS",		"初审不通过",	3),			
		RECHECK_PASS		("RECHECK_PASS",		"复审通过",		4),
		RECHECK_NOT_PASS	("RECHECK_NOT_PASS",	"复审不通过",	5);					
		
		public String en, cn;
		public Integer code;
		AuditStatus(String en, String cn, Integer code){
			this.en = en;
			this.cn = cn;
			this.code = code;
		}
	}


	/**
	 * 返回标的状态的描述
	 * @return 标的状态的描述
	 */
	public String getStrStatus() {
		for (Status statusEach : Status.values()) {
			if(statusEach.code.equals(this.status)){
				return statusEach.cn;
			}
		}
		return "状态有误,谨慎操作!";
	}
	/**
	 * 返回标的状态的描述
	 * @return 标的状态的描述
	 */
	static public String getStrStatus(int status) {
		for (Status statusEach : Status.values()) {
			if(statusEach.code.equals(status)){
				return statusEach.cn;
			}
		}
		return "状态有误,谨慎操作!";
	}
	/**
	 * 返回标的审核状态的描述
	 * @return 标的审核状态的描述
	 */
	public String getStrAuditStatus() {
		for (AuditStatus auditStatusEach : AuditStatus.values()) {
			if(auditStatusEach.code.equals(this.audit_status)){
				return auditStatusEach.cn;
			}
		}
		return "状态有误,谨慎操作!";
	}
	/**
	 * 返回标的审核状态的描述
	 * @return 标的审核状态的描述
	 */
	static public String getStrAuditStatus(int audit_status) {
		for (AuditStatus auditStatusEach : AuditStatus.values()) {
			if(auditStatusEach.code.equals(audit_status)){
				return auditStatusEach.cn;
			}
		}
		return "状态有误,谨慎操作!";
	}
	

	public String getWithdrawTimeStr() {
		return utils.DateUtil.dateToString(this.withdraw_time);
	}

	public void setWithdrawTimeStr(String withdrawTimeStr) {
		this.withdrawTimeStr = withdrawTimeStr;
	}
}
