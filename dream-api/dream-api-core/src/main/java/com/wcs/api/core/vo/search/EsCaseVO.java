package com.wcs.api.core.vo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * 案例
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EsCaseVO {
    /**案例id*/
    private Long caseId;
    /**标题*/
    private String title;
    /**图像*/
    private String image;
    /**标签*/
    private String labelIds;
    /**浏览量*/
    private Long readCount=0L;
    /**创建时间*/
    private Date createTime;
    /**1 置顶*/
    private Integer isTop=0;
    /**简介*/
    private String intro;
    /**显示类型id*/
    private Integer showTypeId;
}
