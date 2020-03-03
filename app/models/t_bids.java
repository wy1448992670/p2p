package models;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.Model;
import utils.Security;
import business.BackstageSet;
import business.Bid;
import constants.Constants;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:32:02
 */
@Entity
public class t_bids extends Model {
	public long user_id;
	public Date time;
	public String bid_no;  //标的编号（资金托管使用）
	public String mer_bill_no;
	public String ips_bill_no;
	public long product_id;
	public String title;
	public long loan_purpose_id;
	public long repayment_type_id;
	public double amount;
	public int period;
	public double min_invest_amount;
	public double average_invest_amount;
	public int invest_period;
	public Date invest_expire_time;
	public Date real_invest_expire_time;
	public double apr;
	public long bank_account_id;
	public int bonus_type;
	public double bonus;
	public double award_scale;
	public String image_filename;
	public boolean is_quality;
	public boolean is_hot;
	public String description;
	public double bail;
	
	@Deprecated
	public double service_fees;
    /** 服务费 */
	public BigDecimal service_amount;

    /** 服务费率 */
	public BigDecimal service_cost_rate;

    /** 服务费计算规则:1.(总服务费=借款金额*服务费率) */
	public Integer service_cost_rule;

    /** 服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) */
	public Integer service_payment_model;
	
	/** 合作用户|机构 t_users.id */
	public Long consociation_user_id;
	
	/**  是否授权代付给合作人|机构 */
	public Boolean accredit_pay_for_consociation;
	
	public boolean is_agency;
	public int agency_id;
	public boolean is_show_agency_name;
	
	public int status;
	@Transient
	public String statusName;

	public double loan_schedule;
	public double has_invested_amount;
	public int read_count;
	public long allocation_supervisor_id;
	public long manage_supervisor_id;
	public Date audit_time;
	public String audit_suggest;
	public Date repayment_time;
	public Date last_repay_time;
	public boolean is_auditmatic_invest_bid;
	
	public int period_unit;
	public boolean is_sec_bid;
	public boolean is_deal_password;
	public int show_type;
	public String mark;
	public int version;
	public String qr_code;//二维码标识
	
	public double invest_rate; // 理财管理费，利息费率
	public double overdue_rate; // 逾期费率
	public boolean is_register_guarantor; // 是否已登记担保方
	public int client;
	
	//v7.2.6 add 
	public int ips_status;  //资金托管交易状态：0正常，1标的结束处理中【环迅】，2放款处理中【环迅】，3:保证金在第三方已解冻【汇付】
	
	public boolean is_show_member_bid;//是否显示在会员贷列表
	
	public boolean is_only_new_user;//是否仅限新手投资
	public String tag; //标签
	public boolean is_increase_rate;//是否加息
	public double increase_rate;//加息年化利率
	public String increase_rate_name;//加息名称
	
	public boolean is_debt_transfer;//是否债权转让
	
	public String repayment_source;//还款来源
	public String related_costs;//相关费用
	public Long bid_risk_id;
	@Transient
	public String[] image_title;//图片标题
	@Transient
	public String[] url;//图片路径
	@Transient
	public String reality_name;//借款人名字
	
	@Transient
	public String agencies_name;//机构名称
	@Transient
	public String purpose_name;//借款用途
	public Long borrow_apply_id;//借款标申请表主键
	
	public String consult_pact_location;//咨询管理服务协议
	public String consult_certificate_url;//咨询管理服务协议存证
	
	public String ymd_fq_fwxy_pact_location;//亿美贷（分期）服务费协议文件路径
	public String ymd_fq_fwxy_pact_certificate_url;//亿美贷（分期）服务费协议文件存证号
	
	public BigDecimal credit_amount;
	// v7.2.8 add 优化前台-借款子账户首页-理财秘书-审核提醒列表时添加  begin
	/**
	 * 标的号，借款标编号代码 + id
	 */
	@Transient
	public String bid_code;
	public String getBid_code(){
		return BackstageSet.getCurrentBackstageSet().loanNumber + this.id;
	}
	
	/**
	 * 所属产品的必选科目数
	 * 
	 */
	@Transient
	public long product_required_item_count = -1;  //默认值为-1，表示还未赋值,注意此属性存在缓存
	public long getProduct_required_item_count(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0L;
		}
		
		if(this.product_required_item_count >= 0){
			
			return this.product_required_item_count;
		}
		
		this.product_required_item_count = Bid.queryProductItemCount(this.mark, true);
		
