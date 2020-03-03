package business;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import constants.SQLTempletes;
import constants.SupervisorEvent;

import play.Logger;
import play.db.jpa.JPA;

import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;

import models.t_bill_invests;
import models.t_content_news;
import models.t_forum_posts_answers;
import models.t_forum_posts;
import models.t_forum_posts_collection;
import models.t_forum_posts_questions;
import models.t_forum_type;
import models.t_users;
import models.v_forum_posts;
import models.v_forum_posts_collection;
import models.v_forum_posts_questions;
import models.v_posts_questions;
import models.v_repayment_news;
import models.v_repayment_news_info;

public class Posts implements Serializable{
	
	public long id;

	public String name;
	public String title;
	public Date add_time;
	public Date show_time;
	public String content;
	public String keywords;
	public int type_id;
	public int status;
	public int show_image;
	public int rId;
	
	public long question_id;
	public long posts_id;
	public long user_id;
	public Date time;
	
	public int type;
	
	public String question_user_name;
	public String to_answer_user;
	
	public long  to_answer_user_id;
	
	/**
	 * 添加论坛帖子
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public int addPostsInfo(long supervisorId, ErrorInfo error){
		error.clear();
		t_forum_posts tPost = new t_forum_posts();
		
		tPost.user_id = this.user_id;
		tPost.name = this.name;
		tPost.title = this.title;
		tPost.add_time = new Date();
		tPost.show_time = this.show_time;
		tPost.content = this.content;
		tPost.keywords = this.keywords;
		tPost.type_id = this.type_id;
		tPost.show_image = this.show_image;
		tPost.rId = 0;
		tPost.status = 0;
		
		try {
			tPost.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "帖子添加失败";
			error.code = -3;
			return error.code;
		}
		if(supervisorId > 0){
			DealDetail.supervisorEvent(supervisorId, SupervisorEvent.CREATE_FORUM_POSTS,
					"添加论坛帖子", error);
		}
		
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "发帖成功";
		return error.code;
	}
	
	/**
	 * 编辑帖子内容
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public int editPostsInfo(long supervisorId, ErrorInfo error){
		error.clear();
		t_forum_posts tPost = new t_forum_posts();
		
		String sql = "UPDATE t_forum_posts set name = ?,title = ?,show_time = ?,content = ?,keywords = ?,show_image = ? where id = ?";
		
		tPost.name = this.name;
		tPost.title = this.title;
		tPost.show_time = this.show_time;
		tPost.content = this.content;
		tPost.keywords = this.keywords;
		tPost.show_image = this.show_image;
	
		
		Query query = JPA.em().createQuery(sql).setParameter(1, this.name)
				.setParameter(2, this.title).setParameter(3, this.show_time).setParameter(4, this.content).setParameter(5, this.keywords)
				.setParameter(6, this.show_image).setParameter(7, this.id);
		
		try {
			query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "帖子编辑失败";
			
			return -2;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EIDT_FORUM_POSTS,
				"编辑论坛帖子", error);
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}
		
		error.code = 0;
		error.msg = "帖子编辑成功";
		return 0;
	}
	
	/**
	 * 删除论坛帖子
	 * @param id
	 */
	public static void deletePosts(long id,long supervisorId,ErrorInfo error){
		t_forum_posts.delete(" id = ?", id);
		t_forum_posts_questions.delete(" posts_id = ? ", id);
		if(supervisorId > 0){
			DealDetail.supervisorEvent(supervisorId, SupervisorEvent.DELETE_FORUM_POSTS,
					"删除论坛帖子", error);
		}
		
		
		if (error.code < 0) {
			JPA.setRollbackOnly();

		}
	}
	
	/**
	 * 根据ID查询帖子
	 * @param id
	 * @return
	 */
	public static t_forum_posts queryPosts(long id){
		t_forum_posts t = t_forum_posts.findById(id);
		
		if(null != t){
			t_forum_type type = t_forum_type.find(" id = ? ", t.type_id).first();
			t.typeName = type.name;
			
		}
		
		return t;
	}
	
	
	
	/**
	 * 查询论坛帖子类型
	 * @return
	 */
	public static List<t_forum_type> getForumTypeList(int type){
		
		List<t_forum_type> list = new ArrayList<t_forum_type>();
		if(type == 0){
			list = t_forum_type.find(" order by rId ").fetch();
		}else if(type == 1){
			list = t_forum_type.find(" status = ? order by rId ",0).fetch();
		}else if(type == 2){
			list = t_forum_type.find(" status = ? and id in (4,5,7,8) order by rId ",0).fetch();
		}
		return list;
	}
	
