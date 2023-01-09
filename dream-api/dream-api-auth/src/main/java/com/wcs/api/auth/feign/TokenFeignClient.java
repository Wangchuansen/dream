package com.wcs.api.auth.feign;

import com.wcs.api.auth.bo.UserInfoInTokenBO;
import com.wcs.common.constant.Auth;
import com.wcs.common.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author: wcs
 * @create: 2023-01-09 9:44
 * @description:
 */
@FeignClient(value = "dream-auth",contextId ="token")
public interface TokenFeignClient {

    /**
     * 校验token并返回token保存的用户信息
     * @param accessToken accessToken
     * @return token保存的用户信息
     */
    @GetMapping(value = Auth.CHECK_TOKEN_URI)
    ResponseResult<UserInfoInTokenBO> checkToken(@RequestParam("accessToken") String accessToken);

}
