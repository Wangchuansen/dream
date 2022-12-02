package com.wcs.core.elasticsearch.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.wcs.api.core.vo.EsIndexEnum;
import com.wcs.api.core.vo.search.EsSearchParamVo;
import com.wcs.common.vo.PageVO;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class EsIndexManager {

    @Autowired
    protected RestHighLevelClient restHighLevelClient;

    /**
     * 索引查询
     * @param indices 库名
     * @param matchQueryBuilder 匹配查询
     */
    protected SearchResponse getSearchResponse(EsSearchParamVo searchParamVo,
                                               String indices, AbstractQueryBuilder matchQueryBuilder){
        // 1.创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(indices);
        // 2.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置高亮
//        searchSourceBuilder.highlighter(new HighlightBuilder());
        // 分页
        builderPage(searchSourceBuilder,searchParamVo);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // (3)条件投入
        if (matchQueryBuilder != null) {
            searchSourceBuilder.query(matchQueryBuilder);
        }
        //构建排序
        this.builderSort(searchSourceBuilder,searchParamVo);
        // 3.添加条件到请求
        searchRequest.source(searchSourceBuilder);
        // 4.客户端查询请求
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return search;
    }

    /**
     * 索引查询
     * @param indices 库名
     * @param matchQueryBuilder 匹配查询
     */
    protected CountResponse getCountSearch(String indices, AbstractQueryBuilder matchQueryBuilder){
        // 设置索引名
        CountRequest countRequest = new CountRequest(indices);
        // (3)条件投入
        if (matchQueryBuilder != null) {
            countRequest.query(matchQueryBuilder);
        }
        // 4.客户端查询请求
        CountResponse countResponse = null;
        try {
            countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countResponse;
    }

    /**
     * 统计数量
     * @param indices
     * @param searchParamVo
     */
    public Long getCount(String indices,EsSearchParamVo searchParamVo){
        // 匹配查询
        AbstractQueryBuilder matchQueryBuilder = builderQuery(searchParamVo);
        // 4.客户端查询请求
        CountResponse countResponse = getCountSearch(indices,matchQueryBuilder);
        return countResponse==null?0L:countResponse.getCount();
    }

    /**
     * 构建查询条件
     * @param searchParamVo
     * @return
     */
    protected AbstractQueryBuilder builderQuery(EsSearchParamVo searchParamVo) {
        return null;
    }

    /**
     * 构建查询条件
     * @param searchParamVo
     * @return
     */
    protected boolean builderQuery(EsSearchParamVo searchParamVo, BoolQueryBuilder boolQueryBuilder,String idName) {
        boolean searchBr = false;
        if (StrUtil.isNotEmpty(searchParamVo.getName())) {

            BoolQueryBuilder shouldQ= QueryBuilders.boolQuery();
            addTitleQuery(shouldQ,searchParamVo);

            addIntroQuery(shouldQ,searchParamVo);
            //搜索词是标签名，且未选择标签，把标签也查出来
            addNameLabelQuery(shouldQ,searchParamVo);

            boolQueryBuilder.must(shouldQ);
            searchBr = true;
        }
        //排除id
        searchBr = addMustNoIdQuery(boolQueryBuilder,searchParamVo,searchBr);

        //显示类型
        if (CollectionUtil.isNotEmpty(searchParamVo.getShowTypeIds())){
            TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("showTypeId",
                    searchParamVo.getShowTypeIds().toArray(new Integer[]{}));
            boolQueryBuilder.must(termQueryBuilder);
            searchBr = true;
        }
        searchBr = addLabelIdQuery(boolQueryBuilder,searchParamVo,searchBr);

        return searchBr;
    }

    /**
     * 构建排序
     * @param searchSourceBuilder
     * @param searchParamVo
     */
    protected void builderSort(SearchSourceBuilder searchSourceBuilder, EsSearchParamVo searchParamVo){
        if (searchParamVo.getCreateTimeSort() == null && searchParamVo.getViewSort() == null){
            searchSourceBuilder.sort("createTime", SortOrder.DESC);
        }
        if (searchParamVo.getViewSort() != null){
            searchSourceBuilder.sort("readCount", searchParamVo.getViewSort()==0? SortOrder.DESC:SortOrder.ASC);
            searchSourceBuilder.sort("createTime", SortOrder.DESC);
        }
        if (searchParamVo.getCreateTimeSort() != null){
            searchSourceBuilder.sort("createTime", searchParamVo.getCreateTimeSort()==0? SortOrder.DESC:SortOrder.ASC);
        }
    }

    /**
     * 构建分页
     * @param searchSourceBuilder
     * @param pageDTO
     */
    protected void builderPage(SearchSourceBuilder searchSourceBuilder, EsSearchParamVo pageDTO){
        searchSourceBuilder.from((pageDTO.getPageNum()-1)*pageDTO.getPageSize());
        searchSourceBuilder.size(pageDTO.getPageSize());
    }

    /**
     * bean 转换
     * @param hits
     */
    protected<T> List<T> toBean(SearchHits hits,Class<T>tClass){
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);

        SearchHit[] hitsValue = hits.getHits();

        List<T> list = new ArrayList<>(hitsValue.length);
        for (SearchHit documentFields : hitsValue) {
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            list.add(BeanUtil.mapToBean(sourceAsMap,tClass ,false,copyOptions));
        }
        return list;
    }

    /**
     * bean 转换
     * @param hits
     */
    protected List toBean(SearchHits hits){
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);

        SearchHit[] hitsValue = hits.getHits();

        List list = new ArrayList<>(hitsValue.length);
        for (SearchHit documentFields : hitsValue) {
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            for (EsIndexEnum value : EsIndexEnum.values()) {
                if (sourceAsMap.keySet().contains(value.idName())){
                    list.add(BeanUtil.mapToBean(sourceAsMap,value.tClass(),false,copyOptions));
                }
            }
        }
        return list;
    }

    /**
     * 构建分页结果
     * @param <T>
     * @return
     */
    protected<T> PageVO<T> builderPageResult(EsSearchParamVo pageDTO, SearchResponse search, Class<T>tClass){
        PageVO<T>result = new PageVO<>();
        SearchHits hits = search.getHits();
        List<T> list = toBean(hits,tClass);
        //===============分页信息====================//
        //总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 总页码
        int totalPages = (int)total % pageDTO.getPageSize() == 0 ?
                (int)total / pageDTO.getPageSize() : ((int)total / pageDTO.getPageSize() + 1);
        result.setPages(totalPages);
        result.setList(list);
        return result;
    }

    /**
     * 构建分页结果
     */
    protected PageVO builderPageResult(EsSearchParamVo pageDTO,SearchResponse search){
        PageVO result = new PageVO<>();
        SearchHits hits = search.getHits();
        List list = toBean(hits);
        //===============分页信息====================//
        //总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // 总页码
        int totalPages = (int)total % pageDTO.getPageSize() == 0 ?
                (int)total / pageDTO.getPageSize() : ((int)total / pageDTO.getPageSize() + 1);
        result.setPages(totalPages);
        result.setList(list);
        return result;
    }

    /**
     * 存储文档
     * @param id id
     * @param indices 库名
     * @param object 属性
     */
    public RestStatus saveIndexDocument(String id,String indices,Object object) {
        // 创建请求
        IndexRequest request = new IndexRequest(indices);
        // 制定规则 PUT /xiaoming_index/_doc/1
        request.id(id);// 设置文档ID
        request.timeout(TimeValue.timeValueMillis(1000));// request.timeout("1s")
        // 将我们的数据放入请求中
        request.source(JSON.toJSONString(object), XContentType.JSON);
        // 客户端发送请求，获取响应的结果
        try {
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            RestStatus status = response.status();
            return status;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量存储文档
     */
    public<T> RestStatus bulk(BulkRequest bulkRequest) {
        try {
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println("--------->"+bulk.status());
            return bulk.status();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 修改文档
     */
    public<T> RestStatus update(UpdateRequest updateRequest) {
        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            return update.status();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文档
     * @param id 主键
     * @param indices 库名
     */
    public DeleteResponse deleteDocument(String id,String indices){
        try {
            DeleteRequest request = new DeleteRequest(indices, id);
            request.timeout("1s");
            DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除文档
     * @param ids 主键
     * @param indices 库名
     */
    public BulkByScrollResponse deleteByIds(String indices, String... ids){
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indices);
            //精准查询
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            idsQueryBuilder.addIds(ids);
            deleteByQueryRequest.setQuery(idsQueryBuilder);
            BulkByScrollResponse bulkByScrollResponse = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
            return bulkByScrollResponse;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据id查询数据
     */
    protected <T> List<T> getAllSearch(Set<String> ids, String indices,Class<T>tClass){
        // 1.创建查询请求对象
        SearchRequest searchRequest = new SearchRequest(indices);
        // 2.构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //精准查询
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
        idsQueryBuilder.addIds(ids.toArray(new String[]{}));
        boolQueryBuilder.must(idsQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        // 4.客户端查询请求
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return toBean(search.getHits(),tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获取模糊词项长度
     * @param str
     * @return
     */
    protected Fuzziness getFuzziness(String str){
        if (str.length() >1 && str.length()<=3){
            return Fuzziness.ONE;
        }
        if (str.length()>3){
            return Fuzziness.TWO;
        }
        return Fuzziness.ZERO;
    }

    /**
     * 添加标题搜索条件
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addTitleQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //标题 精准查询
        TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("title", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilder);
        //标题 ik分词器查询
        TermsQueryBuilder termQueryBuilderIk = QueryBuilders.termsQuery("title.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIk);
        //标题 拼音查询
        TermsQueryBuilder termQueryBuilderPinyin = QueryBuilders.termsQuery("title.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderPinyin);
        //标题 Ngrm查询
        TermsQueryBuilder termQueryBuilderNgram = QueryBuilders.termsQuery("title.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderNgram);
        //ik分词器查询
//        MatchPhraseQueryBuilder title_matchQueryBuilder = QueryBuilders.matchPhraseQuery("title.ik_max_word", searchParamVo.getName());
//        shouldQ.should(title_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderTitle= QueryBuilders.fuzzyQuery("title", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderTitle);
    }

    /**
     * 添加简介搜索条件
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addIntroQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //标题 精准查询
        TermsQueryBuilder termQueryBuilderIntro = QueryBuilders.termsQuery("intro", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIntro);
        //Ngrm查询
        TermsQueryBuilder termQueryBuilderIntroNgram = QueryBuilders.termsQuery("intro.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIntroNgram);
        //ik分词器查询
//        MatchPhraseQueryBuilder intro_matchQueryBuilder = QueryBuilders.matchPhraseQuery("intro", searchParamVo.getName());
//        shouldQ.should(intro_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderIntro = QueryBuilders.fuzzyQuery("intro", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderIntro);
    }

    /**
     * 添加企业名称搜索条件
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addCompanyNameQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //精准查询
        TermsQueryBuilder isvTermQueryBuilder = QueryBuilders.termsQuery("companyName", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilder);
        //ik分词器查询
        TermsQueryBuilder isvTermQueryBuilderIk = QueryBuilders.termsQuery("companyName.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderIk);
        //拼音查询
        TermsQueryBuilder isvTermQueryBuilderPinyin = QueryBuilders.termsQuery("companyName.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderPinyin);
        //Ngrm查询
        TermsQueryBuilder isvTermQueryBuilderNgram = QueryBuilders.termsQuery("companyName.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderNgram);
        //ik分词器查询
        MatchPhraseQueryBuilder companyName_matchQueryBuilder = QueryBuilders.matchPhraseQuery("companyName.ik_max_word", searchParamVo.getName());
        shouldQ.should(companyName_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderCompanyName = QueryBuilders.fuzzyQuery("companyName", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderCompanyName);
    }

    /**
     * 添加商机发布人搜索条件
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addTendereeQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //招标人 拼音分词器查询
        TermsQueryBuilder termQueryBuilderTendereePinyin = QueryBuilders.termsQuery("tenderee.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderTendereePinyin);
        //招标人 Ngrm查询
        TermsQueryBuilder termQueryBuilderTendereeNgram = QueryBuilders.termsQuery("tenderee.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderTendereeNgram);
        //ik分词器查询
//        MatchPhraseQueryBuilder tenderee_matchQueryBuilder = QueryBuilders.matchPhraseQuery("tenderee.ik_max_word", searchParamVo.getName());
//        shouldQ.should(tenderee_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderTenderee = QueryBuilders.fuzzyQuery("tenderee", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderTenderee);
    }

    /**
     * 添加详情搜索条件
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addDetailQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //详情 ik分词器查询
        TermsQueryBuilder termQueryBuilderDetailIk = QueryBuilders.termsQuery("detail.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderDetailIk);
        //详情 Ngrm查询
        TermsQueryBuilder termQueryBuilderDetailNgram = QueryBuilders.termsQuery("detail.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderDetailNgram);
        //ik分词器查询
//        MatchPhraseQueryBuilder detail_matchQueryBuilder = QueryBuilders.matchPhraseQuery("detail", searchParamVo.getName());
//        shouldQ.should(detail_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderDetail = QueryBuilders.fuzzyQuery("detail", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderDetail);
    }

    /**
     * 添加关键词标签搜索
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addNameLabelQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //搜索词是标签名，且未选择标签，把标签也查出来
        if (StrUtil.isNotEmpty(searchParamVo.getNameLabel())){
            List<String> nameLabelIds = new ArrayList<>(Arrays.asList(searchParamVo.getNameLabel().split(",")));
            TermsQueryBuilder termsQueryBuilderLabel = QueryBuilders.termsQuery("labelIds", nameLabelIds);
            shouldQ.should(termsQueryBuilderLabel);
        }
    }

    /**
     * 添加标签id搜索
     * @param searchParamVo
     */
    protected boolean addLabelIdQuery(BoolQueryBuilder boolQueryBuilder,EsSearchParamVo searchParamVo,boolean searchBr){
        if (StrUtil.isNotEmpty(searchParamVo.getLabelIds())){
            String[] strArr = searchParamVo.getLabelIds().split(",");
            if (strArr.length>0){
                BoolQueryBuilder shouldQ= QueryBuilders.boolQuery();
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("labelIds", strArr);
                shouldQ.should(termsQueryBuilder);

                ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery("labelIds");
                BoolQueryBuilder mustNotExists= QueryBuilders.boolQuery();
                mustNotExists.mustNot(existsQueryBuilder);
                shouldQ.should(mustNotExists);
                boolQueryBuilder.must(shouldQ);
                searchBr = true;
            }
        }
        return searchBr;
    }

    /**
     * 排除主键
     * @param searchParamVo
     */
    protected boolean addMustNoIdQuery(BoolQueryBuilder boolQueryBuilder,EsSearchParamVo searchParamVo,boolean searchBr){
        if (CollectionUtil.isNotEmpty(searchParamVo.getNoIds())) {
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
            idsQueryBuilder.addIds(searchParamVo.getNoIds().toArray(new String[]{}));
            boolQueryBuilder.mustNot(idsQueryBuilder);
            searchBr = true;
        }
        return searchBr;
    }
}
