package dao.ymd;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import models.t_users;
import models.t_users_info;
import models.core.t_person;
import net.sf.json.JSONObject;
import utils.ErrorInfo;

/**
 *  亿美贷借款人信息
 * @author MSI
 *
 */
public class BorrowerInfoDao {
	public static void saveUserInfo(Map<String, String> params, Long userId,ErrorInfo error) {
		error.clear();
		//用戶信息
		t_users user = t_users.findById(userId);
		//用戶工作基础信息
		t_users_info userInfo = t_users_info.find(" user_id = ? ", userId).first();
		if(userInfo == null) {
			userInfo = new t_users_info();
		}
		
		if(Integer.parseInt(params.get("workIndustry")) == 1) { //选择其他行业则显示：工作城市；公司全称；月工资；公积金汇缴数额
			user.company_city_id = Integer.parseInt(params.get("companyCityId"));//公司城市Id, t_dict_city
			user.company = params.get("companyName");
			
			userInfo.salary = new BigDecimal(params.get("salary"));
			userInfo.accumulation_fund = new BigDecimal(params.get("accumulationFund"));
		}
		userInfo.work_industry = Integer.parseInt(params.get("workIndustry"));
		user.education_id = Integer.parseInt(params.get("educationId"));
		user.house_id = Integer.parseInt(params.get("houseId"));
		user.marital_id = Integer.parseInt(params.get("maritalId"));
		user.car_id = Integer.parseInt(params.get("carId"));
		
		user.city_id = Integer.parseInt(params.get("cityId"));
		user.address = params.get("address");
		
		user.save();
		
		//用户联系人信息
		t_person person1 = new t_person();
		person1.name = t_users.userName4ByteToStar(params.get("realName1"));
		person1.phone = params.get("mobile1");
		person1.save();
		
		t_person person2 = new t_person();
		person2.name = t_users.userName4ByteToStar(params.get("realName2"));
		person2.phone = params.get("mobile2");
		person2.save();
		
		if(user.house_id == 10) {//租房
			userInfo.rent = new BigDecimal(params.get("rent"));
		}else {
			userInfo.rent = BigDecimal.ZERO;
		}
		userInfo.QQ =  params.get("QQ");
		userInfo.first_contacts_id =  person1.id;
		userInfo.first_contacts_relation = Integer.parseInt(params.get("relationId1")); 
		userInfo.second_contacts_id =  person2.id;
		userInfo.second_contacts_relation = Integer.parseInt(params.get("relationId2")); 
		userInfo.save();
		
		error.code = 1;
		error.msg = "保存成功";
	}
}
