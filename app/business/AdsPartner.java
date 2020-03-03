package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import models.t_content_advertisements_partner;
import play.Logger;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import constants.SupervisorEvent;

/**
 * 广告条
 * 
 * @author zhs
 * @version 6.0
 * @created 2014年3月24日 下午2:27:01
 */
public class AdsPartner implements Serializable{

	public long id;
	private long _id;

	public void setId(long id) {
		t_content_advertisements_partner partner = null;

		try {
			partner = t_content_advertisements_partner.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("setId时，根据合作伙伴ID查询时：" + e.getMessage());
			this._id = -1;

			return;
		}

		if (partner == null) {
			this._id = -1;

			return;
		}

		this._id = id;
		this.no = partner.no;
		this.name = partner.name;
		this.location = partner.location;// 所在位置
		this.imageFileName = partner.image_filename;
		this.resolution = partner.resolution;// 图片分辨率
		this.fileSize = partner.file_size;// 建议文件大小
		this.fileFormat = partner.file_format;// 文件格式
		this.url = partner.url;
		this.description = partner.description;
		this.order = partner._order;
	}

	public long getId() {

		return _id;
	}

	public String no;
	public Date time;
	public String name;
	public String imageFileName;
	public String location;// 所在位置
	public String resolution;// 图片分辨率
	public String fileSize;// 建议文件大小
	public String fileFormat;// 文件格式
	public String url;
	public String description;
	public int order;

