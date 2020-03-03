package constants;

import play.Play;

import com.shove.Convert;

/**
 * 常量值
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-4-7 下午04:07:56
 */
public class Constants {
	
	/**
	 * 资金托管类型(环讯: HX; 汇付:HF)
	 */
	public static final String TRUST_FUNDS_TYPE = Play.configuration.getProperty("trust.funds.type");
	
	/**
	 * 环讯: HX
	 */
	public static final String TRUST_FUNDS_HX = "HX";
	
	/**
	 * 汇付:HF
	 */
	public static final String TRUST_FUNDS_HF = "HF";
	
	/**
	 * 富友:FY
	 */
	public static final String TRUST_FUNDS_FY = "FY";
	
	/**
	 * 普通网关，国付宝：1
	 */
	public static final int GATEWAY_GUO = 1;
	
	/**
	 * 普通网关，环迅：2
	 */
	public static final int GATEWAY_IPS = 2;
	
	/**
	 * 普通网关，通联：3
	 */
	public static final int GATEWAY_TONGLIAN = 3;
	/**
	 * 普通网关，宝付：4
	 */
	public static final int GATEWAY_BOFU = 4;
	
	
	public static final String BORROW_NO_PERFIX = "S";
	public static final String BORROW_NO_SUFFIX_NONG = "N";//农亿贷
	public static final String BORROW_NO_SUFFIX_XIN = "X";//信亿贷
	public static final String BORROW_NO_SUFFIX_FANG = "F";//房亿贷
	public static final String BORROW_NO_SUFFIX_MEI = "M";//亿美贷
	
	public static final String ROOT = Play.applicationPath.getAbsolutePath();
	
	public static final String BASE_URL = Play.configuration.getProperty("test.application.baseUrl") + Play.configuration.getProperty("http.path") + "/";
	public static final String SQL_PATH = Play.configuration.getProperty("sql.path");					//数据库备份文件路径
	public static final String HTTP_PATH = Play.configuration.getProperty("http.path");
	public static final String ENCRYPTION_KEY = Play.configuration.getProperty("fixed.secret");			//加密key
	public static final String APP_ENCRYPTION_KEY = Play.configuration.getProperty("app.fixed.secret");			//APP加密key
	public static final String TRUST_FUNDS = "true";			//资金托管模式
	public static final boolean IPS_ENABLE = Convert.strToBoolean(Play.configuration.getProperty("pay.trustFunds"), false);	//是否开启资金托管模式
	public static final boolean IS_MSG = Convert.strToBoolean(Play.configuration.getProperty("createBid_isMsg"), false);	//是否需要发送短信(针对环迅)
	public static final boolean IS_STINT_OF = Convert.strToBoolean(Play.configuration.getProperty("is_stint_of"), true); // 是否限制逾期总费
	public static final double OF_AMOUNT = Convert.strToDouble(Play.configuration.getProperty("of_amount"), 2.5); // 限制为多少倍
	public static final int WITHDRAWAL_DAY = Convert.strToInt(Play.configuration.getProperty("withdrawal_day"), 15); // 限制时间内的提款时间（单位：天）
	public static final int DEBT_SCALE = Convert.strToInt(Play.configuration.getProperty("debt_scale"), 30); // # 债权成交价是待收本金（债权面额）的上下百分比
	public static final String URL_IP_LOCATION = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json";//查询ip地址接口
	
	public static final String SUPERVISOR_INITIAL_PASSWORD = "123456";
	
	public static final String ERROR_PAGE_PATH_FRONT = "@Application.errorFront";
	public static final String ERROR_PAGE_PATH_SUPERVISOR = "@Application.errorSupervisor";
	public static final String ERROR_PAGE_PATH_INJECTION = "@Application.injection";
	public static final String COOKIE_KEY_SUPERVISOR_ID = "something";
	public static final String COOKIE_KEY_SUPERVISOR_ACTION = "supervisor";
	public static final String COOKIE_KEY_USER_ID = "sp2p6";
	public static final String COOKIE_KEY_USER_ACTION = "front";
	public static final String CACHE_TIME_HOURS_12 = "12h";
	public static final String CACHE_TIME_HOURS_144 = "144h";
	public static final String CACHE_TIME_MINUS_30 = "30min";
	public static final String CACHE_TIME_SECOND_60 = "60s";
	public static final String CACHE_TIME_MINUS_2 = "2min";
	public static final String LOCALHOST_IP = "127.0.0.1";
	public static final int PAGE_SIZE = 10;
	public static final int PAGE_SIZE_EIGHT = 8;
	public static final int PAGE_POSTS_SIZE = 3;
	public static final int PAGE_POSTS_SHOW_SIZE = 5;
	public static String PAGE_POSTS_SHOW_SIZESTR = "8";
	
	public static final String API_KEY = Play.configuration.getProperty("api_key");
	public static final String SECRET_KEY = Play.configuration.getProperty("secret_key");
	
	/**
	 * 不同支付平台差异性融合, 各托管在各自常理类中初始化
	 */
	public static   boolean DEBT_USE = true;				//是否有债权转让，默认普通网管配置
	public static   boolean IS_DEBT_TWO = false;			//是否支持二次债权转让
	public static   boolean IS_LOGIN = false;				//是否需要登录
	public static   boolean IS_SECOND_BID = true;			//是否有秒还标
	public static   boolean IS_FLOW_BID = true;			//是否有自动流标
	public static   boolean IS_WITHDRAWAL_AUDIT;	//提现后是否需要审核
	public static   boolean IS_WITHDRAWAL_INNER;	//是否支持提现内扣
	public static   boolean IS_GUARANTOR;			//是否需要登记担保方
	public static   boolean IS_OFFLINERECEIVE = true;		//是否支持本金垫付、线下收款
	public static   boolean IS_LOCALHOST = Convert.strToBoolean(Play.configuration.getProperty("is.localhost"), true);//是否本地测试，true：测试，false：正式环境
	
	/**
	 * 投资人手续费率上限50%（借款管理费）；借款人手续费率上限100%（理财管理费）；债权转让手续费率10%；单笔交易商户收取服务费最大费率1%（最大提现手续费率）
	 * @author yaoyi version:8.1.2  date:20150908     add begin
	 */
	public static   double  BORROW_MANAGE_MAXRATE = 50.0;       //最大借款管理费率
	public static   double  DEBT_TRANSFER_MAXRATE = 100.0;       //最大债权转让费率
	public static   double  WITHDRAW_MAXRATE = 1.0;       //最大提现手续费率
	/**
	 * end
	 */
	
	public static final int JOB_EMAIL_AMOUNT = 40;  // 邮件定时发送数量
	public static final int JOB_MSG_AMOUNT = 200;   // 短信定时发送数量
	public static final int JOB_STATION_AMOUNT = 100; // 站内信定时发送数量
	
	public static final boolean DEV_PROD = Play.configuration.getProperty("application.mode").equals("dev") ? true : false;
	public static final boolean CHECK_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_code"), false);//是否需要检验普通验证码
	public static final boolean CHECK_MSG_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_msg_code"), false);//是否需要检验短信验证码
	public static final boolean CHECK_PIC_CODE = Convert.strToBoolean(Play.configuration.getProperty("check_pic_code"), false);//是否需要检验图形验证码
	
	public static final boolean SMS_MSG_CODE = Convert.strToBoolean(Play.configuration.getProperty("sms_code_login"), false);//是否需要短信验证码登录
	/**
	 * 加密串有效时间(s)
	 */
	public static final int APP_VALID_TIME = 604800;
	public static final int VALID_TIME = 5400;
	public static final String SHOW_BOX = "show_box"; // 一些弹框的sign加密value
	
	/**
	 * 是否启用密码错误次数超限锁定(0不启用，1启用)
	 */
	public static final int CLOSE_LOCK = 0;
	public static final int OPEN_LOCK = 1;
	
	/**
	 * 缓存时间
	 */
	public static final String CACHE_TIME = "30min";
	
	/**
	 * 邮件发送标识
	 */
	public static final String ACTIVE = "active";
	public static final String PASSWORD = "resetPassword";
	public static final String SECRET_QUESTION = "secretQuestion";
	
	/**
	 * 部分加密action标识
	 */
	public static final String BID_ID_SIGN = "b"; // 标ID
	public static final String BILL_ID_SIGN = "bill"; // 标ID
	public static final String PRODUCT_ID_SIGN = "p"; // 产品ID
	public static final String USER_ID_SIGN = "u"; // 用户ID
	public static final String SUPERVISOR_ID_SIGN = "supervisor_id"; // 管理员ID
	public static final String ITEM_ID_SIGN = "i"; // 资料ID
	public static final String USER_ITEM_ID_SIGN = "ui"; // 用户资料ID
	public static final String RID_ID_SIGN = "red_id"; // 红包ID
	/**
	 * ajax标识
	 */
	public static final String IS_AJAX = "1";
	
	/**
	 * 进行了加密的方法
	 */
	/*修改手机*/
	public static final String VERIFY_SAFE_QUESTION = "front.account.BasicInformation.verifySafeQuestion";
	public static final String SET_SAFE_QUESTION = "front.account.BasicInformation.resetSafeQuestion";
	public static final String PASSWORD_EMAIL = "front.account.LoginAndRegisterAction.resetSafeQuestion";
	
	/**
	 * 固定的路径
	 */
	public static final String LOGIN = BASE_URL + "login";
	public static final String QUICK_LOGIN = BASE_URL + "quick/login";
	public static final String RESET_PASSWORD_EMAIL = BASE_URL + "front/account/resetPassword?sign=";
	public static final String RESET_PAY_PASSWORD_EMAIL = BASE_URL + "front/account/resetDelPassword?sign=";
	public static final String RESET_QUESTION_EMAIL = BASE_URL + "front/account/resetQuestion?sign=";
	public static final String ACTIVE_EMAIL = BASE_URL+"front/account/accountActivation?sign=";
	
	/**
	 * 环迅
	 */
	public static final long IPS_GATEWAY = 2;
	public static final String IPS_MERCHANT_URL = BASE_URL + Play.configuration.getProperty("ips_merchant_url", "");
	public static final String IPS_SERVER_URL = BASE_URL + Play.configuration.getProperty("ips_server_url", "");
	public static final String IPS_URL = Play.configuration.getProperty("ipsURL", "");
	
