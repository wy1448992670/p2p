package controllers.app;  //

/**
 * OPT常量值
 * Description:
 * @author zhs
 * vesion: 6.0 
 * @date 2014-9-16 下午04:06:46
 */
public class AppConstants {
	public static final int APP_LOGIN = 1;  //登录
	public static final int APP_REGISTER = 2;  //注册
	public static final int APP_BASEINFO = 3;  //账户基本信息
	public static final int APP_FIND_PWD_BY_SMS = 4;  //发送短信验证码
	public static final int APP_CONFIRM_CODE = 5;  //找回密码-验证码确认
	public static final int APP_COMMIT_NEW_PWD = 6;  // 重置密码-提交新密码
	public static final int APP_SAVE_CELLPHONE = 7;  //绑定手机号码
	public static final int APP_SERVICE_AGREEMENT = 8;  //注册服务协议
	public static final int APP_REPAYMENT_CALCULATOR = 9;  //还款计算器
	public static final int APP_ALL_BIDS = 10;  //查询借款标列表  211 1040
	public static final int APP_BID_DETAIL = 11;  //借款标详情
	public static final int APP_INVEST_BID_RECORD = 12;  //查询借款标投标记录
	public static final int APP_ALL_QUESTION = 13;  //查询借款标提问以及回答列表
	public static final int APP_ADD_QUESTIONS = 14;  //查询借款标提问记录
	public static final int APP_INVEST_DETAIL = 15;  //投标详情
	public static final int APP_INVEST = 16;  // 投标操作
	public static final int APP_UPLOAD_IMG = 17;  //上传图片
	public static final int APP_LOAN_PRODUCT = 18;  //借款产品列表
	public static final int APP_PRODUCT_INFO = 19;  //借款标产品详情
	public static final int APP_PRODUCT_DETAIL = 20;  //获取借款产品信息
	public static final int APP_CREATE_BID = 21;  //发布借款
	public static final int APP_APR_CALCULATOR = 22;  //利率计算器
	public static final int APP_USER_STATUS = 23;  //获取完善用户资料状态
	public static final int APP_SAVE_BASEINFO = 24;  //完善用户资料
	public static final int APP_ACTIVE_EMAIL = 25;  //通过后台发送激活邮件
	public static final int APP_VIP_APPLY = 26;  //申请vip
	public static final int APP_VIP_AGREEMENT = 27;  //VIP会员服务条款
	public static final int APP_TWO_DIMENSIONANL_CODE = 28;  //二维码
	public static final int APP_SPREAD_USER = 29;  //我推广的会员列表
	public static final int APP_ALL_DEBTS = 30;  //查询所有债权
	public static final int APP_DEBT_DETAIL = 31;  //债权转让标详情
	public static final int APP_DEBTAUCTION_RECORDS = 32;  //债权竞拍记录
	public static final int APP_ACTION_DEBT_DETAIL = 33;  // 获取竞拍相关信息
	public static final int APP_AUCTION = 34;  //债权竞拍
	public static final int APP_INVEST_BILLS = 35;  //理财子账户--理财账单
	public static final int APP_BILL_DETAIL = 36;  // 账单详情
	public static final int APP_CURRENT_BILL_DETAIL = 37;  //本期账单明细
	public static final int APP_BILL_BID_DETAIL = 38;  //账单借款标详情
	public static final int APP_HISTORY_REPAYMENT = 39;  // 账单历史还款情况
	public static final int APP_INVEST_RECORDS = 40;  //理财子账户--投标记录
	public static final int APP_LOANING_INVEST_BIDS = 41;  //理财子账户--等待满标的理财标
	public static final int APP_RECEVING_INVEST_BIDS = 42;  //理财子账户---收款中的理财标列表
	public static final int APP_TRANSFER_DEBT = 43;  //转让债权
	public static final int APP_SUCCESS_DEBT = 44;  // 理财子账户--已成功的理财标
	public static final int APP_DEBT_TRANSFER = 45;  //债权转让管理
	public static final int APP_DEBT_DETAILS_SUCCESS = 46;  //债权转让成功详情页面
	public static final int APP_SUCCESS_DEBT_DETAILS = 47;  //债权转让中详情页面
	public static final int APP_DEBT_DETAILS_NO_PASS = 48;  //债权转让不通过详情页面
	public static final int APP_DEBT_TRANSFER_DETAIL = 49;  //债权转让详情
	public static final int APP_DEBT_TRANSFER_BID_DETAIL = 50;  //债权转让借款标详情页面
	public static final int APP_TRANSACT = 51;  //成交债权
	public static final int APP_ACTION_RECORDS = 52;  // 查询债权竞拍记录
	public static final int APP_RECEIVED_DEBT_TRANSFER = 53;  //查询用户受让债权管理列表
	public static final int APP_RECEIVE_DEBT_DETAIL_SUCCESS= 54;  //受让债权的详情 [竞拍成功]
	public static final int APP_RECEIVE_DEBT_DETAIL_AUCTION = 55;  //受让债权的详情 [竞拍中]
	public static final int APP_RECEIVE_DEBT_DETAIL = 56;  //债权受让详情 [竞拍成功,竞拍中,定向转让]
	public static final int APP_RECEIVE_DEBT_BID_DETAIL = 57;  //受让的借款标详情 [竞拍成功,竞拍中,定向转让]
	public static final int APP_INCREASE_ACTION = 58;  //加价竞拍
	public static final int APP_ACCEPT_DEBTS = 59;  //接受定向转让债权
	public static final int APP_NOT_ACCEPT = 60;  //拒绝接受定向债权转让
	public static final int APP_INVEST_STATISTICS = 61;  //理财情况统计表
	public static final int APP_UPDATE_ROBOTS = 62;  //设置投标机器人
	public static final int APP_AUTO_INVEST = 63;  //进入自动投标页面
	public static final int APP_CLOSE_ROBOT = 64;  //关闭投标机器人
	public static final int APP_ATTENTION_DEBTS = 65;  //收藏的债权列表
	public static final int APP_ATTENTION_BIDS = 66;  //收藏的借款标
	public static final int APP_ATTENTION_USERS_LSIT = 67;  //查询用户关注用户列表
	public static final int APP_BLACK_LIST = 68;  //用户黑名单
	public static final int APP_REPORT_USERS = 69;  //举报用户
	public static final int APP_ADD_BLACK = 70;  //拉黑对方
	public static final int APP_ATTENTION_USERS = 71;  // 关注用户
	public static final int APP_COLLECT_BID = 72;  //收藏借款标
	public static final int APP_COLLECT_DEBT = 73;  //收藏债权
	public static final int APP_HELP_CENTER = 74;  //进入帮助中心页面
	public static final int APP_HELP_CENTER_CONTENT = 75;  //帮助中心内容列表
	public static final int APP_HELP_CENTER_DETAIL = 76;  //帮助中心列表详情
	public static final int APP_COMPANY_INTRODUCTION = 77;  //关于我们---公司介绍
	public static final int APP_MANAGEMENT_TEAM = 78;  //关于我们---管理团队
	public static final int APP_EXPER_ADVISOR = 79;  //关于我们---专家顾问
	public static final int APP_SEND_STATION = 80;  //发送站内信
	public static final int APP_SYSTEM_SMS = 81;  //系统信息接口
	public static final int APP_DELETE_SYSTEM_SMS = 82;  //删除系统信息接口
	public static final int APP_INBOX_SMGS = 83;  //收件箱信息
	public static final int APP_DELETE_INBOX_SMGS = 84;  //删除收件箱信息接口
	public static final int APP_MARK_MSGS_READED = 85;  //标记为已读
	public static final int APP_MARK_MSGS_UNREAD = 86;  //标记为未读
	public static final int APP_LOAN_BILLS = 87;  //借款账单
	public static final int APP_LOAN_BILL_DETAILS = 88;  //借款账单详情
	public static final int APP_SUBMIT_REPAYMENT = 89;  //还款
	public static final int APP_AUDITING_LOAN_BIDS = 90;  //审核中的借款标列表
	public static final int APP_AUDITING_BIDS = 91;  // 等待满标的借款标列表
	public static final int APP_REPAYMENT_BIDS = 92;  //还款中的借款标列表
	public static final int APP_SUCCESS_BIDS = 93;  //已成功的借款标列表
	public static final int APP_AUDIT_MATERIALS = 94;  //审核资料认证
	public static final int APP_AUDIT_MATERIALS_SAMEITEM = 95;  //审核资料认证详情
	public static final int APP_SPREAD_USER_INCOME = 96;  // 我推广的收入接口
	public static final int APP_DEAL_RECORD = 97;  //交易记录
	public static final int APP_BANK_INFO = 98;  //银行卡管理
	public static final int APP_ADD_BANK = 99;  //添加银行卡
	public static final int APP_EDIT_BANK = 100;  //编辑银行卡
	public static final int APP_QUERY_ANSWERS = 101;  //查询安全问题
	public static final int APP_VERIFYE_QUESTION = 102;  //校验安全问题
	public static final int APP_SAVE_PAY_PWD = 103;  //保存交易密码
	public static final int APP_EDIT_PAY_PWD = 104;  //修改交易密码
	public static final int APP_SAVE_PWD = 105;  //保存登录密码
	public static final int APP_QUESTION_STATUS = 106;  //安全问题设置的状态
	public static final int APP_QUESTION_CONTENT = 107;  //获取安全问题内容
	public static final int APP_SAVE_SAFE_QUESTION = 108;  //设置安全问题内容
	public static final int APP_EMAIL_STATUS = 109;  //邮箱激活状态
	public static final int APP_SAVE_EMAIL = 110;  //修改邮箱
	public static final int APP_PHONE_STATUS = 111;  //安全手机详情及状态
	public static final int APP_MY_CREDIT = 112;  //我的信用等级
	public static final int APP_VIEW_CREDIT_RULE = 113;  //查看信用等级规则
	public static final int APP_CREDIT_INTEGRAL = 114;  //查看信用积分规则
	public static final int APP_AUDIT_ITEM_SCORE = 115;  //审核资料积分明细
	public static final int APP_CREDIT_DETAIL_REPATMENT = 116;  //正常还款积分明细
	public static final int APP_CREDIT_DETAIL_LOAN = 117;  //成功借款积分明细
	public static final int APP_CREDIT_DETAIL_INVEST = 118;  //成功投标积分明细
	public static final int APP_CREDIT_DETAIL_OVERDUE = 119;  //逾期扣分积分明细
	public static final int APP_APPLY_FOR_OVER_BORROW = 120;  //申请超额借款
	public static final int APP_OVER_BORROW_LIST = 121;  //申请超额借款记录列表
	public static final int APP_HOME = 122;  // 首页
	public static final int APP_SELECT_AUDIT_ITEMS_INIT = 123;  //选择超额借款审核资料库
	public static final int APP_WEALTH_TOOLKIT_CREDIT_CALCULATOR = 124;  //信用计算器规则
	public static final int APP_RECRUITMENT = 125;  //关于我们---招贤纳士
	public static final int APP_PARTNERS = 126;  // 关于我们---合作伙伴
	public static final int APP_VERSION = 127;  //获取APP版本
	public static final int APP_SERVICE_HOTLINE = 128;  //获取客服热线
	public static final int APP_NEWS_DETAIL = 129;  //财富资讯新闻详情
	public static final int APP_WEALTH_INFO_HOME = 130;  //财富资讯首页
	public static final int APP_WEALTH_INFO_NEWS_LIST = 131;  //富资讯各个栏目下的新闻列表
	public static final int APP_RESET_SAFE_QUESTION = 132;  //通过邮箱重置安全问题
	public static final int APP_DELETE_BANK = 133;  //删除银行卡
	public static final int APP_OUTBOX_MSGS= 134;  //发件箱信息
	public static final int APP_OUTBOX_MSGS_DETAIL= 135;  //发件箱详情
	public static final int APP_SYSTEM_MSGS_DETAIL= 136;  //系统邮件详情
	public static final int APP_INBOX_MSGS_DETAIL= 137;  //收件箱详情
	public static final int APP_USER_INFO_STATUS= 138;  //用户状态
	public static final int APP_KITNET_CALCULATOR= 139;  //净值计算器
	public static final int APP_BID_QUESTIONS= 140;  //针对当前用户的所有借款提问
	public static final int APP_BID_QUESTIONS_DETAILS= 141;  //提问详情
	public static final int APP_CREDIT_ITEM= 142;  //审核科目积分明细
	public static final int APP_VIEW_OVER_BORROW= 143;  //查看超额申请详情
	public static final int APP_SUBMIT_WITHDRAWAL= 144;  //提现申请
	public static final int APP_WITHDRAWAL= 145;  //提现初始信息
	public static final int APP_WITHDRAWAL_RECORDS= 146;  //提现记录
	public static final int APP_FILE= 147;  //上传图片
	public static final int APP_DELETE_OUTBOX_SMGS= 148;  //删除发件箱站内信
	public static final int APP_OFFICIAL_ACTIVITY= 149;  //官方活动首页查询
	public static final int APP_CANCEL_ATTENTION_USERS= 150;  //取消关注用户
	public static final int APP_VIP_FEE= 151;  //获取vip相关信息
	public static final int APP_DELETE_BLACKLIST= 152;  //删除黑名单
	public static final int APP_DELETE_ATTENTION_BID= 153;  //删除收藏的标
	public static final int APP_DELETE_ATTENTION_DEBT= 154;  //删除收藏的标
	public static final int APP_PUSH_SETTINT= 155;  //保存推送设置
	public static final int APP_PUSH_QUERY= 156;  //获取推送设置
	public static final int APP_AUDIT_ITEMS= 157;  //提交用户未交费资料
	public static final int APP_FIRST_DEAl_DEBT = 158;  //债权用户初步成交债权，之后等待竞拍方确认成交
	public static final int APP_AUDIT_SUBMIT_UPLOADED_ITEMS = 159;  //提交用户资料
	public static final int APP_CLEAR_AUDIT_ITEMS = 160;  //用户取消提交资料
	public static final int APP_START_MAP = 161;  //APP用户启动图
	public static final int APP_ALL_PRODUCTS = 162;  //借款标类型
	public static final int APP_ALL_REPAYMENT_TYPES = 163;  //还款方式
	public static final int APP_ACOUNT_CAPITAL = 164;  //资金管理页面
	public static final int APP_ACOUNT_INCOME = 165; //查询收益查询
	public static final int APP_REGISTER_MOBILE = 166; //手机号码注册
	public static final int APP_BANK_PROVINCE_LIST = 167; //获取银行城市列表
	public static final int APP_PROVINCE_LIST = 168; //获取省份
	public static final int APP_CITY_LIST = 169; //获取城市
	public static final int APP_ADD_BANCARD_INFO = 170; //添加银行卡信息
	public static final int APP_BANK_LIST = 171; //获取银行列表
	public static final int APP_UPDATE_EMAIL = 172; //修改邮箱
	public static final int APP_UPDATE_MOBILE = 173; //修改手机号
	public static final int APP_PLATFORM_INFO = 174; //修改手机号
	
	
	public static final int APP_POSTS_SEARCH = 200;//论坛搜索
	public static final int APP_POSTS_FIRST = 201;//论坛首页
	public static final int APP_POSTS = 203;//论坛发帖
	public static final int APP_POSTS_UPDATE_NAME = 204;//修改昵称
	public static final int APP_POSTS_QUERY_NAME = 205;//查询昵称
	public static final int APP_USER_POSTS = 206;//我的帖子
	public static final int APP_USER_COLLECTION = 207;//用户收藏帖子
	public static final int APP_USER_COLLECTION_LIST = 208;//查询用户收藏帖列表
	public static final int APP_POSTS_INFO = 209;//根据id显示帖子内容
	public static final int APP_POSTS_ANSWERS = 210;//用户回帖
	
