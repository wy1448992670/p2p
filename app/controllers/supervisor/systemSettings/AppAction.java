package controllers.supervisor.systemSettings;

import org.apache.commons.lang.StringUtils;
import business.BackstageSet;
import controllers.supervisor.SupervisorController;

public class AppAction extends SupervisorController {
	/**
	 * app版本设置页面
	 */
	public static void version() {
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		
		render(set);
	}
	
	/**
	 * 借款app版本设置页面
	 */
	public static void borrowVersion() {
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		
		render(set);
	}
	
	/**
	 * 保存app版本
	 */
	public static void saveVersion(String iosVersion, String iosCode,
			String iosMsg, String androidVersion, String androidCode,
			String androidMsg,String iosAudit) {
		if( StringUtils.isBlank(iosVersion) ||
			StringUtils.isBlank(iosMsg) ||
			StringUtils.isBlank(androidVersion) ||
			StringUtils.isBlank(iosVersion) ||
			StringUtils.isBlank(iosCode) ||
			StringUtils.isBlank(androidCode) ||
			StringUtils.isBlank(iosAudit)
		  ){
			flash.error("请输入正确的数据!");
			
			version();
		}
		
		BackstageSet set = new BackstageSet();
		set.androidVersion = androidVersion;
		set.androidCode = androidCode;
		set.iosVersion = iosVersion;
		set.iosCode = iosCode;
		set.iosMsg = iosMsg;
		set.androidMsg = androidMsg;
		set.iosAudit = iosAudit;
		
		if(set.appVersionSet() < 1) 
			flash.error("保存失败!");
		else
			flash.error("保存成功!");
		
		version();
	}
	
	/**
	 * 保存借款app版本
	 */
	public static void saveBorrowVersion(String borrowIosVersion, String borrowIosCode,
			String borrowIosMsg, String borrowAndroidVersion, String borrowAndroidCode,
			String borrowAndroidMsg,String borrowIosAudit) {
		if( StringUtils.isBlank(borrowIosVersion) ||
			StringUtils.isBlank(borrowIosMsg) ||
			StringUtils.isBlank(borrowAndroidVersion) ||
			StringUtils.isBlank(borrowAndroidMsg) ||
			StringUtils.isBlank(borrowIosCode) ||
			StringUtils.isBlank(borrowAndroidCode) ||
			StringUtils.isBlank(borrowIosAudit)
		  ){
			flash.error("请输入正确的数据!");
			
			borrowVersion();
		}
		
		BackstageSet set = new BackstageSet();
		set.borrowAndroidCode = borrowAndroidCode;
		set.borrowAndroidMsg = borrowAndroidMsg;
		set.borrowAndroidVersion = borrowAndroidVersion;
		set.borrowIosCode = borrowIosCode;
		set.borrowIosMsg = borrowIosMsg;
		set.borrowIosVersion = borrowIosVersion;
		set.borrowIosAudit = borrowIosAudit;
		
		if(set.appBorrowVersionSet() < 1) 
			flash.error("保存失败!");
		else
			flash.error("保存成功!");
		
		borrowVersion();
	}
	
}
