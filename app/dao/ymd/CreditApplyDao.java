package dao.ymd;

import java.util.List;
import java.util.Map;

import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;

public class CreditApplyDao {
	public static List<Map<String, Object>> getApplyOrgItems(ErrorInfo error,Long applyId, Long userId){
		// 查询手术机构信息
		String hql = "select org.id, "
				+ "	project.name as projectName, "
				+ "	project.id as projectId, "
				+ "	IFNULL(userInfo.brand_name, user.reality_name) as orgName, "
				+ "ifnull(file.real_path,'') as facePhoto"
				+ "	from t_organization org "
				+ "	left join t_org_project project on org.id = project.org_id "
				+ "	left join t_apply_org_project applyOrg on applyOrg.apply_type=1 and applyOrg.org_project_id = project.id "
				+ "	left join t_users user on user.id = org.user_id "
				+ "	left join t_users_info userInfo on user.id = userInfo.user_id "
				+ " left join (select file_relation.relation_id,min(file.real_path) real_path "
				+ "				from t_file_relation file_relation "
				+ " 			left join t_file file on file.id = file_relation.file_id "
				+ "				where file_relation.relation_type = 8 and file.file_dict_id = 14 "
				+ "				group by file_relation.relation_id )file on file.relation_id = org.id "
				+ " where applyOrg.apply_id = ?  ";
		Logger.info("亿美贷申请下单查询手术信息sql: [%s] ", hql);
		List<Map<String, Object>> list = JPAUtil.getList(error, hql, applyId);
		return list;
	}
	public static void main(String[] args) {
		String hql = "select org.id, "
				+ "	project.name as projectName, "
				+ "	project.id as projectId, "
				+ "	IFNULL(userInfo.brand_name, user.reality_name) as orgName, "
				+ "ifnull(file.real_path,'') as facePhoto"
				+ "	from t_organization org "
				+ "	left join t_org_project project on org.id = project.org_id "
				+ "	left join t_apply_org_project applyOrg on applyOrg.org_project_id = project.id "
				+ "	left join t_users user on user.id = org.user_id "
				+ "	left join t_users_info userInfo on user.id = userInfo.user_id "
				+ " left join (select file_relation.relation_id,min(file.real_path) real_path "
				+ "				from t_file_relation file_relation "
				+ " 			left join t_file file on file.id = file_relation.file_id "
				+ "				where file_relation.relation_type = 8 and file.file_dict_id = 14 "
				+ "				group by file_relation.relation_id )file on file.relation_id = org.id "
				+ " where applyOrg.apply_id = ?  ";
		System.out.println(hql);
	}
}
