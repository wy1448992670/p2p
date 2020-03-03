package jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.t_user_insur;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import play.Logger;
import play.Play;
import play.jobs.Every;
import utils.ErrorInfo;
import utils.FileUtil;
import utils.JPAUtil;
import business.User;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timevale.tgtext.text.pdf.security.p;
import com.zhongan.scorpoin.biz.common.CommonRequest;
import com.zhongan.scorpoin.biz.common.CommonResponse;
import com.zhongan.scorpoin.common.ZhongAnApiClient;
import com.zhongan.scorpoin.common.ZhongAnOpenException;

/**
 * 
 * @description. 同步协议号
 * 
 * @modificationHistory.
 * @author liulj 2017年2月24日下午5:17:21 TODO
 */

// @On("0 0 5 * * ?")
@Every("3min")
public class BankProtoJob extends BaseJob {

	public void doJob() {
		if(!"1".equals(IS_JOB))return;
		
		System.out.println("同步协议号...");
		// 协议号存放文件夹
		String bank_proto_folder = Play.configuration.getProperty("bank_proto_folder", "/work/bank");
		
		JSONArray data = null;
		data = FileUtil.readTxtFile4Folder(data, bank_proto_folder, ".txt");
		if(data != null && data.size() > 0) {
			for(int i = 0; i < data.size(); i++) {
				JSONObject temp = data.getJSONObject(i);
				System.out.println(temp.getString("file"));
				System.out.println(temp.getJSONArray("data"));
				JSONArray dataArr = temp.getJSONArray("data");
				for(int j = 0; j < dataArr.size(); j++) {
					JPAUtil.transactionBegin();
					System.out.println(dataArr.getString(j));
					
					try {
						
						String account = StringUtils.substringBefore(dataArr.getString(j), ",");
						String protocol_no = StringUtils.substringAfter(dataArr.getString(j), ",");
						
						JPAUtil.executeUpdate(new ErrorInfo(), "update t_user_bank_accounts set protocol_no = ? where account = ?", protocol_no, account);
						JPAUtil.transactionCommit();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						JPAUtil.transactionBegin();
					}
				}
				FileUtil.renameFileSuffix(temp.getString("file"), System.currentTimeMillis()+"");
			}
		}
	}
}
