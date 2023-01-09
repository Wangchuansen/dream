package com.wcs.api.auth.vo;

import lombok.Data;

/**
 * 登录信息
 */
@Data
public class LoginInfoVo extends TokenInfoVO{
    /**
     * 是否为新用户 0 否 1 是
     */
    private Integer isNewUser;
}
