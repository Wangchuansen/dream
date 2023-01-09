package com.wcs.common.security.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.wcs.api.auth.bo.UserInfoInTokenBO;
import com.wcs.api.auth.constant.HttpMethodEnum;
import com.wcs.api.auth.constant.SysTypeEnum;
import com.wcs.api.auth.feign.PermissionFeignClient;
import com.wcs.api.auth.feign.TokenFeignClient;
import com.wcs.common.constant.Auth;
import com.wcs.common.feign.FeignInsideAuthConfig;
import com.wcs.common.hander.HttpHandler;
import com.wcs.common.response.ResponseEnum;
import com.wcs.common.response.ResponseResult;
import com.wcs.common.security.AuthUserContext;
import com.wcs.common.security.adapter.AuthConfigAdapter;
import com.wcs.common.util.IpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author: wcs
 * @create: 2023-01-09 9:41
 * @description:
 */
@Component
public class AuthFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Autowired
    private AuthConfigAdapter authConfigAdapter;

    @Autowired
    private HttpHandler httpHandler;

    @Autowired
    private TokenFeignClient tokenFeignClient;

    @Autowired
    private PermissionFeignClient permissionFeignClient;

    @Autowired
    private FeignInsideAuthConfig feignInsideAuthConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!feignRequestCheck(req)) {
            httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED),resp);
            return;
        }

        if (Auth.CHECK_TOKEN_URI.equals(req.getRequestURI())) {
            chain.doFilter(req, resp);
            return;
        }


        List<String> excludePathPatterns = authConfigAdapter.excludePathPatterns();

        // 如果匹配不需要授权的路径，就不需要校验是否需要授权
        if (CollectionUtil.isNotEmpty(excludePathPatterns)) {
            for (String excludePathPattern : excludePathPatterns) {
                AntPathMatcher pathMatcher = new AntPathMatcher();
                if (pathMatcher.match(excludePathPattern, req.getRequestURI())) {
                    chain.doFilter(req, resp);
                    return;
                }
            }
        }

        String accessToken = req.getHeader("Authorization");

        if (StrUtil.isBlank(accessToken)) {
            httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED),resp);
            return;
        }

        // 校验token，并返回用户信息
        ResponseResult<UserInfoInTokenBO> userInfoInTokenVoResponseResult = tokenFeignClient
                .checkToken(accessToken);
        if (!userInfoInTokenVoResponseResult.isSuccess()) {
//			httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED));
            httpHandler.printServerResponseToWeb(userInfoInTokenVoResponseResult,resp);
            return;
        }

        UserInfoInTokenBO userInfoInToken = userInfoInTokenVoResponseResult.getData();

        // 需要用户角色权限，就去根据用户角色权限判断是否
//		if (!checkRbac(userInfoInToken,req.getRequestURI(), req.getMethod())) {
//			httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED));
//			return;
//		}

        try {
            // 保存上下文
            AuthUserContext.set(userInfoInToken);

            chain.doFilter(req, resp);
        }
        finally {
            AuthUserContext.clean();
        }

    }

    private boolean feignRequestCheck(HttpServletRequest req) {
        // 不是feign请求，不用校验
        if (!req.getRequestURI().startsWith(FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX)) {
            return true;
        }
        String feignInsideSecret = req.getHeader(feignInsideAuthConfig.getKey());

        // 校验feign 请求携带的key 和 value是否正确
        if (StrUtil.isBlank(feignInsideSecret) || !Objects.equals(feignInsideSecret,feignInsideAuthConfig.getSecret())) {
            return false;
        }
        // ip白名单
        List<String> ips = feignInsideAuthConfig.getIps();
        // 移除无用的空ip
        ips.removeIf(StrUtil::isBlank);
        // 有ip白名单，且ip不在白名单内，校验失败
        if (CollectionUtil.isNotEmpty(ips)
                && !ips.contains(IpHelper.getIpAddr())) {
            logger.error("ip not in ip White list: {}, ip, {}", ips, IpHelper.getIpAddr());
            return false;
        }
        return true;
    }

    /**
     * 用户角色权限校验
     * @param uri uri
     * @return 是否校验成功
     */
    public boolean checkRbac(UserInfoInTokenBO userInfoInToken, String uri, String method) {

        if (!Objects.equals(SysTypeEnum.PCMANAGER.value(), userInfoInToken.getSysType()) && !Objects.equals(SysTypeEnum.PCMANAGER.value(), userInfoInToken.getSysType())) {
            return true;
        }

        ResponseResult<Boolean> booleanResponseResult = permissionFeignClient
                .checkPermission(userInfoInToken.getUserId(), userInfoInToken.getSysType(),uri,null, HttpMethodEnum.valueOf(method.toUpperCase()).value() );

        if (!booleanResponseResult.isSuccess()) {
            return false;
        }

        return booleanResponseResult.getData();
    }

}

