// src/main/java/com/ddl/manager/domain/statistics/dto/ApiStatistics.java
package com.ddl.manager.domain.statistics.dto;

import lombok.Data;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ApiStatistics {
    private String apiName;
    private String apiPath;
    private AtomicInteger callCount = new AtomicInteger(0);
    private long firstCallTime;
    private long lastCallTime;

    public ApiStatistics(String apiName, String apiPath) {
        this.apiName = apiName;
        this.apiPath = apiPath;
        this.firstCallTime = System.currentTimeMillis();
        this.lastCallTime = System.currentTimeMillis();
    }
}