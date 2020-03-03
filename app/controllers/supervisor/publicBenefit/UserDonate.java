package controllers.supervisor.publicBenefit;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import net.sf.json.JSONObject;
import models.t_public_benefit;
import models.t_user_donate;
import utils.ErrorInfo;
import utils.PageBean;
import business.BackstageSet;
import business.PublicBenefit;
import business.User;

import com.alibaba.fastjson.JSON;

import constants.Templets;
import controllers.supervisor.SupervisorController;

/**
 *
 * @description.  亿亿公益
 *
 * @modificationHistory.
 * @author liulj 2017年3月17日上午10:46:49 TODO
 */
public class UserDonate extends SupervisorController {

	/**
	 *
	 * @author liulj
	 * @creationDate. 2017年3月17日 上午10:58:27
	 * @description.  捐款明细
	 *
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param pageSize
	 * @param currPage
	 */
	public static void donateList(String beginTime, String endTime, String name, int usedType, int orderType, int pageSize, int currPage){
		System.out.println(orderType+"===========");
		PageBean<Map<String, Object>> result = User.findUserDonate(beginTime, endTime, name, usedType, orderType, pageSize, currPage);
		System.out.println(JSON.toJSONString(result));
		render(result);
	}

	/**
	 *
	 * @author liulj
	 * @creationDate. 2017年3月17日 上午10:59:23
	 * @description.  项目列表
	 *
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param pageSize
	 * @param currPage
	 */
	public static void benefitList(String beginTime, String endTime, String name, int orderType, int pageSize, int currPage){
		PageBean<Map<String, Object>> result = User.findPublicBenefit(beginTime, endTime, name, orderType, pageSize, currPage);
		System.out.println(JSON.toJSONString(result));
		render(result);
	}

	/**
	 *
	 * @author liulj
	 * @creationDate. 2017年3月17日 上午11:00:38
	 * @description.  编辑项目
	 *
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param pageSize
	 * @param currPage
	 */
	public static void benefitEdit(t_public_benefit benefit, int edit){
		//PageBean<Map<String, Object>> result = User.findUserBankInsur(beginTime, endTime, null, pageSize, currPage);
		System.out.println(JSON.toJSONString(benefit));

		int editSuccess = 0;

		if(edit == 0){
			if(benefit.id != null){
				benefit = t_public_benefit.findById(benefit.id);
			}
			System.out.println(JSON.toJSONString(benefit));
		}else{

			if(benefit != null){

				try {
					benefit.content = URLDecoder.decode(benefit.content, "UTF-8");
					benefit.content = Templets.replaceAllHTML(benefit.content);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//benefit = new t_public_benefit();

				/*if(benefit.id > 0){
					public_benefit.id = benefit.id;
				}

				public_benefit.name = benefit.name;
				public_benefit.start_dt = benefit.start_dt;
				public_benefit.plan_donate = benefit.plan_donate;
				public_benefit.actual_donate = benefit.actual_donate;
				public_benefit.donate_users = benefit.donate_users;
				public_benefit.descr = benefit.descr;
				public_benefit.cover = benefit.cover;
				public_benefit.content = benefit.content;
				public_benefit.ins_dt = benefit.ins_dt;*/
				benefit.save();
				editSuccess = 1;
			}
		}
		render(benefit, editSuccess);
	}

	/**
	 *
	 * @author liulj
	 * @creationDate. 2017年3月17日 上午11:01:14
	 * @description.  公益账户
	 *
	 * @param beginTime
	 * @param endTime
	 * @param name
	 * @param pageSize
	 * @param currPage
	 */
	public static void benefitAccount(){
		JSONObject result = User.findPublicBenefitAccount(null);
		System.out.println(result);
		render(result);
	}

	/**
	 *
	 * @author liulj
	 * @creationDate. 2017年3月23日 上午11:28:52
	 * @description.  编辑捐款年化率
	 *
	 * @param publicBenefitRate
	 * @param edit
	 */
	public static void benefitRateEdit(double publicBenefitRate, int edit){
		//PageBean<Map<String, Object>> result = User.findUserBankInsur(beginTime, endTime, null, pageSize, currPage);
		BackstageSet backstageSet = new BackstageSet();

		int editSuccess = 0;

		if(edit == 1){
			ErrorInfo error = new ErrorInfo();

			backstageSet = new BackstageSet();
			backstageSet.public_benefit_rate = publicBenefitRate;

			backstageSet.publicBenefitRate(error);
			editSuccess = 1;

			flash.success(error.msg);
		}else{
			backstageSet = BackstageSet.getCurrentBackstageSet();
		}
		render(backstageSet, editSuccess);
	}
}
