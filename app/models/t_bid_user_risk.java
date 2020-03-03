package models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import business.BidUserRisk;
import play.db.jpa.Model;

/**
 * 标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-4 下午03:32:02
 */
@Entity
public class t_bid_user_risk extends Model {
	private static final long serialVersionUID = 3389230993480662894L;
	
	@javax.persistence.Access(AccessType.PROPERTY)
	private Long bid_risk_id;
	@javax.persistence.Access(AccessType.PROPERTY)
	private Long user_risk_id;
	@javax.persistence.Access(AccessType.PROPERTY)
	public BigDecimal quota;//限额
	
	@Transient
	public String quotaStr;
	@Transient
	public Integer risk_type;
	
	public void setQuota(BigDecimal quota){
		this.quota=quota;
		if(this.quota.compareTo(new BigDecimal(100000D))>=0){
			this.quotaStr="无限制";
			this.risk_type = 1;
		}else if(this.quota.compareTo(new BigDecimal("0"))==0){
			this.quotaStr="不可投";
			this.risk_type = -1;
		}else{
			this.quotaStr= "<" + quota.intValue();
			risk_type = 0;
		}
	}
	
	@Transient
	public String bid_risk_name;
	@Transient
	public String user_risk_name;
	
	@Transient
	transient public t_bid_risk bid_risk;
	@Transient
	transient public t_user_risk user_risk;
	
	static private Map<Long,t_bid_user_risk> allBidUserRiskMap=null;
	static private List<t_bid_user_risk> allBidUserRiskList=null;
	
	public Long getBid_risk_id() {
		return this.bid_risk_id;
	}

	public void setBid_risk_id(Long bid_risk_id) {
		this.bid_risk=t_bid_user_risk.getAllBidRiskMap().get(bid_risk_id);
		if (this.bid_risk == null) {
			throw new IllegalArgumentException("错误的标的风险等级");
        }
		this.bid_risk_id = bid_risk_id;
		this.bid_risk_name=bid_risk.name;
	}

	public Long getUser_risk_id() {
		return this.user_risk_id;
	}
	
	public void setUser_risk_id(Long user_risk_id) {
		this.user_risk=t_bid_user_risk.getAllUserRiskMap().get(user_risk_id);
		if (this.user_risk == null) {
			throw new IllegalArgumentException("错误的用户风险等级");
        }
		this.user_risk_id = user_risk_id;
		this.user_risk_name=user_risk.name;
	}
	
	static public void init(){
		t_bid_user_risk.allBidUserRiskList=t_bid_user_risk.findAll();
		t_bid_user_risk.allBidUserRiskMap=new HashMap<Long,t_bid_user_risk>();
		for(t_bid_user_risk bidUserRisk:t_bid_user_risk.allBidUserRiskList){
			allBidUserRiskMap.put(bidUserRisk.id,bidUserRisk);

			bidUserRisk.bid_risk.getBidUserRiskList().add(bidUserRisk);
			bidUserRisk.bid_risk.getBidUserRiskMap().put(bidUserRisk.user_risk_id, bidUserRisk);
			
			bidUserRisk.user_risk.getBidUserRiskList().add(bidUserRisk);
			bidUserRisk.user_risk.getBidUserRiskMap().put(bidUserRisk.bid_risk_id,bidUserRisk);
		}
		
	}
	
	static public Map<Long,t_bid_risk> getAllBidRiskMap(){
		return t_bid_risk.getAllBidRiskMap();
	}
	static public List<t_bid_risk> getAllBidRiskList(){
		return t_bid_risk.getAllBidRiskList();
	}
	static public Map<Long,t_user_risk> getAllUserRiskMap(){
		return t_user_risk.getAllUserRiskMap();
	}
	static public List<t_user_risk> getAllUserRiskList(){
		return t_user_risk.getAllUserRiskList();
	}
	static public Map<Long,t_bid_user_risk> getAllBidUserRiskMap(){
		if(t_bid_user_risk.allBidUserRiskMap==null||t_bid_user_risk.allBidUserRiskMap.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_bid_user_risk.allBidUserRiskMap;
	}
	static public List<t_bid_user_risk> getAllBidUserRiskList(){
		if(t_bid_user_risk.allBidUserRiskList==null||t_bid_user_risk.allBidUserRiskList.isEmpty()){
			BidUserRisk.doModelInit();
		}
		return t_bid_user_risk.allBidUserRiskList;
	}

	@Override
	public String toString() {
		return "t_bid_user_risk [bid_risk_id=" + bid_risk_id + ", user_risk_id=" + user_risk_id + ", quota=" + quota
				+ ", id=" + id + "]";
	}

}
