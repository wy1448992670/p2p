package models.core;

import javax.persistence.Entity;

import models.EnumCodeModel;

/*
 * 
 */
@Entity
public class t_new_product extends EnumCodeModel{
	private static final long serialVersionUID = 1L;

	public String name;//1.信亿贷 2.房亿贷 3.农亿贷 4.亿美贷 5.车亿贷 6.亿分期
	
	/**
	 * @see EnumCodeModel.code_name
	 */
	//public String code_name;//1.XIN 2.FANG 3.NONG 4.YI 5.CHE
	
	public String borrow_no_suffix;//借款申请编号后缀1.X 2.F 3.N 4.Y 5.C
	public boolean is_use;//能否使用 true:能使用 ,false:不能使用
	public boolean borrow_app_can_use;//普通借款端是否可以使用,当前可使用id=1,2,3
	public String description;

}
