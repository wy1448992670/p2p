ALTER TABLE `t_users` ADD COLUMN `sub_nature`  varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '主体性质' AFTER `user_type`;
ALTER TABLE `t_users` ADD COLUMN `age`  int(3) NULL DEFAULT NULL COMMENT '年龄' AFTER `sub_nature`;
ALTER TABLE `t_users` ADD COLUMN `credit_amount`  decimal(20,2) NULL DEFAULT NULL COMMENT '授信金额' AFTER `age`;
ALTER TABLE `t_users` ADD COLUMN `law_suit`  varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '涉诉情况' AFTER `credit_amount`;
ALTER TABLE `t_users` ADD COLUMN `credit_report`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '征信报告状况 ' AFTER `law_suit`;


ALTER TABLE `t_bids` ADD COLUMN `repayment_source`  varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '还款来源' AFTER `bid_risk_id`;
ALTER TABLE `t_bids` ADD COLUMN `related_costs`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '相关费用' AFTER `repayment_source`;


CREATE TABLE `t_bid_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bid_id` bigint(20) DEFAULT NULL COMMENT 't_bids  表的主键',
  `title` varchar(200) DEFAULT NULL COMMENT '图片标题',
  `bid_image_url` varchar(800) CHARACTER SET latin1 DEFAULT NULL COMMENT '标相关图片路径',
  `supervisor_id` bigint(20) DEFAULT NULL COMMENT '上传人 id',
  `sort` int(20) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `index_id` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=140 DEFAULT CHARSET=utf8 COMMENT='标的 关联的图片表';

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12372, 81, 'supervisor.SupervisorController.getNewCity', '获取字典表市列表');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (12373, 81, 'supervisor.userManager.AllUser.editUserBaseinfo', '修改用户基本信息');