		return this.product_required_item_count;
	}
	
	/**
	 * 借款人提交已通过的必审科目数
	 * v7.2.8 add 优化前台-借款子账户首页-理财秘书-审核提醒列表时添加
	 */
	@Transient
	public long user_item_count_true = -1L;  //默认值为-1，表示还未赋值,注意此属性存在缓存
	public long getUser_item_count_true(){
		if(StringUtils.isBlank(this.mark)){
			
			return 0L;
		}
		
		if(this.user_item_count_true >= 0){
			
			return this.user_item_count_true;
		}
		
		this.user_item_count_true = Bid.queryUserItemCount(this.mark, true, this.user_id, Constants.AUDITED);
		
		return this.user_item_count_true;
	}
	// v7.2.8 add 优化前台-借款子账户首页-理财秘书-审核提醒列表时添加  end

	
	public t_bids() {

	}

	/**
	 * 我要借款,时间最新的未满借款标 
	 * @param user_id 用户ID
	 * @param name 用户名称
	 * @param id 标ID
	 * @param time 时间
	 * @param amount 金额
	 * @param apr 利率
	 */
	public t_bids(long user_id, long id, Date time, double amount, double apr) {
		this.user_id = user_id;
		this.id = id;
		this.time = time;
		this.amount = amount;
		this.apr = apr;
	}

	/**
	 * 我要借款,最新5个满标
	 * @param id 标ID
	 * @param image_filename 借款图片
	 * @param amount 金额
	 */
	public t_bids(long id, String image_filename, double amount) {
		this.id = id;
		this.image_filename = image_filename;
		this.amount = amount;
	}

	/**
	 * 放款审核后续处理,部分查询项
	 * @param service_fees 服务费
	 * @param amount 金额
	 */
	public t_bids(double service_fees, double amount) {
		this.service_fees = service_fees;
		this.amount = amount;
		//this.bail = bail;
	}

	/**
	 * 放款审核后续处理,部分查询项
	 * @param bonus_type 奖励方式
	 * @param bonus 固定奖金
	 * @param award_scale 奖励百分比
	 */
	public t_bids(int bonus_type, double bonus, double award_scale) {
		this.bonus_type = bonus_type;
		this.bonus = bonus;
		this.award_scale = award_scale;
	}
	
	/**
	 * 账户满标倒计时提醒
	 * @param id 标ID
	 * @param amount 金额
	 * @param time 时间
	 * @param invest_expire_time 满标时间
	 */
	public t_bids(long id, double amount, Date time, Date invest_expire_time) {
		this.id = id;
		this.amount = amount;
		this.time = time;
		this.invest_expire_time = invest_expire_time;
	}
	
	/**
	 * 财务放款
	 * @param amount 金额
	 * @param apr 年利率
	 * @param period 期限
	 * @param repayment_type_id 还款方式
	 * @param service_fees 服务费
	 * @param bonus_type 奖励方式
	 * @param bonus 固定奖励
	 * @param award_scale 比例奖励
	 */
	public t_bids(double amount, double apr, int period, long repayment_type_id,
			double service_fees, int bonus_type, double bonus,
			double award_scale) {
		this.amount = amount;
		this.apr = apr;
		this.period = period;
		this.repayment_type_id = repayment_type_id;
		this.service_fees = service_fees;
		this.bonus_type = bonus_type;
		this.bonus = bonus;
		this.award_scale = award_scale;
	}
	
	/**
	 * 解除秒还标操作
	 * @param amount 金额
	 * @param apr 年利率
	 * @param period 期限
	 * @param service_fees 服务费
	 * @param bail 保证金
	 */
	public t_bids(double amount, double apr, int period, double service_fees,
			double bail) {
		this.amount = amount;
		this.apr = apr;
		this.period = period;
		this.service_fees = service_fees;
		this.bail = bail;
	}
	
	/* 辉哥  */
	@Transient
	public String no;
	
	/**
	 * 我的会员账单-借款会员管理
	 * @param id
	 * @param no
	 * @param title
	 * @param amount
	 * @param status
	 */
	public t_bids(long id,String no,String title,double amount,int status){
		this.id = id;
		this.no = no;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	/**
	 * 会员管理，根据用户ID查询数据
	 * @param id ID
	 * @param title 标题
	 * @param amount 金额
	 * @param status 状态
	 */
	public t_bids(long id, String title, double amount, int status) {
		this.id = id;
		this.title = title;
		this.amount = amount;
		this.status = status;
	}
	
	@Transient
	public String sign;
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		return Security.addSign(this.id, Constants.BILL_ID_SIGN);
	}
	
	/* 2014-11-14 */
	public String pact; // 借款合同
	public String intermediary_agreement; // 居间服务协议
	public String guarantee_bid; // 保障函
	
	public t_bids(String name, String reality_name, String purpose_name, int period, int product_id) {
		this.agencies_name = name;
		this.reality_name = reality_name;
		this.purpose_name = purpose_name;
		this.period = period;
		this.product_id = product_id;
	}
}
