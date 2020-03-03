package models;

import javax.persistence.Entity;

/*
 * 
 */
@Entity
public class t_industries extends EnumModel{
	public String name;//行业名称
	public boolean is_disable;//是否禁用 true:禁用,false:不禁用
}
