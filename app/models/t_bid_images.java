package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 * 标的关联图片表
 * 
 * @ClassName t_bid_images
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年9月27日 下午3:42:05
 * @version 1.0.0
 */
@Entity
public class t_bid_images extends Model {
	/**
	 * @Field @serialVersionUID : TODO(这里用一句话描述这个类的作用)
	 */
	private static final long serialVersionUID = 1L;
	public Long bid_id;
	public String title;
	public String bid_image_url;
	public Long supervisor_id;
	public Integer sort;
	public Date create_time;
	public Date update_time;

}
