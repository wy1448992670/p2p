
#zhangjun
#用户表增加用户理财类型
ALTER TABLE `t_users` 
ADD COLUMN `finance_type` int(255) DEFAULT 1 COMMENT '理财类型  0 借款人  1  投资人' AFTER `credit_report`;

ALTER TABLE `t_bids`
ADD COLUMN `borrow_apply_id` bigint(20) COMMENT '借款申请表主键id' AFTER `related_costs`;
#查询借款人列表
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12390, 42, 'supervisor.bidManager.BidAgencyAction.selectBorrowUsersInit', '机构合作标管理');


#wangyun

ALTER TABLE `t_agencies`
ADD COLUMN `area`  varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '对应区域' AFTER `bad_bid_count`,
ADD COLUMN `supervisor_id`  bigint(20) NULL COMMENT '责任人,用于分配借款申请' AFTER `area`;


#zhongqi

/*
需要上线前确认负责人id*/
update t_agencies set area='枣庄',supervisor_id=47 where id=1;
update t_agencies set area='宁德',supervisor_id=46 where id=2;
update t_agencies set area='温州',supervisor_id=45 where id=3;
update t_agencies set area='上海',supervisor_id=31 where id=4;




#huangsj


#haoyanliang
ALTER TABLE `t_users` DROP COLUMN `sub_nature`;

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12374, 81, 'supervisor.userManager.AllUser.userExclusiveContent', '加载主体独有属性');

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12375, 81, 'supervisor.SupervisorController.getAdCity', '获取字典表市列表');

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12377, 81, 'supervisor.userManager.AllUser.addUserBaseinfo', '添加用户基本信息');

INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) 
VALUES (199, 2, '借款端已分配列表', NULL, NULL);

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12387, 199, 'supervisor.webContentManager.ProductAction.realityNameInfo', '实名信息');

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12391, 199, 'supervisor.userManager.AllUser.addUser', '添加实名信息');

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) 
VALUES (12392, 199, 'supervisor.userManager.AllUser.resetUserBaseinfo', '重置实名信息');

#shixuanyu
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1033', '2018-10-19 13:50:31', '42', '农亿贷介绍', '石轩宇', '<p style=\"color:#A1A1A1;font-size:14px;\">\n	从事于农林畜牧行业有关的企业或\n</p>\n<p style=\"color:#A1A1A1;font-size:14px;\">\n	个人，额度：1千-100万\n</p>', '农亿贷', '农亿贷', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-19 13:50:31', '0', '0', '0', '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1032', '2018-10-19 13:49:32', '41', '房亿贷介绍', '石轩宇', '凭房产证申请，额度1千-100万', '房亿贷', '房亿贷', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-19 13:49:32', '0', '0', '0', '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1031', '2018-10-19 13:48:22', '40', '信亿贷介绍', '石轩宇', '<p style=\"color:#A1A1A1;font-size:14px;\">\n	凭身份证,或企业营业执照申请，\n</p>\n<p style=\"color:#A1A1A1;font-size:14px;\">\n	额度1千-100万\n</p>', '信亿贷', '信亿贷', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-19 13:48:22', '0', '0', '0', '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1030', '2018-10-18 16:54:43', '14', '借款攻略新', '石轩宇', '<p>\n	<span style=\"font-family:&quot;\">这是</span><span style=\"color:#337FE5;font-size:16px;font-family:&quot;\"><strong>借款攻略<img src=\"http://localhost:9000/public/javascripts/kindeditor-4.1.7/plugins/emoticons/images/6.gif\" border=\"0\" alt=\"\" /></strong></span> \n</p>\n<p>\n	<br />\n</p>\n<p>\n	<img src=\"/images?uuid=bb1b2b4c-f4d1-44f9-93ca-820461c9956d\" alt=\"\" /> \n</p>\n<p>\n	<br />\n</p>', '借款攻略新', '借款攻略新', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-18 00:00:00', '0', '0', '0', '');

INSERT INTO  `t_content_news_types` (`id`, `parent_id`, `name`, `description`, `_order`, `status`) VALUES ('45', '-1', '产品介绍', '借款app产品的介绍', '7', '');
INSERT INTO  `t_content_news_types` (`id`, `parent_id`, `name`, `description`, `_order`, `status`) VALUES ('46', '45', '信亿贷', NULL, '1', '');
INSERT INTO  `t_content_news_types` (`id`, `parent_id`, `name`, `description`, `_order`, `status`) VALUES ('47', '45', '房亿贷', NULL, '2', '');
INSERT INTO  `t_content_news_types` (`id`, `parent_id`, `name`, `description`, `_order`, `status`) VALUES ('48', '45', '农亿贷', NULL, '3', '');

