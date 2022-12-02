package com.wcs.core.elasticsearch.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.wcs.api.core.vo.EsIndexEnum;
import com.wcs.api.core.vo.search.EsCaseVO;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.vo.PageVO;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class EsCaseManager extends EsIndexManager{

    /**
     * 查询案例分页数据
     * @param searchParamVo
     */
    public PageVO<EsCaseVO> getPageSearch(EsSearchParamVo searchParamVo){
        if (StrUtil.isNotEmpty(searchParamVo.getName())) {
            searchParamVo.getNameStrSet().add(searchParamVo.getName());
        }
        // 匹配查询
        AbstractQueryBuilder matchQueryBuilder = builderQuery(searchParamVo);
        // 4.客户端查询请求
        SearchResponse search = getSearchResponse(searchParamVo, EsIndexEnum.CASE_INDEX.value(),matchQueryBuilder);
        return builderPageResult(searchParamVo,search,EsCaseVO.class);
    }

    /**
     * 构建查询条件
     * @return
     */
    @Override
    public AbstractQueryBuilder builderQuery(EsSearchParamVo searchParamVo){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolean searchBr = super.builderQuery(searchParamVo,boolQueryBuilder,EsIndexEnum.CASE_INDEX.idName());
        return searchBr?boolQueryBuilder:null;
    }

    /**
     * id查询
     * @param ids
     */
    public List<EsCaseVO> getAllSearch(Set<String> ids) {
        return super.getAllSearch(ids, EsIndexEnum.CASE_INDEX.value(), EsCaseVO.class);
    }

    /**
     * 存储文档
     * @param esCaseVO
     */
    public void saveIndexDocument(EsCaseVO esCaseVO) {
        esCaseVO.setIntro(com.wcs.common.util.StrUtil.delHtmlTags(esCaseVO.getIntro()));
        super.saveIndexDocument(String.valueOf(esCaseVO.getCaseId()),EsIndexEnum.CASE_INDEX.value(),esCaseVO);
    }

    /**
     * 删除文档
     * @param id 主键
     */
    public DeleteResponse deleteDocument(String id){
        return super.deleteDocument(id,EsIndexEnum.CASE_INDEX.value());
    }

    /**
     * 批量存储文档
     */
    public RestStatus bulk(List<EsCaseVO>list) {
        if (CollectionUtil.isEmpty(list)){
            return null;
        }
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        // 批量请求处理
        list.forEach(s->{
            s.setIntro(com.wcs.common.util.StrUtil.delHtmlTags(s.getIntro()));
            bulkRequest.add(new IndexRequest(EsIndexEnum.CASE_INDEX.value())
                            .id(s.getCaseId().toString())
                            .source(JSON.toJSONString(s), XContentType.JSON)
            );
        });
        return bulk(bulkRequest);
    }
}