	/**
	 * 国付宝
	 */
	public static final long GO_GATEWAY = 1;
	public static final String GO_MER_URL = BASE_URL + Play.configuration.getProperty("go_mer_url", "");
	public static final String GO_MER_BACK_URL = BASE_URL + Play.configuration.getProperty("go_mer_back_url", "");
	public static final String GO_URL = Play.configuration.getProperty("go_url", "");
	public static String GO_SERVER_TIME_URL = Play.configuration.getProperty("gopay_server_time_url", "");
	public static final String TRANCODE = "8888"; //支付
	public static final String RETURN_CODE = "4010"; //退款
	public static final String TRAN_QUERY_CODE = "4020"; //单笔交易查询交易
	public static final String CURRENCYTYPE = "156"; //人民币
	public static final String VERFICATION_CODE = "";
	
	public static final boolean TRUE = true;
	public static final boolean FALSE = false;

	public static final boolean ENABLE = true; // 启用
	public static final boolean NOT_ENABLE = false; // 不启用
	public static final int YEAR = -1;// 年
	public static final int MONTH = 0; // 月
	public static final int DAY = 1; // 日

	public static final int _ONE = -1;
	public static final int _TWO = -2;
	public static final int _THREE = -3;
	public static final int _FOUR = -4;
	public static final int _FIVE = -5;
	public static final int _SIX = -6;
	public static final int _SEVEN = -7;
	public static final int _EIGHT = -8;
	public static final int _NINE = -9;
	public static final int _TEN = -10;

	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;
	public static final int SIX = 6;
	public static final int SEVEN = 7;
	public static final int EIGHT = 8;
	public static final int NINE = 9;
	public static final int TEN = 10;
	public static final int FIFTEEN = 15;

	public static final int INSERT = 1;
	public static final int DELETE = 2;
	public static final int UPDATE = 3;
	public static final int SELECT = 4;

	public static final String MAN = "男";
	public static final String WOMAN = "女";
	public static final String UNKNOWN = "未知";

	public static final int FRONT = 0;// 前台
	public static final int ADMIN = 1;// 后台

	public static final int ADD = 1; // 增加
	public static final int MINUS = -1; // 减少
	
	/**
	 * 增加信用积分记录的类型
	 */
	public static final int AUDIT_ITEM = 1;
	public static final int PAYMENT = 2;
	public static final int BID = 3;
	public static final int INVEST = 4;
	public static final int OVERDUE = -1;
	
	/**
	 * 借款标产品
	 */
	public static final int GENERAL_BID = 0; // 普通
	public static final int CREDIT_BID = 1; // 信用
	public static final int NET_VALUE_BID = 2; // 净值
	public static final int S_REPAYMENT_BID = 3; // 秒还

	public static final boolean IS_AGENCY = true; // 合作机构
	public static final boolean NOT_IS_AGENCY = false; // 非合作机构
	
	public static final boolean NEED_TRANSACTION_PASSWORD = true; // 需要交易密码
	public static final boolean NOT_TRANSACTION_PASSWORD = false; // 不需要交易密码
	public static final boolean NEED_AUDIT = true; // 必选资料
	public static final boolean NOT_NEED_AUDIT = false; // 可选资料
	public static final int PAID_MONTH_EQUAL_PRINCIPAL_INTEREST = 1; // 按月还款、等额本息
	public static final int PAID_MONTH_ONCE_REPAYMENT = 2; // 按月付息、一次还款
	public static final int ONCE_REPAYMENT = 3; // 一次还款
	//public static final int SECOND_TIME_REPAYMENT = 4;// 秒还还款
	public static final int USER_UPLOAD = 0; // 用户上传(图片)
	public static final int PLATFORM_UPLOAD = 1; // 平台上传(图片)
	public static final int PRODUCT_INTRODUCTION = 1; // 借款产品简介
	public static final int PRODUCT_DETAIL_INTRODUCTION = 2; // 借款产品详细描述
	public static final int APPLICANT_CONDITION = 3; // 申请人条件
	public static final int DAY_INTEREST = 360; // 日利息结算常量
	
	public static final int PC = 1; // PC端
	public static final int APP = 2; // APP端
	public static final int WECHAT = 3; // 微信端

	public static final String[] INIT_LABLES = { "借款产品简介", "借款产品详情描述", "申请人条件" };
	public static final int INUT = 1; // 单行文本框
	public static final int INUTS = 2; // 多行文本框
	
	public static final int HOME_SHOW_AMOUNT = 5; // 首页显示产品数量
	public static final int HOME_SHOW_AMOUNT_APP = 3;  //APP首页显示产品数量
	public static final int MAX_VALUE = 50000000; // 上限
	public static final int MIN_INTEREST_RATE = 1; // 产品利率下限
	public static final int MAX_INTEREST_RATE = 24; // 产品利率上限
	public static final String AGENCY_NAME_REPEAT = "机构名重复!"; // 机构名称重复(切记和JS对应)
	public static final String SEAL_NAME_REPEAT = "营业执照号重复!"; // 印章名称重复(切记和JS对应)
	
	/**
	 * 审核资料
	 */
	public static final int PICTURE_FILE = 1; // 图片文件
	public static final int NORMAL_FILE = 2; // 文本文件
	public static final int VIDEO_FILE = 3; // 视频文件
	public static final int SOUND_FILE = 4; // 音频文件
	public static final int TABLE_FILE = 5; // 表格文件
	
	public static final int PICTURE_SIZE = 10 * 1024 * 1024; // 图片限制大小(10M左右)
	public static final int TXT_SIZE = 5 * 1024 * 1024; // 文本限制大小(5M左右)
	public static final int VIDEO_SIZE = 100 * 1024 * 1024; // 视频限制大小(100M左右)
	public static final int AUDIO_SIZE = 100 * 1024 * 1024; // 音频限制大小(100M左右)
	public static final int XLS_SIZE = 5 * 1024 * 1024; // 表格限制大小(5M左右)
	
	/**
	 * 超额借款
	 */
	public static final String OVER_LOAN_NO_THROUGH = "未通过"; //超额借款审核不通过

	/**
	 *账单
	 */
	public static final String AUTO_PAYMENT = "0"; // 自动付款
	public static final String HANDLE_PAYMENT = "1"; // 手动付款
	
	public static final int NO_REPAYMENT = -1; // 未还款
	public static final int NORMAL_REPAYMENT = 0; // 正常还款
	
	/**本金垫付还款**/
	public static final int ADVANCE_PRINCIIPAL_REPAYMENT = -2;
	public static final int OVERDUE_PATMENT = -3; // 逾期还款

	public static final int NO_RECEIVABLES = -1;// 未收款
	public static final int OVERDUE_NORECEIVABLES = -2;// 逾期未收款
	public static final int NORMAL_RECEIVABLES = 0;// 正常收款
	public static final int ADVANCE_PRINCIIPAL_RECEIVABLES = -3;// 本金垫付收款
	public static final int OVERDUE_RECEIVABLES = -4;// 逾期收款
	public static final int FOR_PAY = -5;// 待付款  
	public static final int FOR_OVERDUE_PAY = -6;// 逾期待付款
	public static final int FOR_DEBT_MARK = -7;// 债券转让
	public static final int FOR_DEBT_TRANSFERING = -8;// 转让中

	public static final int BILL_NORMAL_OVERDUE = -1;// 账单系统标记逾期
	public static final int BILL_NO_OVERDUE = 0;// 账单未标记逾期
	public static final int BILL_OVERDUE = -2;// 账单标记逾期
	public static final int BILL_BAD_DEBTS = -3;// 账单标记坏账

	public static final int STATAIONLETTER_OVERDUE = 1;// 发站内信催收
	public static final int MAIL_OVERDUE = 2;// 发邮件催收
	public static final int TELEPHONE_OVERDUE = 3;// 电话通知催收

	/**
	 * 标
	 */
	public static final int BALANCE_PAY_ENOUGH = -998; // 资金足够支付
	
	/**资金不够**/
	public static final int BALANCE_NOT_ENOUGH = -999;
	public static final int BAIL_NOT_ENOUGH = -1000; // 发标保证金不够
	public static final int APPLY_NOW_INDEX = 1; // 首页立即申请
	public static final int APPLY_NOW_DETAIL = 2; // 详情立即申请
	public static final int INVEST_DETAIL = 3; //投标详情
	public static final double LOWEST_AMOUNT = 0.01; // 在出现金额算法不匹配的情况下,入库基准数
	
	public static final int NOT_REWARD = 0; // 不奖励
//	public static final int FIXED_AMOUNT_REWARD = 1; // 固定金额奖励  
//	public static final int PROPORTIONATELY_REWARD = 2; // 按比例奖励
	public static final boolean AUTOMATIC_BID = true; // 自动投标
	public static final boolean NOT_AUTOMATIC_BID = false; // 不自动投标
	public static final int AUDIT_RELEASE = 0; // 先审后发
	public static final int RELEASE_AUDIT = 1; // 先发后审
	public static final int RELEASE_AND_AUDIT = 2; // 边发边审
	public static final int BID_COUNT = 5;// 首页显示最新未满标的数量
	public static final int COLLECT = 1; // 收藏
	public static final int NOT_COLLECT = 0;// 未收藏
	
	/**审核中**/
	public static final int BID_AUDIT = 0;
	
	/**提前借款**/
	public static final int BID_ADVANCE_LOAN = 1;
	
	/**借款中(审核通过)**/
	public static final int BID_FUNDRAISE = 2; 
	
	/**待放款(放款审核通过)**/
	public static final int BID_EAIT_LOAN = 3;	
	
	/**还款中(已放款)**/
	public static final int BID_REPAYMENT = 4;
	
	/**已还款**/
	public static final int BID_REPAYMENTS = 5;
	
	/**审核中待验证**/
	public static final int BID_AUDIT_VERIFY = 10;
	
	/**提前借款待验证**/
	public static final int BID_ADVANCE_LOAN_VERIFY = 11;
	
	/**借款中待验证**/
	public static final int BID_FUNDRAISE_VERIFY = 12; 
	
	/**本金垫付还款中(已放款)**/
	public static final int BID_COMPENSATE_REPAYMENT = 14; 
	
	/**审核中待支付投标奖励**/
	public static final int BID_AUDIT_BONUS = 20;
	
	/**前提借款待支付投标奖励**/
	public static final int BID_ADVANCE_LOAN_BONUS = 21;
	
