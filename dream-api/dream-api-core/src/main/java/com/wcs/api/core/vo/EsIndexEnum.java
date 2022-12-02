package com.wcs.api.core.vo;

import com.wcs.api.core.vo.search.*;

/**
 * es当中的index
 * @author hxm
 * @date 2020/11/12
 */
public enum EsIndexEnum {

    /**
     * 案例
     */
    CASE_INDEX(1,"case","caseId", EsCaseVO.class),

    /**
     * 方案
     */
    SCHEME_INDEX(2,"scheme","schemeId", EsSchemeVO.class),
    /**
     * 场景
     */
    SCENE_INDEX(3,"scene","sceneId", EsSceneVO.class),
    /**
     * 白皮书
     */
    WHITEBOOK_INDEX(4,"whitebook","whitebookId", EsWhitebookVO.class),
    /**
     * 政策
     */
    POLICY_INDEX(5,"policy","policyId", EsPolicyVO.class),
    /**
     * 商机
     */
    OPPORTUNITY_INDEX(8,"opportunity","opportunityId", EsOpportunityVO.class),
    /**
     * ISV
     */
    ISV_INDEX(9,"isv","companyId", EsIsvVO.class),
    /**
     * 移动标品
     */
    MOBILE_SAMPLE_INDEX(10,"mobile_sample","sampleId", EsMobileSampleVO.class),
    ;

    /**
     * 通过idName取
     */
    public static EsIndexEnum getIdName(String idName) {
        for (EsIndexEnum enums : EsIndexEnum.values()) {
            if (idName.equals(enums.idName())){
                return enums;
            }
        }
        return null;
    }

    /**
     * 通过id取
     */
    public static EsIndexEnum getId(Integer id) {
        for (EsIndexEnum enums : EsIndexEnum.values()) {
            if (id.equals(enums.id())){
                return enums;
            }
        }
        return null;
    }

    /**
     * 通过value取描述
     * @param moduleValue
     */
    public static EsIndexEnum getValue(String moduleValue) {
        for (EsIndexEnum enums : EsIndexEnum.values()) {
            if (moduleValue.equals(enums.value())){
                return enums;
            }
        }
        return null;
    }

    private final Integer id;

    private final String value;

    private final String idName;

    private final Class<?> tClass;

    public Integer id() {
        return id;
    }

    public String value() {
        return value;
    }

    public String idName() {
        return idName;
    }

    public Class<?> tClass() {
        return tClass;
    }

    EsIndexEnum(Integer id, String value, String idName, Class<?> tClass) {
        this.id = id;
        this.value = value;
        this.idName = idName;
        this.tClass = tClass;
    }
}
