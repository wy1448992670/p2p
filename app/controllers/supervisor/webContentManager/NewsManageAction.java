package controllers.supervisor.webContentManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.t_content_news_types;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import constants.Constants;
import constants.Templets;
import controllers.supervisor.SupervisorController;
import business.News;
import business.NewsType;
import business.Supervisor;
import business.WeChatMenu;
import play.Logger;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;

/**
 * 新闻资讯管理
 * 
 * @author zhs
 * 
 */
public class NewsManageAction extends SupervisorController {
	/**
	 * 内容管理
	 */
	public static void contentManagement() {
		String topTypes = params.get("topTypes");
		String typeIdStr = params.get("typeId");
		//String typeIdStr = null;
		String title = params.get("title");
		String orderType = params.get("orderType");
		String orderStatus = params.get("orderStatus");
		
		
		String currPage  = params.get("currPage");
		String pageSize = params.get("pageSize");
		ErrorInfo error = new ErrorInfo();
		
		PageBean<News> page = News.queryNewsBySupervisor(topTypes, typeIdStr, title,
				orderType, orderStatus, currPage, pageSize, error);

		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List <NewsType> types=  NewsType.queryTopTypes( error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<NewsType> childTypes = new ArrayList<NewsType>();
		
		t_content_news_types parentType = null;
		
		if(StringUtils.isNotBlank(typeIdStr)){
			if(NumberUtil.isNumericInt(typeIdStr)) {
				NewsType type = new NewsType();
				type.id = Long.parseLong(typeIdStr);
				parentType = NewsType.queryParentType(type.parentId, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
				
				childTypes = NewsType.queryChildTypes(parentType.id, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
	 		}
		}else if(StringUtils.isNotBlank(topTypes)) {
 			
			if(NumberUtil.isNumericInt(topTypes)) {
				childTypes = NewsType.queryChildTypes(Long.parseLong(topTypes), error);
 	 		}
 			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
 	 	}
		
		
		render(page,types, childTypes, parentType);
	}
	
	/**
	 *添加内容页面
	 */
	public static void contentManagementAdd() {
		ErrorInfo error = new ErrorInfo();
		
		List <NewsType> types=  NewsType.queryTopTypes( error);
		
		render(types);
	}
	
	/**
	 * 用于类别下拉联动
	 * @param parentId
	 */
	public static void typeList(String parentId) {
		ErrorInfo error = new ErrorInfo();
		
		List<t_content_news_types> typeList = NewsType.queryChildTypesForList(parentId, error);
		
		JSONArray json = JSONArray.fromObject(typeList);
		
		renderJSON(json);
	}
	
	/**
	 * 根据类别id查询到新闻条数
	 * @param typeId
	 */
	public static void newsCount(String typeId) {
		ErrorInfo error = new ErrorInfo();
		
		int count = News.queryTotalNewsCountByTypeId(typeId, error);

		renderJSON(count);
	}
	
	/**
	 * 添加内容
	 */
	public static void addContent() {
		ErrorInfo error = new ErrorInfo();
		
		String typeIdStr = params.get("typeId");
		String imageFilename = params.get("filename");
		String imageFilename2 = params.get("filename2");
		String[] locationPc = params.getAll("locationPc");
		String startShowTime = params.get("beginTime");
		String title = params.get("title");
		String author = params.get("author");
		String keywords = params.get("keyword");
		String descriptions = params.get("description");
		String readCount = params.get("readCount");
		String content = params.get("content");
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			contentManagementAdd();
		}
		if (StringUtils.length(content) > 20000){
			flash.error("对不起,新闻内容字数不能超过20000个字符");
			contentManagementAdd();
		}
		
		content = Templets.replaceAllHTML(content);
		
		News news = new News();
		
		if ("16".equals(typeIdStr) && News.queryTotalNewsCountByTypeId(typeIdStr, error) > 0) {
			flash.error("公司介绍类别下的新闻只能有一条！");
			contentManagementAdd();
		}
		
		if ("19".equals(typeIdStr) && News.queryTotalNewsCountByTypeId(typeIdStr, error) > 0) {
			flash.error("招贤纳士类别下的新闻只能有一条！");
			contentManagementAdd();
		}
		
		if(typeIdStr.equals("12")){//成功故事必须上传图片
			news.locationPc = 1;
			if(StringUtils.isBlank(imageFilename)) {
				flash.error("图片不能为空");
				contentManagementAdd();
			}
			
			if(locationPc != null && locationPc.length > 0 && locationPc.length < 5){
				if(locationPc[0].equals("1")){
					if(imageFilename.contains(Constants.DEFAULT_IMAGE)){
						flash.error("请选择上传图片！成功故事推荐至首页必须上传图片");
						contentManagementAdd();
					}
				}
			}
		}
		
		if(locationPc != null && locationPc.length > 0 && locationPc.length < 5) {
			
			if(locationPc[0].equals("1")){
				if(StringUtils.isBlank(imageFilename) || imageFilename.equals(Constants.DEFAULT_IMAGE)) {
					flash.error("图片不能为空");
					contentManagementAdd();
				}
				news.imageFilename = imageFilename;
			}
			int temp = 0;
			for(String location : locationPc) {
				temp += Integer.parseInt(location);
			}
			Logger.info("temp:"+temp);
			news.locationPc =  temp;
			news.locationApp = temp;
			
			if(temp >= 8) {
				if(StringUtils.isBlank(imageFilename2) || imageFilename2.equals(Constants.DEFAULT_IMAGE)) {
					flash.error("APP端活动图片不能为空");
					contentManagementAdd();
				}
				news.imageFilename2 = imageFilename2;
			}
		}else{
			news.locationPc = 0;
			news.locationApp = 0;
		}
		
		if(StringUtils.isBlank(typeIdStr)) {
			flash.error("类别不能为空");
			contentManagementAdd();
		}
				
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			flash.error("类别类型有误");
			contentManagementAdd();
		}
		
//		if(News.orderExist(typeIdStr, order, error)) {
//			flash.error(error.msg);
//			contentManagement();
//		}
		 
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			contentManagementAdd();
		}
		
		if(StringUtils.isBlank(author)) {
			flash.error("作者不能为空");
			contentManagementAdd();
		}
		
		if(StringUtils.isBlank(content)) {
			flash.error("内容不能为空");
			contentManagementAdd();
		}
		
		if(StringUtils.isBlank(keywords)) {
			flash.error("关键字不能为空");
			contentManagementAdd();
		}
		
		String[] splits;
		if(keywords.indexOf(",")!=-1){
		   splits=keywords.split(",");
		}
		else{
		   splits=keywords.split("，");
		}
		
		if(StringUtils.isBlank(descriptions)) {
			flash.error("关键字不能为空");
			contentManagementAdd();
		}
		
		/*String[] splits_;
		if(descriptions.indexOf(",")!=-1){
			splits_= descriptions.split(",");
		}
		else{
			splits_= descriptions.split("，");
		}

		if (splits_.length >5 ) {
			flash.error("关键字描述不能超过五个词");
			contentManagementAdd();
		}*/
		
		if(!StringUtils.isBlank(readCount)) {
			if(!NumberUtil.isNumericInt(readCount)) {
				flash.error("阅读数量类型有误");
				contentManagementAdd();
			}
			news.readCount = Integer.parseInt(readCount);
		}
		
		if(!StringUtils.isBlank(startShowTime)) {
			
			if(!NumberUtil.isDate(startShowTime)) {
				flash.error("显示时间类型有误");
				contentManagementAdd();
			}
			news.startShowTime = DateUtil.strToYYMMDDDate(startShowTime);
		}else{
			news.startShowTime = new Date();
		}
		
		news.imageFilename = imageFilename;
		news.imageFilename2 = imageFilename2;
		news.typeId = Long.parseLong(typeIdStr);
		news.title = title;
		news.content = content.replace("#s", "<img");
		news.author = author;
		news.keyword = keywords;
		news.description = descriptions;
//		news.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		news.addNews(supervisor.id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.success(error.msg);
		contentManagement();
	}
	
	
	/**
	 * 异步判断排序序号是否已经存在
	 */
	public static void ajaxJudgeOrder(String typeId,String order){
		
		 ErrorInfo error = new ErrorInfo();
		 boolean flag = News.orderExist(typeId, order, error);
		 
		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		 JSONObject json = new JSONObject();
		 json.put("msg", flag);
		 
		 renderJSON(json);
	}
	
