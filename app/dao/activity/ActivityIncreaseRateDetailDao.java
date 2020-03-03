package dao.activity;

import models.activity.t_activity_increase_rate_detail;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:27
 * @Description:
 */
public class ActivityIncreaseRateDetailDao {


    public static t_activity_increase_rate_detail insert(Long activityId, BigDecimal rate, Date startTime, Date stopTime) {
        t_activity_increase_rate_detail detail = new t_activity_increase_rate_detail();

        detail.activity_id = activityId;
        detail.rate = rate;
        detail.state = 0;
        detail.create_time = new Date();
        detail.start_time = startTime;
        detail.stop_time = stopTime;
        detail.visibile = 1;

        return detail.save();
    }
}
