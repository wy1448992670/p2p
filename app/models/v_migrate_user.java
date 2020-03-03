package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_migrate_user extends Model{
	private static final long serialVersionUID = 1L;
	public String name;
	public String realityName;
	public String idNumber;
	public String mobile;
	public Date migrationTime; 
	public Double receiveInterest;
	public Double receiveCorpus;
	public Double afterMigrationRepay;
	public Integer noRepayBillCount;
	public Integer repayedBillCount;
	public Integer investCount;
	public Double migrationAmount;
}
