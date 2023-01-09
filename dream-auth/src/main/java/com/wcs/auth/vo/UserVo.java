package com.wcs.auth.vo;

import com.wcs.auth.model.User;
import lombok.Data;

/**
 * @author: wcs
 * @create: 2023-01-09 9:24
 * @description:
 */
@Data
public class UserVo extends User {
    /**企业id*/
    private Long companyId;
    /**企业类型 0 ISV 1 运营*/
    private Integer companyType;

}
