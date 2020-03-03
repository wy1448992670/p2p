package bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * app首页满标中的借款标
 * @author mingjian
 *
 */
@Entity
public class FullBidsApp implements Serializable{
	@Id
	public Long id;
	public String bid_image_filename;
	public Double has_invested_amount;
	public Double apr;
	public Integer num;
}
