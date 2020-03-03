package jobs;


import constants.Constants;
import models.t_bills;
import models.t_products;
import models.t_statistic_data_disclosure;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.On;
import utils.EmptyUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 每天定时定点任务,每天0:0：1执行
 * @author hsj
 *
 */
@On("1 0 0 * * ?")
//@Every("5min")
public class DataDisclosureJob extends BaseJob {

    /**
     * 每天执行统计信息披露数据的job
     * @return void
     * @author Huangsj
     * @date 2018/8/20 13:16
     */
    public void doJob() {


        if(!"1".equals(IS_JOB))return;


        Logger.info("===> 统计信披数据开始...");

//        " select new t_products(id, name, small_image_filename) from t_products "


        String sql =
                //"SELECT new t_statistic_data_disclosure( no_repay_money, day_in_money," +
                "SELECT  no_repay_money, day_in_money,  " +
                        "          available_balance, no_repay_corpus,  " +
                        "          no_repay_interest,  day_recharge_money,  " +
                        "          day_withdrawals_money,  " +
                        "          day_invest_money,  total_registe_users,  " +
                        "          day_registe_users,  borrowers,  " +
                        "          day_new_borrowers,  investers,  " +
                        "          day_new_investers,  product_total_money,  " +
                        "          day_new_product_money,  product_total_count,  " +
                        "          day_new_product_count,  total_overdue_money,  " +
                        "          norepay_overdue_money,  tatal_overdua_count,  " +
                        "          norepay_overdua_count,  day_repay_money, the_date  " +
                        " " +
                        "FROM (  " +
                        " SELECT (t1.no_repay_corpus + t1.no_repay_interest) no_repay_money,  " +
                        " (t1.day_recharge_money - t1.day_withdrawals_money) day_in_money,  " +
                        " t1.*  " +
                        "FROM (  " +
                        "SELECT  " +
                        "  (select a.m1+b.m2 from " +
                        " (SELECT sum(u.balance) m1 from t_users u where u.finance_type != 0) a,  " +
                        " (select   IFNULL(sum(a.amount),0) m2 from t_user_withdrawals a INNER JOIN t_users b on a.user_id = b.id where a.`status` = 0 and b.finance_type != 0) b ) as available_balance ,  " +
                        "  (SELECT sum(a.repayment_corpus) from t_bills a where a.`status` = -1 and a.overdue_mark = 0) as no_repay_corpus,  " +
                        "  (SELECT sum(a.repayment_interest) from t_bills a where a.`status` = -1 and a.overdue_mark = 0 ) as no_repay_interest,  " +
                        "  (SELECT IFNULL(sum(a.amount),0) from t_user_recharge_details a INNER JOIN t_users b on a.user_id = b.id " +
                        "   where b.finance_type !=0 and date_format(a.completed_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        "  and a.is_completed = 1) as day_recharge_money ,  " +
                        "  (select IFNULL(sum(amount),0) from t_user_withdrawals a INNER JOIN t_users b on a.user_id = b.id " +
                        "   where  b.finance_type !=0 and date_format(a.pay_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        " and status = 2 ) as day_withdrawals_money,  " +
                        "  (SELECT  aa.amount - bb.amount from  " +
                        "   (SELECT ifnull(sum(t.amount),0) amount " +
                        " from t_invests t  " +
                        "  where date_format(t.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        "   ) aa,(  " +
                        " SELECT ifnull(sum(c.amount),0) amount  " +
                        "  from (  " +
                        "   select * from t_bids a  " +
                        "    where a.`status` = -4 " +
                        "  and a.audit_time = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        "   )b INNER JOIN t_invests c on b.id = c.bid_id) bb) as day_invest_money,  " +
                        "  (SELECT count(*) from t_users) as total_registe_users ,  " +
                        "  (SELECT count(*) from t_users u  " +
                        " where date_format(u.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_registe_users,  " +
                        "  (SELECT count(*) from (" +
                        "    SELECT b.user_id from t_bids b GROUP BY b.user_id" +
                        "   ) a) as borrowers ," +
                        "  (SELECT count(*) from (  " +
                        " SELECT * from  " +
                        "  (  " +
                        "   SELECT b.user_id from t_bids b  " +
                        "  where date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')   " +
                        "  GROUP BY b.user_id " +
                        "  ) aa " +
                        "  where (  " +
                        "   SELECT count(*) from t_bids b2  " +
                        "    where aa.user_id = b2.user_id  " +
                        "     and  date_format(b2.time, '%Y-%m-%d') < date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        "  ) = 0 " +
                        "   ) a) as day_new_borrowers ,  " +
                        "  (SELECT count(*) from (" +
                        "    SELECT b.user_id from t_invests b GROUP BY b.user_id" +
                        "   ) a) investers ," +
                        "  (SELECT count(*) from (  " +
                        " SELECT * from (  " +
                        "  SELECT b.user_id from t_invests b  " +
                        "   where date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')   " +
                        "    GROUP BY b.user_id " +
                        " ) aa " +
                        " where (  " +
                        "  SELECT count(*) from t_invests b2 " +
                        "    where b2.user_id = aa.user_id " +
                        "     and  date_format(b2.time, '%Y-%m-%d') < date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
                        "    ) = 0 " +
                        "   ) a) day_new_investers,  " +
                        "  (SELECT IFNULL(sum(b.amount),0) from t_bids b where b.`status` > 0) as product_total_money,  " +
                        "  (SELECT IFNULL(sum(b.amount),0) from t_bids b  " +
                        "   where b.`status` > 0  " +
                        " and date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d') ) as day_new_product_money,  " +
                        "  (SELECT count(*) from t_bids b where b.`status` > 0) as product_total_count,  " +
                        "  (SELECT count(*) from t_bids b  " +
                        "   where b.`status` > 0  " +
                        " and date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_new_product_count,  " +
                        "  (SELECT sum(a.repayment_corpus+ a.repayment_interest) from t_bills a where a.overdue_mark != 0) as total_overdue_money,  " +
                        "  (SELECT sum(IFNULL(a.repayment_corpus,0)+ IFNULL(a.repayment_interest,0)) from t_bills a where a.overdue_mark != 0 and a.`status` != -3) as norepay_overdue_money ,  " +
                        "  (SELECT count(*) from t_bills a where a.overdue_mark != 0) as tatal_overdua_count,  " +
                        "  (SELECT count(*) from t_bills a where a.overdue_mark != 0 and a.`status` != -3) as norepay_overdua_count,  " +
                        "  (SELECT IFNULL(sum(a.real_repayment_corpus),0)+ IFNULL(sum(a.real_repayment_interest),0) from t_bills a  " +
                        " where date_format(a.real_repayment_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_repay_money ,  " +
                        "  (select date_add(CURDATE(), interval -1 day)) as the_date " +
                        " from DUAL " +
                        ") t1 )tt";
