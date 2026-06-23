package com.hrrecruit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.hrrecruit.mapper")   // 扫描Mapper接口
@EnableCaching                       // 开启缓存
@EnableAsync                         // 开启异步任务
public class HrRecruitApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrRecruitApplication.class, args);
    }
}