	/**
	 * 删除内容
	 */
	public static void deletContent(String idStr) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		Supervisor supervisor = Supervisor.currSupervisor();
		News.deleteNews(supervisor.id, idStr, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
        json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 改变显示状态
	 * @param idStr
	 */
	public static void updateUseStatus(String idStr, String useStatus) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		Supervisor supervisor = Supervisor.currSupervisor();
		News.updateNewsUse(supervisor.id, idStr, useStatus, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 搜索内容表
	 */
	public static void searchContent() {
		render();
	}
	
	/**
	 * 编辑初始化
	 */
	public static void editContentInit(long id) {
//		String idStr = params.get("id");
//		if(StringUtils.isBlank(idStr)) {
//			flash.error("新闻不能为空");
//			contentManagement();
//		}
//		
//		if(!NumberUtil.isNumericInt(idStr)) {
//			flash.error("新闻类型有误");
//			contentManagement();
//		}
		
//		ErrorInfo error = new ErrorInfo();
		
//		List<NewsType> topTypes = NewsType.queryTopTypes( error);
		
//		if(error.code < 0) {
//			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
//		}
		
//		long id = Long.parseLong(idStr);
		
		News news = new News();
		
		news.id = id;
		
//		List<NewsType> parentTypes = new ArrayList<NewsType>();
//		
//		if(id != -1) {
//			NewsType parentType = news.type;
//			
//			for(NewsType type :topTypes) {
//				
//				if(type.id == parentType.parentId) {
//					parentTypes = type.childTypes;
//				}
//			}
//		}
		
		render(news);
	}

	
	
	/**
	 * 编辑内容
	 */
	public static void editContent(File imageFile) {
		
		ErrorInfo error = new ErrorInfo();
	
		String idStr = params.get("id");
		long id = Convert.strToLong(idStr, -1);
		if(StringUtils.isBlank(idStr)) {
			flash.error("新闻不能为空");
			editContentInit(id);
		}
		
		if(!NumberUtil.isNumericInt(idStr)) {
			flash.error("新闻类型有误");
			editContentInit(id);
		}
		
		
//		String typeIdStr = params.get("typeId");
//		
//		if(StringUtils.isBlank(typeIdStr)) {
//			flash.error("类别不能为空");
//			contentManagement();
//		}
//		
//		if(!NumberUtil.isNumericInt(typeIdStr)) {
//			flash.error("类别类型有误");
//			contentManagement();
//		}
		
		String title = params.get("edittitle");
		
		if(StringUtils.isBlank(title)) {
			flash.error("标题不能为空");
			editContentInit(id);
		}
		
		String author = params.get("editauthor");
		
		if(StringUtils.isBlank(author)) {
			flash.error("作者不能为空");
			editContentInit(id);
		}
		
		String content = params.get("editcontent");
		
		if(StringUtils.isBlank(content)) {
			flash.error("内容不能为空");
			editContentInit(id);
		}
		
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			flash.error("保存失败");
			editContentInit(id);
		}
		
		if (StringUtils.length(content) > 20000){
			flash.error("对不起,新闻内容字数不能超过20000个字符");
			editContentInit(id);
		}
		
		String keywords = params.get("editkeyword");
		
		String descriptions = params.get("editdescription");
		
		if(StringUtils.isBlank(keywords)) {
			flash.error("关键字不能为空");
			editContentInit(id);
		}
		if(StringUtils.isBlank(descriptions)) {
			flash.error("关键字描述不能为空");
			editContentInit(id);
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
			editContentInit(id);
		}
		
		/*String[] descriptionSplits;
		if(descriptions.indexOf(",") != -1){
			descriptionSplits = descriptions.split(",");
		} else{
			descriptionSplits=keywords.split("，");
		}
*/
		/*if (descriptionSplits.length > 5 ) {
			flash.error("关键字描述不能超过五个词");
			editContentInit(id);
		}*/
		
		String readCount = params.get("editreadCount");
		
		if(StringUtils.isBlank(readCount)) {
			flash.error("阅读数量不能为空");
			editContentInit(id);
		}
		
		if(!NumberUtil.isNumericInt(readCount)) {
			flash.error("阅读数量类型有误");
			editContentInit(id);
		}
		
//		FileType fileType = FileUtil.uploadFile(imageFile, error);
//		
//		if(error.code < 0) {
//			flash.error(error.msg);
//			contentManagement();
//		}
		
//		String imageFilename = fileType.filePath;
		
		News news = new News();
		
		String imageFilename = params.get("filename");
		String imageFilename2 = params.get("filename2");
		String[] locationPc = params.getAll("locationPc");
		String topTypes = params.get("topTypes");
			
		if(topTypes.equals("12")){//成功故事必须上传图片
			news.locationPc = 1;
			if(StringUtils.isBlank(imageFilename)) {
				flash.error("图片不能为空");
				editContentInit(id);
			}
			
			if(locationPc != null && locationPc.length > 0 && locationPc.length < 5){
				if(locationPc[0].equals("1")){
					if(imageFilename.contains(Constants.DEFAULT_IMAGE)){
						flash.error("请选择上传图片！成功故事推荐至首页必须上传图片");
						editContentInit(id);
					}
				}
			}
		}
		
		if(locationPc != null && locationPc.length > 0 && locationPc.length < 5) {
			Logger.info("imageFilename:"+imageFilename);
			if(locationPc[0].equals("1")){
				Logger.info("imageFilename:"+imageFilename);
				if(StringUtils.isBlank(imageFilename) || imageFilename.equals(Constants.DEFAULT_IMAGE)) {
					flash.error("图片不能为空");
					editContentInit(id);
				}
				news.imageFilename = imageFilename;
			}
			int temp = 0;
			for(String location : locationPc) {
				temp += Integer.parseInt(location);
			}
			Logger.info("temp:"+temp);
			news.locationPc =  temp;
			news.locationApp = news.locationPc;
			
			if(temp >= 8) {
				if(StringUtils.isBlank(imageFilename2) || imageFilename2.equals(Constants.DEFAULT_IMAGE)) {
					flash.error("APP端活动图片不能为空");
					editContentInit(id);
				}
				news.imageFilename2 = imageFilename2;
			}
		}else{
			news.locationPc = 0;
			news.locationApp = 0;
		}
		
		String startShowTime = params.get("startShowTime");
		
		if(!StringUtils.isBlank(startShowTime)) {
			
			if(!NumberUtil.isDate(startShowTime)) {
				flash.error("显示时间类型有误");
				editContentInit(id);
			}
		}
		
		
		
//		news.typeId = Long.parseLong(typeIdStr);
		news.title = title;
		news.content = Templets.replaceAllHTML(content);
		news.author = author;
		news.keyword = keywords;
		news.description = descriptions;
		news.readCount = Integer.parseInt(readCount);
		
		news.imageFilename = imageFilename;
		news.imageFilename2 = imageFilename2;
		news.startShowTime = DateUtil.strToYYMMDDDate(startShowTime);
//		news.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		news.updateNews(supervisor.id, id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.error(error.msg);
		contentManagement();
	}

	/**
	 * 确定添加内容
	 */
	public static void okToAdd() {
		render();
	}

	/**
	 * 类别管理
	 */
	public static void categoryManagement() {
		ErrorInfo error = new ErrorInfo();
		
		List <NewsType> types=  NewsType.queryTopTypes( error);
		
		List<WeChatMenu> menus = WeChatMenu.queryMenu(error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(types, menus);
	}

	/**
	 * 编辑类别
	 */
	public static void editCategory() {
		String idStr = params.get("parentId");
		
		if(StringUtils.isBlank(idStr)) {
			flash.error("类型不能为空");
			categoryManagement();
		}
		
		if(!NumberUtil.isNumericInt(idStr)) {
			flash.error("类型有误");
			categoryManagement();
		}
		
		String name = params.get("name");
		
		if(StringUtils.isBlank(name)) {
			flash.error("名称不能为空");
			categoryManagement();
		}
		
		if (StringUtils.length(name) > 8){
			flash.error("名称不能超过8个字符");
			categoryManagement();
		}
		
		if(!name.matches("[\u4e00-\u9fa5\\w]+")) {
			flash.error("名称只能由中文、字母、数字、下划线组成!");
			categoryManagement();
		}
		
		String order = params.get("parentOrder");
		
		if(StringUtils.isBlank(order)) {
			flash.error("排序不能为空");
			categoryManagement();
		}
		
		if(!NumberUtil.isNumericInt(order)) {
			flash.error("排序类型有误");
			categoryManagement();
		}
		
		long id = Long.parseLong(idStr);
		
		ErrorInfo error = new ErrorInfo();
		NewsType type = new NewsType();
		type.id = id;
		String description = params.get("description");
		
		if(NewsType.orderExist(type.parentId, Integer.parseInt(order), error) && type.order != Integer.parseInt(order)) {
			flash.error("排序已存在！");
			categoryManagement();
		}
		
		if(type.parentId == -1) {
			if(StringUtils.isBlank(description)) {
				flash.error("描述不能为空");
			}
		}
		
		type.name = name;
		type.description = description;
		type.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		type.editType(supervisor.id, id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.success(error.msg);
		
		categoryManagement();
	}
	
	/**
	 * 编辑子类别
	 */
	public static void editChildCategory() {
		String idStr = params.get("childId");
		
		if(StringUtils.isBlank(idStr)) {
			flash.error("类型不能为空");
			categoryManagement();
		}
		
		if(!NumberUtil.isNumericInt(idStr)) {
			flash.error("类型有误");
			categoryManagement();
		}
		
		String name = params.get("childName");
		
		if(StringUtils.isBlank(name)) {
			flash.error("名称不能为空");
			categoryManagement();
		}
		
		if(!name.matches("[\u4e00-\u9fa5\\w]+")) {
			flash.error("名称只能由中文、字母、数字、下划线组成!");
			categoryManagement();
		}
		
		String order = params.get("childOrder");
		
		if(StringUtils.isBlank(order)) {
			flash.error("排序不能为空");
			categoryManagement();
		}
		
		if(!NumberUtil.isNumericInt(order)) {
			flash.error("排序类型有误");
			categoryManagement();
		}
		
		long id = Long.parseLong(idStr);
		
		ErrorInfo error = new ErrorInfo();
		NewsType type = new NewsType();
		type.id = id;
		
		if(NewsType.orderExist(type.parentId, Integer.parseInt(order), error) && type.order != Integer.parseInt(order)) {
			flash.error("排序已存在！");
			categoryManagement();
		}
		
		type.name = name;
		type.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		type.editChildType(supervisor.id, id, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		flash.success(error.msg);
		
		categoryManagement();
	}

	/**
	 * 搜索类别
	 */
	public static void searchCategory() {
		render();
	}

	/**
	 * 添加类别
	 */
	public static void addCategory(long parentId, String name, int order) {
		
		
		ErrorInfo error = new ErrorInfo();
		NewsType type = new NewsType();
		
		type._parentId = parentId;
		type.name = name;
		type.order = order;
		
		Supervisor supervisor = Supervisor.currSupervisor();
		type.addChildType(supervisor.id, error);
		
		flash.success(error.msg);
		
		categoryManagement();
	}
	
	/**
	 * 隐藏类型
	 * @param typeIdStr
	 */
	public static void hideType(String typeIdStr) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(StringUtils.isBlank(typeIdStr)) {
			error.msg = "类型有误";
			json.put("error", error);
			renderJSON(json);
		}
		
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			error.msg = "类型转化有误";
			json.put("error", error);
			renderJSON(json);
		}
		
		long typeId = Long.parseLong(typeIdStr);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		NewsType.hideType(supervisor.id, typeId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		json.put("error", error);
		
		renderJSON(json);		
	}
	
	/**
	 * 显示类型
	 * @param typeIdStr
	 */
	public static void showType(String typeIdStr) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		if(StringUtils.isBlank(typeIdStr)) {
			error.msg = "类型有误";
			json.put("error", error);
			renderJSON(json);
		}
		
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			error.msg = "类型有误";
			json.put("error", error);
			renderJSON(json);
		}
		
		long typeId = Long.parseLong(typeIdStr);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		NewsType.showType(supervisor.id, typeId, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		json.put("error", error);
		
		renderJSON(json);	
	}

	/**
	 * 删除子类别
	 */
	public static void deleteSubcategory(String ids) {
		if (StringUtils.isBlank(ids)) {
			flash.error("请选择要删除的子类别");
			
			categoryManagement();
		}
		
		String[] arrIds = ids.split(",");
		
		List<Long> catagoryIds = new ArrayList<Long>();
		
		for (int i = 0; i < arrIds.length; i++) {
			catagoryIds.add(Convert.strToLong(arrIds[i], -1));
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		long supervisorId = supervisor.id;
		ErrorInfo error = new ErrorInfo();
		NewsType.deleteType(supervisorId, catagoryIds.toArray(new Long[catagoryIds.size()]), error);

		renderJSON(error);
	}

	/**
	 * 添加类别的保存
	 */
	public static void saveCategory() {
		render();
	}
}
