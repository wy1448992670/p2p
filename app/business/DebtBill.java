package business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import constants.Constants;
import play.Logger;
import play.db.jpa.JPA;
import utils.Arith;
import utils.DateUtil;
import utils.ErrorInfo;
import utils.TransferUtil;
import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_debt_bill_invest;
import models.t_debt_invest;
import models.t_debt_transfer;
import models.t_invests;

public class DebtBill implements Serializable{
	public static void addDebtBills(t_debt_transfer debtTransfer,t_invests invest,t_bids bid,ErrorInfo error){
		error.clear();
		try {
			EntityManager em = JPA.em();
			List<t_debt_invest> debtInvestList = t_debt_invest.find(" debt_id = ? ", debtTransfer.id).fetch();
			
			//查询剩余的未还期数账单记录
			List<t_bill_invests> billInvestList = t_bill_invests.find(" invest_id = ? and periods >= ? and status = -8 order by periods asc" , debtTransfer.invest_id, debtTransfer.current_period).fetch();
			
			BigDecimal debtCorpusAmountDec=BigDecimal.valueOf(debtTransfer.debt_amount);//债权转让标转让总金额
			debtCorpusAmountDec.setScale(2,BigDecimal.ROUND_HALF_EVEN);
			int scale=debtCorpusAmountDec.precision();//比例计算进度,最终金额需要精确到分,如果计算的原金额整数位为X,比例的小数位需要精确到小数点后X+2位,计算结果可以精确到分
			//for (t_bill_invests billInvest : billInvestList) {//原投资账单,按期循环
			BigDecimal debtInterestAcmountDec=BigDecimal.ZERO;//转让的所有利息
			Map<String,Map<String,BigDecimal>> debtBillCntMap=new HashMap<String,Map<String,BigDecimal>>();
			
			for (int i=0;i<billInvestList.size();i++) {//原投资账单,按期循环
				t_bill_invests billInvest=billInvestList.get(i);
				int currentPeriodAccrualDate=1;//当期总天数
				int accrualDate=1;//当期受让人计息天数
				BigDecimal assigneeRate=BigDecimal.ONE;//受让人所占比例
				if(debtTransfer.current_period == billInvest.periods){ //如果本期账单不是全部属于受让人，则本金全部给受让人，利息按比例分配
					
					Date currentPeriodBeginDate=null;//当期开始时间
					if(debtTransfer.repayment_type == Constants.ONCE_REPAYMENT){//一次性还款
						currentPeriodBeginDate=bid.audit_time;//当期开始时间=放款时间
					}else{
						currentPeriodBeginDate=DateUtil.dateAddMonth(billInvest.receive_time,-1);//当期开始时间=本期还款时间-1month
					}
					currentPeriodAccrualDate=DateUtil.daysBetween(currentPeriodBeginDate,billInvest.receive_time);//当期总天数
					accrualDate=DateUtil.daysBetween(debtTransfer.accrual_time,billInvest.receive_time);//当期受让人计息天数
					assigneeRate=BigDecimal.valueOf(accrualDate).divide(BigDecimal.valueOf(currentPeriodAccrualDate), scale, BigDecimal.ROUND_HALF_EVEN);//受让人所占比例
				}
				BigDecimal debtInvestCntAmount=BigDecimal.ZERO;//债权转让投资已计算金额
				BigDecimal debtBillInvestCntCorpus=BigDecimal.ZERO;//债权转让投资账单已计算本金
				BigDecimal debtBillInvestCntInterest=BigDecimal.ZERO;//债权转让投资账单已计算利息
				BigDecimal currentPeriodCorpus=BigDecimal.valueOf(billInvest.receive_corpus);//所有债权转让投资人可获得的当期本金
				BigDecimal currentPeriodInterest=BigDecimal.valueOf(billInvest.receive_interest).multiply(assigneeRate).setScale(2, BigDecimal.ROUND_HALF_EVEN);//所有债权转让投资人可获得的当期利息
				debtInterestAcmountDec=debtInterestAcmountDec.add(currentPeriodInterest);//转让的所有利息
				for (t_debt_invest debtInvest : debtInvestList) {//债权转让投资,多笔债权转让投资循环
					
					Map<String,BigDecimal> debtBillCnt=debtBillCntMap.get(debtInvest.id+"");//每笔债权转让投资的累积
					if(debtBillCnt==null){
						debtBillCnt=new HashMap<String,BigDecimal>();
						debtBillCntMap.put(debtInvest.id+"",debtBillCnt);
					}
					BigDecimal corpusCnt=debtBillCnt.get("corpusCnt")==null?BigDecimal.ZERO:debtBillCnt.get("corpusCnt");//每笔债权转让投资的发放本金累积
					BigDecimal interestCnt=debtBillCnt.get("interestCnt")==null?BigDecimal.ZERO:debtBillCnt.get("interestCnt");//每笔债权转让投资的发放利息累积
					t_debt_bill_invest debtBillInvest = new t_debt_bill_invest();//债权记录根据原投资记录百分比计算
					debtBillInvest.user_id = debtInvest.user_id;//投资人用户ID
					debtBillInvest.debt_id = debtTransfer.id;//债权转让ID
					debtBillInvest.debt_invest_id = debtInvest.id;//债权投资ID
					debtBillInvest.receive_time = billInvest.receive_time;
					debtBillInvest.periods = billInvest.periods;
					debtBillInvest.title = debtTransfer.title;
					debtBillInvest.old_bill_id = billInvest.id;//原账单ID
					debtBillInvest.real_receive_time = null;
					debtBillInvest.status = billInvest.status==-8?-1:billInvest.status;
					debtBillInvest.is_all_receiver = (accrualDate==currentPeriodAccrualDate);//利息是否全属于受让人
					
					debtInvestCntAmount=debtInvestCntAmount.add(BigDecimal.valueOf(debtInvest.amount));//债权转让投资已计算金额
					
					if(i<billInvestList.size()-1){
						//之前已计算的债权转让投资金额+本次计算的债权转让投资金额 占 原标投资金额的占比
						//BigDecimal debtInvestCntMoreOneRate=debtInvestCntAmount.add(BigDecimal.valueOf(debtInvest.amount)).divide(debtAmountDec, scale, BigDecimal.ROUND_HALF_EVEN);
						//之前已计算的债权转让投资金额  占 原标投资金额的占比
						//BigDecimal debtInvestCntRate=debtInvestCntAmount.divide(debtAmountDec, scale, BigDecimal.ROUND_HALF_EVEN);
						
						//debtInvest.amount/debtTransfer.debt_amount * amountX
						//=( (debtInvestCntAmount+debtInvest.amount)/debtTransfer.debt_amount - debtInvestCntAmount/debtTransfer.debt_amount ) * amountX
						//=(debtInvestCntAmount+debtInvest.amount)/debtTransfer.debt_amount * amountX - (debtInvestCntAmount)/debtTransfer.debt_amount  * amountX
						//=(debtInvestCntAmount+debtInvest.amount)/debtTransfer.debt_amount * amountX - cntAmountX
						//=(debtInvestCntAmount+debtInvest.amount)* amountX /debtTransfer.debt_amount  - cntAmountX
						debtBillInvest.receive_corpus=debtInvestCntAmount.multiply(currentPeriodCorpus).divide(debtCorpusAmountDec, 2, BigDecimal.ROUND_HALF_EVEN).subtract(debtBillInvestCntCorpus).doubleValue();
						debtBillInvest.receive_interest=debtInvestCntAmount.multiply(currentPeriodInterest).divide(debtCorpusAmountDec, 2, BigDecimal.ROUND_HALF_EVEN).subtract(debtBillInvestCntInterest).doubleValue();
						
						//debtBillInvest.receive_corpus = Arith.round(debtInvest.amount / debtTransfer.debt_amount * billInvest.receive_corpus, 2);//债权投资本金
						//debtBillInvest.receive_interest =  Arith.round(debtInvest.amount / debtTransfer.debt_amount * billInvest.receive_interest* assigneeRate.doubleValue(), 2); 
						
						debtBillInvestCntCorpus=debtBillInvestCntCorpus.add(BigDecimal.valueOf(debtBillInvest.receive_corpus));//债权转让投资账单已计算本金
						debtBillInvestCntInterest=debtBillInvestCntInterest.add(BigDecimal.valueOf(debtBillInvest.receive_interest));//债权转让投资账单已计算利息
					}else{//最后一期
						BigDecimal debtInvestInterest=debtInvestCntAmount.multiply(debtInterestAcmountDec).divide(debtCorpusAmountDec, 2, BigDecimal.ROUND_HALF_EVEN).subtract(debtBillInvestCntInterest);//每笔债权投资可以获得的总利息
						debtBillInvest.receive_corpus=BigDecimal.valueOf(debtInvest.amount).subtract(corpusCnt).doubleValue();//最后一期的本金=每笔债权的投资额-之前累积发放的本金
						debtBillInvest.receive_interest=debtInvestInterest.subtract(interestCnt).doubleValue();//最后一期的利息=每笔债权投资可以获得的总利息-之前累积发放的利息
						
						//debtBillInvestCntCorpus=debtBillInvestCntCorpus.add(BigDecimal.valueOf(debtBillInvest.receive_corpus));//债权转让投资账单已计算本金
						debtBillInvestCntInterest=debtBillInvestCntInterest.add(debtInvestInterest);//债权转让投资已计算利息
					}
					
					debtBillCnt.put("corpusCnt",corpusCnt.add(BigDecimal.valueOf(debtBillInvest.receive_corpus)));//每笔债权转让投资的发放本金累积
					debtBillCnt.put("interestCnt",interestCnt.add(BigDecimal.valueOf(debtBillInvest.receive_interest)));//每笔债权转让投资的发放利息累积
					
	                
					try {
						debtBillInvest.save();
					} catch (Exception e) {
						error.setWrongMsg("对不起！债权账单保存失败！");
						Logger.error("对不起！债权账单保存失败！"+e.getStackTrace());
						JPA.setRollbackOnly();
						break;
					}
				}
				try {
					billInvest.status=-7;
					billInvest.save();
				} catch (Exception e) {
					error.setWrongMsg("对不起！原投资账单保存失败！");
					Logger.error("对不起！原投资账单保存失败！"+e.getStackTrace());
					JPA.setRollbackOnly();
					break;
				}
				//更新t_debt_invest 债权本金和利息
				String sql ="update t_debt_invest t, (select t1.debt_invest_id as debt_invest_id, sum(t1.receive_corpus) as  receive_corpus,sum(t1.receive_interest) as receive_interest "
						+ " from t_debt_bill_invest t1  where t1.debt_id = ? GROUP BY t1.debt_invest_id) t2 set t.correct_amount = t2.receive_corpus ,"
						+ "t.correct_interest = t2.receive_interest where t.id = t2.debt_invest_id ";
				Query updateCorInt = em.createNativeQuery(sql).setParameter(1, debtTransfer.id);
				int rows = 0;
				
				try {
					rows = updateCorInt.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					error.setWrongMsg("数据库异常，更新债权投资记录应收本金和利息失败");
					Logger.error("更新债权投资记录应收本金和利息时：" + e.getMessage());
					JPA.setRollbackOnly();
					return;
				}
				
				if (rows == 0) {
					error.setWrongMsg("更新债权投资记录操作数据库没有改变");
					JPA.setRollbackOnly();
					return;
				}
			}
			
		} catch (Exception e) {
			error.setWrongMsg( "添加债权转让账单查询失败！");
			Logger.error( "添加债权转让账单查询失败！" + e.getMessage());
			JPA.setRollbackOnly();
			return;
		}
		error.code=1;
		error.msg="生成债权转让投资账单成功";
	}
	public static List<t_debt_bill_invest> getDebtBillInvestByBillInvestId(long billInvestId){
		List<t_debt_bill_invest> t_debt_bill_invest_list = null;
		try {
			t_debt_bill_invest_list = t_debt_bill_invest.find(" old_bill_id = ? ", billInvestId).fetch();
		} catch (Exception e) {
			Logger.error("查询投资账单"+billInvestId+"的所有债权转让投资账单:" + e.getMessage());
			throw e;
		}
		if(null == t_debt_bill_invest_list){
			return null;
		}
		return t_debt_bill_invest_list;
	}
	