	/**
	 * 添加合作伙伴
	 * 
	 * @param supervisorId
	 * @param error
	 * @return
	 */
	public int createPartner(long supervisorId, ErrorInfo error) {
		error.clear();

        long count = 0;
		//允许排序号重复，如果重复则按照时间在排序
//		try {
//			count = (int)t_content_advertisements_partner.count("_order = ?", this.order);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Logger.info("查询序号是否存在时："+e.getMessage());
//			error.code = -1;
//			error.msg = "查询序号是否存在时出现异常";
//			
//			return error.code;
//		}
//		
//		if(count > 0){
//			error.code = -1;
//			error.msg = "该序号已经存在";
//			return error.code;
//		}
		
		t_content_advertisements_partner partner = new t_content_advertisements_partner();

		partner.time = new Date();
		partner.name = this.name;

		partner.location = "首页";//所在位置

		partner.image_filename = this.imageFileName;

		partner.resolution = this.resolution;//图片分辨率
		partner.file_size = "不超过2M";//建议文件大小
		partner.file_format = this.fileFormat;//文件格式

		partner.url = this.url;
		partner.description = this.description;
		partner._order = this.order;

		try {
			partner.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("添加作伙伴时，保存合作伙伴时：" + e.getMessage());
			error.code = -1;
			error.msg = "添加作伙伴失败！";

			return error.code;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.ADD_PARTNER,
				"添加合作伙伴", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		this._id = partner.id;

		return 0;

	}

	/**
	 * 编辑合作伙伴
	 * 
	 * @param supervisorId
	 * @param id
	 * @param error
	 * @return
	 */
	public int updatePartner(long supervisorId, long id, ErrorInfo error) {
		error.clear();

		t_content_advertisements_partner partner = null;

		try {
			partner = t_content_advertisements_partner.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑合作伙伴时，根据合作伙伴ID查询信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "编辑合作伙伴失败！";

			return error.code;
		}

		partner.name = this.name;
		partner.image_filename = this.imageFileName;
		partner.resolution = this.resolution;// 图片分辨率
		partner.file_size = "不超过2M";// 建议文件大小
		partner.file_format = this.fileFormat;// 文件格式
		partner.url = this.url;
		partner.description = this.description;
		partner._order = this.order;

		try {
			partner.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑作伙伴时，保存合作伙伴时：" + e.getMessage());
			error.msg = "编辑作伙伴失败！";

			return -1;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.EDIT_PARTNER,
				"编辑合作伙伴", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "合作伙伴编辑成功";

		return 0;
	}

	/**
	 * 删除合作伙伴
	 * 
	 * @param supervisorId
	 * @param id
	 * @param error
	 * @return
	 */
	public static int deletePartner(long supervisorId, String idStr,
			ErrorInfo error) {
		error.clear();

		if (!NumberUtil.isNumericInt(idStr)) {
			error.code = -1;
			error.msg = "传入广告条参数有误！";

			return error.code;
		}

		long id = Long.parseLong(idStr);

		String sql = "delete t_content_advertisements_partner where id = ?";

		try {
			JpaHelper.execute(sql).setParameter(1, id).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("删除合作伙伴时：" + e.getMessage());
			error.msg = "删除合作伙伴失败！";

			return -1;
		}

		DealDetail.supervisorEvent(supervisorId, SupervisorEvent.DEL_PARTNER,
				"删除合作伙伴", error);

		if (error.code < 0) {
			JPA.setRollbackOnly();

			return error.code;
		}

		error.code = 0;
		error.msg = "删除合作伙伴条成功";

		return 0;
	}

	/**
	 * 根据ID查询信息
	 * 
	 * @param supervisorId
	 * @param adsId
	 * @param error
	 * @return
	 */
	public static t_content_advertisements_partner queryPartnerForId(
			long adsId, ErrorInfo error) {

		t_content_advertisements_partner security = null;
		error.clear();

		try {
			security = t_content_advertisements_partner.findById(adsId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("编辑合作伙伴，根据ID查询合作伙伴信息时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询合作伙伴失败";
		}

		error.code = 0;

		return security;
	}

	/**
	 * 查询合作伙伴
	 * 
	 * @param obj
	 */
	public static List<AdsPartner> qureyPartner(String key, ErrorInfo error) {
		error.clear();

		List<AdsPartner> partners = new ArrayList<AdsPartner>();
		List<t_content_advertisements_partner> advertisements = new ArrayList<t_content_advertisements_partner>();

		try {
			if(StringUtils.isBlank(key)) {
				advertisements = t_content_advertisements_partner.findAll();
			}else{
				advertisements = t_content_advertisements_partner.find("name like ?","%"+key+"%").fetch();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查找合作伙伴，查找合作伙伴信息时：" + e.getMessage());
			error.msg = "查找合作伙伴失败";

			return null;
		}

		AdsPartner partner = null;

		for (t_content_advertisements_partner myAdvertisements : advertisements) {

			partner = new AdsPartner();

			partner._id = myAdvertisements.id;
			partner.name = myAdvertisements.name;
			partner.location = myAdvertisements.location;
			partner.resolution = myAdvertisements.resolution;
			partner.fileSize = myAdvertisements.file_size;
			partner.fileFormat = myAdvertisements.file_format;
			partner.url = myAdvertisements.url;
			partner.description = myAdvertisements.description;
			partner.order = myAdvertisements._order;

			partners.add(partner);
		}
		
		error.code = 0;

		return partners;
	}

	/**
	 * 查询合作伙伴
	 * 
	 * @param obj
	 */
	public static List<AdsPartner> qureyPartnerForFront(ErrorInfo error) {
		error.clear();

		List<AdsPartner> partners = new ArrayList<AdsPartner>();
		List<t_content_advertisements_partner> advertisements = new ArrayList<t_content_advertisements_partner>();

		String sql = "select new t_content_advertisements_partner(id, name, image_filename, url"
				+ ") from t_content_advertisements_partner partner order by partner._order, partner.time desc";

		try {
			advertisements = t_content_advertisements_partner.find(sql).fetch(
					1, 6);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查找合作伙伴，查找合作伙伴信息时：" + e.getMessage());
			error.msg = "查找合作伙伴失败";

			return null;
		}

		AdsPartner partner = null;

		for (t_content_advertisements_partner myAdvertisements : advertisements) {

			partner = new AdsPartner();

			partner._id = myAdvertisements.id;
			partner.name = myAdvertisements.name;
			partner.imageFileName = myAdvertisements.image_filename;
			partner.url = myAdvertisements.url;

			partners.add(partner);
		}
		
		error.code = 0;

		return partners;
	}

	public static boolean orderExist(String orderStr, ErrorInfo error) {
		error.clear();

		if (!NumberUtil.isNumericInt(orderStr)) {
			error.code = -2;
			error.msg = "传入参数有误！";

			return true;
		}

		int order = Integer.parseInt(orderStr);

		long count = 0;

		try {
			count = t_content_advertisements_partner.count("_order = ?", order);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("查询排序是否存在时：" + e.getMessage());

			error.code = -2;
			error.msg = "数据库查询失败！";

			return true;
		}

		if (count > 0) {
			error.code = -2;
			error.msg = "该排序已存在";

			return true;
		}
		
		error.code = 0;

		return false;
	}

}
