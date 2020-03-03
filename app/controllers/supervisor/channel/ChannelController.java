package controllers.supervisor.channel;

import business.Supervisor;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import services.channel.ChannelService;
import utils.ErrorInfo;
import utils.PageBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/28 14:06
 * @Description:
 */
public class ChannelController extends SupervisorController {

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
    public static void channelList(int state, String num, String type, String name, String subname, String startTime, String stopTime) throws Exception {
        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        PageBean<Map<String, Object>> pageBean = ChannelService.channelList(state,num,type,name,subname,startTime,stopTime,currPageStr,pageSizeStr);


        render(pageBean, state, num, type, name, subname, startTime, stopTime);
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
    public static void channelTotalList(String num, String type, String name, String subname, String startTime, String stopTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        PageBean<Map<String, Object>> pageBean = ChannelService.channelTotalList(num, type, name, subname, startTime, stopTime,currPageStr,pageSizeStr);


        render(pageBean, num, type, name, subname, startTime, stopTime);
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
    public static void channelUserList(String userName, int isValidCard, int isBindCard, String num, String name, String subname, String startTime, String stopTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        PageBean<Map<String, Object>> pageBean = ChannelService.channelUserList(userName,isValidCard,isBindCard,num,name,subname,startTime,stopTime,currPageStr,pageSizeStr);


        render(pageBean, userName, isValidCard, isBindCard, num, name, subname, startTime, stopTime);
    }

    /**
     * 渠道会员投资列表
     * @param userName
     * @param isFirst
     * @param num
     * @param name
     * @param subname
     * @param startTime
     * @param stopTime
     * @throws Exception
     */
    public static void channelUserInvestList(String userName, int isFirst, String num, String name, String subname, String startTime, String stopTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        PageBean<Map<String, Object>> pageBean = ChannelService.channelUserInvestList(userName,isFirst,num,name,subname,startTime,stopTime,currPageStr,pageSizeStr);


        render(pageBean, userName, isFirst, num, name, subname, startTime, stopTime);
    }


    /**
     * 添加渠道
     * @param type
     * @param num
     * @param name
     * @param subname
     */
    public static void addChannel(String type,String num, String name, String subname) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();


        try {
            ChannelService.addChannel(type, num, name, subname);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }


    /**
     * 查看渠道
     * @param id
     */
    public static void getChannelById(Long id) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();


        try {
            ChannelService.getChannelById(id);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }


    /**
     * 修改渠道
     * @param id
     */
    public static void editChannel(Long id, String name, String subname, String type) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();


        try {
            ChannelService.editChannel(id, name, subname, type);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }




    /**
     * 修改渠道
     * @param id
     */
    public static void updateChannelState(Long id,int state) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();


        try {
            ChannelService.updateChannelState(id, state);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }


}
