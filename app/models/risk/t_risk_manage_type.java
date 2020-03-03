package models.risk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.helper.StringUtil;

import com.sun.star.uno.Exception;

import models.EnumModel;

@Entity
public class t_risk_manage_type extends EnumModel {
	private static final long serialVersionUID = 1L;

	public String name;

	/** t_risk_manage_big_type.id */
	public int big_type_id;

	/**
	 1,数字区间:value is decimal,击中[min_value,max_value)得score\r\n2,比较:配置页显示show_str,value.equls(compare_value),true得score\r\n3,日期区间;value is Date().getTime(),击中[min_value,max_value)得score
	 */
	public int compare_type;

	/** 限制,t_risk_manage_score_option最高的得分 */
	public BigDecimal top_score;

	/** 单位,显示用 */
	public String unit;
	
	/** 显示顺序 */
	public Integer sequence;
	
	/** 是否有效 0.无效 1.有效 */
	public boolean is_valid;
	 
	@Transient
	public List<t_risk_manage_score_option> riskManageScoreOptionList= new ArrayList<>();
	
  
	@Override
	public String toString() {
		return "t_risk_manage_type [id=" + id + ", name=" + name + ", big_type_id=" + big_type_id + ", compare_type=" + compare_type
				+ ", top_score=" + top_score + ", unit=" + unit + ", sequence=" + sequence + ", is_valid=" + is_valid
				+ "]";
	}
	
	/**
	 1,数字区间:value is decimal,击中[min_value,max_value)得score
	2,比较:配置页显示show_str,value.equls(compare_value),true得score
	3,日期区间;value is Date().getTime(),击中[min_value,max_value)得score
	 * @throws Exception 
	 */
	public BigDecimal getScore(String valueStr, Long applyId) throws Exception {
		BigDecimal score = BigDecimal.ZERO;
		
		//插入打分表
		t_risk_manage_score riskScore = new t_risk_manage_score();
		riskScore.credit_apply_id = applyId;
		riskScore.type_id = this.id;
		riskScore.value = valueStr;
		
		if(this.compare_type==1) {
			if(StringUtils.isEmpty(valueStr) || valueStr == "null") {
				valueStr = "0";
			}
			if(!NumberUtils.isNumber(valueStr)) {
				throw new Exception(name+":"+valueStr+" 不是数字");
			}
			BigDecimal value=new BigDecimal(valueStr);
			for(t_risk_manage_score_option riskManageScoreOption : riskManageScoreOptionList) {
				if(((riskManageScoreOption.min_value != null ? value.compareTo(riskManageScoreOption.min_value) >= 0 : true)) 
						&& (riskManageScoreOption.max_value != null ? value.compareTo(riskManageScoreOption.max_value) == -1 : true)) {
					score = riskManageScoreOption.score;
					
					riskScore.score = score;
					riskScore.save();
					return score;
				}
			}
		}else if(this.compare_type==2){
			for(t_risk_manage_score_option riskManageScoreOption : riskManageScoreOptionList) {
				if(riskManageScoreOption.compare_value.equals(valueStr)) {
					score = riskManageScoreOption.score;

					riskScore.score = score;
					riskScore.save();
					return score;
				}
			}
		}else if(this.compare_type==3){
			throw new Exception(name+"暂时不支持日期");
		}else {
			throw new Exception(name+"错误的比对类型");
		}

		riskScore.score = score;
		riskScore.save();
		return score;
	}
	
	 
	public List<t_risk_manage_score_option> getRiskManageScoreOptionList(){
		return t_risk_manage_score_option.find(" type_id = ? and is_valid = 1 ", this.id).fetch();
	}
}