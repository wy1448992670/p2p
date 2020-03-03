package constants;

public class SupervisorEvent {
	/**
	 * 网站内容管理
	 */
	public static final int ADD_NEWS = 1;//添加内容
	public static final int DEL_NEWS = 2;//删除内空
	public static final int EDIT_NEWS = 3;//编辑内容
	
	public static final int ADD_NEWSTYPE = 4;//添加类别
	public static final int id5 = 5;//添加子类别
	public static final int DEL_NEWSTYPE = 6;//删除类别
	public static final int EDIT_NEWSTYPE = 7;//编辑类别
	public static final int HIDE_NEWSTYPE = 8;//隐藏类别
	public static final int SHOW_NEWSTYPE = 9;//显示类别
	
	public static final int EDIT_ADS = 100;//编辑广告内
	public static final int CLOSE_USE_ADS = 101;//暂停广告内
	public static final int OPEN_USE_ADS = 102;//启用广告内
	public static final int EDIT_ENSURE = 103;//编辑4大安全保障
	public static final int CLOSE_USE_ENSURE = 104;//暂停4大安全保障
	public static final int OPEN_USE_ENSURE = 105;//启用4大安全保障
	public static final int ADD_PARTNER = 106;//添加合作伙伴
	public static final int DEL_PARTNER = 107;//删除合作伙伴
	public static final int EDIT_PARTNER = 108;//编辑合作伙伴
	public static final int EDIT_BOTTOMLINK = 109;//编辑底部链接
	public static final int ADD_BOTTOMLINK = 110;//添加底部链接
	
	public static final int SEND_MSG = 200;							//发送站内信
	public static final int GROUP_SEND_MSG = 201;					//群发站内信
	public static final int QUICKLY_SEND_MSG = 202;					//快捷站内信
	public static final int REPLY_MSG = 203;						//回复站内信
	public static final int DELETE_INBOX_MSG = 204;					//删除收件箱站内信
	public static final int DELETE_OUTBOX_MSG = 205;				//删除发件箱站内信
	public static final int REFUSE_MSG = 207;						//拒收会员站内信
	public static final int RECEIVE_MSG = 208;						//接收会员站内信
	
	public static final int CREATE_PRODUCT = 300;//添加借款标产品
	public static final int EDIT_PRODUCT = 301;//编辑借款标产品
	public static final int NOT_ENABLE_PRODUCT  = 302;//暂停借款标产品
	public static final int ENABLE_PRODUCT  = 303;//启用借款标产品
	public static final int CREATE_LABLE = 304;//添加产品标签
	public static final int EDIT_LABLE = 304;//编辑产品标签
	//public static final int id305 = 305;//删除产品标签
	public static final int CREATE_FILED = 306;//添加产品字段
	public static final int EDIT_FILED = 306;//编辑产品字段
	//public static final int id307 = 307;//删除产品字段
	
	public static final int CREATE_AUDIT_ITEM = 314;//添加审核科目
	public static final int EDIT_AUDIT_ITEM = 315;//编辑审核科目
	public static final int NOT_ENABLE_AUDIT_ITEM = 316;//暂停审核科目
	public static final int ENABLE_AUDIT_ITEM = 317;//启用审核科目
	
	public static final int CREATE_CREDIT_LEVEL = 318;				//添加信用等级
	public static final int EDIT_CREDIT_LEVEL = 319;				//编辑信用等级
	public static final int ENABLE_CREDIT_LEVEL = 320;				//暂停信用等级
	public static final int DISABLE_CREDIT_LEVEL = 321;				//启用信用等级
	public static final int EDIT_CREDIT_SCORE_RULE = 322;			//设置信用积分规则
	public static final int id323 = 323;//设置借款标常量
	
