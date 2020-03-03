#临时表
CREATE TABLE `t_user_cps_profit_temp` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '投资用户ID',
  `user_mobile` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '投资用户手机号',
  `recommend_user_id` bigint(20) DEFAULT NULL COMMENT '推荐人id',
  `bid_id` bigint(20) DEFAULT NULL COMMENT '投资标ID',
  `bid_title` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '投资标名称',
  `bid_is_loan` int(11) DEFAULT NULL COMMENT '是否已放款 1是 0否',
  `invest_id` bigint(20) DEFAULT NULL COMMENT '账单ID',
  `invest_corpus` decimal(20,2) DEFAULT NULL COMMENT '实际投资金额',
  `invest_interest` decimal(20,2) DEFAULT NULL COMMENT '实际投资收益',
  `invest_time` datetime DEFAULT NULL COMMENT '投资时间',
  `cps_reward` decimal(20,2) DEFAULT NULL COMMENT '奖励金额',
  `cps_rate` decimal(20,2) DEFAULT NULL COMMENT '奖励分成比例',
  `ins_dt` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cps_uri` (`user_id`,`recommend_user_id`,`invest_id`) USING BTREE,
  KEY `recommend_user_id` (`recommend_user_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12548 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;

#备份表
CREATE TABLE `t_user_cps_profit_backup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '投资用户ID',
  `user_mobile` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '投资用户手机号',
  `recommend_user_id` bigint(20) DEFAULT NULL COMMENT '推荐人id',
  `bid_id` bigint(20) DEFAULT NULL COMMENT '投资标ID',
  `bid_title` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '投资标名称',
  `bid_is_loan` int(11) DEFAULT NULL COMMENT '是否已放款 1是 0否',
  `invest_id` bigint(20) DEFAULT NULL COMMENT '账单ID',
  `invest_corpus` decimal(20,2) DEFAULT NULL COMMENT '实际投资金额',
  `invest_interest` decimal(20,2) DEFAULT NULL COMMENT '实际投资收益',
  `invest_time` datetime DEFAULT NULL COMMENT '投资时间',
  `cps_reward` decimal(20,2) DEFAULT NULL COMMENT '奖励金额',
  `cps_rate` decimal(20,2) DEFAULT NULL COMMENT '奖励分成比例',
  `ins_dt` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cps_uri` (`user_id`,`recommend_user_id`,`invest_id`) USING BTREE,
  KEY `recommend_user_id` (`recommend_user_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12548 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;

#备份老数据
insert into t_user_cps_profit_backup select * from t_user_cps_profit;

#将[新定义的数据]插入临时表
insert into t_user_cps_profit_temp 
select null,profit.user_id,min(profit.user_mobile) user_mobile,profit.recommend_user_id,profit.bid_id
	,min(profit.bid_title) bid_title,1 as bid_is_loan,invest.id as invest_id
	,sum(profit.invest_corpus) invest_corpus,sum(profit.invest_interest) invest_interest
	,(select audit_time from t_bids where t_bids.id=profit.bid_id) as invest_time
	,sum(profit.cps_reward) cps_reward,min(profit.cps_rate) cps_rate,min(profit.ins_dt) ins_dt
from t_user_cps_profit profit
inner join t_bill_invests bill_invest on bill_invest.id=profit.invest_id
inner join t_invests invest on invest.id=bill_invest.invest_id
group by profit.user_id,profit.recommend_user_id,profit.bid_id
	,invest.id;
	
#将[新定义的数据]插入t_user_cps_profit
delete from t_user_cps_profit;
insert into t_user_cps_profit select * from t_user_cps_profit_temp;

#插入[推荐关系启动时间]
INSERT INTO `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10060', 'cps_relation_start_date', '2018-06-06', '推荐关系启动时间');

