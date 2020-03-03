set character_set_database=utf8;
set collation_database=utf8_general_ci;


ALTER DATABASE p2p_dev DEFAULT  CHARACTER SET utf8 COLLATE utf8_general_ci;


DROP PROCEDURE IF EXISTS `proc_reset_table_character`;
create PROCEDURE proc_reset_table_character ()
BEGIN

    DECLARE cnt VARCHAR(100);
    DECLARE i int;
    DECLARE t_cnt int;

    set i = 0;
    set @allsql='';

    set t_cnt = 0;
    select count(1) into t_cnt from information_schema.`TABLES` where TABLE_SCHEMA = 'p2p_dev' and TABLE_TYPE = 'BASE TABLE' and TABLE_COLLATION <> 'utf8_general_ci';



    while i < t_cnt do
        select table_name into @cnt from information_schema.`TABLES` where TABLE_SCHEMA = 'p2p_dev' and TABLE_TYPE = 'BASE TABLE' and TABLE_COLLATION <> 'utf8_general_ci'  limit i,1;
        -- select @cnt; -- mysql的打印语句
        -- alter table @cnt convert to character set utf8; -- 这一句报错，必须动态拼接才行

        set @sql = concat("alter table ", @cnt, " convert to character set utf8 COLLATE utf8_general_ci;");  -- 拼接，注意语句中的空格

        prepare stmt from @sql;  -- 预处理
                execute stmt;  -- 执行
        deallocate prepare stmt;  -- 释放


        set @allsql =concat(@allsql,CHAR(10),@sql);

        set i = i + 1;
    end while;
    -- SELECT @allsql;
END;




alter table t_bid_risk convert to character set utf8 COLLATE utf8_general_ci;
alter table t_bid_user_risk convert to character set utf8 COLLATE utf8_general_ci;
alter table t_bid_user_risk_log convert to character set utf8 COLLATE utf8_general_ci;
alter table t_supervisor_citys convert to character set utf8 COLLATE utf8_general_ci;
alter table t_user_city convert to character set utf8 COLLATE utf8_general_ci;
alter table t_user_cps_profit convert to character set utf8 COLLATE utf8_general_ci;
alter table t_user_risk convert to character set utf8 COLLATE utf8_general_ci;

