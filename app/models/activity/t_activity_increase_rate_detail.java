package models.activity;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:18
 * @Description:
 */
@Entity
public class t_activity_increase_rate_detail  extends Model {

    public Long activity_id;

    public BigDecimal rate;

    public Integer state;

    public Date create_time;

    public Date start_time;

    public Date stop_time;

    public Integer visibile;
}
