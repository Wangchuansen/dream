package com.wcs.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: wcs
 * @create: 2022-08-14 16:19
 * @description:
 */
@SpringBootApplication(scanBasePackages = { "com.wcs" })
@EnableFeignClients(basePackages = {"com.wcs.api.**.feign"})
public class CoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }
}
