package dao.activity;

import models.activity.t_activity_increase_rate;
import org.jsoup.helper.StringUtil;
import utils.ErrorInfo;
import utils.JPAUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/17 14:27
 * @Description:
 */
public class ActivityIncreaseRateDao {


    public static t_activity_increase_rate insert(int type, String name, Date startTime, Date stopTime) {
        t_activity_increase_rate increase_rate = new t_activity_increase_rate();

        increase_rate.type = type;
        increase_rate.name = name;
        increase_rate.start_time = startTime;
        increase_rate.stop_time = stopTime;
        return increase_rate.save();
    }


    public static boolean getActivityByType(int type) {

        //=======================================table=======================================
        String table = " from t_activity_increase_rate a " +
                " INNER JOIN (SELECT activity_id from t_activity_increase_rate_detail WHERE state>-3 and stop_time > NOW()) b " +
                " on a.id = b.activity_id where true ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = " and a.type = ? ";
        paramsList.add(type);


        String cntSql = " select count(1) as count " + table + condition;

        BigInteger count = (BigInteger) JPAUtil.createNativeQuery(cntSql, paramsList.toArray()).getSingleResult();

        if (count.intValue() > 0) {
            return true;
        }
        return false;

    }

    /**
     * 获取当前可用的活动
     *
     * @return
     */
    public static List<Map<String, Object>> getEnableActivitysForCurrent() {
        //=======================================columns=======================================
        String columns = " b.id,a.type,b.rate ";
        //=======================================table=======================================
        String table = " from t_activity_increase_rate a " +
                " INNER JOIN t_activity_increase_rate_detail b on b.activity_id =a.id " +
                " where b.state = 2 and b.start_time <=now() and NOW() < b.stop_time";
        //=======================================condition=======================================


        return JPAUtil.getList("select " + columns + table);
    }

    public static List<Map<String, Object>> getActivityDetailLogs(int activityId) {

        //=======================================columns=======================================
        String columns = " s.reality_name,s.`name`,DATE_FORMAT(c.time,'%Y-%m-%d %H:%i:%S') time,IFNULL(c.result,'') result,c.description_title,c.description "/*,m.enum_name*/;
        //=======================================table=======================================
        String table = " from t_activity_increase_rate a " +
                " LEFT JOIN t_activity_increase_rate_detail b on b.activity_id = a.id " +
                " LEFT JOIN t_log c on c.relation_id = b.id and c.relation_type = 3 " +
                " LEFT JOIN t_supervisors s on s.id = c.user_id " +
//                " LEFT JOIN t_enum_map m on b.state = m.enum_code and m.enum_type = 3 " +
                " where true ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (activityId > 0) {
            condition += " and a.id =? ";
            paramsList.add(activityId);
        }

