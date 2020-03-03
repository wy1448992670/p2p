

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

	private ReentrantLockTest() {}

	private static class Single {

		private final static ReentrantLockTest instance = new ReentrantLockTest();
	}

	public static ReentrantLockTest getInstance() {
		return Single.instance;
	}

// 	public static Map<Integer, ReentrantLock> locks = new ConcurrentHashMap<Integer, ReentrantLock>();
 	public static Map<Integer, ReentrantLock> locks = new HashMap<Integer, ReentrantLock>();

	public ReentrantLock getReentrantLock(int id) {
		if (locks.get(id) == null) {
			synchronized (ReentrantLockTest.class) {
//				if(locks.get(id) == null){
//					
//					locks.put(id, new ReentrantLock());
//				}
				
					locks.put(id, new ReentrantLock());
			}
		}
		return locks.get(id);
	}

	public void addLock(int id) {
		
		ReentrantLock lock = getReentrantLock(id);
		
		getReentrantLock(id).lock();
		
		System.out.println("-----------LOCK[" + lock.hashCode() + "]" + "; thread[" + Thread.currentThread().getName() + "]");
	}

	public void unLock(int id) {
		ReentrantLock lock = getReentrantLock(id);
		
		System.out.println("-----------UNLOCK[" + lock.hashCode() + "]" + "; thread[" + Thread.currentThread().getName() + "]");
		
		getReentrantLock(id).unlock();
	}
}