package business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Query;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import constants.OptionKeys;
import constants.SupervisorEvent;
import play.Logger;
import play.db.jpa.JPA;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import models.t_dict_audit_items;
import models.t_dict_audit_items_log;

/**
 * 审计项目
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-23 下午04:57:35
 */
public class AuditItem implements Serializable{
	
	public long id; // 审核资料id
	private long _id;
	public String mark; //唯一标示
	private String _mark;
	public boolean lazy; 
	public boolean getPai;
	public String no;// 科目编号
	public String name; // 资料名称
	public int type; // 审核材料类型
	public int period; // 有效期
	public String description;// 要求描述
	public int creditScore;// 积分分值
	public int auditCycle;// 审核周期
	public double auditFee;// 审核费用
	public boolean isUse;// 是否使用
	public double passRate;// 通过率
	public Date time; // 时间

	/**
	 * 获取ID
	 */
	public long getId() {
		return this._id;
	}
	
	/**
	 * 获取mark
	 */
	public String getMark() {
		return this._mark;
	}
	
	/**
	 * 获取编号
	 */
	public String getNo() {
		if(null == no) {
			String _no = OptionKeys.getvalue(OptionKeys.AUDIT_ITEM_NUMBER, new ErrorInfo());
			this.no = _no == null ? "ZL" + this.id : _no + this.id;
		}
		
		return this.no;
	}

	/**
	 * 获取mark
	 */
	public static String queryMark(long id){
		try {
			return t_dict_audit_items.find("select mark from t_dict_audit_items where id = ? ", id).first();
		} catch (Exception e) {
			Logger.error("资料->获取mark:" + e.getMessage());
			
			return null;
		}
	}
	
	/**
	 * 填充自己
	 */
	public void setId(long id) {
		t_dict_audit_items item = null;
		
		try {
			item = t_dict_audit_items.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->填充自己" + e.getMessage());
			this._id = -1;
			
			return;
		}

		if (null == item) {
			this._id = -1;
			
			return;
		}
		
		this._id = id;
		
		/* 查看产品对应的资料 */
		if(this.getPai){
			this.name = item.name; 
			
			return;
		}
		
		this.name = item.name; // 资料名称
		this.type = item.type; // 审核材料类型
		this.period = item.period; // 有效期
		this.description = item.description;// 要求描述
		this.creditScore = item.credit_score;// 积分分值
		this.auditCycle = item.audit_cycle;// 审核周期
		this.auditFee = item.audit_fee;// 审核费用
		this.isUse = item.is_use;// 是否使用
		//this.passRate = log.pass_rate;// 通过率
		this._mark = item.mark;
		this.time = item.time;
	}

	/**
	 * 填充log表
	 */
	public void setMark(String mark) {
		t_dict_audit_items_log item = null;
		
		try {
			item = t_dict_audit_items_log.find("mark = ?", mark).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->填充log表" + e.getMessage());
			this._id = -1;
			
			return;
		}

		if (null == item) {
			this._id = -1;
			
			return;
		}
		
		this._id = item.id;
		this._mark = item.mark;
		
		if(this.lazy) {
			this.name = item.name; // 资料名称
			this.period = item.period; // 有效期
			this.creditScore = item.credit_score;// 积分分值
			this.auditFee = item.audit_fee;
			this.type = item.type;
			
			return;
		}
		
		this.name = item.name; // 资料名称
		this.period = item.period; // 有效期
		this.creditScore = item.credit_score;// 积分分值
		this.auditFee = item.audit_fee;
		this.auditCycle = item.audit_cycle;// 审核周期
		this.description = item.description;// 要求描述
		this.type = item.type; // 审核材料类型
		this.isUse = item.is_use;// 是否使用
		//this.passRate = log.pass_rate;// 通过率
	}

