package com.wcs.common.constant;

import com.wcs.common.feign.FeignInsideAuthConfig;

/**
 * @author: wcs
 * @create: 2023-01-09 9:44
 * @description:
 */
public interface Auth {

    String CHECK_TOKEN_URI = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/token/checkToken";

    String CHECK_RBAC_URI = FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/permission/checkPermission";
}
