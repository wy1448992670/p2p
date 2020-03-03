package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_credit_levels  extends Model {

    public String name;
    public String image_filename;
    public boolean is_enable;
    public int min_credit_score;
    public int min_audit_items;
    public String is_allow_overdue;
    public String suggest;
    public String must_items;
	
    @Override
	public String toString() {
		return "v_credit_levels [name=" + name + ", image_filename=" + image_filename
				+ ", min_credit_score=" + min_credit_score + ", min_audit_items=" + min_audit_items
				+ ", is_allow_overdue=" + is_allow_overdue + ", suggest=" + suggest
				+ ", must_items=" + must_items + ", id=" + id + "]\n";
	}

}