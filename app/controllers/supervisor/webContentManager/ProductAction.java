package controllers.supervisor.webContentManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import models.t_new_city;
import models.t_new_province;
import models.t_system_options;
import models.t_user_auth_review;
import models.t_user_bank_accounts;
import models.t_user_city;
import constants.Constants;
import constants.OptionKeys;
import controllers.supervisor.SupervisorController;
import business.AuditItem;
import business.Bid;
import business.CompanyUserAuthReviewBusiness;
import business.CreditLevel;
import business.DictBanksDate;
import business.Product;
import business.User;
import business.UserBankAccounts;
import business.Bid.Purpose;
import business.Bid.Repayment;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;

/**
 * 产品 Action
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-29 下午02:50:15
 */
public class ProductAction extends SupervisorController {
	
	/**
	 * 借款端已分配列表详情
	 */
	public static void allotDetails(long userId) {
		User user = new User();
		user.id = userId;
		int authority = 3;
		
		JSONObject authReview = new JSONObject();
		authReview.put("user_type", user.user_type);
		authReview.put("creditAmount", user.creditAmount);
		if(user.realityName == null && (user.user_type == 2 || user.user_type == 3)) {
			t_user_auth_review dbAuthReview = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(userId);
			if(dbAuthReview != null) {
				authReview.put("realityName", dbAuthReview.company_name);
				authReview.put("idNumber", dbAuthReview.credit_code);
				authReview.put("bankName", dbAuthReview.bank_name);
				authReview.put("bankNo", dbAuthReview.bank_no);
				authReview.put("bankAccount", dbAuthReview.real_name);
			}
		} else {
			authReview.put("realityName", user.realityName);
			authReview.put("idNumber", user.idNumber);
			t_user_bank_accounts banks = UserBankAccounts.queryById(userId);
			if(banks != null) {
				authReview.put("bankName", banks.bank_name);
				authReview.put("bankNo", banks.account);
				authReview.put("bankMobile", banks.mobile);
				authReview.put("bankAccount", banks.account_name);
			}
		}
		
		t_user_bank_accounts firstBank = UserBankAccounts.queryById(userId);
		List<t_user_bank_accounts> userBanks = UserBankAccounts.queryMoreById(userId);
		
		t_user_city userCity = t_user_city.find("user_id = ? ", userId).first();
		List<t_new_province> newProvinces =User.queryAllNewProvince();
		List<t_new_city> newCitys = userCity == null? new ArrayList<t_new_city>(): User.queryNewCity(userCity.province_id);
		Map<String, String> bankNames = DictBanksDate.bankCodeNameTable;
		
		render(user,authority,authReview,firstBank,userBanks,userCity,newProvinces,newCitys,bankNames);
	}
	
	/**
	 * 实名信息
	 * @param userId
	 */
	public static void realityNameInfo(long userId,int userType,int authority) {
		JSONObject authReview = new JSONObject();
		User user = new User();
		user.id = userId;
		authReview.put("user_type", userType);
		authReview.put("creditAmount", user.creditAmount);
		if(user.realityName == null && (user.user_type == 2 || user.user_type == 3)) {
			t_user_auth_review dbAuthReview = CompanyUserAuthReviewBusiness.findNotResetedAuthReviewRecord(userId);
			if(dbAuthReview != null) {
				authReview.put("realityName", dbAuthReview.company_name);
				authReview.put("idNumber", dbAuthReview.credit_code);
				authReview.put("bankName", dbAuthReview.bank_name);
				authReview.put("bankNo", dbAuthReview.bank_no);
				authReview.put("bankAccount", dbAuthReview.real_name);
			}
		} else {
			authReview.put("realityName", user.realityName);
			authReview.put("idNumber", user.idNumber);
			t_user_bank_accounts banks = UserBankAccounts.queryById(userId);
			if(banks != null) {
				authReview.put("bankName", banks.bank_name);
				authReview.put("bankNo", banks.account);
				authReview.put("bankMobile", banks.mobile);
				authReview.put("bankAccount", banks.account_name);
			}
		}
		//加载用户户籍所在省市数据
		t_user_city userCity = t_user_city.find("user_id = ?", userId).first();
		List<t_new_province> newProvinces =User.queryAllNewProvince();
		List<t_new_city> newCitys = userCity == null? new ArrayList<t_new_city>(): User.queryNewCity(userCity.province_id);
		Map<String, String> bankNames = DictBanksDate.bankCodeNameTable;
		
		render(user,authority,authReview,userCity,newProvinces,newCitys,bankNames);
	}
	
