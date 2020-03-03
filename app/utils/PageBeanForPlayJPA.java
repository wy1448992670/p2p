package utils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jsoup.helper.StringUtil;

import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPA;

public class PageBeanForPlayJPA {

	public static <T extends GenericModel> PageBean<T> getPageBean(T t,String condition,String orderby,String currPageStr,String pageSizeStr,Object... params){
		int currPage=1;
		int pageSize=10;
		if(!StringUtil.isBlank(currPageStr)&& NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr)&& NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr);
		}
		return getPageBean(t,condition,orderby,currPage,pageSize,params);
	}
	/**
	 * pageSize<1 不分页
	 * @param t
	 * @param condition
	 * @param orderby
	 * @param currPage
	 * @param pageSize
	 * @param params
	 * @return
	 */
	public static <T extends GenericModel> PageBean<T> getPageBean(T t,String condition,String orderby,int currPage,int pageSize,Object... params){
		currPage = currPage <= 0 ? 1 : currPage;
		//pageSize = pageSize <= 0 ? 10 : pageSize;
		Long count=0L;
		List<T> theList=new ArrayList<T>();
		try {
			List<Object> paramsList = new ArrayList<Object>();
			if(params!=null) for(Object object:params){
				paramsList.add(object);
			}

			//JPAQuery jpaQuery=t.find(condition+" "+orderby, params);
			JPAQuery jpaQuery=(JPAQuery)t.getClass().getMethod("find",new Class[]{String.class,Object[].class}).invoke(null,condition+" "+orderby,paramsList.toArray());
			//pageSize<1 不分页
			if(pageSize>0) {
				theList=jpaQuery.fetch(currPage, pageSize);
			}else {
				theList=jpaQuery.fetch();
			}
			//totalCount=t.count(condition, params);
			count=(Long)t.getClass().getMethod("count",new Class[]{String.class,Object[].class}).invoke(null,condition,paramsList.toArray());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		PageBean<T> resultPage = new PageBean<T>();
		resultPage.currPage = currPage;
		resultPage.pageSize = pageSize;
		resultPage.totalCount = count.intValue();
		if(pageSize>0) {
			resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
		}else{
			resultPage.totalPageCount=1;
		}
		resultPage.page = theList;

		return resultPage;
	}

	public static PageBean<Map<String, Object>> getPageBeanMapBySQL(String columns,String table_condition,String orderby,String currPageStr,String pageSizeStr,Object... params) throws Exception{
		int currPage=1;
		int pageSize=10;
		if(!StringUtil.isBlank(currPageStr)&& NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr)&& NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr);
		}
		return getPageBeanMapBySQL(columns,table_condition,orderby,currPage,pageSize,params);
	}

	/**
	 * pageSize<1 不分页
	 * @param columns
	 * @param table_condition
	 * @param orderby
	 * @param currPage
	 * @param pageSize
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static PageBean<Map<String, Object>> getPageBeanMapBySQL(String columns,String table_condition,String orderby,int currPage,int pageSize,Object... params) throws Exception{
		currPage = currPage <= 0 ? 1 : currPage;
		//pageSize = pageSize <= 0 ? 10 : pageSize;
		BigInteger count=BigInteger.ZERO;

		String limitSql = " select "+columns+" "+table_condition+" "+orderby;
		//pageSize<1 不分页
		if(pageSize>0) {
			limitSql+=" LIMIT "+( (currPage - 1) * pageSize )+","+pageSize;
		}
		String cntSql = " select count(1) as count "+table_condition;

		count=(BigInteger) JPAUtil.createNativeQuery(cntSql, params).getSingleResult();
		ErrorInfo errorInfo=new ErrorInfo();
		List<Map<String, Object>> list = JPAUtil.getList(errorInfo, limitSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		PageBean<Map<String, Object>> resultPage = new PageBean<Map<String, Object>>();
		resultPage.currPage = currPage;
		resultPage.pageSize = pageSize;
		resultPage.totalCount = count.intValue();
		if(pageSize>0) {
			resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
		}else{
			resultPage.totalPageCount=1;
		}
		resultPage.page = list;

		return resultPage;
	}


	public static PageBean<Map<String, Object>> getPageBeanMapBySQL2(String columns,String totalColumns,String table_condition,String orderby,int currPage,int pageSize,Object... params) throws Exception{
		currPage = currPage <= 0 ? 1 : currPage;
		pageSize = pageSize <= 0 ? 10 : pageSize;
		BigInteger count=BigInteger.ZERO;

		String limitSql = " select "+columns+" "+table_condition+" "+orderby+" LIMIT "+( (currPage - 1) * pageSize )+","+pageSize;
		String cntSql = " select count(1) as count, "+ totalColumns + " " + table_condition;


		ErrorInfo errorInfo=new ErrorInfo();

		List<Map<String, Object>> totalList = JPAUtil.getList(errorInfo, cntSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		Map<String, Object> totalMap = totalList.get(0);


		List<Map<String, Object>> list = JPAUtil.getList(errorInfo, limitSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		PageBean<Map<String, Object>> resultPage = new PageBean<Map<String, Object>>();
		resultPage.currPage = currPage;
		resultPage.pageSize = pageSize;
		resultPage.totalCount = Integer.parseInt(totalMap.get("count").toString());
		resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
		resultPage.totalMap = totalMap;
		resultPage.page = list;

		return resultPage;
	}


	public static PageBean<Map<String, Object>> getPageBeanMapBySQLExport(String columns,String table_condition,String orderby, boolean isExport, int currPage,int pageSize,Object... params) throws Exception{
		currPage = currPage <= 0 ? 1 : currPage;
		pageSize = pageSize <= 0 ? 10 : pageSize;
		BigInteger count=BigInteger.ZERO;

		String limitSql = " select "+columns+" "+table_condition+" "+orderby;

		if(!isExport) {
			limitSql = limitSql + " LIMIT " + ((currPage - 1) * pageSize) + "," + pageSize;
		}

		String cntSql = " select count(1) as count "+table_condition;

		count=(BigInteger) JPAUtil.createNativeQuery(cntSql, params).getSingleResult();
		ErrorInfo errorInfo=new ErrorInfo();
		List<Map<String, Object>> list = JPAUtil.getList(errorInfo, limitSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		PageBean<Map<String, Object>> resultPage = new PageBean<Map<String, Object>>();
		resultPage.currPage = currPage;
		resultPage.pageSize = pageSize;
		resultPage.totalCount = count.intValue();
		resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
		resultPage.page = list;

		return resultPage;
	}


	public static PageBean<Map<String, Object>> getPageBeanMapBySQLExport2(String columns,String totalColumns,String table_condition,String orderby, boolean isExport,int currPage,int pageSize,Object... params) throws Exception{
		currPage = currPage <= 0 ? 1 : currPage;
		pageSize = pageSize <= 0 ? 10 : pageSize;
		BigInteger count=BigInteger.ZERO;

		String limitSql = " select "+columns+" "+table_condition+" "+orderby;

		if(!isExport) {
			limitSql = limitSql + " LIMIT " + ((currPage - 1) * pageSize) + "," + pageSize;
		}

		String cntSql = " select count(1) as count, "+ totalColumns + " " + table_condition;


		ErrorInfo errorInfo=new ErrorInfo();

		List<Map<String, Object>> totalList = JPAUtil.getList(errorInfo, cntSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		Map<String, Object> totalMap = totalList.get(0);


		List<Map<String, Object>> list = JPAUtil.getList(errorInfo, limitSql, params);
		if(errorInfo.code<0){
			throw new Exception(errorInfo.msg);
		}

		PageBean<Map<String, Object>> resultPage = new PageBean<Map<String, Object>>();
		resultPage.currPage = currPage;
		resultPage.pageSize = pageSize;
		resultPage.totalCount = Integer.parseInt(totalMap.get("count").toString());
		resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
		resultPage.totalMap = totalMap;
		resultPage.page = list;

		return resultPage;
	}
	
	/**
	 * pageSize<1 不分页
	 * @param t
	 * @param columns
	 * @param table_condition
	 * @param orderby
	 * @param currPage
	 * @param pageSize
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static <T extends GenericModel> PageBean<T> getPageBeanBySQL(T t,String columns,String table_condition,String orderby,int currPage,int pageSize,Object... params){
		currPage = currPage <= 0 ? 1 : currPage;
		//pageSize = pageSize <= 0 ? 10 : pageSize;
		BigInteger count=BigInteger.ZERO;
		
		String limitSql = " select "+columns+" "+table_condition+" "+orderby;
		//pageSize<1 不分页
		/*
		if(pageSize>0) {
			limitSql+=" LIMIT "+( (currPage - 1) * pageSize )+","+pageSize;
		}*/
    	String cntSql = " select count(1) as count "+table_condition;

    	count=(BigInteger) JPAUtil.createNativeQuery(cntSql, params).getSingleResult();
    	
    	EntityManager em = JPA.em();
    	Query query = em.createNativeQuery(limitSql.toString(), t.getClass());
    	for (int n = 1; n <= params.length; n++) {
			query.setParameter(n, params[n - 1]);
		}
    	//pageSize<1 不分页
    	if(pageSize>0) {
    		query.setFirstResult((currPage - 1) * pageSize);
    		query.setMaxResults(pageSize);
    	}
		
    	List<T> list = query.getResultList();
    	
    	PageBean<T> resultPage = new PageBean<T>();
    	resultPage.currPage = currPage;
    	resultPage.pageSize = pageSize;
    	resultPage.totalCount = count.intValue();
    	if(pageSize>0) {
    		resultPage.totalPageCount=((resultPage.totalCount-1)/pageSize)+1;
    	}else{
    		resultPage.totalPageCount=1;
    	}
    	resultPage.page = list;

    	return resultPage;
	}
}
