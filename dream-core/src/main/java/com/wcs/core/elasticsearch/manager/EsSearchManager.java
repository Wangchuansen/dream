package com.wcs.core.elasticsearch.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

import com.wcs.api.core.vo.EsIndexEnum;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.vo.PageVO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class EsSearchManager extends EsIndexManager{

    public PageVO getPageSearch(EsSearchParamVo searchParamVo) {
        List<Integer> moduleIdList = searchParamVo.getModuleIdList();
        List<String>indexNames = new ArrayList<>();
        for (Integer id : moduleIdList) {
            EsIndexEnum esIndexEnum = EsIndexEnum.getId(id);
            if (esIndexEnum != null) {
                indexNames.add(esIndexEnum.value());
            }
        }
        if (CollectionUtil.isEmpty(indexNames)){
            return new PageVO();
        }

        //查找索引库
        SearchRequest searchRequest = new SearchRequest(indexNames.toArray(new String[]{}));

        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 分页
        builderPage(searchSourceBuilder,searchParamVo);
        // 排序
        searchSourceBuilder.sort("createTime", SortOrder.DESC);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        AbstractQueryBuilder matchQueryBuilder = builderQuery(searchParamVo);
        if(matchQueryBuilder != null) {
            searchSourceBuilder.query(matchQueryBuilder);
        }

        // 3.添加条件到请求
        searchRequest.source(searchSourceBuilder);
        // 4.客户端查询请求
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builderPageResult(searchParamVo,search);
    }

    /**
     * 构建查询条件
     * @return
     */
    @Override
    public AbstractQueryBuilder builderQuery(EsSearchParamVo searchParamVo){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolean searchBr = false;
        if (StrUtil.isNotEmpty(searchParamVo.getName())) {

            BoolQueryBuilder shouldQ= QueryBuilders.boolQuery();

            /**========================== todo case,scheme,scene,policy,whitebook,opportunity =============================*/
            super.addTitleQuery(shouldQ,searchParamVo);
            super.addIntroQuery(shouldQ,searchParamVo);


            /**========================== todo isv =============================*/
//            if (searchParamVo.getModuleIdList().contains(EsIndexEnum.ISV_INDEX.id())) {
//                super.addCompanyNameQuery(shouldQ,searchParamVo);
//            }
//
//
//            /**========================== todo opportunity =============================*/
//            if (searchParamVo.getModuleIdList().contains(EsIndexEnum.OPPORTUNITY_INDEX.id())) {
//                super.addTendereeQuery(shouldQ,searchParamVo);
//                super.addDetailQuery(shouldQ,searchParamVo);
//            }
            /**=======================================================*/

            super.addNameLabelQuery(shouldQ,searchParamVo);

            boolQueryBuilder.must(shouldQ);
            searchBr = true;
        }

        //不显示类型
        if (CollectionUtil.isNotEmpty(searchParamVo.getNoShowTypeIds())){
            TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("showTypeId",searchParamVo.getShowTypeIds().toArray(new Integer[]{}));
            boolQueryBuilder.mustNot(termQueryBuilder);
            searchBr = true;
        }

        //不显示状态
        if (CollectionUtil.isNotEmpty(searchParamVo.getNoApplyStatus())){
            TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("applyStatus",searchParamVo.getNoApplyStatus().toArray(new Integer[]{}));
            boolQueryBuilder.mustNot(termQueryBuilder);
            searchBr = true;
        }

        searchBr = super.addMustNoIdQuery(boolQueryBuilder,searchParamVo,searchBr);

        searchBr = super.addLabelIdQuery(boolQueryBuilder,searchParamVo,searchBr);
        return searchBr?boolQueryBuilder:null;
    }

}
