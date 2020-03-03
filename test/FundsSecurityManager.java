

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 资金安全管理器
 * 
 * @author yuy
 * @time 2015-07-09 15:40
 */
public class FundsSecurityManager {

	/**
	 * 对象锁映射
	 */
	public static Map<Long, ReentrantLock> objectLockMap = null;

	/**
	 * 锁
	 */
	public ReentrantLock lock = null;

	public long id = 0l;

	public FundsSecurityManager(long id) {
		this.id = id;
	}

	/**
	 * 获取对象锁
	 * 
	 * @param userId
	 * @return Object
	 */
	public static ReentrantLock getLock(long id) {
		if (objectLockMap == null)
			objectLockMap = new HashMap<Long, ReentrantLock>();

		ReentrantLock lock = objectLockMap.get(id);
		if (lock == null) {
			synchronized (FundsSecurityManager.class) {
				lock = objectLockMap.get(id);
				if (lock == null) {
					lock = new ReentrantLock();
					objectLockMap.put(id, lock);
				}
			}
		}
		return lock;
	}

	/**
	 * 加锁
	 */
	public void addLock() {
		if (id == 0) {
			throw new IllegalArgumentException("id不能为0");
		}
		lock = getLock(id);
		lock.lock();
		
		System.out.println("-----------LOCK[" + lock.hashCode() + "]" + "; thread[" + Thread.currentThread().getName() + "]");
	}

	/**
	 * 解锁
	 */
	public void deleteLock() {
		if (id == 0) {
			throw new IllegalArgumentException("id不能为0");
		}
		
		System.out.println("-----------UNLOCK[" + lock.hashCode() + "]" + "; thread[" + Thread.currentThread().getName() + "]");
		
//		lock.unlock();
	}

	public static void main(String[] args) throws Exception {

	}
}
