package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import constants.BidPreFixEnum;
import constants.Constants;
import constants.DealType;
import constants.SQLTempletes;
import constants.SupervisorEvent;
import constants.Templets;
import models.t_bids;
import models.t_bill_invests;
import models.t_debt_transfer;
import models.t_invests;
import models.t_red_packages_history;
import models.t_debt_transfer.Status;
import models.t_users;
import models.v_debt_transfer;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import utils.Arith;
import utils.DataUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;
import utils.Security;

/**
 * 债权转让（新）
 * 
 * @ClassName DebtNew
 * @Description 债权转让服务
 * @author zj
 * @Date 2018年7月18日 上午9:35:51
 * @version 1.0.0
 */
public class DebtNew implements Serializable {
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

	public t_invests invest;
	public Map<String, Object> map;
	public User transactionUser;// 竞拍成功会员
	public User specifiedUser;// 转让指定会员
	public String name;
	public String reality_name;
	public String id_number;
	public String min_invest_amount;
	public String transfer_rate;
	public String red_amount;
	public String increase_rate;
	public String deadline;
	public String transferFee;// 转让费 总计 包含了 本金转让费 红包 利息 收入
	public int bid_id;
	public Bid bid;
	public String is_only_new_user;// 是否是新手标

	public Integer audit_admin;// 初审人id
	public String audit_admin_name;// 初审人姓名
	public String audit_admin_real_name;// 初审人真实姓名
	public String audit_time;// 初审时间
	public Integer recheck_admin;// 复审人id
	public String recheck_admin_name;// 复审人姓名
	public String recheck_admin_real_name;// 复审人真实姓名
	public String recheck_time;// 复审时间
	public Integer audit_status;
	public Long login_user_id;// 登录管理员id
	public Double loan_schedule;// 转让进度
	public BigDecimal has_invested_amount;
	public List<DebtInvest> debtInvests;// 债权标投资信息
	public String reason;// 拒绝通过原因
	public String noPassUserName;// 审核不通过人的name
	public String withdraw_time;//撤标时间
	
	public User transferor=new User();//转让人
	public t_debt_transfer debtTransfer;//model
	public Date actionDate;
	public ErrorInfo actionError;
	private int preStatus=1;		//记录之前的状态
	private int preAuditStatus=1;	//记录之前的状态
	public String debtTransferId;//sign 后的 债权标id
	
	public String getNoPassUserName() {
		if (this.audit_status == 3) {
			return this.audit_admin_name;
		}
		if (this.audit_status == 5) {
			return this.recheck_admin_name;
		}
		return noPassUserName;
	}

	public void setNoPassUserName(String noPassUserName) {
		this.noPassUserName = noPassUserName;
	}

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

	private void setInvestModel(long debtTransferId,LockModeType lockModeType) {
		//this.invest=Invest.getInvestByDebtTransferId(debtTransferId);
		//JPA.em().lock(this.invest, lockModeType);//操作t_debt_transfer要求同步invest状态
		this.invest=JPA.em().find(t_invests.class,Invest.getInvestIdByDebtTransferId(debtTransferId) ,lockModeType);
	}

	//---------------------------------------------------构造器begin-------------------------------------------
	public DebtNew() {
		this.actionDate=new Date();
		this.actionError=new ErrorInfo();
	}
	/**
	 * 构造business
	 * actionDate用于更新时间
	 * @param actionDate
	 */
	public DebtNew(Date actionDate) {
		this.actionDate=actionDate;
		this.actionError=new ErrorInfo();
	}
	/**
	 * 构造business
	 * actionDate用于更新时间
	 * error用于传递调用者的error
	 * @param actionDate
	 * @param error
	 */
	public DebtNew(Date actionDate,ErrorInfo error) {
		this.actionDate=actionDate;
		this.actionError=error;
	}
	/**
	 * 构造business
	 * error用于传递调用者的error
	 * @param error
	 */
	public DebtNew(ErrorInfo error) {
		this.actionDate=new Date();
		this.actionError=error;
	}
	
	public long getId() {
		return _id;
	}
	/**
	 * 使business
	 * 获得t_debt_transfer
	 * 并获得transfers的转让人
	 * @param id {@link t_debt_transfer#id}
	 */
	public void setId(long id){
		//this.debtTransfer=t_debt_transfer.findById(id);
		this.getModelByPessimisticWrite(id);
	}
	
	/**
	 * 使business
	 * 获得t_debt_transfer
	 * 并获得transfers的转让人
	 * @param id {@link t_debt_transfer#id}
	 */
	public void setIdNoLock(long id){
		//this.debtTransfer=t_debt_transfer.findById(id);
		this.getModel(id,LockModeType.NONE);
	}
	
