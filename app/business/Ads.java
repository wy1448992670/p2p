package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import constants.Constants;
import constants.SupervisorEvent;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import models.t_content_advertisements;

/**
 * 广告条
 * 
 * @author zhs
 * @version 6.0
 * @created 2014年3月24日 下午2:27:01
 */
public class Ads implements Serializable {

	public long id;
	private long _id;

	public void setId(long id) {
		t_content_advertisements ads = null;

		try {
			ads = t_content_advertisements.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时,根据ID查询广告条：" + e.getMessage());
			this._id = -1;

			return;
		}

		if (ads == null) {
			this._id = -1;

			return;
		}

		this._id = id;
		this.no = ads.no;
		this.time = new Date();
		this.location = ads.location;// 所在位置
		this.imageFileName = ads.image_filename;
		this.resolution = ads.resolution;// 图片分辨率
		this.fileSize = ads.file_size;// 建议文件大小
		this.fileFormat = ads.file_format;// 文件格式
		this.url = ads.url;
		this.isLinkEnabled = ads.is_link_enabled;
		this.target = ads.target;

		if (this.target == Constants.ONE) {
			this.openTarget = "_self";
		} else {
			this.openTarget = "_blank";
		}
		this.status = ads.is_use;
		this.tag = ads.tag;
		this.txt = ads.txt;
	}

	public long getId() {

		return _id;
	}

	public String no;
	public Date time;
	public String location;// 所在位置
	public String imageFileName;
	public String resolution;// 图片分辨率
	public String fileSize;// 建议文件大小
	public String fileFormat;// 文件格式
	public String url;
	public boolean isLinkEnabled;
	
	public String tag,txt;

	/**
	 * 1 _self 2 _blank
	 */
	public int target;
	public String openTarget;
	public boolean status;

