package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import constants.AuthReviewEnum;
import constants.Constants;
import constants.OperationTypeEnum;
import constants.UserTypeEnum;
import models.t_user_auth_review;
import models.t_user_bank_accounts;
import models.t_user_city;
import models.t_users;
import play.Logger;
import play.db.jpa.JPA;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.QueryUtil;

/**
 * 企业用户认证审核表
 * 
 */
public class UserAuthReview implements Serializable {
	/**
	 * @Field @serialVersionUID : TODO(这里用一句话描述这个类的作用)
	 */
	private static final long serialVersionUID = 1L;
	public Integer id;
	private Integer user_id;
	public String company_name;
	public String credit_code;
	public String bank_name;
	public String bank_no;
	public String reality_name;
	public String supervisor_name;

	public Integer status;
	public String create_time;
	public Integer create_by;
	public String update_time;
	public Integer update_by;
	public String mobile;
	public String authStatusName;
	public String update_user_name;
	public String operation;// 操作内容
	public String name;//
	public String address="";
	public BigDecimal credit_amount=new BigDecimal(0);
	public String real_name;// 个体工商户真实姓名
	public String id_number;
	public String account;// 个人用户的开户行名称
	public String sex;
	public String maritals="";// 婚姻状况
	public String bank_name_gr;// 个人开户行名称
	public String pc;// 企业注册地

	/**
	 * 查询所有企业的会员
	 * 
	 * @param currPage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static PageBean<UserAuthReview> selectCompanyUserList(String mobile, Integer authStatus, String currPageStr,
			String pageSizeStr, ErrorInfo error) {
		error.clear();

		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 22) {
			orderType = 0;
		}

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(
				" select  uar.id, uar.user_id,users.reality_name,users.mobile,uar.company_name,uar.credit_code,uar.bank_name ");
		sBuffer.append(" ,uar.bank_no,uar.status,uar.update_by,uar.update_time , su.reality_name as update_user_name ");
		sBuffer.append(" from t_user_auth_review  uar ");
		sBuffer.append(" left  join  t_users  users    on    uar.user_id=users.id  ");
		sBuffer.append(" left join t_supervisors su  on  uar.update_by=su.id   where  1=1 ");

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		if (!StringUtils.isBlank(mobile)) {
			sBuffer.append(" and  users.mobile=? ");
			params.add(mobile);
			conditionMap.put("mobile", mobile);
		}

		if (authStatus != null && -1 != authStatus) {
			sBuffer.append(" and  uar.status=? ");
			params.add(authStatus);
			conditionMap.put("status", authStatus);
		}

		sBuffer.append(" order by   uar.create_time  desc ");

		List<UserAuthReview> list = new ArrayList<UserAuthReview>();
		int count = 0;

		try {

			EntityManager em = JPA.em();
			Logger.info("sql:" + sBuffer.toString());
			Query query = em.createNativeQuery(sBuffer.toString());
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			List users = query.getResultList();
			count = QueryUtil.getQueryCountByCondition(em, sBuffer.toString(), params);

			users = query.getResultList();
			for (int i = 0; i < users.size(); i++) {
				Map<String, Object> objects = (Map<String, Object>) users.get(i);
				UserAuthReview userAuthReview = new UserAuthReview();
				userAuthReview.id = Integer.parseInt(objects.get("id").toString());
				// userAuthReview.user_id = Integer.parseInt(objects.get("user_id").toString());
				userAuthReview.reality_name = EmptyUtil.obj2Str(objects.get("reality_name"));
				userAuthReview.mobile = EmptyUtil.obj2Str(objects.get("mobile"));
				userAuthReview.company_name = EmptyUtil.obj2Str(objects.get("company_name"));
				userAuthReview.credit_code = EmptyUtil.obj2Str(objects.get("credit_code"));
				userAuthReview.bank_name = EmptyUtil.obj2Str(objects.get("bank_name"));
				userAuthReview.bank_no = EmptyUtil.obj2Str(objects.get("bank_no"));
				userAuthReview.setStatus(Integer.parseInt(objects.get("status").toString()));
				// userAuthReview.update_by =
				// Integer.parseInt(objects.get("update_by").toString());
				userAuthReview.update_time = EmptyUtil.obj2Str(objects.get("update_time"));
				userAuthReview.update_user_name = EmptyUtil.obj2Str(objects.get("update_user_name"));
				list.add(userAuthReview);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的企业会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部企业会员列表时出现异常！";

			return null;
		}

		PageBean<UserAuthReview> page = new PageBean<UserAuthReview>();
		conditionMap.put("orderType", orderType);
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = list;

		error.code = 0;

		return page;
	}

	/**
	 * 企业用户认证审核
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param id    申请记录id test
	 * @param error
	 * @author: zj
	 */
	@SuppressWarnings("unused")
	public static void saveUserBankAccountInfo(long id, int status) throws Exception {
		t_user_auth_review t_user_auth_review = getTuserAuthReview(id);
		if (AuthReviewEnum.RESET.getCode() == status || AuthReviewEnum.NOT_PASSED.getCode() == status) {// 如果是重置，或者审核不通过，则只更新申请表的状态
			updateUserAuthReview(t_user_auth_review.getId(), status);

			//审核拒绝删除user_city表中数据
			t_user_city userCity = t_user_city.find(" user_id = ? ", t_user_auth_review.user_id).first();
			if(userCity != null) {
				t_user_city.delete(" id = ?", userCity.id);
			}
		} else {
			disableOtherBankAccount(t_user_auth_review.user_id);
			t_users tUsers = User.getUserByIdUserId(t_user_auth_review.user_id);
			t_user_bank_accounts account = new t_user_bank_accounts();
			account.user_id = t_user_auth_review.user_id;
			account.time = new Date();
			account.bank_name = t_user_auth_review.bank_name;
			account.account = t_user_auth_review.bank_no;
			account.is_valid = true;
			account.verify_time = new Date();
			if (UserTypeEnum.COMPANY.getCode() == tUsers.user_type) {
				account.account_name = t_user_auth_review.company_name;
			}
			if (UserTypeEnum.INDIVIDUAL.getCode() == tUsers.user_type) {
				account.account_name = t_user_auth_review.real_name;
			}
			account.save();
			updateUserInfo(t_user_auth_review.user_id, t_user_auth_review.credit_code, t_user_auth_review.company_name);
			updateUserAuthReview(t_user_auth_review.getId(), status);
		}

	}

