package controllers.supervisor.debt;

import business.DebtNew;
import business.DebtTransfer;
import business.Supervisor;
import models.t_debt_transfer;
import play.Logger;
import utils.ErrorInfo;
import utils.Security;
import constants.Constants;
import controllers.supervisor.SupervisorController;

public class DebtTransferAction extends SupervisorController{
	public static void auditDebt(){
		ErrorInfo error = new ErrorInfo();
		String audit_status = params.get("audit_status");//1未审核 2 初审通过 3初审不通过 4复审通过 5复审不通过
		String signUserId = params.get("sign");
		String debtId = params.get("debtId");
		String title = params.get("title");
		String min_invest_amount = params.get("min_invest_amount");
		String is_only_new_user = params.get("is_only_new_user");
		String reason = params.get("reason").trim();//审核拒绝时填写
		
		long userId = Security.checkSign(signUserId, Constants.SUPERVISOR_ID_SIGN, Constants.VALID_TIME, error);
		if(error.code < 1){
			Logger.error("债权转让审核时：" + error.msg);
			error.setWrongMsg("债权转让审核失败！");
			renderJSON(error);
		}
		long currSupervisor=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
		if(userId!=currSupervisor){
			Logger.error("债权转让审核,审核人有误！");
			error.setWrongMsg("债权转让审核,审核人有误！");
			renderJSON(error);
		}
		
		boolean isOnlyNewUser = false;
		if(Integer.parseInt(is_only_new_user) == 1){
			isOnlyNewUser = true;
		}
		
		DebtNew debt=new DebtNew(error);
		debt.getModelByPessimisticWrite(Long.valueOf(debtId));
		debt.updateDebtAuditStatus(Integer.parseInt(audit_status),title,isOnlyNewUser,reason,Double.parseDouble(min_invest_amount));
		
		if(error.code < 1){
			error.setWrongMsg("债权转让审核失败！");
			renderJSON(error);
		}
		renderJSON(error);
	}
	
	
}
