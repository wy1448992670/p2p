package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import com.shove.Convert;

import models.t_debt_invest;
import models.t_invests;
import play.Logger;
import play.db.jpa.JPA;
import utils.EmptyUtil;

/**
 * 债权标 投资记录表（新）
 * 
 * @ClassName DebtInvest
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年7月24日 上午10:27:46
 * @version 1.0.0
 */
public class DebtInvest implements Serializable {

	public long id;				//债权投资ID
	private long _id;
	public long userId;			//投资用户ID
	public long oldUserId;		//原投资用户ID
	public long debtId;			//债权标的ID
	public long investId;		//原标的投资记录ID
	public long bidId;			//原标的ID
	public BigDecimal amount;	//投资金额
	public BigDecimal redAmount;//红包金额
	public Date time;			//投资时间

	public double receiveIncreaseInterest;// 应收加息利息
	public double realIncreaseInterest;// 实际收款加息利息
	public String name;// 债权投资人账号
	public String investTime;//投资时间
	
	public long getId() {
		return _id;
	}
	/**
	 * 根据债权标id获取相关投资信息
	 * 
	 * @param id
	 * @return
	 */
	public static List<DebtInvest> debtTransferDetail(Integer id) {
		String sql = "   select  tu.name,tdi.amount,DATE_FORMAT(tdi.time,'%Y-%m-%d %H:%i:%s') as time " 
						+ "	from t_debt_invest tdi   "
						+ "	left join t_users tu " 
						+ "	on  tdi.user_id=tu.id " 
						+ "	where  tdi.debt_id=? order by tdi.time desc";
		Query query = JPA.em().createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.setParameter(1, id);
		List debtInvestList = query.getResultList();
		List<DebtInvest> list = new ArrayList<DebtInvest>();
		for (Iterator iterator = debtInvestList.iterator(); iterator.hasNext();) {
			Map debtInvest = (Map) iterator.next();
			DebtInvest invest = new DebtInvest();
			invest.name = debtInvest.get("name").toString();
			invest.amount = EmptyUtil.obj20(debtInvest.get("amount"));
			invest.investTime = debtInvest.get("time").toString();
			list.add(invest);
		}
		return list;
	}
	
	/**
	 * 查询对应标的的所有投资者以及投资金额
	 * @param bidId
	 * @return
	 */
	public static List<DebtInvest> queryAllInvests(long debtTransferId) throws Exception{
		List<t_debt_invest> t_debt_invest_list = null;
		List<DebtInvest> result = new ArrayList<DebtInvest>();

		/*String query = "select new Map(i.id as id,i.user_id as userId, i.old_user_id as old_user_id, i.debt_id as debt_id, "
				+ "i.invest_id as invest_id, i.bid_id as bid_id,i.amount as amount,i.red_amount as red_amount,i.time as time) "
				+ "from t_debt_invests i where i.debt_id=?  order by time";*/
		String query = "select di from t_debt_invest di where di.debt_id=?  order by di.time";
		try {
			t_debt_invest_list = t_debt_invest.find(query, debtTransferId).fetch();
			//t_debt_invest_list = t_debt_invest.find("debt_id", debtTransferId).fetch();
		} catch (Exception e) {
			Logger.error("查询债权转让标的"+debtTransferId+"的所有投资者以及投资金额:" + e.getMessage());
			throw e;
		}
		
		if(null == t_debt_invest_list){
			return null;
		}
		
		for (t_debt_invest t_debt_invest : t_debt_invest_list) {

			DebtInvest debtInvest = new DebtInvest();
			debtInvest._id =  t_debt_invest.id;
			debtInvest.userId = t_debt_invest.user_id;
			debtInvest.oldUserId = t_debt_invest.old_user_id;
			debtInvest.debtId = t_debt_invest.debt_id;
			debtInvest.investId = t_debt_invest.invest_id;
			debtInvest.bidId =  t_debt_invest.bid_id;
			debtInvest.amount = BigDecimal.valueOf(t_debt_invest.amount);
			debtInvest.redAmount =BigDecimal.valueOf(t_debt_invest.red_amount);
			debtInvest.time=t_debt_invest.time;
			result.add(debtInvest);
		}

		return result;
	}
	/**
	 * 获得债权转让标所有的投资
	 * @param debtTransferId
	 * @return List<t_debt_invest> 债权转让标所有的投资
	 * @throws Exception
	 */
	public static List<t_debt_invest> queryAllInvestModel(long debtTransferId) throws Exception{
		List<t_debt_invest> debtInvestList = t_debt_invest.find(" debt_id = ? ", debtTransferId).fetch();
		return debtInvestList;
	}
	
}

