package com.github.flyinghe.tools;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * Created by FlyingHe on 2016/7/14.
 * MyBatis的工具类,MyBatis的配置文件需要在类目录下,名为"mybatis.xml"
 *
 * @author FlyingHe
 */
public class MyBatisUtils {

    private static SqlSessionFactory sqlSessionFactory;
    //与线程相关的SqlSession，保证线程安全
    private static ThreadLocal<SqlSession> tl_sqlSession = new ThreadLocal<SqlSession>();
    private static boolean hasMybatisConfigFile = false;

    static {

        try {
            //读取Mybatis的配置文件
            Reader reader = Resources.getResourceAsReader("mybatis.xml");
            //创建SqlSessionFactory对象
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            hasMybatisConfigFile = true;
        } catch (IOException e) {
            hasMybatisConfigFile = false;
        }

    }

    /**
     * 禁止直接new该类对象
     */
    private MyBatisUtils() {}

    /**
     * 获取一个与当前线程绑定的SqlSession对象
     *
     * @return 返回一个与当前线程绑定的SqlSession对象
     */
    public static SqlSession getSqlSession() {
        if (!hasMybatisConfigFile) {
            throw new RuntimeException("在类路径下没有读取到您的Mybatis配置文件");
        }
        SqlSession sqlSession = tl_sqlSession.get();
        if (sqlSession == null) {
            //若当前线程中没有SqlSession则创建一个
            sqlSession = sqlSessionFactory.openSession();
            //把新的SqlSession绑定到当前线程
            tl_sqlSession.set(sqlSession);
        }
        return sqlSession;
    }

    /**
     * 关闭SqlSession对象，并与当前线程解除绑定,若当前线程中没有SqlSession对象则不执行任何操作
     */
    public static void closeSqlSession() {
        SqlSession sqlSession = tl_sqlSession.get();
        if (sqlSession != null) {
            //关闭SqlSession对象
            sqlSession.close();
            //与当前线程解绑
            tl_sqlSession.remove();
        }

    }

    /**
     * 此方法用于Mybatis的逆向工程
     *
     * @param configFilePath 逆向工程的配置文件路径
     * @param overwrite      若自动生成的文件在配置文件中配置的路径下有同名文件是否重写
     * @param warnings       逆向工程执行期间产生的警告信息，存入此参数中
     * @throws Exception
     */
    public static void myBatisAutoGenerator(String configFilePath, boolean overwrite, List<String> warnings)
            throws Exception {
        File configFile =
                new File(configFilePath);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
    }
}
