package services.ymd;

 
import java.util.Map;
import dao.ymd.BorrowerInfoDao;
import utils.ErrorInfo;

/**
 *  亿美贷借款人信息
 * @author MSI
 *
 */
public class BorrowerInfoService {
	public static void saveUserInfo(Map<String, String> params, Long userId,ErrorInfo error) {
		BorrowerInfoDao.saveUserInfo(params,userId,error);
	}
	
}
