package models;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.Model;
import utils.DataUtil;
@Entity
public class v_user_bill extends Model{
	public String userName;
	public String account;
	public double systemBlance;
	public double systemFreeze;
	public double pBlance;
	public double pFreeze;
	//状态，0正常，1异常
	public String status;
	
	public String getStatus(){
		if (compareBlance() && compareFreeze()){
			this.status = "0";
			
		}
		else{
			this.status ="1";
		}
		return this.status;
	}
	
	public boolean compareBlance(){
		if (Double.compare(this.systemBlance, this.pBlance) == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean compareFreeze(){
		if (Double.compare(this.systemFreeze, this.pFreeze) == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
}
