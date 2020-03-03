package controllers.front.bid;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_content_news;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.t_dict_cars;
import models.t_dict_educations;
import models.t_dict_houses;
import models.t_dict_maritals;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Before;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.IDCardValidate;
import utils.PageBean;
import utils.Security;
import utils.ServiceFee;
import annotation.InactiveUserCheck;
import annotation.IpsAccountCheck;
import annotation.LoginCheck;
import business.Ads;
import business.BackstageSet;
import business.Bid;
import business.Bid.Purpose;
import business.Bid.Repayment;
import business.News;
import business.Product;
import business.User;
import constants.Constants;
import constants.OptionKeys;
import constants.IPSConstants.IpsCheckStatus;
import controllers.BaseController;
import controllers.front.account.CheckAction;
import controllers.front.account.LoginAndRegisterAction;
import controllers.front.invest.InvestAction;
import controllers.interceptor.UserStatusInterceptor;

/**
 * 标 Action
 * 
 * @author bsr
 * @version 6.0o
 * @created 2014-4-22 上午09:47:28
 */
@With(UserStatusInterceptor.class)
public class BidAction extends BaseController {
	
	/** 
	 * 我要借款首页
	 */
	public static void index(long productId, int code, int status) {
		ErrorInfo error = new ErrorInfo();
		/* 根据排序得到所有的非合作机构产品列表  */
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);
		/* 最新投资资讯 */
		List<Bid> bids = Bid.queryAdvertisement(error);
		/*借款须知*/
		PageBean <t_content_news> pageBean = News.queryNewsByTypeId("14", "1", "7", "", error);
		/*小广告*/
		Ads ads = new Ads();
		ads.id = 13;
		
		renderArgs.put("products", products);
		renderArgs.put("bids", bids);
		renderArgs.put("pageBean", pageBean);
		renderArgs.put("ads", ads);
		renderArgs.put("code", code);
		renderArgs.put("productId", productId);
		renderArgs.put("status", status);
		
		User user = User.currUser();
		
		/* 未完成基本资料 */
		if(code == Constants.NOT_ADDBASEINFO){
			getDataDictForBaseInfo();
			
			render(user);
	    }
		
