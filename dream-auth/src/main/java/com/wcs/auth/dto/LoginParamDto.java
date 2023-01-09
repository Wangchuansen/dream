package com.wcs.auth.dto;

/**
 * @author: wcs
 * @create: 2023-01-09 9:20
 * @description:
 */

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 登录参数
 */
@Data
public class LoginParamDto {

    /**手机号*/
    @NotBlank(message = "手机号不能为空")
    private String mobile;
    /**验证码*/
    @NotBlank(message = "验证码不能为空")
    private String code;
    /**邀请人userId*/
    private String invitationUserId;
    /**
     * sysType 参考SysTypeEnum 系统类型 0.小程序 1.pc
     */
    @NotNull(message = "sysType不能为空")
    protected Integer sysType;
}
