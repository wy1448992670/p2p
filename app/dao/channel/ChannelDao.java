package dao.channel;

import models.channel.t_appstore;
import org.jsoup.helper.StringUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/28 15:03
 * @Description:
 */
public class ChannelDao {

    public static void addChannel(String num, String name, String subname, String type) {

        t_appstore channel = new t_appstore();
        channel.num = num;
        channel.name = name;
        channel.subname = subname;
        channel.type = type;

        channel.save();

    }

    public static PageBean<Map<String, Object>> channelList(int state, String num, String type, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {
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
        String columns = " *,case when state=1 then '正常' when state>1 then '关闭' else '' end statename ";
        //=======================================table=======================================
        String table = " from t_appstore a " +
                " where true ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (state != 0) {
            condition += " and a.state =? ";
            paramsList.add(state);
        }

        if (!StringUtil.isBlank(num)) {
            condition += " and  a.num LIKE ? ";
            paramsList.add("%" + num + "%");
        }

        if (!StringUtil.isBlank(type)) {
            condition += " and  a.type LIKE ? ";
            paramsList.add("%" + type + "%");
        }

        if (!StringUtil.isBlank(name)) {
            condition += " and  a.name LIKE ? ";
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(subname)) {
            condition += " and  a.subname LIKE ? ";
            paramsList.add("%" + subname + "%");
        }


        if (!StringUtil.isBlank(startTime)) {
            condition += " and  a.create_time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            condition += " and  a.create_time <= ? ";
            paramsList.add(stopTime);
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


    public static PageBean<Map<String, Object>> channelTotalList(String num, String type, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {


        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


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
        String columns = " a.id,a.num,a.type,a.`name`,a.subname,IFNULL(u.registe_count,0) registe_count,IFNULL(first_count,0) first_count,IFNULL(reinvest_count,0) reinvest_count,IFNULL(first_amount,0) first_amount,IFNULL(reinvest_amount,0) reinvest_amount, CASE when u.registe_count is NULL then 0 else TRUNCATE(IFNULL(first_count,0)/registe_count,2) end transe_rate1, CASE when first_count is null then 0 else TRUNCATE(IFNULL(reinvest_count,0)/first_count,2) end transe_rate2 ";
        //=======================================table=======================================
        String table = " from t_appstore a " +
                " LEFT JOIN  " +
                " (SELECT store_id,count(*) registe_count from t_users u where store_id is not null ";

        if (!StringUtil.isBlank(startTime)) {
            table += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            table += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }


        table += " GROUP BY store_id )u on u.store_id = a.id " +
                " LEFT JOIN  " +
                " ( " +
                " SELECT count(*) first_count ,sum(aa.amount-aa.red_amount) first_amount,aa.store_id from " +
                " ( " +
                "  SELECT func_inc_rownum(a.user_id,0) AS line, " +
                "    a.id,a.amount,a.bid_id,a.red_amount,a.time,u.store_id " +
                "  FROM " +
                "    t_invests a " +
                "  INNER JOIN (SELECT func_inc_rownum(0,1)) r  on TRUE " +
                "  INNER JOIN t_users u on a.user_id = u.id and u.store_id is not NULL " +
                "   where true ";

        if (!StringUtil.isBlank(startTime)) {
            table += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            table += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }

        table += " ORDER BY a.user_id,a.id ASC  " +
                " )aa where aa.line = 1 GROUP BY aa.store_id " +
                " ) fc on fc.store_id = a.id " +


                " LEFT JOIN ( " +
                "    SELECT store_id,sum(a.amount-a.red_amount)reinvest_amount FROM t_invests a INNER JOIN t_users u ON a.user_id = u.id AND u.store_id IS NOT NULL WHERE TRUE ";


        if (!StringUtil.isBlank(startTime)) {
            table += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            table += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }


        table += "    GROUP BY u.store_id " +
                " ) rf ON rf.store_id = a.id" +


                " LEFT JOIN ( " +
                "   SELECT count(*) reinvest_count,aa.store_id from " +
                "   ( " +
                "    SELECT a.user_id,u.store_id,count(1) FROM t_invests a INNER JOIN t_users u ON a.user_id = u.id AND u.store_id IS NOT NULL WHERE TRUE ";


        if (!StringUtil.isBlank(startTime)) {
            table += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            table += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }


        table += "    GROUP BY a.user_id HAVING count(1)>1 " +
                "   ) aa GROUP BY aa.store_id " +
                " ) rd ON rd.store_id = a.id" +
                " where true ";
        //=======================================condition=======================================

        if (!StringUtil.isBlank(num)) {
            condition += " and  a.num LIKE ? ";
            paramsList.add("%" + num + "%");
        }

        if (!StringUtil.isBlank(type)) {
            condition += " and  a.type LIKE ? ";
            paramsList.add("%" + type + "%");
        }

        if (!StringUtil.isBlank(name)) {
            condition += " and  a.name LIKE ? ";
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(subname)) {
            condition += " and  a.subname LIKE ? ";
            paramsList.add("%" + subname + "%");
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


    public static PageBean<Map<String, Object>> channelUserList(String userName, int isValidCard, int isBindCard, String num, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {
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
        String columns = " u.`name` username,u.time registe_time,u.mobile,case when u.reality_name is null then '否' else '是' end is_reality, u.reality_name,case u.is_bank when 0 then '否' else '是' end is_bank,u.last_login_time, u.balance, IFNULL(b.receive_corpus,0) receive_corpus, a.`name` storename,a.subname,a.num  ";
        //=======================================table=======================================
        String table = " from t_users u LEFT JOIN t_appstore a on u.store_id = a.id " +
                " LEFT JOIN (SELECT user_id,sum(receive_corpus) receive_corpus from t_bill_invests where `status` in (-1,-2) GROUP BY user_id) b on u.id = b.user_id " +
                " where u.store_id is not null and u.store_id != 0 and finance_type = 1 ";
        //=======================================condition=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition = "";


        if (isValidCard == 1) {
            condition += " and u.reality_name is not null ";
        } else if (isValidCard > 1) {
            condition += " and u.reality_name is null ";
        }


        if (isBindCard == 1) {
            condition += " and u.is_bank = 1 ";
        } else if (isBindCard > 1) {
            condition += " and u.is_bank = 0 ";
        }

        if (!StringUtil.isBlank(userName)) {
            condition += " and (u.name LIKE ? or u.reality_name like ? or u.mobile LIKE ?) ";
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
        }

        if (!StringUtil.isBlank(num)) {
            condition += " and  a.num LIKE ? ";
            paramsList.add("%" + num + "%");
        }

        if (!StringUtil.isBlank(name)) {
            condition += " and  a.name LIKE ? ";
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(subname)) {
            condition += " and  a.subname LIKE ? ";
            paramsList.add("%" + subname + "%");
        }


        if (!StringUtil.isBlank(startTime)) {
            condition += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            condition += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }

        //=======================================order_by=======================================
        String order_by_columns = "", asc_desc = "";
        String order_by = "";
        if (StringUtil.isBlank(order_by_columns)) {
            order_by_columns = "u.id";
        }
        if (StringUtil.isBlank(asc_desc)) {
            asc_desc = "desc";
        }
        order_by = " order by " + order_by_columns + " " + asc_desc;

        return PageBeanForPlayJPA.getPageBeanMapBySQL(columns, table + condition, order_by, currPage, pageSize, paramsList.toArray());
    }


    public static PageBean<Map<String, Object>> channelUserInvestList(String userName, int isFirst, String num, String name, String subname, String startTime, String stopTime, String currPageStr, String pageSizeStr) throws Exception {
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
        String columns = " *,case isfirst when 1 then '首投' else '复投' end isfirsts ";
        //=======================================table=======================================
        String table = " from  " +
                " (SELECT  t.id invest_id,t.time invest_time,t.amount,case when (t.id=t1.id) then 1 else 0 end isfirst,  d.id bid_no,d.title,d.apr, " +
                " CASE when d.period_unit = -1 THEN CONCAT(d.period,'年') when d.period_unit = 0 THEN CONCAT(d.period,'个月') when d.period_unit = 1 THEN CONCAT(d.period,'天') ELSE CONCAT(d.period,'个月') end period,     " +
                " u.reality_name,u.name username,u.mobile, " +
                " a.`name` storename,a.subname,a.num  " +
                " from t_invests t  " +
                " LEFT JOIN  " +
                " (SELECT min(id) id,user_id from t_invests GROUP BY user_id ) t1 on t1.user_id = t.user_id  " +
                " LEFT JOIN t_bids d on t.bid_id = d.id " +
                " LEFT JOIN t_users u on t.user_id = u.id " +
                " LEFT JOIN t_appstore a on u.store_id = a.id " +
                " where u.store_id is not null and u.store_id != 0 and u.finance_type = 1 ";
        //=======================================condition1=======================================
        List<Object> paramsList = new ArrayList<Object>();
        String condition1 = "";

        //=======================================condition1=======================================

        if (!StringUtil.isBlank(userName)) {
            condition1 += " and (u.name LIKE ? or u.reality_name like ? or u.mobile LIKE ?) ";
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
            paramsList.add("%" + userName + "%");
        }

        if (!StringUtil.isBlank(num)) {
            condition1 += " and  a.num LIKE ? ";
            paramsList.add("%" + num + "%");
        }

        if (!StringUtil.isBlank(name)) {
            condition1 += " and  a.name LIKE ? ";
            paramsList.add("%" + name + "%");
        }

        if (!StringUtil.isBlank(subname)) {
            condition1 += " and  a.subname LIKE ? ";
            paramsList.add("%" + subname + "%");
        }


        if (!StringUtil.isBlank(startTime)) {
            condition1 += " and  u.time >= ? ";
            paramsList.add(startTime);
        }

        if (!StringUtil.isBlank(stopTime)) {
            condition1 += " and  u.time <= ? ";
            paramsList.add(stopTime);
        }

        table += condition1;

        table += ") aa where true ";

        //=======================================condition2=======================================

        String condition = "";
        if (isFirst == 1) {
            condition += " and aa.isfirst = 1 ";
        } else if (isFirst > 1) {
            condition += " and aa.isfirst = 0 ";
        }

        //=======================================order_by=======================================
        String order_by_columns = "", asc_desc = "";
        String order_by = "";
        if (StringUtil.isBlank(order_by_columns)) {
            order_by_columns = "aa.invest_id";
        }
        if (StringUtil.isBlank(asc_desc)) {
            asc_desc = "desc";
        }
        order_by = " order by " + order_by_columns + " " + asc_desc;

        return PageBeanForPlayJPA.getPageBeanMapBySQL2(columns," IFNULL(sum(amount),0) invest_money ", table + condition, order_by, currPage, pageSize, paramsList.toArray());
    }
}
