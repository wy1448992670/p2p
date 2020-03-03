package models.core;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class t_apply_org_project extends Model {
    private static final long serialVersionUID = 1L;

    /** 1.credit_apply 2.borrow_apply */
    public int apply_type;

    /** apply_type=1:t_credit_apply.id 2:t_borrow_apply.id */
    public long apply_id;

    /** t_org_project.id */
    public long org_project_id;

    @Override
    public String toString() {
        return "t_apply_org_project [" +
        "id=" + id +
        ", apply_type=" + apply_type +
        ", apply_id=" + apply_id +
        ", org_project_id=" + org_project_id +
        "]";
    }
}
