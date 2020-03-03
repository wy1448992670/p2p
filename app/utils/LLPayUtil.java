package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import payment.LLPay.enums.SignTypeEnum;
import payment.LLPay.security.Md5Algorithm;
import payment.LLPay.security.TraderRSAUtil;
import play.mvc.Http.Header;
import play.mvc.Http.Request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


/**
* 常用工具函数
* @author guoyx e-mail:guoyx@lianlian.com
* @date:2013-6-25 下午05:23:05
* @version :1.0
*
*/
public class LLPayUtil{
    /**
     * str空判断
     * @param str
     * @return
     * @author guoyx
     */
    public static boolean isnull(String str)
    {
        if (null == str || str.equalsIgnoreCase("null") || str.equals(""))
        {
            return true;
        } else
            return false;
    }

    /**
     * 获取当前时间str，格式yyyyMMddHHmmss
     * @return
     * @author guoyx
     */
    public static String getCurrentDateTimeStr()
    {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        String timeString = dataFormat.format(date);
        return timeString;
    }

    /**
     * 
     * 功能描述：获取真实的IP地址
     * @param request
     * @return
     * @author guoyx
     */
    public static String getIpAddr(Request request)
    {
  	if (request == null) return "";
    	
    	Header header = request.headers.get("X-Forwarded-For");
        String ip =  header == null ? null : header.value(); 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("Proxy-Client-IP");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("WL-Proxy-Client-IP");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("HTTP_CLIENT_IP"); 
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
//            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        	header = request.headers.get("Proxy-Client-IP");
        	ip =  header == null ? null : header.value();
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
            ip = request.remoteAddress;
        } 
        return ip; 
    }

    /**
     * 生成待签名串
     * @param paramMap
     * @return
     * @author guoyx
     */
    public static String genSignData(JSONObject jsonObject)
    {
        StringBuffer content = new StringBuffer();

        // 按照key做首字母升序排列
        List<String> keys = new ArrayList<String>(jsonObject.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < keys.size(); i++)
        {
            String key = (String) keys.get(i);
            if ("sign".equals(key))
            {
                continue;
            }
            String value = jsonObject.getString(key);
            // 空串不参与签名
            if (isnull(value))
            {
                continue;
            }
            content.append((i == 0 ? "" : "&") + key + "=" + value);

        }
        String signSrc = content.toString();
        if (signSrc.startsWith("&"))
        {
            signSrc = signSrc.replaceFirst("&", "");
        }
        return signSrc;
    }

    /**
     * 加签
     * 
     * @param reqObj
     * @param rsa_private
     * @param md5_key
     * @return
     * @author guoyx
     */
    public static String addSign(JSONObject reqObj, String rsa_private,
            String md5_key)
    {
        if (reqObj == null)
        {
            return "";
        }
        String sign_type = reqObj.getString("sign_type");
        if (payment.LLPay.enums.SignTypeEnum.MD5.getCode().equals(sign_type))
        {
            return addSignMD5(reqObj, md5_key);
        } else
        {
            return addSignRSA(reqObj, rsa_private);
        }
    }

    /**
     * 签名验证
     * 
     * @param reqStr
     * @return
     */
    public static boolean checkSign(String reqStr, String rsa_public, String md5_key)
    {
        JSONObject reqObj = JSON.parseObject(reqStr);
        if (reqObj == null)
        {
            return false;
        }
        String sign_type = reqObj.getString("sign_type");
        if (SignTypeEnum.MD5.getCode().equals(sign_type))
        {
            return checkSignMD5(reqObj, md5_key);
        } else
        {
            return checkSignRSA(reqObj, rsa_public);
        }
    }

    /**
     * RSA签名验证
     * 
     * @param reqObj
     * @return
     * @author guoyx
     */
    private static boolean checkSignRSA(JSONObject reqObj, String rsa_public)
    {

        System.out.println("进入商户[" + reqObj.getString("oid_partner")
                + "]RSA签名验证");
        if (reqObj == null)
        {
            return false;
        }
        String sign = reqObj.getString("sign");
        // 生成待签名串
        String sign_src = genSignData(reqObj);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]待签名原串"
                + sign_src);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]签名串"
                + sign);
        try
        {
            if (TraderRSAUtil.checksign(rsa_public, sign_src, sign))
            {
                System.out.println("商户[" + reqObj.getString("oid_partner")
                        + "]RSA签名验证通过");
                return true;
            } else
            {
                System.out.println("商户[" + reqObj.getString("oid_partner")
                        + "]RSA签名验证未通过");
                return false;
            }
        } catch (Exception e)
        {
            System.out.println("商户[" + reqObj.getString("oid_partner")
                    + "]RSA签名验证异常" + e.getMessage());
            return false;
        }
    }

    /**
     * MD5签名验证
     * 
     * @param signSrc
     * @param sign
     * @return
     * @author guoyx
     */
    public static boolean checkSignMD5(JSONObject reqObj, String md5_key)
    {
        System.out.println("进入商户[" + reqObj.getString("oid_partner")
                + "]MD5签名验证");
        if (reqObj == null)
        {
            return false;
        }
        String sign = reqObj.getString("sign");
        // 生成待签名串
        String sign_src = genSignData(reqObj);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]待签名原串"
                + sign_src);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]签名串"
                + sign);
        sign_src += "&key=" + md5_key;
        try
        {
            if (sign.equals(Md5Algorithm.getInstance().md5Digest(
                    sign_src.getBytes("utf-8"))))
            {
                System.out.println("商户[" + reqObj.getString("oid_partner")
                        + "]MD5签名验证通过");
                return true;
            } else
            {
                System.out.println("商户[" + reqObj.getString("oid_partner")
                        + "]MD5签名验证未通过");
                return false;
            }
        } catch (UnsupportedEncodingException e)
        {
            System.out.println("商户[" + reqObj.getString("oid_partner")
                    + "]MD5签名验证异常" + e.getMessage());
            return false;
        }
    }

    /**
     * RSA加签名
     * 
     * @param reqObj
     * @param rsa_private
     * @return
     * @author guoyx
     */
    private static String addSignRSA(JSONObject reqObj, String rsa_private)
    {
        System.out.println("进入商户[" + reqObj.getString("oid_partner")
                + "]RSA加签名");
        if (reqObj == null)
        {
            return "";
        }
        // 生成待签名串
        String sign_src = genSignData(reqObj);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]加签原串"
                + sign_src);
        try
        {
            return TraderRSAUtil.sign(rsa_private, sign_src);
        } catch (Exception e)
        {
            System.out.println("商户[" + reqObj.getString("oid_partner")
                    + "]RSA加签名异常" + e.getMessage());
            return "";
        }
    }

    /**
     * MD5加签名
     * 
     * @param reqObj
     * @param md5_key
     * @return
     * @author guoyx
     */
    private static String addSignMD5(JSONObject reqObj, String md5_key)
    {
        System.out.println("进入商户[" + reqObj.getString("oid_partner")
                + "]MD5加签名");
        if (reqObj == null)
        {
            return "";
        }
        // 生成待签名串
        String sign_src = genSignData(reqObj);
        System.out.println("商户[" + reqObj.getString("oid_partner") + "]加签原串"
                + sign_src);
        sign_src += "&key=" + md5_key;
        System.out.println("签名串："+sign_src);
        try
        {
            return Md5Algorithm.getInstance().md5Digest(
                    sign_src.getBytes("utf-8"));
        } catch (Exception e)
        {
            System.out.println("商户[" + reqObj.getString("oid_partner")
                    + "]MD5加签名异常" + e.getMessage());
            return "";
        }
    }

    /**
     * 读取request流
     * @param req
     * @return
     * @author guoyx
     */
    public static String readReqStr(HttpServletRequest request)
    {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try
        {
            reader = new BufferedReader(new InputStreamReader(request
                    .getInputStream(), "utf-8"));
            String line = null;

            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (null != reader)
                {
                    reader.close();
                }
            } catch (IOException e)
            {

            }
        }
        return sb.toString();
    }
}
