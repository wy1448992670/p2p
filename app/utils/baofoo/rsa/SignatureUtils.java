package utils.baofoo.rsa;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import utils.baofoo.util.FormatUtil;

/**
 * Created by BF100400 on 2017/4/17.
 */
public class SignatureUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * @encryptStr 摘要
     * @signature  签名
     * @pubCerPath 公钥路径
     * 验签
     */
    public static boolean verifySignature(String pubCerPath, String encryptStr, String signature) throws Exception {
        PublicKey publicKey = RsaReadUtil.getPublicKeyFromFile(pubCerPath);
        return  verify(encryptStr.getBytes("UTF-8"),publicKey.getEncoded(), signature);
    }
    /**
     * @encryptStr 摘要
     * @pfxPath pfx证书路径
     * @priKeyPass 私钥
     * @charset 编码方式
     * 签名
     */
    public static String encryptByRSA(String encryptStr, String pfxPath, String priKeyPass)throws Exception {
        PrivateKey privateKey = RsaReadUtil.getPrivateKeyFromFile(pfxPath, priKeyPass);
        return  sign(encryptStr.getBytes("UTF-8") ,privateKey.getEncoded());
    }

    /**
     * 校验数字签名
     * @param data 已加密数据
     * @param keyBytes 公钥
     * @param sign 数字签名
     * @throws Exception
     *
     */
    public static boolean verify(byte[] data, byte[] keyBytes, String sign) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(FormatUtil.hex2Bytes(sign));
    }

    /**
     *
     * 用私钥对信息生成数字签名
     * @param data 已加密数据
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return FormatUtil.byte2Hex(signature.sign());
    }

}