	public static final int EDIT_EMAIL_TEMPLATE = 400;				//编辑邮件模板
	public static final int DISABLE_EMAIL_TEMPLATE = 401;			//暂停邮件模板
	public static final int ENABLE_EMAIL_TEMPLATE = 402;			//启用邮件模板
	public static final int EDIT_SMS_TEMPLATE = 403;				//编辑短信模版
	public static final int DISABLE_SMS_TEMPLATE = 404;				//暂停短信模版
	public static final int ENABLE_SMS_TEMPLATE = 405;				//启用短信模版
	public static final int EDIT_MSG_TEMPLATE = 406;				//编辑站内信模版
	public static final int DISABLE_MSG_TEMPLATE = 407;				//暂停站内信模版
	public static final int ENABLE_MSG_TEMPLATE = 408;				//启用站内信模版
	public static final int CREATE_MSG_TEMPLATE = 409;				//添加站内信模版
	
	
	public static final int CREATE_FORUM_POSTS = 501;				//创建论坛帖子
	public static final int EIDT_FORUM_POSTS = 501;				//编辑论坛帖子
	public static final int DELETE_FORUM_POSTS = 502;				//删除论坛帖子
	
	/**
	 * 借款标管理
	 */
	public static final int AUDIT_BID = 1000;//审核借款标（审核通过、审核不通过、借款中不通过、提前在线借款）
	public static final int SET_HOT_BID = 1001;//标记借款标为火
	public static final int SET_QUALITY_BID = 1002;//设置借款标为优质标
	public static final int id1003 = 1003;//打印借款标
	
	public static final int CREATE_AGENCY_BID = 1100;//发布机构合作标
	public static final int CREATE_AGENCY = 1101;//添加合作机构
	public static final int NOT_ENABLE_AGENCY= 1102;//暂停合作机构
	public static final int ENABLE_AGENCY = 1103;//启用合作机构
	
	public static final int AUDIT_DEBT_TRANSFER = 1200;//审核债权转让标
	public static final int MARK_QUALITY_DEBT = 1201;//设为优质债权转让标
	public static final int id1202 = 1202;//重审债权转让标
	public static final int CHANGE_DEBT_TRANSFER_STATE = 1203;//债权转让标,改变标的状态
	
	public static final int AUDIT_USER_ITEM = 1300;//审核资料（可见通过、不可见通过，不通过）
	public static final int id1301 = 1301;//下载资料
	
	public static final int AUDIT_OVER_BORROW = 1400;				//审核超额借款（通过、不通过）
	
	public static final int NOT_ENABLE_REPAYMENT_TYPE = 1401; //不启用还款类型	
	public static final int ENABLE_REPAYMENT_TYPE = 1402; // 启用还款类型
	public static final int CREATE_PURPOSE = 1403; // 添加借款用途
	public static final int EDIT_PURPOSE = 1404;// 编辑借款用途
	public static final int NOT_ENABLE_PURPOSE = 1405; //不启用还款类型	
	public static final int ENABLE_PURPOSE = 1406; // 启用还款类型
	public static final int SET_AUDIT_MECHANISM = 1407; // 设置审核机制
	public static final int SET_NUMBER = 1408; // 设置字母
	

	
	/**
	 * 账单催收管理
	 */
	public static final int BILL_COLLECTION = 2000;//账单催收（发站内信、发邮件、打电话）
	public static final int MAKE_BILL_OVER = 2001;//标记账单为逾期
	public static final int MAKE_BILL_BAD = 2002;//标记账单为坏账
	public static final int ASSIGN_BID = 2003;//分配借款标
	public static final int ASSIGN_USER = 2004;//分配会员所有借款标
	public static final int ADD_CUSTOMER = 2005;					//添加客服
	public static final int REASSIGN_USER = 2006;//重新分配会员所有借款标
	public static final int SIMULATE_CUSTOMER_LOGIN = 2009;			//模拟客服登录
	
	/**
	 * 会员管理
	 */
//	public static final int id3000 = 3000;//给用户发站内信
	public static final int SEND_EMAIL = 3001;//给用户发邮件
//	public static final int id3002 = 3002;//给用户发短信
	public static final int RESET_PASSWORD = 3003;//重置用户密码
	public static final int SIMULATE_LOGIN = 3004;//模拟用户登录
	public static final int LOCK_USER = 3005;//锁定用户
	public static final int OPEN_USER = 3006;//启用用户
	public static final int VERIFIED_EMAIL = 3007;//手动激活用户
	public static final int ADD_BLACKLIST = 3008;//添加黒名单
	public static final int DELETE_BLACKLIST = 3009;//解除黑名单
	
