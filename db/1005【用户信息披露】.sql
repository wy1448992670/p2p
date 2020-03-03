CREATE TABLE `t_users_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) COMMENT 't_users 表主键',
  `legal_person` varchar(20) COMMENT '法人代表',
  `business_scope` varchar(800) COMMENT '经营范围',
  `income_debt_info` varchar(500) COMMENT '收入及负债情况（页面限定120个字）',
  `asset_info` varchar(500) COMMENT '资产情况(页面限定输入120个字)',
  `other_finance_info` varchar(500) COMMENT '其它平台融资情况(页面输入限定120个字)',
  `other_info` varchar(500) COMMENT '其它信息(页面输入限定120个字)',
  `industry_id` bigint(20) COMMENT '所属行业表主键',
  `reg_capital` decimal(20, 2) COMMENT '注册资金',
  `create_time` timestamp(0) DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `update_time` timestamp(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '修改日期',
  `create_user_id` bigint(20) COMMENT '创建人主键',
  `update_user_id` bigint(20) COMMENT '修改人主键',
  PRIMARY KEY (`id`)
);

CREATE TABLE `t_industries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL COMMENT '行业名称',
  `is_disable` bit(1) DEFAULT NULL COMMENT '状态  1  隐藏   0 显示',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='行业名称';

 
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (1, '农、林、牧、渔业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (2, '采矿业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (3, '制造业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (4, '电力、热力、燃气及水生产和供应业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (5, '建筑业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (6, '批发和零售业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (7, '交通运输、仓储和邮政业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (8, '住宿和餐饮业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (9, '信息传输、软件和信息技术服务业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (10, '金融业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (11, '租赁和商务服务业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (12, '科学研究和技术服务业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (13, '水利、环境和公共设施管理业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (14, '居民服务、修理和其他服务业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (15, '教育', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (16, '卫生和社会工作', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (17, '文化、体育和娱乐业', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (18, '公共管理、社会保障和社会组织', b'0');
INSERT INTO `t_industries`(`id`, `name`, `is_disable`) VALUES (19, '其它', b'0');




insert into t_right_actions values (4030,81,'supervisor.userManager.AllUser.resetUserBaseinfo','重置实名信息');

ALTER TABLE `p2p_dev`.`t_users` 
MODIFY COLUMN `law_suit` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '涉诉情况' AFTER `credit_amount`;

ALTER TABLE `t_bids` 
ADD COLUMN `credit_amount` decimal(20, 2) DEFAULT NULL COMMENT '授信金额' AFTER `consult_certificate_url`;