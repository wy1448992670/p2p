ALTER TABLE `p2p_dev`.`t_red_packages_history`                             
ADD COLUMN `coupon_type` int(50) NULL DEFAULT 1 COMMENT '优惠券类型  1  红包  2  加息券' AFTER `remark`,
ADD COLUMN `bid_type` int(50) NULL DEFAULT 0 COMMENT '使用标的类型  0 未知   1  普通标 2  债权标' AFTER `coupon_type`;

update t_red_packages_history set bid_type=1 where invest_id <>0;

ALTER TABLE `p2p_dev`.`t_red_packages_type` 
ADD COLUMN `coupon_type` int(10) NULL DEFAULT 1 COMMENT '优惠券类型  1  红包  2  加息券' AFTER `reg_time`;

   