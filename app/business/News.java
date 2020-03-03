package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_content_advertisements_partner;
import models.t_content_news;
import models.t_content_news_types;
import models.t_users;
import models.v_content_wechat_news;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DataUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.PushMessage;

import com.shove.Convert;

import constants.Constants;
import constants.SupervisorEvent;

/**
 * 新闻咨询管理
* @author cp
* @version 6.0
* @created 2014年3月24日 下午8:25:25
 */
public class News implements Serializable{
	public long id=-1L;
	private long _id;
	public Date time;
	
	public NewsType type;
	public long typeId;
	
	public NewsType getType() {
		if(this.type == null) {
			type = new NewsType();
			
			type.id = this.typeId;
		}
		
		return type;
	}
	
	public String title;//标题
	public String author;
	public String content;
	
	/**
	 * 前台显示
	 */
	public String keywords;
	 
	public String getKeywords() {
		if(keyword != null) {
			keywords = keyword.replace(",", "  ");
		}
		
		return keywords;
	}
	public String descriptions;
	public String getDescriptions() {
		if(description != null) {
			descriptions = description.replace(",", "  ");
		}
		
		return descriptions;
	}
	/**
	 * 后台提交/显示
	 */
	public String keyword;
	public String description;
	
	public int readCount;
	
	/**
	 * 1 PC 
	 * 2 APP
	 * 3 PC和APP
	 */
	public int showType;
	/**
	 * 0 不推荐
	 * homepage 1 推荐至首页
	 * marquee 2 荐跑马灯效果区
	 * top 3 推荐首页头条
	 */
	public int locationPc;
	public int locationApp;
	public String imageFilename;
	public String imageFilename2;
	public boolean isUse;
	public Date startShowTime;
	public int order;
	public int support;
	public int opposition;

	public long getId() {
		
		return _id;
	}

	public void setId(long id) {
		
//		if(id < 1) {
//			this._id = -1;
//			
//			return ;
//		}
		
		t_content_news news = null;
		
		try{
			news = t_content_news.findById(id);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("News setId时："+e.getMessage());
			
			this._id = -1;
			
			return;
		}
		
		if(news == null) {
			this._id = -1;
			
			return ;
		}
		
		this._id = id;
		this.time = news.time;
		
		this.type = new NewsType();
		type.id = news.type_id;
		
		this.title = news.title;
		this.author = news.author;
		this.content = news.content;
		
		this.keyword = news.keywords;
		this.description = news.descriptions;
		this.readCount = news.read_count;
//		this.location = news.location;
		this.imageFilename = news.image_filename;
		this.imageFilename2 = news.image_filename2;
		this.isUse = news.is_use;
		this.startShowTime = news.start_show_time;
		this.order = news._order;
		this.locationPc = news.location_pc;
		this.support = news.support;
		this.opposition = news.opposition;
	}
	
