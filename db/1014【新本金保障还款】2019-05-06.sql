insert into t_supervisor_event_types values (4011,'公司垫付',null,null,0);
insert into t_platform_detail_types values(20,'公司垫付理财账单',3,'复合型,公司垫付:支出 公司垫付后还款:收入');

ALTER TABLE `t_bills` 
ADD COLUMN `is_payment_on_company` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公司垫付' AFTER `real_repayment_interest`,
ADD COLUMN `payment_on_company_need_repaid` tinyint(1)  NOT NULL DEFAULT '0' COMMENT '是否需要公司垫付后还款,true需要(未还),false不需要(已还)' AFTER `is_payment_on_company`,
ADD COLUMN `payment_on_company_repaid_time` datetime  NULL COMMENT '公司垫付后还款时间' AFTER `payment_on_company_need_repaid`,
ADD COLUMN `is_offline` tinyint(1) NOT NULL COMMENT '是否线下收款' AFTER `payment_on_company_repaid_time`;

#垫付账单列表视图
CREATE 
	OR REPLACE VIEW `v_bill_advance` AS SELECT
	`a`.`id` AS `id`,
	YEAR ( `a`.`repayment_time` ) AS `year`,
	MONTH ( `a`.`repayment_time` ) AS `month`,
	`b`.`id` AS `bid_id`,
	concat( `d`.`_value`, cast( `a`.`id` AS CHAR charset utf8 ) ) AS `bill_no`,
	`c`.`name` AS `name`,
	`c`.`mobile` AS `mobile`,
	`c`.`reality_name` AS `reality_name`,
	concat( `e`.`_value`, cast( `b`.`id` AS CHAR charset utf8 ) ) AS `bid_no`,
	`b`.`amount` AS `amount`,
	`b`.`apr` AS `apr`,
	`a`.`title` AS `title`,
	( ( ( `a`.`repayment_corpus` + `a`.`repayment_interest` ) + `a`.`service_amount` ) + `a`.`overdue_fine` ) AS `repayment_money`,
	( SELECT concat( `a`.`periods`, '/', count( `t1`.`id` ) ) FROM `t_bills` `t1` WHERE ( `t1`.`bid_id` = `a`.`bid_id` ) ) AS `period`,
	`a`.`repayment_time` AS `repayment_time`,
	`a`.`repayment_interest` AS `repayment_interest`,
	`a`.`repayment_corpus` AS `repayment_corpus`,
	`a`.`service_amount` AS `service_amount`,
	`b`.`tag` AS `tag`,
	`b`.`is_drainage` AS `is_drainage`,
	( SELECT `u`.`reality_name` FROM `t_users` `u` WHERE ( `u`.`id` = `b`.`consociation_user_id` ) ) AS `consociation_user_mobile`,
	( CASE WHEN ( ( `a`.`repayment_time` - now( ) ) > 0 ) THEN 0 ELSE ( to_days( now( ) ) - to_days( `a`.`repayment_time` ) ) END ) AS `overdue_time`,
	( SELECT count( `t_bills`.`id` ) AS `count(id)` FROM `t_bills` WHERE ( ( `t_bills`.`bid_id` = `b`.`id` ) AND ( `t_bills`.`overdue_mark` IN ( - ( 1 ),- ( 2 ),- ( 3 ) ) ) ) ) AS `overdue_count`,
	( SELECT `a`.`name` AS `name` FROM `t_supervisors` `a` WHERE ( `c`.`assigned_to_supervisor_id` = `a`.`id` ) ) AS `supervisor_name`,
	( SELECT `a`.`name` AS `name` FROM `t_supervisors` `a` WHERE ( `b`.`manage_supervisor_id` = `a`.`id` ) ) AS `supervisor_name2`,
	`a`.`is_payment_on_company` AS `is_payment_on_company`,
	`a`.`payment_on_company_need_repaid` AS `payment_on_company_need_repaid` 
FROM
	( ( ( ( `t_bills` `a` JOIN `t_bids` `b` ON ( ( `a`.`bid_id` = `b`.`id` ) ) ) JOIN `t_users` `c` ON ( ( `b`.`user_id` = `c`.`id` ) ) ) JOIN `t_system_options` `d` ) JOIN `t_system_options` `e` ) 
WHERE
	( ( `d`.`_key` = 'loan_bill_number' ) AND ( `e`.`_key` = 'loan_number' ) AND ( `a`.`is_payment_on_company` = 1 ) ) 
GROUP BY
	`a`.`id`;
	

insert into t_rights values(109,6,'应收账单管理-账单代扣',null,null);
insert into t_rights values(110,6,'应收账单管理-公司垫付',null,null);
insert into t_rights values(111,6,'应收账单管理-垫付后代扣',null,null);
insert into t_rights values(112,6,'应收账单管理-垫付后线下收款',null,null);

insert into t_right_actions values(10900,109,'supervisor.financeManager.ReceivableBillManager.autoReturnMoney','应收账单管理-账单代扣');
insert into t_right_actions values(11000,110,'supervisor.financeManager.ReceivableBillManager.paymentOnCompany','应收账单管理-公司垫付');
insert into t_right_actions values(11100,111,'supervisor.financeManager.ReceivableBillManager.autoReturnMoneyAfterPaymentOnCompany','应收账单管理-垫付后代扣');
insert into t_right_actions values(11200,112,'supervisor.financeManager.ReceivableBillManager.offlineReceiveAfterPaymentOnCompany','应收账单管理-垫付后线下收款');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('5112', '102', 'supervisor.financeManager.ReceivableBillManager.advanceBills', '垫付账单列表');