	public static final int APP_QUERY_H_BIDS = 211;//会员贷查询
	
	public static final int APP_MOBILE_EXIST = 212;//验证手机号码是否被注册
	public static final int APP_BID_USER_INFO = 213;//标的借款人信息
	public static final int APP_USER_RED_INFO = 214;//用户红包信息
	public static final int APP_REPAYMENT_CALCULATE = 215;//app还款计算
	public static final int APP_USER_HOME = 216;//app亿亿理财中心
	public static final int APP_USER_EXCHANGE_INFO = 217;//app用户兑换信息
	public static final int APP_USER_CPS = 218;//app用户显示cps推广
	public static final int APP_USER_SAVE_ADDRESS = 219;//app增加用户收货地址
	public static final int GET_MALL_PROVINCE_LIST = 220;//app查询积分商城省份地址
	public static final int QUERY_USER_ADDRESS = 221;//app查询用户收货地址
	public static final int DELETE_USER_ADDRESS = 222;//app删除用户收货地址
	public static final int USER_READ_ALL = 223;//app用户标记全读
	public static final int APP_USER_SEND_MSG = 224;//app用户意见反馈
	public static final int APP_INVEST_BILL_DETAILS = 225;//app用户理财账单详情
	public static final int APP_USER_ACCOUNT_HOME = 226;//app用户账户中心
	public static final int RED_PACKAGE_RULE = 230;//红包使用规则
	
