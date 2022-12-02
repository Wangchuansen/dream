package com.wcs.core.leaf.feign;

import com.wcs.api.core.feign.leaf.SegmentFeignClient;
import com.wcs.common.response.ResponseResult;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: wcs
 * @create: 2022-09-08 15:32
 * @description:
 */
@RestController
public class SegmentFeignController implements SegmentFeignClient {
    @Override
    public ResponseResult<Long> getSegmentId(String key) {
        return null;
    }
}
