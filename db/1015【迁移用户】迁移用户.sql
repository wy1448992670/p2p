

insert into t_rights values(231,5,'会员迁移奔富权限',null,null);
insert into t_right_actions values (23101,231,'supervisor.userManager.InvestUser.migrateBenfuStep1','迁移用户锁定');
insert into t_right_actions values (23102,231,'supervisor.userManager.InvestUser.migrationExport','迁移用户数据导出');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (23200, 231, 'supervisor.dataStatistics.MigrateStatisticsAction.migrateUser', '迁移用户统计');


ALTER TABLE `t_users` 
ADD COLUMN `is_migration` bit(1) NULL DEFAULT b'0' COMMENT '是否迁移用户' AFTER `is_virtual_user`,
ADD COLUMN `migration_time` datetime(0) NULL COMMENT '迁移时间' AFTER `is_migration`;

INSERT INTO `t_user_detail_types`(`id`, `name`, `type`, `code`, `description`) VALUES (330, '迁移用户,回款转移', 2, NULL, '自动提现不审核流程');
INSERT INTO `t_user_detail_types`(`id`, `name`, `type`, `code`, `description`) VALUES (331, '迁移用户,余额转移', 2, NULL, '自动提现不审核流程');


create or replace view migration_investment_vo as 
select t_invests.id,t_users.id as userId,t_bids.id as bidId,t_bids.title as bidTitle,
	t_bids.repayment_type_id as repaymentTypeId, t_bids.period_unit as periodUnit,
	t_bids.period,t_invests.time as createTime, t_bids.audit_time as loanTime,
	t_bids.apr,
	case when activity1.rate is not null THEN true 
	when activity2.rate is not null THEN true 
	else t_bids.is_increase_rate end as isIncreaseRate,
	CASE WHEN activity1.rate IS NOT NULL THEN activity1.rate 
	ELSE t_bids.increase_rate END 
	+ IFNULL(activity2.rate,0) as increaseRate,
	t_invests.amount,t_invests.red_amount as redAmount,t_invests.correct_interest as interest,
	t_invests.correct_increase_interest as increaseInterest, false as isFinished ,
	null as finishedTime,0 as version
from t_bids 
inner join t_invests on t_bids.id=t_invests.bid_id
left JOIN t_activity_increase_rate_detail activity1 on t_invests.increase_activity_id1= activity1.id
left JOIN t_activity_increase_rate_detail activity2 on t_invests.increase_activity_id2= activity2.id
inner join t_users on t_users.id=t_invests.user_id
where (t_bids.status in (4,14) or (t_bids.status=5 and t_bids.last_repay_time>t_users.migration_time));


create or replace view migration_investment_bill_vo as 
select bill_i.id,bill_i.user_id as userId,bill_i.invest_id as migrationInvestmentId,bill_i.bid_id as bidId,
	bill_i.periods,bill_i.receive_time as receiveTime,bill_i.receive_corpus as receiveCorpus,
	bill_i.receive_interest as receiveInterest,bill_i.receive_increase_interest as receiveIncreaseInterest,
	case when bill_i.status in (0,-4) and bill_i.real_receive_time<t_users.migration_time then true else false end as isReceiveBefore,
	case when bill_i.status in (0,-4) and bill_i.real_receive_time<t_users.migration_time then true else false end as isReceive,
	bill_i.real_receive_time as realReceiveTime,0 as version
from t_bids 
inner join t_invests on t_bids.id=t_invests.bid_id
inner join t_users on t_users.id=t_invests.user_id
inner join t_bill_invests as bill_i on bill_i.invest_id=t_invests.id
where (t_bids.status in (4,14) or (t_bids.status=5 and t_bids.last_repay_time>t_users.migration_time));