	/**借款中待支付投标奖励**/
	public static final int BID_FUNDRAISE_BONUS = 22; 
	
	/**审核不通过**/
	public static final int BID_NOT_THROUGH = -1; 
	
	/**借款中不通过**/
	public static final int BID_PEVIEW_NOT_THROUGH = -2;
	
	/**放款不通过**/
	public static final int BID_LOAN_NOT_THROUGH = -3;  
	
	/**流标**/
	public static final int BID_FLOW = -4; 
	
	/**撤销**/
	public static final int BID_REPEAL = -5;
	
	/**未验证**/
	public static final int BID_NOT_VERIFY = -10;// 未验证
	
	
	
	/**
	 * 已放款，包含：BID_REPAYMENT（4.还款中）、BID_REPAYMENTS（5.已还款）、BID_COMPENSATE_REPAYMENT（14.本金垫付还款中）
	 */
	public static final int BID_RELEASED = 100;
	//public static final int[] BID_AUDIT_STATUS = { 0, 1, 2, 3, 4 }; // 正向状态值
	//public static final int[] BID_FAIL_STATUS = { -1, -2, -3, -4, -5 }; // 负向状态值
	
	/**
	 *  上一个，下一个列表标示项
	 */
	/* 标的 */
	public static final int BID_SHZ = 1; //1.审核中。
	public static final int BID_JKZ = 2; //2.借款中。
	public static final int BID_MBZ = 3; //3.满标。
	public static final int BID_DFK = 4; //4.待放款。
	public static final int BID_YFK = 5; //5.已放款。
	public static final int BID_HKZ = 6; //6.还款中。
	public static final int BID_YQZ = 7; //7.逾期的。
	public static final int BID_CGZ = 8; //8.已完成。
	public static final int BID_SBZ = 9; //9.失败的。
	public static final int BID_HZZ = 10; //10.坏账的。
	public static final int BID_HZZG = 300; //合作机构。
	/* 账单催收 */
	public static final int BID_WD_BYZD = 11; //11.我的本月到期账单。
	public static final int BID_WD_YQZD = 12; //12.我的逾期账单。
	public static final int BID_WD_YHZD = 13; //13.我的已还款账单。
	public static final int BID_BYZD = 14; //11.部门本月到期账单。
	public static final int BID_YQZD = 15; //12.部门逾期账单。
	public static final int BID_YHZD = 16; //13.部门已还款账单。
	public static final int BID_WD_HKZ = 106; //106.我的还款中
	public static final int BID_WD_CGZ = 108; //108.我的已完成
	public static final int BID_WD_HZZ = 110; //110.我的坏账的。
	public static final int BID_HKZ2 = 206; //106.部门还款中
	public static final int BID_CGZ2 = 208; //108.部门已完成
	public static final int BID_HZZ2 = 210; //110.部门坏账的。
	public static final int BID_YFPHY = 211; //110.部门已分配的借款会员。
	public static final int BID_YFPB = 212; //110.部门已分配的借款标。
	/* 财务 */
	public static final int BID_YS_DSZD = 400; //400.待收款借款账单。
	public static final int BID_YS_ZD = 401; //401.逾期账单列表。
	public static final int BID_YS_YSZD = 402; //402.已收款借款账单列表。
	public static final int BID_YS_CGZD = 403; //403.已完成借款标列表。
	public static final int BID_YF_DFK = 500; //500.待付款理财账单列表。
	public static final int BID_YF_YQWFK = 501; //501.逾期未付理财账单列表。
	public static final int BID_YF_YFK = 502; //502.已付款理财账单列表。
	public static final int BID_YF_BJDF = 503; //503.本金垫付理财账单列表。
	public static final int BID_YF_HZ = 504; //504.坏账借款标列表。
	
	public static final int V_AUDITING = 0;// 审核中
	public static final int V_FUNDRAISEING = 1;// 借款中
	public static final int V_FULL = 2;// 满标
	public static final int V_REPAYMENTING = 3;// 还款中
	public static final int V_OVERDUE = 4;// 逾期
	public static final int V_REPAYMENT = 5;// 已完成
	public static final int V_BAD = 6;// 坏账
	public static final int V_NOT_THROUGH = 7;// 失败

	public static final boolean AUDIT_PASS = true; // 审核通过
	public static final boolean AUDIT_NOT_PASS = false; // 审核不通过
	
	public static final int NOT_QUALITY_BID = 0;// 非优质标
	public static final int QUALITY_BID = 1;// 优质标
	public static final int NOT_HOT_BID = 0;// 非"火"标
	public static final int HOT_BID = 1;// "火"标

	public static final int AUDIT_SCALE_ALL = 0;// 审核机制审核比例模式 0.全部、1.必须、2.可选
	public static final int AUDIT_SCALE_NEED = 1;
	public static final int AUDIT_SCALE_PICK = 2;
	
	//public static final int NEED = 1; // 必须/启用状态
	//public static final int NOT_NEED = 0; // 可选/未启用状态
	//public static final int ALL = 10; // 全部
	
	public static final int FULL_BID_COUNT = 5; // 满标显示数量值
	public static final int QUALITY_BID_COUNT = 2; // 优质标显示数量
	public static final int FULL_REMIND_BID_COUNT = 3; // 我的账户提醒满标倒计时数量
	public static final int RECENTLY_REPAYMENT_BILL_COUNT = 3; // 账单提醒最近还款账单数量
	public static final int YEAR_PERIOD_LIMIT = 5; // 年借款期限，限制
	public static final int HOME_BID_COUNT = 3; // 首页借款标限制
	public static final int NEW_FUNDRAISEING_BID = 3; // 最新未满标限制数量
	
	public static final int SEARCH_ALL = 0; // 全部搜索
	public static final int SEARCH_ALL_TEN = 10; // 全部搜索(资料)
	
//	public static final int BID_SEARCH_TITLE = 1; // 标题搜索(标)
//	public static final int BID_SEARCH_ID = 2; // 编号搜索(标)
	
	public static final int AGENCY_SEARCH_NAME = 1; // 名称搜索(合作机构)
	public static final int AGENCY_SEARCH_ID = 2; // 编号搜索(合作机构)
	
	public static final int ITEM_SEARCH_NAME = 1; // 名称搜索(会员资料审核)
	public static final int ITEM_SEARCH_EMAIL = 2; // 邮箱搜索(会员资料审核)
	
	public static final int ITEMS_SEARCH_NAME = 1; // 名称搜索(资料库)
	public static final int ITEMS_SEARCH_ID = 2; // 邮箱搜索(资料库)
	
	/* app每页的条数 */
	public static final int APP_PAGESIZE = 18;  
	public static final String APP_PAGESIZE2 = "18"; //字符串类型
	
	/* 会员状态 */
	public static final int SUCCESS_STATUS = 1; // 有效
	public static final int NOT_LOGIN = -1; // 未登录
	public static final int NOT_EMAILVERIFIED = -2; // 未激活
	public static final int NOT_ADDBASEINFO = -3; // 未完成基本资料
	public static final int NOT_SETDEALPWD = -4; // 未设置交易密码
	public static final int NOT_REPAY_AUTH = -5; // 未自动还款签约
	public static final int NOT_IPS_OPEN = -6; // 资金托管未开户
	
	/*微信中根据该数字索引查询所有借款标*/
	public static final String WECHAT_BID_SEARCH = "0";
	
	/*微信中根据该数字索引查询我的借款账单,我的理财账单*/
	public static final int WECHAT_BILL_SEARCH = 2;
	
	/*微信中根据该数字索引查询我的理财标*/
	public static final String WECHAT_INVEST_BID_SEARCH = "0";
	
	/* 所有标相关的下拉搜索数组  */
	public static final String[] BID_SEARCH = { 
		" AND (user_name LIKE ? OR bid_no LIKE ? or product_name LIKE ? or title LIKE ? )",
		" AND bid_no LIKE ?", " AND title LIKE ?", " AND user_name LIKE ?", 
		" AND email LIKE ?" , " AND product_name LIKE ?"};
	
	/* 所有标相关的排序数组 */
	public static final String[] BID_SEARCH_ORDER = {
		" ORDER BY id desc ",
		" ORDER BY (CASE user_item_count_true WHEN 0 then (user_item_count_true + 1) else (user_item_count_true/product_item_count + 1) end)",
		" ORDER BY time ",
		" ORDER BY amount ", " ORDER BY order_sort ", " ORDER BY loan_schedule ",
		" ORDER BY apr ", " ORDER BY product_id ", " ORDER BY mark_overdue_time ", 
		" ORDER BY overdue_count ", " ORDER BY last_repay_time ", " ORDER BY mark_bad_time ",
		" ORDER BY audit_time ", " ORDER BY real_invest_expire_time "};
	
	/* 会员资料审核排序数组  */
	public static final String[] ITEMS_SEARCH_ORDER = {
		" ORDER BY time desc", " ORDER BY sum_count ", " ORDER BY audited_count ", 
		" ORDER BY not_pass_count ", " ORDER BY auditing_count "};

	public static final int LOAN_USER = 1;
	public static final int INVEST_USER = 2;
	public static final int LOAN_INVEST_USER = 3;
	
	/**
	 * 邮件模板场景
	 */
	public static final String FIND_USERNAME = "找回用户名";
	public static final String ACTIVATE_USER = "注册激活";
	public static final String RESET_PASSWORD = "重置密码";
	public static final String RESET_SECRET_QUESTION = "重置安全问题";

	/**
	 * 邮件模板中被替换的词
	 */
	public static final String EMAIL_NAME = "name";
	public static final String EMAIL_EMAIL = "email";
	public static final String EMAIL_LOGIN = "login";
	public static final String EMAIL_URL = "url";
	public static final String EMAIL_TELEPHONE = "telephone";
	public static final String EMAIL_TIME = "time";
	public static final String EMAIL_PLATFORM = "platform";
	
	/**
	 * 债权
	 */

	public static final int AUCTION_MODE = 2; // 竞价模式
	public static final int DIRECTIONAL_MODE = 1; // 定向模式
	
	public static final int AUCTION_STATUS = 1; // 竞价状态
	public static final int TRANSFER_STATUS = 0; // 可转让状态 
	public static final int PENDING_AUDIT_STATUS = 0; // 待审核状态 
	
