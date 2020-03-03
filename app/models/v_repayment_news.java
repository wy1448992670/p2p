package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;
@Entity
public class v_repayment_news extends Model{
	public Date receive_time;
	
	@Transient
	public List<v_repayment_news_info> t = new ArrayList<v_repayment_news_info>();
	
	
	public int counts;
	
	public double money;
}
