package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class v_user_info_v1 extends Model {
	private static final long serialVersionUID = -1L;
	public String name;

}