package com.pk.dynamic.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private DBProperties properties;

    private static final String KEY_MASTER = "master";

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        //按照目标数据源名称和目标数据源对象的映射存放在Map中
        Map<Object, Object> targetDataSources = new HashMap<>();
        //获取配置文件中的数据源
        Map<String, HikariDataSource> hikaris = properties.getHikari();
        Set<String> keys = hikaris.keySet();
        HikariDataSource hikariDataSource = null;
        HikariDataSource masterDataSource = null;
        String poolName = "";
        for (String key : keys){
            hikariDataSource = hikaris.get(key);
            poolName = hikariDataSource.getPoolName();
            targetDataSources.put(hikariDataSource.getPoolName(),hikariDataSource);
            if (poolName.equals(KEY_MASTER)){
                masterDataSource = hikariDataSource;
            }
        }

        DynamicDataSource dataSource = new DynamicDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        //设置默认的数据源，当拿不到数据源时，使用此配置
        if (null != masterDataSource){
            dataSource.setDefaultTargetDataSource(masterDataSource);
        }else {
            logger.error("Can't find master db, project will be exit");
            System.exit(0);
        }
        return dataSource;
    }

    @Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
