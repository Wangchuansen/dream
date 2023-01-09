package com.wcs.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author: wcs
 * @create: 2023-01-09 9:25
 * @description:
 */
@Configuration
@RefreshScope
@Data
public class LoginConfig {

    /**是否允许后四位登录*/
    @Value("${login.is-last-four}")
    private Boolean isLastFour;
}
