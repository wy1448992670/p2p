package controllers.supervisor.webContentManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;

import net.sf.json.JSONObject;

import models.t_forum_posts_answers;
import models.t_forum_posts;
import models.t_forum_type;
import models.v_forum_posts;
import models.v_posts_questions;

import business.Ads;
import business.News;
import business.Posts;
import business.Supervisor;
import constants.Constants;
import constants.Templets;
import controllers.supervisor.SupervisorController;

public class PostsAction extends SupervisorController{
	
	/**
	 * 显示发帖类型
	 */
	public static void showPostsType(){
		List<t_forum_type> list = Posts.getForumTypeList(0);
		render(list);
	}
	
	/**
	 * 修改发帖类型状态
	 * @param idStr
	 * @param statusStr
	 */
	public static void updatePostsType(String idStr, String statusStr){
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		Posts.updatePostsType(supervisor.id,idStr,statusStr,error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 根据帖子类型ID查询信息
	 * @param adsId
	 */
	public static void queryPostsType(long id){
		
		render(id);
	}
	
	/**
	 * 编辑发帖类型内容
	 * @param id
	 * @param name
	 * @param rId
	 */
	public static void editPostsType(long id,String name,int rId){
		ErrorInfo error = new ErrorInfo();
		if(StringUtils.isBlank(name)){
			flash.error("名称为空，编辑失败");
			showPostsType();
		}
		
		Posts.updatePostsTypeInfo(id,name,rId,error);
		if(error.code < 0){
			flash.error("编辑失败");
		}else{
			flash.error("编辑成功");
		}
		showPostsType();
	}
	
	/**
	 * 查看论坛帖子信息
	 * @param postsType
	 */
	public static void showForumPosts(int postsType,String title){
		
		//String title = params.get("title");
	
		String orderType = params.get("orderType");
		String orderStatus = params.get("orderStatus");
		
		
		String currPage  = params.get("currPage");
		String pageSize = params.get("pageSize");
		ErrorInfo error = new ErrorInfo();
		
		PageBean<v_forum_posts> page = Posts.queryForumPosts(postsType,  title,
				orderType, orderStatus, currPage, pageSize, error,0);
		
		List<t_forum_type> typeList = Posts.queryForumType();
		render(page,typeList,postsType,title);
	}
	
	/**
	 * 修改帖子置顶状态
	 * @param id
	 * @param rId
	 */
	public static void updatePostsrId(long id, int rId){
		ErrorInfo error = new ErrorInfo();
		
		Posts.updatePostsTypeStatue(id,rId,error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		int upId = rId == 0 ? 1 : 0;
		json.put("upId", upId);
		renderJSON(json);
	}
	
	/**
	 * 修改帖子显示状态
	 * @param id
	 * @param statue
	 */
	public static void updatePostsStatue(long id, int status){
		ErrorInfo error = new ErrorInfo();
		
		Posts.updatePostsStatue(id,status,error);
		JSONObject json = new JSONObject();
		json.put("error", error);
		int upStatus = status == 0 ? 1 : 0;
		json.put("upStatus", upStatus);
		renderJSON(json);
	}
	
	/**
	 * 添加帖子页面
	 */
	public static void addPosts(){
		List<t_forum_type> typeList = Posts.queryForumType();
		render(typeList);
	}
	
	/**
	 * 添加论坛帖子
	 * @param show_image
	 */
	public static void submitAdd(int showImage){
		ErrorInfo error = new ErrorInfo();
		
		String typeIdStr = params.get("typeId");
		
	
		String startShowTime = params.get("beginTime");
		String title = params.get("title");
		String author = params.get("author");
		String keywords = params.get("keyword");
	
		String content = params.get("content");
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			addPosts();
		}
		if (StringUtils.length(content) > 20000){
			flash.error("对不起,内容字数不能超过20000个字符");
			addPosts();
		}
		
		content = Templets.replaceAllHTML(content);
		
		if(StringUtils.isBlank(typeIdStr)) {
			flash.error("类别不能为空");
			addPosts();
		}
				
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			flash.error("类别类型有误");
			addPosts();
		}
		
		 
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			addPosts();
		}
		
		if(StringUtils.isBlank(author)) {
			flash.error("作者不能为空");
			addPosts();
		}
		
		if(StringUtils.isBlank(content)) {
			flash.error("内容不能为空");
			addPosts();
		}
		
		if(StringUtils.isBlank(keywords)) {
			flash.error("关键字不能为空");
			addPosts();
		}
		
		String[] splits;
		if(keywords.indexOf(",")!=-1){
		   splits=keywords.split(",");
		}
		else{
		   splits=keywords.split("，");
		}

		if (splits.length >5 ) {
			flash.error("关键字不能超过五个词");
			addPosts();
		}
		
		Posts tPosts = new Posts();
		
		if(!StringUtils.isBlank(startShowTime)) {
			
			if(!NumberUtil.isDate(startShowTime)) {
				flash.error("显示时间类型有误");
				addPosts();
			}
			tPosts.show_time = DateUtil.strToYYMMDDDate(startShowTime);
		}else{
			tPosts.show_time = new Date();
		}
		
		tPosts.user_id = 0;
		tPosts.type_id = Integer.parseInt(typeIdStr);
		tPosts.show_image = showImage;
		tPosts.title = title;
		tPosts.content = content.replace("#s", "<img");
		tPosts.name = author;
		tPosts.keywords = keywords;
//		news.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		tPosts.addPostsInfo(supervisor.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.error(error.msg);
		showForumPosts(0,null);
	}
	