	public static final int USER_REALITY_NAME = 231;//借款端理财中心 对应216 接口
	public static final int ADD_USER_REALITY_NAME = 232;//增加实名

	//---20190328新增
	public static final int APP_PROTOCOL_BANK_PROVINCE_LIST = 175; //借款端获取支持协议支付银行城市列表
		
		
	public static final int APP_RECHARGE = 176; //充值
	public static final int APP_SHOW_RECHARGE = 177; //充值
	
	public static final int APP_CPS_REWARD = 178;//查询cps奖励金额
	
	
	public static final int APP_REDPACKAGE_MYSELF = 300;//前台展示我的红包

	public static final int APP_SIGN_SCORE = 227; // 签到获取积分
	public static final int RECHARGE_INFO = 555; // 签到获取积分
	
	public static final int APP_USER_CPS_PROFIT_OLD = 1001;//app用户显示cps推广分成记录//老版app使用 18年4月份后新app不使用
	public static final int APP_USER_CPS_PROFIT = 1011;//app用户显示cps推广分成记录
	public static final int APP_USER_BANK_INSUR_DETAIL = 1002;//app用户银行卡投保详情
	
	public static final int APP_PUBLIC_BENEFIT_HOME = 1003;//app亿亿公益首页
	public static final int APP_PUBLIC_BENEFIT_LIST = 1004;//app亿亿公益活动列表
	public static final int APP_PUBLIC_BENEFIT_DETAIL = 1005;//app亿亿公益活动详情
	public static final int APP_PUBLIC_BENEFIT_RULE = 1006;//app亿亿公益活动规则
	