	private static void calculateInterest(t_debt_invest debtInvest, t_bill_invests billInvest, t_debt_bill_invest debtBill ,int type, t_invests invest,ErrorInfo error){
		try {
			//根据投资ID 查询该投资记录的金额及利息， 按比例计算
			//String sql = "select amount from t_invests where id = ? ";
			//double invest.amount = t_invests.find(sql, billInvest.invest_id).first(); //该笔账单的债权金额
			
			//double debtInvest.amount = debtInvest.amount;//债权投资金额
			
			//double billInvest.receive_corpus =  billInvest.receive_corpus;//本期应收款金额
			//double billInvest.receive_interest = billInvest.receive_interest;//本期应收利息
	 
			if(type == 1){ //如果本期账单不是全部属于受让人，则本金全部给受让人，利息按比例分配
				Date receive_time = billInvest.receive_time;//还款时间
				double canTransAmt = Arith.round(TransferUtil.transferAmount(receive_time, debtInvest.amount), 2);//可债权转让的金额
				Logger.info("原账单信息oldBillInvest: " + billInvest);
				Logger.info("本期账单不是全部属于受让人，可债权转让的金额canTransAmount: " + canTransAmt);
				
				double rate = canTransAmt / invest.amount; //可转让金额与原账单的比例,前一半给出让人，后一半给受让人
				double canReceiveInterest_ = billInvest.receive_interest * rate; //受让人的全部利息
				
				debtBill.receive_corpus = Arith.round(debtInvest.amount / invest.amount * billInvest.receive_corpus, 2);//本金全部给受让人
				debtBill.receive_interest =  Arith.round(debtInvest.amount / invest.amount * canReceiveInterest_, 2);//债权投资利息  ，按比例   （债权投资金额/债权金额 = 债权投资利息/债权利息）
				
				debtBill.is_all_receiver = false;
				
			} else {
				debtBill.receive_corpus = Arith.round(debtInvest.amount / invest.amount * billInvest.receive_corpus, 2);//债权投资本金
				debtBill.receive_interest =  Arith.round(debtInvest.amount / invest.amount * billInvest.receive_interest, 2); 
				debtBill.is_all_receiver = true;
			}
		
		} catch (Exception e) {
			error.code = -2;
			error.msg = "计算债权转让账单利息失败！";
			e.printStackTrace();
		} 
	}
}
