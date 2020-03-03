package bean;

import java.math.BigDecimal;

public class PaymentRedBag {
	public Integer bills_id;
	public Integer bids_id;
	public Integer bills_invests_id;
	public Integer repayment_type_id;
	public Integer user_id;
	public BigDecimal receive_corpus;
	public Integer periods;
	public Integer month_count;
	public BigDecimal all_invest_amount;
	public Integer invest_count;

	public Integer getBills_id() {
		return bills_id;
	}

	public void setBills_id(Integer bills_id) {
		this.bills_id = bills_id;
	}

	public Integer getBids_id() {
		return bids_id;
	}

	public void setBids_id(Integer bids_id) {
		this.bids_id = bids_id;
	}

	public Integer getBills_invests_id() {
		return bills_invests_id;
	}

	public void setBills_invests_id(Integer bills_invests_id) {
		this.bills_invests_id = bills_invests_id;
	}

	public Integer getRepayment_type_id() {
		return repayment_type_id;
	}

	public void setRepayment_type_id(Integer repayment_type_id) {
		this.repayment_type_id = repayment_type_id;
	}

	public Integer getUser_id() {
		return user_id;
	}

	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}

	public BigDecimal getReceive_corpus() {
		return receive_corpus;
	}

	public void setReceive_corpus(BigDecimal receive_corpus) {
		this.receive_corpus = receive_corpus;
	}

	public Integer getPeriods() {
		return periods;
	}

	public void setPeriods(Integer periods) {
		this.periods = periods;
	}

	public Integer getMonth_count() {
		return month_count;
	}

	public void setMonth_count(Integer month_count) {
		this.month_count = month_count;
	}

	public BigDecimal getAll_invest_amount() {
		return all_invest_amount;
	}

	public void setAll_invest_amount(BigDecimal all_invest_amount) {
		this.all_invest_amount = all_invest_amount;
	}

	public Integer getInvest_count() {
		return invest_count;
	}

	public void setInvest_count(Integer invest_count) {
		this.invest_count = invest_count;
	}

}
