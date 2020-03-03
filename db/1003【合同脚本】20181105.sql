ALTER TABLE `t_users`
ADD COLUMN `auto_pact_location`  varchar(255) NULL AFTER `finance_type`,
ADD COLUMN `user_pact_location`  varchar(255) NULL AFTER `auto_pact_location`,
ADD COLUMN `user_certificate_url`  text NULL AFTER `user_pact_location`;

ALTER TABLE `t_bids`
ADD COLUMN `consult_pact_location`  varchar(255) NULL AFTER `borrow_apply_id`,
ADD COLUMN `consult_certificate_url`  text NULL AFTER `consult_pact_location`;