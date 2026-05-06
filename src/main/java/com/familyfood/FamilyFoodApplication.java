package com.familyfood;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan(basePackages = "com.familyfood", annotationClass = Mapper.class)
@SpringBootApplication
public class FamilyFoodApplication {
    private static final Logger log = LoggerFactory.getLogger(FamilyFoodApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FamilyFoodApplication.class, args);
        log.info("family_food_backend_started");
    }
}
