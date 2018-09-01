package com.pk.dynamic.datasource.gray;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Component
@ConfigurationProperties(prefix = "dynamic")
public class DBProperties {
    //一次性从配置文件中读取所有数据源的配置
    private Map<String, HikariDataSource> hikari;

    public Map<String, HikariDataSource> getHikari() {
        return hikari;
    }

    public void setHikari(Map<String, HikariDataSource> hikari) {
        this.hikari = hikari;
    }
}
