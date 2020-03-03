CREATE TABLE `t_user_address_list` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '会员列表主键',
  `name` varchar(500) DEFAULT NULL COMMENT '姓名',
  `mobile` varchar(500) DEFAULT NULL COMMENT '手机号码',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='会员通讯录用户表';

insert into t_dict_maritals values(null,'丧偶',null,null,1);

insert into t_dict_cars values(null,'按揭已结清',null,null,1);

insert into t_dict_houses values(null,'按揭已结清',null,null,1);
insert into t_dict_houses values(null,'自建房',null,null,1);
insert into t_dict_houses values(null,'无产权房',null,null,1);
insert into t_dict_houses values(null,'父母或亲属房',null,null,1);
insert into t_dict_houses values(null,'经济适用房',null,null,1);
insert into t_dict_houses values(null,'动迁房',null,null,1);
insert into t_dict_houses values(null,'租房',null,null,1);


INSERT INTO `t_right_types`(`id`, `name`, `code`, `description`, `is_use`) VALUES (12, '贷后管理', NULL, NULL, NULL);
INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) VALUES (210, 12, '用户列表', NULL, NULL);

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (13000, 210, 'supervisor.postLoanManager.UserListAction.userList', '用户列表');
insert into t_right_actions values(13001,210,'supervisor.postLoanManager.UserListAction.getUserAddressList','用户列表')

