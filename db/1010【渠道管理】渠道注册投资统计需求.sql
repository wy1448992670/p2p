/*==============================================================*/
/* Table: t_channels                                            */
/*==============================================================*/
create table t_appstore
(
   id                   int not null auto_increment,
   type                 varchar(50) not null comment '类型',
   num                  varchar(50) not null comment '渠道号',
   name                 varchar(100) not null comment '名称',
   subname              varchar(50) comment '二级名称',
   create_time          timestamp not null default CURRENT_TIMESTAMP comment '创建时间',
   state                int not null default 1 comment '状态 1正常 -1关闭',
   primary key (id)
);

#ALTER TABLE `t_invests` ADD COLUMN `store_id`  int(10) NULL DEFAULT NULL AFTER `client`;

ALTER TABLE `t_users` ADD COLUMN `store_id`  int(10) NULL DEFAULT NULL AFTER `user_type`;

DROP FUNCTION IF EXISTS func_inc_rownum ;

CREATE  FUNCTION `func_inc_rownum`(userId LONG,isreset int) RETURNS int

     begin

			IF isreset = 1  then

				SET @rownumber := 0;
				set @user_id= 0;
				return @rownumber;

			ELSE
				IF @user_id=userId THEN
					SET @rownumber := IFNULL(@rownumber,0) + 1;
				ELSE
					SET @rownumber := 1;
				END IF;

				set @user_id=userId;
				return @rownumber;
			END IF;

     end;


INSERT INTO `t_rights` (`id`, `type_id`, `name`, `code`, `description`) VALUES ('226', '7', '渠道管理', NULL, NULL);
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13120', '226', 'supervisor.channel.ChannelController.channelList', '渠道列表');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13121', '226', 'supervisor.channel.ChannelController.channelTotalList', '渠道汇总');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13122', '226', 'supervisor.channel.ChannelController.channelUserList', '渠道注册会员');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13123', '226', 'supervisor.channel.ChannelController.channelUserInvestList', '渠道会员投资');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13124', '226', 'supervisor.channel.ChannelController.addChannel', '渠道添加');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13125', '226', 'supervisor.channel.ChannelController.editChannel', '渠道修改');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13126', '226', 'supervisor.channel.ChannelController.updateChannelState', '渠道状态变更');
INSERT INTO `t_right_actions` (`id`, `right_id`, `action`, `description`) VALUES ('13127', '226', 'supervisor.channel.ChannelController.getChannelById', '渠道详情');


