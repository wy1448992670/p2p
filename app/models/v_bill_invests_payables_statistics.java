package models;

import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 应付账单管理--理财情况统计
 * @author zhs
 * @version 6.0
 * @created 2014年5月21日 下午4:33:10
 */
@Entity
public class v_bill_invests_payables_statistics extends Model {
     public int year;
     public int month;
     public int payables_bills;
     public double payables_amount;
     public int has_paid_bills;
     public double has_paid_amount;
     public int normal_paid_bills;
     public double ontime_complete_rate;
     public int principal_advances_bills;
     public double principal_advances_amount;
     public double principal_advances_rate;
     public int nopaid_bills;
     public double nopaid_amount;
     public double nopaid_rate;
     

}
