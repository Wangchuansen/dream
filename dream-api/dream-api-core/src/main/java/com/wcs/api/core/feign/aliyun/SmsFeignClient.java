package com.wcs.api.core.feign.aliyun;

import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: wcs
 * @create: 2023-01-09 9:03
 * @description:
 */
@FeignClient(value = "dream-core",contextId = "sms")
public interface SmsFeignClient {

    /**
     * 发送短信
     * @param templateId 模板id
     * @param signName 签名
     * @param mobile 手机号
     * @param contentMapStr 短信内容
     */
    @PostMapping(value = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/sendSms")
    ResponseResult<Object> sendSms(@RequestParam("templateId")Integer templateId, @RequestParam("signName")String signName,
                                   @RequestParam("mobile")String mobile, @RequestParam("contentMapStr")String contentMapStr);
}
