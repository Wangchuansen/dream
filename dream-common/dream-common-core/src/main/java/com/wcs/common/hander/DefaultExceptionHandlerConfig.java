package com.wcs.common.hander;

import cn.hutool.core.util.StrUtil;
import com.wcs.common.exception.GlobalException;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: wcs
 * @create: 2023-01-09 9:43
 * @description:
 */
@RestController
@RestControllerAdvice
public class DefaultExceptionHandlerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandlerConfig.class);

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ResponseResult<List<String>>> methodArgumentNotValidExceptionHandler(Exception e) {
        logger.error("methodArgumentNotValidExceptionHandler", e);
        List<FieldError> fieldErrors = null;
        if (e instanceof MethodArgumentNotValidException) {
            fieldErrors = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors();
        }
        if (e instanceof BindException) {
            fieldErrors = ((BindException) e).getBindingResult().getFieldErrors();
        }
        if (fieldErrors == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseResult.fail(ResponseEnum.METHOD_ARGUMENT_NOT_VALID));
        }

//		List<String> defaultMessages = new ArrayList<>(fieldErrors.size());
        String defaultMessages = "";
        for (FieldError fieldError : fieldErrors) {
//			defaultMessages.add(fieldError.getField() + ":" + fieldError.getDefaultMessage());
//			defaultMessages.add(fieldError.getDefaultMessage());
            //提示第一条
            defaultMessages = fieldError.getDefaultMessage();
            break;
        }
        return ResponseEntity.status(HttpStatus.OK)
//				.body(ResponseResult.fail(ResponseEnum.METHOD_ARGUMENT_NOT_VALID, defaultMessages));
                .body(ResponseResult.showFailMsg(defaultMessages));
    }

    @ExceptionHandler({ HttpMessageNotReadableException.class })
    public ResponseEntity<ResponseResult<List<FieldError>>> methodArgumentNotValidExceptionHandler(
            HttpMessageNotReadableException e) {
        logger.error("methodArgumentNotValidExceptionHandler", e);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseResult.fail(ResponseEnum.HTTP_MESSAGE_NOT_READABLE));
    }

    /**
     * 自定义异常
     * @param e
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ResponseResult<Object>> globalExceptionHandler(GlobalException e) {
        ResponseEnum responseEnum = e.getResponseEnum();
        // 失败返回失败消息 + 状态码
        if (responseEnum != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ResponseResult.fail(responseEnum, e.getObject()));
        }
        // 失败返回消息 状态码固定为直接显示消息的状态码
        return ResponseEntity.status(HttpStatus.OK).body(ResponseResult.showFailMsg(e.getMessage()));
    }

    /**
     * 客户端连接关闭
     * @param e
     * @return
     */
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<ResponseResult<Object>> clientAbortExceptionHandler(HttpServletRequest request, ClientAbortException e) {
        logger.error("异常关闭----->:[{}]",request.getRequestURI());
        e.printStackTrace();
        // 失败返回消息 状态码固定为直接显示消息的状态码
        return ResponseEntity.status(HttpStatus.OK).body(ResponseResult.showFailMsg("操作太快啦"));
    }


    /**
     * 参数解析异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseResult<Object>> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e)  throws TransactionException {
        logger.error("exceptionHandler", e);
        logger.info("RootContext.getXID(): " + RootContext.getXID());
        if (StrUtil.isNotBlank(RootContext.getXID())) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        // 失败返回消息 状态码固定为直接显示消息的状态码
        return ResponseEntity.status(HttpStatus.OK).body(ResponseResult.showFailMsg("参数解析异常"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Object>> exceptionHandler(Exception e) throws TransactionException {
        logger.error("exceptionHandler", e);
        logger.info("RootContext.getXID(): " + RootContext.getXID());
        if (StrUtil.isNotBlank(RootContext.getXID())) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
        return ResponseEntity.status(HttpStatus.OK).body(ResponseResult.fail(ResponseEnum.EXCEPTION));
    }

//	/**
//	 * 打印参数
//	 * @param request
//	 */
//	private void consoleParam(HttpServletRequest request){
//		logger.error("uri ----------->:"+request.getRequestURI());
//		logger.error("servletPath ----------->:"+request.getServletPath());
//		logger.error("contentType ----------->:"+request.getContentType());
//
//		Map<String, String[]> parameterMap = request.getParameterMap();
//		for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
//			logger.error(stringEntry.getKey()+"----------->:"+stringEntry.getValue());
//		}
//	}

}

