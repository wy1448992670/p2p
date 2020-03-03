##新增用户风险评测类型字段
ALTER TABLE `t_users` 
ADD COLUMN `risk_type` int(2) NULL COMMENT '风险评测类型  1：安全型 2：保守型 3：稳健型 4：积极型 5：进取型' AFTER `forum_name`;

##更新用户历史评测数据
update t_users set
risk_type = (
case risk_result
when '保守型' then 1
when '稳健型' then 2
when '平衡型' then 3
when '积极型' then 4
when '激进型' then 5
end
),
risk_result = (
case risk_result
when '保守型' then '安全型'
when '稳健型' then '保守型'
when '平衡型' then '稳健型'
when '积极型' then '积极型'
when '激进型' then '进取型'
end
)
where risk_result is not null;

##投资表加bid_id索引
ALTER TABLE `t_invests` 
ADD INDEX `index_bid_id`(`bid_id`) USING BTREE;

##bid表加bid_risk_id字段,标的分险等级
ALTER TABLE `t_bids` 
ADD COLUMN `bid_risk_id` bigint(4) NULL DEFAULT NULL AFTER `is_debt_transfer`;

##bid表加status,bid_risk_id索引
ALTER TABLE `t_bids` 
ADD INDEX `status`(`status`) USING BTREE,
ADD INDEX `bid_risk_id`(`bid_risk_id`) USING BTREE;

##标的风险等级表
CREATE TABLE `t_bid_risk` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

##用户风险等级表
CREATE TABLE `t_user_risk` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 NOT NULL,
  `description` varchar(255) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

##标的用户风险等级表
CREATE TABLE `t_bid_user_risk` (
  `id` bigint(20) NOT NULL,
  `bid_risk_id` bigint(20) NOT NULL,
  `user_risk_id` bigint(20) NOT NULL,
  `quota` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

##标的用户风险等级日志
CREATE TABLE `t_bid_user_risk_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid_risk_id` bigint(20) NOT NULL,
  `user_risk_id` bigint(20) NOT NULL,
  `quota` decimal(12,2) NOT NULL,
  `supervisor_id` bigint(20) NOT NULL,
  `mtime` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=latin1;

##初始化-------------------------begin
insert into t_bid_risk values(1,'低风险','低风险');
insert into t_bid_risk values(2,'中低风险','中低风险');
insert into t_bid_risk values(3,'中风险','中风险');
insert into t_bid_risk values(4,'中高风险','中高风险');
insert into t_bid_risk values(5,'高风险','高风险');

insert into t_user_risk values(1,'安全型','安全型');
insert into t_user_risk values(2,'保守型','保守型');
insert into t_user_risk values(3,'稳健型','稳健型');
insert into t_user_risk values(4,'积极型','积极型');
insert into t_user_risk values(5,'进取型','进取型');

insert into t_bid_user_risk
select (t_bid_risk.id-1)*5+t_user_risk.id,t_bid_risk.id,t_user_risk.id,(6-t_bid_risk.id)*t_user_risk.id*10
from t_user_risk
inner join t_bid_risk on true;

insert into t_bid_user_risk_log(id,bid_risk_id,user_risk_id,quota,supervisor_id,mtime) 
select null,bid_risk_id,user_risk_id,quota,1,now() 
from t_bid_user_risk;


INSERT INTO `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('194', '3', '标的风险等级分类划分管理', '', '');

INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('12370', '194', 'supervisor.bidManager.BidAgencyAction.bidUserRiskShow', '标的风险等级分类划分查看');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('12371', '194', 'supervisor.bidManager.BidAgencyAction.bidUserRiskUpdateBatch', '标的风险等级分类划分编辑');


##初始化-------------------------end
