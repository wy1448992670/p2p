package jobs;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import business.BackstageSet;
import models.t_users;
import models.t_users_info;
import models.core.t_credit_apply;
import play.Logger;
import play.jobs.Every;
import services.business.CreditApplyService;
import utils.ErrorInfo;
import utils.JPAUtil;

@Every("10min")
public class MachineScoreJob extends BaseJob {
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		Logger.info("===================机审开始=========================");
		ErrorInfo error = new ErrorInfo();
		List<t_credit_apply> applyList = t_credit_apply.find(" status = 0").fetch();
		if(applyList == null || applyList.size() == 0) {
			Logger.info("======> 暂无机审数据！");
		}
		for (t_credit_apply apply : applyList) {
			JPAUtil.transactionBegin();
			Logger.info("======> 额度申请id 【%s】  ", apply.id);
			Long userId = apply.user_id;
			t_users user = t_users.findById(userId);
			if(StringUtils.isEmpty(user.reality_name)) {
				return;
			}
			t_users_info userInfo = t_users_info.find("user_id= ?", userId).first();
			if(userInfo == null) {
				Logger.info("【%s】请先完善基础信息", user.id);
				return;
			}
//			BigDecimal totalScore = BigDecimal.ZERO;
			BigDecimal machineCreditAmount = BigDecimal.ZERO;
			try {
				//调用第三方, 与平台打分机制匹配打分
				CreditApplyService.getUserCreditInfo(user.reality_name, user.id_number, user.mobile, user.address, apply);
				if(apply.status == 1) {
					int workIndustry = userInfo.work_industry;
					
					if(workIndustry == 0) { //自由职业者
						machineCreditAmount = apply.machine_score.divide(new BigDecimal(100), 2).multiply(apply.items_amount)  ; //手术费*综合评分/100
						
					} else {
						//（月工资*90%-3000元-房租）* 贷款月份数 
						//月工资=(公积金汇缴数额/14%)*L
						//L为区间：1-3，默认为1.6
						
						Double  salary_coefficient = BackstageSet.getCurrentBackstageSet().salary_coefficient;
						BigDecimal salary = userInfo.accumulation_fund.divide(new BigDecimal(0.14), 2).multiply(new BigDecimal(salary_coefficient));
						machineCreditAmount = salary.multiply(new BigDecimal(0.9)).subtract(new BigDecimal(3000)).subtract(userInfo.rent).multiply(new BigDecimal(apply.apply_period));
						if (machineCreditAmount.compareTo(BigDecimal.ZERO) == -1) {
							machineCreditAmount = BigDecimal.ZERO;
						}
					}
					// 机审打分
					apply.machine_credit_amount = machineCreditAmount;
				}
				apply.save();
				JPAUtil.transactionCommit();
			} catch (Exception e) {
				error.code = -3;
				error.msg = "机审失败！";
				e.printStackTrace();
				try {
					apply.status = 0;
					apply.interface_risk_status = 0;
					apply.save();
					JPAUtil.transactionCommit();
				}catch(Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		Logger.info("===================机审结束=========================");
		
	}
}
