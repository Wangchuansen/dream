package com.wcs.common.security.bo;

import lombok.Data;
import lombok.ToString;
import com.wcs.api.auth.bo.UserInfoInTokenBO;

/**
 * @author: wcs
 * @create: 2023-01-09 9:32
 * @description:
 */
@Data
@ToString
public class TokenInfoBO {

    /**
     * 保存在token信息里面的用户信息
     */
    private UserInfoInTokenBO userInfoInToken;

    private String accessToken;

    private String refreshToken;

    /**
     * 在多少秒后过期
     */
    private Integer accessExpiresIn;
    /**
     * refreshToken过期时间
     */
    private Integer refreshExpiresIn;

}
