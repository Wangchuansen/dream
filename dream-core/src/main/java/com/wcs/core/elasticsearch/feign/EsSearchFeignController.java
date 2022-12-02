package com.wcs.core.elasticsearch.feign;


import com.wcs.api.core.feign.elasticsearch.EsSearchFeignClient;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.vo.PageVO;
import com.wcs.core.elasticsearch.manager.EsSearchManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author wcs
 */
@RestController
@Slf4j
public class EsSearchFeignController implements EsSearchFeignClient {
	@Autowired
	private EsSearchManager esSearchManager;

	@Override
	public ResponseResult<PageVO> getPage(EsSearchParamVo searchParamVo) {
		PageVO result = esSearchManager.getPageSearch(searchParamVo);
		return ResponseResult.success(result);
	}

}
