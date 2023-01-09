package com.wcs.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wcs.api.core.feign.aliyun.SmsFeignClient;
import com.wcs.auth.dto.CaptchaDTO;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * @author: wcs
 * @create: 2022-08-23 10:19
 * @description: 图片验证
 */
@Slf4j
@RestController
@RequestMapping("/ua/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SmsFeignClient smsFeignClient;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping({ "/get" })
    public ResponseResult<ResponseModel> get(@RequestBody CaptchaVO captchaVO) {
        return ResponseResult.success(captchaService.get(captchaVO));
    }

    @PostMapping({ "/check" })
    public ResponseResult<Object> check(@Valid @RequestBody CaptchaDTO captchaDTO) {

        ResponseModel check_result = captchaService.check(captchaDTO.getCaptchaVO());
        //图形验证成功
        if (RepCodeEnum.SUCCESS.getCode().equals(check_result.getRepCode())){
            //获取随机四位数
            String code = String.valueOf(Math.round((Math.random()+1) * 1000));
            log.info("{}验证码：{}",captchaDTO.getMobile(),code);
            int seconds = 60*10;
            Long expire = redisTemplate.opsForValue().getOperations().getExpire("mobile_login" + captchaDTO.getMobile());
            if (expire == -1 || (seconds -expire) <60){
                return ResponseResult.success(ResponseEnum.SEND_MSG_SUCCESS);
            }
            JSONObject param = new JSONObject();
            param.put("code",code);
            ResponseResult<Object> responseResult = smsFeignClient.sendSms(1, "", captchaDTO.getMobile(), param.toJSONString());
            redisTemplate.opsForValue().set("mobile_login"+captchaDTO.getMobile(),code,seconds, TimeUnit.SECONDS);
            return responseResult;
        }

        return ResponseResult.fail(ResponseEnum.CAPTCH_CHECK_ERROR);
    }

}
