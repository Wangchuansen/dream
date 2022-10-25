package com.wcs.auth.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: wcs
 * @create: 2022-08-13 10:42
 * @description:
 */
@Data
@Builder
public class User {
    /**用户id*/
    private Long userId;
    /**手机号*/
    private String mobile;
    /**微信openid*/
    private String openid;
    /**密码*/
    private String password;
    /**用户名*/
    private String name;
    /**性别id*/
    private Integer genderId;
    /**地址*/
    private String address;
    /**邮箱*/
    private String email;
    /**对应小程序编号*/
    private Integer program;
    /**头像地址*/
    private String headPath;
    /**创建时间*/
    private LocalDateTime createTime;
    /**编辑时间*/
    private LocalDateTime updateTime;
    private Integer userType;
    private String postName;
}
