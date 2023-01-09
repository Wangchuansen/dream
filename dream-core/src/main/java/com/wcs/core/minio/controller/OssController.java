package com.wcs.core.minio.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.wcs.common.config.OssConfig;
import com.wcs.common.exception.GlobalException;
import com.wcs.common.response.ResponseResult;
import com.wcs.core.minio.config.MinioUtil;
import com.wcs.core.minio.vo.OssVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

/**
 * @author: wcs
 * @create: 2023-01-09 10:01
 * @description:
 */
@RequestMapping(value = "/oss")
@RestController
@Slf4j
public class OssController {

    @Autowired
    private OssConfig ossConfig;
    @Autowired
    private MinioUtil minioUtil;
    /**
     * 上传的文件夹(根据时间确定)
     */
    public static final String NORM_DAY_PATTERN = "yyyy/MM/dd";

    /**
     * 上传文件，返回文件路径与域名
     */
    @PostMapping("/upload_minio")
    public ResponseResult<OssVO> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() ==0) {
            throw new GlobalException("您上传的文件为空");
        }
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        OssVO oss = loadOssVO(new OssVO(),suffix);
        minioUtil.uploadMinio(ossConfig.getBucket(),file.getInputStream(),oss.getDir() + oss.getFileName(), file.getContentType());
//        FileRecord fileRecord = FileRecord.builder().fileName(fileName).filePath(oss.getDir() + oss.getFileName()).build();
//        fileRecordService.insert(fileRecord);

        return ResponseResult.success(oss);
    }
    /**
     * 设置文件名
     * @param ossVo
     * @return
     */
    private OssVO loadOssVO(OssVO ossVo,String suffix) {
        String dir = DateUtil.format(new Date(), NORM_DAY_PATTERN)+ "/";
        String fileName = IdUtil.simpleUUID();
        ossVo.setDir(dir);
        ossVo.setFileName(fileName+suffix);
        ossVo.setPath(dir+fileName+suffix);
        ossVo.setCompletePath(ossConfig.getCompletePath(ossVo.getPath()));
        return ossVo;
    }
}
