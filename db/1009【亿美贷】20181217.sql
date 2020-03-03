/*
table users{
realname
interface_realname_status,	#face++接口实名:未实名,实名失败,实名成功,实名过期(实名存在)
interface_realname_time,
interface_realname_status()	#是否接口实名,查询user对应的实名照片(是否存在,且未过期为已接口实名)

is_bind_bankcard() #银行卡是否绑定

org_approve_status,		#运营商认证
org_approve_time,		
org_approve_status()	#是否运营商认证 1是2否

credit_apply_status(product_id)		#product_id额度申请状态:查最后一笔额度申请状态 没有，申请中，成功->额度，冻结，清零/关闭/(未通知，已通知)
machine_score(product_id) #产品最后一笔申请额度得分
XXX  machine_score 风控得分->应该在额度申请中
}

CREATE TABLE `t_audit` 
*/
CREATE TABLE `t_new_product` (
  `id` int(11) NOT NULL ,
  `name` varchar(255) DEFAULT NULL  COMMENT '1.信亿贷 2.房亿贷 3.农亿贷 4.亿美贷 5.车亿贷',
  `code_name` varchar(255) DEFAULT NULL COMMENT '指代Enum里的对象引用名 1.XIN 2.FANG 3.NONG 4.YI 5.CHE',
  `borrow_no_suffix` varchar(255) DEFAULT NULL COMMENT '借款申请编号后缀1.X 2.F 3.N 4.Y 5.C',
  `is_use` bit(1) DEFAULT NULL COMMENT '能否使用 1 能 2  否',
  `borrow_app_can_use` bit(1) DEFAULT NULL COMMENT '普通借款端是否可以使用,当前可使用id=1,2,3',
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='产品';

insert into t_new_product values(1,'信亿贷','XIN','X',1,1,'');
insert into t_new_product values(2,'房亿贷','FANG','F',1,1,'');
insert into t_new_product values(3,'农亿贷','NONG','N',1,1,'');
insert into t_new_product values(4,'亿美贷','YI','Y',1,0,'');
insert into t_new_product values(5,'车亿贷','CHE','C',1,0,'');

#------------------------------------------------------文件上传
CREATE TABLE `t_file_dict` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '同样类型的文件,在不同的主体中使用,规则不同时,请定义新的file_dict',
  `name` varchar(255) DEFAULT NULL,
  `type` int(255) DEFAULT NULL COMMENT '1,图片 2,PDF',
  `expire_time` bigint(20) DEFAULT NULL COMMENT '过期时间(秒),0只用一次,-1永不过期',
  `can_deploy` bit(1) DEFAULT NULL COMMENT '能否配置 1 能 0  否',
  `is_sensitive` bit(1) DEFAULT NULL COMMENT '是否敏感 1 是 0 否',
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件目录';

CREATE TABLE `t_file_relation_dict` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'file_dict与业务表的关系',
  `relation_type` int(11) DEFAULT NULL COMMENT '1.users身份证照片实名认证 \r\n2.users活体认证  \r\n3.users用户风控资料 \r\n4.credit_apply \r\n5.increase_credit_apply \r\n6.borrow_apply \r\n7.users.user_type in (2,3)企业用户资料 \r\n8.orgnization合作机构资料\r\n9.credit_apply第三方报告',
  `file_dict_id` int(11) DEFAULT NULL COMMENT 'file_dict.id',
  `required_num` int(11) DEFAULT NULL COMMENT '至少文件数量,0不必填',
  `max_num` int(11) DEFAULT NULL COMMENT '最多文件数量,-1不限数量',
  `is_visible` bit(1) DEFAULT NULL COMMENT '客户端,公开信息中是否显示',
  `sequence` int(11) DEFAULT NULL COMMENT 'file_dict在业务表中的顺序',
  `can_rewrite` bit(1) DEFAULT NULL COMMENT '决定了file_relation.can_rewrite\r\n能否修改关系,(如果主体审核通过,且can_rewrite==false,则不能修改关系).\r\n如:实名通过后,用户表的身份证照片不能修改.\r\n如果用户表需要更新身份证,需要特殊流程,审核新的身份证照片后修改关系',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务文件关系目录';

CREATE TABLE `t_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `file_dict_id` int(11) DEFAULT NULL,
  `real_path` varchar(900) DEFAULT NULL COMMENT '文件地址',
  `desensitization_path` varchar(255) DEFAULT NULL COMMENT '脱敏文件地址',
  `cache_path` varchar(255) DEFAULT NULL COMMENT '缓存地址',
  `create_time` datetime DEFAULT NULL,
  `expiry_time` datetime DEFAULT NULL COMMENT '过期时间,通过是否过期判断是否能复用',
  `uploader_type` int(255) DEFAULT NULL COMMENT '1.user 2.supervisor',
  `uploader_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='文件';

CREATE TABLE `t_file_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'file和业务表的关系',
  `relation_type` int(255) DEFAULT NULL COMMENT 't_file_dict_relation.relation_type',
  `relation_id` bigint(20) DEFAULT NULL COMMENT '业务表外键',
  `file_id` bigint(20) DEFAULT NULL COMMENT 'file.id',
  `is_visible` bit(1) DEFAULT NULL COMMENT '客户端,公开信息中是否显示 1 显示 2 不显示',
  `file_dict_sequence` int(11) DEFAULT NULL COMMENT '这一类file_dict在这个业务主体中的顺序',
  `can_rewrite` bit(1) DEFAULT NULL COMMENT '能否修改关系,(如果主体审核通过,且can_rewrite==false,则不能修改关系).\r\n如:实名通过后,用户表的身份证照片不能修改.\r\n如果用户表需要更新身份证,需要特殊流程,审核新的身份证照片后修改关系.',
  `is_temp` bit(1) DEFAULT NULL COMMENT '是否是临时关系,主体还没生成,临时存放在用户下,正式关系确定时,直接修改关系',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '业务文件关系';
#-------------------------------文件数据

#(`id`, `name`, `type`, `expire_time`, `can_deploy`, `is_sensitive`, `description`)
INSERT INTO `t_file_dict` VALUES (1, '身份证正面', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (2, '身份证反面', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (3, '最佳人脸', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (4, '活体全景照片', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (5, '花呗截图', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (6, '借呗截图', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (7, '淘宝收货地址', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (8, '收入证明', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (9, '社保缴费基数截图', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (10, '信用卡额度截图', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (11, '电商认证', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (13, '其它', 1, -1, b'1', b'1', '亿美贷用户资料,风控额度审核时提交的其他资料');
INSERT INTO `t_file_dict` VALUES (14, '机构外景照片', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (15, '租房合同', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (16, '营业执照', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (17, '医疗许可证', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (18, '合作项目样单', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (19, '法人身份证', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (20, '对私账户身份证', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (21, '银行卡', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (22, '合作协议', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (23, '脱敏图', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (24, '手术单', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (25, '手持身份证', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (26, '手术清单', 1, -1, b'1', b'1', NULL);
INSERT INTO `t_file_dict` VALUES (27, '其它', 1, -1, b'1', b'1', '亿美贷借款申请时提交的其他资料');
insert into `t_file_dict` values (28, '星护甲报告', 2, -1, 0, 1, null);
insert into `t_file_dict` values (29, '同盾报告', 2, -1, 0, 1, null);
insert into `t_file_dict` values (30, '魔蝎运营商报告', 2, -1, 0, 1, null);



#(`id`, `relation_type`, `file_dict_id`, `required_num`, `max_num`, `is_visible`, `sequence`, `can_rewrite`, `create_time`)
INSERT INTO `t_file_relation_dict` VALUES (null, 1, 1, 1, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 1, 2, 1, 1, b'1', 2, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 2, 3, 1, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 2, 4, 1, 1, b'1', 2, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 3, 5, 0, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 6, 0, 1, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 7, 0, 1, b'1', 3, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 8, 0, 1, b'1', 4, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 9, 0, 1, b'1', 5, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 10, 0, 1, b'1', 6, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 3, 13, 0, 8, b'1', 7, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 4, 5, 0, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 6, 0, 1, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 7, 0, 1, b'1', 3, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 8, 0, 1, b'1', 4, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 9, 0, 1, b'1', 5, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 10, 0, 1, b'1', 6, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 4, 13, 0, 8, b'1', 7, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 5, 5, 0, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 6, 0, 1, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 7, 0, 1, b'1', 3, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 8, 0, 1, b'1', 4, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 9, 0, 1, b'1', 5, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 10, 0, 1, b'1', 6, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 5, 13, 0, 8, b'1', 7, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 6, 25, 0, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 6, 26, 0, 1, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 6, 27, 0, 8, b'1', 3, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 8, 14, 1, 8, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 15, 1, 8, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 16, 1, 8, b'1', 3, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 17, 1, 8, b'1', 4, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 18, 1, 8, b'1', 5, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 19, 1, 8, b'1', 6, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 20, 1, 8, b'1', 7, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 21, 1, 8, b'1', 8, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 8, 22, 1, 8, b'1', 9, b'0', now());

INSERT INTO `t_file_relation_dict` VALUES (null, 9, 28, 0, 1, b'1', 1, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 9, 29, 0, 1, b'1', 2, b'0', now());
INSERT INTO `t_file_relation_dict` VALUES (null, 9, 30, 0, 1, b'1', 3, b'0', now());

insert into t_file_dict values(31,'近半年银行流水(必填)',1,-1,1,1,null);
insert into t_file_dict values(32,'人民银行征信报告(必填)',1,-1,1,1,null);

update t_file_relation_dict set sequence=sequence+2 where relation_type in(3,4,5);

insert into t_file_relation_dict values(null,3,31,0,1,1,1,0,now());
insert into t_file_relation_dict values(null,3,32,0,1,1,2,0,now());
insert into t_file_relation_dict values(null,4,31,0,1,1,1,0,now());
insert into t_file_relation_dict values(null,4,32,0,1,1,2,0,now());
insert into t_file_relation_dict values(null,5,31,0,1,1,1,0,now());
insert into t_file_relation_dict values(null,5,32,0,1,1,2,0,now());



#--------------------------------------------风控打分
CREATE TABLE `t_risk_manage_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `credit_apply_id` bigint(20) DEFAULT NULL COMMENT 't_credit_apply.id',
  `type_id` int(10) DEFAULT NULL COMMENT 't_risk_manage_type.id',
  `value` varchar(255) DEFAULT NULL COMMENT '输入值',
  `score` decimal(11,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '风控得分';

CREATE TABLE `t_risk_manage_big_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) DEFAULT NULL COMMENT '1.信亿贷\r\n2.房亿贷\r\n3.农亿贷\r\n4.亿美贷\r\nconstants.ProductEnum',
  `name` varchar(255) DEFAULT NULL,
  `total_score` decimal(11,2) DEFAULT NULL COMMENT '总分:限制t_risk_manage_type.sum(top_score)',
  `sequence` int(11) DEFAULT NULL COMMENT '显示顺序',
  `is_valid` bit(1) DEFAULT NULL COMMENT '是否有效 0.无效 1.有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '风控打分大类';

CREATE TABLE `t_risk_manage_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `big_type_id` int(11) DEFAULT NULL COMMENT 't_risk_manage_big_type.id',
  `compare_type` int(11) DEFAULT NULL COMMENT '1,数字区间:value is decimal,击中[min_value,max_value)得score\r\n2,比较:配置页显示show_str,value.equls(compare_value),true得score\r\n3,日期区间;value is Date().getTime(),击中[min_value,max_value)得score',
  `top_score` decimal(11,2) DEFAULT NULL COMMENT '限制,t_risk_manage_score_option最高的得分',
  `unit` varchar(255) DEFAULT NULL COMMENT '单位,显示用',
  `sequence` int(11) DEFAULT NULL COMMENT '显示顺序',
  `is_valid` bit(1) DEFAULT NULL COMMENT '是否有效 0.无效 1.有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '风控打分项';

CREATE TABLE `t_risk_manage_score_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 't_risk_manage_type.compare_type:\r\n1,数字区间:value is decimal,击中[min_value,max_value)得score\r\n2,比较:配置页显示show_str,value.equls(compare_value),true得score\r\n3,日期区间;value is Date().getTime(),击中[min_value,max_value)得score',
  `type_id` int(11) DEFAULT NULL COMMENT 't_risk_manage_type.id',
  `min_value` decimal(22,2) DEFAULT NULL,
  `max_value` decimal(22,2) DEFAULT NULL,
  `compare_value` varchar(255) DEFAULT NULL COMMENT 't_risk_manage_type.compare_type=2,得分时比较此字段',
  `show_str` varchar(255) DEFAULT NULL COMMENT 't_risk_manage_type.compare_type=2,配置分数页显示此字段',
  `score` decimal(11,2) DEFAULT NULL,
  `sequence` int(11) DEFAULT NULL,
  `is_valid` bit(1) DEFAULT NULL COMMENT '是否有效 0.无效 1.有效',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '风控得分项';
#---------------------------------------------风控打分数据
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 0.00, 300.00, NULL, '300分以下', 5.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 300.00, 400.00, NULL, '300-400分', 8.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 400.00, 500.00, NULL, '400-500分', 10.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 500.00, 650.00, NULL, '500-650分', 12.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 650.00, 700.00, NULL, '650-700分', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 700.00, 800.00, NULL, '700-800分', 18.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 1, 800.00, NULL, NULL, '800分以上', 20.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 2, 0.00, 300.00, NULL, '0-300分', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 2, 300.00, 500.00, NULL, '300-500分', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 2, 500.00, 600.00, NULL, '500-600分', 5.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 2, 600.00, 850.00, NULL, '600-850分', 8.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 2, 850.00, 999.00, NULL, '850-999分', 20.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 3, 0.00, 1.00, NULL, '不在黑名单内', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 3, 1.00, NULL, NULL, '在黑名单内', 9.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 4, NULL, NULL, '高风险等级', '高', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 4, NULL, NULL, '中风险等级', '中', 6.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 4, NULL, NULL, '低风险等级', '低', 11.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 5, NULL, NULL, '是', '是', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 5, NULL, NULL, '否', '否', 5.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 6, NULL, NULL, NULL, '是', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 6, NULL, NULL, NULL, '否', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 7, 0.00, 3.00, '0-3个月', '0-3个月', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 7, 3.00, 6.00, '3-6个月', '3-6个月', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 7, 6.00, 12.00, '6-12个月', '6-12个月', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 7, 12.00, 24.00, '12-24个月', '12-24个月', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 7, 24.00, NULL, '24个月以上', '24个月以上', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 8, NULL, NULL, '正常在用', '正常', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 8, NULL, NULL, '停机', '停机', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 8, NULL, NULL, '不在网', '不在网', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 8, NULL, NULL, '拆机', '拆机', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 8, NULL, NULL, '查询无结果', '查询无结果', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 9, NULL, NULL, '验证一致', '是', 2.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 9, NULL, NULL, '验证不一致', '否', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 10, NULL, NULL, '01', '0-2公里', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 10, NULL, NULL, '02', '2-5公里', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 10, NULL, NULL, '03', '5-10公里', NULL, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 10, NULL, NULL, '04', '10公里以上,但在同一个城市', NULL, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 10, NULL, NULL, '05', '不在同一个城市', 11.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 11, 0.00, 50.00, NULL, '0-50个', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 11, 50.00, 100.00, NULL, '50-100个', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 11, 100.00, NULL, NULL, '100个以上', 2.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 12, NULL, NULL, NULL, '联系人1一致', 1.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 12, NULL, NULL, NULL, '联系人1不一致', 0.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 12, NULL, NULL, NULL, '联系人2一致', 1.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 12, NULL, NULL, NULL, '联系人2不一致', 0.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 13, 8.00, NULL, NULL, '8家以上', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 13, 7.00, 8.00, NULL, '7-8家', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 13, 4.00, 6.00, NULL, '4-6家', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 13, 0.00, 3.00, NULL, '3家以内', 2.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 14, 1.00, 2.00, NULL, '1-2家', 3.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 14, 3.00, 4.00, NULL, '3-4家', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 14, 4.00, NULL, NULL, '4家以上', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 15, 3.00, NULL, NULL, '3万以上', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 15, 1.50, 3.00, NULL, '1.5-3万', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 15, 1.00, 1.50, NULL, '1-1.5万', 2.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 15, 0.50, 1.00, NULL, '0.5-1万', 3.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 15, NULL, 0.50, NULL, '0.5万以内', 5.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 16, 180.00, NULL, '180', '180天以上', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 16, 90.00, 180.00, '90', '90-180天', 1.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 16, 30.00, 90.00, NULL, '30-90天', 1.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 16, 1.00, 30.00, NULL, '30天以内', 1.00, NULL, b'0');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 16, 0.00, 1.00, '0', '无逾期', 10.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 17, 0.50, NULL, NULL, '0.5万元以上', 0.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 17, 0.20, 0.50, NULL, '0.2-0.5万元', 2.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 17, 0.10, 0.20, NULL, '0.1-0.2万元', 3.00, NULL, b'1');
INSERT INTO `t_risk_manage_score_option`(`id`, `type_id`, `min_value`, `max_value`, `compare_value`, `show_str`, `score`, `sequence`, `is_valid`) VALUES (null, 17, 0.00, 0.10, NULL, '0.1万元以内', 5.00, NULL, b'1');

INSERT INTO `t_risk_manage_type` VALUES (1, '同盾得分', 1, 1, 0.00, '分', 1, b'1');
INSERT INTO `t_risk_manage_type` VALUES (2, '智多分', 1, 1, 0.00, '分', 2, b'1');
INSERT INTO `t_risk_manage_type` VALUES (3, '是否在黑名单内', 2, 1, 0.00, '是/否', 3, b'0');
INSERT INTO `t_risk_manage_type` VALUES (4, '不良行为等级', 2, 2, 0.00, '等级', 4, b'1');
INSERT INTO `t_risk_manage_type` VALUES (5, '是否涉诉', 3, 2, 0.00, '是/否', 5, b'1');
INSERT INTO `t_risk_manage_type` VALUES (6, '号码归属地（与申报地匹配）', 4, 2, 0.00, '是否', 6, b'0');
INSERT INTO `t_risk_manage_type` VALUES (7, '入网时间', 4, 2, 0.00, '月', 7, b'1');
INSERT INTO `t_risk_manage_type` VALUES (8, '在网状态', 4, 2, 0.00, '是否正常', 8, b'1');
INSERT INTO `t_risk_manage_type` VALUES (9, '姓名、身份证与手机号匹配情况', 4, 2, 0.00, '是/否', 9, b'1');
INSERT INTO `t_risk_manage_type` VALUES (10, '居住地验证', 4, 2, 0.00, '是/否', 10, b'1');
INSERT INTO `t_risk_manage_type` VALUES (11, '通讯录联系人数', 4, 1, 0.00, '个', 11, b'1');
INSERT INTO `t_risk_manage_type` VALUES (12, '常用联系人', 4, 2, 0.00, '是/否', 12, b'0');
INSERT INTO `t_risk_manage_type` VALUES (13, '借款机构数', 5, 1, 0.00, '家', 13, b'1');
INSERT INTO `t_risk_manage_type` VALUES (14, '理财机构数', 5, 1, 0.00, '家', 14, b'1');
INSERT INTO `t_risk_manage_type` VALUES (15, '借款金额', 5, 1, 0.00, '万元', 15, b'1');
INSERT INTO `t_risk_manage_type` VALUES (16, '逾期天数', 6, 1, 0.00, '天', 16, b'1');
INSERT INTO `t_risk_manage_type` VALUES (17, '逾期金额', 6, 1, 0.00, '万元', 21, b'1');

INSERT INTO `t_risk_manage_big_type` VALUES (1, 4, '第三方得分', 40.00, 1, b'1');
INSERT INTO `t_risk_manage_big_type` VALUES (2, 4, '失信人黑名单配置', 20.00, 2, b'1');
INSERT INTO `t_risk_manage_big_type` VALUES (3, 4, '法院涉诉情况', 5.00, 3, b'1');
INSERT INTO `t_risk_manage_big_type` VALUES (4, 4, '运营商数据', 10.00, 4, b'1');
INSERT INTO `t_risk_manage_big_type` VALUES (5, 4, '多头借贷', 10.00, 5, b'1');
INSERT INTO `t_risk_manage_big_type` VALUES (6, 4, '逾期情况', 15.00, 6, b'1');

#--------------------------------------------额度申请
CREATE TABLE `t_credit_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL COMMENT '产品类型:ProductEnum枚举: 1信亿贷 2房亿贷 3农亿贷 4亿美贷',
  `consociation_user_id` bigint(20) DEFAULT NULL COMMENT '合作用户|机构 t_users.id',
  `accredit_pay_for_consociation` tinyint(1) DEFAULT NULL COMMENT '是否授权代付给合作人|机构',
  `items_amount` decimal(11,2) DEFAULT NULL COMMENT '项目总金额',
  `apply_credit_amount` decimal(11,2) DEFAULT NULL COMMENT '申请额度',
  `machine_score` decimal(11,2) DEFAULT NULL COMMENT '机审分数',
  `machine_credit_amount` decimal(11,2) DEFAULT NULL COMMENT '机审额度',
  `audit_credit_amount` decimal(11,2) DEFAULT NULL COMMENT '审批额度',
  `apply_period_unit` int(11) DEFAULT NULL COMMENT '借款期限单位-1: 年;0:月;1:日',
  `apply_period` int(11) DEFAULT NULL COMMENT '借款期限',
  `apply_apr` decimal(11,2) DEFAULT NULL COMMENT '审批利息',
  `service_amount` decimal(11,2) DEFAULT NULL COMMENT '服务费',
  `service_cost_rate` decimal(11,2) DEFAULT NULL COMMENT '服务费率',
  `service_cost_rule` int(11) DEFAULT NULL COMMENT '服务费计算规则:1.(总服务费=借款金额*服务费率)',
  `service_payment_model` int(11) DEFAULT NULL COMMENT '服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款)',
  `repayment_type_id` int(11) DEFAULT NULL  COMMENT '返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款\r\nselect * from t_dict_bid_repayment_types',
  `status` int(11) DEFAULT NULL COMMENT '-3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过 4.冻结 5.关闭\r\nselect * from  t_enum_map where enum_type=4',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `interface_risk_status` int(11) DEFAULT NULL COMMENT '风控接口状态:1成功,0失败',
  `interface_risk_msg` varchar(200) DEFAULT NULL COMMENT '风控接口调用结果描述',
  `interface_risk_time` datetime DEFAULT NULL COMMENT '风控接口调用时间',
  `remark` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '额度申请';

insert into t_enum_map values(4,0,4,'t_credit_apply.status','',0);
insert into t_enum_map values(1204,-1,4,'4','',0);
insert into t_enum_map values(1351,4,-3,'终审不通过','',0);
insert into t_enum_map values(1352,4,-2,'初审不通过','',1);
insert into t_enum_map values(1353,4,-1,'机审不通过','',0);
insert into t_enum_map values(1354,4,0,'待审','',1);
insert into t_enum_map values(1355,4,1,'机审通过','',0);
insert into t_enum_map values(1356,4,2,'初审通过','',1);
insert into t_enum_map values(1357,4,3,'终审通过','',0);
insert into t_enum_map values(1358,4,4,'冻结','',0);
insert into t_enum_map values(1359,4,5,'关闭','',0);

CREATE TABLE `t_credit_increase_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `credit_apply_id` bigint(20) DEFAULT NULL,
  `prepare_credit_amount` decimal(10,0) DEFAULT NULL COMMENT '提额前额度',
  `apply_credit_amount` decimal(10,0) DEFAULT NULL COMMENT '新申请额度:prepare_credit_amount+申请提额额度',
  `audit_credit_amount` decimal(10,0) DEFAULT NULL COMMENT '新审批额度:prepare_credit_amount+审批提额额度',
  `status` int(11) DEFAULT NULL COMMENT '-3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过\r\nselect * from  t_enum_map where enum_type=5',
  `apply_time` datetime DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `remark` varchar(2000) DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT = '提升合度申请';

insert into t_enum_map values(5,0,5,'t_credit_increase_apply.status','',0);
insert into t_enum_map values(1205,-1,5,'5','',0);
insert into t_enum_map values(1401,5,-3,'终审不通过','',0);
insert into t_enum_map values(1402,5,-2,'初审不通过','',1);
insert into t_enum_map values(1403,5,-1,'机审不通过','',1);
insert into t_enum_map values(1404,5,0,'待审','',0);
insert into t_enum_map values(1405,5,1,'机审通过','',1);
insert into t_enum_map values(1406,5,2,'初审通过','',1);
insert into t_enum_map values(1407,5,3,'终审通过','',0);

#--------------------------------------------借款申请
ALTER TABLE `t_bids`
MODIFY COLUMN `service_fees` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '!废弃 服务费' AFTER `bail`,
ADD COLUMN `service_amount` decimal(11,2) DEFAULT NULL COMMENT '服务费' AFTER `service_fees`,
ADD COLUMN `service_cost_rate` decimal(11,2) DEFAULT NULL COMMENT '服务费率 'AFTER `service_amount`,
ADD COLUMN `service_cost_rule` int(11) DEFAULT NULL COMMENT '服务费计算规则:1.(总服务费=借款金额*服务费率)'AFTER `service_cost_rate`,
ADD COLUMN `service_payment_model` int(11) DEFAULT NULL COMMENT '服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款)' AFTER `service_cost_rule`,
ADD COLUMN `consociation_user_id` bigint(20) DEFAULT NULL COMMENT '合作用户|机构 t_users.id' AFTER `service_payment_model`,
ADD COLUMN `accredit_pay_for_consociation` tinyint(1) DEFAULT NULL COMMENT '是否授权代付给合作人|机构' AFTER `consociation_user_id`;

ALTER TABLE `t_bills`
ADD COLUMN `service_amount` decimal(11,2) DEFAULT 0 COMMENT '服务费' AFTER `repayment_increase_interest2`,
ADD COLUMN `real_service_amount` decimal(11,2) DEFAULT 0 COMMENT '实际付服务费' AFTER `service_amount`;

ALTER TABLE `t_borrow_apply`
MODIFY COLUMN `product_id` int(4) NULL DEFAULT NULL COMMENT '产品类型:constants.ProductEnum类: 1信亿贷 2房亿贷 3农亿贷 4亿美贷' AFTER `user_id`,
MODIFY COLUMN `loan_property_id` int(4) NULL DEFAULT NULL COMMENT '申请借款性质:constants.UserTypeEnum类: 1个人借款 2企业借款 3个体工商户借款' AFTER `product_id`,
MODIFY COLUMN `period` int(4) NULL DEFAULT NULL COMMENT '借款期限(单位 月)' AFTER `approve_amount`,
MODIFY COLUMN `apply_area` int(10) NULL DEFAULT NULL COMMENT '申请区域' AFTER `period`;

ALTER TABLE `t_borrow_apply`
ADD COLUMN `credit_apply_id` bigint(20) DEFAULT NULL COMMENT 'product_id=4 then t_credit_apply.id else null' AFTER `product_id`,
ADD COLUMN `consociation_user_id` bigint(20) DEFAULT NULL COMMENT '合作用户|机构 t_users.id' AFTER `loan_purpose_id`,
ADD COLUMN `accredit_pay_for_consociation` tinyint(1) DEFAULT NULL COMMENT '是否授权代付给合作人|机构' AFTER `consociation_user_id`,
ADD COLUMN `period_unit` int(11) DEFAULT NULL COMMENT '借款期限单位-1: 年;0:月;1:日'AFTER `approve_amount`,
ADD COLUMN `interest_rate` decimal(11,2) DEFAULT NULL COMMENT '审批利息' AFTER `period`,
ADD COLUMN `service_amount` decimal(11,2) DEFAULT NULL COMMENT '服务费' AFTER `interest_rate`,
ADD COLUMN `service_cost_rate` decimal(11,2) DEFAULT NULL COMMENT '服务费率 'AFTER `service_amount`,
ADD COLUMN `service_cost_rule` int(11) DEFAULT NULL COMMENT '服务费计算规则:1.(总服务费=借款金额*服务费率)'AFTER `service_cost_rate`,
ADD COLUMN `service_payment_model` int(11) DEFAULT NULL COMMENT '服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款)' AFTER `service_cost_rule`,
ADD COLUMN `repayment_type_id` int(11) DEFAULT NULL  COMMENT '返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款\r\nselect * from t_dict_bid_repayment_types'AFTER `service_payment_model`;
/*
CREATE TABLE `t_borrow_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `borrow_no` varchar(50) DEFAULT NULL COMMENT '借款编号\r\nvalue=''S''+this.id+fun(this.product_id)\r\nfun=信亿贷为X  房亿贷为F 农亿贷为N',
  `user_id` bigint(20) DEFAULT NULL COMMENT '借款申请用户ID',
  `product_id` int(4) DEFAULT NULL COMMENT '产品类型:ProductEnum枚举: 1信亿贷 2房亿贷 3农亿贷 4亿美贷',
  `credit_apply_id` bigint(20) DEFAULT NULL COMMENT 'product_id=4 then t_credit_apply.id else null',
  `loan_property_id` int(4) DEFAULT NULL COMMENT '申请借款性质:UserTypeEnum枚举: 1个人借款 2企业借款 3个体工商户借款',
  `loan_purpose_id` int(4) DEFAULT NULL COMMENT '借款用途(参照t_dict_loan_purposes)',
  `consociation_user_id` bigint(20) DEFAULT NULL COMMENT '合作用户|机构 t_users.id',
  `accredit_pay_for_consociation` tinyint(1) DEFAULT NULL COMMENT '是否授权代付给合作人|机构',
  `apply_amount` decimal(12,2) DEFAULT NULL COMMENT '申请借款金额',
  `approve_amount` decimal(12,2) DEFAULT NULL COMMENT '审批金额',
  `period_unit` int(11) DEFAULT NULL COMMENT '借款期限单位-1: 年;0:月;1:日',
  `period` int(4) DEFAULT NULL COMMENT '借款期限(单位 月)',
  `interest_rate` decimal(11,2) DEFAULT NULL COMMENT '审批利息',
  `service_amount` decimal(11,2) DEFAULT NULL COMMENT '服务费',
  `service_cost_rate` decimal(11,2) DEFAULT NULL COMMENT '服务费率 ',
  `service_cost_rule` int(11) DEFAULT NULL COMMENT '服务费计算规则:1.(总服务费=借款金额*服务费率)',
  `service_payment_model` int(11) DEFAULT NULL COMMENT '服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款)',
  `repayment_type_id` int(11) DEFAULT NULL COMMENT '返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款\r\nselect * from t_dict_bid_repayment_types',
  `apply_area` int(10) DEFAULT NULL COMMENT '申请区域',
  `allot_area` int(10) DEFAULT NULL COMMENT '分配区域',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `status` int(4) DEFAULT NULL COMMENT '状态:select * from t_enum_map where enum_type=2',
  `allot_time` datetime DEFAULT NULL COMMENT '分配时间',
  `allot_admin` bigint(20) DEFAULT NULL COMMENT '分配管理员',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `submit_admin` bigint(20) DEFAULT NULL COMMENT '提交管理员',
  `audit_time` datetime DEFAULT NULL COMMENT '初审时间',
  `audit_admin` bigint(20) DEFAULT NULL COMMENT '初审管理员',
  `recheck_time` datetime DEFAULT NULL COMMENT '复审时间',
  `recheck_admin` bigint(20) DEFAULT NULL COMMENT '复审管理员',
  `reason` varchar(255) DEFAULT NULL COMMENT '不通过原因',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1106 DEFAULT CHARSET=utf8;
*/

#--------------------------------------------申请合作项目
CREATE TABLE `t_apply_org_project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apply_type` int(11) DEFAULT NULL COMMENT '1.credit_apply 2.borrow_apply',
  `apply_id` bigint(20) DEFAULT NULL COMMENT 'apply_type=1:t_credit_apply.id 2:t_borrow_apply.id',
  `org_project_id` bigint(20) DEFAULT NULL COMMENT 't_org_project.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='申请机构项目关系表';


#--------------------------------------------机构
CREATE TABLE `t_org_project` (
  `id` bigint(20) NOT NULL COMMENT 'id' AUTO_INCREMENT,
  `org_id` bigint(20) DEFAULT NULL COMMENT '机构id',
  `p_id` int(11) DEFAULT NULL COMMENT '父类目id,一级类目id为-1',
  `name` varchar(100) DEFAULT NULL COMMENT '类目名称',
  `is_use` bit(1) DEFAULT b'1' COMMENT '项目是否使用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='机构项目类目表';


CREATE TABLE `t_organization` (
  `id` bigint(20) NOT NULL COMMENT '机构id' AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `is_use` bit(1) DEFAULT b'1' COMMENT '机构是否使用',
  `org_type` int(11) DEFAULT NULL COMMENT '机构类型:1.亿美贷产品(product_id=4)合作机构',
  `bd_person_id` bigint(20) DEFAULT NULL COMMENT 'BD人员t_persion.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='机构表';

CREATE TABLE `t_interest_rate` (
  `id` bigint(20) NOT NULL COMMENT 'id' AUTO_INCREMENT,
  `org_id` bigint(20) DEFAULT NULL COMMENT '合作机构id,-1是全局默认利息率,如果一个合作机构的t_interest_rate,对应product_id的数据为0条,使用默认利息率',
  `product_id` int(11) DEFAULT NULL COMMENT '产品id',
  `repayment_type_id` tinyint(11) DEFAULT NULL COMMENT '返款方式:1.按月还款,等额本息 2.按月付息,到期还本 3.一次性还款 select * from t_dict_bid_repayment_types',
  `period_unit` int(11) DEFAULT NULL COMMENT '借款期限单位-1: 年;0:月;1:日',
  `period` int(4) DEFAULT NULL COMMENT '借款期限(单位 月)',
  `interest_rate` decimal(11,2) DEFAULT NULL COMMENT '利息率',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='合作机构利息率';

insert into t_interest_rate values(null,-1,null,null,null,null,8);
insert into t_interest_rate values(null,-1,4,1,0,1,8+period);
insert into t_interest_rate values(null,-1,4,1,0,2,8+period);
insert into t_interest_rate values(null,-1,4,1,0,3,8+period);
insert into t_interest_rate values(null,-1,4,1,0,4,8+period);
insert into t_interest_rate values(null,-1,4,1,0,5,8+period);
insert into t_interest_rate values(null,-1,4,1,0,6,8+period);
insert into t_interest_rate values(null,-1,4,1,0,7,8+period);
insert into t_interest_rate values(null,-1,4,1,0,8,8+period);
insert into t_interest_rate values(null,-1,4,1,0,9,8+period);
insert into t_interest_rate values(null,-1,4,1,0,10,8+period);
insert into t_interest_rate values(null,-1,4,1,0,11,8+period);
insert into t_interest_rate values(null,-1,4,1,0,12,8+period);

CREATE TABLE `t_service_cost_rate` (
  `id` bigint(20) NOT NULL COMMENT 'id' AUTO_INCREMENT,
  `org_id` bigint(20) DEFAULT NULL COMMENT '合作机构id,-1是全局默认服务费,如果一个合作机构的t_service,对应product_id的数据为0条,使用默认服务费',
  `product_id` int(11) DEFAULT NULL COMMENT '产品id',
  `period_unit` int(11) DEFAULT NULL COMMENT '借款期限单位-1: 年;0:月;1:日',
  `period` int(4) DEFAULT NULL COMMENT '借款期限(单位 月)',
  `service_cost_rate` decimal(11,2) DEFAULT NULL COMMENT '服务费率',
  `service_cost_rule` int(11) DEFAULT NULL COMMENT '服务费计算规则:1.(总服务费=借款金额*服务费率)',
  `service_payment_model` int(11) DEFAULT NULL COMMENT '服务费支付方式:1.按月还款(总服务费平摊到每期) (待开发 2.放款前支付 3.首期还款 4.末期还款) ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='合作机构服务费表';

insert into t_service_cost_rate values (null,-1,null,null,null,8,1,1);
insert into t_service_cost_rate values (null,-1,4,0,1,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,2,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,3,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,4,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,5,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,6,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,7,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,8,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,9,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,10,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,11,8+period,1,1);
insert into t_service_cost_rate values (null,-1,4,0,12,8+period,1,1);


#--------------------------------------------用户

CREATE TABLE `t_person` (
  `id` bigint(20) NOT NULL COMMENT 'id' AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `id_card` varchar(20) DEFAULT NULL COMMENT '身份证 ',
  `phone` varchar(50) DEFAULT NULL COMMENT '电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='人物信息表';

ALTER TABLE `t_users`
MODIFY COLUMN `address` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址,居住地' AFTER `id_number`,
MODIFY COLUMN `city_id` int(11) NULL DEFAULT 0 COMMENT '城市Id,居住地' AFTER `birthday`,
MODIFY COLUMN `postcode` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮编,居住地' AFTER `address`,
ADD COLUMN `company_city_id` int(11) DEFAULT 0 NULL COMMENT '公司城市Id, t_dict_city' AFTER `company`,
ADD COLUMN `id_picture_authentication_status` int(11) NULL COMMENT '身份证照片认证实名接口 状态:0.未实名,-1实名失败,1实名成功' AFTER `user_certificate_url`,
ADD COLUMN `id_picture_authentication_time` datetime(0) NULL COMMENT '身份证照片认证实名接口 时间,id_picture_authentication_status=-1|1有,0没有' AFTER `id_picture_authentication_status`,
ADD COLUMN `idcard_start_date` datetime(0) NULL COMMENT '身份证发证时间' AFTER `id_picture_authentication_time`,
ADD COLUMN `idcard_end_date` datetime(0) NULL COMMENT '身份证过期时间' AFTER `idcard_start_date`,
ADD COLUMN `living_authentication_status` int(11) NULL COMMENT '活体认证接口 状态:0.未实名,-1实名失败,1实名成功' AFTER `idcard_end_date`,
ADD COLUMN `living_authentication_time` datetime(0) NULL COMMENT '活体认证接口 时间,living_authentication_status=-1|1有,0没有' AFTER `living_authentication_status`,
ADD COLUMN `mobile_operator_authentication_status` int(11) NULL COMMENT '运营商认证状态:0.未认证,-1认证失败,1认证成功' AFTER `living_authentication_time`,
ADD COLUMN `mobile_operator_authentication_time` datetime(0) NULL COMMENT '运营商认证时间,-1,1有' AFTER `mobile_operator_authentication_status`,
ADD COLUMN `credit_apply_file_status` int(11) NULL COMMENT '用户风控补充资料,文件提交状态 0|null:未提交,1:已提交' AFTER `mobile_operator_authentication_time`,
ADD COLUMN `credit_apply_file_time` datetime(0) NULL COMMENT '用户风控补充资料,文件提交时间 credit_apply_file_status=1有' AFTER `credit_apply_file_status`,
ADD COLUMN `is_virtual_user`  bit(1) NULL DEFAULT b'0' COMMENT '是否是虚拟用户 0 否 1 是,如:医院机构' AFTER `credit_apply_file_time`;


ALTER TABLE `t_user_city` 
MODIFY COLUMN `province_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '户籍省' AFTER `user_id`,
MODIFY COLUMN `city_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '户籍市' AFTER `province`,
ADD COLUMN `address` varchar(200) NULL COMMENT '户籍地址/公司注册地址' AFTER `city`;

ALTER TABLE `t_users_info`
ADD COLUMN `work_industry` int(4) NULL COMMENT '工作性质:0自由职业者;1全职' AFTER `reg_capital`,
ADD COLUMN `salary`	decimal(10,2) NULL COMMENT '月工资' AFTER `work_industry`,
ADD COLUMN `accumulation_fund` decimal(10,0) NULL COMMENT '公积金汇缴数额' AFTER `salary`,
ADD COLUMN `rent` decimal(10,0)	NULL COMMENT '房租' AFTER `accumulation_fund`,
ADD COLUMN `QQ`	varchar(255) NULL COMMENT 'QQ|微信号' AFTER `rent`,
ADD COLUMN `brand_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '品牌名称/企业简称' AFTER `update_user_id`,
ADD COLUMN `first_contacts_id`  bigint(20) NULL COMMENT '常用联系人1t_person.id' AFTER `brand_name`,
ADD COLUMN `first_contacts_relation`  int(11) NULL COMMENT '常用联系人1和用户的关系 t_dict_relation' AFTER `first_contacts_id`,
ADD COLUMN `second_contacts_id`  bigint(20) NULL COMMENT '常用联系人2t_person.id' AFTER `first_contacts_relation`,
ADD COLUMN `second_contacts_relation`  int(11) NULL COMMENT '常用联系人2和用户的关系 t_dict_relation' AFTER `second_contacts_id`,
ADD COLUMN `legal_person_id`  bigint(20) NULL COMMENT '法人t_person.id' AFTER `second_contacts_relation`;

#------------------------------------------人员关系称谓
CREATE TABLE `t_dict_relation` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL COMMENT '关系名称',
  `code` varchar(50) DEFAULT NULL COMMENT '代码',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `is_use` bit(1) DEFAULT b'1' COMMENT '是否使用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='称谓表';

INSERT INTO `t_dict_relation` VALUES (1, '母亲', '01', '母亲', b'1');
INSERT INTO `t_dict_relation` VALUES (2, '父亲', '02', '父亲', b'1');
INSERT INTO `t_dict_relation` VALUES (3, '配偶', '03', '配偶', b'1');
INSERT INTO `t_dict_relation` VALUES (4, '兄弟', '04', '兄弟', b'1');
INSERT INTO `t_dict_relation` VALUES (5, '姐妹', '05', '姐妹', b'1');
INSERT INTO `t_dict_relation` VALUES (6, '亲戚', '06', '亲戚', b'1');
INSERT INTO `t_dict_relation` VALUES (7, '朋友', '07', '朋友', b'1');

#--------------------------------------------接口记录
CREATE TABLE `t_interface_call_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `request_id` varchar(40) NULL DEFAULT NULL COMMENT '请求给平台/接口的我方唯一id,查询用',
  `api_id` varchar(20)  DEFAULT NULL COMMENT '接口id',
  `business_type` int(11) NULL DEFAULT NULL COMMENT '业务编码 1,身份证照片实名 2,活体认证 3,防欺诈接口',
  `business_id` bigint(20) NULL DEFAULT NULL COMMENT '业务id:business_type=1:user.id 2:user.id 3:credit_apply.id',
  `request_time` datetime(0) DEFAULT NULL COMMENT '请求时间',
  `request_params` varchar(2000) DEFAULT NULL COMMENT '第三方请求参数(json格式)',
  `request_status` int(20) DEFAULT NULL COMMENT '请求结果0,未处理 1,处理中 2,完成 3,异常',
  `business_status` int(11) NULL DEFAULT NULL COMMENT '业务状态1成功,0失败',
  `result_msg` varchar(255) DEFAULT NULL COMMENT '返回信息',
  `response_params` blob DEFAULT NULL COMMENT '返回参数(json格式)',
  `response_time` datetime(0) NULL DEFAULT NULL COMMENT '返回时间',
  PRIMARY KEY (`id`) 
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COMMENT='接口调用记录';

#--------------------------------------------新增数据
insert into t_right_types values(13,'风控审核',null,null,null);
insert into t_rights values(211,13,'风控查询',null,null);
insert into t_rights values(212,13,'风控审核',null,null);
insert into t_rights values(213,13,'风控规则制定',null,null);
INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) VALUES (215, 13, '亿美贷审核基本信息设置', NULL, NULL);
INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) VALUES (216, 13, '亿美贷授信列表', NULL, NULL);
INSERT INTO `t_rights`(`id`, `type_id`, `name`, `code`, `description`) VALUES (217, 13, '机构列表', NULL, NULL);



insert into t_right_actions values(21100,211,'supervisor.ymd.CreditManageAction.creditApplyList','额度申请列表');
insert into t_right_actions values(21101,211,'supervisor.ymd.CreditManageAction.increaseCreditApplyList','提额申请列表');
insert into t_right_actions values(21102,211,'supervisor.ymd.CreditManageAction.YMDBorrowApplyList','亿美贷借款申请列表');
insert into t_right_actions values(21103,211,'supervisor.ymd.CreditManageAction.creditApplyInfo','额度申请详情');
insert into t_right_actions values(21104,211,'supervisor.ymd.CreditManageAction.increaseCreditApplyInfo','提额申请详情');
insert into t_right_actions values(21105,211,'supervisor.ymd.CreditManageAction.YMDBorrowApplyInfo','亿美贷借款申请详情');

insert into t_right_actions values(21200,212,'supervisor.ymd.CreditManageAction.audit','额度申请审核通过');
insert into t_right_actions values(21201,212,'supervisor.ymd.CreditManageAction.notThrough','额度申请审核拒绝');
insert into t_right_actions values(21202,212,'supervisor.ymd.CreditManageAction.freeze','冻结额度');
insert into t_right_actions values(21203,212,'supervisor.ymd.CreditManageAction.unfreeze','解冻额度');
insert into t_right_actions values(21204,212,'supervisor.ymd.CreditManageAction.close','关闭额度申请');
insert into t_right_actions values(21205,212,'supervisor.bidManager.BidApplicationAction.YMDApplicationCheck','亿美贷借款申请审核/拒绝/关闭');
INSERT INTO t_right_actions VALUES(21400,212, 'supervisor.ymd.CreditManageAction.auditIncreaseApply', '提额申请审核');

insert into t_right_actions values(21300,213,'supervisor.ymd.SettingAction.setRisk','风控规则配置');
insert into t_right_actions values(21301,213,'supervisor.ymd.SettingAction.saveAllRiskScore','风控规则配置提交');
insert into t_right_actions values(21601,216,'supervisor.ymd.UserManageAction.borrowerInfoList','亿美贷授信列表');

INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21500, 215, 'supervisor.ymd.SettingAction.basicallyInfo', '亿美贷审核基本信息设置');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21600, 216, 'supervisor.ymd.UserManageAction.borrowerInfo', '亿美贷授信列表详情');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21700, 217, 'supervisor.ymd.OrganizationAction.organizationList', '机构列表');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21701, 217, 'supervisor.ymd.OrganizationAction.organizationDetail', '机构列表详情');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21702, 217, 'supervisor.ymd.OrganizationAction.notEnableOrganization', '机构列表禁用');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21703, 217, 'supervisor.ymd.OrganizationAction.enableOrganization', '机构列表启用');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21704, 217, 'supervisor.ymd.OrganizationAction.editOrganizationInfo', '编辑机构信息');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21705, 217, 'supervisor.ymd.OrganizationAction.createOrgProject', '添加机构项目');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21706, 217, 'supervisor.ymd.OrganizationAction.deleteProject', '删除机构项目');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21707, 217, 'supervisor.ymd.OrganizationAction.createInterestRate', '添加商家利率');
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (21708, 217, 'ymd.BackFileUploadController.allFileSubmit', '机构添加图片');

INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('21501', '215', 'supervisor.ymd.SettingAction.saveSalaryCoefficient', '亿美贷审核月工资系数添加');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('21502', '215', 'supervisor.ymd.SettingAction.createServiceCostRate', '亿美贷审核服务费率添加');

INSERT INTO `t_system_options` (`id`, `_key`, `_value`, `description`) VALUES ('10061', 'salary_coefficient', '1.6', '月工资系数');


insert into t_dict_loan_purposes values(7,'医疗美容',null,null,1,7);

update t_enum_map set description='enum_type 为-1的记录,enum_code值表示t_log.relation_type,enum_name表示enum_type=0的enum_code,以此表示t_log.relation_type和enum_type=0的enum_name的关系'  where id=-1;
update t_enum_map set description='enum_type=-1为t_log.relation_type对应t_log.status的中间枚举,此处enum_code对应t_log.relation_type,通过enum_code查到对应enum_name,再以此enum_name为eunm_type,查询对应enum_type的enum_map,此时t_log.status对应enum_code,enum_name为状态名' where id=1201;
update t_products set min_amount=1000 where is_agency = 1;


CREATE TABLE `t_risk_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `status` int(11) DEFAULT NULL COMMENT '1 成功 2 失败 3 创建',
  `is_valid` int(11) DEFAULT NULL COMMENT '1 有效 2 无效',
  `report_response` varchar(7000) DEFAULT NULL COMMENT '资信报告返回的结果信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `h5_response_task_id` varchar(2000) DEFAULT NULL COMMENT 'H5页面授权返回taskId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='摩羯 返回资信报告信息';

insert into t_user_detail_types values(329,'扣除借款管理费',2,null,'还款账单中的借款管理费');
insert into t_platform_detail_types values(19,'借款管理费',1,'还款账单中的借款管理费');

CREATE OR REPLACE VIEW `v_bill_loan_v1` AS SELECT
	`b`.`id` AS `id`,
	`c`.`id` AS `user_id`,
	`b`.`title` AS `title`,
IF
	( ( ( SELECT count( 1 ) FROM `t_bills` `bi` WHERE ( ( `bi`.`id` = `a`.`id` ) AND ( `bi`.`overdue_mark` IN ( - ( 1 ),- ( 2 ),- ( 3 ) ) ) ) ) > 0 ), 1, 0 ) AS `is_overdue`,
	`b`.`period` AS `borrow_period`,
	ifnull( ( SELECT ( ( `b1`.`repayment_corpus` + `b1`.`repayment_interest` ) + `b1`.`overdue_fine` + ifnull(`b1`.`service_amount`,0) ) FROM `t_bills` `b1` WHERE ( ( `b1`.`bid_id` = `b`.`id` ) AND ( `b1`.`status` = - ( 1 ) ) ) ORDER BY `b1`.`periods` LIMIT 1 ), 0 ) AS `repayment_amount`,
	( SELECT `b2`.`repayment_time` FROM `t_bills` `b2` WHERE ( ( `b2`.`bid_id` = `b`.`id` ) AND ( `b2`.`status` = - ( 1 ) ) ) ORDER BY `b2`.`periods` LIMIT 1 ) AS `repayment_time`,
	`b`.`amount` AS `amount`,
	( SELECT count( `t`.`id` ) FROM `t_bills` `t` WHERE ( `t`.`bid_id` = `a`.`bid_id` ) ) AS `loan_periods`,
	( SELECT count( `t1`.`id` ) AS `count(``t1``.``id``)` FROM `t_bills` `t1` WHERE ( ( `t1`.`bid_id` = `a`.`bid_id` ) AND ( `t1`.`status` IN ( - ( 3 ), 0,- ( 2 ) ) ) ) ) AS `has_payed_periods`,
	`b`.`period_unit` AS `period_unit`,
	`b`.`status` AS `bid_status`,
	`b`.`audit_time` AS `audit_time` 
FROM
	( ( ( `t_bids` `b` LEFT JOIN `t_bills` `a` ON ( ( `a`.`bid_id` = `b`.`id` ) ) ) JOIN `t_users` `c` ON ( ( `b`.`user_id` = `c`.`id` ) ) ) JOIN `t_system_options` `d` ) 
WHERE
	( ( `d`.`_key` = 'loan_number' ) AND ( `b`.`status` IN ( 4, 5, 14 ) ) ) 
GROUP BY
	`b`.`id` DESC;

CREATE OR REPLACE VIEW `v_bill_repayment_record_v1` AS SELECT
	`a`.`id` AS `id`,
	`a`.`bid_id` AS `bid_id`,
	`a`.`title` AS `title`,
	sum( ( ( `a`.`repayment_corpus` + `a`.`repayment_interest` ) + `a`.`overdue_fine`+ ifnull(`a`.`service_amount`,0) ) ) AS `current_pay_amount`,
	`a`.`overdue_mark` AS `overdue_mark`,
	`a`.`status` AS `status`,
	`a`.`repayment_time` AS `repayment_time`,
	`a`.`real_repayment_time` AS `real_repayment_time`,
	`a`.`repayment_corpus` AS `repayment_corpus`,
	`a`.`repayment_interest` AS `repayment_interest`,
	ifnull(`a`.`service_amount`,0) `service_amount`,
	`a`.`periods` AS `current_period`,
	( SELECT count( `t`.`id` ) FROM `t_bills` `t` WHERE ( `t`.`bid_id` = `a`.`bid_id` ) ) AS `loan_periods` 
FROM
	( `t_bills` `a` JOIN `t_bids` `b` ON ( ( `a`.`bid_id` = `b`.`id` ) ) ) 
GROUP BY
	`a`.`id` 
ORDER BY
	`a`.`real_repayment_time` DESC;

CREATE OR REPLACE VIEW `v_bill_detail_v1` AS SELECT
	`a`.`id` AS `id`,
	`a`.`status` AS `status`,
	`b`.`user_id` AS `user_id`,
	`e`.`name` AS `name`,
	`b`.`id` AS `bid_id`,
	`a`.`periods` AS `current_period`,
	`a`.`repayment_time` AS `repayment_time`,
	( SELECT `c`.`name` FROM `t_dict_bid_repayment_types` `c` WHERE ( `b`.`repayment_type_id` = `c`.`id` ) ) AS `repayment_type`,
	`b`.`title` AS `bid_title`,
	`b`.`amount` AS `loan_amount`,
	( SELECT count( `t`.`id` ) FROM `t_bills` `t` WHERE ( `t`.`bid_id` = `a`.`bid_id` ) ) AS `loan_periods`,
	`b`.`apr` AS `apr`,
	`e`.`reality_name` AS `user_name`,
	`e`.`balance` AS `user_amount`,
	`b`.`audit_time` AS `produce_bill_time`,
	ifnull(
	( SELECT ( ( `bi`.`repayment_corpus` + `bi`.`repayment_interest` ) + `bi`.`overdue_fine` +ifnull(`bi`.`service_amount`,0) ) FROM `t_bills` `bi` WHERE ( ( `bi`.`bid_id` = `a`.`bid_id` ) AND ( `bi`.`status` = - ( 1 ) ) ) ORDER BY `bi`.`periods` LIMIT 1 ),
	( SELECT ( ( `b3`.`repayment_corpus` + `b3`.`repayment_interest` ) + `b3`.`overdue_fine` + ifnull(`b3`.`service_amount`,0) ) FROM `t_bills` `b3` WHERE ( ( `b3`.`bid_id` = `a`.`bid_id` ) AND ( `b3`.`status` <> - ( 1 ) ) ) ORDER BY `b3`.`periods` DESC LIMIT 1 ) 
	) AS `current_pay_amount`,
	( SELECT count( `t1`.`id` ) AS `count(``t1``.``id``)` FROM `t_bills` `t1` WHERE ( ( `t1`.`bid_id` = `a`.`bid_id` ) AND ( `t1`.`status` IN ( - ( 3 ), 0,- ( 2 ) ) ) ) ) AS `has_payed_periods`,
	`b`.`period` AS `borrow_period`,
	`b`.`period_unit` AS `period_unit`,
	ifnull( ( SELECT sum( ( ( `t`.`repayment_corpus` + `t`.`repayment_interest` ) + `t`.`overdue_fine`+ ifnull(`t`.`service_amount`,0) ) ) FROM `t_bills` `t` WHERE ( ( `t`.`bid_id` = `a`.`bid_id` ) AND ( `t`.`status` = - ( 1 ) ) ) ), 0 ) AS `remain_repay_amount`,
	`a`.`overdue_mark` AS `overdue_mark` 
FROM
	( ( `t_bills` `a` JOIN `t_bids` `b` ON ( ( `a`.`bid_id` = `b`.`id` ) ) ) JOIN `t_users` `e` ON ( ( `b`.`user_id` = `e`.`id` ) ) ) 
WHERE
	( `b`.`status` IN ( 4, 5, 14 ) ) 
ORDER BY
	`current_period` DESC;
	
	
#亿美贷合同
ALTER TABLE `t_bids`
ADD COLUMN `ymd_fq_fwxy_pact_location` varchar(500) NULL COMMENT '亿美贷（分期）服务费协议文件路径' AFTER `credit_amount`,
ADD COLUMN `ymd_fq_fwxy_pact_certificate_url` varchar(500) NULL COMMENT '亿美贷（分期）服务费协议文件存证号' AFTER `ymd_fq_fwxy_pact_location`;


#亿美贷服务协议
INSERT INTO `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-3001', '2019-02-28 09:43:21', '30', '服务费协议', '石轩宇', '1', '1', '1', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2019-02-28 00:00:00', '0', '0', '0', '');
INSERT INTO `t_content_news` (`id`, `time`, `type_id`, `title`, `author`, `content`, `keywords`, `descriptions`, `read_count`, `image_filename`, `image_filename2`, `show_type`, `location_pc`, `location_app`, `start_show_time`, `support`, `opposition`, `_order`, `is_use`) VALUES ('-3002', '2019-02-28 09:44:43', '30', '借款协议', '石轩宇', '<p class=\"MsoNormal\">\n	借款协议\n</p>', '借款协议', '借款协议', '0', '/public/images/default.png', '/public/images/default.png', '0', '0', '0', '2019-02-28 00:00:00', '0', '0', '0', '');

