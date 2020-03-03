package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 标产品
 * 
 * @author bsr
 * @version 6.0
 * @created 2014年4月4日 下午4:20:31
 */
@Entity
public class t_products extends Model {
	
	public Date time; // 时间
	public String name; // 名称
	public String name_image_filename;// 产品名称图片
	public String fit_crowd; // 适合人群
	public String characteristic; // 特点、亮点
	public double min_amount; // 借款金额下限
	public double max_amount; // 借款金额上限
	public double min_invest_amount; // 最低投标金额
	public double min_interest_rate; // 利率下限
	public double max_interest_rate; // 利率上限
	public long credit_id; // 最低信用积分
	public int max_copies; // 最高拆分份数
	public String period_year; // 年期限
	public String period_month; // 月期限
	public String period_day; // 日期限
	public String repayment_type_id; // 还款方式(1,2,3...)
	public String invest_period; // 投标期限(1,2,3...)
	public int audit_cycle; // 审核周期
	public String fee_description; // 手续费描述
	public String applicant_condition; // 申请条件
	public int loan_type; // 0.普通 1 信用 2 净值 3 秒还
	public double bail_scale; // 保证金百分比
	public boolean is_deal_password; // 是否需要交易密码
	public int loan_image_type; // 借款图片上传方式 1 用户上传 2 平台上传
	public String loan_image_filename; // 平台上传图片路径
	public String small_image_filename; // 借款小图标路径
	public boolean is_agency; // 是否是合作机构标
	public boolean is_use; // 上下架
	public int _order; // 排序
	public int show_type; // 发布方式 1.PC;2.APP;3.PC+APP
	public String mark; // 唯一标示
	
	public t_products() {

	}

	/**
	 * 我要借款,产品列表部分字段查询
	 * @param id 主键
	 * @param name 名称
	 * @param name_image_filename 标题图片
	 * @param small_image_filename 小图片
	 * @param min_amount 金额下限
	 * @param max_amount 金额上线
	 * @param fit_crowd 适合人群
	 * @param applicant_condition申请条件
	 */
	public t_products(long id, String name, String name_image_filename,
			String small_image_filename, double min_amount, double max_amount,
			String fit_crowd, String applicant_condition) {
		this.id = id;
		this.name = name;
		this.name_image_filename = name_image_filename;
		this.small_image_filename = small_image_filename;
		this.min_amount = min_amount;
		this.max_amount = max_amount;
		this.fit_crowd = fit_crowd;
		this.applicant_condition = applicant_condition;
	}

	/**
	 * 产品管理,产品列表部分字段查询
	 * @param id 主键
	 * @param name 名称
	 * @param time 时间
	 * @param credit_id 信用ID(最低信用积分图片)
	 * @param min_interest_rate 利率下限
	 * @param max_interest_rate 利率上限
	 * @param min_amount 金额下限
	 * @param max_amount 金额上限
	 * @param is_use 是否启用
	 * @param _order 排序
	 */
	public t_products(long id, String name, Date time, long credit_id,
			double min_interest_rate, double max_interest_rate,
			double min_amount, double max_amount, boolean is_agency,
			boolean is_use, int _order) {
		this.id = id;
		this.name = name;
		this.time = time;
		this.credit_id = credit_id;
		this.min_interest_rate = min_interest_rate;
		this.max_interest_rate = max_interest_rate;
		this.min_amount = min_amount;
		this.max_amount = max_amount;
		this.is_agency = is_agency;
		this.is_use = is_use;
		this._order = _order;
	}
	
	/**
	 * 查询ID,name,small_image_filename
	 * @param id ID
	 * @param name 名称
	 * @param small_image_filename 小图标
	 */
	public t_products(long id, String name, String small_image_filename) {
		this.id = id;
		this.name = name;
		this.small_image_filename = small_image_filename;
	}
	
	/**
	 * 查询id,name
	 * @param id ID
	 * @param name 名称
	 */
	public t_products(long id, String name){
		this.id = id;
		this.name = name;
	}
}
