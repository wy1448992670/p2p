package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.t_content_advertisements_ensure;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import constants.Constants;
import constants.SupervisorEvent;

/**
 * 广告条
 * 
 * @author zhs
 * @version 6.0
 * @created 2014年3月24日 下午2:27:01
 */
public class AdsEnsure implements Serializable{

	public long id;
	private long _id;

	public void setId(long id) {
		t_content_advertisements_ensure ensure = null;

		try {
			ensure = t_content_advertisements_ensure.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时，根据安全保障ID查询时：" + e.getMessage());
			this._id = -1;

			return;
		}

		if (ensure == null) {
			this._id = -1;

			return;
		}

		this._id = id;
		this.time = new Date();
		this.title = ensure.title;
		this.location = ensure.location;// 所在位置
		this.resolution = ensure.resolution;// 图片分辨率
		this.fileSize = ensure.file_size;// 建议文件大小
		this.fileFormat = ensure.file_format;// 文件格式
		this.url = ensure.url;
		this.isLinkEnabled = ensure.is_link_enabled;
		this.target = ensure.target;
		this.imageFileName = ensure.image_filename;

		if (this.target == Constants.ONE) {
			this.openTarget = "_self";
		} else {
			this.openTarget = "_blank";
		}
		this.status = ensure.is_use;
	}

	public long getId() {

		return _id;
	}

	public Date time;
	public String title;
	public String location;// 所在位置
	public String imageFileName;
	public String resolution;// 图片分辨率
	public String fileSize;// 建议文件大小
	public String fileFormat;// 文件格式
	public String url;
	public boolean isLinkEnabled;

	/**
	 * 1 _self 2 _blank
	 */
	public int target;
	public String openTarget;
	public boolean status;// 是否启用

	/**
	 * 编辑安全保障
	 * 
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public int updateAdsEnsure(long supervisorId, long adsId, ErrorInfo error) {
		error.clear();

		t_content_advertisements_ensure ensure = null;

		try {
			ensure = t_content_advertisements_ensure.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑广告条，根据广告ID查询广告信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询广告条失败";

			return error.code;
		}

		ensure.title = this.title;
		ensure.location = this.location;
		ensure.file_size = "不超过2M";
		ensure.resolution = this.resolution;
		ensure.file_size = this.fileSize;
		ensure.file_format = this.fileFormat;
		ensure.url = this.url;
		ensure.is_link_enabled = this.isLinkEnabled;
		ensure.target = this.target;
		ensure.image_filename = this.imageFileName;
		ensure.is_use = this.status;

		try {
			ensure.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑安全保障，保存编辑安全保障信息时：" + e.getMessage());
			error.msg = "编辑安全保障失败";

			return -1;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_ENSURE,
				"编辑安全保障", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;

		}

		error.code = 0;
		error.msg = "编辑安全保障成功";

		return 0;
	}

	/**
	 * 根据ID查询4大安全保障信息
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public static t_content_advertisements_ensure querySecurityForId(
			long adsId, ErrorInfo error) {
		t_content_advertisements_ensure security = null;
		error.clear();

		try {
			security = t_content_advertisements_ensure.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑4大安全保障，根据ID查询4大安全保障信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询4大安全保障失败";
		}

		error.code = 0;

		return security;
	}

	/**
	 * 启用安全保障
	 * 
	 * @param supervisorId
	 * @param id
	 * @param error
	 * @return
	 */
	public static int updateAdsEnsureStatus(long supervisorId, String idStr,
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

		String sql = "update t_content_advertisements_ensure set is_use = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, !status)
				.setParameter(2, adsId);

		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("更新安全保障，更新安全保障信息时：" + e.getMessage());
			error.msg = "更新安全保障失败";

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
					SupervisorEvent.OPEN_USE_ENSURE, "启用四大安全保障使用", error);
		} else {
			DealDetail.supervisorEvent(supervisorId,
					SupervisorEvent.CLOSE_USE_ENSURE, "关闭四大安全保障使用", error);
		}

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "更新安全保障成功";

		return 0;
	}

	/**
	 * 暂停安全保障
	 * 
	 * @param supervisorId
	 * @param id
	 * @param error
	 * @return
	 */
	// public static int stopAdsEnsure(long supervisorId, long ensureId,
	// ErrorInfo error){
	// error.msg = "";
	//
	// String sql =
	// "update t_content_advertisements_ensure set is_use = ? where id = ?";
	// EntityManager em = JPA.em();
	// Query query = em.createQuery(sql).setParameter(1,
	// Constants.FALSE).setParameter(2, ensureId);
	//
	// try{
	// query.executeUpdate();
	// }catch(Exception e) {
	// e.printStackTrace();
	// Logger.info("暂停安全保障，更新安全保障信息时："+e.getMessage());
	// error.msg = "暂停安全保障失败";
	//
	// return -1;
	// }
	//
	// error.msg = "暂停安全保障成功";
	//
	// return 0;
	// }

	/**
	 * 查询安全保障
	 * 
	 * @return
	 */
	public static List<AdsEnsure> queryAdsEnsure(ErrorInfo error) {
		error.clear();

		List<AdsEnsure> adsEnsures = new ArrayList<AdsEnsure>();
		List<t_content_advertisements_ensure> ensure = new ArrayList<t_content_advertisements_ensure>();

		try {
			ensure = t_content_advertisements_ensure.findAll();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查找安全保障，查找安全保障信息时：" + e.getMessage());
			error.msg = "保存安全保障失败";

			return null;
		}

		AdsEnsure adsEnsure = null;

		for (t_content_advertisements_ensure myAdvertisements : ensure) {

			adsEnsure = new AdsEnsure();

			adsEnsure._id = myAdvertisements.id;
			adsEnsure.title = myAdvertisements.title;
			adsEnsure.location = myAdvertisements.location;
			adsEnsure.resolution = myAdvertisements.resolution;
			adsEnsure.fileSize = myAdvertisements.file_size;
			adsEnsure.fileFormat = myAdvertisements.file_format;
			adsEnsure.url = myAdvertisements.url;
			adsEnsure.isLinkEnabled = myAdvertisements.is_link_enabled;
			adsEnsure.status = myAdvertisements.is_use;

			adsEnsures.add(adsEnsure);
		}
		
		error.code = 0;

		return adsEnsures;
	}

	public static List<AdsEnsure> queryEnsureForFront(ErrorInfo error) {
		error.clear();

		List<AdsEnsure> adsEnsures = new ArrayList<AdsEnsure>();
		List<t_content_advertisements_ensure> ensure = new ArrayList<t_content_advertisements_ensure>();

		String sql = "select new t_content_advertisements_ensure(id, title, url, image_filename,"
				+ "is_link_enabled, target) from t_content_advertisements_ensure ensure "
				+ "where ensure.is_use = 1";
		try {
			ensure = t_content_advertisements_ensure.find(sql).fetch(1, 4);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查找安全保障，查找安全保障信息时：" + e.getMessage());
			error.msg = "保存安全保障失败";

			return null;
		}

		AdsEnsure adsEnsure = null;

		for (t_content_advertisements_ensure myAdvertisements : ensure) {

			adsEnsure = new AdsEnsure();

			adsEnsure._id = myAdvertisements.id;
			adsEnsure.title = myAdvertisements.title;
			adsEnsure.imageFileName = myAdvertisements.image_filename;
			adsEnsure.url = myAdvertisements.url;
			adsEnsure.isLinkEnabled = myAdvertisements.is_link_enabled;
			adsEnsure.target = myAdvertisements.target;

			adsEnsures.add(adsEnsure);
		}
		
		error.code = 0;

		return adsEnsures;
	}
}
