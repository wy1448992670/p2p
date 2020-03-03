insert into t_message_sms_templates values(7,now(),'后台还款自动充值成功','s_auto_recharge_for_bill_success','尊敬的userName:您于date成功支付money元,该笔支付用于bid_id号借款第period期借款账单的还款请求,请登录亿亿理财借款端查看还款进度.中亿云投资有限公司客服电话:021-6438-0510',0,1);

ALTER TABLE `t_user_recharge_details` 
ADD COLUMN `recharge_for_type` int(8) NULL COMMENT '后台自动充值原因:1.bill_id ' AFTER `order_no`,
ADD COLUMN `recharge_for_id` bigint(20) NULL COMMENT 'recharge_for_type=1: bill_id' AFTER `recharge_for_type`;

insert into t_enum_map values(6,0,6,'t_user_recharge_details.recharge_for_type','自动充值类型',0);
insert into t_enum_map values(1451,6,1,'t_bill.id','自动充值:账单还款',0);

--增加协议卡标记和代扣协议合同地址
ALTER TABLE `t_user_bank_accounts` 
ADD COLUMN `is_sign` bit(1) DEFAULT 0 COMMENT '是否签协议0   false  否 1是   true  ' AFTER `is_valid`,
ADD COLUMN `deduct_pact_url` varchar(255) DEFAULT NULL COMMENT '代扣协议地址' AFTER `is_sign`;