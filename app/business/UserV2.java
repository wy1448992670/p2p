package business;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;

import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;

public class UserV2 {

	/**
	 * 
	 * @author liulj
	 * @creationDate. Jul 25, 2018 2:35:22 PM 
	 * @description.  查询cps黑名单用户
	 * 
	 * @param keyword
	 * @param pageSize
	 * @param currPage
	 * @return
	 */
	public static PageBean<Map<String, Object>> findUser4CPSBlacklist(String keyword, int pageSize, int currPage){
	    	
	    	List<Object> params = new ArrayList<Object>();
	    	
	    	Map<String,Object> conditionMap = new HashMap<String, Object>();
	    	conditionMap.put("keyword", keyword);
	    	
	    	String sqlSelect = "SELECT id, name,reality_name,mobile,cps_blacklist_dt";
	    	
	    	StringBuffer sqlFrom = new StringBuffer(" FROM t_users WHERE is_cps_blacklist = 1");
	    	if(StringUtils.isNotBlank(keyword)) {
	    		sqlFrom.append(" AND (INSTR(name, ?) > 0 OR INSTR(reality_name, ?) > 0  OR INSTR(mobile, ?) > 0)");
	    		params.add(keyword);
	    		params.add(keyword);
	    		params.add(keyword);
	    	}
	    	
	    	String listSql = sqlSelect.concat(sqlFrom.toString()).concat(" LIMIT ?,?");
	    	String cntSql = "SELECT count(*) as count".concat(sqlFrom.toString());
	    	
	    	int count = 0;
	    	List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql, params.toArray());
	    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
	    		count = ((BigInteger)countMap.get(0).get("count")).intValue();
	    	}
	    	
	    	params.add((currPage - 1) * pageSize);
	    	params.add(pageSize);
	    	List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, params.toArray());
	    	
	    	PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
	    	page.pageSize = pageSize;
	    	page.currPage = currPage;
	    	page.totalCount = count;
	    	page.conditions = conditionMap;
	    	
	    	page.page = list;
	    	
	    	return page;
 	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jul 25, 2018 2:39:04 PM 
	 * @description.  修改用户cps黑名单状态p
	 * 
	 * @param userId
	 * @param is_blacklist
	 */
	public static int updateUserBlacklist(long userId, int is_blacklist) {
		String sql = "UPDATE t_users SET is_cps_blacklist = ?, cps_blacklist_dt = ? WHERE id = ? and (is_cps_blacklist = ? or is_cps_blacklist is null)";
		return JPAUtil.executeUpdate(new ErrorInfo(), sql, is_blacklist, new Date(), userId, is_blacklist == 0 ? 1 : 0);
	}
	
	/**
	 * 修改用户短信黑名单状态
	 * @param userId
	 * @param is_blacklist
	 * @return
	 */
	public static int updateUserSmsBlacklist(long userId, int is_blacklist) {
		String sql = "UPDATE t_users SET is_sms_blacklist = ?, sms_blacklist_dt = ? WHERE id = ? and (is_sms_blacklist = ? or is_sms_blacklist is null)";
		return JPAUtil.executeUpdate(new ErrorInfo(), sql, is_blacklist, new Date(), userId, is_blacklist == 0 ? 1 : 0);
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jul 25, 2018 3:21:57 PM 
	 * @description.  查询用户黑名单状态
	 * 
	 * @param userId
	 * @return
	 */
	public static JSONObject findUserBlacklist(long userId) {
		String sql = "select is_cps_blacklist, cps_blacklist_dt from t_users where id = ?";
		return new JSONObject(JPAUtil.getMap(new ErrorInfo(), sql, userId));
	}
}
