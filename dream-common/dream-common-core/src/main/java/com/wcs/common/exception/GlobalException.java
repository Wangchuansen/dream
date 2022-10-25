package com.wcs.common.exception;

import com.wcs.common.response.ResponseEnum;

/**
 * @author: wcs
 * @create: 2022-08-14 16:25
 * @description:
 */
public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Object object;

    private ResponseEnum responseEnum;

    public GlobalException(String msg) {
        super(msg);
    }

    public GlobalException(String msg, Object object) {
        super(msg);
        this.object = object;
    }

    public GlobalException(String msg, Throwable cause) {
        super(msg, cause);
    }


    public GlobalException(ResponseEnum responseEnum) {
        super(responseEnum.getMsg());
        this.responseEnum = responseEnum;
    }

    public GlobalException(ResponseEnum responseEnum, Object object) {
        super(responseEnum.getMsg());
        this.responseEnum = responseEnum;
        this.object = object;
    }


    public Object getObject() {
        return object;
    }

    public ResponseEnum getResponseEnum() {
        return responseEnum;
    }

}