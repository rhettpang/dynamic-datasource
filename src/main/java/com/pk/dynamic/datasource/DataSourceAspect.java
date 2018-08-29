package com.pk.dynamic.datasource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Component
@Aspect
public class DataSourceAspect {

    private final static Logger log= LoggerFactory.getLogger(DataSourceAspect.class);

    @Value("${slave.hosts:slave}")
    private String slaveHosts;

    /**
     * 这里用来拦截有{@link TargetDataSource}注解的方法
     * */
    @Before("@annotation(targetDataSource)")
    public void before(JoinPoint joinPoint ,TargetDataSource targetDataSource) {
        try {
            String dataSourceName = targetDataSource.value();
            //判断指定的数据源类型，如果是slave，则调用LB方法，随机分配slave数据库
            if (dataSourceName.equals("slave")){
                dataSourceName = DBLoadBalance.getDBWithRandom(slaveHosts);
            }
            //设置要使用的数据源
            DynamicDataSourceHolder.putDataSource(dataSourceName);
            log.debug("current thread " + Thread.currentThread().getName() + " add " + dataSourceName + " to ThreadLocal");
        } catch (Exception e) {
            log.error("current thread " + Thread.currentThread().getName() + " add data to ThreadLocal error", e);
        }
    }

    /**
     * 执行完切面后，将线程共享中的数据源名称清空
     * */
    @After("@annotation(TargetDataSource)")
    public void after(JoinPoint joinPoint){
        DynamicDataSourceHolder.removeDataSource();
    }

}
