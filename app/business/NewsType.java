package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import models.t_content_news_types;
import models.v_news_types;

public class NewsType implements Serializable{

	public long id;
	private long _id;

	public void setId(long id) {
		t_content_news_types type = null;

		try {
			type = t_content_news_types.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时，根据ID查询查询类别："+e.getMessage());
			this._id = -1;
			
			return;
		}

		if (type == null) {
			this._id = -1;
			
			return;
		}

		this._id = type.id;
		this.parentId = type.parent_id;
		this.name = type.name;
		this.description = type.description;
		this.order = type._order;
		this.status = type.status;

	}

	public long getId() {
		return this._id;
	}

	public NewsType parentType;
	/**
	 * 出现此相反情况，是因为逻辑顺序也和以前的相反
	 */
	public long parentId;
	public long _parentId;
	
	/**
	 * 
	 * @param id
	 */
	public void setParentId(long id) {
		this.parentId = id;
		parentType = new NewsType();
		
		if(this.lazy) {
			parentType._id = id;
		}
		
		if (!this.lazy && id > 0) {
			parentType.id = id;
		}
	}
	
	public String name;
	public String description;
	public int order;
	public boolean status;

	public boolean lazy;
	
	public boolean hasChild;
	
	public boolean getHasChild() {
		
		return true;
	}
	
//	public boolean queryHasChild() {
//		long count = 0;
//		
//		try{
//			
//		}catch(Exception e) {
//			e.printStackTrace();
//			Logger.info("判断是否有子类别时："+e.getMessage());
//			return false;
//		}
//		
//		
//		
//	}
	
	public List<NewsType> childTypes;
	
	public List<NewsType> getChildTypes() {
		ErrorInfo error = new ErrorInfo();
		
		return queryChildTypes(1L, this.id+"", error);
	}
	
	public static String queryName(long id) {
		return t_content_news_types.find("select name from t_content_news_types where id = ?", id).first();
	}
	
	
	/**
	 * 查询最顶级的类别
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static List<NewsType> queryTopTypes(ErrorInfo error) {
		error.clear();
		
		List<t_content_news_types> types = new ArrayList<t_content_news_types>();
		List<NewsType> childTypes = new ArrayList<NewsType>();

		try {
			types = t_content_news_types.find("parent_id = -1 order by _order").fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询类别失败";
			return null;
		}

		NewsType childType = null; 
		
		for(t_content_news_types type : types) {
			
			childType = new NewsType();
			
			childType._id = type.id;
			childType.name = type.name;
			childType.parentId  = type.parent_id;
			childType.description = type.description;
			childType.status = type.status;
			childType.order = type._order;
			
			childTypes.add(childType);
		}
		
		error.code = 0;
		
		return childTypes;
	}

	/**
	 * 根据父类别id查询子类别信息（列表显示）
	 * @param parentId 父类别id
	 * @param error
	 * @return
	 */
	public static List<NewsType> queryChildTypes(long supervisorId, String parentIdStr, ErrorInfo error) {
		error.clear();
		
		if(!NumberUtil.isNumericInt(parentIdStr)) {
			error.code = -1;
			error.msg = "传入类型参数有误！";
 		}
		
		long parentId = Long.parseLong(parentIdStr);
		
		List<t_content_news_types> types = new ArrayList<t_content_news_types>();
		List<NewsType> childTypes = new ArrayList<NewsType>();

		try {
			types = t_content_news_types.find("parent_id = ? and status=true order by _order", parentId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询类别失败";
			return null;
		}

		NewsType childType = null; 
		
		for(t_content_news_types type : types) {
			
			childType = new NewsType();
			
			childType._id = type.id;
			childType.name = type.name;
			childType.parentId  = type.parent_id;
			childType.description = type.description;
			childType.status = type.status;
			childType.order = type._order;
			
			childTypes.add(childType);
		}
		
		error.code = 0;
		
		return childTypes;
	}
	
	/**
	 * 用于前台
	 * @param supervisorId
	 * @param parentId
	 * @param error
	 * @return
	 */
	public static List<NewsType> queryChildTypes(long parentId, ErrorInfo error) {
		error.clear();
		
		List<t_content_news_types> types = new ArrayList<t_content_news_types>();
		List<NewsType> childTypes = new ArrayList<NewsType>();
		
		String sql = "select new t_content_news_types(id, name) from t_content_news_types type where "
				+ "type.parent_id = ? and type.status = true order by _order";

		try {
			types = t_content_news_types.find(sql, parentId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询类别失败";
			return null;
		}

		NewsType childType = null; 
		
		for(t_content_news_types type : types) {
			
			childType = new NewsType();
			
			childType._id = type.id;
			childType.name = type.name;
			
			childTypes.add(childType);
		}
		
		error.code = 0;
		
		return childTypes;
	}
	
	/**
	 * 查询类别包含数量
	 * @param parentId
	 * @param error
	 * @return
	 */
	public static List<v_news_types> queryTypeAndCount(long parentId, ErrorInfo error) {
		error.clear();
		
		List<v_news_types> types = new ArrayList<v_news_types>();
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_NEWS_TYPES);
		sql.append(" and parent_id = ? and status = true order by _order");
			
		try {
			//types = v_news_types.find("parent_id = ? and status = true order by _order", parentId).fetch();
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_news_types.class);
            query.setParameter(1, parentId);
            types = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询类别失败";
			return null;
		}
		
		error.code = 0;
		
		return types;
	}
	
