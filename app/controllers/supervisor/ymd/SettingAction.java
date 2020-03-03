package controllers.supervisor.ymd;

import java.math.BigDecimal;import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import business.BackstageSet;
import controllers.supervisor.SupervisorController;
import models.core.t_service_cost_rate;
import models.risk.t_risk_manage_big_type;
import models.risk.t_risk_manage_score_option;
import models.risk.t_risk_manage_type;
import play.mvc.Scope.Params;
import services.ymd.SettingService;
import utils.ErrorInfo;

public class SettingAction extends SupervisorController {
	
	/**
	 * 审核中的借款标列表
	 */
	public static void setRisk() {
		
		//获取全部风险评测类别
		List<Map<String, Object>> riskList = SettingService.queryAllRiskList();
		//获取大类ID列表
		List<t_risk_manage_big_type> bigTypeList = SettingService.queryBigTypeList();
		//获取小类ID列表
		List<t_risk_manage_type> typeList = SettingService.queryTypeList();
		
		render(riskList, bigTypeList, typeList);
	}
	/**
	 * 亿美贷审核基本信息设置
	 */
	public static void basicallyInfo() {
		
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		List<t_service_cost_rate> serviceCostRateList = t_service_cost_rate.find(" org_id = ? and product_id = 4 ", (long)-1).fetch();
		
		render(backstageSet,serviceCostRateList);
	}
	
	/**
	 *  保存风控规则设置
	 *  
	 */
	public static void saveRiskScore(Long id, Double riskScore) {
		ErrorInfo error = new ErrorInfo();
		System.err.println(id + "---" + riskScore);
		BigDecimal score = BigDecimal.ZERO ;
		if(riskScore != null) {
			score = new BigDecimal(riskScore);
		}
		SettingService.saveRiskScore(id, score, error);
	}
	
	
	/**
	 * 风控规则表单提交
	 */
	public static void saveAllRiskScore(String formValue) {
		ErrorInfo error = new ErrorInfo();
		try {
			JSONArray riskList = JSONArray.parseArray(formValue);
			for (int i = 0; i < riskList.size(); i++) {
				JSONObject risk =(JSONObject) riskList.get(i);
				t_risk_manage_score_option option = t_risk_manage_score_option.findById(Long.valueOf(risk.getString("riskId")));
				option.score = new BigDecimal(risk.getString("value"));
				option.save();
			}
		} catch (Exception e) {
			error.code = -1;
			error.msg = "操作失败！";
		}
		
		error.code = 1;
		error.msg = "操作成功！";
		renderJSON(error);
	}
	
	/**
	 * 保存月工资系数
	 * @param salaryCoefficient
	 * @author Sxy
	 */
	public static void saveSalaryCoefficient(double salaryCoefficient){
		
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		BackstageSet backstageSet = new BackstageSet();
		backstageSet.salary_coefficient = salaryCoefficient;
		
		backstageSet.salaryCoefficientSet(error);
		
		json.put("error", error.code);
		json.put("msg", error.msg);
		
		renderJSON(json);
	}
	
	/**
	 * 添加服务费率
	 * 
	 * @author Sxy
	 */
	public static void createServiceCostRate(){
		
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		String serviceCostRate = params.get("serviceCostRate");
		
		SettingService.saveServiceCostRate(-1, serviceCostRate, error); //-1为orgId,代表所有机构默认的服务费率
		
		json.put("error", error.code);
		json.put("msg", error.msg);
		
		renderJSON(json);
	}
	
}
