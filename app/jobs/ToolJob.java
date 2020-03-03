package jobs;

import java.util.Date;

import play.jobs.Job;
import utils.DateUtil;
import constants.Constants;

public class ToolJob extends BaseJob{

	@Override
	public void doJob() throws Exception {
//		if(Constants.TRUST_FUNDS_TYPE.equals("HF")){
//			final ToolsService toolsService = new ToolsService();
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					toolsService.messAccount();
//				}
//			}).run();
////			String beginDate = yesterdayDate();
//			String beginDate = "20150701";
//			String endDate = "20150719";
//			toolsService.creditAssignReconciliation(beginDate,endDate);
//			toolsService.cashReconciliation(beginDate,endDate);
//			toolsService.saveReconciliation(beginDate,endDate);
//			toolsService.reconciliation(beginDate,endDate,ReconciliationEnum.REPAYMENT);
//			toolsService.reconciliation(beginDate,endDate,ReconciliationEnum.LOANS);
//		}
	}
	
	public static String yesterdayDate(){
		return DateUtil.simple(DateUtil.dateAddDay(new Date(), -1));
	}
	

}
