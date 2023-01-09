package com.wcs.api.auth.constant;

/**
 * @author: wcs
 * @create: 2023-01-09 9:22
 * @description:
 */
public enum SysTypeEnum {

    /**
     * 小程序
     */
    MINIPROGRAM(0),

    /**
     * pc管理后台
     */
    PCMANAGER(1),
    ;

    private final Integer value;

    public Integer value() {
        return value;
    }

    SysTypeEnum(Integer value) {
        this.value = value;
    }

}
