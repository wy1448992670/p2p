package controllers.supervisor.systemSettings;

import business.RedPackage;
import business.RedPackageHistory;
import constants.CouponTypeEnum;
import controllers.supervisor.SupervisorController;
import models.t_red_packages_type;
import play.mvc.Scope.Session;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;


public class RedPackageAction extends SupervisorController{

	/**
	 * 查询所有 不分页
	 */
	/**
	public static void redTypeList(ErrorInfo error){
		if(null ==error){
			error = new ErrorInfo();
		}
		 PageBean<t_red_packages_type>  pageBean = RedPackage.redTypeList(error);
		 
		 flash.put("error", error.msg);
		 error.clear();
		render(pageBean);
	}*/
	
	/**
	 * 添加
	 */
	public static void toAddDetails(){
		Integer couponFlag=Integer.parseInt(Session.current().get("couponFlag"));
		String couponName=Session.current().get("couponName");
		render(couponFlag,couponName); 
		   
	}
	 
	public static void addDetails(){
		ErrorInfo error = new ErrorInfo();
//		RedPackage.addDetails(params,error);
		RedPackage.addRedPackage(params,error);
		int couponFlag=Integer.parseInt(Session.current().get("couponFlag"));
		redTypeList(couponFlag,error);
	}
	
	/**
	 * 修改
	 */
	public static void details(){
		ErrorInfo error = new ErrorInfo();
		String sign = params.get("sign");
		RedPackage redPack = RedPackage.details(sign, error);
		Integer couponFlag=Integer.parseInt(Session.current().get("couponFlag"));
		String couponName=Session.current().get("couponName");
		render(redPack,couponName,couponFlag);
	}
	
	public static void updateDetails(){
		ErrorInfo error = new ErrorInfo();
//		RedPackage.updateDetails(params, error);
		RedPackage.updateRedPackage(params, error);
		int couponFlag=Integer.parseInt(Session.current().get("couponFlag"));
		redTypeList( couponFlag,error);
	}
	
	
	/**
	 * 禁用
	 * @param sign
	 */
	public static void disableRedType(){
		ErrorInfo error = new ErrorInfo();
		String sign = params.get("sign");
		RedPackage.disableRedType(sign, error);
		renderJSON(error);
	}
	
	/**
	 * 启用
	 * @param sign
	 */
	public static void enableRedType(){
		ErrorInfo error = new ErrorInfo();
		String sign = params.get("sign");
		RedPackage.enableRedType(sign, error);
		renderJSON(error);
	}
	
	/**
	 * 物理删除
	 * @param sign
	 */
	public static void deleteRedType(){
		ErrorInfo error = new ErrorInfo();
		String sign = params.get("sign");
		RedPackage.deleteRedType(sign, error);
		renderJSON(error);
	}
	
	
	 
	/**
	 * 
	 * @author xinsw
	 * @creationDate 2017年4月25日
	 * @description 自动类红包-分页
	 * @param start
	 * @param end
	 * @param type
	 * @param name
	 * @param currPage
	 * @param pageSize
	 * @param orderType
	 * @param export
	 */
	public static void redTypeList(Integer couponFlag,ErrorInfo error){		
		couponFlag=	RedPackageHistory.storeCouponInfo(couponFlag);
		if(null==error) {
			error=new ErrorInfo();
		}
		String start = params.get("start");
		String end = params.get("end");
		String type = params.get("type");
		String name = params.get("name");
		String currPage  = params.get("currPage");
		String pageSize = params.get("pageSize");
		String orderIndexStr = params.get("orderIndex"); // 排序索引
		String orderStatusStr = params.get("orderStatus"); // 升降标示
		String couponName=RedPackageHistory.couponName;
		
		int orderIndex = NumberUtil.isNumericInt(orderIndexStr)? Integer.parseInt(orderIndexStr): 0;
		int orderStatus = NumberUtil.isNumericInt(orderStatusStr)? Integer.parseInt(orderStatusStr): 0;
		PageBean<t_red_packages_type>  pageBean = RedPackage.queryPage(start, end, type, name, currPage, pageSize, orderIndex,orderStatus,error);

		flash.put("error", error.msg);
		error.clear();

		render(pageBean, start, end, type, name,currPage,pageSize, orderIndex,orderStatus,couponFlag,couponName);
	}
	
}
