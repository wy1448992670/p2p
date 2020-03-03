package services.activity;

import business.Bid;
import business.Bill;
import business.LogCore;
import business.Supervisor;
import com.sun.org.apache.xpath.internal.operations.Bool;
import dao.activity.ActivityIncreaseRateDao;
import dao.activity.ActivityIncreaseRateDetailDao;
import models.activity.t_activity_increase_rate;
import models.activity.t_activity_increase_rate_detail;
import models.t_bill_invests;
import models.t_bills;
import models.t_invests;
import models.t_red_packages_history;
import utils.JPAUtil;
import utils.PageBean;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:29
 * @Description:
 */
public class ActivityIncreaseRateService {

    public static final int relation_type_a = 3;


    public static PageBean<Map<String, Object>> getActivityList(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime, String currPageStr, String pageSizeStr) throws Exception {

        return ActivityIncreaseRateDao.getActivityList(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime, currPageStr, pageSizeStr);

    }

    public  static String getInvestTotal(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime) throws Exception {
        return ActivityIncreaseRateDao.getInvestTotal(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime);
    }

    public static PageBean<Map<String, Object>> getIncreaseTotal(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime, Boolean isExport, String currPageStr, String pageSizeStr) throws Exception {
        return ActivityIncreaseRateDao.getIncreaseTotal(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime, isExport, currPageStr, pageSizeStr);
    }

    public static PageBean<Map<String, Object>> getIncreasDetails(String userName, int orderState, int type, int state, String name, String startTime, String stopTime, Boolean isExport, String currPageStr, String pageSizeStr) throws Exception {
        return ActivityIncreaseRateDao.getIncreaseDetails(userName, orderState, type, state, name, startTime, stopTime, isExport, currPageStr, pageSizeStr);
    }


    /**
     * 获取当前可用的活动
     *
     * @return
     */
    public static List<Map<String, Object>> getEnableActivitysForCurrent() {
        return ActivityIncreaseRateDao.getEnableActivitysForCurrent();
    }

    /**
     * 添加活动
     *
     * @param type      类型:1全场加息 2首投加息 3尾投加息
     * @param name
     * @param rate
     * @param startTime
     * @param stopTime
     */
    public static void addActivity(int type, String name, BigDecimal rate, Date startTime, Date stopTime) {

        //先判断有没有正在使用的同类活动，如果有则返回提示
        boolean isExist = ActivityIncreaseRateDao.getActivityByType(type);
        if (isExist) {
            throw new IllegalArgumentException("已存在同类型的未结束的活动");
        }


        t_activity_increase_rate activity = ActivityIncreaseRateDao.insert(type, name, startTime, stopTime);

        t_activity_increase_rate_detail detail = ActivityIncreaseRateDetailDao.insert(activity.id, rate, startTime, stopTime);

        if (detail.id > 0) {
            //添加操作日志
            LogCore.create(relation_type_a, detail.id, 2, Supervisor.currSupervisor().id, detail.state, detail.create_time, "添加加息活动", "");
        }
    }


    /**
     * 修改活动在审核没通过，或复审通过后修改活动
     *
     * @param detailId
     */
    public static void editActivity(Long detailId, String name, BigDecimal rate, Date startTime, Date stopTime) {

        t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(detailId);

        if (detail.state.intValue() == -3) {
            throw new RuntimeException("此活动已关闭，不能修改");
        }

        //复审通过后的修改
        if (detail.state == 2) {

            t_activity_increase_rate_detail new_detail = ActivityIncreaseRateDetailDao.insert(detail.activity_id, rate, startTime, stopTime);

            t_activity_increase_rate activity = t_activity_increase_rate.findById(detail.activity_id);
            activity.name = name;
            activity.start_time = startTime;
            activity.stop_time = stopTime;
            activity.save();


            //添加操作日志
            LogCore.create(relation_type_a, new_detail.id, 2, Supervisor.currSupervisor().id, new_detail.state, new_detail.create_time, "活动修改", "");

            //审核不通过的修改
        } else if (detail.state < 1) {
            detail.state = 0;
            detail.rate = rate;
            detail.start_time = startTime;
            detail.stop_time = stopTime;
            detail.save();

            t_activity_increase_rate activity = t_activity_increase_rate.findById(detail.activity_id);
            activity.name = name;
            activity.start_time = startTime;
            activity.stop_time = stopTime;
            activity.save();

            //添加操作日志
            LogCore.create(relation_type_a, detail.id, 2, Supervisor.currSupervisor().id, detail.state, detail.create_time, "活动修改", "");
        }

    }


