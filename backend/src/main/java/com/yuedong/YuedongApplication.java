package com.yuedong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuedong.mapper")
public class YuedongApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuedongApplication.class, args);
    }
}
