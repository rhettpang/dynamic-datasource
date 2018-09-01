package com.pk.dynamic.datasource.gray;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({DataSourceConfig.class,DBProperties.class,RefreshConfigController.class})
public @interface EnableDynamicDataSource {
}