    /**
     * 活动详情
     */
    public static Map<String, Object> getActivityDetail(int activityId) {
        Map<String, Object> result = new HashMap<>();
        result.put("detail", ActivityIncreaseRateDao.getActivityDetailById(activityId));
        result.put("logs", ActivityIncreaseRateDao.getActivityDetailLogs(activityId));
        return result;
    }


    /**
     * 初审活动
     */
    public static void firstAuditActivity(Long detailId, int ispass, String remark) {


        t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(detailId);

        if (ispass == 1) {
            detail.state = 1;
        } else {
            detail.state = -1;
        }

        detail.save();

        //添加操作日志
        LogCore.create(relation_type_a, detail.id, 2, Supervisor.currSupervisor().id, detail.state, detail.create_time, ispass == 1 ? "通过" : "不通过", "加息初审", remark);

    }


    /**
     * 复审活动
     */
    public static void lastAuditActivity(Long detailId, int ispass, String remark) {


        t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(detailId);

        if (ispass == 1) {
            //判断此活动有正在运行的吗
            List<t_activity_increase_rate_detail> details = t_activity_increase_rate_detail.find("state = 2 and activity_id = ? order by id desc ", detail.activity_id).fetch();
            if (details.size() > 0) {

                //之前通过的活动
                t_activity_increase_rate_detail d = details.get(0);
                d.state = -3;
                d.save();
            }

            Date curDate = new Date();
            if (curDate.compareTo(detail.start_time) > 0) {
                detail.start_time = curDate;
            }

            detail.state = 2;

        } else {
            detail.state = -2;
        }

        detail.save();

        //添加操作日志
        LogCore.create(relation_type_a, detail.id, 2, Supervisor.currSupervisor().id, detail.state, detail.create_time, ispass == 1 ? "通过" : "不通过", "加息复审", remark);

    }


    /**
     * 关闭活动
     */
    public static void closeActivity(Long detailId) {
        t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(detailId);

        long count = t_activity_increase_rate_detail.count("activity_id=?", detail.activity_id);


        if (detail.state.intValue() == 2) {
            detail.state = -3;
        } else {
            detail.state = -4;
        }

        if (count > 1) {
            detail.visibile = 0;
            t_activity_increase_rate_detail using_detail = t_activity_increase_rate_detail.find("activity_id=? and state =2 ", detail.activity_id).first();
            if (using_detail != null) {
                t_activity_increase_rate activity = t_activity_increase_rate.findById(using_detail.activity_id);
                activity.start_time = using_detail.start_time;
                activity.stop_time = using_detail.stop_time;
                activity.save();
            }

        }

        detail.save();


        //添加操作日志
        LogCore.create(relation_type_a, detail.id, 2, Supervisor.currSupervisor().id, detail.state, new Date(), "加息活动关闭", "");
    }