#2019-03-08新增
ALTER TABLE `t_user_withdrawals` 
ADD COLUMN `is_auto_create` tinyint(1) NULL DEFAULT 0 COMMENT '是否是放款后自动生成的提现单' AFTER `service_fee`;

insert into t_message_sms_templates values(60,now(),'放款成功,无标的服务费','s_financ_release_no_service_fee','尊敬的userName: 您申请的编号bidId借款标已成功放款，借款金额amount元，账户到账金额money元',0,1);


update t_file_relation_dict set max_num=8 where relation_type in(3,4,5) and file_dict_id in(31,32);
update t_file_relation_dict set max_num=1 where relation_type in(3,4,5)  and file_dict_id=5;
update t_file_relation_dict set max_num=2 where relation_type in(3,4,5)  and file_dict_id=6;
update t_file_relation_dict set max_num=3 where relation_type in(3,4,5)  and file_dict_id=7;
update t_file_relation_dict set max_num=4 where relation_type in(3,4,5)  and file_dict_id=8;
update t_file_relation_dict set max_num=5 where relation_type in(3,4,5)  and file_dict_id=9;
update t_file_relation_dict set max_num=6 where relation_type in(3,4,5)  and file_dict_id=10;


# 亿美贷账单类表添加服务费信息
CREATE OR REPLACE VIEW `v_bill_detail` AS select `a`.`id` AS `id`,`a`.`status` AS `status`,`b`.`user_id` AS `user_id`,`e`.`name` AS `name`,`b`.`id` AS `bid_id`,`a`.`periods` AS `current_period`,`a`.`repayment_time` AS `repayment_time`,`c`.`name` AS `repayment_type`,`b`.`title` AS `bid_title`,`b`.`amount` AS `loan_amount`,(select count(`t`.`id`) from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `loan_periods`,`b`.`apr` AS `apr`,`e`.`reality_name` AS `user_name`,sum((`e`.`balance` + `e`.`freeze`)) AS `user_amount`,`e`.`balance` AS `user_balance`,`b`.`audit_time` AS `produce_bill_time`,`a`.`service_amount` AS `service_amount`,`a`.`repayment_interest` AS `repayment_interest`,`a`.`repayment_corpus` AS `repayment_corpus`,`b`.`tag` AS `tag`,sum((((`a`.`repayment_corpus` + `a`.`repayment_interest`) + `a`.`service_amount`) + `a`.`overdue_fine`)) AS `current_pay_amount`,(select sum(((`t1`.`repayment_corpus` + `t1`.`repayment_interest`) + `t1`.`overdue_fine`)) AS `sumall` from `t_bills` `t1` where (`t1`.`bid_id` = `a`.`bid_id`)) AS `loan_principal_interest`,(select count(`t1`.`id`) AS `count(``t1``.``id``)` from `t_bills` `t1` where ((`t1`.`bid_id` = `a`.`bid_id`) and (`t1`.`status` in (-(3),0)))) AS `has_payed_periods`,concat(`d`.`_value`,cast(`a`.`id` as char charset utf8)) AS `bill_number` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_dict_bid_repayment_types` `c` on((`b`.`repayment_type_id` = `c`.`id`))) join `t_users` `e` on((`b`.`user_id` = `e`.`id`))) join `t_system_options` `d`) where (`d`.`_key` = 'loan_bill_number') group by `a`.`id`;
CREATE OR REPLACE VIEW `v_bill_has_received` AS select `a`.`id` AS `id`,year(`a`.`real_repayment_time`) AS `year`,month(`a`.`real_repayment_time`) AS `month`,`b`.`id` AS `bid_id`,concat(`d`.`_value`,cast(`a`.`id` as char charset utf8)) AS `bill_no`,`c`.`name` AS `name`,`c`.`reality_name` AS `reality_name`,concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,(((`a`.`repayment_corpus` + `a`.`repayment_interest`) + `a`.`service_amount`) + `a`.`overdue_fine`) AS `current_pay_amount`,`b`.`amount` AS `amount`,`b`.`apr` AS `apr`,`a`.`title` AS `title`,(select concat(`a`.`periods`,'/',count(`t`.`id`)) from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `period`,`a`.`repayment_time` AS `repayment_time`,`a`.`repayment_interest` AS `repayment_interest`,`a`.`service_amount` AS `service_amount`,`a`.`repayment_corpus` AS `repayment_corpus`,`b`.`tag` AS `tag`,(case when ((`a`.`repayment_time` - now()) > 0) then 0 else (to_days(now()) - to_days(`a`.`repayment_time`)) end) AS `overdue_time`,`a`.`real_repayment_time` AS `real_repayment_time`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`c`.`assigned_to_supervisor_id` = `a`.`id`)) AS `supervisor_name`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`b`.`manage_supervisor_id` = `a`.`id`)) AS `supervisor_name2` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`b`.`user_id` = `c`.`id`))) join `t_system_options` `d`) join `t_system_options` `e`) where ((`d`.`_key` = 'loan_bill_number') and (`e`.`_key` = 'loan_number') and (`a`.`status` in (0,-(3)))) group by `a`.`id`;
CREATE OR REPLACE VIEW `v_bill_receiving` AS select `a`.`id` AS `id`,year(`a`.`repayment_time`) AS `year`,month(`a`.`repayment_time`) AS `month`,`b`.`id` AS `bid_id`,concat(`d`.`_value`,cast(`a`.`id` as char charset utf8)) AS `bill_no`,`c`.`name` AS `name`,`c`.`mobile` AS `mobile`,`c`.`reality_name` AS `reality_name`,concat(`e`.`_value`,cast(`b`.`id` as char charset utf8)) AS `bid_no`,`b`.`amount` AS `amount`,`b`.`apr` AS `apr`,`a`.`title` AS `title`,(((`a`.`repayment_corpus` + `a`.`repayment_interest`) + `a`.`service_amount`) + `a`.`overdue_fine`) AS `repayment_money`,(select concat(`a`.`periods`,'/',count(`t1`.`id`)) from `t_bills` `t1` where (`t1`.`bid_id` = `a`.`bid_id`)) AS `period`,`a`.`repayment_time` AS `repayment_time`,`a`.`repayment_interest` AS `repayment_interest`,`a`.`repayment_corpus` AS `repayment_corpus`,`a`.`service_amount` AS `service_amount`,`b`.`tag` AS `tag`,(case when ((`a`.`repayment_time` - now()) > 0) then 0 else (to_days(now()) - to_days(`a`.`repayment_time`)) end) AS `overdue_time`,(select count(`t_bills`.`id`) AS `count(id)` from `t_bills` where ((`t_bills`.`bid_id` = `b`.`id`) and (`t_bills`.`overdue_mark` in (-(1),-(2),-(3))))) AS `overdue_count`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`c`.`assigned_to_supervisor_id` = `a`.`id`)) AS `supervisor_name`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`b`.`manage_supervisor_id` = `a`.`id`)) AS `supervisor_name2` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`b`.`user_id` = `c`.`id`))) join `t_system_options` `d`) join `t_system_options` `e`) where ((`d`.`_key` = 'loan_bill_number') and (`e`.`_key` = 'loan_number') and (`a`.`status` in (-(1),-(2)))) group by `a`.`id`;
CREATE OR REPLACE VIEW `v_bill_receiving_overdue` AS select `a`.`id` AS `id`,year(`a`.`repayment_time`) AS `year`,month(`a`.`repayment_time`) AS `month`,`b`.`id` AS `bid_id`,concat(`d`.`_value`,`a`.`id`) AS `bill_no`,`c`.`name` AS `name`,concat(`e`.`_value`,`b`.`id`) AS `bid_no`,`b`.`amount` AS `amount`,`b`.`apr` AS `apr`,`a`.`title` AS `title`,(select concat(`a`.`periods`,'/',count(`t`.`id`)) AS `concat(``a``.``periods``,'/',count(``t``.``id``))` from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `period`,`a`.`repayment_time` AS `repayment_time`,`a`.`repayment_interest` AS `repayment_interest`,`a`.`service_amount` AS `service_amount`,`a`.`repayment_corpus` AS `repayment_corpus`,`b`.`tag` AS `tag`,(case when ((`a`.`repayment_time` - now()) > 0) then 0 else (to_days(now()) - to_days(`a`.`repayment_time`)) end) AS `overdue_time`,`a`.`overdue_fine` AS `late_penalty`,(select count(`t_bills`.`id`) AS `count(id)` from `t_bills` where ((`t_bills`.`bid_id` = `b`.`id`) and (`t_bills`.`overdue_mark` in (-(1),-(2),-(3))))) AS `overdue_count`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`c`.`assigned_to_supervisor_id` = `a`.`id`)) AS `supervisor_name`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`b`.`manage_supervisor_id` = `a`.`id`)) AS `supervisor_name2` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`b`.`user_id` = `c`.`id`))) join `t_system_options` `d`) join `t_system_options` `e`) where ((`d`.`_key` = 'loan_bill_number') and (`e`.`_key` = 'loan_number') and (`a`.`overdue_mark` in (-(1),-(2),-(3))) and (`a`.`status` in (-(1),-(2)))) group by `a`.`id`;

#2019-03-20新增
# 新增应收账单明细信息
CREATE OR REPLACE VIEW `v_bill_all` AS select `a`.`id` AS `id`,year(`a`.`repayment_time`) AS `year`,month(`a`.`repayment_time`) AS `month`,`b`.`id` AS `bid_id`,concat(`d`.`_value`,`a`.`id`) AS `bill_no`,`c`.`name` AS `name`,concat(`e`.`_value`,`b`.`id`) AS `bid_no`,`b`.`amount` AS `amount`,`c`.`mobile` AS `mobile`,`c`.`reality_name` AS `reality_name`,`a`.`overdue_mark` AS `overdue_mark`,(case when (`a`.`status` = -(1)) then '待还款' when (`a`.`status` in (-(2),-(3),0)) then '已还款' end) AS `status`,(case when (`a`.`overdue_mark` = 0) then '未逾期' when (`a`.`overdue_mark` in (-(1),-(2),-(3))) then '已逾期' end) AS `overdue_status`,`b`.`apr` AS `apr`,`a`.`title` AS `title`,(((`a`.`repayment_corpus` + `a`.`repayment_interest`) + `a`.`service_amount`) + `a`.`overdue_fine`) AS `repayment_money`,(select concat(`a`.`periods`,'/',count(`t`.`id`)) AS `concat(``a``.``periods``,'/',count(``t``.``id``))` from `t_bills` `t` where (`t`.`bid_id` = `a`.`bid_id`)) AS `period`,`a`.`periods` AS `period_`,(case when (`b`.`period_unit` = -(1)) then '年' when (`b`.`period_unit` = 0) then '月' when (`b`.`period_unit` = 1) then '日' end) AS `period_unit`,`a`.`repayment_time` AS `repayment_time`,`a`.`repayment_interest` AS `repayment_interest`,`a`.`service_amount` AS `service_amount`,`a`.`repayment_corpus` AS `repayment_corpus`,`b`.`tag` AS `tag`,`b`.`audit_time` AS `release_time`,(case when ((`a`.`repayment_time` - now()) > 0) then 0 else (to_days(now()) - to_days(`a`.`repayment_time`)) end) AS `overdue_time`,`a`.`overdue_fine` AS `late_penalty`,(select count(`t_bills`.`id`) AS `count(id)` from `t_bills` where ((`t_bills`.`bid_id` = `b`.`id`) and (`t_bills`.`overdue_mark` in (-(1),-(2),-(3))))) AS `overdue_count`,`a`.`real_repayment_time` AS `real_repayment_time`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`c`.`assigned_to_supervisor_id` = `a`.`id`)) AS `supervisor_name`,(select `a`.`name` AS `name` from `t_supervisors` `a` where (`b`.`manage_supervisor_id` = `a`.`id`)) AS `supervisor_name2` from ((((`t_bills` `a` join `t_bids` `b` on((`a`.`bid_id` = `b`.`id`))) join `t_users` `c` on((`b`.`user_id` = `c`.`id`))) join `t_system_options` `d`) join `t_system_options` `e`) where ((`d`.`_key` = 'loan_bill_number') and (`e`.`_key` = 'loan_number')) group by `a`.`id`;
INSERT INTO `t_right_actions`(`id`, `right_id`, `action`, `description`) VALUES (5111, 102, 'supervisor.financeManager.ReceivableBillManager.allBills', '应收账单管理');

#2019-03-21
#放款后是否自动提现开关
insert into t_system_options values(10062,'is_auto_withdraw','false','放款后是否自动提现');

#亿美贷还款提醒数据
INSERT INTO `sp2p`.`t_message_sms_templates` (`id`, `time`, `title`, `scenarios`, `content`, `size`, `status`) VALUES ('61', '2019-03-21 16:34:44', '亿美贷还款提醒(未逾期)', 's_not_overdue_remind', '(亿美贷还款提醒)您本期账单待还amount元，请在remindDate前进行还款，以免产生逾期。客服电话：021-6438-0510', '0.00', '');
INSERT INTO `sp2p`.`t_message_sms_templates` (`id`, `time`, `title`, `scenarios`, `content`, `size`, `status`) VALUES ('62', '2019-03-21 16:34:44', '亿美贷还款提醒(已逾期)', 's_overdue_remind', '(亿美贷还款提醒)您本期账单待还amount元，已产生逾期，请尽快结清本期账单。客服电话：021-6438-0510', '0.00', '');

