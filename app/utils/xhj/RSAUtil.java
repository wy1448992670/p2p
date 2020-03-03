package utils.xhj;

import com.alibaba.druid.util.Base64;
import com.alibaba.druid.util.JdbcUtils;
import com.alibaba.fastjson.JSONObject;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * create by dong
 */
public class RSAUtil {

    private static final int    MAX_ENCRYPT_BLOCK          = 117;

    private static final int    MAX_DECRYPT_BLOCK          = 128;

    private static final String DEFAULT_PRIVATE_KEY_STRING = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANd5+bse6zf85NY/xSlzHWHJ1M7DG1SY/uuWZX8/lKzlRGa8xtNTDItTdVmvAFqV1k9kiF1P70XTn75c3w27lIRMDQs5zI3jKET8JQKBwh6jqp4RLcFxkxmMz0CG0+4XnSHLynkyr3S2cLwo6YINYOztCkDTfJ4DV3YmHqHkriV7AgMBAAECgYEAhTUn2LE1kugpg/XqakR6/pAqmiPtym/G8FeKqY7h17zoe2bqt4vY7m0K2AWi/10wHua0kpMvBYjdcW6yiifTmI9UlHF3/M4lvq+WAa00Ys50aPuYfGDVcYJnk1OPoJ8i65gv+Xh/qcYxkP6Walfpe/9Xfk9TEsdBDtf8WdPF4RkCQQD0rVdU5jNhOzkqekM0HIyVjRx7U2Iuge7q6xj4SGNzWJfmBBkub/k+opDHZVjx9/CnvbJ/B0DKgtt4Crqk+p6tAkEA4XKyr91cQcaMNjruSzN35AAG5WoaQQb6jGamH/GbA8iGNo4ii7mqDMpjxH7KR/OKkvVcYDcMix6IIm2XZJqhxwJAXWND7/2lIrluCk58FAnJhtNDSbb7xHCHdlahQzKt8rqfz4VE7zqB0WxPAiwmlMRjsEJxPJbSHflwNxMxRgL6SQJAcICMKraME0bBMU63G0/TxDM/Pbx03X5eCTIwNECc2oZ2c0L6ej9sXHWi06txxfYCcNOABYNy/vl9dbC6m7mOgQJBAKutjLLe+k4uODvcVixpQx7V6QpKmCo8UZsmYgwp1+Ldp6523Cdqw+JoFEV+cuVWTCHdcAKDuIWtJ7+b9dbj0hk=";
    public static final String  DEFAULT_PUBLIC_KEY_STRING  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDXefm7Hus3/OTWP8Upcx1hydTOwxtUmP7rlmV/P5Ss5URmvMbTUwyLU3VZrwBaldZPZIhdT+9F05++XN8Nu5SETA0LOcyN4yhE/CUCgcIeo6qeES3BcZMZjM9AhtPuF50hy8p5Mq90tnC8KOmCDWDs7QpA03yeA1d2Jh6h5K4lewIDAQAB";

   public static void main(String[] args) throws Exception {
       //**********  sit *********
        String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANd5+bse6zf85NY/xSlzHWHJ1M7DG1SY/uuWZX8/lKzlRGa8xtNTDItTdVmvAFqV1k9kiF1P70XTn75c3w27lIRMDQs5zI3jKET8JQKBwh6jqp4RLcFxkxmMz0CG0+4XnSHLynkyr3S2cLwo6YINYOztCkDTfJ4DV3YmHqHkriV7AgMBAAECgYEAhTUn2LE1kugpg/XqakR6/pAqmiPtym/G8FeKqY7h17zoe2bqt4vY7m0K2AWi/10wHua0kpMvBYjdcW6yiifTmI9UlHF3/M4lvq+WAa00Ys50aPuYfGDVcYJnk1OPoJ8i65gv+Xh/qcYxkP6Walfpe/9Xfk9TEsdBDtf8WdPF4RkCQQD0rVdU5jNhOzkqekM0HIyVjRx7U2Iuge7q6xj4SGNzWJfmBBkub/k+opDHZVjx9/CnvbJ/B0DKgtt4Crqk+p6tAkEA4XKyr91cQcaMNjruSzN35AAG5WoaQQb6jGamH/GbA8iGNo4ii7mqDMpjxH7KR/OKkvVcYDcMix6IIm2XZJqhxwJAXWND7/2lIrluCk58FAnJhtNDSbb7xHCHdlahQzKt8rqfz4VE7zqB0WxPAiwmlMRjsEJxPJbSHflwNxMxRgL6SQJAcICMKraME0bBMU63G0/TxDM/Pbx03X5eCTIwNECc2oZ2c0L6ej9sXHWi06txxfYCcNOABYNy/vl9dbC6m7mOgQJBAKutjLLe+k4uODvcVixpQx7V6QpKmCo8UZsmYgwp1+Ldp6523Cdqw+JoFEV+cuVWTCHdcAKDuIWtJ7+b9dbj0hk=" ;
        String publickey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDXefm7Hus3/OTWP8Upcx1hydTOwxtUmP7rlmV/P5Ss5URmvMbTUwyLU3VZrwBaldZPZIhdT+9F05++XN8Nu5SETA0LOcyN4yhE/CUCgcIeo6qeES3BcZMZjM9AhtPuF50hy8p5Mq90tnC8KOmCDWDs7QpA03yeA1d2Jh6h5K4lewIDAQAB";
    
        
        JSONObject json = new JSONObject();
//        json.put("idNumber", "13043119900901211X");
//        json.put("personName", "杨川");
//        json.put("degree", "2");
//        json.put("mobileNo", "13730027137");
        
        json.put("idNumber", "320524197108121539");
        json.put("personName", "吴国峰");
        json.put("mobileNo", "17701710507");
        
        //加密
        String text1 = encrypt(privateKey.trim(), new String(json.toJSONString().getBytes("UTF-8")));
       
        System.out.println(text1);
        String text2 = decrypt(publickey,text1);
        System.out.println(text2);
    }