	/**
	 * 财务管理
	 */
	public static final int id4000 = 4000;//编辑借款人放款账号
	public static final int REPAYMENT_FUND = 4001;//放款
	public static final int OFFLINE_COLLECTION = 4002;//应收账单管理-线下收款
	public static final int PRINCIPAL_PAY = 4003;//应付账单管理-本金垫付
	public static final int AUDIT_WITHDRAWAL = 4004;//审核提现（通过、不通过）
	public static final int NOTICE_WITHDRAWAL = 4005;//付款通知（短信、站内信、邮件）
	public static final int PRINT_PAYMENT = 4006;//打印付款单
	public static final int ROLLBACK = 4007;//退回
	public static final int HAND_RECHARGE = 4008;					//手工充值
	public static final int ADD_MONEY = 4009;//添加保障金
	public static final int REPAYMENT_FUND_SIGN = 4010;//放款标记
	
	public static final int PAYMENT_ON_COMPANY = 4011;//公司垫付
	
	/**
	 * 平台推广
	 */
	public static final int CPS_SETTING = 5000;//CPS推广规则设置
	public static final int id5001 = 5001;//编辑流量统计代码
	public static final int SEC = 5002;//SEO优化设置
	
	/**
	 * 数据统计
	 */
	public static final int id6000 = 6000;//#******数据统计******
	
	/**
	 * 系统设置
	 */
	public static final int id7000 = 7000;//制作安全云盾
	public static final int id7001 = 7001;//编辑安全云盾
	public static final int id7002 = 7002;//删除安全云盾
	public static final int SAFE_PARAMETER = 7003;//安全参数设置
	public static final int ADD_SECRET_QUESTION = 7004;//添加安全问题
	public static final int UPDATE_QUESTION_STATUS = 7005;//暂停/启用安全问题
//	public static final int id7006 = 7006;//启用安全问题
	public static final int DB_CLEAR = 7007;//清空数据
	public static final int DB_RESET = 7008;//还原出厂初始数据
	public static final int DB_RECOVER = 7009;//还原运营数据
	public static final int DB_BACKUP = 7010;//备份数据
	public static final int FEE_SETTINT = 7011;//平台服务费设置
	public static final int PAYMENT_SET = 7012;//支付方式设置
	public static final int REPAY_TYPE = 7013;//应付账单设置
	public static final int id7014 = 7014;//资金托管账户设置
	public static final int SCORE_RULE = 7015;//系统积分规则设置
	
	public static final int LOGIN = 7500;							//登录
	public static final int LOGOUT = 7501;							//注销
	public static final int CREATE_SUPERVISOR = 7502;				//添加管理员
	public static final int DELETE_SUPERVISOR = 7503;				//删除管理员
	public static final int EDIT_SUPERVISOR = 7504;					//编辑管理员
	public static final int GRANT_TO_SUPERVISOR = 7505;				//给管理员分配权限
	public static final int EDIT_SUPERVISOR_GROUP = 7506;			//编辑管理员权限组
	public static final int DISABLE_SUPERVISOR = 7507;				//锁定管理员
	public static final int ENABLE_SUPERVISOR = 7508;				//启用管理员
	public static final int RESET_SUPERVISOR_PASSWORD = 7509;		//重置管理员密码
	public static final int CREATE_RIGHT_GROUP = 7510;				//添加权限组
	public static final int DELETE_RIGHT_GROUP = 7511;				//删除权限组
	public static final int EDIT_RIGHT_GROUP = 7512;				//编辑权限组
	public static final int GRANT_TO_RIGHT_GROUP = 7513;			//给权限组分配权限
	public static final int EDIT_RIGTH_GROUP_ROSTER = 7514;			//编辑权限组名单
	
	public static final int SMS_CHANNEL = 7600;//短信发送通道设置
	public static final int MAIL_CHANNEL = 7601;//邮件发送通道设置
	public static final int DELETE_EVENT = 7602;					//删除操作日志
	public static final int PLAT_DATA = 7603;//网贷软件系统基本资料设置
	public static final int AUTHORIZE = 7604;//正版软件认证
	
	/**
	 * OBU风控联盟
	 */
	public static final int id8000 = 8000;//OBU风控联盟
}
