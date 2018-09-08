package dynamic.datasource.gray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author Created by pangkunkun on 2018/8/28.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    private static final Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);
    /**
     * 数据源路由，此方用于产生要选取的数据源逻辑名称
     */
    @Override
    protected Object determineCurrentLookupKey() {
        Object datasource = DynamicDataSourceHolder.getDataSource();
        logger.info("use datasource is {} " ,datasource);
        return datasource;
    }
}