	/**
	 * 企业用户添加银行卡信息 之前，先将已存在的 银行账户信息改为无效禁用状态 再添加新的用户--银行卡信息记录
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @author: zj
	 */
	@SuppressWarnings("unused")
	private static void disableOtherBankAccount(long userId) throws Exception {
		String sql = " update  t_user_bank_accounts  uba    set  uba.is_valid=0 ,uba.account=concat(uba.account,'废',substr(uuid(),2,5))  where   uba.user_id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, userId);
		query.executeUpdate();
	}

	/**
	 * 将企业用户的 社会信用号更新到用户表的身份证号码字段中
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param userId
	 * @param creditCode
	 * @throws Exception
	 * @author: zj
	 */
	@SuppressWarnings("unused")
	private static void updateUserInfo(long userId, String creditCode, String company_name) throws Exception {
		try {
			String sql = " update t_users  u  set  u.id_number=? , u.reality_name=? ,u.is_bank=1 where  u.id=? ";
			Query query = JPA.em().createNativeQuery(sql).setParameter(1, creditCode).setParameter(2, company_name)
					.setParameter(3, userId);
			query.executeUpdate();
		} catch (Exception e) {
			throw new Exception("企业信用号或者身份证号码重复");
		}

	}

	/**
	 * 更新企业会员申请表状态
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param id
	 * @param status
	 * @throws Exception
	 * @author: zj
	 */
	@SuppressWarnings("unused")
	private static void updateUserAuthReview(long id, int status) throws Exception {
		Supervisor supervisor = Supervisor.currSupervisor();
		String sql = " update t_user_auth_review   uar  set  uar.status=? ,uar.update_time=? ,uar.update_by=?  where   id=? ";
		Query query = JPA.em().createNativeQuery(sql).setParameter(1, status).setParameter(2, new Date())
				.setParameter(3, supervisor.getId()).setParameter(4, id);
		query.executeUpdate();
	}