	public static final int INVEST_NORMAL = 0; // 正常状态
	public static final int INVEST_HAS_TRANSFER = -1; // 已转让出
	public static final int INVEST_TRANSFERING = 1; // 转让中
	
	public static final int DEBT_AUDITING = 0; // 审核中
	public static final int DEBT_NOPASS = -1; // 审核不通过
	public static final int DEBT_AUCTIONING = 1; // 竞拍中
	public static final int DEBT_ACCEPT = 2; // 待接受
	public static final int DEBT_SUCCESS = 3; // 已成功
	public static final int DEBT_FLOW = -2; // 流拍
	public static final int DEBT_REFUSE = -3; // 拒绝接受
	public static final int DEBT_REPAY = -5; // 还款撤销
	public static final int WAIT_CONFIRM = 4; // 等待确认
	
	public static final int AUCTION_DETAIL_NORMAL = 0; // 正常状态
	public static final int AUCTION_DETAIL_FAILURE = -1; // 失败
	public static final int AUCTION_DETAIL_DEAL = 1; // 成交
	public static final int AUCTION_DETAIL_WAIT_ACCEPT = 2; // 待接受
	public static final int AUCTION_DETAIL_WAIT_CONFIRM = 3; // 待接受
	

	/**
	 * 用户对应审核资料
	 */
	public static final int UNCOMMITTED = 0; // 未提交
	public static final int AUDITING = 1; // 审核中
	public static final int AUDITED = 2; // 已通过审核
	public static final int EXPIRED = 3; // 过期失效
	public static final int UPLOAD = 4; // 资金托管上传
	public static final int NOT_PASS = -1; // 未通过审核
	
	/**
	 * 提现
	 */
	public static final int WITHDRAWAL_CHECK_PENDING = 0; //待审核
	public static final int WITHDRAWAL_PAYING = 1; //付款中 
	public static final int WITHDRAWAL_SPEND = 2; //已付款
	public static final int WITHDRAWAL_NOT_PASS = -1; //未通过 
	
	public static final int WITHDRAWAL_PAY_TYPE = 2;  //平台账户提现方式    1.外扣、2.内扣
	
	/**
	 * 安全问题答案的格式
	 */
	public static final String TYPE_TEXT = "文本";
	public static final String TYPE_DATE = "日期";
	public static final String TYPE_NUBMER = "数字";
	
	/**
	 * 分页主题
	 */
	public static final int PAGE_SIMPLE = 1;
	public static final int PAGE_ASYNCH = 2;
	
	/**
	 * 默认风格
	 */
	public static final int PAGE_STYLE_DEFAULT = 1;
	
	/**
	 * 前台风格
	 */
	public static final int PAGE_STYLE_FOREGROUND = 2;
	
	/**
	 * 前台混合风格
	 */
	public static final int PAGE_STYPE_MIXED = 3;
	
	/**
	 * 后台风格
	 */
	public static final int PAGE_STYLE_BACKSTAGE = 4;
	
	/**
	 * 系统管理员supervisor
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public static class SystemSupervisor {
		public static final long ID = 1;
	}
	
	/**
	 * 超级管理员组
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class SystemSupervisorGroup {
		public static final long ID = 1;
		public static final String NAME = "超级管理员组";
	}

	/**
	 * 数据库操作类型
	 * @author lzp
	 * @version 6.0
	 * @created 2014-7-22
	 */
	public class DBOperationType {
		public static final int CLEAR = 0;
		public static final int RESET = 1;
		public static final int RECOVER = 2;
		public static final int BACKUP = 3;
	}
	
	/**
	 * 删除类型
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class DeleteType {
		public static final int DELETE = 1; //删除
		public static final int DELETE_COMPLETELY = 2; //彻底删除
	}
	
	/**
	 * 阅读状态
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class ReadStatus {
		public static final int Readed = 1; //已读
		public static final int Unread = 2; //未读
	}
	
	/**
	 * 消息查询关键字类型
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class MessageKeywordType {
		public static final int Title = 1; //标题
		public static final int SenderName = 2; //发信人
	}
	
	/**
	 * 用户组
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class UserGroupType {
		/*全部会员*/
		public static final int ALL_USERS = -1;
		/*全部借款会员*/
		public static final int LOAN_USERS = -2;
		/*全部理财会员*/
		public static final int INVEST_USERS = -3;
		/*全部复合会员*/
		public static final int COMPOSITE_USERS = -4;
		/*全部未激活会员*/
		public static final int NO_ACTIVATE_USERS = -5;
		/*全部黑名单会员*/
		public static final int BLACKLIST_USERS = -6;
		/*一周内注册新会员*/
		public static final int NEW_USERS = -7;
		
		/*自定义会员*/
		public static final long CUSTOM_USERS = -10;
	}
	
	
	
	/**
	 * 新闻类别
	 * @author lzp
	 * @version 6.0
	 * @created 2014-6-9
	 */
	public class NewsTypeId {
		public static final int WEALTH_INFOMATION = 1;
		public static final int HELP_CENTER = 2;
		public static final int COMPANY_DESCRIPTION = 3;
		public static final int PRINCIPAL_PROTECTION = 4;
		public static final int GETTINT_STARTED = 5;
		public static final int PLATFORM_AGREEMENT = 6;
		public static final int OFFICIAL_AMMOUNCEMENT = 7;
		public static final int INTERNET_BANKING = 8;
		public static final int LOAN_MONPOLY = 9;
		public static final int BORROWING_TECHNIQUES = 10;
		public static final int MONEY_TIPS = 11;
		public static final int SUCCESS_STROY = 12;
//		public static final int FREQUENTLY_QUESTIONS = 13;
		public static final int COMPANY_DESCRIPTION2 = 16;
		public static final int MANAGEMENT_TEAM = 17;
		public static final int EXPERT_ADVISOR = 18;
		public static final int CAREERS = 19;
		public static final int PARTNER = 20;
		public static final int PRINCIPAL_PLAN = 21;
		public static final int PRINCIPAL_RULE = 22;
		public static final int PAYOUT_PROCESS = 23;
		public static final int INVESTMENT_STRATEGY = 24; 
		public static final int PRINCIPAL_PROTECTION_FAQ = 25;
		public static final int GETTING_STARTED = 26;
		public static final int CREDIT_HELP = 27;
		public static final int PRIVACY_POLICY = 28;
		public static final int SERVICE_FEES = 29;
		public static final int SERVICE_AGREEMENT = 30;
		public static final int REGISTER_AGREEMENT = 31;
		public static final int SERVICE_TERMS = 32;
		
		/* 首页页面固定数据匹配ID */
		public static final long SERVICE_CLAUSE = -1001; // 服务条款
		public static final long CREDIT_EXPLAIN = -1002L; // 信用说明
		public static final long PRIVACY_EXPLAIN = -1003L; // 隐私说明
		public static final long COMPANY_INTRODUCE = -1004L; // 公司介绍
		public static final long MANAGE_GROUP = -1005L; // 管理团队
		public static final long SPECIALIST_COUNSELOR = -1006L; // 专家顾问
		public static final long INVITE_TALENTS = -1007L; // 招贤纳士
		public static final long MANAGE_PARTER = -1008L; // 合作伙伴
		public static final long VIP_AGREEMENT = -1009L; // vip会员协议
		public static final long REGISTER_AGREEMENT2 = -1010L; // 注册协议
		public static final long INSUR_PROTOCOL_CLAUSE = -2000L; // 保险条款
		public static final long INSUR_PROTOCOL_FLOW = -2001L; // 理赔流程
		public static final long PUBLIC_BENEFIT = -2002L; // 亿亿公益
		public static final long DEBT_AGREEMENT= 210L; // 用户债权协议
		public static final long ABOUT_YIYI= -1012L; // 关于亿亿
		
		public static final long GUARANTEE_ID=-1020;//安全保障
		public static final long RISK_WARNING_ID=-1021;//风险提示
		public static final long DEBIT_CREDIT_ID=-1022;//借款协议
		public static final long DISCLOSURE_COMMITMENT_ID=-1023;//风险揭示书及禁止行为承诺
		public static final long SIGNATURE_AUTHORIZATION_ID=-1024;//电子签章自动签署授权协议
		public static final long LENDER_SERVICE_ID = -1025;//出借人服务协议
		public static final long BORROWER_SERVICE_ID = -1026;//借款人服务协议
		public static final long CONSULT_MANAGE_ID = -1027;//咨询与管理服务协议
		
		public static final long BORROW_STRATEGY_ID = -1030;//借款攻略
		public static final long CREDIT_LOAN_ID = -1031;//信亿贷介绍
		public static final long HOUSE_LOAN_ID = -1032;//房亿贷介绍
		public static final long AGRICULTURE_LOAN_ID = -1033;//农亿贷介绍
//		public static final int SPECIALIST_COUNSELOR = 1006; // 专家顾问
		
		//亿美贷协议
		public static final long YMD_SERVIVE_COST_ID = -3001; //亿美贷服务费协议
		public static final long YMD_DEBIT_CREDIT_ID = -3002; //亿美贷借款协议
		
		public static final long AOTO_PROTOCOL_ID = -4001; //亿美贷借款协议
		
		
	}
	
	/**
	 * 管理员等级
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class SupervisorLevel {
		public static final int Normal = 0; //普通管理员
		public static final int Super = 1;  //超级管理员
	}
	
	/**
	 * 性别
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class Sex {
		public static final int Man = 1;
		public static final int Woman = 2;
		public static final int Unknown = 3;
	}
	
	/**
	 * 权限
	 * @author lzp
	 * @version 6.0
	 * @created 2014-5-27
	 */
	public class Right {
		/**
		 * 管理首页
		 */
		public static final int Home = 1;
		
		/**
		 * 网站内容管理
		 */
		/*平台内容管理*/
		public static final int PLATFORM_CONTENT = 21;
		/*广告内容管理*/
		public static final int ADS_CONTENT = 22;
		/*站内信管理*/
		public static final int STATION_LETTER= 23 ;
		/*借款标产品管理*/
		public static final int BID_PRODUCT = 24;
		/*系统通知模板管理*/
		public static final int SYSTEM_NOTICE_TEMPLATE = 25;

		/**
		 * 借款标管理
		 */
		/*平台借款标管理*/
		public static final int PLATFORM_BID = 41;
		/*机构合作标管理*/
		public static final int AGENCY_BID = 42;
		/*债权转让管理*/
		public static final int DEBT_TRANSFER = 43;
		/*会员借款资料审核管理*/
		public static final int USER_LOAN_SUBJECT = 44;
		/*超额借款管理*/
		public static final int OVER_LOAN = 45;
		
		/**
		 * 账单催收管理
		 */
		/*我的会员账单*/
		public static final int MY_USER_BILL = 61;
		/*部门账单管理*/
		public static final int DEPARTMENT_BILL = 62;
		
		/**
		 * 会员管理
		 */
		/*全部会员列表*/
		public static final int ALL_USER_LIST = 81;
		/*借款会员列表*/
		public static final int LOAN_USER_LIST = 82;
		/*理财会员列表*/
		public static final int INVEST_USER_LIST = 83;
		/*复合会员列表*/
		public static final int COMPOSITE_USER_LIST = 84;
		/*VIP会员列表*/
		public static final int VIP_USER_LIST = 85;
		/*CPS会员列表*/
		public static final int CPS_USER_LIST = 86;
		/*未激活会员列表*/
		public static final int NO_ACTIVATE_USER_LIST = 87;
		/*已锁定会员列表*/
		public static final int LOCKED_USER_LIST = 88;
		/*被举报会员列表*/
		public static final int REPORTED_USER_LIST = 89;
		/*黑名单会员列表*/
		public static final int BLACKLIST_USER_LIST = 90;
		
		/**
		 * 财务管理
		 */
		/*放款管理*/
		public static final int LENDING = 101;
		/*应收账单管理*/
		public static final int RECEIVABLE_BILL = 102;
		/*应付账单管理*/
		public static final int PAYABLE_BILL = 103;
		/*平台账户管理*/
		public static final int PLATFORM_ACCOUNT = 104;
		/*资金托管账户管理*/
		public static final int FUNDS_ESCROW_ACCOUNT = 105;
		
		/**
		 * 平台推广
		 */
		/*CPS推广管理*/
		public static final int CPS_SPREAD = 121;
		/*网站流量统计分析*/
		public static final int WEB_FLOW_STATISTICS = 122;
		/*SEO设置*/
		public static final int SEO_SETTING = 123;
		
		/**
		 * 数据统计
		 */
		/*财务数据统计分析*/
		public static final int FINANCIAL_DATA_STATISTICS = 141;
		/*运营数据统计分析*/
		public static final int OPERATION_DATA_STATISTICS = 142;
		
		/**
		 * 系统设置
		 */
		/*安全设置*/
		public static final int SECURITY_SETTING = 161;
		/*财务设置*/
		public static final int FINANCE_SETTING = 162;
		/*管理员管理*/
		public static final int SUPERVISOR_SETTING = 163;
		/*权限管理*/
		public static final int RIGHT_SETTING = 164;
		/*第三方通道设置*/
		public static final int THIRD_PARTY_SETTING = 165;
		/*操作日志管理*/
		public static final int LOG_SETTING = 166;
		/*软件授权设置*/
		public static final int SOFT_EMPOWER_SETTING = 167;
		
		/**
		 * OBU风控联盟
		 */
		/*OBD*/
		public static final int OBD = 181;
	}
	
	/**
	 * 查询客服关键字
	 */
	public static final String[] QUERY_CUSTOMER_KEYWORD_TYPE = {//全部，编号，姓名，手机，邮箱
		" and (customer_num like ? or name like ? or mobile1 like ? or email like ?) ",
		" and (customer_num like ?) ",
		" and (reality_name like ?) ",
		" and (mobile1 like ?) ",
		" and (email like ?) "};
	
	/**
	 * 客服所在的权限组需要包含模块（4账单催收）
	 */
	public static final int CUSTOMER_RIGHT_ID = 4;
	
	/**
	 * 查询事件关键字
	 * 0 全部, 1 ip地址, 2 操作内容, 3 管理员名字
	 */
	public static final String[] QUERY_EVENT_KEYWORD = {
		" and (ip like ? or descrption like ? or supervisor_name like ?) ",
		" and (ip like ?) ",
		" and (descrption like ?) ",
		" and (supervisor_name like ?) "
		};
	
	/**
	 * 后台->全部会员
	 */
