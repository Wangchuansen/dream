package com.wcs.common.response;

/**
 * @author: wcs
 * @create: 2022-08-12 16:53
 * @description:
 */
public enum ResponseEnum {/**
 * ok
 */
OK("200", "ok"),

    /**
     *
     */
    EDIT_SUCCESS("200", "操作成功"),

    /**
     * 用于直接显示提示用户的错误，内容由输入内容决定
     */
    SHOW_FAIL("A00001", ""),

    /**
     * 方法参数没有校验，内容由输入内容决定
     */
    METHOD_ARGUMENT_NOT_VALID("A00002", ""),

    /**
     * 无法读取获取请求参数
     */
    HTTP_MESSAGE_NOT_READABLE("A00003", "请求参数格式有误"),

    /**
     * 未授权
     */
    UNAUTHORIZED("A00004", "Unauthorized"),

    /**
     * 服务器出了点小差
     */
    EXCEPTION("A00005", "服务器出了点小差"),

    /**
     * 数据异常，请刷新后重新操作
     */
    DATA_ERROR("A00007", "数据异常，请刷新后重新操作"),

    /**
     * 一些需要登录的接口，而实际上因为前端无法知道token是否已过期，导致token已失效时，
     * 应该返回一个状态码，告诉前端token已经失效了，及时清理
     */
    CLEAN_TOKEN("A00008", "clean token"),

    /**
     * 请求token已过期
     */
    ACCESS_TOKEN_EXPIRED("554", "access token expired"),
    /**
     * 刷新token已过期
     */
    REFRESH_TOKEN_EXPIRED("555", "refresh token expired"),

    /**
     * token已过期
     */
    TOKEN_EXPIRED("556", "token expired"),

    /**
     * 数据不完整
     */
    DATA_INCOMPLETE("A00010", "数据不完整"),

    /**
     * 微信登录获取授权信息失败
     */
    CODE_NOT_REGISTER("A04004", "获取授权信息失败"),
    /**
     * 短信验证码错误
     */
    CODE_PHONE_MSG_ERROR("A04005", "验证码错误"),
    /**
     * 发送短信失败
     */
    SEND_MSG_ERROR("A04006", "发送短信失败"),
    /**
     * 图形验证失败
     */
    CAPTCH_CHECK_ERROR("A04007", "图形验证失败");

    private final String code;

    private final String msg;

    public String value() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    ResponseEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseEnum{" + "code='" + code + '\'' + ", msg='" + msg + '\'' + "} " + super.toString();
    }

}
