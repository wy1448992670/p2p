package controllers.supervisor.webContentManager;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;
import models.t_content_advertisements_links;
import constants.Constants;
import constants.OptionKeys;
import controllers.supervisor.SupervisorController;
import business.Ads;
import business.AdsEnsure;
import business.AdsPartner;
import business.BottomLinks;
import business.Supervisor;
import utils.ErrorInfo;
import utils.NumberUtil;

/**
 * 广告内容管理
 * 
 * @author zhs
 * 
 */
public class AdManageAction extends SupervisorController {
	/**
	 * 广告条管理
	 */
	public static void bannermanagement(String tag) {
		ErrorInfo error = new ErrorInfo();
		
		List<Ads> ads = Ads.qureyAds( error);
		
		List<Ads> ads_ = new ArrayList<Ads>();
		tag = StringUtils.isNotBlank(tag) ? tag : "APPBanner";
		
		if(ads != null) {
			for(int i = 0; i < ads.size(); i++) {
				if(tag.equalsIgnoreCase(ads.get(i).tag))
					ads_.add(ads.get(i));
			}
		}
		ads = ads_;
		

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(ads, tag);
	}
	
//	/**
//	 * 上传图片
//	 */
//	public static void upload(String path) {
//
//		if (null == path)
//			return;
//
//		/* 上传且得到路径 */
//		String src = FileUtil.uploadByPath(path);
//
//		JSONObject json = new JSONObject();
//		/* 保存路径 */
//		json.put("src", src);
//		/* 回调返回 */
//		renderJSON(json.toString());
//	}

	/**
	 * 根据广告ID查询广告信息
	 * @param adsId
	 */
	public static void queryAds(long adsId){
		
		Ads ads = new Ads();
		ads.id = adsId;
		render(ads);
	}
	
	
	/**
	 * 广告条编辑
	 */
	public static void editBanner(long adsId,String no, String filename, String type, String resolution, int target
			,String url, String txt) {
		boolean isLinkEnabled = false;
		String linkEnabled = params.get("isLinkEnabled");
		int temp = Integer.parseInt(linkEnabled);
		
		Ads ads = new Ads();
		ads.id = adsId;
		
		if(temp == 1){
			isLinkEnabled = true;
		}
		if(adsId <= 0) {
			flash.error("传入参数有误"); 
			bannermanagement(ads.tag);
		}


		if (type == null) {
			flash.error("图片格式不符合要求");
		}
		if (StringUtils.isBlank(filename)) {
			flash.error("请选择上传图片");
			bannermanagement(ads.tag);
		}
		if (StringUtils.isBlank(no)) {
			flash.error("编号不能为空");
			bannermanagement(ads.tag);
		}
		
		if (StringUtils.isBlank(resolution)) {
			flash.error("请选择上传图片");
			bannermanagement(ads.tag);
		}
		
		if(isLinkEnabled) {
			
			if (StringUtils.isBlank(url)) {
				flash.error("请填写链接地址");
				bannermanagement(ads.tag);
			}
			if(url.indexOf("http://")!= 0 && url.indexOf("https://")!= 0){
				flash.error("请填写完整的地址，包含：http://或https://");
				bannermanagement(ads.tag);
				
			}
			if(target != 1 && target != 2) {
				flash.error("请选择打开方式");
				bannermanagement(ads.tag);
			}
			
			ads.url = url;
			ads.target = target;
		}

		ErrorInfo error = new ErrorInfo();
		
		ads.no = no;
		ads.imageFileName = filename;
		ads.resolution = resolution;
		ads.fileFormat = type;
		ads.isLinkEnabled = isLinkEnabled;
		ads.url = url;
		ads.txt = txt;
		
		Supervisor supervisor = Supervisor.currSupervisor();
		ads.updateAds(supervisor.id, adsId, error);

		flash.success(error.msg);

		bannermanagement(ads.tag);
		
	}

//	/**
//	 * 广告条搜索
//	 */
//	public static void serchBanner() {
//		render();
//	}

