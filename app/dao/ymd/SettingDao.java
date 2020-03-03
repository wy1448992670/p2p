package dao.ymd;

import java.util.List;
import java.util.Map;

import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;

public class SettingDao {
	public static List<Map<String, Object>> queryAllRiskList() {
		String hql = "select score_option.id as id,"
					 + " 	type.id as typeId, " 
					 + " 	type.name as typeName, "
					 + " 	type.compare_type as compareType, "
					 + " 	type.unit as unit, "
					 + " 	big_type.id as bigTypeId,"
					 + " 	big_type.name as bigName,"
					 + " 	big_type.total_score as bigTypeScore, "
					 + " 	score_option.score as score, "
					 + " 	score_option.min_value as min_value, "
					 + " 	score_option.max_value as max_value, "
					 + " 	score_option.show_str as show_str  "
					 + " FROM t_risk_manage_score_option score_option "  
					 + "	 INNER JOIN t_risk_manage_type type ON score_option.type_id = type.id and type.is_valid = 1 "
					 + " 	INNER JOIN t_risk_manage_big_type big_type ON big_type.id = type.big_type_id and big_type.is_valid = 1 ";
	    Logger.info("hql:" + hql);
		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), hql);	 
		return list;
	}
	
	
}
