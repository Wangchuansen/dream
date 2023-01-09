package com.wcs.auth.service;

import com.wcs.api.auth.bo.UserInfoInTokenBO;
import com.wcs.api.auth.vo.LoginInfoVo;
import com.wcs.auth.dto.LoginParamDto;
import com.wcs.auth.model.User;
import com.wcs.common.response.ResponseResult;

import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-12 17:26
 * @description:
 */
public interface LoginService {
    /**
     * 小程序登录
     * @param phoneCode 微信获取手机号code
     */
//    ResponseResult<LoginInfoVo> loginMiniProgram(String phoneCode, String invitationUserId);

    /**
     * 手机号登录
     */
    ResponseResult<LoginInfoVo> loginMiniMobile(LoginParamDto loginParamDto);

    /**
     * 根据id获取用户信息
     * @param userId
     */
    UserInfoInTokenBO getUserByUseId(Long userId, Integer sysType);
}
