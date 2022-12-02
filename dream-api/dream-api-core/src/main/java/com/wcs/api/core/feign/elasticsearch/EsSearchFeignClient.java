package com.wcs.api.core.feign.elasticsearch;


import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.vo.PageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 索引操作
 */
@FeignClient(value = "dream-core",contextId = "allSearch")
public interface EsSearchFeignClient {

    /**
     * 查询所有数据列表
     */
    @GetMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/search/getPage")
    ResponseResult<PageVO> getPage(@RequestBody EsSearchParamVo searchParamVo);
}
