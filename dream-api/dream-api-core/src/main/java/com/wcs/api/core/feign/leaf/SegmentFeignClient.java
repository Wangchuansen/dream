package com.wcs.api.core.feign.leaf;

import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: wcs
 * @create: 2022-09-08 15:26
 * @description:
 */
@FeignClient(value = "dream-core",contextId ="segment")
public interface SegmentFeignClient {

    /**
     * 获取id
     * @param key
     * @return
     */
    @GetMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/segment")
    ResponseResult<Long> getSegmentId(@RequestParam("key") String key);


}
