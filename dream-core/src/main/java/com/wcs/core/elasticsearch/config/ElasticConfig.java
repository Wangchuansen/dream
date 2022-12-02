package com.wcs.core.elasticsearch.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;


/**
 * @author wcs
 */
@RefreshScope
@Configuration
@Data
public class ElasticConfig {

    @Value("${elastic.address}")
    private String elastic_address;

    @Value("${elastic.port}")
    private Integer elastic_port;
}
