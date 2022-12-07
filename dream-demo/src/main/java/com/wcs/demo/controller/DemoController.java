package com.wcs.demo.controller;

import com.wcs.api.core.feign.elasticsearch.EsIndexCaseFeignClient;
import com.wcs.api.core.vo.search.EsCaseVO;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.api.demo.feign.DemoFeignClient;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    @Autowired
    private EsIndexCaseFeignClient esIndexCaseFeignClient;

    @GetMapping("/demo")
    public ResponseResult<String> demo(){
        return ResponseResult.success("hello");
    }

    @PostMapping("/feign")
    public ResponseResult<String> demoFeign(String args) {
        return demoFeignClient.demo(args);
    }

    @PostMapping("saveIndex")
    public ResponseResult saveIndex(@RequestBody EsCaseVO esCaseVO){
        esIndexCaseFeignClient.saveCaseIndex(esCaseVO);
        return ResponseResult.success();
    }

    @GetMapping("/es")
    public ResponseResult esSearch(@Valid EsSearchParamVo searchParamVo){
        ResponseResult<PageVO<EsCaseVO>> page = esIndexCaseFeignClient.getPage(searchParamVo);
        return ResponseResult.success(page);
    }
}
