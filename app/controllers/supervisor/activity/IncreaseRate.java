package controllers.supervisor.activity;

import business.Product;
import business.Supervisor;
import business.User;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.v_bid_repayment;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.lang.StringUtils;
import services.activity.ActivityIncreaseRateService;
import utils.*;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:39
 * @Description:
 */
public class IncreaseRate extends SupervisorController {


    /**
     * 活动列表
     */
    public static void activityList(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        PageBean<Map<String, Object>> pageBean = ActivityIncreaseRateService.getActivityList(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime, currPageStr, pageSizeStr);


        render(pageBean, type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime);//查询参数
    }


    public static void increaseTotalList(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        boolean isExport = false;
        if (params.get("isExport") != null) {
            int exportValue = Integer.parseInt(params.get("isExport").toString());
            isExport = exportValue == 1;
        }

        PageBean<Map<String, Object>> pageBean = ActivityIncreaseRateService.getIncreaseTotal(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime, isExport, currPageStr, pageSizeStr);

        String totalInvest = ActivityIncreaseRateService.getInvestTotal(type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime);


        if (isExport) {

            List<Map<String, Object>> list = pageBean.page;

            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
            jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
            JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

//            for (Object obj : arrList) {
//                JSONObject bid = (JSONObject) obj;
//
//                String showPeriod = "";
//                int period = bid.getInt("period");
//                int period_unit = bid.getInt("period_unit");
//                if (period_unit == -1) {
//                    showPeriod = period + "[年 ]";
//                } else if (period_unit == 1) {
//                    showPeriod = period + "[日]";
//                } else {
//                    showPeriod = period + "[月]";
//                }
//
//                String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
//                String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
//
//                bid.put("period", showPeriod);
//                bid.put("small_image_filename", productName);
//                bid.put("credit_level_image_filename", creditLevel);
//                bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
//            }

            File file = ExcelUtils.export("活动加息汇总列表",
                    arrList,
                    new String[]{
                            "加息类型", "加息利率", "开始时间", "结束时间", "名称", "总投资金额[￥]", "加息总利息",
                            "已付利息", "待付利息"},
                    new String[]{"typename", "rate", "start_time","stop_time", "name", "amount", "total_interest",
                            "received_interest", "no_received_interest"});

            renderBinary(file, "活动加息汇总列表.xls");
        }


        render(pageBean,totalInvest, type, state, name, startBeginTime, startEndTime, stopBeginTime, stopEndTime);

    }

    public static void increaseDetailList(String userName, int orderState, int type, int state, String name, String startTime, String stopTime) throws Exception {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        String currPageStr = params.get("currPage");
        String pageSizeStr = params.get("pageSize");

        boolean isExport = false;
        if (params.get("isExport") != null) {
            int exportValue = Integer.parseInt(params.get("isExport").toString());
            isExport = exportValue == 1;
        }


        PageBean<Map<String, Object>> pageBean = ActivityIncreaseRateService.getIncreasDetails(userName, orderState, type, state, name, startTime, stopTime, isExport, currPageStr, pageSizeStr);


        if (isExport) {

            List<Map<String, Object>> list = pageBean.page;

            JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.registerJsonValueProcessor(Date.class, new JsonDateValueProcessor("yyyy-MM-dd"));
            jsonConfig.registerJsonValueProcessor(Double.class, new JsonDoubleValueProcessor("##,##0.00"));
            JSONArray arrList = JSONArray.fromObject(list, jsonConfig);

//            for (Object obj : arrList) {
//                JSONObject bid = (JSONObject) obj;
//
//                String showPeriod = "";
//                int period = bid.getInt("period");
//                int period_unit = bid.getInt("period_unit");
//                if (period_unit == -1) {
//                    showPeriod = period + "[年 ]";
//                } else if (period_unit == 1) {
//                    showPeriod = period + "[日]";
//                } else {
//                    showPeriod = period + "[月]";
//                }
//
//                String productName = Product.queryProductNameByImage(bid.getString("small_image_filename"));
//                String creditLevel = User.queryCreditLevelByImage(bid.getString("credit_level_image_filename"));
//
//                bid.put("period", showPeriod);
//                bid.put("small_image_filename", productName);
//                bid.put("credit_level_image_filename", creditLevel);
//                bid.put("apr", String.format("%.1f", bid.getDouble("apr")));
//            }

            File file = ExcelUtils.export("活动加息明细列表",
                    arrList,
                    new String[]{
                            "加息编号", "加息账单编号", "关联投资账单编号", "应付加息金额", "账单期数", "投资人", "投资人手机号",
                            "标的名称", "关联借款标编号","借款人真实姓名","应付加息时间","实际加息时间","账单状态","加息类型","加息利率","名称"},
                    new String[]{"activity_id", "bill_id", "invest_id","increase_interest", "period", "user_name", "mobile",
                            "title", "bid","reality_name", "receive_time", "real_receive_time","bill_state","typename","activity_rate","activity_name"});

            renderBinary(file, "活动加息明细列表.xls");
        }


        render(pageBean, userName, orderState, type, state, name, startTime, stopTime);
    }


