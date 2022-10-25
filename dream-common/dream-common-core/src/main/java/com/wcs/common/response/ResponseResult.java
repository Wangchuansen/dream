package com.wcs.common.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author: wcs
 * @create: 2022-08-12 16:53
 * @description:
 */
public class ResponseResult<T> implements Serializable {

	private static final Logger log = LoggerFactory.getLogger(ResponseResult.class);

	/**
	 * 状态码
	 */
	private String code;

	/**
	 * 信息
	 */
	private String msg;

	/**
	 * 数据
	 */
	private T data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "ServerResponseEntity{" + "code=" + code + ", msg='" + msg + '\'' + ", data=" + data + '}';
	}

	public boolean isSuccess() {
		return Objects.equals(ResponseEnum.OK.value(), this.code);
	}

	public static <T> ResponseResult<T> success(T data) {
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setData(data);
		responseResult.setCode(ResponseEnum.OK.value());
		return responseResult;
	}

	public static <T> ResponseResult<T> success() {
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setCode(ResponseEnum.OK.value());
		responseResult.setMsg(ResponseEnum.OK.getMsg());
		return responseResult;
	}

	/**
	 * 请求成功
	 * @param responseEnum
	 */
	public static <T> ResponseResult<T> success(ResponseEnum responseEnum) {
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setCode(responseEnum.value());
		responseResult.setMsg(responseEnum.getMsg());
		return responseResult;
	}

	/**
	 * 前端显示失败消息
	 * @param msg 失败消息
	 * @return
	 */
	public static <T> ResponseResult<T> showFailMsg(String msg) {
		log.error(msg);
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setMsg(msg);
		responseResult.setCode(ResponseEnum.SHOW_FAIL.value());
		return responseResult;
	}

	public static <T> ResponseResult<T> fail(ResponseEnum responseEnum) {
		log.error(responseEnum.toString());
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setMsg(responseEnum.getMsg());
		responseResult.setCode(responseEnum.value());
		return responseResult;
	}

	public static <T> ResponseResult<T> fail(ResponseEnum responseEnum, T data) {
		log.error(responseEnum.toString());
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setMsg(responseEnum.getMsg());
		responseResult.setCode(responseEnum.value());
		responseResult.setData(data);
		return responseResult;
	}

	public static <T> ResponseResult<T> transform(ResponseResult<?> oldResponseResult) {
		ResponseResult<T> responseResult = new ResponseResult<>();
		responseResult.setMsg(oldResponseResult.getMsg());
		responseResult.setCode(oldResponseResult.getCode());
		log.error(responseResult.toString());
		return responseResult;
	}

}