	/**
	 * 产品列表
	 */
	public static void productList() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String keyword = params.get("keyword"); // 关键词
		
		ErrorInfo error = new ErrorInfo();
		
		PageBean<Product> pageBean = new PageBean<Product>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		pageBean.page = Product.queryProduct(pageBean, keyword, error);

		if (null == pageBean.page) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 产品上架
	 */
	public static void enableProduct(long productId) {
		ErrorInfo error = new ErrorInfo();
		Product.editStatus(productId, Constants.ENABLE, error);
		flash.error(error.msg);
		
		productList();
	}

	/**
	 * 产品下架
	 */
	public static void notEnableProduct(long productId) {
		ErrorInfo error = new ErrorInfo();
		Product.editStatus(productId, Constants.NOT_ENABLE, error);
		flash.error(error.msg);
		
		productList();
	}

	/**
	 * 添加产品 页面跳转
	 */
	public static void addProduct() {
		ErrorInfo error = new ErrorInfo();
		
		/* 所有产品的属性值 */
		List<Integer> loanTypes = Product.queryLoanType(error);
		
		if (null == loanTypes) {
			error.msg = error.FRIEND_INFO + "获取还款类型失败了!";
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		/* 还款类型 */
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error);
		
		if (null == rtypes) {
			error.msg = error.FRIEND_INFO + "获取还款类型失败了!";
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		/* 信用等级名称 */
		List<CreditLevel> creditLevels = CreditLevel.queryCreditName(error);
		
		if (null == rtypes) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 审核资料 */
		List<AuditItem> auditItems = AuditItem.queryEnableAuditItems(error);
		
		if (null == rtypes) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		/* 手续费常量值 */
		String strfee = OptionKeys.getvalue(OptionKeys.BORROW_FEE, error);
		String borrowFeeMonth = OptionKeys.getvalue(OptionKeys.BORROW_FEE_MONTH, error);
		String borrowFeeRate = OptionKeys.getvalue(OptionKeys.BORROW_FEE_RATE, error);
		
		//String key = "product_" + session.getId();
		//Product goods = (Product)Cache.get(key); // 从缓存中得到用户输入的临时数据
		//Cache.delete(key);
		
		render(rtypes, creditLevels, auditItems, loanTypes, strfee, borrowFeeMonth, borrowFeeRate);
	}
	
	/**
	 * 添加产品
	 */
	public static void addingProduct(Product product, int showType1, int showType2, int showType3) {
		checkAuthenticity();
        
		if (checkProduct(product, 0)) {
			//Cache.set("product_" + session.getId(), product); // 保存临时数据到缓存
			
			addProduct();
		}
		
		int showValue = 0;  //利用二进制转十进制算法，每一种情况对应一种数值
		
		if(showType1 == 1){
			showValue = showValue +1;
		}
		
		if(showType2 == 1){
			showValue = showValue +2;
		}
		
		if(showType3 == 1){
			showValue = showValue +4;
		}
		product.showType = showValue;
		

		ErrorInfo error = new ErrorInfo();
		product.create(error);
		flash.error(error.msg);
		
		productList();
	}
	
	/**
	 * 编辑产品 页面跳转
	 */
	public static void editProduct(long productId) {
		ErrorInfo error = new ErrorInfo();
		
		/* goods：防止模型驱动与对象名起冲突 */
		Product goods = new Product();
		/* 填充自己 */
		goods.id = productId;
		
		if (goods.id == -1) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 所有产品的属性值 */
		List<Integer> loanTypes = Product.queryLoanType(error);
		
		if (null == loanTypes) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		/* 还款类型 */
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error);
		
		if (null == rtypes) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		
		/* 信用等级名称 */
		List<CreditLevel> creditLevels = CreditLevel.queryCreditName(error);
		
		if (null == creditLevels) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 审核资料 */
		List<AuditItem> auditItems = AuditItem.queryEnableAuditItems(error);
		
		if (null == auditItems) 
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 手续费常量值 */
		String strfee = OptionKeys.getvalue(OptionKeys.BORROW_FEE, error);
		String borrowFeeMonth = OptionKeys.getvalue(OptionKeys.BORROW_FEE_MONTH, error);
		String borrowFeeRate = OptionKeys.getvalue(OptionKeys.BORROW_FEE_RATE, error);
		
		render(goods, rtypes, creditLevels, auditItems, loanTypes, strfee, borrowFeeMonth, borrowFeeRate);
	}

