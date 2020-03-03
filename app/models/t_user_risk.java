package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import business.BidUserRisk;
import play.db.jpa.Model;

@Entity
public class t_user_risk extends Model {
	private static final long serialVersionUID = 3359684608044855599L;
	public String name;
	public String description;
	
	@Transient
	private Map<Long,t_bid_user_risk> bidUserRiskMap=new HashMap<Long,t_bid_user_risk>();
	@Transient
	private List<t_bid_user_risk> bidUserRiskList=new ArrayList<t_bid_user_risk>();
	
	static private Map<Long,t_user_risk> allUserRiskMap=null;
	static private List<t_user_risk> allUserRiskList=null;
	
	static public void init(){
		t_user_risk.allUserRiskList =  t_user_risk.findAll();
		t_user_risk.allUserRiskMap=new HashMap<Long,t_user_risk>();
		for (t_user_risk userRisk : t_user_risk.allUserRiskList) {
			allUserRiskMap.put(userRisk.id, userRisk);
		}
	}
	
	static public Map<Long,t_user_risk> getAllUserRiskMap(){
		if(t_user_risk.allUserRiskMap==null||t_user_risk.allUserRiskMap.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_user_risk.allUserRiskMap;
	}
	
	static public List<t_user_risk> getAllUserRiskList(){
		if(t_user_risk.allUserRiskList==null||t_user_risk.allUserRiskList.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_user_risk.allUserRiskList;
	}
	
	public Map<Long, t_bid_user_risk> getBidUserRiskMap() {
		return bidUserRiskMap;
	}
	
	public List<t_bid_user_risk> getBidUserRiskList() {
		return bidUserRiskList;
	}
}