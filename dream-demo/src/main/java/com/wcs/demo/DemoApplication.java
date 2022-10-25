package com.wcs.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: wcs
 * @create: 2022-10-09 16:12
 * @description:
 */
@SpringBootApplication(scanBasePackages = {"com.wcs"})
@EnableFeignClients(basePackages = {"com.wcs.api.**.feign"})
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
