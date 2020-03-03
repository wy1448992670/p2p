package business;

import java.util.Date;
import java.util.List;

import models.t_enum_map;
import models.t_log;
import org.apache.commons.lang.StringUtils;

public class LogCore {
	/**
	 * 
	 * @param relation_type 外键类型:1.t_bids 2.t_borrow_apply
	 * @param relation_id 	外键id
	 * @param user_type		操作用户类型:1.t_users 2.t_supervisors,job存2
	 * @param user_id		操作用户id:job存1
	 * @param status		状态id
	 * @param time			变更时间:为null时new Date()
	 * @param description_title	描述标题:为null时""
	 * @param description	描述:为null时""
	 */
	public static t_log create(Integer relation_type,Long relation_id,Integer user_type,Long user_id,
			Integer status,Date time,String description_title,String description) {
		
		return create(relation_type,relation_id,user_type,user_id,status,time,"",description_title,description);
	}


	/**
	 *
	 * @param relation_type 外键类型:1.t_bids 2.t_borrow_apply
	 * @param relation_id 	外键id
	 * @param user_type		操作用户类型:1.t_users 2.t_supervisors,job存2
	 * @param user_id		操作用户id:job存1
	 * @param status		状态id
	 * @param time			变更时间:为null时new Date()
	 * @param result		操作结果
	 * @param description_title	描述标题:为null时""
	 * @param description	描述:为null时""
	 */
	public static t_log create(Integer relation_type,Long relation_id,Integer user_type,Long user_id,
							   Integer status,Date time,String result, String description_title,String description) {

		t_log log = new t_log();
		log.relation_type=relation_type;
		log.relation_id=relation_id;
		log.user_type=user_type;
		log.user_id=user_id;
		log.status=status;

		if(StringUtils.isNotEmpty(result)) {
			log.result = result;
		}

		log.time=(time==null?new Date():time);
		log.description_title=(description_title==null?"":description_title);
		log.description=(description==null?"":description);

		log.save();

		return log;
	}
	
	public static t_log create(String relation_type,Long relation_id,Integer user_type,Long user_id,
			Integer status,Date time,String description_title,String description) {
		return LogCore.create(LogCore.getRelationType(relation_type), relation_id, user_type, user_id, status, time, description_title, description);
	}
	
	public static List<t_log> getLog(Integer relation_type,Long relation_id){
		/*
		return  t_log.find("select t_log,'abc' as user_name "
		+ " from t_log as t_log where t_log.relation_type=? and t_log.relation_id=? order by t_log.time asc ", relation_type,relation_id).fetch();
		*/
		
		return  t_log.find(" select new t_log(t_log "
				+ " ,(select t_users.reality_name from t_users as t_users "
				+ "		where t_log.user_type=1 "
				+ "		And t_users.id=t_log.user_id) "
				+ " ,(select t_supervisors.reality_name from t_supervisors as t_supervisors "
				+ "			where t_log.user_type=2 and t_supervisors.id=t_log.user_id) "
				+ " ) "
				+ " from t_log as t_log "
				+ " where t_log.relation_type=? and t_log.relation_id=? order by t_log.time asc ", relation_type,relation_id).fetch();
		
		/*
		return  t_log.find(" select new t_log(t_log.id "
				+ " ,t_log.relation_type "
				+ " ,t_log.relation_id "
				+ " ,t_log.user_type "
				+ " ,t_log.user_id"
				+ " ,t_log.status"
				+ ",t_log.time,t_log.description_title,t_log.description "
				+ " ,nullif("
				+ "		(select t_users.reality_name from t_users as t_users " + 
				"		where t_log.user_type=1 " + 
				"		And t_users.id=t_log.user_id) " + 
				"		,nullif("
				+ "			(select t_supervisors.reality_name from t_supervisors as t_supervisors " + 
				"			where t_log.user_type=2 and t_supervisors.id=t_log.user_id) " + 
				"			,'SYSTEM'))"
				+ " ) "
				+ " from t_log as t_log "
				+ " where t_log.relation_type=? and t_log.relation_id=? order by t_log.time asc ", relation_type,relation_id).fetch();
		*/

	}
	
	public static List<t_log> getLog(String relation_type,Long relation_id){
		return  LogCore.getLog(LogCore.getRelationType(relation_type),relation_id);
	}
	
	public static Integer getRelationType(String relation_type){
		t_enum_map enum_type_enum=t_enum_map.getEnumNameMapByTypeName("t_enum_map.enum_type").get(relation_type);
		t_enum_map relation_type_enum=t_enum_map.getEnumNameMapByTypeName("t_log.relation_type").get(enum_type_enum.enum_code+"");
		return  relation_type_enum.enum_code;
	}
	
	public static String getRelationType(Integer relation_type){
		t_enum_map relation_type_enum=t_enum_map.getEnumCodeMapByTypeName("t_log.relation_type").get(relation_type);
		t_enum_map enum_type_enum=t_enum_map.getEnumCodeMapByTypeName("t_enum_map.enum_type").get(Integer.parseInt(relation_type_enum.enum_name));
		return  enum_type_enum.enum_name;
	}
	
}
