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

        // ???????????????????????????????????????????????????????????????????????????
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

        // ??????token????????????????????????
        ResponseResult<UserInfoInTokenBO> userInfoInTokenVoResponseResult = tokenFeignClient
                .checkToken(accessToken);
        if (!userInfoInTokenVoResponseResult.isSuccess()) {
//			httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED));
            httpHandler.printServerResponseToWeb(userInfoInTokenVoResponseResult,resp);
            return;
        }

        UserInfoInTokenBO userInfoInToken = userInfoInTokenVoResponseResult.getData();

        // ?????????????????????????????????????????????????????????????????????
//		if (!checkRbac(userInfoInToken,req.getRequestURI(), req.getMethod())) {
//			httpHandler.printServerResponseToWeb(ResponseResult.fail(ResponseEnum.UNAUTHORIZED));
//			return;
//		}

        try {
            // ???????????????
            AuthUserContext.set(userInfoInToken);

            chain.doFilter(req, resp);
        }
        finally {
            AuthUserContext.clean();
        }

    }

    private boolean feignRequestCheck(HttpServletRequest req) {
        // ??????feign?????????????????????
        if (!req.getRequestURI().startsWith(FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX)) {
            return true;
        }
        String feignInsideSecret = req.getHeader(feignInsideAuthConfig.getKey());

        // ??????feign ???????????????key ??? value????????????
        if (StrUtil.isBlank(feignInsideSecret) || !Objects.equals(feignInsideSecret,feignInsideAuthConfig.getSecret())) {
            return false;
        }
        // ip?????????
        List<String> ips = feignInsideAuthConfig.getIps();
        // ??????????????????ip
        ips.removeIf(StrUtil::isBlank);
        // ???ip???????????????ip?????????????????????????????????
        if (CollectionUtil.isNotEmpty(ips)
                && !ips.contains(IpHelper.getIpAddr())) {
            logger.error("ip not in ip White list: {}, ip, {}", ips, IpHelper.getIpAddr());
            return false;
        }
        return true;
    }

    /**
     * ????????????????????????
     * @param uri uri
     * @return ??????????????????
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

