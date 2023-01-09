package com.wcs.common.security;

import com.wcs.api.auth.bo.UserInfoInTokenBO;

/**
 * @author: wcs
 * @create: 2023-01-09 9:51
 * @description:
 */
public class AuthUserContext {

    /** The request holder. */
    private static final ThreadLocal<UserInfoInTokenBO> USER_INFO_IN_TOKEN_HOLDER = new ThreadLocal<>();

    public static UserInfoInTokenBO get() {
        return USER_INFO_IN_TOKEN_HOLDER.get();
    }


    public static UserInfoInTokenBO forceGet() {
        return USER_INFO_IN_TOKEN_HOLDER.get();
    }

    public static void set(UserInfoInTokenBO userInfoInTokenBo) {
        USER_INFO_IN_TOKEN_HOLDER.set(userInfoInTokenBo);
    }

    public static void clean() {
        if (USER_INFO_IN_TOKEN_HOLDER.get() != null) {
            USER_INFO_IN_TOKEN_HOLDER.remove();
        }
    }

}