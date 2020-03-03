
update t_user_bank_accounts set is_sign = 0 where protocol_no is not null;


INSERT INTO  `t_content_news_types`(`id`, `parent_id`, `name`, `description`, `_order`, `status`) VALUES (null, 6, '扣款授权协议', NULL, 7, b'1');
update t_dict_banks_col set bank_code = 404 where id= 103;