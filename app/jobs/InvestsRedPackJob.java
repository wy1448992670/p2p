package jobs;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import business.Invest;
import business.RedPackage;
import business.RedPackageHistory;
import business.User;
import models.t_red_packages_type;
import models.t_users;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import utils.DateUtil;
import utils.JPAUtil;

/**
 * 
* Description: 发放累计投资优惠券
* @author xinsw
* @date 2017年5月7日
 */
@On("0 0 1 * * ?")
public class InvestsRedPackJob extends BaseJob{
	
	public void doJob(){
		if(!"1".equals(IS_JOB))return;
		
		Logger.info("--------------累计投资优惠券,开始---------------------");
		Date today = new Date();
		//查询结束日为昨天的累计投资优惠券
		String date = DateUtil.getDate(today, "yyyy-MM-dd", -1);
		List<t_red_packages_type> redPacks = RedPackage.findInvestsRedPack(date);
		if(redPacks == null || redPacks.size() == 0){
			return;
		}
		for(t_red_packages_type red : redPacks){
			grantPack(red);
		}
		Logger.info("--------------累计投资优惠券,结束---------------------");
	}
	
	public void grantPack(t_red_packages_type redPackageType){
		try {
			JSONObject json = JSONObject.parseObject(redPackageType.rules);
			String start = json.getString("start");
			String end = json.getString("end");
			List<Map<String,Object>> lists = Invest.findInvestsForRedPack(start, end, redPackageType.obtain_money);
			if(lists == null || lists.size() == 0){
				return;
			}
			for(Map<String,Object> map : lists){
				String desc = "累计投资发放";
				Long userId = ((BigInteger) map.get("user_id")).longValue();
				User user = new User();
				user.id = userId;
				if(Constants.COUPON_TYPE_RED_PACKAGE == redPackageType.coupon_type) {
					desc += "红包";
				} else if(Constants.COUPON_TYPE_RATE == redPackageType.coupon_type) {
					desc += "加息券";
				}
				JPAUtil.transactionBegin();
				RedPackageHistory.sendRedPackage(user, redPackageType,desc);
				JPAUtil.transactionCommit();
			}
			Logger.info("累计投资优惠券短信通知成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
