package business;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import models.t_bids;
import models.t_bill_invests;
import models.t_invest_transfer_details;
import models.t_invest_transfers;
import models.t_invests;
import models.t_products;
import models.t_system_options;
import models.t_user_attention_bids;
import models.t_user_attention_invest_transfers;
import models.t_users;
import models.v_debt_auction_records;
import models.v_debt_auditing_transfers;
import models.v_debt_no_pass_transfers;
import models.v_debt_transfer_failure;
import models.v_debt_transfering;
import models.v_debt_transfers_success;
import models.v_debt_user_receive_transfers_management;
import models.v_debt_user_transfer_management;
import models.v_front_all_debts;
import models.v_receiving_invest_bids;
import models.v_user_attention_invest_transfers;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.db.jpa.Blob;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.Security;

import com.google.zxing.BarcodeFormat;
import com.shove.Convert;
import com.shove.code.Qrcode;
import com.shove.security.Encrypt;

import constants.Constants;
import constants.DealType;
import constants.OptionKeys;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import constants.UserEvent;

/**
 * 债权转让
 * 
 * @author lwh
 * @version 6.0
 * @created 2014年3月21日 下午2:01:38
 */
public class Debt implements Serializable{
	private long _id;
	public long id;
	public String sign;
	public String no;
	public long investId;
	public Date time;
	public String title;
	public String transerReason;
	public double debtAmount;
	public double transferPrice;
	public int type;
	public long specifiedUserId;
	public int period;
	public int status;
	public String noThroughReason;
	public boolean isQualityDebt;
	public long auditSupervisorId;
	public Date startTime;
	public Date endTime;
	public Date failureTime;
	public String lastTime;
	public int joinTimes;
	public long transactionUserId;
	public Date transactionTime;
	public double transactionPrice;
	public double maxOfferPrice;
	public String qr_code;
	
	public Invest invest;
	public Map<String,Object> map;
	public User transactionUser;//竞拍成功会员
	public User specifiedUser;//转让指定会员
	
	public String getSpreadLink() {
		return Constants.BASE_URL + "front/debt/debtDetails?debtId=" + this.id;
	}
	
	public String getSign() {
		return Security.addSign(this._id, Constants.BID_ID_SIGN);
	}

	public void setSpecifiedUserId(long specifiedUserId) {
		this.specifiedUserId = specifiedUserId;
		this.specifiedUser = new User();
		this.specifiedUser.id = specifiedUserId;
	}
	
	public void setTransactionUserId(long transactionUserId) {
		this.transactionUserId = transactionUserId;
		this.transactionUser = new User();
		this.transactionUser.id = transactionUserId;
	}

	public void setInvestId(long investId) {
		this.investId = investId;
		this.invest = new Invest();
		this.invest.id = investId;
	}
	
	public Debt() {
		
	}
	
	public long getId() {
		return _id;
	}

	public void setId(long id) {
		/*t_invest_transfers 债权转让数据实体*/
		t_invest_transfers investTransfers = null;
		ErrorInfo error = new ErrorInfo();
		
		try {
			 investTransfers = t_invest_transfers.findById(id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		if (investTransfers == null) {
			this._id = -1;
			throw new NullPointerException("数据实体对象不存在!");
		}
		
		this._id = investTransfers.id;
		this.investId = investTransfers.invest_id;
		this.no =  OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, error).concat(id + ""); 
		this.time = investTransfers.time;
		this.title = investTransfers.title;
		this.transerReason = investTransfers.transer_reason;
		this.debtAmount = investTransfers.debt_amount;
		this.transferPrice = investTransfers.transfer_price;
		this.type = investTransfers.type;
		this.qr_code = investTransfers.qr_code;
		
		if(investTransfers.specified_user_id > 0){
			this.specifiedUserId = investTransfers.specified_user_id;
		}
		
		this.period = investTransfers.period;
		this.status = investTransfers.status;
		this.noThroughReason = investTransfers.no_through_reason;
		this.isQualityDebt = investTransfers.is_quality_debt;
		this.auditSupervisorId = investTransfers.audit_supervisor_id;
		this.startTime = investTransfers.start_time;
		this.endTime = investTransfers.end_time;
		this.failureTime = investTransfers.failure_time;
		if(null != investTransfers.end_time ){
			this.lastTime = DateUtil.dateToString2(investTransfers.end_time);//格式化时间，方便时间倒计时计算
		}
		
		this.joinTimes = investTransfers.join_times;
		
		if(investTransfers.transaction_user_id > 0){
			this.transactionUserId = investTransfers.transaction_user_id;
		}
		if(null != investTransfers.transaction_time){
			this.transactionTime = investTransfers.transaction_time;
		}
		if(investTransfers.transaction_price > 0){
			this.transactionPrice = investTransfers.transaction_price;
		}
		this.maxOfferPrice = this.queryMaxPrice(id,error);
		
		if( null != this.queryInvestBill(id)){
			this.map = this.queryInvestBill(id);
		}
	}
	
	
	
	
	
	/**
	 * 查询某个人投资某个借款标的投资账单
	 * @param id
	 * @return
	 */
	private Map<String,Object> queryInvestBill(long id){
		
		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		t_invest_transfers investTransfers = null;
		t_invests invest = null;
		Map<String,Object> map = new HashMap<String, Object>();
		Date time = null;
		Double receive_money  = 0.0;
		Double has_receive_money  = 0.0;
		Double receive_corpus  = 0.0;
		
		try {
			investTransfers = t_invest_transfers.findById(id);
			invest = t_invests.findById(investTransfers.invest_id);
		} catch (Exception e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(invest.transfer_status == -1){
			invest = t_invests.find("transfers_id = ? ", investTransfers.id).first();
		}
		
		if(null != invest){
			String hql = "select receive_time from t_bill_invests   where  invest_id = ? and status = -1 order by receive_time";
			
			time = t_bill_invests.find(hql, invest.id).first();
			

			
			String strTime = "";
			if(time != null){
				strTime = DateUtil.dateToString1(time);
			}
			
			map.put("receive_time", strTime);
			
			String s = "select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where  invest_id = ? and status <> -7";
			String r = "select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where  invest_id = ? and status in (-3,-4,0)";
			String c = "select sum(receive_corpus - real_receive_corpus) from t_bill_invests where  invest_id = ? and status in (-1,-2,-5,-6)";
			
			try {
				
					 receive_money =  t_bill_invests.find(s, invest.id).first();
					 has_receive_money = t_bill_invests.find(r, invest.id).first();
					 receive_corpus = t_bill_invests.find(c,invest.id).first();
				 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		if(null == receive_money || receive_money == 0){
			receive_money = 0.0;
		}
		
		if(null == has_receive_money || has_receive_money == 0){
			has_receive_money = 0.0;
		}
		
		if(null == receive_corpus || receive_corpus == 0){
			receive_corpus = 0.0;
		}
		
		map.put("receive_money", df.format(receive_money));
		map.put("has_receive_money",  df.format(has_receive_money));
		map.put("remain_receive_money",  df.format(receive_money - has_receive_money));
		map.put("receive_corpus",  df.format(receive_corpus));
		
		
		
		return map;
	}
	
	
	
	
	
	/**
	 * 查询最大债权竞拍出价
	 * @param transfer_id
	 * @return
	 */
	public static Double queryMaxPrice(long transfer_id,ErrorInfo error){
		
		List<t_invest_transfer_details> list = null;
		Double maxOfferPrice = 0.0;
		
		try {
			list = t_invest_transfer_details.find("from t_invest_transfer_details where transfer_id=?", transfer_id).fetch();
		} catch (Exception e) {
			Logger.info(e.getMessage());
			e.printStackTrace();
			
			error.code = -1;
			
			return null;
		}
		
		if(list.size() > 0 && null != list){
			
			try {
				 maxOfferPrice = t_invest_transfer_details.find("select max(offer_price) from t_invest_transfer_details where transfer_id=?",transfer_id).first();
			} catch (Exception e) {
				Logger.info(e.getMessage());
				e.printStackTrace();
				
				error.code = -2;
				
				return null;
			}
			
			if(null == maxOfferPrice || maxOfferPrice == 0){
				maxOfferPrice = 0.0;
			}
			
		}
		
		error.code = 1;
		return maxOfferPrice;
	}
	
	/*
	 * 查询某一笔投资的已还金额（只算本金，不包括线下收款和本金垫付）
	 * */
	public static Double queryHasReceivedAmount(Long investId, ErrorInfo error){
		error.clear();
		
		Object rec = null;
		Double hasReceivedAmount = 0d;
		String sql = "SELECT SUM(receive_corpus) FROM t_bill_invests WHERE invest_id = ? AND status IN (?, ?) AND is_offline = ?";
		
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, investId).setParameter(2, Constants.OVERDUE_RECEIVABLES).setParameter(3, Constants.NORMAL_RECEIVABLES).setParameter(4, Constants.ZERO);
		try {
			 rec = query.getSingleResult(); 
		} catch (Exception e) {
			Logger.error("查询查询某一笔投资以及收到本金+利息+罚息：" + e.getMessage());
			
			error.code = -1;
			error.msg = "查询查询某一笔投资以及收到本金+利息+罚息有误";
			
			return 0d;
		}
		
		if (null == rec) {
			
			return 0d;
		}
		
		hasReceivedAmount = Double.parseDouble(rec.toString());
		error.code = 0;
		
		return hasReceivedAmount;
	}  
	
	/**
	 * 查询所有在前台显示的债权转让标
	 * @return
	 */
	public static PageBean<v_front_all_debts> queryAllDebtTransfers(int currPage,int pageSize,String loanType,String debtAmount,String apr,String orderType,String _keywords,ErrorInfo error){
		
		
		int debtAmountType = 0;
		int aprtype = 0;
		int order = 0;
		int loanTypes = 0;
		
		List<v_front_all_debts> debtList = new ArrayList<v_front_all_debts>();
		PageBean<v_front_all_debts> page = new PageBean<v_front_all_debts>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_DEBTS);
		sql.append(" and t_invest_transfers.type=2 ");
		
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(loanType) && StringUtils.isBlank(debtAmount) && StringUtils.isBlank(apr) && StringUtils.isBlank(orderType) && StringUtils.isBlank(_keywords)) {
			
			try {
				sql.append(" order by status,is_quality_debt desc");
				EntityManager em = JPA.em();
	            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            debtList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
			
			error.code = 1;
			page.page = debtList;
			
			return page;
		}
		 
			if(NumberUtil.isNumericInt(loanType)){
				loanTypes = Convert.strToInt(loanType, 0);
				sql.append(" and t_bids.product_id = ?");
				params.add(loanTypes);
			}
			
		
			if(NumberUtil.isNumericInt(debtAmount)){
				debtAmountType = Integer.parseInt(debtAmount);
			}
			
			if(!StringUtils.isBlank(_keywords)){
				sql.append(" and t_invest_transfers.title like ? ");
				params.add("%"+_keywords+"%");
			}
			if(debtAmountType < 0 || debtAmountType > 5){
				sql.append(SQLTempletes.DEBT_AMOUNT_CONDITION[0]);
			}else{
				sql.append(SQLTempletes.DEBT_AMOUNT_CONDITION[debtAmountType]);
			}
				
		
			if(NumberUtil.isNumericInt(apr)){
				aprtype = Integer.parseInt(apr);
			}
		
			if(aprtype < 0 || aprtype > 4){
				sql.append(SQLTempletes.BID_APR_CONDITION[0]);
			}else{
				sql.append(SQLTempletes.BID_APR_CONDITION[aprtype]);
			}
				
			if(NumberUtil.isNumericInt(orderType)){
				order = Integer.parseInt(orderType);
			}
			
			if(order < 0 || order > 9){
				sql.append(SQLTempletes.DEBT_ORDER_CONITION[0]);
			}else{
				sql.append(SQLTempletes.DEBT_ORDER_CONITION[order]);
			}
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            debtList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
		}
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("apr", aprtype);
		conditionMap.put("debtAmount", debtAmountType);
		conditionMap.put("orderType", order);
		conditionMap.put("keywords", _keywords);
		conditionMap.put("loanType", loanTypes);
		
		error.code = 1;
		page.page = debtList;
		page.conditions = conditionMap;
		
		return page;
	}
	
	
	
	
	/**
	 * 查询在竞拍状态的优质债权转让标(前两条)
	 * @return
	 */
	public static List<v_front_all_debts> queryQualityDebtTransfers(ErrorInfo error){
		error.clear();
		
		List<v_front_all_debts> debtList = new ArrayList<v_front_all_debts>();
		
		try {
			StringBuffer sql = new StringBuffer("");
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_FRONT_ALL_DEBTS);
			sql.append("and t_invest_transfers.status = 1 and t_invest_transfers.is_quality_debt = 1 order by t_invest_transfers.time desc");
			
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
            query.setMaxResults(2);
            debtList = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "由于数据库异常，导致优质债权转让标查询失败";
			
			return debtList;
		}
		   
		  error.code = 1;
		  return debtList;
		}
		
	
	
