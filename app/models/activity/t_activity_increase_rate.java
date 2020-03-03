package models.activity;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:17
 * @Description:
 */
@Entity
public class t_activity_increase_rate  extends Model {

    public Integer type;

    public String name;

    public String remark;

    public Date create_time;

    public Date start_time;

    public Date stop_time;
}
