package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

//缓存管理
public class CacheManager {
	static HashMap<String, Object> cacheMap = new HashMap<String, Object>();

	// 单实例构造方法
	private CacheManager() {
		super();
	}

	// 获取缓存信息
	public static Cache getCacheInfo(String key) {
		if (hasCache(key)) {
			Cache cache = getCache(key);
			
			if (cacheExpired(cache)) { // 调用判断是否终止方法
				cache.setExpired(true);
			}
			
			return cache;
		}else{
			clearByKey(key);
			return null;
		}
	}

	// 获取布尔值的缓存
	public static boolean getSimpleFlag(String key) {
		try {
			return (Boolean) cacheMap.get(key);
		} catch (NullPointerException e) {
			return false;
		}
	}

	public static long getServerStartdt(String key) {
		try {
			return (Long) cacheMap.get(key);
		} catch (Exception ex) {
			return 0;
		}
	}

	// 设置布尔值的缓存
	public synchronized static boolean setSimpleFlag(String key, boolean flag) {
		if (flag && getSimpleFlag(key)) {// 假如为真不允许被覆盖
			return false;
		} else {
			cacheMap.put(key, flag);
			return true;
		}
	}

	public synchronized static boolean setSimpleFlag(String key, long serverbegrundt) {
		if (cacheMap.get(key) == null) {
			cacheMap.put(key, serverbegrundt);
			
			return true;
		} else {
			return false;
		}
	}

	// 得到缓存。同步静态方法
	private synchronized static Cache getCache(String key) {
		return (Cache) cacheMap.get(key);
	}

	// 判断是否存在一个缓存
	private synchronized static boolean hasCache(String key) {
		return cacheMap.containsKey(key);
	}

	// 清除所有缓存
	public synchronized static void clearAll() {
		cacheMap.clear();
	}

	// 清除指定的缓存
	public synchronized static void clearByKey(String key) {
		cacheMap.remove(key);
	}

	// 清除某一类特定缓存,通过遍历HASHMAP下的所有对象，来判断它的KEY与传入的TYPE是否匹配
	public synchronized static void clearStartsWithAll(String type) {
		Iterator<Entry<String, Object>> i = cacheMap.entrySet().iterator();
		String key;
		List<String> arr = new ArrayList<String>();
		
		try {
			Entry<String, Object> entry = null;
			
			while (i.hasNext()) {
				entry = i.next();
				key = entry.getKey();
				
				if (key.startsWith(type)) { // 如果匹配则删除掉
					arr.add(key);
				}
				
				entry = null;
			}
			
			int listSize = arr.size();
			
			for (int k = 0; k < listSize; k++) {
				clearByKey(arr.get(k) + "");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// 载入缓存
	public synchronized static void putCache(String key, Cache obj) {
		cacheMap.put(key, obj);
	}

	// 载入缓存信息
	public static void putCacheInfo(String key, Cache obj, long dt,	boolean expired) {
		obj.setTimeOut(dt + System.currentTimeMillis()); // 设置多久后更新缓存
		obj.setExpired(expired); // 缓存默认载入时，终止状态为FALSE
		cacheMap.put(key, obj);
	}

	// 重写载入缓存信息方法
	public static void putCacheInfo(String key, Cache obj, long dt) {
		obj.setKey(key);
		obj.setTimeOut(dt + System.currentTimeMillis());
		obj.setExpired(false);
		cacheMap.put(key, obj);
	}

	// 判断缓存是否终止
	public static boolean cacheExpired(Cache cache) {
		if (null == cache) { // 传入的缓存不存在
			return false;
		}
		
		long nowDt = System.currentTimeMillis(); // 系统当前的毫秒数
		long cacheDt = cache.getTimeOut(); // 缓存内的过期毫秒数
		
		if (cacheDt <= 0 || cacheDt > nowDt) { // 过期时间小于等于零时,或者过期时间大于当前时间时，则为FALSE
			return false;
		} else { // 大于过期时间 即过期
			return true;
		}
	}

	// 获取缓存中的大小
	public static int getCacheSize() {
		return cacheMap.size();
	}

	// 获取指定的类型的大小
	public static int getCacheSize(String type) {
		int k = 0;
		Iterator<Entry<String, Object>> i = cacheMap.entrySet().iterator();
		String key;
		
		try {
			Entry<String, Object> entry = null;
			
			while (i.hasNext()) {
				entry = (Entry<String, Object>) i.next();
				key = (String) entry.getKey();
				
				if (key.indexOf(type) != -1) { // 如果匹配则删除掉
					k++;
				}
				
				entry = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return k;
	}

	// 获取缓存对象中的所有键值名称
	public static ArrayList<String> getCacheAllkey() {
		ArrayList<String> a = new ArrayList<String>();
		
		try {
			Iterator<Entry<String, Object>> i = cacheMap.entrySet().iterator();
			Entry<String, Object> entry = null;
			
			while (i.hasNext()) {
				entry = (Entry<String, Object>) i.next();
				a.add((String) entry.getKey());
				entry = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		
		return a;
	}

	// 获取缓存对象中指定类型 的键值名称
	public static ArrayList<String> getCacheListkey(String type) {
		ArrayList<String> a = new ArrayList<String>();
		String key;
		
		try {
			Iterator<Entry<String, Object>> i = cacheMap.entrySet().iterator();
			Entry<String, Object> entry = null;
			
			while (i.hasNext()) {
				entry = (Entry<String, Object>) i.next();
				key = (String) entry.getKey();
				
				if (key.indexOf(type) != -1) {
					a.add(key);
				}
				
				entry = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		}
		
		return a;
	}
}