	/**
	 * 根据广告ID查询广告信息
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public static t_content_advertisements queryAdsForId(long adsId,
			ErrorInfo error) {
		t_content_advertisements ads = null;
		error.clear();

		try {
			ads = t_content_advertisements.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑广告条，根据广告ID查询广告信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询广告条失败";
		}

		error.code = 0;

		return ads;
	}

	/**
	 * 编辑广告条
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public int updateAds(long supervisorId, long adsId, ErrorInfo error) {
		error.clear();

		t_content_advertisements ads = null;

		try {
			ads = t_content_advertisements.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑广告条，根据广告ID查询广告信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询广告条失败";

			return error.code;
		}

		ads.no = this.no;//
		ads.resolution = this.resolution;// 图片分辨率
		ads.file_size = "不超过2M";// 建议文件大小
		ads.file_format = this.fileFormat;// 文件格式
		ads.image_filename = this.imageFileName;
		ads.url = this.url;
		ads.is_link_enabled = this.isLinkEnabled;
		ads.target = this.target;
		ads.txt = this.txt;

		try {
			ads.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑广告条，保存编辑的广告信息时：" + e.getMessage());
			error.msg = "保存广告条失败";

			return -1;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_ADS,
				"编辑广告条", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "编辑广告信息成功";

		return 0;
	}

	/**
	 * 改变广告条状态
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public static int updateAdsStatus(long supervisorId, String idStr,
			String statusStr, ErrorInfo error) {
		error.clear();

		if (!NumberUtil.isNumericInt(idStr)) {
			error.code = -1;
			error.msg = "传入广告条参数有误！";

			return error.code;
		}

		if (!NumberUtil.isNumericInt(statusStr)) {
			error.code = -2;
			error.msg = "传入广告条参数有误！";

			return error.code;
		}

		int statusInt = Integer.parseInt(statusStr);

		if (statusInt != 0 && statusInt != 1) {
			error.code = -2;
			error.msg = "传入广告条参数有误！";

			return error.code;
		}

		boolean status = statusInt == 0 ? false : true;
		long adsId = Long.parseLong(idStr);

		String sql = "update t_content_advertisements set is_use = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, !status)
				.setParameter(2, adsId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("启用广告条，更新广告条信息时：" + e.getMessage());
			error.msg = "启用广告条失败";

			return -1;
		}

		if (rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";

			return error.code;
		}

		if (status == false) {
			DealDetail.supervisorEvent(supervisorId,
					SupervisorEvent.OPEN_USE_ADS, "启用广告条使用", error);
		} else {
			DealDetail.supervisorEvent(supervisorId,
					SupervisorEvent.CLOSE_USE_ADS, "暂停广告条使用", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "广告条状态改变成功！";

		return 0;

	}
	
	/**
	 * 改变广告条置顶
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public static int updateAdsTop(String idStr,
			Integer isTop, ErrorInfo error) {
		error.clear();
		
		try {
			t_content_advertisements content_advertisements = t_content_advertisements.findById(Long.parseLong(idStr));
			
			JPAUtil.executeUpdate(error, "update t_content_advertisements set is_top = ? where tag = ?", 0, content_advertisements.tag);
			JPAUtil.executeUpdate(error, "update t_content_advertisements set is_top = ? where id = ?", 1, content_advertisements.id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("启用广告条，更新广告条信息时：" + e.getMessage());
			error.msg = "启用广告条失败";
			
			return -1;
		}
		
		error.code = 0;
		error.msg = "广告条置顶成功！";
		
		return 0;
		
	}

	/**
	 * 暂停广告条
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	// public static int stopAds(long supervisorId, long adsId, ErrorInfo
	// error){
	// error.msg = "";
	//
	// String sql =
	// "update t_content_advertisements set is_use = ? where id = ?";
	// EntityManager em = JPA.em();
	// Query query = em.createQuery(sql).setParameter(1,
	// Constants.FALSE).setParameter(2, adsId);
	//
	// try{
	// query.executeUpdate();
	// }catch(Exception e) {
	// e.printStackTrace();
	// Logger.info("暂停广告条，更新广告条信息时："+e.getMessage());
	// error.msg = "暂停广告条失败";
	//
	// return -1;
	// }
	//
	// error.msg = "暂停用广告条成功";
	//
	// return 0;
	//
	// }

	/**
	 * 查询广告条
	 * 
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public static List<Ads> qureyAds(ErrorInfo error) {
		error.clear();

		List<Ads> ads = new ArrayList<Ads>();
		List<t_content_advertisements> advertisements = new ArrayList<t_content_advertisements>();

		try {
			//advertisements = t_content_advertisements.findAll();
			advertisements = t_content_advertisements.find("from t_content_advertisements order by is_top desc,id").fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询广告条时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询广告条时，出错了！";

			return null;
		}

		Ads myAds = null;

		for (t_content_advertisements myAdvertisements : advertisements) {

			myAds = new Ads();

			myAds._id = myAdvertisements.id;
			myAds.no = myAdvertisements.no;
			myAds.location = myAdvertisements.location;
			myAds.resolution = myAdvertisements.resolution;
			myAds.fileSize = myAdvertisements.file_size;
			myAds.fileFormat = myAdvertisements.file_format;
			myAds.url = myAdvertisements.url;
			myAds.isLinkEnabled = myAdvertisements.is_link_enabled;
			myAds.status = myAdvertisements.is_use;
			myAds.tag = myAdvertisements.tag;

			ads.add(myAds);
		}

		return ads;
	}

	/**
	 * 根据不同位置查询广告条
	 * 
	 * @param location
	 * @param error
	 * @return
	 */
	public static List<t_content_advertisements> queryAdsByLocation(
			String location, ErrorInfo error) {
		error.clear();

		String sql = "select new t_content_advertisements(id, image_filename, url, is_link_enabled,"
				+ " target) from t_content_advertisements where location = ? and is_use = true order by id";

		List<t_content_advertisements> ads = new ArrayList<t_content_advertisements>();

		try {
			ads = t_content_advertisements.find(sql, location).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时：" + e.getMessage());

			return null;
		}

		return ads;
	}
	
	public static List<t_content_advertisements> queryAdsByTag(
			String tag, ErrorInfo error) {
		error.clear();
		
		String sql = "select new t_content_advertisements(id, image_filename, url, is_link_enabled,"
				+ " target, txt) from t_content_advertisements where tag = ? and is_use = true order by is_top desc,id";
		
		List<t_content_advertisements> ads = new ArrayList<t_content_advertisements>();
		
		try {
			ads = t_content_advertisements.find(sql, tag).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("财富资讯查询时：" + e.getMessage());
			
			return null;
		}
		
		return ads;
	}
	
	/**
	 * 根据不同位置查询广告条的文件路径
	 * @param location
	 * @param error
	 * @return
	 */
	public static List<String> queryAdsImageNamesByLocation(
			String location, ErrorInfo error){
		error.clear();
		error.code = -1;
		
		String sql = "select t.image_filename from t_content_advertisements t where t.location = ? and t.is_use = true order by t.id";
		
		List<String> imageNames = new ArrayList<String>();
		
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, location);
		
		try {
			imageNames = query.getResultList();
		} catch (Exception e) {
			Logger.error("查询广告条时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询广告条时出错！";
			
			return null;
		}
		
		error.code = 0;
		
		return imageNames;
	}
}
