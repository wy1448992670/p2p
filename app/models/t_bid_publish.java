package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import play.db.jpa.Model;


/**
 * @author l'
 * @ 2015年12月28日
 */
@Entity
public class t_bid_publish extends Model {

	public String bid_title;//借款标题
	
	public int product_id;//借款标产品类型id
	
	public String product_name;//产品名称
	
	public double amount;//借款金额
	
	public int period_unit;//借款期限单位
	
	public int period;//借款期限
	
	public double apr;//年利率
	
	public int repayment_type_id;//还款方式id
	
	public String repayment_type_name;//还款方式名称
	
	public Date publish_time;//公告发布时间
	
	public Date create_time;//创建时间
	
}
