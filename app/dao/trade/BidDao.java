package dao.trade;

import utils.JPAUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/26 15:36
 * @Description:
 */
public class BidDao {

    /**
     * 查找正在募集标的当前的活动信息
     * @param bid
     * @return
     */
    public static List<Map<String, Object>> getActivityInfoForCurrentBid(Long bid){


        //=======================================columns=======================================
        String columns = " if(t_bids.`status` <3 ,CASE when activity1.rate is null THEN CAST(t_bids.is_increase_rate AS signed) else 1 end, CAST(t_bids.is_increase_rate AS signed)) isIncreaseRate,if(t_bids.`status` <3 ,IFNULL(activity1.rate,`t_bids`.`increase_rate`),`t_bids`.`increase_rate`) AS `increaseRate`,if(t_bids.`status` <3 ,CASE when activity1.rate is null then IFNULL(`t_bids`.`increase_rate_name`,'') else activity1.`NAME` end,`t_bids`.`increase_rate_name`)  AS `increaseRateName`,activity1.`name` name1,ifnull(activity1.rate,0) rate1,activity2.`name` name2,ifnull(activity2.rate,0) rate2,activity3.`name` name3,ifnull(activity3.rate,0) rate3 ";
        //=======================================table=======================================
        String table = " FROM `t_bids` " +
                " LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 1 LIMIT 1 ) activity1 ON TRUE " +
                " LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 2 LIMIT 1 ) activity2 ON TRUE " +
                " LEFT JOIN (SELECT b.type,a.rate,b. NAME,b.id FROM t_activity_increase_rate_detail a INNER JOIN t_activity_increase_rate b ON a.activity_id = b.id WHERE a.state = 2 AND a.start_time <= NOW() AND NOW() < a.stop_time AND b.type = 3 LIMIT 1 ) activity3 ON TRUE " +
                " where `t_bids`.`status` < 3 and t_bids.id = ? ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();

        if (bid > 0) {
            paramsList.add(bid);
        }else{
            throw new IllegalArgumentException("标的id参数传入有问题");
        }

        return JPAUtil.getList("select " + columns + table, paramsList.toArray());
    }
}
