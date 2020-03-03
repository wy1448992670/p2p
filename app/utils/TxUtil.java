package utils;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import play.Logger;
import play.db.DB;
import play.db.jpa.JPA;

public class TxUtil {

	/**
	 * 开启一个新事务
	 */
	public static void begin() {
		if (null != JPA.local.get()) {
			try {
				JPA.local.get().entityManager.close();
			} catch (Exception e) {
			}
			
			JPA.local.remove();
		}
		
		JPA jpa = new JPA();
		jpa.entityManager = JPA.entityManagerFactory.createEntityManager();
		jpa.entityManager.setFlushMode(FlushModeType.COMMIT);
		jpa.entityManager.getTransaction().begin();
		JPA.local.set(jpa);
	}

	/**
	 * 关闭事务且提交
	 * @param rollback
	 * @param ignoreQueue
	 */
	public static void close(boolean rollback, boolean ignoreQueue) {
		if (null == JPA.local.get()) {
			return;
		}
		
		EntityManager em = JPA.em();

		try {
			DB.getConnection().setAutoCommit(false);
		} catch (Exception e) {
			Logger.error("设置connection为自动提交事务失败" + e.getMessage());
		}

		if (!ignoreQueue) {
			Logger.info("当前事务没有提交!");
		}

		if (!em.getTransaction().isActive()) {
			Logger.info("当前事务非活动状态!");
		}
			
		if ((rollback) || em.getTransaction().getRollbackOnly()) {
			em.getTransaction().rollback();
		} else {
			try {
				em.getTransaction().commit();
			} catch (Throwable e) {
				Logger.error("事务提交失败!" + e.getMessage());
			}
		}
	}

	/**
	 * 结束事务
	 * @param rollback
	 */
	public static void close(boolean rollback) {
		close(rollback, true);
	}
}