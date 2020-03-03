package business;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_system_options;
import models.t_user_vip_records;
import models.v_user_for_details;

import org.apache.commons.lang.StringUtils;

import payment.PaymentProxy;
import play.Logger;
import play.cache.Cache;
import play.db.helper.JpaHelper;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import constants.Constants;
import constants.DealType;
import constants.IPSConstants;
import constants.OptionKeys;
import constants.Templets;
import constants.UserEvent;
import constants.Constants.RechargeType;

public class Vip implements Serializable{

	public long id;
	public long userId;
	public Date time;
	public Date startTime;
	public Date endTime;
	public boolean status;
	public int serviceTime;
	public boolean isPay;//是否已支付vip费用
	
	/**
	 * 
	 * @param time 申请vip的时长，月为单位
	 * @param user
	 * @param info
	 * @return
	 */
	public int renewal(User user, int client, ErrorInfo error) {
		error.clear();
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
		int vipMinTimeType = backstageSet.vipMinTimeType;
		int vipMinTime = backstageSet.vipMinTimeLength;
		int vipTimeType = backstageSet.vipTimeType; 
		double vipFee = backstageSet.vipFee;
		
		if(vipMinTimeType != 1) {
			vipMinTime *= 12;
		}
		
		if(this.serviceTime < vipMinTime) {
			error.code = -3;
			error.msg = "vip开通不能少于最少时长";
			
			return error.code;
		}
		
		int timeLen = this.serviceTime;
		
		double fee = 0;
		
		if(vipTimeType == 1){
			fee = vipFee * timeLen;
		}else if(vipTimeType == 0){
			fee = Arith.mul(vipFee, serviceTime / 12);
		}
		
		fee = fee*backstageSet.vipDiscount/10;
		fee = Arith.round(fee, 2);
		
		DataSafety data = new DataSafety();
		
		data.id = user.id;
		
		if(!data.signCheck(error)){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		if(error.code < 0) {
			
			return error.code;
		}
		
		if(Constants.IPS_ENABLE){	
			if (fee > user.balance) {
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "对不起，您可用余额不足";
				
				return error.code;
			}
			
			if (fee > 0) {
				error.code = -1;
				error.msg = "申请vip失败!";
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rechargeType", RechargeType.VIP);
				map.put("serviceTime", timeLen);
				map.put("fee", fee);
				map.put("client", client);
				map.put("userId", user.id);
				PaymentProxy.getInstance().applyVIP(error, Constants.PC, map);

				return error.code;
			}
		}else{
			if (fee > user.balance) {
				error.code = Constants.BALANCE_NOT_ENOUGH;
				error.msg = "对不起，您可用余额不足";
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("rechargeType", RechargeType.VIP);
				map.put("serviceTime", timeLen);
				map.put("fee", fee);
				Cache.set("rechargePay"+user.id, map, IPSConstants.CACHE_TIME);
				
				return error.code;
			}
		}
		return returnRenewal(user.id, serviceTime, client, fee).code;
	}
	/**
	 * 申请vip回调逻辑
	 * @param userId 申请用户id
	 * @param serviceTime 申请时间
	 * @param client 申请来源 app/微信/pc/手机网站
	 * @param fee
	 * @return
	 */
	public static ErrorInfo returnRenewal(long userId, int serviceTime, int client, double fee){
		
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		user.id = userId;

		t_user_vip_records vipRecord = new t_user_vip_records();
		t_user_vip_records record = null;
		
		int rows = 0; 
		
		if(user.vipStatus) {
			try{
				record = t_user_vip_records.find("user_id = ? and status = 1", user.id).first();
				rows = JpaHelper.execute("update t_user_vip_records set status = 0 where user_id = ? and status = 1")
				.setParameter(1,user.id).executeUpdate();
			}catch(Exception e) {
				e.printStackTrace();
				Logger.info("申请vip时，查询系统设置中的vip设置时"+e.getMessage());
				error.code = -1;
				error.msg = "申请vip失败";
				
				return error;
			}
			
			if(rows == 0) {
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "数据未更新，vip申请失败";
				
				return error;
			}
			
			vipRecord.start_time = record.expiry_date;
			vipRecord.expiry_date = DateUtil.dateAddMonth(record.expiry_date, serviceTime);
		}else{
			vipRecord.start_time = new Date();
			vipRecord.expiry_date = DateUtil.dateAddMonth(new Date(), serviceTime);
		}
		
		vipRecord.user_id = user.id;
		vipRecord.time = new Date();
		vipRecord.service_fee = fee;
		vipRecord.status = true;
		vipRecord.client = client;
		
		try{
			JpaHelper.execute("update t_user_vip_records set status = 0 where user_id = ? and status = 1").setParameter(1,user.id).executeUpdate();
			vipRecord.save();
			rows = JpaHelper.execute("update t_users set vip_status = true where id = ?", user.id).executeUpdate();
		}catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("申请vip时，查询系统设置中的vip设置时"+e.getMessage());
			error.code = -5;
			error.msg = "申请vip失败";
			
			return error;
		}
		
		if(rows == 0) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据未更新";
			
			return error;
		}		
		//更新用户资金
		DealDetail.minusUserFund(user.id, fee, error);
		if (error.code < 0) {

			return error;
		}
		
		
		DealDetail dealDetail = null;
		v_user_for_details forDetail = DealDetail.queryUserBalance(user.id, error);
		forDetail = DealDetail.queryUserBalance(user.id, error);
		
