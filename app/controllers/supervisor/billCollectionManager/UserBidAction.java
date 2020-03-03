package controllers.supervisor.billCollectionManager;

import models.v_bid_bad;
import models.v_bid_repayment;
import models.v_bid_repaymenting;
import business.Bid;
import business.Supervisor;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import controllers.supervisor.bidManager.BidPlatformAction;
import utils.ErrorInfo;
import utils.PageBean;

/**
 * 我的/部门 会员借款标
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-29 上午10:02:34
 */
public class UserBidAction extends SupervisorController {

	/**
	 * 还款中的借款标列表
	 */
	public static void repaymentingList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.page = Bid.queryBidRepaymenting(0, pageBean,
				Supervisor.currSupervisor().id, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 已完成的借款标列表
	 */
	public static void repaymentList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(0, pageBean,
				Supervisor.currSupervisor().id, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 坏账借款标列表
	 */
	public static void badList() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(0, pageBean,
				Supervisor.currSupervisor().id, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 借款标详情
	 * 
	 * @param bidid
	 * @param type
	 *            会员借款标类型 1 还款中 2 已完成 3 坏账
	 */
	public static void detail(long bidid, int type, int falg) {
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.manageSupervisorId = Supervisor.currSupervisor().id;
		bid.upNextFlag = falg;
		bid.id = bidid;

		render(bid, type, falg);
	}

	/**
	 * 部门还款中的借款标列表
	 */
	public static void repaymentingListToDep() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repaymenting> pageBean = new PageBean<v_bid_repaymenting>();
		pageBean.page = Bid.queryBidRepaymenting(0, pageBean, 0, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 部门已完成的借款标列表的搜索
	 */
	public static void repaymentListToDep() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_repayment> pageBean = new PageBean<v_bid_repayment>();
		pageBean.page = Bid.queryBidRepayment(0, pageBean, 0, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 部门坏账借款标列表
	 */
	public static void badListToDep() {
		ErrorInfo error = new ErrorInfo();
		PageBean<v_bid_bad> pageBean = new PageBean<v_bid_bad>();
		pageBean.page = Bid.queryBidBad(0, pageBean, 0, error,
				BidPlatformAction.getParameter(pageBean, null));

		if (null == pageBean.page)
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);

		render(pageBean);
	}

	/**
	 * 借款标详情
	 * 
	 * @param bidid
	 * @param type
	 *            1 还款中， 2 已完成， 3 坏账
	 */
	public static void detailToDep(long bidid, int type, int falg) {
		Bid bid = new Bid();
		bid.bidDetail = true;
		bid.upNextFlag = falg;
		bid.id = bidid;

		render(bid, type, falg);
	}
}
