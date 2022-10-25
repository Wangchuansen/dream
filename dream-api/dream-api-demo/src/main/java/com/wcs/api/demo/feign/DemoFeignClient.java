package com.wcs.api.demo.feign;

import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: wcs
 * @create: 2022-10-09 16:06
 * @description:
 */
@FeignClient(value = "dream-demo",contextId = "demoClient")
public interface DemoFeignClient {

    @PostMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/demo")
    ResponseResult<String> demo(@RequestParam("args") String args);
}
