package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class t_statistic_data_disclosure extends Model {


    /**
     * 统计当天
     */
    public Date the_date;
    /**
     * 站岗资金
     */
    public BigDecimal available_balance;
    /**
     * 平台待还总额
     */
    public BigDecimal no_repay_money;
    /**
     * 待还本金
     */
    public BigDecimal no_repay_corpus;
    /**
     * 待还利息
     */
    public BigDecimal no_repay_interest;
    /**
     * 当天充值总额
     */
    public BigDecimal day_recharge_money;
    /**
     * 当天提现总额
     */
    public BigDecimal day_withdrawals_money;
    /**
     * 资金净流入
     */
    public BigDecimal day_in_money;
    /**
     * 当天投资总额
     */
    public BigDecimal day_invest_money;
    /**
     * 累计注册用户
     */
    public Integer total_registe_users;
    /**
     * 当日注册用户
     */
    public Integer day_registe_users;
    /**
     * 借款总人数
     */
    public Integer borrowers;
    /**
     * 当天新增借款人数
     */
    public Integer day_new_borrowers;
    /**
     * 投资总人数
     */
    public Integer investers;
    /**
     * 当天新增投资人数
     */
    public Integer day_new_investers;
    /**
     * 上标总额
     */
    public BigDecimal product_total_money;
    /**
     * 当天上标总额
     */
    public BigDecimal day_new_product_money;
    /**
     * 上标总数
     */
    public Integer product_total_count;
    /**
     * 当天上标总数
     */
    public Integer day_new_product_count;
    /**
     * 累计逾期
     */
    public BigDecimal total_overdue_money;
    /**
     * 待还逾期
     */
    public BigDecimal norepay_overdue_money;
    /**
     * 累计逾期笔数
     */
    public Integer tatal_overdua_count;
    /**
     * 待还逾期笔数
     */
    public Integer norepay_overdua_count;
    /**
     * 当天还款总额
     */
    public BigDecimal day_repay_money;
    /**
     * 当天借款总额
     */
    public BigDecimal day_borrow_money;

    public t_statistic_data_disclosure(BigDecimal no_repay_money,BigDecimal day_in_money,
                                      BigDecimal available_balance,BigDecimal no_repay_corpus,
                                       BigDecimal no_repay_interest, BigDecimal day_recharge_money,
                                       BigDecimal day_withdrawals_money,
                                       BigDecimal day_invest_money, Integer total_registe_users,
                                       Integer day_registe_users, Integer borrowers,
                                       Integer day_new_borrowers, Integer investers,
                                       Integer day_new_investers, BigDecimal product_total_money,
                                       BigDecimal day_new_product_money, Integer product_total_count,
                                       Integer day_new_product_count, BigDecimal total_overdue_money,
                                       BigDecimal norepay_overdue_money, Integer tatal_overdua_count,
                                       Integer norepay_overdua_count, BigDecimal day_repay_money,
//                                       BigDecimal day_borrow_money,
                                       Date the_date) {
        this.the_date = the_date;
        this.available_balance = available_balance;
        this.no_repay_money = no_repay_money;
        this.no_repay_corpus = no_repay_corpus;
        this.no_repay_interest = no_repay_interest;
        this.day_recharge_money = day_recharge_money;
        this.day_withdrawals_money = day_withdrawals_money;
        this.day_in_money = day_in_money;
        this.day_invest_money = day_invest_money;
        this.total_registe_users = total_registe_users;
        this.day_registe_users = day_registe_users;
        this.borrowers = borrowers;
        this.day_new_borrowers = day_new_borrowers;
        this.investers = investers;
        this.day_new_investers = day_new_investers;
        this.product_total_money = product_total_money;
        this.day_new_product_money = day_new_product_money;
        this.product_total_count = product_total_count;
        this.day_new_product_count = day_new_product_count;
        this.total_overdue_money = total_overdue_money;
        this.norepay_overdue_money = norepay_overdue_money;
        this.tatal_overdua_count = tatal_overdua_count;
        this.norepay_overdua_count = norepay_overdua_count;
        this.day_repay_money = day_repay_money;
//        this.day_borrow_money = day_borrow_money;
    }
}
