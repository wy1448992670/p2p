package controllers.supervisor.webContentManager;

import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.sun.org.apache.xerces.internal.impl.Constants;

import utils.ErrorInfo;

import models.t_activity_center;

import business.ActivityCenter;
import controllers.supervisor.SupervisorController;

public class ActivityCenterAction extends SupervisorController{
	
	/*
	 * 查询活动中心内容
	 */
	public static void activityCenter(String title){
		
		
		List<t_activity_center> t = ActivityCenter.queryActivityCenter(title);
		render(t,title);
	}
	
	/**
	 * 根据id删除活动中心内容
	 * @param id
	 */
	public static void deleteActivityCenter(long id){
		ActivityCenter.deleteActivityCenter(id);
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		error.code = 1;
		error.msg = "删除成功";
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 初始化添加活动页面
	 */
	public static void addActivityCenter(){
		
		render();
	}
	
	
	public static void eidtActivityCenter(long id){
		t_activity_center t = ActivityCenter.queryActivityCenter(id);
		render(t);
	}
	
	
	
	/**
	 * 生成活动内容
	 */
	public static void submitActivityCenter(){
		ErrorInfo error = new ErrorInfo();
		String titles = params.get("titles");
		String url = params.get("url");
		String order = params.get("order");
		String first_image_url = params.get("filename");
		String info_image_url = params.get("filename2");
		String rule_image_url = params.get("filename3");
		
		if (StringUtils.isBlank(order)) {
			flash.error("排序不能为空");
			activityCenter(null);
		}
		
		
		if (StringUtils.isBlank(url)) {
			flash.error("前往页面不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(titles)) {
			flash.error("标题不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(first_image_url)) {
			flash.error("首页图片路径不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(info_image_url)) {
			flash.error("活动详情图片路径不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(rule_image_url)) {
			flash.error("活动规则图片路径不能为空");
			activityCenter(null);
		}
		
		ActivityCenter activity = new ActivityCenter();
		activity.title = titles;
		activity.url = url;
		activity.order = Integer.parseInt(order);
		activity.first_image_url = first_image_url;
		activity.info_image_url = info_image_url;
		activity.rule_image_url = rule_image_url;
		activity.location = "APP活动中心";
		activity.resolution = "1024*768";
		activity.file_size = "不超过2M";
		activity.file_format = "JPEG";
		activity.saveActivity(error);
		
		flash.error(error.msg);
		activityCenter(null);
		
	}
	
	/**
	 * 编辑提交
	 * @param id
	 */
	public static void eidtActivityCenterByid(long id){
		ErrorInfo error = new ErrorInfo();
		
		String titles = params.get("titles");
		String url = params.get("url");
		String order = params.get("order");
		String first_image_url = params.get("filename");
		String info_image_url = params.get("filename2");
		String rule_image_url = params.get("filename3");
		
		if(id == 0){
			flash.error("参数错误");
			activityCenter(null);
		}
		
		if(id == 0){
			flash.error("排序不能为空");
			activityCenter(null);
		}
		
		if (StringUtils.isBlank(order)) {
			flash.error("排序不能为空");
			activityCenter(null);
		}
		
		
		if (StringUtils.isBlank(url)) {
			flash.error("前往页面不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(titles)) {
			flash.error("标题不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(first_image_url)) {
			flash.error("首页图片路径不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(info_image_url)) {
			flash.error("活动详情图片路径不能为空");
			activityCenter(null);
		}
		if (StringUtils.isBlank(rule_image_url)) {
			flash.error("活动规则图片路径不能为空");
			activityCenter(null);
		}
		
		ActivityCenter activity = new ActivityCenter();
		activity.id = id;
		activity.title = titles;
		activity.url = url;
		activity.order = Integer.parseInt(order);
		activity.first_image_url = first_image_url;
		activity.info_image_url = info_image_url;
		activity.rule_image_url = rule_image_url;
		activity.eidtActivityCenter(error);
		
		flash.error(error.msg);
		activityCenter(null);
		
	}
}