	/**
	 * 修改帖子类型内容
	 * @param supervisorId
	 * @param idStr
	 * @param statusStr
	 * @param error
	 * @return
	 */
	public static int updatePostsType(long supervisorId,String idStr,String statusStr,ErrorInfo error){
		error.clear();

		if (!NumberUtil.isNumericInt(idStr)) {
			error.code = -1;
			error.msg = "传入盈盈社区参数有误！";

			return error.code;
		}

		if (!NumberUtil.isNumericInt(statusStr)) {
			error.code = -2;
			error.msg = "传入盈盈社区参数有误！";

			return error.code;
		}
		
		int statusInt = Integer.parseInt(statusStr);

		if (statusInt != 0 && statusInt != 1) {
			error.code = -2;
			error.msg = "传入参数有误！";

			return error.code;
		}
		long adsId = Long.parseLong(idStr);
		
		String sql = "update t_forum_type set status = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, statusInt)
				.setParameter(2, adsId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("启用广告条，更新广告条信息时：" + e.getMessage());
			error.msg = "启用广告条失败";

			return -1;
		}
		
		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}
		/*boolean status = statusInt == 0 ? true : false;
		if (status == false) {
			DealDetail.supervisorEvent(supervisorId,
					SupervisorEvent.OPEN_USE_ADS, "启用广告条使用", error);
		} else {
			DealDetail.supervisorEvent(supervisorId,
					SupervisorEvent.CLOSE_USE_ADS, "暂停广告条使用", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}*/

