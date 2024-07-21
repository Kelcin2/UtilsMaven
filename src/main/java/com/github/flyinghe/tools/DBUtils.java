package com.github.flyinghe.tools;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DAO (DATA ACCESS OBJECT)。
 *
 * @author Flying
 * @see JDBCUtils
 */
public class DBUtils {

    /**
     * 用于查询SQL语句中返回的记录中的第一个字段的值,适用于单条记录单个字段的查询
     *
     * @param sql 查询SQL语句
     * @param arg 传入的占位符的参数
     * @return 返回查询的记录的第一个字段的值，若有多个记录符合查询条件，则返回第一条记录的第一个字段的值，若没有符合条件的记录，则返回NULL
     */
    public static <T> T getValueOfCertainColumnOfCertainRecord(String sql, Object... arg) {
        List<T> list = DBUtils.getValueOfCertainColumnOfAllRecord(sql, arg);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 用于查询SQL语句中返回的所有记录中的第一个字段的值,适用于多条记录单个字段的查询，将每条记录的该字段存入List中
     *
     * @param sql 查询SQL语句
     * @param arg 传入的占位符的参数
     * @return 返回查询到的所有记录的第一个字段的值，若没有符合条件的记录，则返回空List
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getValueOfCertainColumnOfAllRecord(String sql, Object... arg) {
        Connection connection = JDBCUtils.getConnection();
        PreparedStatement ps = null;

        ResultSet result = null;
        List<T> list = new ArrayList<T>();// 存放查询到的记录的字段的值
        try {
            ps = connection.prepareStatement(sql);
            // 填充占位符
            for (int i = 0; i < arg.length; i++) {
                ps.setObject(i + 1, arg[i]);
            }
            // 获取结果集
            result = ps.executeQuery();
            // 循环遍历结果集中的记录，并将每条记录的第一个字段放入List中
            while (result.next()) {
                list.add((T) result.getObject(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            JDBCUtils.release(result, ps, connection);
        }

        return list;
    }

    /**
     * 查找所有符合查找条件的记录，并包装成相应对象返回，传入的T类的定义必须符合JavaBean规范。
     *
     * @param clazz 需要查找的对象的所属类的一个类（Class）
     * @param sql   只能是SELECT语句,SQL语句中的字段别名必须与T类里的相应属性相同
     * @param arg   SQL语句中的参数占位符参数
     * @return 将所有符合条件的记录包装成相应对象，并返回所有对象的集合,若没有记录将返回一个空数组
     */
    public static <T> List<T> getList(Class<T> clazz, String sql, Object... arg) {

        Connection connection = JDBCUtils.getConnection();
        List<T> list = null;// 存放返回的集合对象
        // 存放所有获取的记录的Map对象
        List<Map<String, Object>> listProperties = new ArrayList<Map<String, Object>>();

        PreparedStatement ps = null;
        ResultSet result = null;

        // 填充占位符

        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < arg.length; i++) {
                ps.setObject(i + 1, arg[i]);
            }

            // 执行SQL语句
            result = ps.executeQuery();

            // 获取结果集中所有记录
            listProperties = handleResultSetToMapList(result, listProperties);

            // 获取每条记录中相应的对象
            list = DBUtils.transformMapListToBeanList(clazz, listProperties);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.release(result, ps, connection);
        }

        return list;
    }

    /**
     * 本函数所查找的结果只能返回一条记录，若查找到的多条记录符合要求将返回第一条符合要求的记录
     *
     * @param sql 传入的SQL 语句
     * @param arg 占位符参数
     * @return 将查找到的的记录包装成一个Map对象，并返回该Map,若没有记录则返回null
     */
    public static Map<String, Object> getMap(String sql, Object... arg) {
        List<Map<String, Object>> list = DBUtils.getListMap(sql, arg);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 把查询结果里的每条记录包装成一个map对象，并将所有map对象放入传入的数组中
     *
     * @param sql 传入的SQL 语句
     * @param arg 占位符参数
     * @return 返回一个包含所有记录的map对象的的集合数组，若没有记录，则返回一个空数组
     */
    public static List<Map<String, Object>> getListMap(String sql, Object... arg) {
        Connection connection = JDBCUtils.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < arg.length; i++) {
                ps.setObject(i + 1, arg[i]);
            }

            result = ps.executeQuery();
            listMap = DBUtils.handleResultSetToMapList(result, listMap);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.release(result, ps, connection);
        }
        return listMap;
    }

    /**
     * 将所有记录包装成T类对象，并将所有T类对象放入一个数组中并返回，传入的T类的定义必须符合JavaBean规范，
     * 因为本函数是调用T类对象的setter器来给对象赋值的
     *
     * @param clazz          T类的类对象
     * @param listProperties 所有记录
     * @return 返回所有被实例化的对象的数组，若没有对象，返回一个空数组
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static <T> List<T> transformMapListToBeanList(Class<T> clazz, List<Map<String, Object>> listProperties)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        // 存放被实例化的T类对象
        T bean = null;
        // 存放所有被实例化的对象
        List<T> list = new ArrayList<T>();
        // 将实例化的对象放入List数组中
        if (listProperties.size() != 0) {

            Iterator<Map<String, Object>> iter = listProperties.iterator();
            while (iter.hasNext()) {
                // 实例化T类对象
                bean = clazz.newInstance();
                // 遍历每条记录的字段，并给T类实例化对象属性赋值
                for (Map.Entry<String, Object> entry : iter.next().entrySet()) {
                    BeanUtils.setProperty(bean, entry.getKey(), entry.getValue());
                }
                // 将赋过值的T类对象放入数组中
                list.add(bean);
            }
        }
        return list;
    }

    /**
     * 把结果集中的每条记录包装成一个map对象，并将所有map对象放入传入的数组中
     *
     * @param result         结果集
     * @param listProperties 一个List对象
     * @return 返回一个包含所有记录的map对象的的集合数组，若没有记录，则返回一个空数组
     * @throws SQLException
     */
    private static List<Map<String, Object>> handleResultSetToMapList(ResultSet result,
                                                                      List<Map<String, Object>> listProperties)
            throws SQLException {
        // 存放记录的信息
        Map<String, Object> property = null;

        // 获取结果集中的列名
        List<String> listColumns = DBUtils.getColumnLabels(result);

        // 存放记录
        while (result.next()) {
            property = new HashMap<String, Object>();
            // 将每条记录的列以及相应的列值存入一个Map对象中
            for (int i = 0; i < listColumns.size(); i++) {
                property.put(listColumns.get(i), result.getObject(i + 1));
            }
            // 将存入记录的每个map对象放入数组中
            listProperties.add(property);
        }
        return listProperties;
    }

    /**
     * 此函数获取结果集中的所有列名，将列名放入数组中并返回该数组
     *
     * @param result 结果集
     * @return 返回存放结果集中所有列名的数组，若为空，则返回空数组
     * @throws SQLException
     */
    private static List<String> getColumnLabels(ResultSet result) throws SQLException {
        // 存放结果集中的列名
        List<String> listColumns = new ArrayList<String>();
        // 获取结果集元数据
        ResultSetMetaData rsmd = result.getMetaData();
        // 将字段名放入数组中
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            listColumns.add(rsmd.getColumnLabel(i + 1));
        }

        return listColumns;
    }

