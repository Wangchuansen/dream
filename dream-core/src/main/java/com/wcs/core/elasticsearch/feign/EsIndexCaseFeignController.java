package com.wcs.core.elasticsearch.feign;

import com.wcs.api.core.feign.elasticsearch.EsIndexCaseFeignClient;
import com.wcs.api.core.vo.EsIndexEnum;
import com.wcs.api.core.vo.search.EsCaseVO;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.vo.PageVO;
import com.wcs.core.elasticsearch.manager.EsCaseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author wcs
 */
@RestController
@Slf4j
public class EsIndexCaseFeignController implements EsIndexCaseFeignClient {
	@Autowired
	private EsCaseManager esCaseManager;

	@Override
	public ResponseResult<String> saveCaseIndex(EsCaseVO esCaseVO) {
		esCaseManager.saveIndexDocument(esCaseVO);
		return ResponseResult.success();
	}

	@Override
	public ResponseResult<PageVO<EsCaseVO>> getPage( EsSearchParamVo searchParamVo) {
		PageVO<EsCaseVO> result = esCaseManager.getPageSearch(searchParamVo);
		return ResponseResult.success(result);
	}

	@Override
	public ResponseResult<String> deleteDocument(String id) {
		esCaseManager.deleteDocument(id);
		return ResponseResult.success();
	}

	@Override
	public ResponseResult<String> bulk(List<EsCaseVO> list) {
		esCaseManager.bulk(list);
		return ResponseResult.success();
	}

	@Override
	public ResponseResult<Long> count(EsSearchParamVo searchParamVo) {
		return ResponseResult.success(esCaseManager.getCount(EsIndexEnum.CASE_INDEX.value(),searchParamVo));
	}
}
