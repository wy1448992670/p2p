package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import com.shove.Convert;

import business.Bid.Repayment;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.Security;
import constants.Constants;
import constants.SupervisorEvent;
import models.t_products;

/**
 * 标产品
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-26 下午07:40:21
 */
public class Product implements Serializable{
	
	public long id; 
	private long _id;
	public String sign;
	public boolean bidDetail;
	public boolean createBid;
	
	public Date time; // 时间
	public String name; // 名称
	public String fitCrowd; // 适合人群
	public String characteristic; // 特点、亮点
	public double minAmount; // 借款金额下限
	public double maxAmount; // 借款金额上限
	public double minInvestAmount; // 最低投标金额
	public int maxCopies; // 最高拆分份数
	public int auditCycle; // 审核周期
	//public String feeDescription; // 手续费描述
	public String applicantCondition; // 申请条件
	public int loanType; //0.普通; 1.信用; 2.净值; 3.秒还
	public String strLoanType; // 0.普通; 1.信用; 2.净值; 3.秒还
	public double bailScale; // 保证金百分比
	public boolean isDealPassword; // 是否需要交易密码
	public boolean isAgency; // 产品模式
	public boolean isUse; // 上下架
	public int order; // 排序
	public int showType; // 发布方式 1.PC;2.APP;3.PC+APP
	public int loanImageType; // 借款图片上传方式 0 用户上传 1 平台上传
	public String nameImageFilename;// 产品名称图片
	public String loanImageFilename; // 平台上传图片
	public String smallImageFilename; // 借款小图标

	public String periodYear; // 年期限单位
	public String [] periodYearArray; // 单个字符组装成的年期限单位
	public String periodMonth; // 月期限单位
	public String [] periodMonthArray; // 单个字符组装成的年期限单位
	public String periodDay; // 日期限单位
	public String [] periodDayArray; // 单个字符组装成的年期限单位
	public String investPeriod; // 投标期限
	public String[] investPeriodArray; // 单个字符组装成的投标期限
	
	public String repaymentId;
	public String [] repaymentTypeId;
	public List<Repayment> repaymentType; // 还款方式集合
	
	public double minInterestRate; // 利率下限
	public double maxInterestRate; // 利率上限
	public double monthMinApr; // 月利率下限
	public double monthMaxApr; // 月利率上限
	
	public List<ProductAuditItem> requiredAuditItem; // 必须审核资料对象集合
	public List<ProductAuditItem> selectAuditItem; // 可选审核资料对象集合 
	public String[] requiredAuditId; // 必须审核资料ID 
	public String[] selectAuditId; // 可选审核资料ID
	public int auditCount;// 审核资料数量

	public List<ProductLable> lables; // 产品标签集合
	public ProductLable [] lablesArray;
	public String jsonLables; // 标签json字符串
	
	public long creditId; // 信用积分ID
	public String creditImageFilename; // 信用积分图标
	
	public String mark; // 唯一标示
	
	/**
	 * 获取加密ID
	 */
	public String getSign() {
		if(null == this.sign) 
			this.sign = Security.addSign(this.id, Constants.PRODUCT_ID_SIGN);
		
		return this.sign;
	}

	/**
	 * 获取借款模式
	 */
	public String getStrLoanType() {
		if(null == this.strLoanType) {
			switch (this.loanType) {
				case Constants.GENERAL_BID: this.strLoanType = "普通"; break;
				case Constants.CREDIT_BID: this.strLoanType = "信用"; break;
				case Constants.NET_VALUE_BID: this.strLoanType = "净值"; break;
				case Constants.S_REPAYMENT_BID: this.strLoanType = "秒还"; break;
			}
		}
		
		return this.strLoanType;
	}

