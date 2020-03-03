
ALTER TABLE `t_red_packages_history` 
ADD COLUMN `coupon_type` int(50) DEFAULT 1 COMMENT '优惠券类型  1  红包  2  加息券' AFTER `remark`;

ALTER TABLE `t_red_packages_history` 
ADD COLUMN `bid_type` int(50) DEFAULT 0 COMMENT '使用标的类型  0 未知   1  普通标 2  债权标' AFTER `remark`;

ALTER TABLE `t_red_packages_type` 
ADD COLUMN `coupon_type` int(10) DEFAULT 1 COMMENT '优惠券类型  1  红包  2  加息券' AFTER `reg_time`;

INSERT INTO `t_platform_detail_types`(`id`, `name`, `type`, `description`) VALUES (18, '加息券费用', 2, '加息券费用');

##加息券短信模板
INSERT INTO `t_message_sms_templates`(`id`, `time`, `title`, `scenarios`, `content`, `size`, `status`) 
VALUES (59, SYSDATE(), '加息券赠送', 's_Rate_gift', '尊敬的userName:,您好！恭喜您获得redPackageName加息券，加息年化利率money%，请您打开APP进行查看或者投资时使用', 0.00, b'0');

##加息券邮件模板
INSERT INTO `t_message_email_templates`(`id`, `time`, `scenarios`, `title`, `content`, `size`, `status`) 
VALUES (59, SYSDATE(), 'e_Rate_gift', '加息券赠送', '尊敬的userName[date]:,您好！您已获得赠送加息券，加息年化利率money%，请您继续在网站进行查看或者理财', 4.00, b'1');

##加息券站内信模板
INSERT INTO `t_message_station_templates`(`id`, `time`, `scenarios`, `title`, `content`, `size`, `status`, `type`) 
VALUES (59, SYSDATE(), 'm_Rate_gift', '加息券赠送', '尊敬的userName[date]:,您好！您已获得赠送加息券，加息年化利率money%，请您继续在网站进行查看或者理财', 0.00, b'1', 0);

##数据库比对脚本
ALTER TABLE `t_red_packages_history` MODIFY COLUMN `coupon_type`  int(50) NULL DEFAULT 1 COMMENT '优惠券类型  1  红包  2  加息券' AFTER `remark`;
ALTER TABLE `t_red_packages_type` MODIFY COLUMN `coupon_type`  int(10) NULL DEFAULT NULL COMMENT '优惠券类型  1  红包  2  加息券' AFTER `reg_time`;
ALTER TABLE `t_users` MODIFY COLUMN `user_type`  int(10) UNSIGNED NULL DEFAULT 0 COMMENT '0 未知 1 个人用户  2 企业用户 3 个体工商户' AFTER `sms_blacklist_dt`;