		/* 添加交易记录 */
		dealDetail = new DealDetail(user.id, DealType.CHARGE_VIP, fee,
				vipRecord.id, forDetail.user_amount, forDetail.freeze, forDetail.receive_amount, "vip扣费");

		dealDetail.addDealDetail(error);
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error;
		}
		DataSafety data = new DataSafety();
		data.updateSignWithLock(user.id, error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error;
		}
		
		DealDetail.addPlatformDetail(DealType.VIP_FEE, vipRecord.id, user.id, -1,
				DealType.ACCOUNT, fee, 1, "vip费用", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error;
		}
		
		DealDetail.userEvent(user.id, UserEvent.VIP, "申请vip", error);
		
		if(error.code < 0) {
			JPA.setRollbackOnly();
			
			return error;
		}
		
		//vip申请站内信
		TemplateStation station = new TemplateStation();
		station.id = Templets.M_VIP_SUCCESS;
		
		if(station.status) {
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = station.content.replace("vipFee",  DataUtil.formatString( fee));
			 
			letter.sendToUserBySupervisor(error);
		}
		
		//发送邮件
		TemplateEmail email = new TemplateEmail();
		email.id = Templets.E_VIP_SUCCESS;
		
		if(email.status) {
			TemplateEmail.addEmailTask(user.email, email.title, email.content.replace("vipFee",  DataUtil.formatString( fee)));
		}
		
		user.balance = forDetail.user_amount - fee;
		user.vipStatus = true;
		
		User.setCurrUser(user);
		
		error.code = 0;
		error.msg = "申请vip成功！";
		return error;
	}
	/**
	 * 根据时间算出vip费用
	 * @param info
	 * @return
	 */
	public double vipMoney(ErrorInfo error) {
		error.clear();
		
		String sql = "select _value from t_system_options where _key = ? or _key =? or _key = ? order by id";
		List<String> keys = null;
		
		try {
			keys = t_system_options.find(sql, OptionKeys.VIP_MIN_TIME, OptionKeys.VIP_FEE, 
					OptionKeys.VIP_TIME_TYPE).fetch();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("申请vip时，查询系统设置中的vip设置时"+e.getMessage());
			error.code = -1;
			error.msg = "申请vip失败";
			
			return error.code;
		}
		
		if(keys == null || keys.size() == 0) {
			error.code = -2;
			error.msg = "读取系统参数失败";
			
			return error.code;
		}
		
		if(keys.get(2).equals(Constants.YEAR+"")) {
			this.serviceTime *= 12;
		}
		
		int vipMinTime = Integer.parseInt(keys.get(1));
		
		if(this.serviceTime <= vipMinTime) {
			error.code = -3;
			error.msg = "至少开通"+vipMinTime+"月";
			
			return error.code;
		}
		
		double vipFee = Double.parseDouble(keys.get(0));
		double fee = Arith.mul(vipFee, serviceTime);
		
		error.code = 0;
		
		return fee;
	}
	
	/**
	 * 查询用户的vip记录
	 * @param userId
	 * @return
	 */
	public static List<t_user_vip_records> queryVipRecord(long userId, ErrorInfo error) {
		error.clear();
		
		List<t_user_vip_records> vipRecords = null;
		
		try {
			vipRecords = t_user_vip_records.find("user_id = ?", userId).fetch();
		} catch(Exception e) {
			e.printStackTrace();
			Logger.info("查询vip记录时："+e.getMessage());
			
			error.code = -1;
			error.msg = "查询用户的vip记录时出现异常";
			
			return null;
		}
		
		error.code = 0;
		
		return vipRecords;
	}
	
	/**
	 * 定期处理会员过期
	 */
	public static void vipExpiredJob() {
		String sql = "select user_id from t_user_vip_records where status = 1 and expiry_date <= NOW()";
		List<Long> user_ids = null;
		
		try{
			user_ids = t_user_vip_records.find(sql).fetch();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("定时任务处理过期vip时（查询）："+e.getMessage());
			
			return;
		}
		
		if(user_ids == null || user_ids.size() == 0) {
			return;
		}
		
		String idStr = StringUtils.join(user_ids, ",");
		
		String updateSql = "update t_user_vip_records set status = 0 where user_id in ( "+idStr+" )";
			
		int rows = 0; 
		
		try{
			rows = JpaHelper.execute(updateSql).executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("定时任务处理过期vip时（更新vip记录）："+e.getMessage());
			
			return;
		}
		
		if(rows == 0) {
			return;
		}
		
		String updateSql2 = "update t_users set vip_status = 0 where id in ( "+idStr+" )";
		
		try{
			JpaHelper.execute(updateSql2).executeUpdate();
		}catch(Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.info("定时任务处理过期vip时（更新用户vip状态）："+e.getMessage());
			
			return;
		}
	}
	
}
