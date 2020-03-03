DROP TABLE IF EXISTS `t_user_auth_review`;
CREATE TABLE `t_user_auth_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `real_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行账户真实姓名',
  `company_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '企业名称',
  `credit_code` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '统一社会信用代码',
  `bank_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '开户行名称',
  `bank_no` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '企业对公账户',
  `status` smallint(1) UNSIGNED NULL DEFAULT 0 COMMENT '状态  0：待审核   1：审核不通过  2：审核通过  3：已重置',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint(20) NULL DEFAULT 0 COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '企业用户认证审核表' ROW_FORMAT = Compact;

ALTER TABLE `t_user_bank_accounts`
ADD COLUMN `is_valid` tinyint(1) NULL COMMENT '是否有效  0：无效   1：有效' AFTER `protocol_no`;


ALTER TABLE `t_users`
ADD COLUMN `user_type` int(2) UNSIGNED DEFAULT 0 COMMENT '0 未知 1 个人  2 企业 ' AFTER `sms_blacklist_dt`;

update `t_user_bank_accounts`  set is_valid  = 1 ;

INSERT INTO `t_system_options`(`id`, `_key`, `_value`, `description`) VALUES (10042, 'android_forced update', '1', 'Android是否强制更新  0:不强制更新   1:强制更新');
INSERT INTO `t_system_options`(`id`, `_key`, `_value`, `description`) VALUES (10043, 'ios_forced update', '1', 'IOS是否强制更新  0:不强制更新   1:强制更新');


INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12360, 81, 'supervisor.userManager.AllUser.companyUser', '企业会员管理');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12361, 81, 'supervisor.userManager.AllUser.auditCompanyUser', '企业会员审核');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12362, 81, 'supervisor.userManager.AllUser.listCompanyUser', '企业会员列表');

update  t_users   a  set   a.user_type=1  where  a.reality_name is not null;