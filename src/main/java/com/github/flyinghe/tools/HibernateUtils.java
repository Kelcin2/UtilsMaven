package com.github.flyinghe.tools;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * 本类为Hibernate工具类,加载hibernate.cfg.xml配置文件，该配置文件需在src目录下，
 * 注意Hibernate版本需4.3.11及以上
 *
 * @author Flying
 */
public class HibernateUtils {
    private static SessionFactory sessionFactory;

    static {
        // 从默认路径中读取hibernate核心配置文件
        Configuration configuration = new Configuration().configure();

        // 创建StandarServiceRegistry对象
        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        // 获取SessionFactory对象
        sessionFactory = configuration
                .buildSessionFactory(standardServiceRegistry);

		/*
         * 当JVM关闭时才销毁SessionFactory对象
		 */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                sessionFactory.close();
            }
        });
    }

    /*不允许创建该类实例*/
    private HibernateUtils() {}

    /**
     * 获取SessionFactory静态对象
     *
     * @return SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return HibernateUtils.sessionFactory;
    }

    /**
     * 获取一个Session对象,该方法每次返回一个全新的session对象
     *
     * @return Session
     */
    public static Session getSession() {
        return HibernateUtils.sessionFactory.openSession();
    }

    /**
     * 获取与当前线程绑定的Session对象
     *
     * @return Session
     */
    public static Session getTSession() {
        return HibernateUtils.sessionFactory.getCurrentSession();
    }
}