    /**
     * 添加加息活动
     *
     * @param type
     * @param name
     * @param rate
     * @param startTime
     * @param stopTime
     */
    public static void addActivity(int type, String name, BigDecimal rate, Date startTime, Date stopTime) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        if (startTime == null || stopTime == null) {
            error.code = -1;
            error.msg = "活动开始或结束时间为空";
        }

        if (startTime.getTime() >= stopTime.getTime()) {
            error.code = -1;
            error.msg = "活动开始必须大于结束时间";
        }
        if (type == 0) {
            error.code = -1;
            error.msg = "活动类型没有指定";
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            error.code = -1;
            error.msg = "活动利率要大于0";
        }
        if (StringUtils.isEmpty(name)) {
            error.code = -1;
            error.msg = "活动名称不能为空";
        }

        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }

        try {
            ActivityIncreaseRateService.addActivity(type, name, rate, startTime, stopTime);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }

    /**
     * 活动详情
     */
    public static void getActivityDetail(int activityId) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }


        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        try {
            Map<String, Object> result = ActivityIncreaseRateService.getActivityDetail(activityId);

            json.put("result", result);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);

    }

    /**
     * 修改活动
     */
    public static void editActivity(Long detailId, String name, BigDecimal rate, Date startTime, Date stopTime) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();


        if (startTime == null || stopTime == null) {
            error.code = -1;
            error.msg = "活动开始或结束时间为空";
        }

        if (startTime.getTime() >= stopTime.getTime()) {
            error.code = -1;
            error.msg = "活动开始必须大于结束时间";
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            error.code = -1;
            error.msg = "活动利率要大于0";
        }
        if (StringUtils.isEmpty(name)) {
            error.code = -1;
            error.msg = "活动名称不能为空";
        }

        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }

        try {
            ActivityIncreaseRateService.editActivity(detailId, name, rate, startTime, stopTime);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);

    }

    /**
     * 初审活动
     */
    public static void firstAuditActivity(Long detailId, int ispass, String remark) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        try {
            ActivityIncreaseRateService.firstAuditActivity(detailId, ispass, remark);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }


    /**
     * 复审活动
     */
    public static void lastAuditActivity(Long detailId, int ispass, String remark) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }

        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        try {
            ActivityIncreaseRateService.lastAuditActivity(detailId, ispass, remark);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }


    /**
     * 关闭活动
     */
    public static void closeActivity(Long activityId) {

        Supervisor supervisor = Supervisor.currSupervisor();

        if (null == supervisor) {
            redirect(Constants.HTTP_PATH + "/supervisor");
        }


        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();

        try {
            ActivityIncreaseRateService.closeActivity(activityId);
        } catch (Exception e) {
            error.code = -1;
            error.msg = e.getMessage();
        }

        json.put("error", error);

        renderJSON(json);
    }
}
