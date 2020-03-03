package services.ymd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import dao.ymd.SettingDao;
import models.core.t_interest_rate;
import models.core.t_service_cost_rate;
import models.risk.t_risk_manage_big_type;
import models.risk.t_risk_manage_score_option;
import models.risk.t_risk_manage_type;
import play.db.jpa.JPA;
import utils.ErrorInfo;

/**
 * 
 * @author MSI
 *    打分机制
 */
public class SettingService {
	public static List<Map<String, Object>> queryAllRiskList() {
		return SettingDao.queryAllRiskList();
	}
	
	public static List<t_risk_manage_big_type> queryBigTypeList(){
		return t_risk_manage_big_type.find("is_valid = 1 order by sequence").fetch();
	}
	
	public static List<t_risk_manage_type> queryTypeList(){
		return t_risk_manage_type.find(" is_valid = 1 order by sequence ").fetch();
	}
	
	public static void saveRiskScore(Long id, BigDecimal riskScore,ErrorInfo error) {
		try {
			t_risk_manage_score_option option = t_risk_manage_score_option.findById(id);
			option.score = riskScore;
			option.save();
			
			error.code = 1;
			error.msg = "操作成功！";
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			
			error.code = -1;
			error.msg = "风控规则异常！";
		}
		
	}
	
	/**
	 * 保存服务费率
	 * @return
	 * @author Sxy
	 */
	public static int saveServiceCostRate(long orgId, String serviceCostRate, ErrorInfo error){
		
		List<Object> serviceCostRateList = JSON.parseArray(serviceCostRate);
		//System.out.println(serviceCostRateList);
		List<t_service_cost_rate> findServiceCostRateList = t_service_cost_rate.find(" org_id = ? and product_id = 4 ", orgId).fetch();
		
		try {
			if(findServiceCostRateList.size()==0){
				for(int i=0;i<serviceCostRateList.size();i++){
					t_service_cost_rate service_cost_rate = new t_service_cost_rate();
					service_cost_rate.org_id = orgId;
					service_cost_rate.product_id = (long) 4;
					service_cost_rate.service_cost_rule = 1; //服务费计算规则:1.(总服务费=借款金额*服务费率)
					service_cost_rate.service_payment_model = 1; //服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) 
					service_cost_rate.period_unit = 0; //借款期限单位-1: 年;0:月;1:日
					service_cost_rate.period = i+1;
					if(!serviceCostRateList.get(i).equals("")){
						service_cost_rate.service_cost_rate = new BigDecimal((String)serviceCostRateList.get(i));
					}
					service_cost_rate.save();
				}
			}else if (findServiceCostRateList.size()!=0) {
				for(int i=0;i<findServiceCostRateList.size();i++){
					if(!serviceCostRateList.get(i).equals("")){
						findServiceCostRateList.get(i).service_cost_rate = new BigDecimal((String)serviceCostRateList.get(i));
						findServiceCostRateList.get(i).save();
					}else {
						findServiceCostRateList.get(i).service_cost_rate = null;
						findServiceCostRateList.get(i).save();
					}
				}
			}
			
			
			error.code = 1;
			error.msg = "服务费率保存成功！";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			error.msg = "保存服务费率失败！";
		}
		
		
		
		return error.code;
		
	}
	
}
