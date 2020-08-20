package com.xh.publicgoods;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PublicGoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicGoodsApplication.class, args);
    }

}
