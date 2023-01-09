package com.wcs.common.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wcs
 * @create: 2023-01-09 9:28
 * @description:
 */
public class Constant {
    /**
     * 默认头像
     */
    public static final String DEFAULT_HEAD_PATH = "defaultheader20221027.png";

    /**收藏*/
    public static final String collectCountKey = "collectCount";
    /**点赞*/
    public static final String likeCountKey = "likeCount";
    /**转发*/
    public static final String transmitCountKey = "transmitCount";
    /**浏览*/
    public static final String readCountKey = "readCount";

    /**是*/
    public static final int YES = 1;
    /**否*/
    public static final int NO = 0;

    /**待审核*/
    public static final int APPLY_STATUS1 = 1;
    /**通过*/
    public static final int APPLY_STATUS2 = 2;
    /**驳回*/
    public static final int APPLY_STATUS3 = 3;
    /**撤销*/
    public static final int APPLY_STATUS4 = 4;
    /**下架*/
    public static final int APPLY_STATUS5 = 5;

    /**下架*/
    public static final int DISABLE = 0;
    /**上架*/
    public static final int ENABLE = 1;

    /**游客*/
    public static final int USER_TYPE0 = 0;
    /**企业用户*/
    public static final int USER_TYPE1 = 1;

    /**企业类型 0 isv*/
    public static final int COMPANY_TYPE0 = 0;
    /**企业类型 1 运营*/
    public static final int COMPANY_TYPE1 = 1;

    /**平台所有用户可查看案例内容，附件可查看可下载*/
    public static final int SHOW_TYPE1 = 1;
    /**平台所有用户仅可查看案例内容，附件内容不可查看及下载*/
    public static final int SHOW_TYPE2 = 2;
    /**仅移动客户经理可查看案例内容及附件*/
    public static final int SHOW_TYPE3 = 3;
    /**显示类型*/
    public static List<Integer> showTypeList = new ArrayList<>();
    /**忽略草稿箱字段*/
    public static List<String> ignoreFields = new ArrayList<>();



    /**审核状态*/
    public static Map<Integer,String> applyStatusMap = new HashMap<>(5);
    /**上下架*/
    public static Map<Integer,String> ableMap = new HashMap<>(2);
    static {

        applyStatusMap.put(APPLY_STATUS1,"待审核");
        applyStatusMap.put(APPLY_STATUS2,"已通过");
        applyStatusMap.put(APPLY_STATUS3,"已驳回");
        applyStatusMap.put(APPLY_STATUS4,"已撤销");
        applyStatusMap.put(APPLY_STATUS5,"已下架");

        ableMap.put(DISABLE,"下架");
        ableMap.put(ENABLE,"上架");

        showTypeList.add(SHOW_TYPE1);
        showTypeList.add(SHOW_TYPE2);
        showTypeList.add(SHOW_TYPE3);

        ignoreFields.add("showTypeId");
    }

}