	public static final int APP_USER_INVEST_LIST = 1007;//理财账单
	public static final int APP_USER_INVEST_RETURNED = 1008;//回款计划
	public static final int APP_USER_INVEST_CERT = 1009;//获取电子存证URL
	
	
	
	public static final int APP_USER_SCORE = 1030;//我的积分
	public static final int APP_USER_SCORE_RECORD = 1031;//积分获取记录
	public static final int APP_USER_SIGN = 1032;//签到
	public static final int APP_DUIBA_URL = 1033;//我的兑换-兑吧
	public static final int APP_RISK_RESULT = 1034;//保存风险评测结果
	
	public static final int APP_PROTO_PAY_SMS = 1035;//获取协议支付验证码
	public static final int APP_PAY_LIMIT = 1050;//银行卡限额说明 
	public static final int APP_BORROW_PAY_LIMIT = 1051;//借款端银行卡限额说明（只支持协议支付） 
	
	public static final int APP_APPLY_DEBT_TRANSFER = 1036; //申请债权转让列表
	public static final int APP_DEBT_TRANSFER_PAY = 1037; //债权转让
	public static final int APP_DEBT_TRANSFER_LIST = 1038; //我的债权转让账单
	public static final int APP_DEBT_TRANSFER_DETAIL_V1 = 1039; //债权转让账单详情
	public static final int APP_BID_DETAIL_V1 = 1040; //投资详情
	public static final int APP_QUERY_DEBT_DETAIL = 1041; //债权转让标的详情
	public static final int APP_INVEST_DEBT_DETAIL = 1042;//债权转让投资详情
	public static final int APP_DEBT_REPAYMENT_CALCULATE = 1043; //债权投资计算器
	public static final int APP_DEBT_INVEST = 1044; //债权投资
	public static final int APP_DEBT_INVEST_DETAIL = 1045; //债权投资记录
	public static final int APP_DEBT_INVEST_RETURNED = 1046;//债权转让回款计划