	/**
	 * 使business
	 * 获得t_debt_transfer并开启排它锁
	 * 并获得transfers的转让人
	 * @param id {@link t_debt_transfer#id}
	 */
	public void getModelByPessimisticWrite(long id){
		//ESSIMISTIC_READ=lock in share mode  
		//PESSIMISTIC_WRITE=for update 
		this.getModel(id,LockModeType.PESSIMISTIC_WRITE);
	}
	/**
	 * 使business
	 * 获得t_debt_transfer并开启JPA锁{LockModeType}
	 * 并获得transfers的转让人
	 * @param id			{@link t_debt_transfer#id}
	 * @param lockModeType	{@link LockModeType}
	 */
	public void getModel(long id,LockModeType lockModeType){
		this.actionError.clear();
		try {
			this._id=id;
			this.setInvestModel(id,lockModeType);//获取投资
			this.debtTransfer=JPA.em().find(t_debt_transfer.class, id, lockModeType);//获取债权转让
			this.preStatus=debtTransfer.status;					//记录之前的状态
			this.preAuditStatus=debtTransfer.audit_status;		//记录之前的状态
			
			//获得转让人
			this.setTransferor(this.debtTransfer.user_id);
			//验证setTransferor是否正确
			if(this.actionError.code < 1){
				this.actionError.setWrongMsg("转让人－＞"+this.debtTransfer.user_id+"－＞"+this.actionError.msg);
				Logger.error("DebtNew.getModel.setTransferor:"+this.debtTransfer.user_id+this.actionError.msg);
				JPA.setRollbackOnly();
				return;
			}
		} catch (Exception e) {
			this._id = -1;
			this.actionError.setWrongMsg(e.getMessage());
			Logger.error("DebtNew.getModel"+id+e.getMessage());
			JPA.setRollbackOnly();
			e.printStackTrace();
			return;
		}
		if (this.debtTransfer == null) {
			this._id = -1;
			this.actionError.setWrongMsg("数据实体对象不存在!");
			Logger.error("DebtNew.getModel"+id+this.actionError.msg);
			JPA.setRollbackOnly();
			return;
			//throw new NullPointerException(this.actionError.msg);
		}
		this.actionError.code=1;
	}
	/**
	 * 使business
	 * 获得transfers的转让人
	 * @param user_id {@link t_users#id}转让人Id
	 */
	private void setTransferor(long user_id){
		this.actionError.clear();
		try {
			this.transferor.id=user_id;
		} catch (Exception e) {
			this.actionError.setWrongMsg(e.getMessage());
			Logger.error("DebtNew.setTransferor:"+user_id+e.getMessage());
			e.printStackTrace();
			return;
		}
		if (this.transferor == null) {
			this.actionError.setWrongMsg("数据实体对象不存在!");
			return;
			//throw new NullPointerException(this.actionError.msg);
		}
		this.actionError.code=1;
	}
	//---------------------------------------------------构造器end-------------------------------------------
	/**
	 * 根据投资invest_id获取对应生存状态的t_debt_transfer:status（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）
	 * @param investId
	 * @param error
	 * @return
	 */
	public static t_debt_transfer getDebtTransferByInvestId(long invest_id){
		t_debt_transfer debttransfer = null;
		try {
			debttransfer = t_debt_transfer.find(" invest_id = ? and status in (1,2,3) " , invest_id).first();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return debttransfer;
	}
	/**
	 * 根据投资invest_id获取对应生存状态的t_debt_transfer.id
	 * @param invest_id
	 * @param error
	 * @return
	 */
	public static Long getDebtTransferIdByInvestId(long invest_id){
		long debtTransferId = 0;
		try {
			String sql = "select id "
						+ "from t_debt_transfer "
						+ "where invest_id = ? and status in (1,2,3) ";
			Query query = JPA.em().createNativeQuery(sql).setParameter(1, invest_id);
			debtTransferId = (Long) query.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return debtTransferId;
	}
/**
	 * 所有审核中的债权转让标（新）
	 * 
	 * @param userId
	 * @param type     0:全部 1：编号 2：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer> queryAllAuditingTransfersNew(String typeStr, String startDateStr,
			String endDateStr, String keyWords, String orderTypeStr, String currPageStr, String pageSizeStr) {

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		int type = 0;
		int orderType = 0;
		String key = new String();

		String[] typeCondition = { "and (tu.reality_name like ? or   t_debt_transfer.title like ? )",
				"and  t_debt_transfer.title like ? ", " and tu.reality_name like ?" };
		String[] orderCondition = { " order by t_debt_transfer.time desc ", " order by t_debt_transfer.time  ",
				" order by t_debt_transfer.time desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_AUDITING_TRANSFERS_NEW);
		List<Object> params = new ArrayList<Object>();

		List<v_debt_transfer> transfersList = new ArrayList<v_debt_transfer>();
		PageBean<v_debt_transfer> page = new PageBean<v_debt_transfer>();

		EntityManager em = JPA.em();
		// String obj = OptionKeys.getvalue(OptionKeys.TRANFER_NUMBER, new
		// ErrorInfo());
		// obj = obj == null ? "" : obj;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		if (!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)) {
			Date startDate = DateUtil.strDateToStartDate(startDateStr);
			Date endDate = DateUtil.strDateToEndDate(endDateStr);

			sql.append(" and t_debt_transfer.time >= ? and t_debt_transfer.time <= ?");
			params.add(startDate);
			params.add(endDate);
		}

		if (StringUtils.isNotBlank(keyWords)) {
			key = keyWords.replace(BidPreFixEnum.TRANSFER.getCode(), "");
			if (type != 0) {
				sql.append(typeCondition[type]);
				params.add("%" + key + "%");
			} else if (type == 0) {
				sql.append(typeCondition[type]);
				params.add("%" + keyWords + "%");
				params.add("%" + key + "%");
			}
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);

		sql.append(orderCondition[orderType]);

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			Query query = em.createNativeQuery(sql.toString(), v_debt_transfer.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
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
	 * 所有转让中的债权转让标(新)
	 * 
	 * @param userId
	 * @param type     0:全部 1：编号 2：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer> queryAllTransferingDebtsNew(String typeStr, String keyWords,
			String orderTypeStr, String currPageStr, String pageSizeStr, String startDateStr, String endDateStr) {

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		int type = 0;
		int orderType = 0;
		String key = new String();

		String[] typeCondition = { "and (tu.reality_name like ? or   t_debt_transfer.title like ? )",
				"and  t_debt_transfer.title like ? ", " and tu.reality_name like ?" };
		String[] orderCondition = { " order by t_debt_transfer.time desc ", " order by t_debt_transfer.time  ",
				" order by t_debt_transfer.time desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_TRANSFERING_NEW);
		List<Object> params = new ArrayList<Object>();

		List<v_debt_transfer> transfersList = new ArrayList<v_debt_transfer>();
		PageBean<v_debt_transfer> page = new PageBean<v_debt_transfer>();

		EntityManager em = JPA.em();
		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		if (!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)) {
			Date startDate = DateUtil.strDateToStartDate(startDateStr);
			Date endDate = DateUtil.strDateToEndDate(endDateStr);

			sql.append(" and t_debt_transfer.time >= ? and t_debt_transfer.time <= ?");
			params.add(startDate);
			params.add(endDate);
		}

		if (StringUtils.isNotBlank(keyWords)) {
			key = keyWords.replace(BidPreFixEnum.TRANSFER.getCode(), "");
			if (type != 0) {
				sql.append(typeCondition[type]);
				params.add("%" + key + "%");
			} else if (type == 0) {
				sql.append(typeCondition[type]);
				params.add("%" + keyWords + "%");
				params.add("%" + key + "%");
			}
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);

		/*
		 * if(type == 0){ sql.append(typeCondition[0]); String key_param = key; key =
		 * key.replace(obj + "", ""); params.add("%"+key+"%");
		 * params.add("%"+key_param+"%"); }else{ sql.append(typeCondition[type]);
		 * if(type == 1){ key = key.replace(obj + "", ""); } params.add("%"+key+"%"); }
		 */
		sql.append(orderCondition[orderType]);

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			Query query = em.createNativeQuery(sql.toString(), v_debt_transfer.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
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
	 * 
	 * @param userId
	 * @param type     0:全部 1：编号 2：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer> queryAllSuccessedDebtsNew(String typeStr, String startDateStr,
			String endDateStr, String keyWords, String orderTypeStr, String currPageStr, String pageSizeStr) {

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		int type = 0;
		int orderType = 0;
		String key = new String();

		String[] typeCondition = { "and (tu.reality_name like ? or   t_debt_transfer.title like ? )",
				"and  t_debt_transfer.title like ? ", " and tu.reality_name like ?" };
		String[] orderCondition = { " order by t_debt_transfer.time desc ", " order by t_debt_transfer.time  ",
				" order by t_debt_transfer.time desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_TRANSFERS_SUCCESS_NEW);
		List<Object> params = new ArrayList<Object>();

		List<v_debt_transfer> transfersList = new ArrayList<v_debt_transfer>();
		PageBean<v_debt_transfer> page = new PageBean<v_debt_transfer>();

		EntityManager em = JPA.em();

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		if (!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)) {
			Date startDate = DateUtil.strDateToStartDate(startDateStr);
			Date endDate = DateUtil.strDateToEndDate(endDateStr);

			sql.append(" and t_debt_transfer.time >= ? and t_debt_transfer.time <= ?");
			params.add(startDate);
			params.add(endDate);
		}

		if (StringUtils.isNotBlank(keyWords)) {
			key = keyWords.replace(BidPreFixEnum.TRANSFER.getCode(), "");
			if (type != 0) {
				sql.append(typeCondition[type]);
				params.add("%" + key + "%");
			} else if (type == 0) {
				sql.append(typeCondition[type]);
				params.add("%" + keyWords + "%");
				params.add("%" + key + "%");
			}
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);

		sql.append(orderCondition[orderType]);

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			Query query = em.createNativeQuery(sql.toString(), v_debt_transfer.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
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
	 * 所有未审核通过的债权转让标
	 * 
	 * @param userId
	 * @param type     0:全部 1：编号 2：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer> queryAllNopassDebtsNew(String typeStr, String startDateStr,
			String endDateStr, String keyWords, String orderTypeStr, String currPageStr, String pageSizeStr) {

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		int type = 0;
		int orderType = 0;
		String key = new String();

		String[] typeCondition = { "and (tu.reality_name like ? or   t_debt_transfer.title like ? )",
				"and  t_debt_transfer.title like ? ", " and tu.reality_name like ?" };
		String[] orderCondition = { " order by t_debt_transfer.time desc ", " order by t_debt_transfer.time  ",
				" order by t_debt_transfer.time desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_NO_PASS_TRANSFERS_NEW);
		List<Object> params = new ArrayList<Object>();

		List<v_debt_transfer> transfersList = new ArrayList<v_debt_transfer>();
		PageBean<v_debt_transfer> page = new PageBean<v_debt_transfer>();

		EntityManager em = JPA.em();

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		if (!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)) {
			Date startDate = DateUtil.strDateToStartDate(startDateStr);
			Date endDate = DateUtil.strDateToEndDate(endDateStr);

			sql.append(" and t_debt_transfer.time >= ? and t_debt_transfer.time <= ?");
			params.add(startDate);
			params.add(endDate);
		}

		if (StringUtils.isNotBlank(keyWords)) {
			key = keyWords.replace(BidPreFixEnum.TRANSFER.getCode(), "");
			if (type != 0) {
				sql.append(typeCondition[type]);
				params.add("%" + key + "%");
			} else if (type == 0) {
				sql.append(typeCondition[type]);
				params.add("%" + keyWords + "%");
				params.add("%" + key + "%");
			}
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);

		sql.append(orderCondition[orderType]);

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			Query query = em.createNativeQuery(sql.toString(), v_debt_transfer.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
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
	 * 所有失败的债权转让标
	 * 
	 * @param userId
	 * @param type     0:全部 1：编号 2：债权人
	 * @param params
	 * @param currPage
	 * @return
	 */
	public static PageBean<v_debt_transfer> queryAllFailureDebtsNew(String typeStr, String startDateStr,
			String endDateStr, String keyWords, String orderTypeStr, String currPageStr, String pageSizeStr) {

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		int type = 0;
		int orderType = 0;
		String key = new String();

		String[] typeCondition = { "and (tu.reality_name like ? or   t_debt_transfer.title like ? )",
				"and  t_debt_transfer.title like ? ", " and tu.reality_name like ?" };
		String[] orderCondition = { " order by t_debt_transfer.time desc ", " order by t_debt_transfer.time  ",
				" order by t_debt_transfer.time desc " };

		StringBuffer sql = new StringBuffer("");
		sql.append(SQLTempletes.SELECT);
		sql.append(SQLTempletes.V_DEBT_TRANSFER_FAILURE_NEW);
		List<Object> params = new ArrayList<Object>();

		List<v_debt_transfer> transfersList = new ArrayList<v_debt_transfer>();
		PageBean<v_debt_transfer> page = new PageBean<v_debt_transfer>();

		EntityManager em = JPA.em();

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (NumberUtil.isNumericInt(typeStr)) {
			type = Integer.parseInt(typeStr);
		}

		if (NumberUtil.isNumericInt(orderTypeStr)) {
			orderType = Integer.parseInt(orderTypeStr);
		}

		if (type < 0 || type > 2) {
			type = 0;
		}

		if (orderType < 0 || orderType > 8) {
			orderType = 0;
		}

		if (!StringUtils.isBlank(startDateStr) && !StringUtils.isBlank(endDateStr)) {
			Date startDate = DateUtil.strDateToStartDate(startDateStr);
			Date endDate = DateUtil.strDateToEndDate(endDateStr);

			sql.append(" and t_debt_transfer.time >= ? and t_debt_transfer.time <= ?");
			params.add(startDate);
			params.add(endDate);
		}

		if (StringUtils.isNotBlank(keyWords)) {
			key = keyWords.replace(BidPreFixEnum.TRANSFER.getCode(), "");
			if (type != 0) {
				sql.append(typeCondition[type]);
				params.add("%" + key + "%");
			} else if (type == 0) {
				sql.append(typeCondition[type]);
				params.add("%" + keyWords + "%");
				params.add("%" + key + "%");
			}
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("keyWords", keyWords);
		conditionMap.put("orderType", orderType);
		conditionMap.put("type", type);
		conditionMap.put("startDateStr", startDateStr);
		conditionMap.put("endDateStr", endDateStr);

		sql.append(orderCondition[orderType]);

		page.pageSize = pageSize;
		page.currPage = currPage;

		try {
			Query query = em.createNativeQuery(sql.toString(), v_debt_transfer.class);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
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
	 * 根据债权标id获取相关信息
	 * 
	 * @param id
	 * @return
	 */
	public static DebtNew debtTransferDetail(Integer id) {
		String sql = "select  a.bid_id,a.id,a.no,a.title ,tu.name,tu.reality_name,tu.id_number,a.period,a.min_invest_amount,a.transfer_rate,a.red_amount,a.increase_rate,DATE_FORMAT(a.deadline,'%Y-%m-%d %H:%i:%s') as deadline,a.is_only_new_user ,a.audit_status,a.debt_amount,a.has_invested_amount,"
				+ " ts1.name as audit_name1,ts1.reality_name as reality_name1,DATE_FORMAT(a.audit_time,'%Y-%m-%d %H:%i:%s') as audit_time,a.loan_schedule,a.reason, DATE_FORMAT(a.withdraw_time,'%Y-%m-%d %H:%i:%s') as withdraw_time, "
				+ " ts2.name as audit_name2,ts2.reality_name as reality_name2, DATE_FORMAT(a.recheck_time,'%Y-%m-%d %H:%i:%s') as recheck_time"
				+ " from  t_debt_transfer a  left   join t_users tu  	 on  a.user_id=tu.id "
				+ " left join t_supervisors ts1 on a.audit_admin =ts1.id "
				+ " left join t_supervisors ts2 on a.recheck_admin=ts2.id  where a.id=? ";
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, id);
		List debt_transfers = query.getResultList();
		if (debt_transfers == null || debt_transfers.size() <= 0) {
			return null;
		} else {
			Map<String, Object> objects = (Map<String, Object>) debt_transfers.get(0);
			DebtNew debtNew = new DebtNew();
			debtNew.no = objects.get("no").toString();
			debtNew.title = objects.get("title").toString();
			debtNew.bid_id = Integer.parseInt(EmptyUtil.obj20(objects.get("bid_id")).toString());
			debtNew.name = objects.get("name").toString();
			debtNew.reality_name = EmptyUtil.obj2Str(objects.get("reality_name"));
			debtNew.id_number = EmptyUtil.obj2Str(objects.get("id_number"));
			debtNew.period = Integer.parseInt(EmptyUtil.obj20(objects.get("period")).toString());
			debtNew.min_invest_amount = EmptyUtil.obj20(objects.get("min_invest_amount")).toString();
			debtNew.transfer_rate = EmptyUtil.obj20(objects.get("transfer_rate")).toString();
			debtNew.red_amount = EmptyUtil.obj20(objects.get("red_amount")).toString();
			debtNew.increase_rate = EmptyUtil.obj20(objects.get("increase_rate")).toString();
			debtNew.deadline = EmptyUtil.obj2Str(objects.get("deadline"));
			debtNew.transferFee = new BigDecimal(debtNew.transfer_rate).add(new BigDecimal(debtNew.red_amount))
					.add(new BigDecimal(debtNew.increase_rate)).toString();
			debtNew.audit_admin_name = EmptyUtil.obj2Str(objects.get("audit_name1"));
			debtNew.audit_admin_real_name = EmptyUtil.obj2Str(objects.get("reality_name1"));
			debtNew.audit_time = EmptyUtil.obj2Str(objects.get("audit_time"));
			debtNew.recheck_admin_name = EmptyUtil.obj2Str(objects.get("audit_name2"));
			debtNew.recheck_admin_real_name = EmptyUtil.obj2Str(objects.get("reality_name2"));
			debtNew.recheck_time = EmptyUtil.obj2Str(objects.get("recheck_time"));
			debtNew.bid = Bid.bidDetail(debtNew.bid_id);
			debtNew.is_only_new_user = EmptyUtil.obj2Str(objects.get("is_only_new_user"));
			debtNew.audit_status = Integer.parseInt(EmptyUtil.obj20(objects.get("audit_status")).toString());
			debtNew.sign = Supervisor.currSupervisor().getSign();
			debtNew.loan_schedule = EmptyUtil.obj20(objects.get("loan_schedule")).doubleValue();
			debtNew.debtAmount = EmptyUtil.obj20(objects.get("debt_amount")).doubleValue();
			debtNew.has_invested_amount = EmptyUtil.obj20(objects.get("has_invested_amount"));
			debtNew.debtInvests = DebtInvest.debtTransferDetail(EmptyUtil.obj20(objects.get("id")).intValue());
			debtNew.reason = EmptyUtil.obj2Str(objects.get("reason"));
			debtNew._id = EmptyUtil.obj20(objects.get("id")).longValue();
			debtNew.withdraw_time = EmptyUtil.obj2Str(objects.get("withdraw_time"));
			debtNew.debtTransferId=Security.addSign(debtNew._id, Constants.BID_ID_SIGN);
			return debtNew;
		}
	}

	/**
	 * 根据债权标id获取相关信息
	 * 
	 * @param id
	 * @return
	 */
	public static DebtNew debtTransferingDetail(Integer id) {
		DebtNew debtNew = debtTransferDetail(id);
		return debtNew;
	}
	
	//----------------------------------------------------------------business begin----------------------------------------------------------------
	/**
	 * 债权转让审核接口
	 * @param debtId
	 * @param status
	 * @param adminId
	 * @param title
	 * @param is_only_new_user
	 * @param reason
	 * @param min_invest_amount
	 * @param error
	 */
	public void updateDebtAuditStatus(int status, String title,boolean is_only_new_user, String reason,double min_invest_amount){
		this.actionError.clear();
		try {
			//审核通过与未通过的资金处理
			//status:要审核的状态,==debtTransfer.audit_status
			//debtTransfer.audit_status:审核状态（1未审核 2 初审通过 3初审不通过 4复审通过 5复审不通过） 
			//debtTransfer.status:标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标） 
			if(status == t_debt_transfer.AuditStatus.AUDIT_NOT_PASS.code || status == t_debt_transfer.AuditStatus.RECHECK_NOT_PASS.code){//审核未通过,退还债权转让服务费
				//本金保障金账户 转至出让人可用余额
				this.auditingToNotThrough(reason);
			}else if(status == t_debt_transfer.AuditStatus.AUDIT_PASS.code ){//初审
				this.auditPass(title,is_only_new_user,reason,min_invest_amount);
			}else if(status == t_debt_transfer.AuditStatus.RECHECK_PASS.code ){ //复审
				this.recheckPass(title,is_only_new_user,reason,min_invest_amount);
			}
			if(this.actionError.code<1){
				Logger.error("更新债权转让标的状态时：" + this.actionError.msg);
				JPA.setRollbackOnly();
				return;
			}
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("更新债权转让标的状态时：" + e.getMessage());
			this.actionError.setWrongMsg("更新债权转让标的状态－＞数据未更新");
			e.printStackTrace();
			return;
		}
		this.actionError.code=1;
	}
	/**
	 * 债权转让审核通过
	 */
	private void auditPass(String title,boolean is_only_new_user, String reason,double min_invest_amount){
		this.actionError.clear();
		long currSupervisor=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
		if(this.debtTransfer.deadline.before(this.actionDate)){
			this.actionError.setWrongMsg("当前债权转让标已过期,请撤标");
			return;
		}
		if(this.debtTransfer.audit_status!=t_debt_transfer.AuditStatus.AUDITING.code 
				&& this.debtTransfer.status!=t_debt_transfer.Status.AUDITING.code){
			this.actionError.setWrongMsg("当前债权转让状态不可审核");
			return;
		}
		this.debtTransfer.audit_status=t_debt_transfer.AuditStatus.AUDIT_PASS.code;
		this.debtTransfer.audit_time=this.actionDate;
		this.debtTransfer.audit_admin=currSupervisor;
		this.debtTransfer.is_only_new_user=is_only_new_user;
		this.debtTransfer.title=title;
		this.debtTransfer.reason=reason;
		this.debtTransfer.min_invest_amount=min_invest_amount;
		this.debtTransfer.save();
		this.actionError.code=1;
	}
	private void recheckPass(String title,boolean is_only_new_user, String reason,double min_invest_amount){
		this.actionError.clear();
		long currSupervisor=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
		if(this.debtTransfer.deadline.before(this.actionDate)){
			this.actionError.setWrongMsg("当前债权转让标已过期,请撤标");
			return;
		}
		if(this.debtTransfer.audit_status!=t_debt_transfer.AuditStatus.AUDIT_PASS.code 
				&& this.debtTransfer.status!=t_debt_transfer.Status.AUDITING.code){
			this.actionError.setWrongMsg("当前债权转让状态不可复审");
			return;
		}
		if(this.debtTransfer.audit_admin ==0){
			this.actionError.setWrongMsg("当前债权转让标没有初审！");
			return;
		}
		if(this.debtTransfer.audit_admin == currSupervisor ){ //已初审
			this.actionError.setWrongMsg("您已审核过，请勿重复审核！");
			return;
		}
		this.debtTransfer.audit_status=t_debt_transfer.AuditStatus.RECHECK_PASS.code;
		this.debtTransfer.status=t_debt_transfer.Status.FUNDRAISE.code;
		this.debtTransfer.recheck_time=this.actionDate;
		this.debtTransfer.recheck_admin=currSupervisor;
		this.debtTransfer.is_only_new_user=is_only_new_user;
		this.debtTransfer.title=title;
		this.debtTransfer.reason=reason;
		this.debtTransfer.min_invest_amount=min_invest_amount;
		this.debtTransfer.save();
		this.actionError.code=1;
	}
	
	/**
	 * 债权转让
	 * 转让中手动撤标,或到期未满标流标
	 * 获得排它锁的debtTransfer
	 * 确定当前debtTransfer的状态是否可以撤标/流标
	 * 判断是否资金存管后走相应的流标方法
	 * @param debtTransferId {@link t_debt_transfer#id}
	 */
	public void transferingToNotThrough(long debtTransferId,String reason){
		this.actionError.clear();
		
		//得到排它锁的debtTransfer
		this.getModelByPessimisticWrite(debtTransferId);
		if(this.actionError.code<1){
			this.actionError.setWrongMsg("撤标/流标－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		
		this.failingDebtTransfer(reason);
		if(this.actionError.code<1){
			this.actionError.setWrongMsg("撤标/流标－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		this.actionError.msg = "债权转让,撤标/流标成功,债权转让状态:["+this.debtTransfer.getStrStatus()+"]!";
		this.actionError.code=1;
	}

	/**
	 * t_debt_transfer.Status.AUDITING		审核中
	 * t_debt_transfer.Status.FUNDRAISE		转让中
	 * 强制关闭债权转让
	 * @param debtTransferId {@link t_debt_transfer#id}
	 */
	public void forceFailingDebtTransfer(long debtTransferId,String reason){
		this.actionError.clear();
		
		//得到排它锁的debtTransfer
		this.getModelByPessimisticWrite(debtTransferId);
		if(this.actionError.code<1){
			this.actionError.setWrongMsg("强制关闭债权转让－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		
		forceFailingDebtTransfer(reason);
		if(this.actionError.code<1){
			this.actionError.setWrongMsg("强制关闭债权转让－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		this.actionError.msg = "债权转让,强制关闭债权转让成功,债权转让标状态:["+this.debtTransfer.getStrStatus()+"]!";
		this.actionError.code=1;
	}
	/**
	 * t_debt_transfer.Status.AUDITING		审核中
	 * t_debt_transfer.Status.FUNDRAISE		转让中
	 * 强制关闭债权转让
	 */
	private void forceFailingDebtTransfer(String reason) {
		this.actionError.clear();
		
		if(this.debtTransfer.status==t_debt_transfer.Status.AUDITING.code){//审核中
			this.auditingToNotThrough(reason);
			if(this.actionError.code < 1){
				this.actionError.setWrongMsg("退审－＞"+this.actionError.msg);
				return;
			}
		}else if (this.debtTransfer.status==t_debt_transfer.Status.FUNDRAISE.code){
			this.failingDebtTransfer(reason);
			if(this.actionError.code < 1){
				this.actionError.setWrongMsg("撤标/流标－＞"+this.actionError.msg);
				return;
			}
		}else{
			this.actionError.setWrongMsg("状态不可强制关闭!");
			return;
		}
		this.actionError.code=1;
	}
	/**
	 * 债权转让:
	 * 转让中手动撤标,或到期未满标流标
	 * 1.设置撤标操作人,操作时间,撤标状态
	 * 2.返还 转让人 的 手续费
	 * 3.返还投资人投资金额 
	 * 4.添加事件
	 * 5.删除银行存管缓存
	 */
	private void failingDebtTransfer(String reason) {
		this.actionError.clear();
		
		//1.确定当前债权转让时可流标的状态 
		if(t_debt_transfer.Status.FUNDRAISE.code != this.debtTransfer.status){
			this.actionError.setWrongMsg("审核失败,请确定当前债权转让标状态,当前标的不是["
											+t_debt_transfer.getStrStatus(t_debt_transfer.Status.FUNDRAISE.code)+"]! 是["
											+this.debtTransfer.getStrStatus()+"]!");
			return;
		}
		
		//2.当前是否是银行存管,如果存管,退出,走存管
		if(Constants.IPS_ENABLE) {
			//TODO
			//流标调用环迅托管接口, 处理业务逻辑在托管实现类回调方法中  error  client  标的id   流标方式  
			//PaymentProxy.getInstance().bidAuditFail(actionError, Constants.PC, debtTransferId, IPSConstants.BID_CANCEL_I);	
			this.actionError.setWrongMsg("无法使用银行存管!");
			return;
		}
		
		//1.设置撤标操作人,操作时间,撤标状态
		//设置debtTransfer撤标理由
		this.debtTransfer.reason=reason;
		//设置debtTransfer撤标操作人
		this.debtTransfer.withdraw_admin=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
		//设置debtTransfer撤标时间
		this.debtTransfer.withdraw_time=this.actionDate;
		//设置debtTransfer撤标状态
		this.debtTransfer.status=Status.FLOW.code;
		
		//保存debtTransfer修改的状态
		try {
			this.debtTransfer.save();//已上排它锁,可以直接操作
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("DebtNew.failingDebtTransfer:" +this.debtTransfer.id+ e.getMessage());
			this.actionError.setWrongMsg(e.getMessage());
			return;
		}
		
		//还原原投资和投资账单的状态
		this.restoreInvestAndBillInvest();
		if(this.actionError.code< 1){
			this.actionError.setWrongMsg("返还转让人的手续费－＞"+this.actionError.msg);
			return;
		}
		
		//2.返还 转让人 的 手续费
		this.relieveUserCommissions();
		if(this.actionError.code < 1){
			this.actionError.setWrongMsg("返还转让人服务费－＞"+this.actionError.msg);
			return;
		}
		
		//3.返还投资人投资金额 
		this.relieveUserInvest(); 
		if(this.actionError.code < 1){
			this.actionError.setWrongMsg("返还投资用户投资金额－＞"+this.actionError.msg);
			return;
		}
		
		
		//4.添加事件
		DealDetail.supervisorEvent(this.debtTransfer.withdraw_admin, SupervisorEvent.CHANGE_DEBT_TRANSFER_STATE, 
				"债权转让标:"+t_debt_transfer.getStrStatus(this.preStatus)+"->"+this.debtTransfer.getStrStatus()
				+"["+this.debtTransfer.id+"]", this.actionError);
		if(this.actionError.code < 0){//DealDetail.supervisorEvent方法成功返回0
			this.actionError.setWrongMsg( "添加事件－＞"+this.actionError.msg);
			return;
		}
		
		//5.删除银行存管缓存
		if(Constants.IPS_ENABLE) {
			//TODO
			//deleteIPSDebtTransfer(IPSConstants.BID_CANCEL_I);
			this.actionError.setWrongMsg("无法使用银行存管!");
			return;
		}
		
		this.actionError.code = 1;
	}
	
	/**
	 * 债权转让->审核中退审
	 * @param debtTransferId {@link t_debt_transfer#id}
	 */
	public void auditingToNotThrough(long debtTransferId,String reason){
		this.actionError.clear();
		
		//得到排它锁的debtTransfer
		this.getModelByPessimisticWrite(debtTransferId);
		//JPA.em().lock(this.debtTransfer, LockModeType.PESSIMISTIC_WRITE);

		if(this.actionError.code< 1){
			this.actionError.setWrongMsg("退审－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		
		this.auditingToNotThrough(reason);
		if(this.actionError.code< 1){
			this.actionError.setWrongMsg("退审－＞"+debtTransferId+"－＞"+this.actionError.msg);
			Logger.error(this.actionError.msg);
			JPA.setRollbackOnly();
			return;
		}
		this.actionError.msg = "债权转让,退审成功,债权转让标状态:["+this.debtTransfer.getStrStatus()+"]!审核状态:["+this.debtTransfer.getStrAuditStatus()+"]";
		this.actionError.code=1;
	}
	/**
	 * 债权转让->审核中退审
	 */
	private void auditingToNotThrough(String reason){
		this.actionError.clear();
		
		if(t_debt_transfer.Status.AUDITING.code != this.debtTransfer.status){
			this.actionError.setWrongMsg("审核失败,请确定当前债权转让标状态,当前标的不是["
					+t_debt_transfer.getStrStatus(t_debt_transfer.Status.AUDITING.code)+"]! 是["
					+this.debtTransfer.getStrStatus()+"]!");
			return;
		}
		long currSupervisor=Supervisor.currSupervisor()==null?0:Supervisor.currSupervisor().id;
		//1.设置撤标操作人,操作时间,债权转让标状态,审核状态
		if(this.preAuditStatus==t_debt_transfer.AuditStatus.AUDITING.code){
			//设置debtTransfer审核人
			this.debtTransfer.audit_admin=currSupervisor;
			//设置debtTransfer审核时间
			this.debtTransfer.audit_time=this.actionDate;
			//设置debtTransfer审核状态状态
			this.debtTransfer.audit_status=t_debt_transfer.AuditStatus.AUDIT_NOT_PASS.code;
		}else if(this.preAuditStatus==t_debt_transfer.AuditStatus.AUDIT_PASS.code){
			//设置debtTransfer审核人
			this.debtTransfer.recheck_admin=currSupervisor;
			//设置debtTransfer审核时间
			this.debtTransfer.recheck_time=this.actionDate;
			//设置debtTransfer审核状态状态
			this.debtTransfer.audit_status=t_debt_transfer.AuditStatus.RECHECK_NOT_PASS.code;
		}else{
			this.actionError.setWrongMsg("审核失败,请确定当前债权转让标状态,当前标的状态 是["
					+this.debtTransfer.getStrAuditStatus()+"]!");
			return;
		}
		//设置debtTransfer不通过理由
		this.debtTransfer.reason=reason;
		//设置debtTransfer不通过状态
		this.debtTransfer.status=Status.NOT_THROUGH.code;
				
		//保存debtTransfer修改的状态
		try {
			this.debtTransfer.save();//已上排它锁,可以直接操作
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("DebtNew.auditingToNotThrough:" +this.debtTransfer.id+ e.getMessage());
			this.actionError.setWrongMsg(e.getMessage());
			return;
		}
		
		//还原原投资和投资账单的状态
		this.restoreInvestAndBillInvest();
		if(this.actionError.code< 1){
			this.actionError.setWrongMsg("返还转让人的手续费－＞"+this.actionError.msg);
			return;
		}
		
		//2.返还 转让人 的 手续费
		this.relieveUserCommissions();
		if(this.actionError.code< 1){
			this.actionError.setWrongMsg("还原原投资和投资账单的状态－＞"+this.actionError.msg);
			return;
		}
		
		//3.添加事件
		DealDetail.supervisorEvent( currSupervisor ,SupervisorEvent.AUDIT_DEBT_TRANSFER ,
				"债权转让标:"+t_debt_transfer.getStrStatus(this.preStatus)+"->"+this.debtTransfer.getStrStatus()
				+" 审核状态:"+t_debt_transfer.getStrStatus(this.preAuditStatus)+"->"+this.debtTransfer.getStrAuditStatus()
				+"["+this.debtTransfer.id+"]", this.actionError);
		if(this.actionError.code < 0){//DealDetail.supervisorEvent方法成功返回0
			this.actionError.setWrongMsg("添加事件－＞"+this.actionError.msg);
			return;
		}
		
		this.actionError.code = 1;
	}
	/**
	 * 债权转让->退审/撤标/流标->还原原投资和投资账单的状态
	 */
	private void restoreInvestAndBillInvest(){
		this.actionError.clear();
		//修改t_invests表转让状态
		try {
			this.invest.transfer_status = 0;
			this.invest.transfers_id = 0;
			this.invest.transfers_time = null;
			this.invest.save();
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("修改t_invests转让状态出错:" +this.debtTransfer.id+ e.getMessage());
			this.actionError.setWrongMsg(e.getMessage());
			return;
		}
		
		//修改t_bill_invest表转让状态
		try {
			//List<t_bill_invests> billInvests = t_bill_invests.find(" invest_id = ? and status = -8 ", this.debtTransfer.invest_id).fetch();
			List<t_bill_invests> billInvests=BillInvests.queryBillInvestsByInvestId(this.debtTransfer.invest_id);
			for (t_bill_invests billInvest : billInvests) {
				if(billInvest.status==-8){
					billInvest.status = -1;
					billInvest.save();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("修改t_bill_invest转让状态出错:" +this.debtTransfer.id+ e.getMessage());
			this.actionError.setWrongMsg(e.getMessage());
			return;
		}
		actionError.code = 1;
	}
	/**
	 * 债权转让->退审/撤标/流标->返还转让人的转让费
	 */
	private void relieveUserCommissions(){
		this.actionError.clear();
		
		//验证数据--------------------------------------begin---------------------------------
		DataSafety dataSafety = new DataSafety();//数据防篡改
		dataSafety.setId(this.debtTransfer.user_id);//更新数据防篡改字段
		//验证金额是否被篡改
		boolean falg = dataSafety.signCheck(this.actionError);
		/*
		//business.Bid 防篡改,查到篡改时,没有return
		if (this.actionError.code < 0) {//dataSafety.signCheck成功返回0
			return;
		}*/
		//验证数据--------------------------------------end------------------------------------
		
		//返还 转让人 的 手续费-----------------------------begin---------------------------------
		//DebtTransfer.auditFailure(this.debtTransfer.id,this.actionError);
		double fee = this.debtTransfer.getAllCommissions();
		if(fee > 0.0){
			// 出让人可用金额增加 
			this.actionError.code=DealDetail.addUserFund(this.debtTransfer.user_id, fee);
			if (this.actionError.code < 0) {//DealDetail.addUserFund 异常返回值-1,不异常返回执行条数
				this.actionError.setWrongMsg("增加用户资金错误");
				return;
			}
			
			Map<String, Double> detail = DealDetail.queryUserFund(this.debtTransfer.user_id ,this.actionError);
			if (this.actionError.code < 0) {//DealDetail.queryUserFund成功返回0
				this.actionError.setWrongMsg("验证用户余额－＞"+this.debtTransfer.user_id+"－＞"+this.actionError.msg);
				return;
			}
			double user_amount = detail.get("user_amount");
			double freeze = detail.get("freeze");
			double receive_amount = detail.get("receive_amount");
			//用户的金额是不能小于0的!  
			if(user_amount < 0 || freeze < 0){
				this.actionError.setWrongMsg("投资人资金有误:"+this.debtTransfer.user_id);
				return;
			}

		    //添加交易记录
		    DealDetail dealDetail = new DealDetail(this.debtTransfer.user_id, DealType.RETURN_DEBT_TRANSFER_MANAGEFEE, fee, this.debtTransfer.id, user_amount, 
					freeze, receive_amount, "第"+this.debtTransfer.id+"号债权转让费用退款");
			dealDetail.addDealDetail(this.actionError);
			if (this.actionError.code < 0){//dealDetail.addDealDetail成功返回0
				this.actionError.setWrongMsg("增加交易记录－＞"+this.debtTransfer.user_id+"－＞"+this.actionError.msg);
				return;
			}

			//保障金收支记录 :付款资金流向本金保障账户
			DealDetail.addPlatformDetail(DealType.TRANSFER_FEE, this.debtTransfer.id, -1,this.debtTransfer.user_id,
					DealType.ACCOUNT,fee, DealType.PAY, "平台支出"+this.debtTransfer.user_id+"债权转让管理费",
					this.actionError);
			if(this.actionError.code < 0) {//DealDetail.addPlatformDetail 成功返回0
				this.actionError.setWrongMsg("增加用户资金记录－＞"+this.actionError.msg);
				return ;
			}
		}
		//返还 转让人 的 手续费-----------------------------end---------------------------------

		/*
		//当服务费在冻结金额中时
		DealDetail.relieveFreezeFund(this.debtTransfer.user_id, this.debtTransfer.getAllCommissions(), this.actionError); // 用户对应冻结的资金(保证金)
		if (this.actionError.code < 0) {
			return;
		}
		// 重复验证 转让人 剩余金额是否大于0
		Map<String, Double> detail = DealDetail.queryUserFund(this.debtTransfer.user_id, this.actionError);
		double user_amount = detail.get("user_amount");
		double freeze = detail.get("freeze");
		double receive_amount = detail.get("receive_amount");
		if(user_amount < 0 || freeze < 0){
			this.actionError.code=-1;
			this.actionError.msg = "借款人资金有误!";
			return;
		}
		
		// 添加交易记录 
		DealDetail dealDetail = new DealDetail(this.debtTransfer.user_id,DealType.RELIEVE_DEBT_TRANSFER_COMMISSIONS,
				this.debtTransfer.getAllCommissions(), this.debtTransfer.id, user_amount,freeze, receive_amount,
				"债权转让流标退还转让服务费");
		*/
		/* 被动修改状态:只有在签名正确的情况下才能继续更新签名 */
		
		//更新验证--------------------------------------begin---------------------------------
		if(falg) {
			dataSafety.updateSignWithLock(this.debtTransfer.user_id, this.actionError);
			if (this.actionError.code < 0) {//dataSafety.updateSignWithLock成功返回0
				this.actionError.setWrongMsg("更新验证－＞"+this.actionError.msg);
				return;
			}
		}
		//更新验证--------------------------------------end----------------------------------
		//通知--------------------------------------begin------------------------------------
		String content = null;
		TemplateStation station = new TemplateStation();
		TemplateEmail email = new TemplateEmail();
		email.setId(Templets.E_FIRST_FAIL);
		station.setId(Templets.M_FIRST_FAIL);
		
		if(station.status){
			content = station.content; 
			content = content.replace("userName", this.transferor.name == null ? "" : this.transferor.name); 
			content = content.replace("date", DateUtil.dateToString(this.actionDate)); 
			content = content.replace("title", this.debtTransfer.title); 
			content = content.replace("fee",  DataUtil.formatString(this.debtTransfer.getAllCommissions())); 
			content = content.replace("status", this.debtTransfer.getStrStatus()); 

			StationLetter letter = new StationLetter();
			letter.senderSupervisorId = 1;
			letter.receiverUserId = this.debtTransfer.user_id;
			letter.title = station.title;
			letter.content = content;
			
			letter.sendToUserBySupervisor(this.actionError);
			/*
			if (this.actionError.code < 0) {//letter.sendToUserBySupervisor成功返回0
				this.actionError.msg = "站内信通知－＞"+this.actionError.msg;
				return;
			}*/
		}
		
		if(email.status){
			content = email.content; 
			content = content.replace("userName", this.transferor.name == null ? "" : this.transferor.name); 
			content = content.replace("date", DateUtil.dateToString(this.actionDate)); 
			content = content.replace("title", this.debtTransfer.title); 
			content = content.replace("fee",  DataUtil.formatString(this.debtTransfer.getAllCommissions())); 
			content = content.replace("status", this.debtTransfer.getStrStatus()); 
			email.addEmailTask(this.transferor.email, email.title, content);
		}
		//通知--------------------------------------end------------------------------------
		actionError.code = 1;
		
	}
	
	/**
	 * 债权转让->退审/撤标/流标->返还受让人的投资金额
	 */
	private void relieveUserInvest() {
		this.actionError.clear();
		
		List<DebtInvest> debtInvestList;
		try {
			debtInvestList = DebtInvest.queryAllInvests(this.debtTransfer.id);
		} catch (Exception e) {
			this.actionError.setWrongMsg(e.getMessage());
			Logger.error("DebtNew.relieveUserInvest:" +this.debtTransfer.id+ e.getMessage());
			e.printStackTrace();
			return;
		}
		
		DealDetail dealDetail = null;
		Map<String, Double> detail = null;
		DataSafety dataSafety = new DataSafety();//数据防篡改
		User user = new User();
		boolean falg = false;
		
		for (DebtInvest debtInvest : debtInvestList) {
			dataSafety.setId(debtInvest.userId);//更新数据防篡改字段
			falg = dataSafety.signCheck(this.actionError);
			/*
			//因为business.Bid 防篡改,查到篡改时,没有return,所以不用这段代码
			if (this.actionError.code < 0) {//dataSafety.signCheck成功返回0
				return;
			}
			*/

			/*
			//因为t_red_packages_history的数据结构暂时不支持债权转让,所以不用这段代码
			//TODO 取红包金额逻辑有误,t_red_packages_history的数据结构暂时不支持债权转让
			t_red_packages_history red = RedPackageHistory.queryRedByUserIdAndInvestId(debtInvest.userId, debtInvest.getId());
			double redMoney = 0;
			if( null != red ){
				redMoney = red.money;
				//DealDetail.minusUserFreezeFund(debtInvest.userId,debtInvest.amount.subtract(BigDecimal.valueOf(redMoney)).doubleValue(), this.actionError); // 减去用户冻结的投标资金			
				DealDetail.relieveFreezeFund(debtInvest.userId,
						debtInvest.amount.subtract(BigDecimal.valueOf(redMoney)).doubleValue(),
						this.actionError); // 返还投标用户冻结资金
			}else{
				//DealDetail.minusUserFreezeFund(debtInvest.userId, debtInvest.amount.doubleValue(), this.actionError); // 减去用户冻结的投标资金
				DealDetail.relieveFreezeFund(debtInvest.userId, debtInvest.amount.doubleValue(), this.actionError); // 返还投标用户冻结资金
			}
			*/
			
			BigDecimal redMoneyDec=debtInvest.redAmount==null?BigDecimal.ZERO:debtInvest.redAmount;
			double realFreezeAmount=debtInvest.amount.subtract(redMoneyDec).doubleValue();
			DealDetail.relieveFreezeFund(debtInvest.userId, 
					realFreezeAmount, this.actionError); // 返还投标用户冻结资金
			
			if (this.actionError.code < 0) {//DealDetail.relieveFreezeFund成功返回0
				this.actionError.setWrongMsg("解冻投资人资金－＞"+debtInvest.userId+"－＞"+this.actionError.msg);
				return;
			}
			
			//验证用户余额
			detail = DealDetail.queryUserFund(debtInvest.userId, this.actionError);
			if (this.actionError.code < 0) {//DealDetail.queryUserFund成功返回0
				this.actionError.setWrongMsg("验证用户余额－＞"+debtInvest.userId+"－＞"+this.actionError.msg);
				return;
			}
			double user_amount = detail.get("user_amount");
			double freeze = detail.get("freeze");
			double receive_amount = detail.get("receive_amount");
			
			//用户的金额是不能小于0的!  
			if(user_amount < 0 || freeze < 0){
				this.actionError.setWrongMsg("投资人资金有误:"+debtInvest.userId);
				return;
			}
			
			//添加交易记录 
			dealDetail = new DealDetail(debtInvest.userId,
					DealType.THAW_FREEZE_DEBT_INVESTAMOUNT,
					realFreezeAmount,
					debtInvest.id, user_amount, freeze, receive_amount,
					this.debtTransfer.title+"投资退款");
			dealDetail.addDealDetail(this.actionError);
			if (this.actionError.code < 0){//dealDetail.addDealDetail成功返回0
				this.actionError.setWrongMsg("增加交易记录－＞"+debtInvest.userId+"－＞"+this.actionError.msg);
				return;
			}
				
			/* 被动修改状态:只有在签名正确的情况下才能继续更新签名 */
			if(falg) {
				dataSafety.updateSignWithLock(debtInvest.userId, this.actionError);
				if (this.actionError.code < 0) {//dataSafety.updateSignWithLock成功返回0
					this.actionError.setWrongMsg("更新资金验证－＞"+debtInvest.userId+"－＞"+this.actionError.msg);
					return;
				}
			}
			
			//因为t_red_packages_history的数据结构暂时不支持债权转让,所以不用这段代码
			//TODO 返还红包金额逻辑有误,t_red_packages_history的数据结构暂时不支持债权转让
			//返回红包
			//RedPackageHistory.rollBackRedPack(debtInvest.userId, debtInvest.id);
			
			
			/* 通知 */
			String content = null;
			TemplateStation station = new TemplateStation();
			TemplateEmail email = new TemplateEmail();
			email.setId(Templets.E_TENDER_OVER);
			station.setId(Templets.M_TENDER_OVER);
			
			user.createBid = true;
			user.id = debtInvest.userId;
			
			if(station.status){
				content = station.content;
				content = content.replace("userName", user.name == null ? "" : user.name);
				content = content.replace("date", DateUtil.dateToString(this.actionDate));
				content = content.replace("title", this.debtTransfer.title);
				content = content.replace("fee",  DataUtil.formatString(realFreezeAmount));
				content = content.replace("status", this.debtTransfer.getStrStatus());
				
				StationLetter letter = new StationLetter();
				letter.senderSupervisorId = 1;
				letter.receiverUserId = debtInvest.userId;
				letter.title = station.title;
				letter.content = content;
				 
				letter.sendToUserBySupervisor(this.actionError);
				/*
				if (this.actionError.code < 0) {//letter.sendToUserBySupervisor成功返回0
					this.actionError.msg = "站内信通知－＞"+this.actionError.msg;
					return;
				}*/
			}
			
			if(email.status){
				content = email.content;
				content = content.replace("userName", user.name == null ? "" : user.name);
				content = content.replace("date", DateUtil.dateToString(this.actionDate));
				content = content.replace("title", this.debtTransfer.title);
				content = content.replace("fee",  DataUtil.formatString(realFreezeAmount));
				content = content.replace("status", this.debtTransfer.getStrStatus());
				
				email.addEmailTask(user.email, email.title, content);
			}
		}
		
		this.actionError.code = 1;
	}
	
	/**
	 * 检查债权转让是否流标(定时任务，基于自动事务)jobs.CheckDebtTransferIsFlow
	 */
	public static void checkDebtTransferIsFlow(){
		List<Long> ids = null;
		Date actionDate=new Date();
		String hql = "select id from t_debt_transfer where status in (?, ?) and ? > deadline order by id";
		
		try {
			ids = t_debt_transfer.find(hql, t_debt_transfer.Status.AUDITING.code,t_debt_transfer.Status.FUNDRAISE.code,DateUtil.dateToString(actionDate)).fetch(); 
		} catch (Exception e) {
			JPA.setRollbackOnly();
			Logger.error("债权转让->过期强制流标:" + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		if(null == ids || ids.size() == 0) return;
		JPAPlugin.closeTx(false);
		
		DebtNew debtNew = null;
		for (Long id : ids) {
			try{
				JPAPlugin.startTx(false);

				debtNew = new DebtNew(new Date());
				debtNew.getModelByPessimisticWrite(id);
				if(debtNew.actionError.code<1){
					Logger.error("债权转让->过期强制流标->"+id+"->"+debtNew.actionError.msg);
					JPA.setRollbackOnly();
					continue;
				}
				
				debtNew.forceFailingDebtTransfer("过期强制流标");
				if(debtNew.actionError.code<1){
					Logger.error("债权转让->过期强制流标->"+id+"->"+debtNew.actionError.msg);
					JPA.setRollbackOnly();
					continue;
				}
			}catch(Exception e){
				Logger.error("债权转让->过期强制流标->"+id+"->"+debtNew.actionError.msg);
				JPA.setRollbackOnly();
				continue;
			}finally{
				JPAPlugin.closeTx(false);
			}
		}
		
		JPAPlugin.startTx(false);
	}
}

