package com.wcs.common.cache.constant;

/**
 * @author: wcs
 * @create: 2023-01-09 9:38
 * @description:
 */
public interface RbacCacheNames {


    /**
     * 前缀
     */
    String RBAC_PREFIX = "njydzq_auth:";

    /**
     * 所有权限列表缓存key
     */
    String PERMISSIONS_KEY = RBAC_PREFIX + RBAC_PREFIX + "permission:permissions:";

    /**
     * 用户拥有的权限列表缓存key
     */
    String USER_PERMISSIONS_KEY = RBAC_PREFIX + "permission:user_permissions:";

    /**
     * uri对应的权限缓存key
     */
    String URI_PERMISSION_KEY = RBAC_PREFIX + "permission:uri_permissions:";

    /**
     * uri对应的权限缓存key
     */
    String MENU_LIST_KEY = RBAC_PREFIX + "menu:list:";

    /**
     * 菜单id key
     */
    String MENU_ID_LIST_KEY = RBAC_PREFIX + "menu:id_list:";

}
