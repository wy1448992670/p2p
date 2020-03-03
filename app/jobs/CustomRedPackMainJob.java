package jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import business.RedPackage;
import models.t_red_packages_type;
import play.Logger;
import play.jobs.Every;
import utils.DateUtil;

/**
 * 
* Description: 发放自定义优惠券
* @author xinsw
* @date 2017年5月7日
 */
@Every("10min")
public class CustomRedPackMainJob extends BaseJob{
	public static List<Long> ids = new ArrayList<Long>();
	
	public void doJob(){
		if(!"1".equals(IS_JOB))return;
		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.HOUR_OF_DAY) < 6 || cal.get(Calendar.HOUR_OF_DAY) > 22){
			return;
		}
		
		Logger.info("--------------自定义优惠券,开始---------------------");
		List<t_red_packages_type> redPacks = RedPackage.findCustomRedPack();
		if(redPacks == null || redPacks.size() == 0){
			return;
		}
		for(t_red_packages_type red : redPacks){
			if(!ids.contains(red.id)){
				com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(red.rules);
				String start = json.getString("start");
				String end = json.getString("end");
				Date today = new Date();
				Date sd = DateUtil.strDateToStartDate(start);
				Date ed = DateUtil.strDateToEndDate(end);
				if(today.getTime() >= sd.getTime() && today.getTime() <= ed.getTime()){
					//自定义优惠券有效期-自定义日期结束
					long c = ed.getTime() - today.getTime();
					long h = c / 1000 / 3600;
					long y = c / 1000 % 3600;
					if(y > 0){//剩余不到1小时 定义为1小时
						h += 1;
					}
					if(h == 0){
						continue;
					}
					red.validate_unit = 0;
					red.validity_time = h;
					
					CustomRedPackJob job = new CustomRedPackJob(red);
					job.now();
					ids.add(red.id);
				}
			}
		}
	
		Logger.info("--------------自定义优惠券,结束---------------------");
	}
}