	/**
	 * 删除帖子
	 * @param id
	 */
	public static void deletePosts(long id){
		ErrorInfo error = new ErrorInfo();
		Supervisor supervisor = Supervisor.currSupervisor();
		Posts.deletePosts(id,supervisor.id,error);
		flash.error("帖子删除成功");
		showForumPosts(0,null);
	}
	
	/**
	 * 编辑页面
	 * @param id
	 */
	public static void editPostsContent(long id){
		t_forum_posts tPosts= Posts.queryPosts(id);
		render(tPosts);
	}
	
	/**
	 * 编辑帖子信息
	 * @param showImage
	 * @param postsId
	 */
	public static void submitEdit(int showImage,long postsId){
		ErrorInfo error = new ErrorInfo();
		
		if(postsId == 0){
			flash.error("编辑页面参数失败");
			editPostsContent(postsId);
		}
		
	
		String startShowTime = params.get("beginTime");
		String title = params.get("title");
		String author = params.get("author");
		String keywords = params.get("keyword");
	
		String content = params.get("content");
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			editPostsContent(postsId);
		}
		if (StringUtils.length(content) > 20000){
			flash.error("对不起,新闻内容字数不能超过20000个字符");
			editPostsContent(postsId);
		}
		
		content = Templets.replaceAllHTML(content);
		
		 
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			editPostsContent(postsId);
		}
		
		if(StringUtils.isBlank(author)) {
			flash.error("作者不能为空");
			editPostsContent(postsId);
		}
		
		if(StringUtils.isBlank(content)) {
			flash.error("内容不能为空");
			editPostsContent(postsId);
		}
		
		if(StringUtils.isBlank(keywords)) {
			flash.error("关键字不能为空");
			editPostsContent(postsId);
		}
		
		String[] splits;
		if(keywords.indexOf(",")!=-1){
		   splits=keywords.split(",");
		}
		else{
		   splits=keywords.split("，");
		}

		if (splits.length >5 ) {
			flash.error("关键字不能超过五个词");
			editPostsContent(postsId);
		}
		
		Posts tPosts = new Posts();
		
		if(!StringUtils.isBlank(startShowTime)) {
			
			if(!NumberUtil.isDate(startShowTime)) {
				flash.error("显示时间类型有误");
				editPostsContent(postsId);
			}
			tPosts.show_time = DateUtil.strToYYMMDDDate(startShowTime);
		}else{
			tPosts.show_time = new Date();
		}
		tPosts.id = postsId;
		tPosts.show_image = showImage;
		tPosts.title = title;
		tPosts.content = content.replace("#s", "<img");
		tPosts.name = author;
		tPosts.keywords = keywords;
		
		Supervisor supervisor = Supervisor.currSupervisor();
		tPosts.editPostsInfo(supervisor.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.error(error.msg);
		showForumPosts(0,null);
	}
	
	/**
	 * 根据id显示回复信息
	 * @param id
	 */
	public static void showUserQuestions(long id,int answerStatus,long userId){
		ErrorInfo error = new ErrorInfo();
		
		String countent = params.get("countent");
		String currPageStr  = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		PageBean<v_posts_questions> page = Posts.showUserQuestions(id, answerStatus, countent, currPageStr, pageSizeStr, error,userId,0l);
		
		render(page,id);
	}
	
	/**
	 * 删除用户回帖
	 * @param id
	 */
	public static void deleteUserQuestions(long id){
		int count = Posts.deleteUserQuestions(id);
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		error.code = 0;
		error.msg = "删除成功";
		
		if(count == 0){
			error.code = -1;
			error.msg = "删除失败";
		}
		json.put("error", error);
		renderJSON(json);
	}
	
	/**
	 * 显示管理员回复
	 * @param id
	 */
	public static void showAdminAnswers(long id,long postsId,long userId){
		List<t_forum_posts_answers> t = Posts.queryAdminAnswers(id);
		render(id,t,postsId,userId);
	}
	
	/**
	 * 管理员回复操作
	 * @param id
	 * @param constent
	 */
	public static void saveAdminAnswers(long id,String constent,long postsId,long userId){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		if(id <= 0){
			error.code = -1;
			error.msg = "参数错误";
		}
		
		if(postsId <= 0){
			error.code = -1;
			error.msg = "参数错误";
		}
		
		if(StringUtils.isBlank(constent)){
			error.code = -1;
			error.msg = "回复内容不能为空";
		}
		
		Posts p = new Posts();
		p.question_id = id;
		p.content = constent;
		p.type = 1;
		p.user_id = userId;
		p.posts_id = postsId;
		p.question_user_name = "管理员";
		p.saveAdminAnswers(error);
		
		json.put("error", error);
		renderJSON(json);
	}
}
