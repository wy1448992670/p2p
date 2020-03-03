package controllers.supervisor.postLoanManager;

import business.User;
import business.UserAddressList;
import com.shove.Convert;
import constants.Constants;
import controllers.supervisor.SupervisorController;
import models.t_user_address_list;
import models.v_user_invest_info;
import models.v_users_apply;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import utils.*;

import java.io.File;
import java.util.Date;
import java.util.List;

public class UserListAction extends SupervisorController {

    /**
     * 所有会员列表
     */
    public static void userList() {
        String mobile = params.get("mobile");
        String id_number = params.get("id_number");
        String reality_name = params.get("reality_name");
        String curPage = params.get("currPage");
        String pageSize = params.get("pageSize");


        ErrorInfo error = new ErrorInfo();
        PageBean<v_users_apply> page = User.queryLoanUserBySupervisor(mobile, id_number, reality_name, curPage,
                pageSize, error);

        if (error.code < 0) {
            render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
        }


        render(page);
    }

    /**
     * 根据会员id获取通讯录列表
     *
     * @Description (TODO这里用一句话描述这个方法的作用)
     * @author: zj
     */
    public static void getUserAddressList(String mobile, String reality_name, String id, String currPage, String pageSize, String isExport,Integer returnJson) {

        int export = Convert.strToInt(isExport, 0);
        ErrorInfo error = new ErrorInfo();
        PageBean<t_user_address_list> page;
        try {
            page = UserAddressList.getUserAddressListByUserId(Long.valueOf(id), currPage, export==1?"-1":pageSize,mobile);
            if (error.code < 0) {
                render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
            }

            /* 导出excel */
            if (export == Constants.IS_EXPORT) {
                List<t_user_address_list> list = page.page;
                		
                JSONArray arrList = JSONArray.fromObject(list);

      
                File file = ExcelUtils.export("手机通讯录", arrList,
                        new String[]{ "手机号", "姓名"},
                        new String[]{"mobile", "name"});

                renderBinary(file, reality_name + "(" + mobile + ")" + "手机通讯录" + ".xls");
            }
            if(returnJson!=null && returnJson==1) {
            	renderJSON(page);
            }else {
            	render(page, mobile, reality_name, id);
            }
            
        } catch (NumberFormatException e) {
            play.Logger.error(e.getMessage(), "查询通讯录出错");
        } catch (Exception e) {
            play.Logger.error(e.getMessage(), "查询通讯录出错");
        }
        JSONObject json = new JSONObject();
        if (error.code < 0) {
            json.put("error", error);
            renderJSON(json);
        }
        render(json);
    }

}