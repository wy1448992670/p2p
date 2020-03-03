package payment.hf.util;

import java.io.Serializable;

import play.Logger;
import chinapnr.SecureLink;

/**
 * 汇付天下验签类
 * @author yx
 *	@create 2014年12月8日 下午7:42:06
 */
public class SignUtils implements Serializable {
    private static final long  serialVersionUID = 3640874934537168392L;

    /** MD5签名类型 **/
    public static final String SIGN_TYPE_MD5 = "M";

    /** RSA签名类型 **/
    public static final String SIGN_TYPE_RSA = "R";

    /** RSA验证签名成功结果 **/
    public static final int    RAS_VERIFY_SIGN_SUCCESS = 0;
    
    /** 商户客户号 **/
    public static final String RECV_MER_ID = HfConstants.MERID;

    /** 商户公钥文件地址 **/
    public static final String MER_PUB_KEY_PATH = HfConstants.MER_PUB_KEY_PATH;

    /** 商户私钥文件地址 **/
    public static final String MER_PRI_KEY_PATH = HfConstants.MER_PRI_KEY_PATH;
    
    /**
     * RSA方式加签
     * 
     * @param custId
     * @param forEncryptionStr
     * @param charset
     * @return
     * @throws Exception 
     */
    public static String encryptByRSA(String forEncryptionStr) throws Exception {
        SecureLink sl = new SecureLink();
        int result = sl.SignMsg(RECV_MER_ID, MER_PRI_KEY_PATH, forEncryptionStr);
        if (result < 0) {
            throw new Exception();
        }
        return sl.getChkValue();
    }

    public static boolean verifyByRSA(String forEncryptionStr, String chkValue) throws Exception {
        try {
            int verifySignResult = new SecureLink().VeriSignMsg(MER_PUB_KEY_PATH, forEncryptionStr, chkValue);
            return verifySignResult == RAS_VERIFY_SIGN_SUCCESS;
        } catch (Exception e) {

        	Logger.error("###SignUtils -> verifyByRSA : %s", e.getMessage());
        	return false;
        }
    }
}