//        Map<String, Object> result = t_bills.find(sql).first();

        try {
            Query query = JPA.em().createNativeQuery(sql);
            query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

            List<Map<String,Object>> results=query.getResultList();

            Map<String, Object> result = (Map<String, Object>) results.get(0);

            t_statistic_data_disclosure statistic_data_disclosure = new t_statistic_data_disclosure(
                    EmptyUtil.obj20(result.get("no_repay_money")), EmptyUtil.obj20(result.get("day_in_money")),
                    EmptyUtil.obj20(result.get("available_balance")), EmptyUtil.obj20(result.get("no_repay_corpus")),
                    EmptyUtil.obj20(result.get("no_repay_interest")), EmptyUtil.obj20(result.get("day_recharge_money")),
                    EmptyUtil.obj20(result.get("day_withdrawals_money")),
                    EmptyUtil.obj20(result.get("day_invest_money")),
                    EmptyUtil.obj20(result.get("total_registe_users")).intValue(),
                    EmptyUtil.obj20(result.get("day_registe_users")).intValue(), EmptyUtil.obj20(result.get("borrowers")).intValue(),
                    EmptyUtil.obj20(result.get("day_new_borrowers")).intValue(), EmptyUtil.obj20(result.get("investers")).intValue(),
                    EmptyUtil.obj20(result.get("day_new_investers")).intValue(),
                    EmptyUtil.obj20(result.get("product_total_money")),
                    EmptyUtil.obj20(result.get("day_new_product_money")), EmptyUtil.obj20(result.get("product_total_count")).intValue(),
                    EmptyUtil.obj20(result.get("day_new_product_count")).intValue(), EmptyUtil.obj20(result.get("total_overdue_money")),
                    EmptyUtil.obj20(result.get("norepay_overdue_money")), EmptyUtil.obj20(result.get("tatal_overdua_count")).intValue(),
                    EmptyUtil.obj20(result.get("norepay_overdua_count")).intValue(), EmptyUtil.obj20(result.get("day_repay_money")),
                    new SimpleDateFormat("yyyy-MM-dd").parse(result.get("the_date").toString()));


            //t_statistic_data_disclosure statistic_data_disclosure= (t_statistic_data_disclosure)query.getResultList().get(0);


            //t_statistic_data_disclosure statistic_data_disclosure = t_statistic_data_disclosure.find(sql).first();

            statistic_data_disclosure.save();

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("统计信披数据出现异常:" + e.getMessage());
        }