	/**
	 * 添加新闻
	 * @param obj
	 */
	public int addNews(long supervisorId, ErrorInfo error){
		error.clear();
		
		t_content_news news = new t_content_news();
		
//		if(orderExist(this.typeId+"", this.order+"", error)) {
//			error.code = -1;
//			error.msg = "该排序已存在";
//			
//			return error.code;
//		}
		
		news.time = new Date();
		news.type_id = this.typeId;
		news.title = this.title;
		news.content = this.content;
		news.author = this.author;
		
		news.keywords = this.keyword;
		news.descriptions = this.description;
		news.read_count = this.readCount;
		
		news.image_filename = this.imageFilename;
		news.image_filename2 = this.imageFilename2;
		news.show_type = this.showType;
		news.location_pc = this.locationPc;
		news.location_app = this.locationApp;
		
		news.is_use = true;
		news.start_show_time = this.startShowTime;
		news._order = this.order;
		
		try {
			news.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "保存内容失败";
			
			return -2;
		}
		
		if(this.typeId == 35) {
			PushMessage.pushNoticeMessage("{\"title\":\""+news.title+"\",\"description\":\"您有一个新的活动消息\",\"custom_content\":{\"id\":\""+news.id+"\",\"type\":\""+Constants.PUSH_ACTIVITY_TYPE+"\"}}",
					"{\"title\":\""+news.title+"\",\"description\":\"您有一个新的活动消息\",\"id\":\""+news.id+"\",\"type\":\""+Constants.PUSH_ACTIVITY_TYPE+"\"}");
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ADD_NEWS,
				"添加新闻", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "新闻添加成功";
		this._id = news.id;
		
		return 0;
	}

	/**
	 * 编辑后更新新闻
	 * @param obj
	 */
	public int updateNews(long supervisorId, long adsId, ErrorInfo error) {
		error.clear();
		
		t_content_news news = null;
		
		try {
			news = t_content_news.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑保存新闻时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询新闻内容失败";
			
			return error.code;
		}
		
		if(news == null) {
			error.code = -1;
			error.msg = "该新闻不存在，传入参数有误";
			
			return error.code;
		}
		
//		if(orderExist(typeId+"", order+"", error)) {
//			error.code = -4;
//			error.msg = "该排序已存在";
//			
//			return error.code;
//		}
		
//		news.time = new Date();
//		news.type_id = this.typeId;
		news.title = this.title;
		news.content = this.content;
		news.author = this.author;
		
		news.keywords = this.keyword;
		news.descriptions = this.description;
		news.read_count = this.readCount;
		news.image_filename = this.imageFilename;
		news.image_filename2 = this.imageFilename2;
		news.show_type = this.showType;
		news.location_pc = this.locationPc;
		news.location_app = this.locationApp;
		
		news.is_use = true;
		news.start_show_time = this.startShowTime;
		news._order = this.order;
		
		try {
			news.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "保存内容失败";
			
			return -1;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_NEWS,
				"编辑新闻", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "内容保存成功";
		
		return 0;
	}
	
	/**
	 * 删除新闻
	 * @param supervisorId
	 * @param adsIdStr
	 * @param error
	 * @return
	 */
	public static int deleteNews(long supervisorId, String adsIdStr, ErrorInfo error) {
		error.clear();
		
		if(!NumberUtil.isNumericInt(adsIdStr)) {
			error.code = -1;
			error.msg = "查询新闻内容失败";
			
			return error.code;
		}
		
		long adsId = Long.parseLong(adsIdStr);
		
		String sql = "delete t_content_news where id = ?";
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, adsId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("删除新闻时：" + e.getMessage());
			error.code = -1;
			error.msg = "删除新闻失败";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.DEL_NEWS,
				"编辑新闻", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "删除新闻成功";
		
		return error.code;
	}
	
	/**
	 * 更新新闻使用状态
	 * @param supervisorId
	 * @param adsIdStr
	 * @param isUseStr
	 * @param error
	 * @return
	 */
	public static int updateNewsUse(long supervisorId, String adsIdStr, String isUseStr, ErrorInfo error) {
		error.clear(); 
		
		if(!NumberUtil.isNumericInt(isUseStr)) {
			error.code = -1;
			error.msg = "传入状态参数有误";
			
			return error.code;
		}
		
		int isUse = Integer.parseInt(isUseStr);
		
		if(isUse!=0 && isUse!=1) {
			error.code = -1;
			error.msg = "传入状态参数有误";
			
			return error.code;
		}
		
		boolean use = isUse == 0 ? false : true;
		
		if(!NumberUtil.isNumericInt(adsIdStr)) {
			error.code = -1;
			error.msg = "传入新闻参数有误";
			
			return error.code;
		}
		
		long adsId = Long.parseLong(adsIdStr);
		
		String sql = "update t_content_news set is_use = ? where id = ?";
		
		int rows = 0;
		
		try {
			rows = JpaHelper.execute(sql, use, adsId).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新新闻使用状态时：" + e.getMessage());
			error.code = -1;
			error.msg = "更新新闻使用状态失败";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "更新显示状态成功";
		
		return error.code;
	}
	
	/**
	 * 判断排序是否存在
	 * @param typeIdStr
	 * @param orderStr
	 * @param error
	 * @return true存在 false不存在
	 */
	public static boolean orderExist(String typeIdStr, String orderStr, ErrorInfo error) {
		if(!NumberUtil.isNumericInt(typeIdStr)) {
			error.code = -1;
			error.msg = "传入状态参数有误";
			
			return true;
		}
		
		if(!NumberUtil.isNumericInt(orderStr)) {
			error.code = -1;
			error.msg = "传入排序参数有误";
			
			return true;
		}
		
		long typeId = Long.parseLong(typeIdStr);
		int order = Integer.parseInt(orderStr);
		
		long count = 0;
		
		try {
			count = t_content_news.count("type_id = ? and _order = ?", typeId, order);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("更新新闻使用状态时：" + e.getMessage());
			error.code = -1;
			error.msg = "更新新闻使用状态失败";
			
			return true;
		}
		
		if(count > 0) {
			error.msg = "排序序号已经存在";
			return true;
		}
		
		error.code = 0;
		
		return false;
		
	}
	
	/**
	 * 查询所有的新闻
	 * @param obj
	 * @return
	 */
	public static PageBean<News> queryNewsBySupervisor(String topTypes, String typeIdStr,
			String title, String orderTypeStr, String orderStatus,
			String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();
		
		long topTypeId = 0;
		long typeId = 0;
		int orderType = 0;
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		StringBuffer types = new StringBuffer(" and type_id in (");
		
 		if(StringUtils.isNotBlank(typeIdStr)){
 			if(NumberUtil.isNumericInt(typeIdStr)) {
 				typeId = Integer.parseInt(typeIdStr);
 	 		}
 		}else {
 			if(StringUtils.isNotBlank(topTypes)) {
 				if(NumberUtil.isNumericInt(topTypes)) {
 					topTypeId = Integer.parseInt(topTypes);
 	 	 		}
 			}
 	 	}
		
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(orderType < 0 || orderType > 10) {
 			orderType = 0;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
 		//根据子类id获取其名称和id以及父类飞名称和id(用于显示在前台条件查询)
 		
 		if(StringUtils.isNotBlank(typeIdStr)){
 			if(typeIdStr != null){
 	 			NewsType childType = new NewsType();
 	 	 		childType.id = typeId;
 	 	 		childType.parentType.id = childType.parentId;
 	 	 		
 	 	 		conditionMap.put("typeName", childType.name);
 	 	 		conditionMap.put("parentId", childType.parentType.id);
 	 	 		conditionMap.put("parentName", childType.parentType.name);
 	 		}
 		}
 		
 		conditionMap.put("topTypes", topTypeId);
		conditionMap.put("typeId", typeId);
		conditionMap.put("title", title);
		conditionMap.put("orderType", orderType);
 		
		StringBuffer conditions = new StringBuffer("1=1 ");
		List<Object> values = new ArrayList<Object>();
		
		if(typeId != 0 ) {
			conditions.append("and type_id = ? ");
			values.add(typeId);
		}else if(typeId == 0 && topTypeId != 0) {
			List<Long> typeList = t_content_news_types.find("select id from t_content_news_types where parent_id = ?", topTypeId).fetch();
			
			for(long type : typeList) {
				types.append(type+",");
			}
			
			String temp = types.substring(0, types.length()-1);
			
			conditions.append(temp+") ");
		}
		
		if(StringUtils.isNotBlank(title)) {
			conditions.append("and title like ? ");
			values.add("%"+title+"%");
		}
		
		conditions.append(Constants.NEW_ORDER[orderType]);
		
		/* 升降序 */
		if(StringUtils.isNotBlank(orderStatus) && orderType > 0){
			
			if(Integer.parseInt(orderStatus) == 1)
				conditions.append(" ASC");
			else
				conditions.append(" DESC");
			
			conditionMap.put("orderStatus", orderStatus);
		}
		
		List<t_content_news> contents = new ArrayList<t_content_news>();
		int count = 0;
		
		try {
			count = (int)t_content_news.count(conditions.toString(), values.toArray());
			contents = t_content_news.find(conditions.toString(), values.toArray()).fetch(currPage, pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		
		List<News> news = new ArrayList<News>();
		
		News aNews = null;
		
		for(t_content_news content : contents) {
			aNews = new News();
			
			aNews._id = content.id;
			aNews.time = content.time;
			aNews.typeId = content.type_id;
			aNews.title = content.title;
			aNews.locationPc = content.location_pc;
			aNews.readCount = content.read_count;
			aNews.isUse = content.is_use;
			aNews.startShowTime = content.start_show_time;
			aNews.order = content._order;
			
			news.add(aNews);
		}
		
		PageBean<News> page = new PageBean<News>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = news;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 用户财富资讯首页
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static List<t_content_news> queryNewForFront(long type_id, int size, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_content_news(id, title,content, read_count,image_filename,time) from t_content_news where "
				+ "type_id = ? and is_use = true and start_show_time <= ? "
				+ " order by time desc";
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		
		try {
			news = t_content_news.find(sql, type_id, new Date()).fetch(1, size);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		return news;
	}
	
	/**
	 * 财富咨询首页（推荐至首页）
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static List<t_content_news> queryNewForFrontHome(ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_content_news(id, title, image_filename, _order) from t_content_news where "
				+ "location_pc in(1,3,5,7) and start_show_time <= ? and is_use = true order by _order";
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		
		try {
			news = t_content_news.find(sql, new Date()).fetch(1, 4);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		return news;
	}
	
	/**
	 * 财富咨询首页（推荐至首页头条）
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static List<t_content_news> queryNewForFrontHeadlines(ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_content_news(id, title, content) from t_content_news where "
				+ "location_pc in(4,5,6,7) and start_show_time <= ? and is_use = true order by _order";
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		
		try {
			news = t_content_news.find(sql, new Date()).fetch(1, 2);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		return news;
	}
	
	/**
	 * 财富咨询首页（跑马灯效果）
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static List<t_content_news> queryNewForFrontMarquee(ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_content_news(id, title) from t_content_news where "
				+ "location_pc in(2,3,6,7,10,11,14,15) and start_show_time <= ? and is_use = true order by _order";
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		
		try {
			news = t_content_news.find(sql, new Date()).fetch(1, 5);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		return news;
	}
	
	/**
	 * 新闻列表
	 * @param typIdStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean <t_content_news>  queryNewsByTypeId(String typIdStr, String currPageStr,
			String pageSizeStr, String keyword, ErrorInfo error) {
		
		error.clear();
		
		int currPage = Constants.ONE;
 		int pageSize = 5;
 		long typeId = -1L;
 		
 		if(StringUtils.isBlank(currPageStr)){
 			currPage = Constants.ONE;
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		
 		if(NumberUtil.isNumericInt(typIdStr)) {
 			typeId = Long.parseLong(typIdStr);
 		}
 		
 		if(typeId == -1) {
 			error.code = -1;
 			error.msg = "传入参数有误";
 			
 			return null;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
 		conditionMap.put("typeId", typeId);
 		
 		String searchCondition = "";
 		List<Object> params = new ArrayList<Object>();
 		params.add(typeId);
 		params.add(new Date());
		
		if (StringUtils.isNotBlank(keyword)) {
			conditionMap.put("keyword", keyword);
			searchCondition = " and news.title like ?";
			params.add("%" + keyword + "%");
		}
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		int count = 0;
		
		String sqlCount = "select count(*) from "
				+ "t_content_news news where news.type_id = ? and news.is_use = true and "
				+ "news.start_show_time <= ? " + searchCondition;
		
		String sqlPage = "select new t_content_news(id, title, content, read_count, image_filename, time,start_show_time) from "
				+ "t_content_news news where news.type_id = ? and news.is_use = true and "
				+ "news.start_show_time <= ? " + searchCondition + " order by time desc";
		
		try {
			count = (int)t_content_news.count(sqlCount, params.toArray());
			news = t_content_news.find(sqlPage, params.toArray()).fetch(currPage, pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询财富资讯列表情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，财富资讯列表失败";
			
			return null;
		}
		
		PageBean<t_content_news> page = new PageBean<t_content_news>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = news;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 新闻列表
	 * @param typIdStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static List <t_content_news>  queryNewsByTypeIds(String typIdStr, ErrorInfo error) {
		
		error.clear();
		
 		long typeId = -1L;
 		
 		if(NumberUtil.isNumericInt(typIdStr)) {
 			typeId = Long.parseLong(typIdStr);
 		}
 		
 		if(typeId == -1) {
 			error.code = -1;
 			error.msg = "传入参数有误";
 			
 			return null;
 		}
 		
 		String searchCondition = "";
 		List<Object> params = new ArrayList<Object>();
 		params.add(typeId);
 		params.add(new Date());
		
		List<t_content_news> news = new ArrayList<t_content_news>();
		
		String sqlPage = "select new t_content_news(id, title, content, read_count, image_filename, time,start_show_time) from "
				+ "t_content_news news where news.type_id = ? and news.is_use = true and "
				+ "news.start_show_time <= ? " + searchCondition + " order by news._order,time";
		
		try {
			news = t_content_news.find(sqlPage, params.toArray()).fetch(5);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询财富资讯列表情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，财富资讯列表失败";
			
			return null;
		}
		
		return news;
	}
	
	/**
	 * 新闻详情
	 * @param idStr
	 * @param error
	 * @return
	 */
	public static List<News> queryNewsDetail(String idStr, String keyword, ErrorInfo error) {
		long id = 0;
 		if(!NumberUtil.isNumericInt(idStr)) {
 			error.code = -1;
 			error.msg = "传入参数有误";
 			
 			return null;
 		}
 		
 		id = Convert.strToLong(idStr, -1);
 		String searchCondition = "";
 		List<Object> params = new ArrayList<Object>();
 		params.add(id);
 		params.add(id);
 		params.add(id);
 		params.add(id);
		
		if (StringUtils.isNotBlank(keyword)) {
			searchCondition = " and title like ? ";
			params.add("%" + keyword + "%");
		}
 		
 		String sqlLast = "select new t_content_news(id,title) from t_content_news " +
 				"where type_id=(select type_id from t_content_news where id=?) " +
 				"and is_use=true and (_order<(select _order from t_content_news where id=?) or " +
 				"(_order=(select _order from t_content_news where id=?) and time<(select time from t_content_news where id=?))) " +
 				searchCondition + "order by _order desc,time desc";
 		
 		String sqlNext = "select new t_content_news(id,title) from t_content_news " +
 				"where type_id=(select type_id from t_content_news where id=?) " +
 				"and is_use=true and (_order>(select _order from t_content_news where id=?) or " +
 				"(_order=(select _order from t_content_news where id=?) and time>(select time from t_content_news where id=?))) " +
 				searchCondition + "order by _order,time";
 		
 		t_content_news newsLast = null;
 		t_content_news newsNext = null;
 		
 		try {
 			newsLast = t_content_news.find(sqlLast, params.toArray()).first();
 			newsNext = t_content_news.find(sqlNext, params.toArray()).first();
 			
 		} catch(Exception e) {
 			e.printStackTrace();
 			Logger.info("查询新闻的上一条和下一条记录时："+e.getMessage());
 			error.code = -1;
 			error.msg = "查询新闻的上一条和下一条记录失败";
 			
 			return null;
 		}
 		
 		List<News> newsDetail = new ArrayList<News>();
 		
 		News news = new News();
 		news.id = id;
 		
 		News _newsLast = null;
 		News _newsNext = null;
 		
 		if(newsLast != null) {
 			_newsLast = new News();
 			_newsLast._id = newsLast.id;
 			_newsLast.title = newsLast.title;
 		}
 		
 		if(newsNext != null) {
 			_newsNext = new News();
 			_newsNext._id = newsNext.id;
 			_newsNext.title = newsNext.title;
 		}
 		
 		newsDetail.add(_newsLast);
 		newsDetail.add(news);
 		newsDetail.add(_newsNext);
 		
 		JpaHelper.execute("update t_content_news set read_count = read_count+1"
					+ "where id = ?", id).executeUpdate();
 		
 		error.code = 0;
 		
 		return newsDetail;
	}
	
	/**
	 * 根据类别id查询到所有的该类别下的所有新闻条数
	 * @param typeIdStr
	 * @param error
	 * @return
	 */
	public static int queryTotalNewsCountByTypeId(String typeIdStr,ErrorInfo error){
		error.clear();
		error.code = -1;
		
		long typeId = -1;
		String sql = "SELECT COUNT(1) FROM t_content_news t WHERE t.type_id = ?";
		
		if (null != typeIdStr && NumberUtil.isNumeric(typeIdStr) == true) {
			typeId = Long.parseLong(typeIdStr);
		}
		
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql).setParameter(1, typeId);
		query.setMaxResults(1);
		
		try {
			error.code = 0;
			
			return query.getResultList().get(0) == null ? 0 : Integer.parseInt(query.getResultList().get(0).toString());
		} catch (Exception e) {
			error.code = -1;
			error.msg = "根据类别id查询新闻条数时有误！";
			Logger.error("根据类别id查询新闻条数时：" + e.getMessage());
			
			return 0;
		}
	}
	
	
	/**
	 * 查询资讯总数
	 * @param error
	 * @return
	 */
	public static long queryTotalNewsCount(ErrorInfo error) {
		error.clear();
		long count = 0;

		try {
			count = t_content_news.count();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("数据库异常");
			error.code = -1;
			error.msg = "查询资讯总数失败";
			
			return -1;
		}
		
		error.code = 0;
		
		return count;
	}
	
	/**
	 * 赞
	 * @param newsId
	 * @param error
	 * @return
	 */
	public static int support(long newsId, ErrorInfo error) {
		error.clear();
		
		if(!News.checkSO(newsId)) {
			error.code = -1;
			error.msg = "别赞了，休息一会儿!";
			
			return -1;
		}
		
		String sqlUpdate = "update t_content_news set support = support + 1 where id = :id";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("id", newsId);
		
		int support = 0;

		int rows = 0;
		
		try {
			rows = queryUpdate.executeUpdate();
			support = t_content_news.find("select support from t_content_news where id = ?", newsId).first();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "赞成功";
		
		return support;
	}
	
	/**
	 * 踩
	 * @param newsId
	 * @param error
	 * @return
	 */
	public static int opposition(long newsId, ErrorInfo error) {
		error.clear();
		
		if(!News.checkSO(newsId)) {
			error.code = -1;
			error.msg = "别踩了，休息一会儿!";
			
			return -1;
		}
		
		String sqlUpdate = "update t_content_news set opposition = opposition + 1 where id = :id";
		Query queryUpdate = JPA.em().createQuery(sqlUpdate).setParameter("id", newsId);
		
		int opposition = 0;

		int rows = 0;
		
		try {
			rows = queryUpdate.executeUpdate();
			opposition = t_content_news.find("select opposition from t_content_news where id = ?", newsId).first();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error(e.getMessage());
			e.printStackTrace();
			error.code = -1;
			error.msg = "数据库异常";

			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "踩成功";
		
		return opposition;
	}
	
	/**
	 * 获取条款、协议等内容
	 * @param newsId 新闻ID
	 * @param error 信息值
	 * @return 服务条款
	 */
	public static String queryContent(long newsId, ErrorInfo error){
		error.clear();
		
		String hql = "select content from t_content_news where id = ?";
		
		try {
			return t_content_news.find(hql, newsId).first();
		} catch (Exception e) {
			Logger.error("新闻->获取条款内容时:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载服务条款失败!";

			return null;
		}
	}
	
	/**
	 * 获取条款、协议等内容
	 * @param newsId 新闻ID
	 * @param error 信息值
	 * @return 服务条款
	 */
	public static String queryContentById(long newsId, ErrorInfo error){
		error.clear();
		
		String hql = "select content from t_content_news where type_id = ? and is_use = true and start_show_time <= now() order by id desc";
		
		try {
			return t_content_news.find(hql, newsId).first();
		} catch (Exception e) {
			Logger.error("新闻->获取条款内容时:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载服务条款失败!";

			return null;
		}
	}
	
	/**
	 * 获取专家顾问，管理团队的内容
	 * @param newsId 新闻ID
	 * @param error 信息值
	 * @return 服务条款
	 */
	public static List<String> queryContentList(long newsId, ErrorInfo error){
		error.clear();
		
		String hql = "select content from t_content_news where type_id = ? and is_use = true and start_show_time <= now()";
		
		try {
			return t_content_news.find(hql, newsId).fetch();
		} catch (Exception e) {
			Logger.error("新闻->获取专家顾问，管理团队内容时:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "加载专家顾问，管理团队内容失败!";

			return null;
		}
	}
	
	/**
	 * 查询公司介绍，招贤纳士的内容
	 * @param typeId
	 * @param error
	 * @return
	 */
	public static String queryContentByTypeId(long typeId,ErrorInfo error){
		error.clear();
		error.code = -1;
		
		String hql = "select content from t_content_news where type_id = ? and is_use = true and start_show_time <= ?";
		
		try {
			error.code = 0;
			
			return t_content_news.find(hql, typeId == -1004 ? (long)16 : (long)19, new Date()).first();
		} catch (Exception e) {
			Logger.error("查询新闻内容时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询新闻内容是有误！";
			
			return null;
		}
	}
	
	
	/**
	 * 获取前台本金保障首页各型内容
	 * @param typeId
	 * @param error
	 * @return
	 */
	public static t_content_news getPrincipalGuaranteeNews(long typeId,ErrorInfo error){
		
		t_content_news temp = new t_content_news();
		
		try {
			temp = t_content_news.find("type_id = ?  order by id desc",typeId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		error.code = 0;
		return temp;
	}
	
	public static PageBean<t_content_advertisements_partner> queryPartners(int currPage, int pageSize, ErrorInfo error) {
		error.clear();
		
		if (currPage < 1) {
			currPage = 1;
		}

		if (pageSize < 1) {
			pageSize = 10;
		}
		
		int count = 0;
		List<t_content_advertisements_partner> page = null;
		
		try {
			count = (int) t_content_advertisements_partner.count();
			page = t_content_advertisements_partner.find("").fetch(currPage, pageSize);
		} catch (Exception e) {
			Logger.error("查询合作伙伴:" + e.getMessage());
			error.code = -1;
			error.msg = "加载合作伙伴失败!";

			return null;
		}
		
		PageBean<t_content_advertisements_partner> bean = new PageBean<t_content_advertisements_partner>();
		bean.pageSize = pageSize;
		bean.currPage = currPage;
		bean.page = page;
		bean.totalCount = count;
		bean.conditions = null;
		
		error.code = 0;

		return bean;
	}
	
	public static List<t_content_advertisements_partner> queryPartners(ErrorInfo error) {
		error.clear();
		
		List<t_content_advertisements_partner> list = null;
		
		try {
			list = t_content_advertisements_partner.find("").fetch();
		} catch (Exception e) {
			Logger.error("查询合作伙伴:" + e.getMessage());
			error.code = -1;
			error.msg = "加载合作伙伴失败!";

			return null;
		}
		error.code = 0;

		return list;
	}
	/**
	 * 投资人已加入晓风安全网贷系统6,投资额已交易成功 统计
	 */
	public static Object[] queryInvestDataSum(){
		Object[] datas = new Object[2];
		datas[0] = 0;
		datas[1] = 0;
		Long count = null;
		
		try {
			count = t_users.count("master_identity in (?, ?)", Constants.INVEST_USER, Constants.LOAN_INVEST_USER);
		} catch (Exception e) {
			Logger.error("查询投资人:" + e.getMessage());
			
			return datas;
		}
		
		datas[0] = count;
		
		Double sum = null;
		
		try {
			sum = t_users.find("select sum(bill.overdue_fine+bill.repayment_corpus+bill.repayment_interest) from t_bills bill").first();
		} catch (Exception e) {
			Logger.error("查询投资额:" + e.getMessage());
			
			return datas;
		}
		double sumBill = Convert.strToDouble(sum+"", 0);
		datas[1] = Arith.round(sumBill, 4);
		
		return datas;
	}
	
	/**
	 * 统计帮助各类型的数量
	 * @param error
	 */
	public static Map<String,String> queryCount(ErrorInfo error) {
		error.clear();
		
		String sql = "select count( ( case when (news.type_id = 13)"
				+ " then news.id end)) as problems, count( ( case when (news.type_id = 14)"
				+ " then news.id end)) as loans, count( ( case when (news.type_id = 15) "
				+ " then news.id end)) as invests from t_content_news news";
		
		Object[] obj = null;
		try {
			obj = (Object[]) JPA.em().createNativeQuery(sql).getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("帮助中心数量统计时："+e.getMessage());
			
			error.code = -1;
			error.msg = "帮助中心数量统计时出现异常";

			return null;
		}
		
		if(obj == null) {
			error.code = -1;
			error.msg = "帮助中心数量统计时出现异常";

			return null;
		}
		
		Map<String, String> newsCount = new HashMap<String, String>();
		
		newsCount.put("problems", obj[0].toString());
		newsCount.put("loans", obj[1].toString());
		newsCount.put("invests", obj[2].toString());
		
		error.code = 0;
		
		return newsCount;
	}
	
	public static String queryVipAgreement() {
		String sql = "select content from t_content_news where id = ?";
		
		String agreement = t_content_news.find(sql, -1009L).first();
		
		return agreement;
	}
	
	/**
	 * 新闻列表头条（app）
	 * @param typIdStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean <News>  queryNewsByTypeIdApp(String currPageStr,
			String pageSizeStr, ErrorInfo error) {
		
		error.clear();
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.FIVE;
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
		List<t_content_news> news = new ArrayList<t_content_news>();
		int count = 0;
		
		String sqlCount = "select count(*) from "
				+ "t_content_news news where location_pc = 1 and news.is_use = true and "
				+ "news.start_show_time <= ? ";
		
		String sqlPage = "select new t_content_news(id, time, title, content, read_count) from t_content_news where "
			+ "location_pc = 1 and start_show_time <= ? and is_use = true order by _order";
	
		try {
			count = (int)t_content_news.count(sqlCount, new Date());
			news = t_content_news.find(sqlPage, new Date()).fetch(currPage, pageSize);
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询财富资讯列表情况时："+e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，财富资讯列表失败";
			
			return null;
		}
		
		List<News> newsList = new ArrayList<News>();
		
		News aNews = null;
		
		for(t_content_news content : news) {
			aNews = new News();
			
			aNews._id = content.id;
			aNews.time = content.time;
			aNews.title = content.title;
			aNews.content = content.content;
			aNews.readCount = content.read_count;
			
			newsList.add(aNews);
		}
		
		PageBean<News> page = new PageBean<News>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = newsList;
		page.totalCount = count;
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 限制一个IP在缓存失效的时间段内只能顶踩一次
	 * @param key
	 * @return
	 */
	public static boolean checkSO(long newsId){
		String ip = DataUtil.getIp();// 获取当前登录IP
		ip = ip + newsId;
		Object obj = Cache.get(ip);
		
		if(null == obj){
			Cache.set(ip, ip);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 查询官方活动
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<t_content_news> queryOfficialActivity(String currPageStr, String pageSizeStr, ErrorInfo error){
		error.clear();
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.FIVE;
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
		List<t_content_news> news = new ArrayList<t_content_news>();
		int count = 0;
		
		try {
			count = (int) t_content_news.count("start_show_time <= ? and is_use = true AND location_app BETWEEN ? AND ?", new Date(), Constants.EIGHT, Constants.FIFTEEN);
			news = t_content_news.find("start_show_time <= ? and is_use = true AND location_app BETWEEN ? AND ?", new Date(), Constants.EIGHT, Constants.FIFTEEN).fetch(currPage, pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询官方活动时:" + e.getMessage());
			error.code = -1;
			error.msg = "查询官方活动失败!";

			return null;
		}
		
		PageBean<t_content_news> page = new PageBean<t_content_news>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = news;
		page.totalCount = count;
		page.conditions = null;
		
		return page;
	}
	
	/**
	 * 微信财富资讯
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static List<v_content_wechat_news> queryNewWechat(ErrorInfo error) {
		error.clear();
		
		List<v_content_wechat_news> news = new ArrayList<v_content_wechat_news>();
		
		try {
			news = t_content_news.findAll();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		return news;
	}
	
	/**
	 * 微信财富资讯详情
	 * @param typeId
	 * @param size
	 * @param error
	 * @return
	 */
	public static t_content_news queryNewWechatDetails(long id, ErrorInfo error) {
		error.clear();
		
		t_content_news news = new t_content_news();
		
		try {
			news = t_content_news.findById(id);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时："+e.getMessage());
			
			return null;
		}
		
		error.code = 0;
		
		JpaHelper.execute("update t_content_news set read_count = read_count+1"
				+ "where id = ?", id).executeUpdate();
		
		return news;
	}
}
