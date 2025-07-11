package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement
public class SkyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("项目启动成功！接口文档地址：{}", "http://localhost:8080/doc.html");
    }
}
