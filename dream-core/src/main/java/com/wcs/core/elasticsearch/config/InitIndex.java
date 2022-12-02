package com.wcs.core.elasticsearch.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 初始化索引库
 */
public class InitIndex implements InitializingBean {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void afterPropertiesSet() throws Exception {
//        for (EsIndexEnum value : EsIndexEnum.values()) {
//            createIndex(value.value());
//        }
    }

    /**
     * Description: 创建索引库和映射
     */
//    private void createIndex(String indicesName) throws IOException {
//        //创建索引库
//        GetIndexRequest request = new GetIndexRequest(indicesName);
//        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
//        if (!exists){
//            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indicesName);
//            CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
//            //创建索引库
//            if (!response.isAcknowledged()){
//                throw new GlobalException("索引创建失败");
//            }
////            PutMappingRequest putMappingRequest = new PutMappingRequest();
////            RequestOptions options = new RequestOptions();
////            //创建映射
////            boolean mapping = restHighLevelClient.indices().putMapping();
////            if (!mapping){
////                //创建映射失败要删除索引库
////                DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indicesName);
////                AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
////            }
//        }
//    }
}