    public static String decrypt(String cipherText) throws Exception {
        return decrypt((String) null, cipherText);
    }

    /**
     * 解密数据
     * */
    public static String decrypt(String publicKeyText, String cipherText) throws Exception {
        PublicKey publicKey = getPublicKey(publicKeyText);

        return decrypt(publicKey, cipherText);
    }

    public static PublicKey getPublicKeyByX509(String x509File) {
        if (x509File == null || x509File.length() == 0) {
            return getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(x509File);

            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate cer = factory.generateCertificate(in);
            return cer.getPublicKey();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            JdbcUtils.close(in);
        }
    }

    public static PublicKey getPublicKey(String publicKeyText) {
        if (publicKeyText == null || publicKeyText.length() == 0) {
            publicKeyText = DEFAULT_PUBLIC_KEY_STRING;
        }

        try {
            byte[] publicKeyBytes = Base64.base64ToByteArray(publicKeyText);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        }
    }

    public static PublicKey getPublicKeyByPublicKeyFile(String publicKeyFile) {
        if (publicKeyFile == null || publicKeyFile.length() == 0) {
            return getPublicKey(null);
        }

        FileInputStream in = null;
        try {
            in = new FileInputStream(publicKeyFile);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len = 0;
            byte[] b = new byte[512 / 8];
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }

            byte[] publicKeyBytes = out.toByteArray();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA", "SunRsaSign");
            return factory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        } finally {
            JdbcUtils.close(in);
        }
    }

    /**
     * 解密数据（分段解密数据）
     * */
    public static String decrypt(PublicKey publicKey, String cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            // 因为 IBM JDK 不支持私钥加密, 公钥解密, 所以要反转公私钥
            // 也就是说对于解密, 可以通过公钥的参数伪造一个私钥对象欺骗 IBM JDK
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
            Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
            cipher = Cipher.getInstance("RSA"); //It is a stateful object. so we need to get new one.
            cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey);
        }

        if (cipherText == null || cipherText.length() == 0) {
            return cipherText;
        }

        //byte[] cipherBytes = Base64.base64ToByteArray(cipherText);
        //byte[] plainBytes = cipher.doFinal(cipherBytes);

        // 开始分段解密
        byte[] encryptedData = Base64.base64ToByteArray(cipherText);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        // 结束分段加密
        // return new String(plainBytes);
        return new String(decryptedData);
    }

    public static String encrypt(String plainText) throws Exception {
        return encrypt((String) null, plainText);
    }

    /**
     * 加密数据
     * */
    public static String encrypt(String key, String plainText) throws Exception {
        if (key == null) {
            key = DEFAULT_PRIVATE_KEY_STRING;
        }

        byte[] keyBytes = Base64.base64ToByteArray(key);
        return encrypt(keyBytes, plainText);
    }

    /**
     * 加密数据（数据分段加密）
     * */
    public static String encrypt(byte[] keyBytes, String plainText) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA", "SunRsaSign");
        PrivateKey privateKey = factory.generatePrivate(spec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            //For IBM JDK, 原因请看解密方法中的说明
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPrivateExponent());
            Key fakePublicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, fakePublicKey);
        }
        // 开始分段处理

        byte[] data = plainText.getBytes("UTF-8");
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 结束分段处理
        //byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
        //String encryptedString = Base64.byteArrayToBase64(encryptedBytes);
        String encryptedString = Base64.byteArrayToBase64(encryptedData);
        return encryptedString;
    }

    public static byte[][] genKeyPairBytes(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[][] keyPairBytes = new byte[2][];

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
        gen.initialize(keySize, new SecureRandom());
        KeyPair pair = gen.generateKeyPair();

        keyPairBytes[0] = pair.getPrivate().getEncoded();
        keyPairBytes[1] = pair.getPublic().getEncoded();

        return keyPairBytes;
    }

    public static String[] genKeyPair(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        byte[][] keyPairBytes = genKeyPairBytes(keySize);
        String[] keyPairs = new String[2];

        keyPairs[0] = Base64.byteArrayToBase64(keyPairBytes[0]);
        keyPairs[1] = Base64.byteArrayToBase64(keyPairBytes[1]);

        return keyPairs;
    }


}

