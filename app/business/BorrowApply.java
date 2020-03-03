package business;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.jsoup.helper.StringUtil;

import constants.Constants;
import constants.ProductEnum;
import constants.SQLTempletes;
import models.t_agencies;
import models.t_bids;
import models.t_borrow_apply;
import models.t_enum_map;
import models.t_user_bank_accounts;
import models.v_borrow_apply;
import models.core.t_new_product;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Scope.Params;
import utils.EmptyUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;

public class BorrowApply {
	public static void addBorrowApply(Long userId, int user_type_id,Long agency_id,
									  Long purpose_id, double apply_amount, int period, long product_id) {
		Date date = new Date();

		t_borrow_apply apply = new t_borrow_apply();
		apply.apply_amount = new BigDecimal(apply_amount);
		apply.user_id = userId;
		apply.apply_time = date;
		apply.period = period;
		apply.loan_property_id = user_type_id;
		apply.product_id = product_id;
		apply.loan_purpose_id= purpose_id;
		apply.apply_area =agency_id;
		apply.status = 1;//初始化状态
		apply.save();

		String borrowNo = Constants.BORROW_NO_PERFIX + apply.id;
		/*
		if(product_id == ProductEnum.FANG.getCode()) {
			borrowNo=  borrowNo + Constants.BORROW_NO_SUFFIX_FANG;
		}
		if(product_id == ProductEnum.XIN.getCode()){
			borrowNo=  borrowNo + Constants.BORROW_NO_SUFFIX_XIN;
		}
		if(product_id == ProductEnum.NONG.getCode()){
			borrowNo=  borrowNo + Constants.BORROW_NO_SUFFIX_NONG;
		}
		*/
		t_new_product product=new t_new_product().getEnumById(product_id);
		if(product!=null && !StringUtil.isBlank(product.borrow_no_suffix)) {
			borrowNo=  borrowNo + product.borrow_no_suffix;
		}

		apply.borrow_no = borrowNo;
		apply.save();

		LogCore.create(2, apply.id, 1, apply.user_id, apply.status, apply.apply_time, "借款审核中", "您的借款急速审核中，1至2个工作日，我们的工作人员给您取得联系，请保持电话畅通");
	}

