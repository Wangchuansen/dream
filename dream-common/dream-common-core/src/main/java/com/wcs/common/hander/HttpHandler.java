package com.wcs.common.hander;

import cn.hutool.core.util.CharsetUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcs.common.exception.GlobalException;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.xss.XssUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author: wcs
 * @create: 2023-01-09 9:42
 * @description:
 */
@Component
public class HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    @Autowired
    private ObjectMapper objectMapper;

    public <T> void printServerResponseToWeb(ResponseResult<T> responseResult, HttpServletResponse response) {
        if (responseResult == null) {
            logger.info("print obj is null");
            return;
        }

//		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
//				.getRequestAttributes();
//		if (requestAttributes == null) {
//			logger.error("requestAttributes is null, can not print to web");
//			return;
//		}
//		HttpServletResponse response = requestAttributes.getResponse();
//		if (response == null) {
//			logger.error("httpServletResponse is null, can not print to web");
//			return;
//		}
        logger.error("response error: " + responseResult.getMsg());
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(XssUtil.clean(objectMapper.writeValueAsString(responseResult)));
        }
        catch (IOException e) {
            throw new GlobalException("io 异常", e);
        }
    }

}
