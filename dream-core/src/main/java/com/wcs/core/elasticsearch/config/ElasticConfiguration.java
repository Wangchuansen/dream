package com.wcs.core.elasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wcs
 */
@Configuration
public class ElasticConfiguration {

    @Autowired
    private ElasticConfig elasticConfig;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        System.out.println("---------->"+elasticConfig.getElastic_address());
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(elasticConfig.getElastic_address(), elasticConfig.getElastic_port(), "http")));
    }

}
