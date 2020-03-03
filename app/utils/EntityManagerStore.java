package utils;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import play.Logger;
import play.db.DB;
import play.db.jpa.JPA;
/**
 * JPA会话暂存器
 * 暂存当前会话(a),开启一个新会话(b),可以在会话b中操作数据,不受会话a的事务控制
 * 但是会话a和会话b无法共享资源:jpa实体资源,数据库锁资源.
 * 所以只能作简单的插入日志功能.不能从事复杂业务
 * @author zqq 2019-04-02
 */
public class EntityManagerStore {
	private JPA store=null;
	
	public void pushAndBegin(boolean readonly) {
		store=JPA.local.get();
		EntityManager manager = JPA.entityManagerFactory.createEntityManager();
        manager.setFlushMode(FlushModeType.COMMIT);
        manager.setProperty("org.hibernate.readOnly", readonly);
        manager.getTransaction().begin();
        JPA context = new JPA();
        context.entityManager = manager;
        JPA.local.set(context);
	}
	
	public void closeAndPop(boolean rollback) {
		if (null != JPA.local.get()) {
			EntityManager em = JPA.em();

			try {
				DB.getConnection().setAutoCommit(false);
			} catch (Exception e) {
				Logger.error("设置connection为自动提交事务失败" + e.getMessage());
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
		JPA.local.set(store);
	}

}