//	public static final String [] SEARCH_TYPE = {"and (email like ? or name like ?) ", "and email like ? ",
//		"and name like ? "};
	
	public static final String [] ORDER_TYPE = {"register_time desc","recharge_amount asc","recharge_amount desc",
		"invest_amount asc","invest_amount desc","invest_count asc","invest_count desc","bid_amount asc",
		"bid_amount desc","bid_count asc","bid_count desc","order_sort asc","order_sort desc","audit_item_count asc","audit_item_count desc"};
	
	public static final String [] ORDER_TYPE_V2 = {"register_time desc","recharge_amount asc","recharge_amount desc",
		"invest_amount asc","invest_amount desc","invest_count asc","invest_count desc","bid_amount asc",
		"bid_amount desc","bid_count asc","bid_count desc","order_sort asc","order_sort desc","audit_item_count asc","audit_item_count desc", 
		"profit asc", "profit desc", "last_login_time asc", "last_login_time desc","last_invest_time asc","last_invest_time desc", "first_invest_time asc","first_invest_time desc","user_amount asc","user_amount desc"};
	
	public static final String [] ORDER_TYPE_CPS_DETAIL = {"time desc","time ","time desc",
		"recommend_count ","recommend_count desc",
		"active_rate ","active_rate desc","commission_amount ","commission_amount desc"};
	
	public static final String [] STATUS_TYPE = {"", "and bad_bid_count > 0  ", 
		"and bad_bid_count = 0 and  overdue_bill_count >0 ", "and bad_bid_count = 0 and  overdue_bill_count = 0 "};
	
	public static final String [] LOAN_USER_ORDER = {"","bid_count asc","bid_count desc","overdue_bill_count asc",
		"overdue_bill_count desc","bad_bid_count asc","bad_bid_count desc","audit_item_count asc",
		"audit_item_count desc"};
	
	public static final String [] INVEST_USER_ORDER = {"","invest_count asc","invest_count desc","invest_amount asc",
		"invest_amount desc","order_sort asc","order_sort desc","user_amount asc","user_amount desc"};
	
	public static final String [] COMPLEX_USER_ORDER = {"","invest_count asc","invest_count desc","invest_amount asc",
		"invest_amount desc","order_sort asc","order_sort desc","user_amount asc","user_amount desc",
		"bid_amount asc","bid_amount desc","repayment_amount asc","repayment_amount desc"};
	
	public static final String [] VIP_USER_ORDER = {"","invest_count asc","invest_count desc","invest_amount asc",
		"invest_amount desc","num asc","num desc","bid_count asc","bid_count desc",
		"bid_amount asc","bid_amount desc","audit_item_count asc","audit_item_count desc"};
	
	public static final String [] CPS_TYPE = {"and (email like ? or name like ? or recommend_user_name like ?) ",
		"and email like ? ", "and name like ? ", "and recommend_user_name like ? "};
	
	public static final String [] CPS_USER_ORDER = {"","commission_amount asc","commission_amount desc","invest_amount asc",
		"invest_amount desc","bid_amount asc","bid_amount desc","recharge_amount asc","recharge_amount desc"};
	
	public static final String [] UNVERIFIED_USER_ORDER = {"","recharge_amount desc","recharge_amount asc"};
	
	public static final String [] LOCKED_USER_ORDER = {"","recharge_amount asc","recharge_amount desc",
		"lock_time asc","lock_time desc","user_amount asc","user_amount desc"};
	
	public static final String [] REPORTED_USER_ORDER = {"","reported_count asc","reported_count desc",
		"user_amount asc","user_amount desc"};
	
	public static final String [] BLACK_USER_ORDER = {"","reported_count asc","reported_count desc",
		"order_sort asc","order_sort desc","user_amount asc","user_amount desc"};
	
	
	public static final String [] USER_CITY_ADDR = {"register_time desc","invest_amount asc","invest_amount desc",
		"user_amount asc","user_amount desc"};
	
	
	public static final String [] USER_INVEST = {"t_invests.time desc", 
		" (CASE t_bids.period_unit WHEN (- 1) THEN (360 * t_bids.period) WHEN 0 THEN (30 * t_bids.period) ELSE t_bids.period END) ASC, t_invests.time desc",
		" (CASE t_bids.period_unit WHEN (- 1) THEN (360 * t_bids.period) WHEN 0 THEN (30 * t_bids.period) ELSE t_bids.period END) DESC , t_invests.time desc",
		" t_invests.amount asc "," t_invests.amount desc, t_invests.time desc",
		" red.money asc "," red.money desc , t_invests.time desc"};
	/*
	 * 发标公告排序
	 */
	public static final String [] ORDER_TYPE_BID_PUBLISH = {"publish_time desc","amount,publish_time desc","amount desc,publish_time desc"};
	
	/**
	 * 前台--cps推广
	 */
	public static final String[] MY_CPS = {"","and is_active = 1 ","and is_active = 0 "};
	public static final String[] WITHDRAWAL_TYPE = {"","and status = 2 ","and status = 0 ",
		"and status = 1 ", "and status = -1 "};
	
	/**
	 * 后台->我的会员账单
	 */
