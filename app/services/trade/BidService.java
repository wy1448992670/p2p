package services.trade;

import dao.trade.BidDao;

import java.util.List;
import java.util.Map;

/**
 * @Auther: huangsj
 * @Date: 2018/12/26 15:33
 * @Description:
 */
public class BidService {



    /**
     * 查找正在募集标的当前的活动信息
     * @param bid
     * @return
     */
    public static List<Map<String, Object>> getActivityInfoForCurrentBid(Long bid){
       return  BidDao.getActivityInfoForCurrentBid(bid);
    }
}
