package services.channel;

import business.Supervisor;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import dao.channel.ChannelDao;
import models.channel.t_appstore;
import net.sf.json.JSONObject;
import utils.ErrorInfo;
import utils.PageBean;

import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/28 15:04
 * @Description:
 */
public class ChannelService {

    /**
     * 渠道列表
     *
     * @param state
     * @param num
     * @param type
     * @param name
     * @param subname
     * @param startTime
     * @param stopTime
     * @throws Exception
     */
    public static PageBean<Map<String, Object>> channelList(int state, String num, String type, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {
        return ChannelDao.channelList(state, num, type, name, subname, startTime, stopTime, currPageStr, pageSizeStr);
    }


    /**
     * 渠道汇总
     *
     * @param num
     * @param type
     * @param name
     * @param subname
     * @param startTime
     * @param stopTime
     * @throws Exception
     */
    public static PageBean<Map<String, Object>> channelTotalList(String num, String type, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {
        return ChannelDao.channelTotalList(num, type, name, subname, startTime, stopTime,currPageStr,pageSizeStr);

    }


    /**
     * 渠道会员列表
     *
     * @param userName
     * @param isValidCard
     * @param isBindCard
     * @param num
     * @param name
     * @param subname
     * @param startTime
     * @param stopTime
     * @throws Exception
     */
    public static PageBean<Map<String, Object>> channelUserList(String userName, int isValidCard, int isBindCard, String num, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {

        return ChannelDao.channelUserList(userName, isValidCard, isBindCard, num, name, subname, startTime, stopTime, currPageStr, pageSizeStr);

    }

    /**
     * 渠道会员投资列表
     *
     * @param userName
     * @param isFirst
     * @param num
     * @param name
     * @param subname
     * @param startTime
     * @param stopTime
     * @throws Exception
     */
    public static PageBean<Map<String, Object>> channelUserInvestList(String userName, int isFirst, String num, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {

        return ChannelDao.channelUserInvestList(userName, isFirst, num, name, subname, startTime, stopTime, currPageStr, pageSizeStr);
    }


    /**
     * 添加渠道
     *
     * @param type
     * @param num
     * @param name
     * @param subname
     */
    public static void addChannel(String type, String num, String name, String subname) {


        List<t_appstore> appstores = t_appstore.find("num = ? ",num).fetch();
        if(appstores!=null && appstores.size()>0) {
            throw new IllegalArgumentException("已存在此渠道号的渠道。");
        }

        ChannelDao.addChannel(num, name, subname,type);
    }


    /**
     * 查看渠道
     *
     * @param id
     */
    public static t_appstore getChannelById(Long id) {

        return t_appstore.findById(id);
    }


    /**
     * 修改渠道
     *
     * @param id
     */
    public static void editChannel(Long id, String name, String subname, String type) {


        t_appstore channel = t_appstore.findById(id);

        if (channel == null) {
            throw new IllegalArgumentException("没有对应的渠道");
        }

        channel.name = name;
        channel.subname = subname;
        channel.type = type;

        channel.save();
    }


    /**
     * 修改渠道
     *
     * @param id
     */
    public static void updateChannelState(Long id, int state) {
        t_appstore channel = t_appstore.findById(id);

        if (channel == null) {
            throw new IllegalArgumentException("没有对应的渠道");
        }

        channel.state = state;
        channel.save();

    }


}
