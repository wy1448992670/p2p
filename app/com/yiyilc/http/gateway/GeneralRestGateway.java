package com.yiyilc.http.gateway;

import com.shove.Convert;
import com.shove.gateway.GeneralRestGatewayInterface;
import com.shove.security.Encrypt;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Play;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
/**
 * 来自com.shove.play.jar version:1.0 
 * @see com.shove.gateway.GeneralRestGateway
 * 容器已经URLDecoder.decode(),Gateway.handle()方法不使用URLDecoder.decode()
 * @author zqq
 * @date 2018-11-28
 */
public class GeneralRestGateway {
	public static int handle(String key, int allowTimespanSeconds, GeneralRestGatewayInterface grgi,
			StringBuilder errorDescription) throws IOException {
		errorDescription.delete(0, errorDescription.length());
		if (StringUtils.isBlank(key)) {
			throw new RuntimeException(
					"在使用 com.shove.gateways.GeneralRestGateway.handle 方法解析参数并处理业务时，必须提供一个用于摘要签名用的 key (俗称 MD5 加盐)。");
		} else {
			Request request = Request.current();
			Logger.debug("request.url："+request.url);
			
			Response response = Response.current();
			if (request != null && response != null) {
				request.encoding = "utf-8";
				response.encoding = "utf-8";
				Set<String> keys = request.params.data.keySet();
				if (keys.isEmpty()) {
					errorDescription.append("没有找到 Query 参数，接口数据非法。");
					return -3;
				} else {
					Map<String, String> parameters = new HashMap();
					String _s = "";
					String _t = "";
					Iterator var11 = keys.iterator();

					while (var11.hasNext()) {
						String t_key = (String) var11.next();
						if (t_key.equals("_s")) {
							_s = request.params.get("_s");
							//System.out.println("t_key:"+t_key+" value:"+request.params.get(t_key));
						} else {
							if (t_key.equals("_t")) {
								//_t = URLDecoder.decode(request.params.get("_t"), "utf-8");
								_t=request.params.get("_t");
							}
							//System.out.println("t_key:"+t_key+" value:"+request.params.get(t_key));
							//parameters.put(t_key, URLDecoder.decode(request.params.get(t_key), "utf-8"));
							parameters.put(t_key, request.params.get(t_key));
						}
					}
					//System.out.println("put end-------------------------------------------------------------------------------------");
					if (!StringUtils.isBlank(_s) && !StringUtils.isBlank(_t)) {
						List<String> parameterNames = new ArrayList(parameters.keySet());
						Collections.sort(parameterNames);
						StringBuffer signData = new StringBuffer();

						for (int i = 0; i < parameters.size(); ++i) {
							//System.out.println("t_key:"+parameterNames.get(i)+" value:"+parameters.get(parameterNames.get(i)));
							signData.append((String) parameterNames.get(i) + "="
									+ (String) parameters.get(parameterNames.get(i))
									+ (i < parameters.size() - 1 ? "&" : ""));
						}

						Date timestamp = Convert.strToDate(_t, getLongGoneDate());
						long span = Math.abs((timestamp.getTime() - System.currentTimeMillis()) / 1000L);
						if (allowTimespanSeconds > 0 && span > (long) allowTimespanSeconds) {
							errorDescription.append("访问超时。");
							return -5;
						} else if (!_s.equalsIgnoreCase(Encrypt.MD5(signData + key, "utf-8"))) {
							errorDescription.append("签名错误，接口数据非法。");
							return -6;
						} else {
							parameters.remove("_t");
							String result = "";
							
							//当前环境的域名或者ip,用于callback等
							String appconfig_domain=Play.configuration.getProperty("appconfig.domain");
							String appconfig_port=Play.configuration.getProperty("appconfig.port");
							
							if(StringUtils.isNotBlank(appconfig_domain)) {
								parameters.put("request.domain", appconfig_domain);
							}else {
								parameters.put("request.domain", request.domain);
							}
							
							if(StringUtils.isNotBlank(appconfig_port)) {
								parameters.put("request.port", appconfig_port);
							}else {
								parameters.put("request.port",  request.port+"");
							}

							try {
								result = grgi.delegateHandleRequest(parameters, errorDescription);
							} catch (Exception var17) {
								var17.printStackTrace();
								errorDescription.append("应用程序的代理回调程序遇到异常，详细原因是：" + var17.getMessage());
								return -7;
							}

							if (!StringUtils.isBlank(result)) {
								response.encoding = "UTF-8";
								response.print(result);
								response.out.flush();
								response.out.close();
							}

							return 0;
						}
					} else {
						errorDescription.append("缺少 _s 或 _t 参数，接口数据非法。");
						return -4;
					}
				}
			} else {
				errorDescription.append("Http 上下文错误。");
				return -2;
			}
		}
	}

	public static String buildUrl(String urlBase, String key, Map<String, String> parameters)
			throws UnsupportedEncodingException {
		if (!parameters.containsKey("_s") && !parameters.containsKey("_t")) {
			if (StringUtils.isBlank(key)) {
				throw new RuntimeException(
						"在使用 com.shove.gateways.GeneralRestGateway.buildUrl 方法构建通用 REST 接口 Url 时，必须提供一个用于摘要签名用的 key (俗称 MD5 加盐)。");
			} else {
				parameters.put("_t", Convert.dateToStr(new Date(), "yyyy-MM-dd HH:mm:ss", "1970-01-01 00:00:00"));
				List<String> parameterNames = new ArrayList(parameters.keySet());
				Collections.sort(parameterNames);
				if (!urlBase.endsWith("?") && !urlBase.endsWith("&")) {
					urlBase = urlBase + (urlBase.indexOf("?") == -1 ? "?" : "&");
				}

				String signData = "";

				for (int i = 0; i < parameters.size(); ++i) {
					String _key = (String) parameterNames.get(i);
					String _value = (String) parameters.get(_key);
					signData = signData + _key + "=" + _value;
					urlBase = urlBase + _key + "=" + URLEncoder.encode(_value, "utf-8");
					if (i < parameters.size() - 1) {
						signData = signData + "&";
						urlBase = urlBase + "&";
					}
				}

				urlBase = urlBase + "&_s=" + Encrypt.MD5(signData + key, "utf-8");
				return urlBase;
			}
		} else {
			throw new RuntimeException(
					"在使用 com.shove.gateways.GeneralRestGateway.buildUrl 方法构建通用 REST 接口 Url 时，不能使用 _s, _t 此保留字作为参数名。");
		}
	}

	private static Date getLongGoneDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(1, -30);
		return calendar.getTime();
	}
}