package com.ddl.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DDL管理系统启动类
 * @author zhenghaipei
 * @since 2025-12-13
 */
@EnableScheduling
@SpringBootApplication
public class DdlApplication {

    public static void main(String[] args) {
        SpringApplication.run(DdlApplication.class, args);
    }
}