    /**
     * 计算投资订单的加息情况
     *
     * @param bid
     */
    public static void caculateIncreaseInterestForInvests(Bid bid) {
        /*
         * 分为    全场加息<----标的加息<----加息券
         * 可以和  首投<---- 尾投加息   并存
         */

        //查询投资订单里有没有加息活动，然后根据活动类型计算加息产生的利息
        boolean isIncreaseRate = false;

        List<t_invests> invests = t_invests.find("bid_id = ?", bid.id).fetch();
        if (invests != null) {
            for (t_invests invest : invests) {
                invest.refresh();
                double increase_rate1 = 0, increase_rate2 = 0;
                //全场加息
                if (invest.increase_activity_id1 != null && invest.increase_activity_id1 > 0) {
                    t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(invest.increase_activity_id1);
                    increase_rate1 = detail.rate.doubleValue();
                } else {

                    //标的加息
                    if (bid.isIncreaseRate || bid.increaseRate > 0) {
                        increase_rate1 = bid.increaseRate;
                    } else {
                        //是否使用加息券
                        List<t_red_packages_history> redPackages = t_red_packages_history.find("invest_id = ? and coupon_type = 2 ", invest.id).fetch();

                        if (redPackages != null && redPackages.size() > 0) {
                            increase_rate1 = redPackages.get(0).money;
                        }
                    }
                }

                //首尾投
                if (invest.increase_activity_id2 != null && invest.increase_activity_id2 > 0) {
                    t_activity_increase_rate_detail detail = t_activity_increase_rate_detail.findById(invest.increase_activity_id2);
                    increase_rate2 = detail.rate.doubleValue();
                }


                //根据加息的利率计算产生的利息
                if (increase_rate1 > 0 || increase_rate2 > 0) {
                    isIncreaseRate = true;
                    BigDecimal correct_increase_interest1 = BigDecimal.ZERO;
                    BigDecimal correct_increase_interest2 = BigDecimal.ZERO;


                    List<Map<String, Object>> increase1 = new ArrayList<>();
                    if (increase_rate1 > 0) {
                        increase1 = Bill.repaymentCalculate(invest.amount, bid.apr, bid.period, bid.periodUnit, (int) bid.repayment.id, increase_rate1);
                    }

                    List<Map<String, Object>> increase2 = new ArrayList<>();
                    if (increase_rate1 > 0 && increase_rate2 > 0) {
                        increase2 = Bill.repaymentCalculate(invest.amount, bid.apr + increase_rate1, bid.period, bid.periodUnit, (int) bid.repayment.id, increase_rate2);
                    }else if (increase_rate1 == 0 && increase_rate2 > 0) {
                        increase2 = Bill.repaymentCalculate(invest.amount, bid.apr, bid.period, bid.periodUnit, (int) bid.repayment.id,  increase_rate2);
                    }

                    // 投资账单
                    List<t_bill_invests> billInvests = t_bill_invests.find(" invest_id = ? ", invest.id).fetch();
                    //对每笔账单计算
                    for (t_bill_invests billInvest : billInvests) {

                        if (increase1.size() > 0) {
                            for (Map<String, Object> map : increase1) {
                                if (billInvest.periods == Integer.parseInt(map.get("period").toString())) {
                                    billInvest.receive_increase_interest1 = Double.valueOf(map.get("monPayIncreaseInterest") + "");
                                    correct_increase_interest1 = correct_increase_interest1.add(new BigDecimal(map.get("monPayIncreaseInterest") + ""));
                                }
                            }
                        }
                        if (increase2.size() > 0) {
                            for (Map<String, Object> map : increase2) {
                                if (billInvest.periods == Integer.parseInt(map.get("period").toString())) {
                                    billInvest.receive_increase_interest2 = Double.valueOf(map.get("monPayIncreaseInterest") + "");
                                    correct_increase_interest2 = correct_increase_interest2.add(new BigDecimal(map.get("monPayIncreaseInterest") + ""));
                                }
                            }
                        }

                        billInvest.receive_increase_interest = (billInvest.receive_increase_interest1 == null ? 0L : billInvest.receive_increase_interest1)
                                + (billInvest.receive_increase_interest2 == null ? 0L : billInvest.receive_increase_interest2);


                        billInvest.save();
                    }

                    //修改投资加息金额
                    invest.correct_increase_interest = correct_increase_interest1.add(correct_increase_interest2).doubleValue();
                    invest.correct_increase_interest1 = correct_increase_interest1.doubleValue();
                    invest.correct_increase_interest2 = correct_increase_interest2.doubleValue();

                    invest.save();

                }
            }

            if (isIncreaseRate) {
                //查询还款账单
                List<t_bills> redIncreaseBills = t_bills.find(" bid_id = ? ", bid.id).fetch();

                //查询该标的的所有投资账单，sum(IncreaseInterest)插入还款加息字段
                String hql = "select t.periods as periods, IFNULL(sum(t.receive_increase_interest),0) as receive_increase_interest, IFNULL(sum(t.receive_increase_interest1),0) as receive_increase_interest1, IFNULL(sum(t.receive_increase_interest2),0) as receive_increase_interest2  from t_bill_invests t where t.bid_id = ? GROUP BY t.periods";
                List<Map<String, Object>> billInvestTotal = JPAUtil.getList(hql, bid.id);

                //将每期的投资账单的加息和  统计到还款账单中
                if (billInvestTotal != null) {
                    for (int i = 0; i < billInvestTotal.size(); i++) {
                        Map<String, Object> map = billInvestTotal.get(i);
                        for (t_bills redIncreaseBill : redIncreaseBills) {
                            if (Integer.parseInt(map.get("periods") + "") == redIncreaseBill.periods) {
                                redIncreaseBill.repayment_increase_interest = Double.parseDouble(map.get("receive_increase_interest") + "");
                                redIncreaseBill.repayment_increase_interest1 = Double.parseDouble(map.get("receive_increase_interest1") + "");
                                redIncreaseBill.repayment_increase_interest2 = Double.parseDouble(map.get("receive_increase_interest2") + "");
                                redIncreaseBill.save();
                            }
                        }
                    }
                }
            }

        }


    }
}
