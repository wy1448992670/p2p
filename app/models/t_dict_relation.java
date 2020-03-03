package models;

import javax.persistence.Entity;

@Entity
public class t_dict_relation extends EnumModel{
	public String name;
	public String code;
	public String description;
	public boolean is_use;
}
