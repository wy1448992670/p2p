package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 
 * @author lzp
 * @version 6.0
 * @created 2014-4-4 下午3:41:24
 */

@Entity
public class v_mall_goods_views extends Model {

	public String name;
	public long surplus;
	public String pic_path;
	public String exchange_scroe;
}
