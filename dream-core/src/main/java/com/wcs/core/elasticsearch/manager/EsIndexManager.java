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
import org.elasticsearch.common.text.Text;
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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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
     * ????????????
     * @param indices ??????
     * @param matchQueryBuilder ????????????
     */
    protected SearchResponse getSearchResponse(EsSearchParamVo searchParamVo,
                                               String indices, AbstractQueryBuilder matchQueryBuilder){
        // 1.????????????????????????
        SearchRequest searchRequest = new SearchRequest(indices);
        // 2.??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title.ngram");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // ??????
        builderPage(searchSourceBuilder,searchParamVo);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // (3)????????????
        if (matchQueryBuilder != null) {
            searchSourceBuilder.query(matchQueryBuilder);
        }
        //????????????
        this.builderSort(searchSourceBuilder,searchParamVo);
        // 3.?????????????????????
        searchRequest.source(searchSourceBuilder);
        // 4.?????????????????????
        SearchResponse search = null;
        try {
            search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return search;
    }

    /**
     * ????????????
     * @param indices ??????
     * @param matchQueryBuilder ????????????
     */
    protected CountResponse getCountSearch(String indices, AbstractQueryBuilder matchQueryBuilder){
        // ???????????????
        CountRequest countRequest = new CountRequest(indices);
        // (3)????????????
        if (matchQueryBuilder != null) {
            countRequest.query(matchQueryBuilder);
        }
        // 4.?????????????????????
        CountResponse countResponse = null;
        try {
            countResponse = restHighLevelClient.count(countRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countResponse;
    }

    /**
     * ????????????
     * @param indices
     * @param searchParamVo
     */
    public Long getCount(String indices,EsSearchParamVo searchParamVo){
        // ????????????
        AbstractQueryBuilder matchQueryBuilder = builderQuery(searchParamVo);
        // 4.?????????????????????
        CountResponse countResponse = getCountSearch(indices,matchQueryBuilder);
        return countResponse==null?0L:countResponse.getCount();
    }

    /**
     * ??????????????????
     * @param searchParamVo
     * @return
     */
    protected AbstractQueryBuilder builderQuery(EsSearchParamVo searchParamVo) {
        return null;
    }

    /**
     * ??????????????????
     * @param searchParamVo
     * @return
     */
    protected boolean builderQuery(EsSearchParamVo searchParamVo, BoolQueryBuilder boolQueryBuilder,String idName) {
        boolean searchBr = false;
        if (StrUtil.isNotEmpty(searchParamVo.getName())) {

            BoolQueryBuilder shouldQ= QueryBuilders.boolQuery();
            addTitleQuery(shouldQ,searchParamVo);

            addIntroQuery(shouldQ,searchParamVo);
            //??????????????????????????????????????????????????????????????????
            addNameLabelQuery(shouldQ,searchParamVo);

            boolQueryBuilder.must(shouldQ);
            searchBr = true;
        }
        //??????id
        searchBr = addMustNoIdQuery(boolQueryBuilder,searchParamVo,searchBr);

        //????????????
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
     * ????????????
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
     * ????????????
     * @param searchSourceBuilder
     * @param pageDTO
     */
    protected void builderPage(SearchSourceBuilder searchSourceBuilder, EsSearchParamVo pageDTO){
        searchSourceBuilder.from((pageDTO.getPageNum()-1)*pageDTO.getPageSize());
        searchSourceBuilder.size(pageDTO.getPageSize());
    }

    /**
     * bean ??????
     * @param hits
     */
    protected<T> List<T> toBean(SearchHits hits,Class<T>tClass){
        CopyOptions copyOptions = new CopyOptions();
        copyOptions.setIgnoreNullValue(true);

        SearchHit[] hitsValue = hits.getHits();

        List<T> list = new ArrayList<>(hitsValue.length);
        for (SearchHit documentFields : hitsValue) {

            // ?????????????????????????????????????????????????????????
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            // ????????????
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField name = highlightFields.get("title.ngram");

            // ??????
            if (name != null){
                Text[] fragments = name.fragments();
                StringBuilder new_name = new StringBuilder();
                for (Text text : fragments) {
                    new_name.append(text);
                }
                sourceAsMap.put("title",new_name.toString());
            }
//            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            list.add(BeanUtil.mapToBean(sourceAsMap,tClass ,false,copyOptions));
        }
        return list;
    }

    /**
     * bean ??????
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
     * ??????????????????
     * @param <T>
     * @return
     */
    protected<T> PageVO<T> builderPageResult(EsSearchParamVo pageDTO, SearchResponse search, Class<T>tClass){
        PageVO<T>result = new PageVO<>();
        SearchHits hits = search.getHits();
        List<T> list = toBean(hits,tClass);
        //===============????????????====================//
        //????????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // ?????????
        int totalPages = (int)total % pageDTO.getPageSize() == 0 ?
                (int)total / pageDTO.getPageSize() : ((int)total / pageDTO.getPageSize() + 1);
        result.setPages(totalPages);
        result.setList(list);
        return result;
    }

    /**
     * ??????????????????
     */
    protected PageVO builderPageResult(EsSearchParamVo pageDTO,SearchResponse search){
        PageVO result = new PageVO<>();
        SearchHits hits = search.getHits();
        List list = toBean(hits);
        //===============????????????====================//
        //????????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // ?????????
        int totalPages = (int)total % pageDTO.getPageSize() == 0 ?
                (int)total / pageDTO.getPageSize() : ((int)total / pageDTO.getPageSize() + 1);
        result.setPages(totalPages);
        result.setList(list);
        return result;
    }

    /**
     * ????????????
     * @param id id
     * @param indices ??????
     * @param object ??????
     */
    public RestStatus saveIndexDocument(String id,String indices,Object object) {
        // ????????????
        IndexRequest request = new IndexRequest(indices);
        // ???????????? PUT /xiaoming_index/_doc/1
        request.id(id);// ????????????ID
        request.timeout(TimeValue.timeValueMillis(1000));// request.timeout("1s")
        // ?????????????????????????????????
        request.source(JSON.toJSONString(object), XContentType.JSON);
        // ?????????????????????????????????????????????
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
     * ??????????????????
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
     * ????????????
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
     * ????????????
     * @param id ??????
     * @param indices ??????
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
     * ????????????
     * @param ids ??????
     * @param indices ??????
     */
    public BulkByScrollResponse deleteByIds(String indices, String... ids){
        try {
            DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indices);
            //????????????
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
     * ??????id????????????
     */
    protected <T> List<T> getAllSearch(Set<String> ids, String indices,Class<T>tClass){
        // 1.????????????????????????
        SearchRequest searchRequest = new SearchRequest(indices);
        // 2.??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //????????????
        IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery();
        idsQueryBuilder.addIds(ids.toArray(new String[]{}));
        boolQueryBuilder.must(idsQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);

        // 4.?????????????????????
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return toBean(search.getHits(),tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * ????????????????????????
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
     * ????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addTitleQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //?????? ????????????
        TermsQueryBuilder termQueryBuilder = QueryBuilders.termsQuery("title", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilder);
        //?????? ik???????????????
        TermsQueryBuilder termQueryBuilderIk = QueryBuilders.termsQuery("title.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIk);
        //?????? ????????????
        TermsQueryBuilder termQueryBuilderPinyin = QueryBuilders.termsQuery("title.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderPinyin);
        //?????? Ngrm??????
        TermsQueryBuilder termQueryBuilderNgram = QueryBuilders.termsQuery("title.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderNgram);
        //ik???????????????
//        MatchPhraseQueryBuilder title_matchQueryBuilder = QueryBuilders.matchPhraseQuery("title.ik_max_word", searchParamVo.getName());
//        shouldQ.should(title_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderTitle= QueryBuilders.fuzzyQuery("title", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderTitle);
    }

    /**
     * ????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addIntroQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //?????? ????????????
        TermsQueryBuilder termQueryBuilderIntro = QueryBuilders.termsQuery("intro", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIntro);
        //Ngrm??????
        TermsQueryBuilder termQueryBuilderIntroNgram = QueryBuilders.termsQuery("intro.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderIntroNgram);
        //ik???????????????
//        MatchPhraseQueryBuilder intro_matchQueryBuilder = QueryBuilders.matchPhraseQuery("intro", searchParamVo.getName());
//        shouldQ.should(intro_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderIntro = QueryBuilders.fuzzyQuery("intro", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderIntro);
    }

    /**
     * ??????????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addCompanyNameQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //????????????
        TermsQueryBuilder isvTermQueryBuilder = QueryBuilders.termsQuery("companyName", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilder);
        //ik???????????????
        TermsQueryBuilder isvTermQueryBuilderIk = QueryBuilders.termsQuery("companyName.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderIk);
        //????????????
        TermsQueryBuilder isvTermQueryBuilderPinyin = QueryBuilders.termsQuery("companyName.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderPinyin);
        //Ngrm??????
        TermsQueryBuilder isvTermQueryBuilderNgram = QueryBuilders.termsQuery("companyName.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(isvTermQueryBuilderNgram);
        //ik???????????????
        MatchPhraseQueryBuilder companyName_matchQueryBuilder = QueryBuilders.matchPhraseQuery("companyName.ik_max_word", searchParamVo.getName());
        shouldQ.should(companyName_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderCompanyName = QueryBuilders.fuzzyQuery("companyName", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderCompanyName);
    }

    /**
     * ?????????????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addTendereeQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //????????? ?????????????????????
        TermsQueryBuilder termQueryBuilderTendereePinyin = QueryBuilders.termsQuery("tenderee.pinyin", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderTendereePinyin);
        //????????? Ngrm??????
        TermsQueryBuilder termQueryBuilderTendereeNgram = QueryBuilders.termsQuery("tenderee.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderTendereeNgram);
        //ik???????????????
//        MatchPhraseQueryBuilder tenderee_matchQueryBuilder = QueryBuilders.matchPhraseQuery("tenderee.ik_max_word", searchParamVo.getName());
//        shouldQ.should(tenderee_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderTenderee = QueryBuilders.fuzzyQuery("tenderee", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderTenderee);
    }

    /**
     * ????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addDetailQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //?????? ik???????????????
        TermsQueryBuilder termQueryBuilderDetailIk = QueryBuilders.termsQuery("detail.ik_max_word", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderDetailIk);
        //?????? Ngrm??????
        TermsQueryBuilder termQueryBuilderDetailNgram = QueryBuilders.termsQuery("detail.ngram", searchParamVo.getNameStrSet());
        shouldQ.should(termQueryBuilderDetailNgram);
        //ik???????????????
//        MatchPhraseQueryBuilder detail_matchQueryBuilder = QueryBuilders.matchPhraseQuery("detail", searchParamVo.getName());
//        shouldQ.should(detail_matchQueryBuilder);

        FuzzyQueryBuilder fuzzyQueryBuilderDetail = QueryBuilders.fuzzyQuery("detail", searchParamVo.getName()).fuzziness(getFuzziness(searchParamVo.getName()));
        shouldQ.should(fuzzyQueryBuilderDetail);
    }

    /**
     * ???????????????????????????
     * @param shouldQ
     * @param searchParamVo
     */
    protected void addNameLabelQuery(BoolQueryBuilder shouldQ,EsSearchParamVo searchParamVo){
        //??????????????????????????????????????????????????????????????????
        if (StrUtil.isNotEmpty(searchParamVo.getNameLabel())){
            List<String> nameLabelIds = new ArrayList<>(Arrays.asList(searchParamVo.getNameLabel().split(",")));
            TermsQueryBuilder termsQueryBuilderLabel = QueryBuilders.termsQuery("labelIds", nameLabelIds);
            shouldQ.should(termsQueryBuilderLabel);
        }
    }

    /**
     * ????????????id??????
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
     * ????????????
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