	/**
	 * 编辑产品
	 */
	public static void editingProduct(long productId, Product product, int showType1, int showType2, int showType3) {
		checkAuthenticity();
		
        int showValue = 0;  //利用二进制转十进制算法，每一种情况对应一种数值
		
        if(showType1 == 1){
			showValue = showValue + 1;
		}
		
		if(showType2 == 1){
			showValue = showValue + 2;
		}
		
		if(showType3 == 1){
			showValue = showValue + 4;
		}
		product.showType = showValue; 
		
		if (checkProduct(product, productId)) 
			editProduct(productId);

		ErrorInfo error = new ErrorInfo();
		product.edit(productId, error);
		flash.error(error.msg);
		
		productList();
	}
	
	/**
	 * 验证
	 */
	private static boolean checkProduct(Product product, long productId){
		if (StringUtils.isBlank(product.periodYear)) {
			flash.error("年期限单位有误!");
			
			return true;
		}
		
		if (StringUtils.isBlank(product.periodMonth)) {
			flash.error("月期限单位有误!");
		
			return true;
		}
		
		if (StringUtils.isBlank(product.periodDay)) {
			flash.error("日期限单位有误!");
		
			return true;
		}

		String[] arr = product.periodYear.split(",");
		Integer value = 0;
		Set<String> set = new HashSet<String>();

		for (String str : arr) {
			value = Integer.parseInt(str);

			if (value > 5 || value <= 0) {
				flash.error("年期限需在1~5年(包含5)之间!");

				return true;
			}

			set.add(str);
		}

		if (set.size() != arr.length) {
			flash.error("年期限出现重复数据!");

			return true;
		}

		arr = product.periodMonth.split(",");
		set = new HashSet<String>();

		for (String str : arr) {
			value = Integer.parseInt(str);

			if (value > 5 * 12 || value <= 0) {
				flash.error("月期限需在1~60月(包含60)之间!");

				return true;
			}

			set.add(str);
		}

		if (set.size() != arr.length) {
			flash.error("月期限出现重复数据!");

			return true;
		}

		arr = product.periodDay.split(",");
		set = new HashSet<String>();

		for (String str : arr) {
			value = Integer.parseInt(str);

			if (value > 30 || value <= 0) {
				flash.error("日期限需在1~30日(包含30)之间!");

				return true;
			}

			set.add(str);
		}

		if (set.size() != arr.length) {
			flash.error("日期限出现重复数据!");

			return true;
		}

		arr = product.investPeriod.split(",");
		set = new HashSet<String>();

		for (String str : arr) {
			value = Integer.parseInt(str);

			if (value <= 0) {
				flash.error("满标期限<=0!");

				return true;
			}

			set.add(str);
		}

		if (set.size() != arr.length) {
			flash.error("满标期限出现重复数据!");

			return true;
		}
		
		if (StringUtils.isBlank(product.name)) {
			flash.error("产品名称有误!");
			return true;
		}
		
		if (Product.checkName(product.name, productId)) {
			flash.error("产品名称重复!");
		
			return true;
		}
		
		if (StringUtils.isBlank(product.fitCrowd)) {
			flash.error("适合人群有误!");
		
			return true;
		}
		
		if (StringUtils.isBlank(product.characteristic)) {
			flash.error("产品特点有误!");
			
			return true;
		}
		
		if (StringUtils.isBlank(product.applicantCondition)) {
			flash.error("申请条件有误!");
			
			return true;
		}
		
		if (StringUtils.isBlank(product.smallImageFilename) || product.smallImageFilename.contains(Constants.DEFAULT_IMAGE)) {
			flash.error("借款小图标有误!");
			
			return true;
		}
		
		if (StringUtils.isBlank(product.nameImageFilename) || product.nameImageFilename.contains(Constants.DEFAULT_IMAGE)) {
			flash.error("产品名称图片有误!");
			
			return true;
		}
		
		if (null == product.investPeriodArray || product.investPeriodArray.length == 0) {
			flash.error("投标期限有误!");
			
			return true;
		}
		
		if (Constants.PLATFORM_UPLOAD == product.loanImageType) {
			if(StringUtils.isBlank(product.loanImageFilename) || product.loanImageFilename.contains(Constants.DEFAULT_IMAGE)) {
				flash.error("借款图片有误!");
				
				return true;
			}
		}
		
		if (null == product.repaymentTypeId || product.repaymentTypeId.length == 0) {
			flash.error("还款类型有误!");
			
			return true;
		}
		
		if (null == product.requiredAuditId || product.requiredAuditId.length == 0) {
			flash.error("必须审核资料有误!");
			
			return true;
		}
		
		/* 必审资料和可选资料不能重复 */
		if(null != product.selectAuditId && product.selectAuditId.length > 0) {
			for (String select : product.selectAuditId) {
				for (String required : product.requiredAuditId) {
					if(select.equals(required)) {
						flash.error("必审资料和可选资料不能重复!");
						
						return true;
					}
				}
			}
		}
		
		if (product.loanType < 0) {
			flash.error("借款模式有误!");
		
			return true;
		}
		
		List<Integer> loanTypes = Product.queryLoanType(new ErrorInfo());
		
		if(!Constants.IS_SECOND_BID && product.loanType == Constants.S_REPAYMENT_BID){
			flash.error("当前支付平台不支持秒还标产品，请勿进行非法操作！");
			
			return true;
		}
		
		int loanType = productId > 0 ? Product.queryLoanType(productId) : 0; // 得到当前的产品借款模式
		
		/* 校验借款模式的唯一性 */
		for (Integer type : loanTypes) {
			if(type == loanType)
				continue ;
			
			if(type == product.loanType){
				flash.error("借款模式只允许[秒还][净值]出现一种!");
				
				return true;
			}
		}
		
		if (0 == product.creditId) {
			flash.error("最低信用等级有误!");
			
			return true;
		}
		
		if (product.showType < 0) {
			flash.error("发布模式有误!");
			
			return true;
		}
		
		if (product.order <= 0) {
			flash.error("产品排序有误!");
			
			return true;
		}
		
		if (product.minAmount <= 0 || product.minAmount > Constants.MAX_VALUE) {
			flash.error("最低借款金额有误!");
			
			return true;
		}
		
		if (product.maxAmount <= 0 || product.maxAmount > Constants.MAX_VALUE) {
			flash.error("最高借款金额有误!");
			
			return true;
		}
		
		if (product.minAmount > product.maxAmount) {
			flash.error("最低借款金额大于了最高借款金额!");
			
			return true;
		}
		
		if (product.minInterestRate < Constants.MIN_INTEREST_RATE || product.minInterestRate >= Constants.MAX_INTEREST_RATE) {
			flash.error("最低借款利率有误!");
			
			return true;
		}
		
		if (product.maxInterestRate > Constants.MAX_INTEREST_RATE || product.maxInterestRate <= Constants.MIN_INTEREST_RATE) {
			flash.error("最高借款利率有误!");
			
			return true;
		}
		
		if (product.minInterestRate > product.maxInterestRate) {
			flash.error("最低借款利率大于了最高借款利率!");
			
			return true;
		}
		
		if (product.minInvestAmount <= 0) {
			flash.error("最低投标金额有误!");
			
			return true;
		}
		
		if (product.maxCopies <= 0) {
			flash.error(" 最高拆分份数有误!");
			
			return true;
		}
		
		if (product.auditCycle <= 0) {
			flash.error("审核周期有误!");
			
			return true;
		}
		
		if (product.bailScale < 0 || product.bailScale > 100) {
			flash.error("保证金百分比有误!");
			
			return true;
		}

		return false;
	}
	
