package constants;

import play.Play;

import com.shove.Convert;

/**
 * 积分商城 常量
 * 
 * @author yuy
 * @ 2016年1月26日
 */
public class MallConstants {

	/** 商品排序 */
	public static final String[] MALL_GOODS_ORDER = {"order by id desc", "order by max_exchange_count", "order by surplus", "order by exchange_scroe",
			"order by time"};

	/** 商品记录排序 */
	public static final String[] MALL_RECORD_ORDER = {"order by id desc", "order by scroe", "order by time"};

	/** 积分商城:每投资100元送1个积分 */
	public static final double MALL_INVEST_SENDSCROE_LINE = Convert.strToDouble(Play.configuration.getProperty("mall.invest.sendScroe.line"), 100d);

	/** 积分商城：常见问题、商城指引 */
	public static final int COMMEN_QUESTION = 35;

	public static final int MALL_GUIDE = 36;

	public static final int ADD = 1;// 增加

	public static final int MODIFY = 2;// 修改

	public static final int SUCCESS_CODE = 1;// 返回成功码

	public static final int COM_ERROR_CODE = -1;// 普通错误码

	public static final int DML_ERROR_CODE = -2;// DML报错码

	public static final int DATA_DUPL_CODE = -3;// 记录重复

	public static final int STATUS_ENABLE = 1;// 启用

	public static final int STATUS_DISABLE = 2;// 暂停/禁用

	public static final int STATUS_SUCCESS = 1;// 成功

	public static final int STATUS_FAIL = 2;// 未成功

	public static final String STR_ENABLE = "开启";// 启用

	public static final String STR_DISABLE = "暂停";// 暂停

	public static final int VISIBLE = 1;// 可见

	public static final int INVISIBLE = 2;// 不可见

	public static final int REGISTER = 1;// 注册

	public static final int SIGN = 2;// 签到

	public static final int INVEST = 3; // 投资

	public static final int RAFFLE = 4;// 抽奖

	public static final int EXCHANGE = 5;// 兑换

	public static final int EXCHANGE_RED = 6;// 兑换红包

	public static final int REPAY_SCORE = 7;// 还款

	public static final int BID_SCORE = 8;// 借款

	public static final String STR_REGISTER = "新用户注册";// 注册

	public static final String STR_SIGN = "每日签到";// 签到

	public static final String STR_INVEST = "投资"; // 投资

	public static final String STR_RAFFLE = "大转盘中奖";// 抽奖

	public static final String STR_EXCHANGE = "兑换实物";// 兑换

	public static final String STR_EXCHANGE_RED = "兑换红包";// 兑换

	public static final String STR_REPAY_SCORE = "还款";// 还款

	public static final String STR_BID_SCORE = "借款";// 借款

}
