package controllers.supervisor.systemSettings;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import controllers.supervisor.SupervisorController;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import business.BackstageSet;
import business.User;
import play.cache.Cache;
import utils.ErrorInfo;

/**
 * 软件授权设置
 * 
 * @author bsr
 * 
 */
public class SoftwareLicensAction extends SupervisorController {
	
	/**
	 * 系统基本资料设置
	 */
	public static void basicInfo() {
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		long provinceId = BackstageSet.getProvince(backstageSet.companyCity, error);
		List<t_dict_ad_citys> cityList = User.queryCity(provinceId);
		
		render(backstageSet,cityList,provinces,provinceId);
	}

	
	/**
	 * 保存系统基本资料设置
	 */
	public static void saveSystemData() {
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.platformName = params.get("platformName");
		backstageSet.platformLogoFilename = params.get("filename");
		backstageSet.companyName = params.get("companyName");
		backstageSet.companyCity = params.get("city");
		backstageSet.companyAddress = params.get("companyAddress");
		
		backstageSet.companyTelephone = params.get("companyTelephone");
		backstageSet.contactMobile1 = params.get("contactMobile1");
		backstageSet.contactMobile2 = params.get("contactMobile2");
		backstageSet.companyFax = params.get("companyFax");
		backstageSet.companyEmail = params.get("companyEmail");
		backstageSet.companyContact_name = params.get("companyContact_name");
		
		backstageSet.companyQQ1 = params.get("companyQQ1");
		backstageSet.companyQQ2 = params.get("companyQQ2");
		backstageSet.siteIcpNumber = params.get("siteIcpNumber");
		backstageSet.platformTelephone = params.get("platformTelephone");
		backstageSet.platformTelephone2 = params.get("platformTelephone2");
		backstageSet.workStatrTime = params.get("workStatrTime");
		backstageSet.workEndTime = params.get("workEndTime");
		backstageSet.supervisorPlatformLog = params.get("filename2");
		     
		backstageSet.plateDataSet(error);
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		BackstageSet.setCurrentBackstageSet(backstageSet);
		
		flash.error(error.msg);
		basicInfo();
	}

	/**
	 * 软件正版注册信息(首页)
	 */
	public static void genuineSoftware() {
		
		render();
	}
	
	/**
	 * 正版注册
	 */
	public static void register() {
		render();
	}
	
	/**
	 * 正版未注册
	 */
	public static void notRegister() {
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces");
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		render(backstageSet, provinces);
	}
	
	
	/**
	 * 保存正版软件提交信息
	 */
	public static void saveSoftwareLicens(String companyNameService, String province,
			String city, String companyDomain, String registerCode) {
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(companyNameService)) {
			error.code = -1;
			error.msg = "请输入公司名称";
			
			flash.success(error.msg);
			notRegister();
		}
		
		if(StringUtils.isBlank(province) || StringUtils.isBlank(city)) {
			error.code = -1;
			error.msg = "请选择地区";
			
			flash.success(error.msg);
			notRegister();
		}
		
		if(StringUtils.isBlank(companyDomain)) {
			error.code = -1;
			error.msg = "请输入绑定域名";
			
			flash.success(error.msg);
			notRegister();
		}
		
		if(StringUtils.isBlank(registerCode)) {
			error.code = -1;
			error.msg = "请选择正版注册码";
			
			flash.success(error.msg);
			notRegister();
		}
		
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.companyNameService = companyNameService;
		backstageSet.companyProvinceService = province;
		backstageSet.companyCityeService = city;
		backstageSet.companyDomain = companyDomain;
		backstageSet.registerCode = registerCode;
		
		backstageSet.authorize(error);
		
		if(error.code < 0){
			flash.error(error.msg);
			notRegister();
		}
		
		flash.success(error.msg);
		notRegister();
	}
	
//	/**
//	 * 上传图片
//	 */
//	public static void upload(String path) {
//
//		if (null == path)
//			return;
//
//		/* 上传且得到路径 */
//		String src = FileUtil.uploadByPath(path);
//
//		JSONObject json = new JSONObject();
//		/* 保存路径 */
//		json.put("src", src);
//		/* 回调返回 */
//		renderJSON(json.toString());
//	}
}
