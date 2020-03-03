ALTER TABLE t_users DROP INDEX email;
ALTER TABLE t_users DROP INDEX qq_key;
ALTER TABLE t_users DROP INDEX weibo_key;
ALTER TABLE t_users DROP INDEX id_number;
ALTER TABLE t_user_bank_accounts DROP INDEX account_index;

ALTER TABLE `t_users` ADD INDEX id_number ( `id_number` ) ;

-- 1.设置仅有借款的用户为借款用户
update t_users u set u.finance_type = 0 where id in(
	SELECT aa.id from
	(
		select t1.id,
	case when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type < 2 then 2
				when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type > 1 then 3
		when t1.invest_count>0 and t1.borrow_count>0 then 1
		when t1.invest_count>0 then 2
		when t1.borrow_count>0 then 3
		end as type
		from
		(
			SELECT a.id,a.user_type,
				(select count(1) from t_invests ivt where ivt.user_id = a.id) as invest_count,
				(select count(1) from t_bids bid where bid.user_id = a.id) as borrow_count

			 from t_users a --  where id<3000
		)t1
	) aa where aa.type = 3
)
;
-- 2.设置仅有投资纪录的用户为投资用户
update t_users u set u.finance_type = 1 where id in(
	SELECT aa.id from
	(
		select t1.id,
	case when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type < 2 then 2
				when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type > 1 then 3
		when t1.invest_count>0 and t1.borrow_count>0 then 1
		when t1.invest_count>0 then 2
		when t1.borrow_count>0 then 3
		end as type
		from
		(
			SELECT a.id,a.user_type,
				(select count(1) from t_invests ivt where ivt.user_id = a.id) as invest_count,
				(select count(1) from t_bids bid where bid.user_id = a.id) as borrow_count

			 from t_users a --  where id<3000
		)t1
	) aa where aa.type = 2
)
;
-- 3 既有投资又有借款的用户先设置投资用户
update t_users u set u.finance_type = 1 where id in(
	SELECT aa.id from
	(
		select t1.id,
	case when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type < 2 then 2
				when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type > 1 then 3
		when t1.invest_count>0 and t1.borrow_count>0 then 1
		when t1.invest_count>0 then 2
		when t1.borrow_count>0 then 3
		end as type
		from
		(
			SELECT a.id,a.user_type,
				(select count(1) from t_invests ivt where ivt.user_id = a.id) as invest_count,
				(select count(1) from t_bids bid where bid.user_id = a.id) as borrow_count

			 from t_users a --  where id<3000
		)t1
	) aa where aa.type = 1
)
;





