package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import models.t_bid_images;
import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;

/**
 * 标的关联的图片信息
 * 
 * @ClassName BidImages
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年9月27日 下午3:54:15
 * @version 1.0.0
 */
public class BidImages implements Serializable {
	/**
	 * @Field @serialVersionUID : TODO(这里用一句话描述这个类的作用)
	 */
	private static final long serialVersionUID = 1L;
	public Long id;
	public Long bid_id;
	public String title;
	public String bid_image_url;
	public Long supervisor_id;
	public Integer sort;
	public Date create_time;
	public Date update_time;

	public Long getBid_id() {
		return bid_id;
	}

	/**
	 * 通过bid_id 查询 关联的 图片信息
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param bid_id
	 * @return
	 * @author: zj
	 */
	public static List<BidImages> getBidImagesByBidId(Long bid_id) throws Exception {
		List<Map<String, Object>> bidImages = new ArrayList<Map<String, Object>>();
		List<BidImages> list = new ArrayList<>();
		String sql = "select id,bid_id,title,bid_image_url from t_bid_images where bid_id = ? order by sort asc ";
		List<Object> p = new ArrayList<Object>();
		p.add(bid_id);
		bidImages = JPAUtil.getList(new ErrorInfo(), sql, p.toArray());
		Logger.info("通过bid_id 查询 关联的 图片信息接收的参数 sql:=========" + sql + "bid_id:===" + bid_id + "=================");
		for (int i = 0; i < bidImages.size(); i++) {
			BidImages bidImages2 = new BidImages();
			JSONObject jsonObject = new JSONObject(bidImages.get(i));
			bidImages2.id = jsonObject.getLong("id");
			bidImages2.title = jsonObject.getString("title");
			bidImages2.bid_image_url = jsonObject.getString("bid_image_url");
			list.add(bidImages2);
		}
		return list;
	}

	public void setId() {

	}

	/**
	 * 新增标的图片
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param bidImages
	 * @throws Exception
	 * @author: zj
	 */
	public static void saveBidImages(BidImages bidImages) throws Exception {
		t_bid_images bid_images = new t_bid_images();
		bid_images.bid_id = bidImages.bid_id;
		bid_images.title = bidImages.title;
		bid_images.bid_image_url = bidImages.bid_image_url;
		bid_images.supervisor_id = bidImages.supervisor_id;
		bid_images.sort = bidImages.sort;
		bid_images.create_time = bidImages.create_time;
		bid_images.update_time = bidImages.update_time;
		bid_images.save();
	}
}