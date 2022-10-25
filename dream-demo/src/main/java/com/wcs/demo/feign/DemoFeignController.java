package com.wcs.demo.feign;

import com.wcs.api.demo.feign.DemoFeignClient;
import com.wcs.common.response.ResponseResult;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: wcs
 * @create: 2022-10-09 16:09
 * @description:
 */
@RestController
public class DemoFeignController implements DemoFeignClient {
    @Override
    public ResponseResult<String> demo(String args) {
        return ResponseResult.success(args);
    }
}