-- 4 对这些用户创建新的借款用户，然后转移相关借款标的及还款计划的相关数据
DROP PROCEDURE IF EXISTS `proc_migrate_user_data`;
create PROCEDURE proc_migrate_user_data ()
BEGIN



	DECLARE `v_id` bigint(20);
	DECLARE `v_time` timestamp;
	DECLARE `v_name` varchar(500);
	DECLARE `v_client` int(10);
	DECLARE `v_photo` varchar(100);
	DECLARE `v_reality_name` varchar(500);
	DECLARE `v_password` varchar(32);
	DECLARE `v_password_continuous_errors` int(11);
	DECLARE `v_is_password_error_locked` bit(1);
	DECLARE `v_password_error_locked_time` datetime;
	DECLARE `v_pay_password` varchar(50);
	DECLARE `v_pay_password_continuous_errors` int(11);
	DECLARE `v_is_pay_password_error_locked` bit(1);
	DECLARE `v_pay_password_error_locked_time` datetime;
	DECLARE `v_is_allow_login` bit(1);
	DECLARE `v_lock_time` datetime;
	DECLARE `v_login_count` bigint(20);
	DECLARE `v_last_login_time` datetime;
	DECLARE `v_login_client` tinyint(4);
	DECLARE `v_last_login_ip` varchar(50);
	DECLARE `v_last_logout_time` datetime;
	DECLARE `v_email` varchar(50);
	DECLARE `v_is_email_verified` bit(1);
	DECLARE `v_mobile` varchar(50);
	DECLARE `v_is_mobile_verified` bit(1);
	DECLARE `v_is_secret_set` bit(1);
	DECLARE `v_secret_set_time` datetime;
	DECLARE `v_secret_question_id1` int(11);
	DECLARE `v_answer1` varchar(255);
	DECLARE `v_secret_question_id2` int(11);
	DECLARE `v_answer2` varchar(255);
	DECLARE `v_secret_question_id3` int(11);
	DECLARE `v_answer3` varchar(255);
	DECLARE `v_id_number` varchar(50);
	DECLARE `v_address` varchar(200);
	DECLARE `v_postcode` varchar(50);
	DECLARE `v_sex` tinyint(4);
	DECLARE `v_birthday` datetime;
	DECLARE `v_city_id` int(11);
	DECLARE `v_family_address` varchar(100);
	DECLARE `v_family_telephone` varchar(50);
	DECLARE `v_company` varchar(100);
	DECLARE `v_company_address` varchar(100);
	DECLARE `v_office_telephone` varchar(50);
	DECLARE `v_fax_number` varchar(50);
	DECLARE `v_education_id` int(11);
	DECLARE `v_marital_id` int(11);
	DECLARE `v_house_id` int(11);
	DECLARE `v_car_id` int(11);
	DECLARE `v_is_add_base_info` bit(1);
	DECLARE `v_is_erased` bit(1);
	DECLARE `v_recommend_time` datetime;
	DECLARE `v_recommend_user_id` bigint(20);
	DECLARE `v_recommend_reward_type` tinyint(4);
	DECLARE `v_master_identity` tinyint(4);
	DECLARE `v_master_client` tinyint(4);
	DECLARE `v_master_time_loan` datetime;
	DECLARE `v_master_time_invest` datetime;
	DECLARE `v_master_time_complex` datetime;
	DECLARE `v_vip_status` bit(1);
	DECLARE `v_balance` decimal(20,2);
	DECLARE `v_balance2` decimal(20,2);
	DECLARE `v_freeze` decimal(20,2);
	DECLARE `v_score` int(11);
	DECLARE `v_credit_score` int(11);
	DECLARE `v_credit_level_id` int(11);
	DECLARE `v_is_refused_receive` bit(1);
	DECLARE `v_refused_time` datetime;
	DECLARE `v_refused_reason` varchar(200);
	DECLARE `v_is_blacklist` bit(1);
	DECLARE `v_joined_time` datetime;
	DECLARE `v_joined_reason` varchar(200);
	DECLARE `v_assigned_time` datetime;
	DECLARE `v_assigned_to_supervisor_id` bigint(20);
	DECLARE `v_telephone` varchar(255);
	DECLARE `v_credit_line` decimal(20,2);
	DECLARE `v_last_credit_line` decimal(20,2);
	DECLARE `v_is_active` bit(1);
	DECLARE `v_sign1` varchar(250);
	DECLARE `v_sign2` varchar(250);
	DECLARE `v_qq_key` varchar(50);
	DECLARE `v_weibo_key` varchar(50);
	DECLARE `v_qr_code` varchar(200);
	DECLARE `v_ips_acct_no` varchar(200);
	DECLARE `v_ips_bid_auth_no` varchar(200);
	DECLARE `v_ips_repay_auth_no` varchar(200);
	DECLARE `v_device_user_id` varchar(200);
	DECLARE `v_channel_id` varchar(200);
	DECLARE `v_device_type` tinyint(4);
	DECLARE `v_is_bill_push` bit(1);
	DECLARE `v_is_invest_push` bit(1);
	DECLARE `v_is_activity_push` bit(1);
	DECLARE `v_open_id` varchar(80);
	DECLARE `v_is_bank` bit(1);
	DECLARE `v_forum_name` varchar(200);
	DECLARE `v_risk_type` int(2);
	DECLARE `v_risk_result` varchar(255);
	DECLARE `v_risk_answer` varchar(255);
	DECLARE `v_is_cps_blacklist` int(11);
	DECLARE `v_cps_blacklist_dt` datetime;
	DECLARE `v_is_sms_blacklist` tinyint(3);
	DECLARE `v_sms_blacklist_dt` datetime;
	DECLARE `v_user_type` int(10);
	DECLARE `v_age` int(3);
	DECLARE `v_credit_amount` decimal(20,2);
	DECLARE `v_law_suit` varchar(40);
	DECLARE `v_credit_report` varchar(255);
	DECLARE `v_finance_type` int(255);

	DECLARE  no_more_record INT DEFAULT 0;
	DECLARE  cur_record CURSOR FOR
					select
					id,now(),CONCAT('jkr',mobile,login_count),client,photo,reality_name,`password`,password_continuous_errors,is_password_error_locked,password_error_locked_time,pay_password,pay_password_continuous_errors,is_pay_password_error_locked,pay_password_error_locked_time,is_allow_login,lock_time,login_count,last_login_time,login_client,last_login_ip,last_logout_time,email,is_email_verified,mobile,is_mobile_verified,is_secret_set,secret_set_time,secret_question_id1,answer1,secret_question_id2,answer2,secret_question_id3,answer3,id_number,address,postcode,sex,birthday,city_id,family_address,family_telephone,company,company_address,office_telephone,fax_number,education_id,marital_id,house_id,car_id,is_add_base_info,is_erased,recommend_time,recommend_user_id,recommend_reward_type,master_identity,master_client,master_time_loan,master_time_invest,master_time_complex,vip_status,0,0,0,score,credit_score,credit_level_id,is_refused_receive,refused_time,refused_reason,is_blacklist,joined_time,joined_reason,assigned_time,assigned_to_supervisor_id,telephone,credit_line,last_credit_line,is_active,sign1,sign2,qq_key,weibo_key,qr_code,ips_acct_no,ips_bid_auth_no,ips_repay_auth_no,device_user_id,channel_id,device_type,is_bill_push,is_invest_push,is_activity_push,open_id,is_bank,forum_name,risk_type,risk_result,risk_answer,is_cps_blacklist,cps_blacklist_dt,is_sms_blacklist,sms_blacklist_dt,user_type,age,credit_amount,law_suit,credit_report,0
					from t_users u where id in(
						SELECT aa.id from
						(
							select t1.id,
						case when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type < 2 then 2
									when t1.invest_count=0 and t1.borrow_count=0 and t1.user_type > 1 then 3
							when t1.invest_count>0 and t1.borrow_count>0 then 1
							when t1.invest_count>0 then 2
							when t1.borrow_count>0 then 3
							end as type
							from
							(
								SELECT a.id,a.user_type,
									(select count(1) from t_invests ivt where ivt.user_id = a.id) as invest_count,
									(select count(1) from t_bids bid where bid.user_id = a.id) as borrow_count

								 from t_users a --  where id<3000
							)t1
						) aa where aa.type = 1 -- and aa.id = 2301
					);

	DECLARE CONTINUE HANDLER FOR NOT FOUND SET  no_more_record = 1;

	set @current_user_id=0;

	OPEN  cur_record; -- 打开游标

 -- 遍历
	read_loop: LOOP
		FETCH  cur_record INTO `v_id`, `v_time`, `v_name`, `v_client`, `v_photo`, `v_reality_name`, `v_password`, `v_password_continuous_errors`, `v_is_password_error_locked`, `v_password_error_locked_time`, `v_pay_password`, `v_pay_password_continuous_errors`, `v_is_pay_password_error_locked`, `v_pay_password_error_locked_time`, `v_is_allow_login`, `v_lock_time`, `v_login_count`, `v_last_login_time`, `v_login_client`, `v_last_login_ip`, `v_last_logout_time`, `v_email`, `v_is_email_verified`, `v_mobile`, `v_is_mobile_verified`, `v_is_secret_set`, `v_secret_set_time`, `v_secret_question_id1`, `v_answer1`, `v_secret_question_id2`, `v_answer2`, `v_secret_question_id3`, `v_answer3`, `v_id_number`, `v_address`, `v_postcode`, `v_sex`, `v_birthday`, `v_city_id`, `v_family_address`, `v_family_telephone`, `v_company`, `v_company_address`, `v_office_telephone`, `v_fax_number`, `v_education_id`, `v_marital_id`, `v_house_id`, `v_car_id`, `v_is_add_base_info`, `v_is_erased`, `v_recommend_time`, `v_recommend_user_id`, `v_recommend_reward_type`, `v_master_identity`, `v_master_client`, `v_master_time_loan`, `v_master_time_invest`, `v_master_time_complex`, `v_vip_status`, `v_balance`, `v_balance2`, `v_freeze`, `v_score`, `v_credit_score`, `v_credit_level_id`, `v_is_refused_receive`, `v_refused_time`, `v_refused_reason`, `v_is_blacklist`, `v_joined_time`, `v_joined_reason`, `v_assigned_time`, `v_assigned_to_supervisor_id`, `v_telephone`, `v_credit_line`, `v_last_credit_line`, `v_is_active`, `v_sign1`, `v_sign2`, `v_qq_key`, `v_weibo_key`, `v_qr_code`, `v_ips_acct_no`, `v_ips_bid_auth_no`, `v_ips_repay_auth_no`, `v_device_user_id`, `v_channel_id`, `v_device_type`, `v_is_bill_push`, `v_is_invest_push`, `v_is_activity_push`, `v_open_id`, `v_is_bank`, `v_forum_name`, `v_risk_type`, `v_risk_result`, `v_risk_answer`, `v_is_cps_blacklist`, `v_cps_blacklist_dt`, `v_is_sms_blacklist`, `v_sms_blacklist_dt`, `v_user_type`, `v_age`, `v_credit_amount`, `v_law_suit`, `v_credit_report`, `v_finance_type`;

		IF no_more_record = 1 THEN
			LEAVE read_loop;
		END IF;


			-- 1添加有投资借款纪录的新借款用户，之前的用户已设置为投资用户
		 INSERT  INTO `t_users` (
				`time`, `name`, `client`, `photo`, `reality_name`, `password`, `password_continuous_errors`, `is_password_error_locked`, `password_error_locked_time`, `pay_password`, `pay_password_continuous_errors`, `is_pay_password_error_locked`, `pay_password_error_locked_time`, `is_allow_login`, `lock_time`, `login_count`, `last_login_time`, `login_client`, `last_login_ip`, `last_logout_time`, `email`, `is_email_verified`, `mobile`, `is_mobile_verified`, `is_secret_set`, `secret_set_time`, `secret_question_id1`, `answer1`, `secret_question_id2`, `answer2`, `secret_question_id3`, `answer3`, `id_number`, `address`, `postcode`, `sex`, `birthday`, `city_id`, `family_address`, `family_telephone`, `company`, `company_address`, `office_telephone`, `fax_number`, `education_id`, `marital_id`, `house_id`, `car_id`, `is_add_base_info`, `is_erased`, `recommend_reward_type`, `master_identity`, `master_client`, `master_time_loan`, `master_time_invest`, `master_time_complex`, `vip_status`, `balance`, `balance2`, `freeze`, `score`, `credit_score`, `credit_level_id`, `is_refused_receive`, `refused_time`, `refused_reason`, `is_blacklist`, `joined_time`, `joined_reason`, `assigned_time`, `assigned_to_supervisor_id`, `telephone`, `credit_line`, `last_credit_line`, `is_active`, `sign1`, `sign2`, `qq_key`, `weibo_key`, `qr_code`, `ips_acct_no`, `ips_bid_auth_no`, `ips_repay_auth_no`, `device_user_id`, `channel_id`, `device_type`, `is_bill_push`, `is_invest_push`, `is_activity_push`, `open_id`, `is_bank`, `forum_name`, `risk_type`, `risk_result`, `risk_answer`, `is_cps_blacklist`, `cps_blacklist_dt`, `is_sms_blacklist`, `sms_blacklist_dt`, `user_type`, `age`, `credit_amount`, `law_suit`, `credit_report`, `finance_type`
				)
				VALUES(`v_time`, `v_name`, `v_client`, `v_photo`, `v_reality_name`, `v_password`, `v_password_continuous_errors`, `v_is_password_error_locked`, `v_password_error_locked_time`, `v_pay_password`, `v_pay_password_continuous_errors`, `v_is_pay_password_error_locked`, `v_pay_password_error_locked_time`, `v_is_allow_login`, `v_lock_time`, `v_login_count`, `v_last_login_time`, `v_login_client`, `v_last_login_ip`, `v_last_logout_time`, `v_email`, `v_is_email_verified`, `v_mobile`, `v_is_mobile_verified`, `v_is_secret_set`, `v_secret_set_time`, `v_secret_question_id1`, `v_answer1`, `v_secret_question_id2`, `v_answer2`, `v_secret_question_id3`, `v_answer3`, `v_id_number`, `v_address`, `v_postcode`, `v_sex`, `v_birthday`, `v_city_id`, `v_family_address`, `v_family_telephone`, `v_company`, `v_company_address`, `v_office_telephone`, `v_fax_number`, `v_education_id`, `v_marital_id`, `v_house_id`, `v_car_id`, `v_is_add_base_info`, `v_is_erased`, `v_recommend_reward_type`, `v_master_identity`, `v_master_client`, `v_master_time_loan`, `v_master_time_invest`, `v_master_time_complex`, `v_vip_status`, `v_balance`, `v_balance2`, `v_freeze`, `v_score`, `v_credit_score`, `v_credit_level_id`, `v_is_refused_receive`, `v_refused_time`, `v_refused_reason`, `v_is_blacklist`, `v_joined_time`, `v_joined_reason`, `v_assigned_time`, `v_assigned_to_supervisor_id`, `v_telephone`, `v_credit_line`, `v_last_credit_line`, `v_is_active`, `v_sign1`, `v_sign2`, `v_qq_key`, `v_weibo_key`, `v_qr_code`, `v_ips_acct_no`, `v_ips_bid_auth_no`, `v_ips_repay_auth_no`, `v_device_user_id`, `v_channel_id`, `v_device_type`, `v_is_bill_push`, `v_is_invest_push`, `v_is_activity_push`, `v_open_id`, `v_is_bank`, `v_forum_name`, `v_risk_type`, `v_risk_result`, `v_risk_answer`, `v_is_cps_blacklist`, `v_cps_blacklist_dt`, `v_is_sms_blacklist`, `v_sms_blacklist_dt`, `v_user_type`, `v_age`, `v_credit_amount`, `v_law_suit`, `v_credit_report`, `v_finance_type`);

		 select @@IDENTITY into @current_user_id;

			-- 修改资金防改动
			update t_users set sign1 = MD5(CONCAT(@current_user_id,'0.00.0xVuUD0FAMKkzYq05')) ,sign2 =MD5(CONCAT(@current_user_id,'0.00.00.00.0xVuUD0FAMKkzYq05')) where id = @current_user_id;

			-- 2转移之前的借款数据到新用户上
			SELECT @current_user_id;

				-- 转移借款标表数据
				update t_bids set user_id = @current_user_id where user_id = v_id;

				-- 转移银行卡信息
				INSERT into t_user_bank_accounts(`user_id`, `time`, `bank_name`, `bank_code`, `account`, `account_name`, `mobile`, `verified`, `verify_time`, `verify_supervisor_id`, `branch_bank_name`, `province`, `province_code`, `city`, `city_code`, `protocol_no`, `is_valid`)
					select @current_user_id,time,bank_name,bank_code,account,account_name,mobile,verified,verify_time,verify_supervisor_id,branch_bank_name,province,province_code,city,city_code,protocol_no,is_valid from t_user_bank_accounts where user_id = v_id;

				-- 转移city
				INSERT into t_user_city(`user_id`, `province_id`, `province`, `city_id`, `city`)
					select @current_user_id,province_id,province,city_id,city from t_user_city where user_id = v_id;


				-- 转移资金明细
				-- INSERT into t_user_details(`user_id`, `time`, `operation`, `amount`, `relation_id`, `balance`, `freeze`, `recieve_amount`, `summary`)
				-- 	select @current_user_id,time,operation,amount,relation_id,balance,freeze,recieve_amount,summary from t_user_details where user_id = v_id;

				-- 转移老的审核图片内容数据
				update t_user_audit_items set user_id = @current_user_id where user_id = v_id;



  END LOOP;

	CLOSE  cur_record;  /*用完后记得用CLOSE把资源释放掉*/

END;



