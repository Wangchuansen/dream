package com.wcs.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author wcs
 */
@SpringBootApplication(scanBasePackages = { "com.wcs" })
@EnableFeignClients(basePackages = {"com.wcs.api.**.feign"})
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
