package com.wcs.demo.controller;

import com.wcs.api.demo.feign.DemoFeignClient;
import com.wcs.common.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: wcs
 * @create: 2022-10-09 13:19
 * @description:
 */
@RestController
public class DemoController {

    @Qualifier("com.wcs.api.demo.feign.DemoFeignClient")
    @Autowired
    private DemoFeignClient demoFeignClient;

    @GetMapping("/demo")
    public ResponseResult<String> demo(){
        return ResponseResult.success("hello");
    }

    @PostMapping("/feign")
    public ResponseResult<String> demoFeign(String args) {
        return demoFeignClient.demo(args);
    }
}
