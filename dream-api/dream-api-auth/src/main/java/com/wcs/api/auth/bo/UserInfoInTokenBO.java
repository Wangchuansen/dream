package com.wcs.api.auth.bo;

import lombok.*;

/**
 * @author: wcs
 * @create: 2023-01-09 9:21
 * @description:
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoInTokenBO {

    /**
     * 用户在自己系统的用户id
     */
    private Long userId;
    /**
     * 系统类型
     * @see SysTypeEnum
     */
    private Integer sysType;
    /**
     * 企业id
     */
    private Long companyId;
    /**
     * 小程序 类型
     */
    private Integer program;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 企业类型
     */
    private Integer companyType;
}