INSERT INTO  `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('8702', '198', 'supervisor.systemSettings.AppAction.borrowVersion', '借款APP版本设置');
INSERT INTO  `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('8703', '198', 'supervisor.systemSettings.AppAction.saveBorrowVersion', '借款APP版本设置');

INSERT INTO  `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('198', '9', '借款APP版本设置', NULL, NULL);

INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10050', 'android_code_borrow', '5', 'android编号');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10051', 'ios_version_borrow', '4', 'iso版本号');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10052', 'ios_code_borrow', '4', 'ios编号');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10053', 'ios_msg_borrow', '654654', 'ios升级信息');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10054', 'android_msg_borrow', '987987', 'android升级信息');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10055', 'android_version_borrow', '5', 'android版本号');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10056', 'borrow_android_forced update', '1', '借款Android是否强制更新  0:不强制更新   1:强制更新');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10057', 'borrow_ios_forced update', '1', '借款IOS是否强制更新  0:不强制更新   1:强制更新');

INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10036', 'platform_account', '{\"bankCode\":\"CCB\",\"bankNo\":\"6222020000000000001\",\"requestNo\":\"171027192344927\"}', '平台账户银行卡信息');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10039', 'ACCOUNT_NO_001', '{\"balance\":3000,\"freeze\":0.0}', '代偿账户');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10040', 'ACCOUNT_NO_002', '{\"balance\":483094.54,\"freeze\":83507}', '运营账户');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10041', 'ACCOUNT_NO_003', '{\"balance\":0.0,\"freeze\":0.0}', '收入账户');


INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('144', '001', '2018-10-19 17:48:04', '借款APP首页', '/public/images/default.png', '640*230', '不超过2M', '', '', '\0', '1', '', '借款APPBanner', '0', '借款app首页1');
INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('145', '002', '2018-10-19 17:48:04', '借款APP首页', '/public/images/default.png', '640*230', '不超过2M', '', '', '\0', '1', '\0', '借款APPBanner', '0', '借款app首页2');
INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('146', '003', '2018-10-19 17:48:04', '借款APP首页', '/data/attachments/873ec7e0-4ac2-401f-b7db-c69fda3067d8', '640*230', '不超过2M', '', '', '\0', '1', '', '借款APPBanner', '0', '借款app首页3');
INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('147', '004', '2018-10-19 17:48:04', '借款APP首页', '/public/images/default.png', '640*230', '不超过2M', '', '', '\0', '1', '', '借款APPBanner', '0', '借款app首页4');
INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('148', '005', '2018-10-19 17:48:04', '借款APP首页', '/public/images/default.png', '640*230', '不超过2M', '', '', '\0', '2', '\0', '借款APPBanner', '0', '借款app首页5');

INSERT INTO  `t_content_advertisements` (`id`, `no`, `time`, `location`, `image_filename`, `resolution`, `file_size`, `file_format`, `url`, `is_link_enabled`, `target`, `is_use`, `tag`, `is_top`, `txt`) VALUES ('149', '35', '2018-10-30 13:54:04', '借款APP启动图', '/public/images/default.png', '640*230', '不超过2M', 'png', '', '\0', '0', '', '其他', NULL, '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1027', '2018-10-30 16:52:22', '30', '咨询与管理服务协议', '石轩宇', '咨询与管理服务协议', '咨询与管理服务协议', '咨询与管理服务协议', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-30 00:00:00', '0', '0', '0', '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1026', '2018-10-30 16:51:54', '30', '借款人服务协议', '石轩宇', '借款人服务协议', '借款人服务协议', '借款人服务协议', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-30 00:00:00', '0', '0', '0', '');
INSERT INTO  `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-1025', '2018-10-30 16:51:22', '30', '出借人服务协议', '石轩宇', '出借人服务协议', '出借人服务协议', '出借人服务协议', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2018-10-30 00:00:00', '0', '0', '0', '');

INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10058', 'ios_audit', '2|1', 'ios审核状态');
INSERT INTO  `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10059', 'ios_audit_borrow', '1|0', '借款ios审核状态');


#zhangqiongqi

insert into t_rights values(195,3,'借款申请审核',null,null);
insert into t_rights values(196,3,'借款申请尽调',null,null);
insert into t_rights values(197,3,'借款申请列表',null,null);

insert into t_right_actions values(12379,195,'supervisor.bidManager.BidApplicationAction.applicationCheckList','借款申请审核列表');
insert into t_right_actions values(12380,195,'supervisor.bidManager.BidApplicationAction.applicationCheckView','借款申请审核视图');
insert into t_right_actions values(12381,195,'supervisor.bidManager.BidApplicationAction.applicationCheck','借款申请审核');
insert into t_right_actions values(12382,195,'supervisor.bidManager.BidApplicationAction.applicationDetailView','借款申请详情');

insert into t_right_actions values(12383,196,'supervisor.bidManager.BidApplicationAction.applicationDueDiligenceList','借款申请尽调列表');
insert into t_right_actions values(12384,196,'supervisor.bidManager.BidApplicationAction.applicationCheck','借款申请尽调');

insert into t_right_actions values(12385,197,'supervisor.bidManager.BidApplicationAction.applicationCheckList','借款申请审核列表');
insert into t_right_actions values(12386,197,'supervisor.bidManager.BidApplicationAction.applicationDetailView','借款申请详情');

insert into t_right_actions values(12393,163,'supervisor.systemSettings.supervisorAction.enumModelInit','重置EnumModel');


delete from t_right_actions where right_id in (194,195,196,197);

insert into t_right_actions values(2500,194,'supervisor.bidManager.BidAgencyAction.bidUserRiskShow','标的风险等级分类划分查看');
insert into t_right_actions values(2501,194,'supervisor.bidManager.BidAgencyAction.bidUserRiskUpdateBatch','标的风险等级分类划分编辑');
insert into t_right_actions values(2510,195,'supervisor.bidManager.BidApplicationAction.applicationCheckList','借款申请审核列表');
insert into t_right_actions values(2511,195,'supervisor.bidManager.BidApplicationAction.applicationCheckView','借款申请审核视图');
insert into t_right_actions values(2512,195,'supervisor.bidManager.BidApplicationAction.applicationCheck','借款申请审核');
insert into t_right_actions values(2513,195,'supervisor.bidManager.BidApplicationAction.applicationDetailView','借款申请详情');
insert into t_right_actions values(2520,196,'supervisor.bidManager.BidApplicationAction.applicationDueDiligenceList','借款申请尽调列表');
insert into t_right_actions values(2521,196,'supervisor.bidManager.BidApplicationAction.applicationCheck','借款申请尽调');
insert into t_right_actions values(2522,196,'supervisor.bidManager.BidApplicationAction.applicationCheckView','借款申请审核视图');
insert into t_right_actions values(2523,196,'supervisor.bidManager.BidApplicationAction.applicationDetailView','借款申请详情');
insert into t_right_actions values(2530,197,'supervisor.bidManager.BidApplicationAction.applicationCheckList','借款申请审核列表');
insert into t_right_actions values(2531,197,'supervisor.bidManager.BidApplicationAction.applicationDetailView','借款申请详情');




DROP TABLE IF EXISTS `t_borrow_apply`;
CREATE TABLE `t_borrow_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `borrow_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '借款编号this.id+fun(this.product_id)\r\nfun=信亿贷为X  房亿贷为F 农亿贷为N',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '借款申请用户ID',
  `product_id` int(4) NULL DEFAULT NULL COMMENT '产品类型 1信亿贷 2 房亿贷 3 农亿贷（ProductEnum枚举）',
  `loan_property_id` int(4) NULL DEFAULT NULL COMMENT '1 个人借款 2企业借款 3 个体工商户借款（UserTypeEnum枚举）',
  `loan_purpose_id` int(4) NULL DEFAULT NULL COMMENT '借款用途(参照t_dict_loan_purposes)',
  `apply_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '申请借款金额',
  `approve_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '审批金额',
  `period` int(4) NULL DEFAULT NULL COMMENT '借款期限（单位 月）',
  `apply_area` int(10) NULL DEFAULT NULL COMMENT '申请区域(）',
  `allot_area` int(10) NULL DEFAULT NULL COMMENT '分配区域',
  `apply_time` datetime(0) NULL DEFAULT NULL COMMENT '申请时间',
  `status` int(4) NULL DEFAULT NULL COMMENT '状态:select * from t_enum_map where enum_type=2',
  `allot_time` datetime(0) NULL DEFAULT NULL COMMENT '分配时间',
  `allot_admin` bigint(20) NULL DEFAULT NULL COMMENT '分配管理员',
  `submit_time` datetime(0) NULL DEFAULT NULL COMMENT '提交时间',
  `submit_admin` bigint(20) NULL DEFAULT NULL COMMENT '提交管理员',
  `audit_time` datetime(0) NULL DEFAULT NULL COMMENT '初审时间',
  `audit_admin` bigint(20) NULL DEFAULT NULL COMMENT '初审管理员',
  `recheck_time` datetime(0) NULL DEFAULT NULL COMMENT '复审时间',
  `recheck_admin` bigint(20) NULL DEFAULT NULL COMMENT '复审管理员',
  `reason` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '不通过原因',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1  ROW_FORMAT = Compact;


DROP TABLE IF EXISTS `t_log`;
CREATE TABLE `t_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `relation_type` int(255) NOT NULL COMMENT '外键类型:1.t_bids 2.t_borrow_apply',
  `relation_id` bigint(20) NOT NULL COMMENT '外键id',
  `user_type` int(255) NOT NULL COMMENT '操作用户类型:1.t_users 2.t_supervisors,job存2',
  `user_id` bigint(20) NOT NULL COMMENT '操作用户id:job存1',
  `status` int(255) NOT NULL COMMENT '状态id',
  `time` datetime(0) NOT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '变更时间:为null时new Date()',
  `description_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '描述标题:为null时\"\"',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '描述:为null时\"\"',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 ROW_FORMAT = Compact;

DROP TABLE IF EXISTS `t_enum_map`;
CREATE TABLE `t_enum_map`  (
  `id` bigint(20) NOT NULL,
  `enum_type` int(255) NOT NULL COMMENT '枚举类型 enum_type=0时,enum_code为enum_type的外键',
  `enum_code` int(11) NOT NULL COMMENT '枚举code',
  `enum_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '枚举名',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `is_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '0.有效 1.无效',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `real_primary_key`(`enum_type`, `enum_code`) USING BTREE
) ENGINE = InnoDB ROW_FORMAT = Compact;

insert into t_enum_map values(-1,0,-1,'t_log.relation_type','',0);
insert into t_enum_map values(0,0,0,'t_enum_map.enum_type','',0);
insert into t_enum_map values(1,0,1,'t_bids.status','',0);
insert into t_enum_map values(2,0,2,'t_borrow_apply.status','',0);


insert into t_enum_map values(1001,1,0,'审核中','',0);
insert into t_enum_map values(1002,1,1,'提前借款','',0);
insert into t_enum_map values(1003,1,2,'借款中(审核通过)','',0);
insert into t_enum_map values(1004,1,3,'待放款(放款审核通过)','',0);
insert into t_enum_map values(1005,1,4,'还款中(财务放款)','',0);
insert into t_enum_map values(1006,1,5,'已还款','',0);

insert into t_enum_map values(1010,1,10,'审核中待验证','',0);
insert into t_enum_map values(1011,1,11,'提前借款待验证','',0);
insert into t_enum_map values(1012,1,12,'借款中待验证','',0);
insert into t_enum_map values(1020,1,20,'审核中待支付投标奖励','',0);
insert into t_enum_map values(1021,1,21,'提前借款待支付投标奖励','',0);
insert into t_enum_map values(1022,1,22,'借款中待支付投标奖励','',0);
insert into t_enum_map values(1014,1,14,'本金垫付还款中(已放款)','',0);

insert into t_enum_map values(1051,1,-1,'审核不通过','',0);
insert into t_enum_map values(1052,1,-2,'借款中不通过','',0);
insert into t_enum_map values(1053,1,-3,'放款不通过','',0);
insert into t_enum_map values(1054,1,-4,'流标','',0);
insert into t_enum_map values(1055,1,-5,'撤销','',0);
insert into t_enum_map values(1060,1,-10,'未验证','',0);


insert into t_enum_map values(1101,2,1,'未分配','',0);
insert into t_enum_map values(1102,2,2,'已分配','',0);
insert into t_enum_map values(1103,2,3,'已提交','',0);
insert into t_enum_map values(1104,2,4,'已初审','',0);
insert into t_enum_map values(1105,2,5,'已通过','',0);
insert into t_enum_map values(1106,2,6,'借款取消','',0);
insert into t_enum_map values(1107,2,7,'审核不通过','',0);

insert into t_enum_map values(1201,-1,1,'1','',0);
insert into t_enum_map values(1202,-1,2,'2','',0);



