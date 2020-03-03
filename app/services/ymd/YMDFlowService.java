package services.ymd;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

import business.Score;
import business.User;
import models.t_users;
import models.t_users_info;
import models.core.t_credit_apply;
import models.core.t_credit_increase_apply;
import models.core.t_new_product;
import models.core.t_person;
import models.file.t_file_relation_dict;
import play.cache.Cache;
import services.business.CreditApplyService;
import services.file.FileService;
//亿美贷流程
public class YMDFlowService {
	//亿美贷产品
	//static final t_new_product PRODUCT=new t_new_product().getEnumByCode("YI");
	/*
	返回数据定义:data={
		//flowNode:
		//1.id_picture_authentication 身份证照片解析
		//2.id_authentication_submit 身份证认证信息提交
		//3.living_authentication 活体认证
		//4.add_bank 绑定银行卡
		//5.mobile_operator_authentication 运营商认证
		//6.base_information 基础信息
		//7.supplement_information 补充资料
		//8.credit_apply 额度申请
		//9.credit_apply_status 额度申请状态
		"flowNode"=int
		,"user"={
			"reality_name"=String//身份证名
			,"id_number"=String//身份证号码
			,"mobile"=String//手机号码
			,"id_picture_authentication_status"=Integer//身份证照片认证实名接口 状态:0.未实名,-1实名失败,1实名成功
			,"living_authentication_status"=Integer//活体认证接口 状态:0.未实名,-1实名失败,1实名成功
			,"education_id"=int//教育情况ID		*需要目录
			,"marital_id"=int//婚姻状况ID		*需要目录
			,"house_id"=int//购房情况			*需要目录
			,"car_id"=int//购车情况				*需要目录
			,"address"=String//居住地址
			,"city_id"=int//城市Id,居住地 t_dict_city		*需要目录
			,"company"=String//公司全称
			,"credit_apply_file_status"=Integer//用户风控补充资料,文件提交状态 0|null:未提交,1:已提交
		}
		,"usersInfo"={
			"work_industry"=Integer//工作性质:0自由职业者;1全职
			,"salary"=BigDecimal//月工资
			,"accumulation_fund"=BigDecimal//公积金汇缴数额
			,"rent"=BigDecimal//房租
			,"QQ"=String//QQ|微信号
			,"first_contacts"={"name"=String,"phone"=String}//常用联系人1 t_person
			,"first_contacts_relation"=Integer//常用联系人1和用户的关系 t_dict_relation		*需要目录
			,"second_contacts"={"name"=String,"phone"=String}//常用联系人2 t_person
			,"second_contacts_relation"=Integer//常用联系人2和用户的关系 t_dict_relation	*需要目录
		}
		
		//解析过身份证,并且没提交才会有,Map<String,Object>
		,"idcardInfo"={
			"name"="徐姐夫"
			,"idcard_number"="110119110119110119"
			,"address"="伊拉克省叙利亚市默罕默德村"
			,"valid_date_start"="19120801"
			,"valid_date_end"="19220921"
		}
		//文件列表(flowNode in 1.id_picture_authentication 身份证照片解析|7.supplement_information 补充资料|9.credit_apply_status 额度申请状态(提额时),类型:List<t_file_relation_dict>):
		,fileDictList=[{
			//relation_type:
			//1.users身份证照片实名认证 2.users活体认证 3.users用户风控资料 4.credit_apply
			//5.increase_credit_apply 6.borrow_apply 7.users.user_type in (2,3)企业用户资料
			//8.orgnization合作机构资料
			"relation_type"=int//业务类型
			,"file_dict_id"=int//文件类目id
			,"t_file_dict"={
				"id"=int//文件类目id
				,"name"=String//文件类目名,如[身份证正面],[银行卡反面],[店铺门面照]
				,"type"=int//1,图片 2,PDF}//文件类目对象
			}
			,"file_relation_list"=[{
				"file_id"=long//文件记录id
				,"file"={
					"id"=long//文件记录id
					,"real_path"=String//文件地址
					,"create_time"=Date//创建时间
				}
			}]
			,"required_num"=int//至少文件数量,0不必填
			,"max_num"=int//最多文件数量,-1不限数量
			,"sequence"=int//文件类目顺序
		}]
		,"meetRelationRequired"=boolean//满足必填文件要求
		,"haveRelationOptional"=boolean//有可选文件可以上传
		,"hasBank"=boolean//是否有银行卡(没有额度申请,且通过活体认证)
		//额度申请,有额度申请的情况下
		,"creditApply"={
			"consociation_user_id"=Long//合作用户|机构 t_users.id
			,"items_amount"=BigDecimal//项目总金额
			,"apply_credit_amount"=BigDecimal//申请额度
			,"audit_credit_amount"=BigDecimal//审批额度
			,"apply_period_unit"=Integer//借款期限单位-1: 年;0:月;1:日
			,"apply_period"=Integer//借款期限
			,"apply_apr"=BigDecimal//审批利息
			,"status"=int//-3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过 4.冻结 5.关闭
			,"remark"=String//说明
		}
		//提升额度申请,有额度申请的情况下
		,"creditIncreaseApply"={
			"status"=int//-3.终审不通过 -2.初审不通过 -1.机审不通过 0.待审 1.机审通过 2.初审通过 3.终审通过
			,"remark"=String//说明
		}
		,"useCredit"=BigDecimal//已使用额度,有额度申请的情况下,可申请提额=[项目总金额-审批额度]改为[申请金额-审批额度]
		,"mobile_operator_authentication_url"=String//运营商认证的url
	}
	 */
	/**
	 * fileRelationType:上传文件关系
	 * 1.users身份证照片实名认证
	 * 2.users活体认证
	 * 3.users用户风控资料
	 * 4.credit_apply 补充资料
	 * 5.increase_credit_apply 补充资料
	 * 6.borrow_apply
	 * 7.users.user_type in (2,3)企业用户资料
	 * 8.orgnization合作机构资料
	 * 9.credit_apply第三方报告
	 */
	/**
	 * flowNode:app流程
	 * 1.id_picture_authentication 身份证照片解析
	 * 2.id_authentication_submit 身份证认证信息提交
	 * 3.living_authentication 活体认证   不直接到这一步,如果user.instance.id_picture_authentication_status有效,则认为已近活体认证
	 * 4.add_bank 绑定银行卡
	 * 5.mobile_operator_authentication 运营商认证
	 * 6.base_information 基础信息
	 * 7.supplement_information 补充资料
	 * 8.credit_apply 额度申请
	 * 9.credit_apply_status 额度申请状态
	 * @throws CloneNotSupportedException 
	 */
	/**
	 * @param user_id
	 * @param product_id
	 * @param forceCreditStatus 是否强制进入[额度申请状态]节点.
	 * @return
	 * @throws CloneNotSupportedException
	 * 如果:
	 * [额度申请单]不存在,则重新计算一遍用户审核流程.
	 * [额度申请单]存在,且forceCreditStatus==false,且[额度申请状态]是[审核拒绝]|([额度关闭]&&额度未使用),则重新计算一遍用户审核流程,
	 * [额度申请单]存在,且forceCreditStatus==false,且[额度申请状态]不是[审核拒绝]|([额度关闭]&&额度未使用),则进入[额度申请状态]节点,
	 * [额度申请单]存在,且forceCreditStatus==true,则进入[额度申请状态]节点,
	 * if(creditApply==null || (creditApply.isClose() &&! forceCreditStatus) ) {
	 * 		重新计算一遍用户审核流程
	 * }else{
	 * 		强制进入[额度申请状态]节点
	 * }
	 */
	public static Map<String,Object> getFlowNode(Long user_id,Long product_id,boolean forceCreditStatus) throws CloneNotSupportedException{
		int flowNode=0;
		int fileRelationType=0;
		Map<String,Object> result=new HashMap<String,Object>();
		User user=new User();
		user.id=user_id;
		result.put("user", user.instance.hide_data());
		t_users_info userInfo= user.getUserInfo();
		result.put("userInfo", userInfo.hide_date());
		
		t_credit_apply creditApply=CreditApplyService.getLastCreditApplyByProductId(user_id, product_id);
		//已用额度
		BigDecimal useCredit=BigDecimal.ZERO;
		if(creditApply!=null) {
			useCredit=CreditApplyService.getUseCreditByCreditApplyId(creditApply.id);
		}
		
		if(creditApply==null || (creditApply.isClose() &&! forceCreditStatus && useCredit.compareTo(BigDecimal.ZERO)<=0 ) ) {
			flowNode=YMDFlowService.userFlowNode(user,product_id, result);
		}else {
			flowNode=9;//credit_apply_status 额度申请状态
			result.put("creditApply", creditApply.hide_data());
			
			//提额申请,该额度申请最后一笔提额记录
			t_credit_increase_apply creditIncreaseApply=CreditApplyService.getCreditIncreaseApplyByCreditApplyId(creditApply.id);
			result.put("creditIncreaseApply", creditIncreaseApply);
			
			result.put("useCredit", useCredit);
			
			//提额时查看fileRelationType.4 credit_apply 资料上传情况
			fileRelationType=4;
			List<t_file_relation_dict> fileDictList=FileService.getFileUpdateByRelation(fileRelationType,creditApply.id);
			result.put("fileDictList", fileDictList);
			result.put("meetRelationRequired", FileService.meetRelationRequired(fileDictList));//满足必填文件要求
			result.put("haveRelationOptional", FileService.haveRelationOptional(fileDictList));//有可选文件可以上传
			
		}
		
		result.put("flowNode", flowNode);
		return result;
	}
	/**
	 * 计算用户额度申请流程,不包含额度生清单状态
	 */
	private static int userFlowNode(User user,Long product_id,Map<String,Object> result) {
		int flowNode=0;
		int fileRelationType=0;
		//身份证照片认证接口 状态:0.未实名,-1认证失败,1认证成功
		if(new ArrayList<Integer>(){{add(null);add(-1);add(0);}}.contains(user.instance.id_picture_authentication_status)) {
			try {
				Object idcardInfo= Cache.get("ymd_idcard_"+user.id);
				if(idcardInfo!=null) {
					flowNode=2;//id_authentication_submit 身份证认证信息提交
					result.put("idcardInfo", (Map<String, String>) idcardInfo );
					fileRelationType=1;//users身份证照片实名认证 
					//上传用户身份证照片时,查看fileRelationType.1 users身份证照片实名认证 资料上传情况
					List<t_file_relation_dict> fileDictList=FileService.getFileUpdateByRelation(fileRelationType,user.id);
					result.put("fileDictList", fileDictList);
					
				}else {
					flowNode=1;//id_picture_authentication 身份证照片解析
				}
			} catch (Exception e) {
				e.printStackTrace();
				flowNode=1;//id_picture_authentication 身份证照片解析
			}
		
		//活体认证接口 状态:0.未实名,-1实名失败,1实名成功
		//}else if(new ArrayList<Integer>(){{add(null);add(-1);add(0);}}.contains(user.instance.living_authentication_status)) {
			//flowNode=3;//living_authentication 活体认证
		} else {
			//是否有银行卡
			boolean hasBank=Score.hasBank(user.id);
			result.put("hasBank", hasBank);
			if(!hasBank) {
				flowNode=4;//add_bank 绑定银行卡
			}else if(new ArrayList<Integer>(){{add(null);add(-1);add(0);}}.contains(user.instance.mobile_operator_authentication_status)) {
				flowNode=5;//mobile_operator_authentication 运营商认证
				//flowNode==5时,需要给出运营商授权页面的url,在YMDFlowRequest中设置
			}else if(!YMDFlowService.hasUserBaseInformation(user.instance,user.getUserInfo()) ) {
				flowNode=6;//base_information 基础信息
			}else {
				//上传用户风控资料时,查看fileRelationType.3 users用户风控资料  上传情况
				fileRelationType=3;
				List<t_file_relation_dict> fileDictList=FileService.getFileUpdateByRelation(fileRelationType,user.id);
				
				//满足必填文件要求
				boolean meetRelationRequired=FileService.meetRelationRequired(fileDictList);
				//(有必选条件&&满足必填文件要求) || (没有必选条件&&上传过文件) || 文件集合为空
				//boolean meetRelationRequiredOrNoRequiredOrEmpty=FileService.meetRelationRequiredOrNoRequiredOrEmpty(fileDictList);
				if(meetRelationRequired && user.instance.credit_apply_file_status !=null && user.instance.credit_apply_file_status.equals(1)) {
					flowNode=8;//credit_apply 额度申请
				}else{
					flowNode=7;//supplement_information 补充资料
					result.put("fileDictList", fileDictList);
					result.put("meetRelationRequired", meetRelationRequired);//满足必填文件要求
					result.put("haveRelationOptional", FileService.haveRelationOptional(fileDictList));//有可选文件可以上传
				}
			}
		}
		
		return flowNode;
	}
	
