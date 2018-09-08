package dynamic.datasource.gray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshConfigController {

    private static final Logger logger = LoggerFactory.getLogger(RefreshConfigController.class);

    @GetMapping("/dynamic/datasource/refresh")
    public String refreshDefaultDatasource(String dbName){
        logger.info("refreshDefaultDatasource dbName = ",dbName);
        DynamicDataSourceHolder.putDataSource(dbName);
        return "success";
    }
}