	/**
	 * 根据投资ID判断所投借款标是否是天标
	 * @param investId
	 * @param error
	 * @return
	 */
	public static boolean judgeIsDayBid(long investId,ErrorInfo error){
		
		String sql = "select bid_id from t_invests where id = ?";
		Long bidId = 0l;
		t_bids bid = null;
		try {
			bidId = t_invests.find(sql, investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(bidId > 0){
			try {
				bid = t_bids.findById(bidId);
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
			
			if(null != bid){
				if(bid.period_unit == 1){//天标单位
					
					error.code = 1;
					return true;
				}
			}
			
		}
		
		error.code = 1;
		return false;
	}
	
	
	/**
	 * 根据投资ID判断所投借款标是否是秒还标
	 * @param investId
	 * @param error
	 * @return
	 */
	public static boolean judgeIsSecBid(long investId,ErrorInfo error){
		
		String sql = "select bid_id from t_invests where id = ?";
		Long bidId = 0l;
		t_bids bid = null;
		try {
			bidId = t_invests.find(sql, investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(bidId > 0){
			try {
				bid = t_bids.findById(bidId);
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
			
			if(null != bid){
					error.code = 1;
					return bid.is_sec_bid;
			}
			
		}
		
		error.code = 1;
		return false;
	}
	
	
	
	
	
	
	/**
	 * 获取债权总额
	 * @param investId
	 * @param error
	 * @return
	 */
	public static double getDebtAmount(long investId,ErrorInfo error){
		
		Double hasReceivedAmount = 0.0;
		
		try {
			hasReceivedAmount =  t_bill_invests.find("select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where status in (-1,-2,-5,-6) and invest_id = ? ", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == hasReceivedAmount){
			hasReceivedAmount = 0.0;
		}
		
		error.code = 1;
		return hasReceivedAmount;
	}
	
	/**
	 * 获取债权待收本金,利息,罚息（本金垫付、线下收款也算已收款）
	 * @param investId
	 * @param error
	 * @return
	 */
	public static Map<String, Object> getReceiveCorpus(long userId,long bidId,long investId,ErrorInfo error){
		error.clear();
		Map<String, Object> obj = null;
		
		String sql = "select new Map(sum(receive_corpus) as receive_corpus, sum(receive_interest) as receive_interest, sum(overdue_fine) as overdue_fine) "
				+ "from t_bill_invests where status in (-1,-2,-5,-6) and user_id = ? and bid_id = ? and invest_id = ?";
		
		try {
			obj =  t_bill_invests.find(sql,userId,bidId,investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			
			return null;
		}
		
		error.code = 1;
		
		return obj;
	}
	
	
	/**
	 * 判断是否被管理员拉黑
	 * @param userId
	 * @param error
	 * @return
	 */
	public static boolean isBlack(long userId,ErrorInfo error){
		
		boolean flag = false;
		String sql = "select is_blacklist from t_users where id = ?";
		
		try {
			flag = t_users.find(sql, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		error.code = 1;
		return flag;
	}
	
	
	/**
	 * 处理该债权之前未通过的同样债权
	 * @param investId
	 * @param error
	 * @return
	 */
	public static long hasNopassDebt(long investId,ErrorInfo error){
		
		long result = -1;
		t_invest_transfers debt = null;
		
		try {
			debt = t_invest_transfers.find(" invest_id = ? and status = -1 ", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null != debt){
			result = debt.id;
		}
		
		return result;
	}
	
	
	/**
	 * 转让债权
	 * @param investId 投资ID
	 * @param transferTitle 债权转让标题
	 * @param transferReason 债权转让原因
	 * @param specifiedUserId 指定转让会员
	 * @param period 转让天数
	 * @param debtAmount 债权金额
	 * @param transferPrice 转让定价
	 * @param type 类型 1定向转让：  2:竞价转让
	 * @param error
	 * @return 返回-1代表转让债权失败，返回 1 代表成功
	 */
	public static int transferDebt(long userId,long investId,String transferTitle,String transferReason,int period,double debtAmount,double transferPrice,int type,String specifiedUserName,ErrorInfo error) {
		
		error.clear();
		EntityManager em = JPA.em();
		t_invests invest = new t_invests();
		
		try {
			invest = t_invests.findById(investId);
		} catch (Exception e) {
			e.printStackTrace();
			
			error.msg = "查询对应投资数据实体对象异常";
			error.code = -1;
			
			return error.code;
		}
		
		if(userId != invest.user_id){
			error.msg = "非法请求！";
			error.code= -2;
			
			return error.code;
		}
		
		
		
       if(!(invest.transfer_status == 0 && (constants.Constants.IS_DEBT_TWO || invest.transfers_id == 0))){//0 正常(转让入的也是0) -1 已转让出 1 转让中
			
			error.msg = "对不起！该债权不能进行转让操作！";
			error.code = -1;
			
			return error.code;
		}
		
		boolean black = isBlack(invest.user_id, error);
		
		if(error.code < 0){
			error.msg = "对不起！系统异常，请联系平台管理员！";
			error.code= -2;
			
			return error.code;
		}
		
		if(black){
			error.msg = "对不起！您被管理员限制操作，请联系平台管理员！";
			error.code= -2;
			
			return error.code;
		}
		

		boolean isDSecBid = judgeIsSecBid(investId, error);//判断是否是天标
		
		if(error.code < 0){
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			error.code= -2;
			
			return error.code;
		}
		
		if(isDSecBid){
			error.msg = "对不起！您投资的是秒还标，不能进行债权转让！";
			error.code= -3;
			
			return error.code;
		}
		
		if(debtAmount < transferPrice){
			error.msg = "对不起！您设置的竞拍底价不能高于债权总额！";
			error.code= -2;
			
			return error.code;
		}
		
		Map<String, Object> map = Debt.getReceiveCorpus(userId, invest.bid_id, investId, error);
		double receivedCorpus = Double.parseDouble(map.get("receive_corpus").toString()); // 投资本金
		double debtScale = Constants.DEBT_SCALE; // 转让上下限比例
		double ds = receivedCorpus * debtScale / 100;
		
		if(transferPrice > (receivedCorpus + ds) || transferPrice < (receivedCorpus - ds)){
			error.msg = "竞拍底价需在本金上下的" + debtScale + "%以内！";
			error.code= -2;
			
			return error.code;
		}
		
		t_users user = null;
		if(type==Constants.DIRECTIONAL_MODE){			
			if(!StringUtils.isBlank(specifiedUserName)){
				try {
					 user = t_users.find(" name = ? ", specifiedUserName).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
					error.code= -1;
					
					return error.code;
				}
				
				if(null == user){
					error.msg = "对不起！您指定的用户名不存在！";
					error.code = -1;
					
					return error.code;
				}
				
				if(userId == user.id){
					error.msg = "对不起！您指定的用户不能是自己！";
					error.code = -1;
					
					return error.code;
				}
				long uId = getUserIdByInvestId(investId,error);//债权借款用户
				
				if(uId == user.id){
					error.msg = "借款持有用户禁止参与该债权交易！";
					error.code = -1;
					
					return error.code;
				}
			}else{
				error.msg = "请输入债券定向转让人信息！";
				error.code = -1;
				
				return error.code;
			}
		}
		
		
		t_invest_transfers  investTransfer = new t_invest_transfers();
		investTransfer.invest_id = investId;
		investTransfer.title = transferTitle;
		
		/*转让状态(0审核中，1为竞拍中，2等待认购 3成功 -1审核不通过,-2流标*/
		investTransfer.status = 0; 
		investTransfer.time = new Date();
		investTransfer.transer_reason = transferReason;
		investTransfer.period = period;
		investTransfer.debt_amount = debtAmount;
		investTransfer.transfer_price = transferPrice;
		investTransfer.type = type;
		
		
		boolean flag=false;
		
		/* DIRECTIONAL_MODE =1;  定向模式*/
		if(Constants.DIRECTIONAL_MODE==type) {
			investTransfer.specified_user_id  = user.id;
			flag=true;
		}
		
		try {
			/*添加债权转让记录*/
			investTransfer.save();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			error.code= -1;
			
			return error.code;
		}
		
		String uuid = UUID.randomUUID().toString();
		Qrcode code = new Qrcode();
		
		String str = Constants.BASE_URL + "front/debt/debtDetails?debtId=" + investTransfer.id;
		try {
			Blob blob = new Blob();
			code.create(str, BarcodeFormat.QR_CODE, 100, 100, new File(blob.getStore(), uuid).getAbsolutePath(), "png");
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("创建二维码图片失败"+e.getMessage());
			error.code = -5;
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			JPA.setRollbackOnly();
			return error.code;
		}
		
		//保存二维码标识
		try {
			int a = em.createQuery("update t_invest_transfers set qr_code = ? where id = ?").setParameter(1, uuid).setParameter(2, investTransfer.id).executeUpdate();
		
			if(a == 0){
				error.code = -5;
				error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
				JPA.setRollbackOnly();
				return error.code;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -5;
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			JPA.setRollbackOnly();
			return error.code;
		}
		
		
		/*在债权竞拍记录表插入一条记录*/
		if(flag){
			t_invest_transfer_details details = new t_invest_transfer_details();
			details.transfer_id = investTransfer.id;
			details.time = new Date();
			details.user_id = user.id;
			details.offer_price = transferPrice;
			
			/*状态：0 正常；1 成交；2 待接受；-1 失败*/
			details.status = 2; 
			
			//添加事件
			DealDetail.userEvent(invest.user_id, UserEvent.DIRECTIONAL_MODE, "申请定向模式债权转让", error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			try {
				details.save();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
				error.code= -2;
				
				return error.code;
			}
			
			
		}
		
		try {
			/*改变投资表对应记录的状态值为 “转让中”*/
			int rows = em.createQuery("update t_invests set transfer_status = 1 where id=? and transfer_status = 0").setParameter(1, investId).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
				error.code= -3;
				
				return error.code;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			error.code= -3;
			
			return error.code;
		}
		
		
		//添加事件
		DealDetail.userEvent(invest.user_id, UserEvent.AUCTION_MODE, "申请竞拍模式债权转让", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//判断该债权之前是否有审核未通过记录，有的话直接置为流拍
		long result = hasNopassDebt(investId, error);
		
		if(error.code < 0){
			error.msg = "对不起！您此次债权转让操作失败！请您重试或联系平台管理员！";
			error.code= -3;
			
			return error.code;
		}
		
		if(result > 0){
			changeDebtStatus(result, Constants.DEBT_FLOW);
		}
		
		error.msg = "债权转让申请成功！请您耐心等待平台审核！谢谢！";
		error.code = 1;
		
		return error.code;
		
		
		
	}

	


	
	/**
	 * 债权审核通过操作
	 * @param investTransferId
	 * @param type AUCTION_MODE: 竞价模式  DIRECTIONAL_MODE：定向模式
	 * @param qualityStatus
	 * @param auditSupervisorId
	 * @param info
	 * @return 返回 -1 代表失败   返回 1 代表成功
	 */
	public static int auditDebtTransferPass(long investTransferId,int type,int qualityStatus,long auditSupervisorId,ErrorInfo error){
	
		error.clear();
		EntityManager em = JPA.em();
		t_invest_transfers transfer = null;
		t_invests invest = new t_invests();
		t_users user = new t_users();
		
		try {
			 transfer = t_invest_transfers.findById(investTransferId);
			 invest = t_invests.findById(transfer.invest_id);
			 user = t_users.findById(invest.user_id);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！";
			error.code = -1;
			
			return error.code;
		}
		
		//非转让中的状态不能审核通过
		if (Constants.INVEST_TRANSFERING != invest.transfer_status){
			error.msg = "转让已取消";
			error.code = -1;
			return -1;
		}
		if(null == transfer) {
			error.msg = "对不起！审核失败！";
			error.code = -1;
			
			return -1;
		}
		
		if(transfer.status != Constants.PENDING_AUDIT_STATUS && transfer.status != Constants.DEBT_NOPASS){
			error.msg = "对不起！该债权已经得到处理！";
			error.code = -1;
			
			return error.code;
		}
		
		
		/*AUCTION_MODE: 竞价模式*/
		if(Constants.AUCTION_MODE == type) {
			/* 0审核中，1为竞拍中，2等待认购 3成功-1审核不通过,-2流标，-3对方未接受，-4主动撤销，-5还款自动撤销,-6系统撤销*/
			transfer.status = 1;   
			
		}else{
			transfer.status = 2;
		}
		
		transfer.audit_supervisor_id = auditSupervisorId;
		
		if(qualityStatus == 1){
			transfer.is_quality_debt = true;
			//添加事件
			DealDetail.supervisorEvent(auditSupervisorId, SupervisorEvent.MARK_QUALITY_DEBT, "设置优质债权转让", error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
		}else{
			transfer.is_quality_debt = false;
		}
		
		transfer.start_time = new Date();
		transfer.end_time = DateUtil.dateAddDay(new Date(),transfer.period);
		
		try {
			transfer.save();
		} catch (Exception e2) {
			error.msg = "对不起，系统异常，审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		int rows = 0;
		
		try {
			/*0 正常(转让入的也是0) -1 已转让出 1 转让中*/
			rows = em.createQuery("update t_invests set transfer_status=1 where id = ? and transfer_status in(0,1)").setParameter(1, transfer.invest_id).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起，系统异常，审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.msg = "对不起，系统异常，审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		try {
			/* 更新竞拍表 */
			rows = em.createNativeQuery("update t_invest_transfer_details set status = ? where transfer_id = ?")
			.setParameter(1, Constants.AUCTION_MODE == type ? Constants.AUCTION_DETAIL_WAIT_CONFIRM : Constants.AUCTION_DETAIL_WAIT_ACCEPT)
			.setParameter(2, investTransferId)
			.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起，系统异常，审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		//发送邮件
		 String name = user.name;
		
		 TemplateEmail email = new TemplateEmail();
		 email.id = Templets.E_DEBT_PASS;
		 
		 if(email.status){
			 String econtent = email.content;
			 econtent = econtent.replace("userName", name);
			 econtent = econtent.replace("date", DateUtil.dateToString(transfer.time));
			 econtent = econtent.replace("title", transfer.title);
			 email.addEmailTask(user.email, email.title, econtent);
		 }
		 
		 
		 TemplateStation station = new TemplateStation();//发送站内信
		 station.id = Templets.M_DEBT_PASS;
		 
		 if(station.status){
			 String sContent = station.content;
			 sContent = sContent.replace("userName", name);	
			 sContent = sContent.replace("date", DateUtil.dateToString(transfer.time));	
			 sContent = sContent.replace("title", transfer.title);
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = user.id;
			 letter.title = station.title;
			 letter.content = sContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }
		//添加事件
		DealDetail.supervisorEvent(auditSupervisorId, SupervisorEvent.AUDIT_DEBT_TRANSFER, "审核转让债权通过", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 1;
		error.msg = "债权审核通过成功！";
		
		return error.code;
	}
	
	
	/**
	 * 债权审核不通过操作
	 * @param investTransferId
	 * @param auditSupervisorId
	 * @param noThroughReason
	 * @param qualityStatus
	 * @param info
	 * @return 返回 -1 代表操作失败   返回 1 代表操作成功
	 */
	public static int auditDebtTransferNoPass(long investTransferId,long auditSupervisorId,String noThroughReason,ErrorInfo error){
		
		error.clear();
		EntityManager em = JPA.em();
		t_invest_transfers transfer = new t_invest_transfers();
		t_invests invest = new t_invests();
		t_users user = new t_users();
		
		try {
			 transfer = t_invest_transfers.findById(investTransferId);
			 invest = t_invests.findById(transfer.invest_id);
			 user = t_users.findById(invest.user_id);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！";
			error.code = -1;
			
			return error.code;
		}
		
		if(transfer.status != 0 && transfer.status != -1){
			error.msg = "对不起！该债权已经得到处理！";
			error.code = -1;
			
			return error.code;
		}
		
		/* 0审核中，1为竞拍中，2等待认购 3成功 -1审核不通过,-2流标，-3对方未接受，-4主动撤销，-5还款自动撤销,-6系统撤销*/
		transfer.status = -1; 
		transfer.audit_supervisor_id = auditSupervisorId;
		transfer.start_time = new Date();
		transfer.no_through_reason = noThroughReason;
		transfer.end_time = DateUtil.dateAddDay(new Date(),transfer.period);
		transfer.is_quality_debt = false;
		
		try {
			transfer.save();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			error.msg="审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		int rows = 0;
		
		try {
			/* 0 正常(转让入的也是0) -1 已转让出 1 转让中*/
			rows = em.createNativeQuery("update t_invests set transfer_status=0 where id=? and transfer_status = 1").setParameter(1, transfer.invest_id).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			error.msg="审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.msg="审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		try {
			/* 把竞拍详情改为失败 */
			rows = em.createNativeQuery("update t_invest_transfer_details set status = -1 where transfer_id = ?").setParameter(1, investTransferId).executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			error.msg="审核失败！";
			error.code = -1;
			
			return error.code;
		}
		
		/*发送站内信通知操作*/
		 String name = user.name;
		
		 TemplateEmail email = new TemplateEmail();
		 email.id = Templets.E_DEBT_NO_PASS;
		 
		 if(email.status){
			 String econtent = email.content;
			 econtent = econtent.replace("userName", name);
			 econtent = econtent.replace("date", DateUtil.dateToString(transfer.time));
			 econtent = econtent.replace("title", transfer.title);
			 email.addEmailTask(user.email, email.title, econtent);
		 }
		 
		 
		 TemplateStation station = new TemplateStation();//发送站内信
		 station.id = Templets.M_DEBT_NO_PASS;
		 
		 if(station.status){
			 String sContent = station.content;
			 sContent = sContent.replace("userName", name);	
			 sContent = sContent.replace("date", DateUtil.dateToString(transfer.time));	
			 sContent = sContent.replace("title", transfer.title);
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = user.id;
			 letter.title = station.title;
			 letter.content = sContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }
		
		//添加事件
		DealDetail.supervisorEvent(auditSupervisorId, SupervisorEvent.AUDIT_DEBT_TRANSFER, "审核转让债权不通过", error);
		
		if(error.code < 0){
			error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
			error.code = -9; 
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 1;
		error.msg = "债权审核不通过操作成功！";
		
		return error.code;
	}
	
	
	
	
	/**
	 * 拒绝接收债权转让
	 * @param investTransferId
	 * @param info
	 * @return 返回 1 代表操作成功，-1 代表失败
	 */
	public static  int refuseAccept(long investTransferId,ErrorInfo error){
		
		error.clear();
		EntityManager em = JPA.em();
		t_invest_transfers transfer = new t_invest_transfers();
		t_invest_transfer_details detail = new t_invest_transfer_details();
		
		try {
		    transfer = t_invest_transfers.findById(investTransferId);
		    detail = t_invest_transfer_details.find(" transfer_id = ?", investTransferId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！";
			error.code = -1;
			
			return error.code;
		}
		
		
		if(transfer.status != Constants.DEBT_ACCEPT){
			error.msg = "对不起！该债权您已经处理过了，请不要重复操作！";
			error.code = -1;
			
			return error.code;
		}
		
		long investId = transfer.invest_id;
		
		try {
			/*对方未接受*/
			int a = em.createQuery("update t_invest_transfers set status=-3 ,failure_time =? where id = ? and status = 2").setParameter(1, new Date()).setParameter(2, investTransferId).executeUpdate();
			int b = em.createQuery("update t_invests set transfer_status=0  where id=? and transfer_status = 1").setParameter(1, investId).executeUpdate();
			
			/*失败*/
			int c = em.createQuery("update t_invest_transfer_details set status=-1   where transfer_id=? and status = 2").setParameter(1, investTransferId).executeUpdate();
			
			if(a == 0 || b == 0 || c == 0){
				JPA.setRollbackOnly();
				error.msg = "拒绝操作失败！";
				error.code = -1;
				
				return error.code;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "拒绝操作失败！";
			error.code = -1;
			
			return error.code;
		}
		
		
		t_invests invest = new t_invests();
		t_users user = new t_users();
		
		try {
			 invest = t_invests.findById(investId);
			 user = t_users.findById(invest.user_id);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！";
			error.code = -1;
			
			return error.code;
		}
		
		//发送消息
		 TemplateEmail email = new TemplateEmail();//发送邮件
		 email.id = Templets.E_DEBT_TRANSFER_FAILURE;
		 
		 if(email.status){
			 String econtent = email.content;
			 econtent = econtent.replace("userName", user.name);
			 econtent = econtent.replace("date",DateUtil.dateToString(transfer.time));
			 econtent = econtent.replace("describe", transfer.title);
			 econtent = econtent.replace("money",  DataUtil.formatString(transfer.debt_amount));
			 email.addEmailTask(user.email, email.title, econtent);
		 }
		
		 
		 TemplateStation station = new TemplateStation();//发送站内信
		 station.id = Templets.M_DEBT_TRANSFER_FAILURE;
		 
		 if(station.status){
			 String sContent = station.content;
			 sContent = sContent.replace("userName", user.name);	
			 sContent = sContent.replace("date",DateUtil.dateToString(transfer.time));
			 sContent = sContent.replace("describe", transfer.title);
			 sContent = sContent.replace("money",  DataUtil.formatString(transfer.debt_amount));
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = user.id;
			 letter.title = station.title;
			 letter.content = sContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }		 
		 
		//添加事件
		DealDetail.userEvent(detail.user_id, UserEvent.REFUSE_DEBT, "拒绝受让定向转让债权", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		error.code = 1;
		error.msg = "拒绝操作成功";
		return 1;
	}
	
	/**
	 * 判断是否收藏了该债权，并返回收藏id
	 * @param userId
	 * @param investTransferId
	 * @param error
	 * @return
	 */
	public static long isAttentionDebt(long userId,long investTransferId,ErrorInfo error) {
		error.clear();
		
		t_user_attention_invest_transfers  userAttentionInvestTransfer = null;
		
		try{
			userAttentionInvestTransfer = t_user_attention_invest_transfers.find("user_id=? and invest_transfer_id=?", userId,investTransferId).first();
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("判断用户是否关注该债权时：" + e.getMessage());
			
			error.code = -1;
			error.msg = "判断用户是否关注该债权失败";
			
			return error.code;
		}
		
		if(userAttentionInvestTransfer == null) {
			error.code = -1;
			error.msg = "未收藏该债权";
			
			return error.code;
		}
		
		return userAttentionInvestTransfer.id;
	}
	
	/**
	 * 收藏债权
	 * @param userId
	 * @param investTransferId
	 */
	public static long collectDebt(long userId,long investTransferId,ErrorInfo error){
		
		error.clear();
		
		long investUserId = getInvestUserId(investTransferId, error);
		
		if(error.code < 0){
			error.msg = "对不起！您此次收藏该债权失败！";
			error.code = -2;
			
			return error.code;
		}
		
		if(investUserId == userId){
			error.msg = "对不起！您不能收藏自己的债权！";
			error.code = -2;
			
			return error.code;
		}
		
		t_user_attention_invest_transfers  userAttentionInvestTransfer = t_user_attention_invest_transfers.find("from t_user_attention_invest_transfers where user_id=? and invest_transfer_id=?", userId,investTransferId).first();
		
		if(null==userAttentionInvestTransfer){
			t_user_attention_invest_transfers  userAttentionInvestTransfers = new t_user_attention_invest_transfers();
			
			userAttentionInvestTransfers.user_id = userId;
			userAttentionInvestTransfers.time = new Date();
			userAttentionInvestTransfers.invest_transfer_id = investTransferId;
			
			try {
				userAttentionInvestTransfers.save();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！您此次收藏该债权失败！请您重试或联系平台管理员！";
				error.code = -1;
				
				return error.code;
			}
			
			//添加事件
			DealDetail.userEvent(userId, UserEvent.COLLECT_DEBT, "收藏转让债权", error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			error.msg = "收藏债权成功！";
			error.code = 1;
			
			return userAttentionInvestTransfers.id;
		}
		error.msg = "对不起！您已经收藏过该债权！";
		error.code = -2;
		
		return error.code;
	}
	
	
	/**
	 * 删除收藏的债权
	 * @param userId
	 * @param investTransferId
	 */
	public int deleteAttentionInvestTransfer(long userId,long investTransferId,ErrorInfo error){
		
		t_user_attention_invest_transfers  userAttentionInvestTransfers = null;
		
		try {
			 userAttentionInvestTransfers = t_user_attention_invest_transfers.find(" user_id=? and invest_transfer_id=?", userId,investTransferId).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			
			error.msg = "对不起！系统异常，在此给你造成的不便敬请谅解！";
			error.code = -1;
			
			return error.code;
			 
		}
		 
		if(null == userAttentionInvestTransfers){
			error.msg = "对不起！该债权已经不在你的收藏列表！";
			error.code = -2;
			
			return error.code;
		}
		
		userAttentionInvestTransfers.delete();
		error.msg = "操作成功！";
		error.code = 1;
		
		return error.code;
	}
	
	/**
	 * 查询债权转让信息
	 * @param debtId
	 * @return
	 */
	public static Map<String, Object> queryTransferInfo(long debtId, ErrorInfo error) {
		error.clear();
		
		t_invest_transfers debt = null;
		t_invests invest = null;
		t_bids bid = null;
		
		try {
			debt = t_invest_transfers.findById(debtId);
			invest = t_invests.findById(debt.invest_id);
			bid = t_bids.findById(invest.bid_id);
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			
			return null;
		}
		
		if (debt == null || invest == null || bid == null) {
			return null;
		}
		
		//借款人id
		long bidUserId = bid.user_id;  
		
		//投资人id
		long fromUserId = invest.user_id;
		
		//借款标编号
		String bidNo = bid.bid_no;
		
		//投资流水号
		String creMerBillNo = invest.mer_bill_no;
		
		Map<String, Object> debtAmount = Debt.getReceiveCorpus(invest.user_id, invest.bid_id, invest.id, error); 
		
		//债权待收本金
		double corpus = Double.parseDouble(debtAmount.get("receive_corpus").toString());
		
		//债权待收利息
		double interest = Double.parseDouble(debtAmount.get("receive_interest").toString());
		
		//已还金额(只算本金，不包含本金垫付和线下收款)
		double printAmt = queryHasReceivedAmount(invest.id, error); 
		
		//债权成交金额
		double pPayAmt = queryMaxPrice(debtId, error);
		
		//受让人
		t_invest_transfer_details auctionDebtRecord = t_invest_transfer_details.find(" offer_price=? and transfer_id=? ", pPayAmt,debtId).first();
		long toUserId = auctionDebtRecord.user_id;
		
		//转让管理费
		double percent = BackstageSet.getCurrentBackstageSet().debtTransferFee;//债权转让管理费费率
		double managefee = debtTransferMaxfee(invest.id, percent);
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("bidUser", bidUserId); //借款人
		map.put("fromUserId", fromUserId); //投资人
		map.put("toUserId", toUserId); //受让人
		map.put("bidNo", bidNo); ///标的编号
		map.put("pCreMerBillNo", creMerBillNo); //投资流水号
		map.put("pCretAmt", corpus);  //债权面额（只算本金）（环讯用）
		map.put("interest", interest);  //债权待收利息
		map.put("pCretAmt2", corpus);  //转让金额（只算本金）（汇付用）
		map.put("pPayAmt", pPayAmt);  //成交金额
		map.put("managefee", managefee);  //转让管理费，收转让人
		map.put("orderDate", DateUtil.simple(invest.time));
		map.put("printAmt", printAmt);  //已还金额 (只算本金)（汇付用）
		map.put("bid", bid);
		return map;
	}
	/**
	 * 获取最大债权转让手续费，费率超过100%则按照100%取。
	 * version 8.1.2
	 * @param id
	 * @param percent
	 * @return
	 */
	public static double debtTransferMaxfee(long id, double percent){
		double temp = new BigDecimal(percent).compareTo(new BigDecimal(Constants.DEBT_TRANSFER_MAXRATE))>0 ? 
				Constants.DEBT_TRANSFER_MAXRATE : percent;
		return Double.parseDouble(Debt.debtBidReceiveSituation(id).get("remainReceivedInterest"))*temp/100;
	}
	/**
	 * 更新订单号
	 * @param id
	 * @param merBillNo
	 * @param error
	 */
	public static void updateMerBillNo(long id, String merBillNo, ErrorInfo error) {
		error.clear();
		
		String sql = "update t_invest_transfers set mer_bill_no = ? where id = ?";
		EntityManager em = JPA.em();
		Query query = em.createQuery(sql).setParameter(1, merBillNo).setParameter(2, id);
		int rows = 0;

		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.info(e.getMessage());
			error.code = -1;
			error.msg = "数据库异常";
			
			return;
		}
		
		if (rows == 0) {
			error.code = -1;
			error.msg = "数据未更新";
			
			return;
		}
		
		error.code = 0;
		error.msg = "更新订单号成功";
	}
	
	/**
	 * 受让人是否有足够的钱成交债权
	 * @param userId
	 * @param debtId
	 * @param error
	 * @return
	 */
	public static boolean isEnoughToDealDebt(long transferId, ErrorInfo error) {
		error.clear();
		t_invest_transfer_details transferDetails = null;
		t_users assignUser = null;
		
		try {
			transferDetails = t_invest_transfer_details.find("transfer_id=?", transferId).first();
			/*债权转让指定人*/
			assignUser = t_users.findById(transferDetails.user_id); 
		} catch (Exception e) {
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "数据库异常！";
			
			return false;
		}
		
		if(assignUser.balance < transferDetails.offer_price){
			error.code = -999;
			error.msg = "对不起！您可用余额不足，请及时充值";
			
			return false;
		}
		
		return true;
	}
	/**
	 * 确认债权成交，记录对应竞拍者的资金转移
	 * @param paymentMerBillNo 成交流水号
	 * @param investTransferId 转让人id
	 * @param dealpwdStr 交易密码
	 * @param isRepair 是否是补单操作
	 * @param error
	 * @return  返回 1 代表操作成功
	 */
	public static int dealDebtTransfer(String paymentMerBillNo, long investTransferId, String dealpwdStr, boolean isRepair,ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		Double maxOfferPrice = 0.0;
		t_invest_transfer_details  auctionDebtRecord = new t_invest_transfer_details();
		t_invest_transfers debt = new t_invest_transfers();
		t_invests invest = new t_invests();
		t_users assignUser = new t_users();
		t_bids bid = new t_bids();
		t_products product = new t_products();
		List<Map<String,Object>> userIdMap = null;
		t_invest_transfer_details transferDetails = new t_invest_transfer_details();
		t_invest_transfers transfers = new t_invest_transfers();
		DealDetail detail = null;
		Map<String, Double> funds = new HashMap<String, Double>();
		BackstageSet set = BackstageSet.getCurrentBackstageSet();
		double percent = set.debtTransferFee;//债权转让管理费费率
		
		try {
			debt = t_invest_transfers.findById(investTransferId);
			invest = t_invests.findById(debt.invest_id);
			bid = t_bids.findById(invest.bid_id);
			product = t_products.findById(bid.product_id);
			
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		if(debt.status == Constants.TRANSFER_STATUS){
			error.msg = "对不起！该债权还没有审核，请耐心等待！";
			error.code = -1;
			
			return error.code;
		}
		
		
		if(debt.status != Constants.DIRECTIONAL_MODE && debt.status != Constants.AUCTION_MODE && debt.status != Constants.WAIT_CONFIRM){
			error.msg = "对不起！该债权已经不处于待成交状态！";
			error.code = -1;
			
			return error.code;
			
		}
		
		
		 maxOfferPrice = queryMaxPrice(investTransferId, error);
		 
		 if(error.code < 0){
			    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
		 }
		
		 if(maxOfferPrice == 0){//说明没有竞拍记录，不能执行成交操作
			 
			    error.msg = "对不起！该债权暂时没有用户参与竞拍，暂时不能成交！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
		 }
		 
		if(debt.specified_user_id <= 0){//没有指定用户说明是竞价模式
			
			
			/*修改债权相关状态字段*/
		     try {
			     auctionDebtRecord = t_invest_transfer_details.find(" offer_price=? and transfer_id=? ", maxOfferPrice,investTransferId).first();
		    	 debt = t_invest_transfers.findById(auctionDebtRecord.transfer_id);
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
			}
		    
			if(product.is_deal_password && !isRepair){
				t_users user = t_users.findById(auctionDebtRecord.user_id);
				if(dealpwdStr != null){
					if (!Encrypt.MD5(dealpwdStr + Constants.ENCRYPTION_KEY).equals(
							user.pay_password)) {
						error.msg = "对不起！交易密码错误!";
						error.code = -13;
						return error.code;
					}
				}
			}
			
			debt.transaction_price = maxOfferPrice;
			debt.transaction_time = new Date();
			debt.transaction_user_id = auctionDebtRecord.user_id;
			
			/*3 成功*/
			debt.status = Constants.DEBT_SUCCESS;   
			
			try {
				
				debt.save();          
				/*已转让出,transfer_status=-1*/
				int a = em.createQuery("update t_invests set transfer_status = -1 where id = ? and transfer_status = 1").setParameter(1, debt.invest_id).executeUpdate();
				
				if(a == 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				
			
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			//更新竞拍记录状态
			int rows = changeAuctionStatus(investTransferId, auctionDebtRecord.user_id, Constants.AUCTION_DETAIL_DEAL);
			
			if(rows == 0){
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			/*原始投资人*/
			long originalInvestUserId = invest.user_id;
			
			DataSafety data1 = new DataSafety();
			data1.setId(originalInvestUserId);//数据防篡改（针对债权所有者）
			boolean flag1 = data1.signCheck(error);
			
			if(error.code < 0){
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -11;
				
				return error.code;
			}
			
			if(!flag1){
				error.msg = "对不起！尊敬的用户，债权所有者账户资金出现异常变动，暂时不能成交该债权，请速联系管理员！";
				error.code = -11;
				
				return error.code;
			}
			
            t_invests tInvest = new t_invests();//债权竞拍成功在投资表里面插入新纪录
			
			tInvest.user_id = auctionDebtRecord.user_id;
			tInvest.time = new Date();
			tInvest.bid_id = invest.bid_id;
			tInvest.amount = invest.amount;
			tInvest.correct_amount = invest.correct_amount;
			tInvest.correct_interest = invest.correct_interest;
			tInvest.transfer_status = 0; 
			tInvest.transfers_id = investTransferId;
			tInvest.client = invest.client;  //债权转让后，重新生成一条新的投资记录，需要把该条投资记录的来源也加进来（用于后台理财情况数据统计分析）
			tInvest.mer_bill_no = paymentMerBillNo;
			
			tInvest.save();  
			
			/*现在投资人*/
			long presentInvestUserId = auctionDebtRecord.user_id;
			
			DataSafety data2 = new DataSafety();
			data2.setId(presentInvestUserId);//数据防篡改（针对债权竞拍者）
			boolean flag2 = data2.signCheck(error);
			
			if(error.code < 0){
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -11;
				
				return error.code;
			}
			
			if(!flag2){
				error.msg = "对不起！尊敬的用户，您的账户资金出现异常变动，暂时不能成交该债权，请速联系管理员！";
				error.code = -11;
				
				return error.code;
			}
			
			/*标ID*/
			long bidId = invest.bid_id;
			
			//计算债权转让管理费
			double managefee = Double.parseDouble( Debt.debtBidReceiveSituation(invest.id).get("remainReceivedInterest"))*percent/100;
			 
			/*更新账单表*/
			Bill.investBillsTransfer(bidId, originalInvestUserId,invest.id, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			Bill.debtTransfer(paymentMerBillNo, bidId, originalInvestUserId, presentInvestUserId,invest.id,tInvest.id, error);
			
			 if(error.code < 0){
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
			    
			    /*出价最高用户冻结资金减少*/
			    DealDetail.minusUserFreezeFund(presentInvestUserId, maxOfferPrice, error);
				
				if (error.code < 0) {

					return error.code;
				}
			    
				//添加交易记录
				 funds = DealDetail.queryUserFund(presentInvestUserId, error);
				
				if(error.code < 0){
					JPA.setRollbackOnly();
					return error.code;
				}
				
				 detail = new DealDetail(presentInvestUserId, DealType.CHARGE_FREEZE_AUCTIONAMOUNT, maxOfferPrice, investTransferId, funds.get("user_amount"), funds.get("freeze"), funds.get("receive_amount"), "债权竞拍成功，冻结金额减少");
				 detail.addDealDetail(error);
				
				 if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
				 
				    data2.updateSignWithLock(presentInvestUserId, error);//债权竞拍用户更新防数据篡改字段
				    
				    if(error.code < 0){
				    	error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
						error.code = -11;
				    	JPA.setRollbackOnly();
						return error.code;
				    }
				    
			
				
				/*债权转让人可用余额增加*/
				 
				 int result2 = DealDetail.addUserFund(originalInvestUserId, maxOfferPrice);
				 
				 if(result2 <= 0 ){
					 JPA.setRollbackOnly();
						
					 return error.code;
				 }
				
				
				//添加交易记录
				 funds = DealDetail.queryUserFund(originalInvestUserId, error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				 detail = new DealDetail(originalInvestUserId, DealType.AUCTION_DEBT_SUCCESS, maxOfferPrice, investTransferId, funds.get("user_amount"), funds.get("freeze"), funds.get("receive_amount"), "竞拍模式债权转让成功，增加可用余额");
				 detail.addDealDetail(error);
				
				 if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
				 //更新防数据篡改字段
				 data1.updateSignWithLock(originalInvestUserId, error);
				 
				 if(error.code < 0){
						error.msg = "对不起！系统异常，请联系平台管理员！";
						error.code = -1;
						JPA.setRollbackOnly();
						return error.code;
					}
				 
				 //扣除债权人转让管理费
				 DealDetail.minusUserFund(originalInvestUserId, managefee, error);
				 
				 if (error.code < 0) {

					return error.code;
				 }
				
				 funds = DealDetail.queryUserFund(originalInvestUserId, error);
					
				 if(error.code < 0){
					error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
					JPA.setRollbackOnly();
					
					return error.code;
				 }
				 
				 //添加扣除管理费交易记录
				 detail = new DealDetail(originalInvestUserId, DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE, managefee, investTransferId, funds.get("user_amount"), funds.get("freeze"), funds.get("receive_amount"), "竞拍模式债权转让成功，扣除转让管理费");
				 detail.addDealDetail(error);
				 
				 if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
				 
					//-------------------发送消息----------------------- 
				 t_users debtUser = new t_users();
				 t_users auctionUser = new t_users();
				//发送通知
			    try {
			    	 debtUser = t_users.findById(originalInvestUserId);//债权所有者
					 auctionUser = t_users.findById(originalInvestUserId);//债权竞拍用户
				} catch (Exception e) {
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
			    	JPA.setRollbackOnly();
					return error.code;
				}
				
				
				 if(error.code < 0){
				    	error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
						error.code = -11;
				    	JPA.setRollbackOnly();
						return error.code;
				  }
				  
				 
				 //发送消息(竞拍成功)
				 TemplateEmail email = new TemplateEmail();//发送邮件
				 email.id = Templets.E_AUCTION_SUCCESS;
				 
				 if(email.status){
					 String content = email.content;
					 content = content.replace("userName", auctionUser.name);
					 content = content.replace("date", DateUtil.dateToString(debt.time));
					 content = content.replace("describe", debt.title);
					 content = content.replace("money",  DataUtil.formatString(debt.transaction_price));
					 email.addEmailTask(auctionUser.email, email.title, content);
				 }
				
				 
				 TemplateStation station = new TemplateStation();//发送站内信
				 station.id = Templets.M_AUCTION_SUCCESS;
				 
				 if(station.status){
					 String sContent = station.content;
					 sContent = sContent.replace("userName", auctionUser.name);	
					 sContent = sContent.replace("date", DateUtil.dateToString(debt.time));		
					 sContent = sContent.replace("describe", debt.title);
					 sContent = sContent.replace("money",  DataUtil.formatString(debt.transaction_price));
					 
					 StationLetter letter = new StationLetter();
					 letter.senderSupervisorId = 1;
					 letter.receiverUserId = auctionUser.id;
					 letter.title = station.title;
					 letter.content = sContent;
					 
					 letter.sendToUserBySupervisor(error);
				 }
				
				 
				 
				 
				 //发送消息（债权转让成功）
				 email.id = Templets.E_DEBT_TRANSFER_SUCCESS;
				 
				 if(email.status){
					 String content1 = email.content;
					 content1 = content1.replace("date", DateUtil.dateToString(debt.time));
					 content1 = content1.replace("userName", debtUser.name);			
					 content1 = content1.replace("describe", debt.title);
					 content1 = content1.replace("money",  DataUtil.formatString(debt.transaction_price));  
					 content1 = content1.replace("debtReward",  DataUtil.formatString(managefee));          
					 email.addEmailTask(debtUser.email, email.title, content1);
				 }
				 
				 station = new TemplateStation();//发送站内信
				 station.id = Templets.M_DEBT_TRANSFER_SUCCESS;
				 
				 if(station.status){
					 String stationContent1 = station.content;
					 stationContent1 = stationContent1.replace("date",DateUtil.dateToString(debt.time));
					 stationContent1 = stationContent1.replace("userName", debtUser.name);			
					 stationContent1 = stationContent1.replace("describe", debt.title);
					 stationContent1 = stationContent1.replace("money",  DataUtil.formatString(debt.transaction_price));
					 stationContent1 = stationContent1.replace("debtReward",  DataUtil.formatString(managefee));
					 
					 StationLetter letter = new StationLetter();
					 letter.senderSupervisorId = 1;
					 letter.receiverUserId = debtUser.id;
					 letter.title = station.title;
					 letter.content = stationContent1;
					 
					 letter.sendToUserBySupervisor(error);
				 }
				 
				//-------------------------------------------------------------- 
				 
				//更新防数据篡改字段
				 data1.updateSignWithLock(originalInvestUserId, error);
				 
				 if(error.code < 0){
						error.msg = "对不起！系统异常，请联系平台管理员！";
						error.code = -1;
						JPA.setRollbackOnly();
						return error.code;
					}
				 
				 //添加平台交易记录
				 DealDetail.addPlatformDetail(DealType.TRANSFER_FEE, investTransferId, originalInvestUserId, -1, DealType.ACCOUNT, managefee, 1, "扣除债权转让管理费", error);
				
				 if(error.code < 0){
					 JPA.setRollbackOnly();
						
					  return error.code;
				 }
				 
				 
				 
				 
				/*解冻竞拍失败者冻结资金*/
				String sql="select new Map(user_id as userId,offer_price as offerPrice ) from t_invest_transfer_details  where transfer_id=? and offer_price < ?";

				try {
					userIdMap = t_invest_transfer_details.find(sql,investTransferId, maxOfferPrice).fetch();
							
				} catch (Exception e) {
					e.printStackTrace();
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();

					return error.code;
				}
			
			
			DataSafety d = new DataSafety();
			if(null != userIdMap){
				
				for(Map<String,Object> map : userIdMap) {
					
					long userId = Long.parseLong(map.get("userId").toString());
					double offerPrice = Double.parseDouble(map.get("offerPrice").toString());
					
					//解冻竞拍资金
					DealDetail.relieveFreezeFund(userId, offerPrice, error);

					if (error.code < 0) {
						
						return error.code;
					}
					
					//添加交易记录
					 funds = DealDetail.queryUserFund(userId, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					double user_amount = funds.get("user_amount");
					double freeze = funds.get("freeze");
					double receive_amount = funds.get("receive_amount");
					
					 detail = new DealDetail(
							 userId, 
							 DealType.THAW_FREEZE_AUCTIONAMOUNT, 
							 offerPrice, 
							 investTransferId, 
							 user_amount, 
							 freeze, 
							 receive_amount, 
							 "竞拍债权失败，解冻竞拍金额");
					 detail.addDealDetail(error);
					
					 if(error.code < 0){
							JPA.setRollbackOnly();
							
							return error.code;
					}
					 
					t_users user = new t_users();
					
					try {
						user = t_users.findById(userId);
					} catch (Exception e) {
						e.printStackTrace();
						error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
						error.code = -111;
						JPA.setRollbackOnly();

						return error.code;
					}
					
					//发送消息(竞拍失败)
					 email = new TemplateEmail();//发送邮件
					 email.id = Templets.E_AUCTION_FAILURE;
					 
					 if(email.status){
						 String content2 = email.content;
						 content2 = content2.replace("userName", user.name);
						 content2 = content2.replace("date", DateUtil.dateToString(debt.time));
						 content2 = content2.replace("describe", debt.title);
						 content2 = content2.replace("money",  DataUtil.formatString(offerPrice));
						 email.addEmailTask(user.email, email.title, content2);
					 }

					 station = new TemplateStation();//发送站内信
					 station.id = Templets.M_AUCTION_FAILURE;
					 
					 if(station.status){
						 String stationContent2 = station.content;
						 stationContent2 = stationContent2.replace("userName", user.name);
						 stationContent2 = stationContent2.replace("date", DateUtil.dateToString(debt.time));
						 stationContent2 = stationContent2.replace("describe", debt.title);
						 stationContent2 = stationContent2.replace("money",  DataUtil.formatString(offerPrice));
						 
						 StationLetter letter = new StationLetter();
						 letter.senderSupervisorId = 1;
						 letter.receiverUserId = user.id;
						 letter.title = station.title;
						 letter.content = stationContent2;
						 
						 letter.sendToUserBySupervisor(error);
					 }
					
					//更新数据防篡改字段
					d.updateSignWithLock(userId, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						return error.code;
					}
					
					/*修改竞拍记录表状态值*/
					
					int resultc = changeAuctionStatus(investTransferId, userId,Constants.AUCTION_DETAIL_FAILURE);
					if(resultc < 0){
						JPA.setRollbackOnly();
						return -1;
					}
				}
			}
			
			
		
			 
			 //添加事件
			DealDetail.userEvent(originalInvestUserId, UserEvent.ACCEPT_DEBT_TRANSFER, "债权成交操作成功", error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			
			//奖励债权竞拍用户推广用户CPS奖励
			User.rewardCPS(presentInvestUserId, managefee, investTransferId, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			Debt.creatDebtAgreement(originalInvestUserId, presentInvestUserId, investTransferId,tInvest.id, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				error.msg = "债权成交操作失败！";
				error.code = -1;
				return error.code;
			}
			error.msg = "债权成交操作成功！";
			error.code = 1;
			return error.code;
			
		}else{
			
			/*定向转让模式*/
			try {
				 transferDetails = t_invest_transfer_details.find(" transfer_id=?", investTransferId).first();
				 transfers = t_invest_transfers.findById(transferDetails.transfer_id);
				
				/*债权转让指定人*/
				 assignUser = t_users.findById(transferDetails.user_id); 
				
				/*原始投资记录*/
				 invest=t_invests.findById(transfers.invest_id);
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
			}
			   
			if(product.is_deal_password && !isRepair){
				if(dealpwdStr!=null){
					if (!Encrypt.MD5(dealpwdStr + Constants.ENCRYPTION_KEY).equals(
							assignUser.pay_password)) {
						error.msg = "对不起！交易密码错误!";
						error.code = -13;
						return error.code;
					}
				}
			}
				
				/*原始投资人*/
				long originalInvestUserId=invest.user_id;
				
				DataSafety data1 = new DataSafety();
				data1.setId(originalInvestUserId);//数据防篡改（针对债权所有者）
				boolean flag1 = data1.signCheck(error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
					
					return error.code;
				}
				
				if(!flag1){
					error.msg = "对不起！尊敬的用户，债权所有者账户资金出现异常变动，暂时不能成交该债权，请速联系管理员！";
					error.code = -11;
					
					return error.code;
				}
				
				/*现在投资人*/
				long presentInvestUserId=assignUser.id;
				
				DataSafety data2 = new DataSafety();
				data2.setId(presentInvestUserId);//数据防篡改（针对债权所有者）
				boolean flag2 = data1.signCheck(error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
					
					return error.code;
				}
				
				if(!flag2){
					error.msg = "对不起！尊敬的用户，债权所有者账户资金出现异常变动，暂时不能成交该债权，请速联系管理员！";
					error.code = -11;
					
					return error.code;
				}
				
				/*投资标ID*/
				long bidId=invest.bid_id;
				
				if(assignUser.balance < transferDetails.offer_price){
					error.msg = "对不起！您可用余额不足，请及时充值";
					error.code = -999;
					
					return error.code;
				}
				
				/*3 成功*/
				transfers.status = 3;	 
				transfers.transaction_price  = transferDetails.offer_price;
				transfers.transaction_time = new Date();
				transfers.transaction_user_id = transferDetails.user_id;
				
				try {
					transfers.save();
					
				} catch (Exception e) {
					e.printStackTrace();
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				//计算债权转让管理费
				double managefee = Arith.round(Double.parseDouble( Debt.debtBidReceiveSituation(invest.id).get("remainReceivedInterest"))*percent/100,2);
				
				/*更新账单表*/
				Bill.investBillsTransfer(bidId, originalInvestUserId,invest.id, error);
				
				if(error.code < 0){
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
			
				 
				
				//冻结指定用户可用余额
				DealDetail.freezeFund(assignUser.id, transferDetails.offer_price, error);
				
				if (error.code < 0) {

					return error.code;
				}
				 funds = DealDetail.queryUserFund(assignUser.id, error);
				 
				 if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
					JPA.setRollbackOnly();
					
					return error.code;
				 }
				
				double user_amount = funds.get("user_amount");
				double freeze = funds.get("freeze");
				double receive_amount = funds.get("receive_amount");
					
				//添加冻结可用余额交易记录
				 detail = new DealDetail(assignUser.id, DealType.FREEZE_DEBT, transferDetails.offer_price, investTransferId, 
						 user_amount , freeze, receive_amount, "冻结竞拍金额");
				 detail.addDealDetail(error);
				
				if(error.code < 0){
					JPA.setRollbackOnly();
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
					
					return error.code;
				}
				
				data2.updateSignWithLock(presentInvestUserId, error);
				
				if(error.code < 0){
					JPA.setRollbackOnly();
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -11;
					
					return error.code;
				}
				
				 
				/*更改投资表状态  已转让出*/
				int result = changeInvestStatus(transfers.invest_id, -1);
				if(result < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				
				/*债权竞拍成功在投资表里面插入新纪录*/
				t_invests tInvest = new t_invests();
				
				tInvest.user_id = transferDetails.user_id;
				tInvest.time = new Date();
				tInvest.bid_id = invest.bid_id;
				tInvest.amount = invest.amount;
				tInvest.correct_amount = invest.correct_amount;
				tInvest.correct_interest = invest.correct_interest;
				tInvest.transfer_status = 0; 
				tInvest.transfers_id = investTransferId;
				tInvest.mer_bill_no = paymentMerBillNo;
				
					try {
						tInvest.save();  
					} catch (Exception e) {
						error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
						error.code = -111;
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					Bill.debtTransfer(paymentMerBillNo, bidId, originalInvestUserId, presentInvestUserId,invest.id,tInvest.id, error);
					
					 if(error.code < 0){
							JPA.setRollbackOnly();
							
							return error.code;
						}
					
					/*竞拍者冻结金额减少*/
					DealDetail.minusUserFreezeFund(assignUser.id, transferDetails.offer_price, error);
					
					if (error.code < 0) {

						return error.code;
					}
					data2.updateSignWithLock(presentInvestUserId, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					//添加交易记录
					 funds = DealDetail.queryUserFund(assignUser.id, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					user_amount = funds.get("user_amount");
					detail = new DealDetail(assignUser.id,
							DealType.CHARGE_FREEZE_AUCTIONAMOUNT,
							transferDetails.offer_price, investTransferId, user_amount,
							funds.get("freeze"), funds.get("receive_amount"),
							"扣除竞拍金额");
					 detail.addDealDetail(error);
					
					 if(error.code < 0){
							JPA.setRollbackOnly();
							
							return error.code;
						}
					
					 data2.updateSignWithLock(presentInvestUserId, error);
					 
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					 
					 
					/*债权转让人可用余额增加*/
					 int resultd = DealDetail.addUserFund( invest.user_id, transferDetails.offer_price );
					 
					 if(resultd < 0){
						    JPA.setRollbackOnly();
						    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
							error.code = -111;
							return error.code;
					 }
					 
					data1.updateSignWithLock(originalInvestUserId, error);
					
					if(error.code < 0){
						    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
							return error.code;
					}
					 
					//添加交易记录
					 funds = DealDetail.queryUserFund(invest.user_id, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					 detail = new DealDetail(invest.user_id, DealType.DIRECT_DEBT_SUCCESS, transferDetails.offer_price, investTransferId, funds.get("user_amount"), funds.get("freeze"), funds.get("receive_amount"), "定向债权转让成功，增加可用余额");
					 detail.addDealDetail(error);
					
					 if(error.code < 0){
							JPA.setRollbackOnly();
							
							return error.code;
						}
					 
					 
					 //扣除债权人转让管理费
					 DealDetail.minusUserFund(invest.user_id, managefee, error);
					 
						
					if (error.code < 0) {

						return error.code;
					}
					 
					data1.updateSignWithLock(originalInvestUserId, error);
					
					if(error.code < 0){
						    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
							return error.code;
					}
						
					 funds = DealDetail.queryUserFund(invest.user_id, error);
						
						if(error.code < 0){
							error.msg = "对不起！系统异常！请您重试或联系平台管理员！";
							JPA.setRollbackOnly();
							
							return error.code;
						}
					 
					 //添加扣除管理费交易记录
					 detail = new DealDetail(invest.user_id, DealType.CHARGE_DEBT_TRANSFER_MANAGEFEE, managefee, investTransferId, funds.get("user_amount"), funds.get("freeze"), funds.get("receive_amount"), "定向模式债权转让成功，扣除转让管理费");
					 detail.addDealDetail(error);
					 
					 if(error.code < 0){
							JPA.setRollbackOnly();
							
							return error.code;
						}
					 
					 data1.updateSignWithLock(originalInvestUserId, error);
					 
					 
					/*竞拍记录状态改变   状态：0 正常；1 成交；2 待接受；-1 失败*/
					 try {
						int rows = em.createQuery("update  t_invest_transfer_details set status=1 where id=? and status = 2").setParameter(1, transferDetails.id).executeUpdate();
					
						if(rows == 0){
							JPA.setRollbackOnly();
						    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
							error.code = -111;
							return error.code;
						}
					 } catch (Exception e) {
						    e.printStackTrace();
						    JPA.setRollbackOnly();
						    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
							error.code = -111;
							return error.code;
					}
					
					
				
				    
				
				 //添加事件
					DealDetail.userEvent(invest.user_id, UserEvent.DEAL_DEBT_TRANSFER, "债权成交操作成功", error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
				 
					//更新缓存
					Map<String, Double> v = DealDetail.queryUserFund( invest.user_id, error);
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					//奖励债权竞拍用户推广用户CPS奖励
					User.rewardCPS(presentInvestUserId, managefee, investTransferId, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return error.code;
					}
					
					Debt.creatDebtAgreement(originalInvestUserId, presentInvestUserId, investTransferId,tInvest.id, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						error.msg = "债权成交操作失败！";
						error.code = -1;
						return error.code;
					}
					
					error.msg = "债权成交操作成功！";
					error.code = 1;
								
					if(v.get("user_amount") < 0 || v.get("freeze") < 0){
						error.msg = "对不起！资金出现负数，不能进行操作。给您造成的不便敬请谅解！";
						error.code = -1;
						JPA.setRollbackOnly();
							    	
						return error.code;
					} 
					
					return error.code;
			
		}
			
		
		
	}
	
	
	
	
	
	/**
	 * 获取债权表特定标版本号
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int getDebtVersion(long transfer_id,ErrorInfo error){
		
		int version = 0;
		String sql = "select version from t_invest_transfers where id = ?";
		
		try {
			version = t_invest_transfers.find(sql, transfer_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -1;
		}
		
		error.code = 1;
		return version ;
	}
	
	
	
	
	/**
	 * 更新债权表Version字段
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int updatevVersion(long transfer_id,int version,ErrorInfo error){
		
		EntityManager em = JPA.em();
		String sql = "update t_invest_transfers set version = version + 1 where id = ? and version = ?";
		
		try {
			int rows = em.createQuery(sql).setParameter(1, transfer_id).setParameter(2, version).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				error.msg = "对不起！系统异常！请您联系平台管理员！";
				error.code = -1;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			error.msg = "对不起！系统异常！请您联系平台管理员！";
			error.code = -1;
		}
		
		error.code = 1;
		return error.code;
	}
	
	
	/**
	 * 竞拍同一债权的时候进行更新
	 * @param transferDetailId
	 * @param offerPrice
	 * @param error
	 */
	public static void updateAuctionDetail(long transferDetailId,double offerPrice,ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		try {
			int rows = em.createQuery("update t_invest_transfer_details set time = ? , offer_price = ? where id = ?").setParameter(1, new Date()).setParameter(2, offerPrice).setParameter(3, transferDetailId).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				error.code = -1;
				return;
			}
		
		} catch (Exception e) {
			JPA.setRollbackOnly();
			e.printStackTrace();
			error.code = -1;
			return;
		}
		
		error.code = 1;
	}
	
	/**
	 * 定时判断债权流拍
	 */
	public static void judgeDebtFlow(){
		
		String sql = "select id from t_invest_transfers where status in (1,2,4) and end_time < NOW()";
		List<Long> transfers = new ArrayList<Long>();
		
		try {
			transfers = t_invest_transfers.find(sql).fetch();
		} catch (Exception e) {
			Logger.info("查询债权id时，%s", e.getMessage());
			return ;
		}
		
		if(transfers == null | transfers.size() == 0)
			return ;
		
		JPAPlugin.closeTx(false);
		
		for (Long tid : transfers) {
			try {
				JPAPlugin.startTx(false);
				
				int row = debtTransferFailure(tid);
				
				if(row < 0)
					Logger.info("自动流拍失败的债权编号：" + tid);
			} catch (Exception e) {
				Logger.error("自动流拍失败的债权编号：" + e.getMessage());
			}finally{
				
				JPAPlugin.closeTx(false);
			}
		}
		
		JPAPlugin.startTx(false);
	}

	/**
	 * 竞拍债权
	 * @param userId
	 * @param offerPrice
	 * @param transfer_id
	 * @param error
	 * @return 返回 -1 代表竞拍失败 ，返回 1 代表竞拍成功
	 */
	public static int auctionDebt(long userId,int offerPrice,long transfer_id,String dealpwdStr, int client, ErrorInfo error) {
		
		error.clear();
		Double maxOfferPrice=0.0;
		t_users users = new t_users();
		t_invest_transfers transfer = new t_invest_transfers();
		t_invests invest = new t_invests();
		t_bids bid = new t_bids();
		t_products product = new t_products();
		List<t_invest_transfer_details> list = null;
		DealDetail dealDetail = null;
		
		DataSafety data = new DataSafety();
		data.setId(userId);//数据防篡改（针对竞拍会员）
		boolean sign = data.signCheck(error);
		
		User user = new User();
    	user.id = userId;
    	
		if(error.code < 0){
			error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
			error.code = -1;
			return error.code;
		}
		
		if(!sign){
			error.msg = "对不起！尊敬的用户，你的账户资金出现异常变动，请速联系管理员！";
			error.code = -1;
			return error.code;
		}
		
		try {
			 users = t_users.findById(userId);
				
			 transfer = t_invest_transfers.findById(transfer_id);
			
			 invest = t_invests.findById(transfer.invest_id);
			 bid  = t_bids.findById(invest.bid_id);
			 product = t_products.findById(bid.product_id);
		} catch (Exception e) {
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();//发生异常，事务回滚
			
			return error.code;
		}
		
		long bidUserId = getBidUserId(transfer_id,error);//获取债权的借款用户
		
		if(error.code < 0){
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -8;
			
			return error.code;
		}
		
		if(userId == bidUserId){
			error.msg = "对不起！借款持有用户禁止参与该债权交易！";
			error.code = -9;
			
			return error.code;
		}
		
		if(transfer.status != Constants.AUCTION_STATUS){
			error.msg = "对不起！该债权不处于竞拍状态！";
			error.code = -8;
			
			return error.code;
		}
		
		/*投资者Id*/
		long investUserId = invest.user_id;
		
		if(userId == investUserId) {
			error.msg = "对不起！您不能竞拍自己转让的债权";
			error.code = -4;
			
			return error.code;
		}
		
		if(User.isInMyBlacklist(investUserId, userId, error)<0){
			error.msg = "对不起！您已经被债权所有者拉黑,不能竞拍该债权！";
			error.code = -5;
			
			return error.code;
		}
		
		/*判断竞拍会员是否被管理员拉黑*/
		if(users.is_blacklist){
			error.msg = "对不起！您已经被平台管理员限制操作！请您与平台管理员联系！";
			error.code = -6;
			
			return error.code;
		}
		
		
		

		Date endTime = transfer.end_time;
		
		if(endTime == null || endTime.getTime() < new Date().getTime()){
			
			error.msg = "对不起！该债权已经过期！";
			error.code = -8;
			
			return error.code;
		}
		
		if(product.is_deal_password){
			if(dealpwdStr!=null){
				if(StringUtils.isBlank(dealpwdStr)){
					error.msg = "对不起！请输入交易密码！";
					error.code = -8;
					
					return error.code;
				}
				
				if (!Encrypt.MD5(dealpwdStr + Constants.ENCRYPTION_KEY).equals(
						users.pay_password)) {
					error.msg = "对不起！交易密码错误!";
					error.code = -13;
					return error.code;
				}
			}
		}
		
		if(offerPrice < transfer.transfer_price){
			error.msg = "对不起！您的出价应该高于债权底价！";
			error.code = -8;
			
			return error.code;
		}
		
		if(offerPrice >transfer.debt_amount) {
			error.msg = "对不起！您的出价不能高于债权总额！";
			error.code = -3;
			
			return error.code;
		}
		
		
		try {
			 list = t_invest_transfer_details.find("from t_invest_transfer_details where transfer_id=?", transfer_id).fetch();
				
		} catch (Exception e) {
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();//发生异常，事务回滚
			
			return error.code;
		}
	   
		if(null != list && list.size() > 0 ){
			
			/*说明已经有人竞拍,找出最大出价*/
				maxOfferPrice = queryMaxPrice(transfer_id, error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();//发生异常，事务回滚
					
					return error.code;
				}
			
			if(offerPrice <= maxOfferPrice) {
				error.msg = "对不起！您出价应该高于"+maxOfferPrice+"元！";
				error.code = -1;
				
				return error.code;
			}
		}
		
		/*查询用户之前有没有竞拍该债权*/
		t_invest_transfer_details detail = t_invest_transfer_details.find(" user_id = ? and transfer_id = ?", userId,transfer_id).first();
		
		if(users.balance < (detail == null ? offerPrice : offerPrice - detail.offer_price)) {
			error.msg = "对不起！您可用余额不足";
			error.code = -999;
			return error.code;
		}
		
		
		//获取verison
		int version = getDebtVersion(transfer_id, error);
		
		if(error.code < 0){
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();
			
			return error.code;
			
		}
		
		t_invest_transfer_details investTransferDetails = new t_invest_transfer_details();
		
		investTransferDetails.transfer_id = transfer_id;
		investTransferDetails.time = new Date();
		investTransferDetails.user_id = userId;
		investTransferDetails.offer_price = offerPrice;
		investTransferDetails.status = 0;

		if(detail != null) {
			
			updateAuctionDetail(detail.id, offerPrice, error);
			
			if(error.code < 0){
				error.msg = "对不起！系统异常，请联系平台管理员！";
				error.code = -2;
				return error.code;
			}
			
			/*说明该会员之前竞拍过该债权，继续冻结比上次出价高出的价格*/
			DealDetail.freezeFund(userId, offerPrice - detail.offer_price, error);
			
			if (error.code < 0) {

				return error.code;
			}
			

			Map<String, Double> funds = DealDetail.queryUserFund(userId, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			double user_amount = funds.get("user_amount");
			double freeze = funds.get("freeze");
			double receive_amount = funds.get("receive_amount");
			
			//添加交易记录
			dealDetail = new DealDetail(userId, DealType.FREEZE_DEBT,
					offerPrice - detail.offer_price, transfer_id, user_amount, 
					freeze, receive_amount, "竞拍相同债权成功，继续冻结对应可用余额" + (offerPrice - detail.offer_price) + "元");
			
			try {
				dealDetail.addDealDetail(error);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				
				Map<String, Double> details = DealDetail.queryUserFund(userId, error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(details.get("user_amount") < 0){
					error.msg = "对不起！您的可用余额不足，请及时充值！";
					error.code = -999;
					JPA.setRollbackOnly();
					
					return error.code;
				}
			}
			
			data.updateSignWithLock(userId, error);
			
			if(error.code < 0){
				error.msg = "对不起！系统异常，请联系平台管理员！";
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
		
		}else{
			
			/*冻结竞拍者资金  可用余额减少*/
			DealDetail.freezeFund(userId, offerPrice, error);
			
			if (error.code < 0) {

				return error.code;
			}
			
			Map<String, Double> funds = DealDetail.queryUserFund(userId, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			double user_amount = funds.get("user_amount");
			double freeze = funds.get("freeze");
			double receive_amount = funds.get("receive_amount");
			
			//添加交易记录
			dealDetail = new DealDetail(
					userId,
					DealType.FREEZE_DEBT,
					offerPrice ,
					transfer_id,
					user_amount, 
					freeze, 
					receive_amount,  
					"竞拍债权成功，冻结对应可用余额" + offerPrice + "元");
			dealDetail.addDealDetail(error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				
				return error.code;
			}
			
			data.updateSignWithLock(userId, error);
			
			if(error.code < 0){
				JPA.setRollbackOnly();
				error.msg = "对不起！系统异常，请联系平台管理员！";
				return error.code;
			}
			
			try {
				
				/*添加债权竞拍记录*/
				investTransferDetails.save();
				
			} catch (Exception e) {
				error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -111;
				JPA.setRollbackOnly();
				
				return error.code;
			}finally{
				Map<String, Double> details = DealDetail.queryUserFund(userId, error);
				
				if(error.code < 0){
					error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
					error.code = -111;
					JPA.setRollbackOnly();
					
					return error.code;
				}
				
				if(details.get("user_amount") < 0){
					error.msg = "对不起！您的可用余额不足，请及时充值！";
					error.code = -999;
					JPA.setRollbackOnly();
					
					return error.code;
				}
			}
		
		}
		

		
		/*更新该债权竞拍次数*/
		int result = updateDebtJoinTimes(transfer_id, error);
		
		if(result < 0){
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		//更新会员性质
		User.updateMasterIdentity(userId, Constants.INVEST_USER, client, error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			
			return error.code;
		}
		
		DealDetail.userEvent(userId, UserEvent.AUCTION, "竞拍债权成功", error);
		
		if(error.code < 0){
            JPA.setRollbackOnly();
            
			return error.code;
		}
		
		//更新verson
		updatevVersion(transfer_id,version, error);
		
		if(error.code < 0){
			error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
			error.code = -111;
			JPA.setRollbackOnly();
			
			return error.code;
			
		}
		
		
		//刷新缓存
		Map<String, Double> details = DealDetail.queryUserFund(userId, error);
		if(details.get("user_amount") < 0){
			JPA.setRollbackOnly();
			error.msg = "对不起！您的余额不足请及时充值！";
			error.code = -999;
			return error.code;
		}
		
		
		//发送消息（冻结竞拍金额）
		 String username = users.name;
		 String title = transfer.title;
		 
		 TemplateEmail email = new TemplateEmail();//发送邮件
		 email.id = Templets.E_AUCTION_FREEZE;
		 
		 if(email.status){
			 String content = email.content;
			 content = content.replace("date", DateUtil.dateToString((new Date())));
			 content = content.replace("userName", username);			
			 content = content.replace("describe", title);
			 content = content.replace("money",  DataUtil.formatString(offerPrice));
			 email.addEmailTask(users.email, email.title, content);
		 }
		 
		
		 TemplateStation station = new TemplateStation();//发送站内信
		 station.id = Templets.M_AUCTION_FREEZE;
		 
		 if(station.status){
			 String stationContent = station.content;
			 stationContent = stationContent.replace("date", DateUtil.dateToString((new Date())));
			 stationContent = stationContent.replace("userName", username);			
			 stationContent = stationContent.replace("describe", title);
			 stationContent = stationContent.replace("money",  DataUtil.formatString(offerPrice));
			 
			 StationLetter letter = new StationLetter();
			 letter.senderSupervisorId = 1;
			 letter.receiverUserId = userId;
			 letter.title = station.title;
			 letter.content = stationContent;
			 
			 letter.sendToUserBySupervisor(error);
		 }	 
		error.code = 1;
		error.msg = "竞拍成功！";
		return error.code;
	}
	
	
	/**
	 * 更新债权竞拍次数
	 * @param transfer_id
	 * @param error
	 * @return
	 */
	public static int updateDebtJoinTimes(long transfer_id,ErrorInfo error){
		
		EntityManager em = JPA.em();
		
		try {
			int rows = em.createQuery("update t_invest_transfers set join_times = join_times + 1  where id = ? " ).setParameter(1, transfer_id).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				error.code = -1;
				return error.code;
			}
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}
		
		error.code = 1;
		return 1;
	}
	
	
	
	
	
	/**
	 * 根据条件查询用户所有的转让债权
	 * @param userId
	 * @param type  1:全部  2：借款标标题 3：借款标编号
	 * @param title 借款标标题
	 * @param bidNo 借款标编号
	 * @param status 状态
	 * @return
	 */
	@SuppressWarnings("unused")
	public static PageBean<v_debt_user_transfer_management> queryUserAllDebtTransfersByConditions(long userId,String typeStr,String param,String statusStr,int currPage,int pageSize){
		
		int status = 0;
		int type = 0;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_USER_TRANSFER_MANAGEMENT);
		sql.append(" and t_invests.user_id = ?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(userId);
		
		PageBean<v_debt_user_transfer_management> page = new PageBean<v_debt_user_transfer_management>();
		List<v_debt_user_transfer_management> transferList = new ArrayList<v_debt_user_transfer_management>();
		page.pageSize = pageSize;
		
		if(0 == currPage){
			page.currPage = 1;
		}else{
			page.currPage = currPage;
			
		}
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		if(typeStr==null && params==null && statusStr==null){
			
			try {
	            Query query = em.createNativeQuery(sql.toString(),v_debt_user_transfer_management.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            transferList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				
				return null;
			}
			page.page = transferList;
			return page;
		}
		
		if(NumberUtil.isNumericInt(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(NumberUtil.isNumericInt(statusStr)){
			status = Integer.parseInt(statusStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if(status < 0 || status > 5){
			status = 0;
		}
		
		if(type == 0){
			sql.append(SQLTempletes.TRANSFER_MANAGEMENT_TYPE_CONDITION[type]);
			sql.append(SQLTempletes.TRANSFER_MANAGEMENT_STATUS_CONDITION[status]);
		}else{
			sql.append(SQLTempletes.TRANSFER_MANAGEMENT_TYPE_CONDITION[type]);
			sql.append(SQLTempletes.TRANSFER_MANAGEMENT_STATUS_CONDITION[status]);
			if(type == 2){
				param = param.replace(obj + "", "");
			}
			params.add("%"+param+"%");
		}
		
		try {
            Query query = em.createNativeQuery(sql.toString(),v_debt_user_transfer_management.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            transferList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		
		
		conditionMap.put("type", type);
		conditionMap.put("status", status);
		
		page.conditions = conditionMap;
		page.page = transferList;
		return page;
	}
	
	
	
	
	/**
	 * 根据条件查询用户所有的受让债权
	 * @param userId
	 * @param type 1:全部  2：借款标标题 3：借款标编号
	 * @param param  借款标标题  借款标编号
	 * @return
	 */
	public static PageBean<v_debt_user_receive_transfers_management> queryUserAllReceivedDebtTransfersByConditions(long userId,String typeStr,String param,int currPage,int pageSize){
		
		String [] condition = {"and (t_bids.title like ? or t_bids.id like ?) "," and t_bids.title like ? ","  and t_bids.id like ? "};
		int type = 0;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_USER_RECEIVE_TRANSFERS_MANAGEMENT);
		sql.append(" and t_invest_transfer_details.user_id=?  and  t_invest_transfer_details.status in (0,1,2,-1,3) ");
		List<Object> params = new ArrayList<Object>();
		
		params.add(userId);
		
		List<v_debt_user_receive_transfers_management> transferList = new ArrayList<v_debt_user_receive_transfers_management>();
		PageBean<v_debt_user_receive_transfers_management> page = new PageBean<v_debt_user_receive_transfers_management>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		

		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", param);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.LOAN_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;

		if(typeStr == null && params == null){
			
			try {
				sql.append(" order by id desc");
	            Query query = em.createNativeQuery(sql.toString(),v_debt_user_receive_transfers_management.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            transferList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				
				return page;
			}
			
			page.page = transferList;
			
			return page;
		}
		
		if(NumberUtil.isNumericInt(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 2){
			type = 0;
		}
		
		if (StringUtils.isNotBlank(param)) {
			if(type == 0){
				sql.append(condition[type]);
				params.add("%"+param+"%");
				param = param.replace(obj + "", "");
				params.add("%"+param+"%");
			}else{
				sql.append(condition[type]);
				if(type == 2){
					param = param.replace(obj + "", "");
				}
				params.add("%"+param+"%");
			}
		}
		
		try {
			sql.append(" order by id desc");
            Query query = em.createNativeQuery(sql.toString(),v_debt_user_receive_transfers_management.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            transferList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
		
		conditionMap.put("type", type);
		page.conditions = conditionMap;
		page.page = transferList;
		
		return page;
	}
	
	
	/**
	 * 用户收藏的债权
	 * @param type 1:全部  2：借款标标题 3：转让标题 4:债权编号  5：转让用户名 6：借款用户名
	 * @param userId
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_user_attention_invest_transfers> queryUserAttentionDebtTransfers(long userId,int currPage,String typeStr,String paramter,int pageSize,ErrorInfo error){
		
		String [] condition = {" and (transfer_title  like ? or debt_transfer_no like ? or bid_title like ? or transfer_user_name like ? or bid_user_name like ?) ","  and transfer_title  like ? "," and debt_transfer_no like ? ","  and bid_title like ? "," and transfer_user_name like ? "," and bid_user_name like ? "};
		int type = 0;
		StringBuffer conditions = new StringBuffer(" 1=1 and  user_id = ?  ");
		List<Object> values = new ArrayList<Object>();
		values.add(userId);
		
        List<v_user_attention_invest_transfers> transferList = new ArrayList<v_user_attention_invest_transfers>();
		PageBean<v_user_attention_invest_transfers> page = new PageBean<v_user_attention_invest_transfers>();
		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
		if(typeStr == null && paramter == null){
			
			try {
				page.totalCount = (int) v_user_attention_invest_transfers.count(conditions.toString(), values.toArray());
				transferList = v_user_attention_invest_transfers.find(conditions.toString(), values.toArray()).fetch(currPage,page.pageSize);
			
				page.page = transferList;
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return page;
			}
			
			error.code = 1;
			return page;
		}
		
		if(NumberUtil.isNumericInt(typeStr)){
			type = Integer.parseInt(typeStr);
		}
		
		if(type < 0 || type > 5){
			type = 0;
		}
		
		if(type == 0){
			conditions.append(condition[type]);
			values.add("%"+paramter+"%");
			values.add("%"+paramter+"%");
			values.add("%"+paramter+"%");
			values.add("%"+paramter+"%");
			values.add("%"+paramter+"%");
		}else{
			conditions.append(condition[type]);
			values.add("%"+paramter+"%");
		}
		
		try {
			page.totalCount = (int) v_user_attention_invest_transfers.count(conditions.toString(), values.toArray());
			transferList = v_user_attention_invest_transfers.find(conditions.toString(), values.toArray()).fetch(currPage,page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			return page;
		}
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", paramter);
		conditionMap.put("type", type);
		
		page.conditions = conditionMap;
        page.page = transferList;
        error.code = 1;
		return page;
	}
	
	/**
	 * 判断还款的借款标有没有债权在转让，如果有，将其状态回归原态,并解冻竞拍者资金
	 * @param bidId
	 * @param error
	 * @return
	 */
	public static int judgeHasBidTransfer(long bidId){
		String sql = "select id from t_invests where transfer_status = 1 and bid_id = ?";
		List<Long> investIdList = null;
		
		try {
			investIdList = t_invests.find(sql, bidId).fetch();
		} catch (Exception e) {
			JPA.setRollbackOnly();
			
			return -1;
		}
		
		if(null == investIdList || investIdList.size() == 0) {
			/* 如果没有债权转让，则返回，且不能返回负数 */
			return 1;
		}
		
			
		Long debtTrasferId = null;
		int result = 0;
		sql = "select id from t_invest_transfers where invest_id = ? and status in (0, 1, 2, 4, -1)";
		
		for (long id : investIdList) {
			try {
				 debtTrasferId = t_invest_transfers.find(sql, id).first();
			} catch (Exception e) {
				JPA.setRollbackOnly();
				
				return -1;
			}
			
			if(null == debtTrasferId || debtTrasferId < 1) {
				continue ;
			}
			
			result = debtTransferFailure(debtTrasferId);
			
			if(result < 0) {
				JPA.setRollbackOnly();
				
				return -1;
			}
		}
		
		return 1;
	}
	
	/**
	 * 债权转让流标时，改变相关状态以及资金解冻
	 * @param investransferId
	 */
	public static int debtTransferFailure(long investransferId){
		
		ErrorInfo error = new ErrorInfo();
		DealDetail dealDetail = null;
		List<Map<String,Object>> listMap = new ArrayList<Map<String,Object>>();
		
		
			/*更新债权转让状态*/
			int resulta = changeDebtStatus(investransferId, Constants.DEBT_REPAY);
			
			if(resulta < 0){
				JPA.setRollbackOnly();
				return -1;
			}
			
			t_invest_transfers investTransfer = t_invest_transfers.findById(investransferId);
			
			/*更新投资表状态*/
			int resultb = changeInvestStatus( investTransfer.invest_id, Constants.INVEST_NORMAL);
			
			if(resultb < 0){
				JPA.setRollbackOnly();
				return -1;
			}
			
			t_invests invest = new t_invests();
			t_users user = new t_users();
			
			try {
				 invest = t_invests.findById(investTransfer.invest_id);
				 user = t_users.findById(invest.user_id);
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起！系统异常！";
				error.code = -1;
				
				return error.code;
			}
			
			//发送消息(债权转让失败)
			 TemplateEmail email = new TemplateEmail();//发送邮件
			 email.id = Templets.E_DEBT_TRANSFER_FAILURE;
			 
			 if(email.status){
				 String content1 = email.content;
				 content1 = content1.replace("userName", user.name);
				 content1 = content1.replace("date", DateUtil.dateToString(investTransfer.time));
				 content1 = content1.replace("describe", investTransfer.title);
				 content1 = content1.replace("money",  DataUtil.formatString(investTransfer.debt_amount));
				 email.addEmailTask(user.email, email.title, content1); 
			 }
			 
			
			 
			 TemplateStation station = new TemplateStation();//发送站内信
			 station.id = Templets.M_DEBT_TRANSFER_FAILURE;
			 
			 if(station.status){
				 String scontent = station.content;
				 scontent = scontent.replace("userName", user.name);
				 scontent = scontent.replace("date", DateUtil.dateToString(investTransfer.time));
				 scontent = scontent.replace("describe", investTransfer.title);
				 scontent = scontent.replace("money",  DataUtil.formatString(investTransfer.debt_amount));
				 
				 StationLetter letter = new StationLetter();
				 letter.senderSupervisorId = 1;
				 letter.receiverUserId = user.id;
				 letter.title = station.title;
				 letter.content = scontent;
				 
				 letter.sendToUserBySupervisor(error);
			 }
			 
	    /*如果有竞拍记录，修改状态值和资金退回*/
		String sql="select new Map(user_id as userId,offer_price as offerPrice) from t_invest_transfer_details where transfer_id=?";
		
		listMap = t_invest_transfer_details.find(sql, investransferId).fetch();
		 
		DataSafety data = new DataSafety();
		
		if(null != listMap && listMap.size() > 0){
			
			for(Map<String,Object> map : listMap){
				long userId =Long.parseLong( map.get("userId").toString());
				double offerPrice = Double.parseDouble(map.get("offerPrice").toString());
					
				// 定向模式不需要解冻冻结金额
				if(investTransfer.type != Constants.DIRECTIONAL_MODE) {
					
					//解冻竞拍金额
					DealDetail.relieveFreezeFund(userId, offerPrice, error);

					if (error.code < 0) {
						
						return error.code;
					}
					
					//添加交易记录(解冻竞拍金额)
					Map<String, Double> funds = DealDetail.queryUserFund(userId, error);
					double user_amount = funds.get("user_amount");
					double freeze = funds.get("freeze");
					double receive_amount = funds.get("receive_amount");
					
					dealDetail = new DealDetail(userId,
							DealType.THAW_FREEZE_AUCTIONAMOUNT, offerPrice,
							investransferId, user_amount, freeze,
							receive_amount, "债权流拍，解冻竞拍金额");
					
					dealDetail.addDealDetail(error);
	
					if (error.code < 0) {
						JPA.setRollbackOnly();
	
						return -1;
					}

					//数据防篡改字段
					data.updateSignWithLock(userId, error);
					
					if(error.code < 0){
						JPA.setRollbackOnly();
						
						return -1;
					}
				}
				
				String hql = "select new Map(name as name,mobile as moile,email as email) from t_users where id = ? ";
				Map<String,String> msgmap = new HashMap<String, String>();
				
				try {
					msgmap = t_users.find(hql, userId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					error.msg = "对不起！系统异常，对您造成的不便敬请谅解";
					return error.code;
				}
				
//					//发送消息(竞拍失败)
				 email = new TemplateEmail();//发送邮件
				 email.id = Templets.E_AUCTION_FAILURE;
				 
				 if(email.status){
					 String content = email.content;
					 content = content.replace("userName", msgmap.get("name")+"");	
					 content = content.replace("date", DateUtil.dateToString(investTransfer.time));
					 content = content.replace("describe", investTransfer.title);
					 content = content.replace("money",  DataUtil.formatString(offerPrice));
					 email.addEmailTask(msgmap.get("email"), email.title, content); 
				 }
				 
				 
				 
				 station = new TemplateStation();//发送站内信
				 station.id = Templets.M_AUCTION_FAILURE;
				 
				 if(station.status){
					 String scontent = station.content;
					 scontent = scontent.replace("userName", msgmap.get("name")+"");
					 scontent = scontent.replace("date", DateUtil.dateToString(investTransfer.time));
					 scontent = scontent.replace("describe", investTransfer.title);
					 scontent = scontent.replace("money",  DataUtil.formatString(offerPrice));
					 
					 StationLetter letter = new StationLetter();
					 letter.senderSupervisorId = 1;
					 letter.receiverUserId = userId;
					 letter.title = station.title;
					 letter.content = scontent;
					 
					 letter.sendToUserBySupervisor(error);
				 }
				
				int resultc = changeAuctionStatus(investransferId, userId,Constants.AUCTION_DETAIL_FAILURE);
				
				if(resultc < 0){
					JPA.setRollbackOnly();
					return -1;
				}
			}
			
		}
	   return 1;
	}
		
	
	
	/**
	 * 更新债权标状态
	 * @param debtId
	 * @param status
	 * @param error
	 */
	public static int changeDebtStatus(long debtId,int status){
		
		EntityManager em = JPA.em();
		try {
			int rows = em.createQuery(" update t_invest_transfers set status = ? ,failure_time = ? where id = ? and status in (0,1,2,-1,4)").setParameter(1, status).setParameter(2, new Date()).setParameter(3, debtId).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				return -1;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	
	
	/**
	 * 更新投资记录状态
	 * @param investId
	 * @param status
	 * @param error
	 */
	public static int changeInvestStatus(long investId,int status){
		
		EntityManager em = JPA.em();
		try {
			int rows = em.createQuery(" update t_invests set transfer_status = ? where id = ? and transfer_status = 1").setParameter(1, status).setParameter(2, investId).executeUpdate();
			
			if(rows == 0){
				JPA.setRollbackOnly();
				return -1;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	
	
	/**
	 * 债权流拍时更新竞拍记录状态
	 * @param investId
	 * @param status
	 * @param error
	 */
	public static int changeAuctionStatus(long transferId,long userId,int status ){
		
		EntityManager em = JPA.em();
		try {
			int rows = em.createQuery(" update t_invest_transfer_details set status = ? where transfer_id = ?  and user_id = ? ").setParameter(1, status).setParameter(2, transferId).setParameter(3, userId).executeUpdate();
		
			if(rows == 0){
				JPA.setRollbackOnly();
				return -1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	
	
	/**
	 * 债权成交时，更新竞拍记录状态
	 * @param detailId
	 * @param status
	 * @return
	 */
	public static int changeAuctionDetailStatus(double maxOfferPrice,long investTransferId){
		
		EntityManager em = JPA.em();
		try {
			
			int a = em.createQuery("update t_invest_transfer_details  set status=1"
					+ " where offer_price = ? and transfer_id=? and status = 0 )").setParameter(1,maxOfferPrice).setParameter(2, investTransferId).executeUpdate();
			
			int b = em.createQuery("update t_invest_transfer_details  set status=-1"
					+ " where offer_price < ? and transfer_id=? and status = 0)").setParameter(1,maxOfferPrice).setParameter(2, investTransferId).executeUpdate();
			
			if(a == 0 || b == 0){
				JPA.setRollbackOnly();
				return -1;
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}
	
	
	/**
	 * 债权转让借款标收款情况
	 * @param userId
	 * @param bidId
	 * @return
	 */
	public static Map<String,String> debtBidReceiveSituation(long investId){
		
		Map<String,String> map = new HashMap<String, String>();
		Double receivingAmount = 0.0;
		Double hasReceivedAmount = 0.0;
		Double remainReceivedCorpus = 0.0;
		Double remainReceivedInterest = 0.0;
		
		
		try {
			 receivingAmount = t_bill_invests.find("select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where invest_id = ?  ", investId).first();
			 hasReceivedAmount =  t_bill_invests.find("select sum(receive_corpus + receive_interest + overdue_fine) from t_bill_invests where status in (-3,-4,0) and invest_id = ? ", investId).first();
			 remainReceivedCorpus =  t_bill_invests.find("select sum(receive_corpus - real_receive_corpus ) from t_bill_invests where status in (-1,-2,-5,-6) and invest_id = ? ",investId).first();
			 remainReceivedInterest = t_bill_invests.find("select sum(receive_interest - real_receive_interest ) from t_bill_invests where status in (-1,-2,-5,-6) and invest_id = ? ",investId).first();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return map;
		}
		
		if(null == receivingAmount || receivingAmount == 0){
			receivingAmount = 0.0;
		}
		if(null == hasReceivedAmount || hasReceivedAmount == 0){
			hasReceivedAmount = 0.0;
		}
		if(null == remainReceivedCorpus || remainReceivedCorpus == 0){
			remainReceivedCorpus = 0.0;
		}
		if(null == remainReceivedInterest || remainReceivedInterest == 0){
			remainReceivedInterest = 0.0;
		}
		
		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");//保留2位小数
		
		map.put("remainReceivedCorpus", df.format(remainReceivedCorpus));//剩余应收本金
		map.put("remainReceivedInterest", df.format(remainReceivedInterest));//剩余应收利息
		map.put("receivingAmount", df.format(receivingAmount));//本息合计应收
		map.put("hasReceivedAmount", df.format(hasReceivedAmount));//已收金额
		map.put("remainReceivedAmount", df.format(receivingAmount-hasReceivedAmount));//剩余应收金额
		
		return map;
	}
	
	
	
	/**
	 * 所有审核中的债权转让标
	 * @param userId
	 * @param type 1:全部  2：编号 3：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_auditing_transfers> queryAllAuditingTransfers(String typeStr,String startDateStr,String endDateStr,String keyWords,String orderTypeStr,String currPageStr,String pageSizeStr){
	 	
		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int type = 0;
 		int orderType = 0;
 		String key = new String();
 		
 		String [] typeCondition = {" and (t_invest_transfers.id like ? or t_users.name like ?)"," and t_invest_transfers.id like ? "," and t_users.name like ?"};
 		String [] orderCondition = {" order by id desc "," order by time "," order by time desc "," order by bid_amount "," order by bid_amount desc ",
 				                    " order by order_sort desc"," order by order_sort  "," order by overdue_payback_period "," order by overdue_payback_period desc "};
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_AUDITING_TRANSFERS);
		
		List<Object> params = new ArrayList<Object>();
		
	    List<v_debt_auditing_transfers> transfersList = new ArrayList<v_debt_auditing_transfers>();
		PageBean<v_debt_auditing_transfers> page = new PageBean<v_debt_auditing_transfers>();
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(type < 0 || type > 2){
 			type = 0;
 		}
 		
 		if(orderType < 0 || orderType > 8){
 			orderType = 0;
 		}
 		
 		if(!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)){
 			Date startDate = DateUtil.strDateToStartDate(startDateStr);
 			Date endDate = DateUtil.strDateToEndDate(endDateStr);
 			
 			sql.append(" and t_invest_transfers.time >= ? and t_invest_transfers.time <= ?");
 			params.add(startDate);
 			params.add(endDate);
 		}
 		
 		if(StringUtils.isNotBlank(keyWords)){
 			key = keyWords;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);
 		
 		if(type == 0){
 			sql.append(typeCondition[0]);
 			String key_param = key;
 			key = key.replace(obj + "", "");
 			params.add("%"+key+"%");
 			params.add("%"+key_param+"%");
 		}else{
 			sql.append(typeCondition[type]);
 			if(type == 1){
 				key = key.replace(obj + "", "");
			}
 			params.add("%"+key+"%");
 		}
 		sql.append(orderCondition[orderType]);
 		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
 		try {
 			
 			Query query = em.createNativeQuery(sql.toString(),v_debt_auditing_transfers.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            transfersList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
 		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
		
		page.page = transfersList;
		page.conditions = conditionMap;
	
		
		return page;
	}
	
	
	
	
	

	/**
	 * 所有转让中的债权转让标
	 * @param userId
	 * @param type 1:全部  2：编号 3：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfering> queryAllTransferingDebts(String typeStr,String keyWords, String orderTypeStr,String currPageStr,String pageSizeStr){
		
		int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int type = 0;
 		int orderType = 0;
 		String key = new String();
 		
 		String [] typeCondition = {" and (t_invest_transfers.id like ? or t_users.name like ?)"," and t_invest_transfers.id like ? "," and t_users.name like ?"};
 		String [] orderCondition = {" "," order by time "," order by time desc "," order by bid_amount "," order by bid_amount desc ",
 				                    " order by order_sort desc "," order by order_sort  "," order by overdue_payback_period "," order by overdue_payback_period desc ",
 				                   " order by end_time "," order by end_time desc "  };
 		
 		StringBuffer sql = new StringBuffer("");
 		sql.append(SQLTempletes.SELECT);
 		sql.append(SQLTempletes.V_DEBT_TRANSFERING);
 		
 		List<Object> params = new ArrayList<Object>();
 		
		
	    List<v_debt_transfering> transfersList = new ArrayList<v_debt_transfering>();
		PageBean<v_debt_transfering> page = new PageBean<v_debt_transfering>();
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(type < 0 || type > 2){
 			type = 0;
 		}
 		
 		if(orderType < 0 || orderType > 10){
 			orderType = 0;
 		}
 		
 		if(StringUtils.isNotBlank(keyWords)){
 			key = keyWords;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		
 		if(type == 0){
 			sql.append(typeCondition[0]);
 			String key_param = key;
 			key = key.replace(obj + "", "");
 			params.add("%"+key+"%");
 			params.add("%"+key_param+"%");
 		}else{
 			sql.append(typeCondition[type]);
 			if(type == 1){
 				key = key.replace(obj + "", "");
			}
 			params.add("%"+key+"%");
 		}
 		sql.append(orderCondition[orderType]);
 		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
 		try {
            Query query = em.createNativeQuery(sql.toString(),v_debt_transfering.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            transfersList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
 		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
 		
		page.page = transfersList;
		page.conditions = conditionMap;
		
		
		return page;
	}
	
	
	/**
	 * 所有转让成功的债权转让标
	 * @param userId
	 * @param type 1:全部  2：编号 3：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfers_success> queryAllSuccessedDebts(int noPage, String typeStr,String startDateStr,String endDateStr, String keyWords, String orderTypeStr,String currPageStr,String pageSizeStr){
		
		
	    int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int type = 0;
 		int orderType = 0;
 		String key = new String();
 		
 		
 		String [] typeCondition = {" and (t_invest_transfers.id like ? or t_users.name like ?)"," and t_invest_transfers.id like ? "," and t_users.name like ?"};
 		String [] orderCondition = {" "," order by time "," order by time desc "," order by bid_amount "," order by bid_amount desc ",
 				                    " order by order_sort desc "," order by order_sort  "," order by overdue_payback_period "," order by overdue_payback_period desc ",
 				                   " order by transaction_time "," order by transaction_time desc "  };
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_TRANSFERS_SUCCESS);
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		List<Object> params = new ArrayList<Object>();
		
	    List<v_debt_transfers_success> transfersList = new ArrayList<v_debt_transfers_success>();
		PageBean<v_debt_transfers_success> page = new PageBean<v_debt_transfers_success>();
		
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(type < 0 || type > 2){
 			type = 0;
 		}
 		
 		if(orderType < 0 || orderType > 10){
 			orderType = 0;
 		}
 		
 		if(!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)){
 			Date startDate = DateUtil.strDateToStartDate(startDateStr);
 			Date endDate = DateUtil.strDateToEndDate(endDateStr);
 			
 			sql.append(" and t_invest_transfers.time >= ? and t_invest_transfers.time <= ?");
 			params.add(startDate);
 			params.add(endDate);
 		}
 		
 		if(StringUtils.isNotBlank(keyWords)){
 			key = keyWords;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);
 		
		if(type == 0){
 			sql.append(typeCondition[0]);
 			String key_param = key;
 			key = key.replace(obj + "", "");
 			params.add("%"+key+"%");
 			params.add("%"+key_param+"%");
 		}else{
 			sql.append(typeCondition[type]);
 			if(type == 1){
 				key = key.replace(obj + "", "");
			}
 			params.add("%"+key+"%");
 		}
 		sql.append(orderCondition[orderType]);
 		
 		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
 		try {
            Query query = em.createNativeQuery(sql.toString(),v_debt_transfers_success.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * pageSize);
            	query.setMaxResults(pageSize);
            }
            
            transfersList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
 		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
 		
		page.page = transfersList;
		page.conditions = conditionMap;
		
		
		return page;
	}
	
	
	
	
	/**
	 * 所有未审核通过的债权转让标
	 * @param userId
	 * @param type 1:全部  2：编号 3：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_no_pass_transfers> queryAllNopassDebts(int noPage, String typeStr,String startDateStr,String endDateStr, String keyWords, String orderTypeStr,String currPageStr,String pageSizeStr){
		
		
	    int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int type = 0;
 		int orderType = 0;
 		String key = new String();
 		
 		String [] typeCondition = {" and (t_invest_transfers.id like ? or t_users.name like ?)"," and t_invest_transfers.id like ? "," and t_users.name like ?"};
 		String [] orderCondition = {" "," order by time "," order by time desc "," order by bid_amount "," order by bid_amount desc ",
 				                    " order by order_sort desc "," order by order_sort  "," order by overdue_payback_period "," order by overdue_payback_period desc "};
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_NO_PASS_TRANSFERS);
		
		List<Object> params = new ArrayList<Object>();
		
	    List<v_debt_no_pass_transfers> transfersList = new ArrayList<v_debt_no_pass_transfers>();
		PageBean<v_debt_no_pass_transfers> page = new PageBean<v_debt_no_pass_transfers>();
		
		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(type < 0 || type > 2){
 			type = 0;
 		}
 		
 		if(orderType < 0 || orderType > 8){
 			orderType = 0;
 		}
 		
 		if(!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)){
 			Date startDate = DateUtil.strDateToStartDate(startDateStr);
 			Date endDate = DateUtil.strDateToEndDate(endDateStr);
 			
 			sql.append(" and t_invest_transfers.time >= ? and t_invest_transfers.time <= ?");
 			params.add(startDate);
 			params.add(endDate);
 		}
 		
 		if(StringUtils.isNotBlank(keyWords)){
 			key = keyWords;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);
 		
		if(type == 0){
 			sql.append(typeCondition[0]);
 			String key_param = key;
 			key = key.replace(obj + "", "");
 			params.add("%"+key+"%");
 			params.add("%"+key_param+"%");
 		}else{
 			sql.append(typeCondition[type]);
 			if(type == 1){
 				key = key.replace(obj + "", "");
			}
 			params.add("%"+key+"%");
 		}
 		sql.append(orderCondition[orderType]);
 		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
 		try {
            Query query = em.createNativeQuery(sql.toString(),v_debt_no_pass_transfers.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * pageSize);
            	query.setMaxResults(pageSize);
            }
            	
            transfersList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
 		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
 		
		page.page = transfersList;
		page.conditions = conditionMap;
		
		
		return page;
	}
	
	
	
	
	/**
	 * 所有失败的债权转让标
	 * @param userId
	 * @param type 1:全部  2：编号 3：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer_failure> queryAllFailureDebts(int noPage, String typeStr,String startDateStr,String endDateStr, String keyWords, String orderTypeStr,String currPageStr,String pageSizeStr){
		
	    int currPage = Constants.ONE;
 		int pageSize = Constants.TEN;
 		int type = 0;
 		int orderType = 0;
 		String key = new String();
 		
 		String [] typeCondition = {" and (t_invest_transfers.id like ? or t_users.name like ?)"," and t_invest_transfers.id like ? "," and t_users.name like ?"};
 		String [] orderCondition = {" "," order by time "," order by time desc "," order by bid_amount "," order by bid_amount desc ",
 				                    " order by order_sort desc "," order by order_sort  "," order by overdue_payback_period "," order by overdue_payback_period desc "};
 		
 		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_TRANSFER_FAILURE);
		
		List<Object> params = new ArrayList<Object>();
		
	    List<v_debt_transfer_failure> transfersList = new ArrayList<v_debt_transfer_failure>();
		PageBean<v_debt_transfer_failure> page = new PageBean<v_debt_transfer_failure>();

		EntityManager em = JPA.em();
        String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new ErrorInfo());
        obj = obj == null ? "" : obj;
        
		if(NumberUtil.isNumericInt(currPageStr)) {
 			currPage = Integer.parseInt(currPageStr);
 		}
 		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(typeStr)) {
 			type = Integer.parseInt(typeStr);
 		}
 		
 		if(NumberUtil.isNumericInt(orderTypeStr)) {
 			orderType = Integer.parseInt(orderTypeStr);
 		}
 		
 		if(type < 0 || type > 2){
 			type = 0;
 		}
 		
 		if(orderType < 0 || orderType > 8){
 			orderType = 0;
 		}
 		
 		if(!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)){
 			Date startDate = DateUtil.strDateToStartDate(startDateStr);
 			Date endDate = DateUtil.strDateToEndDate(endDateStr);
 			
 			sql.append(" and failure_time >= ? and failure_time <= ?");
 			params.add(startDate);
 			params.add(endDate);
 		}
 		
 		if(StringUtils.isNotBlank(keyWords)){
 			key = keyWords;
 		}
 		
 		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);
 		
 		if(type == 0){
 			sql.append(typeCondition[0]);
 			String key_param = key;
 			key = key.replace(obj + "", "");
 			params.add("%"+key+"%");
 			params.add("%"+key_param+"%");
 		}else{
 			sql.append(typeCondition[type]);
 			if(type == 1){
 				key = key.replace(obj + "", "");
			}
 			params.add("%"+key+"%");
 		}
 		sql.append(orderCondition[orderType]);
 		
		page.pageSize = pageSize;
		page.currPage = currPage;
		
 		try {
            Query query = em.createNativeQuery(sql.toString(),v_debt_transfer_failure.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            
            if(noPage != Constants.NO_PAGE){
            	query.setFirstResult((currPage - 1) * pageSize);
            	query.setMaxResults(pageSize);
            }
            	
            transfersList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
 		} catch (Exception e) {
			e.printStackTrace();
			
			return page;
		}
 		
		page.page = transfersList;
		page.conditions = conditionMap;
		
		
		return page;
		
	}
	
	
	
	
	/**
	 * 受让债权管理竞价成功债权详情页面
	 * @param debtId
	 */
	public static v_debt_user_receive_transfers_management details(long debtId){
		
		v_debt_user_receive_transfers_management debt = null;
		List<v_debt_user_receive_transfers_management> v_debt_list = null;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_USER_RECEIVE_TRANSFERS_MANAGEMENT);
		sql.append(" and t_invest_transfer_details.id = ? ");
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_debt_user_receive_transfers_management.class);
            query.setParameter(1, debtId);
            query.setMaxResults(1);
            v_debt_list = query.getResultList();
            
            if(v_debt_list.size() > 0){
            	debt = v_debt_list.get(0);
            }
            
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return debt;
	}
	
	
	/**
	 * 转让债权管理竞价成功债权详情页面
	 * @param debtId
	 */
	public static v_debt_user_transfer_management transferDetails(long debtId,ErrorInfo error){
		
		v_debt_user_transfer_management debt = null;
		
		try {
			debt = v_debt_user_transfer_management.findById(debtId);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return null;
		}
		
		error.code = 1;
		return debt;
		
	}
	
	
	/**
	 * 查询用户竞拍某个债权的最高价
	 * @param transferId
	 * @param userId
	 * @param error
	 * @return
	 */
	public static Double getMyAuctionPrice(long transferId,long userId,ErrorInfo error){
		
		Double auctionPrice = 0.0;
		try {
			auctionPrice = t_invest_transfer_details.find("select max(offer_price) from t_invest_transfer_details where transfer_id = ? and user_id = ?" ,transferId, userId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == auctionPrice || auctionPrice == 0){
			auctionPrice = 0.0;
		}
		
		error.code = 1;
		return 	auctionPrice;
	}
	
	
	
	 
	
	/**
	 * 统计所有的债权转让标
	 * @param error
	 * @return
	 */
	public static Long getAllDebtCount(ErrorInfo error){
		
		Long count = 0l;
		
		try {
			List<v_front_all_debts> debtList = null;
			StringBuffer sql = new StringBuffer("");
			sql.append(SQLTempletes.SELECT);
			sql.append(SQLTempletes.V_FRONT_ALL_DEBTS);
			
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
            debtList = query.getResultList();
            count = Convert.strToLong(debtList.size()+"", 0);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == count || count == 0){
			count = 0l;
		}
		
		error.code = 1;
		return count;
	}
	
	
	/**
	 * 根据债权ID查询借款用户ID
	 * @param debtId
	 * @return
	 */
	public static long getBidUserId(long debtId,ErrorInfo error){
		
		Long userId = 0l;
		Long investId = 0l;
		Long bidId = 0l;
		
		try {
			investId = t_invest_transfers.find("select invest_id from t_invest_transfers where id = ?", debtId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == investId){
			error.code = -2;
		}else{
			try {
				bidId = t_invests.find("select bid_id from t_invests where id = ?", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -3;
			}
			
			if(null == bidId){
				error.code = -4;
			}else{
				try {
					userId = t_bids.find("select user_id from t_bids where id = ?", bidId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -3;
				}
				if(null == userId){
					error.code = -4;
				}
			}
		}
		
		error.code = 1;
		return userId;
	}
	
	
	
	/**
	 * 根据债权ID查询投资用户ID
	 * @param debtId
	 * @return
	 */
	public static long getInvestUserId(long debtId,ErrorInfo error){
		
		Long userId = 0l;
		Long investId = 0l;
		
		try {
			investId = t_invest_transfers.find("select invest_id from t_invest_transfers where id = ?", debtId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == investId){
			error.code = -2;
		}else{
			try {
				userId = t_invests.find("select user_id from t_invests where id = ?", investId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -3;
			}
			
		}
		
		error.code = 1;
		return userId;
	}
	
	
	
	/**
	 * 根据投资ID查询借款用户ID
	 * @param debtId
	 * @return
	 */
	public static long getUserIdByInvestId(long investId,ErrorInfo error){
		
		Long userId = 0l;
		Long bidId = 0l;
		
		try {
			bidId = t_invests.find("select bid_id from t_invests where id = ?", investId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
		}
		
		if(null == bidId){
			error.code = -2;
		}else{
			try {
				userId = t_bids.find("select user_id from t_bids where id = ?", bidId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -3;
			}
			
		}
		
		error.code = 1;
		return userId;
	}
	
	
	/**
	 * ajax分页查询债权竞拍记录
	 * @param debtId
	 */
	public static PageBean<v_debt_auction_records> queryDebtAllAuctionRecords(int pageNum, int pageSize,long debtId,ErrorInfo error){
		
		PageBean<v_debt_auction_records> page = new PageBean<v_debt_auction_records>();
		List<v_debt_auction_records> list = new ArrayList<v_debt_auction_records>();
		
		page.pageSize = pageSize;
		page.currPage = pageNum;
		
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.PAGE_SELECT);
		sql.append(SQLTempletes.V_DEBT_AUCTION_RECORDS);
		sql.append(" and t_invest_transfer_details.transfer_id=?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(debtId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_debt_auction_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((page.currPage - 1) * page.pageSize);
            query.setMaxResults(page.pageSize);
            list = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());

			error.code = -1;
		}
		
		error.code = 1;
		page.page = list;
		
		return page;
		
	}
	
	
	
	/**
	 * 后台债权转让管理详情页面前一条记录(审核中)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long auditingAhead(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long minId = -1;
		// 审核中
			
			try {
				 minId = v_debt_auditing_transfers.find("select min(id) from v_debt_auditing_transfers").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}

			if (debtId == minId || minId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_auditing_transfers where id = (select max(id) from v_debt_auditing_transfers where id < ?) ";
				
				try {
					temp = v_debt_auditing_transfers.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
		}
			
		error.code = 1;
		return temp;
	}
	
	
	
	
	/**
	 * 后台债权转让管理详情页面前一条记录(竞拍中)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long auctioningAhead(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long minId = -1;

		//竞拍转让中
			try {
				 minId = v_debt_transfering.find("select min(id) from v_debt_transfering").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
			  

			if (debtId == minId || minId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_transfering where id = (select max(id) from v_debt_transfering where id < ?) ";
				
				try {
					temp = v_debt_transfering.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
		}
		
		error.code = 1;
		return temp;
	}
	
	
	/**
	 * 后台债权转让管理详情页面前一条记录(竞拍中)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long successAhead(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long minId = -1;
		
		try {
			 minId = v_debt_transfers_success.find("select min(id) from v_debt_transfers_success").first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}

		if (debtId == minId || minId == -1) {
			temp = debtId;
		} else {
			String sql = "select id from v_debt_transfers_success where id = (select max(id) from v_debt_transfers_success where id < ?) ";
			
			try {
				temp = v_debt_transfers_success.find(sql, debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
		}
		error.code = 1;
		return temp;
	}
	
	
	/**
	 * 后台债权转让管理详情页面前一条记录(未通过)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long nopassAhead(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long minId = -1;
		
		try {
			 minId = v_debt_no_pass_transfers.find("select min(id) from v_debt_no_pass_transfers").first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}
		
		if (debtId == minId || minId == -1) {
			temp = debtId;
		} else {
			String sql = "select id from v_debt_no_pass_transfers where id = (select max(id) from v_debt_no_pass_transfers where id < ?) ";
			
			try {
				temp = v_debt_no_pass_transfers.find(sql, debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
		}
		error.code = 1;
		return temp;
	}
	
	
	/**
	 * 后台债权转让管理详情页面前一条记录(失败的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long failureAhead(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long minId = -1;
		
		try {
			 minId = v_debt_transfer_failure.find("select min(id) from v_debt_transfer_failure").first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			return error.code;
		}
	   
		if (debtId == minId || minId == -1) {
			temp = debtId;
		} else {
			String sql = "select id from v_debt_transfer_failure where id = (select max(id) from v_debt_transfer_failure where id < ?) ";
			
			try {
				temp = v_debt_transfer_failure.find(sql, debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
		}
		error.code = 1;
		return temp;
		
	}
	
	
	
	
	/**
	 * 后台债权转让管理详情页面后一条记录(审核中的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long auditingBack(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long maxId = -1;
		
		// 审核中
			
			try {
				maxId = v_debt_auditing_transfers.find("select max(id) from v_debt_auditing_transfers").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
					
			if (debtId == maxId || maxId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_auditing_transfers where id = (select min(id) from v_debt_auditing_transfers where id > ?) ";
				try {
					temp = v_debt_auditing_transfers.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
			}
			
			error.code = 1;
			return temp;
	}
	
	
	
	/**
	 * 后台债权转让管理详情页面后一条记录(竞拍中的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long auctioningBack(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long maxId = -1;
		
		//竞拍转让中
			
			try {
				maxId = v_debt_transfering.find("select max(id) from v_debt_transfering").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
			 

			if (debtId == maxId || maxId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_transfering where id = (select min(id) from v_debt_transfering where id > ?) ";
				try {
					temp = v_debt_transfering.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
				
			}
			error.code = 1;
		    return temp;
	}
	
	/**
	 * 后台债权转让管理详情页面后一条记录(已成功的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long successBack(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long maxId = -1;
		
		//已成功
				
			try {
				 maxId = v_debt_transfers_success.find("select max(id) from v_debt_transfers_success").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
		

			if (debtId == maxId ) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_transfers_success where id = (select min(id) from v_debt_transfers_success where id > ?) ";
				
				try {
					temp = v_debt_transfers_success.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
			}
			
			error.code = 1;
			return temp;
	}
	
	
	/**
	 * 后台债权转让管理详情页面后一条记录(未通过的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long nopassBack(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long maxId = -1;
		
		//审核不通过
			
			try {
				 maxId = v_debt_no_pass_transfers.find("select max(id) from v_debt_no_pass_transfers").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}
			

			if (debtId == maxId || maxId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_no_pass_transfers where id = (select min(id) from v_debt_no_pass_transfers where id > ?) ";
				try {
					temp = v_debt_no_pass_transfers.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
				
			}
			
			error.code = 1;
			return temp;
	}
	
	/**
	 * 后台债权转让管理详情页面后一条记录(失败的)
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static long failureBack(long debtId, int status,ErrorInfo error) {
		
		long temp = -1;
		long maxId = -1;
		
		//失败的
			
			try {
				maxId = v_debt_transfer_failure.find("select max(id) from v_debt_transfer_failure").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
				return error.code;
			}

			if (debtId == maxId || maxId == -1) {
				temp = debtId;
			} else {
				String sql = "select id from v_debt_transfer_failure where id = (select min(id) from v_debt_transfer_failure where id > ?) ";
				try {
					temp = v_debt_transfer_failure.find(sql, debtId).first();
				} catch (Exception e) {
					e.printStackTrace();
					error.code = -1;
					return error.code;
				}
			}
			
			error.code = 1;
			return temp;
	}
	
	
	/**
	 * 
	 * @param debtId
	 * @param status
	 * @param error
	 * @return
	 */
	public static Map countMap(long debtId,int status,ErrorInfo error){
		
		Long countFront = 0l;
		Long countAfter = 0l;
		
		Map<String,Long> map = new HashMap<String, Long>();
		
		if(status == Constants.DEBT_AUDITING){//审核中
			try {
				countFront = v_debt_auditing_transfers.find("select count(id) from v_debt_auditing_transfers where id > ?",debtId).first();
				countAfter = v_debt_auditing_transfers.find("select count(id) from v_debt_auditing_transfers where id < ?",debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
			
		}
		
		if(status == Constants.DEBT_NOPASS){//审核未通过
			try {
				countFront = v_debt_no_pass_transfers.find("select count(id) from v_debt_no_pass_transfers where id > ?",debtId).first();
				countAfter = v_debt_no_pass_transfers.find("select count(id) from v_debt_no_pass_transfers where id < ?",debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		
		if(status == Constants.DEBT_AUCTIONING || status == Constants.DEBT_ACCEPT || status == Constants.WAIT_CONFIRM){//竞拍中
			
			try {
				countFront = v_debt_transfering.find("select count(id) from v_debt_transfering where id > ?",debtId).first();
				countAfter = v_debt_transfering.find("select count(id) from v_debt_transfering where id < ?",debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		
		if(status == Constants.DEBT_SUCCESS){//已成功的
			try {
				countFront = v_debt_transfers_success.find("select count(id) from v_debt_transfers_success where id > ?",debtId).first();
				countAfter = v_debt_transfers_success.find("select count(id) from v_debt_transfers_success where id < ?",debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		
		if(status == Constants.DEBT_FLOW || status == Constants.DEBT_REFUSE || status == Constants.DEBT_REPAY){//失败的
			try {
				countFront = v_debt_transfer_failure.find("select count(id) from v_debt_transfer_failure where id > ?",debtId).first();
				countAfter = v_debt_transfer_failure.find("select count(id) from v_debt_transfer_failure where id < ?",debtId).first();
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
		}
		
		if(null == countFront){
			countFront = 0l;
		}
		
		if(null == countAfter){
			countAfter = 0l;
		}
		
		map.put("countFront", countFront);
		map.put("countAfter", countAfter);
		
		error.code = 1;
		return map;
	}
	
	
	
	
	/**
	 * 查询某个债权的竞拍记录
	 * @param pageNum
	 * @param debtId
	 * @param error
	 * @return
	 */
	public static PageBean<v_debt_auction_records> queryAllAuctionRecords(int pageNum,long debtId,ErrorInfo error){
		
		PageBean<v_debt_auction_records> page = new PageBean<v_debt_auction_records>();

		page.currPage = pageNum;
		page.pageSize = 7;
		
		List<v_debt_auction_records> list = new ArrayList<v_debt_auction_records>();

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_AUCTION_RECORDS);
		sql.append(" and t_invest_transfer_details.transfer_id=?");
		
		List<Object> params = new ArrayList<Object>();
		params.add(debtId);
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_debt_auction_records.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((pageNum - 1) * page.pageSize);
            query.setMaxResults(page.pageSize);
            list = query.getResultList();
            
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(e.getMessage());
			error.code = -1;
		}
		
		page.page = list;
		error.code = 1;
		return page;
	}
	
	
	/**
	 * 取消债权关注
	 * @param debtId
	 * @param error
	 */
	public static void canaleDebt(long attentionId,ErrorInfo error){
		
		t_user_attention_invest_transfers attentionDebt = null;
		
		try {
			attentionDebt = t_user_attention_invest_transfers.findById(attentionId);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询关注的债权异常";
			
			return;
		}
		
		if(null != attentionDebt){
			attentionDebt.delete();
			error.code = 1;
			error.msg = "取消关注债权成功";
			
			return;
		}
		
		error.code = -2;
		error.msg = "查询关注的债权异常";
		
		return;
	}
	
	
	/**
	 * 取消关注借款标
	 * @param bidId
	 * @param userId
	 * @param error
	 */
	public static void cancleBid(long bidId,long userId,ErrorInfo error){
		
		t_user_attention_bids attentionBid = null;
		
		try {
			attentionBid = t_user_attention_bids.find(" user_id = ? and bid_id = ?", userId,bidId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询关注借款标异常";
			
			return;
		}
		
		if(null != attentionBid){
			attentionBid.delete();
			error.code = 1;
			error.msg = "取消关注借款标成功";
			
			return;
		}
		
		error.code = -2;
		error.msg = "查询关注借款标异常";
		
		return;
	}
	
	
	/**
	 * 债权用户初步成交债权，之后等待竞拍方确认成交
	 * @param debtId
	 * @param error
	 */
	public static void firstDealDebt(long debtId,ErrorInfo error){
		
		t_invest_transfers debt = null;
		t_invest_transfer_details detail = null;
		t_users user = null;
		
		try {
			debt = t_invest_transfers.findById(debtId);
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询债权对象异常";
			
			return;
		}
		
		if(debt.status == Constants.TRANSFER_STATUS){
			error.msg = "对不起！该债权还没有审核，请耐心等待！";
			error.code = -1;
			
			return ;
		}
		
		
		if(debt.status != Constants.DIRECTIONAL_MODE && debt.status != Constants.AUCTION_MODE){
			error.msg = "对不起！该债权已经不处于待成交状态！";
			error.code = -1;
			
			return ;
			
		}
		
		
		 Double maxOfferPrice = queryMaxPrice(debtId, error);
		 
		 if(error.code < 0){
			    error.msg = "对不起！系统异常，给您造成的不便敬请谅解！";
				error.code = -1;
				JPA.setRollbackOnly();
				
				return ;
		 }
		
		 if(maxOfferPrice == 0 || null == maxOfferPrice){//说明没有竞拍记录，不能执行成交操作
			 
			    error.msg = "对不起！该债权暂时没有用户参与竞拍，暂时不能成交！";
				error.code = -1;
				JPA.setRollbackOnly();
				
				return ;
		 }
		 
		 try {
			 detail = t_invest_transfer_details.find(" transfer_id = ? and offer_price = ? ", debtId,maxOfferPrice).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
			error.msg = "查询债权竞拍对象异常";
			
			return;
		}
		
		if(null != debt && null != detail){
			debt.status = Constants.WAIT_CONFIRM;
			detail.status = Constants.AUCTION_DETAIL_WAIT_CONFIRM;
			
			
			try {
				user = t_users.findById(detail.user_id);
				debt.save();
				detail.save();
				
				if(null != user){
					 TemplateEmail email = new TemplateEmail();//发送邮件
					 email.id = Templets.E_DEAL_DEBT_FIRST;
					 
					 if(email.status){
						 String content = email.content;
						 content = content.replace("userName", user.name);
						 content = content.replace("title", debt.title);
						 content = content.replace("money",  DataUtil.formatString(detail.offer_price));
						 email.addEmailTask(user.email, email.title, content);
					 }
				}
			
				error.code = 1;
				error.msg = "债权成交成功，等待对方确认成交";
				return;
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -2;
				error.msg = "修改债权对象异常";
				
				return;
			}
		}
		
		/**
		 * 发送消息、、、、、、
		 */
		error.code = -3;
		error.msg = "修改债权对象异常";
		
		return;
		
	}
	
	/* 2014-11-14 */
    /**
     * 转让协议
     */
	public static String queryPact(long debtId, ErrorInfo error) {
		
		String pactContent = "";
		String sql = "select pact from t_invest_transfers where id = ? ";
		
		try {
			pactContent = t_invest_transfers.find(sql, debtId).first();
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -1;
			error.msg = "查询平台债权协议异常";
			
			return null;
		}
		
		
		return pactContent;

	}
	
	
	/**
	 * 生成平台债权协议
	 * @param originalInvestUserId
	 * @param presentInvestUserId
	 * @param debtId
	 * @param error
	 */
	public static void creatDebtAgreement(long originalInvestUserId, long presentInvestUserId, long debtId, long investId,  ErrorInfo error){
		
		TemplatePact pact = new TemplatePact();
		pact.id = Templets.DEBT_TRANSFER_AGREEMENT;
		
		if(pact.is_use){
			t_users debtUser = new t_users();
			t_users auctionUser = new t_users();
			t_invest_transfers debt = new t_invest_transfers();
			t_invests invest = new t_invests();
			t_bids bid = new t_bids();
			t_users bidUser = new t_users();
			String hql = "select _value from t_system_options where _key = ?";
			String companyName = "";
			
			try {
				debtUser = t_users.findById(originalInvestUserId);
				auctionUser = t_users.findById(presentInvestUserId);
				debt = t_invest_transfers.findById(debtId);
				invest = t_invests.findById(debt.invest_id);
				bid = t_bids.findById(invest.bid_id);
				bidUser = t_users.findById(bid.user_id);
				companyName = t_system_options.find(hql, "company_name").first();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起，系统异常，请联系平台管理员";
				error.code = -1;
				return;
				
			}
			
			String debtUserRealityName = debtUser.reality_name == null ? "" : debtUser.reality_name;
			String debtUserRealityIdno = debtUser.id_number == null ? "" : debtUser.id_number;
			String auctionUserRealityName = auctionUser.reality_name == null ? "" : auctionUser.reality_name;
			String auctionUserRealityIdno = auctionUser.id_number == null ? "" : auctionUser.id_number;
			String no = debtId + DateUtil.simple(new Date());
			String content = pact.content;
			content = content.replace(Templets.TRANSFER_REALITY_NAME,debtUserRealityName)
			.replace(Templets.TRANSFER_ID_NUMBER, debtUserRealityIdno)
			.replace(Templets.TRANSFER_USER_NAME, debtUser.name)
			.replace(Templets.TRANSFEREE_REALITY_NAME, auctionUserRealityName)
			.replace(Templets.TRANSFEREE_ID_NUMBER, auctionUserRealityIdno)
			.replace(Templets.TRANSFEREE_USER_NAME, auctionUser.name)
			.replace(Templets.LOAN_REALITY_NAME, bidUser.reality_name)
			.replace(Templets.LOAN_ID_NUMBER, bidUser.id_number)
			.replace(Templets.LOAN_USER_NAME, bidUser.name)
			.replace(Templets.SUM_MONEY, debt.debt_amount + "")
			.replace(Templets.COMPANY_NAME, companyName)
			.replace(Templets.PACT_NO, no)
			.replace(Templets.DATE, DateUtil.dateToString(new Date()));
			
			String sql = "update t_invest_transfers set pact = ? where id = ? ";
			
			EntityManager em = JPA.em();
			int rows = 0;
			
			try {
				rows = em.createQuery(sql).setParameter(1, content).setParameter(2, debt.id).executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				error.msg = "对不起，系统异常，请联系平台管理员";
				error.code = -1;
				return;
			}
			
			if(rows == 0){
				error.msg = "对不起，系统异常，请联系平台管理员";
				error.code = -1;
				return;
			}
			
			//根据新的投标记录生成对应的理财合同，保障函，居间服务协议
			String pact_no = investId + DateUtil.simple(new Date());
			Invest.creatSingleGuaranteeInvest(investId,pact_no, error);
			if(error.code < 0){
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "创建平台协议模板异常";
				return;
			}
			Invest.creatSingleIntermediaryAgreement(investId, error);
			if(error.code < 0){
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "创建平台协议模板异常";
				return;
			}
			Invest.creatSingleInvestPact(investId, error);
			if(error.code < 0){
				JPA.setRollbackOnly();
				error.code = -1;
				error.msg = "创建平台协议模板异常";
				return;
			}
			
			error.msg = "保存平台债权协议成功";
			error.code = 1;
			return;
		}else{
			error.msg = "平台债权协议未开启";
			error.code = 1;
			return;
		}
	}
	
	/**
	 * 查询当前的状态
	 * @param id
	 * @return
	 */
	public static Integer queryStatusById(long id) {
		try {
			return t_invest_transfers.find("select status from t_invest_transfers where id = ? ", id).first();
		} catch (Exception e) {
			Logger.info("查询债权的状态时，%s", e.getMessage());
			
			return null;
		}
	}
	
	/**
	 * 查询债权转让类型
	 * @param id
	 * @return
	 */
	public static Integer queryTypeById(long id) {
		try {
			return t_invest_transfers.find("select type from t_invest_transfers where id = ? ", id).first();
		} catch (Exception e) {
			Logger.info("查询债权转让类型时，%s", e.getMessage());
			
			return null;
		}
	}
	
	/**
	 * 查询用户受让债权id
	 * @param debtId  债权id
	 * @param userId  受让人id
	 * @return
	 */
	public static long queryDebtDetailIdByDebtIdAndUserId(long debtId, long userId) {
		try {
			return t_invest_transfers.find("select id from t_invest_transfer_details where transfer_id = ? and user_id = ?", debtId, userId).first();
		} catch (Exception e) {
			Logger.info("查询用户受让债权id时，%s", e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 查询所有在前台显示的债权转让标(不包含已完成)
	 * @return
	 */
	public static PageBean<v_front_all_debts> queryAllDebtTransfersNotSuccess(int currPage,int pageSize,String loanType,String debtAmount,String apr,String orderType,String _keywords,ErrorInfo error){
		
		
		int debtAmountType = 0;
		int aprtype = 0;
		int order = 0;
		int loanTypes = 0;
		
		List<v_front_all_debts> debtList = new ArrayList<v_front_all_debts>();
		PageBean<v_front_all_debts> page = new PageBean<v_front_all_debts>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_FRONT_ALL_DEBTS);
		sql.append(" and t_invest_transfers.type=2 ");
		sql.append(" and t_invest_transfers.status <> 3 ");
		
		
		List<Object> params = new ArrayList<Object>();
		
		if(StringUtils.isBlank(loanType) && StringUtils.isBlank(debtAmount) && StringUtils.isBlank(apr) && StringUtils.isBlank(orderType) && StringUtils.isBlank(_keywords)) {
			
			try {
				sql.append(" order by status,is_quality_debt desc");
				EntityManager em = JPA.em();
	            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
	            for(int n = 1; n <= params.size(); n++){
	                query.setParameter(n, params.get(n-1));
	            }
	            query.setFirstResult((currPage - 1) * pageSize);
	            query.setMaxResults(pageSize);
	            debtList = query.getResultList();
	            
	            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
	            
			} catch (Exception e) {
				e.printStackTrace();
				error.code = -1;
			}
			
			error.code = 1;
			page.page = debtList;
			
			return page;
		}
		 
			if(NumberUtil.isNumericInt(loanType)){
				loanTypes = Convert.strToInt(loanType, 0);
				sql.append(" and t_bids.product_id = ?");
				params.add(loanTypes);
			}
			
		
			if(NumberUtil.isNumericInt(debtAmount)){
				debtAmountType = Integer.parseInt(debtAmount);
			}
			
			if(!StringUtils.isBlank(_keywords)){
				sql.append(" and t_invest_transfers.title like ? ");
				params.add("%"+_keywords+"%");
			}
			if(debtAmountType < 0 || debtAmountType > 5){
				sql.append(SQLTempletes.DEBT_AMOUNT_CONDITION[0]);
			}else{
				sql.append(SQLTempletes.DEBT_AMOUNT_CONDITION[debtAmountType]);
			}
				
		
			if(NumberUtil.isNumericInt(apr)){
				aprtype = Integer.parseInt(apr);
			}
		
			if(aprtype < 0 || aprtype > 4){
				sql.append(SQLTempletes.BID_APR_CONDITION[0]);
			}else{
				sql.append(SQLTempletes.BID_APR_CONDITION[aprtype]);
			}
				
			if(NumberUtil.isNumericInt(orderType)){
				order = Integer.parseInt(orderType);
			}
			
			if(order < 0 || order > 9){
				sql.append(SQLTempletes.DEBT_ORDER_CONITION[0]);
			}else{
				sql.append(SQLTempletes.DEBT_ORDER_CONITION[order]);
			}
		
		try {
			EntityManager em = JPA.em();
            Query query = em.createNativeQuery(sql.toString(),v_front_all_debts.class);
            for(int n = 1; n <= params.size(); n++){
                query.setParameter(n, params.get(n-1));
            }
            query.setFirstResult((currPage - 1) * pageSize);
            query.setMaxResults(pageSize);
            debtList = query.getResultList();
            
            page.totalCount = QueryUtil.getQueryCountByCondition(em, sql.toString(), params);
            
		} catch (Exception e) {
			e.printStackTrace();
			error.code = -2;
		}
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("apr", aprtype);
		conditionMap.put("debtAmount", debtAmountType);
		conditionMap.put("orderType", order);
		conditionMap.put("keywords", _keywords);
		conditionMap.put("loanType", loanTypes);
		
		error.code = 1;
		page.page = debtList;
		page.conditions = conditionMap;
		
		return page;
	}
	
	/**
	 * 查询转让申请个数
	 * @param error
	 * @return
	 */
	public static int querytransferBidCount(ErrorInfo error){
		error.clear();
		
		Object result = null;
		
		String sql = "select count(1) from t_invest_transfers where status=0 ";
		Query query = JPA.em().createNativeQuery(sql);
		
		try {
			result = query.getSingleResult();
		} catch (Exception e) {
			Logger.error("查询转让申请个数:" + e.getMessage());
			error.code = -1;
			error.msg = "查询转让申请个数异常";
			
			return 0;
		}
		
		return result==null?0:Integer.parseInt(result.toString());
	}
}
