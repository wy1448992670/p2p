
CREATE TABLE `t_debt_bill_invest` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '债权投资还款ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '投资用户ID',
  `debt_invest_id` bigint(20) DEFAULT NULL COMMENT '债权投资ID',
  `debt_id` bigint(20) DEFAULT NULL COMMENT '债权ID',
  `old_bill_id` bigint(20) DEFAULT NULL COMMENT '原账单ID',
  `periods` tinyint(4) DEFAULT '0' COMMENT '期数',
  `title` varchar(50) CHARACTER SET utf8 DEFAULT NULL COMMENT '标题',
  `receive_time` datetime DEFAULT NULL COMMENT '收款时间',
  `receive_corpus` decimal(20,2) DEFAULT '0.00' COMMENT '本期应收款金额',
  `receive_interest` decimal(20,2) DEFAULT '0.00' COMMENT '本期应收利息',
  `receive_increase_interest` decimal(20,2) DEFAULT '0.00' COMMENT '应收加息利息',
  `status` tinyint(11) DEFAULT '-1' COMMENT '收款状态:-1 未收款 -2 逾期未收款-3 本金垫付收款-4 逾期收款 -5 待收款 -6 逾期待收款 -7 已转让出 0 正常收款',
  `overdue_fine` decimal(20,2) DEFAULT '0.00' COMMENT '逾期的罚款',
  `real_receive_time` datetime DEFAULT NULL COMMENT '实际收款时间',
  `real_receive_corpus` decimal(20,2) DEFAULT '0.00' COMMENT '实际收款本金',
  `real_receive_interest` decimal(20,2) DEFAULT '0.00' COMMENT '实际收款利息',
  `is_all_receiver` bit(1) DEFAULT b'0' COMMENT '是否全部属于受让人(1是 0否)',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=756 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;



