package com.wcs.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: wcs
 * @create: 2022-08-12 16:12
 * @description:
 */
@SpringBootApplication(scanBasePackages = {"com.wcs"})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