	/**
	YMD流程 用户 基础信息是否有效
	*/
	private static boolean hasUserBaseInformation(t_users user,t_users_info userInfo){
		//t_dict_educations
		if(user.education_id==0) return false;
		//工作性质:0自由职业者;1全职
		if(userInfo.work_industry==null) {
			return false;
		}else {
			if(userInfo.work_industry.equals(1)) {
				//公司城市Id, t_dict_city
				if(user.company_city_id==0) return false;
	
				//公司全称
				if(StringUtils.isBlank(user.company)) return false;
				
				//月工资
				if(userInfo.salary==null) return false;
				
				//公积金汇缴数额
				if(userInfo.accumulation_fund==null) return false;
			}
		}
		//t_dict_maritals
		if(user.marital_id==0) return false;
		//城市Id,居住地 t_dict_city
		if(user.city_id==0) return false;
		
		//房租
		if(userInfo.rent==null) return false;
		//购房情况 t_dict_houses
		if(user.house_id==0) return false;
		
		//QQ|微信号
		if(StringUtils.isBlank(userInfo.QQ)) return false;
		//购车情况 t_dict_cars
		if(user.car_id==0) return false;
		//居住地址
		if(StringUtils.isBlank(user.address)) return false;
		
		//常用联系人1 t_person.id
		if(!YMDFlowService.hasPersonInformation(userInfo.first_contacts)) return false;
		//常用联系人1和用户的关系 t_dict_relation
		if(userInfo.first_contacts_relation==null) return false;
		//常用联系人2 t_person.id
		if(!YMDFlowService.hasPersonInformation(userInfo.second_contacts)) return false;
		//常用联系人2和用户的关系 t_dict_relation
		if(userInfo.second_contacts_relation==null) return false;
		
		return true;
	}
	
	/**
	YMD流程 用户 联系人信息是否有效
	*/
	private static boolean hasPersonInformation(t_person person){
		if(StringUtils.isNotBlank(person.name)) return true;
		if(StringUtils.isNotBlank(person.id_card)) return true;
		if(StringUtils.isNotBlank(person.phone)) return true;
		if(StringUtils.isNotBlank(person.email)) return true;
		return false;
	}
}
