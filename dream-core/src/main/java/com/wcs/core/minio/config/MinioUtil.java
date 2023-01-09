package com.wcs.core.minio.config;

import com.google.common.collect.HashMultimap;
import com.wcs.common.config.OssConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import io.minio.messages.Part;
import io.minio.messages.VersioningConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author: wcs
 * @create: 2023-01-09 10:02
 * @description:
 */
@Slf4j
@Component
public class MinioUtil {
    @Autowired
    private CustomMinioClient customMinioClient;
    @Autowired
    private OssConfig ossConfig;
    /**
     * 获取文件上传地址
     * 单文件上传
     * @param objectName 文件全路径名称
     */
    public String getPresignedObjectUrl(String bucketName, String objectName) {
        try {
            return customMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)//GET方式请求
                            .bucket(bucketName)//存储桶的名字
                            .object(objectName)//文件的名字
                            .expiry(ossConfig.getExpiry(), TimeUnit.HOURS)//上传地址有效时长
                            .build()
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *  创建分块任务
     * @param bucketName 存储桶名称
     * @param objectName 文件全路径名称
     * @param partCount 分片数量
     * @return /
     */
//    public FilePartVo initMultipartUpload(String bucketName, String objectName, int partCount, String contentType) {
//        FilePartVo result = new FilePartVo();
//        try {
//            //如果类型使用默认流会导致无法预览
////            contentType = "application/octet-stream";
//
//            HashMultimap<String, String> headers = HashMultimap.create();
//            headers.put("Content-Type", contentType);
////            checkAsyncBucket(customMinioClient,false,bucketName);
//            String uploadId = customMinioClient.initMultiPartUpload(bucketName, null, objectName, headers, null);
//
//            result.setUploadId(uploadId);
//            List<String> partList = new ArrayList<>();
//
//            Map<String, String> reqParams = new HashMap<>();
//            reqParams.put("uploadId", uploadId);
//            for (int i = 1; i <= partCount; i++) {
//                reqParams.put("partNumber", String.valueOf(i));
//                String uploadUrl = customMinioClient.getPresignedObjectUrl(
//                        GetPresignedObjectUrlArgs.builder()
//                                .method(Method.PUT)
//                                .bucket(bucketName)
//                                .object(objectName)
//                                .expiry(ossConfig.getExpiry(), TimeUnit.HOURS)
//                                .extraQueryParams(reqParams)
//                                .build());
//                partList.add(uploadUrl);
//            }
//            result.setUploadUrls(partList);
//        } catch (Exception e) {
//            return null;
//        }
//
//        return result;
//    }

    /**
     * 检查是否存在指定桶 不存在则先创建
     * @param minioClient
     * @param versioning
     * @param bucket
     * @throws Exception
     */
//    private static void checkAsyncBucket(MinioAsyncClient minioClient ,boolean versioning, String bucket) throws Exception {
//
//        CompletableFuture<Boolean> exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
//        if (exists.isDone() && !exists.get()) {
//            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
//            //设置Procy属性 默认所有请求都能读取
//            String config = "{ " +
//                    "    \"Id\": \"Policy1\", " +
//                    "    \"Version\": \"2012-10-17\", " +
//                    "    \"Statement\": [ " +
//                    "        { " +
//                    "            \"Sid\": \"Statement1\", " +
//                    "            \"Effect\": \"Allow\", " +
//                    "            \"Action\": [ " +
//                    "                \"s3:ListBucket\", " +
//                    "                \"s3:GetObject\" " +
//                    "            ], " +
//                    "            \"Resource\": [ " +
//                    "                \"arn:aws:s3:::"+bucket+"\", " +
//                    "                \"arn:aws:s3:::"+bucket+"/*\" " +
//                    "            ]," +
//                    "            \"Principal\": \"*\"" +
//                    "        } " +
//                    "    ] " +
//                    "}";
//            minioClient.setBucketPolicy(
//                    SetBucketPolicyArgs.builder().bucket(bucket).config(config).build());
//        }
//        // 版本控制
//        CompletableFuture<VersioningConfiguration> configuration = minioClient.getBucketVersioning(GetBucketVersioningArgs.builder().bucket(bucket).build());
//        if(configuration.isDone()) {
//            boolean enabled = configuration.get().status() == VersioningConfiguration.Status.ENABLED;
//            if (versioning && !enabled) {
//                minioClient.setBucketVersioning(SetBucketVersioningArgs.builder()
//                        .config(new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, null)).bucket(bucket).build());
//            } else if (!versioning && enabled) {
//                minioClient.setBucketVersioning(SetBucketVersioningArgs.builder()
//                        .config(new VersioningConfiguration(VersioningConfiguration.Status.SUSPENDED, null)).bucket(bucket).build());
//            }
//        }
//    }

    /**
     * 获取指定 uploadId 下已上传的分块信息
     * @param bucketName
     * @param objectName
     * @param uploadId
     */
    public List<String> listMultipart(String bucketName, String objectName, String uploadId) {
        List<String> parts = new ArrayList<>();
        try {
            //最大分片1000
            ListPartsResponse partResult = customMinioClient.listMultipart(bucketName, null, objectName, 1024, 0, uploadId, null, null);
            for (Part part : partResult.result().partList()) {
                parts.add(part.etag());
            }
        } catch (Exception e) {
            log.error("查询任务分片错误");
        }
        return parts;
    }


    /**
     * 文件合并
     * @param bucketName
     * @param objectName
     * @param uploadId
     * @return
     */
    public boolean mergeMultipartUpload(String bucketName, String objectName, String uploadId,Integer partNum) {
        try {
            Part[] parts = new Part[partNum];
            /**最大分片1000*/
            ListPartsResponse partResult = customMinioClient.listMultipart(bucketName, null, objectName, partNum, 0, uploadId, null, null);
            int partNumber = 1;
            for (Part part : partResult.result().partList()) {
                parts[partNumber - 1] = new Part(partNumber, part.etag());
                partNumber++;
            }
            //合并分片
            customMinioClient.mergeMultipartUpload(bucketName, null, objectName, uploadId, parts, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 删除指定分片上传任务
     * @param bucketName
     * @param objectName
     * @param uploadId
     * @return
     */
    public boolean removeMultipartUpload(String bucketName, String objectName, String uploadId) {
        try {
            /**最大分片1000*/
            customMinioClient.removeMultipartUpload(bucketName,null,objectName,uploadId,null,null);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 流文件直接上传
     * @param input
     * @param filePath
     * @param contentType
     */
    public void uploadMinio(String bucket, InputStream input, String filePath, String contentType) throws IOException {
        try {
            customMinioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .contentType(contentType)
                            .stream(input, input.available(), -1)
                            .object(filePath)
                            .build()
            ).get();
        } catch (Exception e) {
            log.error("上传文件错误：", e);
        }finally {
            if (Objects.nonNull(input)) {
                input.close();
            }
        }
    }

    /**
     * 文件 上传
     * @param bucket
     * @param bytes
     * @param filePath
     * @param contentType
     */
    public void uploadMinio(String bucket,byte[] bytes, String filePath, String contentType) throws IOException {
        InputStream input = null;
        try {
            input = new ByteArrayInputStream(bytes);
            uploadMinio(bucket,input,filePath,contentType);
        } catch (Exception e) {
            log.error("minio上传文件错误：", e);
        } finally {
            if (Objects.nonNull(input)) {
                input.close();
            }
        }
    }

    /**
     * 删除文件
     * @param objectName 文件名称
     * @throws Exception https://docs.minio.io/cn/java-client-api-reference.html#removeObject
     */
    public void removeObject(String bucket,String objectName) throws Exception {
        customMinioClient.removeObject(RemoveObjectArgs.builder().object(objectName).bucket(bucket).build());
    }

    /**
     * 判断文件是否存在
     * @param bucketName 存储桶
     * @param objectName 文件名
     * @return
     */
    public boolean isObjectExist(String bucketName, String objectName) {
        boolean exist = true;
        try {
            customMinioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }

    /**
     * 判断文件夹是否存在
     * @param bucketName 存储桶
     * @param objectName 文件夹名称
     * @return
     */
    public boolean isFolderExist(String bucketName, String objectName) {
        boolean exist = false;
        try {
            Iterable<Result<Item>> results = customMinioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(objectName).recursive(false).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && objectName.equals(item.objectName())) {
                    exist = true;
                }
            }
        } catch (Exception e) {
            exist = false;
        }
        return exist;
    }
}

