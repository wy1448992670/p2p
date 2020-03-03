package business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 投资记录实体类
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-3-27 下午03:31:06
 */
public class InvestRecord implements Serializable{

	public long id;
	public long loanBidId;  //借款标ID
	public long userId;   //用户ID
	//public long billId; //账单ID
	
	public Date createTime; //投资时间
	public double loanAmount; //投资金额
	//..
	
	/**
	 * 根据借款标来查询投资记录
	 */
	public static List<InvestRecord> queryInvestByLoanBid(long loanBidId){
		return null;
	}
	
	/**
	 * 根据用户来查询投资记录
	 */
	public static List<InvestRecord> queryInvestByUser(long userId){
		return null;
	}
	
	/**
	 * 添加投资记录
	 */
	public void add(){
		
	}
	
	/**
	 * 确认投标
	 */
	public void invest(){
		// 1.冻结用户资金
		// 2.添加交易记录
		// 3.借款标添加部分钱
		// 4.借款标算出百分比
		// 5.改变各种状态
		// 6.添加投资记录
	}
	
	/**
	 * 自动投标
	 */
	public void automaticInvest(){
		
	}
}
