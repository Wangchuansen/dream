package com.wcs.core.minio;

import com.wcs.common.config.OssConfig;
import com.wcs.common.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: wcs
 * @create: 2022-08-14 16:20
 * @description:
 */
@RestController
@RequestMapping("/oss")
@Slf4j
public class OssController {

    @Autowired
    private OssConfig ossConfig;

    @GetMapping("/demo")
    public void demo(){
        log.info(ossConfig.getResourcesUrl());
    }
}
