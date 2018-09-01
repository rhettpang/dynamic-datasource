package com.pk.dynamic.datasource.gray;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshConfigController {

    @GetMapping("/dynamic/datasource/refresh")
    public String refreshDefaultDatasource(String dbName){
        System.out.println("refreshDefaultDatasource dbName = " + dbName);
        DynamicDataSourceHolder.putDataSource(dbName);
        return "success";
    }
}
