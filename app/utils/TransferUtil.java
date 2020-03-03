package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TransferUtil {

	/**
	 * 判断时间是否处于中间（即上个还款日与下个还款日中间）
	 * @author wangyun
	 * 2018年7月2日 
	 * @description
	 */
	public static boolean isDaysBetween(Date receiveTime){
		Calendar lastMonth = Calendar.getInstance();
		//获取还款时间上一个月时间
		lastMonth.setTime(receiveTime);     
		lastMonth.add(Calendar.MONTH, -1);//即一个月前的时间     
		
		Calendar now = Calendar.getInstance();//现在时间
		now.setTime(new Date());       
		
		Calendar receiveTime_ = Calendar.getInstance();//获取还款时间
		receiveTime_.setTime(receiveTime); 
		
		//判断当前时间是否在中间
		if(lastMonth.before(now) && now.before(receiveTime_)) {
			return true;
			
		} else {
			return false;
		}
	}
	
	/**
	 * 可债权转让天数
	 * @author wangyun
	 * 2018年7月2日 
	 * @description
	 */
	public static int transferDebtDays(Date receiveTime){
		Calendar lastMonth = Calendar.getInstance();
		//获取还款时间上一个月时间
		lastMonth.setTime(receiveTime);     
		lastMonth.add(Calendar.MONTH, -1);//即一个月前的时间     
		
		Calendar now = Calendar.getInstance();//现在时间
		now.setTime(new Date());       
		
		Calendar receiveTime_ = Calendar.getInstance();//获取还款时间上一个月时间
		receiveTime_.setTime(receiveTime); 
		
		int remainDay = 0;
		//判断当前时间是否在中间
		if(lastMonth.before(now) && now.before(receiveTime_)) { //是，则返回当期剩余天数
		    remainDay = (int)((receiveTime_.getTimeInMillis() - now.getTimeInMillis())/(24*60*60*1000));
			
		} else {//否则返回整月天数
			 remainDay = (int)((receiveTime_.getTimeInMillis() - lastMonth.getTimeInMillis())/(24*60*60*1000));
		}
		
		return remainDay;
	}
	/**
	 * 每一期的债权金额（可能不足月，按比例算金额）
	 * @author wangyun
	 * 2018年6月29日 
	 * receiveTime 还款时间
	 * corpus 本金
	 * @description 
	 */
	public static double transferAmount(Date receiveTime, double amount){
		double remainAmt = 0;
		Calendar lastMonth = Calendar.getInstance();
		//获取还款时间上一个月时间
		lastMonth.setTime(receiveTime);     
		lastMonth.add(Calendar.MONTH, -1);//即一个月前的时间     
		
		Calendar now = Calendar.getInstance();//现在时间
		now.setTime(new Date());       
		
		Calendar receiveTime_ = Calendar.getInstance();//获取还款时间上一个月时间
		receiveTime_.setTime(receiveTime); 
		//判断当前时间是否在中间
		if(lastMonth.before(now) && now.before(receiveTime_)) {
			//在中间，按比例算债权金额
			
			int totalDay = (int)((receiveTime_.getTimeInMillis() - lastMonth.getTimeInMillis())/(24*60*60*1000));
			System.err.println("总天数：" + totalDay);
			//剩余天数，即现在到还款时间天数
			int remainDay = (int)((receiveTime_.getTimeInMillis() - now.getTimeInMillis())/(24*60*60*1000));
			System.err.println("剩余天数：" + remainDay);
			//剩余天数/总天数 = 剩余金额/总金额 (利息同理)
			remainAmt = remainDay * amount / totalDay;
			System.err.println("剩余可债权金额：" + remainAmt);
		}
		return remainAmt;
		
	}
	
	
	/**
	 * 标的期限转为天数
	 * @author wangyun
	 * 2018年7月2日 
	 * @description
	 */
	public static int bidDays(Date auditTime, int periods, int period_unit){
		int bid_days = 0;
		// 按照天计算
		if(period_unit == -1){ //年
			Calendar cal = Calendar.getInstance();
		    cal.setTime(auditTime);//设置起时间
		    cal.add(Calendar.YEAR, periods);//增加相应期数年
		    
		    bid_days = DateUtil.daysBetween(auditTime, cal.getTime());
		    
		} else if(period_unit == 0){ //月
			Calendar cal = Calendar.getInstance();
		    cal.setTime(auditTime);//设置起时间
		    cal.add(Calendar.MONTH, periods);//增加相应期数月
		    
		    bid_days = DateUtil.daysBetween(auditTime, cal.getTime());
		    
		} else if(period_unit == 1){ //日
			
			bid_days = periods;
		}
		
		return bid_days;
	}
	
	
	public static void main(String[] args) throws ParseException {
		String date = "2018-11-15";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date receiveTime = sdf.parse(date) ; 
		System.err.println(bidDays(receiveTime, 1,0));
	}
}
