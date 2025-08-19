package org.example.stock_demo;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.stock_demo.mapper")
public class StockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDemoApplication.class, args);
    }

}
