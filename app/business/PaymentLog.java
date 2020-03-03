package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_mmm_data;
import models.t_return_data;
import models.v_mmm_return_data;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.JPA;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.RegexUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shove.Convert;

import constants.Constants;
import constants.SQLTempletes;

public class PaymentLog implements Serializable{

	public static final Gson gson = new Gson();
	/**
	 * 后台--流水账单管理
	 * @param id
	 * @return
	 */
	public static PageBean<v_mmm_return_data> queryMMMBySupervisor( String beginTimeStr, String endTimeStr, String key,
			String currPageStr, String pageSizeStr,String username, String orderNum,  String status,String type, ErrorInfo error){
		error.clear();
 		Date beginTime = null;
 		Date endTime = null;
 		
 		int currPage = Constants.ONE;
 		int pageSize = Constants.PAGE_SIZE;
 
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
 		if(RegexUtils.isDate(beginTimeStr)) {
 			beginTime = DateUtil.strDateToStartDate(beginTimeStr);
 			conditionMap.put("beginTime", beginTimeStr);
 		}
 		
 		if(RegexUtils.isDate(endTimeStr)) {
 			endTime = DateUtil.strDateToEndDate(endTimeStr);
 			conditionMap.put("endTime", endTimeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(currPageStr) && !currPageStr.equals("0")) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr) && !pageSizeStr.equals("0")) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.V_MMM_RETURN_DATA);
		
		List<Object> params = new ArrayList<Object>();
		
		if(beginTime != null) {
			sql.append(" and t_return_data.op_time >= ? ");
			params.add(beginTime);
		}
		
		if(endTime != null) {
			sql.append(" and t_return_data.op_time <= ? ");
			params.add(endTime);
		}
		
		if(StringUtils.isNotBlank(username)) {
			conditionMap.put("username", username);
			sql.append(" and t_users.name like ? ");
			params.add("%"+username.trim()+"%");
		}
		if (StringUtils.isNotBlank(orderNum)) {
			conditionMap.put("orderNum", orderNum);
			sql.append(" and t_return_data.orderNum like  ? ");
			params.add("%"+orderNum.trim()+"%");

		}
		if(StringUtils.isNotBlank(status)) {
			conditionMap.put("status", status);
			if(status.trim().equals("成") || status.trim().equals("成功"))
				status = "2";
			else if(status.trim().equals("失败") || status.trim().equals("失"))
				status = "1";
			sql.append(" and t_mmm_data.status like ?  ");
			params.add("%"+status.trim()+"%");
			
		}
		if(StringUtils.isNotBlank(type)) {
			conditionMap.put("type", type);
			sql.append(" and t_mmm_data.msg like ? ");
			params.add("%"+type.trim()+"%");
		}
		if(StringUtils.isNotBlank(key)) {
			conditionMap.put("key", key);
			if(key.trim().equals("成") || key.trim().equals("成功"))
				orderNum = "2";
			else if(key.trim().equals("失败") || key.trim().equals("失"))
				orderNum = "1";
			
			sql.append(" and t_users.name like ? or t_return_data.orderNum like ? or t_mmm_data.status like ? or t_mmm_data.msg like ? ");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			params.add("%"+key+"%");
			
		}
		
		sql.append(" order by t_mmm_data.op_time desc");
		
		List<v_mmm_return_data> withdrawals = new ArrayList<v_mmm_return_data>();
		int count = 0;
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_mmm_return_data.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            withdrawals = query.getResultList();
            count = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询流水号账单时！："+e.getMessage());
			error.code = -1;
			error.msg = "查询流水号账单时！";
			
			return null;
		}
		
		
		
		PageBean<v_mmm_return_data> page = new PageBean<v_mmm_return_data>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = withdrawals;
		
		error.code = 0;
		return page;
	}
	
	/**
	 * 查找回调参数信息
	 * @param orderNum
	 */
	public static String lookRarkInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_return_data data = null;
		long id = Convert.strToLong(orderNum, -1);
		try {
			data = t_return_data.find(" id = ? ", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return data.data;
	}
	
	/**
	 * 查找发送参数信息
	 * @param orderNum
	 */
	public static Map<String, String> lookRarkSendInfo(String orderNum, ErrorInfo error){
		error.clear();
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ? ", orderNum).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		map.put("url", data.url);
		return map;
	}
	
	
	
	
	/**
	 * 根据流水号查找异步回调地址
	 * @param orderNum
	 */
	public static String lookForReturnUrl(String id, ErrorInfo error){
		error.clear();
		t_mmm_data data = null;
		
		try {
			data = t_mmm_data.find(" orderNum = ?", id).first();
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		
		Map<String, String> map = new HashMap<String, String>();
		return data.url;
	}
	
	
	/**
	 * 根据流水号查询回调参数
	 */
	public static Map<String,String> getReturnData(long id,ErrorInfo error){
		error.clear();
		t_return_data data = null;
		
		try {
			data = t_return_data.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		Map<String, String> map = gson.fromJson(data.data, new TypeToken<Map<String, String>>(){}.getType());
		error.code = 0;
		error.msg = "查询流水号账单时信息成功!";
		return map;
	}
	
	/**
	 * 查询视图里面的单条记录
	 * @param id
	 */
	public static v_mmm_return_data findVmmmReturnDate(long id, ErrorInfo error){
		error.clear();
		v_mmm_return_data data = null;
		
		try {
			data = v_mmm_return_data.findById(id);
		} catch (Exception e) {
			error.code = -1;
			error.msg = "查询流水号账单时链接数据库失败";
			
			return null;
		}
		
		if(data == null){
			
			error.code = -1;
			error.msg = "查询流水号账单时传入的流水账单号有误";
			
			return null;
			
		}
		
		
		error.code = 0;
		error.msg = "查询单条流水号账单时信息成功!";

		return data;
	}
	
}
