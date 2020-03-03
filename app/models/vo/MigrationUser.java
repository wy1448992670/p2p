package models.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class MigrationUser implements Serializable {

	private Long id;
	private String reality_name;
	private String id_number;
	private String mobile;
	private double balance;
	private Date migration_time;
	/**
	 * 迁移投资及账单
	 */
	private List<MigrationInvestment> investmentList;
	private List<MigrationInvestmentBill> billList;
	/**
	 * 验证字段
	 */
	private Date signTime;
	private String signature;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the reality_name
	 */
	public String getReality_name() {
		return reality_name;
	}

	/**
	 * @param reality_name the reality_name to set
	 */
	public void setReality_name(String reality_name) {
		this.reality_name = reality_name;
	}

	/**
	 * @return the id_number
	 */
	public String getId_number() {
		return id_number;
	}

	/**
	 * @param id_number the id_number to set
	 */
	public void setId_number(String id_number) {
		this.id_number = id_number;
	}

	/**
	 * @return the mobile
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @param balance the balance to set
	 */
	public void setBalance(double balance) {
		this.balance = balance;
	}

	/**
	 * @return the migration_time
	 */
	public Date getMigration_time() {
		return migration_time;
	}

	/**
	 * @param migration_time the migration_time to set
	 */
	public void setMigration_time(Date migration_time) {
		this.migration_time = migration_time;
	}

	/**
	 * @return the investmentList
	 */
	public List<MigrationInvestment> getInvestmentList() {
		return investmentList;
	}

	/**
	 * @param investmentList the investmentList to set
	 */
	public void setInvestmentList(List<MigrationInvestment> investmentList) {
		this.investmentList = investmentList;
	}

	/**
	 * @return the billList
	 */
	public List<MigrationInvestmentBill> getBillList() {
		return billList;
	}

	/**
	 * @param billList the billList to set
	 */
	public void setBillList(List<MigrationInvestmentBill> billList) {
		this.billList = billList;
	}

	/**
	 * @return the now
	 */
	public Date getSignTime() {
		return signTime;
	}

	/**
	 * @param now the now to set
	 */
	public void setSignTime(Date now) {
		this.signTime = now;
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}



	public void sign() {
		String slat = "doyouwanttobuildasnowman";
		this.signTime=new Date();
		this.signature=null;
		String content=new Gson().toJson(this);
		System.out.println(content.length()+" content:"+content);
		try {
			content+=slat;
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 对字符串进行加密
			md.update(content.getBytes());
			byte s[] = md.digest();
			// 获得加密后的数据
			String md5Str = "";
			for (int i = 0; i < s.length; i++) {
				md5Str += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
			}
			this.signature = md5Str;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("没有md5这个算法！");
		}

	}

	public boolean verify() throws Exception{
		String slat = "doyouwanttobuildasnowman";
		if((this.signTime.getTime()+15*60*1000)<new Date().getTime() || (this.signTime.getTime()-5*60*1000)>new Date().getTime()) {
			throw new Exception("迁移用户数据超时,请使用15分钟内生成的用户数据导入");
		}
		String theSignature=this.signature;
		this.signature=null;
		String content=new Gson().toJson(this);
		this.signature=theSignature;
		System.out.println(content);
		try {
			content+=slat;
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 对字符串进行加密
			md.update(content.getBytes());
			byte s[] = md.digest();
			// 获得加密后的数据
			String md5Str = "";
			for (int i = 0; i < s.length; i++) {
				md5Str += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
			}
			if(md5Str.equals(this.signature)) {
				return true;
			}else {
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("没有md5这个算法！");
		}
	}

}