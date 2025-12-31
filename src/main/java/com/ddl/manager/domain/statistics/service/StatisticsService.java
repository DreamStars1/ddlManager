// src/main/java/com/ddl/manager/domain/statistics/service/StatisticsService.java
package com.ddl.manager.domain.statistics.service;

import com.ddl.manager.domain.statistics.dto.ApiStatistics;
import java.util.List;

public interface StatisticsService {
    void incrementCallCount(String apiName, String apiPath);
    List<ApiStatistics> getAllStatistics();
}