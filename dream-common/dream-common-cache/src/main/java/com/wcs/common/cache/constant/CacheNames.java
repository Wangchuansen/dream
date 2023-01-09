package com.wcs.common.cache.constant;

/**
 * @author: wcs
 * @create: 2023-01-09 9:38
 * @description:
 */
public interface CacheNames extends RbacCacheNames,OauthCacheNames, MiniprogramCacheNames {
    /**
     *
     * 参考CacheKeyPrefix
     * cacheNames 与 key 之间的默认连接字符
     */
    String UNION = "::";

    /**
     * key内部的连接字符（自定义）
     */
    String UNION_KEY = ":";

}
