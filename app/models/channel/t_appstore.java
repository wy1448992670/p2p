package models.channel;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @Auther: huangsj
 * @Date: 2018/12/28 15:04
 * @Description:
 */
@Entity
public class t_appstore extends Model {

    public String type;
    public String num;
    public String name;
    public String subname;
    public Date create_time;
    public int state;

}
