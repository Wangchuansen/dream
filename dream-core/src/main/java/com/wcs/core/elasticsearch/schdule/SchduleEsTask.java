package com.wcs.core.elasticsearch.schdule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.wcs.api.core.vo.EsIndexEnum;
import com.wcs.api.core.vo.search.EsCaseVO;
import com.wcs.common.util.DateUtil;
import com.wcs.core.elasticsearch.manager.EsCaseManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SchduleEsTask {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EsCaseManager esCaseManager;
//    @Autowired
//    private EsSchemeManager esSchemeManager;
//    @Autowired
//    private EsSceneManager esSceneManager;
//    @Autowired
//    private EsPolicyManager esPolicyManager;
//    @Autowired
//    private EsWhitebookManager esWhitebookManager;
//    @Autowired
//    private EsOpportunityManager esOpportunityManager;
//    @Autowired
//    private EsMobileSampleManager esMobileSampleManager;

    /**
     * 同步浏览量数据
     * 每隔30分钟执行
     */
//    @Scheduled(fixedDelay = 60*1000*30 )
    public void saveCount(){
        //修改案例
        updateReadCount(getReadCountKey(EsIndexEnum.CASE_INDEX.value()),EsIndexEnum.CASE_INDEX.value());
        //修改方案
//        updateReadCount(getReadCountKey(EsIndexEnum.SCHEME_INDEX.value()),EsIndexEnum.SCHEME_INDEX.value());
        //修改场景
//        updateReadCount(getReadCountKey(EsIndexEnum.SCENE_INDEX.value()),EsIndexEnum.SCENE_INDEX.value());
        //修改政策
//        updateReadCount(getReadCountKey(EsIndexEnum.POLICY_INDEX.value()),EsIndexEnum.POLICY_INDEX.value());
        //修改白皮书
//        updateReadCount(getReadCountKey(EsIndexEnum.WHITEBOOK_INDEX.value()),EsIndexEnum.WHITEBOOK_INDEX.value());
        //修改商机
//        updateReadOpportunityCount(getReadCountKey(EsIndexEnum.OPPORTUNITY_INDEX.value()),EsIndexEnum.OPPORTUNITY_INDEX.value());
        //修改移动标品
//        updateReadCount(getReadCountKey(EsIndexEnum.MOBILE_SAMPLE_INDEX.value()),EsIndexEnum.MOBILE_SAMPLE_INDEX.value());
    }

    /**
     * 修改 es中浏览量数据
     * @param idCountMap
     */
    private<T> void updateEs(Map<String,Long>idCountMap,String indexName){
        if (MapUtil.isNotEmpty(idCountMap)){
            EsIndexEnum indexEnum = EsIndexEnum.getValue(indexName);
            switch (indexEnum){
                case CASE_INDEX:
                    List<EsCaseVO>caseList = esCaseManager.getAllSearch(idCountMap.keySet());
                    if (CollectionUtil.isNotEmpty(caseList)){
                        caseList.forEach(s->{
                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getCaseId().toString()));
                        });
                        esCaseManager.bulk(caseList);
                    }
                    break;
//                case SCHEME_INDEX:
//                    List<EsSchemeVO>schemeList = esSchemeManager.getAllSearch(idCountMap.keySet());
//                    if (CollectionUtil.isNotEmpty(schemeList)){
//                        schemeList.forEach(s->{
//                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getSchemeId().toString()));
//                        });
//                        esSchemeManager.bulk(schemeList);
//                    }
//                    break;
//                case SCENE_INDEX:
//                    List<EsSceneVO>sceneList = esSceneManager.getAllSearch(idCountMap.keySet());
//                    if (CollectionUtil.isNotEmpty(sceneList)){
//                        sceneList.forEach(s->{
//                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getSceneId().toString()));
//                        });
//                        esSceneManager.bulk(sceneList);
//                    }
//                    break;
//                case POLICY_INDEX:
//                    List<EsPolicyVO>policyList = esPolicyManager.getAllSearch(idCountMap.keySet());
//                    if (CollectionUtil.isNotEmpty(policyList)){
//                        policyList.forEach(s->{
//                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getPolicyId().toString()));
//                        });
//                        esPolicyManager.bulk(policyList);
//                    }
//                    break;
//                case WHITEBOOK_INDEX:
//                    List<EsWhitebookVO>whitebookList = esWhitebookManager.getAllSearch(idCountMap.keySet());
//                    if (CollectionUtil.isNotEmpty(whitebookList)){
//                        whitebookList.forEach(s->{
//                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getWhitebookId().toString()));
//                        });
//                        esWhitebookManager.bulk(whitebookList);
//                    }
//                    break;
//                case MOBILE_SAMPLE_INDEX:
//                    List<EsMobileSampleVO>esMobileSampleVOList = esMobileSampleManager.getAllSearch(idCountMap.keySet());
//                    if (CollectionUtil.isNotEmpty(esMobileSampleVOList)){
//                        esMobileSampleVOList.forEach(s->{
//                            s.setReadCount(s.getReadCount()+idCountMap.get(s.getSampleId().toString()));
//                        });
//                        esMobileSampleManager.bulk(esMobileSampleVOList);
//                    }
//                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 获取 key值
     * @param searchName
     */
    private Set<String> getReadCountKey(String searchName){
        //搜索到的 key 值存放的集合
        Set<String> keys = new LinkedHashSet<>();
        //开始搜索,数据存储在上面的 set 集合中
        stringRedisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().count(Long.MAX_VALUE)
                            //* 号通配符的作用
                            .match(searchName+"_count_*")
                            .build()
            )) {
                cursor.forEachRemaining(item -> {
                    keys.add(RedisSerializer.string().deserialize(item));
                });
                return keys;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return keys;
    }

    /**
     * 获取 key值
     * @param searchName
     */
    private void updateReadCount(Set<String> keys,String searchName){
        if (CollectionUtil.isNotEmpty(keys)){
            //获取一个 redis 操作对象
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            Map<String,Long> idCountMap = new HashMap<>(keys.size());
            for (String key : keys) {
                Long value = Long.valueOf(opsForValue.get(key));
                String[] s = key.split("_");
//                if (searchName.equals(EsIndexEnum.MOBILE_SAMPLE_INDEX.value())){
//                    idCountMap.put(s[3], value);
//                }else {
//                    idCountMap.put(s[2], value);
//                }
            }
            updateEs(idCountMap,searchName);
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * 获取 key值
     * @param searchName
     */
    public void updateReadOpportunityCount(Set<String> keys,String searchName){
        if (CollectionUtil.isNotEmpty(keys)){
            //获取一个 redis 操作对象
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            Map<String,Long> idCountMap = new HashMap<>(keys.size());
            Map<String,String>hotDayCountMap = new HashMap<>(keys.size());
            for (String key : keys) {
                Long value = Long.valueOf(opsForValue.get(key));
                String[] s = key.split("_");
                idCountMap.put(s[2],value); //主键id_阅读数量

                //存储天数浏览量 opportunity_day_count_142
                String dayKey = searchName+"_day_count_"+s[2];
                String dayCountValue = opsForValue.get(dayKey);
                JSONObject jsonObject =
                        StrUtil.isNotEmpty(dayCountValue)?JSONObject.parseObject(dayCountValue):new JSONObject();
                LocalDate now = LocalDate.now();
                String nowStr = now.toString();
                Long count = jsonObject.getLongValue(nowStr);
                jsonObject.put(nowStr,count+value);

                //比对获取三天中最大的阅读次数
                List<String>days = DateUtil.getDateStr(now,3,false);
                long maxValue = 0L;
                Set<String> jsonKeySet = jsonObject.keySet().stream().collect(Collectors.toSet());
                for (String keyStr : jsonKeySet) {
                    if(!days.contains(keyStr)){
                        jsonObject.remove(keyStr);
                    }else {
                        long longValue = jsonObject.getLongValue(keyStr);
                        if (longValue>= maxValue){
                            maxValue = longValue;
                            hotDayCountMap.put(s[2],keyStr+"_"+maxValue);
                        }
                    }
                }
                stringRedisTemplate.opsForValue().set(dayKey,jsonObject.toJSONString(),3, TimeUnit.DAYS);
            }
            //修改es中的值
//            List<EsOpportunityVO>opportunityList = esOpportunityManager.getAllSearch(idCountMap.keySet());
//            if (CollectionUtil.isNotEmpty(opportunityList)){
//                opportunityList.forEach(s->{
//                    s.setReadCount(s.getReadCount()+idCountMap.get(s.getOpportunityId().toString()));
//                    String hotRecord = hotDayCountMap.get(s.getOpportunityId().toString());
//                    if (StrUtil.isNotEmpty(hotRecord)) {
//                        s.setHotReadRecord(hotRecord.replace("-", ""));
//                    }
//                });
//                esOpportunityManager.bulk(opportunityList);
//            }
            stringRedisTemplate.delete(keys);
        }
    }

}