	/**
	 * 改变广告条状态
	 */
	public static void updateBanner(String idStr, String statusStr, Integer isTop) {
		ErrorInfo error = new ErrorInfo();
		
		if(isTop != null) {
			Ads.updateAdsTop(idStr, isTop, error);
			renderJSON(error);
		}
		
		Supervisor supervisor = Supervisor.currSupervisor();
		Ads.updateAdsStatus(supervisor.id, idStr, statusStr, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 根据ID查询4大安全保障信息
	 * @param adsId
	 */
	public static void securityEditor(long adsId){
		AdsEnsure security = new AdsEnsure();
		security.id = adsId;
		render(security);
	}

	/**
	 * 4大安全保障
	 */
	public static void security() {
		ErrorInfo error = new ErrorInfo();
		
		List<AdsEnsure> adsEnsure = AdsEnsure.queryAdsEnsure( error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		render(adsEnsure);
	}

//	/**
//	 * 安全保障搜索
//	 */
//	public static void serchSecurity() {
//		render();
//	}

	/**
	 * 暂停/启用安全保障内容
	 */
	public static void updateSecurityStatus(String idStr, String statusStr) {
		ErrorInfo error = new ErrorInfo();
		
		Supervisor supervisor = Supervisor.currSupervisor();
		AdsEnsure.updateAdsEnsureStatus(supervisor.id, idStr, statusStr, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 保存安全保障编辑
	 */
	public static void SaveSecurity() {
		
		ErrorInfo error = new ErrorInfo();
		AdsEnsure ads = new AdsEnsure();
		
		String type = params.get("type");
		String location = params.get("ensureLocation");
		String title = params.get("ensureTitle");
		String adsId = params.get("ensureId");
		String isLinkEnabled = params.get("isLinkEnabled");
		String url = "";
		String target = "";
		String resolution = params.get("resolution");
		String filePath = params.get("filename");
		String status = params.get("status");
		
		if (type == null) {
			flash.error("图片格式不符合要求");
			security();
		}
		
		if(StringUtils.isBlank(title)){
			flash.error("标题不能为空");
			security();
		}
		
		if(!NumberUtil.isNumeric(adsId)){
			flash.error("id类型错误");
			security();
		}
		
		if(!NumberUtil.isNumeric(isLinkEnabled)){
			flash.error("传入是否启用超链接数据类型错误");
			security();
		}
		
		if(!NumberUtil.isNumericInt(isLinkEnabled)){
			flash.error("传入是否启用数据类型错误");
			security();
		}
		
		if(Integer.parseInt(isLinkEnabled) == 1){
			ads.isLinkEnabled = true;
			url = params.get("url");
			target = params.get("target");
			
			if(StringUtils.isBlank(url)){
				flash.error("链接地址不能为空");
				security();
			}
			if(url.indexOf("http://") != 0 && url.indexOf("https://") != 0){
				flash.error("请填写完整的地址，包含:http://或htpps://");
				security();
				
			}
			
			if(!NumberUtil.isNumericInt(target)){
				flash.error("传入链接打开方式数据类型错误");
				security();
			}
			
			ads.url = url;
			ads.target = Integer.parseInt(target);
			
		}else{
			ads.isLinkEnabled = false;
		}
		
		if(StringUtils.isBlank(resolution)){
			flash.error("传入图片分辨率数据类型错误");
			security();
		}
		
		if(StringUtils.isBlank(filePath)){
			flash.error("传入图片地址不能为空");
			security();
		}
		
		if(StringUtils.isBlank(location)){
			flash.error("传入图片位置不能为空");
			security();
		}
		
		ads.fileSize = "不超过2M";
		ads.title = title;
		ads.location = location;
		ads.resolution = resolution;// 图片分辨率
		ads.fileFormat = type;// 文件格式
		ads.imageFileName = filePath;
		ads.status = StringUtils.isBlank(status) ? true : Boolean.parseBoolean(status);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		ads.updateAdsEnsure(supervisor.id, Long.parseLong(adsId), error);

		flash.success(error.msg);

		security();
	}

	
	
	/**
	 * 合作伙伴
	 */
	public static void partner() {
		
		ErrorInfo error = new ErrorInfo();
		
		String key = params.get("partnerName");
		
		List<AdsPartner> partners = AdsPartner.qureyPartner(key, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		render(partners);
	}

//	/**
//	 * 搜索合作伙伴
//	 */
//	public static void serchPartner() {
//		
//		render();
//	}
	
	/**
	 * 进入编辑合作伙伴页面
	 * @param adsId
	 */
	public static void partnerEditor(long adsId){
		
		AdsPartner partner = new AdsPartner();
		partner.id = adsId;
		render(partner);
	}

	/**
	 * 编辑合作伙伴
	 */
	public static void editPartner() {
		
		ErrorInfo error = new ErrorInfo();
		AdsPartner partner = new AdsPartner();
		
		String type = params.get("type");
		String size = "不超过2M";
		String id = params.get("partnerId");
		String name = params.get("name");
		String url = params.get("url");
		String description = params.get("description");
		String order = params.get("order");
		String resolution = params.get("resolution");
		String filePath = params.get("filename");
		
		if(StringUtils.isBlank(id)){
			flash.error("数据有误!");
			
			partner();
		}
		
		if(url.indexOf("http://") != 0 && url.indexOf("https://") != 0) {
			flash.error("请填写完整的地址，包含:http://或htpps://");
			
			partner();
		}
		
		if (StringUtils.isBlank(type)) {
			flash.error("图片格式不符合要求");
			partner();
		}
		
		if (StringUtils.isBlank(name)) {
			flash.error("名称不能为空");
			partner();
		}
		
		if (StringUtils.isBlank(order)) {
			flash.error("排序不能为空");
			partner();
		}
		
		if (StringUtils.isBlank(description)) {
			flash.error("描述不能为空");
			partner();
		}
		
		partner.fileSize = size;
		partner.name = name;
		partner.resolution = resolution;// 图片分辨率
		partner.fileFormat = type;// 文件格式
		partner.imageFileName = filePath;
		partner.url = url;
		partner.description = description;
		partner.order = Integer.parseInt(order);
		
		Supervisor supervisor = Supervisor.currSupervisor();
		
		partner.updatePartner(supervisor.id, Long.parseLong(id), error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}

		flash.success(error.msg);

		partner();
	}

	/**
	 * 删除合作伙伴
	 */
	public static void deletPartner(String idStr) {
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		Supervisor supervisor = Supervisor.currSupervisor();
		AdsPartner.deletePartner(supervisor.id, idStr, error);
		
        json.put("error", error);
		
		renderJSON(json);
   }

	/**
	 * 增加合作伙伴
	 */
	public static void increasedPartner(String addPartner, String imageFileName, String addUrl, int addOrder, String addDescription,
			String imageResolution, String imageSize, String imageType) {
		
		if(addUrl.indexOf("http://") != 0 && addUrl.indexOf("https://") != 0) {
			flash.error("请填写完整的地址，包含:http://或htpps://");
			
			partner();
		}
		
		ErrorInfo error = new ErrorInfo();
		AdsPartner partner = new AdsPartner();

		partner.name = addPartner;
		partner.imageFileName = imageFileName;
		partner.url = addUrl;
		partner.description = addDescription;
		partner.order = addOrder;
		partner.resolution = imageResolution;
		partner.fileSize = imageSize;
		partner.fileFormat = imageType;
		
		Supervisor supervisor = Supervisor.currSupervisor();
		partner.createPartner(supervisor.id, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
		}else{
			flash.success(error.msg);
		}

		partner();
	}
	
//	/**
//	 * 判断排序是否存在
//	 * @param orderStr
//	 */
//	public static void verifyOrderForPartner(String orderStr) {
//		ErrorInfo error = new ErrorInfo();
//		AdsPartner.orderExist(orderStr, error);
//		
//		render(error);
//		
//		
//	}

	/**
	 * 底部链接管理
	 */
	public static void bottomLinkManager() {
		String key = params.get("key");
		key = (key == null || key.equals("")) ? OptionKeys.LABLE_BEGINNER_INTRODUCTION
				: key;

		List<t_content_advertisements_links> bottomLinks = BottomLinks
				.queryBottomLinksByKey(key);

		render(bottomLinks, key);
	}

	/**
     * 更新链接的显示状态
     * @param id 主键
     * @param status 状态
     */
	public static void updateBottomLinkStatus(String id, String status){
		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();
		
		BottomLinks.updateBottomLinkStatus(id, status, error);
		
		
		json.put("error", error);
		
		renderJSON(json);
	}

	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年4月2日
	 * @description 底部-友情连接添加
	 * @param title
	 * @param url
	 * @param target
	 * @param key
	 * @param order
	 */
	public static void bottomManagerAdd(String title, String url,
			int target, String key, int order) {
		ErrorInfo error = new ErrorInfo();
		BottomLinks bottomLink = new BottomLinks();
		
		if(target !=1 && target != 2) {
			error.msg = "请传入正确的参数";
			error.code = -1;
			
			renderJSON(error);
		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "请输入标题";
			error.code = -1;
			
			renderJSON(error);
		}
		
		if(!OptionKeys.LABLE_CUSTOMER_SUPPORT.equals(key)) {
			if(StringUtils.isBlank(url)) {
				error.msg = "请填写链接地址";
				error.code = -1;
				
				renderJSON(error);
			}
		}

		bottomLink.title = title;
		bottomLink.url = url;
		bottomLink.target = target;
		bottomLink.key = key;
		bottomLink.order = order;
		
		bottomLink.AddBottomLink(error);

		renderJSON(error);
	}
	
	/**
	 * 底部链接管理编辑
	 */
	public static void bottomManagerEditor(long id, String title, String url,
			int target, String key, int order) {
		ErrorInfo error = new ErrorInfo();
		BottomLinks bottomLink = new BottomLinks();
		
		if(target !=1 && target != 2) {
			error.msg = "请传入正确的参数";
			error.code = -1;
			
			renderJSON(error);
		}
		
		if(StringUtils.isBlank(title)) {
			error.msg = "请输入标题";
			error.code = -1;
			
			renderJSON(error);
		}
		
		if(!OptionKeys.LABLE_CUSTOMER_SUPPORT.equals(key)) {
			if(StringUtils.isBlank(url)) {
				error.msg = "请填写链接地址";
				error.code = -1;
				
				renderJSON(error);
			}
		}

		bottomLink.title = title;
		bottomLink.url = url;
		bottomLink.target = target;
		bottomLink.key = key;
		bottomLink.order = order;
		
		bottomLink.updateBottomLink(id, error);

		renderJSON(error);
	}
	
	/**
	 * 校验order的唯一性
	 * 
	 * @param key
	 * @param order
	 */
	public static void verifyOrder(long id, String key, int order) {
		ErrorInfo error = new ErrorInfo();

		BottomLinks.verifyOrder(id, key, order, error);

		render(error);
	}
}