	/**
	 * 根据id获取用户申请记录
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param id
	 * @return
	 * @author: zj
	 */
	public static t_user_auth_review getTuserAuthReview(long id) {
		t_user_auth_review auth_review = null;
		try {
			auth_review = t_user_auth_review.findById(id);
			return auth_review;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return auth_review;

	}

	/**
	 * 根据用户id获取申请表里的公司名称
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param id
	 * @return
	 * @author: zj
	 */
	public static String getCompanyNameUserId(long userId) {
		String companyName = null;
		try {
			companyName = t_user_auth_review
					.find("select company_name from t_user_auth_review  where user_id=? and status=2 ", userId).first();
			return companyName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return companyName;

	}

	/**
	 * 查询所有企业的会员
	 * 
	 * @param currPage
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static PageBean<UserAuthReview> selectCompanyUserListView(String mobile, Integer authStatus,
			String currPageStr, String pageSizeStr, ErrorInfo error) {
		error.clear();

		int orderType = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;

		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}

		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}

		if (orderType < 0 || orderType > 22) {
			orderType = 0;
		}

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(
				" select  users.id, uar.user_id,users.name,users.reality_name,users.mobile,uar.company_name,uar.credit_code,uar.bank_name ");
		sBuffer.append(" ,uar.bank_no,uar.status,uar.update_by,uar.update_time , su.reality_name as update_user_name , su.name as supervisor_name ");
		sBuffer.append(" from t_users users    ");
		sBuffer.append(" left  join  t_user_auth_review  uar    on    uar.user_id=users.id  ");
		sBuffer.append(" left join t_supervisors su  on  uar.update_by=su.id   where  users.user_type in (2,3) ");

		List<Object> params = new ArrayList<Object>();
		Map<String, Object> conditionMap = new HashMap<String, Object>();

		if (!StringUtils.isBlank(mobile)) {
			sBuffer.append(" and  users.mobile=? ");
			params.add(mobile);
			conditionMap.put("mobile", mobile);
		}

		if (authStatus != null && -1 != authStatus) {
			sBuffer.append(" and  uar.status=? ");
			params.add(authStatus);
			conditionMap.put("status", authStatus);
		}

		sBuffer.append(" order by   uar.create_time  desc ");

		List<UserAuthReview> list = new ArrayList<UserAuthReview>();
		int count = 0;

		try {

			EntityManager em = JPA.em();
			Logger.info("sql:" + sBuffer.toString());
			Query query = em.createNativeQuery(sBuffer.toString());
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			for (int n = 1; n <= params.size(); n++) {
				query.setParameter(n, params.get(n - 1));
			}
			query.setFirstResult((currPage - 1) * pageSize);
			query.setMaxResults(pageSize);
			List users = query.getResultList();
			count = QueryUtil.getQueryCountByCondition(em, sBuffer.toString(), params);

			users = query.getResultList();
			for (int i = 0; i < users.size(); i++) {
				Map<String, Object> objects = (Map<String, Object>) users.get(i);
				UserAuthReview userAuthReview = new UserAuthReview();
				userAuthReview.id = Integer.parseInt(objects.get("id").toString());
				// userAuthReview.user_id = Integer.parseInt(objects.get("user_id").toString());
				userAuthReview.reality_name = EmptyUtil.obj2Str(objects.get("reality_name"));
				userAuthReview.mobile = EmptyUtil.obj2Str(objects.get("mobile"));
				userAuthReview.company_name = EmptyUtil.obj2Str(objects.get("company_name"));
				userAuthReview.credit_code = EmptyUtil.obj2Str(objects.get("credit_code"));
				userAuthReview.bank_name = EmptyUtil.obj2Str(objects.get("bank_name"));
				userAuthReview.bank_no = EmptyUtil.obj2Str(objects.get("bank_no"));
				userAuthReview.supervisor_name = EmptyUtil.obj2Str(objects.get("supervisor_name"));

				if (objects.get("status") == null) {
					userAuthReview.setStatus(-1);
				} else {
					userAuthReview.setStatus(EmptyUtil.obj20(objects.get("status")).intValue());
				}

				// userAuthReview.update_by =
				// Integer.parseInt(objects.get("update_by").toString());
				userAuthReview.update_time = EmptyUtil.obj2Str(objects.get("update_time"));
				userAuthReview.update_user_name = EmptyUtil.obj2Str(objects.get("update_user_name"));
				userAuthReview.name = EmptyUtil.obj2Str(objects.get("name"));
				list.add(userAuthReview);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("查询所有的企业会员时：" + e.getMessage());
			error.code = -1;
			error.msg = "查询全部企业会员列表时出现异常！";

			return null;
		}

		PageBean<UserAuthReview> page = new PageBean<UserAuthReview>();
		conditionMap.put("orderType", orderType);
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;
		page.page = list;

		error.code = 0;

		return page;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public String getCompany_name() {
		return company_name;
	}

	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	public String getCredit_code() {
		return credit_code;
	}

	public void setCredit_code(String credit_code) {
		this.credit_code = credit_code;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_no() {
		return bank_no;
	}

	public void setBank_no(String bank_no) {
		this.bank_no = bank_no;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		if (AuthReviewEnum.UNREVIEW.getCode() == status) {
			this.authStatusName = AuthReviewEnum.UNREVIEW.getName();
		}
		if (AuthReviewEnum.NOT_PASSED.getCode() == status) {
			this.authStatusName = AuthReviewEnum.NOT_PASSED.getName();
			this.operation = OperationTypeEnum.NOT_PASSED.getName();
		}
		if (AuthReviewEnum.PASSED.getCode() == status) {
			this.authStatusName = AuthReviewEnum.PASSED.getName();
			this.operation = OperationTypeEnum.PASSED.getName();
		}
		if (AuthReviewEnum.RESET.getCode() == status) {
			this.authStatusName = AuthReviewEnum.RESET.getName();
			this.operation = OperationTypeEnum.RESET.getName();
		}
		this.status = status;
	}

	public Integer getCreate_by() {
		return create_by;
	}

	public void setCreate_by(Integer create_by) {
		this.create_by = create_by;
	}

	public Integer getUpdate_by() {
		return update_by;
	}

	public void setUpdate_by(Integer update_by) {
		this.update_by = update_by;
	}

	public String getReality_name() {
		return reality_name;
	}

	public void setReality_name(String reality_name) {
		this.reality_name = reality_name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getAuthStatusName() {
		return authStatusName;
	}

	public void setAuthStatusName(String authStatusName) {
		this.authStatusName = authStatusName;
	}

	public String getSupervisor_name() {
		return supervisor_name;
	}

	public void setSupervisor_name(String supervisor_name) {
		this.supervisor_name = supervisor_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUpdate_user_name() {
		return update_user_name;
	}

	public void setUpdate_user_name(String update_user_name) {
		this.update_user_name = update_user_name;
	}

	public String getOperation() {

		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Override
	public String toString() {
		return "UserAuthReview [id=" + id + ", user_id=" + user_id + ", company_name=" + company_name + ", credit_code="
				+ credit_code + ", bank_name=" + bank_name + ", bank_no=" + bank_no + ", status=" + status
				+ ", create_time=" + create_time + ", create_by=" + create_by + ", update_time=" + update_time
				+ ", update_by=" + update_by + "]";
	}

	/**
	 * 通过借款申请表id 得到该用户的基本信息
	 *
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param id
	 * @return
	 * @author: zj
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static UserAuthReview getUserBaseInfo(Long id) {
		String sql = "";
		sql = " select   b.address,c.real_name,c.company_name,c.bank_no,c.credit_code,  (d.province+d.city) as  pc, c.bank_name, f.name as maritals, "
				+ "						b.credit_amount,b.reality_name,b.id_number, e.account ,case  b.sex when 1 then  '男' when 2 then '女'  else '未知' end as sex  ,e.mobile,e.bank_name as  bank_name_gr "
				+ " from  t_borrow_apply   a   left join   t_users b on  a.user_id  =b.id "
				+ " left join   t_user_auth_review   c  on   b.id=c.user_id "
				+ " left join   t_user_city  d   on   b.id=d.user_id "
				+ " left join   (  select    y.account,y.user_id ,y.mobile,y.bank_name from  t_user_bank_accounts y left  join  ( "
				+ "									select  min(z.time) as time , max(z.user_id) as user_id from t_user_bank_accounts z group  by z.user_id   ) x    "
				+ "									on y.user_Id=x.user_id and y.time=x.time  )  e    on   a.user_id=e.user_id "
				+ " left join   t_dict_maritals  f  on  b.marital_id=f.id " + " where  ( c.status=2 or  b.reality_name is not null ) and a.id=? ";

		Logger.info("通过借款申请表id 得到该用户的基本信息 ： " + sql);
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, id);
		List userBaseInfo = query.getResultList();
		if (userBaseInfo == null || userBaseInfo.size() <= 0) {
			return null;
		} else {
			Map<String, Object> objects = (Map<String, Object>) userBaseInfo.get(0);
			UserAuthReview userAuthReview = new UserAuthReview();
			userAuthReview.company_name = EmptyUtil.obj2Str(objects.get("company_name"));
			userAuthReview.setBank_no(EmptyUtil.obj2Str(objects.get("bank_no")));
			userAuthReview.setCredit_code(EmptyUtil.obj2Str(objects.get("credit_code")));
			userAuthReview.setAddress(EmptyUtil.obj2Str(objects.get("pc")));// 企业注册地 省市
			userAuthReview.setBank_name(EmptyUtil.obj2Str(objects.get("bank_name")));
			userAuthReview.setCredit_amount(EmptyUtil.obj20(objects.get("credit_amount")));
			userAuthReview.setReal_name(EmptyUtil.obj2Str(objects.get("real_name")));
			userAuthReview.setReality_name(EmptyUtil.obj2Str(objects.get("reality_name")));
			userAuthReview.setId_number(EmptyUtil.obj2Str(objects.get("id_number")));
			userAuthReview.setAccount(EmptyUtil.obj2Str(objects.get("account")));
			userAuthReview.setSex(EmptyUtil.obj2Str(objects.get("sex")));
			userAuthReview.setMaritals(EmptyUtil.obj2Str(objects.get("maritals")));//婚姻状况
			userAuthReview.setAddress(EmptyUtil.obj2Str(objects.get("address")));
			userAuthReview.setBank_name_gr(EmptyUtil.obj2Str(objects.get("bank_name_gr")));
			return userAuthReview;
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public BigDecimal getCredit_amount() {
		return credit_amount;
	}

	public void setCredit_amount(BigDecimal credit_amount) {
		this.credit_amount = credit_amount;
	}

	public String getReal_name() {
		return real_name;
	}

	public void setReal_name(String real_name) {
		this.real_name = real_name;
	}

	public String getId_number() {
		return id_number;
	}

	public void setId_number(String id_number) {
		this.id_number = id_number;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getMaritals() {
		return maritals;
	}

	public void setMaritals(String maritals) {
		if(maritals==null) {
			this.maritals="";
		}
		this.maritals = maritals;
	}

	public String getBank_name_gr() {
		return bank_name_gr;
	}

	public void setBank_name_gr(String bank_name_gr) {
		this.bank_name_gr = bank_name_gr;
	}

	public String getPc() {
		return pc;
	}

	public void setPc(String pc) {
		this.pc = pc;
	}

}