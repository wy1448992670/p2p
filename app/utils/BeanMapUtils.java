package utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JavaBean and map converter.
 * 
 */
public final class BeanMapUtils {
	
	/**
	 * Converts a map to a JavaBean.
	 * 
	 * @param type type to convert
	 * @param map map to convert
	 * @return JavaBean converted
	 * @throws IntrospectionException failed to get class fields
	 * @throws IllegalAccessException failed to instant JavaBean
	 * @throws InstantiationException failed to instant JavaBean
	 * @throws InvocationTargetException failed to call setters
	 */
	public static final Object toBean(Class<?> type, Map<String, ? extends Object> map) 
			throws IntrospectionException, IllegalAccessException,	InstantiationException, InvocationTargetException {
		BeanInfo beanInfo = Introspector.getBeanInfo(type);
		Object obj = type.newInstance();
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i< propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (map.containsKey(propertyName)) {
				Object value = map.get(propertyName);
				Object[] args = new Object[1];
				args[0] = value;
				descriptor.getWriteMethod().invoke(obj, args);
			}
		}
		return obj;
	}
	
	/**
	 * Converts a JavaBean to a map.
	 * 
	 * @param bean JavaBean to convert
	 * @return map converted
	 * @throws IntrospectionException failed to get class fields
	 * @throws IllegalAccessException failed to instant JavaBean
	 * @throws InvocationTargetException failed to call setters
	 */
	public static final Map<String, String> toMap(Object bean)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		Map<String, String> returnMap = new HashMap<String, String>();
		BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (int i = 0; i< propertyDescriptors.length; i++) {
			PropertyDescriptor descriptor = propertyDescriptors[i];
			String propertyName = descriptor.getName();
			if (!propertyName.equals("class")) {
				Method readMethod = descriptor.getReadMethod();
				String result = (String) readMethod.invoke(bean, new Object[0]);
				if (null != result) {
					returnMap.put(toUpperCase(propertyName), result);
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * 首字母大写
	 * @param str
	 * @return
	 */
	private static String toUpperCase(String str){
		StringBuilder sb = new StringBuilder(str);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		str = sb.toString(); 
		return str;
	}
}
