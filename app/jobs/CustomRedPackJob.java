package jobs;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import business.RedPackageHistory;
import business.User;
import constants.Constants;
import models.t_red_packages_type;
import play.Logger;
import utils.ErrorInfo;
import utils.JPAUtil;

/**
 * 
* Description: 发放自定义优惠券
* @author xinsw
* @date 2017年5月7日
 */
public class CustomRedPackJob extends BaseJob{
	public t_red_packages_type red;
	
	public CustomRedPackJob(t_red_packages_type red){
		this.red = red;
	}
	
	public void doJob(){
		if(!"1".equals(IS_JOB))return;
		if(red == null){
			return;
		}
		Logger.info("--------------[%s]优惠券,开始---------------------",red.typeName);
		
		List<User> lists = null;
		
		while((lists = User.findCustomRedUser(red.id)).size() > 0){
			grantPack(lists,red);
		}
		
		//从已启动任务列表中删除
		CustomRedPackMainJob.ids.remove(red.id);
		
		Logger.info("--------------[%s]优惠券,结束---------------------",red.typeName);
	}
	
	public void grantPack(List<User> lists,t_red_packages_type redPackageType){
		try {
			for(User user : lists){
				String desc = "自定义发放";
				if(isExist(redPackageType.id, user.id)){
					if(Constants.COUPON_TYPE_RED_PACKAGE == redPackageType.coupon_type) {
						desc += "红包";
					} else if(Constants.COUPON_TYPE_RATE == redPackageType.coupon_type) {
						desc += "加息券";
					}
					JPAUtil.transactionBegin();
					RedPackageHistory.sendRedPackage(user, redPackageType,desc);
					JPAUtil.transactionCommit();
				}
			}
			Logger.info("自定义优惠券短信通知成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isExist(long redId,long userId){
		String sql = "select count(1) as count from t_red_packages_history where type_id = ? and user_id = ?";
		
		List<Map<String,Object>> countMap =  JPAUtil.getList(new ErrorInfo(), sql,redId,userId);
		int count = 0;
    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
    		count = ((BigInteger)countMap.get(0).get("count")).intValue();
    	}
		return count == 0 ? true : false;
	}
}