	/**
	 * 填充自己
	 */
	public void setId(long id) {
		t_products product = null;

		try {
			product = t_products.findById(id);
		} catch (Exception e) {
			Logger.error("产品->填充自己:" + e.getMessage());
			this._id = -1;
			
			return;
		}
		
		if (null == product) {
			this._id = -1;
			
			return;
		}
		
		this._id = product.id;
		this.mark = product.mark;

		if(this.createBid){
			this.isUse = product.is_use;
			if(!this.isUse) return;
			this.isAgency = product.is_agency;
			this.name = product.name;
			this.loanImageFilename = product.loan_image_filename;
			this.bailScale = product.bail_scale;
			this.maxCopies = product.max_copies; 
			this.minInvestAmount = product.min_invest_amount;
			this.minInterestRate = product.min_interest_rate; 
			this.maxInterestRate = product.max_interest_rate; 
			this.minAmount = product.min_amount; 
			this.maxAmount = product.max_amount;
			this.loanType = product.loan_type;
			this.isDealPassword = product.is_deal_password;
			this.repaymentId = product.repayment_type_id;
			this.periodYear = product.period_year;
			this.periodMonth = product.period_month;
			this.periodDay = product.period_day;
			this.investPeriod = product.invest_period;
			this.loanImageType = product.loan_image_type;
			this.creditId = product.credit_id;
			this.showType = product.show_type;
			
			return;
		}
		
		if(this.bidDetail) {
			this.name = product.name;
			this.smallImageFilename = product.small_image_filename;
			
			return;
		}
		
		this.time = product.time;
		this.name = product.name;
		this.nameImageFilename = product.name_image_filename;
		this.fitCrowd = product.fit_crowd;
		this.characteristic = product.characteristic;
		this.minAmount = product.min_amount;
		this.maxAmount = product.max_amount;
		this.minInvestAmount = product.min_invest_amount;
		this.maxCopies = product.max_copies;
		this.creditId = product.credit_id;
		this.auditCycle = product.audit_cycle;
		this.applicantCondition = product.applicant_condition;
		this.loanType = product.loan_type;
		this.bailScale = product.bail_scale;
		this.isDealPassword = product.is_deal_password;
		this.loanImageType = product.loan_image_type;
		this.loanImageFilename = product.loan_image_filename;
		this.smallImageFilename = product.small_image_filename;
		this.isAgency = product.is_agency;
		this.isUse = product.is_use;
		this.order = product._order;
		this.showType = product.show_type;
		this.repaymentId = product.repayment_type_id;
		
		/* 年借款期限 */
		this.periodYear = product.period_year;
		
		/* 月借款期限 */
		this.periodMonth = product.period_month;
		
		/* 日借款期限 */
		this.periodDay = product.period_day; 
		
		/* 投标期限 */
		this.investPeriod = product.invest_period;
		
		/* 年利率 */
		double minApr = product.min_interest_rate;
		double maxApr = product.max_interest_rate;
		this.minInterestRate = minApr;
		this.maxInterestRate = maxApr;
		this.monthMinApr = minApr > 0 ? Arith.div(minApr, 12, 2) : 0;
		this.monthMaxApr = maxApr > 0 ? Arith.div(maxApr, 12, 2) : 0;
	}

	/**
	 * 获取ID
	 */
	public long getId() {
		return this._id;
	}
	
	/**
	 * 年期限集合
	 */
	public String[] getPeriodYearArray() {
		if(null == this.periodYear || this.periodYear.length() < 1)
			return new String[]{"1"};
		
		if(null == this.periodYearArray) 
			this.periodYearArray = this.periodYear.split(",");
		
		return this.periodYearArray;
	}

	/**
	 * 月期限集合
	 */
	public String[] getPeriodMonthArray() {
		if(null == this.periodMonth || this.periodMonth.length() < 1)
			return new String[]{"1"};
		
		if(null == this.periodMonthArray)
			this.periodMonthArray = this.periodMonth.split(",");
		
		return this.periodMonthArray;
	}

	/**
	 * 日期限集合
	 */
	public String[] getPeriodDayArray() {
		if(null == this.periodDay || this.periodDay.length() < 1)
			return new String[]{"1"};
		
		if(null == this.periodDayArray)
			this.periodDayArray = this.periodDay.split(",");
		
		return this.periodDayArray;
	}

