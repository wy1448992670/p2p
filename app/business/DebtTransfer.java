package business;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.lf5.util.DateFormatManager;

import com.shove.security.Encrypt;

import constants.Constants;
import constants.DealType;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import models.t_bids;
import models.t_bill_invests;
import models.t_debt_invest;
import models.t_debt_transfer;
import models.t_dict_bid_repayment_types;
import models.t_invest_user;
import models.t_invests;
import models.t_red_packages_history;
import models.t_red_packages_type;
import models.t_users;
import models.v_debt_invest_records;
import models.v_invest_records;
import models.v_user_for_details;
import utils.Arith;
import utils.CnUpperCaser;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.PushMessage;
import utils.QueryUtil;
import utils.ServiceFee;
import utils.TransferUtil;

public class DebtTransfer {
	/**
	 * 查询可债权转让列表
	 * @author wangyun
	 * 2018年7月2日 
	 * @description
	 */
	private static List<Map<String, Object>> getTransferList(long investId,  ErrorInfo error){
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT t.receive_corpus AS receive_corpus,");
			sql.append("t.receive_interest as receive_interest,");
			sql.append("t.receive_increase_interest AS receive_increase_interest,");
			// 还款时间-现在时间 >3天，已有一期还款
			sql.append("t.status as status,");
			
			sql.append("t.receive_time AS receive_time,");
			sql.append("t.real_receive_time as real_receive_time,");
			sql.append("t.real_receive_interest as real_receive_interest, ");
			sql.append("inv.red_amount as red_amount  ");
			sql.append("FROM t_bill_invests t left join t_invests inv on t.invest_id = inv.id ");
			  
			sql.append("where t.invest_id = ? ");
			
			Logger.info("可转让清单sql：" + sql.toString());
			Logger.info("invest_id: " + investId);
			list = JPAUtil.getList(new ErrorInfo(), sql.toString(), investId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询可债权转让列表时："+e.getMessage());
			error.code = -1;
			error.msg = "查询可债权转让列表时出现异常！";
		}
		 
		return list;
	}
	
	
	/**
	 * 获取标的信息
	 * @author wangyun
	 * 2018年7月2日 
	 * @description
	 */
	public static Map<String, Object> getBidInfo(long bidId, ErrorInfo error){
		Map<String, Object> map_ =  new HashMap<>(); 
		
		try {
			
			StringBuffer bidSql = new StringBuffer("select t.id as bid_id , t.title as title, t.audit_time as audit_time,");
			bidSql.append(" case when t.period_unit = -1 then DATE_ADD(audit_time,INTERVAL t.period * 12 MONTH)  ");
			bidSql.append(" when t.period_unit = 0 then DATE_ADD(audit_time,INTERVAL t.period  MONTH)  ");
			bidSql.append(" when t.period_unit = 1 then DATE_ADD(audit_time,INTERVAL t.period  DAY)  ");
			bidSql.append(" END AS end_time, ");
			bidSql.append(" t.apr as apr, ");
			bidSql.append(" t.period as period, t.period_unit as period_unit ");
			bidSql.append(" from t_bids t where t.id = ? ");
			
			map_ = JPAUtil.getMap(error, bidSql.toString(), bidId);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("获取标的信息时："+e.getMessage());
			error.code = -1;
			error.msg = "获取标的信息出现异常！";
		}
		
		return map_;
	}
	