	/**
	 * 查询全部的审计资料列表
	 * @param currPage 当前页
	 * @param pageSize 每页条数
	 * @param name 名称
	 * @param flag 是否启用
	 * @param error 信息值
	 */
	public static PageBean<AuditItem> queryAuditItems(String currPage, String pageSize, String keyword, boolean flag, ErrorInfo error) {
		error.clear();

		PageBean<AuditItem> pageBean = new PageBean<AuditItem>();
		pageBean.currPage = NumberUtil.isNumericInt(currPage)? Integer.parseInt(currPage): 1;
		pageBean.pageSize = NumberUtil.isNumericInt(pageSize)? Integer.parseInt(pageSize): 10;
		
		int count = -1;
		StringBuffer conditions = new StringBuffer(" 1 = 1"); 
		List<Object> values = new ArrayList<Object>(); 
		Map<String, Object> mapconditions = new HashMap<String, Object>();
		
		/* 是否名称搜索 */
		if(StringUtils.isNotBlank(keyword)){
			if(NumberUtil.isNumeric(keyword)) {
				conditions.append(" AND (name LIKE ? or id LIKE ?)");
				values.add("%" + keyword + "%");
				values.add(Long.parseLong(keyword));
			}else{
				conditions.append(" AND name LIKE ?");
				values.add("%" + keyword + "%");
			}
			
			mapconditions.put("keyword", keyword);
		}

		/* 是否启用 */
		if(flag){
			conditions.append(" AND is_use = ?");
			values.add(flag);
		}
		
		pageBean.conditions = mapconditions;
		
		try {
			/* 得到总记录数 */
			count = (int) t_dict_audit_items.count(conditions.toString(), values.toArray());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->产查询全部的审计资料列表,查询总记录数:" + e.getMessage());
			error.code = -1;
			error.msg = error.FRIEND_INFO + "产品列表加载失败!";

			return null;
		}

		if (count < 0)
			return pageBean;
		
		pageBean.totalCount = count;
		List<t_dict_audit_items> tauditItems = null;
		
		try {
			tauditItems = t_dict_audit_items.find(conditions.toString(), values.toArray()).fetch(pageBean.currPage, pageBean.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询全部的审计资料列表:" + e.getMessage());
			error.code = -2;
			error.msg = error.FRIEND_INFO + "查询审计资料列表有误!";
			
			return null;
		}

		if(0 == tauditItems.size()) 
			return pageBean;
		
		pageBean.page = AuditItem.queryAuditItems(tauditItems);
		
		error.code = 0;
		
		return pageBean;
	}
	
	/**
	 * 查询有效的审计资料列表
	 * @param error 信息值
	 * @return List<AuditItem>
	 */
	public static List<AuditItem> queryEnableAuditItems(ErrorInfo error) {
		error.clear();

		List<t_dict_audit_items> tauditItems = null;

		try {
			tauditItems = t_dict_audit_items.find("is_use", Constants.ENABLE).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询有效的审计资料列表:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "查询审计资料列表有误!";
			
			return null;
		}

		if(null == tauditItems) {
			return new ArrayList<AuditItem>();
		}
		
		error.code = 0;
		
		return AuditItem.queryAuditItems(tauditItems);
	}
	
	/**
	 * 查询有效的审计资料列表
	 * @param error 信息值
	 * @return List<AuditItem>
	 */
	public static PageBean<t_dict_audit_items> queryEnableAuditItems(String key, int currPage, int pageSize, ErrorInfo error) {
		error.clear();

		if(currPage == 0) {
		   currPage = 1;
		}
		
		PageBean<t_dict_audit_items> page = new PageBean<t_dict_audit_items>();
		page.currPage = currPage;
		page.pageSize = pageSize;
		
		if(pageSize == 0){
			page.pageSize = Constants.FIVE;
		}
		
		Map<String,Object> conditionMap = new HashMap<String, Object>();
		conditionMap.put("key", key);
		
		StringBuffer conditions = new StringBuffer("1=1 ");
		List<Object> values = new ArrayList<Object>();
		
		if(!StringUtils.isBlank(key)) {
			conditions.append("and name like ?");
			values.add("%"+key+"%");
		}
		
		List<t_dict_audit_items> tauditItems = null;

		try {
			page.totalCount =  (int) t_dict_audit_items.count(conditions.toString(), values.toArray());
			tauditItems = t_dict_audit_items.find(conditions.toString(), values.toArray()).fetch(page.currPage, page.pageSize);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询有效的审计资料列表:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "查询审计资料列表有误!";
			
			return null;
		}

		error.code = 0;
		
		page.page = tauditItems;
		page.conditions = conditionMap;
		
		return page;
		
	}

	/**
	 * 读取数据对象,填充自己
	 */
	private static List<AuditItem> queryAuditItems(List<t_dict_audit_items> tauditItems) {
		List<AuditItem> auditItems = new ArrayList<AuditItem>();
		AuditItem auditItem = null;

		for (t_dict_audit_items item : tauditItems) {
			auditItem = new AuditItem();

			auditItem._id = item.id;
			auditItem.name = item.name;
			auditItem.time = item.time;
			auditItem.type = item.type;
			auditItem.period = item.period;
			auditItem.description = item.description;
			auditItem.creditScore = item.credit_score;
			auditItem.auditCycle = item.audit_cycle;
			if(constants.Constants.IPS_ENABLE){
				auditItem.auditFee = 0.00;
			}else{				
				auditItem.auditFee = item.audit_fee;
			}
			auditItem.isUse = item.is_use;

			auditItems.add(auditItem);
		}

		return auditItems;
	}
	
	/**
	 * 查询ID和name集合
	 * @param error 信息值
	 * @return List<AuditItem>
	 */
	public static List<AuditItem> queryAuditItems(ErrorInfo error){
		error.clear();
		
		List<t_dict_audit_items> tauditItems = null;
		List<AuditItem> auditItems = new ArrayList<AuditItem>();
		String hql = "select new t_dict_audit_items(id, name, credit_score, period) from t_dict_audit_items where is_use = ?";
		
		try {
			tauditItems = t_dict_audit_items.find(hql, Constants.ENABLE).fetch();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->查询有效的审计资料列表:" + e.getMessage());
			error.msg = error.FRIEND_INFO + "查询审计资料列表有误!";
			
			return null;
		}
		
		if(null == tauditItems)
			return auditItems;
			
		AuditItem auditItem = null;

		for (t_dict_audit_items item : tauditItems) {
			auditItem = new AuditItem();
			
			auditItem._id = item.id;
			auditItem.name = item.name;
			auditItem.creditScore = item.credit_score;
			auditItem.period = item.period;
			
			auditItems.add(auditItem);
		}
		
		error.code = 0;
		
		return auditItems;
	}
	
	/**
	 * 检查用户是否存在
	 * @param name 名称
	 * @return true : 存在,false : 不存在
	 */
	public static boolean checkName(String name, long id){
		String hql = "select name from t_dict_audit_items where name = ?";
		
		if(id > 0) hql += " and id <> " + id;

		try {
			name = t_dict_audit_items.find(hql, name.trim()).first();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->根据name查询name:" + e.getMessage());
			
			return true;
		}
		
		if(null == name) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 查询使用中的审核资料数量
	 */
	public static long auditItemCount() {
		try{
			return t_dict_audit_items.count("is_use = ?", Constants.ENABLE);
		}catch(Exception e) {
			e.printStackTrace();
			Logger.info("资料->查询审核资料数目时："+e.getMessage());
			
			return -1;
		}
	}
	
	/**
	 * 添加
	 */
	public void create(ErrorInfo error) {
		error.clear();
		
		t_dict_audit_items auditItem = new t_dict_audit_items();
		auditItem.time = new Date(); // 创建时间
		auditItem.is_use = Constants.ENABLE; // 默认为可以使用
		
		error.code = addOrEdit(auditItem);
		
		if(error.code < 0){
			error.code = -1;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 添加log表 */
		long logId = this.createAuditItemLog(auditItem);
		
		if(logId < 1){
			error.code = -2;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.CREATE_AUDIT_ITEM, "添加审计科目", error);
		
		if(error.code < 0){
			error.code = -3;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 0;
	}

	/**
	 * 编辑
	 * @param id ID
	 * @param info 信息值
	 */
	public void edit(long id, ErrorInfo error) {
		error.clear();
		
		t_dict_audit_items auditItem = null;

		try {
			auditItem = t_dict_audit_items.findById(id);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->编辑审计资料时,根据主键查询 出现异常!" + e.getMessage());
			error.code = -1;
			
			return;
		}
		
		error.code = addOrEdit(auditItem);
		
		if(error.code < 0){
			error.code = -3;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 添加log表 */
		long logId = this.createAuditItemLog(auditItem);
		
		if(logId < 1){
			error.code = -4;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		/* 添加事件 */
		DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.EDIT_AUDIT_ITEM, "编辑审计科目", error);
		
		if(error.code < 0){
			error.code = -4;
			error.msg = "保存失败!";
			JPA.setRollbackOnly();
			
			return;
		}
		
		error.code = 0;
	}

	/**
	 * 添加/编辑
	 * @param auditItem t_dict_audit_items 对象
	 * @param info 信息值
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	private int addOrEdit(t_dict_audit_items auditItem) {
		
		if(null == auditItem) {
			return -1;
		}
		
		auditItem.name = this.name; // 资料名称
		auditItem.type = this.type; // 审核材料类型
		auditItem.period = this.period; // 有效期
		auditItem.description = this.description;// 要求描述
		auditItem.credit_score = this.creditScore;// 积分分值
		auditItem.audit_cycle = this.auditCycle;// 审核周期
		
		if(constants.Constants.IPS_ENABLE){
			auditItem.audit_fee = 0.00;// 审核费用
		}else{
			
			auditItem.audit_fee = this.auditFee;// 审核费用
		}
		auditItem.mark = UUID.randomUUID().toString();

		try {
			auditItem.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->添加/编辑 出现异常!" + e.getMessage());
			
			return -2;
		}

		
		return auditItem.id < 1 ? -3 : 1;
	}
	
	/**
	 * 添加备份表
	 */
	private long createAuditItemLog(t_dict_audit_items auditItem){
		t_dict_audit_items_log log = new t_dict_audit_items_log();
		
		log.name = auditItem.name;
		log.time = auditItem.time;
		log.type = auditItem.type;
		log.period = auditItem.period;
		log.description = auditItem.description;
		log.credit_score = auditItem.credit_score;
		log.audit_cycle = auditItem.audit_cycle;
		log.audit_fee = auditItem.audit_fee;
		log.mark = auditItem.mark;
		//log.pass_rate = auditItem.pass_rate;
		
		try {
			log.save();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料-> 添加备份标:" + e.getMessage());
			
			return -1;
		}
		
		return log.id;
	}
	
	/**
	 * 暂停/启用
	 * @param id ID
	 * @param isUse 状态值
	 * @param info 错误信息
	 * @return ? > 0 : success; ? < 0 : fail
	 */
	public static void editStatus(long id, boolean isUse, ErrorInfo error) {
		error.clear();
		
		String hql = "update t_dict_audit_items set is_use=? where id=?";
		
		Query query = JPA.em().createQuery(hql);
		query.setParameter(1, isUse);
		query.setParameter(2, id);
		
		int rows = 0;
		
		try {
			rows = query.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("资料->暂停/启用审计资料 出现异常!" + e.getMessage());
			error.msg = "设置失败!";
			
			return;
		}
		
		if(rows == 0){
			JPA.setRollbackOnly();
			error.code = -1;
			error.msg = "设置失败!";
			
			return;
		}
		
		/* 添加事件 */
		if(isUse)
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.ENABLE_AUDIT_ITEM, "启用审计科目", error);
		else
			DealDetail.supervisorEvent(Supervisor.currSupervisor().id, SupervisorEvent.NOT_ENABLE_AUDIT_ITEM, "暂停审计科目", error);
		
		if(error.code < 0){
			JPA.setRollbackOnly();
			error.msg = "设置失败!";
			
			return;
		}
		
		error.code = 0;
	}
	
	/**
	 * 根据ID 查询费用
	 * @param itemId
	 * @param error
	 * @return
	 */
	public static double queryItemFeeById(long itemId, ErrorInfo error) {
		error.code = -1;
		
		String hql = "select audit_fee from t_dict_audit_items where id = ?";
		Double fee = null;
		
		try {
			fee = t_dict_audit_items.find(hql, itemId).first();
		} catch (Exception e) {
			error.code = -4;
			error.msg = "查询失败!";
			Logger.error("资料-> 根据ID 查询费用" + e.getMessage());
			
			return -1;
		}
		
		return fee == null ? -1 : fee;
	}
}
