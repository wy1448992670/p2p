package business;

import java.math.BigDecimal;
import java.util.Date;

import models.t_bid_risk;
import models.t_bid_user_risk;
import models.t_bid_user_risk_log;
import models.t_user_risk;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import play.Logger;
import utils.ErrorInfo;	

public class BidUserRisk {
	
	public static void bidUserRiskUpdate(Long id,BigDecimal quota){
		t_bid_user_risk bidUserRisk=t_bid_user_risk.getAllBidUserRiskMap().get(id);
		if(bidUserRisk.quota.compareTo(quota)!=0){
			bidUserRisk.quota=quota;
			bidUserRisk=bidUserRisk.merge();
			bidUserRisk.save();
			
			t_bid_user_risk_log bidUserRiskLog=new t_bid_user_risk_log();
			bidUserRiskLog.bid_risk_id=bidUserRisk.bid_risk.id;
			bidUserRiskLog.user_risk_id=bidUserRisk.user_risk.id;
			bidUserRiskLog.quota=bidUserRisk.quota;
			bidUserRiskLog.supervisor_id=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
			bidUserRiskLog.mtime=new Date();
			bidUserRiskLog.save();
		}
	}
	
	/**
	 * 标的用户风险等级初始化
	 */
	public static void doModelInit(){
		t_bid_risk.init();
		t_user_risk.init();
		t_bid_user_risk.init();
	}
	
	/**
	 * 通过标的风险等级id,用户风险等级id,查询标的用户风险等级
	 * @param bidRiskId
	 * @param userRiskId
	 * @return
	 */
	public static t_bid_user_risk getRiskByBidAndUser(Long bidRiskId,Long userRiskId){
		return t_bid_user_risk.getAllBidRiskMap().get(bidRiskId).getBidUserRiskMap().get(userRiskId);
	}
	
	
	public static String bidUserRisk(ErrorInfo error){
		String bidUserRisk = "";
		try {
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setExcludes(new String[]{"bidUserRiskMap","allBidRiskMap","allBidRiskList","bid_risk","user_risk"}); 
			jsonConfig.setIgnoreDefaultExcludes(false);
			jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
		 
			JSONArray jsonArray = JSONArray.fromObject(t_bid_risk.getAllBidRiskList(), jsonConfig);
			bidUserRisk = jsonArray.toString();
			Logger.info("bidUserRisk: " + bidUserRisk);
			
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -3;
			error.msg = "获取标的风险评估出错！" ;
		}
		return bidUserRisk;
	}
}
