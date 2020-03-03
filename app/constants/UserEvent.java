package constants;

public class UserEvent {
	
	/*命名规范：添加   ADD  修改 EDIT 删除 DELETE*/
	
	public static final int REGISTER = 1; //注册
	public static final int LOGIN = 2; //登录
	public static final int LOGOUT = 3; //退出
	public static final int VERIFIED_EMAIL = 4; //激活邮箱
	public static final int t5 = 5; //邮箱找回用户名
	public static final int t6 = 6; //手机找回用户名
	public static final int t7 = 7; //手机重置密码
	public static final int t8 = 8; //邮箱重置密码
	public static final int ADD_BASIC_INFORMATION = 9; //填写基本资料
	public static final int ADD_QUESTION = 10; //添加安全问题
	public static final int EDIT_QUESTION = 11; // 修改安全问题
	public static final int EDIT_PASSWORD = 12; // 修改密码
	public static final int ADD_PAY_PASSWORD = 13; // 添加交易密码
	public static final int EDIT_PAY_PASSWORD = 14; // 修改交易密码
	public static final int EDIT_EMAIL = 15; // 修改绑定邮箱
	public static final int t16 = 16; // 添加绑定手机
	public static final int EDIT_MOBILE = 17; // 修改绑定手机
	public static final int VIP = 18; // 申请vip
	public static final int t19 = 19; // 续费vip
	public static final int APPLY_FOR_OVER_BORROW = 20; 	// 申请超额借款
	public static final int ATTENTION_USER = 21; // 关注用户
	public static final int CANCEL_ATTENTION = 22; // 取消关注用户
	public static final int EDIT_NOTE_NAME = 23; // 修改关注用户备注名
	public static final int ADD_BLACKLIST = 24; // 添加黑名单
	public static final int DELETE_BLACKLIST = 25; // 删除黑名单
	public static final int SEND_MSG = 26; 					// 发送站内信
	public static final int ADD_BANK = 27; // 添加银行账户
	public static final int EDIT_BANK = 28; // 编辑银行账户
	public static final int DELETE_BANK = 29; // 删除银行账号
	public static final int RECHARGE = 30; // 充值
	public static final int APPLY_WITHDRAWALT = 31; // 申请提现
	public static final int COLLECT_BID = 32; // 收藏借款标
	public static final int COLLECT_DEBT = 33; // 收藏债权
	public static final int REPORT_USER = 34; // 举报会员
	public static final int RESET_PAY_PASSWORD = 35; // 重置支付密码
	public static final int EDIT_PHOTO = 36; // 更换头像
	public static final int RESET_PASSWORD_MOBILE = 37; // 手机重置用户密码
	public static final int RESET_PASSWORD_EMAIL = 38; // 邮箱重置用户密码
	public static final int RESET_QUESTION = 38; // 重置安全问题
	public static final int IPS_ACCT_NO = 39; // 添加ips账号
	public static final int IPS_BID_AUTH_NO = 40; // 添加自动投标签约号
	public static final int IPS_REPAY_AUTH_NO = 41; // 添加自动还款签约号
	public static final int ROLLBACK_WITHDRAWALT = 42; // 提现回退
	public static final int AUTO_APPLY_WITHDRAWALT = 43; //自动申请提现
	
	/*----------站内信-------------*/
	public static final int MARK_MSG_READED = 101; 			// 标记为已读
	public static final int MARK_MSG_UNREAD = 102;			// 标记为未读
	public static final int DELETE_INBOX_MSG = 103; 		// 删除收件箱消息
	public static final int DELETE_OUTBOX_MSG = 104; 		// 删除发件箱消息
	public static final int REPLY_MSG = 105; 				// 回复站内信
	
	/*----------借款-------------*/
	public static final int ADD_BID = 151; // 发布借款标
	public static final int REPEAL_BID = 152; // 撤销借款标
	public static final int FLOW_BID = 153; // 流标
	public static final int SUMBIT_AUDIT_ITEM = 154; // 提交审核资料
	public static final int DELETE_AUDIT_ITEM = 155; // 删除审核资料
	public static final int ANSWERS_TO_QUESTION = 156; // 回答借款提问
	public static final int ADD_USER_AUDIT_ITEM = 156; // 添加用户需上传的审核资料
	public static final int DELETE_ANSWERS_TO_QUESTION = 156; // 删除借款提问

	/*----------投标-------------*/
	public static final int INVEST = 201; // 投标
	public static final int QUESTION_TO_BORROWER = 202; // 向借款人提问

	/*----------债权-------------*/
	public static final int AUCTION_MODE = 203; // 竞拍债权转让
	public static final int DIRECTIONAL_MODE = 204; // 定向债权转让
	public static final int AUCTION = 205; // 竞拍
	public static final int ACCEPT_DEBT_TRANSFER = 206; // 接受定向转让
	public static final int DEAL_DEBT_TRANSFER = 210; // 成交债权
	public static final int REFUSE_DEBT = 209; // 拒绝接受定向转让
	public static final int OPEN_ROBOT = 207; // 开启自动投标
	public static final int CLOSE_ROBOT = 208; // 关闭自动投标

	/*----------还款-------------*/
	public static final int ADD_NOMAL_PAY = 251; // 还款
}
