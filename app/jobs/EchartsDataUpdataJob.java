package jobs;

import business.EChartsData;
import business.TemplateEmail;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import utils.ErrorInfo;

/***
 * 
 * <Description functions in a word>
 * 每天凌晨 00：10更新Ecahrts数据
 * <Detail description>
 * 
 * @author ChenZhipeng
 * @version  [Version NO, 2015年8月21日]
 * @see  [Related classes/methods]
 * @since  [product/module version]
 */
@On("0 10 00 * * ?")
@OnApplicationStart
public class EchartsDataUpdataJob extends BaseJob{

	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		ErrorInfo error = new ErrorInfo();
		EChartsData.saveOrUpdataEchartsData(error);
	}
}
