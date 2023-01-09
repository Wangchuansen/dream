package com.wcs.auth.dto;

import com.anji.captcha.model.vo.CaptchaVO;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 图形验证拓展
 */
@Data
public class CaptchaDTO {

    /**手机号*/
    @NotBlank(message = "请填写手机号")
    private String mobile;

    /**图形滑动验证*/
    private CaptchaVO captchaVO;

}