	public static final int APP_USER_INVEST_LIST_BY_CONDITION=1047;//返回筛选投资列表
	public static final int APP_DEBT_SERVICE_AGREEMENT = 1048;  //用户债权协议
	public static final int CONTENT_SERVICE = 1049;  //内容服务
	
	public static final int APP_BID_USER_RISK = 1060;//标的用户风险评估
	
	public static final int SIGN_PROTOCOL = 1070;//用户扣款协议签约接口


	// --------------------------------借款端---------------------------------
	public static final int APP_BORROW_APPLY_LIST = 2006;//借款申请列表
	
	public static final int APP_BORROW_APPLY = 2000;//借款申请
	public static final int APP_BORROW_APPLY_SUBMIT = 2001;//借款申请提交
	public static final int APP_CERTIFICATION_PERSON = 2002;//个人实名认证
	public static final int APP_CERTIFICATION_COMPANY = 2003;//企业实名认证 
	
	public static final int APP_AREA_NEW = 2004;//企业实名认证获取地区列表 
	public static final int APP_BORROW_LOG = 2005;//我的借款日志
	
	public static final int USER_BASIC_INFO = 2007;//个人基础信息
	
	public static final int APP_BORROW_HOME = 3000;  // 借款app首页
	public static final int APP_BORROW_VERSION = 3001;  //获取APP版本
	public static final int APP_START_MAP_BORROW = 3002; //借款app启动图
	public static final int APP_IOS_AUDIT = 3003;//ios版本审核状态,是否通过

	
	// --------------------------------亿美贷---------------------------------
	public static final int APP_YMD_BASE_INFO_ENUM = 4001;//基础信息枚举
	public static final int APP_YMD_USER_INFO_SUBMIT = 4002;//基本信息保存
	public static final int APP_YMD_FLOW_NODE = 4003;//亿美贷APP流程节点
	public static final int CREDIT_APPLY = 4004;//亿美贷额度申请提交
	public static final int CREDIT_APPLY_SUBMIT = 4005;//借款申请提交
	public static final int USE_CREDIT = 4006;//用户打开使用额度的界面
	public static final int INCREASE_CREDIT_SUBMIT = 4007;//提额申请提交
	public static final int APP_YMD_USER_FILE_SUBMIT = 4008;//users用户风控补充资料,文件提交
	
	
	public static final int YMD_ORGANIZATION_LIST = 4100; //亿美贷机构列表
	public static final int YMD_ORG_PROJECT = 4101; //亿美贷机构项目
	public static final int YMD_PRO_INTEREST_AND_SERVICE_RATE = 4102; //项目和服务费期数、利息
}