	/**
	 * 投标期限集合
	 */
	public String[] getInvestPeriodArray() {
		if(null == this.investPeriod || this.investPeriod.length() < 1)
			return new String[]{"1"};
		
		if(null == this.investPeriodArray)
			this.investPeriodArray = this.investPeriod.split(",");
		
		return this.investPeriodArray;
	}

	/**
	 * 我要借款,产品列表
	 * @param isAgency 是否合作机构标
	 * @param error 信息值
	 * @return List<Product>
	 */
	public static List<Product> queryProduct(int showType, ErrorInfo error) {
		error.clear();
		int type = 0;
		
		if(showType == Constants.PC ){
			type = 1;
		}
		
		if(showType == Constants.APP ){
			type = 2;
		}
		
		if(showType == Constants.WECHAT ){
			type = 4;
		}
		List<Product> poducts = new ArrayList<Product>();
		
		String hql = "select " +
		 "id, name,time, name_image_filename, small_image_filename, min_amount, max_amount, fit_crowd, applicant_condition, loan_type"+
		 " from t_products where is_use=? and is_agency=? and (?&show_type) <> 0  order by _order, time desc";
		
		List<Map<String, Object>> tpoducts = JPAUtil.getList(error, hql, Constants.ENABLE, Constants.NOT_IS_AGENCY, type);

		if(null == tpoducts) 
			return poducts;
		
		Product product = null;
		int size = tpoducts.size();

		for (int i=0;i<size;i++) {
			Map<String, Object> pro = tpoducts.get(i);
			product = new Product();
			
			product._id = Long.parseLong(""+pro.get("id"));
			product.name = ""+pro.get("name");
			product.nameImageFilename = ""+pro.get("name_image_filename");
			product.smallImageFilename = ""+pro.get("small_image_filename");
			product.time = (Date)pro.get("time");
			product.minAmount = Double.parseDouble(""+pro.get("min_amount"));
			product.maxAmount = Double.parseDouble(""+pro.get("max_amount"));
			product.fitCrowd = ""+pro.get("fit_crowd");
			product.applicantCondition = (""+pro.get("applicant_condition")).length() > 75
										? (""+pro.get("applicant_condition")).substring(0, 75)
										: ""+pro.get("applicant_condition");
			poducts.add(product);
		}
		
		error.code = 0;
		
		return poducts;
	}

