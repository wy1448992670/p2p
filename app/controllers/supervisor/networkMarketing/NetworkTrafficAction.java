package controllers.supervisor.networkMarketing;

import business.BackstageSet;
import controllers.supervisor.SupervisorController;
import utils.ErrorInfo;

/**
 * 网络流量统计分析
 * 
 * @author bsr
 * 
 */
public class NetworkTrafficAction extends SupervisorController {

	/**
	 * 流量统计(首页)
	 */
	public static void trafficStatistic(){
		render();
	}
	
	/**
	 * 左边导航栏
	 */
	public static void leftBar(){
		render();
	}
	
	/**
	 * 申请开通
	 */
	public static void applyOpen(){
		boolean flag=true;
		if(flag){
			open();
		}else{
			notOpen();
		}
	}
	
	/**
	 * 已开通
	 */
	public static void open(){
		render();
	}
	
	/**
	 * 未开通
	 */
	public static void notOpen(){
		boolean flag=true;
		if(flag){
			notAccount();
		}else{
			account();
		}
	}
	
	/**
	 * 未开通，无百度账号
	 */
	public static void notAccount(){
		/*
		String url = "http://tongji.baidu.com/web/register";
		String entered_login = "";
		String entered_password = "";
		Map<String, Object> params = new HashMap<String, Object>();
		HttpResponse response = WS.url(url).params(params).post();
		String httpContent = response.getString();
		httpContent = httpContent.replaceAll("href=\"/", "href=\"http://tongji.baidu.com/");
		httpContent = httpContent.replaceAll("src=\"/", "src=\"http://tongji.baidu.com/");
		Logger.info("httpContent:%s", httpContent);*/
		render();
	}
	
	/**
	 * 无百度账号，注册
	 */
	public static void register(){
		render();
	}
	
	/**
	 * 未开通，有百度账号
	 */
	public static void account(){
	    /*
		String url = "https://cas.baidu.com/";
		String entered_login = "ganglework";
		String entered_password = "hopechart7890";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("appid", "12");
		params.put("fromu", "12");
		HttpResponse response = WS.url(url).params(params).post();
		String httpContent = response.getString();
		httpContent = httpContent.replaceAll("href=\"/", "href=\"http://cas.baidu.com/");
		httpContent = httpContent.replaceAll("src=\"/", "src=\"http://cas.baidu.com/");
		httpContent = httpContent.replaceAll("name=\"entered_login\" value=\"\"", "name=\"entered_login\" value=\""+entered_login+"\"");
		httpContent = httpContent.replaceAll("name=\"entered_password\"", "name=\"entered_password\" value=\""+entered_password+"\"");
		Logger.info("httpContent:%s", httpContent);*/
		render();
	}
	
	/**
	 * 有百度账号，登录
	 */
	public static void login(){
		boolean flag=true;
		if(flag){
			loginSuccess();
		}else{
			//没有登录成功的代码
		}
	}
	
	/**
	 * 登录成功
	 */
	public static void loginSuccess(){
		/*String url = "https://cas.baidu.com/?action=login";
		String login = "login";
		String appid = params.get("appid");
		String selfu = params.get("selfu");
		String fromu = params.get("appid");
		String sid = params.get("appid");
		String pwd = params.get("appid");
		String entered_login = params.get("entered_login");
		String entered_password = params.get("entered_password");
		String entered_imagecode = params.get("entered_imagecode");*/
		/*
		Map<String, Object> params = new HashMap<String, Object>();
		//params.put("action", login);
		params.put("appid", appid);
		params.put("selfu", selfu);
		params.put("fromu", fromu);
		params.put("sid", sid);
		params.put("pwd", pwd);
		//params.put("isajax", "");
        params.put("entered_login", entered_login);
        params.put("password_edit", entered_password);
        params.put("entered_imagecode", entered_imagecode);
        Logger.info("params:%s", params);
		HttpResponse response = WS.url(url).params(params).post();
		String httpContent = response.getString();
		Logger.info("httpContent:%s", httpContent);
		*/
		render();
	}
	
	/**
	 * 保存流量统计代码
	 */
	public static void saveNetworkTraffic(String code){
		ErrorInfo error = new ErrorInfo();
		
		BackstageSet backstageSet = new BackstageSet();
		String content = code.replace("#s", "script").replace("#h", "https").replace("#doc", "document");
		backstageSet.baiduCode = content;
		backstageSet.saveBaiduCode(content, error);
		BackstageSet.setCurrentBackstageSet(backstageSet);
		
		if(error.code < 0){
			flash.error(error.msg);
		}
		
		flash.success(error.msg);
		
		editNetworkTraffic();
	}
	
	/**
	 * 编辑流量统计代码
	 */
	public static void editNetworkTraffic(){
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		render(backstageSet);
	}
}
