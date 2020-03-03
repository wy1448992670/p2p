package utils;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.shove.Convert;

/**
 * 查询工具
 * @author Administrator
 *
 */
public class QueryUtil {
	

	/**
	 * 获取当前查询的总记录数
	 * @param em
	 * @return
	 */
	public static int  getQueryCount(EntityManager em){
		int count = 0;
		Query queryCount = em.createNativeQuery("select FOUND_ROWS() as result");
		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
		return count;
	}
	
	public static int getQueryCountByCondition(EntityManager em, String sql, List<Object> params){
		int count = 0;
		int start = sql.lastIndexOf(" from ");
		if(sql.contains(") t1")){
			start = sql.indexOf(" from ");
		}
		
		int len = sql.lastIndexOf(" where ");
		int end = sql.lastIndexOf(" order by ");
		if(len > end){
			end = sql.length();
		}
		
		if(sql.contains(" group by ")){
			sql = " from (" + sql +") t1";
		}else{
			sql = sql.substring(start, end);
		}
		
		Query queryCount = em.createNativeQuery("select count(1) as result " + sql);
		for(int n = 1; n <= params.size(); n++){
			queryCount.setParameter(n, params.get(n-1));
        }
		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
		return count;
	}

	public static int getQueryCountByCondition2(EntityManager em, String sql, List<Object> params){
		int count = 0;


		int start = sql.indexOf(" from ");

		if(sql.contains(") t1")){
			start = sql.indexOf(" from ");
		}

		int len = sql.lastIndexOf(" where ");
		int end = sql.lastIndexOf(" order by ");
		if(len > end){
			end = sql.length();
		}

		if(sql.contains(" group by ")){
			sql = " from (" + sql +") t1";
		}else{
			sql = sql.substring(start, end);
		}

		Query queryCount = em.createNativeQuery("select count(1) as result " + sql);
		for(int n = 1; n <= params.size(); n++){
			queryCount.setParameter(n, params.get(n-1));
		}
		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
		return count;
	}
	
	//去掉重复行（id）
	public static int getQueryCount1ByCondition(EntityManager em, String sql, List<Object> params){
		int count = 0;
		int start = sql.lastIndexOf(" from ");
		if(sql.contains(") t1")){
			start = sql.indexOf(" from ");
		}
		
		int len = sql.lastIndexOf(" where ");
		int end = sql.lastIndexOf(" order by ");
		if(len > end){
			end = sql.length();
		}
		
		if(sql.contains(" group by ")){
			sql = " from (" + sql +") t1";
		}else{
			sql = sql.substring(start, end);
		}
		
		Query queryCount = em.createNativeQuery("select count(DISTINCT(id)) as result " + sql);
		for(int n = 1; n <= params.size(); n++){
			queryCount.setParameter(n, params.get(n-1));
        }
		count = Convert.strToInt(queryCount.getResultList().get(0)+"",0);
		return count;
	}
}