        return JPAUtil.getList("select " + columns + table + condition, paramsList.toArray());

    }

    public static List<Map<String, Object>> getActivityDetailById(int activityId) {
        //=======================================columns=======================================
        String columns = " a.id, a.type, DATE_FORMAT(a.create_time,'%Y-%m-%d %H:%i:%S') create_time,a.`name`,DATE_FORMAT(a.start_time,'%Y-%m-%d %H:%i:%S') start_time,DATE_FORMAT(a.stop_time,'%Y-%m-%d %H:%i:%S') stop_time,b.rate,b.state,case when NOW() > a.stop_time then '已关闭' else c.enum_name end statename ";
        //=======================================table=======================================
        String table = " from t_activity_increase_rate a " +
                " LEFT JOIN " +
                " (" +
                " SELECT * from " +
                " t_activity_increase_rate_detail where id in(SELECT max(id) from t_activity_increase_rate_detail GROUP BY activity_id)" +
                " )" +
                " b on a.id = b.activity_id " +
                " LEFT JOIN t_enum_map c on b.state = c.enum_code and c.enum_type = 3 " +
                " where true ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (activityId > 0) {
            condition += " and a.id =? ";
            paramsList.add(activityId);
        }

        return JPAUtil.getList("select " + columns + table + condition, paramsList.toArray());

    }


    public static PageBean<Map<String, Object>> getActivityList(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime, String currPageStr, String pageSizeStr) throws Exception {
        //=======================================page=======================================
        int currPage = 1;
        int pageSize = 10;
        if (!StringUtil.isBlank(currPageStr) && StringUtil.isNumeric(currPageStr)) {
            currPage = Integer.parseInt(currPageStr) > 0 ? Integer.parseInt(currPageStr) : currPage;
        }
        if (!StringUtil.isBlank(pageSizeStr) && StringUtil.isNumeric(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr) > 0 ? Integer.parseInt(pageSizeStr) : pageSize;
        }
        //=======================================columns=======================================
        String columns = " a.id,case a.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename," +
                " a.create_time,a.`name`,a.start_time,a.stop_time,b.id d_id,b.rate,b.state,case when NOW() > a.stop_time then '已关闭' else c.enum_name end statename ";
        //=======================================table=======================================
        String table = " from t_activity_increase_rate a " +
                " LEFT JOIN " +
                " (" +
                " SELECT * from " +
                " t_activity_increase_rate_detail where id in(SELECT max(id) from t_activity_increase_rate_detail where visibile=1 GROUP BY activity_id)" +
                " )" +
                " b on a.id = b.activity_id " +
                " LEFT JOIN t_enum_map c on b.state = c.enum_code and c.enum_type = 3 " +
                " where true ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (type > 0) {
            condition += " and a.type =? ";
            paramsList.add(type);
        }

        if (state > -10) {
            if (state == -3) {
                condition += " and (b.state =? or b.state =-4 or a.stop_time < NOW()) ";
            } else {
                condition += " and b.state =? and a.stop_time > NOW()";
            }
            paramsList.add(state);
        }

        if (!StringUtil.isBlank(name)) {
            condition += " and  a.`name` LIKE ? ";
            paramsList.add("%" + name + "%");
        }


        if (!StringUtil.isBlank(startBeginTime)) {
            condition += " and  a.start_time >= ? ";
            paramsList.add(startBeginTime);
        }

        if (!StringUtil.isBlank(startEndTime)) {
            condition += " and  a.start_time <= ? ";
            paramsList.add(startEndTime);
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            condition += " and  a.stop_time >= ? ";
            paramsList.add(stopBeginTime);
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            condition += " and  a.stop_time <= ? ";
            paramsList.add(stopEndTime);
        }

        //=======================================order_by=======================================
        String order_by_columns = "", asc_desc = "";
        String order_by = "";
        if (StringUtil.isBlank(order_by_columns)) {
            order_by_columns = "a.id";
        }
        if (StringUtil.isBlank(asc_desc)) {
            asc_desc = "desc";
        }
        order_by = " order by " + order_by_columns + " " + asc_desc;

        return PageBeanForPlayJPA.getPageBeanMapBySQL(columns, table + condition, order_by, currPage, pageSize, paramsList.toArray());
    }


    public  static String getInvestTotal(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime) throws Exception {

        //=======================================columns=======================================
        String columns = " * ";
        //=======================================table=======================================

        String innerCondition = "";
        List<Object> paramsList = new ArrayList<Object>();

        if (state > -10) {
            if (state == -3) {
                innerCondition += " and (b.state =? or b.state =-4 or b.stop_time < NOW()) ";
            } else {
                innerCondition += " and b.state =? and a.stop_time > NOW() ";
            }
        }

        if (type > 0) {
            innerCondition += " and a.type =? ";
        }

        if (!StringUtil.isBlank(name)) {
            innerCondition += " and  a.`name` LIKE ? ";
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            innerCondition += " and  b.start_time >= ? ";
        }

        if (!StringUtil.isBlank(startEndTime)) {
            innerCondition += " and  b.start_time <= ? ";
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            innerCondition += " and  b.stop_time >= ? ";
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            innerCondition += " and  b.stop_time <= ? ";
        }

        String table = " from (SELECT t.id, t.invest_id, t.type, case t.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename,t.name,DATE_FORMAT(t.start_time,'%Y-%m-%d %H:%i:%s') start_time,DATE_FORMAT(t.stop_time,'%Y-%m-%d %H:%i:%s') stop_time,max(t.rate) rate,IFNULL(sum(t.amount),0) amount,IFNULL(sum(t.total_interest),0) total_interest,IFNULL(sum(t.received_interest),0) received_interest,sum(IFNULL(t.total_interest,0)-IFNULL(t.received_interest,0)) no_received_interest from  " +
                "(" +
                " SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a" +
                " LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id" +
                " LEFT JOIN t_invests c on c.increase_activity_id1 = b.id" +
                " LEFT JOIN ( " +
                " SELECT aa.invest_id,sum(aa.receive_increase_interest1) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.receive_increase_interest1 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id" +
                " ) d on d.invest_id = c.id" +
                " where true ";

        table = table + innerCondition;

        if (state > -10) {
            paramsList.add(state);
        }

        if (type > 0) {
            paramsList.add(type);
        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            paramsList.add(startBeginTime);
        }

        if (!StringUtil.isBlank(startEndTime)) {
            paramsList.add(startEndTime);
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            paramsList.add(stopBeginTime);
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            paramsList.add(stopEndTime);
        }

        table = table +
                " UNION " +
                " SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a" +
                " LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id" +
                " LEFT JOIN t_invests c on c.increase_activity_id2 = b.id" +
                " LEFT JOIN ( " +
                " SELECT aa.invest_id,sum(aa.receive_increase_interest2) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.receive_increase_interest2 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id" +
                " ) d on d.invest_id = c.id" +
                " where true ";

        table = table + innerCondition;

        if (state > -10) {
            paramsList.add(state);
        }

        if (type > 0) {
            paramsList.add(type);
        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            paramsList.add(startBeginTime);
        }

        if (!StringUtil.isBlank(startEndTime)) {
            paramsList.add(startEndTime);
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            paramsList.add(stopBeginTime);
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            paramsList.add(stopEndTime);
        }

        table = table +
                ")t GROUP BY t.id,t.invest_id HAVING t.invest_id is not NULL ) tt GROUP BY tt.invest_id )ttt  ";
        //=======================================condition=======================================

        ErrorInfo errorInfo=new ErrorInfo();

        List<Map<String, Object>> totalList = JPAUtil.getList(errorInfo, " SELECT IFNULL(sum(amount),0) amount from (SELECT tt.invest_id,tt.amount " + table, paramsList.toArray());
        if(errorInfo.code<0){
            throw new Exception(errorInfo.msg);
        }

        Map<String, Object> totalMap = totalList.get(0);

        return totalMap.get("amount").toString();
    }

    public static PageBean<Map<String, Object>> getIncreaseTotal(int type, int state, String name, String startBeginTime, String startEndTime, String stopBeginTime, String stopEndTime, Boolean isExport, String currPageStr, String pageSizeStr) throws Exception {
        //=======================================page=======================================
        int currPage = 1;
        int pageSize = 10;
        if (!StringUtil.isBlank(currPageStr) && StringUtil.isNumeric(currPageStr)) {
            currPage = Integer.parseInt(currPageStr) > 0 ? Integer.parseInt(currPageStr) : currPage;
        }
        if (!StringUtil.isBlank(pageSizeStr) && StringUtil.isNumeric(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr) > 0 ? Integer.parseInt(pageSizeStr) : pageSize;
        }

        /*
        SELECT *
        from (SELECT t.id, t.type, case t.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename,t.name,t.start_time,t.stop_time,max(t.rate) rate,IFNULL(sum(t.amount),0) amount,IFNULL(sum(t.total_interest),0) total_interest,IFNULL(sum(t.received_interest),0) received_interest,sum(IFNULL(t.total_interest,0)-IFNULL(t.received_interest,0)) no_received_interest from
            (
                SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a
                LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id
                LEFT JOIN t_invests c on c.increase_activity_id1 = b.id
                LEFT JOIN (
                SELECT aa.invest_id,sum(aa.receive_increase_interest1) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.real_increase_interest1 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id
                ) d on d.invest_id = c.id
                where true and b.state = -3 and b.stop_time<NOW()
            UNION
            SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a
                LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id
                LEFT JOIN t_invests c on c.increase_activity_id2 = b.id
                LEFT JOIN (
                SELECT aa.invest_id,sum(aa.receive_increase_interest2) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.real_increase_interest2 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id
                ) d on d.invest_id = c.id
                where true
            )t GROUP BY t.id
        ) tt
        ORDER BY tt.id DESC
        */


        //=======================================columns=======================================
        String columns = " * ";

        String totalColumns = " IFNULL(sum(total_interest),0) total_received_interest  ";//IFNULL(sum(amount),0) total_invest_money
        //=======================================table=======================================

        String innerCondition = "";
        List<Object> paramsList = new ArrayList<Object>();

        if (state > -10) {
            if (state == -3) {
                innerCondition += " and (b.state =? or b.state =-4 or b.stop_time < NOW()) ";
            } else {
                innerCondition += " and b.state =? and a.stop_time > NOW() ";
            }
        }

        if (type > 0) {
            innerCondition += " and a.type =? ";
        }

        if (!StringUtil.isBlank(name)) {
            innerCondition += " and  a.`name` LIKE ? ";
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            innerCondition += " and  b.start_time >= ? ";
        }

        if (!StringUtil.isBlank(startEndTime)) {
            innerCondition += " and  b.start_time <= ? ";
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            innerCondition += " and  b.stop_time >= ? ";
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            innerCondition += " and  b.stop_time <= ? ";
        }

        String table = " from (SELECT t.id, t.type, case t.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename,t.name,DATE_FORMAT(t.start_time,'%Y-%m-%d %H:%i:%s') start_time,DATE_FORMAT(t.stop_time,'%Y-%m-%d %H:%i:%s') stop_time,max(t.rate) rate,IFNULL(sum(t.amount),0) amount,IFNULL(sum(t.total_interest),0) total_interest,IFNULL(sum(t.received_interest),0) received_interest,sum(IFNULL(t.total_interest,0)-IFNULL(t.received_interest,0)) no_received_interest from  " +
                "(" +
                " SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a" +
                " LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id" +
                " LEFT JOIN t_invests c on c.increase_activity_id1 = b.id" +
                " LEFT JOIN ( " +
                " SELECT aa.invest_id,sum(aa.receive_increase_interest1) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.receive_increase_interest1 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id" +
                " ) d on d.invest_id = c.id" +
                " where true ";

        table = table + innerCondition;

        if (state > -10) {
            paramsList.add(state);
        }

        if (type > 0) {
            paramsList.add(type);
        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            paramsList.add(startBeginTime);
        }

        if (!StringUtil.isBlank(startEndTime)) {
            paramsList.add(startEndTime);
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            paramsList.add(stopBeginTime);
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            paramsList.add(stopEndTime);
        }

        table = table +
                " UNION " +
                " SELECT a.id,c.id invest_id,a.type,a.`name`,a.start_time,a.stop_time,b.rate,c.amount,d.total_interest,d.received_interest from t_activity_increase_rate a" +
                " LEFT JOIN t_activity_increase_rate_detail b on a.id = b.activity_id" +
                " LEFT JOIN t_invests c on c.increase_activity_id2 = b.id" +
                " LEFT JOIN ( " +
                " SELECT aa.invest_id,sum(aa.receive_increase_interest2) total_interest,sum(CASE when aa.`status` IN(0,-3,-4) then aa.receive_increase_interest2 ELSE 0 end) received_interest  from t_bill_invests aa GROUP BY aa.invest_id" +
                " ) d on d.invest_id = c.id" +
                " where true ";

        table = table + innerCondition;

        if (state > -10) {
            paramsList.add(state);
        }

        if (type > 0) {
            paramsList.add(type);
        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startBeginTime)) {
            paramsList.add(startBeginTime);
        }

        if (!StringUtil.isBlank(startEndTime)) {
            paramsList.add(startEndTime);
        }

        if (!StringUtil.isBlank(stopBeginTime)) {
            paramsList.add(stopBeginTime);
        }

        if (!StringUtil.isBlank(stopEndTime)) {
            paramsList.add(stopEndTime);
        }

        table = table +
                ")t GROUP BY t.id ) tt where true ";
        //=======================================condition=======================================


        //=======================================order_by=======================================
        String order_by_columns = "";
        String order_by = "";
        if (StringUtil.isBlank(order_by_columns)) {
            order_by_columns = " tt.id DESC ";
        }
        order_by = " order by " + order_by_columns;


        return PageBeanForPlayJPA.getPageBeanMapBySQLExport2(columns, totalColumns, table, order_by, isExport, currPage, pageSize, paramsList.toArray());
    }


    public static PageBean<Map<String, Object>> getIncreaseDetails(String userName, int orderState, int type, int state, String name, String startTime, String stopTime, Boolean isExport, String currPageStr, String pageSizeStr) throws Exception {
        /*

        SELECT d.id bill_id,c.id invest_id,d.receive_increase_interest1,CONCAT(d.periods,'/',bid.period) period,d.receive_time,d.real_receive_time,d.`status` bill_state,
            u.`name` user_name,u.reality_name,u.mobile,a.`name` activity_name,
            a.type,case a.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename, b.rate,bid.id bid,bid.title

         from t_activity_increase_rate a
        INNER JOIN t_activity_increase_rate_detail b on a.id = b.activity_id
        INNER JOIN t_invests c on c.increase_activity_id1 = b.id
        INNER JOIN t_bids bid on bid.id = c.bid_id
        INNER JOIN  t_bill_invests  d on d.invest_id = c.id

        INNER JOIN t_users u on u.id = c.user_id

        UNION

        SELECT d.id bill_id,c.id invest_id,d.receive_increase_interest1,CONCAT(d.periods,'/',bid.period) period,d.receive_time,d.real_receive_time,d.`status` bill_state,
            u.`name` user_name,u.reality_name,u.mobile,a.`name` activity_name,
            a.type,case a.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename, b.rate,bid.id bid,bid.title

        SELECT d.id,d.receive_increase_interest2,CONCAT(d.periods,'/',bid.period) period from t_activity_increase_rate a
        INNER JOIN t_activity_increase_rate_detail b on a.id = b.activity_id
        INNER JOIN t_invests c on c.increase_activity_id2 = b.id
        INNER JOIN t_bids bid on bid.id = c.bid_id
        INNER JOIN  t_bill_invests  d on d.invest_id = c.id

        INNER JOIN t_users u on u.id = c.user_id

        */

        //=======================================page=======================================
        int currPage = 1;
        int pageSize = 10;
        if (!StringUtil.isBlank(currPageStr) && StringUtil.isNumeric(currPageStr)) {
            currPage = Integer.parseInt(currPageStr) > 0 ? Integer.parseInt(currPageStr) : currPage;
        }
        if (!StringUtil.isBlank(pageSizeStr) && StringUtil.isNumeric(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr) > 0 ? Integer.parseInt(pageSizeStr) : pageSize;
        }
        //=======================================columns=======================================
        String columns = " * ";

        String totalColumns = " IFNULL(sum(increase_interest), 0) total_increase_interest ";
        //=======================================table=======================================


        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (type > 0) {
            condition += " and a.type =? ";
        }

//        if (state > -10) {
//            if (state == -3) {
//                condition += " and (b.state =? or b.state =-4 or a.stop_time < NOW()) ";
//            } else {
//                condition += " and b.state =? and a.stop_time > NOW() ";
//            }
//        }

        if (!StringUtil.isBlank(name)) {
            condition += " and  a.`name` LIKE ? ";
        }

        if (!StringUtil.isBlank(startTime)) {
            condition += " and d.receive_time >= ? ";
        }

        if (!StringUtil.isBlank(stopTime)) {
            condition += " and d.receive_time <= ? ";
        }

        if (!StringUtil.isBlank(userName)) {
            condition += " and u.`name` like ? or u.reality_name like ? or u.mobile like ? ";
        }

        if (orderState > 0) {
            if (orderState == 1) {
                condition += " and d.`status` in (0,-3,-4) ";
            } else {
                condition += " and d.`status` not in (0,-3,-4) ";
            }
        }


        String table = " from  " +
                "( " +
                "  SELECT a.id activity_id,b.id activity_detail_id,c.id invest_id,d.id bill_id,d.receive_increase_interest1 increase_interest,case when repayment_type_id=3 then '1/1' else CONCAT(d.periods, '/', bid.period) end period,DATE_FORMAT(d.receive_time,'%Y-%m-%d %h:%i:%s') receive_time,DATE_FORMAT(d.real_receive_time,'%Y-%m-%d %h:%i:%s') real_receive_time, " +
                "  case when d.`status` in(0,-3,-4) then '已付' else '未付' end bill_state, " +
                "  u.`name` user_name,u.reality_name,u.mobile,a.`name` activity_name, " +
                " a.type,case a.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename, b.rate activity_rate,bid.id bid,bid.title " +
                "  from t_activity_increase_rate a " +
                " INNER JOIN t_activity_increase_rate_detail b on a.id = b.activity_id " +
                " INNER JOIN t_invests c on c.increase_activity_id1 = b.id " +
                " INNER JOIN t_bids bid on bid.id = c.bid_id " +
                " INNER JOIN  t_bill_invests  d on d.invest_id = c.id " +
                " INNER JOIN t_users u on u.id = c.user_id where true ";


        if (type > 0) {
            paramsList.add(type);
        }

//        if (state > -10) {
//            paramsList.add(state);
//        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startTime)) {
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            paramsList.add(stopTime);
        }

        if (!StringUtil.isBlank(userName)) {
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
        }


        table = table + condition;

        table = table + " UNION " +
                " SELECT a.id activity_id,b.id activity_detail_id,c.id invest_id,d.id bill_id,d.receive_increase_interest2 increase_interest,case when repayment_type_id=3 then '1/1' else CONCAT(d.periods, '/', bid.period) end period,d.receive_time,d.real_receive_time," +
                " case when d.`status` in(0,-3,-4) then '已付' else '未付' end bill_state, " +
                "   u.`name` user_name,u.reality_name,u.mobile,a.`name` activity_name, " +
                " a.type,case a.type when 1 then '全场加息' when 2 then '首投加息' when 3 then '尾投加息' end typename, b.rate,bid.id bid,bid.title " +
                "  from t_activity_increase_rate a " +
                " INNER JOIN t_activity_increase_rate_detail b on a.id = b.activity_id " +
                " INNER JOIN t_invests c on c.increase_activity_id2 = b.id " +
                " INNER JOIN t_bids bid on bid.id = c.bid_id " +
                " INNER JOIN  t_bill_invests  d on d.invest_id = c.id " +
                " INNER JOIN t_users u on u.id = c.user_id  where true ";

        if (type > 0) {
            paramsList.add(type);
        }

//        if (state > -10) {
//            paramsList.add(state);
//        }

        if (!StringUtil.isBlank(name)) {
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(startTime)) {
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            paramsList.add(stopTime);
        }

        if (!StringUtil.isBlank(userName)) {
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
        }

        table = table + condition;

        table = table + ") t ";

        //=======================================order_by=======================================
        String order_by_columns = "";
        String order_by = "";
        if (StringUtil.isBlank(order_by_columns)) {
            order_by_columns = "t.activity_id desc,t.activity_detail_id DESC,t.invest_id,t.bill_id";
        }
        order_by = " order by " + order_by_columns;

        return PageBeanForPlayJPA.getPageBeanMapBySQLExport2(columns, totalColumns, table, order_by, isExport, currPage, pageSize, paramsList.toArray());
    }
}
