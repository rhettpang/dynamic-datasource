package dynamic.datasource.core;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
public class DynamicDataSourceHolder {

    private static  String USEFUL_DB = null;
    /**
     * 本地线程共享对象
     */
    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    public static void putDataSource(String name) {
        THREAD_LOCAL.set(name);
    }

    public static String getDataSource() {
        if (null != USEFUL_DB){
            return USEFUL_DB;
        }
        return THREAD_LOCAL.get();
    }

    public static void removeDataSource() {
        THREAD_LOCAL.remove();
    }

}
