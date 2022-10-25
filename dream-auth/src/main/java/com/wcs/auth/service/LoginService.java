package com.wcs.auth.service;

import com.wcs.auth.model.User;
import com.wcs.common.response.ResponseResult;

import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-12 17:26
 * @description:
 */
public interface LoginService {
    ResponseResult<Map<String,Object>> login(String mobile,String code);

    ResponseResult<User> userInfo();
}
