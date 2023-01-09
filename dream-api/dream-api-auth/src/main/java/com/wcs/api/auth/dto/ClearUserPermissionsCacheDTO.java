package com.wcs.api.auth.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * @author: wcs
 * @create: 2023-01-09 9:47
 * @description:
 */
@Data
@ToString
public class ClearUserPermissionsCacheDTO {

    /**
     * 用户id
     */
    @NotNull(message = "userId not null")
    private Long userId;

    /**
     * 系统类型
     */
    @NotNull(message = "sysType not null")
    private Integer sysType;

}
