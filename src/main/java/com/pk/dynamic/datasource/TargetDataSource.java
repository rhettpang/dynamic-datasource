package com.pk.dynamic.datasource;

import java.lang.annotation.*;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
@Inherited
public @interface TargetDataSource {
    //此处接收的是数据源的名称
    String value();
}
