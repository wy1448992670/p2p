package business;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import models.t_user_actions;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;

/**
 * 
* Description: 客户服务-实名、绑卡失败统计
* @author xinsw
* @date 2017年8月16日
 */
public class UserActions implements Serializable{
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年8月16日
	 * @description 添加失败统计记录
	 * @param userId
	 * @param type
	 * @param error
	 * @return
	 */
	public static int insertAction(long userId,int type,String msg,ErrorInfo error){
		error.clear();
		t_user_actions action = new t_user_actions();
		
		action.status = 0;
		action.user_id = userId;
		action.type = type;
		action.msg = msg;
		
		try {
			action.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("添加实名、绑卡失败统计时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致添加实名、绑卡失败统计失败！";
			
			return error.code;
		}
		
		error.code = 0;
		error.msg = "实名、绑卡失败统计添加成功！";

		return 0;
	}
	
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年8月16日
	 * @description 客服修改状态和回访记录
	 * @param params
	 * @param error
	 */
	public static void updateAction(long id,int status,String record,ErrorInfo error){
		error.clear();
		if(status < -1 || status > 3){
			error.code = -1;
			error.msg = "记录状态错误";
			return;
		}
		if(StringUtils.isBlank(record)){
			error.code = -1;
			error.msg = "回访记录不能为空";
			return;
		}
		if(record.length() > 50){
			error.code = -1;
			error.msg = "回访记录最多50个字";
			return;
		}
		
		t_user_actions action = t_user_actions.findById(id);
		if(action == null){
			error.code = -1;
			error.msg = "无效的记录";
			return;
		}
		
		action.status = status;
		action.record = record;
		
		try {
			action.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("修改实名、绑卡失败统计时：" + e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致修改实名、绑卡失败统计失败！";
		}
		
		error.code = 0;
		error.msg = "实名、绑卡失败统计修改成功！";
	}
	
	
	public static t_user_actions findById(long id,ErrorInfo error){
		t_user_actions actions = null;
		try {
			actions = t_user_actions.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "由于数据库异常查询失败！";
		}
		if(actions == null){
			error.code = -1;
			error.msg = "无效的记录！";
		}else{
			error.code = 0;
		}
		return actions;
	}
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年8月17日
	 * @description 实名、绑卡失败统计分页
	 * @param start
	 * @param end
	 * @param keyword
	 * @param statusStr
	 * @param orderTypeStr
	 * @param currPageStr
	 * @param pageSizeStr
	 * @param error
	 * @return
	 */
	public static PageBean<Map<String,Object>> getPage(String start,String end,String keyword,String statusStr,
			String orderTypeStr,String currPageStr, String pageSizeStr, ErrorInfo error){
		int status = -2;
 		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int orderType = 0;
 		
 		if(NumberUtil.isNumericInt(statusStr)) {
 			status = Integer.parseInt(statusStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 			if(orderType < 0 || orderType > 2) {
 	 			orderType = 0;
 	 		}
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		StringBuffer sql = new StringBuffer("FROM `t_user_actions` a join t_users u on a.user_id = u.id where 1=1 ");
 		
 		List<Object> params = new ArrayList<Object>();
 		
 		if(StringUtils.isNotBlank(start)){
 			sql.append(" and a.action_time >= ?");
 			params.add(DateUtil.strDateToStartDate(start));
 		}
 		
 		if(StringUtils.isNotBlank(end)){
 			sql.append(" and a.action_time <= ?");
 			params.add(DateUtil.strDateToEndDate(end));
 		}
 		
 		if(StringUtils.isNotBlank(keyword)){
 			sql.append(" and (instr(u.name,?) > 0 or instr(u.mobile,?) > 0) ");
 			params.add(keyword);
 			params.add(keyword);
 		}
 		
 		if(status != -2){
 			sql.append(" and a.status = ? ");
 			params.add(status);
 		}
 		
 		String cntSql = "select count(1) as count " + sql.toString();
 		
 		int count = 0;
		List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, params.toArray());
		if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
			count = ((BigInteger)countMap.get(0).get("count")).intValue();
		}
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
    	
    	conditionMap.put("start", start);
		conditionMap.put("end", end);
		conditionMap.put("keyword", keyword);
		conditionMap.put("status", status);
		conditionMap.put("currPage", currPage);
		conditionMap.put("pageSize", pageSize);
		conditionMap.put("orderType", orderType);
		
		String listSql = "select a.id,u.`name`,u.mobile,a.type, a.action_time as actionTime,a.`status`,a.record ";
  	
		listSql += sql.toString();
		
		if(orderType != 0) {
			listSql += Constants.USER_ACTION_ORDER[orderType];
		}else {
			listSql += " ORDER BY a.ID DESC ";
		}
		
		listSql += " limit ?,?";
		params.add((currPage - 1) * pageSize);
  		params.add(pageSize);
  		
	  	System.out.println(listSql);
	  	System.out.println(params.size());
	  	List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, params.toArray());
	  	
	  	PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		
		page.page = list;
 		
        return page;
	}
}
