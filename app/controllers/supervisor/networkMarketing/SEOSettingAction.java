package controllers.supervisor.networkMarketing;

import business.BackstageSet;
import controllers.supervisor.SupervisorController;
import utils.ErrorInfo;

/**
 * SEO设置
 * 
 * @author bsr
 * 
 */
public class SEOSettingAction extends SupervisorController {

	/**
	 * SEO设置
	 */
	public static void SEOSite() {
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}

	/**
	 * 保存设置
	 */
	public static void save(String seoTitle, String seoDescription, String seoKeywords) {
		ErrorInfo error = new ErrorInfo();
		BackstageSet backstageSet = new BackstageSet();
		
		backstageSet.seoTitle = seoTitle;
		backstageSet.seoDescription = seoDescription;
		backstageSet.seoKeywords = seoKeywords;
		
		backstageSet.SEOSet(error);
		
		if(error.code < 0){
			flash.error(error.msg);
			SEOSite();
		}
		
		backstageSet.setCurrentBackstageSet(backstageSet);
		
		flash.error(error.msg);
		
		SEOSite();
	}
}