    /**
     * 本函数所查找的结果只能返回一条记录，若查找到的多条记录符合要求将返回第一条符合要求的记录
     *
     * @param clazz 需要查找的对象的所属类的一个类（Class）
     * @param sql   只能是SELECT语句,SQL语句中的字段别名必须与T类里的相应字段相同
     * @param arg   SQL语句中的参数占位符参数
     * @return 将查找到的的记录包装成一个对象，并返回该对象,若没有记录则返回null
     */
    public static <T> T get(Class<T> clazz, String sql, Object... arg) {
        List<T> list = DBUtils.getList(clazz, sql, arg);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 传入的SQL语句只能是INSERT,DELETE,UPDATE和DDL语句
     *
     * @param sql 相应SQL语句
     * @param arg 传入的占位符的参数
     */
    public static void update(String sql, Object... arg) {
        Connection connection = JDBCUtils.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < arg.length; i++) {
                ps.setObject(i + 1, arg[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JDBCUtils.release(ps, connection);
    }

    /**
     * 该方法用于批处理，当需要一次执行多条相同SQL语句时使用该方法时效率较高
     *
     * @param sql 需要执行的SQL语句
     * @param arg 传入SQL语句所需要的占位符参数，下标均从0开始，每个一维下标对应于SQL语句的一组占位符参数
     */
    public static void updateBatch(String sql, Object[][] arg) {
        Connection connection = JDBCUtils.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < arg.length; i++) {
                for (int j = 0; j < arg[i].length; j++) {
                    ps.setObject(j + 1, arg[i][j]);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JDBCUtils.release(ps, connection);
    }

    /**
     * 用于查询记录中大数据类型值，若有多条记录符合要求则返回第一条记录的大数据
     *
     * @param sql 查询SQL语句，查询字段的类型必须为Blob类型
     * @param arg 传入的占位符的参数
     * @return 返回查询的大数据，封装在Map中，若没有符合条件的记录则返回空Map
     */
    public static Map<String, byte[]> getBigData(String sql, Object... arg) {
        Map<String, byte[]> bigDataMap = new HashMap<String, byte[]>();
        Connection connection = JDBCUtils.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;

        // 填充占位符
        try {
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < arg.length; i++) {
                ps.setObject(i + 1, arg[i]);
            }

            // 执行SQL语句
            result = ps.executeQuery();
            // 获取字段名
            List<String> columnList = DBUtils.getColumnLabels(result);
            // 遍历结果集
            while (result.next()) {
                // 遍历字段名获取相应大数据值
                for (String column : columnList) {
                    Blob data = result.getBlob(column);
                    byte[] datas = data.getBytes(1, (int) data.length());
                    bigDataMap.put(column, datas);
                }
                break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.release(result, ps, connection);
        }

        return bigDataMap;
    }
}