		render();
	}
	
	/**
	 * 详情
	 */
	public static void detail(long productId, int code, int status) {
		ErrorInfo error = new ErrorInfo();
		
		Product product = new Product();
		product.id = productId;
		
		if(product.id < 1)
			render(Constants.ERROR_PAGE_PATH_FRONT);

		/* 非合作机构,PC/PC+APP产品列表 */
		List<Product> products = Product.queryProduct(Constants.SHOW_TYPE_1, error);

		/* 手续费常量值 */
	    BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
	    double strfee = backstageSet.borrowFee;
	    double borrowFeeMonth = backstageSet.borrowFeeMonth;
	    double borrowFeeRate = backstageSet.borrowFeeRate;

	    renderArgs.put("product", product);
	    renderArgs.put("productId", productId);
	    renderArgs.put("products", products);
	    renderArgs.put("strfee", strfee);
	    renderArgs.put("borrowFeeMonth", borrowFeeMonth);
	    renderArgs.put("borrowFeeRate", borrowFeeRate);
	    renderArgs.put("code", code);
	    renderArgs.put("status", status);
	    
	    User user = User.currUser();
	    
	    if(null == user){
	    	code = -1;
	    
    		render(code);
	    }
	    
	    /* 未完成基本资料 */
	    if(code == Constants.NOT_ADDBASEINFO){
	    	getDataDictForBaseInfo();
			
			render(user);
	    }
	    
	    render();
	}
	
	/**
	 * 立即申请
	 */
	@InactiveUserCheck
	@IpsAccountCheck
	public static void applyNow(long productId, int code, int status) {
		User user = User.currUser();
		
		if(user.simulateLogin != null){
	    	if(user.simulateLogin.equalsIgnoreCase(user.encrypt())){
	        	flash.error("模拟登录不能进行该操作");
	        	String url = request.headers.get("referer").value();
	        	redirect(url);
	        }else{
	        	flash.error("模拟登录超时，请重新操作");
	        	String url = request.headers.get("referer").value();
	        	redirect(url);
	        }
        }
		
		ErrorInfo error = new ErrorInfo();
		
		/* 借款用途  */
		List<Purpose> purpose = Purpose.queryLoanPurpose(error, true);
		
		if(null == purpose) {
			flash.error("借款用途有误!");
			
			render();
		}
		
		/* 还款方式  */
		List<Repayment> repaymentTypes = Repayment.queryRepaymentType(null, error);
		
		if (null == repaymentTypes) {
			flash.error("还款方式为空！");
			
			render();
		}		
		
		Product product = new Product();
		product.createBid = true;
		product.id = productId; 

		if(product.id < 1)
			render(Constants.ERROR_PAGE_PATH_FRONT);
		
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && Constants.S_REPAYMENT_BID == product.loanType) {						
			if(Constants.TRUST_FUNDS_TYPE.equals("HX") &&  !StringUtils.isNotBlank(user.ipsRepayAuthNo)){
				PaymentProxy.getInstance().autoRepaymentSignature(error, Constants.PC);
			}
		}
		
		String key = "bid_" + session.getId();
		Bid loanBid = (Bid) Cache.get(key);  // 获取用户输入的临时数据
		
		//查询此标还有多少条资料没有通过
		int hasAuditCount = 0;
		if(code >= 0 && loanBid != null){
			hasAuditCount = loanBid.queryHasAudit();
			loanBid = null; //成功清空表单
		}
		
		Cache.delete(key); // 删除缓存中的bid对象
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		
		render(purpose, repaymentTypes, product, code, uuid, loanBid, status,hasAuditCount);
	}
	
	/**
	 * 发布借款
	 */
	@IpsAccountCheck
	@InactiveUserCheck
	public static void createBid(Bid bid, String signProductId, String uuid, int status) {
		checkAuthenticity(); 
		
		User user = User.currUser();
		
        if(user.simulateLogin != null){
        	if(user.simulateLogin.equalsIgnoreCase(user.encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }

		ErrorInfo error = new ErrorInfo();
		long productId = Security.checkSign(signProductId, Constants.PRODUCT_ID_SIGN, Constants.VALID_TIME, error);
		
		if(productId < 1){
			flash.put("msg", error.msg);

			applyNow(productId, -100, status);
		}
		
		/* 防重复提交 */
		if(!CaptchaUtil.checkUUID(uuid)){
			flash.put("msg", "请求已提交或请求超时!");
			
			applyNow(productId, -100, status);
		}
		
		bid.createBid = true; // 优化加载
		bid.productId = productId;  // 填充产品对象
		bid.userId = User.currUser().id; // 填充用户对象
		
		/* 非友好提示 */
		if (null == bid || null == bid.product || !bid.product.isUse || bid.product.isAgency || bid.user.id < 1) {

			Logger.info("发布借款标条件不足");

			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		/* 秒还标未进行自动还款签约 */
		if (Constants.IPS_ENABLE && Constants.S_REPAYMENT_BID == bid.product.loanType) {						
			if(Constants.TRUST_FUNDS_TYPE.equals("HX") &&  !StringUtils.isNotBlank(user.ipsRepayAuthNo)){
				PaymentProxy.getInstance().autoRepaymentSignature(error, Constants.PC);
			}
		}
		
		/* 发布借款 */
		t_bids tbid = new t_bids();
		
		//标的发布前校验， 及组装标的信息，不插入数据库
		bid.createBid(Constants.CLIENT_PC, tbid, error);
		
		//校验错误信息，提示到页面
		flash.put("msg", error.msg);		
		Cache.set("bid_" + session.getId(), bid); // 缓存用户输入的临时数据
		if(error.code < 0){			
			applyNow(productId, error.code, status);
		}		
	
		//资金托管接口调用
		if(Constants.IPS_ENABLE){					
			//资金托管调用标的发布接口
			PaymentProxy.getInstance().bidCreate(error, Constants.CLIENT_PC, tbid, bid);
			
			flash.put("msg", error.msg);
			applyNow(productId, error.code, status);
		}else{			
			
			//普通网关标的发布的实际业务			
			//标的发布的实际业务逻辑
			bid.afterCreateBid(tbid, null, Constants.CLIENT_PC, 0, error);			
			flash.put("no", OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, error) + bid.id);
			flash.put("title", bid.title);
			DecimalFormat myformat = new DecimalFormat();
			myformat.applyPattern("##,##0.00");
			flash.put("amount", myformat.format(bid.amount));
			flash.put("status", bid.status);
			applyNow(productId, error.code, status);
		}
	}
	
   /**
    * 弹框登录(异步)
    */
   public static void logining(String name, String password, String code, String randomID){
	   
	   business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
	   Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
	   
	   if(null != currBackstageSet){
		   Cache.delete("backstageSet");//清除系统设置缓存
	   }
	   
	   if(null != bottomLinks){
		   Cache.delete("bottomlinks");//清除底部连接缓存
	   }
	  
	   ErrorInfo error = new ErrorInfo();
	   
	   if(StringUtils.isBlank(name)) 
		  renderText("请输入用户名!");
	   
	   if(StringUtils.isBlank(password)) 
		   renderText("请输入密码!");
	   
	   if(StringUtils.isBlank(code)) 
		   renderText("请输入验证码");
		   
	   
	   if(StringUtils.isBlank(randomID)) 
		   renderText("请刷新验证码");
	   
	   if(!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) 
		   renderText("验证码错误");
	   
	   User user = new User();
	   user.name = name;
	   
	   if(user.id < 0) 
		   renderText("该用户名不存在");
	   
	   if(user.login(password,false, Constants.CLIENT_PC, error)<0) 
		   renderText(error.msg);
	   
   }

   /**
	 * 完善基本资料,下拉框数据
	 */
	private static void getDataDictForBaseInfo(){
		List<t_dict_cars> cars = (List<t_dict_cars>) Cache.get("cars"); // 车子
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces"); // 省
		List<t_dict_educations> educations = (List<t_dict_educations>) Cache.get("educations"); // 教育
		List<t_dict_houses> houses = (List<t_dict_houses>) Cache.get("houses"); // 房子
		List<t_dict_maritals> maritals = (List<t_dict_maritals>) Cache.get("maritals"); // 婚姻
		
		String key = "province" + session.getId();
		Object obj = Cache.get(key);
		Cache.delete(key);
		int province = obj == null ? 1 : Integer.parseInt(obj.toString());
		List<t_dict_ad_citys> cityList = User.queryCity(province); // 市
		
		renderArgs.put("cars", cars);
		renderArgs.put("provinces", provinces);
		renderArgs.put("educations", educations);
		renderArgs.put("houses", houses);
		renderArgs.put("maritals", maritals);
		renderArgs.put("cityList", cityList);

	}
	
	/**
	 * 保存基本信息(弹窗页面)
	 */
	@InactiveUserCheck
	@IpsAccountCheck
	public static void saveInformation(String realityName, int sex, int age,
			int city, int province, String idNumber, int education,
			int marital, int car, int house, long productId, int status, String bidId) {
		ErrorInfo error = new ErrorInfo();
		
		if(User.currUser().simulateLogin != null){
        	if(User.currUser().simulateLogin.equalsIgnoreCase(User.currUser().encrypt())){
            	flash.error("模拟登录不能进行该操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }else{
            	flash.error("模拟登录超时，请重新操作");
            	String url = request.headers.get("referer").value();
            	redirect(url);
            }
        }
		
		User user = new User();
		user.id = User.currUser().id; // 及时在抓取一次	

		user.editInfo(realityName, sex, age, city, idNumber, education, marital, car, house, user.financeType, error);
		
		if(error.code < 0){
			//数据回显
			flash.put("realityName", realityName);
			flash.put("sex", sex);
			flash.put("age", age);
			flash.put("city", city);
			flash.put("province", province);
			flash.put("idNumber", idNumber);
			flash.put("education", education);
			flash.put("marital", marital);
			flash.put("car", car);
			flash.put("house", house);
			
			flash.error(error.msg);
			
			if (Constants.APPLY_NOW_INDEX == status){
				index(productId, Constants.NOT_ADDBASEINFO, status);
			}else{
				detail(productId, Constants.NOT_ADDBASEINFO, status);
			}
		}

		applyNow(productId, 0, status);
	}
	
	/**
	 * 最新满标
	 */
	public static void fullBid(int nowPage) {
		ErrorInfo error = new ErrorInfo();
		
		PageBean<Bid> pageBean = new PageBean<Bid>();
		pageBean.currPage = nowPage;
		pageBean.pageSize = Constants.FULL_BID_COUNT;
		pageBean.page = Bid.queryFullBid(pageBean, error);

		render(pageBean);
	}
	
	/**
	* 总付利息
	*/
	public static void planapr(double amount, double apr, int unit, int period, int repayment){
		if (amount <= 0 || apr < 0 || apr > 100 || unit < -1 || unit > 1 || period <= 0 || repayment < 1 || repayment > 3) {
			renderJSON(0);
		}
		
		double lastAmount = ServiceFee.interestCompute(amount, apr, unit, period, repayment);
		
		renderJSON(lastAmount);
		
	}
}
