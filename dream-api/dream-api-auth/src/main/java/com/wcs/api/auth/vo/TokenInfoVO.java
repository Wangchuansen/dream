package com.wcs.api.auth.vo;

import lombok.Data;
import lombok.ToString;

/**
 * token信息，该信息用户返回给前端，前端请求携带accessToken进行用户校验
 *
 * @author hxm
 * @date 2020/7/2
 */
@Data
@ToString
public class TokenInfoVO {

	private String accessToken;

	private String refreshToken;

	//在多少秒后过期
	private Integer expiresIn;

}
