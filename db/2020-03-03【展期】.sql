ALTER TABLE `t_bills`
ADD COLUMN `is_defer`  bit(1) NULL DEFAULT 0 AFTER `real_service_amount`;

ALTER  
VIEW `v_bill_receiving` AS 
SELECT
	`a`.`id` AS `id`,
	a.is_defer as is_defer,
	YEAR (`a`.`repayment_time`) AS `year`,
	MONTH (`a`.`repayment_time`) AS `month`,
	`b`.`id` AS `bid_id`,
	concat(
		`d`.`_value`,
		cast(`a`.`id` AS CHAR charset utf8)
	) AS `bill_no`,
	`c`.`name` AS `name`,
	`c`.`mobile` AS `mobile`,
	`c`.`reality_name` AS `reality_name`,
	concat(
		`e`.`_value`,
		cast(`b`.`id` AS CHAR charset utf8)
	) AS `bid_no`,
	`b`.`amount` AS `amount`,
	`b`.`apr` AS `apr`,
	`a`.`title` AS `title`,
	(
		(
			(
				`a`.`repayment_corpus` + `a`.`repayment_interest`
			) + `a`.`service_amount`
		) + `a`.`overdue_fine`
	) AS `repayment_money`,
	(
		SELECT
			concat(
				`a`.`periods`,
				'/',
				count(`t1`.`id`)
			)
		FROM
			`t_bills` `t1`
		WHERE
			(`t1`.`bid_id` = `a`.`bid_id`)
	) AS `period`,
	`a`.`repayment_time` AS `repayment_time`,
	`a`.`repayment_interest` AS `repayment_interest`,
	`a`.`repayment_corpus` AS `repayment_corpus`,
	`a`.`service_amount` AS `service_amount`,
	`b`.`tag` AS `tag`,
	(
		CASE
		WHEN ((`a`.`repayment_time` - now()) > 0) THEN
			0
		ELSE
			(
				to_days(now()) - to_days(`a`.`repayment_time`)
			)
		END
	) AS `overdue_time`,
	(
		SELECT
			count(`t_bills`.`id`) AS `count(id)`
		FROM
			`t_bills`
		WHERE
			(
				(`t_bills`.`bid_id` = `b`.`id`)
				AND (
					`t_bills`.`overdue_mark` IN (-(1) ,-(2) ,-(3))
				)
			)
	) AS `overdue_count`,
	(
		SELECT
			`a`.`name` AS `name`
		FROM
			`t_supervisors` `a`
		WHERE
			(
				`c`.`assigned_to_supervisor_id` = `a`.`id`
			)
	) AS `supervisor_name`,
	(
		SELECT
			`a`.`name` AS `name`
		FROM
			`t_supervisors` `a`
		WHERE
			(
				`b`.`manage_supervisor_id` = `a`.`id`
			)
	) AS `supervisor_name2`
FROM
	(
		(
			(
				(
					`t_bills` `a`
					JOIN `t_bids` `b` ON ((`a`.`bid_id` = `b`.`id`))
				)
				JOIN `t_users` `c` ON ((`b`.`user_id` = `c`.`id`))
			)
			JOIN `t_system_options` `d`
		)
		JOIN `t_system_options` `e`
	)
WHERE
	(
		(`d`.`_key` = 'loan_bill_number')
		AND (`e`.`_key` = 'loan_number')
		AND (`a`.`status` IN(-(1) ,-(2)))
	)
GROUP BY
	`a`.`id` ;

