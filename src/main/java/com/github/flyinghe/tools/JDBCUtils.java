package com.github.flyinghe.tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Stack;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * JDBC工具类
 * ，本类采用的数据库连接池是c3p0。c3p0连接池的配置文件必须在src目录下，且名称必须为c3p0-config.xml，并且采用的是默认配置。
 *
 * @author Flying
 */
public class JDBCUtils {
    // 创建一个连接池
    private static ComboPooledDataSource dataSource = new ComboPooledDataSource();
    // 事务专用连接 ， 本连接是线程安全的
    private static ThreadLocal<Connection> tl_conn = new ThreadLocal<Connection>();
    // 用于在事务中设置保存点，这是线程安全的
    private static ThreadLocal<Stack<Savepoint>> tl_sp = new ThreadLocal<Stack<Savepoint>>();

    /**
     * 获取数据库连接池对象，一般提供给第三方dbUtils使用
     *
     * @return 连接池对象
     */
    public static DataSource getDataSource() {
        return JDBCUtils.dataSource;
    }

    /**
     * 获取连接池里的一个数据库连接。若为有事务连接则直接返回事务连接，事务连接是线程安全的。
     *
     * @return 返回一个数据库连接, 失败返回null
     */
    public static Connection getConnection() {
        Connection connection = tl_conn.get();
        try {
            // 若tl.get()返回不是null说明当前线程有事务连接，直接返回事务连接
            if (connection != null) {
                return connection;
            }
            // 若为null则从连接池里返回一个连接
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    /**
     * 获取与数据库的连接，数据库配置文件需要在当前类目录(即源文件夹src目录下，或者类文件夹下，对于JavaProject项目是在bin目录下，
     * 对于webProject项目是在classes目录下)下，数据库配置文件名为JDBCInfo.properties
     *
     * @return connection 一个与数据库的连接对象
     */
    // public static Connection getConnection() {
    // Properties properties = null;
    // Connection connection = null;
    // try {
    // properties = new Properties();
    // properties.load(JDBCTools.class.getClassLoader()
    // .getResourceAsStream("JDBCInfo.properties"));
    // // 为兼容JDBC4.0以下的Jar包，需要先注册驱动
    // Class.forName(properties.getProperty("driver"));
    //
    // connection = DriverManager.getConnection(
    // properties.getProperty("url"),
    // properties.getProperty("user"),
    // properties.getProperty("password"));
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (SQLException e) {
    // e.printStackTrace();
    // } catch (ClassNotFoundException e) {
    // e.printStackTrace();
    // }
    // return connection;
    // }

    /**
     * 开始一个事务，该事务连接是线程安全的。
     */
    public static void startTransaction() {
        Connection connection = tl_conn.get();
        if (connection != null) {
            throw new RuntimeException(
                    "You have started a Transaction and can not start transaction repeatedly for the same connection!");
        }

        try {
            connection = JDBCUtils.getConnection();
            connection.setAutoCommit(false);
            tl_conn.set(connection);
            tl_sp.set(new Stack<Savepoint>());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用于在事务中设置一个保存点，属于线程安全的
     */
    public static void setSavePoint() {
        Connection connection = tl_conn.get();
        if (connection == null) {
            throw new RuntimeException("You do not start a Transaction so you can not set a savepoint!");
        }
        try {
            Stack<Savepoint> stack_sp = tl_sp.get();
            Savepoint sp = connection.setSavepoint();
            stack_sp.push(sp);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 提交事务并关闭该事务，该事务连接是线程安全的。提交失败会回滚事务。
     */
    public static void commitTransaction() {
        Connection connection = tl_conn.get();
        if (connection == null) {
            throw new RuntimeException("You do not start a Transaction so you can not commit a transaction!");
        }
        try {
            connection.commit();
            connection.close();
            tl_conn.remove();
            tl_sp.remove();
        } catch (SQLException e) {
            JDBCUtils.rollbackTransaction();
            throw new RuntimeException(e);
        }
    }

    /**
     * 回滚事务并关闭该连接，该事务连接是线程安全的。
     */
    public static void rollbackTransaction() {
        Connection connection = tl_conn.get();
        if (connection == null) {
            throw new RuntimeException("You do not start a Transaction so you can not rollback a transaction!");
        }
        try {
            connection.rollback();
            connection.close();
            tl_conn.remove();
            tl_sp.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将事务回滚到最近的一个保存点，若没有保存点则回滚到开始事务处
     */
    public static void rollbackTransactionToTheLatestSavepoint() {
        Connection connection = tl_conn.get();
        if (connection == null) {
            throw new RuntimeException("You do not start a Transaction so you can not rollback a transaction!");
        }
        try {
            Stack<Savepoint> stack_sp = tl_sp.get();
            if (stack_sp.empty()) {
                JDBCUtils.rollbackTransaction();
                return;
            }
            connection.rollback(stack_sp.pop());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭连接，若不是事务连接则直接关闭，若是事务连接则不执行任何操作，事务连接由提交事务方法关闭
     *
     * @param connection 一个连接
     */
    private static void release(Connection connection) {
        Connection con = tl_conn.get();
        try {
            // 若不是事务连接则直接关闭，若是事务连接不执行任何操作，事务连接由提交事务方法关闭
            if (con == null || con != connection) {
                connection.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭与数据库的连接资源，包括‘ResultSet’‘Statement’‘Connection’ 的实例对象所占用的资源，注意顺序不能颠倒
     *
     * @param result     结果集
     * @param statement  执行SQL语句的Statement对象
     * @param connection 连接数据库的连接对象
     */
    public static void release(ResultSet result, Statement statement, Connection connection) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            JDBCUtils.release(connection);
        }
    }

    /**
     * 关闭与数据库的连接资源，包括‘Statement’‘Connection’ 的实例对象所占用的资源，注意顺序不能颠倒
     *
     * @param statement  执行SQL语句的Statement对象
     * @param connection 连接数据库的连接对象
     */
    public static void release(Statement statement, Connection connection) {

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            JDBCUtils.release(connection);
        }
    }

}
