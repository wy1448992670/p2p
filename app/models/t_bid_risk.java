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
public class t_bid_risk extends Model{
	private static final long serialVersionUID = -236698519981327225L;
	public String name;
	public String description;
	
	@Transient
	private Map<Long,t_bid_user_risk> bidUserRiskMap=new HashMap<Long,t_bid_user_risk>();
	@Transient
	private List<t_bid_user_risk> bidUserRiskList=new ArrayList<t_bid_user_risk>();
	
	static private Map<Long,t_bid_risk> allBidRiskMap=null;
	static private List<t_bid_risk> allBidRiskList=null;
	
	static public void init(){
		t_bid_risk.allBidRiskList=t_bid_risk.findAll();
		t_bid_risk.allBidRiskMap=new HashMap<Long,t_bid_risk>();
		for(t_bid_risk bidRisk:t_bid_risk.allBidRiskList){
			allBidRiskMap.put(bidRisk.id,bidRisk);
		}
	}
	
	static public Map<Long,t_bid_risk> getAllBidRiskMap(){
		if(t_bid_risk.allBidRiskMap==null||t_bid_risk.allBidRiskMap.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_bid_risk.allBidRiskMap;
	}
	
	static public List<t_bid_risk> getAllBidRiskList(){
		if(t_bid_risk.allBidRiskList==null||t_bid_risk.allBidRiskList.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_bid_risk.allBidRiskList;
	}
	
	public Map<Long, t_bid_user_risk> getBidUserRiskMap() {
		return bidUserRiskMap;
	}
	
	public List<t_bid_user_risk> getBidUserRiskList() {
		return bidUserRiskList;
	}
	
}