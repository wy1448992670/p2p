


drop table if exists t_activity_increase_rate;

drop table if exists t_activity_increase_rate_detail;

/*==============================================================*/
/* Table: t_activity_increase_rate                              */
/*==============================================================*/
create table t_activity_increase_rate
(
   id                   bigint not null auto_increment comment '编号',
   type                 int default 1 comment '类型:1全场加息 2首投加息 3尾投加息',
   name                 varchar(20) comment '加息名称',
   remark               varchar(200) comment '简介',
   create_time          timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '创建时间',
   start_time           datetime not null comment '活动开始时间',
   stop_time            datetime not null comment '活动结束时间',
   primary key (id)
);

alter table t_activity_increase_rate comment '加息活动表';

/*==============================================================*/
/* Table: t_activity_increase_rate_detail                       */
/*==============================================================*/
create table t_activity_increase_rate_detail
(
   id                   bigint not null auto_increment comment '编号',
   activity_id          bigint not null comment '活动编号',
   rate                 decimal(5,2) not null comment '加息利率',
   create_time          timestamp not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '创建时间',
   state                int default 0 comment '状态0待审核 1初审通过 2终审通过 -1 初始未通过 -2复审未通过 -3 关闭',
   start_time           datetime not null comment '活动开始时间',
   stop_time            datetime not null comment '活动结束时间',
   visibile             int default 1 comment '二次审核不通过时不显示控制',
   primary key (id)
);

alter table t_activity_increase_rate_detail comment '加息活动详情';


ALTER TABLE `t_bills` ADD COLUMN `repayment_increase_interest1`  decimal(20,2) NULL DEFAULT NULL AFTER `repayment_increase_interest`;
ALTER TABLE `t_bills` ADD COLUMN `repayment_increase_interest2`  decimal(20,2) NULL DEFAULT NULL AFTER `repayment_increase_interest1`;

ALTER TABLE `t_bill_invests` ADD COLUMN `receive_increase_interest1`  decimal(20,2) NULL DEFAULT NULL AFTER `receive_increase_interest`;
ALTER TABLE `t_bill_invests` ADD COLUMN `receive_increase_interest2`  decimal(20,2) NULL DEFAULT NULL AFTER `receive_increase_interest1`;
#ALTER TABLE `t_bill_invests` ADD COLUMN `real_increase_interest1`  decimal(20,2) NULL DEFAULT NULL AFTER `real_increase_interest`;
#ALTER TABLE `t_bill_invests` ADD COLUMN `real_increase_interest2`  decimal(20,2) NULL DEFAULT NULL AFTER `real_increase_interest1`;

ALTER TABLE `t_invests` ADD COLUMN `increase_activity_id1`  bigint(20) NULL DEFAULT NULL AFTER `correct_increase_interest`;
ALTER TABLE `t_invests` ADD COLUMN `correct_increase_interest1`  decimal(20,2) NULL DEFAULT NULL AFTER `increase_activity_id1`;
ALTER TABLE `t_invests` ADD COLUMN `increase_activity_id2`  bigint(20) NULL DEFAULT NULL AFTER `correct_increase_interest1`;
ALTER TABLE `t_invests` ADD COLUMN `correct_increase_interest2`  decimal(20,2) NULL DEFAULT NULL AFTER `increase_activity_id2`;

ALTER TABLE `t_log` ADD COLUMN `result`  varchar(255) NULL DEFAULT NULL COMMENT '操作结果' AFTER `description`;

-- t_log  外键类型:1.t_bids 2.t_borrow_apply  3 t_activity_increase_rate_detail


INSERT INTO `t_right_types` (`id`, `name`, `code`, `description`, `is_use`) VALUES ('14', '运营活动', NULL, NULL, NULL);
INSERT INTO `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('220', '14', '活动加息审核', NULL, NULL);
INSERT INTO `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('221', '14', '活动加息汇总', NULL, NULL);
INSERT INTO `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('222', '14', '活动加息明细', NULL, NULL);
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13100', '220', 'supervisor.activity.IncreaseRate.addActivity', '加息活动添加');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13101', '220', 'supervisor.activity.IncreaseRate.activityList', '加息活动列表');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13102', '220', 'supervisor.activity.IncreaseRate.editActivity', '加息活动修改');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13103', '220', 'supervisor.activity.IncreaseRate.getActivityDetail', '加息活动详情');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13104', '220', 'supervisor.activity.IncreaseRate.firstAuditActivity', '加息活动初审');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13105', '220', 'supervisor.activity.IncreaseRate.lastAuditActivity', '加息活动复审');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13106', '220', 'supervisor.activity.IncreaseRate.closeActivity', '加息活动关闭');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13107', '221', 'supervisor.activity.IncreaseRate.increaseTotalList', '加息汇总');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13108', '222', 'supervisor.activity.IncreaseRate.increaseDetailList', '加息明细');

INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('8820', '176', 'supervisor.systemSettings.RedPackageHistoryAction.redRules', '红包规则设置');




update t_rights set type_id = 14 where id in(108,176);


INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1203', '-1', '3', '3', '', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('3', '0', '3', 't_activity_increase_rate_detail.state', '', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1302', '3', '0', '待审核', '', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1303', '3', '1', '初审通过', '', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1304', '3', '2', '复审通过', '', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1305', '3', '-1', '初审未通过', ' ', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1306', '3', '-2', '复审未通过', ' ', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1307', '3', '-3', '已关闭', ' ', '\0');
INSERT INTO `t_enum_map` (`id`, `enum_type`, `enum_code`, `enum_name`, `description`, `is_deleted`) VALUES ('1308', '3', '-4', '已关闭', ' ', '\0');