	/**
	 * 常量
	 */
	public static void constantList() {
		ErrorInfo error = new ErrorInfo();
		
		/* 审核机制 */
		List<t_system_options> auditMechanism = Bid.getAuditMechanism(error);
		
		if(null == auditMechanism)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 借款用途 */
		List<Purpose> purposes = Purpose.queryLoanPurpose(error, false);
		
		if(null == purposes)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 还款类型 */
		List<Repayment> types = Repayment.queryRepaymentType(null, error);
		
		if(null == types)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		/* 编号字母 */
		List<t_system_options> numbers = Bid.getNumberList(error);
		
		if(null == numbers)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(auditMechanism, purposes, types, numbers);
	}
	
	/**
	 * 设置审核机制
	 */
	public static void editAuditMechanism(String auditMechanism, String auditItem, String passRate){
		checkAuthenticity();
		
		if(	StringUtils.isBlank(auditMechanism) ||
			(StringUtils.isNotBlank(auditItem) && StringUtils.isBlank(passRate))
	      ){
			flash.error("数据有误!");
			
			constantList();
		}
		
		ErrorInfo error = new ErrorInfo();
		Bid.setAuditMechanism(auditMechanism, auditItem, passRate, error);
		flash.error(error.msg);
		
		constantList();
	}
	
