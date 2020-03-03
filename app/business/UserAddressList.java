package business;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import constants.Constants;
import models.t_user_address_list;
import play.Logger;
import play.db.jpa.JPA;
import utils.NumberUtil;
import utils.PageBean;
import utils.PageBeanForPlayJPA;

public class UserAddressList {
	public static void pushUserAddressList(Long userId, List<t_user_address_list> paramAddressListList) throws Exception {
		List<t_user_address_list> userAddressListList=t_user_address_list.find("byUser_id", userId).fetch();
		
		for(t_user_address_list paramAddressList:paramAddressListList) {
			paramAddressList.user_id=userId;
			if(userAddressListList.contains(paramAddressList)) {
				continue;
			}
			
			try {
				paramAddressList=paramAddressList.save();
			} catch (Exception e) {
				Logger.error("通讯录"+paramAddressList+"保存失败");
				JPA.setRollbackOnly();
				throw new Exception("通讯录"+paramAddressList+"保存失败");
			}
		}
	}

	/**
	 *
	 * @Description  根据userId，获取通讯录列表
	 * @param userId
	 * @return  通讯录列表
	 * @throws Exception
	 * @author: zj
	 */
	public static PageBean<t_user_address_list> getUserAddressListByUserId(Long userId,String currPageStr, 
			String pageSizeStr,String mobile) throws Exception {
		int currPage = Constants.ONE;
		int pageSize = Constants.PAGE_SIZE;
		List<Object> paramsList = new ArrayList<Object>();
		if (NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
		}
		if (NumberUtil.isNumericInt(pageSizeStr)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		StringBuffer hql = new StringBuffer();
		hql = hql.append("from t_user_address_list where user_id=? ");
		paramsList.add(userId);
		if(StringUtils.isNotEmpty(mobile)) {
			hql.append(" and mobile = ? ");
			paramsList.add(mobile.trim());
		} 
		
		
		Logger.info(" sql :[%s]", hql);
	
		
		PageBean<t_user_address_list> page=PageBeanForPlayJPA.getPageBeanBySQL(new t_user_address_list()," * ", hql.toString(),
				" order by update_time desc ", currPage, pageSize, paramsList.toArray());

		return page;
	}
}
