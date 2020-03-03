
-- ----------------------------
-- Table structure for t_statistic_data_disclosure
-- ----------------------------
DROP TABLE IF EXISTS `t_statistic_data_disclosure`;
CREATE TABLE `t_statistic_data_disclosure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `the_date` date NOT NULL COMMENT '统计当天',
  `available_balance` decimal(22,2) DEFAULT NULL COMMENT '站岗资金',
  `no_repay_money` decimal(12,2) DEFAULT NULL COMMENT '平台待还总额',
  `no_repay_corpus` decimal(12,2) DEFAULT NULL COMMENT '待还本金',
  `no_repay_interest` decimal(12,2) DEFAULT NULL COMMENT '待还利息',
  `day_recharge_money` decimal(12,2) DEFAULT NULL COMMENT '当天充值总额',
  `day_withdrawals_money` decimal(12,2) DEFAULT NULL COMMENT '当天提现总额',
  `day_in_money` decimal(12,2) DEFAULT NULL COMMENT '资金净流入',
  `day_invest_money` decimal(12,2) DEFAULT NULL COMMENT '当天投资总额',
  `total_registe_users` int(11) DEFAULT NULL COMMENT '累计注册用户',
  `day_registe_users` int(11) DEFAULT NULL COMMENT '当日注册用户',
  `borrowers` int(11) DEFAULT NULL COMMENT '借款总人数',
  `day_new_borrowers` int(11) DEFAULT NULL COMMENT '当天新增借款人数',
  `investers` int(11) DEFAULT NULL COMMENT '投资总人数',
  `day_new_investers` int(11) DEFAULT NULL COMMENT '当天新增投资人数',
  `product_total_money` decimal(12,2) DEFAULT NULL COMMENT '上标总额',
  `day_new_product_money` decimal(12,2) DEFAULT NULL COMMENT '当天上标总额',
  `product_total_count` int(11) DEFAULT NULL COMMENT '上标总数',
  `day_new_product_count` int(11) DEFAULT NULL COMMENT '当天上标总数',
  `total_overdue_money` decimal(12,2) DEFAULT NULL COMMENT '累计逾期',
  `norepay_overdue_money` decimal(12,2) DEFAULT NULL COMMENT '待还逾期',
  `tatal_overdua_count` int(11) DEFAULT NULL COMMENT '累计逾期笔数',
  `norepay_overdua_count` int(11) DEFAULT NULL COMMENT '待还逾期笔数',
  `day_repay_money` decimal(12,2) DEFAULT NULL COMMENT '当天还款总额',
  `day_borrow_money` decimal(12,2) DEFAULT NULL COMMENT '当天借款总额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;



ALTER TABLE `t_bids`
MODIFY COLUMN `audit_suggest`  varchar(4000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '审核意见' AFTER `audit_time`;

ALTER TABLE `t_users` 
ADD COLUMN `is_sms_blacklist` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '是否是短信黑名单 0：否  1：是' AFTER `cps_blacklist_dt`,
ADD COLUMN `sms_blacklist_dt` datetime(0) NULL COMMENT '黑名单添加或移除时间' AFTER `is_sms_blacklist`;

ALTER TABLE `t_red_packages_type` 
ADD COLUMN `received_money_start` decimal(8, 2) DEFAULT 0 COMMENT '回款本金 起始' AFTER `item_code`,
ADD COLUMN `received_money_end` decimal(8, 2) DEFAULT 0 COMMENT '回款本金 截止' AFTER `received_money_start`,
ADD COLUMN `all_invest_money` decimal(8, 2) DEFAULT 0 COMMENT '累计投资金额' AFTER `received_money_end`,
ADD COLUMN `all_invest_count` int(20) DEFAULT 0 COMMENT '累计投资笔数' AFTER `all_invest_money`,
ADD COLUMN `reg_time` int(20) DEFAULT 0 COMMENT '注册月数' AFTER `all_invest_count`;




INSERT INTO `t_rights` VALUES ('192', '8', '信披数据统计', null, null);
INSERT INTO `t_right_actions` VALUES ('12348', '192', 'supervisor.dataStatistics.OperationStatisticsAction.dataDisclosureStatistic', '信披数据统计');

INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) VALUES (193, 2, '短信黑名单', NULL, NULL);
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12349, 193, 'supervisor.webContentManager.TemplateAction.smsBlacklistTemplates', '短信黑名单');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12350, 193, 'supervisor.webContentManager.TemplateAction.addOrRemoveSmsBlacklist', '添加短信黑名单');



