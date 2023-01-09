package com.wcs.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: wcs
 * @create: 2022-12-28 13:45
 * @description:
 */
@SpringBootApplication(scanBasePackages = { "com.wcs" },
        exclude = {org.activiti.spring.boot.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@EnableFeignClients(basePackages = {"com.wcs.api.**.feign"})
public class ActivitiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitiApplication.class, args);
    }

}