	/**
	 * 产品管理,产品列表
	 * @param error 产品名称
	 * @param error 信息值
	 * @return List<Product>
	 */
	public static List<Product> queryProduct(PageBean<Product> pageBean, String name, ErrorInfo error) {
		error.clear();
		
		int count = 0;
		String condition = "1=1";
		
		if(StringUtils.isNotBlank(name)){
			condition += name == null ? "" : " AND name LIKE '%" + name + "%'";
			
			pageBean.conditions = new HashMap<String, Object>();
			pageBean.conditions.put("name", name);
		}
		
		try {
			/* 得到总记录数 */
			count = (int) t_products.count(condition);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("标->产品管理,产品列表,查询总记录数:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "产品列表加载失败!";

			return null;
		}

		List<Product> products = new ArrayList<Product>();

		if (count < 0)
			return products;
		
		pageBean.totalCount = count;
		List<t_products> tproducts = null;
		String hql = "select new t_products" +
				     "(id, name, time, credit_id, min_interest_rate, max_interest_rate, min_amount, max_amount, is_agency, is_use, _order)" +
					 " from t_products where " + condition + " order by _order, time" ;

		try {
			tproducts = t_products.find(hql).fetch(pageBean.currPage, pageBean.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->管理,产品列表部分字段查询:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "产品列表加载失败!";
			
			return null;
		}
		
		if(null == tproducts) 
			return products;
		
		Product product = null;
		String _creditImageFilename =null;
		long _creditId = -1;
		long _pid = -1;
	
		for (t_products pro : tproducts) {
			product = new Product();
			
			_creditId = pro.credit_id;
			/* 最低信用积分图标 */
			_creditImageFilename = CreditLevel.queryImageFilename(_creditId, error);

			_pid = pro.id;
			// 审核资料数量
			count = ProductAuditItem.queryAuditCount(_pid, error);
			
			product.name = pro.name;
			product.time = pro.time;
			product.minInterestRate = pro.min_interest_rate;
			product.maxInterestRate = pro.max_interest_rate;
			product.minAmount = pro.min_amount;
			product.maxAmount = pro.max_amount;
			product.isAgency = pro.is_agency;
			product.isUse = pro.is_use;
			product.order = pro._order;
			product._id = _pid;
			product.creditId = _creditId;
			product.auditCount = count;
			product.creditImageFilename = _creditImageFilename;
			
			products.add(product);
		}
		
		error.code = 0;
		
		return products;
	}

	/**
	 * 查询机构产品
	 * @param error 信息值
	 * @return Product
	 */
	public static Product queryAgencyProduct(ErrorInfo error){
		error.clear();
		
		String hql = "select id from t_products where is_agency = ?";
		Long id = null;
		
		try {
			id = t_products.find(hql, Constants.ENABLE).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->查询机构产品:" + e.getMessage());
			error.msg = error.FRIEND_INFO + ">查询机构产品失败!";
			
			return null;
		}
		
		if(null == id || id == 0){
			error.msg = "没有检测到合作机构产品!";
			
			return null;
		}
		
		Product product = new Product();
		product.createBid = true;
		product.id = id;
		
		return product;
	}
	
	/**
	 * 检查用户是否存在
	 * @param name 名称
	 * @return true : 存在,false : 不存在
	 */
	public static boolean checkName(String name, long id){
		String hql = "select name from t_products where name = ?";

		if(id > 0)
			hql += " and id <> " + id;
		
		try {
			name = t_products.find(hql, name.trim()).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->根据name查询name:" + e.getMessage());
			
			return true;
		}
		
		if(null == name) return false;
		
		return true;
	}

	/**
	 * 填充必须产品审计的资料
	 */
	public List<ProductAuditItem> getRequiredAuditItem() {
		if (null == this.requiredAuditItem) 
			this.requiredAuditItem = ProductAuditItem.queryAuditByProductMark(this.mark, false, Constants.NEED_AUDIT);
		
		return this.requiredAuditItem;
	}

	/**
	 * 填充可选产品审计的资料
	 */
	public List<ProductAuditItem> getSelectAuditItem() {
		if (null == this.selectAuditItem) 
			this.selectAuditItem = ProductAuditItem.queryAuditByProductMark(this.mark, false, Constants.NOT_NEED_AUDIT);
		
		return this.selectAuditItem;
	}
	
	/**
	 * 填充还款类型
	 */
	public List<Repayment> getRepaymentType() {
		if (null == this.repaymentType) {
			String num [] = null;
			
			try {
				/* 拆分字符串 */
				num = this.repaymentId.split(",");
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("产品->填充还款类型:" + e.getMessage());
				
				return null;
			}
			
			this.repaymentType = Repayment.queryRepaymentType(num, new ErrorInfo());
		}
		
		return this.repaymentType;
	}

	/**
	 * 拆分成"1,2,..."形式的字符
	 */
	private String splitRepaymentType(String[] typeId) {
		String type = "";

		for (String str : typeId) {
			type += str.concat(",");
		}
		
		return type.substring(0, type.length() - 1);
	}
	
	/**
	 * 填充产品标签
	 * @return
	 */
	public List<ProductLable> getLables() {
		if(null == this.lables) 
			this.lables = ProductLable.queryProductLableByProductId(this.id);
		
		return this.lables;
	}

	/**
	 * 获取产品借款属性
	 * @param pid 产品ID
	 * @return 借款属性值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static int queryLoanType(long pid) {
		String hql = "select loan_type from t_products  where id=?";
		Integer loanType = null;
		
		try {
			loanType = t_products.find(hql, pid).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->获取产品借款属性:" + e.getMessage());

			return -1;
		}
		
		if(loanType == null) {
			return -1;
		}else {
			return loanType;
		}
	}
	
	/**
	 * 获取产品中净值和秒还的属性值
	 * @param error 信息值
	 * @return List<Integer>
	 */
	public static List<Integer> queryLoanType(ErrorInfo error){
		error.clear();
		
		String hql = "select loan_type from t_products where loan_type in (?, ?)";
		
		try {	
			return t_products.find(hql, Constants.NET_VALUE_BID, Constants.S_REPAYMENT_BID).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->获取所有产品属性值:" + e.getMessage());
			error.msg = error.FRIEND_INFO+ "加载产品属性值失败!" + error.PROCESS_INFO;

			return null;
		}
	}

	/**
	 * 获取净值产品的保证金比例
	 */
	public static double queryNetValueBailScale(ErrorInfo error) {
		error.clear();
		
		String hql = "select bail_scale from t_products where loan_type = ?";
		Double bailScale = null;
		
		try {
			bailScale = t_products.find(hql, Constants.NET_VALUE_BID).first();
		} catch (Exception e) {
			Logger.error("产品->获取净值产品的保证金比例:" + e.getMessage());
			error.msg = error.FRIEND_INFO+ "获取净值产品的保证金比例失败!";
			
			return 1;
		}
		
		if(null == bailScale) {
			return 1;
		}else {
			return bailScale;
		}	
	}
	
	/**
	 * 搜素部分字段查询
	 * @param error 信息值 
	 * @return List<Product>
	 */
	public static List<Product> queryProductNames(boolean flag, ErrorInfo error){
		error.clear();
		
		List<Product> products = new ArrayList<Product>();
		List<t_products> tproducts = null;

		String hql = "select new t_products(id, name, small_image_filename) from t_products where is_use = ?";
		
		if(!flag) {
			hql += " and is_agency = " + flag;
		}
		
		try {
			tproducts = t_products.find(hql, Constants.ENABLE).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->搜素部分字段查询:" + e.getMessage());
			error.msg = error.FRIEND_INFO+ "产品列表加载失败!" + error.PROCESS_INFO;

			return null;
		}
		
		if(null == tproducts) {
			return products;
		}
		
		Product product = null;

		for (t_products pro : tproducts) {
			product = new Product();

			product._id = pro.id;
			product.name = pro.name;
			product.smallImageFilename = pro.small_image_filename;

			products.add(product);
		}

		return products;
	}
	
	/**
	 * 上架/下架
	 * @param pid 产品ID
	 * @param isUse 状态值
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static void editStatus(long pid, boolean isUse, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_products set is_use=? where id=?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, isUse);
		query.setParameter(2, pid);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->上架/下架:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "上架/下架失败!";

			return;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "设置失败!";
			
			return;
		}
		
		/* 添加事件 */
		if(isUse) {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_PRODUCT, "启用借款标产品", error);
		}else {
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_PRODUCT, "暂停借款标产品", error);
		}
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}
	}

	/**
	 * 添加
	 * @param info 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void create(ErrorInfo error) {
		error.clear();
		
		t_products product = new t_products();
		product.is_use = Constants.ENABLE;
		product.is_agency = Constants.NOT_IS_AGENCY;
		//product.fee_description = this.feeDescription; // 手续费描述(常量值读取拼接，无需编辑)
		
		/* 添加基本信息 */
		error.code = this.addOrEdit(product);
		
		if (error.code < 0) {
			error.code = -1;
			error.msg = error.FRIEND_INFO + "添加基本信息失败!";

			return;
		}

		/* 添加对应的审核资料 */
		error.code = this.addProductAudit(product.id, product.mark);
		
		if(error.code < 0){
			error.code = -2;
			error.msg = error.FRIEND_INFO + "添加审核资料失败!";

			return;
		}
		
		/* 添加标签和字段 */
		error.code = this.addProductLableAndFiled(product.id, error);
		
		if(error.code < 0){
			error.code = -3;
			error.msg = error.FRIEND_INFO + "添加产品标签/字段失败!";

			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_PRODUCT, "添加产品", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "保存失败!";
			
			return;
		}
	}

	/**
	 * 编辑
	 * @param id 产品ID
	 * @param error 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public void edit(long id, ErrorInfo error) {
		error.clear();

		t_products product = null;

		try {
			product = t_products.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->编辑:" + e.getMessage());
			error.code = -1;
			error.msg = error.FRIEND_INFO + "编辑失败!";

			return;
		}
		
		if (null == product) {
			error.code = -2;
			error.msg = error.FRIEND_INFO + "编辑失败!";

			return;
		}
		
		/* 添加基本信息 */
		error.code = this.addOrEdit(product);
		
		if(error.code < 0){
			error.code = -3;
			error.msg = error.FRIEND_INFO + "编辑失败!";

			return;
		}

		/* 删除作废的审核资料 */
		error.code = ProductAuditItem.deleteProductAudit(id);
		
		if(error.code < 0){
			error.code = -4;
			error.msg = error.FRIEND_INFO + "编辑失败!";
			
			return;
		}

		/* 添加对应的审核资料 */
		error.code = this.addProductAudit(id, product.mark);
		
		if(error.code < 0){
			error.code = -5;
			error.msg = error.FRIEND_INFO + "编辑失败!";
			
			return;
		}
		
		/* 编辑标签和字段 */
		error.code = this.editFiledConten();
		
		if(error.code < 0){
			error.code = -6;
			error.msg = error.FRIEND_INFO + "编辑失败!";
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_PRODUCT, "编辑产品", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "保存失败!";
			
			return;
		}
	}

	/**
	 * 添加/编辑
	 */
	private int addOrEdit(t_products product) {
		product.time = new Date(); // 时间
		product.name = this.name; // 名称
		product.fit_crowd = this.fitCrowd; // 适合人群
		product.characteristic = this.characteristic; // 特点、亮点
		product.min_amount = this.minAmount; // 借款金额下限
		product.max_amount = this.maxAmount; // 借款金额上限
		product.min_invest_amount = this.minInvestAmount; // 最低投标金额
		product.min_interest_rate = this.minInterestRate; // 利率下限
		product.max_interest_rate = this.maxInterestRate; // 利率上限
		product.credit_id = this.creditId; // 信用积分Id
		product.max_copies = this.maxCopies; // 最高拆分份数
		product.period_year = this.periodYear; // 年期限单位
		product.period_month = this.periodMonth; // 月期限单位
		product.period_day = this.periodDay; // 日期限单位
		product.audit_cycle = this.auditCycle; // 审核周期
		product.applicant_condition = this.applicantCondition; // 申请条件
		product.loan_type = this.loanType; // 1 秒还 2 净值 3 普通
		product.bail_scale = this.bailScale; // 保证金百分比
		product.is_deal_password = Constants.IPS_ENABLE ? false : this.isDealPassword; // 是否需要交易密码
		product._order = this.order; // 排序
		product.show_type = this.showType; // 显示方式
		product.small_image_filename = this.smallImageFilename; // 借款小图标
		product.name_image_filename = this.nameImageFilename; // 借款标题图片
		product.mark = UUID.randomUUID().toString();
		product.invest_period = this.investPeriod; // 投标期限
		product.repayment_type_id = this.splitRepaymentType(this.repaymentTypeId); // 还款方式
		//product.is_use = this.isUse; // 默认为上架
		//product.is_agency = this.isAgency; // 产品模式(非合作机构标)
		
		int _loanImageType = this.loanImageType;
		product.loan_image_type = _loanImageType; // 借款图片上传方式
		
		if (_loanImageType == Constants.PLATFORM_UPLOAD) {
			product.loan_image_filename = this.loanImageFilename; // 平台上传图片
		} else {
			product.loan_image_filename = ""; // 用户自己上传图片
		}
			
		try {
			product.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("产品->添加产品基本资料:" + e.getMessage());

			return -1;
		}
		
		return 1;
	}
	
	/**
	 * 添加产品对应的审核资料
	 */
	private int addProductAudit(long id, String mark) {
		int num = -1;

		// 添加产品对应的必审资料
		if (null != this.requiredAuditId) {

			for (String rid : requiredAuditId) {
				num = ProductAuditItem.createroductAuditItem(id, Long.parseLong(rid), Constants.NEED_AUDIT, mark);

				if (num < 1) return -1;
			}
		}

		// 添加产品对应的可审资料
		if (null != this.selectAuditId) {

			for (String sid : selectAuditId) {
				num = ProductAuditItem.createroductAuditItem(id, Long.parseLong(sid), Constants.NOT_NEED_AUDIT, mark);

				if (num < 1) return -1;
			}
		}
		
		return 1;
	}
	
	/**
	 * 添加标签/字段
	 * @param productId
	 * @return
	 */
	public int addProductLableAndFiled(long productId, ErrorInfo error){
		if(StringUtils.isBlank(this.jsonLables))
			return 1;

		/* 得到集合 */
        List<Map<String,Object>> lables = (List)JSONArray.fromObject(this.jsonLables);
        List<Map<String,Object>> fields = null;
        ProductLable lable = null;
        ProductLableField field = null;
        
        /* 标签 */
        for (Map<String, Object> lableMap : lables) {
        	lable = new ProductLable();
			lable.productId = productId;
        	lable.name = lableMap.get("name").toString();
        	lable.create();
        	
        	if(lable.id < 1) return -1;
        	
        	fields = (List<Map<String,Object>>)lableMap.get("fields");
        	
        	if(null == fields || fields.size() == 0) continue;
        	
        	/* 字段 */
        	for (Map<String,Object> fieldMap : fields) {
        		field = new ProductLableField();
        		field.lableId = lable.id;
        		field.name = fieldMap.get("name").toString();
        		field.type = Integer.parseInt(fieldMap.get("type").toString());
        		field.content = fieldMap.get("content").toString();
        		
        		field.create();
        		
        		if(field.id < 1) return -2;
        	}
        }
        
        return 1;
	}
	
	/**
	 * 修改标签字段内容
	 * @return
	 */
	private int editFiledConten(){
		
		if(null == this.lablesArray || this.lablesArray.length <= 0)
			return 1;
			
		ProductLableField field = null;
		
		for (ProductLable lables : this.lablesArray) {
			for (ProductLableField fields : lables.fieldsArray) {
				field = new ProductLableField();
				field.content = fields.content;
				
				if(field.editContent(fields.id) < 1) return -1;
				
//				/* 添加事件 */
//        		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_LABLE, "添加标签", error);
//        		
//        		if(error.code < 0){
//        			JPA.setRollbackOnly();
//        			
//        			return -2;
//        		}
			}
		}
		
		return 1;
	}
	
	/**
	 * 是否是合作机构标
	 * @param id
	 * @return
	 */
	public static Boolean isAgency(long id){
		String sql = "select is_agency from t_products where id = ? and is_use = 1";
		Query query = JPA.em().createNativeQuery(sql);
		query.setParameter(1, id);
		List<Object> obj = null;
		
		try {
			obj = query.getResultList();
		} catch (Exception e) {
			Logger.info("查询产品是否是机构合作标时，%s", e.getMessage());
			return null;
		}
		
		if(null == obj || obj.size() == 0)
			return null;
			
		Object isAgency = obj.get(0);
		
		if(null == isAgency)
			return null;
		
		return Boolean.parseBoolean(isAgency.toString());
	}
	
	/**
	 * 获取产品借款属性
	 * @param smallImageFilename
	 */
	public static String queryProductNameByImage(String smallImageFilename) {
		String hql = "select name from t_products  where small_image_filename=?";
		String name = smallImageFilename;
		
		try {
			name = t_products.find(hql, smallImageFilename).first();
		} catch (Exception e) {
			Logger.error("产品->获取产品名称:" + e.getMessage());

			return smallImageFilename;
		}
		
		return name;
	}
	
	/**
	 * 获取所有产品的id和name
	 */
	public static List<t_products> queryAllProducts(ErrorInfo error){
		error.clear();
		
		List<t_products> products;
		String sql = "select new t_products(p.id, p.name) from t_products p";
		
		Query query = JPA.em().createQuery(sql, t_products.class);
		try {
			products = query.getResultList();
		} catch (Exception e) {
			Logger.error("" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询所有产品时有误！";
			
			return null;
		}
		
		error.code = 0;
		
		return products;
	}
}
