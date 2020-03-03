/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.net
 *
 *    Filename:    MyX509TrustManager.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月21日 下午2:06:57
 *
 *    Revision:
 *
 *    2018年12月21日 下午2:06:57
 *        - first revision
 *
 *****************************************************************/
package utils.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @ClassName MyX509TrustManager
 * @Description https是对链接加了安全证书SSL的，如果服务器中没有相关链接的SSL证书，<br>
 *              它就不能够信任那个链接，也就不会访问到了。所以我们第一步是自定义一个信任管理器。<br>
 *              自要实现自带的X509TrustManager接口<br>
 *              当方法为空是默认为所有的链接都为安全，也就是所有的链接都能够访问到。<br>
 *              当然这样有一定的安全风险，可以根据实际需要写入内容。
 * @author zj
 * @Date 2018年12月21日 下午2:06:57
 * @version 1.0.0
 */
public class MyX509TrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		// TODO Auto-generated method stub
		return null;
	}

}
