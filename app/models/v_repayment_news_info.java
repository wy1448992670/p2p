package models;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class v_repayment_news_info extends Model{
	public double money;
	public String title;
	public int mPeriodes;
	public int periods;
	
}