		error.code = 0;
		error.msg = "修改成功！";
		return error.code;
	}
	
	public void setPostsTypeId(long id){
		t_forum_type t = t_forum_type.findById(id);
		this.id = t.id;
		this.name = t.name;
		this.status = t.status;
		this.rId = t.rId;
	}
	
	/**
	 * 修改发帖类型内容
	 * @param id
	 * @param name
	 * @param rId
	 */
	public static void updatePostsTypeInfo(long id,String name,int rId,ErrorInfo error){
		
		String sql = "update t_forum_type set name = ?,rId=? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, name)
				.setParameter(2, rId).setParameter(3, id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("启用广告条，更新广告条信息时：" + e.getMessage());
			error.msg = "启用广告条失败";
			error.code=-1;
			
		}
		
		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

		}
		error.code = 0;
		error.msg = "修改成功！";
		
	}
	
	/**
	 * 增加帖子浏览次数
	 * @param id
	 * @return
	 */
	public static int  updatePostReadCount(long id){
		String sql = "update t_forum_posts set read_count = read_count + 1 where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, id);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("增加浏览次数：" + e.getMessage());
			
			return rows;
		}
		return rows;
	}
	
	/**
	 * 根据帖子类型查询帖子
	 * @param type
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<t_forum_posts> queryForumPostsByTypeId(long type,String currPageStr,String pageSizeStr,ErrorInfo error){
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_POSTS_SHOW_SIZE;
 		
 		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		List<t_forum_posts> list = new ArrayList<t_forum_posts>();
		int count = 0;
		try {
			list = t_forum_posts.find(" type_id = ? and show_time <= ? order by show_time desc ", type,new Date()).fetch(currPage, pageSize);
			count = t_forum_posts.find(" type_id = ? and show_time <= ? ", type,new Date()).fetch().size();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		PageBean<t_forum_posts> page = new PageBean<t_forum_posts>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = list;
		page.totalCount = count;
	
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 查询所有论坛帖子
	 * @param postsType
	 * @param title
	 * @param orderTypeStr
	 * @param orderStatus
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_forum_posts> queryForumPosts(int postsType,String title,String orderTypeStr, String orderStatus,
			String currPageStr,String pageSizeStr, ErrorInfo error,long userId){
		
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		int orderType = 0;
 		
 		StringBuffer conditions = new StringBuffer();
 		StringBuffer countSql = new StringBuffer();
 		List<Object> values = new ArrayList<Object>();
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
 		
 		conditions.append(SQLTempletes.V_FORUM_POSTS);
 		countSql.append("select count(s.id) from t_forum_posts s where 1=1 ");
 		
		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		
		if(postsType > 0) {
			
			conditions.append(" and type_id = ? ");
			countSql.append(" and type_id = ? ");
			values.add(postsType);
 		}
		
		if(StringUtils.isNotBlank(title)){
			conditions.append(" and title like ? "); 
			countSql.append(" and title like ? ");
			values.add("%"+title+"%");
		}
		
		if(userId > 0){
			conditions.append(" and user_id = ? ");
			countSql.append(" and user_id = ? ");
			values.add(userId);
		}
		
		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
		
		conditionMap.put("title", title);
		conditionMap.put("orderType", orderType);
		
		conditions.append(Constants.POSTS_ORDER[orderType]);
		countSql.append(Constants.POSTS_ORDER[orderType]);
		
		if(StringUtils.isNotBlank(orderStatus) && orderType > 0){
				
			if(Integer.parseInt(orderStatus) == 1)
				conditions.append(" ASC");
			else
				conditions.append(" DESC");
			
			conditionMap.put("orderStatus", orderStatus);
		}
		
		
		
		int count = 0;
		List<v_forum_posts> contents = new ArrayList<v_forum_posts>();
		try {
			Query countV = JPA.em().createNativeQuery(countSql.toString());
			for (int i = 0; i < values.size(); i++) {
				countV.setParameter(i+1 , values.get(i));
			}
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(conditions.toString(),v_forum_posts.class);
			for (int i = 0; i < values.size(); i++) {
				query.setParameter(i+1 , values.get(i));
			}
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
			contents = query.getResultList();
			
		
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		PageBean<v_forum_posts> page = new PageBean<v_forum_posts>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;

		return page;
	}
	
	/**
	 * 查询我收藏的帖子
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @param userId
	 * @return
	 */
	public static PageBean<v_forum_posts> queryCollectionPosts(String currPageStr,String pageSizeStr, ErrorInfo error,long userId){
		
		
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		int orderType = 0;
		
		StringBuffer conditions = new StringBuffer();
		StringBuffer countSql = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		
		//conditions.append("SELECT s.id as id, s.title as title, s.add_time as add_time, s.show_time as show_time, s.read_count as read_count, s.answers_count as answers_count, f.name as typeName, s.status as status, s.name as userName, s.rId as rId FROM t_forum_posts s LEFT JOIN t_forum_type f on s.type_id = f.id where 1=1 ");
		conditions.append(SQLTempletes.V_COLLECTION_POSTS);
		countSql.append("SELECT COUNT(s.id) from t_forum_posts s ,t_forum_posts_collection c where s.id = c.posts_id and status = 0 ");
		//String sql = "";
		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		
		/*if(postsType > 0) {
			
			conditions.append(" and type_id = ? ");
			countSql.append(" and type_id = ? ");
			values.add(postsType);
		}
		
		if(StringUtils.isNotBlank(title)){
			conditions.append(" and title like ? "); 
			countSql.append(" and title like ? ");
			values.add("%"+title+"%");
		}*/
		
		if(userId > 0){
			conditions.append(" and c.user_id = ? ");
			countSql.append(" and c.user_id = ? ");
			values.add(userId);
		}
		
		/*if(NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}*/
		
		//conditionMap.put("title", title);
		//conditionMap.put("orderType", orderType);
		
		conditions.append(Constants.POSTS_ORDER[orderType]);
		countSql.append(Constants.POSTS_ORDER[orderType]);
		
		/*if(StringUtils.isNotBlank(orderStatus) && orderType > 0){
			
			if(Integer.parseInt(orderStatus) == 1)
				conditions.append(" ASC");
			else
				conditions.append(" DESC");
			
			conditionMap.put("orderStatus", orderStatus);
		}*/
		
		
		
		int count = 0;
		List<v_forum_posts> contents = new ArrayList<v_forum_posts>();
		try {
			//count = (int)v_forum_posts.count(conditions.toString(), values.toArray());
			//contents = v_forum_posts.find(conditions.toString(), values.toArray()).fetch(currPage, pageSize);
			Query countV = JPA.em().createNativeQuery(countSql.toString());
			for (int i = 0; i < values.size(); i++) {
				countV.setParameter(i+1 , values.get(i));
			}
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(conditions.toString(),v_forum_posts.class);
			for (int i = 0; i < values.size(); i++) {
				query.setParameter(i+1 , values.get(i));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			contents = query.getResultList();
			
			
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		PageBean<v_forum_posts> page = new PageBean<v_forum_posts>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;
		
		return page;
	}
	
	/**
	 * 查询所有发帖类型状态
	 * @return
	 */
	public static List<t_forum_type> queryForumType(){
		
		List<t_forum_type> t = t_forum_type.find("status = 0").fetch();
		return t;
	}
	
	/**
	 * 修改帖子置顶状态
	 * @param id
	 * @param rId
	 * @param error
	 */
	public static void updatePostsTypeStatue(long id,int rId, ErrorInfo error){
		int row = 0;
		String sql = "update t_forum_posts set rId = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, rId).setParameter(2, id);
		try{
			row = query.executeUpdate();
		
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改帖子置顶状态："+e.getMessage());
			
			error.code = -1;
			error.msg = "修改失败";
			
			return ;
		}
		
		if(row == 0){
			error.code = -1;
			error.msg = "修改失败";
			
			return ;
		}
		
		error.code = 0;
		error.msg = "修改成功";
		
		return ;
	}
	
	/**
	 * 修改帖子显示状态
	 * @param id
	 * @param status
	 * @param error
	 */
	public static void updatePostsStatue(long id,int status, ErrorInfo error){
		int row = 0;
		String sql = "update t_forum_posts set status = ? where id = ?";
		Query query = JPA.em().createQuery(sql).setParameter(1, status).setParameter(2, id);
		try{
			row = query.executeUpdate();
			
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改帖子显示状态："+e.getMessage());
			
			error.code = -1;
			error.msg = "修改失败";
			
			return ;
		}
		
		if(row == 0){
			error.code = -1;
			error.msg = "修改失败";
			
			return ;
		}
		
		error.code = 0;
		error.msg = "修改成功";
		
		return ;
	}
	
	/**
	 * 根据条件查询回复信息
	 * @param id
	 * @param answerStatus
	 * @param countent
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_posts_questions> showUserQuestions(long id,int answerStatus,String countent,String currPageStr,String pageSizeStr,ErrorInfo error,long userId,long answerUser){
		
		List<v_posts_questions> contents = new ArrayList<v_posts_questions>();
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		//我的消息区分
		if(answerUser > 0){
			sql.append("select * from (SELECT s.id,s.user_id,s.posts_id, ( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) AS userName,( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) as forumName,INSERT (s.to_answer_user,5,4,'****' )  as toAnswerUser, s.content, s.time, ( SELECT p.answers_count FROM t_forum_posts p WHERE p.id = s.posts_id ) AS answers_count, ( SELECT y.`name` FROM t_forum_posts f, t_forum_type y WHERE f.type_id = y.id AND f.id = s.posts_id ) AS typeName,s.read_status as readStatus FROM t_forum_posts_questions s WHERE 1 = 1 ");
			sqlCount.append("SELECT SUM(t.con) from (select count(id) as con from t_forum_posts_questions where 1 = 1");
		}else{
			sql.append("SELECT s.id,s.user_id,s.posts_id, ( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) AS userName,( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) as forumName,INSERT (s.to_answer_user,5,4,'****' )  as toAnswerUser, s.content, s.time, ( SELECT p.answers_count FROM t_forum_posts p WHERE p.id = s.posts_id ) AS answers_count, ( SELECT y.`name` FROM t_forum_posts f, t_forum_type y WHERE f.type_id = y.id AND f.id = s.posts_id ) AS typeName,s.read_status as readStatus FROM t_forum_posts_questions s WHERE 1 = 1 ");
			sqlCount.append("select count(id) from t_forum_posts_questions where 1 = 1");
		}
		
		if(id > 0){
			sql.append(" and posts_id = ? ");
			sqlCount.append(" and posts_id = ? ");
			values.add(id);
		}
		
		if(userId > 0){
			sql.append(" and user_id = ? ");
			sqlCount.append(" and user_id = ? ");
			values.add(userId);
		}
		
		if(answerStatus > 0){
			sql.append(" and answer_status = ? ");
			sqlCount.append(" and answer_status = ? ");
			
			if(answerStatus == 1){
				values.add(1);
			}else{
				values.add(0);
			}
		}
		
		if(answerUser > 0){
			sql.append(" and to_answer_user_id = ? ");
			sqlCount.append(" and to_answer_user_id = ? ");
			values.add(answerUser);
			
			sql.append(" union ALL  select m.id,0 as user_id,m.posts_id,'管理员' as userName,'管理员' as forumName, INSERT ( (select IFNULL(r.forum_name,r.name) from t_users r where r.id = m.user_id), 5, 4, '****' ) AS toAnswerUser,m.content,m.time, ( SELECT p.answers_count FROM t_forum_posts p WHERE p.id = m.posts_id ) AS answers_count,( SELECT y.`name` FROM t_forum_posts f, t_forum_type y WHERE f.type_id = y.id AND f.id = m.posts_id ) AS typeName,1 AS readStatus from t_forum_posts_answers m where m.type = 1 and m.user_id = ?) as tb ");
			sqlCount.append(" union ALL select count(id) as con from t_forum_posts_answers where  user_id = ?) as t ");
			values.add(answerUser);
		}
		
		
		
		if(StringUtils.isNotBlank(countent)){
			sql.append(" and content like ? "); 
			sqlCount.append(" and content like ? ");
			values.add("%"+countent+"%");
		}
		
		conditionMap.put("answerStatus", answerStatus);
		conditionMap.put("countent", countent);
		
		sql.append(" order by time desc ");
		int count = 0;
		try{
			Query countV = JPA.em().createNativeQuery(sqlCount.toString());
			for (int i = 0; i < values.size(); i++) {
				countV.setParameter(i+1 , values.get(i));
			}
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(sql.toString(),v_posts_questions.class).setParameter(1, id);
			for (int i = 0; i < values.size(); i++) {
				query.setParameter(i+1 , values.get(i));
			}
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
			contents = query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		Calendar cal = Calendar.getInstance();
		for(v_posts_questions vo : contents){
			vo.timeBetween = getTimeBetween(cal,vo.time);  
	            
		}
		
		PageBean<v_posts_questions> page = new PageBean<v_posts_questions>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;
		return page;
	}
	
	
	public static PageBean<v_posts_questions> showAppPostsQuestions(long id,int answerStatus,String countent,String currPageStr,String pageSizeStr,ErrorInfo error){
		
		List<v_posts_questions> contents = new ArrayList<v_posts_questions>();
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		sql.append("SELECT * from (SELECT s.id,s.user_id,s.posts_id, ( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) AS userName,( SELECT IFNULL(u.forum_name,INSERT (u.mobile,5,4,'****' )) FROM t_users u WHERE u.id = s.user_id ) as forumName,INSERT (s.to_answer_user,5,4,'****' )  as toAnswerUser, s.content, s.time, ( SELECT p.answers_count FROM t_forum_posts p WHERE p.id = s.posts_id ) AS answers_count, ( SELECT y.`name` FROM t_forum_posts f, t_forum_type y WHERE f.type_id = y.id AND f.id = s.posts_id ) AS typeName,s.read_status as readStatus FROM t_forum_posts_questions s WHERE s.posts_id = ? UNION ALL "+
					"SELECT a.id, 0 AS user_id, a.posts_id, '管理员' AS userName, '管理员' AS forumName, (SELECT IFNULL( u.forum_name, INSERT (u.mobile, 5, 4, '****') ) from t_users u where u.id = a.user_id) as toAnswerUser, a.content, a.time, ( SELECT p.answers_count FROM t_forum_posts p WHERE p.id = a.posts_id ) AS answers_count, ( SELECT y.`name` FROM t_forum_posts f, t_forum_type y WHERE f.type_id = y.id AND f.id = a.posts_id) AS typeName, 0 AS readStatus FROM t_forum_posts_answers a WHERE posts_id = ?) t ");
		sqlCount.append("SELECT SUM(s.cou) from (SELECT count(1) as cou  from t_forum_posts_questions s where s.posts_id = ? UNION ALL SELECT count(1) as cou  from t_forum_posts_answers a where a.posts_id = ?) s");
		
		if(id > 0){
			//sql.append(" and posts_id = ? ");
			//sqlCount.append(" and posts_id = ? ");
			values.add(id);
			values.add(id);
		}
		
		
		conditionMap.put("answerStatus", answerStatus);
		conditionMap.put("countent", countent);
		
		sql.append(" order by t.time desc ");
		int count = 0;
		try{
			Query countV = JPA.em().createNativeQuery(sqlCount.toString());
			for (int i = 0; i < values.size(); i++) {
				countV.setParameter(i+1 , values.get(i));
			}
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(sql.toString(),v_posts_questions.class).setParameter(1, id);
			for (int i = 0; i < values.size(); i++) {
				query.setParameter(i+1 , values.get(i));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			contents = query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		Calendar cal = Calendar.getInstance();
		for(v_posts_questions vo : contents){
			vo.timeBetween = getTimeBetween(cal,vo.time);  
			
		}
		
		PageBean<v_posts_questions> page = new PageBean<v_posts_questions>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;
		return page;
	}
	
	/**
	 * 论坛版块查询帖子回复
	 * @param typeId
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_forum_posts_questions> queryForumQuestion(long typeId,String currPageStr,String pageSizeStr,ErrorInfo error){
		
		List<v_forum_posts_questions> contents = new ArrayList<v_forum_posts_questions>();
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		String sql = "select q.id,p.id as posts_id,q.content,p.title,p.show_image,q.time,(SELECT IFNULL(u.forum_name,u.`name`) from t_users u where u.id = q.user_id) as userName,p.answers_count,p.read_count from t_forum_posts p ,t_forum_posts_questions q where p.id = q.posts_id and p.type_id = ? and status = 0 ORDER BY q.time desc";
		String sqlCount = "select count(q.id) from t_forum_posts p ,t_forum_posts_questions q where p.id = q.posts_id and p.type_id = ? and status = 0";
		int count = 0;
		try{
			Query countV = JPA.em().createNativeQuery(sqlCount).setParameter(1, typeId);
			
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(sql,v_forum_posts_questions.class).setParameter(1, typeId);
			
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
			contents = query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询论坛版块回复时："+e.getMessage());
			error.code = -1;
			error.msg = "查询论坛版块回复失败";
			
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		for(v_forum_posts_questions vo : contents){
			  
			vo.timeBetween = getTimeBetween(cal,vo.time);
	            
		}
		
		PageBean<v_forum_posts_questions> page = new PageBean<v_forum_posts_questions>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		//page.conditions = conditionMap;
		
		error.code = 0;
		return page;
	}
	
	/**
	 * 得到发帖时间与现在时间差
	 * @param cal
	 * @param time
	 * @return
	 */
	public static String getTimeBetween(Calendar cal,Date time){
		cal.setTime(time);  
        long time1 = cal.getTimeInMillis();               
        cal.setTime(new Date());  
        long time2 = cal.getTimeInMillis();       
        long between_days=(time2-time1)/(1000*3600*24);  
        if(between_days == 0){
        	long between_hour=(time2-time1)/(1000*3600);
        	if(between_hour == 0){
        		return "刚刚";
        	}else{
        		return Integer.parseInt(String.valueOf(between_hour))+"小时前";
        	}
        }else{
        	return Integer.parseInt(String.valueOf(between_days))+"天前";
        } 
	}
	
	
	public static int deleteUserQuestions(long id){
		return t_forum_posts_questions.delete(" id = ? ", id);
	}
	
	/**
	 * 查询我的消息
	 * @param userId
	 * @return
	 */
	public static int queryMyNewCount(long userId){
		Query countV = JPA.em().createNativeQuery("select SUM(t.id) from (select count(id) as id from t_forum_posts_questions where to_answer_user_id = ? and read_status = 0 union ALL select count(id) as id from t_forum_posts_answers where user_id = ? and read_status = 0) as t").setParameter(1, userId).setParameter(2, userId);
		return Integer.parseInt(countV.getSingleResult().toString());
	}
	
	/**
	 * 查询我的收藏
	 * @param userId
	 * @param postsId
	 * @return
	 */
	public static int queryMyCollection(long userId,long postsId){
		Query countV = JPA.em().createNativeQuery("SELECT COUNT(s.id) from t_forum_posts_collection s where s.posts_id = ? and s.user_id = ?").setParameter(1, postsId).setParameter(2, userId);
		return Integer.parseInt(countV.getSingleResult().toString());
	}
	
	/**
	 * 修改我的消息状态
	 * @return
	 */
	public static int updateMyNewStatus(long id){
		String sql = "UPDATE t_forum_posts_questions set read_status = 1 where id = ?";
		int count = 0;
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, id);
		try {
			count = query.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("根据用户回复id修改回复状态", e);
			
		}
		return count;
	}
	/**
	 * 修改管理员消息状态
	 * @param id
	 * @return
	 */
	public static int updateAdminMyNewStatus(long id){
		String sql = "UPDATE t_forum_posts_answers set read_status = 1 where id = ?";
		int count = 0;
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, id);
		try {
			count = query.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("根据用户回复id修改回复状态", e);
			
		}
		return count;
	}
	
	/**
	 * 操作收藏
	 * @param postsId
	 * @param userId
	 * @param status
	 */
	public static void eidtColection(long postsId,long userId,boolean status){
		if(status){
			
			t_forum_posts_collection.delete(" posts_id = ? and user_id = ? ", postsId,userId);
		}else{
			t_forum_posts_collection t = new t_forum_posts_collection();
			t.posts_id = postsId;
			t.user_id = userId;
			t.time = new Date();
			try {
				t.save();
			} catch (Exception e) {
				Logger.info("%%%%%%%%%%%%%%%%%%%%%%增加收藏帖子失败postsId=="+postsId+"userId=="+userId);
			}
		}
	}
	
	/**
	 * 还款公告查询
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_repayment_news> queryRepaymentNews(String currPageStr,String pageSizeStr,ErrorInfo error){
		
		List<v_repayment_news> contents = new ArrayList<v_repayment_news>();
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_POSTS_SIZE;
 		
 		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		
		String sql = "SELECT b.id, b.receive_time,COUNT(b.bid_id) as counts,SUM(b.receive_corpus + b.receive_interest) as money from t_bill_invests b where  b.receive_time < ? GROUP BY date_format(receive_time,'%Y-%m-%d') desc ";
		String sqlCount = "SELECT COUNT(t.receive_time) from (SELECT count(b.receive_time) as receive_time from t_bill_invests b where  b.receive_time < ? GROUP BY date_format(receive_time,'%Y-%m-%d')) t";
		Date date = DateUtil.strDateToEndDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		
		int count = 0;
		try{
			Query countV = JPA.em().createNativeQuery(sqlCount).setParameter(1, date);
			
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(sql,v_repayment_news.class).setParameter(1, date);
			
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
			contents = query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询还款公告时："+e.getMessage());
			error.code = -1;
			error.msg = "查询还款公告失败";
			
			return null;
		}
		
		String infoSql = "select i.id,i.title,i.receive_corpus+i.receive_interest as money,(SELECT MAX(t.periods) from t_bill_invests t where t.invest_id = i.invest_id) as mPeriodes,i.periods from t_bill_invests i where i.receive_time > ? and i.receive_time < ?";
		
		for(v_repayment_news vo : contents){
			
			Date startDate = DateUtil.strDateToStartDate(new SimpleDateFormat("yyyy-MM-dd").format(vo.receive_time));
			Date endDate = DateUtil.strDateToEndDate(new SimpleDateFormat("yyyy-MM-dd").format(vo.receive_time));
			try {
				Query query = JPA.em().createNativeQuery(infoSql,v_repayment_news_info.class).setParameter(1, startDate).setParameter(2, endDate);
				vo.t = query.getResultList();
			
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		PageBean<v_repayment_news> page = new PageBean<v_repayment_news>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		
		
		error.code = 0;
		return page;
		
		
	}
	
	/**
	 * 根据回帖id查询管理员回复
	 * @param id
	 * @return
	 */
	public static List<t_forum_posts_answers> queryAdminAnswers(long id){
		List<t_forum_posts_answers> t = new ArrayList<t_forum_posts_answers>();
		t = t_forum_posts_answers.find(" question_id = ? and type = 1 ", id).fetch();
		return t;
	}
	
	/**
	 * 用户或者管理员回复帖子
	 * @param error
	 * @return
	 */
	public int saveAdminAnswers(ErrorInfo error){
		t_forum_posts_answers t = new t_forum_posts_answers();
		
		t.question_id = this.question_id;
		t.time = new Date();
		t.content = this.content;
		t.type = this.type;
		t.user_id = this.user_id;
		t.posts_id = this.posts_id;
		t.question_user_name = this.question_user_name;
		try {
			t.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("保存管理员回复时：", e);
			error.code = -1;
			error.msg = "管理员回复失败";
			return -1;
		}
		updateAnswersStatus(this.question_id);
		error.code = 0;
		error.msg = "管理员回复成功";
		return 1;
		
	}
	
	/**
	 * 用户回复
	 * @param error
	 * @return
	 */
	public int savePostsAnswers(ErrorInfo error){
		t_forum_posts_questions t = new t_forum_posts_questions();
		
		t.posts_id = this.posts_id;
		t.time = new Date();
		t.content = this.content;
		t.user_id = this.user_id;
		t.answer_status = 0;
		t.to_answer_user = this.to_answer_user;
		t.to_answer_user_id = this.to_answer_user_id;
		t.read_status = 0;
		try {
			t.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("用户回复时：", e);
			error.code = -1;
			error.msg = "用户回复失败";
			return -1;
		}
		//updateAnswersStatus(this.question_id);
		error.code = 0;
		error.msg = "用户回复成功";
		return 1;
		
	}
	
	/**
	 * 增加回帖次数
	 * @param id
	 */
	public static void updateAppAnswersCount(long id){
		String sql = "update t_forum_posts set answers_count = answers_count + 1 where id = ?";
		
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, id);
		try {
			query.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("根据用户回复id修改回复状态", e);
			
		}
	}
	
	/**
	 * 根据用户回复id修改状态为已回复
	 * @param id
	 */
	public void updateAnswersStatus(long id){
		String sql = "update t_forum_posts_questions set answer_status = 1 where id = ?";
		
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, id);
		try {
			query.executeUpdate();
		} catch (Exception e) {
			
			e.printStackTrace();
			Logger.info("根据用户回复id修改回复状态", e);
			
		}
	}
	
	/**
	 * 用户收藏帖子
	 * @param error
	 */
	public int saveForumPostsCollection(ErrorInfo error){
		
		List<t_forum_posts_collection> list = t_forum_posts_collection.find(" posts_id = ? and user_id = ? ", this.posts_id,this.user_id).fetch();
		
		if(list.size() > 0){
			error.code = -4;
			error.msg = "用户已收藏帖子";
			return error.code;
		}
		
		t_forum_posts_collection t = new t_forum_posts_collection();
		t.posts_id = this.posts_id;
		t.user_id = this.user_id;
		t.time = this.time;
		
		try {
			t.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("$$$$$$$$$$$$$$$$############用户收藏帖子失败：userId="+this.user_id, e);
			error.code = -3;
			error.msg = "用户收藏帖子失败";
			return error.code;
		}
		error.code = 0;
		error.msg = "用户收藏帖子成功";
		return error.code;
	}
	
	/**
	 * 保存论坛昵称
	 * @param name
	 * @param error
	 */
	public static int saveForumName(String name,long id,ErrorInfo error){
		String sql = "update t_users set forum_name = ? where id = ?";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, name).setParameter(2, id);
		int row = 0;
		try {
			row = query.executeUpdate();
		} catch (Exception e) {
			Logger.info("@@@@@@@@@@@@@@@@@@@用户增加昵称失败：userId="+id,e);
			error.code = -3;
			error.msg = "用户增加昵称失败";
			return row;
		}
		error.code = 0;
		error.msg = "用户增加昵称成功";
		return row;
	}
	
	/**
	 * 论坛模糊搜索标题，内容
	 * @param name
	 * @param typeId
	 * @return
	 */
	public static PageBean<t_forum_posts> earchPostsByName(String content,String currPage, String pageSize){
		int Page = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		int Size = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 5;
		List<t_forum_posts> t = new ArrayList<t_forum_posts>();
		
		if(StringUtils.isBlank(content)){
			t = t_forum_posts.find(" show_time <= ? and status = 0 order by rId desc, add_time desc ",new Date()).fetch(Page,Size);
		}else{
			t = t_forum_posts.find(" (content like ? or title like ?) and show_time <= ? and status = 0 order by rId desc,add_time desc ", "%"+content+"%","%"+content+"%",new Date()).fetch(Page,Size);
		}

		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("content", content);
		PageBean<t_forum_posts> page = new PageBean<t_forum_posts>();
		page.pageSize = Size;
		page.currPage = Page;
		page.page = t;
		
		page.conditions = conditionMap;
		return page;
	}
	
	/**
	 * 查询所有的帖子类型
	 * @return
	 */
	public static List<t_forum_type> queryPostsType(String currPage, String pageSize){
		
		int Page = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		int Size = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 4;
		
		List<t_forum_type> t = t_forum_type.find( " status = 0 order by rId,id " ).fetch(Page,Size);
		return t;
	}
	
	/**
	 * 查询用户发帖类型
	 * @return
	 */
	public static List<t_forum_type> queryUserPostsType(){
		List<t_forum_type> t = t_forum_type.find( " id in (4,5,7,8) and status = 0 order by rId,id " ).fetch();
		return t;
	}
	
	
	/**
	 * 查询用户昵称
	 * @param id
	 * @return
	 */
	public static String queryForumName(long id){
		t_users t = t_users.findById(id);
		if(StringUtils.isNotBlank(t.forum_name)){
			return t.forum_name;
		}
		
		return t.mobile;
	}
	
	/**
	 * 添加收藏
	 */
	public int saveCollection(ErrorInfo error){
		t_forum_posts_collection t = new t_forum_posts_collection();
		t.posts_id = this.posts_id;
		t.user_id = this.user_id;
		t.time = new Date();
		
		try {
			t.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("app添加收藏：", e);
			error.code = -3;
			error.msg = "收藏失败";
			return error.code;
					
		}
		error.code = 0;
		error.msg = "收藏成功";
		return error.code;
		
	}
	
	/**
	 * 我收藏的帖子
	 * @param userId
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<v_forum_posts_collection> queryUserPostsCollection(long userId,String currPageStr,String pageSizeStr,ErrorInfo error){
		
		List<v_forum_posts_collection> contents = new ArrayList<v_forum_posts_collection>();
		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = new StringBuffer();
		List<Object> values = new ArrayList<Object>();
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		sql.append(SQLTempletes.V_POSTS_COLLECTION);
		sqlCount.append("select count(s.id) from t_forum_posts s,t_forum_posts_collection f where s.id = f.posts_id and f.user_id = ? ");
		sql.append(" and f.user_id = ? ");
		values.add(userId);
		
		sql.append(" order by add_time desc ");
		int count = 0;
		try{
			Query countV = JPA.em().createNativeQuery(sqlCount.toString());
			for (int i = 0; i < values.size(); i++) {
				countV.setParameter(i+1 , values.get(i));
			}
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			Query query = JPA.em().createNativeQuery(sql.toString(),v_forum_posts_collection.class).setParameter(1, userId);
			for (int i = 0; i < values.size(); i++) {
				query.setParameter(i+1 , values.get(i));
			}
			query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
			contents = query.getResultList();
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		PageBean<v_forum_posts_collection> page = new PageBean<v_forum_posts_collection>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;
		return page;
	}
	
	/**
	 * 查询相关帖子回复
	 * @param id
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<t_forum_posts_questions> queryUserPostsListById(long id,String currPageStr,String pageSizeStr,ErrorInfo error){
		
		List<t_forum_posts_questions> contents = new ArrayList<t_forum_posts_questions>();
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		if(NumberUtil.isNumericInt(currPageStr)){
			currPage = Integer.parseInt(currPageStr);
		}
		
		if(NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		/*StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = new StringBuffer();*/
	
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		/*sql.append("SELECT * from (");
		sql.append(SQLTempletes.V_USER_POSTS_ANSWERS_LIST);
		sql.append(") t");
		sqlCount.append("SELECT count() from (");
		sqlCount.append(SQLTempletes.V_USER_POSTS_ANSWERS_LIST);
		
		
		sql.append(" order by time desc ");*/
		int count = 0;
		try{
			
			count = t_forum_posts_questions.find(" posts_id = ? order by time desc", id).fetch().size();
			
			contents = t_forum_posts_questions.find(" posts_id = ? order by time desc", id).fetch((currPage - 1) * pageSize, pageSize);
			
			/*Query countV = JPA.em().createNativeQuery(sqlCount.toString()).setParameter(1, id).setParameter(1, id);
			
			count = Integer.parseInt(countV.getSingleResult().toString());
			
			
			Query query = JPA.em().createNativeQuery(sql.toString(),v_forum_posts_collection.class).setParameter(1, id).setParameter(1, id);
		
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			contents = query.getResultList();*/
		}catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询新闻列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询新闻列表失败";
			
			return null;
		}
		
		User user = new User();
		for(t_forum_posts_questions vo:contents){
			user.id = vo.user_id;
			vo.userName = user.name;
		}
		
		PageBean<t_forum_posts_questions> page = new PageBean<t_forum_posts_questions>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.page = contents;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		error.code = 0;
		return page;
	}
	
	public void deletePostscollection(ErrorInfo error){
		t_forum_posts_collection.delete(" user_id = ? and posts_id = ?", this.user_id,this.posts_id);
	}
}
