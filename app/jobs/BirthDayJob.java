package jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.t_red_packages_type;
import models.t_users;
import constants.Constants;
import business.Invest;
import business.RedPackage;
import business.RedPackageHistory;
import business.User;
import business.Vip;
import play.Logger;
import play.jobs.Job;
import play.jobs.On;
import reports.StatisticAuditItems;
import reports.StatisticBorrow;
import reports.StatisticDebt;
import reports.StatisticInvest;
import reports.StatisticInvitation;
import reports.StatisticMember;
import reports.StatisticProduct;
import reports.StatisticRecharge;
import reports.StatisticSecurity;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;


/**
 * 
* Description:每天执行，为过生日用户发送生日优惠券(红包+加息券)
* @author xinsw
* @date 2017年5月6日
 */
@On("0 0 1 * * ?")
public class BirthDayJob extends BaseJob{
	
	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		Logger.info("--------------生日优惠券,开始---------------------");
		String date = DateUtil.dateToString(new Date(),"MMdd");
		List<t_users> users = User.findBirthDay(date);
		if(users == null || users.size() == 0){
			return;
		}
		String redTypeName = Constants.RED_PACKAGE_TYPE_BIRTHDAY;//类型
		
		long status  = Constants.RED_PACKAGE_TYPE_STATUS_ENABLED;//启用状态
		
		
		List<t_red_packages_type> reds = RedPackage.getRegRed(redTypeName, status);
		if(null != reds && reds.size() > 0){
			for(t_red_packages_type redPackageType : reds){
				for(t_users u : users){
					String desc = "生日发放";
					if(RedPackageHistory.isExist(redPackageType.id, u.id)){
						User user = new User();
						user.id = u.id;
						JPAUtil.transactionBegin();
						if(Constants.COUPON_TYPE_RED_PACKAGE == redPackageType.coupon_type) {
							desc += "红包";
						} else if(Constants.COUPON_TYPE_RATE == redPackageType.coupon_type) {
							desc += "加息券";
						}
						RedPackageHistory.sendRedPackage(user, redPackageType,desc);
						JPAUtil.transactionCommit();
					}
				}
			}
			Logger.info("生日发放优惠券短信通知成功");
		}	
		Logger.info("--------------生日优惠券,结束---------------------");
	}
}
