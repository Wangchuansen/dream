package com.wcs.common.security.filter;

import com.wcs.common.xss.XssWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: wcs
 * @create: 2022-08-12 17:21
 * @description:
 */
@WebFilter(filterName = "xssFilter")
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(XssFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // replaceAll("[\r\n]" =》 Potential CRLF Injection for logs
        logger.info("AuthFilter RequestURI :{}", req.getRequestURI().replaceAll("[\r\n]",""));
        // xss 过滤
        chain.doFilter(new XssWrapper(req), resp);
    }
}