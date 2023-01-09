package com.wcs.auth.controller;

import com.wcs.api.auth.vo.LoginInfoVo;
import com.wcs.auth.dto.LoginParamDto;
import com.wcs.auth.model.User;
import com.wcs.auth.service.LoginService;
import com.wcs.common.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

/**
 * @author: wcs
 * @create: 2022-08-12 16:24
 * @description:
 */
@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/user/login")
    public ResponseResult<LoginInfoVo> login(@Valid LoginParamDto loginParamDto) {
        return loginService.loginMiniMobile(loginParamDto);
    }

//    @GetMapping("/user/info")
//    public ResponseResult<User> userInfo(String token) {
//        return loginService.userInfo();
//    }

    @PostMapping("/user/logout")
    public ResponseResult userLogout() {
        return ResponseResult.success();
    }
}
