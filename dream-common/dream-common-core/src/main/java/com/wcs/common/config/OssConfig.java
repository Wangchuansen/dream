package com.wcs.common.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wcs
 * @create: 2022-08-14 16:22
 * @description:
 */
@RefreshScope
@Configuration
@Data
public class OssConfig {

    @Value("${minio.oss.endpoint}")
    private String endpoint;
    @Value("${minio.oss.bucket}")
    private String bucket;
    @Value("${minio.oss.access-key-id}")
    private String accessKeyId;
    @Value("${minio.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${minio.oss.type}")
    private Integer ossType;
    @Value("${minio.oss.resources-url}")
    private String resourcesUrl;
    /**
     * 最大上传长度单位m，默认20M
     */
    @Value("${minio.oss.maxLength:20}")
    private Integer maxLength;
    /**最大分片数量*/
    @Value("${minio.oss.max-part-num}")
    private Integer maxPartNum;
    /**过期时长*/
    @Value("${minio.oss.expiry}")
    private Integer expiry;

    /**
     * 获取完整路径
     * @param filePaths 文件路径
     */
    public List<String> getCompletePath(List<String> filePaths){
        List<String>result = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(filePaths)){
            filePaths.forEach(s->{
                result.add(getCompletePath(s));
            });
        }
        return result;
    }

    /**
     * 获取完整路径
     * @param filePath 文件路径
     */
    public String getCompletePath(String filePath){
        if (!StrUtil.isBlank(filePath)){
            return this.resourcesUrl + "/" + filePath;
        }
        return "";
    }

    /**
     * 移除文件前缀
     * @param filePath 文件路径
     */
    public String removeEndPoint(String filePath){
        if (!StrUtil.isBlank(filePath)){
            return filePath.replaceAll(this.resourcesUrl + "/","");
        }
        return filePath;
    }

}
