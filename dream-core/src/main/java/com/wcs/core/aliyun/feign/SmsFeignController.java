package com.wcs.core.aliyun.feign;

import cn.hutool.core.util.StrUtil;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.wcs.api.core.feign.aliyun.SmsFeignClient;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import com.wcs.core.aliyun.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: wcs
 * @create: 2023-01-09 9:05
 * @description:
 */
@Slf4j
@RestController
public class SmsFeignController implements SmsFeignClient {

    @Autowired
    private SmsConfig smsConfig;
    /**
     * 发送短信
     * @param templateId 模板id
     * @param signName 签名
     * @param mobile 手机号
     * @param contentMapStr 短信内容
     */
    @Override
    public ResponseResult<Object> sendSms(@RequestParam("templateId")Integer templateId, @RequestParam("signName")String signName,
                                          @RequestParam("mobile")String mobile, @RequestParam("contentMapStr")String contentMapStr) {
        String templateCode = smsConfig.getTemplate().get(String.valueOf(templateId));
//		log.info("模板编号----->:"+templateCode);
        if (StrUtil.isNotEmpty(templateCode)) {
            SendSmsRequest sendSmsRequest = new SendSmsRequest();
            sendSmsRequest.setSignName(smsConfig.getSign());
            sendSmsRequest.setTemplateCode(templateCode);
            sendSmsRequest.setPhoneNumbers(mobile);
            sendSmsRequest.setTemplateParam(contentMapStr);

            RuntimeOptions runtime = new RuntimeOptions();
            try {
                // 复制代码运行请自行打印 API 的返回值
                SendSmsResponse sendSmsResponse = smsConfig.getSmsClient().sendSmsWithOptions(sendSmsRequest, runtime);
//				log.info("短信调用结果----->:"+ JSONObject.toJSONString(sendSmsResponse));
            } catch (TeaException error) {
                // 如有需要，请打印 error
                com.aliyun.teautil.Common.assertAsString(error.message);
                log.error(error.message);
                return ResponseResult.fail(ResponseEnum.SEND_MSG_ERROR);
            } catch (Exception _error) {
                TeaException error = new TeaException(_error.getMessage(), _error);
                // 如有需要，请打印 error
                com.aliyun.teautil.Common.assertAsString(error.message);
                log.error(error.getMessage());
                return ResponseResult.fail(ResponseEnum.SEND_MSG_ERROR);
            }
        }
        return ResponseResult.success(ResponseEnum.SEND_MSG_SUCCESS);
    }

}
