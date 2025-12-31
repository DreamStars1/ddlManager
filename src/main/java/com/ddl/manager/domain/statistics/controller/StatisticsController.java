// src/main/java/com/ddl/manager/domain/statistics/controller/StatisticsController.java
package com.ddl.manager.domain.statistics.controller;

import com.ddl.manager.domain.statistics.dto.ApiStatistics;
import com.ddl.manager.domain.statistics.service.StatisticsService;
import com.ddl.manager.shared.dto.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    // 无需登录即可访问的统计页面
    @GetMapping
    public String statisticsPage(Model model) {
        List<ApiStatistics> stats = statisticsService.getAllStatistics();
        model.addAttribute("statistics", stats);
        return "statistics"; // 对应前端模板
    }

    // 提供API接口供前端刷新数据
    @GetMapping("/api/data")
    @ResponseBody
    public AjaxResult getStatisticsData() {
        return AjaxResult.ok(statisticsService.getAllStatistics());
    }
}