//	public static final int [] TIME_YEAR = {0,2013,2014,2015};
//	public static final int [] TIME_MONTH = {0,1,2,3,4,5,6,7,8,9,10,11,12};

	public static final String [] C_TYPE = {"and (bid_no like ? or name like ? or bill_no like ?) ",
		"and bid_no like ? ","and name like ? ","and bill_no like ? "};
	
	public static final String [] BILL_ORDER_Maturity = {" id desc", "repayment_time asc", "repayment_time desc",
		"apr asc", "apr desc", "amount asc","amount desc", "overdue_time asc", "overdue_time desc",
		"overdue_count asc","overdue_count desc"};
	
	public static final String [] BILL_ORDER_PAID = {" id desc", "repayment_time", "repayment_time desc",
		"apr", "apr desc", "amount","amount desc", "overdue_time", "overdue_time desc", ""};
	
	public static final String [] BILL_ORDER_OVERDUE = {" id desc", "repayment_time asc", "repayment_time desc",
		"apr asc", "apr desc", "amount asc","amount desc", "overdue_time asc", "overdue_time desc",
		"late_penalty asc","late_penalty desc"};
	
	public static final String [] BILL_ORDER_RECEIVE = {" real_repayment_time desc", "repayment_time asc", "repayment_time desc",
		"apr asc", "apr desc", "amount asc","amount desc", "overdue_time asc", "overdue_time desc",
		"real_repayment_time asc","real_repayment_time desc"};
	
	public static final String [] BILL_ORDER_RECEVIABLE = {" id desc", "bills_timely_completion_rate asc",
		"bills_timely_completion_rate desc", "overdue_counts asc", "overdue_counts desc",
		"uncollected_rate asc","uncollected_rate desc"};
	
	public static final String [] BILL_ORDER_REPAYMENT = {" id desc", "receive_time asc",
		"receive_time desc", "pay_amount asc", "pay_amount desc", "overdue_time asc","overdue_time desc",
		"real_receive_time asc","real_receive_time desc"};
	
	public static final String [] BILL_ORDER_INVEST = {" id desc", "ontime_complete_rate asc", "ontime_complete_rate desc",
		"principal_advances_rate asc", "principal_advances_rate desc", "nopaid_rate asc","nopaid_rate desc",
		 "nopaid_amount asc","nopaid_amount desc"};
	
	public static final String [] BILL_ORDER_STATUS = {"", "and status = 0 ", "and status = -2 "};
	public static final String [] INVEST_BILL_HAS_PAID = {" id desc", " receive_time asc ", " receive_time desc ",
		" pay_amount asc ", " pay_amount desc", " real_receive_time asc ", " real_receive_time desc"};
	
	/**
	 * 后台--提现管理
	 */
	public static final String [] WITHDRAWAL_ORDER_TYPE = {"", "order by time asc", "order by time desc",
		"order by amount asc", "order by amount desc","order by audit_time asc", "order by audit_time desc",};
	
	/**
	 * 后台--本金保障
	 */
	public static final String [] SIDE = {"and (from_pay_name like ? or to_receive_name like ?) ", "and to_receive_name like ? ", "and from_pay_name like ? "};

	public static final String [] TYPE = {"and (detail.type = 2 or detail.type = 1) ", "and detail.type = 1 ", "and detail.type = 2 "};

	public static final String[] NEW_ORDER = { "order by time desc",
			"order by read_count", "order by time", "order by start_show_time",
			" and location_pc > 0 order by time",
			" and is_use = 0 order by time" };
	
	public static String[] POSTS_ORDER = { "order by add_time desc",
		"order by read_count", "order by add_time", "order by show_time"," and s.status = 0 order by add_time"
		 };
	

	public static final String [] BID_APR_CONDITION = {" "," and apr <= 10 ","  and apr > 10 and apr < 15 ",
		" and apr >= 15 and apr <= 20  "," and apr > 20 "};
	
	public static final String [] BID_AMOUNT_CONDITION = {" "," and amount <= 100000 ","  and amount > 100000 and amount <= 500000  ",
			                  " and amount > 500000 and amount <= 1000000 ","  and amount > 1000000 and amount <= 3000000 ",
			                  "  and amount > 3000000 "};
	
	public static final String [] BID_LOAN_SCHEDULE_CONDITION = { " "," and loan_schedule <= 50 ",
		" and loan_schedule > 50  and loan_schedule <=80 "," and loan_schedule > 80  and loan_schedule <100 "," and loan_schedule =100 "};

	public static final String [] BID_ORDER_CONDITION  = {" order by time desc,loan_schedule,is_hot desc,id desc "," order by amount desc ", " order by amount asc", " order by apr desc ", " order by apr asc", " order by loan_schedule desc ", " order by loan_schedule asc", " ORDER BY (CASE period_unit WHEN (- 1) THEN (360 * period) WHEN 0 THEN (30 * period) ELSE period END) DESC ", " ORDER BY (CASE period_unit WHEN (- 1) THEN (360 * period) WHEN 0 THEN (30 * period) ELSE period END) ASC"};

	public static final String [] DEBT_AMOUNT_CONDITION = {" ","  and  debt_amount <= 1000 ",
		"  and  debt_amount > 1000  and debt_amount <=5000 ","  and  debt_amount > 5000   and debt_amount <=10000 ",
		"  and  debt_amount > 10000 and  debt_amount <=30000 ","  and  debt_amount > 30000 "};
	
	public static final String [] DEBT_ORDER_CONITION = {" "," order by  debt_amount desc"," order by  debt_amount asc",
		" order by  apr desc", " order by  apr asc", " order by  end_time desc", " order by  end_time asc", " order by  repayment_time desc", "order by  repayment_time asc"};

	
	public static final String [] TRANSFER_MANAGEMENT_STATUS_CONDITION = {" ", " and status = 0 "," (and status = 1  or status = 2)"," and status = 3 "," (and status = -2  or  status = -3  or status = -5)   "," and status = -1 "};
	
	public static final String [] TRANSFER_MANAGEMENT_TYPE_CONDITION = {"   ","  and title like ? ","  and bid_no like ? "};
	
	/**
	 * 前台--我的借款账单
	 */
	public static final String [] LOAN_BILL_REPAYMENT = {"", "and status in (-1, -2) ", "and status in (-3, 0) "};
	public static final String [] LOAN_BILL_OVDUE = {"", "and is_overdue in (0) ", "and is_overdue in (-1, -2, -3) "};
	public static final String [] LOAN_BILL_ALL = {"and title like ? ", "and title like ? "};   
	
	/**
	 * 前台--我的理财账单
	 */
	public static final String [] LOAN_INVESTBILL_RECEIVE = {"", "and status in (-1, -2,-5,-6) ", "and status in (-3 ,-4, 0) "};
	public static final String [] LOAN_INVESTBILL_OVDUE = {"", "and status in (-1, 0,-5) ", "and status in (-4, -2, -3,-6) "};
	public static final String [] LOAN_INVESTBILL_ALL = {"and title like ? ", "and title like ? "};	
	
	/**
	 * 广告条位置
	 */
	public static final String HOME_PAGE_PC = "PC首页";
	public static final String HOME_PAGE_APP = "APP首页";
	public static final String STARTUP_BOOT_APP = "APP启动图";
	public static final String HOME_PAGE_BACK = "PC后台";
	public static final String LOAN_PAGE = "我要借款";
	public static final String ENSURE_PAGE = "本金保障";
	public static final String FUN_PAGE = "财富资讯";
	public static final String NEWS_PAGE = "财富资讯列表";
	public static final String HOME_APP_WAP = "app论坛首页";
