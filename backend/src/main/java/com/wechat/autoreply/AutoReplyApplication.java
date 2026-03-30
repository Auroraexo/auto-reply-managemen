package com.wechat.autoreply;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 微信公众号自动回复管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.wechat.autoreply.mapper")
@EnableScheduling
public class AutoReplyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoReplyApplication.class, args);
        System.out.println("========================================");
        System.out.println("微信公众号自动回复管理系统启动成功！");
        System.out.println("API 文档地址：http://localhost:8080/api/doc.html");
        System.out.println("========================================");
    }
}
