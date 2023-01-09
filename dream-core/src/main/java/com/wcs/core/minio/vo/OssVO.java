package com.wcs.core.minio.vo;

import lombok.Data;

/**
 * @author: wcs
 * @create: 2023-01-09 10:01
 * @description:
 */
@Data
public class OssVO {

    private String dir;
    /**文件名称*/
    private String fileName="";
    /**上传token*/
    private String actionUrl;
    /**路径*/
    private String path;
    /**完整路径*/
    private String completePath;
}
