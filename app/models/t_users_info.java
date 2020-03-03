package models;

import java.math.BigDecimal;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.core.t_person;
import play.db.jpa.Model;

@Entity
public class t_users_info extends Model implements Cloneable{
	public long user_id;
	public String legal_person;//2:法人代表|3:经营者
	public String business_scope;//1:工作性质|2,3:经营范围
	public String income_debt_info;//收入及负债情况
	public String asset_info;//资产情况
	public String other_finance_info;//其他平台融资情况
	public String other_info;//其他信息
	
	@Access(AccessType.PROPERTY)
	public Long industry_id;//所属行业id
	@Transient
	public String industry_name;//所属行业名称
	@Transient
	public t_industries industry;//所属行业
	
	public BigDecimal reg_capital;//注册资金

	//工作性质:0自由职业者;1全职
	@Access(AccessType.PROPERTY)
	public Integer work_industry;
	@Transient
	public String work_industry_name;
	public void setWork_industry(Integer work_industry) {
		this.work_industry=work_industry;
		if(work_industry==null) {
			this.work_industry_name="";
		}else if(work_industry==1) {
			this.work_industry_name="全职";
		}else if(work_industry==0) {
			this.work_industry_name="自由职业者";
		}
	}
	
	//月工资
	public BigDecimal salary;
	//公积金汇缴数额
	public BigDecimal accumulation_fund;
	//房租
	public BigDecimal rent;
	//QQ|微信号
	public String QQ;
	//品牌名称/企业简称
	public String brand_name;
	
	//常用联系人1 t_person.id
	public Long first_contacts_id;
	@Transient
	public t_person first_contacts;
	
	//常用联系人1和用户的关系 t_dict_relation
	@Access(AccessType.PROPERTY)
	public Integer first_contacts_relation;
	@Transient
	public t_dict_relation first_contacts_relation_obj;
	
	//常用联系人2 t_person.id
	public Long second_contacts_id;
	@Transient
	public t_person second_contacts;
	
	//常用联系人2和用户的关系 t_dict_relation
	@Access(AccessType.PROPERTY)
	public Integer second_contacts_relation;
	@Transient
	public t_dict_relation second_contacts_relation_obj;
	
	//法人t_person.id
	public Long legal_person_id;
	@Transient
	public t_person legal;
	
	public Long getIndustry_id() {
		return industry_id;
	}

	public void setIndustry_id(Long industry_id) {
		if(industry_id==null){
			this.industry_id=null;
			this.industry_name = null;
			return;
		}
		this.industry = new t_industries().getEnumById(industry_id);
		if (this.industry == null) {
			throw new IllegalArgumentException("错误的行业ID！");
        }
		this.industry_id = industry_id;
		this.industry_name = industry.name;
	}

	public void setFirst_contacts_relation(Integer contacts_relation_id) {
		if(contacts_relation_id==null){
			this.first_contacts_relation=null;
			this.first_contacts_relation_obj = null;
			return;
		}
		this.first_contacts_relation_obj = new t_dict_relation().getEnumById(contacts_relation_id);
		if (this.first_contacts_relation_obj == null) {
			throw new IllegalArgumentException("错误的联系人1关系！");
        }
		this.first_contacts_relation = contacts_relation_id;
	}

	public void setSecond_contacts_relation(Integer contacts_relation_id) {
		if(contacts_relation_id==null){
			this.second_contacts_relation=null;
			this.second_contacts_relation_obj = null;
			return;
		}
		this.second_contacts_relation_obj = new t_dict_relation().getEnumById(contacts_relation_id);
		if (this.second_contacts_relation_obj == null) {
			throw new IllegalArgumentException("错误的联系人2关系！");
        }
		this.second_contacts_relation = contacts_relation_id;
	}

	public t_person getFirst_contacts() {
		if(this.first_contacts_id==null){
			return null;
		}
		if(first_contacts!=null && this.first_contacts_id==this.first_contacts.id) {
			return first_contacts;
		}
		this.first_contacts=t_person.findById(first_contacts_id);
		return this.first_contacts;
	}

	public t_person getSecond_contacts() {
		if(this.second_contacts_id==null){
			return null;
		}
		if(second_contacts!=null && this.second_contacts_id==this.second_contacts.id) {
			return second_contacts;
		}
		this.second_contacts=t_person.findById(second_contacts_id);
		return this.second_contacts;
	}

	public t_person getLegal() {
		if(this.legal_person_id==null){
			return null;
		}
		if(legal!=null && this.legal_person_id==this.legal.id) {
			return legal;
		}
		this.legal=t_person.findById(legal_person_id);
		return this.legal;
	}
	
	@Override
	public String toString() {
		return "t_users_info [id=" + id+", user_id=" + user_id + ", legal_person=" + legal_person + ", business_scope="
				+ business_scope + ", income_debt_info=" + income_debt_info + ", asset_info=" + asset_info
				+ ", other_finance_info=" + other_finance_info + ", other_info=" + other_info + ", industry_id="
				+ industry_id + ", industry_name=" + industry_name + ", industry=" + industry + ", reg_capital="
				+ reg_capital + ", work_industry=" + work_industry + ", salary=" + salary + ", accumulation_fund="
				+ accumulation_fund + ", rent=" + rent + ", QQ=" + QQ + ", brand_name=" + brand_name
				+ ", first_contacsts_id=" + first_contacts_id + ", first_contacts_relation=" + first_contacts_relation
				+ ", second_contacts_id=" + second_contacts_id + ", second_contacts_relation="
				+ second_contacts_relation + ", legal_person_id=" + legal_person_id  + "]";
	}

	/*
	 * 对敏感信息做隐藏
	 */
	public t_users_info hide_date() throws CloneNotSupportedException {
		//this.id=0L;
		//this.user_id=0L;
		t_users_info userInfo=(t_users_info) this.clone();
		userInfo.id=0L;
		userInfo.user_id=0L;
		return userInfo;
	}
	
	
}