	/**
	 * 根据父类别id查询子类别信息（用于下拉显示）
	 * @param parentId 父类别id
	 * @param error
	 * @return
	 */
	public static List<t_content_news_types> queryChildTypesForList(String parentIdStr, ErrorInfo error) {
		error.clear();
		
		if(!NumberUtil.isNumericInt(parentIdStr)) {
			error.code = -1;
			error.msg = "传入类型参数有误！";
 		}
		
		long parentId = Long.parseLong(parentIdStr);
		
		List<t_content_news_types> types = new ArrayList<t_content_news_types>();
//		List<NewsType> childTypes = new ArrayList<NewsType>();
		
		String sql = "select new t_content_news_types(id, name) from t_content_news_types type where "
				+ "type.status = true and type.parent_id = ? order by _order";

		try {
			types = t_content_news_types.find(sql, parentId).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询类别失败";
			return null;
		}

//		NewsType childType = null; 
		
//		for(t_content_news_types type : types) {
//			
//			childType = new NewsType();
//			
//			childType._id = type.id;
//			childType.name = type.name;
//			
//			childTypes.add(childType);
//		}
		
		error.code = 0;
		
		return types;
	}
	
	/**
	 * 根据父类别id查询子类别信息（用于下拉显示）
	 * @param parentId 父类别id
	 * @param error
	 * @return
	 */
	public static t_content_news_types queryParentType(long typeId, ErrorInfo error) {
		error.clear();
		
		
		t_content_news_types parentType = new t_content_news_types();
		
		String sql = "select new t_content_news_types(id, name) from t_content_news_types type where "
				+ "type.status = true and id = ?";

		try {
			parentType = t_content_news_types.find(sql, typeId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询父类别失败";
			return null;
		}
		
		if(parentType == null) {
			error.code = -1;
			error.msg = "父类别不存在";
			
			return null;
		}
		
		error.code = 0;
		
		return parentType;
	}
	
	

	/**
	 * 添加类别
	 * @param supvisorId 管理员id
	 * @param error
	 * @return
	 */
	public int addChildType(long supervisorId, ErrorInfo error) {
		error.clear();
		
		if(this._parentId <=0 ) {
			error.code = -1;
			error.msg = "请选择父类别";
			
			return error.code;
		}
		
		if(StringUtils.isBlank(name)) {
			error.code = -1;
			error.msg = "请输入类别名称";
			
			return error.code;
		}
		
		if(this.order <= 0) {
			error.code = -1;
			error.msg = "请输入排序";
			
			return error.code;
		}
		
		if(NewsType.orderExist(this._parentId, order, error)) {
			return error.code;
		}
		
		t_content_news_types childType = new t_content_news_types();
		
		childType.parent_id = this._parentId;
		childType.name = this.name;
		childType.description = this.description;
		childType.status = Constants.TRUE;
		childType._order = this.order;
		
		try {
			childType.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加添加类别时，保存添加的类别时："+e.getMessage());
			error.code = -1;
			error.msg = "添加类别失败";
			
			return -1;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ADD_NEWSTYPE,
				"添加类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		error.code = 0;
		error.msg = "添加类别成功";
		this._id = childType.id;
		
		return 0;
	}

	/**
	 * 隐藏类别(该类别的子类别也需隐藏)
	 * @param supvisorId 管理员id
	 * @param typeId 隐藏类别的id
	 * @param error
	 * @return
	 */
	public static int hideType(long supvisorId, long typeId, ErrorInfo error) {
		EntityManager em = JPA.em();
		
		String mySql = "update t_content_news_types set status = ? where id = ?";
		
		int rows = 0;
		
		try {
			rows = em.createQuery(mySql).setParameter(1, false).setParameter(2, typeId).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("隐藏类别,更新类别状态时："+e.getMessage());
			error.code = -1;
			error.msg = "更新类别状态失败";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		/*
		 * 递归修改子类别的属性
		 */
		String sql2 = "select id from t_content_news_types where parent_id = ?";
		List<Long> ids = null;
		
		try {
			ids = t_content_news_types.find(sql2, typeId).fetch();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("隐藏类别,更新类别状态时："+e.getMessage());
			error.code = -2;
			error.msg = "查询子类别失败";
			
			return error.code;
		}
		
		if(ids != null && ids.size() != 0) {
			
			for(long id : ids) {
				hideType(supvisorId, id, error);
			}
		}
		
		DealDetail.supervisorEvent(supvisorId, SupervisorEvent.HIDE_NEWSTYPE,
				"隐藏类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		error.code = 0;
		error.msg = "更新类别状态成功";
		
		return 0;
	}
	
	/**
	 * 显示类别(该类别的子类别也跟着显示)
	 * @param supvisorId 管理员id
	 * @param typeId 隐藏类别的id
	 * @param error
	 * @return
	 */
	public static int showType(long supvisorId, long typeId, ErrorInfo error) {
		error.clear();
		
//		String sql = "select status from t_content_news_types where id = ?";
//		boolean status = false;
//		
//		try {
//			status = t_content_news_types.find(sql, typeId).first();
//		} catch(Exception e) {
//			e.printStackTrace();
//			error.msg = "查询类别状态失败";
//			return -1;
//		}
//		
//		if(status) {
//			error.msg = "该类别已是显示状态";
//			return -1;
//		}
		
		EntityManager em = JPA.em();
		String mySql = "update t_content_news_types set status = ? where id = ?";
	
		int rows = 0;
	
		try {
			rows = em.createQuery(mySql).setParameter(1, true).setParameter(2, typeId).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("显示类别,更新类别状态时："+e.getMessage());
			error.code = -1;
			error.msg = "更新类别状态失败";
			
			return error.code;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error.code;
		}
		
		/*
		 * 递归修改子类别的属性
		 */
		String sql2 = "select id from t_content_news_types where parent_id = ?";
		List<Long> ids = null;
		
		try {
			ids = t_content_news_types.find(sql2, typeId).fetch();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("显示类别,查询子类别时："+e.getMessage());
			error.code = -2;
			error.msg = "更新类别状态失败";
			
			return error.code;
		}
		
		if(ids != null && ids.size() != 0) {
			
			for(long id : ids) {
				showType(supvisorId, id, error);
			}
		}
		
		DealDetail.supervisorEvent(supvisorId, SupervisorEvent.SHOW_NEWSTYPE,
				"显示类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		error.code = 0;
		error.msg = "更新类别状态成功";
		
		return 0;
	}
	
	/**
	 * 编辑类别
	 * @param supvisorId
	 * @param typeId
	 * @param error
	 * @return
	 */
	public int editType(long supervisorId, long id, ErrorInfo error) {
		error.clear();
		
		t_content_news_types type = null;
		
		try {
			type = t_content_news_types.findById(id);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑类别,查询类别时："+e.getMessage());
			error.code = -1;
			error.msg = "编辑类别失败";
			
			return error.code;
		}
		
		type.name = this.name;
		type.description = this.description;
		type._order = this.order;
		
		try {
			type.save();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑类别,更新类别时："+e.getMessage());
			error.code = -2;
			error.msg = "编辑类别失败";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_NEWSTYPE,
				"编辑类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		error.code = 0;
		error.msg = "更新类别成功";
		
		return 0;
	}
	
	/**
	 * 编辑子类别
	 * @param supvisorId
	 * @param typeId
	 * @param error
	 * @return
	 */
	public int editChildType(long supervisorId, long id, ErrorInfo error) {
		error.clear();
		
		t_content_news_types type = null;
		
		try {
			type = t_content_news_types.findById(id);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑类别,查询类别时："+e.getMessage());
			error.code = -1;
			error.msg = "编辑类别失败";
			
			return error.code;
		}
		
		type.name = this.name;
		type._order = this.order;
		
		try {
			type.save();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("编辑类别,更新类别时："+e.getMessage());
			error.code = -2;
			error.msg = "编辑类别失败";
			
			return error.code;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_NEWSTYPE,
				"编辑类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}
		
		error.code = 0;
		error.msg = "更新类别成功";
		
		return 0;
	}
	
	/**
	 * 判断排序是否存在
	 * @param typeId
	 * @param order
	 * @param error
	 * @return true存在 false不存在
	 */
	public static boolean orderExist(long typeId, int order, ErrorInfo error) {
		error.clear();
		
//		if(typeId <= 0) {
// 			error.code = -1;
// 			error.msg = "传入参数有误！";
// 			
// 			return true;
// 		}
// 		
// 		if(!NumberUtil.isNumericInt(orderStr)) {
// 			error.code = -2;
// 			error.msg = "传入参数有误！";
// 			
// 			return true;
// 		}
// 		
// 		long typeId = Long.parseLong(typeIdStr);
// 		int order = Integer.parseInt(orderStr);
 		
 		long count = 0;
 		
 		try {
 			count = t_content_news_types.count("parent_id = ? and _order = ?", typeId, order);
 		} catch(Exception e) {
 			e.printStackTrace();
 			Logger.info("查询类别的排序是否存在时："+e.getMessage());
 			
 			error.code = -2;
 			error.msg = "数据库查询失败！";
 			
 			return true;
 		}
 		
 		if(count > 0) {
 			error.code = -2;
 			error.msg = "该排序已存在";
 			
 			return true;
 		}
 		
 		error.code = 0;
 		
 		return false;
	}
	
	/**
	 * 删除类别
	 * @param supervisorId
	 * @param type
	 * @param error
	 */
	public static void deleteType(long supervisorId, Long[] types, ErrorInfo error) {
		error.clear();
		
		if(types == null || types.length == 0) {
			error.code = -1;
			error.msg = "请选择要删除的类别";
			
			return;
		}
		
		StringBuffer typeString = new StringBuffer("(");
		
		for(int i=0;i<types.length;i++) {
			typeString.append("?,");
		}
		
		typeString.replace(typeString.length()-1, typeString.length(), ")");
		
		
		String sql = "delete from t_content_news_types as type where type.id in "+typeString.toString();
		String sql2 = "delete from t_content_news as news where news.type_id in "+typeString.toString();
		
		Query query = JPA.em().createQuery(sql);
		Query query2 = JPA.em().createQuery(sql2);
		
		for(int i=0;i<types.length;i++) {
			query.setParameter(i+1, types[i]);
			query2.setParameter(i+1, types[i]);
		}
		
		int rows1 = 0;
		
		try{
			rows1 = query.executeUpdate();
			query2.executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("删除类别时："+e.getMessage());
			error.code = -1;
			error.msg = "删除类别时出现异常";
			
			return;
		}
		
		if(rows1 == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return ;
		}
		
		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.DEL_NEWSTYPE, "删除类别", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return ;

		}
		
		error.code = 0;
		error.msg = "类别删除成功";
	}
	
	/**
	 * 根据底部连接传递过来的ID查询
	 * @param id
	 * @return
	 */
	public static List<t_content_news_types> queryNewsTypeByPid(long id) {
		String sql = "select new t_content_news_types(id, name) from t_content_news_types where status = true and parent_id = (select parent_id from t_content_news_types where id = ?)";
		List<t_content_news_types> newsTypes = new ArrayList<t_content_news_types>();
		
		try {
			newsTypes = t_content_news_types.find(sql, id).fetch();
		} catch (Exception e) {
			
		}
		
		return newsTypes;
	}
}