//	public static final String NEWS_DETAIL_PAGE = "新闻资讯详情";
	public static final String HOME_PAGE_PC_ADVISE = "PC首页宣传位";
	public static final String STARTUP_BOOT_APP_BORROW = "借款APP启动图";
	
	
	/**
	 * 交易事件
	 */
	public class DetailType{
		public static final int HAND_RECHARGE = 1;
		public static final int FREEZE_WITHDRAWALS = 201;
		public static final int FREEZE_WITHDRAWALS_P = 201;
	}
	
	/**
	 * 文件格式
	 * @author lzp
	 * @version 6.0
	 * @created 2014-8-16
	 */
	public class FileFormat {
		public static final int IMG = 1;	//图片
		public static final int TXT = 2;	//文本
		public static final int VIDEO = 3;	//视频
		public static final int AUDIO = 4;	//音频
		public static final int XLS = 5;	//表格
	}
	
	public class TemplateType {
		public static final int CUSTOM = 1;
		public static final int SYSTEM = 0;
	}
	
	public static final int INCOME = 1;
	public static final int EXPENSE = 2;
	public static final int FREEZE = 3;
	public static final int THAW = 4;
	
	/**
	 * 超额借款排序条件
	 */
	public static final String[] OVER_BORROWS_ORDER_CONDITION = 
		{
			" order by time desc",
			" order by time desc",
			" order by appended_items_count desc",
			" order by appended_items_count",
			" order by passed_items_count desc",
			" order by passed_items_count",
			" order by unpassed_items_count desc",
			" order by unpassed_items_count",
			" order by auditing_items_count desc",
			" order by auditing_items_count"
		};
	
	/**
	 * 理财情况统计表
	 */
	public static final String[] INVEST_STATISTICS = 
		{
			"",
			"order by invest_fee_back desc ",
			"order by invest_fee_back ",
			"order by average_invest_amount desc ",
			"order by average_invest_amount ",
			"order by invest_count desc ",
			"order by invest_count "
		};
	
	/**
	 * 审核科目库统计分析表-排序类型
	 */
	public static final String[] AUDIT_ITEMS_ORDER_TYPE = 
		{
			" order by id",
			" order by id desc",
			" order by risk_control_ranking desc",
			" order by risk_control_ranking ",
			" order by submit_per desc",
			" order by submit_per ",
			" order by relate_overdue_bid_num desc",
			" order by relate_overdue_bid_num ",
			" order by relate_bad_bid_num desc",
			" order by relate_bad_bid_num "
		};
	
	/**
	 * 审核科目库统计分析表-搜索关键字
	 */
	public static final String[] AUDIT_ITEMS_CONDITION = 
		{
			" and (no like ? or name like ?)",
			" and (no like ?)",
			" and (name like ?)"
		};
	
	/**
	 * 借款情况统计分析表-排序类型
	 */
	public static final String[] BORROW_ORDER_TYPE = 
		{
			" order by id desc",
			" order by id desc",
			" order by month desc",
			" order by month ",
			" order by total_borrow_amount desc",
			" order by total_borrow_amount ",
			" order by overdue_per desc",
			" order by overdue_per ",
			" order by bad_bill_amount_per desc",
			" order by bad_bill_amount_per "
		};
	
	/**
	 * 借款标销售情况分析表-排序类型
	 */
	public static final String[] PRODUCT_ORDER_TYPE = 
		{
			" order by id desc",
			" order by id desc",
			" order by released_bids_num desc",
			" order by released_bids_num ",
			" order by average_bid_amount desc",
			" order by average_bid_amount ",
			" order by success_bids_num desc",
			" order by success_bids_num "
		};
	
	/**
	 * 借款标销售情况分析表-搜索关键字
	 */
	public static final String[] PRODUCT_CONDITION = 
		{
			" and (name like ?)",
			" and (name like ?)",
		};
	
	/**
	 * 充值统计表-排序类型
	 */
	public static final String[] RECHARGE_ORDER_TYPE = 
		{
			" order by id desc",
			" order by id asc",
			" order by id desc",
			" order by recharge_amount asc",
			" order by recharge_amount desc",
			" order by recharge_menber asc",
			" order by recharge_menber desc",
			" order by new_recharge_menber asc",
			" order by new_recharge_menber desc",
		};
	
	/**
	 * 保障本金统计分析表-排序类型
	 */
	public static final String[] SECURITY_ORDER_TYPE = 
		{
			" order by id desc",
			" order by id asc",
			" order by id desc",
			" order by bad_debt_income_rate asc",
			" order by bad_debt_income_rate desc",
			" order by pay asc",
			" order by pay desc",
			" order by advance_acount asc",
			" order by advance_acount desc",
		};
	
	/**
	 * 会员数据统计分析表-排序类型
	 */
	public static final String[] MEMBER_ORDER_TYPE = 
		{
			" order by id desc",
			" order by id asc",
			" order by id desc",
			" order by new_member asc",
			" order by new_member desc",
			" order by new_recharge_member asc",
			" order by new_recharge_member desc",
		};
	
	public static final String [] IPS_TYPE = {"01", "128", "04", "16", "32", "1024"};
	public static final String [] GO_TYPE = {"ICBC", "CMB", "CCB", "ABC", "BOC", "SPDB", "CIB", "BOBJ",
		"CEB", "BOCOM", "CMBC", "CITIC", "GDB", "PAB", "PSBC", "SRCB", "BOS", "HXBC","TCCB","NBCB","NJCB"};
	
	public static final String [] GO_CODE = {"1020000", "3080000", "1050000", "1030000", "1040000", "3100000", "3090000", "3030000",
		"3050000", "3020000", "3060000", "3070000", "1000000", "3040000"};
	
	public static final String [] GO_BANK_NAME = {"工商银行", "招商银行", "建设银行", "农业银行 ", "中国银行 ", "浦发银行", "兴业银行", "光大银行",
		"民生银行", "中信银行", "广发银行", "平安银行", "储蓄银行", "华夏银行 "};
	
	/**
	 * QQ唯一标示
	 */
	public static final long IDENTIFIED_QQ = 1;
	
	/**
	 * QQ授权地址
	 */
	public static final String AUTHORIZATIONURL_QQ = Play.configuration.getProperty("authorizationURL_QQ", "");
	
	/**
	 * 获取QQ令牌地址
	 */
	public static final String ACCESSTOKENURL_QQ = Play.configuration.getProperty("accessTokenURL_QQ", "");
	
	/**
	 * QQ客户id
	 */
	public static final String CLIENTID_QQ = Play.configuration.getProperty("clientid_QQ", "");
	
	/**
	 * QQ客户密码
	 */
	public static final String SECRET_QQ = Play.configuration.getProperty("secret_QQ", "");
	
	/**
	 * QQ响应类型
	 */
	public static final String RESPONSETYPE_QQ = Play.configuration.getProperty("responseType_QQ", "");
	
	/**
	 * 获取QQ应用平台id地址
	 */
	public static final String GETOPENIDURL_QQ = Play.configuration.getProperty("getOpenIDURL_QQ", "");
	
	/**
	 * 获取QQ用户信息地址
	 */
	public static final String GETUSERINFOURL_QQ = Play.configuration.getProperty("getUserINFOURL_QQ", "");
	
	
	/**
	 * 微博唯一标示
	 */
	public static final long IDENTIFIED_WB = 2;
	
	
	/**
	 * 微博授权地址
	 */
	public static final String AUTHORIZATIONURL_WB = Play.configuration.getProperty("authorizationURL_WB", "");
	
	/**
	 * 获取微博令牌地址
	 */
	public static final String ACCESSTOKENURL_WB = Play.configuration.getProperty("accessTokenURL_WB", "");
	
	/**
	 * 微博客户id
	 */
	public static final String CLIENTID_WB = Play.configuration.getProperty("clientid_WB", "");
	
	/**
	 * 微博客户密码
	 */
	public static final String SECRET_WB = Play.configuration.getProperty("secret_WB", "");
	
	/**
	 * 微博响应类型
	 */
	public static final String RESPONSETYPE_WB = Play.configuration.getProperty("responseType_WB", "");
	
	/**
	 * 获取微博用户信息地址
	 */
	public static final String GETUSERINFOURL_WB = Play.configuration.getProperty("getUserINFOURL_WB", "");
	
	/**
	 * 登录注册标识
	 */
	public static final String LOGIN_AREAL_FLAG = "loginOrRegister";

	/**
	 * 用户默认头像
	 */
	public static final String DEFAULT_PHOTO = "/public/images/userImg.png";

	/**
	 * 云盾校验用户
	 */
	public static final String CLOUD_SHIELD_NOUSER = "-1";
	
	/**
	 * 云盾校验密码
	 */
	public static final String CLOUD_SHIELD_PASSWORD_ERROR = "-2";

	/**
	 * 云盾校验签名
	 */
	public static final String CLOUD_SHIELD_SIGN_FAULT = "-3";
	
	/**
	 * 云盾校验服务器时间
	 */
	public static final String CLOUD_SHIELD_SERVICE_TIME = "-4";
	
	/**
	 * 校验当前云盾没有插入
	 */
	public static final String CLOUD_SHIELD_NOT_EXIST = "-1";
	
	/**
	 * 校验云盾不属于当前管理员
	 */
	public static final String CLOUD_SHIELD_SUPERVISOR = "-3";
	
	/**
	 * 校验云盾不属于当前系统
	 */
	public static final String CLOUD_SHIELD_UN_SYSTEM = "-2";
	
	/**
	 * 校验云盾正确
	 */
	public static final String CLOUD_SHIELD_OK = "0";
	
	public static final int SELL_SERVICE_DATE = 1; // 售后服务到期时间(年单位)
	
	public static final int CPSREWARDTYPE_USER_COUNT = 1;//推广规则：1:-按会员数   2-按交易额
	public static final int CPSREWARDTYPE_USER_DEALCOUNT = 2;//推广规则：1:-按会员数   2-按交易额
	public static final int CPSREWARDTYPE_USER_PROFIT = 3;//推广规则：1:-按会员数   2-按交易额  3-按收益
	
	/**
	 * 普通网关充值类型：1：在线充值， 2：手工充值
	 */
	public static final int GATEWAY_RECHARGE = 1;  //在线充值
	public static final int ORDINARY_RECHARGE = 2;  //手工充值
	public static final int QUARTZ_RECHARGE = 3;  //定时任务充值
	/**
	 * 资金托管充值类型： 1：账户直充，2：闪电快充，
	 */
	public static final int ACCOUNT_RECHARGE = 1;  //账户直充
	public static final int AGENT_RECHARGE = 2;  //闪电快充
	
	public static final String NORMAL_CODE_PREFIX = "YQ"; //理财邀请码前缀
	
	public static final int INVITE_CODE_TYPE_INVEST = 1;
	
	public static final int INVITE_CODE_TYPE_GIVE = 2;
	
	public static final int INVITE_CODE_ACTIVE = 1;
	
	public static final int INVITE_CODE_UNACTIVE = 0;
	/**
	 * 资金托管普通网关充值类型
	 * @author lzp
	 * @version 6.0
	 * @created 2014-11-7
	 */
	public class RechargeType {
		public static final int Normal = 0;
		public static final int ItemAudit = 1;//单个资料审核费
		public static final int VIP = 2;//vip申请费
		public static final int UploadItems = 3;//多个资料资料审核费
		public static final int InvestBonus = 4;//投标奖励
		public static final int UploadItemsCR = 5;//多个资料资料审核费(信用等级超额借款补交)
		public static final int UploadItemsOB = 6;//多个资料资料审核费(超额借款申请)
		public static final int CREATBID = 7;
	}
	
	public static final int SHOW_TYPE_1 = 1; //标PC端显示
	public static final int SHOW_TYPE_2 = 2; //标APP端显示
	public static final int SHOW_TYPE_3 = 3; //标微信端显示
	public static final double AUTO_INVEST = 100000000; // 自动投标限制金额上限
	public static final String DEFAULT_IMAGE = "/public/images/default.png"; // 默认图片路径
	public static final String DEFAULT_SPAY_IMAGE = "/public/images/s_linklogo.png"; //默认logo路径
	public static final String IPS_LOGO_IMAGE = "/public/images/s_linklogo.png"; //环讯logo路径
	public static final String LOAN_LOGO_IMAGE = "/public/images/sq_logo.png"; //双乾logo路径
	public static final String GUO_LOGO_IMAGE = "/public/images/gfb_logo.png"; //国付宝logo路径
	public static final String YEE_LOGO_IMAGE = "/public/images/yb_logo.png"; //易宝logo路径
	
	public static final String PUSH_BILL = "3天"; // 账单推送时间设定
	public static final int PUSH_ACTIVITY_TYPE = 1; //活动推送
	public static final int PUSH_BILL_TYPE = 2; //账单提醒推送
	public static final int PUSH_INVEST_TYPE = 3; //满表提醒推送
	
	//商户转账给用户类型
	public class MerToUserType {
		public static final int CPS = 0;//发放cps推广费
		public static final int Fund = 1;//发放投标奖励
		public static final int ItemFefund = 2;//退回资料审核费
	}
	
	
	/**
	 * 微信参数
	 */
	public static final String TOKEN = Play.configuration.getProperty("wechat.token");
	public static final String APPID = Play.configuration.getProperty("wechat.appId");
	public static final String APPSECRET = Play.configuration.getProperty("wechat.appSecret");
	public static final String REDIRECT_URI = Play.configuration.getProperty("wechat.redirect_uri");
	public static final int TEXT_MAX_LENGTH = 2048;

	/**
	 * 注册入口
	 */
	public static final int CLIENT_PC = 1;
	public static final int CLIENT_APP = 2;
	public static final int CLIENT_WECHAT = 3;
	public static final int CLIENT_OTHERS = 4;
	public static final int CLIENT_BACKSTAGE = 5;
	
	
	/**
	 * 微信分页页数
	 */
	public static final int WECHAT_CURRPAGE = 1;
	public static final int WECHAT_PAGESIZE = 10;
	
	/**
	 * 微信查询全部对应的type值
	 */
	public static final String WECHAT_SEARCH_TYPE = "0";
	
	public static final int ALREADY_RUN = -11; // 某个操作已执行
	public static final int REFUND_ITEM_FEE = -12; // 完全资金托管模式退回资料审核费

	/**
	 * 导出excel标示
	 */
	public static final int IS_EXPORT = 1; // 是否导出excel标示
	public static final int NO_PAGE = 1; // 不分页   add by hys
	
	/**
	 * 充值来源标志
	 */
	public static final String RECHARGE_APP = "1"; // app充值
	public static final String RECHARGE_WECHAT = "2"; // 微信充值
	public static final String RECHARGE_PC = "3"; // 微信充值
	
	/**
	 * APP文件服务器地址，IOS版本
	 */
	public static final String IPA_PATH = Play.configuration.getProperty("ipa.path");
		
	/**
	 * APP文件服务器地址，android版本
	 */
	public static final String APK_PATH = Play.configuration.getProperty("apk.path");
	
	/**
	 * 投资超标
	 */
	public static final int OVERBIDAMOUNT = -12;
	
	/**
	 * 后台运营数据报表显示类型 1：昨天  2：最近7天  3：最近30天
	 */
	public static final int YESTERDAY = 1;
	public static final int LAST_7_DAYS = 2;
	public static final int LAST_30_DAYS = 3;
	
	/**
	 * 一天两端时间
	 */
	public static final String STARTTIME = " 00:00:00";
	public static final String ENDTIME = " 23:59:59";
	
	/**
	 * 连连
	 */
	public static final int LL_GATEWAY = 3;
	/**
	 * 宝付
	 */
	public static final int BF_GATEWAY = 4;
	/**
	 * 连连网银支付版本号
	 */
	public static final String LL_WEB_PAY_VERSION = Play.configuration.getProperty("LL.web.pay.version");
	/**
	 * 连连商户编号
	 */
	public static final String LL_PAY_OID_PARTNER = Play.configuration.getProperty("LL.pay.oid.partner");
	/**
	 * 连连签名类型
	 */
	public static final String LL_PAY_SIGN_TYPE = Play.configuration.getProperty("LL.pay.sign.type");
	/**
	 * 连连网银支付异步通知地址
	 */
	public static final String LL_WEB_PAY_NOTIFY_URL = BASE_URL+Play.configuration.getProperty("LL.web.pay.notify.url");
	/**
	 * 连连网银支付同步通知地址
	 */
	public static final String LL_WEB_PAY_RETURN_URL = BASE_URL+Play.configuration.getProperty("LL.web.pay.return.url");
	
	/**
	 * 连连私钥
	 */
	public static final String LL_PAY_PRIMARY_KEY = Play.configuration.getProperty("LL.pay.primary.key");
	/**
	 * 银联公钥
	 */
	public static final String LL_PAY_PUBLIC_KEY = Play.configuration.getProperty("LL.pay.public.key");
	/**
	 * 连连MD5 key
	 */
	public static final String LL_PAY_MD5_KEY = Play.configuration.getProperty("LL.pay.md5.key");
	/**
	 * 连连网银支付服务地址
	 */
	public static final String LL_WEB_PAY_SERVICE_URL = Play.configuration.getProperty("LL.pay.web.pay.url");
	/**
	 * 连连网银支付服务地址
	 */
	public static final String LL_AUTH_PAY_SERVICE_URL = Play.configuration.getProperty("LL.auth.pay_url");
	
	/**
	 * 红包发发放方式 手动
	 */
	public static final long RED_PACKAGE_SENDTYPE_HAND = 1;
	
	/**
	 * 红包发发放方式 自动
	 */
	public static final long RED_PACKAGE_SENDTYPE_AUTO = 0;
	public static final String RED_PACKAGE_TYPE_HAND = "0";
	
	
	//已过期
	public static final long RED_PACKAGE_STATUS_OVERDUE = -1;
	//未使用
	public static final long RED_PACKAGE_STATUS_UNUSED  = 0;
	public static final int RED_PACKAGE_STATUS_UNUSED_INT  = 0;
	//已使用
	public static final long RED_PACKAGE_STATUS_USED = 1;
	//使用中
	public static final long RED_PACKAGE_STATUS_USING = 2;
	//充值完成前奖励红包状态
	public static final long RED_PACKAGE_STATUS_UN = -2;
	
	//
	public static String RED_PACKAGE_APP = "APP充值发放红包";
	
	//红包类型状态  启用  禁用
	//禁用
	public static final long RED_PACKAGE_TYPE_STATUS_DISABLED  = 0;
	//  启用
	public static final long RED_PACKAGE_TYPE_STATUS_ENABLED = 1;
	
	
	public static final String RED_PACKAGE_TYPE_HANDER =   "0";//手动红包
	public static final String RED_PACKAGE_TYPE_REGIST =   "1";//注册红包
	public static final String RED_PACKAGE_TYPE_RECHARGE = "2";//充值红包
	public static final String RED_PACKAGE_TYPE_INVEST =   "3";//投资红包
	public static final String RED_PACKAGE_TYPE_SCROE =   "4";//积分红包
	public static final String RED_PACKAGE_TYPE_AUTH =   "5";//实名认证
	public static final String RED_PACKAGE_TYPE_BANK =   "6";//添加银行卡
	public static final String RED_PACKAGE_TYPE_INVESTS =   "7";//累计投资
	public static final String RED_PACKAGE_TYPE_BIRTHDAY =   "8";//生日
	public static final String RED_PACKAGE_TYPE_CUSTOM =   "9";//自定义
	public static final String RED_PACKAGE_TYPE_RECEIVED =   "11";//回款红包
	
	public static final String APP_WAP_POST_TYPE_PAGE_O = "1";//查询帖子类型页数1
	public static final String APP_WAP_POST_TYPE_PAGE_T = "2";//查询帖子类型页数2
	public static final String APP_WAP_POST_TYPE_SIZE = "4";//查询帖子类型个数
	
	public static final long APP_WAP_FORUNM_YOU_FIND = 2;//亿亿看板id
	public static final long APP_WAP_FORUNM_EVERY_DAY = 3;//每日签到id
	public static final long APP_WAP_FORUNM_HELP_SCHOOL = 4;//求助学堂id
	public static final long APP_WAP_FORUNM_NEW_STUDENT = 5;//新人报道id
	public static final long APP_WAP_FORUNM_FORUM_ACTIVITY = 6;//社区活动id
	public static final long APP_WAP_FORUNM_INVESTMENT_IDEA = 7;//理财心得id
	public static final long APP_WAP_FORUNM_LIVE_CHAT = 8;//亿亿闲聊id
	
	public static final int COUPON_TYPE_RED_PACKAGE = 1;//优惠券类型-红包
	public static final int COUPON_TYPE_RATE = 2;//优惠券类型-加息券
	
	
	public static String APP_URL_NAME_FIRST = "充值";
	public static String APP_URL_NAME_TOW = "产品中心";
	/**
	 * 按月还款、等额本息
	 */
	public static String DEBX = "1";
	/**
	 * 按月付息、到期还本
	 */
	public static String DQHB = "2";
	/**
	 * 一次性还款
	 */
	public static String YCXHK = "3";
	
	/**
	 * 宝付认证支付--银行卡编号
	 */
	public static final String [] BAOFU_TYPE = {"ICBC", "CMB","CCB","ABC","BOC","SPDB","CIB","CEB","BCOM","CITIC","SHB",
		"PAB","PSBC","CMBC","CDB", "HXB"};
	
	public static final String [] BAOFU_BANK_NAME = {"工商银行", "招商银行", "建设银行", "农业银行 ", "中国银行 ", "浦发银行", "兴业银行", "光大银行",
		"交通银行", "中信银行", "上海银行", "平安银行", "储蓄银行","民生银行","国家开发银行","华夏银行"};
	
	
	public static final String  BF_END_NUM_SDK= Play.configuration.getProperty("bf_end_num_sdk", "100000916");
	public static final String BF_MERCHANT_APP_URL= Play.configuration.getProperty("bf_merchant_app_url", "");
	public static final String BF_MERCHANT_APP_URLBG= Play.configuration.getProperty("bf_merchant_app_urlbg", "");
	public static final String BF_APP_URL = Play.configuration.getProperty("bf_app_url", "");
	
	/**
	 * 红包排序
	 */
	public static final String[] RED_PACKAGE_ORDER_CONDITION = {
		"ORDER BY %sID DESC",
		"ORDER BY %sTIME",
		"ORDER BY VALIDATE"
	};
	
	/**
	 * 加息汇总排序
	 */
	public static final String[] INCREASE_SUM_ORDER_CONDITION = {
		"ORDER BY b.ID DESC",
		"ORDER BY b.audit_time",
		"ORDER BY b.audit_time desc",
		"ORDER BY nextTime",
		"ORDER BY nextTime desc"
	};
	
	/**
	 * 加息明细排序
	 */
	public static final String[] INCREASE_DETAIL_ORDER_CONDITION = {
		"ORDER BY i.ID DESC", 
		"ORDER BY i.receive_time",
		"ORDER BY i.receive_time desc"
	};
	
	/**
	 * 实名、绑卡失败统计 排序
	 */
	public static final String[] USER_ACTION_ORDER = {
		" ORDER BY a.ID DESC", 
		" ORDER BY a.action_time",
		" ORDER BY a.action_time desc"
	};
	
	/*字典表缓存KEYS*/
	public static final String DICT_KEYS_PROVINCES = "provinces";
	
	public static final Long APPLICATION_CHECK_RIGHT_ID=new Long(195);
	public static final Long APPLICATION_DUE_DILIGENCE_RIGHT_ID=new Long(196);
	
	/**
	 * 公司信用代码
	 */
	public static final String SHXYDM="91310107MA1G016X4C";
	/**
	 * 公司名称
	 */
	public static final String GSMC="中亿云投资有限公司";
	/**
	 * 公司名称
	 */
	public static final String GSMC_KEY="乙方：杭州天谷信息科技有限公司";
	/**
	 * 合同 甲方关键字
	 */
	public static final String JIAFANG_KEY="甲方（签字）：";
	/**
	 * 合同 乙方关键字
	 */
	public static final String YIFANG_KEY="乙方（签字）：";
	/**
	 * 合同 甲方关键字
	 */
	public static final String JIAFANG_KEY_XY="甲方（签章）：";
	/**
	 * 合同 乙方关键字
	 */
	public static final String YIFANG_KEY_XY="乙方（签章）：";
}