	/**
	 * 显示借款用途
	 */
	public static void enablePurpose(long purposeId){
		ErrorInfo error = new ErrorInfo();
		Purpose.editLoanPurposeStatus(purposeId, Constants.ENABLE, error);
		flash.error(error.msg); 
		 
		constantList();
	} 
	
	/**
	 * 隐藏借款用途
	 */
	public static void notEnablePurpose(long purposeId){
		ErrorInfo error = new ErrorInfo();
		Purpose.editLoanPurposeStatus(purposeId, Constants.NOT_ENABLE, error);
		flash.error(error.msg); 
		 
		constantList();
	} 
	
	/**
	 * 添加借款用途
	 */
	public static void addingPurpose(String purposename, int purposeorder) {
		if( StringUtils.isBlank(purposename) ||
			0 == purposeorder
		  ) {
			flash.error("数据有误!");
			
			constantList();
		}
		 
		ErrorInfo error = new ErrorInfo();
		Purpose.addLoanPurpose(purposename, purposeorder, error);
		flash.error(error.msg); 
		
		constantList();
	}
	
	/**
	 * 编辑借款用途
	 */
	public static void editingPurpose(long purposeId, String purposename, int purposeorder){
		if(	0 == purposeId ||
			StringUtils.isBlank(purposename) ||
			0 == purposeorder
		  ) {
				flash.error("数据有误!");
				
				constantList();
			}
		
		ErrorInfo error = new ErrorInfo();
		Purpose.editLoanPurpose(purposeId, purposename, purposeorder, error);
		flash.error(error.msg); 
		
		constantList();
	}
	