	/**
	 * 借款人信息
	 * @param params
	 * @return
	 * @throws Exception 
	 */
	public static  PageBean<Map<String, Object>> borrowerList(Params params) throws Exception {

		String currPageStr=params.get("currPage");
		String pageSizeStr=params.get("pageSize");

		String idCard = params.get("idCard");
		String mobile = params.get("mobile");
		String peerNum = params.get("peerNum");
		
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("id_number");
		String credit_start_date=params.get("credit_start_date");
		String credit_end_date=params.get("credit_end_date");
		
		
		int currPage = 1;
		int pageSize = 10;
		if(!StringUtil.isBlank(currPageStr) && NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr) && NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr)>0?Integer.parseInt(pageSizeStr):pageSize;
		}
		String columns = "	apply.id as id,	" + 
					"	users.name as user_name," + 
					"	users.reality_name as reality_name," + 
					"	users.id_number as id_number," + 
					"	users.mobile as mobile," + 
					"	users.time as registe_time," + 
					"	apply.apply_time as apply_time," + 
					"	bid_time as bid_time," + 
					"	ifnull(orgUserInfo.brand_name, orgUser.reality_name) as org_name," + 
					"	score.value as tongdun_score," + 
					"	creditApply.machine_score as machine_score," + 
					"	creditApply.audit_credit_amount as audit_credit_amount," + 
					"	ifnull(borrow_a.use_amount,0)-ifnull(borrow_a.real_repayment_corpus,0) as use_credit, " + 
					"	users.last_login_time as last_login_time ," +
					"	hasnot_repayment_corpus as hasnot_repayment_corpus";
		
		String table= " from" + 
				"	t_borrow_apply apply" + 
				"	inner join t_users users ON users.id = apply.user_id" + 
				"	inner join t_credit_apply creditApply on creditApply.id = apply.credit_apply_id" + 
				"	inner join t_organization org on org.user_id = apply.consociation_user_id" + 
				"	inner join t_users orgUser on org.user_id = orgUser.id" + 
				"	inner join t_users_info orgUserInfo on users.id = orgUserInfo.user_id" + 
				"	inner join t_apply_org_project applyOrgProject on applyOrgProject.apply_id = creditApply.id" + 
				"	inner join t_risk_manage_score score on score.credit_apply_id = creditApply.id and score.type_id = 1" + 
				"	left join (  " + 
				"					select" + 
				"					borrow_a.credit_apply_id," + 
				"				sum(case when borrow_a.status in(1,2,3,4,5) then ifnull(borrow_a.approve_amount,ifnull(borrow_a.apply_amount,0))" + 
				"						when borrow_a.status=6 then ifnull(bid_bill.bid_amount,0) else 0 end ) as use_amount, " + 
				"					sum(ifnull(bid_bill.real_repayment_corpus, 0 )) as real_repayment_corpus," + 
				"					ifnull((bid_amount - sum( ifnull( bid_bill.real_repayment_corpus, 0 ) )),0) AS hasnot_repayment_corpus," + 
				"					bid_time as bid_time " +
				"					from t_borrow_apply borrow_a" + 
				"					left join (" + 
				"						select bid.borrow_apply_id,sum(bid.amount) bid_amount,sum(ifnull( bill.real_repayment_corpus, 0 )) as  real_repayment_corpus,bid.time as bid_time " + 
				"						from  t_bids bid " + 
				"						left JOIN ( SELECT bid_id, sum( real_repayment_corpus ) as real_repayment_corpus FROM t_bills WHERE STATUS IN ( -2, -3, 0 ) GROUP BY bid_id ) bill ON bill.bid_id = bid.id" + 
				"						where bid.borrow_apply_id is not null" + 
				"						and bid.status>=0" + 
				"						group by bid.borrow_apply_id" + 
				"					)bid_bill on bid_bill.borrow_apply_id= borrow_a.id" + 
				"					where true" + 
				"					and borrow_a.status in(1,2,3,4,5,6)" + 
				"					and borrow_a.credit_apply_id is not null" + 
				"					group by borrow_a.credit_apply_id" + 
				"				 )borrow_a on borrow_a.credit_apply_id=creditApply.id where true ";
		
		
		List<Object> paramsList = new ArrayList<Object>();
		String condition="";
		 
		if(!StringUtil.isBlank(reality_name)){
			condition+=" and users.reality_name like ? ";
			paramsList.add("%" + reality_name +"%");
		}
		if(!StringUtil.isBlank(user_idnumber)){
			condition+=" and users.id_number like ? ";
			paramsList.add("%" + user_idnumber +"%");
		}
		if(!StringUtil.isBlank(mobile)){
			condition+=" and users.mobile like ? ";
			paramsList.add("%" + mobile +"%");
		}
		if(!StringUtil.isBlank(credit_start_date) ){
			try {
				Date starDate=new SimpleDateFormat("yyyy-MM-dd").parse(credit_start_date);
				condition+=" and creditApply.apply_time >= ? ";
				paramsList.add(starDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!StringUtil.isBlank(credit_end_date) ){
			try {
				Date endDate=new SimpleDateFormat("yyyy-MM-dd").parse(credit_end_date);
				condition+=" and creditApply.apply_time < ? ";
				paramsList.add(endDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		//=======================================order_by=======================================
		String order_by=" and apply.product_id = 4  GROUP BY users.id  order by apply.id desc ";
//		System.err.println("亿美贷借款人信息列表: " + columns+table+condition+order_by);
		return PageBeanForPlayJPA.getPageBeanMapBySQL(columns,table+condition,order_by,currPage,pageSize,paramsList.toArray());
	}


	/**
	 * 查询用户的所有借款账单
	 */
	public static PageBean<v_borrow_apply> queryMyBorrowApply(long userId,int currPageStr, int pageSizeNum, 
															  ErrorInfo error) {
		error.clear();

		int count = 0;
		int currPage = Constants.ONE;
		int pageSize = Constants.FIVE;

		if (pageSizeNum != 0) {
			pageSize = pageSizeNum;
		}

		Map<String, Object> conditionMap = new HashMap<String, Object>();
		List<v_borrow_apply> apply_result = new ArrayList<v_borrow_apply>();
		List<Object> values = new ArrayList<Object>();
		if (currPageStr != 0) {
			currPage = currPageStr;
		}


		StringBuffer sql = new StringBuffer();
		sql.append(SQLTempletes.V_FINANCE_APPLY_COUNT);
		/*String productSqlCount = "";
		if(productType == 1) { //产品类型为1, 信亿贷，农亿贷，房亿贷
			productSqlCount = " and product_id in (1,2,3) ";
			
		} else if(productType == 4){ //产品类型为4,亿美贷
			productSqlCount = " and product_id = 4 ";
			 
		}*/
		//sql.append(productSqlCount);
		values.add(userId);
	 
		EntityManager em = JPA.em();
		Query query = em.createNativeQuery(sql.toString());

		for(int n = 1; n <= values.size(); n++){
			query.setParameter(n, values.get(n-1));
		}

		List<?> list = null;

		try {
			list = query.getResultList();
		} catch (Exception e) {
			Logger.error("查询我的借款列表总数出错:" + e.getMessage());
			error.msg = "查询我的借款列表总数失败!";
		}

		count = list == null ? 0 : Integer.parseInt(list.get(0).toString());

		if(count >0) {
			sql = new StringBuffer();
			sql.append(SQLTempletes.V_FINANCE_APPLY_LIST);
			//String productSql = "";
			/*if(productType == 1) { //产品类型为1, 信亿贷，农亿贷，房亿贷
				productSql = " and a.product_id in (1,2,3) ";
				
				
			} else if(productType == 4){ //产品类型为4,亿美贷
				productSql = " and a.product_id = 4 ";
				 
			}*/
			//sql.append(productSql);
			
			String order = " ORDER BY a.id desc,b.id desc ";
			sql.append(order);
			Logger.info("查询借款申请列表sql: " + sql.toString());
			values.add((currPage - 1) * pageSize);
			values.add(currPage * pageSize);

			query = em.createNativeQuery(sql.toString());
			query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

			for (int n = 1; n <= values.size(); n++) {
				query.setParameter(n, values.get(n - 1));
			}

			try {
				List result = query.getResultList();

				for (Object object  : result) {
					Map obj = (Map) object ;
					v_borrow_apply apply=new v_borrow_apply();

					apply.id = Long.parseLong(obj.get("id").toString());
					apply.borrow_no = obj.get("borrow_no").toString();
					apply.user_id = Long.parseLong(obj.get("user_id").toString());
					apply.product_name = obj.get("product_name")==null?"":obj.get("product_name").toString();
					apply.loan_property_name = obj.get("loan_property_name")==null?"":obj.get("loan_property_name").toString();
					apply.loan_purpose_name = obj.get("loan_purpose_name")==null?"":obj.get("loan_purpose_name").toString();
					apply.apply_amount = EmptyUtil.obj20(obj.get("apply_amount")==null?"0":obj.get("apply_amount").toString());
					apply.approve_amount = EmptyUtil.obj20(obj.get("approve_amount")==null?"0":obj.get("approve_amount").toString());
					apply.apply_time = obj.get("apply_time").toString();
					apply.period = Integer.parseInt(obj.get("period").toString());
					apply.statu_name = obj.get("statu_name").toString();
					if(obj.get("bid")!=null) {
						apply.bid = Long.parseLong(obj.get("bid").toString());
						apply.title = obj.get("title")==null?"":obj.get("title").toString();
						apply.bid_status = Integer.parseInt(obj.get("bid_status").toString());
						apply.amount = Double.parseDouble(obj.get("amount").toString());
						apply.has_invested_amount = Double.parseDouble(obj.get("has_invested_amount").toString());
						apply.period_bid = Integer.parseInt(obj.get("period_bid").toString());
						apply.period_unit = Integer.parseInt(obj.get("period_unit").toString());
					}

					//已存在借款订单
					if(apply_result.contains(apply)) {
						v_borrow_apply v = apply_result.get(apply_result.size() - 1);

						t_bids b = new t_bids();
						b.id = apply.bid;
						b.title = apply.title;

						b.status = apply.bid_status;
						if (b.status < 0) {
							b.statusName = "借款失败";
						} else if (b.status < 4 || (b.status > 5 &&  b.status < 23 &&  b.status != 14)) {
							b.statusName ="借款中";
						} else if (b.status == 4 || b.status == 14 ){
							b.statusName ="还款中";
						} else if (b.status == 5){
							b.statusName ="已还款";
						}else{
							b.statusName ="未知";
						}

						b.amount = apply.amount;
						b.has_invested_amount = apply.has_invested_amount;
						b.period = apply.period_bid;
						b.period_unit = apply.period_unit;

						v.bids.add(b);
					}else{
						apply_result.add(apply);
						apply.bids =  new ArrayList<>();

						t_bids b = new t_bids();
						if(apply.bid != null && apply.bid != 0) {
							b.id = apply.bid;
							b.title = apply.title;

							b.status = apply.bid_status;
							if (b.status < 0) {
								b.statusName = "借款失败";
							} else if (b.status < 4 || (b.status > 5 &&  b.status < 23 &&  b.status != 14)) {
								b.statusName ="借款中";
							} else if (b.status == 4 || b.status == 14 ){
								b.statusName ="还款中";
							} else if (b.status == 5){
								b.statusName ="已还款";
							}else{
								b.statusName ="未知";
							}

							b.amount = apply.amount;
							b.has_invested_amount = apply.has_invested_amount;
							b.period = apply.period_bid;
							b.period_unit = apply.period_unit;
							apply.bids.add(b);
						}
						
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("查询我的借款列表出错:" + e.getMessage());
				error.msg = "查询我的借款列表失败!";
			}
		}


		PageBean<v_borrow_apply> page = new PageBean<v_borrow_apply>();
		page.pageSize = pageSize;
		page.currPage = currPage;
		page.totalCount = count;
		page.conditions = conditionMap;

		page.page = apply_result;

		return page;
	}
	
	/**
	 * 借款申请列表
	 * @throws Exception 
	 */
	public static PageBean<Map<String, Object>> applicationList(Params params) throws Exception{

		String currPageStr=params.get("currPage");
		String pageSizeStr=params.get("pageSize");
		
		String apply_Id=params.get("apply_Id");
		String mobile=params.get("mobile");
		String borrow_no=params.get("borrow_no");
		String loan_property_id=params.get("loan_property_id");
		String the_area=params.get("apply_area");
		String apply_status=params.get("apply_status");
		
		String reality_name=params.get("reality_name");
		String user_idnumber=params.get("user_idnumber");
		String apply_star_date=params.get("apply_star_date");
		String apply_end_date=params.get("apply_end_date");

		String order_by_columns=params.get("order_by_columns");
		String asc_desc=params.get("asc_desc");
		
		//尽调action进入会有此参数
		String is_due_diligence=params.get("is_due_diligence");
		//YMD亿美贷action进入会有此参数
		String is_ymd=params.get("is_ymd");
		//params.put("is_due_diligence", "is_due_diligence");
		//=======================================page=======================================
		int currPage = 1;
		int pageSize = 10;
		if(!StringUtil.isBlank(currPageStr) && NumberUtil.isNumericInt(currPageStr)){
			currPage=Integer.parseInt(currPageStr)>0?Integer.parseInt(currPageStr):currPage;
		}
		if(!StringUtil.isBlank(pageSizeStr) && NumberUtil.isNumericInt(pageSizeStr)){
			pageSize=Integer.parseInt(pageSizeStr)>0?Integer.parseInt(pageSizeStr):pageSize;
		}
		//=======================================columns=======================================
		String columns=" borrow_a.id,user.name user_name,borrow_a.borrow_no,user.time user_time,borrow_a.apply_time,user.mobile mobile,user.reality_name,user.id_number, "
					+ " borrow_a.product_id,product.name as product_name,borrow_a.loan_property_id,borrow_a.loan_purpose_id,loan_purposes.name loan_purpose_name, "
					+ " borrow_a.apply_amount,ifnull(borrow_a.approve_amount,0) approve_amount,ifnull(bid_amount.amount,0) bid_amount, borrow_a.period, "
					+ " borrow_a.apply_area,apply_agency.area apply_area_name,borrow_a.allot_area,allot_agency.area allot_area_name, "
					+ " borrow_a.status apply_status, apply_status.enum_name apply_status_name, "
					+ " borrow_a.allot_time,allot_admin.reality_name allot_admin_name,borrow_a.submit_time,assigned_admin.reality_name assigned_admin_name , "
					+ " borrow_a.audit_time,audit_admin.reality_name audit_admin_name,borrow_a.recheck_time,recheck_admin.reality_name recheck_admin_name, "
					+ " ifnull(credit_a.audit_credit_amount,0) audit_credit_amount,ifnull(all_borrow_a.use_amount,0)-ifnull(all_borrow_a.real_repayment_corpus,0) as useCredit ";
		//=======================================table=======================================
		String table= " from t_borrow_apply borrow_a "
					+ " left join t_new_product product on product.id=borrow_a.product_id"
					+ " left join (select * from t_enum_map where enum_type=2 ) apply_status on apply_status.enum_code=borrow_a.status "
					+ " left join t_dict_loan_purposes loan_purposes on loan_purposes.id=borrow_a.loan_purpose_id "
					+ " left join (select borrow_apply_id,sum(amount) amount "
					+ "				from t_bids where status>=0 group by borrow_apply_id "
					+ " )bid_amount on bid_amount.borrow_apply_id = borrow_a.id "
					+ " left join t_users user on user.id=borrow_a.user_id "
					+ " left join t_supervisors allot_admin on allot_admin.id=borrow_a.allot_admin "
					+ " left join t_supervisors audit_admin on audit_admin.id=borrow_a.audit_admin "
					+ " left join t_supervisors recheck_admin on recheck_admin.id=borrow_a.recheck_admin "
					+ " left join t_agencies apply_agency on apply_agency.id=borrow_a.apply_area "
					+ " left join t_agencies allot_agency on allot_agency.id=borrow_a.allot_area "
					+ " left join t_supervisors assigned_admin on assigned_admin.id=allot_agency.supervisor_id "
					+ " left join t_credit_apply credit_a on credit_a.id=borrow_a.credit_apply_id "
					+ " left join ( "
					+ "		select "
					+ "		borrow_a.credit_apply_id, "
					+ "		sum(case when borrow_a.status in(1,2,3,4,5) then ifnull(borrow_a.approve_amount,ifnull(borrow_a.apply_amount,0)) "
					+ "			when borrow_a.status=6 then ifnull(bid_bill.bid_amount,0) else 0 end ) as use_amount, "
					+ "		sum(ifnull(bid_bill.real_repayment_corpus, 0 )) as real_repayment_corpus "
					+ "		from t_borrow_apply borrow_a "
					+ "		left join ( "
					+ "			select bid.borrow_apply_id,sum(bid.amount) bid_amount,sum(ifnull( bill.real_repayment_corpus, 0 )) as  real_repayment_corpus "
					+ "			from  t_bids bid "
					+ "			left JOIN ( SELECT bid_id, sum( real_repayment_corpus ) as real_repayment_corpus FROM t_bills WHERE STATUS IN ( -2, -3, 0 ) GROUP BY bid_id ) bill ON bill.bid_id = bid.id "
					+ "			where bid.borrow_apply_id is not null "
					+ "			and bid.status>=0 "
					+ "			group by bid.borrow_apply_id "
					+ "		)bid_bill on bid_bill.borrow_apply_id= borrow_a.id "
					+ "		where true "
					+ "		and borrow_a.status in(1,2,3,4,5,6) "
					+ "		and borrow_a.credit_apply_id is not null "
					+ "		group by borrow_a.credit_apply_id "
					+ " )all_borrow_a on all_borrow_a.credit_apply_id=credit_a.id "
					+ " where true ";
		//=======================================condition=======================================
		List<Object> paramsList = new ArrayList<Object>();
		String condition="";
		if(!StringUtil.isBlank(apply_Id) && NumberUtil.isNumericInt(apply_Id)){
			condition+=" and borrow_a.id =? ";
			paramsList.add(apply_Id);
		}
		if(!StringUtil.isBlank(mobile) && NumberUtil.isNumericInt(mobile)){
			condition+=" and user.mobile like ? ";
			paramsList.add("%" + mobile +"%");
		}
		if(!StringUtil.isBlank(borrow_no) ){
			condition+=" and borrow_a.borrow_no like ? ";
			paramsList.add("%"+borrow_no+"%");
		}
		if(!StringUtil.isBlank(loan_property_id) && NumberUtil.isNumericInt(loan_property_id) ){
			condition+=" and borrow_a.loan_property_id = ? ";
			paramsList.add(loan_property_id);
		}
		if(!StringUtil.isBlank(the_area) && NumberUtil.isNumericInt(the_area) ){
			condition+=" and (borrow_a.allot_area = ? or (borrow_a.allot_area is null and borrow_a.apply_area = ?)) ";
			paramsList.add(the_area);
			paramsList.add(the_area);
		}
		if(!StringUtil.isBlank(apply_status) && NumberUtil.isNumericInt(apply_status) ){
			condition+=" and borrow_a.status=? ";
			paramsList.add(apply_status);
		}
		if(!StringUtil.isBlank(reality_name)){
			condition+=" and user.reality_name like ? ";
			paramsList.add("%" + reality_name +"%");
		}
		if(!StringUtil.isBlank(user_idnumber)){
			condition+=" and user.id_number like ? ";
			paramsList.add("%" + user_idnumber +"%");
		}
		if(!StringUtil.isBlank(apply_star_date) ){
			try {
				Date starDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_star_date);
				condition+=" and credit_increase.apply_time >= ? ";
				paramsList.add(starDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!StringUtil.isBlank(apply_end_date) ){
			try {
				Date endDate=new SimpleDateFormat("yyyy-MM-dd").parse(apply_end_date);
				condition+=" and credit_increase.apply_time < ? ";
				paramsList.add(endDate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if("is_due_diligence".equals(is_due_diligence) ){
			condition+=" and allot_agency.supervisor_id=? ";
			paramsList.add(Supervisor.currSupervisor().id);
		}
		
		if("is_ymd".equals(is_ymd) ){
			condition+=" and borrow_a.product_id=? ";
			paramsList.add(new t_new_product().getEnumByCode("YI").id);
		}else {
			condition+=" and borrow_a.product_id<>? ";
			paramsList.add(new t_new_product().getEnumByCode("YI").id);
		}
		
		//=======================================order_by=======================================
		String order_by="";
		if(StringUtil.isBlank(order_by_columns)){
			order_by_columns="borrow_a.id";
		}
		if(StringUtil.isBlank(asc_desc)){
			asc_desc="desc";
		}
		order_by=" order by "+order_by_columns+" "+asc_desc;
		
		return PageBeanForPlayJPA.getPageBeanMapBySQL(columns,table+condition,order_by,currPage,pageSize,paramsList.toArray());
	}
	
	public static PageBean<t_borrow_apply> applyPageBean(int currPageStr, int pageSizeNum,String a,Params params) {
		t_borrow_apply borrowApply=new t_borrow_apply();
		return PageBeanForPlayJPA.getPageBean(borrowApply, "", "", currPageStr, pageSizeNum);
	}
	
	/*
	t_borrow_apply.status:
	1	未分配
	2	已分配
	3	已提交
	4	已初审
	5	已通过
	6	借款取消
	7	审核不通过
	 */
	/**
	 * 借款申请审核查询
	 * @throws Exception 
	 */
	public static t_borrow_apply applicationCheckView(Long applyId,Integer applyStatus) throws Exception{
		
		t_borrow_apply borrowApply=applicationDetailView(applyId,applyStatus);
		//额外权限
		if(borrowApply.status==4 && borrowApply.audit_admin !=null && borrowApply.audit_admin==Supervisor.currSupervisor().id){
			throw new Exception("初审操作人员不能参与复审");
		}
		//尽调
		if(borrowApply.status==2){
			if(!Supervisor.currSupervisor().getAllRightIds().contains(Constants.APPLICATION_DUE_DILIGENCE_RIGHT_ID)){//操作用户权限不包含尽调权
				throw new Exception("您没有尽调权");
			}
			Map<Long,t_agencies> agenciesMap=new t_agencies().fetchEnumMap();
			t_agencies agency=agenciesMap.get(borrowApply.allot_area);
			if(agency.supervisor_id!=Supervisor.currSupervisor().id){
				throw new Exception("该借款申请只能由 "+agency.name+" 负责人员尽调");
			}
		}else{
			if(!Supervisor.currSupervisor().getAllRightIds().contains(Constants.APPLICATION_CHECK_RIGHT_ID)){//操作用户权限不包含审核权
				throw new Exception("您没有审核权");
			}
		}
		return  borrowApply;
	}
	/**
	 * 借款申请查询
	 * @throws Exception 
	 */
	public static t_borrow_apply applicationDetailView(Long applyId,Integer applyStatus) throws Exception{
		
		Map<Integer,t_enum_map> applyStatusMap=t_enum_map.getEnumCodeMapByTypeName("t_borrow_apply.status");
		if(applyStatusMap.get(applyStatus)==null){
			throw new Exception("该借款申请状态有误");
		}
		
		t_borrow_apply borrowApply=JPA.em().find(t_borrow_apply.class, applyId, LockModeType.PESSIMISTIC_WRITE);
		if(borrowApply.id == null || borrowApply.id == 0){
			throw new Exception("错误的借款申请id");
		}
		if(borrowApply.status!=applyStatus){
			throw new Exception("该借款申请状态已变更,请刷新重试");
		}

		return  borrowApply;
	}

	/**
	 * 借款申请审核
	 * @param borrow_apply
	 * @param operation			1.check 2.noPass 3.sendBack
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static t_borrow_apply applicationCheck(t_borrow_apply borrow_apply,Integer operation,Params params) throws Exception{
		/*
		1	未分配
		2	已分配
		3	已提交
		4	已初审
		5	已通过
		6	借款取消
		7	审核不通过
		未分配-1->已分配-1->已提交-1->已初审-1->已通过-1->借款取消
		已提交-2->审核不通过|已初审-2->审核不通过
		已提交-3->已分配|已初审-3->已分配
		 */
		t_borrow_apply borrowApply=BorrowApply.applicationCheckView(borrow_apply.id,borrow_apply.status);
		Date actionTime=new Date();
		String description_title="";
		String description="";
		List<Integer> statuslist=new ArrayList<Integer>();
		//1.check 2.noPass 3.sendBack
		if(operation==1){
			//操作验证
			statuslist.add(1);statuslist.add(2);statuslist.add(3);statuslist.add(4);statuslist.add(5);
			if(!statuslist.contains(borrowApply.status)){
				throw new Exception("该借款申请当前不能执行通过操作");
			}
			
			//实名,银行卡判断2,3,4
			statuslist.clear();
			statuslist.add(2);statuslist.add(3);statuslist.add(4);
			
			if(borrowApply.status==1){//未分配1-->已分配2
				description_title="借款已提交";
				description="您的借款已提交至后台审批,我们会尽快完成审批工作,并告知您审批结果";
				if(borrow_apply.allot_area==null){
					throw new Exception("没有分配区域");
				}
				borrowApply.allot_area=borrow_apply.allot_area;
				borrowApply.allot_admin=Supervisor.currSupervisor().id;
				borrowApply.allot_time=actionTime;
				borrowApply.status=2;
			} else if(statuslist.contains(borrowApply.status)){
				User user=new User();
				user.id=borrowApply.user_id;
				//判断实名
				if(StringUtil.isBlank(user.realityName)){
					throw new Exception("申请用户未实名,请实名后重试");
				}
				//判断银行卡信息
				t_user_bank_accounts userBankAccounts=UserBankAccounts.queryById(user.id);
				if(userBankAccounts == null){
					throw new Exception("申请用户没有绑定银行卡,请绑定后重试");
				}
				//判断借款性质和用户实名性质是否相同
				if(borrow_apply.loan_property_id!=user.user_type){
					throw new Exception("借款性质和用户实名性质不相同");
				}
				//判断审批金额必填
				if(borrow_apply.approve_amount==null){
					throw new Exception("没有审批金额");
				}
				borrowApply.approve_amount=borrow_apply.approve_amount;
				borrowApply.loan_property_id=borrow_apply.loan_property_id;
				
				if(borrowApply.status==2){
					borrowApply.submit_admin=Supervisor.currSupervisor().id;
					borrowApply.submit_time=actionTime;
				}else if(borrowApply.status==3){
					borrowApply.audit_admin=Supervisor.currSupervisor().id;
					borrowApply.audit_time=actionTime;
				}else if(borrowApply.status==4){
					borrowApply.recheck_admin=Supervisor.currSupervisor().id;
					borrowApply.recheck_time=actionTime;
					description_title="借款上标中";
					description="您的借款正在进行上标,请耐心等待,如有疑问请致电:021-6438-0510";
				}
				//已分配2-->已提交3|已提交3-->已初审4|已初审4-->已通过5
				borrowApply.status=borrowApply.status+1;
				
			}else if(borrowApply.status==5){ //已通过5-->借款取消6
				description_title="借款已取消";
				description="您的借款已取消上标操作";
				borrowApply.status=6;
			}
		//1.check 2.noPass 3.sendBack
		}else if(operation==2){
			//操作验证
			statuslist.add(3);statuslist.add(4);
			if(!statuslist.contains(borrowApply.status)){
				throw new Exception("该借款申请当前不能执行审核不通过");
			}
			if(StringUtil.isBlank(borrow_apply.reason)){
				throw new Exception("没有拒绝理由");
			}
			if(borrowApply.status==3){
				borrowApply.audit_admin=Supervisor.currSupervisor().id;
				borrowApply.audit_time=actionTime;
			}else if(borrowApply.status==4){
				borrowApply.recheck_admin=Supervisor.currSupervisor().id;
				borrowApply.recheck_time=actionTime;
			}
			description_title="审核失败";
			description="失败原因:"+borrow_apply.reason;
			borrowApply.status=7;
			borrowApply.reason=borrow_apply.reason;
		//1.check 2.noPass 3.sendBack
		}else if(operation==3){
			//操作验证
			statuslist.add(3);statuslist.add(4);
			if(!statuslist.contains(borrowApply.status)){
				throw new Exception("该借款申请当前不能执行退回操作");
			}
			if(borrowApply.status==3){
				borrowApply.audit_admin=Supervisor.currSupervisor().id;
				borrowApply.audit_time=actionTime;
			}else if(borrowApply.status==4){
				borrowApply.recheck_admin=Supervisor.currSupervisor().id;
				borrowApply.recheck_time=actionTime;
			}
			borrowApply.status=2;
		}else{
			throw new Exception("错误的操作");
		}
		
		borrowApply.save();
		LogCore.create(2, borrowApply.id, 2,Supervisor.currSupervisor().id , borrowApply.status, actionTime, description_title, description);
		return  borrowApply;
	}
	
	/**
	 * 亿美贷审核通过
	 * @param borrowApply
	 * @param operation			1.check 2.noPass
	 * @throws Exception
	 */
	public static void checkYMD(t_borrow_apply borrowApply,Integer operation,Params params) throws Exception {
		if(borrowApply.id==null || borrowApply.id==0){
			throw new Exception("错误的借款申请id");
		}
		if( borrowApply.product_id!=new t_new_product().getEnumByCode("YI").id){
			throw new Exception("错误的借款申请,不是亿美贷产品");
		}
		if( borrowApply.status==1) {
			borrowApply.status=4;//亿美贷直接跳到预审完成状态操作
		}

		BorrowApply.applicationCheck(borrowApply,operation,params);

	}
	
	public static t_borrow_apply getModelByPessimisticWrite(long id){
		//ESSIMISTIC_READ=lock in share mode  
		//PESSIMISTIC_WRITE=for update 
		return BorrowApply.getModel(id,LockModeType.PESSIMISTIC_WRITE);
	}
	
	public static t_borrow_apply getModel(long id,LockModeType lockModeType){
		t_borrow_apply borrowApply=null;
		try {
			borrowApply=JPA.em().find(t_borrow_apply.class, id, lockModeType);
		} catch (Exception e) {
			Logger.error("BorrowApply.getModel"+id+e.getMessage());
			JPA.setRollbackOnly();
			e.printStackTrace();
			return null;
		}
		if (borrowApply == null) {
			Logger.error("BorrowApply.getModel"+id+"数据实体对象不存在!");
			JPA.setRollbackOnly();
			return null;
		}
		return borrowApply;
	}
	
}