//        String insertSql = "INSERT into t_statistic_data_disclosure(no_repay_money,day_in_money,available_balance,no_repay_corpus,no_repay_interest,day_recharge_money," +
//                "day_withdrawals_money,day_invest_money,total_registe_users,day_registe_users,borrowers,day_new_borrowers,investers,day_new_investers,product_total_money," +
//                "day_new_product_money,product_total_count,day_new_product_count,total_overdue_money,norepay_overdue_money,tatal_overdua_count,norepay_overdua_count,day_repay_money,the_date" +
//                ")" +
//                " SELECT (t1.no_repay_corpus + t1.no_repay_interest) no_repay_money," +
//                " (t1.day_recharge_money - t1.day_withdrawals_money) day_in_money," +
//                " t1.*" +
//                "FROM (" +
//                "SELECT " +
//                "  (select a.m1+b.m2 from" +
//                "    (SELECT sum(balance) m1 from t_users) a," +
//                "    (select sum(amount) m2 from t_user_withdrawals where `status` = 0) b) as available_balance ,"+
//                "  (SELECT sum(a.repayment_corpus) from t_bills a where a.`status` = -1 and a.overdue_mark = 0) as no_repay_corpus," +
//                "  (SELECT sum(a.repayment_interest) from t_bills a where a.`status` = -1 and a.overdue_mark = 0 ) as no_repay_interest," +
//                "  (SELECT IFNULL(sum(a.amount),0) from t_user_recharge_details a " +
//                "   where date_format(a.completed_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')" +
//                "     and a.is_completed = 1) as day_recharge_money ," +
//                "  (select IFNULL(sum(amount),0) from t_user_withdrawals " +
//                "   where date_format(pay_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')" +
//                "    and status = 2 ) as day_withdrawals_money," +
//                "  (SELECT  aa.amount - bb.amount from " +
//                "   (SELECT ifnull(sum(t.amount),0) amount" +
//                "    from t_invests t " +
//                "     where date_format(t.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')" +
//                "   ) aa,(" +
//                "    SELECT ifnull(sum(c.amount),0) amount " +
//                "     from (" +
//                "      select * from t_bids a " +
//                "       where a.`status` = -4" +
//                "        and a.audit_time = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d') " +
//                "      )b INNER JOIN t_invests c on b.id = c.bid_id) bb) as day_invest_money," +
//                "  (SELECT count(*) from t_users) as total_registe_users ," +
//                "  (SELECT count(*) from t_users u " +
//                "    where date_format(u.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_registe_users," +
//                "  (SELECT count(*) from (" +
//                "    SELECT b.user_id from t_bids b GROUP BY b.user_id" +
//                "   ) a) as borrowers ," +
//                "  (SELECT count(*) from (" +
//                "    SELECT * from " +
//                "     (" +
//                "      SELECT b.user_id from t_bids b " +
//                "        where date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
//                "        GROUP BY b.user_id" +
//                "     ) aa" +
//                "     where (" +
//                "         SELECT count(*) from t_bids b2 " +
//                "          where aa.user_id = b2.user_id " +
//                "           and  date_format(b2.time, '%Y-%m-%d') < date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d') " +
//                "        ) = 0" +
//                "   ) a) as day_new_borrowers , " +
//                "  (SELECT count(*) from (" +
//                "    SELECT b.user_id from t_invests b GROUP BY b.user_id" +
//                "   ) a) investers ," +
//                "  (SELECT count(*) from (" +
//                "    SELECT * from (" +
//                "     SELECT b.user_id from t_invests b " +
//                "      where date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')  " +
//                "       GROUP BY b.user_id" +
//                "    ) aa" +
//                "    where (" +
//                "        SELECT count(*) from t_invests b2" +
//                "          where b2.user_id = aa.user_id" +
//                "           and  date_format(b2.time, '%Y-%m-%d') < date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d') " +
//                "       ) = 0" +
//                "   ) a) day_new_investers," +
//                "  (SELECT IFNULL(sum(b.amount),0) from t_bids b where b.`status` > 0) as product_total_money," +
//                "" +
//                "  (SELECT IFNULL(sum(b.amount),0) from t_bids b " +
//                "   where b.`status` > 0 " +
//                "    and date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d') ) as day_new_product_money," +
//                "  (SELECT count(*) from t_bids b where b.`status` > 0) as product_total_count," +
//                "  (SELECT count(*) from t_bids b " +
//                "   where b.`status` > 0 " +
//                "    and date_format(b.time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_new_product_count, " +
//                "" +
//                "" +
//                "  (SELECT sum(a.repayment_corpus+ a.repayment_interest) from t_bills a where a.overdue_mark != 0) as total_overdue_money, " +
//                "  (SELECT sum(IFNULL(a.repayment_corpus,0)+ IFNULL(a.repayment_interest,0)) from t_bills a where a.overdue_mark != 0 and a.`status` != -3) as norepay_overdue_money ," +
//                "  (SELECT count(*) from t_bills a where a.overdue_mark != 0) as tatal_overdua_count," +
//                "  (SELECT count(*) from t_bills a where a.overdue_mark != 0 and a.`status` != -3) as norepay_overdua_count," +
//                "  (SELECT IFNULL(sum(a.real_repayment_corpus),0)+ IFNULL(sum(a.real_repayment_interest),0) from t_bills a " +
//                "    where date_format(a.real_repayment_time, '%Y-%m-%d') = date_format(date_add(CURDATE(), interval -1 day), '%Y-%m-%d')) as day_repay_money ," +
//                "  (select date_add(CURDATE(), interval -1 day)) as the_date" +
//                " from DUAL" +
//                ") t1";
//
//
//
//        EntityManager em = JPA.em();
//
//        em.createNativeQuery(insertSql).executeUpdate();




    }
}