	/**
	 * 显示还款类型
	 */
	public static void enableRepaymentType(long rid) {
		ErrorInfo error = new ErrorInfo();
		Repayment.editRepaymentType(rid, Constants.ENABLE, error);
		flash.error(error.msg); 
		
		constantList();
	}
	
	/**
	 * 隐藏还款类型
	 */
	public static void notEnableRepaymentType(long rid) {
		ErrorInfo error = new ErrorInfo();
		Repayment.editRepaymentType(rid, Constants.NOT_ENABLE, error);
		flash.error(error.msg); 
		
		constantList();
	}
	
	/**
	 * 编辑编号字母
	 */
	public static void editingNumber(String key, String value){
		if(	StringUtils.isBlank(key) ||
			StringUtils.isBlank(value)) {
			flash.error("数据有误!");
			
			constantList();
		}  
		
		ErrorInfo error = new ErrorInfo();
		Bid.setNumber(key, value, error);
		flash.error(error.msg); 
		
		constantList();
	}

	/**
	 * 审计资料列表
	 */
	public static void audtiItemList() {
		String currPage = params.get("currPage"); // 当前页
		String pageSize = params.get("pageSize"); // 分页行数
		String keyword = params.get("keyword"); // 关键词
		
		ErrorInfo error = new ErrorInfo();
		PageBean<AuditItem> pageBean = AuditItem.queryAuditItems(currPage, pageSize, keyword, Constants.NOT_ENABLE, error);
		
		if (null == pageBean)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}
	
	/**
	 * 启用审计资料
	 */
	public static void enableAuditItem(long aid){
		ErrorInfo error = new ErrorInfo();
		AuditItem.editStatus(aid, Constants.ENABLE, error);
		flash.error(error.msg);
		
		audtiItemList();
	}
	
	/**
	 * 暂停审计资料
	 */
	public static void notEnableAuditItem(long aid){
		ErrorInfo error = new ErrorInfo();
		AuditItem.editStatus(aid, Constants.NOT_ENABLE, error);
		flash.error(error.msg);
		
		audtiItemList();
	}
	
	/**
	 * 检查名称是否存在
	 * @param name 名称
	 * @param flag 0 ：资料,1：产品 
	 */
	public static void checkName(String name, long id, int flag){
		
		if(0 == flag)
			renderJSON(AuditItem.checkName(name, id));
		else if(1 == flag)
			renderJSON(Product.checkName(name, id));
		else
			renderJSON(true);
	}
	
	/**
	 * 添加审计项目
	 */
	public static void addingAuditItem(AuditItem item){
		checkAuthenticity();
		
		if( AuditItem.checkName(item.name, 0) ||
			checkAuditItem(item)){
			flash.error("数据有误!");
			
			audtiItemList();
		}
		
		ErrorInfo error = new ErrorInfo();
		item.create(error);
		flash.error(error.msg);
		
		audtiItemList();
	}
	
	/**
	 * 编辑审计项目
	 */
	public static void editingAuditItem(AuditItem item, long itemId){
		checkAuthenticity();

		if(
			AuditItem.checkName(item.name, itemId) ||
			checkAuditItem(item) || 0 == itemId){
			flash.error("数据有误!");
			
			audtiItemList();
		}
		
		ErrorInfo error = new ErrorInfo();
		item.edit(itemId, error);
		flash.error(error.msg);
		
		audtiItemList();
	}
	
	/**
	 * 约束资料数据
	 */
	private static boolean checkAuditItem(AuditItem item){
		if( StringUtils.isBlank(item.name) ||
			StringUtils.isBlank(item.description) ||
			0 == item.type ||
			1 > item.period ||
			1 > item.auditCycle ||
			0 > item.auditFee ||
			0 > item.creditScore ||
			10 < item.name.length()
		  ) 
			
			return true;
			
		return false;
	}
}