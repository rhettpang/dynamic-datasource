package com.pk.dynamic.datasource;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({DataSourceAspect.class,DataSourceConfig.class,DBProperties.class})
public @interface EnableDynamicDataSource {
}
