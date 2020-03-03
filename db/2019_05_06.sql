
#运营商报告 用户信息
DROP TABLE IF EXISTS `t_report_user_info`;
CREATE TABLE `t_report_user_info` (
  `id` bigint(20) NOT NULL COMMENT '运营商报告用户信息',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `user_name` varchar(20) DEFAULT '' COMMENT '用户姓名',
  `native_place` varchar(255) DEFAULT '' COMMENT '身份证住址',
  `mobile` varchar(20) DEFAULT '' COMMENT '手机号',
  `carrier_name` varchar(20) DEFAULT '' COMMENT '手机号户名',
  `reg_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '手机注册时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#运营商报告 用户通讯信息
DROP TABLE IF EXISTS `t_report_contact_detail`;
CREATE TABLE `t_report_contact_detail` (
  `id` int(20) NOT NULL,
  `user_info_id` int(20) DEFAULT NULL COMMENT 't_report_user_info',
  `number` varchar(20) DEFAULT '' COMMENT '通讯号码',
  `group_name` varchar(64) DEFAULT '' COMMENT '组名',
  `company_name` varchar(255) DEFAULT '' COMMENT '公司名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


#后台 运营商报告-用户通讯信息 增加权限
insert t_right_actions value(77778,211,'supervisor.ymd.CreditManageAction.reportContactDetail','运营商报告-用户通讯信息');