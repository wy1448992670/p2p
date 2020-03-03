package controllers.front.home;

import business.PactYmd;
import constants.PactTypeEnum;
import controllers.BaseController;

public class PactTestAction extends BaseController {

	/**
	 * 测试合同
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @throws Exception
	 * @author: zj
	 */
	public static void test() throws Exception {
//		NewPact.createPact(3218l, null, PactTypeEnum.QZSQ.getCode(), 1);
//		NewPact.createPact(3218l, null, PactTypeEnum.CJFWXY.getCode(), 1);
//		// 个人
//		NewPact.createPact(2270l, null, PactTypeEnum.JKFWXY.getCode(), 1);
//		// 企业
//		NewPact.createPact(2225l, null, PactTypeEnum.JKFWXY.getCode(), 1);
//		// 企业 咨询管理协议
//		NewPact.createPact(3225l, 112593l, PactTypeEnum.ZXGL.getCode(), 1);
//		NewPact.createPact(2270l, 112446l, PactTypeEnum.ZXGL.getCode(), 1);
		// NewPact.createPact(3218l, null, PactTypeEnum.CJFWXY.getCode());
		// User.updateUserNewPact(2304l, "123", "456", null,
		// PactTypeEnum.JKFWXY.getCode(), 2);
		PactYmd.createPact(3612L, 112639L, PactTypeEnum.YMDFQFWFXY.getCode());
	}

}