	/**
	 * 债权转让支付
	 * @author wangyun
	 * 2018年7月3日 
	 * @description
	 * principal_fee 本金转让费
	 * increase_interest 加息利息
	 * 
	 */
	public static int transferPay(long userId, long investId, String dealPasswordStr,int transferDays, ErrorInfo error){
		error.clear();
		try {

			User user = new User();
	    	user.id = userId;
	    	t_users user1 = User.queryUserforInvest(userId, error);
	    	if(error.code < 0) {
	    		JPA.setRollbackOnly();
				return error.code;
			}

			if (user1.balance <= 0) {
				error.setWrongMsg("对不起！您余额不足，请及时充值！");
				error.code = Constants.BALANCE_NOT_ENOUGH;
				JPA.setRollbackOnly();
				return error.code;
			}
			
			double balance = user1.balance;
			boolean black = user1.is_blacklist;
			String dealpwd = user1.pay_password;

			if (black) {
				error.setWrongMsg("对不起！您已经被平台管理员限制操作！请您与平台管理员联系！");
				JPA.setRollbackOnly();
				return error.code;
			}
			
			if (!Encrypt.MD5(dealPasswordStr + Constants.ENCRYPTION_KEY).equals(
					dealpwd)) {
				error.setWrongMsg("对不起！交易密码错误!");
				JPA.setRollbackOnly();
				return error.code;
			}
			//-----------------------------------------------------begin---------------------------------------

			
			//后台创建待审核债权转让标的 *********----------------------------
			t_debt_transfer debtTransfer = createTransfer(userId,investId,error);
			if(error.code < 1){
				JPA.setRollbackOnly();
				return error.code;
			}
			if(debtTransfer==null){
				error.setWrongMsg("对不起！创建债权转让标失败!");
			}
			if(transferDays!=debtTransfer.period){
				error.setWrongMsg("对不起！该投资状态变更,不能转让!");
				JPA.setRollbackOnly();
				return error.code;
			}
			//债权转让手续费=1%转让给金额+已获得加息收益+投资红包
			double transfer_fee=Arith.add(Arith.add(debtTransfer.transfer_rate, debtTransfer.increase_rate),debtTransfer.red_amount);
			if (balance < transfer_fee) {
				error.setWrongMsg("对不起！您可用余额不足！ ");
				error.code = Constants.BALANCE_NOT_ENOUGH;
				JPA.setRollbackOnly();
				return error.code;
			}
			
			/* 可用金额减少,冻结资金增加 */
			DealDetail.freezeFund(userId, transfer_fee, error);
			if(error.code < 0){
				JPA.setRollbackOnly();
				return error.code;
			}			
			/*以上操作操作完， 冻结资金减少  */
		    DealDetail.minusUserFreezeFund(userId, transfer_fee, error);
		    if(error.code < 0){
		    	JPA.setRollbackOnly();
				return error.code;
			}
		    
		    //付款资金流向本金保障账户
		    if(transfer_fee > 0.0){
				//添加标的加息-保障金收支记录 
				DealDetail.addPlatformDetail(DealType.TRANSFER_FEE, investId, userId, -1,DealType.ACCOUNT,
						transfer_fee, DealType.INCOME, "平台收入"+userId+"债权转让管理费", error);
				if(error.code < 0) {
					JPA.setRollbackOnly();
					return error.code;
				}
			}
		    v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);
			if (error.code < 0) {
				JPA.setRollbackOnly();
				return error.code;
			}
			
			double user_amount = forDetail.user_amount;
			double freeze = forDetail.freeze;
			double receive_amount = forDetail.receive_amount;
			
		    //添加交易记录
		    DealDetail dealDetail = new DealDetail(userId, DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE, transfer_fee, investId, user_amount, 
					freeze, receive_amount, "第" + debtTransfer.id + "号债权转让费用" );
			dealDetail.addDealDetail(error);
			if(error.code < 0){
				JPA.setRollbackOnly();
				return error.code;
			}
			
			String username = user1.name;
			//通知
			TemplateEmail email = TemplateEmail.getEmailTemplate(Templets.E_INVEST, error);//发送邮件
			
			if(error.code < 0) {
				email = new TemplateEmail();
			}
			
			if(email.status){
				 String econtent = email.content;
				 econtent = econtent.replace("date", DateUtil.dateToString((new Date())));
				 econtent = econtent.replace("userName", username);			
				 econtent = econtent.replace("title", "第" + debtTransfer.id + "号债权转让费用");
				 econtent = econtent.replace("transfer_fee",  DataUtil.formatString(transfer_fee));
				 
				 TemplateEmail.addEmailTask(user1.email, email.title, econtent);
			 }
			
			 
			TemplateStation station = TemplateStation.getStationTemplate(Templets.M_INVEST, error);//发送站内信
			 
			if(error.code < 0) {
				station = new TemplateStation();
			}
			 
			 if(station.status){
				 String stationContent = station.content;
				 stationContent = stationContent.replace("date", DateUtil.dateToString((new Date())));
				 stationContent = stationContent.replace("userName", username);			
				 stationContent = stationContent.replace("title", "第" + debtTransfer.id + "号债权转让费用");
				 stationContent = stationContent.replace("transfer_fee",  DataUtil.formatString(transfer_fee));
				 
				 StationLetter letter = new StationLetter();
				 letter.senderSupervisorId = 1;
				 letter.receiverUserId = userId;
				 letter.title = station.title;
				 letter.content = stationContent;
				 
				 letter.sendToUserBySupervisor(error);
			 }
			
		    /* 更新自己的防篡改 */
			DataSafety bidUserData = new DataSafety(); // 会员数据防篡改对象
			bidUserData.updateSignWithLock(userId, error);
			
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			Logger.error("债权转让支付时："+e.getMessage());
			error.setWrongMsg("债权转让支付出现异常！");
			return error.code;
		}
		error.code=1;
		return error.code;
	}
	
	
	private static t_debt_transfer createTransfer(long userId, long investId,  ErrorInfo error){
		error.clear();
		
		String bidIdSql = "select bid_id from t_invests where id = ? and user_id=? ";
		Long bidId;
		try {
			bidId =((BigInteger)JPA.em().createNativeQuery(bidIdSql).setParameter(1, investId).setParameter(2, userId).getSingleResult()).longValue() ;
		} catch (Exception e) {
			error.setWrongMsg("用户投资标的信息错误!");
			Logger.error("用户投资标的信息错误： " + e.getMessage());
			return null;
		}
		
		//Bid bid=new Bid();
		//bid.setIdByPessimisticWrite(bidId);
		t_bids bid=Bid.getModelByPessimisticWrite(bidId);//获得标的,上排它锁
		//Invest invest=new Invest();
		//invest.setIdByPessimisticWrite(investId);//获得投资,上排它锁
		t_invests invest=Invest.getModelByPessimisticWrite(investId);
		GregorianCalendar actionCalendar = new GregorianCalendar();//申请时间
		/*
		 * 0.审核中; 1.提前借款; 2.借款中(审核通过); 3.待放款(放款审核通过); 4.还款中(财务放款); 5.已还款; 
		 * 10.审核中待验证 11.提前借款待验证 12.借款中待验证 
		 * 20.审核中待支付投标奖励 21.提前借款待支付投标奖励 22.借款中待支付投标奖励 
		 * 14.本金垫付还款中(已放款) 
		 * -1.审核不通过; -2.借款中不通过; -3.放款不通过; -4.流标; -5.撤销 -10.未验证
		 */
		if(bid.status!=4 && bid.status!=14){
			error.setWrongMsg("对不起！该标的不可转让!");
			return null;
		}
		if(!bid.is_debt_transfer){
			error.setWrongMsg("对不起！该标的不可转让!");
			return null;
		}
		if(invest.transfer_status!=0){//转让状态:0 正常(转让入的也是0) -1 已转让出 1 转让中
			if(invest.transfer_status==-1){
				error.setWrongMsg("对不起！该投资已转让!不可再转让!");
			}else{
				error.setWrongMsg("对不起！该投资正在转让!不可再转让!");
			}
			return null;
		}
		t_debt_transfer transfer=null;
		try {
			transfer=DebtNew.getDebtTransferByInvestId(investId);
		} catch (Exception e) {
			error.setWrongMsg("对不起！该投资异常!");
			Logger.error("对不起！该投资异常!" + e.getMessage());
			return null;
		}
		if(transfer!=null ){
			//status（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）
			if(transfer.status == 1 || transfer.status == 2){
				error.setWrongMsg("对不起！该投资正在转让!不可再转让!");
			}else{//status == 3
				error.setWrongMsg("对不起！该投资已转让!不可再转让!");
			}
			return null;
		}
		//本月是否完成3次债权转让
		List<t_debt_transfer> transList = t_debt_transfer.find("select id from t_debt_transfer where user_id = ? and status in(1,2,3) and  DATE_FORMAT( ? , '%Y%m' ) = DATE_FORMAT( accrual_time , '%Y%m' ) ", userId, DateUtil.dateToString(actionCalendar.getTime())).fetch();
		if(transList.size() >= 3){
			error.setWrongMsg("每个月仅可申请完成三次债券转让！");
			return null;
		}
		
		GregorianCalendar bidAuditTimeCalendar = new GregorianCalendar();//原标放款时间
		bidAuditTimeCalendar.setTime(bid.audit_time);
		
		//债权转让计息时间
		GregorianCalendar accrualTimeCalendar = new GregorianCalendar(actionCalendar.get(Calendar.YEAR), actionCalendar.get(Calendar.MONTH), actionCalendar.get(Calendar.DATE),
																	bidAuditTimeCalendar.get(Calendar.HOUR),bidAuditTimeCalendar.get(Calendar.MINUTE),bidAuditTimeCalendar.get(Calendar.SECOND));
		accrualTimeCalendar.set(Calendar.MILLISECOND, bidAuditTimeCalendar.get(Calendar.MILLISECOND));

		if(accrualTimeCalendar.after(actionCalendar)){//如果计息时间大于审核时间,计息日往前一天
			accrualTimeCalendar.add(Calendar.DATE, -1);
		}
		
		transfer = new t_debt_transfer();
		transfer.bid_id = bid.id;
		transfer.user_id = userId;
		transfer.invest_id = investId;
		//审核状态（1未审核 2 初审通过 3初审不通过 4复审通过 5复审不通过）
		transfer.audit_status = 1;  // 未审核
		//标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标
		transfer.status = 1; //待审核
		transfer.repayment_type =Integer.parseInt(bid.repayment_type_id+"");
		transfer.apr = bid.apr;
		transfer.time = actionCalendar.getTime();//创建时间,申请时间

		transfer.accrual_time = accrualTimeCalendar.getTime();//计息时间,如果计息时间的钱已还,后面重新计算计息时间
		transfer.min_invest_amount = 100;//最小金额默认100;
		transfer.is_only_new_user = false;//默认为false，管理员审核时标记是否为新手标
		transfer.red_amount=invest.red_amount;
		
		List<t_bill_invests> billInvestList=null;
		
		try {
			billInvestList= BillInvests.queryBillInvestsByInvestId(investId);
			if(billInvestList==null || billInvestList.size()==0){
				error.setWrongMsg("查询投资"+investId+"的所有投资账单:没有投资账单");
				return null;
			}
		} catch (Exception e1) {
			error.setWrongMsg("查询投资"+investId+"的所有投资账单:" + e1.getMessage());
			e1.printStackTrace();
			return null;
		}
		
		Date last_repay_time = null;//最后还款时间
		//收款状态:-1 未收款 -2 逾期未收款-3 本金垫付收款-4 逾期收款 (不使用-5 待收款 -6 逾期待收款) -7 已转让出 -8 转让中 0 正常收款
		for(t_bill_invests billInvest:billInvestList){
			
			if(last_repay_time==null || last_repay_time.before(billInvest.receive_time)){//记录最后一期还款时间
				last_repay_time=billInvest.receive_time;
			}
			
			if(billInvest.status==-3||billInvest.status==-4||billInvest.status==0){//已收款
				//已收到的加息利息
				//transfer.increase_rate+=billInvest.real_increase_interest;
				transfer.increase_rate=Arith.add(transfer.increase_rate, billInvest.real_increase_interest);
				
//				Calendar lastReceiveTime = Calendar.getInstance();//上期回款时间
//				lastReceiveTime.setTime(billInvest.receive_time);
//				lastReceiveTime.add(Calendar.MONTH, -1);
				//如果计息时间比已收款账单的预定回款时间小(提前还款),计息日改为该款账单的预定回款时间(从下一期,期头开始)
				if( billInvest.receive_time.after(transfer.accrual_time) ){
					transfer.accrual_time=billInvest.receive_time;
				}
			}else if(billInvest.status==-1){//未收款
				if(billInvest.receive_time.before(actionCalendar.getTime())){
					error.setWrongMsg("创建债权转让"+investId+",有逾期未收款账单,不可转");
					return null;
				}
				if(bid.repayment_type_id ==3 ){//3:一次性还款,审核过期时间是还款前一个月
					Calendar previousReceiveTime = Calendar.getInstance();//上期回款时间(一次性还款 提前一个月)
					previousReceiveTime.setTime(billInvest.receive_time);
					previousReceiveTime.add(Calendar.MONTH, -1);
					transfer.deadline=previousReceiveTime.getTime();
					
				}else if(transfer.deadline==null || transfer.deadline.after(billInvest.receive_time)){//审核过期时间是下一期未还款账单的还款时间
					transfer.deadline=billInvest.receive_time;
				}
				if(transfer.current_period==0 ){//转让当前第几期
					transfer.current_period=billInvest.periods;
				}
				//转让本金
				//transfer.debt_amount+=billInvest.receive_corpus;//转让本金
				transfer.debt_amount=Arith.add(transfer.debt_amount, billInvest.receive_corpus);
				
				try {
					billInvest.status=-8;	//账单改为-8 转让中
					billInvest.save();
				} catch (Exception e) {
					error.setWrongMsg("对不起！原投资账单保存失败！");
					e.printStackTrace();
					Logger.error("对不起！原投资账单保存失败"+e.getMessage());
					return null;
				}
			}else if(billInvest.status==-2){//逾期未收款
				error.setWrongMsg("创建债权转让"+investId+",有逾期未收款账单,不可转");
				return null;
			}else if(billInvest.status==-7||billInvest.status==-8){//转出
				error.setWrongMsg("创建债权转让"+investId+",账单已转,不可再转");
				return null;
			}
		}
		transfer.remain_amount=transfer.debt_amount;//剩余金额(可投金额)
		//转让手续费:transfer.debt_amount*0.01 + transfer.increase_rate + transfer.red_amount
		//transfer.transfer_rate=Arith.add(Arith.add(Arith.mul(transfer.debt_amount, 0.01), transfer.increase_rate),transfer.red_amount);
		transfer.transfer_rate= Arith.mul(transfer.debt_amount, 0.01);
		
		//-------------------------验证-------------------------验证-------------------------验证-------------------------验证-------------------------
		//转让人必须有一期(对一次还款来说是一个月)的利息
		GregorianCalendar firstReceiveTimeCalendar = new GregorianCalendar();//第一次还款时间(对一次性还款来说是放款时间后一个月)
		firstReceiveTimeCalendar.setTime(bid.audit_time);//放款时间+1 month
		firstReceiveTimeCalendar.add(Calendar.MONTH, 1);
		
		if(firstReceiveTimeCalendar.after(transfer.accrual_time)){//计息日必须>=第一期还款时间,转让人一期(对一次性还款来说是一个月)的利息都没拿到是不行的
			error.setWrongMsg("创建债权转让"+investId+",必须获得一期以上的收益");
			return null;
		}
		
		//受让人必须有一期(对一次还款来说是一个月)的利息
		GregorianCalendar secondLastReceiveTimeCalendar = new GregorianCalendar();//最后第二期还款时间(对一次性还款来说是最后一期还款时间前一个月)
		secondLastReceiveTimeCalendar.setTime(last_repay_time);//最后一期还款时间-1 month
		secondLastReceiveTimeCalendar.add(Calendar.MONTH, -1);
		
		if(transfer.accrual_time.after(secondLastReceiveTimeCalendar.getTime())){//受让人必须有一期(对一次还款来说是一个月)的利息
			error.setWrongMsg("创建债权转让"+investId+",必须转让一期以上的收益");
			return null;
		}
		
		//需要三天审核筹集时间
		GregorianCalendar lastApplyTimeCalendar = new GregorianCalendar();//审核,筹集过期时间
		lastApplyTimeCalendar.setTime(transfer.deadline);
		lastApplyTimeCalendar.add(Calendar.DATE, -3);//过期时间前三天
		
		if(lastApplyTimeCalendar.before(transfer.time)){//需要三天审核筹集时间
			error.setWrongMsg("创建债权转让"+investId+",至少需要三天审核筹集时间");
			return null;
		}

		int bid_period_days = TransferUtil.bidDays(bid.audit_time, bid.period, bid.period_unit);//标的期限 （天）
		int complete_period_days = DateUtil.daysBetween(bid.audit_time,transfer.accrual_time);//转让前天数,转让人收益天数
		int deb_period_days=bid_period_days-complete_period_days;//转让期限(天)
		
		transfer.bid_period = bid_period_days;//标的期限 （天）
		transfer.period = deb_period_days;//转让期限
		transfer.complete_period = complete_period_days;//转让前收益天数
		try {
			transfer.save();
			invest.transfer_status=1;
			invest.transfers_id=transfer.id;
			invest.transfers_time=transfer.accrual_time;
			invest.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("债权转让插入数据库："+e.getMessage());
			error.setWrongMsg("债权转让插入数据库异常");
			return null;
		}
		String no = "Z" + transfer.id;
		String title = "第" + transfer.id + "号债权转让";
		String upd_transferSql = "update t_debt_transfer set no = ?, title = ? where id = ?";
		Query query = JPA.em().createQuery(upd_transferSql).setParameter(1, no).setParameter(2, title).setParameter(3, transfer.id);
		int rows = 0;
		try{
			rows = query.executeUpdate();
		}catch(Exception e) {
			Logger.error("对不起，由于平台出现故障，债权转让申请失败！："+e.getMessage());
			e.printStackTrace();
			error.setWrongMsg("对不起，由于平台出现故障，债权转让申请失败！");
			return null;
		}
		
		if(rows == 0){
			error.setWrongMsg("债权转让申请失败！");
			return null;
		}
		error.code=1;
		return transfer;
	}
	
	public static Map<String, Object> getTransferInfo(long investId, ErrorInfo error){
		error.clear();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("investId", investId);
		//转让信息（汇总）
		Map<String, Object> transferInfo = new HashMap<>();
		result.put("transferInfo", transferInfo);
		//可转账单清单
		List<Map<String, Object>> billInvestListMap = new ArrayList<Map<String, Object>>();
		result.put("canTransferList", billInvestListMap);

		String bidIdSql = "select bid_id from t_invests where id = ?  ";
		Long bidId;
		try {
			bidId =((BigInteger)JPA.em().createNativeQuery(bidIdSql).setParameter(1, investId).getSingleResult()).longValue() ;
			
		} catch (Exception e) {
			error.setWrongMsg("用户投资标的信息错误!");
			Logger.error("用户投资标的信息错误： " + e.getMessage());
			//result.put("canTransferStatus", 0); //0,不可转让状态;-1, 已回款;1, 可转让
			return result;
		}

		t_bids bid=t_bids.findById(bidId);//获得标的

		t_invests invest=t_invests.findById(investId);
		GregorianCalendar actionCalendar = new GregorianCalendar();//申请时间
		if(bid.status!=4 && bid.status!=14){
			error.setWrongMsg("对不起！该标的不可转让!");
			return result;
		}
		if(!bid.is_debt_transfer){
			error.setWrongMsg("对不起！该标的不可转让!");
			return result;
		}
		if(invest.transfer_status!=0){//0 正常(转让入的也是0) -1 已转让出 1 转让中
			error.setWrongMsg("对不起！该投资正在转让或已转让!");
			return result;
		}
		
		GregorianCalendar bidAuditTimeCalendar = new GregorianCalendar();//原标放款时间
		bidAuditTimeCalendar.setTime(bid.audit_time);
		
		//债权转让计息时间
		GregorianCalendar accrualTimeCalendar = new GregorianCalendar(actionCalendar.get(Calendar.YEAR), actionCalendar.get(Calendar.MONTH), actionCalendar.get(Calendar.DATE),
																	bidAuditTimeCalendar.get(Calendar.HOUR),bidAuditTimeCalendar.get(Calendar.MINUTE),bidAuditTimeCalendar.get(Calendar.SECOND));
		accrualTimeCalendar.set(Calendar.MILLISECOND, bidAuditTimeCalendar.get(Calendar.MILLISECOND));

		if(accrualTimeCalendar.after(actionCalendar)){//如果计息时间大于审核时间,计息日往前一天
			accrualTimeCalendar.add(Calendar.DATE, -1);
		}
		
		t_debt_transfer transfer = new t_debt_transfer();

		transfer.time = actionCalendar.getTime();//创建时间,申请时间

		transfer.accrual_time = accrualTimeCalendar.getTime();//计息时间,如果计息时间的钱已还,后面重新计算计息时间
		transfer.min_invest_amount = 100;//最小金额默认100;
		transfer.is_only_new_user = false;//默认为false，管理员审核时标记是否为新手标
		transfer.red_amount=invest.red_amount;
		
		List<t_bill_invests> billInvestList=null;
		
		try {
			billInvestList= BillInvests.queryBillInvestsByInvestId(investId);
			if(billInvestList==null){
				error.setWrongMsg("查询投资"+investId+"的所有投资账单:没有投资账单");
				return result;
			}
		} catch (Exception e1) {
			error.setWrongMsg("查询投资"+investId+"的所有投资账单:" + e1.getMessage());
			e1.printStackTrace();
			Logger.error(e1.getMessage());
			return result;
		}
		
		Date last_repay_time = null;//最后还款时间
		//收款状态:-1 未收款 -2 逾期未收款-3 本金垫付收款-4 逾期收款 (不使用-5 待收款 -6 逾期待收款) -7 已转让出 -8 转让中 0 正常收款
		BigDecimal debtInterestAcmountDec=BigDecimal.ZERO;//转让的所有利息
		int scale=BigDecimal.valueOf(invest.amount).setScale(2,BigDecimal.ROUND_HALF_EVEN).precision();//比例计算进度,最终金额需要精确到分,如果计算的原金额整数位为X,比例的小数位需要精确到小数点后X+2位,计算结果可以精确到分
		for(t_bill_invests billInvest:billInvestList){
			
			Map<String, Object> billInvestMap=new HashMap<String, Object>();
			billInvestListMap.add(billInvestMap);
			billInvestMap.put("receive_corpus",billInvest.receive_corpus);
			billInvestMap.put("receive_interest",billInvest.receive_interest);
			billInvestMap.put("receive_increase_interest",billInvest.receive_increase_interest);
			billInvestMap.put("status",billInvest.status);
			billInvestMap.put("receive_time",billInvest.receive_time);
			billInvestMap.put("real_receive_time",billInvest.real_receive_time);
			billInvestMap.put("real_receive_interest",billInvest.real_receive_interest);
			
			BigDecimal assigneeRate=BigDecimal.ZERO;//受让人所占比例

			if(last_repay_time==null || last_repay_time.before(billInvest.receive_time)){//记录最后一期还款时间
				last_repay_time=billInvest.receive_time;
			}
			
			if(billInvest.status==-3||billInvest.status==-4||billInvest.status==0){//已收款
				//已收到的加息利息
				//transfer.increase_rate+=billInvest.real_increase_interest;
				transfer.increase_rate=Arith.add(transfer.increase_rate, billInvest.real_increase_interest);
				
//				Calendar lastReceiveTime = Calendar.getInstance();//上期回款时间
//				lastReceiveTime.setTime(billInvest.receive_time);
//				lastReceiveTime.add(Calendar.MONTH, -1);
				//如果计息时间比已收款账单的预定回款时间小(提前还款),计息日改为该款账单的预定回款时间(从下一期,期头开始)
				if( billInvest.receive_time.after(transfer.accrual_time) ){
					transfer.accrual_time=billInvest.receive_time;
				}
			}else if(billInvest.status==-1){//未收款
				if(bid.repayment_type_id ==3 ){//3:一次性还款,审核过期时间是还款前一个月
					Calendar previousReceiveTime = Calendar.getInstance();//上期回款时间(一次性还款 提前一个月)
					previousReceiveTime.setTime(billInvest.receive_time);
					previousReceiveTime.add(Calendar.MONTH, -1);
					transfer.deadline=previousReceiveTime.getTime();
					
				}else if(transfer.deadline==null || transfer.deadline.after(billInvest.receive_time)){//审核过期时间是下一期未还款账单的还款时间
					transfer.deadline=billInvest.receive_time;
				}
				if(transfer.current_period==0 ){//转让当前第几期
					transfer.current_period=billInvest.periods;
					
					Date currentPeriodBeginDate=null;//当期开始时间
					if(bid.repayment_type_id == Constants.ONCE_REPAYMENT){//一次性还款
						currentPeriodBeginDate=bid.audit_time;//当期开始时间=放款时间
					}else{
						currentPeriodBeginDate=DateUtil.dateAddMonth(billInvest.receive_time,-1);//当期开始时间=本期还款时间-1month
					}
					int currentPeriodAccrualDate=DateUtil.daysBetween(currentPeriodBeginDate,billInvest.receive_time);//当期总天数
					int accrualDate=DateUtil.daysBetween(transfer.accrual_time,billInvest.receive_time);//当期受让人计息天数
					assigneeRate=BigDecimal.valueOf(accrualDate).divide(BigDecimal.valueOf(currentPeriodAccrualDate), scale, BigDecimal.ROUND_HALF_EVEN);//受让人所占比例
				}else{
					assigneeRate=BigDecimal.ONE;
				}
				//转让本金
				//transfer.debt_amount+=billInvest.receive_corpus;//转让本金
				transfer.debt_amount=Arith.add(transfer.debt_amount, billInvest.receive_corpus);

			}else if(billInvest.status==-2){//坏账
				error.setWrongMsg("创建债权转让"+investId+",有逾期未收款账单,不可转");
				return result;
			}else if(billInvest.status==-7||billInvest.status==-8){//转出
				error.setWrongMsg("创建债权转让"+investId+",账单已转,不可再转");
				return result;
			}
			
			
			BigDecimal currentPeriodInterest=BigDecimal.valueOf(billInvest.receive_interest).multiply(assigneeRate).setScale(2, BigDecimal.ROUND_HALF_EVEN);//所有债权转让投资人可获得的当期利息
			debtInterestAcmountDec=debtInterestAcmountDec.add(currentPeriodInterest);//转让的所有利息
			
		}
		transfer.remain_amount=transfer.debt_amount;//剩余金额(可投金额)
		//转让手续费:transfer.debt_amount*0.01 + transfer.increase_rate + transfer.red_amount
		//transfer.transfer_rate=Arith.add(Arith.add(Arith.mul(transfer.debt_amount, 0.01), transfer.increase_rate),transfer.red_amount);
		transfer.transfer_rate= Arith.mul(transfer.debt_amount, 0.01);
		
		//-------------------------验证-------------------------验证-------------------------验证-------------------------验证-------------------------
		//转让人必须有一期(对一次还款来说是一个月)的利息
		GregorianCalendar firstReceiveTimeCalendar = new GregorianCalendar();//第一次还款时间(对一次性还款来说是放款时间后一个月)
		firstReceiveTimeCalendar.setTime(bid.audit_time);//放款时间+1 month
		firstReceiveTimeCalendar.add(Calendar.MONTH, 1);
		
		if(firstReceiveTimeCalendar.after(transfer.accrual_time)){//计息日必须>=第一期还款时间,转让人一期(对一次性还款来说是一个月)的利息都没拿到是不行的
			error.setWrongMsg("创建债权转让"+investId+",必须获得一期以上的收益");
			return result;
		}
		
		//受让人必须有一期(对一次还款来说是一个月)的利息
		GregorianCalendar secondLastReceiveTimeCalendar = new GregorianCalendar();//最后第二期还款时间(对一次性还款来说是最后一期还款时间前一个月)
		secondLastReceiveTimeCalendar.setTime(last_repay_time);//最后一期还款时间-1 month
		secondLastReceiveTimeCalendar.add(Calendar.MONTH, -1);
		
		if(transfer.accrual_time.after(secondLastReceiveTimeCalendar.getTime())){//受让人必须有一期(对一次还款来说是一个月)的利息
			error.setWrongMsg("创建债权转让"+investId+",必须转让一期以上的收益");
			return result;
		}
		
		//需要三天审核筹集时间
		GregorianCalendar lastApplyTimeCalendar = new GregorianCalendar();//审核,筹集过期时间
		lastApplyTimeCalendar.setTime(transfer.deadline);
		lastApplyTimeCalendar.add(Calendar.DATE, -3);//过期时间前三天
		
		if(lastApplyTimeCalendar.before(transfer.time)){//需要三天审核筹集时间
			error.setWrongMsg("创建债权转让"+investId+",至少需要三天审核筹集时间");
			return result;
		}

		int bid_period_days = TransferUtil.bidDays(bid.audit_time, bid.period, bid.period_unit);//标的期限 （天）
		int complete_period_days = DateUtil.daysBetween(bid.audit_time,transfer.accrual_time);//转让前天数,转让人收益天数
		int deb_period_days=bid_period_days-complete_period_days;//转让期限(天)
		
		transfer.bid_period = bid_period_days;//标的期限 （天）
		transfer.period = deb_period_days;//转让期限
		transfer.complete_period = complete_period_days;//转让前收益天数
		
		DecimalFormat df = new DecimalFormat("#0.00"); 
		transferInfo.put("accrual_time",  transfer.accrual_time.getTime()); //计息时间
		transferInfo.put("corpus",  df.format(BigDecimal.valueOf(transfer.debt_amount)) ); //转让金额
		transferInfo.put("interest",    df.format(debtInterestAcmountDec));
		//transfer.debt_amount*0.01+transfer.increase_rate+invest.red_amount
		transferInfo.put("transfer_fee", df.format(BigDecimal.valueOf(Arith.add(Arith.add(Arith.mul(transfer.debt_amount, 0.01),transfer.increase_rate),invest.red_amount)))); //转让金额*0.01+已使用的红包+已到账的加息金额
		transferInfo.put("transferDays", transfer.period);//转让期限
		transferInfo.put("increase_interest",  df.format(BigDecimal.valueOf(transfer.increase_rate)));//加息金额
		transferInfo.put("red_amount",   df.format(BigDecimal.valueOf(invest.red_amount)));//红包金额
		transferInfo.put("principal_fee", df.format(BigDecimal.valueOf(Arith.mul(transfer.debt_amount, 0.01))));//本金转让费
		error.code=1;
		return result;
	}
	
	
	private static Map<String, Object> getTransferInfoOld(long investId, ErrorInfo error){
		Map<String, Object> result = new HashMap<String, Object>();
		//可转让清单
		List<Map<String, Object>> canTransfer = DebtTransfer.getTransferList(investId, error);
		
		//转让信息（汇总）
		Map<String, Object> transferInfo = new HashMap<>();
		double corpus = 0;
		double interest = 0;
		double increase_interest = 0;
		double red_amount = 0;
		int transferDays = 0;
		for (int i = 0; i < canTransfer.size(); i++) {
			double corpus_ = 0;// 可债权金额
			double interest_ = 0;//可债权利息
			double increase_interest_ = 0 ;//加息利息全部转让
			int transferDays_ = 0 ;//剩余还款天数（月大月小都计算）
			
			if(Integer.parseInt(canTransfer.get(i).get("status").toString()) == -1){// 可债权转让的
				Date receive_time = (Date) canTransfer.get(i).get("receive_time");//还款时间
				
				if(TransferUtil.isDaysBetween(receive_time)){
					//corpus_ = TransferUtil.transferAmount(receive_time,  Double.parseDouble(canTransfer.get(i).get("receive_corpus")+""));
					interest_ = TransferUtil.transferAmount(receive_time, Double.parseDouble(canTransfer.get(i).get("receive_interest")+""));
					
				} else {
					//corpus_ = Double.parseDouble(canTransfer.get(i).get("receive_corpus")+"");
					interest_ = Double.parseDouble(canTransfer.get(i).get("receive_interest")+"");
				}
				transferDays_ = TransferUtil.transferDebtDays(receive_time);
				corpus_ = Double.parseDouble(canTransfer.get(i).get("receive_corpus")+"");
			} else { //已还款的加息利息
				increase_interest_ =  Double.parseDouble(canTransfer.get(i).get("receive_increase_interest")+"");//已经到账的加息利息
			}
			
			corpus = corpus + corpus_;
			interest = interest + interest_;
			increase_interest = increase_interest + increase_interest_;
			transferDays = transferDays + transferDays_;
		}
		DecimalFormat df = new DecimalFormat("#0.00"); 
		if(canTransfer != null){
			red_amount = Double.parseDouble(canTransfer.get(0).get("red_amount")+"");
		}
		
		transferInfo.put("corpus",  df.format(new BigDecimal(corpus)) ); //转让金额
		transferInfo.put("interest",    df.format(new BigDecimal(interest)));
		transferInfo.put("transfer_fee", df.format(new BigDecimal(corpus*0.01 + increase_interest + red_amount))); //转让金额*0.01+已使用的红包+已到账的加息金额
		transferInfo.put("transferDays", transferDays);//转让期限
		transferInfo.put("increase_interest",  df.format(new BigDecimal(increase_interest)));//加息金额
		transferInfo.put("red_amount",   df.format(new BigDecimal(red_amount)));//红包金额
		transferInfo.put("principal_fee", df.format(new BigDecimal(corpus*0.01)));//本金转让费
		
		result.put("transferInfo", transferInfo);
		result.put("canTransferList", canTransfer);
		result.put("investId", investId);
		
		return result;
		
	}
	
	/**
	 * 债权转让账单
	 * @author wangyun
	 * 2018年7月6日 
	 * @description
	 */
	public static PageBean<Map<String, Object>> getDebtTransferList(long userId, int pageSize, int currPage,ErrorInfo error){
		pageSize = pageSize == 0 ? 10 : pageSize;
    	currPage = currPage == 0 ? 1 : currPage;
    	try {
    		//债权转让信息
    		StringBuffer sql = new StringBuffer();
    		sql.append("select t.invest_id as invest_id, t.id as id,  t.no as no, ");
    		sql.append("t.debt_amount as debt_amount,t.period as period,");
    		sql.append("t.status as status, t.audit_status as audit_status, ");
    		sql.append("b.id as bid_id , b.title as title, b.audit_time as audit_time, ");
    		sql.append("case when b.period_unit = -1 then DATE_ADD(b.audit_time,INTERVAL b.period * 12 MONTH)  ");
    		sql.append(" when b.period_unit = 0 then DATE_ADD(b.audit_time,INTERVAL b.period  MONTH) ");
    		sql.append(" when b.period_unit = 1 then DATE_ADD(b.audit_time,INTERVAL b.period  DAY)");
    		sql.append(" END AS end_time, ");
    		sql.append("b.apr as apr, ");
    		sql.append("b.period as bid_period, b.period_unit as bid_period_unit");
 
    		
    		StringBuffer sqlFrom = new StringBuffer();
    		sqlFrom.append(" from t_debt_transfer t,t_bids b where t.bid_id = b.id and t.user_id = ? order by t.time desc");
    		String listSql = sql.toString().concat(sqlFrom.toString()).concat(" LIMIT ?,?");
    		Logger.info("债权转让账单 sql: " + listSql);
    		String cntSql = "SELECT count(*) as count".concat(sqlFrom.toString());
    		int count = 0;
	    	List<Map<String, Object>> countMap = JPAUtil.getList(new ErrorInfo(), cntSql.toString(), userId);
	    	if(countMap != null && countMap.get(0) != null && countMap.get(0).get("count") != null){
	    		count = ((BigInteger)countMap.get(0).get("count")).intValue();
	    	}
	    	
	    	List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), listSql, userId, (currPage - 1) * pageSize, pageSize);
	    	Logger.info("债权转让账单list: " +  list);
        	
        	PageBean<Map<String, Object>> page = new PageBean<Map<String, Object>>();
	    	page.pageSize = pageSize;
	    	page.currPage = currPage;
	    	page.totalCount = count;
	    	
	    	page.page = list;
	    	
	    	return page;
		} catch (Exception e) {
			error.code = -2;
			error.msg = "债权转让账单查询失败！";
			e.printStackTrace();
		}
    	
    	return null;
	}
	
	/**
	 * 
	 * @author wangyun
	 * 2018年7月6日 
	 * @description 债权转让账单详情
	 */
	public static Map<String, Object> getDebtTransferDetail(long userId, long investId, long debt_id, ErrorInfo error){
		Map<String, Object> map = new HashMap<>();  
		try {
			//债权转让信息
    		StringBuffer sql = new StringBuffer();
    		sql.append("select t.id as debt_id, t.title as title, case when t.repayment_type = 1 then '按月还款、等额本息' when t.repayment_type = 2 then '按月付息、到期还本' when t.repayment_type = 3 then '一次性还款' end  as repayment_type,  ");
    		sql.append("t.invest_id as invest_id, t.bid_id as bid_id, t.user_id as debt_user_id, t.no as no,t.loan_schedule as loan_schedule,");
    		sql.append("t.debt_amount as debt_amount,t.period as period,t.has_invested_amount as has_invested_amount, t.time as time,");
    		sql.append("t.status as status, t.audit_status as audit_status ,t.min_invest_amount as min_invest_amount, t.deadline as deadline, u.id_number as id_number,u.reality_name as reality_name,");
    		sql.append("(select i.amount from t_invests i where i.id = t.invest_id) as invest_amount, t.reason as reason, ");
    		sql.append("(select count(1) from t_debt_invest d where d.debt_id = t.id) as debtCount ");
    		sql.append(" from t_debt_transfer t left join t_users u on t.user_id = u.id  where t.user_id = ? and t.invest_id = ? and t.id = ? order by t.time desc");
    		Logger.info("债权转让信息sql：" +  sql.toString());
    		Map<String, Object> transferMap = JPAUtil.getMap(error, sql.toString(), userId, investId, debt_id);
    		
    		if (transferMap != null ) {
				String id_number = transferMap.get("id_number").toString();
				String reality_name = transferMap.get("reality_name").toString();
				if (StringUtils.isNotBlank(id_number)) {
					id_number = id_number.substring(0, 3) + "***********" + id_number.substring(id_number.length()-4, id_number.length());
					reality_name = reality_name.substring(0,1)+"***"; 
					transferMap.put("id_number", id_number);
					transferMap.put("reality_name", reality_name);
				}
			}
    		//标的信息
    		StringBuffer bidSql = new StringBuffer("select t.id as bid_id , t.title as title, t.audit_time as start_time,t.repayment_type_id as repayment_type_id,");
    		bidSql.append(" case when t.period_unit = -1 then DATE_ADD(audit_time,INTERVAL t.period * 12 MONTH) ");
    		bidSql.append(" when t.period_unit = 0 then DATE_ADD(t.audit_time,INTERVAL t.period  MONTH)  ");
    		bidSql.append(" when t.period_unit = 1 then DATE_ADD(t.audit_time,INTERVAL t.period  DAY)  ");
    		bidSql.append(" END AS end_time, ");
    		bidSql.append(" t.apr as apr, ");
    		bidSql.append(" t.amount as amount, ");
    		bidSql.append(" ( SELECT NAME FROM t_dict_loan_purposes p WHERE p.id = t.loan_purpose_id ) AS purpose ,");
    		bidSql.append(" t.period as period, t.period_unit as period_unit ");
    		bidSql.append(" from t_bids t left join t_bill_invests b on t.id = b.bid_id  where b.invest_id = ? limit 1");
    		Logger.info("标的信息sql：" +  bidSql.toString());
    		Map<String, Object> bidMap  = JPAUtil.getMap(error, bidSql.toString(), investId);
    		
    		
    		map.put("transferMap", transferMap);
    		map.put("bidMap", bidMap);
		} catch (Exception e) {
			error.code = -2;
			error.msg = "债权转让账单详情查询失败！";
		}
		return map;
	}
	
	
	public static int invest(long userId, long debtId, double totalAmount, String dealPwd,RedPackageHistory redPackage, ErrorInfo error){
		error.clear();
		Date actionTime=new Date();
		
		try {
			DebtNew debtNew=new DebtNew(actionTime,error);
			debtNew.getModelByPessimisticWrite(debtId);
			t_debt_transfer debtTransfer=debtNew.debtTransfer;
			
			if(debtTransfer.user_id == userId){//判断投资人是否是原标的的投资人
				error.setWrongMsg("出让人不可投自己的债权转让标！");
				JPA.setRollbackOnly();
				return error.code;
			}

			User user = new User();
	    	user.id = userId;
	    	if (totalAmount <= 0) {
				error.setWrongMsg("对不起！请输入正确格式的数字！");
				JPA.setRollbackOnly();
				return error.code;
			}
	    	t_users user1 = User.queryUserforInvest(userId, error);
			
			if(error.code < 0) {
				JPA.setRollbackOnly();
				return error.code;
			}

			if (user1.balance <= 0) {
				error.msg = "对不起！您余额不足，请及时充值！";
				error.code = Constants.BALANCE_NOT_ENOUGH;
				JPA.setRollbackOnly();
				return error.code;
			}

			double balance = user1.balance;
			boolean black = user1.is_blacklist;

			if (black) {
				error.setWrongMsg("对不起！您已经被平台管理员限制操作！请您与平台管理员联系！");
				JPA.setRollbackOnly();
				return error.code;
			}
			
			if (User.isInMyBlacklist(debtTransfer.user_id, userId, error) < 0) {
				error.setWrongMsg("对不起！您已经被对方拉入黑名单，您被限制投资此债权转让标！");
				JPA.setRollbackOnly();
				return error.code;
			}
			//Map<String, Object> debt = debtMap(debtId, error);

			if (error.code < 0) {
				error.setWrongMsg( "对不起！系统异常！请您联系平台管理员！");
				JPA.setRollbackOnly();
				return error.code;
			}
			
			if (balance < totalAmount) {
				error.msg = "对不起！您可用余额不足！根据您的余额您最多只能投" + balance + "元";
				error.code = Constants.BALANCE_NOT_ENOUGH;
				JPA.setRollbackOnly();
				return error.code;
			}
			//double minInvestAmount =debtTransfer.min_invest_amount;// Double.parseDouble(debt.get("min_invest_amount") + "");
			//double averageInvestAmount = Double.parseDouble(debt.get("average_invest_amount") + "");
			//double amount = debtTransfer.debt_amount;//Double.parseDouble(debt.get("debt_amount") + "");//转让金额
			//int status = debtTransfer.status;//Integer.parseInt(debt.get("status") + "");
			
			//Date invest_expire_time =debtTransfer.deadline;// DateUtil.strToDate(debt.get("deadline").toString());//截止日期
			
			//double hasInvestedAmount =debtTransfer.has_invested_amount; //Double.parseDouble(debt.get("has_invested_amount") + "");
			//long debtUserId = debtTransfer.user_id;//Long.parseLong(debt.get("user_id") + "");// 转让者
			//boolean is_only_new_user = debtTransfer.is_only_new_user;//(boolean) debt.get("is_only_new_user");
			//long time = actionTime.getTime();//action time
			//long time2 = debtTransfer.deadline.getTime();
			
			//验证红包
			double redAmount = 0.0;
			if(redPackage != null){
				//查询最新红包信息，防止脏数据
				redPackage = RedPackageHistory.findBySign(redPackage.id + "");
				redAmount  = redPackage.money;
				if(redPackage.status != Constants.RED_PACKAGE_STATUS_UNUSED){
					error.setWrongMsg("红包已经失效，或已使用!");
					JPA.setRollbackOnly();
					return error.code;
				}
				//判断是否满足红包的起投点 输入的钱 + 红包钱 - 起投点的钱 >= 0
				if(totalAmount < redPackage.valid_money){
					error.setWrongMsg("对不起！您的投标金额少于红包最低投资限制金额！");
					JPA.setRollbackOnly();
					return error.code;
				}
			}
			totalAmount += redAmount;
			
			//int is_only_new_user = Integer.parseInt(debt.get("is_only_new_user") +"");

			if(debtTransfer.is_only_new_user){
				
				String new_invest_open = Play.configuration.getProperty("new_invest_open", "0");
				if("1".equals(new_invest_open)) {
					
					long new_invest_amount = Long.valueOf(Play.configuration.getProperty("new_invest_amount", "50000"));
					int new_invest_time_unit = Integer.valueOf(Play.configuration.getProperty("new_invest_time_unit", "3"));
					
					Date overDt = DateUtil.dateAddMonth(user.time, new_invest_time_unit);
					if(overDt.before(actionTime)) {
						error.setWrongMsg("已超出可投资新手标时间，请选择其他类型标的");
						JPA.setRollbackOnly();
						return error.code;
					}
					
					double investAmount = Double.parseDouble(JPAUtil.createNativeQuery("SELECT sum(amount) FROM ( SELECT IFNULL( SUM( i.amount ), 0 ) as amount   FROM t_invests i LEFT JOIN t_bids b ON i.bid_id = b.id LEFT JOIN t_users u ON u.id = i.user_id  WHERE b.is_only_new_user = 1  	AND i.user_id = ? UNION ALL  SELECT IFNULL( SUM( d.amount ), 0 ) as amount FROM t_debt_invest d LEFT JOIN t_debt_transfer f ON f.id = d.debt_id  	LEFT JOIN t_users u ON u.id = d.user_id WHERE d.user_id = ?  AND f.is_only_new_user = 1  ) a", userId,userId).getSingleResult().toString());
					Logger.info(String.format("已投新手标: %s=>%s", userId, investAmount));
					if(investAmount >= new_invest_amount) {
						error.setWrongMsg("新手标额度已用完，请选择其他类型标的");
						JPA.setRollbackOnly();
						return error.code;
					}else if(totalAmount > Arith.sub(new_invest_amount, investAmount)) { // 投资金额超过可投金额
						error.setWrongMsg("可投新手标剩余额度"+ Arith.sub(new_invest_amount, investAmount) +"元");
						JPA.setRollbackOnly();
						return error.code;
					}
				}else {
					
					long investCount = Long.valueOf(JPAUtil.createNativeQuery("select count(*) from t_invests bi left join t_bids b on bi.bid_id = b.id where b.is_only_new_user = 1 AND bi.user_id = ?", userId).getSingleResult()+"");
					long debtCount = Long.valueOf(JPAUtil.createNativeQuery("select count(*) from t_debt_invest  bi left join t_debt_transfer  b on bi.debt_id = b.id where b.is_only_new_user = 1 AND bi.user_id = ?", userId).getSingleResult()+"");
					if(Arith.add(investCount, debtCount) > 0){
						error.setWrongMsg("每位用户限享有一次新手标投资机会");
						JPA.setRollbackOnly();
						return error.code;
					}
					else if(totalAmount > 20000){
						error.setWrongMsg("新手标上限为20000元");
						JPA.setRollbackOnly();
						return error.code;
					}
				}
				
				
			}
			if( actionTime.after(debtTransfer.deadline)){
				error.setWrongMsg("对不起！此债权转让已经不处于招标状态，请投资其他债权转让标！谢谢！");
				JPA.setRollbackOnly();
				Logger.error("-----------DebtIncest.invest().error;bidId:%s,time:%s ,invest_expire_time.getTime:%s-------------",debtId,actionTime.getTime(),debtTransfer.deadline.getTime());
				return error.code;
			}

			//标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）
			if (debtTransfer.status != 2) {
				error.setWrongMsg("对不起！此债权转让标已经不处于招标状态，请投资其他债权转让标！谢谢！");
				JPA.setRollbackOnly();
				return error.code;
			}

			if (debtTransfer.debt_amount != Arith.add(debtTransfer.has_invested_amount, debtTransfer.remain_amount)) {
				error.setWrongMsg("债权表状态有误！");
				JPA.setRollbackOnly();
				return error.code;
			}

			DataSafety data = new DataSafety();// 数据防篡改(针对当前投标会员)
			data.setId(userId);
			boolean sign = data.signCheck(error);
			
			if (error.code < 0) {
				error.setWrongMsg("对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！");
				JPA.setRollbackOnly();
				return error.code;
			}

			if (!sign) {// 数据被异常改动
				error.setWrongMsg("对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！");
				JPA.setRollbackOnly();
				return error.code;
			}

			if (StringUtils.isBlank(dealPwd)) {
				error.setWrongMsg("对不起！请输入交易密码!");
				JPA.setRollbackOnly();
				return error.code;
			}
			if (!Encrypt.MD5(dealPwd + Constants.ENCRYPTION_KEY).equals( user1.pay_password )) {
				error.setWrongMsg("对不起！交易密码错误!");
				JPA.setRollbackOnly();
				return error.code;
			}
			//剩余投资额度>=最小投资额度
			if (Arith.sub(debtTransfer.debt_amount, debtTransfer.has_invested_amount)  >= debtTransfer.min_invest_amount) {
				
				if (totalAmount < debtTransfer.min_invest_amount) {
					error.setWrongMsg("对不起！您最少要投" + debtTransfer.min_invest_amount + "元");
					JPA.setRollbackOnly();
					return error.code;
				}
			} else {//剩余投资额度<最小投资额度

				if (totalAmount != Arith.sub(debtTransfer.debt_amount, debtTransfer.has_invested_amount)  ) {
					double money = Arith.sub(debtTransfer.debt_amount, debtTransfer.has_invested_amount);
					error.setWrongMsg("对不起！您必须要投" + money + "元");
					JPA.setRollbackOnly();
					return error.code;
				}
			}

			if (totalAmount > Arith.sub(debtTransfer.debt_amount , debtTransfer.has_invested_amount)) {
				double money = Arith.sub(debtTransfer.debt_amount , debtTransfer.has_invested_amount);
				error.setWrongMsg("对不起！您的投资金额超过了该标的剩余金额,您最多只能投" + money + "元！");
				JPA.setRollbackOnly();
				return error.code;
			}
			
		/*	Bid bids = new Bid();
			bids.id = bidId;
			
			//计算投资时分摊到投资人身上的借款管理费及投标奖励
			Map<String, Double> map = Bid.queryAwardAndBidFee(bids, investTotal, error);
			double award = map.get("award");
			double bid_fee = map.get("bid_fee") ;*/
			String debtInvestId = doInvest(user1, debtTransfer, totalAmount, redPackage,actionTime, error);
			if (error.code < 1) {
				JPA.setRollbackOnly();
				return error.code;
			}
			
			// 修改红包状态
			if (null != redPackage) {
				 RedPackageHistory.updateStatus(redPackage.id+"", Constants.RED_PACKAGE_STATUS_USING+"", debtInvestId, error);
				 if (error.code < 0) {
					JPA.setRollbackOnly();
					return error.code;
				}
			}
			
			
			error.code=1;
			return error.code;
		} catch (Exception e) {
			error.setWrongMsg("债权标的投资失败！");
			JPA.setRollbackOnly();
			return error.code;
		}
		
	}
	
	
	/**
	 *  投标操作(写入数据库)
	 * @param user1 投资人
	 * @param debtId 债权标的id
	 * @param investTotal  此次投资金额 
	 * @param redPackage  红包
	 * @param error
	 */
	private static String doInvest(t_users user1, t_debt_transfer debtTransfer, double investTotal,  RedPackageHistory redPackage,Date actionTime, ErrorInfo error) {
		error.clear();
		double redMoney = 0.0;
		if (null != redPackage) {
			redMoney = redPackage.money;
		}
		long userId = user1.id;
		//double schedule = Arith.divDown(Arith.add(debtTransfer.has_invested_amount, investTotal) , debtTransfer.debt_amount, 4) * 100;//

		/* 已投总额增加,投标进度增加, 超标控制 */
		updateBidschedule(debtTransfer.id, investTotal, error); 
		if(error.code < 0){  //超标或更新失败

			return null;
		}
		
		if (error.code < 0) {
			error.setWrongMsg("对不起！系统异常！对此造成的不便敬请谅解！");
			return null;
		}

		/* 可用金额减少,冻结资金增加 */
		DealDetail.freezeFund(userId, new BigDecimal(investTotal).subtract(new BigDecimal(redMoney)).doubleValue(), error);

		if (error.code < 0) {
			return null;
		}

		// 更新会员性质
		User.updateMasterIdentity(userId, Constants.INVEST_USER,  Constants.CLIENT_APP, error);
		if (error.code < 0) {
			return null;
		}
		v_user_for_details forDetail = DealDetail.queryUserBalance(userId, error);

		if (error.code < 0) {
			return null;
		}

		double user_amount = forDetail.user_amount;
		double freeze = forDetail.freeze;
		double receive_amount = forDetail.receive_amount;
		
		// 添加交易记录
		DealDetail dealDetail = new DealDetail(userId, DealType.FREEZE_DEBT_INVEST, investTotal-redMoney, debtTransfer.id, user_amount, 
				freeze, receive_amount, "冻结债权投标金额" + (investTotal - redMoney) + "元");
		dealDetail.addDealDetail(error);
		
		// 添加红包使用交易记录 有可能页面显示数据有问题 
		DealDetail redDealDetail = new DealDetail(userId, DealType.FREEZE_DEBT_INVEST, redMoney, debtTransfer.id, user_amount, 
				freeze, receive_amount, "扣除红包金额" + (redMoney) + "元");
		redDealDetail.addDealDetail(error);
		if (error.code < 0) {
			return null;
		}
		/*
		// 投标用户增加系统积分
		DealDetail.addScore(userId, 1, investTotal, debtId, "投标成功，添加系统积分", error);
		if (error.code < 0) {
			return null;
		}*/
		
		// 投标添加用户事件
		DealDetail.userEvent(userId, UserEvent.INVEST, "债权转让投标成功", error);
		if (error.code < 0) {
			return null;
		}

		t_debt_invest invest = new t_debt_invest();
		invest.debt_id = debtTransfer.id;
		invest.invest_id = debtTransfer.invest_id;
		invest.bid_id = debtTransfer.bid_id;
		invest.old_user_id = debtTransfer.user_id;//债权转让用户
		invest.user_id = userId;//投资用户
		invest.time = actionTime;
		invest.amount = investTotal;
		invest.red_amount = redMoney;
		try {
			invest.save();
			
			// 标记为有效会员
			User.updateActive(userId, error);
		} catch (Exception e) {
			error.setWrongMsg("对不起！您此次投资失败！请您重试或联系平台管理员！");
			Logger.error("保存投资记录失败，事务回滚"+e.getStackTrace());
			return null;
		}
		
		/*
		// 投标一次增加信用积分
		DealDetail.addCreditScore(userId, 4, 1, invest.id, "成功投标一次，投资人添加信用积分",error);
		if (error.code < 0) {
			Logger.error("增加信用积分失败，事务回滚");
			return null;
		}*/
		
		v_user_for_details forDetail2 = DealDetail.queryUserBalance(userId, error);
		if (error.code < 0) {
			error.setWrongMsg("查询用户余额失败，事务回滚");
			Logger.error("查询用户余额失败，事务回滚");
			return null;
		}
		
		if (forDetail2.user_amount < 0) {
			error.setWrongMsg("对不起！您账户余额不足，请及时充值！");
			Logger.error("账户余额不足，事务回滚");
			return null;
		}

		DataSafety data = new DataSafety();
		data.updateSignWithLock(userId, error);
		if (error.code < 0) {
			error.setWrongMsg("对不起！系统异常！请您重试或联系平台管理员！");
			Logger.error("更新数据防篡改字段，事务回滚");
			return null;
		}
		
		// 发送消息
		String username = user1.name;

		TemplateEmail email = TemplateEmail.getEmailTemplate(Templets.E_INVEST, error);//发送邮件
		
		if(error.code < 0) {
			email = new TemplateEmail();
		}
		 
		if(email.status){
			String econtent = email.content;
			econtent = econtent.replace("date", DateUtil.dateToString(actionTime));
			econtent = econtent.replace("userName", username);
			econtent = econtent.replace("title", debtTransfer.title);
			econtent = econtent.replace("investAmount",  DataUtil.formatString(investTotal));
			TemplateEmail.addEmailTask(user1.email, email.title, econtent);
		}
		
		TemplateStation station = TemplateStation.getStationTemplate(Templets.M_INVEST, error);//发送站内信
		
		if(error.code < 0) {
			station = new TemplateStation();
		}
		if(station.status){
			String stationContent = station.content;
			stationContent = stationContent.replace("date", DateUtil.dateToString(actionTime));
			stationContent = stationContent.replace("userName", username);			
			stationContent = stationContent.replace("title", debtTransfer.title);
			stationContent = stationContent.replace("investAmount",  DataUtil.formatString(investTotal-redMoney));
			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = userId;
			letter.title = station.title;
			letter.content = stationContent;
			letter.sendToUserBySupervisor(error);
		}
		t_dict_bid_repayment_types repaymentTypes=null;
		try {
			 repaymentTypes = t_dict_bid_repayment_types.findById(Long.valueOf(debtTransfer.repayment_type));
		} catch (Exception e) {
			error.setWrongMsg("对不起！还款方式有误！请您重试或联系平台管理员！");
			Logger.error("对不起！还款方式有误！请您重试或联系平台管理员！"+e.getStackTrace());
			return null;
		}
		 
		 
		 //尊敬的userName: 恭喜您投资成功！投资金额￥investAmount元，投资期限period date，年化收益率apr%，还款方式description.
		TemplateSms sms = TemplateSms.getSmsTemplate(Templets.S_TENDER, error);//发送短信
		if(error.code < 0) {
			sms = new TemplateSms();
		}
		if (sms.status && StringUtils.isNotBlank(user1.mobile)) {
			String smscontent = sms.content;
			smscontent = smscontent.replace("userName", username);
			smscontent = smscontent.replace("investAmount", DataUtil.formatString(investTotal));
			smscontent = smscontent.replace("period", debtTransfer.period + "");
			smscontent = smscontent.replace("date", "日");
			smscontent = smscontent.replace("apr", debtTransfer.apr + "");
			smscontent = smscontent.replace("description", repaymentTypes.description);
			TemplateSms.addSmsTask(user1.mobile, smscontent);
		}

		 //满标操作，包括放款！！
		if (debtTransfer.debt_amount == (Arith.add(debtTransfer.has_invested_amount, investTotal))) {

			List<t_debt_invest> investUser;
			try {
				investUser = DebtInvest.queryAllInvestModel(debtTransfer.id);
			} catch (Exception e) {
				error.setWrongMsg("对不起！放款流程有误！无法获得投资人清单!请您重试或联系平台管理员！");
				Logger.error("对不起！放款流程有误！无法获得投资人清单!请您重试或联系平台管理员！" + e.getStackTrace());
				return null;
			}
			
			if(investUser != null && investUser.size() > 0) {
				for(t_debt_invest userInvest : investUser) {
					t_users user = t_users.find("select new t_users(id, device_user_id, channel_id, device_type, is_bill_push, is_invest_push, is_activity_push) "
							+ "from t_users where id = ?",userInvest.user_id).first();
					
					if(user.is_invest_push) {
						String device = user.device_type == 1 ? "\"custom_content\":{\"debtId\":\""+debtTransfer.id+"\",\"type\":\"3\"}" : "\"aps\": {\"alert\":\"test\",\"sound\":\"1\",\"badge\":\"1\"},\"debtId\":\""+debtTransfer.id+"\",\"type\":\"3\"";
						device = "{\"title\":\"理财满标提醒通知\",\"description\":\"你有一条新的理财满标\","+ device+"}";
//						PushMessage.pushUnicastMessage(bill.device_user_id, bill.channel_id, bill.device_type, device);
						PushMessage.pushUnicastMessage(user.device_user_id, user.channel_id, user.device_type, device);
					}
				}
				// 放款操作
				releaseDebtBids(debtTransfer.id,actionTime,error);
				if(error.code < 1){
					error.setWrongMsg("债权投资失败！");
					return null;
				}
			}else{
				error.setWrongMsg("债权投资失败！无法获得投资人清单!");
				return null;
			}

			// 更新满标时间
			/*
			int resulta = updateBidExpiretime(debtId,actionTime, error);
			if (resulta < 0) {
				error.msg = "对不起！系统异常！对此造成的不便敬请谅解！";
				error.code = -8;
				JPA.setRollbackOnly();
				
				return null;
			}*/
			
			
			
		}
		error.msg = "债权投资成功！";
		error.code = 1;
		return invest.id+"";
	}
	
	/**
	 * 已投总额增加,投标进度增加
	 * @param bidId
	 * @param amount
	 * @param schedule
	 * @param error
	 * @return
	 */
	private static void updateBidschedule(long debtId,double amount,ErrorInfo error){
		EntityManager em = JPA.em();
		int rows = 0;
		
		try {
			/*rows = em.createQuery("update t_bids set loan_schedule=?,has_invested_amount= has_invested_amount + ? where id=? and amount >= has_invested_amount + ?")
			.setParameter(1, schedule).setParameter(2, amount).setParameter(3, bidId).setParameter(4, amount).executeUpdate();*/
			rows = em.createQuery("update t_debt_transfer set loan_schedule = truncate((((has_invested_amount + ?)/ debt_amount) * 100), 2) , has_invested_amount= has_invested_amount + ?, remain_amount = remain_amount - ? where id=? and debt_amount >= has_invested_amount + ?") 
					.setParameter(1, amount).setParameter(2, amount).setParameter(3, amount).setParameter(4, debtId).setParameter(5, amount).executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("更新已投总额,投标进度时,%s", e.getMessage());
			error.code = -1;
			error.msg = "更新已投总额,投标进度异常";
			JPA.setRollbackOnly();
			return ;
		}
		
		if(rows == 0){
			Logger.info("更新已投总额,投标进度时,已满标");
			error.code = Constants.OVERBIDAMOUNT;
			error.msg = "超额投资，请解冻投资金额";
			JPA.setRollbackOnly();
			return ;
		}
		
		error.code = 1;
		return ;
	}
 
	/**
	 * 债权转让放款
	 */
	private  static void releaseDebtBids(long debtId,Date actionTime, ErrorInfo error){
		//债权记录
		DebtNew debtNew=new DebtNew(actionTime,error);
		debtNew.getModelByPessimisticWrite(debtId);
		t_debt_transfer debtTransfer=debtNew.debtTransfer;
		debtTransfer.refresh();
		t_invests invest=debtNew.invest;
		invest.refresh();
		t_bids bid=t_bids.findById(debtTransfer.bid_id);
		
		long userId = debtTransfer.user_id; //债权人的
		
		User user = new User();
		user.id = userId; 
		DataSafety bidUserData = new DataSafety(); // 借款会员数据防篡改对象
		bidUserData.setId(userId);
		if(!bidUserData.signCheck(error)) {
			error.setWrongMsg("借款用户资金有异常,无法放款!");
			return;
		}
		
		Map<String, Double> detail  = DealDetail.queryUserFund(userId, error);

		if(null == detail || detail.size() == 0){
			error.setWrongMsg("查询转让人用户的可用余额有异常,无法放款!");
			return;
		}
		
		/* 打钱给出让人 */
		int row = DealDetail.addUserFund(userId, debtTransfer.debt_amount);
		if (row < 1) {
			error.setWrongMsg("增加转让人资金失败!");
			return;
		}
		
		detail = DealDetail.queryUserFund(userId, error);
		/* 添加交易记录    债权转让金额是否可以算作借款金额*/
		DealDetail dealDetail = new DealDetail(userId, DealType.ADD_DEBT_TRANSFER_FUND,
				debtTransfer.debt_amount, debtId, detail.get("user_amount"), detail.get("freeze"),
				detail.get("receive_amount"), "获得"+debtTransfer.id+"号债权转让金额");
		dealDetail.addDealDetail(error);

		if (error.code < 0) {
			error.setWrongMsg("增加转让人资金,添加交易记录失败!");
			return;
		}
		
		if(null == detail || detail.size() == 0){
			error.setWrongMsg("查询转让人用户的可用余额有异常,无法放款!");
			return;
		}
		// 确定操作资金不为负数 
		if(detail.get("user_amount") < 0 || detail.get("freeze") < 0){
			error.setWrongMsg("转让人资金出现负数!");
			return;
		}
		
		/* 更新自己的防篡改 */
		bidUserData.updateSignWithLock(userId, error);
		List<t_debt_invest> debtInvestList=null;
		try {
			debtInvestList = DebtInvest.queryAllInvestModel(debtId);
		} catch (Exception e1) {
			error.setWrongMsg("获得所有受让人投资的金额有误!");
			e1.printStackTrace();
			Logger.error("获得所有受让人投资的金额有误!"+e1.getStackTrace());
		} // 获得用户对这个标所投的金额
		if (null == debtInvestList || debtInvestList.size() == 0) {
			error.setWrongMsg("获得所有受让人投资的金额有误!");
			return;
		}
		
		/* 减去投资人的投标冻结金额/发奖金 */
		investUserAmountProcess(debtTransfer,error, debtInvestList);
		if(error.code < 1){
			return;
		}
		/* 生成债权转让投资账单 */
		DebtBill.addDebtBills(debtTransfer,invest,bid, error);
		if(error.code < 0){
			return;
		}
		
		//添加CPS推广费 
//		User.rewardCPS(userId, 0, transfer.id, error); //只插入cps奖励记录，真实业务逻辑，在线程中完成
//		
//		if (error.code < 0) {
//			JPA.setRollbackOnly();
//
//			return -3;
//		}
		
		// 财富圈，发放返佣金额，这里只做记录，通过定时任务进行发放,债权是否需要财富圈
		//Wealthcircle.addInviteIncome(transfer.id);
		
		
		 //再次确定是否满标状态 
		long debtId_ = checkDebtBidStatus(debtTransfer.id);
		
		if(debtId_ != debtId){
			error.setWrongMsg("债权标有误，请确定是否满标!");
			return ;
		}
		
		// 更新自己的防篡改 
		bidUserData.setId(userId);
		bidUserData.updateSignWithLock(userId, error);
		if(error.code < 0){
			error.setWrongMsg("更新数据防篡改字段异常!");
			return;
		}
		
		//生成借款合同(借款人) 
	//	boolean flag = createPact(transfer);
		
		/*if(!flag) {
			error.msg = "生成借款合同(借款人)失败!";
			JPA.setRollbackOnly();

			return -3;
		}
		
		// 生成理财合同 
		Invest.creatInvestPact(transfer.id, error);
		
		if(error.code < 0){
			error.msg = "生成借款合同(投资人)失败!";
			JPA.setRollbackOnly();

			return -3;
		}*/
		
		// 添加事件 
		DealDetail.supervisorEvent(1, SupervisorEvent.REPAYMENT_FUND, "审核标：待放款->还款中", error);
		
		if(error.code < 0){
			error.setWrongMsg("添加事件失败!");
			return;
		}
		
		// 添加借款积分以及投标积分 
		BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
		
//		// 投标送积分
//		for (t_debt_invest invest : debtInvest) {
//			User user1 = new User();
//			user1.id = invest.user_id;
//			MallScroeRecord.saveScroeInvest(user1, invest, backstageSet.invest_sign_scroe, transfer.title, error);
//			if (error.code < 0) {
//				error.msg = "添加投标送积分失败!";
//				JPA.setRollbackOnly();
//
//				return -3;
//			}
//		}

		// 通知 
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		TemplateSms sms = new TemplateSms();
		email.setId(Templets.E_FINANC_RELEASE);
		station.setId(Templets.M_FINANC_RELEASE);
		sms.setId(Templets.S_FINANC_RELEASE);
		
		/*User debtUser = new User();
		user.id = transfer.user_id;*/
		
		if(station.status){
			content = station.content; 
			content = content.replace("userName", user.name);
			content = content.replace("title", debtTransfer.title);

			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = debtTransfer.user_id;
			letter.title = station.title;
			letter.content = content;
			 
			letter.sendToUserBySupervisor(error);
		}
		
		if(email.status){
			content = email.content;
			content = content.replace("userName", user.name);
			content = content.replace("date", DateUtil.dateToString((new Date())));
			content = content.replace("title", debtTransfer.title);
			
			email.addEmailTask(user.email, email.title, content);
		}
		
		//尊敬的userName: 您申请的编号bidId债权标已成功放款，借款金额amount元，扣除管理费serviceFees元
		if(sms.status && StringUtils.isNotBlank(user.mobile)){
			// BackstageSet backstageSet = BackstageSet.getCurrentBackstageSet();
			content = sms.content;
			content = content.replace("userName", user.name);
			content = content.replace("bidId",backstageSet.loanNumber + debtTransfer.id);
			content = content.replace("amount",DataUtil.formatString(debtTransfer.debt_amount));
			content = content.replace("serviceFees",DataUtil.formatString(0));
			content = content.replace("money",DataUtil.formatString(debtTransfer.debt_amount));//到账金额
			sms.addSmsTask(user.mobile, content);
		}
		
		// 放款成功，增加信用积分
//		DealDetail.addCreditScore(transfer.user_id, 3, 1, transfer.id, "债权转让成功，出让人添加信用积分", error);
//		if (error.code < 0) {
//			JPA.setRollbackOnly();
//			return -3;
//		}
		 //
		
		//修改债权标的状态为成功（包括还款中和已还款，放款即视为成功）
		try {
			invest.transfer_status=-1;
			invest.save();
			//更新满标时间
			debtTransfer.real_invest_expire_time=actionTime;
			debtTransfer.status=3;
			debtTransfer.save();
		} catch (Exception e) {
			error.setWrongMsg("债权转让标修改状态失败！");
			Logger.error("债权转让标修改状态失败！"+e.getStackTrace());
			return ;
		}
		
		//TODO 放款成功后捐款++liulj
		//Bid.addUserDonate(transfer.id);
		//TODO 放款成功后生成电子合同++liulj 170523
		//Pact.doJob(null, transfer.id, 0);
		
		error.code = 1;
		error.msg = "债权转让放款成功!";
		return ;
	}
	
	/**
	 * 扣除投资人的投资冻结金额/发奖金
	 */
	private static void investUserAmountProcess(t_debt_transfer transfer,ErrorInfo error, List<t_debt_invest> debtInvests){
		error.clear();

		User invest_user = null;
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		email.setId(Templets.E_INVEST_DEDUCT);
		station.setId(Templets.M_INVEST_DEDUCT);
		
		DataSafety bidUserData = new DataSafety();
		DealDetail dealDetail = null;
		Map<String, Double> detail = null;
		
		//double deductMoney = 0; //借款总奖励
		//String _round = null; // 金额字符串形式
						
		/* 循环处理,涉及操作过多性能消耗较大 */
		
		User investUser = new User();
		for (t_debt_invest invest:debtInvests) {

			//t_debt_transfer transfer = t_debt_transfer.find(" id = ? ", invest.debt_id).first(); //获取债权标的信息
			
			bidUserData.id = invest.user_id;	
			investUser.id  = invest.user_id;
			
			//放款检查是否使用红包
			//TODO 红包逻辑有误
			double redMoney = 0;
			t_red_packages_history red = RedPackageHistory.queryRedByUserIdAndInvestId(invest.user_id, invest.getId());
			if( null != red ){
				redMoney = red.money;
				DealDetail.minusUserFreezeFund(invest.user_id,new BigDecimal(invest.amount).subtract(new BigDecimal(red.money)).doubleValue() , error); // 减去用户冻结的投标资金			
			}else{
				DealDetail.minusUserFreezeFund(invest.user_id, invest.amount, error); // 减去用户冻结的投标资金
			}
			if (error.code < 0) {
				return;
			}
			
			//修改当前投资人红包状态，更新成已使用
			if(null != red){
				RedPackageHistory.updateRedPackagesHistory(invest.user_id, invest.getId());
			}
			
			detail = DealDetail.queryUserFund(invest.user_id, error);
			if (error.code < 0 || null == detail || detail.size() == 0) {
				error.setWrongMsg("查询投资人账户失败!");
				return;
			}
			/* 添加交易记录 */
			dealDetail = new DealDetail(invest.user_id, DealType.CHARGE_DEBT_INVEST_FUND,
					new BigDecimal(invest.amount).subtract(new BigDecimal(redMoney)).doubleValue(), invest.debt_id, detail.get("user_amount"), detail.get("freeze"),
					detail.get("receive_amount"), "第" + transfer.id +"号债权转让投资");
			dealDetail.addDealDetail(error);
			if (error.code < 0) {
				error.setWrongMsg("扣除投资人 债权投标冻结金额、添加交易记录失败!");
				return;
			}
			detail = DealDetail.queryUserFund(invest.user_id, error);
			if(error.code <0 || null == detail || detail.size() == 0){
				error.setWrongMsg("查询投资人用户的可用余额有异常,无法放款!");
				return;
			}
			// 确定操作资金不为负数 
			if(detail.get("user_amount") < 0 || detail.get("freeze") < 0){
				error.setWrongMsg("投资人资金出现负数!");
				return;
			}
			
			//TODO 投资人奖金
			
			bidUserData.setId(invest.user_id); // 加防篡改标示(投资会员,只更新)
			/* 被动修改状态：只有在签名正确的情况下才能继续更新签名  */
			invest_user = new User();
			invest_user.createBid = true;
			invest_user.id = invest.user_id;
			
			//_round = Arith.round(fund, 2) + "";
			
			if(station.status){
				content = station.content;
				content = content.replace("userName", invest_user.name); 
				content = content.replace("date", DateUtil.dateToString((new Date()))); 
				content = content.replace("title", transfer.title); //标的标题
			    content = content.replace("rebackAmount",  DataUtil.formatString(new BigDecimal(invest.amount).subtract(new BigDecimal(redMoney)).doubleValue())); 
				//content = content.replace("bonus",  DataUtil.formatString(_round)); 
				
				StationLetter letter = new StationLetter();
				letter.senderSupervisorId = 1;
				letter.receiverUserId = invest_user.id;
				letter.title = station.title;
				letter.content = content;
				 
				letter.sendToUserBySupervisor(error);
			}
			
			if(email.status){
				content = email.content;
				content = content.replace("userName", invest_user.name); 
				content = content.replace("date", DateUtil.dateToString((new Date()))); 
				content = content.replace("title", transfer.title); 
				content = content.replace("rebackAmount",  DataUtil.formatString(new BigDecimal(invest.amount).subtract(new BigDecimal(redMoney)).doubleValue())); 
				//content = content.replace("bonus",  DataUtil.formatString(_round)); 
				email.addEmailTask(invest_user.email, email.title, content);
			}
			
		/*	//发送投资红包
			try {
				List<t_red_packages_type> packs = RedPackage.findInvestRedPack(invest.user_id, transfer.debt_amount, invest.amount,invest.id);
				if(packs != null && packs.size() > 0){
					for(t_red_packages_type red1 : packs){
						User user = new User();
						user.id = invest.user_id;//投资人ID
						RedPackageHistory.sendRedPackage(user, red1,red1.name);
					}
					Logger.info("投资红包发放成功");
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("投资红包发放失败");
			}*/
			
		}		
		
		error.code = 1;
	}
	
	
	/**
	 * 检查当前已投金额和借款金额是否相等
	 */
	private static long checkDebtBidStatus(long debtId){
		Long id = null;
		String hql = "select id from t_debt_transfer where id = ? and has_invested_amount = debt_amount and loan_schedule = 100";
		
		try {
			id = t_bids.find(hql, debtId).first();
		} catch (Exception e) {
			return 0;
		}
		 
		if(null == id)
			return 0;
		
		return id;
	}
	
	public static PageBean<v_debt_invest_records> getDebrInvestDetail(int currPage, int pageSize, long debtId, ErrorInfo error){
		PageBean<v_debt_invest_records> pageBean = new PageBean<v_debt_invest_records>();
		List<v_debt_invest_records> list = new ArrayList<v_debt_invest_records>();
		pageBean.currPage = currPage;
		pageBean.pageSize = pageSize;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_DEBT_INVEST_RECORDS);
		sql.append(" and i.debt_id = ?");
		sql.append(" order by i.time desc");
		List<Object> params = new ArrayList<Object>();
		params.add(debtId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_debt_invest_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            list = query.getResultList();
            
			if (list != null && list.size() > 0) {
				for (v_debt_invest_records r : list) {
					String mobile = r.mobile;
					if (StringUtils.isNotBlank(mobile)) {
						r.mobile = mobile.substring(0, 3) + "****" + mobile.substring(7, mobile.length());
					}
				}
			}
            
            pageBean.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			
			return pageBean;
		}
		
		pageBean.page = list;
		error.code = 1;
		return pageBean;
	}
	
	public static List<Map<String, Object>> findUserDebtInvestReturn(long debt_invest_id, long userId){
		StringBuffer sql = new StringBuffer("SELECT id,receive_time,receive_corpus,receive_interest,user_id,debt_id,receive_increase_interest");
    	 
    	sql.append(",  bi.status as status, 0 as isIncreaseRate ");
    	sql.append(" FROM t_debt_bill_invest bi WHERE debt_invest_id = ? AND user_id = ?");
    	sql.append(" ORDER BY receive_time");
    	Logger.info("债权转让回款计划sql: " + sql);
 		List<Map<String, Object>> list = JPAUtil.getList(new ErrorInfo(), sql.toString(), debt_invest_id, userId);
        return list;
	}
}
