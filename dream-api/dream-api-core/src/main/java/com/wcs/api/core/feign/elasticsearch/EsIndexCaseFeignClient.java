package com.wcs.api.core.feign.elasticsearch;

import com.wcs.api.core.vo.search.EsCaseVO;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.vo.PageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 索引操作
 */
@FeignClient(value = "dream-core",contextId = "indexCase")
public interface EsIndexCaseFeignClient {

    /**
     * 存储案例索引
     */
    @PutMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/case/saveCaseIndex")
    ResponseResult<String> saveCaseIndex(@RequestBody EsCaseVO esCaseVO);

    /**
     * 案例列表
     */
    @GetMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/case/getPage")
    ResponseResult<PageVO<EsCaseVO>> getPage(@RequestBody EsSearchParamVo searchParamVo);

    /**
     * 删除文档
     */
    @DeleteMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/case/deleteDocument")
    ResponseResult<String> deleteDocument(@RequestParam("id")String id);

    /**
     * 批量存储索引
     */
    @PutMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/case/bulk")
    ResponseResult<String> bulk(@RequestBody List<EsCaseVO> list);

    /**
     * 统计数量
     */
    @GetMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/case/count")
    ResponseResult<Long> count(@RequestBody EsSearchParamVo searchParamVo);
}