CREATE TABLE `t_debt_invest` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '债权投资ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '投资用户ID',
  `old_user_id` bigint(20) DEFAULT NULL COMMENT '原投资用户ID',
  `debt_id` bigint(20) DEFAULT NULL COMMENT '债权标的ID',
  `invest_id` bigint(20) DEFAULT NULL COMMENT '原标的投资记录ID',
  `bid_id` bigint(20) DEFAULT NULL COMMENT '原标的ID',
  `amount` decimal(20,2) DEFAULT NULL COMMENT '投资金额',
  `red_amount` decimal(20,2) DEFAULT NULL COMMENT '红包金额',
  `time` datetime DEFAULT NULL COMMENT '投资时间',
  `correct_amount` decimal(20,2) DEFAULT '0.00' COMMENT ' 纠正本金',
  `correct_interest` decimal(20,2) DEFAULT '0.00' COMMENT '纠偏利息',
  PRIMARY KEY (`id`),
  KEY `debt_id` (`debt_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=330 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;


CREATE TABLE `t_debt_transfer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '债权转让ID',
  `no` varchar(20) DEFAULT NULL COMMENT '编号',
  `bid_id` bigint(20) DEFAULT NULL COMMENT '标的ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '债权人',
  `invest_id` bigint(20) DEFAULT '0' COMMENT '投资ID',
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `accrual_time` timestamp NULL DEFAULT NULL COMMENT '开始计息时间',
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '转让标题',
  `transfer_reason` varchar(255) DEFAULT NULL COMMENT '转让原因',
  `debt_amount` decimal(20,2) DEFAULT '0.00' COMMENT '债权金额',
  `apr` decimal(20,2) DEFAULT NULL COMMENT '年利率',
  `period` int(11) DEFAULT NULL COMMENT '转让期限（天）（剩余期限）',
  `bid_period` int(20) DEFAULT NULL COMMENT '标的期限（天）(借款期限)',
  `min_invest_amount` decimal(10,2) DEFAULT NULL COMMENT '起投金额',
  `repayment_type` int(4) DEFAULT NULL COMMENT '还款方式',
  `complete_period` int(20) DEFAULT NULL COMMENT '标的已经完成的期限（天）(完成期限)',
  `remain_amount` decimal(20,2) DEFAULT NULL COMMENT '剩余金额(可投金额)',
  `loan_schedule` decimal(6,2) DEFAULT NULL COMMENT '借款进度比例(冗余)',
  `has_invested_amount` decimal(20,2) DEFAULT NULL COMMENT '已投总额(冗余)',
  `audit_status` int(10) DEFAULT NULL COMMENT '审核状态（1未审核 2 初审通过 3初审不通过 4复审通过 5复审不通过）',
  `audit_time` timestamp NULL DEFAULT NULL COMMENT '初审时间',
  `audit_admin` bigint(50) DEFAULT NULL COMMENT '初审人(id)',
  `recheck_time` timestamp NULL DEFAULT NULL COMMENT '复审时间',
  `recheck_admin` bigint(50) DEFAULT NULL COMMENT '复审人（id）',
  `withdraw_time` timestamp NULL DEFAULT NULL COMMENT '撤标时间',
  `withdraw_admin` bigint(50) DEFAULT NULL COMMENT '撤标人',
  `is_only_new_user` bit(1) DEFAULT b'0' COMMENT '是否新手标  0  不是  1  是',
  `transfer_rate` decimal(20,2) DEFAULT NULL COMMENT '本金转让费',
  `red_amount` decimal(20,2) DEFAULT NULL COMMENT '红包金额（原投资红包）',
  `increase_rate` decimal(20,2) DEFAULT NULL COMMENT '加息费用',
  `deadline` timestamp NULL DEFAULT NULL COMMENT '截止日期（系统自动生成）',
  `reason` varchar(255) DEFAULT NULL COMMENT '拒绝原因',
  `status` int(4) DEFAULT NULL COMMENT '标的状态（1待审核 2转让中 3成功，包括:还款中，已还款 4未通过 5 转让失败（流标，撤标）',
  `real_invest_expire_time` datetime DEFAULT NULL COMMENT '满标时间',
  `current_period` int(4) DEFAULT NULL COMMENT '当前转让期数（从第几期开始转让的）',
  PRIMARY KEY (`id`),
  KEY `id_debt_transfer` (`id`) USING BTREE,
  KEY `debt_invest_id` (`invest_id`) USING BTREE,
  KEY `debt_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=242 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

ALTER TABLE  `t_invests` 
ADD COLUMN `transfers_time` datetime(0) NULL COMMENT '转让计息时间' AFTER `transfers_id`;

ALTER TABLE `t_bids`
ADD COLUMN `is_debt_transfer`  bit(1) NULL DEFAULT b'0' COMMENT '是否债权转让(1是 0否)';

ALTER TABLE  `t_bill_invests`
MODIFY COLUMN `status` tinyint(11) NULL DEFAULT -1 COMMENT '收款状态:-1 未收款 -2 逾期未收款-3 本金垫付收款-4 逾期收款 (不使用-5 待收款 -6 逾期待收款) -7 已转让出 -8 转让中 0 正常收款' AFTER `receive_increase_interest`;

-- 2018-09-13 修改
ALTER TABLE `t_invests` ADD COLUMN `red_amount`  decimal(20,2) NULL DEFAULT 0.00 COMMENT '使用红包金额' AFTER `amount`;



#1 收入 2 支出 3 冻结 4 解冻

insert into t_user_detail_types values(15,'债权转让投资回款',1,null,null);				#DEBT_TRANSFER_RECEIVE 债权转让投资回款     (t_debt_bill_invest id)
insert into t_user_detail_types values(111,'解冻债权转让投标金额',4,null,null);			#THAW_FREEZE_DEBT_INVESTAMOUNT 解冻债权转让投标金额   (t_debt_invests id)
insert into t_user_detail_types values(112,'平台退还债权转让管理费',1,null,null);			#RETURN_DEBT_TRANSFER_MANAGEFEE 平台退还债权转让管理费 (t_debt_transfer id)
insert into t_user_detail_types values(113,'债权转让成功,增加转让人金额',1,null,null);	#ADD_DEBT_TRANSFER_FUND 债权转让成功,增加转让人金额 (t_debt_transfer id)
insert into t_user_detail_types values(207,'冻结债权投标金额',3,null,null);				#FREEZE_DEBT_INVEST 冻结债权投标金额   (t_debt_invests id）
insert into t_user_detail_types values(323,'扣除债权转让投标冻结金额',2,null,null);		#CHARGE_DEBT_INVEST_FUND 扣除债权转让投标冻结金额(t_debt_invests id)
 

update t_bids set is_debt_transfer = 0;


