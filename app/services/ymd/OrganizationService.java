package services.ymd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import models.core.t_interest_rate;
import models.core.t_org_project;
import models.core.t_service_cost_rate;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.GenericModel.JPAQuery;
import utils.JPAUtil;

/**
 *  医院机构信息 
 * @author MSI
 *
 */
public class OrganizationService {
	
	//通过额度申请id获取机构项目
		public static List<t_org_project> getOrgProjectByCreditApplyId(Long creditApplyId) {
			List<t_org_project> orgProjectList=new ArrayList<t_org_project>();
			orgProjectList=t_org_project.find("id in (select org_project_id from t_apply_org_project apply_project " + 
							" where apply_id=?) ", creditApplyId).fetch();
			return orgProjectList;
		}
		
	//获取机构项目
	public static List<t_org_project> getOrgProject(Long orgId) {
		List<t_org_project> parentProjectList=new ArrayList<t_org_project>();
		List<t_org_project> orgProjectList=new ArrayList<t_org_project>();
		orgProjectList=t_org_project.find("select childProject "
				+ "from t_org_project parentProject,t_org_project childProject "
				+ "where parentProject.p_id=-1 and parentProject.org_id=?"
				+ "and (childProject.p_id=parentProject.id or childProject.id=parentProject.id)", orgId).fetch();
		for(t_org_project majorProject:orgProjectList) {
			if(majorProject.p_id==-1){
				majorProject.childProjectList=new ArrayList<t_org_project>();
				for(t_org_project subProject:orgProjectList) {
					if(subProject.p_id==majorProject.id) {
						majorProject.childProjectList.add(subProject);
						subProject.parentProject=majorProject;
					}
				}
				parentProjectList.add(majorProject);
			}
		}
		return parentProjectList;
	}
	
	//获取项目期数、利息
	public static List<t_interest_rate> getInterestRate(Long product_id,Long orgId,Long repayment_type_id,Long period_unit) {
		List<t_interest_rate> interest_rates = new ArrayList<t_interest_rate>();
		try {
			String sql="select * from t_interest_rate where  product_id=? and org_id = ? and interest_rate is not null ";
			if(repayment_type_id!=null){
				sql+=" and repayment_type_id = ? ";
			}
			if(period_unit!=null){
				sql+=" and period_unit = ? ";
			}
			Query query = JPA.em().createNativeQuery(sql, t_interest_rate.class);
			int paramenterIndex=1;
			query.setParameter(paramenterIndex++, product_id);
			query.setParameter(paramenterIndex++, orgId);
			if(repayment_type_id!=null){
				query.setParameter(paramenterIndex++, repayment_type_id);
			}
			if(period_unit!=null){
				query.setParameter(paramenterIndex++, period_unit);
			}
			interest_rates=query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("机构项目利息查询时：" + e.getMessage());
		}
		
		return interest_rates;
	}
	
	//获取项目服务费期数、利息
	public static List<t_service_cost_rate> getServiceCostRate(Long product_id,Long orgId,Long period_unit) {
		
		List<t_service_cost_rate> service_cost_rates = new ArrayList<t_service_cost_rate>();
		
		try {
			String sql="select * from t_service_cost_rate where  product_id=? and org_id = ? ";
			if(period_unit!=null){
				sql+=" and period_unit = ? ";
			}
			Query query = JPA.em().createNativeQuery(sql, t_service_cost_rate.class);
			query.setParameter(1, product_id);
			query.setParameter(2, orgId);
			if(period_unit!=null){
				query.setParameter(3, period_unit);
			}
			service_cost_rates=query.getResultList();
			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info("机构项目服务费利息查询时：" + e.getMessage());
		}
		
		return service_cost_rates;
	}
	public static void installInterestRateList(List<t_interest_rate> interestRateList,List<t_service_cost_rate> serviceCostRateList,
			List<t_service_cost_rate> defultServiceCostRateList){
		for(t_interest_rate interest_rate:interestRateList){
			t_service_cost_rate serviceCostRate=OrganizationService.installInterestRate(interest_rate,serviceCostRateList);
			if(serviceCostRate==null){
				serviceCostRate=OrganizationService.installInterestRate(interest_rate,defultServiceCostRateList);
			}
			if(serviceCostRate==null){
				serviceCostRate=new t_service_cost_rate();
				serviceCostRate.org_id=interest_rate.org_id;
				serviceCostRate.product_id=interest_rate.product_id;
				serviceCostRate.period_unit=interest_rate.period_unit;
				serviceCostRate.period=interest_rate.period;
				serviceCostRate.service_cost_rate=BigDecimal.ZERO;
				serviceCostRate.service_cost_rule=1;
				serviceCostRate.service_payment_model=1;
			}
			interest_rate.service_cost_rate=serviceCostRate;
		}
	}
	
	private static t_service_cost_rate installInterestRate(t_interest_rate interest_rate,List<t_service_cost_rate> serviceCostRateList){
		for(t_service_cost_rate serviceCostRate:serviceCostRateList){
			if( interest_rate.product_id==serviceCostRate.product_id 
					&& interest_rate.period_unit.equals(serviceCostRate.period_unit) 
					&& interest_rate.period.equals(serviceCostRate.period)){
				return serviceCostRate;
			}
		}
		return null;
	}
}
