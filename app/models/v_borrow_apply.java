package models;

import constants.ProductEnum;
import constants.UserTypeEnum;
import play.db.jpa.Model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Auther: huangsj
 * @Date: 2018/10/18 13:38
 * @Description:
 */
@Entity
public class v_borrow_apply  extends Model {



    public String borrow_no;
    public Long user_id;


    public String product_name;

    public String loan_property_name;

    public String loan_purpose_name;

    transient public t_dict_loan_purposes loan_purpose;

    public BigDecimal apply_amount;//申请金额
    public BigDecimal approve_amount;//审批金额
    public Integer period;

    public String apply_time;

   // public int status;
    public String statu_name;



    public Long bid;
    public String title;
    public Integer bid_status;
    public Double amount;
    public Double has_invested_amount;
    public Integer period_bid;
    public Integer period_unit;


    /*public v_borrow_apply(Long id,String borrow_no, Long user_id, String product_name, String loan_property_name, String loan_purpose_name,
                          BigDecimal apply_amount, BigDecimal approve_amount,Date apply_time, int period, int status, String statu_name,
                          Long bid, String title, int bid_status, double amount, double has_invested_amount, int period_bid, int period_unit) {
        this.id = id;
        this.borrow_no = borrow_no;
        this.user_id = user_id;
        this.product_name = product_name;
        this.loan_property_name = loan_property_name;
        this.loan_purpose_name = loan_purpose_name;
        this.apply_amount = apply_amount;
        this.approve_amount = approve_amount;
        this.apply_time = apply_time;
        this.period = period;
        this.status = status;
        this.statu_name = statu_name;
        this.bid = bid;
        this.title = title;
        this.bid_status = bid_status;
        this.amount = amount;
        this.has_invested_amount = has_invested_amount;
        this.period_bid = period_bid;
        this.period_unit = period_unit;
    }*/


    /**
     * 分标标的列表
     */
    @Transient
    public List<t_bids> bids;


    @Override
    public boolean equals(Object obj) {
        if(this==obj) {
            return true;
        }

        if (null != obj && obj instanceof v_borrow_apply) {
            v_borrow_apply p = (v_borrow_apply) obj;

            if (this.id.equals(p.id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
